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
        scope: {},
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
          scope.loadingCollection = false;

          scope.registryUrl = '';
          scope.defaultRegistry = 'http://inspire.ec.europa.eu';

          scope.setDefault = function() {
            scope.registryUrl = scope.defaultRegistry;
          }

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
            if (scope.registryUrl === '') {
              return;
            }
            // On init,
            // * load language first (select the first one)
            // * load available itemClass (select the firs one)
            gnRegistryService.loadLanguages(scope.registryUrl).then(
              function (languages) {
                scope.languages = languages;
                scope.selectedClass = null;
                scope.selectedCollection = null;
                scope.itemCollection = [];
                
                if (languages.length > 0) {
                  scope.selectedLanguages[languages[0].key] = true;

                  gnRegistryService.loadItemClass(scope.registryUrl).then(
                    function (itemClass) {
                      scope.itemClass = itemClass;

                      if (itemClass.length > 0) {
                        scope.selectedClass = itemClass[0].key;
                        loadCollection();
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

          scope.$watch('registryUrl', function (n, o) {
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

          function loadCollection() {
            scope.loadingCollection = true;
            gnRegistryService.loadItemCollection(
              scope.registryUrl,
              scope.selectedClass,
              getMainLanguage()).then(
              function (itemCollection) {
                scope.itemCollection =
                  itemCollection.data.register.containeditems;
                scope.itemCollection.sort(compareItem);
                scope.selectedCollection =
                  scope.itemCollection[0].codelist.id;
                scope.loadingCollection = false;
              }, function(error) {
                scope.loadingCollection = false;
              }
            );
          }

          init();
        }
      };
    }]);
})();
