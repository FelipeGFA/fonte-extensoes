# Local Request And Decode Reference

This document explains how to reproduce the captured Sakura Mangas request values locally:

- `challenge`
- `proof`
- `_enc` decode

All steps below use only files already present in this workspace.

## Source Files

Standalone scripts:
- [decode_goal_enc.js](C:\Users\e1281432\Downloads\sakuramangas\scripts\decode_goal_enc.js)
- [inspect_manga_strings.js](C:\Users\e1281432\Downloads\sakuramangas\scripts\inspect_manga_strings.js)
- [rebuild_goal_request_values.js](C:\Users\e1281432\Downloads\sakuramangas\scripts\rebuild_goal_request_values.js)

## Quick Commands

Rebuild the request values:

```powershell
node scripts\rebuild_goal_request_values.js
```

Decode the `_enc` payload:

```powershell
node scripts\decode_goal_enc.js --goal goal.txt --out .omx\proofs\decode-goal-enc\decoded-goal.json
```

Inspect the packed manga string table:

```powershell
node scripts\inspect_manga_strings.js 49 54 60 99
```

## Local Inputs

From [details.html](C:\Users\e1281432\Downloads\sakuramangas\html\details.html):
- `header-challenge`
- `security-key-name`
- `csrf-token`
- `manga-id`

From [goal.txt](C:\Users\e1281432\Downloads\sakuramangas\goal.txt):
- expected `proof`
- expected `challenge`
- target `_enc`

Recovered runtime constants:
- `cipher_key = "SakuraKey"`
- `context_keys.manga_info = 8006199014741981`
- `context_keys.chapter_read = 9006099254140970`
- `context_keys.default = 8105199014741981`

User-agent string used by the standalone rebuild script for exact `proof` parity in this captured case:

```text
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36
```

## Challenge Generation

`challenge` is derived from the `header-challenge` hex string in [details.html](C:\Users\e1281432\Downloads\sakuramangas\html\details.html:40).

Algorithm:

1. Split the hex string at `Math.ceil(length / 2)`.
2. Parse byte pairs from the left half.
3. Parse byte pairs from the right half.
4. Interleave the two byte streams.
5. XOR the interleaved bytes with the repeating ASCII key `SakuraKey`.
6. Interpret the result as the final base64 challenge string.

Equivalent standalone logic:

```js
function buildChallenge(rawChallenge, cipherKey = "SakuraKey") {
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
```

Expected output for this workspace:

```text
MTkwLjE1LjEwOS4yNS80YjM1MGFjMDNjNWY1NGVkMDg4NDQyNDBmZTFmOTIxYmFiZmQ5OTU4LzVmMWNjNTA1OTA0NmMwYzdkOWNhMmFiNjk5NDNiMTRl
```

## Proof Generation

`proof` is generated from:

- decoded challenge field 1
- decoded challenge field 3
- the user-agent string above
- the numeric context seed for the current `security-key-name`

The middle decoded challenge field is ignored.

### Seed Table

First build the 256-byte table from the context seed:

```js
function buildSeedTable(seedValue) {
  let state = Number(BigInt(seedValue) & 255n) >>> 0;
  const output = new Uint8Array(256);

  for (let index = 0; index < output.length; index += 1) {
    state = (Math.imul(state, 1664525) + 1013904223) >>> 0;
    output[index] = (state & 255) ^ index;
  }

  return output;
}
```

### Proof Mixer

Recovered standalone proof recurrence:

```js
const INITIAL_PROOF_STATE = [
  1116352408, 1899447441, 3049323471, 3921009573,
  961987163, 1508970993, 2453635748, 2870763221,
  3624381080, 310598401, 607225278, 1426881987,
  1925078388, 2162078206, 2614888103, 3248222580
].map(value => value >>> 0);

function rotateLeft32(value, shift) {
  const amount = shift & 31;
  return ((value << amount) | (value >>> (32 - amount))) >>> 0;
}

function buildProof(challenge, seedValue, userAgent) {
  const decoded = Buffer.from(challenge, "base64").toString("utf8");
  const parts = decoded.split("/");
  if (parts.length !== 3) {
    return null;
  }

  const [field0, , field2] = parts;
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
```

Expected output for:
- `challenge = MTkwLjE1LjEwOS4yNS80YjM1MGFjMDNjNWY1NGVkMDg4NDQyNDBmZTFmOTIxYmFiZmQ5OTU4LzVmMWNjNTA1OTA0NmMwYzdkOWNhMmFiNjk5NDNiMTRl`
- `seed = 8006199014741981`
- the standalone rebuild-script UA above, which is consistent with the captured Chrome 147 Windows client hints

is:

```text
145a5b40aaf5b5aa457b98d7bd1404bb
```

## `_enc` Decode

The `_enc` payload decode path is separate from request generation.

Algorithm:

1. Extract `_enc` from [goal.txt](C:\Users\e1281432\Downloads\sakuramangas\goal.txt).
2. Base64-decode `_enc`.
3. XOR the bytes with the repeating ASCII key `SakuraCSS`.
4. Interpret the XOR result as another base64 string.
5. Base64-decode again.
6. JSON-parse the result.

Equivalent standalone logic:

```js
function decodeEnc(enc, key = "SakuraCSS") {
  const stage0 = Buffer.from(enc, "base64");
  const keyBytes = Buffer.from(key, "utf8");
  const stage1 = Buffer.alloc(stage0.length);

  for (let index = 0; index < stage0.length; index += 1) {
    stage1[index] = stage0[index] ^ keyBytes[index % keyBytes.length];
  }

  const stage2Base64 = stage1.toString("latin1");
  const jsonText = Buffer.from(stage2Base64, "base64").toString("utf8");
  return JSON.parse(jsonText);
}
```

Expected top-level keys in the decoded payload:

```text
success
has_more
total_groups
data
volumes_data
```

## Reproducible Local Workflow

1. Rebuild request values:

```powershell
node scripts\rebuild_goal_request_values.js > .omx\proofs\decode-goal-enc\rebuilt-request-values.json
```

2. Confirm exact request match in [rebuilt-request-values.json](C:\Users\e1281432\Downloads\sakuramangas\.omx\proofs\decode-goal-enc\rebuilt-request-values.json):
- `matches.proof = true`
- `matches.challenge = true`

3. Decode `_enc`:

```powershell
node scripts\decode_goal_enc.js --goal goal.txt --out .omx\proofs\decode-goal-enc\decoded-goal.json
```

4. Optionally inspect the packed helper strings:

```powershell
node scripts\inspect_manga_strings.js 49 54 60 99
```

## Notes

- The exact `proof` match depends on the user-agent string used by the standalone rebuild script.
- That user-agent string is consistent with the captured Chrome 147 Windows client hints in [goal.txt](C:\Users\e1281432\Downloads\sakuramangas\goal.txt:9).
- The implementation is now fully standalone for both request generation and `_enc` decoding.
- The packed runtime is no longer required to reproduce the captured `proof`, `challenge`, or decoded `_enc`.
