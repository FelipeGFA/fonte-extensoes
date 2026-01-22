(function() {
    if (window.__kuroHooked) return;
    window.__kuroHooked = true;
    
    var originalParse = JSON.parse;
    var sentKeys = {};
    var expectPages = window.__kuroExpectPages || false;
    
    JSON.parse = function(text) {
        var result = originalParse.apply(this, arguments);
        try {
            if (result && typeof result === 'object' && !result._r_data) {
                var jsonStr = JSON.stringify(result);
                if (expectPages) {
                    if (result.pages && Array.isArray(result.pages) && result.pages.length > 0) {
                        var key = 'pages-' + result.pages.length;
                        if (!sentKeys[key]) {
                            sentKeys[key] = true;
                            window.__interface__.onDataParsed(jsonStr);
                        }
                    }
                    return result;
                }
                var hasApiData = result.data || result.token || result.manga || result.chapters || result.pages || result.message || result.error;
                if (hasApiData && jsonStr.length > 50) {
                    var key = jsonStr.substring(0, 100);
                    if (!sentKeys[key]) {
                        sentKeys[key] = true;
                        window.__interface__.onDataParsed(jsonStr);
                    }
                }
            }
        } catch(e) {}
        return result;
    };
    
    var originalFetch = window.fetch;
    window.fetch = function() {
        var args = arguments;
        return originalFetch.apply(this, args).then(function(response) {
            var clone = response.clone();
            clone.text().then(function(text) {
                try {
                    var json = JSON.parse(text);
                    if (json && (json.token || json.data || json.message || json.error)) {
                        window.__interface__.onDataParsed(text);
                    }
                } catch(e) {}
            }).catch(function(e) {});
            return response;
        });
    };
    
    var originalXHROpen = XMLHttpRequest.prototype.open;
    var originalXHRSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method, url) {
        this._kuroUrl = url;
        return originalXHROpen.apply(this, arguments);
    };
    
    XMLHttpRequest.prototype.send = function() {
        var xhr = this;
        var originalOnReadyStateChange = xhr.onreadystatechange;
        
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                try {
                    var text = xhr.responseText;
                    var json = JSON.parse(text);
                    if (json && (json.token || json.data || json.message || json.error)) {
                        window.__interface__.onDataParsed(text);
                    }
                } catch(e) {}
            }
            if (originalOnReadyStateChange) {
                originalOnReadyStateChange.apply(this, arguments);
            }
        };
        
        return originalXHRSend.apply(this, arguments);
    };
})();
