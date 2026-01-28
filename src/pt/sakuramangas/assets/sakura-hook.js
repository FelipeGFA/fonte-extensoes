(function() {
    'use strict';

    if (window.__SAKURA_HOOK_INSTALLED__) return;
    window.__SAKURA_HOOK_INSTALLED__ = true;

    var firstChapterPostCaptured = false;
    var capturedData = {
        manga_info: null,
        chapter_post_body: null,
        csrf_token: null
    };

    function getCsrfToken() {
        try {
            var meta = document.querySelector('meta[name="csrf-token"]');
            return meta ? meta.getAttribute('content') : null;
        } catch (e) {
            return null;
        }
    }

    function sendMangaInfo(data, url) {
        try {
            var jsonStr = typeof data === 'string' ? data : JSON.stringify(data);
            if (window.SakuraInterface && typeof window.SakuraInterface.onMangaInfo === 'function') {
                window.SakuraInterface.onMangaInfo(jsonStr, url || '');
            }
        } catch (e) {}
    }

    function sendChapterPostBody(body, url) {
        try {
            var csrfToken = getCsrfToken();
            if (window.SakuraInterface && typeof window.SakuraInterface.onChapterPostData === 'function') {
                window.SakuraInterface.onChapterPostData(body, csrfToken || '', url || '');
            }
        } catch (e) {}
    }

    function createHooks(targetWindow, source) {
        if (targetWindow.__SAKURA_HOOKED__) return;
        targetWindow.__SAKURA_HOOKED__ = true;

        try {
            var originalJSONParse = targetWindow.JSON.parse;
            targetWindow.JSON.parse = function(text) {
                var result = originalJSONParse.apply(this, arguments);
                try {
                    if (result && typeof result === 'object') {
                        if (result.titulo && (result.sinopse !== undefined || result.tags)) {
                            capturedData.manga_info = result;
                            sendMangaInfo(result, source + ':json.parse');
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
                    var isMangaInfo = url.indexOf('manga_info') !== -1;
                    var isChapters = url.indexOf('manga_capitulos') !== -1;

                    if (isChapters && !firstChapterPostCaptured && init && init.body) {
                        firstChapterPostCaptured = true;
                        var postBody = null;

                        if (typeof init.body === 'string') {
                            postBody = init.body;
                        } else if (init.body instanceof URLSearchParams) {
                            postBody = init.body.toString();
                        } else if (init.body instanceof FormData) {
                            var params = [];
                            init.body.forEach(function(value, key) {
                                params.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
                            });
                            postBody = params.join('&');
                        }

                        if (postBody) {
                            capturedData.chapter_post_body = postBody;
                            sendChapterPostBody(postBody, url);
                        }
                    }

                    return originalFetch.apply(this, arguments).then(function(response) {
                        if (isMangaInfo) {
                            var clonedResponse = response.clone();
                            clonedResponse.text().then(function(text) {
                                try {
                                    var jsonData = JSON.parse(text);
                                    sendMangaInfo(jsonData, url);
                                } catch (e) {}
                            });
                        }
                        return response;
                    });
                };
            }
        } catch (e) {}
    }

    try {
        var iframeProto = HTMLIFrameElement.prototype;
        var originalContentWindowDescriptor = Object.getOwnPropertyDescriptor(iframeProto, 'contentWindow');

        if (originalContentWindowDescriptor && originalContentWindowDescriptor.get) {
            Object.defineProperty(iframeProto, 'contentWindow', {
                get: function() {
                    var contentWindow = originalContentWindowDescriptor.get.call(this);
                    if (contentWindow && !contentWindow.__SAKURA_HOOKED__) {
                        try {
                            createHooks(contentWindow, 'iframe');
                        } catch (e) {}
                    }
                    return contentWindow;
                },
                configurable: true
            });
        }
    } catch (e) {}

    createHooks(window, 'main');

    window.__sakuraGetCapturedData = function() {
        return capturedData;
    };

    if (window.SakuraInterface && typeof window.SakuraInterface.onHookReady === 'function') {
        window.SakuraInterface.onHookReady();
    }
})();
