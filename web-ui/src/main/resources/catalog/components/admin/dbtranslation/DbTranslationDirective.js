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
  goog.provide('gn_dbtranslation_directive');

  var module = angular.module('gn_dbtranslation_directive', []);

  /**
     * Provide a table layout to edit db translations
     * for some types of entries (eg. groups, categories).
     *
     * Changes are saved on keyup event.
     *
     * Usage:
     * <div data-gn-db-translation="groupSelected" data-type="group"></div>
     */
  module.directive('gnDbTranslation', ['$http', '$translate', '$rootScope',
    function($http, $translate, $rootScope) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          type: '@type',
          element: '=gnDbTranslation'
        },
        templateUrl: '../../catalog/components/admin/dbtranslation/partials/' +
            'dbtranslation.html',
        link: function(scope, element, attrs) {

          /**
                 * Save a translation
                 */
          scope.saveTranslation = function(e) {
            // TODO: No need to save if translation not updated

            // TODO : could we use Angular compile here ?
            var xml = '<request><' + scope.type + ' id="{{id}}">' +
                      '<label>' +
                      '<{{key}}>{{value}}</{{key}}>' +
                                  '</label>' +
                      '</' + scope.type + '></request>';

            // id may be in id property (eg. group) or @id (eg. category)
            xml = xml.replace('{{id}}',
                scope.element.id || scope.element['@id'])
                .replace(/{{key}}/g, e.key)
                .replace('{{value}}', e.value);
            $http.post('admin.' + scope.type + '.update.labels', xml, {
              headers: {'Content-type': 'application/xml'}
            }).success(function(data) {
            }).error(function(data) {
              // FIXME: XML error to be converted to JSON ?
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate(scope.type + 'TranslationUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
          };
        }
      };
    }]);
})();
