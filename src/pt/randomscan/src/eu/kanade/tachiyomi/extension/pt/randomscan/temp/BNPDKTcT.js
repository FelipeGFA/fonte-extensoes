async function Module(p = {}) {
  function f2() {
    var v14 = v46.buffer;
    v47 = new Int8Array(v14);
    v49 = new Int16Array(v14);
    vP.HEAPU8 = v48 = new Uint8Array(v14);
    v50 = new Uint16Array(v14);
    v51 = new Int32Array(v14);
    v52 = new Uint32Array(v14);
    v53 = new Float32Array(v14);
    v54 = new Float64Array(v14);
    v55 = new BigInt64Array(v14);
    v56 = new BigUint64Array(v14);
  }
  function f3() {
    v69 = true;
    if (v35) {
      return v45();
    }
    if (!vP.noFSInit && !vO23.initialized) {
      vO23.init();
    }
    v446.Ia();
    vO23.ignorePermissions = false;
  }
  function f4(p6) {
    vLN05++;
    vP.monitorRunDependencies?.(vLN05);
  }
  function f5(p7) {
    vLN05--;
    vP.monitorRunDependencies?.(vLN05);
    if (vLN05 == 0 && v70) {
      var v_0x107f08 = v70;
      v70 = null;
      v_0x107f08();
    }
  }
  function f6(p8) {
    vP.onAbort?.(p8);
    v58(p8 = "Aborted(" + p8 + ")");
    v59 = true;
    p8 += ". Build with -sASSERTIONS for more info.";
    var v15 = new WebAssembly.RuntimeError(p8);
    v43?.(v15);
    throw v15;
  }
  function f7() {
    return {
      a: {
        K: vF20,
        C: vF21,
        W: vF22,
        b: vF25,
        h: vF26,
        l: vF27,
        H: vF28,
        ga: vF29,
        f: vF30,
        ua: vF31,
        U: vF35,
        xa: vF37,
        va: vF39,
        T: vF41,
        V: vF53,
        x: vF58,
        p: vF60,
        wa: vF64,
        I: vF71,
        ya: vF72,
        oa: vF73,
        ea: vF76,
        ia: vF77,
        _: vF78,
        S: vF79,
        na: vF74,
        sa: vF80,
        Da: vF81,
        w: vF84,
        ca: vF38,
        z: vF86,
        Ea: vF87,
        Y: vF88,
        L: vF89,
        u: vF90,
        za: vF91,
        J: vF92,
        Ca: vF93,
        Ga: vF94,
        Ba: vF95,
        Fa: vF96,
        ja: f14,
        ka: f53,
        $: f15,
        la: vF101,
        Ha: vF103,
        ha: vF105,
        ra: vF106,
        E: vF100,
        da: vF107,
        Aa: vF108,
        aa: f16,
        ba: f17,
        fa: vVF16,
        ta: f18,
        qa: f19,
        ma: f20,
        pa: f21,
        M: f45,
        q: f33,
        N: f44,
        o: f32,
        d: f24,
        c: f25,
        j: f30,
        n: f35,
        B: f36,
        v: f42,
        s: f39,
        G: f47,
        P: f40,
        t: f34,
        X: f41,
        r: f37,
        Q: f38,
        O: f43,
        i: f31,
        m: f29,
        e: f23,
        g: f27,
        k: f22,
        R: f28,
        y: f46,
        A: f48,
        F: f49,
        D: f26,
        a: v46,
        Z: f8
      }
    };
  }
  function f8(p9) {
    if (v35) {
      return vF15(0, 0, 1, p9);
    }
    v41 = p9;
    if (!vF11()) {
      vO2.terminateAllThreads();
      vP.onExit?.(p9);
      v59 = true;
    }
    vF4(p9, new C(p9));
  }
  function f9(p10) {
    if (v35) {
      return vF15(1, 0, 0, p10);
    }
    vVF16(p10);
  }
  function f10(p11, p12, p13, p14) {
    if (v35) {
      return vF15(2, 0, 1, p11, p12, p13, p14);
    } else {
      return vF29(p11, p12, p13, p14);
    }
  }
  function f11(p15, p16, p17 = {}) {
    return function (p18, p19, p20 = {}) {
      var v16 = p19.name;
      if (!p18) {
        vF33("type \"" + v16 + "\" must have a positive integer typeid pointer");
      }
      if (vO6.hasOwnProperty(p18)) {
        if (p20.ignoreDuplicateRegistrations) {
          return;
        }
        vF33("Cannot register type '" + v16 + "' twice");
      }
      vO6[p18] = p19;
      delete vO7[p18];
      if (vO5.hasOwnProperty(p18)) {
        var v17 = vO5[p18];
        delete vO5[p18];
        v17.forEach(p21 => p21());
      }
    }(p15, p16, p17);
  }
  function f12(p22) {
    return this.fromWireType(v52[p22 >> 2]);
  }
  function f13(p23) {
    for (var vLN1 = 1; vLN1 < p23.length; ++vLN1) {
      if (p23[vLN1] !== null && p23[vLN1].destructorFunction === undefined) {
        return true;
      }
    }
    return false;
  }
  function f14(p24, p25) {
    p24 = vF98(p24);
    var v18 = new Date(p24 * 1000);
    v51[p25 >> 2] = v18.getSeconds();
    v51[p25 + 4 >> 2] = v18.getMinutes();
    v51[p25 + 8 >> 2] = v18.getHours();
    v51[p25 + 12 >> 2] = v18.getDate();
    v51[p25 + 16 >> 2] = v18.getMonth();
    v51[p25 + 20 >> 2] = v18.getFullYear() - 1900;
    v51[p25 + 24 >> 2] = v18.getDay();
    var v19 = vF97(v18) | 0;
    v51[p25 + 28 >> 2] = v19;
    v51[p25 + 36 >> 2] = v18.getTimezoneOffset() * -60;
    var v20 = new Date(v18.getFullYear(), 0, 1);
    var v21 = new Date(v18.getFullYear(), 6, 1).getTimezoneOffset();
    var v22 = v20.getTimezoneOffset();
    var v23 = (v21 != v22 && v18.getTimezoneOffset() == Math.min(v22, v21)) | 0;
    v51[p25 + 32 >> 2] = v23;
  }
  function f15(p26, p27) {
    if (v35) {
      return vF15(3, 0, 1, p26, p27);
    }
    if (vO15[p26]) {
      clearTimeout(vO15[p26].id);
      delete vO15[p26];
    }
    if (!p27) {
      return 0;
    }
    var vSetTimeout = setTimeout(() => {
      delete vO15[p26];
      vF46(() => v402(p26, vF100()));
    }, p27);
    vO15[p26] = {
      id: vSetTimeout,
      timeout_ms: p27
    };
    return 0;
  }
  function f16(p28, p29) {
    if (v35) {
      return vF15(4, 0, 1, p28, p29);
    }
    var vLN02 = 0;
    var vLN03 = 0;
    for (var v24 of vF109()) {
      var v25 = p29 + vLN02;
      v52[p28 + vLN03 >> 2] = v25;
      vLN02 += vF62(v24, v25, Infinity) + 1;
      vLN03 += 4;
    }
    return 0;
  }
  function f17(p30, p31) {
    if (v35) {
      return vF15(5, 0, 1, p30, p31);
    }
    var v_0x3a9b5d = vF109();
    v52[p30 >> 2] = v_0x3a9b5d.length;
    var vLN04 = 0;
    for (var v26 of v_0x3a9b5d) {
      vLN04 += vF63(v26) + 1;
    }
    v52[p31 >> 2] = vLN04;
    return 0;
  }
  function f18(p32) {
    if (v35) {
      return vF15(6, 0, 1, p32);
    }
    try {
      var v27 = vO30.getStreamFromFD(p32);
      vO23.close(v27);
      return 0;
    } catch (e3) {
      if (vO23 === undefined || e3.name !== "ErrnoError") {
        throw e3;
      }
      return e3.errno;
    }
  }
  function f19(p33, p34, p35, p36) {
    if (v35) {
      return vF15(7, 0, 1, p33, p34, p35, p36);
    }
    try {
      var v28 = vO30.getStreamFromFD(p33);
      var v_0x2df34c = vF117(v28, p34, p35);
      v52[p36 >> 2] = v_0x2df34c;
      return 0;
    } catch (e4) {
      if (vO23 === undefined || e4.name !== "ErrnoError") {
        throw e4;
      }
      return e4.errno;
    }
  }
  function f20(p37, p38, p39, p40) {
    if (v35) {
      return vF15(8, 0, 1, p37, p38, p39, p40);
    }
    p38 = vF98(p38);
    try {
      if (isNaN(p38)) {
        return 61;
      }
      var v29 = vO30.getStreamFromFD(p37);
      vO23.llseek(v29, p38, p39);
      v55[p40 >> 3] = BigInt(v29.position);
      if (v29.getdents && p38 === 0 && p39 === 0) {
        v29.getdents = null;
      }
      return 0;
    } catch (e5) {
      if (vO23 === undefined || e5.name !== "ErrnoError") {
        throw e5;
      }
      return e5.errno;
    }
  }
  function f21(p41, p42, p43, p44) {
    if (v35) {
      return vF15(9, 0, 1, p41, p42, p43, p44);
    }
    try {
      var v30 = vO30.getStreamFromFD(p41);
      var v_0x11e3e3 = vF118(v30, p42, p43);
      v52[p44 >> 2] = v_0x11e3e3;
      return 0;
    } catch (e6) {
      if (vO23 === undefined || e6.name !== "ErrnoError") {
        throw e6;
      }
      return e6.errno;
    }
  }
  function f22(p45, p46, p47, p48, p49) {
    var v_0x5a334f = vF12();
    try {
      v417(p45, p46, p47, p48, p49);
    } catch (e7) {
      vF13(v_0x5a334f);
      if (e7 !== e7 + 0) {
        throw e7;
      }
      v404(1, 0);
    }
  }
  function f23(p50, p51, p52) {
    var v_0x5a334f2 = vF12();
    try {
      v415(p50, p51, p52);
    } catch (e8) {
      vF13(v_0x5a334f2);
      if (e8 !== e8 + 0) {
        throw e8;
      }
      v404(1, 0);
    }
  }
  function f24(p53, p54) {
    var v_0x5a334f3 = vF12();
    try {
      return v418(p53, p54);
    } catch (e9) {
      vF13(v_0x5a334f3);
      if (e9 !== e9 + 0) {
        throw e9;
      }
      v404(1, 0);
    }
  }
  function f25(p55, p56, p57) {
    var v_0x5a334f4 = vF12();
    try {
      return v419(p55, p56, p57);
    } catch (e10) {
      vF13(v_0x5a334f4);
      if (e10 !== e10 + 0) {
        throw e10;
      }
      v404(1, 0);
    }
  }
  function f26(p58, p59, p60, p61) {
    var v_0x5a334f5 = vF12();
    try {
      v420(p58, p59, p60, p61);
    } catch (e11) {
      vF13(v_0x5a334f5);
      if (e11 !== e11 + 0) {
        throw e11;
      }
      v404(1, 0);
    }
  }
  function f27(p62, p63, p64, p65) {
    var v_0x5a334f6 = vF12();
    try {
      v421(p62, p63, p64, p65);
    } catch (e12) {
      vF13(v_0x5a334f6);
      if (e12 !== e12 + 0) {
        throw e12;
      }
      v404(1, 0);
    }
  }
  function f28(p66, p67, p68, p69, p70, p71) {
    var v_0x5a334f7 = vF12();
    try {
      v422(p66, p67, p68, p69, p70, p71);
    } catch (e13) {
      vF13(v_0x5a334f7);
      if (e13 !== e13 + 0) {
        throw e13;
      }
      v404(1, 0);
    }
  }
  function f29(p72, p73) {
    var v_0x5a334f8 = vF12();
    try {
      v416(p72, p73);
    } catch (e14) {
      vF13(v_0x5a334f8);
      if (e14 !== e14 + 0) {
        throw e14;
      }
      v404(1, 0);
    }
  }
  function f30(p74, p75, p76, p77) {
    var v_0x5a334f9 = vF12();
    try {
      return v423(p74, p75, p76, p77);
    } catch (e15) {
      vF13(v_0x5a334f9);
      if (e15 !== e15 + 0) {
        throw e15;
      }
      v404(1, 0);
    }
  }
  function f31(p78) {
    var v_0x5a334f10 = vF12();
    try {
      v414(p78);
    } catch (e16) {
      vF13(v_0x5a334f10);
      if (e16 !== e16 + 0) {
        throw e16;
      }
      v404(1, 0);
    }
  }
  function f32(p79) {
    var v_0x5a334f11 = vF12();
    try {
      return v424(p79);
    } catch (e17) {
      vF13(v_0x5a334f11);
      if (e17 !== e17 + 0) {
        throw e17;
      }
      v404(1, 0);
    }
  }
  function f33(p80, p81, p82, p83, p84, p85) {
    var v_0x5a334f12 = vF12();
    try {
      return v425(p80, p81, p82, p83, p84, p85);
    } catch (e18) {
      vF13(v_0x5a334f12);
      if (e18 !== e18 + 0) {
        throw e18;
      }
      v404(1, 0);
    }
  }
  function f34(p86, p87, p88, p89, p90) {
    var v_0x5a334f13 = vF12();
    try {
      return v433(p86, p87, p88, p89, p90);
    } catch (e19) {
      vF13(v_0x5a334f13);
      if (e19 !== e19 + 0) {
        throw e19;
      }
      v404(1, 0);
    }
  }
  function f35(p91, p92, p93, p94, p95) {
    var v_0x5a334f14 = vF12();
    try {
      return v426(p91, p92, p93, p94, p95);
    } catch (e20) {
      vF13(v_0x5a334f14);
      if (e20 !== e20 + 0) {
        throw e20;
      }
      v404(1, 0);
    }
  }
  function f36(p96, p97, p98, p99, p100, p101) {
    var v_0x5a334f15 = vF12();
    try {
      return v427(p96, p97, p98, p99, p100, p101);
    } catch (e21) {
      vF13(v_0x5a334f15);
      if (e21 !== e21 + 0) {
        throw e21;
      }
      v404(1, 0);
    }
  }
  function f37(p102, p103, p104, p105, p106) {
    var v_0x5a334f16 = vF12();
    try {
      return v428(p102, p103, p104, p105, p106);
    } catch (e22) {
      vF13(v_0x5a334f16);
      if (e22 !== e22 + 0) {
        throw e22;
      }
      v404(1, 0);
    }
  }
  function f38(p107, p108, p109, p110, p111, p112) {
    var v_0x5a334f17 = vF12();
    try {
      return v429(p107, p108, p109, p110, p111, p112);
    } catch (e23) {
      vF13(v_0x5a334f17);
      if (e23 !== e23 + 0) {
        throw e23;
      }
      v404(1, 0);
    }
  }
  function f39(p113, p114, p115, p116, p117, p118, p119, p120) {
    var v_0x5a334f18 = vF12();
    try {
      return v430(p113, p114, p115, p116, p117, p118, p119, p120);
    } catch (e24) {
      vF13(v_0x5a334f18);
      if (e24 !== e24 + 0) {
        throw e24;
      }
      v404(1, 0);
    }
  }
  function f40(p121, p122, p123, p124) {
    var v_0x5a334f19 = vF12();
    try {
      return v431(p121, p122, p123, p124);
    } catch (e25) {
      vF13(v_0x5a334f19);
      if (e25 !== e25 + 0) {
        throw e25;
      }
      v404(1, 0);
    }
  }
  function f41(p125, p126, p127, p128) {
    var v_0x5a334f20 = vF12();
    try {
      return v432(p125, p126, p127, p128);
    } catch (e26) {
      vF13(v_0x5a334f20);
      if (e26 !== e26 + 0) {
        throw e26;
      }
      v404(1, 0);
    }
  }
  function f42(p129, p130, p131, p132, p133, p134, p135) {
    var v_0x5a334f21 = vF12();
    try {
      return v434(p129, p130, p131, p132, p133, p134, p135);
    } catch (e27) {
      vF13(v_0x5a334f21);
      if (e27 !== e27 + 0) {
        throw e27;
      }
      v404(1, 0);
    }
  }
  function f43(p136, p137, p138, p139, p140) {
    var v_0x5a334f22 = vF12();
    try {
      return v435(p136, p137, p138, p139, p140);
    } catch (e28) {
      vF13(v_0x5a334f22);
      if (e28 !== e28 + 0) {
        throw e28;
      }
      v404(1, 0);
      return 0x0n;
    }
  }
  function f44(p141, p142, p143, p144) {
    var v_0x5a334f23 = vF12();
    try {
      return v436(p141, p142, p143, p144);
    } catch (e29) {
      vF13(v_0x5a334f23);
      if (e29 !== e29 + 0) {
        throw e29;
      }
      v404(1, 0);
    }
  }
  function f45(p145, p146, p147, p148) {
    var v_0x5a334f24 = vF12();
    try {
      return v437(p145, p146, p147, p148);
    } catch (e30) {
      vF13(v_0x5a334f24);
      if (e30 !== e30 + 0) {
        throw e30;
      }
      v404(1, 0);
    }
  }
  function f46(p149, p150, p151, p152, p153, p154, p155, p156) {
    var v_0x5a334f25 = vF12();
    try {
      v438(p149, p150, p151, p152, p153, p154, p155, p156);
    } catch (e31) {
      vF13(v_0x5a334f25);
      if (e31 !== e31 + 0) {
        throw e31;
      }
      v404(1, 0);
    }
  }
  function f47(p157, p158, p159, p160, p161, p162, p163, p164, p165, p166, p167, p168) {
    var v_0x5a334f26 = vF12();
    try {
      return v439(p157, p158, p159, p160, p161, p162, p163, p164, p165, p166, p167, p168);
    } catch (e32) {
      vF13(v_0x5a334f26);
      if (e32 !== e32 + 0) {
        throw e32;
      }
      v404(1, 0);
    }
  }
  function f48(p169, p170, p171, p172, p173, p174, p175, p176, p177, p178, p179) {
    var v_0x5a334f27 = vF12();
    try {
      v440(p169, p170, p171, p172, p173, p174, p175, p176, p177, p178, p179);
    } catch (e33) {
      vF13(v_0x5a334f27);
      if (e33 !== e33 + 0) {
        throw e33;
      }
      v404(1, 0);
    }
  }
  function f49(p180, p181, p182, p183, p184, p185, p186, p187, p188, p189, p190, p191, p192, p193, p194, p195) {
    var v_0x5a334f28 = vF12();
    try {
      v441(p180, p181, p182, p183, p184, p185, p186, p187, p188, p189, p190, p191, p192, p193, p194, p195);
    } catch (e34) {
      vF13(v_0x5a334f28);
      if (e34 !== e34 + 0) {
        throw e34;
      }
      v404(1, 0);
    }
  }
  var vP = p;
  var v31 = typeof window == "object";
  var v32 = typeof WorkerGlobalScope != "undefined";
  if (typeof process == "object" && process.versions?.node) {
    process.type;
  }
  var v33;
  var v34;
  var v35 = v32 && self.name?.startsWith("em-pthread");
  var vLSthisprogram = "./this.program";
  var vF4 = (p196, p197) => {
    throw p197;
  };
  var v36 = import.meta.url;
  var vLS = "";
  if (v31 || v32) {
    try {
      vLS = new URL(".", v36).href;
    } catch {}
    if (v32) {
      v34 = p198 => {
        var v37 = new XMLHttpRequest();
        v37.open("GET", p198, false);
        v37.responseType = "arraybuffer";
        v37.send(null);
        return new Uint8Array(v37.response);
      };
    }
    v33 = async p199 => {
      var v38 = await fetch(p199, {
        credentials: "same-origin"
      });
      if (v38.ok) {
        return v38.arrayBuffer();
      }
      throw new Error(v38.status + " : " + v38.url);
    };
  }
  var v39;
  var v40;
  var v41;
  var v42;
  var v43;
  var v44;
  var v45;
  var v46;
  var v47;
  var v48;
  var v49;
  var v50;
  var v51;
  var v52;
  var v53;
  var v54;
  var v55;
  var v56;
  var v57 = function () {}.bind();
  var v58 = function () {}.bind();
  var v59 = false;
  if (v35) {
    let vF5 = function (p200) {
      try {
        var v60 = p200.data;
        var v61 = v60.cmd;
        if (v61 === "load") {
          let vA2 = [];
          self.onmessage = p201 => vA2.push(p201);
          v45 = () => {
            postMessage({
              cmd: "loaded"
            });
            for (let v62 of vA2) {
              vF5(v62);
            }
            self.onmessage = vF5;
          };
          for (const v63 of v60.handlers) {
            if (!vP[v63] || !!vP[v63].proxy) {
              vP[v63] = (..._0x1967f9) => {
                postMessage({
                  cmd: "callHandler",
                  handler: v63,
                  args: _0x1967f9
                });
              };
              if (v63 == "print") {
                v57 = vP[v63];
              }
              if (v63 == "printErr") {
                v58 = vP[v63];
              }
            }
          }
          v46 = v60.wasmMemory;
          f2();
          v44(v60.wasmModule);
        } else if (v61 === "run") {
          v64 = v60.pthread_ptr;
          v65 = v52[v64 + 52 >> 2];
          v66 = v52[v64 + 56 >> 2];
          v406(v65, v65 - v66);
          vF13(v65);
          v397(v60.pthread_ptr, 0, 0, 1, 0, 0);
          vO2.threadInitTLS();
          vF74(v60.pthread_ptr);
          if (!v67) {
            v396();
            v67 = true;
          }
          try {
            vF17(v60.start_routine, v60.arg);
          } catch (e35) {
            if (e35 != "unwind") {
              throw e35;
            }
          }
        } else if (v60.target !== "setimmediate") {
          if (v61 === "checkMailbox") {
            if (v67) {
              vF75();
            }
          } else if (v61) {
            v58("worker: received unknown command " + v61);
            v58(v60);
          }
        }
      } catch (e36) {
        v398();
        throw e36;
      }
      var v64;
      var v65;
      var v66;
    };
    var v67 = false;
    self.onunhandledrejection = p202 => {
      throw p202.reason || p202;
    };
    self.onmessage = vF5;
  }
  var v68;
  var v69 = false;
  var vLN05 = 0;
  var v70 = null;
  class C {
    name = "ExitStatus";
    constructor(p203) {
      this.message = "Program terminated with exit(" + p203 + ")";
      this.status = p203;
    }
  }
  var vF6 = p204 => {
    p204.terminate();
    p204.onmessage = p205 => {};
  };
  var vF7 = p206 => {
    var v71 = vO2.pthreads[p206];
    vO2.returnWorkerToPool(v71);
  };
  var vF8 = p207 => {
    while (p207.length > 0) {
      p207.shift()(vP);
    }
  };
  var vA3 = [];
  var vF9 = p208 => vA3.push(p208);
  var vF10 = p209 => {
    var v72 = vO2.getNewWorker();
    if (!v72) {
      return 6;
    }
    vO2.runningWorkers.push(v72);
    vO2.pthreads[p209.pthread_ptr] = v72;
    v72.pthread_ptr = p209.pthread_ptr;
    var vO = {
      cmd: "run",
      start_routine: p209.startRoutine,
      arg: p209.arg,
      pthread_ptr: p209.pthread_ptr
    };
    v72.postMessage(vO, p209.transferList);
    return 0;
  };
  var vLN06 = 0;
  var vF11 = () => v85 || vLN06 > 0;
  var vF12 = () => v409();
  var vF13 = p210 => v407(p210);
  var vF14 = p211 => v408(p211);
  var vF15 = (p212, p213, p214, ..._0x1e6f81) => {
    var v73 = _0x1e6f81.length * 2;
    var vVF12 = vF12();
    var vVF14 = vF14(v73 * 8);
    var v74 = vVF14 >> 3;
    for (var vLN07 = 0; vLN07 < _0x1e6f81.length; vLN07++) {
      var v75 = _0x1e6f81[vLN07];
      if (typeof v75 == "bigint") {
        v55[v74 + vLN07 * 2] = 0x1n;
        v55[v74 + vLN07 * 2 + 1] = v75;
      } else {
        v55[v74 + vLN07 * 2] = 0x0n;
        v54[v74 + vLN07 * 2 + 1] = v75;
      }
    }
    var v_0x2b82ad = v399(p212, p213, v73, vVF14, p214);
    vF13(vVF12);
    return v_0x2b82ad;
  };
  var vF16 = (p215, p216) => {
    v41 = p215;
    if (v35) {
      f9(p215);
      throw "unwind";
    }
    f8(p215);
  };
  var vVF16 = vF16;
  var vO2 = {
    unusedWorkers: [],
    runningWorkers: [],
    tlsInitFunctions: [],
    pthreads: {},
    init() {
      if (!v35) {
        vO2.initMainThread();
      }
    },
    initMainThread() {
      for (var vLN2 = 2; vLN2--;) {
        vO2.allocateUnusedWorker();
      }
      vF9(() => {
        f4();
        vO2.loadWasmModuleToAllWorkers(() => f5());
      });
    },
    terminateAllThreads: () => {
      for (var v76 of vO2.runningWorkers) {
        vF6(v76);
      }
      for (var v76 of vO2.unusedWorkers) {
        vF6(v76);
      }
      vO2.unusedWorkers = [];
      vO2.runningWorkers = [];
      vO2.pthreads = {};
    },
    returnWorkerToPool: p217 => {
      var v77 = p217.pthread_ptr;
      delete vO2.pthreads[v77];
      vO2.unusedWorkers.push(p217);
      vO2.runningWorkers.splice(vO2.runningWorkers.indexOf(p217), 1);
      p217.pthread_ptr = 0;
      v400(v77);
    },
    threadInitTLS() {
      vO2.tlsInitFunctions.forEach(p218 => p218());
    },
    loadWasmModuleToWorker: p219 => new Promise(p220 => {
      p219.onmessage = p221 => {
        var v78 = p221.data;
        var v79 = v78.cmd;
        if (v78.targetThread && v78.targetThread != v394()) {
          var v80 = vO2.pthreads[v78.targetThread];
          if (v80) {
            v80.postMessage(v78, v78.transferList);
          } else {
            v58("Internal error! Worker sent a message \"" + v79 + "\" to target pthread " + v78.targetThread + ", but that thread no longer exists!");
          }
        } else if (v79 === "checkMailbox") {
          vF75();
        } else if (v79 === "spawnThread") {
          vF10(v78);
        } else if (v79 === "cleanupThread") {
          vF46(() => vF7(v78.thread));
        } else if (v79 === "loaded") {
          p219.loaded = true;
          p220(p219);
        } else if (v78.target === "setimmediate") {
          p219.postMessage(v78);
        } else if (v79 === "callHandler") {
          vP[v78.handler](...v78.args);
        } else if (v79) {
          v58("worker sent an unknown command " + v79);
        }
      };
      p219.onerror = p222 => {
        v58("worker sent an error! " + p222.filename + ":" + p222.lineno + ": " + p222.message);
        throw p222;
      };
      var vA4 = [];
      for (var v81 of ["onExit", "onAbort", "print", "printErr"]) {
        if (vP.propertyIsEnumerable(v81)) {
          vA4.push(v81);
        }
      }
      p219.postMessage({
        cmd: "load",
        handlers: vA4,
        wasmMemory: v46,
        wasmModule: v40
      });
    }),
    loadWasmModuleToAllWorkers(p223) {
      if (v35) {
        return p223();
      }
      Promise.all(vO2.unusedWorkers.map(vO2.loadWasmModuleToWorker)).then(p223);
    },
    allocateUnusedWorker() {
      var v82;
      if (vP.mainScriptUrlOrBlob) {
        var v83 = vP.mainScriptUrlOrBlob;
        if (typeof v83 != "string") {
          v83 = URL.createObjectURL(v83);
        }
        v82 = new Worker(v83, {
          type: "module",
          name: "em-pthread"
        });
      } else {
        v82 = new Worker(new URL("/static/assets/wasm-C5IAUDsv.js", import.meta.url), {
          type: "module",
          name: "em-pthread"
        });
      }
      vO2.unusedWorkers.push(v82);
    },
    getNewWorker: () => {
      if (vO2.unusedWorkers.length == 0) {
        vO2.allocateUnusedWorker();
        vO2.loadWasmModuleToWorker(vO2.unusedWorkers[0]);
      }
      return vO2.unusedWorkers.pop();
    }
  };
  var vA5 = [];
  var vO3 = {};
  var vF17 = (p224, p225) => {
    var v84;
    vLN06 = 0;
    v85 = 0;
    v84 = v418(p224, p225);
    if (vF11()) {
      v41 = v84;
    } else {
      v401(v84);
    }
  };
  vF17.isAsync = true;
  var v85 = true;
  var v86 = new TextDecoder();
  var vF18 = (p226, p227, p228, p229) => {
    var v87 = p227 + p228;
    if (p229) {
      return v87;
    }
    while (p226[p227] && !(p227 >= v87)) {
      ++p227;
    }
    return p227;
  };
  var vF19 = (p230, p231, p232) => {
    if (!p230) {
      return "";
    }
    var vVF18 = vF18(v48, p230, p231, p232);
    return v86.decode(v48.slice(p230, vVF18));
  };
  var vF20 = (p233, p234, p235, p236) => f6("Assertion failed: " + vF19(p233) + ", at: " + [p234 ? vF19(p234) : "unknown filename", p235, p236 ? vF19(p236) : "unknown function"]);
  var vA6 = [];
  var vLN08 = 0;
  var vF21 = p237 => {
    var v88 = new C2(p237);
    if (!v88.get_caught()) {
      v88.set_caught(true);
      vLN08--;
    }
    v88.set_rethrown(false);
    vA6.push(v88);
    v411(p237);
    return v413(p237);
  };
  var vLN09 = 0;
  var vF22 = () => {
    v404(0, 0);
    var v89 = vA6.pop();
    v410(v89.excPtr);
    vLN09 = 0;
  };
  class C2 {
    constructor(p238) {
      this.excPtr = p238;
      this.ptr = p238 - 24;
    }
    set_type(p239) {
      v52[this.ptr + 4 >> 2] = p239;
    }
    get_type() {
      return v52[this.ptr + 4 >> 2];
    }
    set_destructor(p240) {
      v52[this.ptr + 8 >> 2] = p240;
    }
    get_destructor() {
      return v52[this.ptr + 8 >> 2];
    }
    set_caught(p241) {
      p241 = p241 ? 1 : 0;
      v47[this.ptr + 12] = p241;
    }
    get_caught() {
      return v47[this.ptr + 12] != 0;
    }
    set_rethrown(p242) {
      p242 = p242 ? 1 : 0;
      v47[this.ptr + 13] = p242;
    }
    get_rethrown() {
      return v47[this.ptr + 13] != 0;
    }
    init(p243, p244) {
      this.set_adjusted_ptr(0);
      this.set_type(p243);
      this.set_destructor(p244);
    }
    set_adjusted_ptr(p245) {
      v52[this.ptr + 16 >> 2] = p245;
    }
    get_adjusted_ptr() {
      return v52[this.ptr + 16 >> 2];
    }
  }
  var vF23 = p246 => v405(p246);
  var vF24 = p247 => {
    var vVLN09 = vLN09;
    if (!vVLN09) {
      vF23(0);
      return 0;
    }
    var v90 = new C2(vVLN09);
    v90.set_adjusted_ptr(vVLN09);
    var v91 = v90.get_type();
    if (!v91) {
      vF23(0);
      return vVLN09;
    }
    for (var v92 of p247) {
      if (v92 === 0 || v92 === v91) {
        break;
      }
      var v93 = v90.ptr + 16;
      if (v412(v92, v91, v93)) {
        vF23(v92);
        return vVLN09;
      }
    }
    vF23(v91);
    return vVLN09;
  };
  var vF25 = () => vF24([]);
  var vF26 = p248 => vF24([p248]);
  var vF27 = (p249, p250, p251) => {
    new C2(p249).init(p250, p251);
    vLN08++;
    throw vLN09 = p249;
  };
  var vF28 = () => vLN08;
  var vF29 = (p252, p253, p254, p255) => {
    if (typeof SharedArrayBuffer == "undefined") {
      return 6;
    }
    var vA7 = [];
    if (v35 && vA7.length === 0) {
      return f10(p252, p253, p254, p255);
    }
    var vO4 = {
      startRoutine: p254,
      pthread_ptr: p252,
      arg: p255,
      transferList: vA7
    };
    if (v35) {
      vO4.cmd = "spawnThread";
      postMessage(vO4, vA7);
      return 0;
    } else {
      return vF10(vO4);
    }
  };
  var vF30 = p256 => {
    vLN09 ||= p256;
    throw vLN09;
  };
  var vF31 = () => f6("");
  var vF32 = p257 => {
    var vLS2 = "";
    for (;;) {
      var v94 = v48[p257++];
      if (!v94) {
        return vLS2;
      }
      vLS2 += String.fromCharCode(v94);
    }
  };
  var vO5 = {};
  var vO6 = {};
  var vO7 = {};
  var vC = class extends Error {
    constructor(p258) {
      super(p258);
      this.name = "BindingError";
    }
  };
  var vF33 = p259 => {
    throw new vC(p259);
  };
  var vF34 = (p260, p261, p262) => {
    switch (p261) {
      case 1:
        if (p262) {
          return p263 => v47[p263];
        } else {
          return p264 => v48[p264];
        }
      case 2:
        if (p262) {
          return p265 => v49[p265 >> 1];
        } else {
          return p266 => v50[p266 >> 1];
        }
      case 4:
        if (p262) {
          return p267 => v51[p267 >> 2];
        } else {
          return p268 => v52[p268 >> 2];
        }
      case 8:
        if (p262) {
          return p269 => v55[p269 >> 3];
        } else {
          return p270 => v56[p270 >> 3];
        }
      default:
        throw new TypeError("invalid integer width (" + p261 + "): " + p260);
    }
  };
  var vF35 = (p271, p272, p273, p274, p275) => {
    p272 = vF32(p272);
    const v95 = 0x0n === p274;
    let vF36 = p276 => p276;
    if (v95) {
      const v96 = p273 * 8;
      vF36 = p277 => BigInt.asUintN(v96, p277);
      p275 = vF36(p275);
    }
    f11(p271, {
      name: p272,
      fromWireType: vF36,
      toWireType: (p278, p279) => {
        if (typeof p279 == "number") {
          p279 = BigInt(p279);
        }
        return p279;
      },
      readValueFromPointer: vF34(p272, p273, !v95),
      destructorFunction: null
    });
  };
  var vF37 = (p280, p281, p282, p283) => {
    f11(p280, {
      name: p281 = vF32(p281),
      fromWireType: function (p284) {
        return !!p284;
      },
      toWireType: function (p285, p286) {
        if (p286) {
          return p282;
        } else {
          return p283;
        }
      },
      readValueFromPointer: function (p287) {
        return this.fromWireType(v48[p287]);
      },
      destructorFunction: null
    });
  };
  var vA8 = [];
  var vA9 = [0, 1,, 1, null, 1, true, 1, false, 1];
  var vF38 = p288 => {
    if (p288 > 9 && --vA9[p288 + 1] == 0) {
      vA9[p288] = undefined;
      vA8.push(p288);
    }
  };
  var vO8 = {
    toValue: p289 => {
      if (!p289) {
        vF33("Cannot use deleted val. handle = " + p289);
      }
      return vA9[p289];
    },
    toHandle: p290 => {
      switch (p290) {
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
            const v97 = vA8.pop() || vA9.length;
            vA9[v97] = p290;
            vA9[v97 + 1] = 1;
            return v97;
          }
      }
    }
  };
  var vO9 = {
    name: "emscripten::val",
    fromWireType: p291 => {
      var v98 = vO8.toValue(p291);
      vF38(p291);
      return v98;
    },
    toWireType: (p292, p293) => vO8.toHandle(p293),
    readValueFromPointer: f12,
    destructorFunction: null
  };
  var vF39 = p294 => f11(p294, vO9);
  var vF40 = (p295, p296) => {
    switch (p296) {
      case 4:
        return function (p297) {
          return this.fromWireType(v53[p297 >> 2]);
        };
      case 8:
        return function (p298) {
          return this.fromWireType(v54[p298 >> 3]);
        };
      default:
        throw new TypeError("invalid float width (" + p296 + "): " + p295);
    }
  };
  var vF41 = (p299, p300, p301) => {
    f11(p299, {
      name: p300 = vF32(p300),
      fromWireType: p302 => p302,
      toWireType: (p303, p304) => p304,
      readValueFromPointer: vF40(p300, p301),
      destructorFunction: null
    });
  };
  var vF42 = (p305, p306) => Object.defineProperty(p306, "name", {
    value: p305
  });
  var vF43 = p307 => {
    while (p307.length) {
      var v99 = p307.pop();
      p307.pop()(v99);
    }
  };
  var vF44 = p308 => {
    try {
      return p308();
    } catch (e37) {
      f6(e37);
    }
  };
  var vF45 = p309 => {
    if (p309 instanceof C || p309 == "unwind") {
      return v41;
    }
    vF4(0, p309);
  };
  var vF46 = p310 => {
    if (!v59) {
      try {
        p310();
        (() => {
          if (!vF11()) {
            try {
              if (v35) {
                v401(v41);
              } else {
                vVF16(v41);
              }
            } catch (e38) {
              vF45(e38);
            }
          }
        })();
      } catch (e39) {
        vF45(e39);
      }
    }
  };
  var vF47 = () => {
    vLN06 += 1;
  };
  var vF48 = () => {
    vLN06 -= 1;
  };
  var vO10 = {
    instrumentWasmImports(p311) {
      var v100 = /^(invoke_.*|__asyncjs__.*)$/;
      for (let [v101, v102] of Object.entries(p311)) {
        if (typeof v102 == "function") {
          if (!v102.isAsync) {
            v100.test(v101);
          }
        }
      }
    },
    instrumentFunction(p312) {
      var vF49 = (..._0x4dd01b) => {
        vO10.exportCallStack.push(p312);
        try {
          return p312(..._0x4dd01b);
        } finally {
          if (!v59) {
            vO10.exportCallStack.pop();
            vO10.maybeStopUnwind();
          }
        }
      };
      vO10.funcWrappers.set(p312, vF49);
      return vF49;
    },
    instrumentWasmExports(p313) {
      var vO11 = {};
      for (let [v103, v104] of Object.entries(p313)) {
        if (typeof v104 == "function") {
          var v105 = vO10.instrumentFunction(v104);
          vO11[v103] = v105;
        } else {
          vO11[v103] = v104;
        }
      }
      return vO11;
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
    getCallStackId(p314) {
      if (!vO10.callstackFuncToId.has(p314)) {
        var v106 = vO10.callStackId++;
        vO10.callstackFuncToId.set(p314, v106);
        vO10.callStackIdToFunc.set(v106, p314);
      }
      return vO10.callstackFuncToId.get(p314);
    },
    maybeStopUnwind() {
      if (vO10.currData && vO10.state === vO10.State.Unwinding && vO10.exportCallStack.length === 0) {
        vO10.state = vO10.State.Normal;
        vF47();
        vF44(v443);
        if (typeof Fibers != "undefined") {
          Fibers.trampoline();
        }
      }
    },
    whenDone: () => new Promise((p315, p316) => {
      vO10.asyncPromiseHandlers = {
        resolve: p315,
        reject: p316
      };
    }),
    allocateData() {
      var v_0x3b3145 = v392(12 + vO10.StackSize);
      vO10.setDataHeader(v_0x3b3145, v_0x3b3145 + 12, vO10.StackSize);
      vO10.setDataRewindFunc(v_0x3b3145);
      return v_0x3b3145;
    },
    setDataHeader(p317, p318, p319) {
      v52[p317 >> 2] = p318;
      v52[p317 + 4 >> 2] = p318 + p319;
    },
    setDataRewindFunc(p320) {
      var v107 = vO10.exportCallStack[0];
      var v108 = vO10.getCallStackId(v107);
      v51[p320 + 8 >> 2] = v108;
    },
    getDataRewindFunc(p321) {
      var v109 = v51[p321 + 8 >> 2];
      return vO10.callStackIdToFunc.get(v109);
    },
    doRewind(p322) {
      var v110 = vO10.getDataRewindFunc(p322);
      var v111 = vO10.funcWrappers.get(v110);
      vF48();
      return v111();
    },
    handleSleep(p323) {
      if (!v59) {
        if (vO10.state === vO10.State.Normal) {
          var v112 = false;
          var v113 = false;
          p323((p324 = 0) => {
            if (!v59 && (vO10.handleSleepReturnValue = p324, v112 = true, v113)) {
              vO10.state = vO10.State.Rewinding;
              vF44(() => v444(vO10.currData));
              if (typeof MainLoop != "undefined" && MainLoop.func) {
                MainLoop.resume();
              }
              var v114;
              var v115 = false;
              try {
                v114 = vO10.doRewind(vO10.currData);
              } catch (e40) {
                v114 = e40;
                v115 = true;
              }
              var v116 = false;
              if (!vO10.currData) {
                var v117 = vO10.asyncPromiseHandlers;
                if (v117) {
                  vO10.asyncPromiseHandlers = null;
                  (v115 ? v117.reject : v117.resolve)(v114);
                  v116 = true;
                }
              }
              if (v115 && !v116) {
                throw v114;
              }
            }
          });
          v113 = true;
          if (!v112) {
            vO10.state = vO10.State.Unwinding;
            vO10.currData = vO10.allocateData();
            if (typeof MainLoop != "undefined" && MainLoop.func) {
              MainLoop.pause();
            }
            vF44(() => v442(vO10.currData));
          }
        } else if (vO10.state === vO10.State.Rewinding) {
          vO10.state = vO10.State.Normal;
          vF44(v445);
          v391(vO10.currData);
          vO10.currData = null;
          vO10.sleepCallbacks.forEach(vF46);
        } else {
          f6("invalid state: " + vO10.state);
        }
        return vO10.handleSleepReturnValue;
      }
    },
    handleAsync: p325 => vO10.handleSleep(p326 => {
      p325().then(p326);
    })
  };
  var vF50 = (p327, p328, p329) => {
    if (p327[p328].overloadTable === undefined) {
      var v118 = p327[p328];
      p327[p328] = function (..._0xe0fdb3) {
        if (!p327[p328].overloadTable.hasOwnProperty(_0xe0fdb3.length)) {
          vF33("Function '" + p329 + "' called with an invalid number of arguments (" + _0xe0fdb3.length + ") - expects one of (" + p327[p328].overloadTable + ")!");
        }
        return p327[p328].overloadTable[_0xe0fdb3.length].apply(this, _0xe0fdb3);
      };
      p327[p328].overloadTable = [];
      p327[p328].overloadTable[v118.argCount] = v118;
    }
  };
  var vC2 = class extends Error {
    constructor(p330) {
      super(p330);
      this.name = "InternalError";
    }
  };
  var vF51 = p331 => {
    throw new vC2(p331);
  };
  class C3 extends Error {}
  var vF52 = p332 => {
    var v_0x467549 = v395(p332);
    var vVF32 = vF32(v_0x467549);
    v391(v_0x467549);
    return vVF32;
  };
  var vF53 = (p333, p334, p335, p336, p337, p338, p339, p340) => {
    var vF54 = ((p341, p342) => {
      var vA10 = [];
      for (var vLN010 = 0; vLN010 < p341; vLN010++) {
        vA10.push(v52[p342 + vLN010 * 4 >> 2]);
      }
      return vA10;
    })(p334, p335);
    p333 = (p343 => {
      const v119 = (p343 = p343.trim()).indexOf("(");
      if (v119 === -1) {
        return p343;
      } else {
        return p343.slice(0, v119);
      }
    })(p333 = vF32(p333));
    p337 = ((p344, p345) => {
      var v120;
      var v121;
      v120 = p344 = vF32(p344);
      v121 = p345;
      var vF55 = (..._0x2fe407) => ((p346, p347, p348 = []) => {
        var vF56 = ((p349, p350, p351) => {
          p349 = p349.replace(/p/g, "i");
          return (0, vO3[p349])(p350, ...p351);
        })(p346, p347, p348);
        return vF56;
      })(v120, v121, _0x2fe407);
      if (typeof vF55 != "function") {
        vF33("unknown function pointer with signature " + p344 + ": " + p345);
      }
      return vF55;
    })(p336, p337, p339);
    ((p352, p353, p354) => {
      if (vP.hasOwnProperty(p352)) {
        if (p354 === undefined || vP[p352].overloadTable !== undefined && vP[p352].overloadTable[p354] !== undefined) {
          vF33("Cannot register public name '" + p352 + "' twice");
        }
        vF50(vP, p352, p352);
        if (vP[p352].overloadTable.hasOwnProperty(p354)) {
          vF33("Cannot register multiple overloads of a function with the same number of arguments (" + p354 + ")!");
        }
        vP[p352].overloadTable[p354] = p353;
      } else {
        vP[p352] = p353;
        vP[p352].argCount = p354;
      }
    })(p333, function () {
      ((p355, p356) => {
        var vA11 = [];
        var vO12 = {};
        p356.forEach(function f50(p357) {
          if (!vO12[p357] && !vO6[p357]) {
            if (vO7[p357]) {
              vO7[p357].forEach(f50);
            } else {
              vA11.push(p357);
              vO12[p357] = true;
            }
          }
        });
        throw new C3(p355 + ": " + vA11.map(vF52).join([", "]));
      })("Cannot call " + p333 + " due to unbound types", vF54);
    }, p334 - 1);
    ((p358, p359) => {
      function f51(p360) {
        var v122;
        var v123;
        v123 = [(v122 = p360)[0], null].concat(v122.slice(1));
        ((p361, p362, p363) => {
          if (!vP.hasOwnProperty(p361)) {
            vF51("Replacing nonexistent public symbol");
          }
          if (vP[p361].overloadTable !== undefined && p363 !== undefined) {
            vP[p361].overloadTable[p363] = p362;
          } else {
            vP[p361] = p362;
            vP[p361].argCount = p363;
          }
        })(p333, function (p364, p365, p366, p367, p368, p369) {
          var v124 = p365.length;
          if (v124 < 2) {
            vF33("argTypes array size mismatch! Must at least get return value and 'this' types!");
          }
          p365[1];
          var vF132 = f13(p365);
          var v125 = !p365[0].isVoid;
          var v126 = p365[0];
          var v127 = p365[1];
          var vA12 = [p364, vF33, p367, p368, vF43, v126.fromWireType.bind(v126), v127?.toWireType.bind(v127)];
          for (var vLN22 = 2; vLN22 < v124; ++vLN22) {
            var v128 = p365[vLN22];
            vA12.push(v128.toWireType.bind(v128));
          }
          vA12.push(vO10);
          if (!vF132) {
            for (vLN22 = 2; vLN22 < p365.length; ++vLN22) {
              if (p365[vLN22].destructorFunction !== null) {
                vA12.push(p365[vLN22].destructorFunction);
              }
            }
          }
          let vF57 = function (p370, p371, p372, p373) {
            var vF133 = f13(p370);
            for (var v129 = p370.length - 2, vA13 = [], vA14 = ["fn"], vLN011 = 0; vLN011 < v129; ++vLN011) {
              vA13.push("arg" + vLN011);
              vA14.push("arg" + vLN011 + "Wired");
            }
            vA13 = vA13.join(",");
            vA14 = vA14.join(",");
            var v130 = "return function (" + vA13 + ") {\n";
            if (vF133) {
              v130 += "var destructors = [];\n";
            }
            var v131 = vF133 ? "destructors" : "null";
            var vA15 = ["humanName", "throwBindingError", "invoker", "fn", "runDestructors", "fromRetWire", "toClassParamWire"];
            for (vLN011 = 0; vLN011 < v129; ++vLN011) {
              var v132 = "toArg" + vLN011 + "Wire";
              v130 += "var arg" + vLN011 + "Wired = " + v132 + "(" + v131 + ", arg" + vLN011 + ");\n";
              vA15.push(v132);
            }
            v130 += (p372 || p373 ? "var rv = " : "") + ("invoker(" + vA14 + ");\n");
            var v133 = p372 ? "rv" : "";
            vA15.push("Asyncify");
            v130 += "function onDone(" + v133 + ") {\n";
            if (vF133) {
              v130 += "runDestructors(destructors);\n";
            } else {
              for (vLN011 = 2; vLN011 < p370.length; ++vLN011) {
                var v134 = vLN011 === 1 ? "thisWired" : "arg" + (vLN011 - 2) + "Wired";
                if (p370[vLN011].destructorFunction !== null) {
                  v130 += v134 + "_dtor(" + v134 + ");\n";
                  vA15.push(v134 + "_dtor");
                }
              }
            }
            if (p372) {
              v130 += "var ret = fromRetWire(rv);\nreturn ret;\n";
            }
            v130 += "}\n";
            v130 += "return Asyncify.currData ? Asyncify.whenDone().then(onDone) : onDone(" + v133 + ");\n";
            v130 += "}\n";
            return new Function(vA15, v130);
          }(p365, 0, v125, p369);
          var vVF57 = vF57(...vA12);
          return vF42(p364, vVF57);
        }(p333, v123, 0, p337, p338, p339), p334 - 1);
        var vA16 = [];
        if (vA16.length !== p358.length) {
          vF51("Mismatched type converter count");
        }
        for (var vLN012 = 0; vLN012 < p358.length; ++vLN012) {
          f11(p358[vLN012], vA16[vLN012]);
        }
      }
      p358.forEach(p374 => vO7[p374] = p359);
      var v135 = new Array(p359.length);
      var vA17 = [];
      var vLN013 = 0;
      p359.forEach((p375, p376) => {
        if (vO6.hasOwnProperty(p375)) {
          v135[p376] = vO6[p375];
        } else {
          vA17.push(p375);
          if (!vO5.hasOwnProperty(p375)) {
            vO5[p375] = [];
          }
          vO5[p375].push(() => {
            v135[p376] = vO6[p375];
            if (++vLN013 === vA17.length) {
              f51(v135);
            }
          });
        }
      });
      if (vA17.length === 0) {
        f51(v135);
      }
    })([], vF54);
  };
  var vF58 = (p377, p378, p379, p380, p381) => {
    p378 = vF32(p378);
    let vF59 = p382 => p382;
    if (p380 === 0) {
      var v136 = 32 - p379 * 8;
      vF59 = p383 => p383 << v136 >>> v136;
      p381 = vF59(p381);
    }
    f11(p377, {
      name: p378,
      fromWireType: vF59,
      toWireType: (p384, p385) => p385,
      readValueFromPointer: vF34(p378, p379, p380 !== 0),
      destructorFunction: null
    });
  };
  var vF60 = (p386, p387, p388) => {
    function f52(p389) {
      var v137 = v52[p389 >> 2];
      var v138 = v52[p389 + 4 >> 2];
      return new v139(v47.buffer, v138, v137);
    }
    var v139 = [Int8Array, Uint8Array, Int16Array, Uint16Array, Int32Array, Uint32Array, Float32Array, Float64Array, BigInt64Array, BigUint64Array][p387];
    f11(p386, {
      name: p388 = vF32(p388),
      fromWireType: f52,
      readValueFromPointer: f52
    }, {
      ignoreDuplicateRegistrations: true
    });
  };
  var vF61 = (p390, p391, p392, p393) => {
    if (!(p393 > 0)) {
      return 0;
    }
    var vP392 = p392;
    var v140 = p392 + p393 - 1;
    for (var vLN014 = 0; vLN014 < p390.length; ++vLN014) {
      var v141 = p390.codePointAt(vLN014);
      if (v141 <= 127) {
        if (p392 >= v140) {
          break;
        }
        p391[p392++] = v141;
      } else if (v141 <= 2047) {
        if (p392 + 1 >= v140) {
          break;
        }
        p391[p392++] = v141 >> 6 | 192;
        p391[p392++] = v141 & 63 | 128;
      } else if (v141 <= 65535) {
        if (p392 + 2 >= v140) {
          break;
        }
        p391[p392++] = v141 >> 12 | 224;
        p391[p392++] = v141 >> 6 & 63 | 128;
        p391[p392++] = v141 & 63 | 128;
      } else {
        if (p392 + 3 >= v140) {
          break;
        }
        p391[p392++] = v141 >> 18 | 240;
        p391[p392++] = v141 >> 12 & 63 | 128;
        p391[p392++] = v141 >> 6 & 63 | 128;
        p391[p392++] = v141 & 63 | 128;
        vLN014++;
      }
    }
    p391[p392] = 0;
    return p392 - vP392;
  };
  var vF62 = (p394, p395, p396) => vF61(p394, v48, p395, p396);
  var vF63 = p397 => {
    var vLN015 = 0;
    for (var vLN016 = 0; vLN016 < p397.length; ++vLN016) {
      var v142 = p397.charCodeAt(vLN016);
      if (v142 <= 127) {
        vLN015++;
      } else if (v142 <= 2047) {
        vLN015 += 2;
      } else if (v142 >= 55296 && v142 <= 57343) {
        vLN015 += 4;
        ++vLN016;
      } else {
        vLN015 += 3;
      }
    }
    return vLN015;
  };
  var vF64 = (p398, p399) => {
    f11(p398, {
      name: p399 = vF32(p399),
      fromWireType(p400) {
        var v143;
        var v144 = v52[p400 >> 2];
        v143 = vF19(p400 + 4, v144, true);
        v391(p400);
        return v143;
      },
      toWireType(p401, p402) {
        var v145;
        if (p402 instanceof ArrayBuffer) {
          p402 = new Uint8Array(p402);
        }
        var v146 = typeof p402 == "string";
        if (!v146 && (!ArrayBuffer.isView(p402) || p402.BYTES_PER_ELEMENT != 1)) {
          vF33("Cannot pass non-string to std::string");
        }
        v145 = v146 ? vF63(p402) : p402.length;
        var v_0x3b31452 = v392(4 + v145 + 1);
        var v147 = v_0x3b31452 + 4;
        v52[v_0x3b31452 >> 2] = v145;
        if (v146) {
          vF62(p402, v147, v145 + 1);
        } else {
          v48.set(p402, v147);
        }
        if (p401 !== null) {
          p401.push(v391, v_0x3b31452);
        }
        return v_0x3b31452;
      },
      readValueFromPointer: f12,
      destructorFunction(p403) {
        v391(p403);
      }
    });
  };
  var v148 = new TextDecoder("utf-16le");
  var vF65 = (p404, p405, p406) => {
    var v149 = p404 >> 1;
    var vVF182 = vF18(v50, v149, p405 / 2, p406);
    return v148.decode(v50.slice(v149, vVF182));
  };
  var vF66 = (p407, p408, p409) => {
    p409 ??= 2147483647;
    if (p409 < 2) {
      return 0;
    }
    var vP408 = p408;
    for (var v150 = (p409 -= 2) < p407.length * 2 ? p409 / 2 : p407.length, vLN017 = 0; vLN017 < v150; ++vLN017) {
      var v151 = p407.charCodeAt(vLN017);
      v49[p408 >> 1] = v151;
      p408 += 2;
    }
    v49[p408 >> 1] = 0;
    return p408 - vP408;
  };
  var vF67 = p410 => p410.length * 2;
  var vF68 = (p411, p412, p413) => {
    var vLS3 = "";
    var v152 = p411 >> 2;
    for (var vLN018 = 0; !(vLN018 >= p412 / 4); vLN018++) {
      var v153 = v52[v152 + vLN018];
      if (!v153 && !p413) {
        break;
      }
      vLS3 += String.fromCodePoint(v153);
    }
    return vLS3;
  };
  var vF69 = (p414, p415, p416) => {
    p416 ??= 2147483647;
    if (p416 < 4) {
      return 0;
    }
    var vP415 = p415;
    var v154 = vP415 + p416 - 4;
    for (var vLN019 = 0; vLN019 < p414.length; ++vLN019) {
      var v155 = p414.codePointAt(vLN019);
      if (v155 > 65535) {
        vLN019++;
      }
      v51[p415 >> 2] = v155;
      if ((p415 += 4) + 4 > v154) {
        break;
      }
    }
    v51[p415 >> 2] = 0;
    return p415 - vP415;
  };
  var vF70 = p417 => {
    var vLN020 = 0;
    for (var vLN021 = 0; vLN021 < p417.length; ++vLN021) {
      if (p417.codePointAt(vLN021) > 65535) {
        vLN021++;
      }
      vLN020 += 4;
    }
    return vLN020;
  };
  var vF71 = (p418, p419, p420) => {
    var v156;
    var v157;
    var v158;
    p420 = vF32(p420);
    if (p419 === 2) {
      v156 = vF65;
      v157 = vF66;
      v158 = vF67;
    } else {
      v156 = vF68;
      v157 = vF69;
      v158 = vF70;
    }
    f11(p418, {
      name: p420,
      fromWireType: p421 => {
        var v159 = v52[p421 >> 2];
        var vV156 = v156(p421 + 4, v159 * p419, true);
        v391(p421);
        return vV156;
      },
      toWireType: (p422, p423) => {
        if (typeof p423 != "string") {
          vF33("Cannot pass non-string to C++ string type " + p420);
        }
        var vV158 = v158(p423);
        var v_0x3b31453 = v392(4 + vV158 + p419);
        v52[v_0x3b31453 >> 2] = vV158 / p419;
        v157(p423, v_0x3b31453 + 4, vV158 + p419);
        if (p422 !== null) {
          p422.push(v391, v_0x3b31453);
        }
        return v_0x3b31453;
      },
      readValueFromPointer: f12,
      destructorFunction(p424) {
        v391(p424);
      }
    });
  };
  var vF72 = (p425, p426) => {
    f11(p425, {
      isVoid: true,
      name: p426 = vF32(p426),
      fromWireType: () => {},
      toWireType: (p427, p428) => {}
    });
  };
  var vF73 = p429 => {
    v397(p429, !v32, 1, !v31, 65536, false);
    vO2.threadInitTLS();
  };
  var vF74 = p430 => {
    if (typeof Atomics.waitAsync == "function") {
      Atomics.waitAsync(v51, p430 >> 2, p430).value.then(vF75);
      var v160 = p430 + 128;
      Atomics.store(v51, v160 >> 2, 1);
    }
  };
  var vF75 = () => {
    var v_0x30e10f = v394();
    if (v_0x30e10f) {
      vF74(v_0x30e10f);
      vF46(v403);
    }
  };
  var vF76 = (p431, p432) => {
    if (p431 == p432) {
      setTimeout(vF75);
    } else if (v35) {
      postMessage({
        targetThread: p431,
        cmd: "checkMailbox"
      });
    } else {
      var v161 = vO2.pthreads[p431];
      if (!v161) {
        return;
      }
      v161.postMessage({
        cmd: "checkMailbox"
      });
    }
  };
  var vA18 = [];
  var vF77 = (p433, p434, p435, p436, p437) => {
    p436 /= 2;
    vA18.length = p436;
    var v162 = p437 >> 3;
    for (var vLN022 = 0; vLN022 < p436; vLN022++) {
      if (v55[v162 + vLN022 * 2]) {
        vA18[vLN022] = v55[v162 + vLN022 * 2 + 1];
      } else {
        vA18[vLN022] = v54[v162 + vLN022 * 2 + 1];
      }
    }
    var v163 = p434 ? vO32[p434] : vA31[p433];
    vO2.currentProxiedOperationCallerThread = p435;
    var vV163 = v163(...vA18);
    vO2.currentProxiedOperationCallerThread = 0;
    return vV163;
  };
  var vF78 = () => {
    v85 = false;
    vLN06 = 0;
  };
  var vF79 = p438 => {
    if (v35) {
      postMessage({
        cmd: "cleanupThread",
        thread: p438
      });
    } else {
      vF7(p438);
    }
  };
  var vF80 = p439 => {};
  var vF81 = p440 => vO10.handleAsync(async () => {
    var v164 = await vO8.toValue(p440);
    return vO8.toHandle(v164);
  });
  vF81.isAsync = true;
  var vA19 = [];
  var vF82 = (p441, p442, p443) => {
    var vA20 = [];
    var vP441 = p441(vA20, p443);
    if (vA20.length) {
      v52[p442 >> 2] = vO8.toHandle(vA20);
    }
    return vP441;
  };
  var vO13 = {};
  var vF83 = p444 => {
    var v165 = vO13[p444];
    if (v165 === undefined) {
      return vF32(p444);
    } else {
      return v165;
    }
  };
  var vF84 = (p445, p446, p447) => {
    var [v166, ..._0x328875] = ((p448, p449) => {
      var v167;
      var v168;
      var v169;
      var v170 = new Array(p448);
      for (var vLN023 = 0; vLN023 < p448; ++vLN023) {
        v167 = v52[p449 + vLN023 * 4 >> 2];
        v168 = "parameter " + vLN023;
        if ((v169 = vO6[v167]) === undefined) {
          vF33(v168 + " has unknown type " + vF52(v167));
        }
        v170[vLN023] = v169;
      }
      return v170;
    })(p445, p446);
    var v171 = v166.toWireType.bind(v166);
    var v172 = _0x328875.map(p450 => p450.readValueFromPointer.bind(p450));
    p445--;
    var v173;
    var vO14 = {
      toValue: vO8.toValue
    };
    var v174 = v172.map((p451, p452) => {
      var v175 = "argFromPtr" + p452;
      vO14[v175] = p451;
      return v175 + "(args" + (p452 ? "+" + p452 * 8 : "") + ")";
    });
    switch (p447) {
      case 0:
        v173 = "toValue(handle)";
        break;
      case 2:
        v173 = "new (toValue(handle))";
        break;
      case 3:
        v173 = "";
        break;
      case 1:
        vO14.getStringOrSymbol = vF83;
        v173 = "toValue(handle)[getStringOrSymbol(methodName)]";
    }
    v173 += "(" + v174 + ")";
    if (!v166.isVoid) {
      vO14.toReturnWire = v171;
      vO14.emval_returnValue = vF82;
      v173 = "return emval_returnValue(toReturnWire, destructorsRef, " + v173 + ")";
    }
    v173 = "return function (handle, methodName, destructorsRef, args) {\n  " + v173 + "\n  }";
    var v176;
    var v177;
    var v178 = new Function(Object.keys(vO14), v173)(...Object.values(vO14));
    var v179 = "methodCaller<(" + _0x328875.map(p453 => p453.name) + ") => " + v166.name + ">";
    v176 = vF42(v179, v178);
    v177 = vA19.length;
    vA19.push(v176);
    return v177;
  };
  var vF85 = () => globalThis;
  var vF86 = p454 => p454 === 0 ? vO8.toHandle(vF85()) : (p454 = vF83(p454), vO8.toHandle(vF85()[p454]));
  var vF87 = p455 => {
    p455 = vF83(p455);
    return vO8.toHandle(vP[p455]);
  };
  var vF88 = (p456, p457) => {
    p456 = vO8.toValue(p456);
    p457 = vO8.toValue(p457);
    return vO8.toHandle(p456[p457]);
  };
  var vF89 = p458 => {
    if (p458 > 9) {
      vA9[p458 + 1] += 1;
    }
  };
  var vF90 = (p459, p460, p461, p462, p463) => vA19[p459](p460, p461, p462, p463);
  var vF91 = () => vO8.toHandle([]);
  var vF92 = p464 => vO8.toHandle(vF83(p464));
  var vF93 = () => vO8.toHandle({});
  var vF94 = p465 => {
    var v180 = vO8.toValue(p465);
    vF43(v180);
    vF38(p465);
  };
  var vF95 = (p466, p467, p468) => {
    p466 = vO8.toValue(p466);
    p467 = vO8.toValue(p467);
    p468 = vO8.toValue(p468);
    p466[p467] = p468;
  };
  var vF96 = p469 => {
    p469 = vO8.toValue(p469);
    return vO8.toHandle(typeof p469);
  };
  var vA21 = [0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335];
  var vA22 = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
  var vF97 = p470 => {
    var v181;
    return ((v181 = p470.getFullYear()) % 4 != 0 || v181 % 100 == 0 && v181 % 400 != 0 ? vA22 : vA21)[p470.getMonth()] + p470.getDate() - 1;
  };
  var vF98 = p471 => p471 < -9007199254740992 || p471 > 9007199254740992 ? NaN : Number(p471);
  function f53(p472) {
    var vF99 = (() => {
      var v182 = new Date(v51[p472 + 20 >> 2] + 1900, v51[p472 + 16 >> 2], v51[p472 + 12 >> 2], v51[p472 + 8 >> 2], v51[p472 + 4 >> 2], v51[p472 >> 2], 0);
      var v183 = v51[p472 + 32 >> 2];
      var v184 = v182.getTimezoneOffset();
      var v185 = new Date(v182.getFullYear(), 0, 1);
      var v186 = new Date(v182.getFullYear(), 6, 1).getTimezoneOffset();
      var v187 = v185.getTimezoneOffset();
      var v188 = Math.min(v187, v186);
      if (v183 < 0) {
        v51[p472 + 32 >> 2] = Number(v186 != v187 && v188 == v184);
      } else if (v183 > 0 != (v188 == v184)) {
        var v189 = Math.max(v187, v186);
        var v190 = v183 > 0 ? v188 : v189;
        v182.setTime(v182.getTime() + (v190 - v184) * 60000);
      }
      v51[p472 + 24 >> 2] = v182.getDay();
      var v191 = vF97(v182) | 0;
      v51[p472 + 28 >> 2] = v191;
      v51[p472 >> 2] = v182.getSeconds();
      v51[p472 + 4 >> 2] = v182.getMinutes();
      v51[p472 + 8 >> 2] = v182.getHours();
      v51[p472 + 12 >> 2] = v182.getDate();
      v51[p472 + 16 >> 2] = v182.getMonth();
      v51[p472 + 20 >> 2] = v182.getYear();
      var v192 = v182.getTime();
      if (isNaN(v192)) {
        return -1;
      } else {
        return v192 / 1000;
      }
    })();
    return BigInt(vF99);
  }
  var vO15 = {};
  var vF100 = () => performance.timeOrigin + performance.now();
  var vF101 = (p473, p474, p475, p476) => {
    var v193 = new Date().getFullYear();
    var v194 = new Date(v193, 0, 1);
    var v195 = new Date(v193, 6, 1);
    var v196 = v194.getTimezoneOffset();
    var v197 = v195.getTimezoneOffset();
    var v198 = Math.max(v196, v197);
    v52[p473 >> 2] = v198 * 60;
    v51[p474 >> 2] = Number(v196 != v197);
    var vF102 = p477 => {
      var v199 = p477 >= 0 ? "-" : "+";
      var v200 = Math.abs(p477);
      return "UTC" + v199 + String(Math.floor(v200 / 60)).padStart(2, "0") + String(v200 % 60).padStart(2, "0");
    };
    var vVF102 = vF102(v196);
    var vVF1022 = vF102(v197);
    if (v197 < v196) {
      vF62(vVF102, p475, 17);
      vF62(vVF1022, p476, 17);
    } else {
      vF62(vVF102, p476, 17);
      vF62(vVF1022, p475, 17);
    }
  };
  var vA23 = [];
  var vF103 = (p478, p479, p480) => ((p481, p482, p483) => {
    var vF104 = ((p484, p485) => {
      var v201;
      for (vA23.length = 0; v201 = v48[p484++];) {
        var v202 = v201 != 105;
        p485 += (v202 &= v201 != 112) && p485 % 8 ? 4 : 0;
        vA23.push(v201 == 112 ? v52[p485 >> 2] : v201 == 106 ? v55[p485 >> 3] : v201 == 105 ? v51[p485 >> 2] : v54[p485 >> 3]);
        p485 += v202 ? 8 : 4;
      }
      return vA23;
    })(p482, p483);
    return vO32[p481](...vF104);
  })(p478, p479, p480);
  var vF105 = () => {};
  var vF106 = () => {
    vF47();
    throw "unwind";
  };
  var vF107 = p486 => {
    v48.length;
    f6("OOM");
  };
  var vF108 = p487 => vO10.handleSleep(p488 => {
    v203 = p488;
    v204 = p487;
    vF47();
    return setTimeout(() => {
      vF48();
      vF46(v203);
    }, v204);
    var v203;
    var v204;
  });
  vF108.isAsync = true;
  var vO16 = {};
  var vF109 = () => {
    if (!vF109.strings) {
      var vO17 = {
        USER: "web_user",
        LOGNAME: "web_user",
        PATH: "/",
        PWD: "/",
        HOME: "/home/web_user",
        LANG: (typeof navigator == "object" && navigator.language || "C").replace("-", "_") + ".UTF-8",
        _: vLSthisprogram || "./this.program"
      };
      for (var v205 in vO16) {
        if (vO16[v205] === undefined) {
          delete vO17[v205];
        } else {
          vO17[v205] = vO16[v205];
        }
      }
      var vA24 = [];
      for (var v205 in vO17) {
        vA24.push(v205 + "=" + vO17[v205]);
      }
      vF109.strings = vA24;
    }
    return vF109.strings;
  };
  var vO18 = {
    isAbs: p489 => p489.charAt(0) === "/",
    splitPath: p490 => /^(\/?|)([\s\S]*?)((?:\.{1,2}|[^\/]+?|)(\.[^.\/]*|))(?:[\/]*)$/.exec(p490).slice(1),
    normalizeArray: (p491, p492) => {
      var vLN024 = 0;
      for (var v206 = p491.length - 1; v206 >= 0; v206--) {
        var v207 = p491[v206];
        if (v207 === ".") {
          p491.splice(v206, 1);
        } else if (v207 === "..") {
          p491.splice(v206, 1);
          vLN024++;
        } else if (vLN024) {
          p491.splice(v206, 1);
          vLN024--;
        }
      }
      if (p492) {
        for (; vLN024; vLN024--) {
          p491.unshift("..");
        }
      }
      return p491;
    },
    normalize: p493 => {
      var v208 = vO18.isAbs(p493);
      var v209 = p493.slice(-1) === "/";
      if (!(p493 = vO18.normalizeArray(p493.split("/").filter(p494 => !!p494), !v208).join("/")) && !v208) {
        p493 = ".";
      }
      if (p493 && v209) {
        p493 += "/";
      }
      return (v208 ? "/" : "") + p493;
    },
    dirname: p495 => {
      var v210 = vO18.splitPath(p495);
      var v211 = v210[0];
      var v212 = v210[1];
      if (v211 || v212) {
        v212 &&= v212.slice(0, -1);
        return v211 + v212;
      } else {
        return ".";
      }
    },
    basename: p496 => p496 && p496.match(/([^\/]+|\/)\/*$/)[1],
    join: (..._0x2ada5e) => vO18.normalize(_0x2ada5e.join("/")),
    join2: (p497, p498) => vO18.normalize(p497 + "/" + p498)
  };
  var vF110 = p499 => {
    (vF110 = p500 => p500.set(crypto.getRandomValues(new Uint8Array(p500.byteLength))))(p499);
  };
  var vO19 = {
    resolve: (..._0x4a2622) => {
      var vLS4 = "";
      for (var v213 = false, v214 = _0x4a2622.length - 1; v214 >= -1 && !v213; v214--) {
        var v215 = v214 >= 0 ? _0x4a2622[v214] : vO23.cwd();
        if (typeof v215 != "string") {
          throw new TypeError("Arguments to path.resolve must be strings");
        }
        if (!v215) {
          return "";
        }
        vLS4 = v215 + "/" + vLS4;
        v213 = vO18.isAbs(v215);
      }
      return (v213 ? "/" : "") + (vLS4 = vO18.normalizeArray(vLS4.split("/").filter(p501 => !!p501), !v213).join("/")) || ".";
    },
    relative: (p502, p503) => {
      function f54(p504) {
        for (var vLN025 = 0; vLN025 < p504.length && p504[vLN025] === ""; vLN025++);
        for (var v216 = p504.length - 1; v216 >= 0 && p504[v216] === ""; v216--);
        if (vLN025 > v216) {
          return [];
        } else {
          return p504.slice(vLN025, v216 - vLN025 + 1);
        }
      }
      p502 = vO19.resolve(p502).slice(1);
      p503 = vO19.resolve(p503).slice(1);
      var vF542 = f54(p502.split("/"));
      var vF543 = f54(p503.split("/"));
      for (var v217 = Math.min(vF542.length, vF543.length), vV217 = v217, vLN026 = 0; vLN026 < v217; vLN026++) {
        if (vF542[vLN026] !== vF543[vLN026]) {
          vV217 = vLN026;
          break;
        }
      }
      var vA25 = [];
      for (vLN026 = vV217; vLN026 < vF542.length; vLN026++) {
        vA25.push("..");
      }
      return (vA25 = vA25.concat(vF543.slice(vV217))).join("/");
    }
  };
  var vF111 = (p505, p506 = 0, p507, p508) => {
    var vVF183 = vF18(p505, p506, p507, p508);
    return v86.decode(p505.buffer ? p505.slice(p506, vVF183) : new Uint8Array(p505.slice(p506, vVF183)));
  };
  var vA26 = [];
  var vF112 = (p509, p510, p511) => {
    var v218 = vF63(p509) + 1;
    var v219 = new Array(v218);
    var vVF61 = vF61(p509, v219, 0, v219.length);
    v219.length = vVF61;
    return v219;
  };
  var vO20 = {
    ttys: [],
    init() {},
    shutdown() {},
    register(p512, p513) {
      vO20.ttys[p512] = {
        input: [],
        output: [],
        ops: p513
      };
      vO23.registerDevice(p512, vO20.stream_ops);
    },
    stream_ops: {
      open(p514) {
        var v220 = vO20.ttys[p514.node.rdev];
        if (!v220) {
          throw new vO23.ErrnoError(43);
        }
        p514.tty = v220;
        p514.seekable = false;
      },
      close(p515) {
        p515.tty.ops.fsync(p515.tty);
      },
      fsync(p516) {
        p516.tty.ops.fsync(p516.tty);
      },
      read(p517, p518, p519, p520, p521) {
        if (!p517.tty || !p517.tty.ops.get_char) {
          throw new vO23.ErrnoError(60);
        }
        var vLN027 = 0;
        for (var vLN028 = 0; vLN028 < p520; vLN028++) {
          var v221;
          try {
            v221 = p517.tty.ops.get_char(p517.tty);
          } catch (e41) {
            throw new vO23.ErrnoError(29);
          }
          if (v221 === undefined && vLN027 === 0) {
            throw new vO23.ErrnoError(6);
          }
          if (v221 == null) {
            break;
          }
          vLN027++;
          p518[p519 + vLN028] = v221;
        }
        if (vLN027) {
          p517.node.atime = Date.now();
        }
        return vLN027;
      },
      write(p522, p523, p524, p525, p526) {
        if (!p522.tty || !p522.tty.ops.put_char) {
          throw new vO23.ErrnoError(60);
        }
        try {
          for (var vLN029 = 0; vLN029 < p525; vLN029++) {
            p522.tty.ops.put_char(p522.tty, p523[p524 + vLN029]);
          }
        } catch (e42) {
          throw new vO23.ErrnoError(29);
        }
        if (p525) {
          p522.node.mtime = p522.node.ctime = Date.now();
        }
        return vLN029;
      }
    },
    default_tty_ops: {
      get_char: p527 => (() => {
        if (!vA26.length) {
          var v222 = null;
          if (typeof window != "undefined" && typeof window.prompt == "function" && (v222 = window.prompt("Input: ")) !== null) {
            v222 += "\n";
          }
          if (!v222) {
            return null;
          }
          vA26 = vF112(v222);
        }
        return vA26.shift();
      })(),
      put_char(p528, p529) {
        if (p529 === null || p529 === 10) {
          v57(vF111(p528.output));
          p528.output = [];
        } else if (p529 != 0) {
          p528.output.push(p529);
        }
      },
      fsync(p530) {
        if (p530.output?.length > 0) {
          v57(vF111(p530.output));
          p530.output = [];
        }
      },
      ioctl_tcgets: p531 => ({
        c_iflag: 25856,
        c_oflag: 5,
        c_cflag: 191,
        c_lflag: 35387,
        c_cc: [3, 28, 127, 21, 4, 0, 1, 0, 17, 19, 26, 0, 18, 15, 23, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      }),
      ioctl_tcsets: (p532, p533, p534) => 0,
      ioctl_tiocgwinsz: p535 => [24, 80]
    },
    default_tty1_ops: {
      put_char(p536, p537) {
        if (p537 === null || p537 === 10) {
          v58(vF111(p536.output));
          p536.output = [];
        } else if (p537 != 0) {
          p536.output.push(p537);
        }
      },
      fsync(p538) {
        if (p538.output?.length > 0) {
          v58(vF111(p538.output));
          p538.output = [];
        }
      }
    }
  };
  var vF113 = p539 => {
    f6();
  };
  var vO21 = {
    ops_table: null,
    mount: p540 => vO21.createNode(null, "/", 16895, 0),
    createNode(p541, p542, p543, p544) {
      if (vO23.isBlkdev(p543) || vO23.isFIFO(p543)) {
        throw new vO23.ErrnoError(63);
      }
      vO21.ops_table ||= {
        dir: {
          node: {
            getattr: vO21.node_ops.getattr,
            setattr: vO21.node_ops.setattr,
            lookup: vO21.node_ops.lookup,
            mknod: vO21.node_ops.mknod,
            rename: vO21.node_ops.rename,
            unlink: vO21.node_ops.unlink,
            rmdir: vO21.node_ops.rmdir,
            readdir: vO21.node_ops.readdir,
            symlink: vO21.node_ops.symlink
          },
          stream: {
            llseek: vO21.stream_ops.llseek
          }
        },
        file: {
          node: {
            getattr: vO21.node_ops.getattr,
            setattr: vO21.node_ops.setattr
          },
          stream: {
            llseek: vO21.stream_ops.llseek,
            read: vO21.stream_ops.read,
            write: vO21.stream_ops.write,
            mmap: vO21.stream_ops.mmap,
            msync: vO21.stream_ops.msync
          }
        },
        link: {
          node: {
            getattr: vO21.node_ops.getattr,
            setattr: vO21.node_ops.setattr,
            readlink: vO21.node_ops.readlink
          },
          stream: {}
        },
        chrdev: {
          node: {
            getattr: vO21.node_ops.getattr,
            setattr: vO21.node_ops.setattr
          },
          stream: vO23.chrdev_stream_ops
        }
      };
      var v223 = vO23.createNode(p541, p542, p543, p544);
      if (vO23.isDir(v223.mode)) {
        v223.node_ops = vO21.ops_table.dir.node;
        v223.stream_ops = vO21.ops_table.dir.stream;
        v223.contents = {};
      } else if (vO23.isFile(v223.mode)) {
        v223.node_ops = vO21.ops_table.file.node;
        v223.stream_ops = vO21.ops_table.file.stream;
        v223.usedBytes = 0;
        v223.contents = null;
      } else if (vO23.isLink(v223.mode)) {
        v223.node_ops = vO21.ops_table.link.node;
        v223.stream_ops = vO21.ops_table.link.stream;
      } else if (vO23.isChrdev(v223.mode)) {
        v223.node_ops = vO21.ops_table.chrdev.node;
        v223.stream_ops = vO21.ops_table.chrdev.stream;
      }
      v223.atime = v223.mtime = v223.ctime = Date.now();
      if (p541) {
        p541.contents[p542] = v223;
        p541.atime = p541.mtime = p541.ctime = v223.atime;
      }
      return v223;
    },
    getFileDataAsTypedArray: p545 => p545.contents ? p545.contents.subarray ? p545.contents.subarray(0, p545.usedBytes) : new Uint8Array(p545.contents) : new Uint8Array(0),
    expandFileStorage(p546, p547) {
      var v224 = p546.contents ? p546.contents.length : 0;
      if (!(v224 >= p547)) {
        p547 = Math.max(p547, v224 * (v224 < 1048576 ? 2 : 1.125) >>> 0);
        if (v224 != 0) {
          p547 = Math.max(p547, 256);
        }
        var v225 = p546.contents;
        p546.contents = new Uint8Array(p547);
        if (p546.usedBytes > 0) {
          p546.contents.set(v225.subarray(0, p546.usedBytes), 0);
        }
      }
    },
    resizeFileStorage(p548, p549) {
      if (p548.usedBytes != p549) {
        if (p549 == 0) {
          p548.contents = null;
          p548.usedBytes = 0;
        } else {
          var v226 = p548.contents;
          p548.contents = new Uint8Array(p549);
          if (v226) {
            p548.contents.set(v226.subarray(0, Math.min(p549, p548.usedBytes)));
          }
          p548.usedBytes = p549;
        }
      }
    },
    node_ops: {
      getattr(p550) {
        var vO22 = {};
        vO22.dev = vO23.isChrdev(p550.mode) ? p550.id : 1;
        vO22.ino = p550.id;
        vO22.mode = p550.mode;
        vO22.nlink = 1;
        vO22.uid = 0;
        vO22.gid = 0;
        vO22.rdev = p550.rdev;
        if (vO23.isDir(p550.mode)) {
          vO22.size = 4096;
        } else if (vO23.isFile(p550.mode)) {
          vO22.size = p550.usedBytes;
        } else if (vO23.isLink(p550.mode)) {
          vO22.size = p550.link.length;
        } else {
          vO22.size = 0;
        }
        vO22.atime = new Date(p550.atime);
        vO22.mtime = new Date(p550.mtime);
        vO22.ctime = new Date(p550.ctime);
        vO22.blksize = 4096;
        vO22.blocks = Math.ceil(vO22.size / vO22.blksize);
        return vO22;
      },
      setattr(p551, p552) {
        for (const v227 of ["mode", "atime", "mtime", "ctime"]) {
          if (p552[v227] != null) {
            p551[v227] = p552[v227];
          }
        }
        if (p552.size !== undefined) {
          vO21.resizeFileStorage(p551, p552.size);
        }
      },
      lookup(p553, p554) {
        if (!vO21.doesNotExistError) {
          vO21.doesNotExistError = new vO23.ErrnoError(44);
          vO21.doesNotExistError.stack = "<generic error, no stack>";
        }
        throw vO21.doesNotExistError;
      },
      mknod: (p555, p556, p557, p558) => vO21.createNode(p555, p556, p557, p558),
      rename(p559, p560, p561) {
        var v228;
        try {
          v228 = vO23.lookupNode(p560, p561);
        } catch (e43) {}
        if (v228) {
          if (vO23.isDir(p559.mode)) {
            for (var v229 in v228.contents) {
              throw new vO23.ErrnoError(55);
            }
          }
          vO23.hashRemoveNode(v228);
        }
        delete p559.parent.contents[p559.name];
        p560.contents[p561] = p559;
        p559.name = p561;
        p560.ctime = p560.mtime = p559.parent.ctime = p559.parent.mtime = Date.now();
      },
      unlink(p562, p563) {
        delete p562.contents[p563];
        p562.ctime = p562.mtime = Date.now();
      },
      rmdir(p564, p565) {
        var v230 = vO23.lookupNode(p564, p565);
        for (var v231 in v230.contents) {
          throw new vO23.ErrnoError(55);
        }
        delete p564.contents[p565];
        p564.ctime = p564.mtime = Date.now();
      },
      readdir: p566 => [".", "..", ...Object.keys(p566.contents)],
      symlink(p567, p568, p569) {
        var v232 = vO21.createNode(p567, p568, 41471, 0);
        v232.link = p569;
        return v232;
      },
      readlink(p570) {
        if (!vO23.isLink(p570.mode)) {
          throw new vO23.ErrnoError(28);
        }
        return p570.link;
      }
    },
    stream_ops: {
      read(p571, p572, p573, p574, p575) {
        var v233 = p571.node.contents;
        if (p575 >= p571.node.usedBytes) {
          return 0;
        }
        var v234 = Math.min(p571.node.usedBytes - p575, p574);
        if (v234 > 8 && v233.subarray) {
          p572.set(v233.subarray(p575, p575 + v234), p573);
        } else {
          for (var vLN030 = 0; vLN030 < v234; vLN030++) {
            p572[p573 + vLN030] = v233[p575 + vLN030];
          }
        }
        return v234;
      },
      write(p576, p577, p578, p579, p580, p581) {
        if (!p579) {
          return 0;
        }
        var v235 = p576.node;
        v235.mtime = v235.ctime = Date.now();
        if (p577.subarray && (!v235.contents || v235.contents.subarray)) {
          if (p581) {
            v235.contents = p577.subarray(p578, p578 + p579);
            v235.usedBytes = p579;
            return p579;
          }
          if (v235.usedBytes === 0 && p580 === 0) {
            v235.contents = p577.slice(p578, p578 + p579);
            v235.usedBytes = p579;
            return p579;
          }
          if (p580 + p579 <= v235.usedBytes) {
            v235.contents.set(p577.subarray(p578, p578 + p579), p580);
            return p579;
          }
        }
        vO21.expandFileStorage(v235, p580 + p579);
        if (v235.contents.subarray && p577.subarray) {
          v235.contents.set(p577.subarray(p578, p578 + p579), p580);
        } else {
          for (var vLN031 = 0; vLN031 < p579; vLN031++) {
            v235.contents[p580 + vLN031] = p577[p578 + vLN031];
          }
        }
        v235.usedBytes = Math.max(v235.usedBytes, p580 + p579);
        return p579;
      },
      llseek(p582, p583, p584) {
        var vP583 = p583;
        if (p584 === 1) {
          vP583 += p582.position;
        } else if (p584 === 2 && vO23.isFile(p582.node.mode)) {
          vP583 += p582.node.usedBytes;
        }
        if (vP583 < 0) {
          throw new vO23.ErrnoError(28);
        }
        return vP583;
      },
      mmap(p585, p586, p587, p588, p589) {
        if (!vO23.isFile(p585.node.mode)) {
          throw new vO23.ErrnoError(43);
        }
        var v236;
        var v237;
        var v238 = p585.node.contents;
        if (p589 & 2 || !v238 || v238.buffer !== v47.buffer) {
          v237 = true;
          if (!(v236 = vF113())) {
            throw new vO23.ErrnoError(48);
          }
          if (v238) {
            if (p587 > 0 || p587 + p586 < v238.length) {
              v238 = v238.subarray ? v238.subarray(p587, p587 + p586) : Array.prototype.slice.call(v238, p587, p587 + p586);
            }
            v47.set(v238, v236);
          }
        } else {
          v237 = false;
          v236 = v238.byteOffset;
        }
        return {
          ptr: v236,
          allocated: v237
        };
      },
      msync: (p590, p591, p592, p593, p594) => {
        vO21.stream_ops.write(p590, p591, 0, p593, p592, false);
        return 0;
      }
    }
  };
  var vF114 = (p595, p596) => {
    var vLN032 = 0;
    if (p595) {
      vLN032 |= 365;
    }
    if (p596) {
      vLN032 |= 146;
    }
    return vLN032;
  };
  var vA27 = [];
  var vF115 = async (p597, p598, p599, p600, p601, p602, p603, p604) => {
    var v239 = p598 ? vO19.resolve(vO18.join2(p597, p598)) : p597;
    f4();
    try {
      var vP599 = p599;
      if (typeof p599 == "string") {
        vP599 = await (async p605 => {
          var v240 = await v33(p605);
          return new Uint8Array(v240);
        })(p599);
      }
      vP599 = await (async (p606, p607) => {
        if (typeof Browser != "undefined") {
          Browser.init();
        }
        for (var v241 of vA27) {
          if (v241.canHandle(p607)) {
            return v241.handle(p606, p607);
          }
        }
        return p606;
      })(vP599, v239);
      p604?.();
      if (!p602) {
        ((..._0x215d63) => {
          vO23.createDataFile(..._0x215d63);
        })(p597, p598, vP599, p600, p601, p603);
      }
    } finally {
      f5();
    }
  };
  var vO23 = {
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
      constructor(p608) {
        this.errno = p608;
      }
    },
    FSStream: class {
      shared = {};
      get object() {
        return this.node;
      }
      set object(p609) {
        this.node = p609;
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
      set flags(p610) {
        this.shared.flags = p610;
      }
      get position() {
        return this.shared.position;
      }
      set position(p611) {
        this.shared.position = p611;
      }
    },
    FSNode: class {
      node_ops = {};
      stream_ops = {};
      readMode = 365;
      writeMode = 146;
      mounted = null;
      constructor(p612, p613, p614, p615) {
        p612 ||= this;
        this.parent = p612;
        this.mount = p612.mount;
        this.id = vO23.nextInode++;
        this.name = p613;
        this.mode = p614;
        this.rdev = p615;
        this.atime = this.mtime = this.ctime = Date.now();
      }
      get read() {
        return (this.mode & this.readMode) === this.readMode;
      }
      set read(p616) {
        if (p616) {
          this.mode |= this.readMode;
        } else {
          this.mode &= ~this.readMode;
        }
      }
      get write() {
        return (this.mode & this.writeMode) === this.writeMode;
      }
      set write(p617) {
        if (p617) {
          this.mode |= this.writeMode;
        } else {
          this.mode &= ~this.writeMode;
        }
      }
      get isFolder() {
        return vO23.isDir(this.mode);
      }
      get isDevice() {
        return vO23.isChrdev(this.mode);
      }
    },
    lookupPath(p618, p619 = {}) {
      if (!p618) {
        throw new vO23.ErrnoError(44);
      }
      p619.follow_mount ??= true;
      if (!vO18.isAbs(p618)) {
        p618 = vO23.cwd() + "/" + p618;
      }
      _0x408d39: for (var vLN033 = 0; vLN033 < 40; vLN033++) {
        for (var v242 = p618.split("/").filter(p620 => !!p620), v243 = vO23.root, vLS5 = "/", vLN034 = 0; vLN034 < v242.length; vLN034++) {
          var v244 = vLN034 === v242.length - 1;
          if (v244 && p619.parent) {
            break;
          }
          if (v242[vLN034] !== ".") {
            if (v242[vLN034] !== "..") {
              vLS5 = vO18.join2(vLS5, v242[vLN034]);
              try {
                v243 = vO23.lookupNode(v243, v242[vLN034]);
              } catch (e44) {
                if (e44?.errno === 44 && v244 && p619.noent_okay) {
                  return {
                    path: vLS5
                  };
                }
                throw e44;
              }
              if (!!vO23.isMountpoint(v243) && (!v244 || !!p619.follow_mount)) {
                v243 = v243.mounted.root;
              }
              if (vO23.isLink(v243.mode) && (!v244 || p619.follow)) {
                if (!v243.node_ops.readlink) {
                  throw new vO23.ErrnoError(52);
                }
                var v245 = v243.node_ops.readlink(v243);
                if (!vO18.isAbs(v245)) {
                  v245 = vO18.dirname(vLS5) + "/" + v245;
                }
                p618 = v245 + "/" + v242.slice(vLN034 + 1).join("/");
                continue _0x408d39;
              }
            } else {
              vLS5 = vO18.dirname(vLS5);
              if (vO23.isRoot(v243)) {
                p618 = vLS5 + "/" + v242.slice(vLN034 + 1).join("/");
                vLN033--;
                continue _0x408d39;
              }
              v243 = v243.parent;
            }
          }
        }
        return {
          path: vLS5,
          node: v243
        };
      }
      throw new vO23.ErrnoError(32);
    },
    getPath(p621) {
      var v246;
      for (;;) {
        if (vO23.isRoot(p621)) {
          var v247 = p621.mount.mountpoint;
          if (v246) {
            if (v247[v247.length - 1] !== "/") {
              return v247 + "/" + v246;
            } else {
              return v247 + v246;
            }
          } else {
            return v247;
          }
        }
        v246 = v246 ? p621.name + "/" + v246 : p621.name;
        p621 = p621.parent;
      }
    },
    hashName(p622, p623) {
      var vLN035 = 0;
      for (var vLN036 = 0; vLN036 < p623.length; vLN036++) {
        vLN035 = (vLN035 << 5) - vLN035 + p623.charCodeAt(vLN036) | 0;
      }
      return (p622 + vLN035 >>> 0) % vO23.nameTable.length;
    },
    hashAddNode(p624) {
      var v248 = vO23.hashName(p624.parent.id, p624.name);
      p624.name_next = vO23.nameTable[v248];
      vO23.nameTable[v248] = p624;
    },
    hashRemoveNode(p625) {
      var v249 = vO23.hashName(p625.parent.id, p625.name);
      if (vO23.nameTable[v249] === p625) {
        vO23.nameTable[v249] = p625.name_next;
      } else {
        for (var v250 = vO23.nameTable[v249]; v250;) {
          if (v250.name_next === p625) {
            v250.name_next = p625.name_next;
            break;
          }
          v250 = v250.name_next;
        }
      }
    },
    lookupNode(p626, p627) {
      var v251 = vO23.mayLookup(p626);
      if (v251) {
        throw new vO23.ErrnoError(v251);
      }
      var v252 = vO23.hashName(p626.id, p627);
      for (var v253 = vO23.nameTable[v252]; v253; v253 = v253.name_next) {
        var v254 = v253.name;
        if (v253.parent.id === p626.id && v254 === p627) {
          return v253;
        }
      }
      return vO23.lookup(p626, p627);
    },
    createNode(p628, p629, p630, p631) {
      var v255 = new vO23.FSNode(p628, p629, p630, p631);
      vO23.hashAddNode(v255);
      return v255;
    },
    destroyNode(p632) {
      vO23.hashRemoveNode(p632);
    },
    isRoot: p633 => p633 === p633.parent,
    isMountpoint: p634 => !!p634.mounted,
    isFile: p635 => (p635 & 61440) == 32768,
    isDir: p636 => (p636 & 61440) == 16384,
    isLink: p637 => (p637 & 61440) == 40960,
    isChrdev: p638 => (p638 & 61440) == 8192,
    isBlkdev: p639 => (p639 & 61440) == 24576,
    isFIFO: p640 => (p640 & 61440) == 4096,
    isSocket: p641 => !(~p641 & 49152),
    flagsToPermissionString(p642) {
      var v256 = ["r", "w", "rw"][p642 & 3];
      if (p642 & 512) {
        v256 += "w";
      }
      return v256;
    },
    nodePermissions: (p643, p644) => vO23.ignorePermissions || (!p644.includes("r") || p643.mode & 292) && (!p644.includes("w") || p643.mode & 146) && (!p644.includes("x") || p643.mode & 73) ? 0 : 2,
    mayLookup: p645 => vO23.isDir(p645.mode) ? vO23.nodePermissions(p645, "x") || (p645.node_ops.lookup ? 0 : 2) : 54,
    mayCreate(p646, p647) {
      if (!vO23.isDir(p646.mode)) {
        return 54;
      }
      try {
        vO23.lookupNode(p646, p647);
        return 20;
      } catch (e45) {}
      return vO23.nodePermissions(p646, "wx");
    },
    mayDelete(p648, p649, p650) {
      var v257;
      try {
        v257 = vO23.lookupNode(p648, p649);
      } catch (e46) {
        return e46.errno;
      }
      var v258 = vO23.nodePermissions(p648, "wx");
      if (v258) {
        return v258;
      }
      if (p650) {
        if (!vO23.isDir(v257.mode)) {
          return 54;
        }
        if (vO23.isRoot(v257) || vO23.getPath(v257) === vO23.cwd()) {
          return 10;
        }
      } else if (vO23.isDir(v257.mode)) {
        return 31;
      }
      return 0;
    },
    mayOpen: (p651, p652) => p651 ? vO23.isLink(p651.mode) ? 32 : vO23.isDir(p651.mode) && (vO23.flagsToPermissionString(p652) !== "r" || p652 & 576) ? 31 : vO23.nodePermissions(p651, vO23.flagsToPermissionString(p652)) : 44,
    checkOpExists(p653, p654) {
      if (!p653) {
        throw new vO23.ErrnoError(p654);
      }
      return p653;
    },
    MAX_OPEN_FDS: 4096,
    nextfd() {
      for (var vLN037 = 0; vLN037 <= vO23.MAX_OPEN_FDS; vLN037++) {
        if (!vO23.streams[vLN037]) {
          return vLN037;
        }
      }
      throw new vO23.ErrnoError(33);
    },
    getStreamChecked(p655) {
      var v259 = vO23.getStream(p655);
      if (!v259) {
        throw new vO23.ErrnoError(8);
      }
      return v259;
    },
    getStream: p656 => vO23.streams[p656],
    createStream: (p657, p658 = -1) => {
      p657 = Object.assign(new vO23.FSStream(), p657);
      if (p658 == -1) {
        p658 = vO23.nextfd();
      }
      p657.fd = p658;
      vO23.streams[p658] = p657;
      return p657;
    },
    closeStream(p659) {
      vO23.streams[p659] = null;
    },
    dupStream(p660, p661 = -1) {
      var v260 = vO23.createStream(p660, p661);
      v260.stream_ops?.dup?.(v260);
      return v260;
    },
    doSetAttr(p662, p663, p664) {
      var v261 = p662?.stream_ops.setattr;
      var v262 = v261 ? p662 : p663;
      v261 ??= p663.node_ops.setattr;
      vO23.checkOpExists(v261, 63);
      v261(v262, p664);
    },
    chrdev_stream_ops: {
      open(p665) {
        var v263 = vO23.getDevice(p665.node.rdev);
        p665.stream_ops = v263.stream_ops;
        p665.stream_ops.open?.(p665);
      },
      llseek() {
        throw new vO23.ErrnoError(70);
      }
    },
    major: p666 => p666 >> 8,
    minor: p667 => p667 & 255,
    makedev: (p668, p669) => p668 << 8 | p669,
    registerDevice(p670, p671) {
      vO23.devices[p670] = {
        stream_ops: p671
      };
    },
    getDevice: p672 => vO23.devices[p672],
    getMounts(p673) {
      var vA28 = [];
      for (var vA29 = [p673]; vA29.length;) {
        var v264 = vA29.pop();
        vA28.push(v264);
        vA29.push(...v264.mounts);
      }
      return vA28;
    },
    syncfs(p674, p675) {
      function f55(p676) {
        vO23.syncFSRequests--;
        return p675(p676);
      }
      function f56(p677) {
        if (p677) {
          if (f56.errored) {
            return undefined;
          } else {
            f56.errored = true;
            return f55(p677);
          }
        }
        if (++vLN038 >= v265.length) {
          f55(null);
        }
      }
      if (typeof p674 == "function") {
        p675 = p674;
        p674 = false;
      }
      vO23.syncFSRequests++;
      if (vO23.syncFSRequests > 1) {
        v58("warning: " + vO23.syncFSRequests + " FS.syncfs operations in flight at once, probably just doing extra work");
      }
      var v265 = vO23.getMounts(vO23.root.mount);
      var vLN038 = 0;
      v265.forEach(p678 => {
        if (!p678.type.syncfs) {
          return f56(null);
        }
        p678.type.syncfs(p678, p674, f56);
      });
    },
    mount(p679, p680, p681) {
      var v266;
      var v267 = p681 === "/";
      var v268 = !p681;
      if (v267 && vO23.root) {
        throw new vO23.ErrnoError(10);
      }
      if (!v267 && !v268) {
        var v269 = vO23.lookupPath(p681, {
          follow_mount: false
        });
        p681 = v269.path;
        v266 = v269.node;
        if (vO23.isMountpoint(v266)) {
          throw new vO23.ErrnoError(10);
        }
        if (!vO23.isDir(v266.mode)) {
          throw new vO23.ErrnoError(54);
        }
      }
      var vO24 = {
        type: p679,
        opts: p680,
        mountpoint: p681,
        mounts: []
      };
      var v270 = p679.mount(vO24);
      v270.mount = vO24;
      vO24.root = v270;
      if (v267) {
        vO23.root = v270;
      } else if (v266) {
        v266.mounted = vO24;
        if (v266.mount) {
          v266.mount.mounts.push(vO24);
        }
      }
      return v270;
    },
    unmount(p682) {
      var v271 = vO23.lookupPath(p682, {
        follow_mount: false
      });
      if (!vO23.isMountpoint(v271.node)) {
        throw new vO23.ErrnoError(28);
      }
      var v272 = v271.node;
      var v273 = v272.mounted;
      var v274 = vO23.getMounts(v273);
      Object.keys(vO23.nameTable).forEach(p683 => {
        for (var v275 = vO23.nameTable[p683]; v275;) {
          var v276 = v275.name_next;
          if (v274.includes(v275.mount)) {
            vO23.destroyNode(v275);
          }
          v275 = v276;
        }
      });
      v272.mounted = null;
      var v277 = v272.mount.mounts.indexOf(v273);
      v272.mount.mounts.splice(v277, 1);
    },
    lookup: (p684, p685) => p684.node_ops.lookup(p684, p685),
    mknod(p686, p687, p688) {
      var v278 = vO23.lookupPath(p686, {
        parent: true
      }).node;
      var v279 = vO18.basename(p686);
      if (!v279) {
        throw new vO23.ErrnoError(28);
      }
      if (v279 === "." || v279 === "..") {
        throw new vO23.ErrnoError(20);
      }
      var v280 = vO23.mayCreate(v278, v279);
      if (v280) {
        throw new vO23.ErrnoError(v280);
      }
      if (!v278.node_ops.mknod) {
        throw new vO23.ErrnoError(63);
      }
      return v278.node_ops.mknod(v278, v279, p687, p688);
    },
    statfs: p689 => vO23.statfsNode(vO23.lookupPath(p689, {
      follow: true
    }).node),
    statfsStream: p690 => vO23.statfsNode(p690.node),
    statfsNode(p691) {
      var vO25 = {
        bsize: 4096,
        frsize: 4096,
        blocks: 1000000,
        bfree: 500000,
        bavail: 500000,
        files: vO23.nextInode,
        ffree: vO23.nextInode - 1,
        fsid: 42,
        flags: 2,
        namelen: 255
      };
      if (p691.node_ops.statfs) {
        Object.assign(vO25, p691.node_ops.statfs(p691.mount.opts.root));
      }
      return vO25;
    },
    create: (p692, p693 = 438) => {
      p693 &= 4095;
      p693 |= 32768;
      return vO23.mknod(p692, p693, 0);
    },
    mkdir: (p694, p695 = 511) => {
      p695 &= 1023;
      p695 |= 16384;
      return vO23.mknod(p694, p695, 0);
    },
    mkdirTree(p696, p697) {
      var v281 = p696.split("/");
      var vLS6 = "";
      for (var v282 of v281) {
        if (v282) {
          if (vLS6 || vO18.isAbs(p696)) {
            vLS6 += "/";
          }
          vLS6 += v282;
          try {
            vO23.mkdir(vLS6, p697);
          } catch (e47) {
            if (e47.errno != 20) {
              throw e47;
            }
          }
        }
      }
    },
    mkdev: (p698, p699, p700) => {
      if (p700 === undefined) {
        p700 = p699;
        p699 = 438;
      }
      p699 |= 8192;
      return vO23.mknod(p698, p699, p700);
    },
    symlink(p701, p702) {
      if (!vO19.resolve(p701)) {
        throw new vO23.ErrnoError(44);
      }
      var v283 = vO23.lookupPath(p702, {
        parent: true
      }).node;
      if (!v283) {
        throw new vO23.ErrnoError(44);
      }
      var v284 = vO18.basename(p702);
      var v285 = vO23.mayCreate(v283, v284);
      if (v285) {
        throw new vO23.ErrnoError(v285);
      }
      if (!v283.node_ops.symlink) {
        throw new vO23.ErrnoError(63);
      }
      return v283.node_ops.symlink(v283, v284, p701);
    },
    rename(p703, p704) {
      var v286;
      var v287;
      var v288 = vO18.dirname(p703);
      var v289 = vO18.dirname(p704);
      var v290 = vO18.basename(p703);
      var v291 = vO18.basename(p704);
      v286 = vO23.lookupPath(p703, {
        parent: true
      }).node;
      v287 = vO23.lookupPath(p704, {
        parent: true
      }).node;
      if (!v286 || !v287) {
        throw new vO23.ErrnoError(44);
      }
      if (v286.mount !== v287.mount) {
        throw new vO23.ErrnoError(75);
      }
      var v292;
      var v293 = vO23.lookupNode(v286, v290);
      var v294 = vO19.relative(p703, v289);
      if (v294.charAt(0) !== ".") {
        throw new vO23.ErrnoError(28);
      }
      if ((v294 = vO19.relative(p704, v288)).charAt(0) !== ".") {
        throw new vO23.ErrnoError(55);
      }
      try {
        v292 = vO23.lookupNode(v287, v291);
      } catch (e48) {}
      if (v293 !== v292) {
        var v295 = vO23.isDir(v293.mode);
        var v296 = vO23.mayDelete(v286, v290, v295);
        if (v296) {
          throw new vO23.ErrnoError(v296);
        }
        if (v296 = v292 ? vO23.mayDelete(v287, v291, v295) : vO23.mayCreate(v287, v291)) {
          throw new vO23.ErrnoError(v296);
        }
        if (!v286.node_ops.rename) {
          throw new vO23.ErrnoError(63);
        }
        if (vO23.isMountpoint(v293) || v292 && vO23.isMountpoint(v292)) {
          throw new vO23.ErrnoError(10);
        }
        if (v287 !== v286 && (v296 = vO23.nodePermissions(v286, "w"))) {
          throw new vO23.ErrnoError(v296);
        }
        vO23.hashRemoveNode(v293);
        try {
          v286.node_ops.rename(v293, v287, v291);
          v293.parent = v287;
        } catch (e49) {
          throw e49;
        } finally {
          vO23.hashAddNode(v293);
        }
      }
    },
    rmdir(p705) {
      var v297 = vO23.lookupPath(p705, {
        parent: true
      }).node;
      var v298 = vO18.basename(p705);
      var v299 = vO23.lookupNode(v297, v298);
      var v300 = vO23.mayDelete(v297, v298, true);
      if (v300) {
        throw new vO23.ErrnoError(v300);
      }
      if (!v297.node_ops.rmdir) {
        throw new vO23.ErrnoError(63);
      }
      if (vO23.isMountpoint(v299)) {
        throw new vO23.ErrnoError(10);
      }
      v297.node_ops.rmdir(v297, v298);
      vO23.destroyNode(v299);
    },
    readdir(p706) {
      var v301 = vO23.lookupPath(p706, {
        follow: true
      }).node;
      return vO23.checkOpExists(v301.node_ops.readdir, 54)(v301);
    },
    unlink(p707) {
      var v302 = vO23.lookupPath(p707, {
        parent: true
      }).node;
      if (!v302) {
        throw new vO23.ErrnoError(44);
      }
      var v303 = vO18.basename(p707);
      var v304 = vO23.lookupNode(v302, v303);
      var v305 = vO23.mayDelete(v302, v303, false);
      if (v305) {
        throw new vO23.ErrnoError(v305);
      }
      if (!v302.node_ops.unlink) {
        throw new vO23.ErrnoError(63);
      }
      if (vO23.isMountpoint(v304)) {
        throw new vO23.ErrnoError(10);
      }
      v302.node_ops.unlink(v302, v303);
      vO23.destroyNode(v304);
    },
    readlink(p708) {
      var v306 = vO23.lookupPath(p708).node;
      if (!v306) {
        throw new vO23.ErrnoError(44);
      }
      if (!v306.node_ops.readlink) {
        throw new vO23.ErrnoError(28);
      }
      return v306.node_ops.readlink(v306);
    },
    stat(p709, p710) {
      var v307 = vO23.lookupPath(p709, {
        follow: !p710
      }).node;
      return vO23.checkOpExists(v307.node_ops.getattr, 63)(v307);
    },
    fstat(p711) {
      var v308 = vO23.getStreamChecked(p711);
      var v309 = v308.node;
      var v310 = v308.stream_ops.getattr;
      var v311 = v310 ? v308 : v309;
      v310 ??= v309.node_ops.getattr;
      vO23.checkOpExists(v310, 63);
      return v310(v311);
    },
    lstat: p712 => vO23.stat(p712, true),
    doChmod(p713, p714, p715, p716) {
      vO23.doSetAttr(p713, p714, {
        mode: p715 & 4095 | p714.mode & -4096,
        ctime: Date.now(),
        dontFollow: p716
      });
    },
    chmod(p717, p718, p719) {
      var v312;
      v312 = typeof p717 == "string" ? vO23.lookupPath(p717, {
        follow: !p719
      }).node : p717;
      vO23.doChmod(null, v312, p718, p719);
    },
    lchmod(p720, p721) {
      vO23.chmod(p720, p721, true);
    },
    fchmod(p722, p723) {
      var v313 = vO23.getStreamChecked(p722);
      vO23.doChmod(v313, v313.node, p723, false);
    },
    doChown(p724, p725, p726) {
      vO23.doSetAttr(p724, p725, {
        timestamp: Date.now(),
        dontFollow: p726
      });
    },
    chown(p727, p728, p729, p730) {
      var v314;
      v314 = typeof p727 == "string" ? vO23.lookupPath(p727, {
        follow: !p730
      }).node : p727;
      vO23.doChown(null, v314, p730);
    },
    lchown(p731, p732, p733) {
      vO23.chown(p731, p732, p733, true);
    },
    fchown(p734, p735, p736) {
      var v315 = vO23.getStreamChecked(p734);
      vO23.doChown(v315, v315.node, false);
    },
    doTruncate(p737, p738, p739) {
      if (vO23.isDir(p738.mode)) {
        throw new vO23.ErrnoError(31);
      }
      if (!vO23.isFile(p738.mode)) {
        throw new vO23.ErrnoError(28);
      }
      var v316 = vO23.nodePermissions(p738, "w");
      if (v316) {
        throw new vO23.ErrnoError(v316);
      }
      vO23.doSetAttr(p737, p738, {
        size: p739,
        timestamp: Date.now()
      });
    },
    truncate(p740, p741) {
      if (p741 < 0) {
        throw new vO23.ErrnoError(28);
      }
      var v317;
      v317 = typeof p740 == "string" ? vO23.lookupPath(p740, {
        follow: true
      }).node : p740;
      vO23.doTruncate(null, v317, p741);
    },
    ftruncate(p742, p743) {
      var v318 = vO23.getStreamChecked(p742);
      if (p743 < 0 || !(v318.flags & 2097155)) {
        throw new vO23.ErrnoError(28);
      }
      vO23.doTruncate(v318, v318.node, p743);
    },
    utime(p744, p745, p746) {
      var v319 = vO23.lookupPath(p744, {
        follow: true
      }).node;
      vO23.checkOpExists(v319.node_ops.setattr, 63)(v319, {
        atime: p745,
        mtime: p746
      });
    },
    open(p747, p748, p749 = 438) {
      if (p747 === "") {
        throw new vO23.ErrnoError(44);
      }
      var v320;
      var v321;
      p749 = (p748 = typeof p748 == "string" ? (p750 => {
        var v322 = {
          r: 0,
          "r+": 2,
          w: 577,
          "w+": 578,
          a: 1089,
          "a+": 1090
        }[p750];
        if (v322 === undefined) {
          throw new Error("Unknown file open mode: " + p750);
        }
        return v322;
      })(p748) : p748) & 64 ? p749 & 4095 | 32768 : 0;
      if (typeof p747 == "object") {
        v320 = p747;
      } else {
        v321 = p747.endsWith("/");
        var v323 = vO23.lookupPath(p747, {
          follow: !(p748 & 131072),
          noent_okay: true
        });
        v320 = v323.node;
        p747 = v323.path;
      }
      var v324 = false;
      if (p748 & 64) {
        if (v320) {
          if (p748 & 128) {
            throw new vO23.ErrnoError(20);
          }
        } else {
          if (v321) {
            throw new vO23.ErrnoError(31);
          }
          v320 = vO23.mknod(p747, p749 | 511, 0);
          v324 = true;
        }
      }
      if (!v320) {
        throw new vO23.ErrnoError(44);
      }
      if (vO23.isChrdev(v320.mode)) {
        p748 &= -513;
      }
      if (p748 & 65536 && !vO23.isDir(v320.mode)) {
        throw new vO23.ErrnoError(54);
      }
      if (!v324) {
        var v325 = vO23.mayOpen(v320, p748);
        if (v325) {
          throw new vO23.ErrnoError(v325);
        }
      }
      if (p748 & 512 && !v324) {
        vO23.truncate(v320, 0);
      }
      p748 &= -131713;
      var v326 = vO23.createStream({
        node: v320,
        path: vO23.getPath(v320),
        flags: p748,
        seekable: true,
        position: 0,
        stream_ops: v320.stream_ops,
        ungotten: [],
        error: false
      });
      if (v326.stream_ops.open) {
        v326.stream_ops.open(v326);
      }
      if (v324) {
        vO23.chmod(v320, p749 & 511);
      }
      if (!!vP.logReadFiles && !(p748 & 1) && !(p747 in vO23.readFiles)) {
        vO23.readFiles[p747] = 1;
      }
      return v326;
    },
    close(p751) {
      if (vO23.isClosed(p751)) {
        throw new vO23.ErrnoError(8);
      }
      p751.getdents &&= null;
      try {
        if (p751.stream_ops.close) {
          p751.stream_ops.close(p751);
        }
      } catch (e50) {
        throw e50;
      } finally {
        vO23.closeStream(p751.fd);
      }
      p751.fd = null;
    },
    isClosed: p752 => p752.fd === null,
    llseek(p753, p754, p755) {
      if (vO23.isClosed(p753)) {
        throw new vO23.ErrnoError(8);
      }
      if (!p753.seekable || !p753.stream_ops.llseek) {
        throw new vO23.ErrnoError(70);
      }
      if (p755 != 0 && p755 != 1 && p755 != 2) {
        throw new vO23.ErrnoError(28);
      }
      p753.position = p753.stream_ops.llseek(p753, p754, p755);
      p753.ungotten = [];
      return p753.position;
    },
    read(p756, p757, p758, p759, p760) {
      if (p759 < 0 || p760 < 0) {
        throw new vO23.ErrnoError(28);
      }
      if (vO23.isClosed(p756)) {
        throw new vO23.ErrnoError(8);
      }
      if ((p756.flags & 2097155) == 1) {
        throw new vO23.ErrnoError(8);
      }
      if (vO23.isDir(p756.node.mode)) {
        throw new vO23.ErrnoError(31);
      }
      if (!p756.stream_ops.read) {
        throw new vO23.ErrnoError(28);
      }
      var v327 = p760 !== undefined;
      if (v327) {
        if (!p756.seekable) {
          throw new vO23.ErrnoError(70);
        }
      } else {
        p760 = p756.position;
      }
      var v328 = p756.stream_ops.read(p756, p757, p758, p759, p760);
      if (!v327) {
        p756.position += v328;
      }
      return v328;
    },
    write(p761, p762, p763, p764, p765, p766) {
      if (p764 < 0 || p765 < 0) {
        throw new vO23.ErrnoError(28);
      }
      if (vO23.isClosed(p761)) {
        throw new vO23.ErrnoError(8);
      }
      if (!(p761.flags & 2097155)) {
        throw new vO23.ErrnoError(8);
      }
      if (vO23.isDir(p761.node.mode)) {
        throw new vO23.ErrnoError(31);
      }
      if (!p761.stream_ops.write) {
        throw new vO23.ErrnoError(28);
      }
      if (p761.seekable && p761.flags & 1024) {
        vO23.llseek(p761, 0, 2);
      }
      var v329 = p765 !== undefined;
      if (v329) {
        if (!p761.seekable) {
          throw new vO23.ErrnoError(70);
        }
      } else {
        p765 = p761.position;
      }
      var v330 = p761.stream_ops.write(p761, p762, p763, p764, p765, p766);
      if (!v329) {
        p761.position += v330;
      }
      return v330;
    },
    mmap(p767, p768, p769, p770, p771) {
      if (p770 & 2 && !(p771 & 2) && (p767.flags & 2097155) != 2) {
        throw new vO23.ErrnoError(2);
      }
      if ((p767.flags & 2097155) == 1) {
        throw new vO23.ErrnoError(2);
      }
      if (!p767.stream_ops.mmap) {
        throw new vO23.ErrnoError(43);
      }
      if (!p768) {
        throw new vO23.ErrnoError(28);
      }
      return p767.stream_ops.mmap(p767, p768, p769, p770, p771);
    },
    msync: (p772, p773, p774, p775, p776) => p772.stream_ops.msync ? p772.stream_ops.msync(p772, p773, p774, p775, p776) : 0,
    ioctl(p777, p778, p779) {
      if (!p777.stream_ops.ioctl) {
        throw new vO23.ErrnoError(59);
      }
      return p777.stream_ops.ioctl(p777, p778, p779);
    },
    readFile(p780, p781 = {}) {
      p781.flags = p781.flags || 0;
      p781.encoding = p781.encoding || "binary";
      if (p781.encoding !== "utf8" && p781.encoding !== "binary") {
        throw new Error("Invalid encoding type \"" + p781.encoding + "\"");
      }
      var v331 = vO23.open(p780, p781.flags);
      var v332 = vO23.stat(p780).size;
      var v333 = new Uint8Array(v332);
      vO23.read(v331, v333, 0, v332, 0);
      if (p781.encoding === "utf8") {
        v333 = vF111(v333);
      }
      vO23.close(v331);
      return v333;
    },
    writeFile(p782, p783, p784 = {}) {
      p784.flags = p784.flags || 577;
      var v334 = vO23.open(p782, p784.flags, p784.mode);
      if (typeof p783 == "string") {
        p783 = new Uint8Array(vF112(p783));
      }
      if (!ArrayBuffer.isView(p783)) {
        throw new Error("Unsupported data type");
      }
      vO23.write(v334, p783, 0, p783.byteLength, undefined, p784.canOwn);
      vO23.close(v334);
    },
    cwd: () => vO23.currentPath,
    chdir(p785) {
      var v335 = vO23.lookupPath(p785, {
        follow: true
      });
      if (v335.node === null) {
        throw new vO23.ErrnoError(44);
      }
      if (!vO23.isDir(v335.node.mode)) {
        throw new vO23.ErrnoError(54);
      }
      var v336 = vO23.nodePermissions(v335.node, "x");
      if (v336) {
        throw new vO23.ErrnoError(v336);
      }
      vO23.currentPath = v335.path;
    },
    createDefaultDirectories() {
      vO23.mkdir("/tmp");
      vO23.mkdir("/home");
      vO23.mkdir("/home/web_user");
    },
    createDefaultDevices() {
      vO23.mkdir("/dev");
      vO23.registerDevice(vO23.makedev(1, 3), {
        read: () => 0,
        write: (p786, p787, p788, p789, p790) => p789,
        llseek: () => 0
      });
      vO23.mkdev("/dev/null", vO23.makedev(1, 3));
      vO20.register(vO23.makedev(5, 0), vO20.default_tty_ops);
      vO20.register(vO23.makedev(6, 0), vO20.default_tty1_ops);
      vO23.mkdev("/dev/tty", vO23.makedev(5, 0));
      vO23.mkdev("/dev/tty1", vO23.makedev(6, 0));
      var v337 = new Uint8Array(1024);
      var vLN039 = 0;
      var vF116 = () => {
        if (vLN039 === 0) {
          vF110(v337);
          vLN039 = v337.byteLength;
        }
        return v337[--vLN039];
      };
      vO23.createDevice("/dev", "random", vF116);
      vO23.createDevice("/dev", "urandom", vF116);
      vO23.mkdir("/dev/shm");
      vO23.mkdir("/dev/shm/tmp");
    },
    createSpecialDirectories() {
      vO23.mkdir("/proc");
      var v338 = vO23.mkdir("/proc/self");
      vO23.mkdir("/proc/self/fd");
      vO23.mount({
        mount() {
          var v339 = vO23.createNode(v338, "fd", 16895, 73);
          v339.stream_ops = {
            llseek: vO21.stream_ops.llseek
          };
          v339.node_ops = {
            lookup(p791, p792) {
              var v340 = +p792;
              var v341 = vO23.getStreamChecked(v340);
              var vO26 = {
                parent: null,
                mount: {
                  mountpoint: "fake"
                },
                node_ops: {
                  readlink: () => v341.path
                },
                id: v340 + 1
              };
              vO26.parent = vO26;
              return vO26;
            },
            readdir: () => Array.from(vO23.streams.entries()).filter(([v342, v343]) => v343).map(([v344, v345]) => v344.toString())
          };
          return v339;
        }
      }, {}, "/proc/self/fd");
    },
    createStandardStreams(p793, p794, p795) {
      if (p793) {
        vO23.createDevice("/dev", "stdin", p793);
      } else {
        vO23.symlink("/dev/tty", "/dev/stdin");
      }
      if (p794) {
        vO23.createDevice("/dev", "stdout", null, p794);
      } else {
        vO23.symlink("/dev/tty", "/dev/stdout");
      }
      if (p795) {
        vO23.createDevice("/dev", "stderr", null, p795);
      } else {
        vO23.symlink("/dev/tty1", "/dev/stderr");
      }
      vO23.open("/dev/stdin", 0);
      vO23.open("/dev/stdout", 1);
      vO23.open("/dev/stderr", 1);
    },
    staticInit() {
      vO23.nameTable = new Array(4096);
      vO23.mount(vO21, {}, "/");
      vO23.createDefaultDirectories();
      vO23.createDefaultDevices();
      vO23.createSpecialDirectories();
      vO23.filesystems = {
        MEMFS: vO21
      };
    },
    init(p796, p797, p798) {
      vO23.initialized = true;
      p796 ??= vP.stdin;
      p797 ??= vP.stdout;
      p798 ??= vP.stderr;
      vO23.createStandardStreams(p796, p797, p798);
    },
    quit() {
      vO23.initialized = false;
      for (var v346 of vO23.streams) {
        if (v346) {
          vO23.close(v346);
        }
      }
    },
    findObject(p799, p800) {
      var v347 = vO23.analyzePath(p799, p800);
      if (v347.exists) {
        return v347.object;
      } else {
        return null;
      }
    },
    analyzePath(p801, p802) {
      try {
        p801 = (v348 = vO23.lookupPath(p801, {
          follow: !p802
        })).path;
      } catch (e51) {}
      var vO27 = {
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
        var v348 = vO23.lookupPath(p801, {
          parent: true
        });
        vO27.parentExists = true;
        vO27.parentPath = v348.path;
        vO27.parentObject = v348.node;
        vO27.name = vO18.basename(p801);
        v348 = vO23.lookupPath(p801, {
          follow: !p802
        });
        vO27.exists = true;
        vO27.path = v348.path;
        vO27.object = v348.node;
        vO27.name = v348.node.name;
        vO27.isRoot = v348.path === "/";
      } catch (e52) {
        vO27.error = e52.errno;
      }
      return vO27;
    },
    createPath(p803, p804, p805, p806) {
      p803 = typeof p803 == "string" ? p803 : vO23.getPath(p803);
      for (var v349 = p804.split("/").reverse(); v349.length;) {
        var v350 = v349.pop();
        if (v350) {
          var v351 = vO18.join2(p803, v350);
          try {
            vO23.mkdir(v351);
          } catch (e53) {
            if (e53.errno != 20) {
              throw e53;
            }
          }
          p803 = v351;
        }
      }
      return v351;
    },
    createFile(p807, p808, p809, p810, p811) {
      var v352 = vO18.join2(typeof p807 == "string" ? p807 : vO23.getPath(p807), p808);
      var vVF114 = vF114(p810, p811);
      return vO23.create(v352, vVF114);
    },
    createDataFile(p812, p813, p814, p815, p816, p817) {
      var vP813 = p813;
      if (p812) {
        p812 = typeof p812 == "string" ? p812 : vO23.getPath(p812);
        vP813 = p813 ? vO18.join2(p812, p813) : p812;
      }
      var vVF1142 = vF114(p815, p816);
      var v353 = vO23.create(vP813, vVF1142);
      if (p814) {
        if (typeof p814 == "string") {
          var v354 = new Array(p814.length);
          for (var vLN040 = 0, v355 = p814.length; vLN040 < v355; ++vLN040) {
            v354[vLN040] = p814.charCodeAt(vLN040);
          }
          p814 = v354;
        }
        vO23.chmod(v353, vVF1142 | 146);
        var v356 = vO23.open(v353, 577);
        vO23.write(v356, p814, 0, p814.length, 0, p817);
        vO23.close(v356);
        vO23.chmod(v353, vVF1142);
      }
    },
    createDevice(p818, p819, p820, p821) {
      var v357 = vO18.join2(typeof p818 == "string" ? p818 : vO23.getPath(p818), p819);
      var vVF1143 = vF114(!!p820, !!p821);
      vO23.createDevice.major ??= 64;
      var v358 = vO23.makedev(vO23.createDevice.major++, 0);
      vO23.registerDevice(v358, {
        open(p822) {
          p822.seekable = false;
        },
        close(p823) {
          if (p821?.buffer?.length) {
            p821(10);
          }
        },
        read(p824, p825, p826, p827, p828) {
          var vLN041 = 0;
          for (var vLN042 = 0; vLN042 < p827; vLN042++) {
            var v359;
            try {
              v359 = p820();
            } catch (e54) {
              throw new vO23.ErrnoError(29);
            }
            if (v359 === undefined && vLN041 === 0) {
              throw new vO23.ErrnoError(6);
            }
            if (v359 == null) {
              break;
            }
            vLN041++;
            p825[p826 + vLN042] = v359;
          }
          if (vLN041) {
            p824.node.atime = Date.now();
          }
          return vLN041;
        },
        write(p829, p830, p831, p832, p833) {
          for (var vLN043 = 0; vLN043 < p832; vLN043++) {
            try {
              p821(p830[p831 + vLN043]);
            } catch (e55) {
              throw new vO23.ErrnoError(29);
            }
          }
          if (p832) {
            p829.node.mtime = p829.node.ctime = Date.now();
          }
          return vLN043;
        }
      });
      return vO23.mkdev(v357, vVF1143, v358);
    },
    forceLoadFile(p834) {
      if (p834.isDevice || p834.isFolder || p834.link || p834.contents) {
        return true;
      }
      if (typeof XMLHttpRequest != "undefined") {
        throw new Error("Lazy loading should have been performed (contents set) in createLazyFile, but it was not. Lazy loading only works in web workers. Use --embed-file or --preload-file in emcc on the main thread.");
      }
      try {
        p834.contents = v34(p834.url);
        p834.usedBytes = p834.contents.length;
      } catch (e56) {
        throw new vO23.ErrnoError(29);
      }
    },
    createLazyFile(p835, p836, p837, p838, p839) {
      function f57(p840, p841, p842, p843, p844) {
        var v360 = p840.node.contents;
        if (p844 >= v360.length) {
          return 0;
        }
        var v361 = Math.min(v360.length - p844, p843);
        if (v360.slice) {
          for (var vLN044 = 0; vLN044 < v361; vLN044++) {
            p841[p842 + vLN044] = v360[p844 + vLN044];
          }
        } else {
          for (vLN044 = 0; vLN044 < v361; vLN044++) {
            p841[p842 + vLN044] = v360.get(p844 + vLN044);
          }
        }
        return v361;
      }
      class C4 {
        lengthKnown = false;
        chunks = [];
        get(p845) {
          if (!(p845 > this.length - 1) && !(p845 < 0)) {
            var v362 = p845 % this.chunkSize;
            var v363 = p845 / this.chunkSize | 0;
            return this.getter(v363)[v362];
          }
        }
        setDataGetter(p846) {
          this.getter = p846;
        }
        cacheLength() {
          var v364 = new XMLHttpRequest();
          v364.open("HEAD", p837, false);
          v364.send(null);
          if ((!(v364.status >= 200) || !(v364.status < 300)) && v364.status !== 304) {
            throw new Error("Couldn't load " + p837 + ". Status: " + v364.status);
          }
          var v365;
          var vNumber = Number(v364.getResponseHeader("Content-length"));
          var v366 = (v365 = v364.getResponseHeader("Accept-Ranges")) && v365 === "bytes";
          var v367 = (v365 = v364.getResponseHeader("Content-Encoding")) && v365 === "gzip";
          var vLN1048576 = 1048576;
          if (!v366) {
            vLN1048576 = vNumber;
          }
          var vThis = this;
          vThis.setDataGetter(p847 => {
            var v368 = p847 * vLN1048576;
            var v369 = (p847 + 1) * vLN1048576 - 1;
            v369 = Math.min(v369, vNumber - 1);
            if (vThis.chunks[p847] === undefined) {
              vThis.chunks[p847] = ((p848, p849) => {
                if (p848 > p849) {
                  throw new Error("invalid range (" + p848 + ", " + p849 + ") or no bytes requested!");
                }
                if (p849 > vNumber - 1) {
                  throw new Error("only " + vNumber + " bytes available! programmer error!");
                }
                var v370 = new XMLHttpRequest();
                v370.open("GET", p837, false);
                if (vNumber !== vLN1048576) {
                  v370.setRequestHeader("Range", "bytes=" + p848 + "-" + p849);
                }
                v370.responseType = "arraybuffer";
                if (v370.overrideMimeType) {
                  v370.overrideMimeType("text/plain; charset=x-user-defined");
                }
                v370.send(null);
                if ((!(v370.status >= 200) || !(v370.status < 300)) && v370.status !== 304) {
                  throw new Error("Couldn't load " + p837 + ". Status: " + v370.status);
                }
                if (v370.response !== undefined) {
                  return new Uint8Array(v370.response || []);
                } else {
                  return vF112(v370.responseText || "");
                }
              })(v368, v369);
            }
            if (vThis.chunks[p847] === undefined) {
              throw new Error("doXHR failed!");
            }
            return vThis.chunks[p847];
          });
          if (!!v367 || !vNumber) {
            vLN1048576 = vNumber = 1;
            vNumber = this.getter(0).length;
            vLN1048576 = vNumber;
            v57("LazyFiles on gzip forces download of the whole file when length is accessed");
          }
          this._length = vNumber;
          this._chunkSize = vLN1048576;
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
        if (!v32) {
          throw "Cannot do synchronous binary XHRs outside webworkers in modern browsers. Use --embed-file or --preload-file in emcc";
        }
        var vO28 = {
          isDevice: false,
          contents: new C4()
        };
      } else {
        vO28 = {
          isDevice: false,
          url: p837
        };
      }
      var v371 = vO23.createFile(p835, p836, vO28, p838, p839);
      if (vO28.contents) {
        v371.contents = vO28.contents;
      } else if (vO28.url) {
        v371.contents = null;
        v371.url = vO28.url;
      }
      Object.defineProperties(v371, {
        usedBytes: {
          get: function () {
            return this.contents.length;
          }
        }
      });
      var vO29 = {};
      Object.keys(v371.stream_ops).forEach(p850 => {
        var v372 = v371.stream_ops[p850];
        vO29[p850] = (..._0x23001f) => {
          vO23.forceLoadFile(v371);
          return v372(..._0x23001f);
        };
      });
      vO29.read = (p851, p852, p853, p854, p855) => {
        vO23.forceLoadFile(v371);
        return f57(p851, p852, p853, p854, p855);
      };
      vO29.mmap = (p856, p857, p858, p859, p860) => {
        vO23.forceLoadFile(v371);
        var vVF113 = vF113();
        if (!vVF113) {
          throw new vO23.ErrnoError(48);
        }
        f57(p856, v47, vVF113, p857, p858);
        return {
          ptr: vVF113,
          allocated: true
        };
      };
      v371.stream_ops = vO29;
      return v371;
    }
  };
  var vO30 = {
    DEFAULT_POLLMASK: 5,
    calculateAt(p861, p862, p863) {
      if (vO18.isAbs(p862)) {
        return p862;
      }
      var v373;
      v373 = p861 === -100 ? vO23.cwd() : vO30.getStreamFromFD(p861).path;
      if (p862.length == 0) {
        if (!p863) {
          throw new vO23.ErrnoError(44);
        }
        return v373;
      }
      return v373 + "/" + p862;
    },
    writeStat(p864, p865) {
      v52[p864 >> 2] = p865.dev;
      v52[p864 + 4 >> 2] = p865.mode;
      v52[p864 + 8 >> 2] = p865.nlink;
      v52[p864 + 12 >> 2] = p865.uid;
      v52[p864 + 16 >> 2] = p865.gid;
      v52[p864 + 20 >> 2] = p865.rdev;
      v55[p864 + 24 >> 3] = BigInt(p865.size);
      v51[p864 + 32 >> 2] = 4096;
      v51[p864 + 36 >> 2] = p865.blocks;
      var v374 = p865.atime.getTime();
      var v375 = p865.mtime.getTime();
      var v376 = p865.ctime.getTime();
      v55[p864 + 40 >> 3] = BigInt(Math.floor(v374 / 1000));
      v52[p864 + 48 >> 2] = v374 % 1000 * 1000 * 1000;
      v55[p864 + 56 >> 3] = BigInt(Math.floor(v375 / 1000));
      v52[p864 + 64 >> 2] = v375 % 1000 * 1000 * 1000;
      v55[p864 + 72 >> 3] = BigInt(Math.floor(v376 / 1000));
      v52[p864 + 80 >> 2] = v376 % 1000 * 1000 * 1000;
      v55[p864 + 88 >> 3] = BigInt(p865.ino);
      return 0;
    },
    writeStatFs(p866, p867) {
      v52[p866 + 4 >> 2] = p867.bsize;
      v52[p866 + 60 >> 2] = p867.bsize;
      v55[p866 + 8 >> 3] = BigInt(p867.blocks);
      v55[p866 + 16 >> 3] = BigInt(p867.bfree);
      v55[p866 + 24 >> 3] = BigInt(p867.bavail);
      v55[p866 + 32 >> 3] = BigInt(p867.files);
      v55[p866 + 40 >> 3] = BigInt(p867.ffree);
      v52[p866 + 48 >> 2] = p867.fsid;
      v52[p866 + 64 >> 2] = p867.flags;
      v52[p866 + 56 >> 2] = p867.namelen;
    },
    doMsync(p868, p869, p870, p871, p872) {
      if (!vO23.isFile(p869.node.mode)) {
        throw new vO23.ErrnoError(43);
      }
      if (p871 & 2) {
        return 0;
      }
      var v377 = v48.slice(p868, p868 + p870);
      vO23.msync(p869, v377, p872, p870, p871);
    },
    getStreamFromFD: p873 => vO23.getStreamChecked(p873),
    varargs: undefined,
    getStr: p874 => vF19(p874)
  };
  var vF117 = (p875, p876, p877, p878) => {
    var vLN045 = 0;
    for (var vLN046 = 0; vLN046 < p877; vLN046++) {
      var v378 = v52[p876 >> 2];
      var v379 = v52[p876 + 4 >> 2];
      p876 += 8;
      var v380 = vO23.read(p875, v47, v378, v379, p878);
      if (v380 < 0) {
        return -1;
      }
      vLN045 += v380;
      if (v380 < v379) {
        break;
      }
    }
    return vLN045;
  };
  var vF118 = (p879, p880, p881, p882) => {
    var vLN047 = 0;
    for (var vLN048 = 0; vLN048 < p881; vLN048++) {
      var v381 = v52[p880 >> 2];
      var v382 = v52[p880 + 4 >> 2];
      p880 += 8;
      var v383 = vO23.write(p879, v47, v381, v382, p882);
      if (v383 < 0) {
        return -1;
      }
      vLN047 += v383;
      if (v383 < v382) {
        break;
      }
    }
    return vLN047;
  };
  vO2.init();
  vO23.createPreloadedFile = (p883, p884, p885, p886, p887, p888, p889, p890, p891, p892) => {
    vF115(p883, p884, p885, p886, p887, p890, p891, p892).then(p888).catch(p889);
  };
  vO23.preloadFile = vF115;
  vO23.staticInit();
  (function () {
    if (!v35) {
      if (vP.wasmMemory) {
        v46 = vP.wasmMemory;
      } else {
        var v384 = vP.INITIAL_MEMORY || 134217728;
        v46 = new WebAssembly.Memory({
          initial: v384 / 65536,
          maximum: v384 / 65536,
          shared: true
        });
      }
      f2();
    }
  })();
  if (vP.noExitRuntime) {
    v85 = vP.noExitRuntime;
  }
  if (vP.preloadPlugins) {
    vA27 = vP.preloadPlugins;
  }
  if (vP.print) {
    v57 = vP.print;
  }
  if (vP.printErr) {
    v58 = vP.printErr;
  }
  if (vP.wasmBinary) {
    v39 = vP.wasmBinary;
  }
  if (vP.arguments) {
    vP.arguments;
  }
  if (vP.thisProgram) {
    vLSthisprogram = vP.thisProgram;
  }
  vP.ccall = (p893, p894, p895, p896, p897) => {
    function f58(p898) {
      vF48();
      if (vLN050 !== 0) {
        vF13(vLN050);
      }
      return function (p899) {
        if (p894 === "string") {
          return vF19(p899);
        } else if (p894 === "boolean") {
          return Boolean(p899);
        } else {
          return p899;
        }
      }(p898);
    }
    var vO31 = {
      string: p900 => {
        var vLN049 = 0;
        if (p900 != null && p900 !== 0) {
          vLN049 = (p901 => {
            var v385 = vF63(p901) + 1;
            var vVF142 = vF14(v385);
            vF62(p901, vVF142, v385);
            return vVF142;
          })(p900);
        }
        return vLN049;
      },
      array: p902 => {
        var v386;
        var v387;
        var vVF143 = vF14(p902.length);
        v386 = p902;
        v387 = vVF143;
        v47.set(v386, v387);
        return vVF143;
      }
    };
    var vF119 = (p903 => vP["_" + p903])(p893);
    var vA30 = [];
    var vLN050 = 0;
    if (p896) {
      for (var vLN051 = 0; vLN051 < p896.length; vLN051++) {
        var v388 = vO31[p895[vLN051]];
        if (v388) {
          if (vLN050 === 0) {
            vLN050 = vF12();
          }
          vA30[vLN051] = v388(p896[vLN051]);
        } else {
          vA30[vLN051] = p896[vLN051];
        }
      }
    }
    var v389 = vO10.currData;
    var vVF119 = vF119(...vA30);
    var v390 = p897?.async;
    vF47();
    if (vO10.currData != v389) {
      return vO10.whenDone().then(f58);
    } else {
      vVF119 = f58(vVF119);
      if (v390) {
        return Promise.resolve(vVF119);
      } else {
        return vVF119;
      }
    }
  };
  var v391;
  var v392;
  var v393;
  var v394;
  var v395;
  var v396;
  var v397;
  var v398;
  var v399;
  var v400;
  var v401;
  var v402;
  var v403;
  var v404;
  var v405;
  var v406;
  var v407;
  var v408;
  var v409;
  var v410;
  var v411;
  var v412;
  var v413;
  var v414;
  var v415;
  var v416;
  var v417;
  var v418;
  var v419;
  var v420;
  var v421;
  var v422;
  var v423;
  var v424;
  var v425;
  var v426;
  var v427;
  var v428;
  var v429;
  var v430;
  var v431;
  var v432;
  var v433;
  var v434;
  var v435;
  var v436;
  var v437;
  var v438;
  var v439;
  var v440;
  var v441;
  var v442;
  var v443;
  var v444;
  var v445;
  var vA31 = [f8, f9, f10, f15, f16, f17, f18, f19, f20, f21];
  var vO32 = {
    170760: () => {
      try {
        window.location.href = "https://google.com";
      } catch (e57) {}
    }
  };
  var v446 = await async function () {
    function f59(p904, p905) {
      var v447;
      var v448;
      v446 = p904.exports;
      v448 = (v446 = vO10.instrumentWasmExports(v446)).Pa;
      vO2.tlsInitFunctions.push(v448);
      v446.Qa;
      v40 = p905;
      v391 = (v447 = v446).Ja;
      v392 = v447.Ka;
      vP._main = v393 = v447.La;
      v394 = v447.Ma;
      v395 = v447.Na;
      v396 = v447.Oa;
      v447.Pa;
      v397 = v447.Ra;
      v398 = v447.Sa;
      v399 = v447.Ta;
      v400 = v447.Ua;
      v401 = v447.Va;
      v402 = v447.Wa;
      v403 = v447.Xa;
      v404 = v447.Ya;
      v405 = v447.Za;
      v406 = v447._a;
      v407 = v447.$a;
      v408 = v447.ab;
      v409 = v447.bb;
      v410 = v447.cb;
      v411 = v447.db;
      v412 = v447.eb;
      v413 = v447.fb;
      vO3.v = v414 = v447.gb;
      vO3.vii = v415 = v447.hb;
      vO3.vi = v416 = v447.ib;
      vO3.viiii = v417 = v447.jb;
      vO3.ii = v418 = v447.kb;
      vO3.iii = v419 = v447.lb;
      vO3.viji = v420 = v447.mb;
      vO3.viii = v421 = v447.nb;
      vO3.viiiii = v422 = v447.ob;
      vO3.iiii = v423 = v447.pb;
      vO3.i = v424 = v447.qb;
      vO3.diiiii = v425 = v447.rb;
      vO3.iiiii = v426 = v447.sb;
      vO3.iiiiii = v427 = v447.tb;
      vO3.ijiii = v428 = v447.ub;
      vO3.ijjiii = v429 = v447.vb;
      vO3.iiiiiiii = v430 = v447.wb;
      vO3.iiji = v431 = v447.xb;
      vO3.ijii = v432 = v447.yb;
      vO3.iijii = v433 = v447.zb;
      vO3.iiiiiii = v434 = v447.Ab;
      vO3.iidiiii = v447.Bb;
      vO3.jiiii = v435 = v447.Cb;
      vO3.fiii = v436 = v447.Db;
      vO3.diii = v437 = v447.Eb;
      vO3.viiiiiii = v438 = v447.Fb;
      vO3.iiiiiiiiiiii = v439 = v447.Gb;
      vO3.viiiiiiiiii = v440 = v447.Hb;
      vO3.viiiiiiiiiiiiiii = v441 = v447.Ib;
      vO3.vij = v447.Jb;
      vO3.viiij = v447.Kb;
      vO3.ji = v447.Lb;
      vO3.jij = v447.Mb;
      vO3.iiiijii = v447.Nb;
      vO3.jiji = v447.Ob;
      vO3.viijii = v447.Pb;
      vO3.iiiiiiiii = v447.Qb;
      vO3.iiiiij = v447.Rb;
      vO3.iiiiid = v447.Sb;
      vO3.iiiiijj = v447.Tb;
      vO3.iiiiiijj = v447.Ub;
      vO3.viiiiii = v447.Vb;
      v442 = v447.Wb;
      v443 = v447.Xb;
      v444 = v447.Yb;
      v445 = v447.Zb;
      f5();
      return v446;
    }
    f4();
    var v449;
    var v450;
    var vF710 = f7();
    if (vP.instantiateWasm) {
      return new Promise((p906, p907) => {
        vP.instantiateWasm(vF710, (p908, p909) => {
          p906(f59(p908, p909));
        });
      });
    } else if (v35) {
      return new Promise(p910 => {
        v44 = p911 => {
          var v451 = new WebAssembly.Instance(p911, f7());
          p910(f59(v451, p911));
        };
      });
    } else {
      v68 ??= vP.locateFile ? (v449 = "wasm.wasm", vP.locateFile ? vP.locateFile(v449, vLS) : vLS + v449) : new URL("/static/assets/d4pJlc1V.wasm", import.meta.url).href;
      return f59((v450 = await async function (p912, p913, p914) {
        if (!p912) {
          try {
            var vFetch = fetch(p913, {
              credentials: "same-origin"
            });
            return await WebAssembly.instantiateStreaming(vFetch, p914);
          } catch (e58) {
            v58("wasm streaming compile failed: " + e58);
            v58("falling back to ArrayBuffer instantiation");
          }
        }
        return async function (p915, p916) {
          try {
            var v452 = await async function (p917) {
              if (!v39) {
                try {
                  var v453 = await v33(p917);
                  return new Uint8Array(v453);
                } catch {}
              }
              return function (p918) {
                if (p918 == v68 && v39) {
                  return new Uint8Array(v39);
                }
                if (v34) {
                  return v34(p918);
                }
                throw "both async and sync fetching of the wasm failed";
              }(p917);
            }(p915);
            return await WebAssembly.instantiate(v452, p916);
          } catch (e59) {
            v58("failed to asynchronously prepare wasm: " + e59);
            f6(e59);
          }
        }(p913, p914);
      }(v39, v68, vF710)).instance, v450.module);
    }
  }();
  (function () {
    if (vP.preInit) {
      for (typeof vP.preInit == "function" && (vP.preInit = [vP.preInit]); vP.preInit.length > 0;) {
        vP.preInit.shift()();
      }
    }
  })();
  (function f60() {
    function f61() {
      vP.calledRun = true;
      if (!v59) {
        f3();
        v42?.(vP);
        vP.onRuntimeInitialized?.();
        if (!vP.noInitialRun) {
          (function () {
            var vV393 = v393;
            try {
              var vVV393 = vV393(0, 0);
              vF16(vVV393);
              return vVV393;
            } catch (e60) {
              return vF45(e60);
            }
          })();
        }
        (function () {
          if (!v35) {
            if (vP.postRun) {
              for (typeof vP.postRun == "function" && (vP.postRun = [vP.postRun]); vP.postRun.length;) {
                v454 = vP.postRun.shift();
                vA5.push(v454);
              }
            }
            vF8(vA5);
          }
          var v454;
        })();
      }
    }
    if (vLN05 > 0) {
      v70 = f60;
    } else {
      if (v35) {
        v42?.(vP);
        f3();
        return;
      }
      (function () {
        if (vP.preRun) {
          for (typeof vP.preRun == "function" && (vP.preRun = [vP.preRun]); vP.preRun.length;) {
            vF9(vP.preRun.shift());
          }
        }
        vF8(vA3);
      })();
      if (vLN05 > 0) {
        v70 = f60;
      } else if (vP.setStatus) {
        vP.setStatus("Running...");
        setTimeout(() => {
          setTimeout(() => vP.setStatus(""), 1);
          f61();
        }, 1);
      } else {
        f61();
      }
    }
  })();
  if (v69) {
    return vP;
  } else {
    return new Promise((p919, p920) => {
      v42 = p919;
      v43 = p920;
    });
  }
}
var e = globalThis.self?.name?.startsWith("em-pthread");
if (e) {
  Module();
}
export { Module as M };