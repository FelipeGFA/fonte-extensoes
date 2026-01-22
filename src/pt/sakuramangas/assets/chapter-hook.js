(function() {
    if (window.__chapterHookInstalled) return;
    window.__chapterHookInstalled = true;

    function hookJQuery() {
        if (window.$ && $.ajax && !$.__chapterHooked) {
            var orig = $.ajax;
            $.ajax = function(url, opts) {
                var o = typeof url === 'object' ? url : (opts || {});
                if (typeof url !== 'object') o.url = url;
                if ((o.url || '').indexOf('capitulos__read') !== -1) {
                    var origSuccess = o.success;
                    o.success = function(data) {
                        if (data && data.numPages && window.Interceptor) {
                            window.Interceptor.onNumPages(data.numPages);
                        }
                        if (origSuccess) origSuccess.apply(this, arguments);
                    };
                }
                return orig.apply(this, arguments);
            };
            $.__chapterHooked = true;
            return true;
        }
        return false;
    }

    if (!hookJQuery()) {
        var attempts = 0;
        var interval = setInterval(function() {
            if (hookJQuery() || ++attempts > 50) clearInterval(interval);
        }, 100);
    }
})();
