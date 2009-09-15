/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: (define)
 *  module = Ext.lib.Ajax
 */

(function() {

    /** private: function[createComplete]
     *  ``Function``
     */
    var createComplete = function(fn, cb) {
        return function(request) {
            if(cb && cb[fn]) {
                cb[fn].call(cb.scope || window, {
                    responseText: request.responseText,
                    responseXML: request.responseXML,
                    argument: cb.argument
                });
            }
        };
    };

    Ext.apply(Ext.lib.Ajax, {
        /** private: method[request]
         */
        request: function(method, uri, cb, data, options) {
            options = options || {};
            var hs = options.headers;
            if(options.xmlData) {
                if(!hs || !hs["Content-Type"]) {
                    hs = hs || {};
                    hs["Content-Type"] = "text/xml";
                }
                method = (method ? method :
                    (options.method ? options.method : "POST"));
                data = options.xmlData;
            } else if(options.jsonData) {
                if(!hs || !hs["Content-Type"]) {
                    hs = hs || {};
                    hs["Content-Type"] = "application/json";
                }
                method = (method ? method :
                    (options.method ? options.method : "POST"));
                data = typeof options.jsonData == "object" ?
                       Ext.encode(options.jsonData) : options.jsonData;
            }
            return OpenLayers.Request.issue({
                success: createComplete("success", cb),
                failure: createComplete("failure", cb),
                headers: options.headers,
                method: method,
                headers: hs,
                data: data,
                url: uri
            });
        },

        /** private: method[isCallInProgress]
         *  :params request: ``Object`` The XHR object.
         */
        isCallInProgress: function(request) {
            // do not prevent our caller from calling abort()
            return true;
        },

        /** private: method[abort]
         *  :params request: ``Object`` The XHR object.
         */
        abort: function(request) {
            request.abort();
        }
    });
})();
