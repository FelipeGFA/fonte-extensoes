(function() {
    'use strict';

    if (window.__WEBDRIVER_SPOOFED__) return;
    window.__WEBDRIVER_SPOOFED__ = true;

    function defineHiddenProp(obj, prop, value) {
        try {
            Object.defineProperty(obj, prop, {
                get: function() { return value; },
                set: function() {},
                configurable: true,
                enumerable: false
            });
        } catch (e) {}
    }

    // Hide WebDriver / Automation indicators
    defineHiddenProp(navigator, 'webdriver', false);
    defineHiddenProp(navigator, '__webdriver_script_fn', undefined);
    defineHiddenProp(navigator, '__driver_evaluate', undefined);
    defineHiddenProp(navigator, '__webdriver_evaluate', undefined);
    defineHiddenProp(navigator, '__selenium_evaluate', undefined);
    defineHiddenProp(navigator, '__fxdriver_evaluate', undefined);
    defineHiddenProp(navigator, '__driver_unwrapped', undefined);
    defineHiddenProp(navigator, '__webdriver_unwrapped', undefined);
    defineHiddenProp(navigator, '__selenium_unwrapped', undefined);
    defineHiddenProp(navigator, '__fxdriver_unwrapped', undefined);

    // Hide automation-related objects
    defineHiddenProp(window, 'callPhantom', undefined);
    defineHiddenProp(window, '_phantom', undefined);
    defineHiddenProp(window, 'phantom', undefined);
    defineHiddenProp(window, '__nightmare', undefined);
    defineHiddenProp(window, 'domAutomation', undefined);
    defineHiddenProp(window, 'domAutomationController', undefined);
    defineHiddenProp(window, 'cdc_adoQpoasnfa76pfcZLmcfl_Array', undefined);
    defineHiddenProp(window, 'cdc_adoQpoasnfa76pfcZLmcfl_Promise', undefined);
    defineHiddenProp(window, 'cdc_adoQpoasnfa76pfcZLmcfl_Symbol', undefined);
    defineHiddenProp(document, 'cdc_adoQpoasnfa76pfcZLmcfl_Array', undefined);
    defineHiddenProp(document, 'cdc_adoQpoasnfa76pfcZLmcfl_Promise', undefined);
    defineHiddenProp(document, 'cdc_adoQpoasnfa76pfcZLmcfl_Symbol', undefined);

    // Hide Chrome Runtime
    if (window.chrome && window.chrome.runtime) {
        try {
            Object.defineProperty(window.chrome, 'runtime', {
                get: function() { return undefined; },
                configurable: true,
                enumerable: false
            });
        } catch (e) {}
    }

    // Spoof permissions query
    try {
        var originalQuery = navigator.permissions.query;
        navigator.permissions.query = function(parameters) {
            if (parameters.name === 'notifications') {
                return Promise.resolve({ state: 'denied', onchange: null });
            }
            return originalQuery.call(navigator.permissions, parameters);
        };
    } catch (e) {}

    // Spoof WebGL
    try {
        var getParameter = WebGLRenderingContext.prototype.getParameter;
        WebGLRenderingContext.prototype.getParameter = function(parameter) {
            if (parameter === 37445) return 'Google Inc.';
            if (parameter === 37446) return 'ANGLE (Intel HD Graphics Direct3D11 vs_5_0 ps_5_0)';
            return getParameter.apply(this, arguments);
        };
    } catch (e) {}

    // Spoof plugins
    try {
        Object.defineProperty(navigator, 'plugins', {
            get: function() {
                return [
                    { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer' },
                    { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai' },
                    { name: 'Native Client', filename: 'internal-nacl-plugin' }
                ];
            },
            configurable: true
        });
    } catch (e) {}

    // Spoof languages
    try {
        Object.defineProperty(navigator, 'languages', {
            get: function() { return ['pt-BR', 'pt', 'en-US', 'en']; },
            configurable: true
        });
    } catch (e) {}

    // Prevent detection of Object.getOwnPropertyDescriptor checks
    try {
        var originalGetOwnPropertyDescriptor = Object.getOwnPropertyDescriptor;
        Object.getOwnPropertyDescriptor = function(obj, prop) {
            if (obj === navigator && prop === 'webdriver') return undefined;
            return originalGetOwnPropertyDescriptor.apply(this, arguments);
        };
    } catch (e) {}

    // Prevent detection via hasOwnProperty
    try {
        var originalHasOwnProperty = Object.prototype.hasOwnProperty;
        Object.prototype.hasOwnProperty = function(prop) {
            if (this === navigator && prop === 'webdriver') return false;
            return originalHasOwnProperty.apply(this, arguments);
        };
    } catch (e) {}
})();
