function e() {
  try {
    return typeof indexedDB == "object";
  } catch (e3) {
    return false;
  }
}
function t() {
  return new Promise((p6, p7) => {
    try {
      let v15 = true;
      const vLSValidatebrowserconte = "validate-browser-context-for-indexeddb-analytics-module";
      const v16 = self.indexedDB.open(vLSValidatebrowserconte);
      v16.onsuccess = () => {
        v16.result.close();
        if (!v15) {
          self.indexedDB.deleteDatabase(vLSValidatebrowserconte);
        }
        p6(true);
      };
      v16.onupgradeneeded = () => {
        v15 = false;
      };
      v16.onerror = () => {
        p7(v16.error?.message || "");
      };
    } catch (e4) {
      p7(e4);
    }
  });
}
function n(p8, p9) {
  if (p8 === p9) {
    return true;
  }
  const v17 = Object.keys(p8);
  const v18 = Object.keys(p9);
  for (const v19 of v17) {
    if (!v18.includes(v19)) {
      return false;
    }
    const v20 = p8[v19];
    const v21 = p9[v19];
    if (i(v20) && i(v21)) {
      if (!n(v20, v21)) {
        return false;
      }
    } else if (v20 !== v21) {
      return false;
    }
  }
  for (const v22 of v18) {
    if (!v17.includes(v22)) {
      return false;
    }
  }
  return true;
}
function i(p10) {
  return p10 !== null && typeof p10 == "object";
}
/**
 * @license
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function r(p11) {
  if (p11 && p11._delegate) {
    return p11._delegate;
  } else {
    return p11;
  }
}
function a(p12) {
  if (p12 instanceof IDBRequest) {
    return function (p13) {
      const v23 = new Promise((p14, p15) => {
        const vF4 = () => {
          p13.removeEventListener("success", vF5);
          p13.removeEventListener("error", vF6);
        };
        const vF5 = () => {
          p14(a(p13.result));
          vF4();
        };
        const vF6 = () => {
          p15(p13.error);
          vF4();
        };
        p13.addEventListener("success", vF5);
        p13.addEventListener("error", vF6);
      });
      v23.then(p16 => {
        if (p16 instanceof IDBCursor) {
          De.set(p16, p13);
        }
      }).catch(() => {});
      Oe.set(v23, p13);
      return v23;
    }(p12);
  }
  if (Ae.has(p12)) {
    return Ae.get(p12);
  }
  const vF7 = function (p17) {
    if (typeof p17 == "function") {
      if ((v26 = p17) !== IDBDatabase.prototype.transaction || "objectStoreNames" in IDBTransaction.prototype) {
        if ((Ce ||= [IDBCursor.prototype.advance, IDBCursor.prototype.continue, IDBCursor.prototype.continuePrimaryKey]).includes(v26)) {
          return function (..._0x424c80) {
            v26.apply(Pe(this), _0x424c80);
            return a(De.get(this));
          };
        } else {
          return function (..._0x41923f) {
            return a(v26.apply(Pe(this), _0x41923f));
          };
        }
      } else {
        return function (p18, ..._0x2a0e81) {
          const v24 = v26.call(Pe(this), p18, ..._0x2a0e81);
          Te.set(v24, p18.sort ? p18.sort() : [p18]);
          return a(v24);
        };
      }
    } else {
      if (p17 instanceof IDBTransaction) {
        (function (p19) {
          if (ke.has(p19)) {
            return;
          }
          const v25 = new Promise((p20, p21) => {
            const vF8 = () => {
              p19.removeEventListener("complete", vF9);
              p19.removeEventListener("error", vF10);
              p19.removeEventListener("abort", vF10);
            };
            const vF9 = () => {
              p20();
              vF8();
            };
            const vF10 = () => {
              p21(p19.error || new DOMException("AbortError", "AbortError"));
              vF8();
            };
            p19.addEventListener("complete", vF9);
            p19.addEventListener("error", vF10);
            p19.addEventListener("abort", vF10);
          });
          ke.set(p19, v25);
        })(p17);
      }
      v27 = p17;
      if ((_e ||= [IDBDatabase, IDBObjectStore, IDBIndex, IDBCursor, IDBTransaction]).some(p22 => v27 instanceof p22)) {
        return new Proxy(p17, Ne);
      } else {
        return p17;
      }
    }
    var v26;
    var v27;
  }(p12);
  if (vF7 !== p12) {
    Ae.set(p12, vF7);
    Oe.set(vF7, p12);
  }
  return vF7;
}
function o(p23, p24, {
  blocked: _0xcbc863,
  upgrade: _0x4b4947,
  blocking: _0x469b65,
  terminated: _0x4838ab
} = {}) {
  const v28 = indexedDB.open(p23, p24);
  const vA2 = a(v28);
  if (_0x4b4947) {
    v28.addEventListener("upgradeneeded", p25 => {
      _0x4b4947(a(v28.result), p25.oldVersion, p25.newVersion, a(v28.transaction), p25);
    });
  }
  if (_0xcbc863) {
    v28.addEventListener("blocked", p26 => _0xcbc863(p26.oldVersion, p26.newVersion, p26));
  }
  vA2.then(p27 => {
    if (_0x4838ab) {
      p27.addEventListener("close", () => _0x4838ab());
    }
    if (_0x469b65) {
      p27.addEventListener("versionchange", p28 => _0x469b65(p28.oldVersion, p28.newVersion, p28));
    }
  }).catch(() => {});
  return vA2;
}
function s(p29, {
  blocked: _0x1d1e15
} = {}) {
  const v29 = indexedDB.deleteDatabase(p29);
  if (_0x1d1e15) {
    v29.addEventListener("blocked", p30 => _0x1d1e15(p30.oldVersion, p30));
  }
  return a(v29).then(() => {});
}
function c(p31, p32) {
  if (!(p31 instanceof IDBDatabase) || p32 in p31 || typeof p32 != "string") {
    return;
  }
  if (je.get(p32)) {
    return je.get(p32);
  }
  const v30 = p32.replace(/FromIndex$/, "");
  const v31 = p32 !== v30;
  const v32 = Be.includes(v30);
  if (!(v30 in (v31 ? IDBIndex : IDBObjectStore).prototype) || !v32 && !Me.includes(v30)) {
    return;
  }
  const vF11 = async function (p33, ..._0x2ef2e9) {
    const v33 = this.transaction(p33, v32 ? "readwrite" : "readonly");
    let v34 = v33.store;
    if (v31) {
      v34 = v34.index(_0x2ef2e9.shift());
    }
    return (await Promise.all([v34[v30](..._0x2ef2e9), v32 && v33.done]))[0];
  };
  je.set(p32, vF11);
  return vF11;
}
function d(p34, p35) {
  try {
    p34.container.addComponent(p35);
  } catch (e5) {
    Fe.debug("Component " + p35.name + " failed to register with FirebaseApp " + p34.name, e5);
  }
}
function l(p36) {
  const v35 = p36.name;
  if (bt.has(v35)) {
    Fe.debug("There were multiple attempts to register component " + v35 + ".");
    return false;
  }
  bt.set(v35, p36);
  for (const v36 of gt.values()) {
    d(v36, p36);
  }
  for (const v37 of mt.values()) {
    d(v37, p36);
  }
  return true;
}
function u(p37, p38) {
  const v38 = p37.container.getProvider("heartbeat").getImmediate({
    optional: true
  });
  if (v38) {
    v38.triggerHeartbeat();
  }
  return p37.container.getProvider(p38);
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function p(p39, p40 = {}) {
  let vP39 = p39;
  if (typeof p40 != "object") {
    p40 = {
      name: p40
    };
  }
  const v39 = Object.assign({
    name: ht,
    automaticDataCollectionEnabled: false
  }, p40);
  const v40 = v39.name;
  if (typeof v40 != "string" || !v40) {
    throw wt.create("bad-app-name", {
      appName: String(v40)
    });
  }
  vP39 ||= de();
  if (!vP39) {
    throw wt.create("no-options");
  }
  const v41 = gt.get(v40);
  if (v41) {
    if (n(vP39, v41.options) && n(v39, v41.config)) {
      return v41;
    }
    throw wt.create("duplicate-app", {
      appName: v40
    });
  }
  const v42 = new be(v40);
  for (const v43 of bt.values()) {
    v42.addComponent(v43);
  }
  const v44 = new yt(vP39, v39, v42);
  gt.set(v40, v44);
  return v44;
}
function h(p41, p42, p43) {
  let v45 = ft[p41] ?? p41;
  if (p43) {
    v45 += "-" + p43;
  }
  const v46 = v45.match(/\s|\//);
  const v47 = p42.match(/\s|\//);
  if (v46 || v47) {
    const vA3 = ["Unable to register library \"" + v45 + "\" with version \"" + p42 + "\":"];
    if (v46) {
      vA3.push("library name \"" + v45 + "\" contains illegal characters (whitespace or \"/\")");
    }
    if (v46 && v47) {
      vA3.push("and");
    }
    if (v47) {
      vA3.push("version name \"" + p42 + "\" contains illegal characters (whitespace or \"/\")");
    }
    Fe.warn(vA3.join(" "));
    return;
  }
  l(new fe(v45 + "-version", () => ({
    library: v45,
    version: p42
  }), "VERSION"));
}
/**
 * @license
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function f() {
  It ||= o("firebase-heartbeat-database", 1, {
    upgrade: (p44, p45) => {
      if (p45 === 0) {
        try {
          p44.createObjectStore(vt);
        } catch (e6) {}
      }
    }
  }).catch(p46 => {
    throw wt.create("idb-open", {
      originalErrorMessage: p46.message
    });
  });
  return It;
}
async function g(p47, p48) {
  try {
    const v48 = (await f()).transaction(vt, "readwrite");
    const v49 = v48.objectStore(vt);
    await v49.put(p48, m(p47));
    await v48.done;
  } catch (e7) {
    if (e7 instanceof ue) {
      Fe.warn(e7.message);
    } else {
      const v50 = wt.create("idb-set", {
        originalErrorMessage: e7 == null ? undefined : e7.message
      });
      Fe.warn(v50.message);
    }
  }
}
function m(p49) {
  return p49.name + "!" + p49.options.appId;
}
/**
 * @license
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function b() {
  return new Date().toISOString().substring(0, 10);
}
function w(p50) {
  return ce(JSON.stringify({
    version: 2,
    heartbeats: p50
  })).length;
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function y(p51) {
  return p51 instanceof ue && p51.code.includes("request-failed");
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function v({
  projectId: _0x557da1
}) {
  return "https://firebaseinstallations.googleapis.com/v1/projects/" + _0x557da1 + "/installations";
}
function I(p52) {
  return {
    token: p52.token,
    requestStatus: 2,
    expiresIn: (v51 = p52.expiresIn, Number(v51.replace("s", "000"))),
    creationTime: Date.now()
  };
  var v51;
}
async function S(p53, p54) {
  const v52 = (await p54.json()).error;
  return At.create("request-failed", {
    requestName: p53,
    serverCode: v52.code,
    serverMessage: v52.message,
    serverStatus: v52.status
  });
}
function E({
  apiKey: _0x5787a5
}) {
  return new Headers({
    "Content-Type": "application/json",
    Accept: "application/json",
    "x-goog-api-key": _0x5787a5
  });
}
async function _(p55) {
  const v53 = await p55();
  if (v53.status >= 500 && v53.status < 600) {
    return p55();
  } else {
    return v53;
  }
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function C(p56) {
  return new Promise(p57 => {
    setTimeout(p57, p56);
  });
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function D() {
  try {
    const v54 = new Uint8Array(17);
    (self.crypto || self.msCrypto).getRandomValues(v54);
    v54[0] = 112 + v54[0] % 16;
    const vF12 = function (p58) {
      var v55;
      return (v55 = p58, btoa(String.fromCharCode(...v55)).replace(/\+/g, "-").replace(/\//g, "_")).substr(0, 22);
    }(
    /**
     * @license
     * Copyright 2019 Google LLC
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *   http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    v54);
    if (Ot.test(vF12)) {
      return vF12;
    } else {
      return "";
    }
  } catch (e8) {
    return "";
  }
}
function k(p59) {
  return p59.appName + "!" + p59.appId;
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function T(p60, p61) {
  const vK = k(p60);
  A(vK, p61);
  (function (p62, p63) {
    if (!Pt && "BroadcastChannel" in self) {
      Pt = new BroadcastChannel("[Firebase] FID Change");
      Pt.onmessage = p64 => {
        A(p64.data.key, p64.data.fid);
      };
    }
    const vPt = Pt;
    if (vPt) {
      vPt.postMessage({
        key: p62,
        fid: p63
      });
    }
    if (Nt.size === 0 && Pt) {
      Pt.close();
      Pt = null;
    }
  })(vK, p61);
}
function A(p65, p66) {
  const v56 = Nt.get(p65);
  if (v56) {
    for (const v57 of v56) {
      v57(p66);
    }
  }
}
function O() {
  Bt ||= o("firebase-installations-database", 1, {
    upgrade: (p67, p68) => {
      if (p68 === 0) {
        p67.createObjectStore(Mt);
      }
    }
  });
  return Bt;
}
async function N(p69, p70) {
  const vK2 = k(p69);
  const v58 = (await O()).transaction(Mt, "readwrite");
  const v59 = v58.objectStore(Mt);
  const v60 = await v59.get(vK2);
  await v59.put(p70, vK2);
  await v58.done;
  if (!v60 || v60.fid !== p70.fid) {
    T(p69, p70.fid);
  }
  return p70;
}
async function P(p71) {
  const vK3 = k(p71);
  const v61 = (await O()).transaction(Mt, "readwrite");
  await v61.objectStore(Mt).delete(vK3);
  await v61.done;
}
async function M(p72, p73) {
  const vK4 = k(p72);
  const v62 = (await O()).transaction(Mt, "readwrite");
  const v63 = v62.objectStore(Mt);
  const v64 = await v63.get(vK4);
  const vP73 = p73(v64);
  if (vP73 === undefined) {
    await v63.delete(vK4);
  } else {
    await v63.put(vP73, vK4);
  }
  await v62.done;
  if (!!vP73 && (!v64 || v64.fid !== vP73.fid)) {
    T(p72, vP73.fid);
  }
  return vP73;
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
async function B(p74) {
  let v65;
  const v66 = await M(p74.appConfig, p75 => {
    const vF13 = function (p76) {
      return R(p76 || {
        fid: D(),
        registrationStatus: 0
      });
    }(p75);
    const vF14 = function (p77, p78) {
      if (p78.registrationStatus === 0) {
        if (!navigator.onLine) {
          return {
            installationEntry: p78,
            registrationPromise: Promise.reject(At.create("app-offline"))
          };
        }
        const vO = {
          fid: p78.fid,
          registrationStatus: 1,
          registrationTime: Date.now()
        };
        const vF15 = async function (p79, p80) {
          try {
            const v67 =
            /**
             * @license
             * Copyright 2019 Google LLC
             *
             * Licensed under the Apache License, Version 2.0 (the "License");
             * you may not use this file except in compliance with the License.
             * You may obtain a copy of the License at
             *
             *   http://www.apache.org/licenses/LICENSE-2.0
             *
             * Unless required by applicable law or agreed to in writing, software
             * distributed under the License is distributed on an "AS IS" BASIS,
             * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
             * See the License for the specific language governing permissions and
             * limitations under the License.
             */
            await async function ({
              appConfig: _0x4810db,
              heartbeatServiceProvider: _0x532b7c
            }, {
              fid: _0x5f1322
            }) {
              const vV = v(_0x4810db);
              const vE = E(_0x4810db);
              const v68 = _0x532b7c.getImmediate({
                optional: true
              });
              if (v68) {
                const v69 = await v68.getHeartbeatsHeader();
                if (v69) {
                  vE.append("x-firebase-client", v69);
                }
              }
              const vO2 = {
                fid: _0x5f1322,
                authVersion: Tt,
                appId: _0x4810db.appId,
                sdkVersion: kt
              };
              const vO3 = {
                method: "POST",
                headers: vE,
                body: JSON.stringify(vO2)
              };
              const v70 = await _(() => fetch(vV, vO3));
              if (v70.ok) {
                const v71 = await v70.json();
                return {
                  fid: v71.fid || _0x5f1322,
                  registrationStatus: 2,
                  refreshToken: v71.refreshToken,
                  authToken: I(v71.authToken)
                };
              }
              throw await S("Create Installation", v70);
            }(p79, p80);
            return N(p79.appConfig, v67);
          } catch (e9) {
            if (y(e9) && e9.customData.serverCode === 409) {
              await P(p79.appConfig);
            } else {
              await N(p79.appConfig, {
                fid: p80.fid,
                registrationStatus: 0
              });
            }
            throw e9;
          }
        }(p77, vO);
        return {
          installationEntry: vO,
          registrationPromise: vF15
        };
      }
      if (p78.registrationStatus === 1) {
        return {
          installationEntry: p78,
          registrationPromise: j(p77)
        };
      } else {
        return {
          installationEntry: p78
        };
      }
    }(p74, vF13);
    v65 = vF14.registrationPromise;
    return vF14.installationEntry;
  });
  if (v66.fid === "") {
    return {
      installationEntry: await v65
    };
  } else {
    return {
      installationEntry: v66,
      registrationPromise: v65
    };
  }
}
async function j(p81) {
  let v72 = await L(p81.appConfig);
  while (v72.registrationStatus === 1) {
    await C(100);
    v72 = await L(p81.appConfig);
  }
  if (v72.registrationStatus === 0) {
    const {
      installationEntry: _0x1a8f4f,
      registrationPromise: _0x1a61e3
    } = await B(p81);
    return _0x1a61e3 || _0x1a8f4f;
  }
  return v72;
}
function L(p82) {
  return M(p82, p83 => {
    if (!p83) {
      throw At.create("installation-not-found");
    }
    return R(p83);
  });
}
function R(p84) {
  if ((v73 = p84).registrationStatus === 1 && v73.registrationTime + Dt < Date.now()) {
    return {
      fid: p84.fid,
      registrationStatus: 0
    };
  } else {
    return p84;
  }
  var v73;
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
async function $(p85, p86 = false) {
  let v74;
  const v75 = await M(p85.appConfig, p87 => {
    if (!F(p87)) {
      throw At.create("not-registered");
    }
    const v76 = p87.authToken;
    if (!p86 && (v78 = v76).requestStatus === 2 && !function (p88) {
      const v77 = Date.now();
      return v77 < p88.creationTime || p88.creationTime + p88.expiresIn < v77 + 3600000;
    }(v78)) {
      return p87;
    }
    var v78;
    if (v76.requestStatus === 1) {
      v74 = async function (p89, p90) {
        let v79 = await H(p89.appConfig);
        while (v79.authToken.requestStatus === 1) {
          await C(100);
          v79 = await H(p89.appConfig);
        }
        const v80 = v79.authToken;
        if (v80.requestStatus === 0) {
          return $(p89, p90);
        } else {
          return v80;
        }
      }(p85, p86);
      return p87;
    }
    {
      if (!navigator.onLine) {
        throw At.create("app-offline");
      }
      const vF16 = function (p91) {
        const vO4 = {
          requestStatus: 1,
          requestTime: Date.now()
        };
        return Object.assign(Object.assign({}, p91), {
          authToken: vO4
        });
      }(p87);
      v74 = async function (p92, p93) {
        try {
          const v81 = await async function ({
            appConfig: _0x386a6d,
            heartbeatServiceProvider: _0x132069
          }, p94) {
            const vF17 = function (p95, {
              fid: _0x49cb92
            }) {
              return v(p95) + "/" + _0x49cb92 + "/authTokens:generate";
            }(_0x386a6d, p94);
            const vF18 = function (p96, {
              refreshToken: _0x279689
            }) {
              const vE2 = E(p96);
              vE2.append("Authorization", function (p97) {
                return Tt + " " + p97;
              }(_0x279689));
              return vE2;
            }(_0x386a6d, p94);
            const v82 = _0x132069.getImmediate({
              optional: true
            });
            if (v82) {
              const v83 = await v82.getHeartbeatsHeader();
              if (v83) {
                vF18.append("x-firebase-client", v83);
              }
            }
            const vO5 = {
              installation: {
                sdkVersion: kt,
                appId: _0x386a6d.appId
              }
            };
            const vO6 = {
              method: "POST",
              headers: vF18,
              body: JSON.stringify(vO5)
            };
            const v84 = await _(() => fetch(vF17, vO6));
            if (v84.ok) {
              return I(await v84.json());
            }
            throw await S("Generate Auth Token", v84);
          }(p92, p93);
          const v85 = Object.assign(Object.assign({}, p93), {
            authToken: v81
          });
          await N(p92.appConfig, v85);
          return v81;
        } catch (e10) {
          if (!y(e10) || e10.customData.serverCode !== 401 && e10.customData.serverCode !== 404) {
            const v86 = Object.assign(Object.assign({}, p93), {
              authToken: {
                requestStatus: 0
              }
            });
            await N(p92.appConfig, v86);
          } else {
            await P(p92.appConfig);
          }
          throw e10;
        }
      }(p85, vF16);
      return vF16;
    }
  });
  if (v74) {
    return await v74;
  } else {
    return v75.authToken;
  }
}
function H(p98) {
  return M(p98, p99 => {
    if (!F(p99)) {
      throw At.create("not-registered");
    }
    if ((v87 = p99.authToken).requestStatus === 1 && v87.requestTime + Dt < Date.now()) {
      return Object.assign(Object.assign({}, p99), {
        authToken: {
          requestStatus: 0
        }
      });
    } else {
      return p99;
    }
    var v87;
  });
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function F(p100) {
  return p100 !== undefined && p100.registrationStatus === 2;
}
function x(p101) {
  return At.create("missing-app-config-values", {
    valueName: p101
  });
}
/**
 * @license
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @license
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function K(p102) {
  const v88 = new Uint8Array(p102);
  return btoa(String.fromCharCode(...v88)).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
}
function V(p103) {
  const v89 = (p103 + "=".repeat((4 - p103.length % 4) % 4)).replace(/\-/g, "+").replace(/_/g, "/");
  const vAtob = atob(v89);
  const v90 = new Uint8Array(vAtob.length);
  for (let vLN02 = 0; vLN02 < vAtob.length; ++vLN02) {
    v90[vLN02] = vAtob.charCodeAt(vLN02);
  }
  return v90;
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function W() {
  Wt ||= o("firebase-messaging-database", 1, {
    upgrade: (p104, p105) => {
      if (p105 === 0) {
        p104.createObjectStore(Vt);
      }
    }
  });
  return Wt;
}
async function U(p106, p107) {
  const vZ = z(p106);
  const v91 = (await W()).transaction(Vt, "readwrite");
  await v91.objectStore(Vt).put(p107, vZ);
  await v91.done;
  return p107;
}
function z({
  appConfig: _0x1b4e05
}) {
  return _0x1b4e05.appId;
}
/**
 * @license
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function q({
  projectId: _0x357a67
}) {
  return "https://fcmregistrations.googleapis.com/v1/projects/" + _0x357a67 + "/registrations";
}
async function G({
  appConfig: _0x5e7df5,
  installations: _0x3433f7
}) {
  const v92 = await _0x3433f7.getToken();
  return new Headers({
    "Content-Type": "application/json",
    Accept: "application/json",
    "x-goog-api-key": _0x5e7df5.apiKey,
    "x-goog-firebase-installations-auth": "FIS " + v92
  });
}
function J({
  p256dh: _0x569bf4,
  auth: _0x2f9b5b,
  endpoint: _0x457dfb,
  vapidKey: _0x136bcd
}) {
  const vO7 = {
    web: {
      endpoint: _0x457dfb,
      auth: _0x2f9b5b,
      p256dh: _0x569bf4
    }
  };
  if (_0x136bcd !== Lt) {
    vO7.web.applicationPubKey = _0x136bcd;
  }
  return vO7;
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
async function Y(p108, p109) {
  const v93 =
  /**
   * @license
   * Copyright 2019 Google LLC
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  await async function (p110, p111) {
    const v94 = await G(p110);
    const vJ = J(p111);
    const vO8 = {
      method: "POST",
      headers: v94,
      body: JSON.stringify(vJ)
    };
    let v95;
    try {
      const v96 = await fetch(q(p110.appConfig), vO8);
      v95 = await v96.json();
    } catch (e11) {
      throw Ut.create("token-subscribe-failed", {
        errorInfo: e11 == null ? undefined : e11.toString()
      });
    }
    if (v95.error) {
      const v97 = v95.error.message;
      throw Ut.create("token-subscribe-failed", {
        errorInfo: v97
      });
    }
    if (!v95.token) {
      throw Ut.create("token-subscribe-no-token");
    }
    return v95.token;
  }(p108, p109);
  const vO9 = {
    token: v93,
    createTime: Date.now(),
    subscriptionOptions: p109
  };
  await U(p108, vO9);
  return vO9.token;
}
/**
 * @license
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function Z(p112) {
  const vO10 = {
    from: p112.from,
    collapseKey: p112.collapse_key,
    messageId: p112.fcmMessageId
  };
  (function (p113, p114) {
    if (!p114.notification) {
      return;
    }
    p113.notification = {};
    const v98 = p114.notification.title;
    if (v98) {
      p113.notification.title = v98;
    }
    const v99 = p114.notification.body;
    if (v99) {
      p113.notification.body = v99;
    }
    const v100 = p114.notification.image;
    if (v100) {
      p113.notification.image = v100;
    }
    const v101 = p114.notification.icon;
    if (v101) {
      p113.notification.icon = v101;
    }
  })(vO10, p112);
  (function (p115, p116) {
    if (p116.data) {
      p115.data = p116.data;
    }
  })(vO10, p112);
  (function (p117, p118) {
    if (!p118.fcmOptions && !p118.notification?.click_action) {
      return;
    }
    p117.fcmOptions = {};
    const v102 = p118.fcmOptions?.link ?? p118.notification?.click_action;
    if (v102) {
      p117.fcmOptions.link = v102;
    }
    const v103 = p118.fcmOptions?.analytics_label;
    if (v103) {
      p117.fcmOptions.analyticsLabel = v103;
    }
  })(
  /**
   * @license
   * Copyright 2019 Google LLC
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  vO10, p112);
  return vO10;
}
function X(p119) {
  return Ut.create("missing-app-config-values", {
    valueName: p119
  });
}
/**
 * @license
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @license
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
async function Q(p120, p121) {
  if (!navigator) {
    throw Ut.create("only-available-in-window");
  }
  if (Notification.permission === "default") {
    await Notification.requestPermission();
  }
  if (Notification.permission !== "granted") {
    throw Ut.create("permission-blocked");
  }
  await async function (p122, p123) {
    if (p123) {
      p122.vapidKey = p123;
    } else {
      p122.vapidKey ||= Lt;
    }
  }(p120, p121 == null ? undefined : p121.vapidKey);
  /**
   * @license
   * Copyright 2020 Google LLC
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  await async function (p124, p125) {
    if (!p125 && !p124.swRegistration) {
      /**
       * @license
       * Copyright 2020 Google LLC
       *
       * Licensed under the Apache License, Version 2.0 (the "License");
       * you may not use this file except in compliance with the License.
       * You may obtain a copy of the License at
       *
       *   http://www.apache.org/licenses/LICENSE-2.0
       *
       * Unless required by applicable law or agreed to in writing, software
       * distributed under the License is distributed on an "AS IS" BASIS,
       * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       * See the License for the specific language governing permissions and
       * limitations under the License.
       */
      await async function (p126) {
        try {
          p126.swRegistration = await navigator.serviceWorker.register("/firebase-messaging-sw.js", {
            scope: "/firebase-cloud-messaging-push-scope"
          });
          p126.swRegistration.update().catch(() => {});
        } catch (e12) {
          throw Ut.create("failed-service-worker-registration", {
            browserErrorMessage: e12 == null ? undefined : e12.message
          });
        }
      }(p124);
    }
    if (p125 || !p124.swRegistration) {
      if (!(p125 instanceof ServiceWorkerRegistration)) {
        throw Ut.create("invalid-sw-registration");
      }
      p124.swRegistration = p125;
    }
  }(p120, p121 == null ? undefined : p121.serviceWorkerRegistration);
  return async function (p127) {
    const v104 = await async function (p128, p129) {
      return (await p128.pushManager.getSubscription()) || p128.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: V(p129)
      });
    }(p127.swRegistration, p127.vapidKey);
    const vO11 = {
      vapidKey: p127.vapidKey,
      swScope: p127.swRegistration.scope,
      endpoint: v104.endpoint,
      auth: K(v104.getKey("auth")),
      p256dh: K(v104.getKey("p256dh"))
    };
    const v105 = await async function (p130) {
      const vZ2 = z(p130);
      const v106 = await W();
      const v107 = await v106.transaction(Vt).objectStore(Vt).get(vZ2);
      if (v107) {
        return v107;
      }
      {
        const v108 = await async function (p131) {
          if ("databases" in indexedDB && !(await indexedDB.databases()).map(p132 => p132.name).includes(xt)) {
            return null;
          }
          let v109 = null;
          (await o(xt, 5, {
            upgrade: async (p133, p134, p135, p136) => {
              if (p134 < 2) {
                return;
              }
              if (!p133.objectStoreNames.contains(Kt)) {
                return;
              }
              const v110 = p136.objectStore(Kt);
              const v111 = await v110.index("fcmSenderId").get(p131);
              await v110.clear();
              if (v111) {
                if (p134 === 2) {
                  const vV111 = v111;
                  if (!vV111.auth || !vV111.p256dh || !vV111.endpoint) {
                    return;
                  }
                  v109 = {
                    token: vV111.fcmToken,
                    createTime: vV111.createTime ?? Date.now(),
                    subscriptionOptions: {
                      auth: vV111.auth,
                      p256dh: vV111.p256dh,
                      endpoint: vV111.endpoint,
                      swScope: vV111.swScope,
                      vapidKey: typeof vV111.vapidKey == "string" ? vV111.vapidKey : K(vV111.vapidKey)
                    }
                  };
                } else if (p134 === 3) {
                  const vV1112 = v111;
                  v109 = {
                    token: vV1112.fcmToken,
                    createTime: vV1112.createTime,
                    subscriptionOptions: {
                      auth: K(vV1112.auth),
                      p256dh: K(vV1112.p256dh),
                      endpoint: vV1112.endpoint,
                      swScope: vV1112.swScope,
                      vapidKey: K(vV1112.vapidKey)
                    }
                  };
                } else if (p134 === 4) {
                  const vV1113 = v111;
                  v109 = {
                    token: vV1113.fcmToken,
                    createTime: vV1113.createTime,
                    subscriptionOptions: {
                      auth: K(vV1113.auth),
                      p256dh: K(vV1113.p256dh),
                      endpoint: vV1113.endpoint,
                      swScope: vV1113.swScope,
                      vapidKey: K(vV1113.vapidKey)
                    }
                  };
                }
              }
            }
          })).close();
          await s(xt);
          await s("fcm_vapid_details_db");
          await s("undefined");
          if (function (p137) {
            if (!p137 || !p137.subscriptionOptions) {
              return false;
            }
            const {
              subscriptionOptions: _0x2629d6
            } = p137;
            return typeof p137.createTime == "number" && p137.createTime > 0 && typeof p137.token == "string" && p137.token.length > 0 && typeof _0x2629d6.auth == "string" && _0x2629d6.auth.length > 0 && typeof _0x2629d6.p256dh == "string" && _0x2629d6.p256dh.length > 0 && typeof _0x2629d6.endpoint == "string" && _0x2629d6.endpoint.length > 0 && typeof _0x2629d6.swScope == "string" && _0x2629d6.swScope.length > 0 && typeof _0x2629d6.vapidKey == "string" && _0x2629d6.vapidKey.length > 0;
          }(
          /**
           * @license
           * Copyright 2019 Google LLC
           *
           * Licensed under the Apache License, Version 2.0 (the "License");
           * you may not use this file except in compliance with the License.
           * You may obtain a copy of the License at
           *
           *   http://www.apache.org/licenses/LICENSE-2.0
           *
           * Unless required by applicable law or agreed to in writing, software
           * distributed under the License is distributed on an "AS IS" BASIS,
           * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
           * See the License for the specific language governing permissions and
           * limitations under the License.
           */
          v109)) {
            return v109;
          } else {
            return null;
          }
        }(p130.appConfig.senderId);
        if (v108) {
          await U(p130, v108);
          return v108;
        }
      }
    }(p127.firebaseDependencies);
    if (v105) {
      if (function (p138, p139) {
        const v112 = p139.vapidKey === p138.vapidKey;
        const v113 = p139.endpoint === p138.endpoint;
        const v114 = p139.auth === p138.auth;
        const v115 = p139.p256dh === p138.p256dh;
        return v112 && v113 && v114 && v115;
      }(v105.subscriptionOptions, vO11)) {
        if (Date.now() >= v105.createTime + 604800000) {
          return async function (p140, p141) {
            try {
              const v116 = await async function (p142, p143) {
                const v117 = await G(p142);
                const vJ2 = J(p143.subscriptionOptions);
                const vO12 = {
                  method: "PATCH",
                  headers: v117,
                  body: JSON.stringify(vJ2)
                };
                let v118;
                try {
                  const v119 = await fetch(q(p142.appConfig) + "/" + p143.token, vO12);
                  v118 = await v119.json();
                } catch (e13) {
                  throw Ut.create("token-update-failed", {
                    errorInfo: e13 == null ? undefined : e13.toString()
                  });
                }
                if (v118.error) {
                  const v120 = v118.error.message;
                  throw Ut.create("token-update-failed", {
                    errorInfo: v120
                  });
                }
                if (!v118.token) {
                  throw Ut.create("token-update-no-token");
                }
                return v118.token;
              }(p140.firebaseDependencies, p141);
              const v121 = Object.assign(Object.assign({}, p141), {
                token: v116,
                createTime: Date.now()
              });
              await U(p140.firebaseDependencies, v121);
              return v116;
            } catch (e14) {
              throw e14;
            }
          }(p127, {
            token: v105.token,
            createTime: Date.now(),
            subscriptionOptions: vO11
          });
        } else {
          return v105.token;
        }
      }
      try {
        await async function (p144, p145) {
          const vO13 = {
            method: "DELETE",
            headers: await G(p144)
          };
          try {
            const v122 = await fetch(q(p144.appConfig) + "/" + p145, vO13);
            const v123 = await v122.json();
            if (v123.error) {
              const v124 = v123.error.message;
              throw Ut.create("token-unsubscribe-failed", {
                errorInfo: v124
              });
            }
          } catch (e15) {
            throw Ut.create("token-unsubscribe-failed", {
              errorInfo: e15 == null ? undefined : e15.toString()
            });
          }
        }(p127.firebaseDependencies, v105.token);
      } catch (e16) {}
      return Y(p127.firebaseDependencies, vO11);
    }
    return Y(p127.firebaseDependencies, vO11);
  }(p120);
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
async function ee() {
  if ((await Notification.requestPermission()) === "granted") {
    const v125 = await async function () {
      return Q(r(Jt), {
        vapidKey: "BPO_oXd1sQ3AMKI8Fm7WA2-Kqr1ZZsh0lStEeP5wYmlEvd93fhhKI4W7DU3SxulA-AwWOEN_BWP0ixZUUJy75GM"
      });
    }();
    return (await _0xa134a7.post("/api/register-token/", {
      token: v125
    }, {
      withCredentials: true,
      headers: {
        "X-CSRFToken": _0x2eed9b("csrftoken")
      }
    })).status;
  }
  window.alert("Permissão para notificações não autorizado, permita caso queira receber notificações de lançamento da Lura.");
  return 400;
}
import { l as _0xa134a7, E as _0x2eed9b, r as _0x9528f9 } from "./CqksSKFa.js";
var re = {};
/**
 * @license
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const ae = function (p146) {
  const vA4 = [];
  let vLN03 = 0;
  for (let vLN04 = 0; vLN04 < p146.length; vLN04++) {
    let v126 = p146.charCodeAt(vLN04);
    if (v126 < 128) {
      vA4[vLN03++] = v126;
    } else if (v126 < 2048) {
      vA4[vLN03++] = v126 >> 6 | 192;
      vA4[vLN03++] = v126 & 63 | 128;
    } else if ((v126 & 64512) == 55296 && vLN04 + 1 < p146.length && (p146.charCodeAt(vLN04 + 1) & 64512) == 56320) {
      v126 = 65536 + ((v126 & 1023) << 10) + (p146.charCodeAt(++vLN04) & 1023);
      vA4[vLN03++] = v126 >> 18 | 240;
      vA4[vLN03++] = v126 >> 12 & 63 | 128;
      vA4[vLN03++] = v126 >> 6 & 63 | 128;
      vA4[vLN03++] = v126 & 63 | 128;
    } else {
      vA4[vLN03++] = v126 >> 12 | 224;
      vA4[vLN03++] = v126 >> 6 & 63 | 128;
      vA4[vLN03++] = v126 & 63 | 128;
    }
  }
  return vA4;
};
const oe = {
  byteToCharMap_: null,
  charToByteMap_: null,
  byteToCharMapWebSafe_: null,
  charToByteMapWebSafe_: null,
  ENCODED_VALS_BASE: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
  get ENCODED_VALS() {
    return this.ENCODED_VALS_BASE + "+/=";
  },
  get ENCODED_VALS_WEBSAFE() {
    return this.ENCODED_VALS_BASE + "-_.";
  },
  HAS_NATIVE_SUPPORT: typeof atob == "function",
  encodeByteArray(p147, p148) {
    if (!Array.isArray(p147)) {
      throw Error("encodeByteArray takes an array as a parameter");
    }
    this.init_();
    const v127 = p148 ? this.byteToCharMapWebSafe_ : this.byteToCharMap_;
    const vA5 = [];
    for (let vLN05 = 0; vLN05 < p147.length; vLN05 += 3) {
      const v128 = p147[vLN05];
      const v129 = vLN05 + 1 < p147.length;
      const v130 = v129 ? p147[vLN05 + 1] : 0;
      const v131 = vLN05 + 2 < p147.length;
      const v132 = v131 ? p147[vLN05 + 2] : 0;
      const v133 = v128 >> 2;
      const v134 = (v128 & 3) << 4 | v130 >> 4;
      let v135 = (v130 & 15) << 2 | v132 >> 6;
      let v136 = v132 & 63;
      if (!v131) {
        v136 = 64;
        if (!v129) {
          v135 = 64;
        }
      }
      vA5.push(v127[v133], v127[v134], v127[v135], v127[v136]);
    }
    return vA5.join("");
  },
  encodeString(p149, p150) {
    if (this.HAS_NATIVE_SUPPORT && !p150) {
      return btoa(p149);
    } else {
      return this.encodeByteArray(ae(p149), p150);
    }
  },
  decodeString(p151, p152) {
    if (this.HAS_NATIVE_SUPPORT && !p152) {
      return atob(p151);
    } else {
      return function (p153) {
        const vA6 = [];
        let vLN06 = 0;
        let vLN07 = 0;
        while (vLN06 < p153.length) {
          const v137 = p153[vLN06++];
          if (v137 < 128) {
            vA6[vLN07++] = String.fromCharCode(v137);
          } else if (v137 > 191 && v137 < 224) {
            const v138 = p153[vLN06++];
            vA6[vLN07++] = String.fromCharCode((v137 & 31) << 6 | v138 & 63);
          } else if (v137 > 239 && v137 < 365) {
            const v139 = ((v137 & 7) << 18 | (p153[vLN06++] & 63) << 12 | (p153[vLN06++] & 63) << 6 | p153[vLN06++] & 63) - 65536;
            vA6[vLN07++] = String.fromCharCode(55296 + (v139 >> 10));
            vA6[vLN07++] = String.fromCharCode(56320 + (v139 & 1023));
          } else {
            const v140 = p153[vLN06++];
            const v141 = p153[vLN06++];
            vA6[vLN07++] = String.fromCharCode((v137 & 15) << 12 | (v140 & 63) << 6 | v141 & 63);
          }
        }
        return vA6.join("");
      }(this.decodeStringToByteArray(p151, p152));
    }
  },
  decodeStringToByteArray(p154, p155) {
    this.init_();
    const v142 = p155 ? this.charToByteMapWebSafe_ : this.charToByteMap_;
    const vA7 = [];
    for (let vLN08 = 0; vLN08 < p154.length;) {
      const v143 = v142[p154.charAt(vLN08++)];
      const v144 = vLN08 < p154.length ? v142[p154.charAt(vLN08)] : 0;
      ++vLN08;
      const v145 = vLN08 < p154.length ? v142[p154.charAt(vLN08)] : 64;
      ++vLN08;
      const v146 = vLN08 < p154.length ? v142[p154.charAt(vLN08)] : 64;
      ++vLN08;
      if (v143 == null || v144 == null || v145 == null || v146 == null) {
        throw new se();
      }
      const v147 = v143 << 2 | v144 >> 4;
      vA7.push(v147);
      if (v145 !== 64) {
        const v148 = v144 << 4 & 240 | v145 >> 2;
        vA7.push(v148);
        if (v146 !== 64) {
          const v149 = v145 << 6 & 192 | v146;
          vA7.push(v149);
        }
      }
    }
    return vA7;
  },
  init_() {
    if (!this.byteToCharMap_) {
      this.byteToCharMap_ = {};
      this.charToByteMap_ = {};
      this.byteToCharMapWebSafe_ = {};
      this.charToByteMapWebSafe_ = {};
      for (let vLN09 = 0; vLN09 < this.ENCODED_VALS.length; vLN09++) {
        this.byteToCharMap_[vLN09] = this.ENCODED_VALS.charAt(vLN09);
        this.charToByteMap_[this.byteToCharMap_[vLN09]] = vLN09;
        this.byteToCharMapWebSafe_[vLN09] = this.ENCODED_VALS_WEBSAFE.charAt(vLN09);
        this.charToByteMapWebSafe_[this.byteToCharMapWebSafe_[vLN09]] = vLN09;
        if (vLN09 >= this.ENCODED_VALS_BASE.length) {
          this.charToByteMap_[this.ENCODED_VALS_WEBSAFE.charAt(vLN09)] = vLN09;
          this.charToByteMapWebSafe_[this.ENCODED_VALS.charAt(vLN09)] = vLN09;
        }
      }
    }
  }
};
class se extends Error {
  constructor() {
    super(...arguments);
    this.name = "DecodeBase64StringError";
  }
}
const ce = function (p156) {
  return function (p157) {
    const vAe = ae(p157);
    return oe.encodeByteArray(vAe, true);
  }(p156).replace(/\./g, "");
};
const de = () => {
  return (() => {
    try {
      return function () {
        if (typeof self != "undefined") {
          return self;
        }
        if (typeof window != "undefined") {
          return window;
        }
        if (typeof global != "undefined") {
          return global;
        }
        throw new Error("Unable to locate global object.");
      }().__FIREBASE_DEFAULTS__ || (() => {
        if (typeof process == "undefined") {
          return;
        }
        const v150 = re.__FIREBASE_DEFAULTS__;
        if (v150) {
          return JSON.parse(v150);
        } else {
          return undefined;
        }
      })() || (() => {
        if (typeof document == "undefined") {
          return;
        }
        let v151;
        try {
          v151 = document.cookie.match(/__FIREBASE_DEFAULTS__=([^;]+)/);
        } catch (e17) {
          return;
        }
        const v152 = v151 && function (p158) {
          try {
            return oe.decodeString(p158, true);
          } catch (e18) {}
          return null;
        }(v151[1]);
        return v152 && JSON.parse(v152);
      })();
    } catch (e19) {
      return;
    }
  })()?.config;
};
/**
 * @license
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class le {
  constructor() {
    this.reject = () => {};
    this.resolve = () => {};
    this.promise = new Promise((p159, p160) => {
      this.resolve = p159;
      this.reject = p160;
    });
  }
  wrapCallback(p161) {
    return (p162, p163) => {
      if (p162) {
        this.reject(p162);
      } else {
        this.resolve(p163);
      }
      if (typeof p161 == "function") {
        this.promise.catch(() => {});
        if (p161.length === 1) {
          p161(p162);
        } else {
          p161(p162, p163);
        }
      }
    };
  }
}
class ue extends Error {
  constructor(p164, p165, p166) {
    super(p165);
    this.code = p164;
    this.customData = p166;
    this.name = "FirebaseError";
    Object.setPrototypeOf(this, ue.prototype);
    if (Error.captureStackTrace) {
      Error.captureStackTrace(this, pe.prototype.create);
    }
  }
}
class pe {
  constructor(p167, p168, p169) {
    this.service = p167;
    this.serviceName = p168;
    this.errors = p169;
  }
  create(p170, ..._0x52f34a) {
    const v153 = _0x52f34a[0] || {};
    const v154 = this.service + "/" + p170;
    const v155 = this.errors[p170];
    const v156 = v155 ? function (p171, p172) {
      return p171.replace(he, (p173, p174) => {
        const v157 = p172[p174];
        if (v157 != null) {
          return String(v157);
        } else {
          return "<" + p174 + "?>";
        }
      });
    }(v155, v153) : "Error";
    const v158 = this.serviceName + ": " + v156 + " (" + v154 + ").";
    return new ue(v154, v158, v153);
  }
}
const he = /\{\$([^}]+)}/g;
class fe {
  constructor(p175, p176, p177) {
    this.name = p175;
    this.instanceFactory = p176;
    this.type = p177;
    this.multipleInstances = false;
    this.serviceProps = {};
    this.instantiationMode = "LAZY";
    this.onInstanceCreated = null;
  }
  setInstantiationMode(p178) {
    this.instantiationMode = p178;
    return this;
  }
  setMultipleInstances(p179) {
    this.multipleInstances = p179;
    return this;
  }
  setServiceProps(p180) {
    this.serviceProps = p180;
    return this;
  }
  setInstanceCreatedCallback(p181) {
    this.onInstanceCreated = p181;
    return this;
  }
}
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const ge = "[DEFAULT]";
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class me {
  constructor(p182, p183) {
    this.name = p182;
    this.container = p183;
    this.component = null;
    this.instances = new Map();
    this.instancesDeferred = new Map();
    this.instancesOptions = new Map();
    this.onInitCallbacks = new Map();
  }
  get(p184) {
    const v159 = this.normalizeInstanceIdentifier(p184);
    if (!this.instancesDeferred.has(v159)) {
      const v160 = new le();
      this.instancesDeferred.set(v159, v160);
      if (this.isInitialized(v159) || this.shouldAutoInitialize()) {
        try {
          const v161 = this.getOrInitializeService({
            instanceIdentifier: v159
          });
          if (v161) {
            v160.resolve(v161);
          }
        } catch (e20) {}
      }
    }
    return this.instancesDeferred.get(v159).promise;
  }
  getImmediate(p185) {
    var v162;
    const v163 = this.normalizeInstanceIdentifier(p185 == null ? undefined : p185.identifier);
    const v164 = (v162 = p185 == null ? undefined : p185.optional) !== null && v162 !== undefined && v162;
    if (!this.isInitialized(v163) && !this.shouldAutoInitialize()) {
      if (v164) {
        return null;
      }
      throw Error("Service " + this.name + " is not available");
    }
    try {
      return this.getOrInitializeService({
        instanceIdentifier: v163
      });
    } catch (e21) {
      if (v164) {
        return null;
      }
      throw e21;
    }
  }
  getComponent() {
    return this.component;
  }
  setComponent(p186) {
    if (p186.name !== this.name) {
      throw Error("Mismatching Component " + p186.name + " for Provider " + this.name + ".");
    }
    if (this.component) {
      throw Error("Component for " + this.name + " has already been provided");
    }
    this.component = p186;
    if (this.shouldAutoInitialize()) {
      if (function (p187) {
        return p187.instantiationMode === "EAGER";
      }(
      /**
       * @license
       * Copyright 2019 Google LLC
       *
       * Licensed under the Apache License, Version 2.0 (the "License");
       * you may not use this file except in compliance with the License.
       * You may obtain a copy of the License at
       *
       *   http://www.apache.org/licenses/LICENSE-2.0
       *
       * Unless required by applicable law or agreed to in writing, software
       * distributed under the License is distributed on an "AS IS" BASIS,
       * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       * See the License for the specific language governing permissions and
       * limitations under the License.
       */
      p186)) {
        try {
          this.getOrInitializeService({
            instanceIdentifier: ge
          });
        } catch (e22) {}
      }
      for (const [v165, v166] of this.instancesDeferred.entries()) {
        const v167 = this.normalizeInstanceIdentifier(v165);
        try {
          const v168 = this.getOrInitializeService({
            instanceIdentifier: v167
          });
          v166.resolve(v168);
        } catch (e23) {}
      }
    }
  }
  clearInstance(p188 = ge) {
    this.instancesDeferred.delete(p188);
    this.instancesOptions.delete(p188);
    this.instances.delete(p188);
  }
  async delete() {
    const v169 = Array.from(this.instances.values());
    await Promise.all([...v169.filter(p189 => "INTERNAL" in p189).map(p190 => p190.INTERNAL.delete()), ...v169.filter(p191 => "_delete" in p191).map(p192 => p192._delete())]);
  }
  isComponentSet() {
    return this.component != null;
  }
  isInitialized(p193 = ge) {
    return this.instances.has(p193);
  }
  getOptions(p194 = ge) {
    return this.instancesOptions.get(p194) || {};
  }
  initialize(p195 = {}) {
    const {
      options: _0x2cb092 = {}
    } = p195;
    const v170 = this.normalizeInstanceIdentifier(p195.instanceIdentifier);
    if (this.isInitialized(v170)) {
      throw Error(this.name + "(" + v170 + ") has already been initialized");
    }
    if (!this.isComponentSet()) {
      throw Error("Component " + this.name + " has not been registered yet");
    }
    const v171 = this.getOrInitializeService({
      instanceIdentifier: v170,
      options: _0x2cb092
    });
    for (const [v172, v173] of this.instancesDeferred.entries()) {
      if (v170 === this.normalizeInstanceIdentifier(v172)) {
        v173.resolve(v171);
      }
    }
    return v171;
  }
  onInit(p196, p197) {
    const v174 = this.normalizeInstanceIdentifier(p197);
    const v175 = this.onInitCallbacks.get(v174) ?? new Set();
    v175.add(p196);
    this.onInitCallbacks.set(v174, v175);
    const v176 = this.instances.get(v174);
    if (v176) {
      p196(v176, v174);
    }
    return () => {
      v175.delete(p196);
    };
  }
  invokeOnInitCallbacks(p198, p199) {
    const v177 = this.onInitCallbacks.get(p199);
    if (v177) {
      for (const v178 of v177) {
        try {
          v178(p198, p199);
        } catch (e24) {}
      }
    }
  }
  getOrInitializeService({
    instanceIdentifier: _0x1bd477,
    options: _0x220f0c = {}
  }) {
    let v179 = this.instances.get(_0x1bd477);
    if (!v179 && this.component && (v179 = this.component.instanceFactory(this.container, {
      instanceIdentifier: (v180 = _0x1bd477, v180 === ge ? undefined : v180),
      options: _0x220f0c
    }), this.instances.set(_0x1bd477, v179), this.instancesOptions.set(_0x1bd477, _0x220f0c), this.invokeOnInitCallbacks(v179, _0x1bd477), this.component.onInstanceCreated)) {
      try {
        this.component.onInstanceCreated(this.container, _0x1bd477, v179);
      } catch (e25) {}
    }
    var v180;
    return v179 || null;
  }
  normalizeInstanceIdentifier(p200 = ge) {
    if (this.component) {
      if (this.component.multipleInstances) {
        return p200;
      } else {
        return ge;
      }
    } else {
      return p200;
    }
  }
  shouldAutoInitialize() {
    return !!this.component && this.component.instantiationMode !== "EXPLICIT";
  }
}
class be {
  constructor(p201) {
    this.name = p201;
    this.providers = new Map();
  }
  addComponent(p202) {
    const v181 = this.getProvider(p202.name);
    if (v181.isComponentSet()) {
      throw new Error("Component " + p202.name + " has already been registered with " + this.name);
    }
    v181.setComponent(p202);
  }
  addOrOverwriteComponent(p203) {
    if (this.getProvider(p203.name).isComponentSet()) {
      this.providers.delete(p203.name);
    }
    this.addComponent(p203);
  }
  getProvider(p204) {
    if (this.providers.has(p204)) {
      return this.providers.get(p204);
    }
    const v182 = new me(p204, this);
    this.providers.set(p204, v182);
    return v182;
  }
  getProviders() {
    return Array.from(this.providers.values());
  }
}
/**
 * @license
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var we;
var ye;
(ye = we ||= {})[ye.DEBUG = 0] = "DEBUG";
ye[ye.VERBOSE = 1] = "VERBOSE";
ye[ye.INFO = 2] = "INFO";
ye[ye.WARN = 3] = "WARN";
ye[ye.ERROR = 4] = "ERROR";
ye[ye.SILENT = 5] = "SILENT";
const ve = {
  debug: we.DEBUG,
  verbose: we.VERBOSE,
  info: we.INFO,
  warn: we.WARN,
  error: we.ERROR,
  silent: we.SILENT
};
const Ie = we.INFO;
const Se = {
  [we.DEBUG]: "log",
  [we.VERBOSE]: "log",
  [we.INFO]: "info",
  [we.WARN]: "warn",
  [we.ERROR]: "error"
};
const Ee = (p205, p206, ..._0x2c2438) => {
  if (!(p206 < p205.logLevel) && !(new Date().toISOString(), Se[p206])) {
    throw new Error("Attempted to log a message with an invalid logType (value: " + p206 + ")");
  }
};
let _e;
let Ce;
const De = new WeakMap();
const ke = new WeakMap();
const Te = new WeakMap();
const Ae = new WeakMap();
const Oe = new WeakMap();
let Ne = {
  get(p207, p208, p209) {
    if (p207 instanceof IDBTransaction) {
      if (p208 === "done") {
        return ke.get(p207);
      }
      if (p208 === "objectStoreNames") {
        return p207.objectStoreNames || Te.get(p207);
      }
      if (p208 === "store") {
        if (p209.objectStoreNames[1]) {
          return undefined;
        } else {
          return p209.objectStore(p209.objectStoreNames[0]);
        }
      }
    }
    return a(p207[p208]);
  },
  set: (p210, p211, p212) => {
    p210[p211] = p212;
    return true;
  },
  has: (p213, p214) => p213 instanceof IDBTransaction && (p214 === "done" || p214 === "store") || p214 in p213
};
const Pe = p215 => Oe.get(p215);
const Me = ["get", "getKey", "getAll", "getAllKeys", "count"];
const Be = ["put", "add", "delete", "clear"];
const je = new Map();
var Le;
Le = Ne;
Ne = {
  ...Le,
  get: (p216, p217, p218) => c(p216, p217) || Le.get(p216, p217, p218),
  has: (p219, p220) => !!c(p219, p220) || Le.has(p219, p220)
};
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class Re {
  constructor(p221) {
    this.container = p221;
  }
  getPlatformInfoString() {
    return this.container.getProviders().map(p222 => {
      if (function (p223) {
        const v183 = p223.getComponent();
        return (v183 == null ? undefined : v183.type) === "VERSION";
      }(p222)) {
        const v184 = p222.getImmediate();
        return v184.library + "/" + v184.version;
      }
      return null;
    }).filter(p224 => p224).join(" ");
  }
}
const $e = "@firebase/app";
const He = "0.10.16";
const Fe = new class {
  constructor(p225) {
    this.name = p225;
    this._logLevel = Ie;
    this._logHandler = Ee;
    this._userLogHandler = null;
  }
  get logLevel() {
    return this._logLevel;
  }
  set logLevel(p226) {
    if (!(p226 in we)) {
      throw new TypeError("Invalid value \"" + p226 + "\" assigned to `logLevel`");
    }
    this._logLevel = p226;
  }
  setLogLevel(p227) {
    this._logLevel = typeof p227 == "string" ? ve[p227] : p227;
  }
  get logHandler() {
    return this._logHandler;
  }
  set logHandler(p228) {
    if (typeof p228 != "function") {
      throw new TypeError("Value assigned to `logHandler` must be a function");
    }
    this._logHandler = p228;
  }
  get userLogHandler() {
    return this._userLogHandler;
  }
  set userLogHandler(p229) {
    this._userLogHandler = p229;
  }
  debug(..._0x337ebf) {
    if (this._userLogHandler) {
      this._userLogHandler(this, we.DEBUG, ..._0x337ebf);
    }
    this._logHandler(this, we.DEBUG, ..._0x337ebf);
  }
  log(..._0x3cb95b) {
    if (this._userLogHandler) {
      this._userLogHandler(this, we.VERBOSE, ..._0x3cb95b);
    }
    this._logHandler(this, we.VERBOSE, ..._0x3cb95b);
  }
  info(..._0x28e7ac) {
    if (this._userLogHandler) {
      this._userLogHandler(this, we.INFO, ..._0x28e7ac);
    }
    this._logHandler(this, we.INFO, ..._0x28e7ac);
  }
  warn(..._0xa9b4ad) {
    if (this._userLogHandler) {
      this._userLogHandler(this, we.WARN, ..._0xa9b4ad);
    }
    this._logHandler(this, we.WARN, ..._0xa9b4ad);
  }
  error(..._0x196706) {
    if (this._userLogHandler) {
      this._userLogHandler(this, we.ERROR, ..._0x196706);
    }
    this._logHandler(this, we.ERROR, ..._0x196706);
  }
}("@firebase/app");
const xe = "@firebase/app-compat";
const Ke = "@firebase/analytics-compat";
const Ve = "@firebase/analytics";
const We = "@firebase/app-check-compat";
const Ue = "@firebase/app-check";
const ze = "@firebase/auth";
const qe = "@firebase/auth-compat";
const Ge = "@firebase/database";
const Je = "@firebase/data-connect";
const Ye = "@firebase/database-compat";
const Ze = "@firebase/functions";
const Xe = "@firebase/functions-compat";
const Qe = "@firebase/installations";
const et = "@firebase/installations-compat";
const tt = "@firebase/messaging";
const nt = "@firebase/messaging-compat";
const it = "@firebase/performance";
const rt = "@firebase/performance-compat";
const at = "@firebase/remote-config";
const ot = "@firebase/remote-config-compat";
const st = "@firebase/storage";
const ct = "@firebase/storage-compat";
const dt = "@firebase/firestore";
const lt = "@firebase/vertexai";
const ut = "@firebase/firestore-compat";
const pt = "firebase";
const ht = "[DEFAULT]";
const ft = {
  [$e]: "fire-core",
  [xe]: "fire-core-compat",
  [Ve]: "fire-analytics",
  [Ke]: "fire-analytics-compat",
  [Ue]: "fire-app-check",
  [We]: "fire-app-check-compat",
  [ze]: "fire-auth",
  [qe]: "fire-auth-compat",
  [Ge]: "fire-rtdb",
  [Je]: "fire-data-connect",
  [Ye]: "fire-rtdb-compat",
  [Ze]: "fire-fn",
  [Xe]: "fire-fn-compat",
  [Qe]: "fire-iid",
  [et]: "fire-iid-compat",
  [tt]: "fire-fcm",
  [nt]: "fire-fcm-compat",
  [it]: "fire-perf",
  [rt]: "fire-perf-compat",
  [at]: "fire-rc",
  [ot]: "fire-rc-compat",
  [st]: "fire-gcs",
  [ct]: "fire-gcs-compat",
  [dt]: "fire-fst",
  [ut]: "fire-fst-compat",
  [lt]: "fire-vertex",
  "fire-js": "fire-js",
  [pt]: "fire-js-all"
};
const gt = new Map();
const mt = new Map();
const bt = new Map();
const wt = new pe("app", "Firebase", {
  "no-app": "No Firebase App '{$appName}' has been created - call initializeApp() first",
  "bad-app-name": "Illegal App name: '{$appName}'",
  "duplicate-app": "Firebase App named '{$appName}' already exists with different options or config",
  "app-deleted": "Firebase App named '{$appName}' already deleted",
  "server-app-deleted": "Firebase Server App has been deleted",
  "no-options": "Need to provide options, when not being deployed to hosting via source.",
  "invalid-app-argument": "firebase.{$appName}() takes either no argument or a Firebase App instance.",
  "invalid-log-argument": "First argument to `onLog` must be null or a function.",
  "idb-open": "Error thrown when opening IndexedDB. Original error: {$originalErrorMessage}.",
  "idb-get": "Error thrown when reading from IndexedDB. Original error: {$originalErrorMessage}.",
  "idb-set": "Error thrown when writing to IndexedDB. Original error: {$originalErrorMessage}.",
  "idb-delete": "Error thrown when deleting from IndexedDB. Original error: {$originalErrorMessage}.",
  "finalization-registry-not-supported": "FirebaseServerApp deleteOnDeref field defined but the JS runtime does not support FinalizationRegistry.",
  "invalid-server-app-environment": "FirebaseServerApp is not for use in browser environments."
});
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class yt {
  constructor(p230, p231, p232) {
    this._isDeleted = false;
    this._options = Object.assign({}, p230);
    this._config = Object.assign({}, p231);
    this._name = p231.name;
    this._automaticDataCollectionEnabled = p231.automaticDataCollectionEnabled;
    this._container = p232;
    this.container.addComponent(new fe("app", () => this, "PUBLIC"));
  }
  get automaticDataCollectionEnabled() {
    this.checkDestroyed();
    return this._automaticDataCollectionEnabled;
  }
  set automaticDataCollectionEnabled(p233) {
    this.checkDestroyed();
    this._automaticDataCollectionEnabled = p233;
  }
  get name() {
    this.checkDestroyed();
    return this._name;
  }
  get options() {
    this.checkDestroyed();
    return this._options;
  }
  get config() {
    this.checkDestroyed();
    return this._config;
  }
  get container() {
    return this._container;
  }
  get isDeleted() {
    return this._isDeleted;
  }
  set isDeleted(p234) {
    this._isDeleted = p234;
  }
  checkDestroyed() {
    if (this.isDeleted) {
      throw wt.create("app-deleted", {
        appName: this._name
      });
    }
  }
}
const vt = "firebase-heartbeat-store";
let It = null;
class St {
  constructor(p235) {
    this.container = p235;
    this._heartbeatsCache = null;
    const v185 = this.container.getProvider("app").getImmediate();
    this._storage = new Et(v185);
    this._heartbeatsCachePromise = this._storage.read().then(p236 => {
      this._heartbeatsCache = p236;
      return p236;
    });
  }
  async triggerHeartbeat() {
    try {
      const v186 = this.container.getProvider("platform-logger").getImmediate().getPlatformInfoString();
      const vB = b();
      if (this._heartbeatsCache?.heartbeats == null && (this._heartbeatsCache = await this._heartbeatsCachePromise, this._heartbeatsCache?.heartbeats == null)) {
        return;
      }
      if (this._heartbeatsCache.lastSentHeartbeatDate === vB || this._heartbeatsCache.heartbeats.some(p237 => p237.date === vB)) {
        return;
      }
      this._heartbeatsCache.heartbeats.push({
        date: vB,
        agent: v186
      });
      this._heartbeatsCache.heartbeats = this._heartbeatsCache.heartbeats.filter(p238 => {
        const v187 = new Date(p238.date).valueOf();
        return Date.now() - v187 <= 2592000000;
      });
      return this._storage.overwrite(this._heartbeatsCache);
    } catch (e26) {
      Fe.warn(e26);
    }
  }
  async getHeartbeatsHeader() {
    try {
      if (this._heartbeatsCache === null) {
        await this._heartbeatsCachePromise;
      }
      if (this._heartbeatsCache?.heartbeats == null || this._heartbeatsCache.heartbeats.length === 0) {
        return "";
      }
      const vB2 = b();
      const {
        heartbeatsToSend: _0x36eab7,
        unsentEntries: _0x131f9a
      } = function (p239, p240 = 1024) {
        const vA8 = [];
        let v188 = p239.slice();
        for (const v189 of p239) {
          const v190 = vA8.find(p241 => p241.agent === v189.agent);
          if (v190) {
            v190.dates.push(v189.date);
            if (w(vA8) > p240) {
              v190.dates.pop();
              break;
            }
          } else {
            vA8.push({
              agent: v189.agent,
              dates: [v189.date]
            });
            if (w(vA8) > p240) {
              vA8.pop();
              break;
            }
          }
          v188 = v188.slice(1);
        }
        return {
          heartbeatsToSend: vA8,
          unsentEntries: v188
        };
      }(this._heartbeatsCache.heartbeats);
      const vCe = ce(JSON.stringify({
        version: 2,
        heartbeats: _0x36eab7
      }));
      this._heartbeatsCache.lastSentHeartbeatDate = vB2;
      if (_0x131f9a.length > 0) {
        this._heartbeatsCache.heartbeats = _0x131f9a;
        await this._storage.overwrite(this._heartbeatsCache);
      } else {
        this._heartbeatsCache.heartbeats = [];
        this._storage.overwrite(this._heartbeatsCache);
      }
      return vCe;
    } catch (e27) {
      Fe.warn(e27);
      return "";
    }
  }
}
class Et {
  constructor(p242) {
    this.app = p242;
    this._canUseIndexedDBPromise = this.runIndexedDBEnvironmentCheck();
  }
  async runIndexedDBEnvironmentCheck() {
    return !!e() && t().then(() => true).catch(() => false);
  }
  async read() {
    if (await this._canUseIndexedDBPromise) {
      const v191 = await async function (p243) {
        try {
          const v192 = (await f()).transaction(vt);
          const v193 = await v192.objectStore(vt).get(m(p243));
          await v192.done;
          return v193;
        } catch (e28) {
          if (e28 instanceof ue) {
            Fe.warn(e28.message);
          } else {
            const v194 = wt.create("idb-get", {
              originalErrorMessage: e28 == null ? undefined : e28.message
            });
            Fe.warn(v194.message);
          }
        }
      }(this.app);
      if (v191 == null ? undefined : v191.heartbeats) {
        return v191;
      } else {
        return {
          heartbeats: []
        };
      }
    }
    return {
      heartbeats: []
    };
  }
  async overwrite(p244) {
    if (await this._canUseIndexedDBPromise) {
      const v195 = await this.read();
      return g(this.app, {
        lastSentHeartbeatDate: p244.lastSentHeartbeatDate ?? v195.lastSentHeartbeatDate,
        heartbeats: p244.heartbeats
      });
    }
  }
  async add(p245) {
    if (await this._canUseIndexedDBPromise) {
      const v196 = await this.read();
      return g(this.app, {
        lastSentHeartbeatDate: p245.lastSentHeartbeatDate ?? v196.lastSentHeartbeatDate,
        heartbeats: [...v196.heartbeats, ...p245.heartbeats]
      });
    }
  }
}
l(new fe("platform-logger", p246 => new Re(p246), "PRIVATE"));
l(new fe("heartbeat", p247 => new St(p247), "PRIVATE"));
h($e, He, "");
h($e, He, "esm2017");
h("fire-js", "");
/**
 * @license
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
h("firebase", "11.0.2", "app");
const _t = "@firebase/installations";
const Ct = "0.6.11";
const Dt = 10000;
const kt = "w:" + Ct;
const Tt = "FIS_v2";
const At = new pe("installations", "Installations", {
  "missing-app-config-values": "Missing App configuration value: \"{$valueName}\"",
  "not-registered": "Firebase Installation is not registered.",
  "installation-not-found": "Firebase Installation not found.",
  "request-failed": "{$requestName} request failed with error \"{$serverCode} {$serverStatus}: {$serverMessage}\"",
  "app-offline": "Could not process request. Application offline.",
  "delete-pending-registration": "Can't delete installation while there is a pending registration request."
});
const Ot = /^[cdef][\w-]{21}$/;
const Nt = new Map();
let Pt = null;
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const Mt = "firebase-installations-store";
let Bt = null;
const jt = "installations";
l(new fe(jt, p248 => {
  const v197 = p248.getProvider("app").getImmediate();
  const vF19 =
  /**
   * @license
   * Copyright 2019 Google LLC
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  function (p249) {
    if (!p249 || !p249.options) {
      throw x("App Configuration");
    }
    if (!p249.name) {
      throw x("App Name");
    }
    const vA9 = ["projectId", "apiKey", "appId"];
    for (const v198 of vA9) {
      if (!p249.options[v198]) {
        throw x(v198);
      }
    }
    return {
      appName: p249.name,
      projectId: p249.options.projectId,
      apiKey: p249.options.apiKey,
      appId: p249.options.appId
    };
  }(v197);
  return {
    app: v197,
    appConfig: vF19,
    heartbeatServiceProvider: u(v197, "heartbeat"),
    _delete: () => Promise.resolve()
  };
}, "PUBLIC"));
l(new fe("installations-internal", p250 => {
  const v199 = u(p250.getProvider("app").getImmediate(), jt).getImmediate();
  return {
    getId: () => async function (p251) {
      const vP251 = p251;
      const {
        installationEntry: _0x338f5b,
        registrationPromise: _0x2cfd7a
      } = await B(vP251);
      if (_0x2cfd7a) {
        _0x2cfd7a.catch(console.error);
      } else {
        $(vP251).catch(console.error);
      }
      return _0x338f5b.fid;
    }(
    /**
     * @license
     * Copyright 2019 Google LLC
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *   http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    v199),
    getToken: p252 => async function (p253, p254 = false) {
      const vP253 = p253;
      await async function (p255) {
        const {
          registrationPromise: _0xd0e7e2
        } = await B(p255);
        if (_0xd0e7e2) {
          await _0xd0e7e2;
        }
      }(vP253);
      return (await $(vP253, p254)).token;
    }(v199, p252)
  };
}, "PRIVATE"));
h(_t, Ct);
h(_t, Ct, "esm2017");
/**
 * @license
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const Lt = "BDOU99-h67HcA6JeFXHbSNMu7e2yNNu3RzoMj8TM4W88jITfq7ZmPvIM1Iv-4_l2LxQcYwhqby2xGpWwzjfAnG4";
const Rt = "google.c.a.c_id";
var $t;
var Ht;
var Ft;
(Ht = $t ||= {})[Ht.DATA_MESSAGE = 1] = "DATA_MESSAGE";
Ht[Ht.DISPLAY_NOTIFICATION = 3] = "DISPLAY_NOTIFICATION";
(function (p256) {
  p256.PUSH_RECEIVED = "push-received";
  p256.NOTIFICATION_CLICKED = "notification-clicked";
})(Ft ||= {});
const xt = "fcm_token_details_db";
const Kt = "fcm_token_object_Store";
const Vt = "firebase-messaging-store";
let Wt = null;
const Ut = new pe("messaging", "Messaging", {
  "missing-app-config-values": "Missing App configuration value: \"{$valueName}\"",
  "only-available-in-window": "This method is available in a Window context.",
  "only-available-in-sw": "This method is available in a service worker context.",
  "permission-default": "The notification permission was not granted and dismissed instead.",
  "permission-blocked": "The notification permission was not granted and blocked instead.",
  "unsupported-browser": "This browser doesn't support the API's required to use the Firebase SDK.",
  "indexed-db-unsupported": "This browser doesn't support indexedDb.open() (ex. Safari iFrame, Firefox Private Browsing, etc)",
  "failed-service-worker-registration": "We are unable to register the default service worker. {$browserErrorMessage}",
  "token-subscribe-failed": "A problem occurred while subscribing the user to FCM: {$errorInfo}",
  "token-subscribe-no-token": "FCM returned no token when subscribing the user to push.",
  "token-unsubscribe-failed": "A problem occurred while unsubscribing the user from FCM: {$errorInfo}",
  "token-update-failed": "A problem occurred while updating the user from FCM: {$errorInfo}",
  "token-update-no-token": "FCM returned no token when updating the user to push.",
  "use-sw-after-get-token": "The useServiceWorker() method may only be called once and must be called before calling getToken() to ensure your service worker is used.",
  "invalid-sw-registration": "The input to useServiceWorker() must be a ServiceWorkerRegistration.",
  "invalid-bg-handler": "The input to setBackgroundMessageHandler() must be a function.",
  "invalid-vapid-key": "The public VAPID key must be a string.",
  "use-vapid-key-after-get-token": "The usePublicVapidKey() method may only be called once and must be called before calling getToken() to ensure your VAPID key is used."
});
class zt {
  constructor(p257, p258, p259) {
    this.deliveryMetricsExportedToBigQueryEnabled = false;
    this.onBackgroundMessageHandler = null;
    this.onMessageHandler = null;
    this.logEvents = [];
    this.isLogServiceStarted = false;
    const vF20 =
    /**
     * @license
     * Copyright 2019 Google LLC
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *   http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    function (p260) {
      if (!p260 || !p260.options) {
        throw X("App Configuration Object");
      }
      if (!p260.name) {
        throw X("App Name");
      }
      const vA10 = ["projectId", "apiKey", "appId", "messagingSenderId"];
      const {
        options: _0x1191a7
      } = p260;
      for (const v200 of vA10) {
        if (!_0x1191a7[v200]) {
          throw X(v200);
        }
      }
      return {
        appName: p260.name,
        projectId: _0x1191a7.projectId,
        apiKey: _0x1191a7.apiKey,
        appId: _0x1191a7.appId,
        senderId: _0x1191a7.messagingSenderId
      };
    }(p257);
    this.firebaseDependencies = {
      app: p257,
      appConfig: vF20,
      installations: p258,
      analyticsProvider: p259
    };
  }
  _delete() {
    return Promise.resolve();
  }
}
const qt = "@firebase/messaging";
const Gt = "0.12.14";
l(new fe("messaging", p261 => {
  const v201 = new zt(p261.getProvider("app").getImmediate(), p261.getProvider("installations-internal").getImmediate(), p261.getProvider("analytics-internal"));
  navigator.serviceWorker.addEventListener("message", p262 =>
  /**
   * @license
   * Copyright 2017 Google LLC
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  async function (p263, p264) {
    const v202 = p264.data;
    if (!v202.isFirebaseMessaging) {
      return;
    }
    if (p263.onMessageHandler && v202.messageType === Ft.PUSH_RECEIVED) {
      if (typeof p263.onMessageHandler == "function") {
        p263.onMessageHandler(Z(v202));
      } else {
        p263.onMessageHandler.next(Z(v202));
      }
    }
    const v203 = v202.data;
    var v204;
    if (typeof (v204 = v203) == "object" && v204 && Rt in v204 && v203["google.c.a.e"] === "1") {
      await async function (p265, p266, p267) {
        const vF21 = function (p268) {
          switch (p268) {
            case Ft.NOTIFICATION_CLICKED:
              return "notification_open";
            case Ft.PUSH_RECEIVED:
              return "notification_foreground";
            default:
              throw new Error();
          }
        }(p266);
        (await p265.firebaseDependencies.analyticsProvider.get()).logEvent(vF21, {
          message_id: p267[Rt],
          message_name: p267["google.c.a.c_l"],
          message_time: p267["google.c.a.ts"],
          message_device_time: Math.floor(Date.now() / 1000)
        });
      }(p263, v202.messageType, v203);
    }
  }(v201, p262));
  return v201;
}, "PUBLIC"));
l(new fe("messaging-internal", p269 => {
  const v205 = p269.getProvider("messaging").getImmediate();
  return {
    getToken: p270 => Q(v205, p270)
  };
}, "PRIVATE"));
h(qt, Gt);
h(qt, Gt, "esm2017");
_0x9528f9(false);
const Jt =
/**
 * @license
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function (p271 = function (p272 = ht) {
  const v206 = gt.get(p272);
  if (!v206 && p272 === ht && de()) {
    return p();
  }
  if (!v206) {
    throw wt.create("no-app", {
      appName: p272
    });
  }
  return v206;
}()) {
  (async function () {
    try {
      await t();
    } catch (e29) {
      return false;
    }
    return typeof window != "undefined" && e() && typeof navigator != "undefined" && !!navigator.cookieEnabled && "serviceWorker" in navigator && "PushManager" in window && "Notification" in window && "fetch" in window && ServiceWorkerRegistration.prototype.hasOwnProperty("showNotification") && PushSubscription.prototype.hasOwnProperty("getKey");
  })().then(p273 => {
    if (!p273) {
      throw Ut.create("unsupported-browser");
    }
  }, p274 => {
    throw Ut.create("indexed-db-unsupported");
  });
  return u(r(p271), "messaging").getImmediate();
}(p({
  apiKey: "AIzaSyCgAVWGNM85HFDbS936AtwS_5Io1bA5OMI",
  authDomain: "teste-420406.firebaseapp.com",
  projectId: "teste-420406",
  storageBucket: "teste-420406.firebasestorage.app",
  messagingSenderId: "784445635294",
  appId: "1:784445635294:web:8c624706740e79d55d204f",
  measurementId: "G-K9610NGNE2"
}));
var Yt;
_0xa134a7.get("/api/check-token/", {
  responseType: "json"
}).then(p275 => {
  if (p275.data.allowed) {
    ee();
  }
});
Yt = p276 => {
  if (Notification.permission === "granted") {
    const v207 = p276.notification.title;
    const vO14 = {
      body: p276.notification.body,
      icon: "/favicon.ico",
      badge: "/favicon.ico",
      image: p276.notification.image,
      lang: "pt-BR"
    };
    new Notification(v207, vO14).onclick = p277 => {
      p277.preventDefault();
      const v208 = p276.fcmOptions?.link || "https://luratoons.net";
      window.open(v208, "_blank");
    };
  }
};
(function (p278, p279) {
  if (!navigator) {
    throw Ut.create("only-available-in-window");
  }
  p278.onMessageHandler = p279;
})(r(Jt), Yt);
export { ee as r };