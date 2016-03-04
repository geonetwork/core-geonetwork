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
  goog.provide('gn_locale');
  goog.require('gn_cat_controller');

  var module = angular.module('gn_locale', [
    'pascalprecht.translate',
    'angular-md5',
    'gn_cat_controller'
  ]);

  module.constant('$LOCALES', ['core']);

  module.factory('localeLoader', ['$http', '$q', 'gnLangs',
    function($http, $q, gnLangs) {
      return function(options) {

        function buildUrl(prefix, lang, value, suffix) {
          if (value.indexOf('/') === 0) {
            return value.substring(1);
          } else {
            return prefix + gnLangs.getIso2Lang(lang) + '-' + value + suffix;
          }
        };
        var allPromises = [];
        angular.forEach(options.locales, function(value, index) {
          var langUrl = buildUrl(options.prefix, options.key,
              value, options.suffix);

          var deferredInst = $q.defer();
          allPromises.push(deferredInst.promise);

          $http({
            method: 'GET',
            url: langUrl,
            headers: {
              'Accept-Language': options.key
            }
          }).success(function(data) {
            deferredInst.resolve(data);
          }).error(function() {
            // Load english locale file if not available
            $http({
              method: 'GET',
              url: buildUrl(options.prefix, 'en', value, options.suffix)
            }).success(function(data) {
              deferredInst.resolve(data);
            }).error(function() {
              deferredInst.reject(options.key);
            });
          });
        });

        // Finally, create a single promise containing all the promises
        // for each app module:
        var deferred = $q.all(allPromises);
        return deferred;
      };
    }]);


  // TODO: could be improved instead of putting this in all main modules ?
  module.config(['$translateProvider', '$LOCALES', 'gnGlobalSettings',
    'gnLangs',
    function($translateProvider, $LOCALES, gnGlobalSettings, gnLangs) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: (gnGlobalSettings.locale.path || '../../') + 'catalog/locales/',
        suffix: '.json'
      });

      gnGlobalSettings.iso3lang = gnGlobalSettings.locale.iso3lang ||
          location.href.split('/')[5] || 'eng';
      gnGlobalSettings.lang = gnLangs.getIso2Lang(gnGlobalSettings.iso3lang);
      $translateProvider.preferredLanguage(gnGlobalSettings.iso3lang);
      moment.lang(gnGlobalSettings.lang);
    }]);

})();
