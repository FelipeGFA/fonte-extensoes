(function() {
    'use strict';

    if (window.__SAKURA_CHAPTER_HOOK_INSTALLED__) return;
    window.__SAKURA_CHAPTER_HOOK_INSTALLED__ = true;

    var capturedData = {
        imageHash: null,
        imageExtension: null,
        numPages: null
    };
    var dataSent = false;

    function tryNotifyAndroid() {
        if (capturedData.imageHash && capturedData.numPages && !dataSent) {
            dataSent = true;
            if (window.SakuraChapterInterface && typeof window.SakuraChapterInterface.onChapterData === 'function') {
                window.SakuraChapterInterface.onChapterData(
                    capturedData.imageHash,
                    capturedData.imageExtension || 'jpg',
                    capturedData.numPages
                );
            }
        }
    }

    function extractImageHashFromUrl(url) {
        if (!url || typeof url !== 'string') return null;
        var match = url.match(/\/imagens\/([a-f0-9]{32,})\/\d{3}\.(jpg|png|webp|gif)/i);
        if (match) {
            return { hash: match[1], extension: match[2].toLowerCase() };
        }
        return null;
    }

    function extractNumPagesFromResponse(data) {
        if (!data) return null;
        if (typeof data === 'string') {
            try {
                data = JSON.parse(data);
            } catch (e) {
                return null;
            }
        }
        if (typeof data.numPages === 'number' && data.numPages > 0) {
            return data.numPages;
        }
        return null;
    }

    function createChapterHooks(targetWindow, source) {
        if (targetWindow.__SAKURA_CHAPTER_HOOKED__) return;
        targetWindow.__SAKURA_CHAPTER_HOOKED__ = true;

        try {
            var originalJSONParse = targetWindow.JSON.parse;
            targetWindow.JSON.parse = function(text) {
                var result = originalJSONParse.apply(this, arguments);
                try {
                    if (result && typeof result === 'object') {
                        var numPages = extractNumPagesFromResponse(result);
                        if (numPages && !capturedData.numPages) {
                            capturedData.numPages = numPages;
                            tryNotifyAndroid();
                        }
                    }
                } catch (e) {}
                return result;
            };
        } catch (e) {}

        try {
            var originalFetch = targetWindow.fetch;
            if (originalFetch) {
                targetWindow.fetch = function(input, init) {
                    var url = typeof input === 'string' ? input : (input && input.url ? input.url : '');

                    if (!capturedData.imageHash) {
                        var imageData = extractImageHashFromUrl(url);
                        if (imageData) {
                            capturedData.imageHash = imageData.hash;
                            capturedData.imageExtension = imageData.extension;
                            tryNotifyAndroid();
                        }
                    }

                    var isChapterRead = url.indexOf('capitulos__read') !== -1 || url.indexOf('capitulo') !== -1;

                    return originalFetch.apply(this, arguments).then(function(response) {
                        if (isChapterRead && !capturedData.numPages) {
                            var clonedResponse = response.clone();
                            clonedResponse.text().then(function(text) {
                                var numPages = extractNumPagesFromResponse(text);
                                if (numPages) {
                                    capturedData.numPages = numPages;
                                    tryNotifyAndroid();
                                }
                            }).catch(function() {});
                        }
                        return response;
                    });
                };
            }
        } catch (e) {}

        try {
            var originalXHROpen = targetWindow.XMLHttpRequest.prototype.open;
            var originalXHRSend = targetWindow.XMLHttpRequest.prototype.send;

            targetWindow.XMLHttpRequest.prototype.open = function(method, url) {
                this._sakuraUrl = url;

                if (!capturedData.imageHash) {
                    var imageData = extractImageHashFromUrl(url);
                    if (imageData) {
                        capturedData.imageHash = imageData.hash;
                        capturedData.imageExtension = imageData.extension;
                        tryNotifyAndroid();
                    }
                }

                return originalXHROpen.apply(this, arguments);
            };

            targetWindow.XMLHttpRequest.prototype.send = function() {
                var xhr = this;
                var url = this._sakuraUrl || '';
                var isChapterRead = url.indexOf('capitulos__read') !== -1 || url.indexOf('capitulo') !== -1;

                if (isChapterRead && !capturedData.numPages) {
                    xhr.addEventListener('load', function() {
                        try {
                            var numPages = extractNumPagesFromResponse(xhr.responseText);
                            if (numPages) {
                                capturedData.numPages = numPages;
                                tryNotifyAndroid();
                            }
                        } catch (e) {}
                    });
                }

                return originalXHRSend.apply(this, arguments);
            };
        } catch (e) {}
    }

    try {
        var iframeProto = HTMLIFrameElement.prototype;
        var originalContentWindowDescriptor = Object.getOwnPropertyDescriptor(iframeProto, 'contentWindow');

        if (originalContentWindowDescriptor && originalContentWindowDescriptor.get) {
            var originalGetter = originalContentWindowDescriptor.get;
            Object.defineProperty(iframeProto, 'contentWindow', {
                get: function() {
                    var contentWindow = originalGetter.call(this);
                    if (contentWindow && !contentWindow.__SAKURA_CHAPTER_HOOKED__) {
                        try {
                            createChapterHooks(contentWindow, 'iframe');
                        } catch (e) {}
                    }
                    return contentWindow;
                },
                configurable: true
            });
        }
    } catch (e) {}

    createChapterHooks(window, 'main');

    window.__sakuraGetChapterData = function() {
        return capturedData;
    };

    if (window.SakuraChapterInterface && typeof window.SakuraChapterInterface.onHookReady === 'function') {
        window.SakuraChapterInterface.onHookReady();
    }
})();
