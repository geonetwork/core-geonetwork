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
  goog.provide('gn_registry_directive');

  var module = angular.module('gn_registry_directive', []);

  /**
     * Provide a Registry element selector.
   *
     * Usage:
     * <div data-gn-registry-browser="registryUrl"></div>
     */
  module.directive('gnRegistryBrowser', ['gnRegistryService',
    function(gnRegistryService) {
      return {
        restrict: 'A',
        replace: true,
        scope: {
          url: '=gnRegistryBrowser'
        },
        templateUrl: '../../catalog/components/admin/registry/partials/' +
            'registrybrowser.html',
        link: function(scope, element, attrs) {
          scope.itemClass = [];
          scope.languages = [];
          scope.itemCollection = [];
          scope.item = [];
          scope.selectedClass = null;
          scope.selectedCollection = null;
          scope.selectedLanguages = {};

          function getMainLanguage() {
            for (var p in scope.selectedLanguages) {
              if (scope.selectedLanguages.hasOwnProperty(p) &&
                  scope.selectedLanguages[p] === true) {
                return p;
              }
            }
            return null;
          };

          function init() {
            if (scope.url === '') {
              return;
            }
            // On init,
            // * load language first (select the first one)
            // * load available itemClass (select the firs one)
            gnRegistryService.loadLanguages(scope.url).then(
              function (languages) {
                scope.languages = languages;
                if (languages.length > 0) {
                  scope.selectedLanguages[languages[0].key] = true;

                  gnRegistryService.loadItemClass(scope.url).then(
                    function (itemClass) {
                      scope.itemClass = itemClass;

                      if (itemClass.length > 0) {
                        scope.selectedClass = itemClass[0].key;
                      } else {
                        console.warn('ItemClass are required. ' +
                          'None found in ' + itemClass)
                      }
                    }
                  );
                } else {
                  console.warn('Languages are required. ' +
                    'None found in ' + languages)
                }
              }
            );
          }

          scope.$watch('url', function (n, o) {
            if (n !== o && n != null) {
              init();
            }
          });

          function compareItem(a,b) {
            if (a.codelist.label.text < b.codelist.label.text)
              return -1;
            if (a.codelist.label.text > b.codelist.label.text)
              return 1;
            return 0;
          }

          scope.$watch('selectedClass', function (n, o) {
            if (n !== o && n != null) {
              gnRegistryService.loadItemCollection(
                scope.url,
                scope.selectedClass,
                getMainLanguage()).then(
                function (itemCollection) {
                  scope.itemCollection =
                    itemCollection.data.register.containeditems;
                  scope.itemCollection.sort(compareItem);
                  scope.selectedCollection =
                    scope.itemCollection[0].codelist.id;
                }
              );
            }
          });

          init();
        }
      };
    }]);
})();
