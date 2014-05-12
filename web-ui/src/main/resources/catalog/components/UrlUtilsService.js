(function() {
  goog.provide('gn_urlutils_service');

  var module = angular.module('gn_urlutils_service', []);

  module.provider('gnUrlUtils', function() {

    this.$get = function() {

      var UrlUtils = function() {

        // from Angular
        // https://github.com/angular/angular.js/blob/master/src/ng/directive/input.js#L3
        var URL_REGEXP =
            /^(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?$/;

        // Test validity of a URL
        this.isValid = function(url) {
          return (!!url && url.length > 0 && URL_REGEXP.test(url));
        };

        this.append = function(url, paramString) {
          if (paramString) {
            var parts = (url + ' ').split(/[?&]/);
            url += (parts.pop() === ' ' ? paramString :
                (parts.length > 0 ? '&' + paramString : '?' + paramString));
          }
          return url;
        };

        this.remove = function(url, params, ignoreCase) {
          var parts = url.split('?');
          if (parts.length > 0) {
            var qs = '&' + parts[1];
            var flags = (ignoreCase) ? 'gi' : 'g';
            qs = qs.replace(
                new RegExp('&(' + params.join('|') + ')=[^&]*', flags), '');
            url = parts[0] + qs.replace(/^&/, '?');
          }
          return url;
        };

        // stolen from Angular
        // https://github.com/angular/angular.js/blob/master/src/Angular.js
        this.encodeUriQuery = function(val, pctEncodeSpaces) {
          return encodeURIComponent(val).
              replace(/%40/gi, '@').
              replace(/%3A/gi, ':').
              replace(/%24/g, '$').
              replace(/%2C/gi, ',').
              replace(/%20/g, (pctEncodeSpaces ? '%20' : '+'));
        };

        // stolen from Angular
        // https://github.com/angular/angular.js/blob/master/src/Angular.js
        this.parseKeyValue = function(keyValue) {
          var obj = {}, kv, key, this_ = this;
          angular.forEach((keyValue || '').split('&'), function(keyValue) {
            if (keyValue) {
              kv = keyValue.split('=');
              key = this_.tryDecodeURIComponent(kv[0]);
              if (angular.isDefined(key)) {
                obj[key] = angular.isDefined(kv[1]) ?
                    this_.tryDecodeURIComponent(kv[1]) : true;
              }
            }
          });
          return obj;
        };

        // stolen from Angular
        // https://github.com/angular/angular.js/blob/master/src/Angular.js
        this.toKeyValue = function(obj) {
          var parts = [], this_ = this;
          angular.forEach(obj, function(value, key) {
            parts.push(this_.encodeUriQuery(key, true) +
                (value === true ? '' : '=' +
                    this_.encodeUriQuery(value, true)));
          });
          return parts.length ? parts.join('&') : '';
        };

        // stolen from Angular
        // https://github.com/angular/angular.js/blob/master/src/Angular.js
        this.tryDecodeURIComponent = function(value) {
          try {
            return decodeURIComponent(value);
          } catch (e) {
            // Ignore any invalid uri component
          }
        };

        // stolen from Angular
        // https://github.com/angular/angular.js/blob/master/src/ng/urlUtils.js
        this.urlResolve = function(url, base) {
          var href = url;
          var urlParsingNode = document.createElement('a');
          var msie = parseInt((/msie (\d+)/.exec(
              navigator.userAgent.toLowerCase()) || [])[1], 10);

          if (msie) {
            // Normalize before parse. Refer Implementation Notes on why this
            //  is done in two steps on IE.
            urlParsingNode.setAttribute('href', href);
            href = urlParsingNode.href;
          }

          urlParsingNode.setAttribute('href', href);

          // urlParsingNode provides the UrlUtils interface -
          //http://url.spec.whatwg.org/#urlutils
          return {
            href: urlParsingNode.href,
            protocol: urlParsingNode.protocol ?
                urlParsingNode.protocol.replace(/:$/, '') : '',
            host: urlParsingNode.host,
            search: urlParsingNode.search ?
                urlParsingNode.search.replace(/^\?/, '') : '',
            hash: urlParsingNode.hash ?
                urlParsingNode.hash.replace(/^#/, '') : '',
            hostname: urlParsingNode.hostname,
            port: urlParsingNode.port,
            pathname: (urlParsingNode.pathname.charAt(0) === '/') ?
                urlParsingNode.pathname : '/' + urlParsingNode.pathname
          };
        };

      };

      return new UrlUtils();
    };
  });

})();
