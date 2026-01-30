import { _ as _0xb1ec90, r as _0x512cbb, l as _0x1dfafc, c as _0x15860d, e as _0x1b0b76, a as _0x5ea1b3, H as _0x348f64, b as _0xf0c3d1, d as _0x21f106, n as _0xf1be04, t as _0x4de34c, B as _0x4cc958, N as _0x1ef1f9, j as _0x9c5cd2, F as _0x8b6a8a, E as _0x7b0c4, o as _0x1a400a, L as _0x223e96 } from "./CqksSKFa.js";
import { r as _0x5792d2 } from "./Ca5sk1_d.js";
const g = "data:image/svg+xml,%3csvg%20xmlns='http://www.w3.org/2000/svg'%20fill='%23f70'%20height='16'%20width='16'%20viewBox='0%200%20512%20512'%3e%3c!--!Font%20Awesome%20Free%206.7.1%20by%20@fontawesome%20-%20https://fontawesome.com%20License%20-%20https://fontawesome.com/license/free%20Copyright%202024%20Fonticons,%20Inc.--%3e%3cpath%20d='M471.6%2021.7c-21.9-21.9-57.3-21.9-79.2%200L362.3%2051.7l97.9%2097.9%2030.1-30.1c21.9-21.9%2021.9-57.3%200-79.2L471.6%2021.7zm-299.2%20220c-6.1%206.1-10.8%2013.6-13.5%2021.9l-29.6%2088.8c-2.9%208.6-.6%2018.1%205.8%2024.6s15.9%208.7%2024.6%205.8l88.8-29.6c8.2-2.7%2015.7-7.4%2021.9-13.5L437.7%20172.3%20339.7%2074.3%20172.4%20241.7zM96%2064C43%2064%200%20107%200%20160L0%20416c0%2053%2043%2096%2096%2096l256%200c53%200%2096-43%2096-96l0-96c0-17.7-14.3-32-32-32s-32%2014.3-32%2032l0%2096c0%2017.7-14.3%2032-32%2032L96%20448c-17.7%200-32-14.3-32-32l0-256c0-17.7%2014.3-32%2032-32l96%200c17.7%200%2032-14.3%2032-32s-14.3-32-32-32L96%2064z'/%3e%3c/svg%3e";
const h = {
  key: 1
};
const w = {
  key: 0
};
const C = {
  key: 0
};
const N = {
  key: 0
};
const L = {
  key: 1
};
const b = {
  key: 2
};
const F = {
  key: 1
};
const V = {
  key: 2
};
const P = {
  key: 3
};
const S = _0xb1ec90({
  __name: "config",
  setup(p5) {
    async function f() {
      if ((await _0x5792d2()) === 201) {
        v_0x512cbb3.value = Notification.permission === "granted";
        v_0x512cbb.value.allow_notification = true;
      }
    }
    function f2() {
      v_0x512cbb.value.notification_obra = vA2[0].value;
      v_0x512cbb.value.notification_comentario = vA2[1].value;
      v_0x512cbb.value.notification_curtida = vA2[2].value;
      let vO = {};
      if (vA2[0].value !== null) {
        vO.obra = vA2[0].value;
      }
      if (vA2[1].value !== null) {
        vO.comentario = vA2[1].value;
      }
      if (vA2[2].value !== null) {
        vO.curtida = vA2[2].value;
      }
      _0x1dfafc.post("/api/config/edit/", vO, {
        headers: {
          "X-CSRFToken": _0x7b0c4("csrftoken")
        }
      }).then(p6 => {
        window.alert("ConfiguraÃ§Ãµes de notificaÃ§Ã£o alteradas com sucesso!");
        v_0x512cbb4.value = false;
      });
    }
    const v_0x512cbb = _0x512cbb({});
    const v_0x512cbb2 = _0x512cbb(true);
    const v_0x512cbb3 = _0x512cbb(Notification.permission === "granted");
    const v_0x512cbb4 = _0x512cbb(false);
    const vA2 = [_0x512cbb(null), _0x512cbb(null), _0x512cbb(null)];
    _0x1dfafc.get("/api/config/", {
      withCredentials: true,
      responseType: "json"
    }).then(p7 => {
      v_0x512cbb2.value = false;
      v_0x512cbb.value = p7.data;
      vA2[0].value = p7.data.notification_obra;
      vA2[1].value = p7.data.notification_comentario;
      vA2[2].value = p7.data.notification_curtida;
    });
    return (p8, p9) => {
      _0x1a400a();
      return _0x15860d(_0x8b6a8a, null, [_0x1b0b76(_0x348f64), _0x5ea1b3("main", null, [p9[24] ||= _0x5ea1b3("h1", null, "ConfiguraÃ§Ãµes Extras", -1), v_0x512cbb2.value ? (_0x1a400a(), _0xf0c3d1(_0x223e96, {
        key: 0
      })) : (_0x1a400a(), _0x15860d("section", h, [_0x5ea1b3("div", {
        class: _0xf1be04(p8.$style.item)
      }, [p9[8] ||= _0x5ea1b3("p", null, "VÃ­nculo Discord:", -1), v_0x512cbb.value.discord ? (_0x1a400a(), _0x15860d("p", w, _0x4de34c(v_0x512cbb.value.discord), 1)) : (_0x1a400a(), _0x15860d("a", {
        key: 1,
        class: _0xf1be04(p8.$style.link),
        href: "https://discord.com/oauth2/authorize?client_id=1250229289828487282&response_type=code&redirect_uri=https%3A%2F%2Fluratoons.net%2Fdiscord-api%2F&scope=identify+guilds.join"
      }, " Vincular e Resgatar Tag VIP ", 2))], 2), _0x21f106("", true), _0x5ea1b3("div", {
        class: _0xf1be04(p8.$style.item)
      }, [p9[10] ||= _0x5ea1b3("p", null, "VÃ­nculo MyAnimeList:", -1), v_0x512cbb.value.mal_auth ? (_0x1a400a(), _0x15860d("p", C, "Vinculado")) : (_0x1a400a(), _0x15860d("a", {
        key: 1,
        class: _0xf1be04(p8.$style.link),
        href: "/api/v2/auth-mal/"
      }, " Vincular e Sincronizar ", 2))], 2), p9[23] ||= _0x5ea1b3("hr", null, null, -1), _0x5ea1b3("div", {
        class: _0xf1be04(p8.$style.item)
      }, [p9[13] ||= _0x5ea1b3("p", null, "NotificaÃ§Ã£o Permida:", -1), v_0x512cbb.value.allow_notification && v_0x512cbb3.value ? (_0x1a400a(), _0x15860d("p", N, "Sim")) : v_0x512cbb.value.allow_notification ? (_0x1a400a(), _0x15860d("div", L, [p9[11] ||= _0x5ea1b3("p", null, "Sim, mas nÃ£o nesse dispositivo.", -1), _0x5ea1b3("button", {
        onClick: p9[0] ||= p10 => f()
      }, "Permitir neste dispositivo")])) : (_0x1a400a(), _0x15860d("div", b, [p9[12] ||= _0x5ea1b3("p", null, "NÃ£o permitido", -1), _0x5ea1b3("button", {
        onClick: p9[1] ||= p11 => f()
      }, "Permitir")]))], 2), v_0x512cbb.value.allow_notification && v_0x512cbb3.value ? (_0x1a400a(), _0x15860d("ul", F, [_0x5ea1b3("li", null, [_0x5ea1b3("div", null, [p9[16] ||= _0x5ea1b3("p", null, "Notificar lanÃ§amentos:", -1), v_0x512cbb4.value ? _0x4cc958((_0x1a400a(), _0x15860d("select", {
        key: 1,
        "onUpdate:modelValue": p9[3] ||= p12 => vA2[0].value = p12
      }, p9[15] ||= [_0x5ea1b3("option", {
        value: "1"
      }, "Sim", -1), _0x5ea1b3("option", {
        value: "0"
      }, "NÃ£o", -1)], 512)), [[_0x1ef1f9, vA2[0].value]]) : (_0x1a400a(), _0x15860d("div", {
        key: 0,
        onClick: p9[2] ||= p13 => v_0x512cbb4.value = !v_0x512cbb4.value
      }, [_0x5ea1b3("p", null, _0x4de34c(v_0x512cbb.value.notification_obra ? "Permitido" : "Negado"), 1), p9[14] ||= _0x5ea1b3("img", {
        src: g,
        alt: "Editar"
      }, null, -1)]))])])])) : _0x21f106("", true), v_0x512cbb.value.allow_notification && v_0x512cbb3.value ? (_0x1a400a(), _0x15860d("p", V, "ComentÃ¡rios:")) : _0x21f106("", true), v_0x512cbb.value.allow_notification && v_0x512cbb3.value ? (_0x1a400a(), _0x15860d("ul", P, [_0x5ea1b3("li", null, [_0x5ea1b3("div", null, [p9[19] ||= _0x5ea1b3("p", null, "Notificar respostas:", -1), v_0x512cbb4.value ? _0x4cc958((_0x1a400a(), _0x15860d("select", {
        key: 1,
        "onUpdate:modelValue": p9[5] ||= p14 => vA2[1].value = p14
      }, p9[18] ||= [_0x5ea1b3("option", {
        value: "1"
      }, "Sim", -1), _0x5ea1b3("option", {
        value: "0"
      }, "NÃ£o", -1)], 512)), [[_0x1ef1f9, vA2[1].value]]) : (_0x1a400a(), _0x15860d("div", {
        key: 0,
        onClick: p9[4] ||= p15 => v_0x512cbb4.value = !v_0x512cbb4.value
      }, [_0x5ea1b3("p", null, _0x4de34c(v_0x512cbb.value.notification_comentario ? "Permitido" : "Negado"), 1), p9[17] ||= _0x5ea1b3("img", {
        src: g,
        alt: "Editar"
      }, null, -1)]))])]), _0x5ea1b3("li", null, [_0x5ea1b3("div", null, [p9[22] ||= _0x5ea1b3("p", null, "Notificar Likes:", -1), v_0x512cbb4.value ? _0x4cc958((_0x1a400a(), _0x15860d("select", {
        key: 1,
        "onUpdate:modelValue": p9[7] ||= p16 => vA2[2].value = p16
      }, p9[21] ||= [_0x5ea1b3("option", {
        value: "1"
      }, "Sim", -1), _0x5ea1b3("option", {
        value: "0"
      }, "NÃ£o", -1)], 512)), [[_0x1ef1f9, vA2[2].value]]) : (_0x1a400a(), _0x15860d("div", {
        key: 0,
        onClick: p9[6] ||= p17 => v_0x512cbb4.value = !v_0x512cbb4.value
      }, [_0x5ea1b3("p", null, _0x4de34c(v_0x512cbb.value.notification_curtida ? "Permitido" : "Negado"), 1), p9[20] ||= _0x5ea1b3("img", {
        src: g,
        alt: "Editar"
      }, null, -1)]))])])])) : _0x21f106("", true), v_0x512cbb4.value ? (_0x1a400a(), _0x15860d("button", {
        key: 4,
        onClick: f2
      }, "Confirmar")) : _0x21f106("", true)]))]), _0x1b0b76(_0x9c5cd2)], 64);
    };
  }
}, [["__cssModules", {
  $style: {
    item: "_6hLmq",
    link: "IID0k"
  }
}], ["__scopeId", "data-v-5e088ca8"]]);
export { S as default };