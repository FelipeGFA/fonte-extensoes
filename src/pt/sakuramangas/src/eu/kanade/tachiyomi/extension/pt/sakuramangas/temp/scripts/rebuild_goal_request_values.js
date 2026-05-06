const fs = require("fs");
const path = require("path");

const REPO_ROOT = path.resolve(__dirname, "..");
const DETAILS_HTML = path.join(REPO_ROOT, "html", "details.html");
const GOAL_FILE = path.join(REPO_ROOT, "goal.txt");

const CONTEXT_KEYS = {
  manga_info: 8006199014741981,
  chapter_read: 9006099254140970,
  default: 8105199014741981,
  cipher_key: "SakuraKey"
};

const DEFAULT_USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

const INITIAL_PROOF_STATE = [
  1116352408, 1899447441, 3049323471, 3921009573,
  961987163, 1508970993, 2453635748, 2870763221,
  3624381080, 310598401, 607225278, 1426881987,
  1925078388, 2162078206, 2614888103, 3248222580
].map(value => value >>> 0);

function extractMeta(html, pattern) {
  const match = html.match(pattern);
  if (!match) {
    throw new Error(`Could not match ${pattern}`);
  }
  return match[1];
}

function loadInputs() {
  const html = fs.readFileSync(DETAILS_HTML, "utf8");
  const goal = fs.readFileSync(GOAL_FILE, "utf8");
  const rawChallenge = extractMeta(html, /<meta name="header-challenge" content="([^"]+)"/i);
  const keyName = extractMeta(html, /<meta name="security-key-name" content="([^"]+)"/i);
  const expectedBody = extractMeta(goal, /"body":\s*"([^"]+)"/);
  const params = new URLSearchParams(expectedBody);

  return {
    rawChallenge,
    keyName,
    expectedProof: params.get("proof"),
    expectedChallenge: params.get("challenge")
  };
}

function buildChallenge(_context, rawChallenge) {
  const keyBytes = Buffer.from(CONTEXT_KEYS.cipher_key, "utf8");
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

function rotateLeft32(value, shift) {
  const amount = shift & 31;
  return ((value << amount) | (value >>> (32 - amount))) >>> 0;
}

function buildSeedTable(seedValue) {
  let state = Number(BigInt(seedValue) & 255n) >>> 0;
  const output = new Uint8Array(256);

  for (let index = 0; index < output.length; index += 1) {
    state = (Math.imul(state, 1664525) + 1013904223) >>> 0;
    output[index] = (state & 255) ^ index;
  }

  return output;
}

function buildProof(_context, challenge, keyName, userAgent) {
  const seed = CONTEXT_KEYS[keyName] ?? CONTEXT_KEYS.default;
  const decoded = Buffer.from(challenge, "base64").toString("utf8");
  const parts = decoded.split("/");

  if (parts.length !== 3) {
    return null;
  }

  const [field0, , field2] = parts;
  const lookupTable = buildSeedTable(seed);
  const stream = field0 + userAgent + String(seed) + field2;
  const state = INITIAL_PROOF_STATE.slice();
  let cursor = 0;
  let accumulator = 0;

  for (let index = 0; index < stream.length; index += 1) {
    const charCode = stream.charCodeAt(index);
    cursor = (cursor + charCode + accumulator) & 15;

    const mixedIndex = (state[cursor] ^ charCode) & 255;
    const mixedByte = lookupTable[mixedIndex];
    const mixedWord = (state[cursor] ^ Math.imul(state[(cursor + 1) & 15], mixedByte)) >>> 0;
    const rotated = rotateLeft32(mixedWord, mixedWord & 31);

    state[cursor] = rotated;
    accumulator = (accumulator + (rotated | 0)) | 0;
  }

  let output = "";
  for (let group = 0; group < 4; group += 1) {
    const offset = group * 4;
    const folded = (((state[offset] ^ state[offset + 1]) + state[offset + 2]) ^ state[offset + 3]) >>> 0;
    output += folded.toString(16).padStart(8, "0");
  }

  return output;
}

async function main() {
  const inputs = loadInputs();
  const challenge = buildChallenge(null, inputs.rawChallenge);
  const proof = buildProof(null, challenge, inputs.keyName, DEFAULT_USER_AGENT);
  const result = {
    rawChallenge: inputs.rawChallenge,
    keyName: inputs.keyName,
    cipher_key: CONTEXT_KEYS.cipher_key,
    contextSeed: CONTEXT_KEYS[inputs.keyName] ?? CONTEXT_KEYS.default,
    userAgent: DEFAULT_USER_AGENT,
    proof,
    challenge,
    body: `proof=${proof}&challenge=${challenge}`,
    matches: {
      proof: proof === inputs.expectedProof,
      challenge: challenge === inputs.expectedChallenge
    }
  };

  process.stdout.write(JSON.stringify(result, null, 2) + "\n");

  if (!result.matches.proof || !result.matches.challenge) {
    process.exitCode = 1;
  }
}

main().catch(error => {
  console.error(error instanceof Error ? error.stack : String(error));
  process.exitCode = 1;
});
