/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
        /**
         *
         * Implementation Notes for non-IE browsers
         * ----------------------------------------
         * Assigning a URL to the href property of an anchor DOM node, even
         * one attached to the DOM, results both in the normalizing and
         * parsing of the URL.  Normalizing means that a relative URL will be
         * resolved into an absolute URL in the context of the application
         * document.
         * Parsing means that the anchor node's host, hostname, protocol,
         * port, pathname and related properties are all populated to reflect
         * the normalized URL.  This approach has wide compatibility -
         * Safari 1+, Mozilla 1+ etc.  See
         * http://www.aptana.com/reference/html/api/HTMLAnchorElement.html
         *
         * Implementation Notes for IE
         * ---------------------------
         * IE <= 10 normalizes the URL when assigned to the anchor node similar
         * to the other browsers.  However, the parsed components will not be
         * set if the URL assigned did not specify them.  (e.g. if you assign
         * a.href = "foo", then a.protocol, a.host, etc. will be empty.)  We
         * work around that by performing the parsing in a 2nd step by taking
         * a previously normalized URL (e.g. by assigning to a.href) and
         * assigning it a.href again.  This correctly populates the
         * properties such as protocol, hostname, port, etc.
         *
         * References:
         *   http://developer.mozilla.org/en-US/docs/Web/API/HTMLAnchorElement
         *   http://www.aptana.com/reference/html/api/HTMLAnchorElement.html
         *   http://url.spec.whatwg.org/#urlutils
         *   https://github.com/angular/angular.js/pull/2902
         *   http://james.padolsey.com/javascript/parsing-urls-with-the-dom/
         *
         * @kind function
         * @param {string} url The URL to be parsed.
         * @description Normalizes and parses a URL.
         * @return {object} Returns the normalized URL as a dictionary.
         *
         * Member name/description:
         *
         * - href: A normalized version of the provided URL if it was
         *         not an absolute URL
         * - protocol: The protocol including the trailing colon.
         * - host: The host and port (if the port is non-default)
         *         of the normalizedUrl
         * - search: The search params, minus the question mark
         * - hash: The hash string, minus the hash symbol
         * - hostname: The hostname
         * - port: The port, without ":"
         * - pathname: The pathname, beginning with "/"
         *
         */
        this.urlResolve = function(url) {
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

        /**
         * Parse a request URL and determine whether this is a same-origin
         * request as the application document.
         *
         * Taken from
         * https://github.com/angular/angular.js/blob/v1.6.x/src/ng/urlUtils.js
         *
         * @param {string|object} requestUrl The url of the request as a string
         * that will be resolved or a parsed URL object.
         * @return {boolean} Whether the request is for the same origin as
         * the application document.
         */
        this.urlIsSameOrigin = function(requestUrl) {
          var originUrl = this.urlResolve(window.location.href);
          var parsed = (angular.isString(requestUrl)) ?
              this.urlResolve(requestUrl) : requestUrl;
          return (parsed.protocol === originUrl.protocol &&
              parsed.host === originUrl.host);
        };

      };

      return new UrlUtils();
    };
  });

})();
