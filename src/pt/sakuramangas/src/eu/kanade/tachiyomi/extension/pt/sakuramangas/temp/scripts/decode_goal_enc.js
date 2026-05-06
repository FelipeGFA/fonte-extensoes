const fs = require("fs");
const path = require("path");

const repoRoot = path.resolve(__dirname, "..");

function extractEnc(goalText) {
  const match = goalText.match(/"_enc":\s*"([\s\S]*?)"\s*}/);
  if (!match) {
    throw new Error("Could not find `_enc` in goal text.");
  }
  return match[1];
}

function xorBuffer(buffer, key) {
  const keyBuffer = Buffer.from(key, "utf8");
  const output = Buffer.alloc(buffer.length);

  for (let index = 0; index < buffer.length; index += 1) {
    output[index] = buffer[index] ^ keyBuffer[index % keyBuffer.length];
  }

  return output;
}

function decodeEnc(enc, key = "SakuraCSS") {
  const stage0 = Buffer.from(enc, "base64");
  const stage1 = xorBuffer(stage0, key).toString("latin1");
  const stage2 = Buffer.from(stage1, "base64").toString("utf8");
  const parsed = JSON.parse(stage2);

  return {
    key,
    stage0Length: stage0.length,
    stage1Preview: stage1.slice(0, 120),
    parsed
  };
}

function parseArgs(argv) {
  const options = {
    goalPath: path.join(repoRoot, "goal.txt"),
    outPath: null
  };
  const positionals = [];

  for (let index = 0; index < argv.length; index += 1) {
    const current = argv[index];
    if (current === "--goal" && argv[index + 1]) {
      options.goalPath = path.resolve(argv[index + 1]);
      index += 1;
    } else if (current === "--out" && argv[index + 1]) {
      options.outPath = path.resolve(argv[index + 1]);
      index += 1;
    } else {
      positionals.push(current);
    }
  }

  if (positionals[0]) {
    options.goalPath = path.resolve(positionals[0]);
  }

  if (positionals[1]) {
    options.outPath = path.resolve(positionals[1]);
  }

  return options;
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  const goalText = fs.readFileSync(options.goalPath, "utf8");
  const enc = extractEnc(goalText);
  const result = decodeEnc(enc);
  const output = JSON.stringify(result.parsed, null, 2);

  if (options.outPath) {
    fs.writeFileSync(options.outPath, output);
  }

  process.stdout.write(`${output}\n`);
}

main();
