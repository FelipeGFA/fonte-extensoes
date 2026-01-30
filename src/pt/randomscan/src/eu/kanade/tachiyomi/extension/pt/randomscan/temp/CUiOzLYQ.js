import { _ as _0x301feb, r as _0x4393c7, c as _0x3a4257, o as _0x5bd96a, B as _0x34ef6b, a as _0x255ca7, C as _0x18f9d4, n as _0x9b3f02, d as _0x107b60, t as _0x257e04, D as _0x199f04, l as _0x558732, E as _0x2dfab7, G as _0x585b13, I as _0x303419, s as _0x333b2b, J as _0x140264, F as _0x33e8e6, u as _0x4344bc, e as _0x2bf4c8, y, K as _0x21061d, b as _0x5b6e62, p as _0x497ef1, z as _0x13fb99, h as _0x2994f3, q as _0x3337de, L as _0x276817, M as _0x245777, k as _0xc668d3 } from "./CqksSKFa.js";
const L = ["src"];
const M = ["disabled"];
const F = ["disabled"];
const S = _0x301feb({
  __name: "comentarioForm",
  props: {
    recarregarFunction: {
      type: Function,
      required: true
    },
    pageId: {
      type: Number,
      required: true
    },
    fatherId: {
      type: Number,
      required: false
    },
    editingId: {
      type: Number,
      required: false,
      default: 0
    },
    editingMessage: {
      type: String,
      required: false,
      default: ""
    },
    editingIsSpoiler: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  setup(p5) {
    function f() {
      v_0x4393c74.value.click();
    }
    function f2(p6) {
      const v14 = p6.target.files[0];
      if (v14) {
        const vLN4194304 = 4194304;
        if (v14.size > vLN4194304) {
          window.alert("Arquivo de imagem excede o limite de 4 mb, selecione um arquivo menor.");
          f3();
          return;
        }
        v_0x4393c75.value = v14.name;
        const v15 = new FileReader();
        v15.readAsDataURL(v14);
        v15.onload = p7 => {
          v_0x4393c76.value = p7.target.result;
        };
      }
    }
    function f3() {
      v_0x4393c74.value.value = null;
      v_0x4393c75.value = "";
      v_0x4393c76.value = "";
    }
    const v_0x4393c7 = _0x4393c7(true);
    const vP5 = p5;
    const v_0x4393c72 = _0x4393c7(vP5.editingIsSpoiler);
    const v_0x4393c73 = _0x4393c7(vP5.editingMessage);
    const v_0x4393c74 = _0x4393c7(null);
    const v_0x4393c75 = _0x4393c7("");
    const v_0x4393c76 = _0x4393c7("");
    return (p8, p9) => {
      _0x5bd96a();
      return _0x3a4257("div", {
        class: _0x9b3f02(p8.$style.form)
      }, [_0x34ef6b(_0x255ca7("textarea", {
        "onUpdate:modelValue": p9[0] ||= p10 => v_0x4393c73.value = p10,
        placeholder: "escreva um comentÃ¡rio"
      }, null, 512), [[_0x18f9d4, v_0x4393c73.value]]), _0x255ca7("div", null, [_0x255ca7("div", {
        class: _0x9b3f02([p8.$style.image, v_0x4393c76.value ? p8.$style.preview : null])
      }, [v_0x4393c75.value ? (_0x5bd96a(), _0x3a4257("div", {
        key: 0,
        onClick: f3
      }, [_0x255ca7("span", null, _0x257e04(v_0x4393c75.value), 1), p9[5] ||= _0x255ca7("img", {
        src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='%23e8eaed'%3e%3cpath%20d='m480-424%20116%20116q11%2011%2028%2011t28-11q11-11%2011-28t-11-28L536-480l116-116q11-11%2011-28t-11-28q-11-11-28-11t-28%2011L480-536%20364-652q-11-11-28-11t-28%2011q-11%2011-11%2028t11%2028l116%20116-116%20116q-11%2011-11%2028t11%2028q11%2011%2028%2011t28-11l116-116Zm0%20344q-83%200-156-31.5T197-197q-54-54-85.5-127T80-480q0-83%2031.5-156T197-763q54-54%20127-85.5T480-880q83%200%20156%2031.5T763-763q54%2054%2085.5%20127T880-480q0%2083-31.5%20156T763-197q-54%2054-127%2085.5T480-80Zm0-80q134%200%20227-93t93-227q0-134-93-227t-227-93q-134%200-227%2093t-93%20227q0%20134%2093%20227t227%2093Zm0-320Z'/%3e%3c/svg%3e",
        alt: "Cancelar"
      }, null, -1)])) : _0x107b60("", true), v_0x4393c76.value ? (_0x5bd96a(), _0x3a4257("img", {
        key: 1,
        src: v_0x4393c76.value,
        onClick: f,
        class: _0x9b3f02(p8.$style.preview)
      }, null, 10, L)) : (_0x5bd96a(), _0x3a4257("img", {
        key: 2,
        src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20fill='%23fff'%20viewBox='0%200%20512%20512'%3e%3c!--!Font%20Awesome%20Free%206.7.2%20by%20@fontawesome%20-%20https://fontawesome.com%20License%20-%20https://fontawesome.com/license/free%20Copyright%202024%20Fonticons,%20Inc.--%3e%3cpath%20d='M448%2080c8.8%200%2016%207.2%2016%2016l0%20319.8-5-6.5-136-176c-4.5-5.9-11.6-9.3-19-9.3s-14.4%203.4-19%209.3L202%20340.7l-30.5-42.7C167%20291.7%20159.8%20288%20152%20288s-15%203.7-19.5%2010.1l-80%20112L48%20416.3l0-.3L48%2096c0-8.8%207.2-16%2016-16l384%200zM64%2032C28.7%2032%200%2060.7%200%2096L0%20416c0%2035.3%2028.7%2064%2064%2064l384%200c35.3%200%2064-28.7%2064-64l0-320c0-35.3-28.7-64-64-64L64%2032zm80%20192a48%2048%200%201%200%200-96%2048%2048%200%201%200%200%2096z'/%3e%3c/svg%3e",
        alt: "Upload Imagem",
        onClick: f
      })), _0x255ca7("input", {
        ref_key: "imageRef",
        ref: v_0x4393c74,
        type: "file",
        accept: ".png,.jpg,.jpeg,.webp,.gif,.avif",
        onChange: f2
      }, null, 544)], 2), _0x255ca7("div", {
        class: _0x9b3f02(p8.$style.spoiler),
        onClick: p9[2] ||= p11 => v_0x4393c72.value = !v_0x4393c72.value
      }, [_0x34ef6b(_0x255ca7("input", {
        type: "checkbox",
        id: "spoiler_option",
        "onUpdate:modelValue": p9[1] ||= p12 => v_0x4393c72.value = p12
      }, null, 512), [[_0x199f04, v_0x4393c72.value]]), p9[6] ||= _0x255ca7("p", null, "Spoiler", -1)], 2), vP5.editingMessage ? (_0x5bd96a(), _0x3a4257("button", {
        key: 0,
        onClick: p9[3] ||= p13 => {
          v_0x4393c7.value = false;
          if (v_0x4393c73.value.length >= 3) {
            _0x558732.post("/api/comments/edit/" + vP5.editingId + "/", {
              texto: v_0x4393c73.value,
              spoiler: v_0x4393c72.value
            }, {
              headers: {
                "X-CSRFToken": _0x2dfab7("csrftoken")
              }
            }).then(() => {
              setTimeout(vP5.recarregarFunction, 200);
            });
          } else {
            alert("O ComentÃ¡rio deve ter pelo menos 3 caracteres.");
          }
          return;
        },
        disabled: !v_0x4393c7.value
      }, " Editar ", 8, M)) : (_0x5bd96a(), _0x3a4257("button", {
        key: 1,
        onClick: p9[4] ||= p14 => function (p15, p16) {
          v_0x4393c7.value = false;
          const vV_0x4393c73 = v_0x4393c73;
          if (vV_0x4393c73.value.length >= 3 || v_0x4393c76.value) {
            const v16 = _0x558732.create({
              baseURL: "/api/comments/add",
              withCredentials: true
            });
            v16.interceptors.response.use(p17 => {
              if (p17.status >= 300 && p17.status < 400) {
                const v17 = p17.headers.location;
                if (v17) {
                  window.location.href = v17;
                }
              }
              return p17;
            });
            v16.post("/" + vP5.pageId + "/", {
              texto: vV_0x4393c73.value,
              resposta: p16,
              spoiler: v_0x4393c72.value,
              image: v_0x4393c76.value
            }, {
              headers: {
                "X-CSRFToken": _0x2dfab7("csrftoken")
              }
            }).then(p18 => {
              f3();
              v_0x4393c7.value = true;
              v_0x4393c73.value = "";
              if (p18.status === 201) {
                setTimeout(vP5.recarregarFunction, 200);
              }
            }).catch(p19 => {
              v_0x4393c7.value = true;
              v_0x4393c73.value = "";
              throw p19;
            });
          } else {
            alert("O ComentÃ¡rio deve ter pelo menos 3 caracteres ou uma imagem.");
          }
        }(0, vP5.fatherId ? vP5.fatherId : null),
        disabled: !v_0x4393c7.value
      }, " Comentar ", 8, F))])], 2);
    };
  }
}, [["__cssModules", {
  $style: {
    form: "X9RnM",
    image: "WpYHj",
    preview: "ToyOp",
    spoiler: "torvo"
  }
}], ["__scopeId", "data-v-9a3f405c"]]);
const R = ["id"];
const E = ["src"];
const H = {
  key: 1
};
const N = ["src"];
const B = _0x301feb({
  __name: "comentarioElement",
  props: {
    comment: {
      type: JSON,
      required: true
    },
    recarregarFunction: {
      type: Function,
      required: true
    },
    pageId: {
      type: Number,
      required: true
    },
    level: {
      type: Number,
      required: true
    },
    canDelete: {
      type: Boolean,
      required: true
    },
    reply: {
      type: String,
      required: false
    }
  },
  setup(p20) {
    function f4(p21, p22, p23) {
      const v18 = _0x558732.create({
        baseURL: "/api/comments/",
        withCredentials: true
      });
      const v19 = p23.currentTarget;
      if (p22 === "up") {
        v18.post("up/" + p21 + "/", {}, {
          headers: {
            "X-CSRFToken": _0x2dfab7("csrftoken")
          }
        }).then(p24 => {
          const v20 = v19.childNodes[1];
          if (p24.status === 201) {
            v20.innerText = String(Number(v20.innerText) + 1);
            v_0x4393c77.value.up_likes.push({
              name: _0x5efef5.value
            });
          } else {
            v20.innerText = String(Number(v20.innerText) - 1);
            v_0x4393c77.value.up_likes = v_0x4393c77.value.up_likes.filter(p25 => p25.name !== _0x5efef5.value);
          }
        });
      } else if (p22 === "down") {
        v18.post("down/" + p21 + "/", {}, {
          headers: {
            "X-CSRFToken": _0x2dfab7("csrftoken")
          }
        }).then(p26 => {
          const v21 = v19.childNodes[1];
          if (p26.status === 201) {
            v21.innerText = String(Number(v21.innerText) + 1);
            v_0x4393c77.value.down_likes.push({
              name: _0x5efef5.value
            });
          } else {
            v21.innerText = String(Number(v21.innerText) - 1);
            v_0x4393c77.value.down_likes = v_0x4393c77.value.down_likes.filter(p27 => p27.name !== _0x5efef5.value);
          }
        });
      }
    }
    function f5(p28) {
      if (!p28.composedPath().includes(v_0x4393c712.value)) {
        v_0x4393c711.value = false;
        document.removeEventListener("click", f5);
      }
    }
    function f6() {
      v_0x4393c711.value = !v_0x4393c711.value;
      document.addEventListener("click", f5);
    }
    function f7() {
      v_0x4393c710.value = !v_0x4393c710.value;
    }
    const vP20 = p20;
    const v_0x4393c77 = _0x4393c7(vP20.comment);
    const v_0x4393c78 = _0x4393c7(vP20.comment.spoiler);
    const v_0x4393c79 = _0x4393c7(false);
    const v_0x4393c710 = _0x4393c7(false);
    const v_0x4393c711 = _0x4393c7(false);
    const v_0x4393c712 = _0x4393c7(null);
    const v_0x585b13 = _0x585b13();
    const {
      username: _0x5efef5
    } = _0x303419(v_0x585b13);
    _0x333b2b(() => {
      document.removeEventListener("click", f5);
    });
    return (p29, p30) => {
      const v_0x140264 = _0x140264("comentario-element", true);
      _0x5bd96a();
      return _0x3a4257("div", {
        id: "comment-" + v_0x4393c77.value.id,
        class: _0x9b3f02(p29.$style.comment)
      }, [v_0x4393c77.value.deleted ? (_0x5bd96a(), _0x3a4257("img", {
        key: 1,
        class: _0x9b3f02(p29.$style.deleted),
        src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='rgba(255,%20255,%20255,%200.3)'%3e%3cpath%20d='M791-55%20686-160H160v-112q0-34%2017.5-62.5T224-378q45-23%2091.5-37t94.5-21L55-791l57-57%20736%20736-57%2057ZM240-240h366L486-360h-6q-56%200-111%2013.5T260-306q-9%205-14.5%2014t-5.5%2020v32Zm496-138q29%2014%2046%2042.5t18%2061.5L666-408q18%207%2035.5%2014t34.5%2016ZM568-506l-59-59q23-9%2037-29.5t14-45.5q0-33-23.5-56.5T480-720q-25%200-45.5%2014T405-669l-59-59q23-34%2058-53t76-19q66%200%20113%2047t47%20113q0%2041-19%2076t-53%2058Zm38%20266H240h366ZM457-617Z'/%3e%3c/svg%3e",
        alt: "Deleted User Image"
      }, null, 2)) : (_0x5bd96a(), _0x3a4257("img", {
        key: 0,
        src: v_0x4393c77.value.foto_hash + "/",
        alt: "Foto do perfil",
        crossorigin: "anonymous"
      }, null, 8, E)), _0x255ca7("div", null, [v_0x4393c77.value.deleted ? (_0x5bd96a(), _0x3a4257("span", {
        key: 0,
        class: _0x9b3f02(p29.$style.deleted)
      }, "Este comentÃ¡rio foi apagado.", 2)) : (_0x5bd96a(), _0x3a4257(_0x33e8e6, {
        key: 1
      }, [_0x255ca7("div", {
        class: _0x9b3f02(p29.$style.options),
        ref_key: "listaElemento",
        ref: v_0x4393c712
      }, [_0x255ca7("img", {
        src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='%23a9a9a9'%3e%3cpath%20d='M240-400q-33%200-56.5-23.5T160-480q0-33%2023.5-56.5T240-560q33%200%2056.5%2023.5T320-480q0%2033-23.5%2056.5T240-400Zm240%200q-33%200-56.5-23.5T400-480q0-33%2023.5-56.5T480-560q33%200%2056.5%2023.5T560-480q0%2033-23.5%2056.5T480-400Zm240%200q-33%200-56.5-23.5T640-480q0-33%2023.5-56.5T720-560q33%200%2056.5%2023.5T800-480q0%2033-23.5%2056.5T720-400Z'/%3e%3c/svg%3e",
        alt: "Options",
        onClick: f6
      }), v_0x4393c711.value ? (_0x5bd96a(), _0x3a4257("div", {
        key: 0,
        class: _0x9b3f02(p29.$style.menu)
      }, [v_0x4393c79.value ? (_0x5bd96a(), _0x3a4257("button", {
        key: 0,
        class: _0x9b3f02(p29.$style.non_delete),
        onClick: p30[0] ||= p31 => {
          v_0x4393c79.value = false;
          v_0x4393c711.value = false;
        }
      }, " Cancelar EdiÃ§Ã£o ", 2)) : (_0x5bd96a(), _0x3a4257(_0x33e8e6, {
        key: 1
      }, [v_0x4393c77.value.autor === _0x4344bc(_0x5efef5) ? (_0x5bd96a(), _0x3a4257("button", {
        key: 0,
        class: _0x9b3f02(p29.$style.non_delete),
        onClick: p30[1] ||= p32 => {
          v_0x4393c79.value = true;
          v_0x4393c711.value = false;
        }
      }, " Editar ComentÃ¡rio ", 2)) : _0x107b60("", true), v_0x4393c77.value.autor === _0x4344bc(_0x5efef5) || vP20.canDelete ? (_0x5bd96a(), _0x3a4257("button", {
        key: 1,
        onClick: p30[2] ||= p33 => {
          v22 = v_0x4393c77.value.id;
          _0x558732.delete("/api/comments/delete/" + v22 + "/", {
            withCredentials: true
          }).then(p34 => {
            if (p34.status === 204) {
              v_0x4393c711.value = false;
              v_0x4393c77.value.deleted = true;
            }
          });
          return;
          var v22;
        }
      }, " Excluir ComentÃ¡rio ")) : _0x107b60("", true)], 64))], 2)) : _0x107b60("", true)], 2), v_0x4393c79.value ? (_0x5bd96a(), _0x3a4257("span", {
        key: 0,
        class: _0x9b3f02(p29.$style.editing)
      }, [_0x2bf4c8(S, y(_0x21061d({
        recarregarFunction: vP20.recarregarFunction,
        pageId: 0,
        editingId: v_0x4393c77.value.id,
        editingMessage: v_0x4393c77.value.content,
        editingIsSpoiler: v_0x4393c77.value.spoiler
      })), null, 16)], 2)) : (_0x5bd96a(), _0x3a4257(_0x33e8e6, {
        key: 1
      }, [_0x255ca7("div", {
        class: _0x9b3f02(p29.$style.header)
      }, [_0x255ca7("span", null, _0x257e04(v_0x4393c77.value.autor), 1), vP20.reply ? (_0x5bd96a(), _0x3a4257("div", {
        key: 0,
        class: _0x9b3f02(p29.$style.reply)
      }, [p30[6] ||= _0x255ca7("img", {
        src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20fill='rgba(255,%20255,%20255,%200.3)'%20style='transform:scaleX(-1)%20scaleY(-1)'%20viewBox='0%200%20512%20512'%3e%3c!--!Font%20Awesome%20Free%206.6.0%20by%20@fontawesome%20-%20https://fontawesome.com%20License%20-%20https://fontawesome.com/license/free%20Copyright%202024%20Fonticons,%20Inc.--%3e%3cpath%20d='M205%2034.8c11.5%205.1%2019%2016.6%2019%2029.2l0%2064%20112%200c97.2%200%20176%2078.8%20176%20176c0%20113.3-81.5%20163.9-100.2%20174.1c-2.5%201.4-5.3%201.9-8.1%201.9c-10.9%200-19.7-8.9-19.7-19.7c0-7.5%204.3-14.4%209.8-19.5c9.4-8.8%2022.2-26.4%2022.2-56.7c0-53-43-96-96-96l-96%200%200%2064c0%2012.6-7.4%2024.1-19%2029.2s-25%203-34.4-5.4l-160-144C3.9%20225.7%200%20217.1%200%20208s3.9-17.7%2010.6-23.8l160-144c9.4-8.5%2022.9-10.6%2034.4-5.4z'/%3e%3c/svg%3e",
        alt: "Reply Seta"
      }, null, -1), _0x255ca7("span", null, _0x257e04(vP20.reply), 1)], 2)) : _0x107b60("", true)], 2), _0x255ca7("p", {
        class: _0x9b3f02(p29.$style.data)
      }, _0x257e04(_0x4344bc(_0x497ef1)(v_0x4393c77.value.data, true)), 3), v_0x4393c77.value.edited ? (_0x5bd96a(), _0x3a4257("p", {
        key: 0,
        class: _0x9b3f02(p29.$style.data)
      }, "(editado)", 2)) : _0x107b60("", true), _0x255ca7("div", {
        class: _0x9b3f02(p29.$style.content)
      }, [v_0x4393c78.value && v_0x4393c77.value.autor !== _0x4344bc(_0x5efef5) ? (_0x5bd96a(), _0x3a4257("div", {
        key: 0,
        class: _0x9b3f02(p29.$style.spoiler),
        onClick: p30[3] ||= p35 => v_0x4393c78.value = false
      }, p30[7] ||= [_0x255ca7("p", null, "Spoiler", -1)], 2)) : _0x107b60("", true), v_0x4393c77.value.content ? (_0x5bd96a(), _0x3a4257("p", H, _0x257e04(v_0x4393c77.value.content), 1)) : _0x107b60("", true)], 2), v_0x4393c77.value.image ? (_0x5bd96a(), _0x3a4257("img", {
        key: 1,
        src: v_0x4393c77.value.image,
        alt: "Imagem do comentÃ¡rio"
      }, null, 8, N)) : _0x107b60("", true), _0x255ca7("div", {
        class: _0x9b3f02(p29.$style.down_menu)
      }, [_0x255ca7("button", {
        onClick: p30[4] ||= p36 => f4(v_0x4393c77.value.id, "up", p36),
        class: _0x9b3f02(v_0x4393c77.value.up_likes.some(p37 => p37.name === _0x4344bc(_0x5efef5)) ? p29.$style.selected : null)
      }, [p30[8] ||= _0x255ca7("img", {
        src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='%23e8eaed'%3e%3cpath%20d='M720-120H280v-520l280-280%2050%2050q7%207%2011.5%2019t4.5%2023v14l-44%20174h258q32%200%2056%2024t24%2056v80q0%207-2%2015t-4%2015L794-168q-9%2020-30%2034t-44%2014Zm-360-80h360l120-280v-80H480l54-220-174%20174v406Zm0-406v406-406Zm-80-34v80H160v360h120v80H80v-520h200Z'/%3e%3c/svg%3e",
        alt: "Uplike"
      }, null, -1), _0x255ca7("span", null, _0x257e04(v_0x4393c77.value.up_likes.length), 1)], 2), _0x255ca7("button", {
        onClick: p30[5] ||= p38 => f4(v_0x4393c77.value.id, "down", p38),
        class: _0x9b3f02(v_0x4393c77.value.down_likes.some(p39 => p39.name === _0x4344bc(_0x5efef5)) ? p29.$style.selected : null)
      }, [p30[9] ||= _0x255ca7("img", {
        src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='%23e8eaed'%3e%3cpath%20d='M240-840h440v520L400-40l-50-50q-7-7-11.5-19t-4.5-23v-14l44-174H120q-32%200-56-24t-24-56v-80q0-7%202-15t4-15l120-282q9-20%2030-34t44-14Zm360%2080H240L120-480v80h360l-54%20220%20174-174v-406Zm0%20406v-406%20406Zm80%2034v-80h120v-360H680v-80h200v520H680Z'/%3e%3c/svg%3e",
        alt: "Downlike"
      }, null, -1), _0x255ca7("span", null, _0x257e04(v_0x4393c77.value.down_likes.length), 1)], 2), _0x255ca7("button", {
        style: {
          padding: "0"
        },
        onClick: f7
      }, p30[10] ||= [_0x255ca7("span", {
        style: {
          "font-weight": "bold"
        }
      }, "Responder", -1)])], 2), v_0x4393c710.value ? (_0x5bd96a(), _0x5b6e62(S, y(_0x13fb99({
        key: 2
      }, {
        recarregarFunction: vP20.recarregarFunction,
        fatherId: v_0x4393c77.value.id,
        pageId: vP20.pageId,
        formViewRef: f7
      })), null, 16)) : _0x107b60("", true)], 64))], 64)), v_0x4393c77.value.children.length ? (_0x5bd96a(), _0x3a4257("div", {
        key: 2,
        class: _0x9b3f02([p29.$style.subtree, vP20.level > 1 ? p29.$style.limited : null])
      }, [(_0x5bd96a(true), _0x3a4257(_0x33e8e6, null, _0x2994f3(v_0x4393c77.value.children, p40 => {
        _0x5bd96a();
        return _0x5b6e62(v_0x140264, _0x13fb99({
          key: p40.id
        }, {
          ref_for: true
        }, {
          comment: p40,
          recarregarFunction: vP20.recarregarFunction,
          pageId: vP20.pageId,
          level: vP20.level + 1,
          canDelete: vP20.canDelete,
          reply: v_0x4393c77.value.autor
        }), null, 16);
      }), 128))], 2)) : _0x107b60("", true)])], 10, R);
    };
  }
}, [["__cssModules", {
  $style: {
    comment: "n-4Dc",
    deleted: "_7T9wb",
    options: "Rrmel",
    menu: "JLOLW",
    non_delete: "CCjNz",
    editing: "OC5TD",
    header: "Mzvj-",
    reply: "TV47-",
    data: "gXC2l",
    content: "_5RS--",
    spoiler: "_66NNw",
    down_menu: "TK2cM",
    subtree: "xn63f",
    limited: "GxGiL",
    selected: "oBlTh"
  }
}], ["__scopeId", "data-v-7a061318"]]);
const D = {
  key: 1
};
const z = {
  key: 0,
  src: "data:image/svg+xml,%3csvg%20style='fill:darkgrey;'%20xmlns='http://www.w3.org/2000/svg'%20viewBox='0%200%20448%20512'%3e%3c!--!Font%20Awesome%20Free%206.6.0%20by%20@fontawesome%20-%20https://fontawesome.com%20License%20-%20https://fontawesome.com/license/free%20Copyright%202024%20Fonticons,%20Inc.--%3e%3cpath%20d='M224%200c-17.7%200-32%2014.3-32%2032l0%2019.2C119%2066%2064%20130.6%2064%20208l0%2018.8c0%2047-17.3%2092.4-48.5%20127.6l-7.4%208.3c-8.4%209.4-10.4%2022.9-5.3%2034.4S19.4%20416%2032%20416l384%200c12.6%200%2024-7.4%2029.2-18.9s3.1-25-5.3-34.4l-7.4-8.3C401.3%20319.2%20384%20273.9%20384%20226.8l0-18.8c0-77.4-55-142-128-156.8L256%2032c0-17.7-14.3-32-32-32zm45.3%20493.3c12-12%2018.7-28.3%2018.7-45.3l-64%200-64%200c0%2017%206.7%2033.3%2018.7%2045.3s28.3%2018.7%2045.3%2018.7s33.3-6.7%2045.3-18.7z'/%3e%3c/svg%3e",
  alt: "NotificaÃ§Ãµes"
};
const j = {
  key: 1,
  src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='%23f70'%3e%3cpath%20d='M160-200v-80h80v-280q0-83%2050-147.5T420-792v-28q0-25%2017.5-42.5T480-880q25%200%2042.5%2017.5T540-820v28q80%2020%20130%2084.5T720-560v280h80v80H160ZM480-80q-33%200-56.5-23.5T400-160h160q0%2033-23.5%2056.5T480-80ZM80-560q0-100%2044.5-183.5T244-882l47%2064q-60%2044-95.5%20111T160-560H80Zm720%200q0-80-35.5-147T669-818l47-64q75%2055%20119.5%20138.5T880-560h-80Z'/%3e%3c/svg%3e",
  alt: "NotificaÃ§Ãµes"
};
const X = ["disabled"];
const A = ["disabled"];
const O = ["href"];
const U = {
  key: 0,
  src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='%23a9a9a9'%3e%3cpath%20d='M120-560h40q17%200%2028.5%2011.5T200-520q0%2017-11.5%2028.5T160-480h-40q-17%200-28.5-11.5T80-520q0-17%2011.5-28.5T120-560Zm68%20216%2028-28q12-12%2028-11.5t28%2011.5q12%2012%2012.5%2028.5T273-315l-28%2028q-12%2012-28.5%2011.5T188-288q-11-12-11.5-28t11.5-28Zm28-324-28-28q-12-12-11.5-28t11.5-28q12-12%2028.5-12.5T245-753l28%2028q12%2012%2011.5%2028.5T272-668q-12%2011-28%2011.5T216-668Zm476%20480L530-350l-30%2090q-2%207-7.5%2010.5T481-246q-6%200-11.5-4t-7.5-11l-86-286q-2-8%20.5-16t7.5-13q5-5%2013-7.5t16-.5l288%2086q7%202%2010.5%207.5T715-479q0%206-3%2011.5t-10%207.5l-90%2032%20160%20160q12%2012%2012%2028t-12%2028l-24%2024q-12%2012-28%2012t-28-12ZM400-760v-40q0-17%2011.5-28.5T440-840q17%200%2028.5%2011.5T480-800v40q0%2017-11.5%2028.5T440-720q-17%200-28.5-11.5T400-760Zm207%2035%2029-29q11-11%2027.5-11.5T692-754q11%2011%2011.5%2027.5T693-698l-29%2030q-11%2012-27.5%2011.5T608-668q-12-12-12.5-28.5T607-725Z'/%3e%3c/svg%3e",
  alt: "Link"
};
const V = _0x301feb({
  __name: "comentariosComponent",
  props: {
    pageId: {
      type: Number,
      required: true
    },
    canLoad: {
      type: [Object, null],
      required: false,
      default: null
    }
  },
  setup(p41) {
    function f8() {
      v_0x4393c716.value = false;
      v_0x4393c715.value = true;
      v_0x4393c713.value = [];
      fetch("/api/comments/" + vP41.pageId + "/", {
        credentials: "same-origin"
      }).then(p42 => p42.json()).then(p43 => {
        v_0x4393c715.value = false;
        v_0x4393c717.value = true;
        v_0x4393c713.value = p43.comentarios;
        v_0x4393c718.value = p43.can_delete;
        v_0x4393c714.value = p43.quantidade;
        v_0x4393c719.value = p43.num_notifications;
        f10();
        (async function () {
          await _0x245777();
          if (v_0xc668d3.hash) {
            const v23 = document.getElementById(v_0xc668d3.hash.slice(1));
            if (v23) {
              v23.scrollIntoView({
                behavior: "smooth"
              });
            }
          }
        })();
      });
    }
    function f9(p44, p45) {
      if (p44 === "recente") {
        p45 = p45.sort((p46, p47) => new Date(p47.data) - new Date(p46.data));
      } else if (p44 === "relevante") {
        p45 = p45.sort((p48, p49) => {
          const v24 = p48.deleted ? -1 : p48.up_likes.length - p48.down_likes.length;
          return (p49.deleted ? -1 : p49.up_likes.length - p49.down_likes.length) - v24;
        });
      } else if (p44 === "antigo") {
        p45 = p45.sort((p50, p51) => new Date(p50.data) - new Date(p51.data));
      }
      p45.forEach(p52 => {
        if (p52.children.length) {
          p52.children = f9(p44, p52.children);
        }
      });
      return p45;
    }
    async function f10(p53 = "") {
      let v25 = v_0x4393c713.value;
      for (let v26 in v_0x4393c725.value) {
        v_0x4393c725.value[v26] = false;
      }
      if (p53) {
        localStorage.setItem("orderCommentType", p53);
      } else {
        p53 = localStorage.getItem("orderCommentType") ?? "relevante";
      }
      v25 = f9(p53, v25);
      if (p53 === "recente") {
        v_0x4393c725.value.recentes = true;
      } else if (p53 === "relevante") {
        v_0x4393c725.value.relevantes = true;
      } else if (p53 === "antigo") {
        v_0x4393c725.value.antigos = true;
      }
      v_0x4393c713.value = v25;
    }
    function f11() {
      _0x558732.get("/api/notifications/comment/", {
        withCredentials: true,
        responseType: "json"
      }).then(p54 => {
        v_0x4393c721.value = p54.data.notifications;
        v_0x4393c720.value = true;
        document.addEventListener("click", f14);
      });
    }
    function f12() {
      _0x558732.post("/api/notifications/view-all/", {}, {
        withCredentials: true,
        headers: {
          "X-CSRFToken": _0x2dfab7("csrftoken")
        }
      }).then(p55 => {
        if (p55.status === 204) {
          v_0x4393c721.value.forEach(p56 => {
            p56.viewed = true;
          });
        }
      }).catch(() => {
        window.alert("Algo deu errado ao visualizar todas as notificaÃ§Ãµes, entre em contato com nosso suporte.");
      });
    }
    function f13() {
      if (window.confirm("VocÃª tem certeza que deseja deletar todas as notificaÃ§Ãµes?")) {
        _0x558732.delete("/api/notifications/delete-all/", {
          withCredentials: true,
          headers: {
            "X-CSRFToken": _0x2dfab7("csrftoken")
          }
        }).then(p57 => {
          if (p57.status === 204) {
            v_0x4393c721.value = [];
          }
        }).catch(() => {
          window.alert("Algo deu errado ao deletar as notificaÃ§Ãµes, entre em contato com nosso suporte.");
        });
      }
    }
    function f14(p58) {
      if (!p58.composedPath().includes(v_0x4393c722.value)) {
        v_0x4393c720.value = false;
        document.removeEventListener("click", f14);
      }
    }
    const vP41 = p41;
    const v_0xc668d3 = _0xc668d3();
    const v_0x4393c713 = _0x4393c7(null);
    const v_0x4393c714 = _0x4393c7(0);
    const v_0x4393c715 = _0x4393c7(false);
    const v_0x4393c716 = _0x4393c7(true);
    const v_0x4393c717 = _0x4393c7(false);
    const v_0x4393c718 = _0x4393c7(false);
    const v_0x4393c719 = _0x4393c7(0);
    const v_0x4393c720 = _0x4393c7(false);
    const v_0x4393c721 = _0x4393c7([]);
    const v_0x4393c722 = _0x4393c7(null);
    const v_0x4393c723 = _0x4393c7(null);
    const v_0x4393c724 = _0x4393c7(true);
    const v_0x4393c725 = _0x4393c7({
      recentes: true,
      relevantes: false,
      antigos: false
    });
    _0x3337de(() => {
      f8();
    });
    _0x333b2b(() => {
      document.removeEventListener("click", f14);
    });
    return (p59, p60) => {
      _0x5bd96a();
      return _0x3a4257("section", {
        ref_key: "commentElement",
        ref: v_0x4393c723,
        class: _0x9b3f02(p59.$style.comentarios)
      }, [v_0x4393c715.value ? (_0x5bd96a(), _0x5b6e62(_0x276817, {
        key: 0
      })) : _0x107b60("", true), v_0x4393c717.value ? (_0x5bd96a(), _0x3a4257("div", D, [_0x255ca7("div", {
        class: _0x9b3f02(p59.$style.over_form),
        ref_key: "notificationsElement",
        ref: v_0x4393c722
      }, [_0x255ca7("h3", null, _0x257e04(v_0x4393c714.value) + " ComentÃ¡rios ", 1), _0x255ca7("div", {
        class: _0x9b3f02(p59.$style.notification),
        onClick: f11
      }, [v_0x4393c719.value === 0 ? (_0x5bd96a(), _0x3a4257("img", z)) : (_0x5bd96a(), _0x3a4257("img", j)), _0x255ca7("span", null, _0x257e04(v_0x4393c719.value), 1)], 2), v_0x4393c720.value ? (_0x5bd96a(), _0x3a4257("div", {
        key: 0,
        class: _0x9b3f02(p59.$style.list_notifications)
      }, [_0x255ca7("div", null, [_0x255ca7("button", {
        onClick: f13,
        disabled: !v_0x4393c721.value.length
      }, " Apagar Todas ", 8, X), _0x255ca7("button", {
        onClick: f12,
        disabled: !v_0x4393c721.value.some(p61 => p61.viewed === false)
      }, " Marcar todas como Lido ", 8, A)]), p60[4] ||= _0x255ca7("hr", null, null, -1), (_0x5bd96a(true), _0x3a4257(_0x33e8e6, null, _0x2994f3(v_0x4393c721.value, p62 => {
        _0x5bd96a();
        return _0x3a4257("a", {
          href: "/api/notifications/read/" + p62.id + "/",
          key: p62.id
        }, [p60[3] ||= _0x255ca7("img", {
          src: "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20height='24px'%20viewBox='0%20-960%20960%20960'%20width='24px'%20fill='%23a9a9a9'%3e%3cpath%20d='M280-400h400q17%200%2028.5-11.5T720-440q0-17-11.5-28.5T680-480H280q-17%200-28.5%2011.5T240-440q0%2017%2011.5%2028.5T280-400Zm0-120h400q17%200%2028.5-11.5T720-560q0-17-11.5-28.5T680-600H280q-17%200-28.5%2011.5T240-560q0%2017%2011.5%2028.5T280-520Zm0-120h400q17%200%2028.5-11.5T720-680q0-17-11.5-28.5T680-720H280q-17%200-28.5%2011.5T240-680q0%2017%2011.5%2028.5T280-640ZM160-240q-33%200-56.5-23.5T80-320v-480q0-33%2023.5-56.5T160-880h640q33%200%2056.5%2023.5T880-800v623q0%2027-24.5%2037.5T812-148l-92-92H160Zm594-80%2046%2045v-525H160v480h594Zm-594%200v-480%20480Z'/%3e%3c/svg%3e",
          alt: "Comentario"
        }, null, -1), _0x255ca7("span", {
          class: _0x9b3f02(p62.viewed ? p59.$style.viewed : null)
        }, _0x257e04(p62.content), 3), p62.viewed ? _0x107b60("", true) : (_0x5bd96a(), _0x3a4257("img", U))], 8, O);
      }), 128))], 2)) : _0x107b60("", true)], 2), p60[5] ||= _0x255ca7("hr", null, null, -1), _0x255ca7("div", null, [v_0x4393c724.value ? (_0x5bd96a(), _0x5b6e62(S, y(_0x13fb99({
        key: 0
      }, {
        recarregarFunction: f8,
        pageId: vP41.pageId
      })), null, 16)) : _0x107b60("", true)]), _0x255ca7("div", {
        class: _0x9b3f02(p59.$style.order)
      }, [_0x255ca7("span", {
        id: "order-recent",
        class: _0x9b3f02(v_0x4393c725.value.recentes ? p59.$style.select : null),
        onClick: p60[0] ||= p63 => f10("recente")
      }, "Mais Recentes", 2), _0x255ca7("span", {
        id: "order-relevant",
        class: _0x9b3f02(v_0x4393c725.value.relevantes ? p59.$style.select : null),
        onClick: p60[1] ||= p64 => f10("relevante")
      }, "Mais Relevantes", 2), _0x255ca7("span", {
        id: "order-old",
        class: _0x9b3f02(v_0x4393c725.value.antigos ? p59.$style.select : null),
        onClick: p60[2] ||= p65 => f10("antigo")
      }, "Mais Antigos", 2)], 2), _0x255ca7("div", {
        class: _0x9b3f02(p59.$style.comments)
      }, [(_0x5bd96a(true), _0x3a4257(_0x33e8e6, null, _0x2994f3(v_0x4393c713.value, p66 => {
        _0x5bd96a();
        return _0x5b6e62(B, _0x13fb99({
          key: p66.id
        }, {
          ref_for: true
        }, {
          comment: p66,
          recarregarFunction: f8,
          pageId: vP41.pageId,
          canDelete: v_0x4393c718.value,
          level: 0
        }), null, 16);
      }), 128))], 2)])) : _0x107b60("", true)], 2);
    };
  }
}, [["__cssModules", {
  $style: {
    comentarios: "DpM21",
    over_form: "X9Cpk",
    notification: "oB3Kq",
    list_notifications: "y2-Gc",
    viewed: "_1gERn",
    order: "Q7-WL",
    select: "_7x4SR",
    comments: "s8zxC"
  }
}], ["__scopeId", "data-v-ed9c6629"]]);
export { V as C };