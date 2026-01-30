import { _ as _0x5425a5, r as _0x1c4cc3, l as _0x1fdf9e, c as _0x538bad, e as _0x196894, a as _0x3020be, H as _0x319366, u as _0x2a9196, F as _0x183a5a, h as _0x18f015, j as _0xe331e6, J as _0x554825, o as _0x3d6029, g as _0x1300cf, t as _0x58b200, n as _0xe2bc94 } from "./CqksSKFa.js";
const k = {
  key: 0
};
const y = {
  key: 1
};
const b = ["src"];
const g = _0x5425a5({
  __name: "AllObras",
  setup(p5) {
    const v_0x1c4cc3 = _0x1c4cc3([]);
    const v14 = window.location.search;
    const v15 = new URLSearchParams(v14);
    _0x1fdf9e.get("/api/obras/", {
      params: v15,
      withCredentials: true,
      responseType: "json"
    }).then(p6 => {
      v_0x1c4cc3.value = p6.data.obras;
    });
    return (p7, p8) => {
      const v_0x554825 = _0x554825("router-link");
      _0x3d6029();
      return _0x538bad(_0x183a5a, null, [_0x196894(_0x319366), _0x3020be("main", null, [_0x2a9196(v15).size ? (_0x3d6029(), _0x538bad("h1", k, "Obras (Filtro Aplicado)")) : (_0x3d6029(), _0x538bad("h1", y, "Obras")), _0x3020be("section", null, [(_0x3d6029(true), _0x538bad(_0x183a5a, null, _0x18f015(v_0x1c4cc3.value, p9 => {
        _0x3d6029();
        return _0x538bad("div", {
          key: p9.id
        }, [_0x196894(v_0x554825, {
          to: "/" + p9.slug + "/",
          class: _0xe2bc94(p7.$style.link)
        }, {
          default: _0x1300cf(() => [_0x3020be("img", {
            src: p9.capa
          }, null, 8, b), _0x3020be("p", null, _0x58b200(p9.title), 1)]),
          _: 2
        }, 1032, ["to", "class"])]);
      }), 128))])]), _0x196894(_0xe331e6)], 64);
    };
  }
}, [["__cssModules", {
  $style: {
    link: "nEjte"
  }
}], ["__scopeId", "data-v-28a1c77e"]]);
export { g as default };