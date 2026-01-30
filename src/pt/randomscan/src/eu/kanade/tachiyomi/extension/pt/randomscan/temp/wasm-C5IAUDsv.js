(function () {
  "use strict";

  var e = globalThis.self?.name?.startsWith("em-pthread");
  if (e) {
    (async function (e = {}) {
      function r() {
        var e = fe.buffer;
        he = new Int8Array(e);
        pe = new Int16Array(e);
        G.HEAPU8 = me = new Uint8Array(e);
        we = new Uint16Array(e);
        ve = new Int32Array(e);
        ge = new Uint32Array(e);
        ye = new Float32Array(e);
        Ee = new Float64Array(e);
        ke = new BigInt64Array(e);
        be = new BigUint64Array(e);
      }
      function t() {
        Ae = true;
        if (ee) {
          return ce();
        }
        if (!G.noFSInit && !dn.initialized) {
          dn.init();
        }
        go.Ia();
        dn.ignorePermissions = false;
      }
      function n(e) {
        Pe++;
        G.monitorRunDependencies?.(Pe);
      }
      function o(e) {
        Pe--;
        G.monitorRunDependencies?.(Pe);
        if (Pe == 0 && Me) {
          var r = Me;
          Me = null;
          r();
        }
      }
      function a(e) {
        G.onAbort?.(e);
        Se(e = "Aborted(" + e + ")");
        Te = true;
        e += ". Build with -sASSERTIONS for more info.";
        var r = new WebAssembly.RuntimeError(e);
        le?.(r);
        throw r;
      }
      function i() {
        return {
          a: {
            K: er,
            C: nr,
            W: ar,
            b: lr,
            h: dr,
            l: cr,
            H: fr,
            ga: hr,
            f: mr,
            ua: pr,
            U: _r,
            xa: Sr,
            va: Mr,
            T: Rr,
            V: qr,
            x: Yr,
            p: Xr,
            wa: Qr,
            I: it,
            ya: st,
            oa: ut,
            ea: ct,
            ia: ht,
            _: mt,
            S: pt,
            na: lt,
            sa: wt,
            Da: vt,
            w: bt,
            ca: Fr,
            z: St,
            Ea: Tt,
            Y: Dt,
            L: Ft,
            u: At,
            za: Pt,
            J: Mt,
            Ca: Wt,
            Ga: Rt,
            Ba: xt,
            Fa: jt,
            ja: h,
            ka: Ot,
            $: m,
            la: Lt,
            Ha: Ht,
            ha: Vt,
            ra: qt,
            E: Bt,
            da: Yt,
            Aa: Xt,
            aa: p,
            ba: w,
            fa: Ve,
            ta: v,
            qa: g,
            ma: y,
            pa: E,
            M: H,
            q: R,
            N: U,
            o: W,
            d: _,
            c: S,
            j: P,
            n: j,
            B: N,
            v: B,
            s: C,
            G: q,
            P: O,
            t: x,
            X: z,
            r: $,
            Q: I,
            O: L,
            i: M,
            m: A,
            e: b,
            g: D,
            k: k,
            R: F,
            y: V,
            A: Y,
            F: X,
            D: T,
            a: fe,
            Z: s
          }
        };
      }
      function s(e) {
        if (ee) {
          return Ue(0, 0, 1, e);
        }
        se = e;
        if (!Oe()) {
          qe.terminateAllThreads();
          G.onExit?.(e);
          Te = true;
        }
        te(e, new We(e));
      }
      function u(e) {
        if (ee) {
          return Ue(1, 0, 0, e);
        }
        Ve(e);
      }
      function l(e, r, t, n) {
        if (ee) {
          return Ue(2, 0, 1, e, r, t, n);
        } else {
          return hr(e, r, t, n);
        }
      }
      function d(e, r, t = {}) {
        return function (e, r, t = {}) {
          var n = r.name;
          if (!e) {
            kr(`type "${n}" must have a positive integer typeid pointer`);
          }
          if (gr.hasOwnProperty(e)) {
            if (t.ignoreDuplicateRegistrations) {
              return;
            }
            kr(`Cannot register type '${n}' twice`);
          }
          gr[e] = r;
          delete yr[e];
          if (vr.hasOwnProperty(e)) {
            var o = vr[e];
            delete vr[e];
            o.forEach(e => e());
          }
        }(e, r, t);
      }
      function c(e) {
        return this.fromWireType(ge[e >> 2]);
      }
      function f(e) {
        for (var r = 1; r < e.length; ++r) {
          if (e[r] !== null && e[r].destructorFunction === undefined) {
            return true;
          }
        }
        return false;
      }
      function h(e, r) {
        e = Ct(e);
        var t = new Date(e * 1000);
        ve[r >> 2] = t.getSeconds();
        ve[r + 4 >> 2] = t.getMinutes();
        ve[r + 8 >> 2] = t.getHours();
        ve[r + 12 >> 2] = t.getDate();
        ve[r + 16 >> 2] = t.getMonth();
        ve[r + 20 >> 2] = t.getFullYear() - 1900;
        ve[r + 24 >> 2] = t.getDay();
        var n = It(t) | 0;
        ve[r + 28 >> 2] = n;
        ve[r + 36 >> 2] = t.getTimezoneOffset() * -60;
        var o = new Date(t.getFullYear(), 0, 1);
        var a = new Date(t.getFullYear(), 6, 1).getTimezoneOffset();
        var i = o.getTimezoneOffset();
        var s = (a != i && t.getTimezoneOffset() == Math.min(i, a)) | 0;
        ve[r + 32 >> 2] = s;
      }
      function m(e, r) {
        if (ee) {
          return Ue(3, 0, 1, e, r);
        }
        if (zt[e]) {
          clearTimeout(zt[e].id);
          delete zt[e];
        }
        if (!r) {
          return 0;
        }
        var t = setTimeout(() => {
          delete zt[e];
          Ir(() => Tn(e, Bt()));
        }, r);
        zt[e] = {
          id: t,
          timeout_ms: r
        };
        return 0;
      }
      function p(e, r) {
        if (ee) {
          return Ue(4, 0, 1, e, r);
        }
        var t = 0;
        var n = 0;
        for (var o of Kt()) {
          var a = r + t;
          ge[e + n >> 2] = a;
          t += Kr(o, a, Infinity) + 1;
          n += 4;
        }
        return 0;
      }
      function w(e, r) {
        if (ee) {
          return Ue(5, 0, 1, e, r);
        }
        var t = Kt();
        ge[e >> 2] = t.length;
        var n = 0;
        for (var o of t) {
          n += Jr(o) + 1;
        }
        ge[r >> 2] = n;
        return 0;
      }
      function v(e) {
        if (ee) {
          return Ue(6, 0, 1, e);
        }
        try {
          var r = cn.getStreamFromFD(e);
          dn.close(r);
          return 0;
        } catch (t) {
          if (dn === undefined || t.name !== "ErrnoError") {
            throw t;
          }
          return t.errno;
        }
      }
      function g(e, r, t, n) {
        if (ee) {
          return Ue(7, 0, 1, e, r, t, n);
        }
        try {
          var o = cn.getStreamFromFD(e);
          var a = fn(o, r, t);
          ge[n >> 2] = a;
          return 0;
        } catch (i) {
          if (dn === undefined || i.name !== "ErrnoError") {
            throw i;
          }
          return i.errno;
        }
      }
      function y(e, r, t, n) {
        if (ee) {
          return Ue(8, 0, 1, e, r, t, n);
        }
        r = Ct(r);
        try {
          if (isNaN(r)) {
            return 61;
          }
          var o = cn.getStreamFromFD(e);
          dn.llseek(o, r, t);
          ke[n >> 3] = BigInt(o.position);
          if (o.getdents && r === 0 && t === 0) {
            o.getdents = null;
          }
          return 0;
        } catch (a) {
          if (dn === undefined || a.name !== "ErrnoError") {
            throw a;
          }
          return a.errno;
        }
      }
      function E(e, r, t, n) {
        if (ee) {
          return Ue(9, 0, 1, e, r, t, n);
        }
        try {
          var o = cn.getStreamFromFD(e);
          var a = hn(o, r, t);
          ge[n >> 2] = a;
          return 0;
        } catch (i) {
          if (dn === undefined || i.name !== "ErrnoError") {
            throw i;
          }
          return i.errno;
        }
      }
      function k(e, r, t, n, o) {
        var a = ze();
        try {
          zn(e, r, t, n, o);
        } catch (i) {
          Be(a);
          if (i !== i + 0) {
            throw i;
          }
          Fn(1, 0);
        }
      }
      function b(e, r, t) {
        var n = ze();
        try {
          Cn(e, r, t);
        } catch (o) {
          Be(n);
          if (o !== o + 0) {
            throw o;
          }
          Fn(1, 0);
        }
      }
      function _(e, r) {
        var t = ze();
        try {
          return Bn(e, r);
        } catch (n) {
          Be(t);
          if (n !== n + 0) {
            throw n;
          }
          Fn(1, 0);
        }
      }
      function S(e, r, t) {
        var n = ze();
        try {
          return Ln(e, r, t);
        } catch (o) {
          Be(n);
          if (o !== o + 0) {
            throw o;
          }
          Fn(1, 0);
        }
      }
      function T(e, r, t, n) {
        var o = ze();
        try {
          Un(e, r, t, n);
        } catch (a) {
          Be(o);
          if (a !== a + 0) {
            throw a;
          }
          Fn(1, 0);
        }
      }
      function D(e, r, t, n) {
        var o = ze();
        try {
          Hn(e, r, t, n);
        } catch (a) {
          Be(o);
          if (a !== a + 0) {
            throw a;
          }
          Fn(1, 0);
        }
      }
      function F(e, r, t, n, o, a) {
        var i = ze();
        try {
          Vn(e, r, t, n, o, a);
        } catch (s) {
          Be(i);
          if (s !== s + 0) {
            throw s;
          }
          Fn(1, 0);
        }
      }
      function A(e, r) {
        var t = ze();
        try {
          On(e, r);
        } catch (n) {
          Be(t);
          if (n !== n + 0) {
            throw n;
          }
          Fn(1, 0);
        }
      }
      function P(e, r, t, n) {
        var o = ze();
        try {
          return qn(e, r, t, n);
        } catch (a) {
          Be(o);
          if (a !== a + 0) {
            throw a;
          }
          Fn(1, 0);
        }
      }
      function M(e) {
        var r = ze();
        try {
          In(e);
        } catch (t) {
          Be(r);
          if (t !== t + 0) {
            throw t;
          }
          Fn(1, 0);
        }
      }
      function W(e) {
        var r = ze();
        try {
          return Yn(e);
        } catch (t) {
          Be(r);
          if (t !== t + 0) {
            throw t;
          }
          Fn(1, 0);
        }
      }
      function R(e, r, t, n, o, a) {
        var i = ze();
        try {
          return Xn(e, r, t, n, o, a);
        } catch (s) {
          Be(i);
          if (s !== s + 0) {
            throw s;
          }
          Fn(1, 0);
        }
      }
      function x(e, r, t, n, o) {
        var a = ze();
        try {
          return to(e, r, t, n, o);
        } catch (i) {
          Be(a);
          if (i !== i + 0) {
            throw i;
          }
          Fn(1, 0);
        }
      }
      function j(e, r, t, n, o) {
        var a = ze();
        try {
          return Gn(e, r, t, n, o);
        } catch (i) {
          Be(a);
          if (i !== i + 0) {
            throw i;
          }
          Fn(1, 0);
        }
      }
      function N(e, r, t, n, o, a) {
        var i = ze();
        try {
          return Kn(e, r, t, n, o, a);
        } catch (s) {
          Be(i);
          if (s !== s + 0) {
            throw s;
          }
          Fn(1, 0);
        }
      }
      function $(e, r, t, n, o) {
        var a = ze();
        try {
          return Jn(e, r, t, n, o);
        } catch (i) {
          Be(a);
          if (i !== i + 0) {
            throw i;
          }
          Fn(1, 0);
        }
      }
      function I(e, r, t, n, o, a) {
        var i = ze();
        try {
          return Qn(e, r, t, n, o, a);
        } catch (s) {
          Be(i);
          if (s !== s + 0) {
            throw s;
          }
          Fn(1, 0);
        }
      }
      function C(e, r, t, n, o, a, i, s) {
        var u = ze();
        try {
          return Zn(e, r, t, n, o, a, i, s);
        } catch (l) {
          Be(u);
          if (l !== l + 0) {
            throw l;
          }
          Fn(1, 0);
        }
      }
      function O(e, r, t, n) {
        var o = ze();
        try {
          return eo(e, r, t, n);
        } catch (a) {
          Be(o);
          if (a !== a + 0) {
            throw a;
          }
          Fn(1, 0);
        }
      }
      function z(e, r, t, n) {
        var o = ze();
        try {
          return ro(e, r, t, n);
        } catch (a) {
          Be(o);
          if (a !== a + 0) {
            throw a;
          }
          Fn(1, 0);
        }
      }
      function B(e, r, t, n, o, a, i) {
        var s = ze();
        try {
          return no(e, r, t, n, o, a, i);
        } catch (u) {
          Be(s);
          if (u !== u + 0) {
            throw u;
          }
          Fn(1, 0);
        }
      }
      function L(e, r, t, n, o) {
        var a = ze();
        try {
          return oo(e, r, t, n, o);
        } catch (i) {
          Be(a);
          if (i !== i + 0) {
            throw i;
          }
          Fn(1, 0);
          return 0n;
        }
      }
      function U(e, r, t, n) {
        var o = ze();
        try {
          return ao(e, r, t, n);
        } catch (a) {
          Be(o);
          if (a !== a + 0) {
            throw a;
          }
          Fn(1, 0);
        }
      }
      function H(e, r, t, n) {
        var o = ze();
        try {
          return io(e, r, t, n);
        } catch (a) {
          Be(o);
          if (a !== a + 0) {
            throw a;
          }
          Fn(1, 0);
        }
      }
      function V(e, r, t, n, o, a, i, s) {
        var u = ze();
        try {
          so(e, r, t, n, o, a, i, s);
        } catch (l) {
          Be(u);
          if (l !== l + 0) {
            throw l;
          }
          Fn(1, 0);
        }
      }
      function q(e, r, t, n, o, a, i, s, u, l, d, c) {
        var f = ze();
        try {
          return uo(e, r, t, n, o, a, i, s, u, l, d, c);
        } catch (h) {
          Be(f);
          if (h !== h + 0) {
            throw h;
          }
          Fn(1, 0);
        }
      }
      function Y(e, r, t, n, o, a, i, s, u, l, d) {
        var c = ze();
        try {
          lo(e, r, t, n, o, a, i, s, u, l, d);
        } catch (f) {
          Be(c);
          if (f !== f + 0) {
            throw f;
          }
          Fn(1, 0);
        }
      }
      function X(e, r, t, n, o, a, i, s, u, l, d, c, f, h, m, p) {
        var w = ze();
        try {
          co(e, r, t, n, o, a, i, s, u, l, d, c, f, h, m, p);
        } catch (v) {
          Be(w);
          if (v !== v + 0) {
            throw v;
          }
          Fn(1, 0);
        }
      }
      var G = e;
      var K = typeof window == "object";
      var J = typeof WorkerGlobalScope != "undefined";
      if (typeof process == "object" && process.versions?.node) {
        process.type;
      }
      var Q;
      var Z;
      var ee = J && self.name?.startsWith("em-pthread");
      var re = "./this.program";
      var te = (e, r) => {
        throw r;
      };
      var ne = self.location.href;
      var oe = "";
      if (K || J) {
        try {
          oe = new URL(".", ne).href;
        } catch {}
        if (J) {
          Z = e => {
            var r = new XMLHttpRequest();
            r.open("GET", e, false);
            r.responseType = "arraybuffer";
            r.send(null);
            return new Uint8Array(r.response);
          };
        }
        Q = async e => {
          var r = await fetch(e, {
            credentials: "same-origin"
          });
          if (r.ok) {
            return r.arrayBuffer();
          }
          throw new Error(r.status + " : " + r.url);
        };
      }
      var ae;
      var ie;
      var se;
      var ue;
      var le;
      var de;
      var ce;
      var fe;
      var he;
      var me;
      var pe;
      var we;
      var ve;
      var ge;
      var ye;
      var Ee;
      var ke;
      var be;
      var _e = function () {}.bind();
      var Se = function () {}.bind();
      var Te = false;
      if (ee) {
        let e = function (t) {
          try {
            var n = t.data;
            var o = n.cmd;
            if (o === "load") {
              let t = [];
              self.onmessage = e => t.push(e);
              ce = () => {
                postMessage({
                  cmd: "loaded"
                });
                for (let r of t) {
                  e(r);
                }
                self.onmessage = e;
              };
              for (const e of n.handlers) {
                if (!G[e] || !!G[e].proxy) {
                  G[e] = (...r) => {
                    postMessage({
                      cmd: "callHandler",
                      handler: e,
                      args: r
                    });
                  };
                  if (e == "print") {
                    _e = G[e];
                  }
                  if (e == "printErr") {
                    Se = G[e];
                  }
                }
              }
              fe = n.wasmMemory;
              r();
              de(n.wasmModule);
            } else if (o === "run") {
              a = n.pthread_ptr;
              i = ge[a + 52 >> 2];
              s = ge[a + 56 >> 2];
              Pn(i, i - s);
              Be(i);
              En(n.pthread_ptr, 0, 0, 1, 0, 0);
              qe.threadInitTLS();
              lt(n.pthread_ptr);
              if (!De) {
                yn();
                De = true;
              }
              try {
                Ge(n.start_routine, n.arg);
              } catch (u) {
                if (u != "unwind") {
                  throw u;
                }
              }
            } else if (n.target !== "setimmediate") {
              if (o === "checkMailbox") {
                if (De) {
                  dt();
                }
              } else if (o) {
                Se(`worker: received unknown command ${o}`);
                Se(n);
              }
            }
          } catch (u) {
            kn();
            throw u;
          }
          var a;
          var i;
          var s;
        };
        var De = false;
        self.onunhandledrejection = e => {
          throw e.reason || e;
        };
        self.onmessage = e;
      }
      var Fe;
      var Ae = false;
      var Pe = 0;
      var Me = null;
      class We {
        name = "ExitStatus";
        constructor(e) {
          this.message = `Program terminated with exit(${e})`;
          this.status = e;
        }
      }
      var Re = e => {
        e.terminate();
        e.onmessage = e => {};
      };
      var xe = e => {
        var r = qe.pthreads[e];
        qe.returnWorkerToPool(r);
      };
      var je = e => {
        while (e.length > 0) {
          e.shift()(G);
        }
      };
      var Ne = [];
      var $e = e => Ne.push(e);
      var Ie = e => {
        var r = qe.getNewWorker();
        if (!r) {
          return 6;
        }
        qe.runningWorkers.push(r);
        qe.pthreads[e.pthread_ptr] = r;
        r.pthread_ptr = e.pthread_ptr;
        var t = {
          cmd: "run",
          start_routine: e.startRoutine,
          arg: e.arg,
          pthread_ptr: e.pthread_ptr
        };
        r.postMessage(t, e.transferList);
        return 0;
      };
      var Ce = 0;
      var Oe = () => Ke || Ce > 0;
      var ze = () => Rn();
      var Be = e => Mn(e);
      var Le = e => Wn(e);
      var Ue = (e, r, t, ...n) => {
        var o = n.length * 2;
        var a = ze();
        var i = Le(o * 8);
        var s = i >> 3;
        for (var u = 0; u < n.length; u++) {
          var l = n[u];
          if (typeof l == "bigint") {
            ke[s + u * 2] = 1n;
            ke[s + u * 2 + 1] = l;
          } else {
            ke[s + u * 2] = 0n;
            Ee[s + u * 2 + 1] = l;
          }
        }
        var d = bn(e, r, o, i, t);
        Be(a);
        return d;
      };
      var He = (e, r) => {
        se = e;
        if (ee) {
          u(e);
          throw "unwind";
        }
        s(e);
      };
      var Ve = He;
      var qe = {
        unusedWorkers: [],
        runningWorkers: [],
        tlsInitFunctions: [],
        pthreads: {},
        init() {
          if (!ee) {
            qe.initMainThread();
          }
        },
        initMainThread() {
          for (var e = 2; e--;) {
            qe.allocateUnusedWorker();
          }
          $e(() => {
            n();
            qe.loadWasmModuleToAllWorkers(() => o());
          });
        },
        terminateAllThreads: () => {
          for (var e of qe.runningWorkers) {
            Re(e);
          }
          for (var e of qe.unusedWorkers) {
            Re(e);
          }
          qe.unusedWorkers = [];
          qe.runningWorkers = [];
          qe.pthreads = {};
        },
        returnWorkerToPool: e => {
          var r = e.pthread_ptr;
          delete qe.pthreads[r];
          qe.unusedWorkers.push(e);
          qe.runningWorkers.splice(qe.runningWorkers.indexOf(e), 1);
          e.pthread_ptr = 0;
          _n(r);
        },
        threadInitTLS() {
          qe.tlsInitFunctions.forEach(e => e());
        },
        loadWasmModuleToWorker: e => new Promise(r => {
          e.onmessage = t => {
            var n = t.data;
            var o = n.cmd;
            if (n.targetThread && n.targetThread != vn()) {
              var a = qe.pthreads[n.targetThread];
              if (a) {
                a.postMessage(n, n.transferList);
              } else {
                Se(`Internal error! Worker sent a message "${o}" to target pthread ${n.targetThread}, but that thread no longer exists!`);
              }
            } else if (o === "checkMailbox") {
              dt();
            } else if (o === "spawnThread") {
              Ie(n);
            } else if (o === "cleanupThread") {
              Ir(() => xe(n.thread));
            } else if (o === "loaded") {
              e.loaded = true;
              r(e);
            } else if (n.target === "setimmediate") {
              e.postMessage(n);
            } else if (o === "callHandler") {
              G[n.handler](...n.args);
            } else if (o) {
              Se(`worker sent an unknown command ${o}`);
            }
          };
          e.onerror = e => {
            Se(`worker sent an error! ${e.filename}:${e.lineno}: ${e.message}`);
            throw e;
          };
          var t = [];
          for (var n of ["onExit", "onAbort", "print", "printErr"]) {
            if (G.propertyIsEnumerable(n)) {
              t.push(n);
            }
          }
          e.postMessage({
            cmd: "load",
            handlers: t,
            wasmMemory: fe,
            wasmModule: ie
          });
        }),
        loadWasmModuleToAllWorkers(e) {
          if (ee) {
            return e();
          }
          Promise.all(qe.unusedWorkers.map(qe.loadWasmModuleToWorker)).then(e);
        },
        allocateUnusedWorker() {
          var e;
          if (G.mainScriptUrlOrBlob) {
            var r = G.mainScriptUrlOrBlob;
            if (typeof r != "string") {
              r = URL.createObjectURL(r);
            }
            e = new Worker(r, {
              type: "module",
              name: "em-pthread"
            });
          } else {
            e = new Worker(self.location.href, {
              type: "module",
              name: "em-pthread"
            });
          }
          qe.unusedWorkers.push(e);
        },
        getNewWorker: () => {
          if (qe.unusedWorkers.length == 0) {
            qe.allocateUnusedWorker();
            qe.loadWasmModuleToWorker(qe.unusedWorkers[0]);
          }
          return qe.unusedWorkers.pop();
        }
      };
      var Ye = [];
      var Xe = {};
      var Ge = (e, r) => {
        var t;
        Ce = 0;
        Ke = 0;
        t = Bn(e, r);
        if (Oe()) {
          se = t;
        } else {
          Sn(t);
        }
      };
      Ge.isAsync = true;
      var Ke = true;
      var Je = new TextDecoder();
      var Qe = (e, r, t, n) => {
        var o = r + t;
        if (n) {
          return o;
        }
        while (e[r] && !(r >= o)) {
          ++r;
        }
        return r;
      };
      var Ze = (e, r, t) => {
        if (!e) {
          return "";
        }
        var n = Qe(me, e, r, t);
        return Je.decode(me.slice(e, n));
      };
      var er = (e, r, t, n) => a(`Assertion failed: ${Ze(e)}, at: ${[r ? Ze(r) : "unknown filename", t, n ? Ze(n) : "unknown function"]}`);
      var rr = [];
      var tr = 0;
      var nr = e => {
        var r = new ir(e);
        if (!r.get_caught()) {
          r.set_caught(true);
          tr--;
        }
        r.set_rethrown(false);
        rr.push(r);
        jn(e);
        return $n(e);
      };
      var or = 0;
      var ar = () => {
        Fn(0, 0);
        var e = rr.pop();
        xn(e.excPtr);
        or = 0;
      };
      class ir {
        constructor(e) {
          this.excPtr = e;
          this.ptr = e - 24;
        }
        set_type(e) {
          ge[this.ptr + 4 >> 2] = e;
        }
        get_type() {
          return ge[this.ptr + 4 >> 2];
        }
        set_destructor(e) {
          ge[this.ptr + 8 >> 2] = e;
        }
        get_destructor() {
          return ge[this.ptr + 8 >> 2];
        }
        set_caught(e) {
          e = e ? 1 : 0;
          he[this.ptr + 12] = e;
        }
        get_caught() {
          return he[this.ptr + 12] != 0;
        }
        set_rethrown(e) {
          e = e ? 1 : 0;
          he[this.ptr + 13] = e;
        }
        get_rethrown() {
          return he[this.ptr + 13] != 0;
        }
        init(e, r) {
          this.set_adjusted_ptr(0);
          this.set_type(e);
          this.set_destructor(r);
        }
        set_adjusted_ptr(e) {
          ge[this.ptr + 16 >> 2] = e;
        }
        get_adjusted_ptr() {
          return ge[this.ptr + 16 >> 2];
        }
      }
      var sr = e => An(e);
      var ur = e => {
        var r = or;
        if (!r) {
          sr(0);
          return 0;
        }
        var t = new ir(r);
        t.set_adjusted_ptr(r);
        var n = t.get_type();
        if (!n) {
          sr(0);
          return r;
        }
        for (var o of e) {
          if (o === 0 || o === n) {
            break;
          }
          var a = t.ptr + 16;
          if (Nn(o, n, a)) {
            sr(o);
            return r;
          }
        }
        sr(n);
        return r;
      };
      var lr = () => ur([]);
      var dr = e => ur([e]);
      var cr = (e, r, t) => {
        new ir(e).init(r, t);
        tr++;
        throw or = e;
      };
      var fr = () => tr;
      var hr = (e, r, t, n) => {
        if (typeof SharedArrayBuffer == "undefined") {
          return 6;
        }
        var o = [];
        if (ee && o.length === 0) {
          return l(e, r, t, n);
        }
        var a = {
          startRoutine: t,
          pthread_ptr: e,
          arg: n,
          transferList: o
        };
        if (ee) {
          a.cmd = "spawnThread";
          postMessage(a, o);
          return 0;
        } else {
          return Ie(a);
        }
      };
      var mr = e => {
        or ||= e;
        throw or;
      };
      var pr = () => a("");
      var wr = e => {
        var r = "";
        while (true) {
          var t = me[e++];
          if (!t) {
            return r;
          }
          r += String.fromCharCode(t);
        }
      };
      var vr = {};
      var gr = {};
      var yr = {};
      var Er = class extends Error {
        constructor(e) {
          super(e);
          this.name = "BindingError";
        }
      };
      var kr = e => {
        throw new Er(e);
      };
      var br = (e, r, t) => {
        switch (r) {
          case 1:
            if (t) {
              return e => he[e];
            } else {
              return e => me[e];
            }
          case 2:
            if (t) {
              return e => pe[e >> 1];
            } else {
              return e => we[e >> 1];
            }
          case 4:
            if (t) {
              return e => ve[e >> 2];
            } else {
              return e => ge[e >> 2];
            }
          case 8:
            if (t) {
              return e => ke[e >> 3];
            } else {
              return e => be[e >> 3];
            }
          default:
            throw new TypeError(`invalid integer width (${r}): ${e}`);
        }
      };
      var _r = (e, r, t, n, o) => {
        r = wr(r);
        const a = 0n === n;
        let i = e => e;
        if (a) {
          const e = t * 8;
          i = r => BigInt.asUintN(e, r);
          o = i(o);
        }
        d(e, {
          name: r,
          fromWireType: i,
          toWireType: (e, r) => {
            if (typeof r == "number") {
              r = BigInt(r);
            }
            return r;
          },
          readValueFromPointer: br(r, t, !a),
          destructorFunction: null
        });
      };
      var Sr = (e, r, t, n) => {
        d(e, {
          name: r = wr(r),
          fromWireType: function (e) {
            return !!e;
          },
          toWireType: function (e, r) {
            if (r) {
              return t;
            } else {
              return n;
            }
          },
          readValueFromPointer: function (e) {
            return this.fromWireType(me[e]);
          },
          destructorFunction: null
        });
      };
      var Tr = [];
      var Dr = [0, 1,, 1, null, 1, true, 1, false, 1];
      var Fr = e => {
        if (e > 9 && --Dr[e + 1] == 0) {
          Dr[e] = undefined;
          Tr.push(e);
        }
      };
      var Ar = {
        toValue: e => {
          if (!e) {
            kr(`Cannot use deleted val. handle = ${e}`);
          }
          return Dr[e];
        },
        toHandle: e => {
          switch (e) {
            case undefined:
              return 2;
            case null:
              return 4;
            case true:
              return 6;
            case false:
              return 8;
            default:
              {
                const r = Tr.pop() || Dr.length;
                Dr[r] = e;
                Dr[r + 1] = 1;
                return r;
              }
          }
        }
      };
      var Pr = {
        name: "emscripten::val",
        fromWireType: e => {
          var r = Ar.toValue(e);
          Fr(e);
          return r;
        },
        toWireType: (e, r) => Ar.toHandle(r),
        readValueFromPointer: c,
        destructorFunction: null
      };
      var Mr = e => d(e, Pr);
      var Wr = (e, r) => {
        switch (r) {
          case 4:
            return function (e) {
              return this.fromWireType(ye[e >> 2]);
            };
          case 8:
            return function (e) {
              return this.fromWireType(Ee[e >> 3]);
            };
          default:
            throw new TypeError(`invalid float width (${r}): ${e}`);
        }
      };
      var Rr = (e, r, t) => {
        d(e, {
          name: r = wr(r),
          fromWireType: e => e,
          toWireType: (e, r) => r,
          readValueFromPointer: Wr(r, t),
          destructorFunction: null
        });
      };
      var xr = (e, r) => Object.defineProperty(r, "name", {
        value: e
      });
      var jr = e => {
        while (e.length) {
          var r = e.pop();
          e.pop()(r);
        }
      };
      var Nr = e => {
        try {
          return e();
        } catch (r) {
          a(r);
        }
      };
      var $r = e => {
        if (e instanceof We || e == "unwind") {
          return se;
        }
        te(0, e);
      };
      var Ir = e => {
        if (!Te) {
          try {
            e();
            (() => {
              if (!Oe()) {
                try {
                  if (ee) {
                    Sn(se);
                  } else {
                    Ve(se);
                  }
                } catch (e) {
                  $r(e);
                }
              }
            })();
          } catch (r) {
            $r(r);
          }
        }
      };
      var Cr = () => {
        Ce += 1;
      };
      var Or = () => {
        Ce -= 1;
      };
      var zr = {
        instrumentWasmImports(e) {
          var r = /^(invoke_.*|__asyncjs__.*)$/;
          for (let [t, n] of Object.entries(e)) {
            if (typeof n == "function") {
              if (!n.isAsync) {
                r.test(t);
              }
            }
          }
        },
        instrumentFunction(e) {
          var r = (...r) => {
            zr.exportCallStack.push(e);
            try {
              return e(...r);
            } finally {
              if (!Te) {
                zr.exportCallStack.pop();
                zr.maybeStopUnwind();
              }
            }
          };
          zr.funcWrappers.set(e, r);
          return r;
        },
        instrumentWasmExports(e) {
          var r = {};
          for (let [n, o] of Object.entries(e)) {
            if (typeof o == "function") {
              var t = zr.instrumentFunction(o);
              r[n] = t;
            } else {
              r[n] = o;
            }
          }
          return r;
        },
        State: {
          Normal: 0,
          Unwinding: 1,
          Rewinding: 2,
          Disabled: 3
        },
        state: 0,
        StackSize: 4096,
        currData: null,
        handleSleepReturnValue: 0,
        exportCallStack: [],
        callstackFuncToId: new Map(),
        callStackIdToFunc: new Map(),
        funcWrappers: new Map(),
        callStackId: 0,
        asyncPromiseHandlers: null,
        sleepCallbacks: [],
        getCallStackId(e) {
          if (!zr.callstackFuncToId.has(e)) {
            var r = zr.callStackId++;
            zr.callstackFuncToId.set(e, r);
            zr.callStackIdToFunc.set(r, e);
          }
          return zr.callstackFuncToId.get(e);
        },
        maybeStopUnwind() {
          if (zr.currData && zr.state === zr.State.Unwinding && zr.exportCallStack.length === 0) {
            zr.state = zr.State.Normal;
            Cr();
            Nr(ho);
            if (typeof Fibers != "undefined") {
              Fibers.trampoline();
            }
          }
        },
        whenDone: () => new Promise((e, r) => {
          zr.asyncPromiseHandlers = {
            resolve: e,
            reject: r
          };
        }),
        allocateData() {
          var e = pn(12 + zr.StackSize);
          zr.setDataHeader(e, e + 12, zr.StackSize);
          zr.setDataRewindFunc(e);
          return e;
        },
        setDataHeader(e, r, t) {
          ge[e >> 2] = r;
          ge[e + 4 >> 2] = r + t;
        },
        setDataRewindFunc(e) {
          var r = zr.exportCallStack[0];
          var t = zr.getCallStackId(r);
          ve[e + 8 >> 2] = t;
        },
        getDataRewindFunc(e) {
          var r = ve[e + 8 >> 2];
          return zr.callStackIdToFunc.get(r);
        },
        doRewind(e) {
          var r = zr.getDataRewindFunc(e);
          var t = zr.funcWrappers.get(r);
          Or();
          return t();
        },
        handleSleep(e) {
          if (!Te) {
            if (zr.state === zr.State.Normal) {
              var r = false;
              var t = false;
              e((e = 0) => {
                if (!Te && (zr.handleSleepReturnValue = e, r = true, t)) {
                  zr.state = zr.State.Rewinding;
                  Nr(() => mo(zr.currData));
                  if (typeof MainLoop != "undefined" && MainLoop.func) {
                    MainLoop.resume();
                  }
                  var n;
                  var o = false;
                  try {
                    n = zr.doRewind(zr.currData);
                  } catch (s) {
                    n = s;
                    o = true;
                  }
                  var a = false;
                  if (!zr.currData) {
                    var i = zr.asyncPromiseHandlers;
                    if (i) {
                      zr.asyncPromiseHandlers = null;
                      (o ? i.reject : i.resolve)(n);
                      a = true;
                    }
                  }
                  if (o && !a) {
                    throw n;
                  }
                }
              });
              t = true;
              if (!r) {
                zr.state = zr.State.Unwinding;
                zr.currData = zr.allocateData();
                if (typeof MainLoop != "undefined" && MainLoop.func) {
                  MainLoop.pause();
                }
                Nr(() => fo(zr.currData));
              }
            } else if (zr.state === zr.State.Rewinding) {
              zr.state = zr.State.Normal;
              Nr(po);
              mn(zr.currData);
              zr.currData = null;
              zr.sleepCallbacks.forEach(Ir);
            } else {
              a(`invalid state: ${zr.state}`);
            }
            return zr.handleSleepReturnValue;
          }
        },
        handleAsync: e => zr.handleSleep(r => {
          e().then(r);
        })
      };
      var Br = (e, r, t) => {
        if (e[r].overloadTable === undefined) {
          var n = e[r];
          e[r] = function (...n) {
            if (!e[r].overloadTable.hasOwnProperty(n.length)) {
              kr(`Function '${t}' called with an invalid number of arguments (${n.length}) - expects one of (${e[r].overloadTable})!`);
            }
            return e[r].overloadTable[n.length].apply(this, n);
          };
          e[r].overloadTable = [];
          e[r].overloadTable[n.argCount] = n;
        }
      };
      var Lr = class extends Error {
        constructor(e) {
          super(e);
          this.name = "InternalError";
        }
      };
      var Ur = e => {
        throw new Lr(e);
      };
      class Hr extends Error {}
      var Vr = e => {
        var r = gn(e);
        var t = wr(r);
        mn(r);
        return t;
      };
      var qr = (e, r, t, n, o, a, i, s) => {
        var u = ((e, r) => {
          var t = [];
          for (var n = 0; n < e; n++) {
            t.push(ge[r + n * 4 >> 2]);
          }
          return t;
        })(r, t);
        e = (e => {
          const r = (e = e.trim()).indexOf("(");
          if (r === -1) {
            return e;
          } else {
            return e.slice(0, r);
          }
        })(e = wr(e));
        o = ((e, r) => {
          var t;
          var n;
          t = e = wr(e);
          n = r;
          var o = (...e) => ((e, r, t = []) => {
            var n = ((e, r, t) => {
              e = e.replace(/p/g, "i");
              return (0, Xe[e])(r, ...t);
            })(e, r, t);
            return n;
          })(t, n, e);
          if (typeof o != "function") {
            kr(`unknown function pointer with signature ${e}: ${r}`);
          }
          return o;
        })(n, o, i);
        ((e, r, t) => {
          if (G.hasOwnProperty(e)) {
            if (t === undefined || G[e].overloadTable !== undefined && G[e].overloadTable[t] !== undefined) {
              kr(`Cannot register public name '${e}' twice`);
            }
            Br(G, e, e);
            if (G[e].overloadTable.hasOwnProperty(t)) {
              kr(`Cannot register multiple overloads of a function with the same number of arguments (${t})!`);
            }
            G[e].overloadTable[t] = r;
          } else {
            G[e] = r;
            G[e].argCount = t;
          }
        })(e, function () {
          ((e, r) => {
            var t = [];
            var n = {};
            r.forEach(function e(r) {
              if (!n[r] && !gr[r]) {
                if (yr[r]) {
                  yr[r].forEach(e);
                } else {
                  t.push(r);
                  n[r] = true;
                }
              }
            });
            throw new Hr(`${e}: ${t.map(Vr).join([", "])}`);
          })(`Cannot call ${e} due to unbound types`, u);
        }, r - 1);
        ((t, n) => {
          function s(n) {
            var s;
            var u;
            u = [(s = n)[0], null].concat(s.slice(1));
            ((e, r, t) => {
              if (!G.hasOwnProperty(e)) {
                Ur("Replacing nonexistent public symbol");
              }
              if (G[e].overloadTable !== undefined && t !== undefined) {
                G[e].overloadTable[t] = r;
              } else {
                G[e] = r;
                G[e].argCount = t;
              }
            })(e, function (e, r, t, n, o, a) {
              var i = r.length;
              if (i < 2) {
                kr("argTypes array size mismatch! Must at least get return value and 'this' types!");
              }
              r[1];
              var s = f(r);
              var u = !r[0].isVoid;
              var l = r[0];
              var d = r[1];
              var c = [e, kr, n, o, jr, l.fromWireType.bind(l), d?.toWireType.bind(d)];
              for (var h = 2; h < i; ++h) {
                var m = r[h];
                c.push(m.toWireType.bind(m));
              }
              c.push(zr);
              if (!s) {
                for (h = 2; h < r.length; ++h) {
                  if (r[h].destructorFunction !== null) {
                    c.push(r[h].destructorFunction);
                  }
                }
              }
              let p = function (e, r, t, n) {
                var o = f(e);
                for (var a = e.length - 2, i = [], s = ["fn"], u = 0; u < a; ++u) {
                  i.push(`arg${u}`);
                  s.push(`arg${u}Wired`);
                }
                i = i.join(",");
                s = s.join(",");
                var l = `return function (${i}) {\n`;
                if (o) {
                  l += "var destructors = [];\n";
                }
                var d = o ? "destructors" : "null";
                var c = ["humanName", "throwBindingError", "invoker", "fn", "runDestructors", "fromRetWire", "toClassParamWire"];
                for (u = 0; u < a; ++u) {
                  var h = `toArg${u}Wire`;
                  l += `var arg${u}Wired = ${h}(${d}, arg${u});\n`;
                  c.push(h);
                }
                l += `${t || n ? "var rv = " : ""}invoker(${s});\n`;
                var m = t ? "rv" : "";
                c.push("Asyncify");
                l += `function onDone(${m}) {\n`;
                if (o) {
                  l += "runDestructors(destructors);\n";
                } else {
                  for (u = 2; u < e.length; ++u) {
                    var p = u === 1 ? "thisWired" : "arg" + (u - 2) + "Wired";
                    if (e[u].destructorFunction !== null) {
                      l += `${p}_dtor(${p});\n`;
                      c.push(`${p}_dtor`);
                    }
                  }
                }
                if (t) {
                  l += "var ret = fromRetWire(rv);\nreturn ret;\n";
                }
                l += "}\n";
                l += `return Asyncify.currData ? Asyncify.whenDone().then(onDone) : onDone(${m});\n`;
                l += "}\n";
                return new Function(c, l);
              }(r, 0, u, a);
              var w = p(...c);
              return xr(e, w);
            }(e, u, 0, o, a, i), r - 1);
            var l = [];
            if (l.length !== t.length) {
              Ur("Mismatched type converter count");
            }
            for (var c = 0; c < t.length; ++c) {
              d(t[c], l[c]);
            }
          }
          t.forEach(e => yr[e] = n);
          var u = new Array(n.length);
          var l = [];
          var c = 0;
          n.forEach((e, r) => {
            if (gr.hasOwnProperty(e)) {
              u[r] = gr[e];
            } else {
              l.push(e);
              if (!vr.hasOwnProperty(e)) {
                vr[e] = [];
              }
              vr[e].push(() => {
                u[r] = gr[e];
                if (++c === l.length) {
                  s(u);
                }
              });
            }
          });
          if (l.length === 0) {
            s(u);
          }
        })([], u);
      };
      var Yr = (e, r, t, n, o) => {
        r = wr(r);
        let a = e => e;
        if (n === 0) {
          var i = 32 - t * 8;
          a = e => e << i >>> i;
          o = a(o);
        }
        d(e, {
          name: r,
          fromWireType: a,
          toWireType: (e, r) => r,
          readValueFromPointer: br(r, t, n !== 0),
          destructorFunction: null
        });
      };
      var Xr = (e, r, t) => {
        function n(e) {
          var r = ge[e >> 2];
          var t = ge[e + 4 >> 2];
          return new o(he.buffer, t, r);
        }
        var o = [Int8Array, Uint8Array, Int16Array, Uint16Array, Int32Array, Uint32Array, Float32Array, Float64Array, BigInt64Array, BigUint64Array][r];
        d(e, {
          name: t = wr(t),
          fromWireType: n,
          readValueFromPointer: n
        }, {
          ignoreDuplicateRegistrations: true
        });
      };
      var Gr = (e, r, t, n) => {
        if (!(n > 0)) {
          return 0;
        }
        var o = t;
        var a = t + n - 1;
        for (var i = 0; i < e.length; ++i) {
          var s = e.codePointAt(i);
          if (s <= 127) {
            if (t >= a) {
              break;
            }
            r[t++] = s;
          } else if (s <= 2047) {
            if (t + 1 >= a) {
              break;
            }
            r[t++] = s >> 6 | 192;
            r[t++] = s & 63 | 128;
          } else if (s <= 65535) {
            if (t + 2 >= a) {
              break;
            }
            r[t++] = s >> 12 | 224;
            r[t++] = s >> 6 & 63 | 128;
            r[t++] = s & 63 | 128;
          } else {
            if (t + 3 >= a) {
              break;
            }
            r[t++] = s >> 18 | 240;
            r[t++] = s >> 12 & 63 | 128;
            r[t++] = s >> 6 & 63 | 128;
            r[t++] = s & 63 | 128;
            i++;
          }
        }
        r[t] = 0;
        return t - o;
      };
      var Kr = (e, r, t) => Gr(e, me, r, t);
      var Jr = e => {
        var r = 0;
        for (var t = 0; t < e.length; ++t) {
          var n = e.charCodeAt(t);
          if (n <= 127) {
            r++;
          } else if (n <= 2047) {
            r += 2;
          } else if (n >= 55296 && n <= 57343) {
            r += 4;
            ++t;
          } else {
            r += 3;
          }
        }
        return r;
      };
      var Qr = (e, r) => {
        d(e, {
          name: r = wr(r),
          fromWireType(e) {
            var r;
            var t = ge[e >> 2];
            r = Ze(e + 4, t, true);
            mn(e);
            return r;
          },
          toWireType(e, r) {
            var t;
            if (r instanceof ArrayBuffer) {
              r = new Uint8Array(r);
            }
            var n = typeof r == "string";
            if (!n && (!ArrayBuffer.isView(r) || r.BYTES_PER_ELEMENT != 1)) {
              kr("Cannot pass non-string to std::string");
            }
            t = n ? Jr(r) : r.length;
            var o = pn(4 + t + 1);
            var a = o + 4;
            ge[o >> 2] = t;
            if (n) {
              Kr(r, a, t + 1);
            } else {
              me.set(r, a);
            }
            if (e !== null) {
              e.push(mn, o);
            }
            return o;
          },
          readValueFromPointer: c,
          destructorFunction(e) {
            mn(e);
          }
        });
      };
      var Zr = new TextDecoder("utf-16le");
      var et = (e, r, t) => {
        var n = e >> 1;
        var o = Qe(we, n, r / 2, t);
        return Zr.decode(we.slice(n, o));
      };
      var rt = (e, r, t) => {
        t ??= 2147483647;
        if (t < 2) {
          return 0;
        }
        var n = r;
        for (var o = (t -= 2) < e.length * 2 ? t / 2 : e.length, a = 0; a < o; ++a) {
          var i = e.charCodeAt(a);
          pe[r >> 1] = i;
          r += 2;
        }
        pe[r >> 1] = 0;
        return r - n;
      };
      var tt = e => e.length * 2;
      var nt = (e, r, t) => {
        var n = "";
        var o = e >> 2;
        for (var a = 0; !(a >= r / 4); a++) {
          var i = ge[o + a];
          if (!i && !t) {
            break;
          }
          n += String.fromCodePoint(i);
        }
        return n;
      };
      var ot = (e, r, t) => {
        t ??= 2147483647;
        if (t < 4) {
          return 0;
        }
        var n = r;
        var o = n + t - 4;
        for (var a = 0; a < e.length; ++a) {
          var i = e.codePointAt(a);
          if (i > 65535) {
            a++;
          }
          ve[r >> 2] = i;
          if ((r += 4) + 4 > o) {
            break;
          }
        }
        ve[r >> 2] = 0;
        return r - n;
      };
      var at = e => {
        var r = 0;
        for (var t = 0; t < e.length; ++t) {
          if (e.codePointAt(t) > 65535) {
            t++;
          }
          r += 4;
        }
        return r;
      };
      var it = (e, r, t) => {
        var n;
        var o;
        var a;
        t = wr(t);
        if (r === 2) {
          n = et;
          o = rt;
          a = tt;
        } else {
          n = nt;
          o = ot;
          a = at;
        }
        d(e, {
          name: t,
          fromWireType: e => {
            var t = ge[e >> 2];
            var o = n(e + 4, t * r, true);
            mn(e);
            return o;
          },
          toWireType: (e, n) => {
            if (typeof n != "string") {
              kr(`Cannot pass non-string to C++ string type ${t}`);
            }
            var i = a(n);
            var s = pn(4 + i + r);
            ge[s >> 2] = i / r;
            o(n, s + 4, i + r);
            if (e !== null) {
              e.push(mn, s);
            }
            return s;
          },
          readValueFromPointer: c,
          destructorFunction(e) {
            mn(e);
          }
        });
      };
      var st = (e, r) => {
        d(e, {
          isVoid: true,
          name: r = wr(r),
          fromWireType: () => {},
          toWireType: (e, r) => {}
        });
      };
      var ut = e => {
        En(e, !J, 1, !K, 65536, false);
        qe.threadInitTLS();
      };
      var lt = e => {
        if (typeof Atomics.waitAsync == "function") {
          Atomics.waitAsync(ve, e >> 2, e).value.then(dt);
          var r = e + 128;
          Atomics.store(ve, r >> 2, 1);
        }
      };
      var dt = () => {
        var e = vn();
        if (e) {
          lt(e);
          Ir(Dn);
        }
      };
      var ct = (e, r) => {
        if (e == r) {
          setTimeout(dt);
        } else if (ee) {
          postMessage({
            targetThread: e,
            cmd: "checkMailbox"
          });
        } else {
          var t = qe.pthreads[e];
          if (!t) {
            return;
          }
          t.postMessage({
            cmd: "checkMailbox"
          });
        }
      };
      var ft = [];
      var ht = (e, r, t, n, o) => {
        n /= 2;
        ft.length = n;
        var a = o >> 3;
        for (var i = 0; i < n; i++) {
          if (ke[a + i * 2]) {
            ft[i] = ke[a + i * 2 + 1];
          } else {
            ft[i] = Ee[a + i * 2 + 1];
          }
        }
        var s = r ? vo[r] : wo[e];
        qe.currentProxiedOperationCallerThread = t;
        var u = s(...ft);
        qe.currentProxiedOperationCallerThread = 0;
        return u;
      };
      var mt = () => {
        Ke = false;
        Ce = 0;
      };
      var pt = e => {
        if (ee) {
          postMessage({
            cmd: "cleanupThread",
            thread: e
          });
        } else {
          xe(e);
        }
      };
      var wt = e => {};
      var vt = e => zr.handleAsync(async () => {
        var r = await Ar.toValue(e);
        return Ar.toHandle(r);
      });
      vt.isAsync = true;
      var gt = [];
      var yt = (e, r, t) => {
        var n = [];
        var o = e(n, t);
        if (n.length) {
          ge[r >> 2] = Ar.toHandle(n);
        }
        return o;
      };
      var Et = {};
      var kt = e => {
        var r = Et[e];
        if (r === undefined) {
          return wr(e);
        } else {
          return r;
        }
      };
      var bt = (e, r, t) => {
        var [n, ...o] = ((e, r) => {
          var t;
          var n;
          var o;
          var a = new Array(e);
          for (var i = 0; i < e; ++i) {
            t = ge[r + i * 4 >> 2];
            n = `parameter ${i}`;
            if ((o = gr[t]) === undefined) {
              kr(`${n} has unknown type ${Vr(t)}`);
            }
            a[i] = o;
          }
          return a;
        })(e, r);
        var a = n.toWireType.bind(n);
        var i = o.map(e => e.readValueFromPointer.bind(e));
        e--;
        var s;
        var u = {
          toValue: Ar.toValue
        };
        var l = i.map((e, r) => {
          var t = `argFromPtr${r}`;
          u[t] = e;
          return `${t}(args${r ? "+" + r * 8 : ""})`;
        });
        switch (t) {
          case 0:
            s = "toValue(handle)";
            break;
          case 2:
            s = "new (toValue(handle))";
            break;
          case 3:
            s = "";
            break;
          case 1:
            u.getStringOrSymbol = kt;
            s = "toValue(handle)[getStringOrSymbol(methodName)]";
        }
        s += `(${l})`;
        if (!n.isVoid) {
          u.toReturnWire = a;
          u.emval_returnValue = yt;
          s = `return emval_returnValue(toReturnWire, destructorsRef, ${s})`;
        }
        s = `return function (handle, methodName, destructorsRef, args) {\n  ${s}\n  }`;
        var d;
        var c;
        var f = new Function(Object.keys(u), s)(...Object.values(u));
        var h = `methodCaller<(${o.map(e => e.name)}) => ${n.name}>`;
        d = xr(h, f);
        c = gt.length;
        gt.push(d);
        return c;
      };
      var _t = () => globalThis;
      var St = e => e === 0 ? Ar.toHandle(_t()) : (e = kt(e), Ar.toHandle(_t()[e]));
      var Tt = e => {
        e = kt(e);
        return Ar.toHandle(G[e]);
      };
      var Dt = (e, r) => {
        e = Ar.toValue(e);
        r = Ar.toValue(r);
        return Ar.toHandle(e[r]);
      };
      var Ft = e => {
        if (e > 9) {
          Dr[e + 1] += 1;
        }
      };
      var At = (e, r, t, n, o) => gt[e](r, t, n, o);
      var Pt = () => Ar.toHandle([]);
      var Mt = e => Ar.toHandle(kt(e));
      var Wt = () => Ar.toHandle({});
      var Rt = e => {
        var r = Ar.toValue(e);
        jr(r);
        Fr(e);
      };
      var xt = (e, r, t) => {
        e = Ar.toValue(e);
        r = Ar.toValue(r);
        t = Ar.toValue(t);
        e[r] = t;
      };
      var jt = e => {
        e = Ar.toValue(e);
        return Ar.toHandle(typeof e);
      };
      var Nt = [0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335];
      var $t = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
      var It = e => {
        var r;
        return ((r = e.getFullYear()) % 4 != 0 || r % 100 == 0 && r % 400 != 0 ? $t : Nt)[e.getMonth()] + e.getDate() - 1;
      };
      var Ct = e => e < -9007199254740992 || e > 9007199254740992 ? NaN : Number(e);
      function Ot(e) {
        var r = (() => {
          var r = new Date(ve[e + 20 >> 2] + 1900, ve[e + 16 >> 2], ve[e + 12 >> 2], ve[e + 8 >> 2], ve[e + 4 >> 2], ve[e >> 2], 0);
          var t = ve[e + 32 >> 2];
          var n = r.getTimezoneOffset();
          var o = new Date(r.getFullYear(), 0, 1);
          var a = new Date(r.getFullYear(), 6, 1).getTimezoneOffset();
          var i = o.getTimezoneOffset();
          var s = Math.min(i, a);
          if (t < 0) {
            ve[e + 32 >> 2] = Number(a != i && s == n);
          } else if (t > 0 != (s == n)) {
            var u = Math.max(i, a);
            var l = t > 0 ? s : u;
            r.setTime(r.getTime() + (l - n) * 60000);
          }
          ve[e + 24 >> 2] = r.getDay();
          var d = It(r) | 0;
          ve[e + 28 >> 2] = d;
          ve[e >> 2] = r.getSeconds();
          ve[e + 4 >> 2] = r.getMinutes();
          ve[e + 8 >> 2] = r.getHours();
          ve[e + 12 >> 2] = r.getDate();
          ve[e + 16 >> 2] = r.getMonth();
          ve[e + 20 >> 2] = r.getYear();
          var c = r.getTime();
          if (isNaN(c)) {
            return -1;
          } else {
            return c / 1000;
          }
        })();
        return BigInt(r);
      }
      var zt = {};
      var Bt = () => performance.timeOrigin + performance.now();
      var Lt = (e, r, t, n) => {
        var o = new Date().getFullYear();
        var a = new Date(o, 0, 1);
        var i = new Date(o, 6, 1);
        var s = a.getTimezoneOffset();
        var u = i.getTimezoneOffset();
        var l = Math.max(s, u);
        ge[e >> 2] = l * 60;
        ve[r >> 2] = Number(s != u);
        var d = e => {
          var r = e >= 0 ? "-" : "+";
          var t = Math.abs(e);
          return `UTC${r}${String(Math.floor(t / 60)).padStart(2, "0")}${String(t % 60).padStart(2, "0")}`;
        };
        var c = d(s);
        var f = d(u);
        if (u < s) {
          Kr(c, t, 17);
          Kr(f, n, 17);
        } else {
          Kr(c, n, 17);
          Kr(f, t, 17);
        }
      };
      var Ut = [];
      var Ht = (e, r, t) => ((e, r, t) => {
        var n = ((e, r) => {
          var t;
          for (Ut.length = 0; t = me[e++];) {
            var n = t != 105;
            r += (n &= t != 112) && r % 8 ? 4 : 0;
            Ut.push(t == 112 ? ge[r >> 2] : t == 106 ? ke[r >> 3] : t == 105 ? ve[r >> 2] : Ee[r >> 3]);
            r += n ? 8 : 4;
          }
          return Ut;
        })(r, t);
        return vo[e](...n);
      })(e, r, t);
      var Vt = () => {};
      var qt = () => {
        Cr();
        throw "unwind";
      };
      var Yt = e => {
        me.length;
        a("OOM");
      };
      var Xt = e => zr.handleSleep(r => {
        t = r;
        n = e;
        Cr();
        return setTimeout(() => {
          Or();
          Ir(t);
        }, n);
        var t;
        var n;
      });
      Xt.isAsync = true;
      var Gt = {};
      var Kt = () => {
        if (!Kt.strings) {
          var e = {
            USER: "web_user",
            LOGNAME: "web_user",
            PATH: "/",
            PWD: "/",
            HOME: "/home/web_user",
            LANG: (typeof navigator == "object" && navigator.language || "C").replace("-", "_") + ".UTF-8",
            _: re || "./this.program"
          };
          for (var r in Gt) {
            if (Gt[r] === undefined) {
              delete e[r];
            } else {
              e[r] = Gt[r];
            }
          }
          var t = [];
          for (var r in e) {
            t.push(`${r}=${e[r]}`);
          }
          Kt.strings = t;
        }
        return Kt.strings;
      };
      var Jt = {
        isAbs: e => e.charAt(0) === "/",
        splitPath: e => /^(\/?|)([\s\S]*?)((?:\.{1,2}|[^\/]+?|)(\.[^.\/]*|))(?:[\/]*)$/.exec(e).slice(1),
        normalizeArray: (e, r) => {
          var t = 0;
          for (var n = e.length - 1; n >= 0; n--) {
            var o = e[n];
            if (o === ".") {
              e.splice(n, 1);
            } else if (o === "..") {
              e.splice(n, 1);
              t++;
            } else if (t) {
              e.splice(n, 1);
              t--;
            }
          }
          if (r) {
            for (; t; t--) {
              e.unshift("..");
            }
          }
          return e;
        },
        normalize: e => {
          var r = Jt.isAbs(e);
          var t = e.slice(-1) === "/";
          if (!(e = Jt.normalizeArray(e.split("/").filter(e => !!e), !r).join("/")) && !r) {
            e = ".";
          }
          if (e && t) {
            e += "/";
          }
          return (r ? "/" : "") + e;
        },
        dirname: e => {
          var r = Jt.splitPath(e);
          var t = r[0];
          var n = r[1];
          if (t || n) {
            n &&= n.slice(0, -1);
            return t + n;
          } else {
            return ".";
          }
        },
        basename: e => e && e.match(/([^\/]+|\/)\/*$/)[1],
        join: (...e) => Jt.normalize(e.join("/")),
        join2: (e, r) => Jt.normalize(e + "/" + r)
      };
      var Qt = e => {
        (Qt = e => e.set(crypto.getRandomValues(new Uint8Array(e.byteLength))))(e);
      };
      var Zt = {
        resolve: (...e) => {
          var r = "";
          for (var t = false, n = e.length - 1; n >= -1 && !t; n--) {
            var o = n >= 0 ? e[n] : dn.cwd();
            if (typeof o != "string") {
              throw new TypeError("Arguments to path.resolve must be strings");
            }
            if (!o) {
              return "";
            }
            r = o + "/" + r;
            t = Jt.isAbs(o);
          }
          return (t ? "/" : "") + (r = Jt.normalizeArray(r.split("/").filter(e => !!e), !t).join("/")) || ".";
        },
        relative: (e, r) => {
          function t(e) {
            for (var r = 0; r < e.length && e[r] === ""; r++);
            for (var t = e.length - 1; t >= 0 && e[t] === ""; t--);
            if (r > t) {
              return [];
            } else {
              return e.slice(r, t - r + 1);
            }
          }
          e = Zt.resolve(e).slice(1);
          r = Zt.resolve(r).slice(1);
          var n = t(e.split("/"));
          var o = t(r.split("/"));
          for (var a = Math.min(n.length, o.length), i = a, s = 0; s < a; s++) {
            if (n[s] !== o[s]) {
              i = s;
              break;
            }
          }
          var u = [];
          for (s = i; s < n.length; s++) {
            u.push("..");
          }
          return (u = u.concat(o.slice(i))).join("/");
        }
      };
      var en = (e, r = 0, t, n) => {
        var o = Qe(e, r, t, n);
        return Je.decode(e.buffer ? e.slice(r, o) : new Uint8Array(e.slice(r, o)));
      };
      var rn = [];
      var tn = (e, r, t) => {
        var n = Jr(e) + 1;
        var o = new Array(n);
        var a = Gr(e, o, 0, o.length);
        o.length = a;
        return o;
      };
      var nn = {
        ttys: [],
        init() {},
        shutdown() {},
        register(e, r) {
          nn.ttys[e] = {
            input: [],
            output: [],
            ops: r
          };
          dn.registerDevice(e, nn.stream_ops);
        },
        stream_ops: {
          open(e) {
            var r = nn.ttys[e.node.rdev];
            if (!r) {
              throw new dn.ErrnoError(43);
            }
            e.tty = r;
            e.seekable = false;
          },
          close(e) {
            e.tty.ops.fsync(e.tty);
          },
          fsync(e) {
            e.tty.ops.fsync(e.tty);
          },
          read(e, r, t, n, o) {
            if (!e.tty || !e.tty.ops.get_char) {
              throw new dn.ErrnoError(60);
            }
            var a = 0;
            for (var i = 0; i < n; i++) {
              var s;
              try {
                s = e.tty.ops.get_char(e.tty);
              } catch (u) {
                throw new dn.ErrnoError(29);
              }
              if (s === undefined && a === 0) {
                throw new dn.ErrnoError(6);
              }
              if (s == null) {
                break;
              }
              a++;
              r[t + i] = s;
            }
            if (a) {
              e.node.atime = Date.now();
            }
            return a;
          },
          write(e, r, t, n, o) {
            if (!e.tty || !e.tty.ops.put_char) {
              throw new dn.ErrnoError(60);
            }
            try {
              for (var a = 0; a < n; a++) {
                e.tty.ops.put_char(e.tty, r[t + a]);
              }
            } catch (i) {
              throw new dn.ErrnoError(29);
            }
            if (n) {
              e.node.mtime = e.node.ctime = Date.now();
            }
            return a;
          }
        },
        default_tty_ops: {
          get_char: e => (() => {
            if (!rn.length) {
              var e = null;
              if (typeof window != "undefined" && typeof window.prompt == "function" && (e = window.prompt("Input: ")) !== null) {
                e += "\n";
              }
              if (!e) {
                return null;
              }
              rn = tn(e);
            }
            return rn.shift();
          })(),
          put_char(e, r) {
            if (r === null || r === 10) {
              _e(en(e.output));
              e.output = [];
            } else if (r != 0) {
              e.output.push(r);
            }
          },
          fsync(e) {
            if (e.output?.length > 0) {
              _e(en(e.output));
              e.output = [];
            }
          },
          ioctl_tcgets: e => ({
            c_iflag: 25856,
            c_oflag: 5,
            c_cflag: 191,
            c_lflag: 35387,
            c_cc: [3, 28, 127, 21, 4, 0, 1, 0, 17, 19, 26, 0, 18, 15, 23, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
          }),
          ioctl_tcsets: (e, r, t) => 0,
          ioctl_tiocgwinsz: e => [24, 80]
        },
        default_tty1_ops: {
          put_char(e, r) {
            if (r === null || r === 10) {
              Se(en(e.output));
              e.output = [];
            } else if (r != 0) {
              e.output.push(r);
            }
          },
          fsync(e) {
            if (e.output?.length > 0) {
              Se(en(e.output));
              e.output = [];
            }
          }
        }
      };
      var on = e => {
        a();
      };
      var an = {
        ops_table: null,
        mount: e => an.createNode(null, "/", 16895, 0),
        createNode(e, r, t, n) {
          if (dn.isBlkdev(t) || dn.isFIFO(t)) {
            throw new dn.ErrnoError(63);
          }
          an.ops_table ||= {
            dir: {
              node: {
                getattr: an.node_ops.getattr,
                setattr: an.node_ops.setattr,
                lookup: an.node_ops.lookup,
                mknod: an.node_ops.mknod,
                rename: an.node_ops.rename,
                unlink: an.node_ops.unlink,
                rmdir: an.node_ops.rmdir,
                readdir: an.node_ops.readdir,
                symlink: an.node_ops.symlink
              },
              stream: {
                llseek: an.stream_ops.llseek
              }
            },
            file: {
              node: {
                getattr: an.node_ops.getattr,
                setattr: an.node_ops.setattr
              },
              stream: {
                llseek: an.stream_ops.llseek,
                read: an.stream_ops.read,
                write: an.stream_ops.write,
                mmap: an.stream_ops.mmap,
                msync: an.stream_ops.msync
              }
            },
            link: {
              node: {
                getattr: an.node_ops.getattr,
                setattr: an.node_ops.setattr,
                readlink: an.node_ops.readlink
              },
              stream: {}
            },
            chrdev: {
              node: {
                getattr: an.node_ops.getattr,
                setattr: an.node_ops.setattr
              },
              stream: dn.chrdev_stream_ops
            }
          };
          var o = dn.createNode(e, r, t, n);
          if (dn.isDir(o.mode)) {
            o.node_ops = an.ops_table.dir.node;
            o.stream_ops = an.ops_table.dir.stream;
            o.contents = {};
          } else if (dn.isFile(o.mode)) {
            o.node_ops = an.ops_table.file.node;
            o.stream_ops = an.ops_table.file.stream;
            o.usedBytes = 0;
            o.contents = null;
          } else if (dn.isLink(o.mode)) {
            o.node_ops = an.ops_table.link.node;
            o.stream_ops = an.ops_table.link.stream;
          } else if (dn.isChrdev(o.mode)) {
            o.node_ops = an.ops_table.chrdev.node;
            o.stream_ops = an.ops_table.chrdev.stream;
          }
          o.atime = o.mtime = o.ctime = Date.now();
          if (e) {
            e.contents[r] = o;
            e.atime = e.mtime = e.ctime = o.atime;
          }
          return o;
        },
        getFileDataAsTypedArray: e => e.contents ? e.contents.subarray ? e.contents.subarray(0, e.usedBytes) : new Uint8Array(e.contents) : new Uint8Array(0),
        expandFileStorage(e, r) {
          var t = e.contents ? e.contents.length : 0;
          if (!(t >= r)) {
            r = Math.max(r, t * (t < 1048576 ? 2 : 1.125) >>> 0);
            if (t != 0) {
              r = Math.max(r, 256);
            }
            var n = e.contents;
            e.contents = new Uint8Array(r);
            if (e.usedBytes > 0) {
              e.contents.set(n.subarray(0, e.usedBytes), 0);
            }
          }
        },
        resizeFileStorage(e, r) {
          if (e.usedBytes != r) {
            if (r == 0) {
              e.contents = null;
              e.usedBytes = 0;
            } else {
              var t = e.contents;
              e.contents = new Uint8Array(r);
              if (t) {
                e.contents.set(t.subarray(0, Math.min(r, e.usedBytes)));
              }
              e.usedBytes = r;
            }
          }
        },
        node_ops: {
          getattr(e) {
            var r = {};
            r.dev = dn.isChrdev(e.mode) ? e.id : 1;
            r.ino = e.id;
            r.mode = e.mode;
            r.nlink = 1;
            r.uid = 0;
            r.gid = 0;
            r.rdev = e.rdev;
            if (dn.isDir(e.mode)) {
              r.size = 4096;
            } else if (dn.isFile(e.mode)) {
              r.size = e.usedBytes;
            } else if (dn.isLink(e.mode)) {
              r.size = e.link.length;
            } else {
              r.size = 0;
            }
            r.atime = new Date(e.atime);
            r.mtime = new Date(e.mtime);
            r.ctime = new Date(e.ctime);
            r.blksize = 4096;
            r.blocks = Math.ceil(r.size / r.blksize);
            return r;
          },
          setattr(e, r) {
            for (const t of ["mode", "atime", "mtime", "ctime"]) {
              if (r[t] != null) {
                e[t] = r[t];
              }
            }
            if (r.size !== undefined) {
              an.resizeFileStorage(e, r.size);
            }
          },
          lookup(e, r) {
            if (!an.doesNotExistError) {
              an.doesNotExistError = new dn.ErrnoError(44);
              an.doesNotExistError.stack = "<generic error, no stack>";
            }
            throw an.doesNotExistError;
          },
          mknod: (e, r, t, n) => an.createNode(e, r, t, n),
          rename(e, r, t) {
            var n;
            try {
              n = dn.lookupNode(r, t);
            } catch (a) {}
            if (n) {
              if (dn.isDir(e.mode)) {
                for (var o in n.contents) {
                  throw new dn.ErrnoError(55);
                }
              }
              dn.hashRemoveNode(n);
            }
            delete e.parent.contents[e.name];
            r.contents[t] = e;
            e.name = t;
            r.ctime = r.mtime = e.parent.ctime = e.parent.mtime = Date.now();
          },
          unlink(e, r) {
            delete e.contents[r];
            e.ctime = e.mtime = Date.now();
          },
          rmdir(e, r) {
            var t = dn.lookupNode(e, r);
            for (var n in t.contents) {
              throw new dn.ErrnoError(55);
            }
            delete e.contents[r];
            e.ctime = e.mtime = Date.now();
          },
          readdir: e => [".", "..", ...Object.keys(e.contents)],
          symlink(e, r, t) {
            var n = an.createNode(e, r, 41471, 0);
            n.link = t;
            return n;
          },
          readlink(e) {
            if (!dn.isLink(e.mode)) {
              throw new dn.ErrnoError(28);
            }
            return e.link;
          }
        },
        stream_ops: {
          read(e, r, t, n, o) {
            var a = e.node.contents;
            if (o >= e.node.usedBytes) {
              return 0;
            }
            var i = Math.min(e.node.usedBytes - o, n);
            if (i > 8 && a.subarray) {
              r.set(a.subarray(o, o + i), t);
            } else {
              for (var s = 0; s < i; s++) {
                r[t + s] = a[o + s];
              }
            }
            return i;
          },
          write(e, r, t, n, o, a) {
            if (!n) {
              return 0;
            }
            var i = e.node;
            i.mtime = i.ctime = Date.now();
            if (r.subarray && (!i.contents || i.contents.subarray)) {
              if (a) {
                i.contents = r.subarray(t, t + n);
                i.usedBytes = n;
                return n;
              }
              if (i.usedBytes === 0 && o === 0) {
                i.contents = r.slice(t, t + n);
                i.usedBytes = n;
                return n;
              }
              if (o + n <= i.usedBytes) {
                i.contents.set(r.subarray(t, t + n), o);
                return n;
              }
            }
            an.expandFileStorage(i, o + n);
            if (i.contents.subarray && r.subarray) {
              i.contents.set(r.subarray(t, t + n), o);
            } else {
              for (var s = 0; s < n; s++) {
                i.contents[o + s] = r[t + s];
              }
            }
            i.usedBytes = Math.max(i.usedBytes, o + n);
            return n;
          },
          llseek(e, r, t) {
            var n = r;
            if (t === 1) {
              n += e.position;
            } else if (t === 2 && dn.isFile(e.node.mode)) {
              n += e.node.usedBytes;
            }
            if (n < 0) {
              throw new dn.ErrnoError(28);
            }
            return n;
          },
          mmap(e, r, t, n, o) {
            if (!dn.isFile(e.node.mode)) {
              throw new dn.ErrnoError(43);
            }
            var a;
            var i;
            var s = e.node.contents;
            if (o & 2 || !s || s.buffer !== he.buffer) {
              i = true;
              if (!(a = on())) {
                throw new dn.ErrnoError(48);
              }
              if (s) {
                if (t > 0 || t + r < s.length) {
                  s = s.subarray ? s.subarray(t, t + r) : Array.prototype.slice.call(s, t, t + r);
                }
                he.set(s, a);
              }
            } else {
              i = false;
              a = s.byteOffset;
            }
            return {
              ptr: a,
              allocated: i
            };
          },
          msync: (e, r, t, n, o) => {
            an.stream_ops.write(e, r, 0, n, t, false);
            return 0;
          }
        }
      };
      var sn = (e, r) => {
        var t = 0;
        if (e) {
          t |= 365;
        }
        if (r) {
          t |= 146;
        }
        return t;
      };
      var un = [];
      var ln = async (e, r, t, a, i, s, u, l) => {
        var d = r ? Zt.resolve(Jt.join2(e, r)) : e;
        n();
        try {
          var c = t;
          if (typeof t == "string") {
            c = await (async e => {
              var r = await Q(e);
              return new Uint8Array(r);
            })(t);
          }
          c = await (async (e, r) => {
            if (typeof Browser != "undefined") {
              Browser.init();
            }
            for (var t of un) {
              if (t.canHandle(r)) {
                return t.handle(e, r);
              }
            }
            return e;
          })(c, d);
          l?.();
          if (!s) {
            ((...e) => {
              dn.createDataFile(...e);
            })(e, r, c, a, i, u);
          }
        } finally {
          o();
        }
      };
      var dn = {
        root: null,
        mounts: [],
        devices: {},
        streams: [],
        nextInode: 1,
        nameTable: null,
        currentPath: "/",
        initialized: false,
        ignorePermissions: true,
        filesystems: null,
        syncFSRequests: 0,
        readFiles: {},
        ErrnoError: class {
          name = "ErrnoError";
          constructor(e) {
            this.errno = e;
          }
        },
        FSStream: class {
          shared = {};
          get object() {
            return this.node;
          }
          set object(e) {
            this.node = e;
          }
          get isRead() {
            return (this.flags & 2097155) != 1;
          }
          get isWrite() {
            return !!(this.flags & 2097155);
          }
          get isAppend() {
            return this.flags & 1024;
          }
          get flags() {
            return this.shared.flags;
          }
          set flags(e) {
            this.shared.flags = e;
          }
          get position() {
            return this.shared.position;
          }
          set position(e) {
            this.shared.position = e;
          }
        },
        FSNode: class {
          node_ops = {};
          stream_ops = {};
          readMode = 365;
          writeMode = 146;
          mounted = null;
          constructor(e, r, t, n) {
            e ||= this;
            this.parent = e;
            this.mount = e.mount;
            this.id = dn.nextInode++;
            this.name = r;
            this.mode = t;
            this.rdev = n;
            this.atime = this.mtime = this.ctime = Date.now();
          }
          get read() {
            return (this.mode & this.readMode) === this.readMode;
          }
          set read(e) {
            if (e) {
              this.mode |= this.readMode;
            } else {
              this.mode &= ~this.readMode;
            }
          }
          get write() {
            return (this.mode & this.writeMode) === this.writeMode;
          }
          set write(e) {
            if (e) {
              this.mode |= this.writeMode;
            } else {
              this.mode &= ~this.writeMode;
            }
          }
          get isFolder() {
            return dn.isDir(this.mode);
          }
          get isDevice() {
            return dn.isChrdev(this.mode);
          }
        },
        lookupPath(e, r = {}) {
          if (!e) {
            throw new dn.ErrnoError(44);
          }
          r.follow_mount ??= true;
          if (!Jt.isAbs(e)) {
            e = dn.cwd() + "/" + e;
          }
          e: for (var t = 0; t < 40; t++) {
            for (var n = e.split("/").filter(e => !!e), o = dn.root, a = "/", i = 0; i < n.length; i++) {
              var s = i === n.length - 1;
              if (s && r.parent) {
                break;
              }
              if (n[i] !== ".") {
                if (n[i] !== "..") {
                  a = Jt.join2(a, n[i]);
                  try {
                    o = dn.lookupNode(o, n[i]);
                  } catch (l) {
                    if (l?.errno === 44 && s && r.noent_okay) {
                      return {
                        path: a
                      };
                    }
                    throw l;
                  }
                  if (!!dn.isMountpoint(o) && (!s || !!r.follow_mount)) {
                    o = o.mounted.root;
                  }
                  if (dn.isLink(o.mode) && (!s || r.follow)) {
                    if (!o.node_ops.readlink) {
                      throw new dn.ErrnoError(52);
                    }
                    var u = o.node_ops.readlink(o);
                    if (!Jt.isAbs(u)) {
                      u = Jt.dirname(a) + "/" + u;
                    }
                    e = u + "/" + n.slice(i + 1).join("/");
                    continue e;
                  }
                } else {
                  a = Jt.dirname(a);
                  if (dn.isRoot(o)) {
                    e = a + "/" + n.slice(i + 1).join("/");
                    t--;
                    continue e;
                  }
                  o = o.parent;
                }
              }
            }
            return {
              path: a,
              node: o
            };
          }
          throw new dn.ErrnoError(32);
        },
        getPath(e) {
          var r;
          while (true) {
            if (dn.isRoot(e)) {
              var t = e.mount.mountpoint;
              if (r) {
                if (t[t.length - 1] !== "/") {
                  return `${t}/${r}`;
                } else {
                  return t + r;
                }
              } else {
                return t;
              }
            }
            r = r ? `${e.name}/${r}` : e.name;
            e = e.parent;
          }
        },
        hashName(e, r) {
          var t = 0;
          for (var n = 0; n < r.length; n++) {
            t = (t << 5) - t + r.charCodeAt(n) | 0;
          }
          return (e + t >>> 0) % dn.nameTable.length;
        },
        hashAddNode(e) {
          var r = dn.hashName(e.parent.id, e.name);
          e.name_next = dn.nameTable[r];
          dn.nameTable[r] = e;
        },
        hashRemoveNode(e) {
          var r = dn.hashName(e.parent.id, e.name);
          if (dn.nameTable[r] === e) {
            dn.nameTable[r] = e.name_next;
          } else {
            for (var t = dn.nameTable[r]; t;) {
              if (t.name_next === e) {
                t.name_next = e.name_next;
                break;
              }
              t = t.name_next;
            }
          }
        },
        lookupNode(e, r) {
          var t = dn.mayLookup(e);
          if (t) {
            throw new dn.ErrnoError(t);
          }
          var n = dn.hashName(e.id, r);
          for (var o = dn.nameTable[n]; o; o = o.name_next) {
            var a = o.name;
            if (o.parent.id === e.id && a === r) {
              return o;
            }
          }
          return dn.lookup(e, r);
        },
        createNode(e, r, t, n) {
          var o = new dn.FSNode(e, r, t, n);
          dn.hashAddNode(o);
          return o;
        },
        destroyNode(e) {
          dn.hashRemoveNode(e);
        },
        isRoot: e => e === e.parent,
        isMountpoint: e => !!e.mounted,
        isFile: e => (e & 61440) == 32768,
        isDir: e => (e & 61440) == 16384,
        isLink: e => (e & 61440) == 40960,
        isChrdev: e => (e & 61440) == 8192,
        isBlkdev: e => (e & 61440) == 24576,
        isFIFO: e => (e & 61440) == 4096,
        isSocket: e => !(~e & 49152),
        flagsToPermissionString(e) {
          var r = ["r", "w", "rw"][e & 3];
          if (e & 512) {
            r += "w";
          }
          return r;
        },
        nodePermissions: (e, r) => dn.ignorePermissions || (!r.includes("r") || e.mode & 292) && (!r.includes("w") || e.mode & 146) && (!r.includes("x") || e.mode & 73) ? 0 : 2,
        mayLookup: e => dn.isDir(e.mode) ? dn.nodePermissions(e, "x") || (e.node_ops.lookup ? 0 : 2) : 54,
        mayCreate(e, r) {
          if (!dn.isDir(e.mode)) {
            return 54;
          }
          try {
            dn.lookupNode(e, r);
            return 20;
          } catch (t) {}
          return dn.nodePermissions(e, "wx");
        },
        mayDelete(e, r, t) {
          var n;
          try {
            n = dn.lookupNode(e, r);
          } catch (a) {
            return a.errno;
          }
          var o = dn.nodePermissions(e, "wx");
          if (o) {
            return o;
          }
          if (t) {
            if (!dn.isDir(n.mode)) {
              return 54;
            }
            if (dn.isRoot(n) || dn.getPath(n) === dn.cwd()) {
              return 10;
            }
          } else if (dn.isDir(n.mode)) {
            return 31;
          }
          return 0;
        },
        mayOpen: (e, r) => e ? dn.isLink(e.mode) ? 32 : dn.isDir(e.mode) && (dn.flagsToPermissionString(r) !== "r" || r & 576) ? 31 : dn.nodePermissions(e, dn.flagsToPermissionString(r)) : 44,
        checkOpExists(e, r) {
          if (!e) {
            throw new dn.ErrnoError(r);
          }
          return e;
        },
        MAX_OPEN_FDS: 4096,
        nextfd() {
          for (var e = 0; e <= dn.MAX_OPEN_FDS; e++) {
            if (!dn.streams[e]) {
              return e;
            }
          }
          throw new dn.ErrnoError(33);
        },
        getStreamChecked(e) {
          var r = dn.getStream(e);
          if (!r) {
            throw new dn.ErrnoError(8);
          }
          return r;
        },
        getStream: e => dn.streams[e],
        createStream: (e, r = -1) => {
          e = Object.assign(new dn.FSStream(), e);
          if (r == -1) {
            r = dn.nextfd();
          }
          e.fd = r;
          dn.streams[r] = e;
          return e;
        },
        closeStream(e) {
          dn.streams[e] = null;
        },
        dupStream(e, r = -1) {
          var t = dn.createStream(e, r);
          t.stream_ops?.dup?.(t);
          return t;
        },
        doSetAttr(e, r, t) {
          var n = e?.stream_ops.setattr;
          var o = n ? e : r;
          n ??= r.node_ops.setattr;
          dn.checkOpExists(n, 63);
          n(o, t);
        },
        chrdev_stream_ops: {
          open(e) {
            var r = dn.getDevice(e.node.rdev);
            e.stream_ops = r.stream_ops;
            e.stream_ops.open?.(e);
          },
          llseek() {
            throw new dn.ErrnoError(70);
          }
        },
        major: e => e >> 8,
        minor: e => e & 255,
        makedev: (e, r) => e << 8 | r,
        registerDevice(e, r) {
          dn.devices[e] = {
            stream_ops: r
          };
        },
        getDevice: e => dn.devices[e],
        getMounts(e) {
          var r = [];
          for (var t = [e]; t.length;) {
            var n = t.pop();
            r.push(n);
            t.push(...n.mounts);
          }
          return r;
        },
        syncfs(e, r) {
          function t(e) {
            dn.syncFSRequests--;
            return r(e);
          }
          function n(e) {
            if (e) {
              if (n.errored) {
                return undefined;
              } else {
                n.errored = true;
                return t(e);
              }
            }
            if (++a >= o.length) {
              t(null);
            }
          }
          if (typeof e == "function") {
            r = e;
            e = false;
          }
          dn.syncFSRequests++;
          if (dn.syncFSRequests > 1) {
            Se(`warning: ${dn.syncFSRequests} FS.syncfs operations in flight at once, probably just doing extra work`);
          }
          var o = dn.getMounts(dn.root.mount);
          var a = 0;
          o.forEach(r => {
            if (!r.type.syncfs) {
              return n(null);
            }
            r.type.syncfs(r, e, n);
          });
        },
        mount(e, r, t) {
          var n;
          var o = t === "/";
          var a = !t;
          if (o && dn.root) {
            throw new dn.ErrnoError(10);
          }
          if (!o && !a) {
            var i = dn.lookupPath(t, {
              follow_mount: false
            });
            t = i.path;
            n = i.node;
            if (dn.isMountpoint(n)) {
              throw new dn.ErrnoError(10);
            }
            if (!dn.isDir(n.mode)) {
              throw new dn.ErrnoError(54);
            }
          }
          var s = {
            type: e,
            opts: r,
            mountpoint: t,
            mounts: []
          };
          var u = e.mount(s);
          u.mount = s;
          s.root = u;
          if (o) {
            dn.root = u;
          } else if (n) {
            n.mounted = s;
            if (n.mount) {
              n.mount.mounts.push(s);
            }
          }
          return u;
        },
        unmount(e) {
          var r = dn.lookupPath(e, {
            follow_mount: false
          });
          if (!dn.isMountpoint(r.node)) {
            throw new dn.ErrnoError(28);
          }
          var t = r.node;
          var n = t.mounted;
          var o = dn.getMounts(n);
          Object.keys(dn.nameTable).forEach(e => {
            for (var r = dn.nameTable[e]; r;) {
              var t = r.name_next;
              if (o.includes(r.mount)) {
                dn.destroyNode(r);
              }
              r = t;
            }
          });
          t.mounted = null;
          var a = t.mount.mounts.indexOf(n);
          t.mount.mounts.splice(a, 1);
        },
        lookup: (e, r) => e.node_ops.lookup(e, r),
        mknod(e, r, t) {
          var n = dn.lookupPath(e, {
            parent: true
          }).node;
          var o = Jt.basename(e);
          if (!o) {
            throw new dn.ErrnoError(28);
          }
          if (o === "." || o === "..") {
            throw new dn.ErrnoError(20);
          }
          var a = dn.mayCreate(n, o);
          if (a) {
            throw new dn.ErrnoError(a);
          }
          if (!n.node_ops.mknod) {
            throw new dn.ErrnoError(63);
          }
          return n.node_ops.mknod(n, o, r, t);
        },
        statfs: e => dn.statfsNode(dn.lookupPath(e, {
          follow: true
        }).node),
        statfsStream: e => dn.statfsNode(e.node),
        statfsNode(e) {
          var r = {
            bsize: 4096,
            frsize: 4096,
            blocks: 1000000,
            bfree: 500000,
            bavail: 500000,
            files: dn.nextInode,
            ffree: dn.nextInode - 1,
            fsid: 42,
            flags: 2,
            namelen: 255
          };
          if (e.node_ops.statfs) {
            Object.assign(r, e.node_ops.statfs(e.mount.opts.root));
          }
          return r;
        },
        create: (e, r = 438) => {
          r &= 4095;
          r |= 32768;
          return dn.mknod(e, r, 0);
        },
        mkdir: (e, r = 511) => {
          r &= 1023;
          r |= 16384;
          return dn.mknod(e, r, 0);
        },
        mkdirTree(e, r) {
          var t = e.split("/");
          var n = "";
          for (var o of t) {
            if (o) {
              if (n || Jt.isAbs(e)) {
                n += "/";
              }
              n += o;
              try {
                dn.mkdir(n, r);
              } catch (a) {
                if (a.errno != 20) {
                  throw a;
                }
              }
            }
          }
        },
        mkdev: (e, r, t) => {
          if (t === undefined) {
            t = r;
            r = 438;
          }
          r |= 8192;
          return dn.mknod(e, r, t);
        },
        symlink(e, r) {
          if (!Zt.resolve(e)) {
            throw new dn.ErrnoError(44);
          }
          var t = dn.lookupPath(r, {
            parent: true
          }).node;
          if (!t) {
            throw new dn.ErrnoError(44);
          }
          var n = Jt.basename(r);
          var o = dn.mayCreate(t, n);
          if (o) {
            throw new dn.ErrnoError(o);
          }
          if (!t.node_ops.symlink) {
            throw new dn.ErrnoError(63);
          }
          return t.node_ops.symlink(t, n, e);
        },
        rename(e, r) {
          var t;
          var n;
          var o = Jt.dirname(e);
          var a = Jt.dirname(r);
          var i = Jt.basename(e);
          var s = Jt.basename(r);
          t = dn.lookupPath(e, {
            parent: true
          }).node;
          n = dn.lookupPath(r, {
            parent: true
          }).node;
          if (!t || !n) {
            throw new dn.ErrnoError(44);
          }
          if (t.mount !== n.mount) {
            throw new dn.ErrnoError(75);
          }
          var u;
          var l = dn.lookupNode(t, i);
          var d = Zt.relative(e, a);
          if (d.charAt(0) !== ".") {
            throw new dn.ErrnoError(28);
          }
          if ((d = Zt.relative(r, o)).charAt(0) !== ".") {
            throw new dn.ErrnoError(55);
          }
          try {
            u = dn.lookupNode(n, s);
          } catch (h) {}
          if (l !== u) {
            var c = dn.isDir(l.mode);
            var f = dn.mayDelete(t, i, c);
            if (f) {
              throw new dn.ErrnoError(f);
            }
            if (f = u ? dn.mayDelete(n, s, c) : dn.mayCreate(n, s)) {
              throw new dn.ErrnoError(f);
            }
            if (!t.node_ops.rename) {
              throw new dn.ErrnoError(63);
            }
            if (dn.isMountpoint(l) || u && dn.isMountpoint(u)) {
              throw new dn.ErrnoError(10);
            }
            if (n !== t && (f = dn.nodePermissions(t, "w"))) {
              throw new dn.ErrnoError(f);
            }
            dn.hashRemoveNode(l);
            try {
              t.node_ops.rename(l, n, s);
              l.parent = n;
            } catch (h) {
              throw h;
            } finally {
              dn.hashAddNode(l);
            }
          }
        },
        rmdir(e) {
          var r = dn.lookupPath(e, {
            parent: true
          }).node;
          var t = Jt.basename(e);
          var n = dn.lookupNode(r, t);
          var o = dn.mayDelete(r, t, true);
          if (o) {
            throw new dn.ErrnoError(o);
          }
          if (!r.node_ops.rmdir) {
            throw new dn.ErrnoError(63);
          }
          if (dn.isMountpoint(n)) {
            throw new dn.ErrnoError(10);
          }
          r.node_ops.rmdir(r, t);
          dn.destroyNode(n);
        },
        readdir(e) {
          var r = dn.lookupPath(e, {
            follow: true
          }).node;
          return dn.checkOpExists(r.node_ops.readdir, 54)(r);
        },
        unlink(e) {
          var r = dn.lookupPath(e, {
            parent: true
          }).node;
          if (!r) {
            throw new dn.ErrnoError(44);
          }
          var t = Jt.basename(e);
          var n = dn.lookupNode(r, t);
          var o = dn.mayDelete(r, t, false);
          if (o) {
            throw new dn.ErrnoError(o);
          }
          if (!r.node_ops.unlink) {
            throw new dn.ErrnoError(63);
          }
          if (dn.isMountpoint(n)) {
            throw new dn.ErrnoError(10);
          }
          r.node_ops.unlink(r, t);
          dn.destroyNode(n);
        },
        readlink(e) {
          var r = dn.lookupPath(e).node;
          if (!r) {
            throw new dn.ErrnoError(44);
          }
          if (!r.node_ops.readlink) {
            throw new dn.ErrnoError(28);
          }
          return r.node_ops.readlink(r);
        },
        stat(e, r) {
          var t = dn.lookupPath(e, {
            follow: !r
          }).node;
          return dn.checkOpExists(t.node_ops.getattr, 63)(t);
        },
        fstat(e) {
          var r = dn.getStreamChecked(e);
          var t = r.node;
          var n = r.stream_ops.getattr;
          var o = n ? r : t;
          n ??= t.node_ops.getattr;
          dn.checkOpExists(n, 63);
          return n(o);
        },
        lstat: e => dn.stat(e, true),
        doChmod(e, r, t, n) {
          dn.doSetAttr(e, r, {
            mode: t & 4095 | r.mode & -4096,
            ctime: Date.now(),
            dontFollow: n
          });
        },
        chmod(e, r, t) {
          var n;
          n = typeof e == "string" ? dn.lookupPath(e, {
            follow: !t
          }).node : e;
          dn.doChmod(null, n, r, t);
        },
        lchmod(e, r) {
          dn.chmod(e, r, true);
        },
        fchmod(e, r) {
          var t = dn.getStreamChecked(e);
          dn.doChmod(t, t.node, r, false);
        },
        doChown(e, r, t) {
          dn.doSetAttr(e, r, {
            timestamp: Date.now(),
            dontFollow: t
          });
        },
        chown(e, r, t, n) {
          var o;
          o = typeof e == "string" ? dn.lookupPath(e, {
            follow: !n
          }).node : e;
          dn.doChown(null, o, n);
        },
        lchown(e, r, t) {
          dn.chown(e, r, t, true);
        },
        fchown(e, r, t) {
          var n = dn.getStreamChecked(e);
          dn.doChown(n, n.node, false);
        },
        doTruncate(e, r, t) {
          if (dn.isDir(r.mode)) {
            throw new dn.ErrnoError(31);
          }
          if (!dn.isFile(r.mode)) {
            throw new dn.ErrnoError(28);
          }
          var n = dn.nodePermissions(r, "w");
          if (n) {
            throw new dn.ErrnoError(n);
          }
          dn.doSetAttr(e, r, {
            size: t,
            timestamp: Date.now()
          });
        },
        truncate(e, r) {
          if (r < 0) {
            throw new dn.ErrnoError(28);
          }
          var t;
          t = typeof e == "string" ? dn.lookupPath(e, {
            follow: true
          }).node : e;
          dn.doTruncate(null, t, r);
        },
        ftruncate(e, r) {
          var t = dn.getStreamChecked(e);
          if (r < 0 || !(t.flags & 2097155)) {
            throw new dn.ErrnoError(28);
          }
          dn.doTruncate(t, t.node, r);
        },
        utime(e, r, t) {
          var n = dn.lookupPath(e, {
            follow: true
          }).node;
          dn.checkOpExists(n.node_ops.setattr, 63)(n, {
            atime: r,
            mtime: t
          });
        },
        open(e, r, t = 438) {
          if (e === "") {
            throw new dn.ErrnoError(44);
          }
          var n;
          var o;
          t = (r = typeof r == "string" ? (e => {
            var r = {
              r: 0,
              "r+": 2,
              w: 577,
              "w+": 578,
              a: 1089,
              "a+": 1090
            }[e];
            if (r === undefined) {
              throw new Error(`Unknown file open mode: ${e}`);
            }
            return r;
          })(r) : r) & 64 ? t & 4095 | 32768 : 0;
          if (typeof e == "object") {
            n = e;
          } else {
            o = e.endsWith("/");
            var a = dn.lookupPath(e, {
              follow: !(r & 131072),
              noent_okay: true
            });
            n = a.node;
            e = a.path;
          }
          var i = false;
          if (r & 64) {
            if (n) {
              if (r & 128) {
                throw new dn.ErrnoError(20);
              }
            } else {
              if (o) {
                throw new dn.ErrnoError(31);
              }
              n = dn.mknod(e, t | 511, 0);
              i = true;
            }
          }
          if (!n) {
            throw new dn.ErrnoError(44);
          }
          if (dn.isChrdev(n.mode)) {
            r &= -513;
          }
          if (r & 65536 && !dn.isDir(n.mode)) {
            throw new dn.ErrnoError(54);
          }
          if (!i) {
            var s = dn.mayOpen(n, r);
            if (s) {
              throw new dn.ErrnoError(s);
            }
          }
          if (r & 512 && !i) {
            dn.truncate(n, 0);
          }
          r &= -131713;
          var u = dn.createStream({
            node: n,
            path: dn.getPath(n),
            flags: r,
            seekable: true,
            position: 0,
            stream_ops: n.stream_ops,
            ungotten: [],
            error: false
          });
          if (u.stream_ops.open) {
            u.stream_ops.open(u);
          }
          if (i) {
            dn.chmod(n, t & 511);
          }
          if (!!G.logReadFiles && !(r & 1) && !(e in dn.readFiles)) {
            dn.readFiles[e] = 1;
          }
          return u;
        },
        close(e) {
          if (dn.isClosed(e)) {
            throw new dn.ErrnoError(8);
          }
          e.getdents &&= null;
          try {
            if (e.stream_ops.close) {
              e.stream_ops.close(e);
            }
          } catch (r) {
            throw r;
          } finally {
            dn.closeStream(e.fd);
          }
          e.fd = null;
        },
        isClosed: e => e.fd === null,
        llseek(e, r, t) {
          if (dn.isClosed(e)) {
            throw new dn.ErrnoError(8);
          }
          if (!e.seekable || !e.stream_ops.llseek) {
            throw new dn.ErrnoError(70);
          }
          if (t != 0 && t != 1 && t != 2) {
            throw new dn.ErrnoError(28);
          }
          e.position = e.stream_ops.llseek(e, r, t);
          e.ungotten = [];
          return e.position;
        },
        read(e, r, t, n, o) {
          if (n < 0 || o < 0) {
            throw new dn.ErrnoError(28);
          }
          if (dn.isClosed(e)) {
            throw new dn.ErrnoError(8);
          }
          if ((e.flags & 2097155) == 1) {
            throw new dn.ErrnoError(8);
          }
          if (dn.isDir(e.node.mode)) {
            throw new dn.ErrnoError(31);
          }
          if (!e.stream_ops.read) {
            throw new dn.ErrnoError(28);
          }
          var a = o !== undefined;
          if (a) {
            if (!e.seekable) {
              throw new dn.ErrnoError(70);
            }
          } else {
            o = e.position;
          }
          var i = e.stream_ops.read(e, r, t, n, o);
          if (!a) {
            e.position += i;
          }
          return i;
        },
        write(e, r, t, n, o, a) {
          if (n < 0 || o < 0) {
            throw new dn.ErrnoError(28);
          }
          if (dn.isClosed(e)) {
            throw new dn.ErrnoError(8);
          }
          if (!(e.flags & 2097155)) {
            throw new dn.ErrnoError(8);
          }
          if (dn.isDir(e.node.mode)) {
            throw new dn.ErrnoError(31);
          }
          if (!e.stream_ops.write) {
            throw new dn.ErrnoError(28);
          }
          if (e.seekable && e.flags & 1024) {
            dn.llseek(e, 0, 2);
          }
          var i = o !== undefined;
          if (i) {
            if (!e.seekable) {
              throw new dn.ErrnoError(70);
            }
          } else {
            o = e.position;
          }
          var s = e.stream_ops.write(e, r, t, n, o, a);
          if (!i) {
            e.position += s;
          }
          return s;
        },
        mmap(e, r, t, n, o) {
          if (n & 2 && !(o & 2) && (e.flags & 2097155) != 2) {
            throw new dn.ErrnoError(2);
          }
          if ((e.flags & 2097155) == 1) {
            throw new dn.ErrnoError(2);
          }
          if (!e.stream_ops.mmap) {
            throw new dn.ErrnoError(43);
          }
          if (!r) {
            throw new dn.ErrnoError(28);
          }
          return e.stream_ops.mmap(e, r, t, n, o);
        },
        msync: (e, r, t, n, o) => e.stream_ops.msync ? e.stream_ops.msync(e, r, t, n, o) : 0,
        ioctl(e, r, t) {
          if (!e.stream_ops.ioctl) {
            throw new dn.ErrnoError(59);
          }
          return e.stream_ops.ioctl(e, r, t);
        },
        readFile(e, r = {}) {
          r.flags = r.flags || 0;
          r.encoding = r.encoding || "binary";
          if (r.encoding !== "utf8" && r.encoding !== "binary") {
            throw new Error(`Invalid encoding type "${r.encoding}"`);
          }
          var t = dn.open(e, r.flags);
          var n = dn.stat(e).size;
          var o = new Uint8Array(n);
          dn.read(t, o, 0, n, 0);
          if (r.encoding === "utf8") {
            o = en(o);
          }
          dn.close(t);
          return o;
        },
        writeFile(e, r, t = {}) {
          t.flags = t.flags || 577;
          var n = dn.open(e, t.flags, t.mode);
          if (typeof r == "string") {
            r = new Uint8Array(tn(r));
          }
          if (!ArrayBuffer.isView(r)) {
            throw new Error("Unsupported data type");
          }
          dn.write(n, r, 0, r.byteLength, undefined, t.canOwn);
          dn.close(n);
        },
        cwd: () => dn.currentPath,
        chdir(e) {
          var r = dn.lookupPath(e, {
            follow: true
          });
          if (r.node === null) {
            throw new dn.ErrnoError(44);
          }
          if (!dn.isDir(r.node.mode)) {
            throw new dn.ErrnoError(54);
          }
          var t = dn.nodePermissions(r.node, "x");
          if (t) {
            throw new dn.ErrnoError(t);
          }
          dn.currentPath = r.path;
        },
        createDefaultDirectories() {
          dn.mkdir("/tmp");
          dn.mkdir("/home");
          dn.mkdir("/home/web_user");
        },
        createDefaultDevices() {
          dn.mkdir("/dev");
          dn.registerDevice(dn.makedev(1, 3), {
            read: () => 0,
            write: (e, r, t, n, o) => n,
            llseek: () => 0
          });
          dn.mkdev("/dev/null", dn.makedev(1, 3));
          nn.register(dn.makedev(5, 0), nn.default_tty_ops);
          nn.register(dn.makedev(6, 0), nn.default_tty1_ops);
          dn.mkdev("/dev/tty", dn.makedev(5, 0));
          dn.mkdev("/dev/tty1", dn.makedev(6, 0));
          var e = new Uint8Array(1024);
          var r = 0;
          var t = () => {
            if (r === 0) {
              Qt(e);
              r = e.byteLength;
            }
            return e[--r];
          };
          dn.createDevice("/dev", "random", t);
          dn.createDevice("/dev", "urandom", t);
          dn.mkdir("/dev/shm");
          dn.mkdir("/dev/shm/tmp");
        },
        createSpecialDirectories() {
          dn.mkdir("/proc");
          var e = dn.mkdir("/proc/self");
          dn.mkdir("/proc/self/fd");
          dn.mount({
            mount() {
              var r = dn.createNode(e, "fd", 16895, 73);
              r.stream_ops = {
                llseek: an.stream_ops.llseek
              };
              r.node_ops = {
                lookup(e, r) {
                  var t = +r;
                  var n = dn.getStreamChecked(t);
                  var o = {
                    parent: null,
                    mount: {
                      mountpoint: "fake"
                    },
                    node_ops: {
                      readlink: () => n.path
                    },
                    id: t + 1
                  };
                  o.parent = o;
                  return o;
                },
                readdir: () => Array.from(dn.streams.entries()).filter(([e, r]) => r).map(([e, r]) => e.toString())
              };
              return r;
            }
          }, {}, "/proc/self/fd");
        },
        createStandardStreams(e, r, t) {
          if (e) {
            dn.createDevice("/dev", "stdin", e);
          } else {
            dn.symlink("/dev/tty", "/dev/stdin");
          }
          if (r) {
            dn.createDevice("/dev", "stdout", null, r);
          } else {
            dn.symlink("/dev/tty", "/dev/stdout");
          }
          if (t) {
            dn.createDevice("/dev", "stderr", null, t);
          } else {
            dn.symlink("/dev/tty1", "/dev/stderr");
          }
          dn.open("/dev/stdin", 0);
          dn.open("/dev/stdout", 1);
          dn.open("/dev/stderr", 1);
        },
        staticInit() {
          dn.nameTable = new Array(4096);
          dn.mount(an, {}, "/");
          dn.createDefaultDirectories();
          dn.createDefaultDevices();
          dn.createSpecialDirectories();
          dn.filesystems = {
            MEMFS: an
          };
        },
        init(e, r, t) {
          dn.initialized = true;
          e ??= G.stdin;
          r ??= G.stdout;
          t ??= G.stderr;
          dn.createStandardStreams(e, r, t);
        },
        quit() {
          dn.initialized = false;
          for (var e of dn.streams) {
            if (e) {
              dn.close(e);
            }
          }
        },
        findObject(e, r) {
          var t = dn.analyzePath(e, r);
          if (t.exists) {
            return t.object;
          } else {
            return null;
          }
        },
        analyzePath(e, r) {
          try {
            e = (n = dn.lookupPath(e, {
              follow: !r
            })).path;
          } catch (o) {}
          var t = {
            isRoot: false,
            exists: false,
            error: 0,
            name: null,
            path: null,
            object: null,
            parentExists: false,
            parentPath: null,
            parentObject: null
          };
          try {
            var n = dn.lookupPath(e, {
              parent: true
            });
            t.parentExists = true;
            t.parentPath = n.path;
            t.parentObject = n.node;
            t.name = Jt.basename(e);
            n = dn.lookupPath(e, {
              follow: !r
            });
            t.exists = true;
            t.path = n.path;
            t.object = n.node;
            t.name = n.node.name;
            t.isRoot = n.path === "/";
          } catch (o) {
            t.error = o.errno;
          }
          return t;
        },
        createPath(e, r, t, n) {
          e = typeof e == "string" ? e : dn.getPath(e);
          for (var o = r.split("/").reverse(); o.length;) {
            var a = o.pop();
            if (a) {
              var i = Jt.join2(e, a);
              try {
                dn.mkdir(i);
              } catch (s) {
                if (s.errno != 20) {
                  throw s;
                }
              }
              e = i;
            }
          }
          return i;
        },
        createFile(e, r, t, n, o) {
          var a = Jt.join2(typeof e == "string" ? e : dn.getPath(e), r);
          var i = sn(n, o);
          return dn.create(a, i);
        },
        createDataFile(e, r, t, n, o, a) {
          var i = r;
          if (e) {
            e = typeof e == "string" ? e : dn.getPath(e);
            i = r ? Jt.join2(e, r) : e;
          }
          var s = sn(n, o);
          var u = dn.create(i, s);
          if (t) {
            if (typeof t == "string") {
              var l = new Array(t.length);
              for (var d = 0, c = t.length; d < c; ++d) {
                l[d] = t.charCodeAt(d);
              }
              t = l;
            }
            dn.chmod(u, s | 146);
            var f = dn.open(u, 577);
            dn.write(f, t, 0, t.length, 0, a);
            dn.close(f);
            dn.chmod(u, s);
          }
        },
        createDevice(e, r, t, n) {
          var o = Jt.join2(typeof e == "string" ? e : dn.getPath(e), r);
          var a = sn(!!t, !!n);
          dn.createDevice.major ??= 64;
          var i = dn.makedev(dn.createDevice.major++, 0);
          dn.registerDevice(i, {
            open(e) {
              e.seekable = false;
            },
            close(e) {
              if (n?.buffer?.length) {
                n(10);
              }
            },
            read(e, r, n, o, a) {
              var i = 0;
              for (var s = 0; s < o; s++) {
                var u;
                try {
                  u = t();
                } catch (l) {
                  throw new dn.ErrnoError(29);
                }
                if (u === undefined && i === 0) {
                  throw new dn.ErrnoError(6);
                }
                if (u == null) {
                  break;
                }
                i++;
                r[n + s] = u;
              }
              if (i) {
                e.node.atime = Date.now();
              }
              return i;
            },
            write(e, r, t, o, a) {
              for (var i = 0; i < o; i++) {
                try {
                  n(r[t + i]);
                } catch (s) {
                  throw new dn.ErrnoError(29);
                }
              }
              if (o) {
                e.node.mtime = e.node.ctime = Date.now();
              }
              return i;
            }
          });
          return dn.mkdev(o, a, i);
        },
        forceLoadFile(e) {
          if (e.isDevice || e.isFolder || e.link || e.contents) {
            return true;
          }
          if (typeof XMLHttpRequest != "undefined") {
            throw new Error("Lazy loading should have been performed (contents set) in createLazyFile, but it was not. Lazy loading only works in web workers. Use --embed-file or --preload-file in emcc on the main thread.");
          }
          try {
            e.contents = Z(e.url);
            e.usedBytes = e.contents.length;
          } catch (r) {
            throw new dn.ErrnoError(29);
          }
        },
        createLazyFile(e, r, t, n, o) {
          function a(e, r, t, n, o) {
            var a = e.node.contents;
            if (o >= a.length) {
              return 0;
            }
            var i = Math.min(a.length - o, n);
            if (a.slice) {
              for (var s = 0; s < i; s++) {
                r[t + s] = a[o + s];
              }
            } else {
              for (s = 0; s < i; s++) {
                r[t + s] = a.get(o + s);
              }
            }
            return i;
          }
          class i {
            lengthKnown = false;
            chunks = [];
            get(e) {
              if (!(e > this.length - 1) && !(e < 0)) {
                var r = e % this.chunkSize;
                var t = e / this.chunkSize | 0;
                return this.getter(t)[r];
              }
            }
            setDataGetter(e) {
              this.getter = e;
            }
            cacheLength() {
              var e = new XMLHttpRequest();
              e.open("HEAD", t, false);
              e.send(null);
              if ((!(e.status >= 200) || !(e.status < 300)) && e.status !== 304) {
                throw new Error("Couldn't load " + t + ". Status: " + e.status);
              }
              var r;
              var n = Number(e.getResponseHeader("Content-length"));
              var o = (r = e.getResponseHeader("Accept-Ranges")) && r === "bytes";
              var a = (r = e.getResponseHeader("Content-Encoding")) && r === "gzip";
              var i = 1048576;
              if (!o) {
                i = n;
              }
              var s = this;
              s.setDataGetter(e => {
                var r = e * i;
                var o = (e + 1) * i - 1;
                o = Math.min(o, n - 1);
                if (s.chunks[e] === undefined) {
                  s.chunks[e] = ((e, r) => {
                    if (e > r) {
                      throw new Error("invalid range (" + e + ", " + r + ") or no bytes requested!");
                    }
                    if (r > n - 1) {
                      throw new Error("only " + n + " bytes available! programmer error!");
                    }
                    var o = new XMLHttpRequest();
                    o.open("GET", t, false);
                    if (n !== i) {
                      o.setRequestHeader("Range", "bytes=" + e + "-" + r);
                    }
                    o.responseType = "arraybuffer";
                    if (o.overrideMimeType) {
                      o.overrideMimeType("text/plain; charset=x-user-defined");
                    }
                    o.send(null);
                    if ((!(o.status >= 200) || !(o.status < 300)) && o.status !== 304) {
                      throw new Error("Couldn't load " + t + ". Status: " + o.status);
                    }
                    if (o.response !== undefined) {
                      return new Uint8Array(o.response || []);
                    } else {
                      return tn(o.responseText || "");
                    }
                  })(r, o);
                }
                if (s.chunks[e] === undefined) {
                  throw new Error("doXHR failed!");
                }
                return s.chunks[e];
              });
              if (!!a || !n) {
                i = n = 1;
                n = this.getter(0).length;
                i = n;
                _e("LazyFiles on gzip forces download of the whole file when length is accessed");
              }
              this._length = n;
              this._chunkSize = i;
              this.lengthKnown = true;
            }
            get length() {
              if (!this.lengthKnown) {
                this.cacheLength();
              }
              return this._length;
            }
            get chunkSize() {
              if (!this.lengthKnown) {
                this.cacheLength();
              }
              return this._chunkSize;
            }
          }
          if (typeof XMLHttpRequest != "undefined") {
            if (!J) {
              throw "Cannot do synchronous binary XHRs outside webworkers in modern browsers. Use --embed-file or --preload-file in emcc";
            }
            var s = {
              isDevice: false,
              contents: new i()
            };
          } else {
            s = {
              isDevice: false,
              url: t
            };
          }
          var u = dn.createFile(e, r, s, n, o);
          if (s.contents) {
            u.contents = s.contents;
          } else if (s.url) {
            u.contents = null;
            u.url = s.url;
          }
          Object.defineProperties(u, {
            usedBytes: {
              get: function () {
                return this.contents.length;
              }
            }
          });
          var l = {};
          Object.keys(u.stream_ops).forEach(e => {
            var r = u.stream_ops[e];
            l[e] = (...e) => {
              dn.forceLoadFile(u);
              return r(...e);
            };
          });
          l.read = (e, r, t, n, o) => {
            dn.forceLoadFile(u);
            return a(e, r, t, n, o);
          };
          l.mmap = (e, r, t, n, o) => {
            dn.forceLoadFile(u);
            var i = on();
            if (!i) {
              throw new dn.ErrnoError(48);
            }
            a(e, he, i, r, t);
            return {
              ptr: i,
              allocated: true
            };
          };
          u.stream_ops = l;
          return u;
        }
      };
      var cn = {
        DEFAULT_POLLMASK: 5,
        calculateAt(e, r, t) {
          if (Jt.isAbs(r)) {
            return r;
          }
          var n;
          n = e === -100 ? dn.cwd() : cn.getStreamFromFD(e).path;
          if (r.length == 0) {
            if (!t) {
              throw new dn.ErrnoError(44);
            }
            return n;
          }
          return n + "/" + r;
        },
        writeStat(e, r) {
          ge[e >> 2] = r.dev;
          ge[e + 4 >> 2] = r.mode;
          ge[e + 8 >> 2] = r.nlink;
          ge[e + 12 >> 2] = r.uid;
          ge[e + 16 >> 2] = r.gid;
          ge[e + 20 >> 2] = r.rdev;
          ke[e + 24 >> 3] = BigInt(r.size);
          ve[e + 32 >> 2] = 4096;
          ve[e + 36 >> 2] = r.blocks;
          var t = r.atime.getTime();
          var n = r.mtime.getTime();
          var o = r.ctime.getTime();
          ke[e + 40 >> 3] = BigInt(Math.floor(t / 1000));
          ge[e + 48 >> 2] = t % 1000 * 1000 * 1000;
          ke[e + 56 >> 3] = BigInt(Math.floor(n / 1000));
          ge[e + 64 >> 2] = n % 1000 * 1000 * 1000;
          ke[e + 72 >> 3] = BigInt(Math.floor(o / 1000));
          ge[e + 80 >> 2] = o % 1000 * 1000 * 1000;
          ke[e + 88 >> 3] = BigInt(r.ino);
          return 0;
        },
        writeStatFs(e, r) {
          ge[e + 4 >> 2] = r.bsize;
          ge[e + 60 >> 2] = r.bsize;
          ke[e + 8 >> 3] = BigInt(r.blocks);
          ke[e + 16 >> 3] = BigInt(r.bfree);
          ke[e + 24 >> 3] = BigInt(r.bavail);
          ke[e + 32 >> 3] = BigInt(r.files);
          ke[e + 40 >> 3] = BigInt(r.ffree);
          ge[e + 48 >> 2] = r.fsid;
          ge[e + 64 >> 2] = r.flags;
          ge[e + 56 >> 2] = r.namelen;
        },
        doMsync(e, r, t, n, o) {
          if (!dn.isFile(r.node.mode)) {
            throw new dn.ErrnoError(43);
          }
          if (n & 2) {
            return 0;
          }
          var a = me.slice(e, e + t);
          dn.msync(r, a, o, t, n);
        },
        getStreamFromFD: e => dn.getStreamChecked(e),
        varargs: undefined,
        getStr: e => Ze(e)
      };
      var fn = (e, r, t, n) => {
        var o = 0;
        for (var a = 0; a < t; a++) {
          var i = ge[r >> 2];
          var s = ge[r + 4 >> 2];
          r += 8;
          var u = dn.read(e, he, i, s, n);
          if (u < 0) {
            return -1;
          }
          o += u;
          if (u < s) {
            break;
          }
        }
        return o;
      };
      var hn = (e, r, t, n) => {
        var o = 0;
        for (var a = 0; a < t; a++) {
          var i = ge[r >> 2];
          var s = ge[r + 4 >> 2];
          r += 8;
          var u = dn.write(e, he, i, s, n);
          if (u < 0) {
            return -1;
          }
          o += u;
          if (u < s) {
            break;
          }
        }
        return o;
      };
      qe.init();
      dn.createPreloadedFile = (e, r, t, n, o, a, i, s, u, l) => {
        ln(e, r, t, n, o, s, u, l).then(a).catch(i);
      };
      dn.preloadFile = ln;
      dn.staticInit();
      (function () {
        if (!ee) {
          if (G.wasmMemory) {
            fe = G.wasmMemory;
          } else {
            var e = G.INITIAL_MEMORY || 134217728;
            fe = new WebAssembly.Memory({
              initial: e / 65536,
              maximum: e / 65536,
              shared: true
            });
          }
          r();
        }
      })();
      if (G.noExitRuntime) {
        Ke = G.noExitRuntime;
      }
      if (G.preloadPlugins) {
        un = G.preloadPlugins;
      }
      if (G.print) {
        _e = G.print;
      }
      if (G.printErr) {
        Se = G.printErr;
      }
      if (G.wasmBinary) {
        ae = G.wasmBinary;
      }
      if (G.arguments) {
        G.arguments;
      }
      if (G.thisProgram) {
        re = G.thisProgram;
      }
      G.ccall = (e, r, t, n, o) => {
        function a(e) {
          Or();
          if (l !== 0) {
            Be(l);
          }
          return function (e) {
            if (r === "string") {
              return Ze(e);
            } else if (r === "boolean") {
              return Boolean(e);
            } else {
              return e;
            }
          }(e);
        }
        var i = {
          string: e => {
            var r = 0;
            if (e != null && e !== 0) {
              r = (e => {
                var r = Jr(e) + 1;
                var t = Le(r);
                Kr(e, t, r);
                return t;
              })(e);
            }
            return r;
          },
          array: e => {
            var r;
            var t;
            var n = Le(e.length);
            r = e;
            t = n;
            he.set(r, t);
            return n;
          }
        };
        var s = (e => G["_" + e])(e);
        var u = [];
        var l = 0;
        if (n) {
          for (var d = 0; d < n.length; d++) {
            var c = i[t[d]];
            if (c) {
              if (l === 0) {
                l = ze();
              }
              u[d] = c(n[d]);
            } else {
              u[d] = n[d];
            }
          }
        }
        var f = zr.currData;
        var h = s(...u);
        var m = o?.async;
        Cr();
        if (zr.currData != f) {
          return zr.whenDone().then(a);
        } else {
          h = a(h);
          if (m) {
            return Promise.resolve(h);
          } else {
            return h;
          }
        }
      };
      var mn;
      var pn;
      var wn;
      var vn;
      var gn;
      var yn;
      var En;
      var kn;
      var bn;
      var _n;
      var Sn;
      var Tn;
      var Dn;
      var Fn;
      var An;
      var Pn;
      var Mn;
      var Wn;
      var Rn;
      var xn;
      var jn;
      var Nn;
      var $n;
      var In;
      var Cn;
      var On;
      var zn;
      var Bn;
      var Ln;
      var Un;
      var Hn;
      var Vn;
      var qn;
      var Yn;
      var Xn;
      var Gn;
      var Kn;
      var Jn;
      var Qn;
      var Zn;
      var eo;
      var ro;
      var to;
      var no;
      var oo;
      var ao;
      var io;
      var so;
      var uo;
      var lo;
      var co;
      var fo;
      var ho;
      var mo;
      var po;
      var wo = [s, u, l, m, p, w, v, g, y, E];
      var vo = {
        170760: () => {
          try {
            window.location.href = "https://google.com";
          } catch (e) {}
        }
      };
      var go = await async function () {
        function e(e, r) {
          var t;
          var n;
          go = e.exports;
          n = (go = zr.instrumentWasmExports(go)).Pa;
          qe.tlsInitFunctions.push(n);
          go.Qa;
          ie = r;
          mn = (t = go).Ja;
          pn = t.Ka;
          G._main = wn = t.La;
          vn = t.Ma;
          gn = t.Na;
          yn = t.Oa;
          t.Pa;
          En = t.Ra;
          kn = t.Sa;
          bn = t.Ta;
          _n = t.Ua;
          Sn = t.Va;
          Tn = t.Wa;
          Dn = t.Xa;
          Fn = t.Ya;
          An = t.Za;
          Pn = t._a;
          Mn = t.$a;
          Wn = t.ab;
          Rn = t.bb;
          xn = t.cb;
          jn = t.db;
          Nn = t.eb;
          $n = t.fb;
          Xe.v = In = t.gb;
          Xe.vii = Cn = t.hb;
          Xe.vi = On = t.ib;
          Xe.viiii = zn = t.jb;
          Xe.ii = Bn = t.kb;
          Xe.iii = Ln = t.lb;
          Xe.viji = Un = t.mb;
          Xe.viii = Hn = t.nb;
          Xe.viiiii = Vn = t.ob;
          Xe.iiii = qn = t.pb;
          Xe.i = Yn = t.qb;
          Xe.diiiii = Xn = t.rb;
          Xe.iiiii = Gn = t.sb;
          Xe.iiiiii = Kn = t.tb;
          Xe.ijiii = Jn = t.ub;
          Xe.ijjiii = Qn = t.vb;
          Xe.iiiiiiii = Zn = t.wb;
          Xe.iiji = eo = t.xb;
          Xe.ijii = ro = t.yb;
          Xe.iijii = to = t.zb;
          Xe.iiiiiii = no = t.Ab;
          Xe.iidiiii = t.Bb;
          Xe.jiiii = oo = t.Cb;
          Xe.fiii = ao = t.Db;
          Xe.diii = io = t.Eb;
          Xe.viiiiiii = so = t.Fb;
          Xe.iiiiiiiiiiii = uo = t.Gb;
          Xe.viiiiiiiiii = lo = t.Hb;
          Xe.viiiiiiiiiiiiiii = co = t.Ib;
          Xe.vij = t.Jb;
          Xe.viiij = t.Kb;
          Xe.ji = t.Lb;
          Xe.jij = t.Mb;
          Xe.iiiijii = t.Nb;
          Xe.jiji = t.Ob;
          Xe.viijii = t.Pb;
          Xe.iiiiiiiii = t.Qb;
          Xe.iiiiij = t.Rb;
          Xe.iiiiid = t.Sb;
          Xe.iiiiijj = t.Tb;
          Xe.iiiiiijj = t.Ub;
          Xe.viiiiii = t.Vb;
          fo = t.Wb;
          ho = t.Xb;
          mo = t.Yb;
          po = t.Zb;
          o();
          return go;
        }
        n();
        var r;
        var t;
        var s = i();
        if (G.instantiateWasm) {
          return new Promise((r, t) => {
            G.instantiateWasm(s, (t, n) => {
              r(e(t, n));
            });
          });
        } else if (ee) {
          return new Promise(r => {
            de = t => {
              var n = new WebAssembly.Instance(t, i());
              r(e(n, t));
            };
          });
        } else {
          Fe ??= G.locateFile ? (r = "wasm.wasm", G.locateFile ? G.locateFile(r, oe) : oe + r) : new URL("/static/assets/wasm-d4pJlc1V.wasm", self.location.href).href;
          return e((t = await async function (e, r, t) {
            if (!e) {
              try {
                var n = fetch(r, {
                  credentials: "same-origin"
                });
                return await WebAssembly.instantiateStreaming(n, t);
              } catch (o) {
                Se(`wasm streaming compile failed: ${o}`);
                Se("falling back to ArrayBuffer instantiation");
              }
            }
            return async function (e, r) {
              try {
                var t = await async function (e) {
                  if (!ae) {
                    try {
                      var r = await Q(e);
                      return new Uint8Array(r);
                    } catch {}
                  }
                  return function (e) {
                    if (e == Fe && ae) {
                      return new Uint8Array(ae);
                    }
                    if (Z) {
                      return Z(e);
                    }
                    throw "both async and sync fetching of the wasm failed";
                  }(e);
                }(e);
                return await WebAssembly.instantiate(t, r);
              } catch (o) {
                Se(`failed to asynchronously prepare wasm: ${o}`);
                a(o);
              }
            }(r, t);
          }(ae, Fe, s)).instance, t.module);
        }
      }();
      (function () {
        if (G.preInit) {
          for (typeof G.preInit == "function" && (G.preInit = [G.preInit]); G.preInit.length > 0;) {
            G.preInit.shift()();
          }
        }
      })();
      (function e() {
        function r() {
          G.calledRun = true;
          if (!Te) {
            t();
            ue?.(G);
            G.onRuntimeInitialized?.();
            if (!G.noInitialRun) {
              (function () {
                var e = wn;
                try {
                  var r = e(0, 0);
                  He(r);
                  return r;
                } catch (t) {
                  return $r(t);
                }
              })();
            }
            (function () {
              if (!ee) {
                if (G.postRun) {
                  for (typeof G.postRun == "function" && (G.postRun = [G.postRun]); G.postRun.length;) {
                    e = G.postRun.shift();
                    Ye.push(e);
                  }
                }
                je(Ye);
              }
              var e;
            })();
          }
        }
        if (Pe > 0) {
          Me = e;
        } else {
          if (ee) {
            ue?.(G);
            t();
            return;
          }
          (function () {
            if (G.preRun) {
              for (typeof G.preRun == "function" && (G.preRun = [G.preRun]); G.preRun.length;) {
                $e(G.preRun.shift());
              }
            }
            je(Ne);
          })();
          if (Pe > 0) {
            Me = e;
          } else if (G.setStatus) {
            G.setStatus("Running...");
            setTimeout(() => {
              setTimeout(() => G.setStatus(""), 1);
              r();
            }, 1);
          } else {
            r();
          }
        }
      })();
      if (!Ae) {
        new Promise((e, r) => {
          ue = e;
          le = r;
        });
      }
    })();
  }
})();