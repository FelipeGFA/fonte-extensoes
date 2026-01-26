(function () {
  if (window.__kuroHooked) return;
  window.__kuroHooked = true;

  var I = window.__IFACE_PLACEHOLDER__;
  if (!I) return;

  try {
    delete window.__IFACE_PLACEHOLDER__;
    Object.defineProperty(window, "__IFACE_PLACEHOLDER__", {
      value: I,
      writable: false,
      enumerable: false,
      configurable: false,
    });
  } catch (e) { }

  var sentKeys = {};
  var P = window.__kuroExpectPages || false;

  function sendData(data) {
    try {
      if (!data || typeof data !== "object") return false;

      var hp = Object.prototype.hasOwnProperty;
      if (hp.call(data, "_v_secure")) return false;

      var hasToken = hp.call(data, "token") && typeof data.token === "string" && data.token.length > 20;
      var hasPages = hp.call(data, "pages") && Array.isArray(data.pages) && data.pages.length > 0;
      var hasData = hp.call(data, "data") && Array.isArray(data.data) && data.data.length > 0;
      var hasChapters = hp.call(data, "chapters") && Array.isArray(data.chapters);
      var hasManga = hp.call(data, "manga") && data.manga && data.manga.id;
      var hasUser = hp.call(data, "user") && data.user && data.user.id;

      if (P && hasPages) {
        var jsonStr = JSON.stringify(data);
        var key = "pages-" + data.pages.length;
        if (!sentKeys[key]) {
          sentKeys[key] = true;
          I.onDataParsed(jsonStr);
          return true;
        }
        return false;
      }

      if (hasToken || hasPages || hasData || hasChapters || hasManga || hasUser) {
        var jsonStr = JSON.stringify(data);
        if (jsonStr.length < 50) return false;

        var key = jsonStr.substring(0, 200);
        if (sentKeys[key]) return false;
        sentKeys[key] = true;

        I.onDataParsed(jsonStr);
        return true;
      }
    } catch (e) { }
    return false;
  }

  try {
    var origThen = Promise.prototype.then;
    Promise.prototype.then = function (onFulfilled, onRejected) {
      var wrappedOnFulfilled = onFulfilled;

      if (typeof onFulfilled === 'function') {
        wrappedOnFulfilled = function (value) {
          if (value && typeof value === 'object') {
            if (P) {
              if (value.pages && Array.isArray(value.pages) && value.pages.length > 0) {
                sendData(value);
              } else if (value.data && typeof value.data === 'object' && value.status && value.headers) {
                var d = value.data;
                if (d && d.pages && Array.isArray(d.pages) && d.pages.length > 0) {
                  sendData(d);
                }
              }
            } else {
              if (value.data && Array.isArray(value.data) && value.pagination) {
                sendData(value);
              } else if (value.manga && value.manga.id) {
                sendData(value);
              } else if (value.chapters && Array.isArray(value.chapters)) {
                sendData(value);
              } else if (value.pages && Array.isArray(value.pages)) {
                sendData(value);
              } else if (value.token && typeof value.token === 'string') {
                sendData(value);
              } else if (value.user && value.user.id) {
                sendData(value);
              } else if (value.data && typeof value.data === 'object' && value.status && value.headers) {
                var d = value.data;
                if (d && !d._v_secure) {
                  if ((d.data && d.pagination) || d.manga || d.chapters || d.pages || d.token || d.user) {
                    sendData(d);
                  }
                }
              }
            }
          }
          return onFulfilled(value);
        };
      }

      return origThen.call(this, wrappedOnFulfilled, onRejected);
    };
  } catch (e) { }

  try {
    var originalSetItem = localStorage.setItem;
    localStorage.setItem = function (key, value) {
      if (key === "token" && value) {
        try {
          I.onDataParsed(JSON.stringify({ token: value }));
        } catch (e) { }
      }
      return originalSetItem.apply(this, arguments);
    };
  } catch (e) { }
})();
