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
  goog.provide('gn_ui_config_directive');

  var module = angular.module('gn_ui_config_directive', ['ui.ace']);

  module.directive('gnUiConfig', ['gnGlobalSettings',
    function(gnGlobalSettings) {

      return {
        restrict: 'A',
        scope: {
          config: '=gnUiConfig',
          id: '='
        },
        templateUrl: '../../catalog/components/admin/uiconfig/partials/' +
            'uiconfig.html',
        link: function(scope, element, attrs) {
          var testAppUrl = '../../catalog/views/api/?config=';

          function init() {
            // merge on top of default config
            scope.jsonConfig = angular.merge(
              // If the config is empty, use the default one set in CatController
              (Object.keys(scope.config).length === 0 || scope.config === "null" || !scope.config ?
                gnGlobalSettings.getDefaultConfig() :
                gnGlobalSettings.getMergeableDefaultConfig()),
              angular.fromJson(scope.config));
          }
          scope.$watch('config', function (n, o) {
            if (angular.isDefined(n) && n !== o) {
              init();
            }
          }, true);


          // Make a copy as string for the ui-ace widget to work on
          scope.$watch('jsonConfig', function (n) {
            scope.jsonConfigSource = scope.config = JSON.stringify(n, null, 2);
          }, true);
          scope.$watch('jsonConfigSource', function (n, o) {
            if (n !== o) {
              scope.jsonConfig = JSON.parse(n);
            }
          });

          scope.sortOrderChoices = ['', 'reverse'];


          // ng-model can't bind to object key, so
          // when key value change, reorganize object.
          scope.updateKey = function(obj, new_key, id) {
            var keys = Object.keys(obj);
            var values = Object.values(obj);
            var old_key = keys[id];
            if (keys.indexOf(new_key) == -1 && new_key.length > 0) {
              for (var i = 0, key; key = keys[i]; i++) {
                delete obj[key];
              }
              keys[id] = new_key;
              for (var i = 0, key; key = keys[i]; i++) {
                obj[key] = values[i];
              }
            }
          };

          // Add an item to an array
          // or duplicate last item multiplied by 10 (eg. hitsPerPage)
          scope.addItem = function(array, item) {
            if (angular.isArray(array)) {
              array.push(item);
            } else if (angular.isObject(array)) {
              var key = Object.keys(item)[0];
              array[key] = item[key];
            }
          };

          // Remove item from array
          scope.removeItem = function(array, index) {
            if (angular.isArray(array)) {
              array.splice(index, 1);
            } else if (angular.isObject(array)) {
              delete array[index];
            }
          };

          scope.reset = function() {
            angular.extend(scope.jsonConfig,
                gnGlobalSettings.getDefaultConfig());
          };

          scope.testClientConfig = function() {
            window.open(
                testAppUrl +
                encodeURIComponent(angular.toJson(scope.jsonConfig)),
                'gnClientTestWindow');
          };
        }
      };
    }]);

  // used to edit an object as a JSON string
  module.directive('gnJsonEdit', function() {
    return {
      restrict: 'A',
      scope: {
        value: '=gnJsonEdit'
      },
      link: function(scope, element, attrs) {
        element.val(JSON.stringify(scope.value));

        scope.$watch('value', function(newValue, oldValue) {
          element.val(JSON.stringify(newValue));
        }, true);

        element.on('change', function(eventObject) {
          scope.$apply(function() {
            var newValue = element.val();
            try {
              angular.merge(scope.value, JSON.parse(newValue));
            } catch (e) {
              console.warn('Error parsing JSON: ', newValue);
            }
          });
        });
      }
    };
  });
})();
