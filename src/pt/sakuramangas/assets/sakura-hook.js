(function() {
    'use strict';

    if (window.__SAKURA_STEALTH_V3__) return;
    window.__SAKURA_STEALTH_V3__ = true;

    var nativeToString = Function.prototype.toString;

    function makeNative(func, originalFunc) {
        Object.defineProperty(func, 'toString', {
            value: function() {
                return originalFunc ? nativeToString.call(originalFunc) : 'function () { [native code] }';
            },
            writable: false,
            enumerable: false,
            configurable: true
        });
        return func;
    }

    function sendPayload(type, data) {
        console.log('__SAKURA_STEALTH__:' + JSON.stringify({ type: type, payload: data }));
    }

    function parseBody(body) {
        if (!body) return '';
        if (typeof body === 'string') return body;
        if (body instanceof FormData) {
            var arr = [];
            body.forEach(function(v, k) { arr.push(encodeURIComponent(k) + '=' + encodeURIComponent(v)); });
            return arr.join('&');
        }
        if (body instanceof URLSearchParams) return body.toString();
        return '';
    }

    var captured = false;

    function hookContext(win) {
        if (!win || win.__NET_HOOKED__) return;

        try {
            var realFetch = win.fetch;
            if (realFetch) {
                win.fetch = makeNative(function(input, init) {
                    var url = (typeof input === 'string') ? input : (input && input.url ? input.url : '');
                    var isMangaInfo = url.indexOf('manga_info') !== -1;

                    if (isMangaInfo && !captured && init && init.method && init.method.toUpperCase() === 'POST' && init.body) {
                        captured = true;
                        sendPayload('MANGA_INFO_REQUEST', { body: parseBody(init.body) });
                    }

                    return realFetch.apply(this, arguments).then(function(response) {
                        if (isMangaInfo) {
                            response.clone().text().then(function(text) {
                                try {
                                    var data = JSON.parse(text);
                                    if (data && data.titulo) sendPayload('MANGA_INFO_RESPONSE', { data: data });
                                } catch (e) {}
                            }).catch(function() {});
                        }
                        return response;
                    });
                }, realFetch);
            }

            var XHR = win.XMLHttpRequest.prototype;
            var realOpen = XHR.open;
            var realSend = XHR.send;

            XHR.open = makeNative(function(method, url) {
                this._url = url;
                this._method = method;
                return realOpen.apply(this, arguments);
            }, realOpen);

            XHR.send = makeNative(function(body) {
                var url = this._url || '';
                var method = this._method || '';
                var isMangaInfo = url.indexOf('manga_info') !== -1;

                if (isMangaInfo && !captured && method.toUpperCase() === 'POST' && body) {
                    captured = true;
                    sendPayload('MANGA_INFO_REQUEST', { body: parseBody(body) });
                }

                if (isMangaInfo) {
                    var xhr = this;
                    xhr.addEventListener('load', function() {
                        try {
                            var data = JSON.parse(xhr.responseText);
                            if (data && data.titulo) sendPayload('MANGA_INFO_RESPONSE', { data: data });
                        } catch (e) {}
                    });
                }

                return realSend.apply(this, arguments);
            }, realSend);

            win.__NET_HOOKED__ = true;
        } catch (e) {}
    }

    try {
        var originalAttachShadow = Element.prototype.attachShadow;
        Element.prototype.attachShadow = makeNative(function(init) {
            var shadowRoot = originalAttachShadow.call(this, init);
            new MutationObserver(function(mutations) {
                mutations.forEach(function(m) {
                    m.addedNodes.forEach(function(node) {
                        if (node.tagName === 'IFRAME') {
                            try { hookContext(node.contentWindow); } catch(e) {}
                            node.addEventListener('load', function() { try { hookContext(node.contentWindow); } catch(e) {} });
                        }
                    });
                });
            }).observe(shadowRoot, { childList: true, subtree: true });
            return shadowRoot;
        }, originalAttachShadow);
    } catch (e) {}

    try {
        new MutationObserver(function(mutations) {
            mutations.forEach(function(m) {
                m.addedNodes.forEach(function(node) {
                    if (node.tagName === 'IFRAME') {
                        try { hookContext(node.contentWindow); } catch(e) {}
                        node.addEventListener('load', function() { try { hookContext(node.contentWindow); } catch(e) {} });
                    }
                });
            });
        }).observe(document.documentElement, { childList: true, subtree: true });
    } catch (e) {}

    try {
        var iframeProto = HTMLIFrameElement.prototype;
        var desc = Object.getOwnPropertyDescriptor(iframeProto, 'contentWindow');
        if (desc && desc.get) {
            var getter = desc.get;
            Object.defineProperty(iframeProto, 'contentWindow', {
                get: makeNative(function() {
                    var cw = getter.call(this);
                    if (cw && !cw.__NET_HOOKED__) try { hookContext(cw); } catch(e) {}
                    return cw;
                }, getter),
                configurable: true
            });
        }
    } catch (e) {}

    hookContext(window);
    sendPayload('HOOK_READY', { version: 'v3' });
})();
