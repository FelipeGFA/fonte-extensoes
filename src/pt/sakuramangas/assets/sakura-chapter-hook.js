(function() {
    'use strict';

    if (window.__SAKURA_CHAPTER_V2__) return;
    window.__SAKURA_CHAPTER_V2__ = true;

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

    var capturedData = { imageHash: null, imageExtension: null, numPages: null };
    var dataSent = false;

    function tryNotify() {
        if (capturedData.imageHash && capturedData.numPages && !dataSent) {
            dataSent = true;
            sendPayload('CHAPTER_DATA', {
                hash: capturedData.imageHash,
                extension: capturedData.imageExtension || 'jpg',
                numPages: capturedData.numPages
            });
        }
    }

    function extractImageHash(url) {
        if (!url) return null;
        var match = url.match(/\/imagens\/([a-f0-9]{32,})\/\d{3}\.(jpg|png|webp|gif)/i);
        return match ? { hash: match[1], extension: match[2].toLowerCase() } : null;
    }

    function extractNumPages(data) {
        if (!data) return null;
        if (typeof data === 'string') {
            try { data = JSON.parse(data); } catch (e) { return null; }
        }
        return (typeof data.numPages === 'number' && data.numPages > 0) ? data.numPages : null;
    }

    function hookContext(win) {
        if (!win || win.__CHAPTER_NET_HOOKED__) return;

        try {
            var realFetch = win.fetch;
            if (realFetch) {
                win.fetch = makeNative(function(input, init) {
                    var url = (typeof input === 'string') ? input : (input && input.url ? input.url : '');

                    if (!capturedData.imageHash) {
                        var img = extractImageHash(url);
                        if (img) { capturedData.imageHash = img.hash; capturedData.imageExtension = img.extension; tryNotify(); }
                    }

                    var isChapterRead = url.indexOf('capitulos__read') !== -1 || url.indexOf('capitulo') !== -1;

                    return realFetch.apply(this, arguments).then(function(response) {
                        if (isChapterRead && !capturedData.numPages) {
                            response.clone().text().then(function(text) {
                                var pages = extractNumPages(text);
                                if (pages) { capturedData.numPages = pages; tryNotify(); }
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
                if (!capturedData.imageHash) {
                    var img = extractImageHash(url);
                    if (img) { capturedData.imageHash = img.hash; capturedData.imageExtension = img.extension; tryNotify(); }
                }
                return realOpen.apply(this, arguments);
            }, realOpen);

            XHR.send = makeNative(function() {
                var xhr = this;
                var url = this._url || '';
                var isChapterRead = url.indexOf('capitulos__read') !== -1 || url.indexOf('capitulo') !== -1;

                if (isChapterRead && !capturedData.numPages) {
                    xhr.addEventListener('load', function() {
                        try {
                            var pages = extractNumPages(xhr.responseText);
                            if (pages) { capturedData.numPages = pages; tryNotify(); }
                        } catch (e) {}
                    });
                }

                return realSend.apply(this, arguments);
            }, realSend);

            var originalJSONParse = win.JSON.parse;
            win.JSON.parse = makeNative(function(text) {
                var result = originalJSONParse.apply(this, arguments);
                try {
                    if (result && typeof result === 'object' && !capturedData.numPages) {
                        var pages = extractNumPages(result);
                        if (pages) { capturedData.numPages = pages; tryNotify(); }
                    }
                } catch (e) {}
                return result;
            }, originalJSONParse);

            win.__CHAPTER_NET_HOOKED__ = true;
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
                    if (cw && !cw.__CHAPTER_NET_HOOKED__) try { hookContext(cw); } catch(e) {}
                    return cw;
                }, getter),
                configurable: true
            });
        }
    } catch (e) {}

    hookContext(window);
    sendPayload('CHAPTER_HOOK_READY', { version: 'v2' });
})();
