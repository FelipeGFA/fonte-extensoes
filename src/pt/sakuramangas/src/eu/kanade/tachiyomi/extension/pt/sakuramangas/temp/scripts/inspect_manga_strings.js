#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

const TYPE_NULL = 0;
const TYPE_UNDEFINED = 1;
const TYPE_FALSE = 2;
const TYPE_TRUE = 3;
const TYPE_I8 = 4;
const TYPE_I16 = 5;
const TYPE_I32 = 6;
const TYPE_F64 = 7;
const TYPE_STRING = 8;

const FLAG_1 = 1;
const FLAG_2 = 2;
const FLAG_3 = 4;
const FLAG_4 = 8;
const FLAG_5 = 32;
const FLAG_6 = 64;
const FLAG_7 = 128;
const FLAG_8 = 256;
const FLAG_9 = 512;
const FLAG_10 = 1024;
const FLAG_11 = 2048;
const FLAG_14 = 8192;
const FLAG_22 = 16384;
const FLAG_4B = 32768;
const FLAG_21 = 65536;
const FLAG_8B = 131072;
const FLAG_1B = 262144;
const FLAG_18 = 524288;

class Reader {
  constructor(bytes) {
    this.bytes = bytes;
    this.view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
    this.offset = 0;
  }

  u8() {
    return this.bytes[this.offset++];
  }

  u16() {
    const value = this.view.getUint16(this.offset, true);
    this.offset += 2;
    return value;
  }

  u32() {
    const value = this.view.getUint32(this.offset, true);
    this.offset += 4;
    return value;
  }

  i32() {
    const value = this.view.getInt32(this.offset, true);
    this.offset += 4;
    return value;
  }

  f64() {
    const value = this.view.getFloat64(this.offset, true);
    this.offset += 8;
    return value;
  }

  varint() {
    let value = 0;
    let shift = 0;
    let byte;

    do {
      byte = this.u8();
      value |= (byte & 0x7f) << shift;
      shift += 7;
    } while (byte >= 0x80);

    return (value >>> 1) ^ -(value & 1);
  }

  utf8String() {
    const length = this.varint();
    const slice = this.bytes.subarray(this.offset, this.offset + length);
    this.offset += length;
    return Buffer.from(slice).toString("utf8");
  }
}

const BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
const BASE64_MAP = new Uint8Array(128);
for (let i = 0; i < BASE64_ALPHABET.length; i += 1) {
  BASE64_MAP[BASE64_ALPHABET.charCodeAt(i)] = i;
}

function decodeBase64(text) {
  const padding =
    text.charCodeAt(text.length - 1) === 61
      ? text.charCodeAt(text.length - 2) === 61
        ? 2
        : 1
      : 0;
  const byteLength = ((text.length * 3) >> 2) - padding;
  const out = new Uint8Array(byteLength);
  let outIndex = 0;

  for (let i = 0; i < text.length; i += 4) {
    const a = BASE64_MAP[text.charCodeAt(i)];
    const b = BASE64_MAP[text.charCodeAt(i + 1)];
    const c = BASE64_MAP[text.charCodeAt(i + 2)];
    const d = BASE64_MAP[text.charCodeAt(i + 3)];

    out[outIndex++] = (a << 2) | (b >> 4);
    if (outIndex < byteLength) {
      out[outIndex++] = ((b & 0x0f) << 4) | (c >> 2);
    }
    if (outIndex < byteLength) {
      out[outIndex++] = ((c & 0x03) << 6) | d;
    }
  }

  return out;
}

function decodeConstant(reader) {
  const type = reader.u8();

  switch (type) {
    case TYPE_NULL:
      return null;
    case TYPE_UNDEFINED:
      return undefined;
    case TYPE_FALSE:
      return false;
    case TYPE_TRUE:
      return true;
    case TYPE_I8: {
      const value = reader.u8();
      return value > 127 ? value - 256 : value;
    }
    case TYPE_I16: {
      const value = reader.u16();
      return value > 32767 ? value - 65536 : value;
    }
    case TYPE_I32:
      return reader.i32();
    case TYPE_F64:
      return reader.f64();
    case TYPE_STRING:
      return reader.utf8String();
    default:
      return { unknownType: type };
  }
}

function decodeEntry(encoded) {
  const reader = new Reader(decodeBase64(encoded));
  const first = reader.u8();
  const flags = reader.u32();
  const p = reader.varint();
  const l = reader.varint();

  if (flags & FLAG_4) reader.varint();
  if (flags & FLAG_5) {
    const count = reader.varint();
    for (let i = 0; i < count; i += 1) {
      reader.varint();
      reader.varint();
    }
  }
  if (flags & FLAG_6) reader.u32();
  if (flags & FLAG_7) reader.u32();
  if (flags & FLAG_8) reader.u32();
  if (flags & FLAG_9) reader.varint();
  if (flags & FLAG_10) reader.u32();
  if (flags & FLAG_18) reader.varint();
  if (flags & FLAG_14) {
    // marker only
  }
  if (flags & FLAG_22) {
    // marker only
  }
  if (flags & FLAG_4B) {
    // marker only
  }
  if (flags & FLAG_21) {
    // marker only
  }
  if (flags & FLAG_8B) {
    // marker only
  }
  if (flags & FLAG_1B) {
    // marker only
  }

  const constantCount = reader.varint();
  const constants = [];
  for (let i = 0; i < constantCount; i += 1) {
    constants.push(decodeConstant(reader));
  }

  return { first, flags, p, l, constants };
}

function loadArray() {
  const file = path.resolve(process.cwd(), "javascript", "manga.v264w.core.js");
  const source = fs.readFileSync(file, "utf8");
  const match = source.match(/let G = \[(.*?)\];\s*let F =/s);
  if (!match) {
    throw new Error("Could not locate the packed string table.");
  }
  return eval("[" + match[1] + "]");
}

function main() {
  const table = loadArray();
  const args = process.argv.slice(2);
  const findIndex = args.indexOf("--find");
  const needle = findIndex >= 0 ? args[findIndex + 1] : null;
  const filteredArgs = findIndex >= 0 ? args.filter((_, index) => index !== findIndex && index !== findIndex + 1) : args;
  const indices = filteredArgs.map(value => Number(value)).filter(Number.isFinite);

  if (needle) {
    for (let index = 0; index < table.length; index += 1) {
      const entry = decodeEntry(table[index]);
      const strings = entry.constants.filter(value => typeof value === "string");
      if (strings.some(value => value.includes(needle))) {
        console.log(`IDX ${index}`);
        console.log(JSON.stringify({
          first: entry.first,
          flags: entry.flags,
          p: entry.p,
          l: entry.l,
          strings
        }, null, 2));
      }
    }
    return;
  }

  if (indices.length === 0) {
    throw new Error("Pass table indices or use --find <text>, for example: node scripts/inspect_manga_strings.js --find proof");
  }

  for (const index of indices) {
    const entry = decodeEntry(table[index]);
    console.log(`IDX ${index}`);
    console.log(JSON.stringify({
      first: entry.first,
      flags: entry.flags,
      p: entry.p,
      l: entry.l,
      strings: entry.constants.filter(value => typeof value === "string")
    }, null, 2));
  }
}

try {
  main();
} catch (error) {
  console.error(error instanceof Error ? error.message : String(error));
  process.exitCode = 1;
}
