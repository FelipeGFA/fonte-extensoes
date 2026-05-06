const USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:150.0) Gecko/20100101 Firefox/150.0";

const RAW_HEADER_CHALLENGE =
  "1e083e06352438552830253c11282e2338270a2e2b1a37092c28280a263c123b2e202f331e53280e202c38282b09252b06232c202c331e3d281e235c35460b56395821322d170f0a0f662f1a21140c0c1b1f07010878135132080f3b3c427911220225543e5f087b130c47501f3b3f080f140b1825120256";

const SECURITY_KEY_NAME = "manga_info";
const MANGA_ID = "91";
const OFFSET = "0";
const ORDER = "desc";
const LIMIT = "10";

const CIPHER_KEY = "SakuraKey";
const CONTEXT_KEYS = {
  manga_info: 8006199014741981n,
  chapter_read: 9006099254140970n,
  default: 8105199014741981n
};

const INITIAL_PROOF_STATE = [
  1116352408, 1899447441, 3049323471, 3921009573,
  961987163, 1508970993, 2453635748, 2870763221,
  3624381080, 310598401, 607225278, 1426881987,
  1925078388, 2162078206, 2614888103, 3248222580
].map(value => value >>> 0);

function buildChallenge(rawChallenge, cipherKey) {
  const keyBytes = Buffer.from(cipherKey, "utf8");
  const half = Math.ceil(rawChallenge.length / 2);
  const left = rawChallenge.slice(0, half);
  const right = rawChallenge.slice(half);
  const interleaved = [];

  for (let index = 0; index < Math.ceil(left.length / 2); index += 1) {
    const leftPair = left.slice(index * 2, index * 2 + 2);
    if (leftPair.length === 2) {
      interleaved.push(parseInt(leftPair, 16));
    }

    const rightPair = right.slice(index * 2, index * 2 + 2);
    if (rightPair.length === 2) {
      interleaved.push(parseInt(rightPair, 16));
    }
  }

  let output = "";
  for (let index = 0; index < interleaved.length; index += 1) {
    output += String.fromCharCode(interleaved[index] ^ keyBytes[index % keyBytes.length]);
  }

  return output;
}

function buildSeedTable(seedValue) {
  let state = Number(seedValue & 255n) >>> 0;
  const output = new Uint8Array(256);

  for (let index = 0; index < output.length; index += 1) {
    state = (Math.imul(state, 1664525) + 1013904223) >>> 0;
    output[index] = (state & 255) ^ index;
  }

  return output;
}

function rotateLeft32(value, shift) {
  const amount = shift & 31;
  return ((value << amount) | (value >>> (32 - amount))) >>> 0;
}

function buildProof(challenge, seedValue, userAgent) {
  const decoded = Buffer.from(challenge, "base64").toString("utf8");
  const parts = decoded.split("/");
  const field0 = parts[0];
  const field2 = parts[2];
  const table = buildSeedTable(seedValue);
  const stream = field0 + userAgent + String(seedValue) + field2;
  const state = INITIAL_PROOF_STATE.slice();
  let cursor = 0;
  let accumulator = 0;

  for (let index = 0; index < stream.length; index += 1) {
    const charCode = stream.charCodeAt(index);
    cursor = (cursor + charCode + accumulator) & 15;

    const mixedIndex = (state[cursor] ^ charCode) & 255;
    const mixedByte = table[mixedIndex];
    const mixedWord = (state[cursor] ^ Math.imul(state[(cursor + 1) & 15], mixedByte)) >>> 0;

    state[cursor] = rotateLeft32(mixedWord, mixedWord & 31);
    accumulator = (accumulator + (state[cursor] | 0)) | 0;
  }

  let output = "";
  for (let group = 0; group < 4; group += 1) {
    const offset = group * 4;
    const folded = (((state[offset] ^ state[offset + 1]) + state[offset + 2]) ^ state[offset + 3]) >>> 0;
    output += folded.toString(16).padStart(8, "0");
  }

  return output;
}

const contextSeed = CONTEXT_KEYS[SECURITY_KEY_NAME] ?? CONTEXT_KEYS.default;
const challenge = buildChallenge(RAW_HEADER_CHALLENGE, CIPHER_KEY);
const proof = buildProof(challenge, contextSeed, USER_AGENT);
const body = `manga_id=${MANGA_ID}&offset=${OFFSET}&order=${ORDER}&limit=${LIMIT}&proof=${proof}&challenge=${challenge}`;

console.log({ proof, challenge, body });
