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
  goog.provide('gn_registry_service');

  var module = angular.module('gn_registry_service', []);


  /**
   * Interface to discuss with registry
   * http://inspire.ec.europa.eu/registry/help
   */
  module.service('gnRegistryService', [
    '$http', '$q',
    function($http, $q) {
      var responseFormat = '.json';

      this.loadLanguages = function (url) {
        var languages = [];
        var deferred = $q.defer();

        // TODO: In the current version of the
        // INSPIRE registry service we do not
        // have a format containing all the languages.
        languages = languages.concat(
          {key: 'en', label: 'en'}, {key: 'bg', label: 'bg'},
          {key: 'cs', label: 'cs'}, {key: 'hr', label: 'hr'},
          {key: 'da', label: 'da'}, {key: 'de', label: 'de'},
          {key: 'el', label: 'el'}, {key: 'es', label: 'es'},
          {key: 'et', label: 'et'}, {key: 'fi', label: 'fi'},
          {key: 'fr', label: 'fr'}, {key: 'hu', label: 'hu'},
          {key: 'it', label: 'it'}, {key: 'lt', label: 'lt'},
          {key: 'lv', label: 'lv'}, {key: 'mt', label: 'mt'},
          {key: 'nl', label: 'nl'}, {key: 'pl', label: 'pl'},
          {key: 'pt', label: 'pt'}, {key: 'ro', label: 'ro'},
          {key: 'sk', label: 'sk'}, {key: 'si', label: 'si'},
          {key: 'sv', label: 'sv'});
        deferred.resolve(languages);

        return deferred.promise;
      };

      this.loadItemClass = function (url, lang) {
        var itemClass = [];

        var deferred = $q.defer();

        $http({
          url: url + '/registry/registry.' + lang + responseFormat,
          method: 'GET',
          cache: true
        }).then(function (r) {
          if (angular.isUndefined(r.data.registry)) {
            deferred.reject(r);
          } else {
            angular.forEach(r.data.registry.registers, function (value, key) {
              itemClass.push({
                key: value.register.id,
                label: value.register.label.text})
            });
            deferred.resolve(itemClass);
          }
        }, function (r) {
          deferred.reject(r);
        });

        return deferred.promise;
      };

      this.loadItemCollection = function (url, lang) {
        var clazz = url.substring(url.lastIndexOf('/') + 1);
        return $http({
          url: url + '/' + clazz + '.' + lang + responseFormat,
          method: 'GET',
          cache: true
        })
      };

      this.loadItem = function (url, collection, lang) {
        return $http({
          url: url + '/' +
               collection + '/' + collection + '.' + lang + responseFormat,
          method: 'GET',
          cache: true
        })
      };

    }]);

})();
