(function() {
    if (window.__proofCaptured) return;

    try {
        Object.defineProperty(navigator, 'webdriver', { get: () => undefined, configurable: true });
        if (!window.chrome) window.chrome = {};
        window.chrome.runtime = { id: undefined };
        Object.defineProperty(document, 'hidden', { get: () => false });
        Object.defineProperty(document, 'visibilityState', { get: () => 'visible' });
    } catch(e) {}

    function extract(data) {
        if (!data) return null;
        var p, c;
        if (typeof data === 'object') {
            p = data.proof;
            c = data.challenge;
        } else if (typeof data === 'string') {
            var pm = data.match(/proof=([^&]+)/);
            var cm = data.match(/challenge=([^&]+)/);
            if (pm && cm) {
                p = decodeURIComponent(pm[1]);
                c = decodeURIComponent(cm[1]);
            }
        }
        return (p && c) ? { proof: String(p), challenge: String(c) } : null;
    }

    function report(result) {
        if (result && window.Interceptor && !window.__proofCaptured) {
            window.__proofCaptured = true;
            window.Interceptor.onProof(result.proof, result.challenge);
        }
    }

    function hookAjax(jq) {
        if (!jq || !jq.ajax || jq.__sakuraHooked) return jq;
        var orig = jq.ajax;
        jq.ajax = function(url, opts) {
            var o = typeof url === 'object' ? url : (opts || {});
            if (typeof url !== 'object') o.url = url;
            var method = (o.type || o.method || 'GET').toUpperCase();
            if (method === 'POST' && o.data) {
                var dataStr = typeof o.data === 'string' ? o.data : (jq.param ? jq.param(o.data) : JSON.stringify(o.data));
                if (dataStr.indexOf('proof') !== -1) {
                    var r = extract(o.data) || extract(dataStr);
                    if (r) report(r);
                }
            }
            return orig.apply(this, arguments);
        };
        jq.__sakuraHooked = true;
        return jq;
    }

    var _jq = window.$;
    var _jQuery = window.jQuery;
    if (_jq && _jq.ajax) hookAjax(_jq);
    if (_jQuery && _jQuery.ajax && _jQuery !== _jq) hookAjax(_jQuery);

    try {
        Object.defineProperty(window, '$', {
            configurable: true, enumerable: true,
            get: function() { return _jq; },
            set: function(v) { _jq = hookAjax(v) || v; }
        });
        Object.defineProperty(window, 'jQuery', {
            configurable: true, enumerable: true,
            get: function() { return _jQuery; },
            set: function(v) { _jQuery = hookAjax(v) || v; if (!_jq) _jq = _jQuery; }
        });
    } catch(e) {}
})();
