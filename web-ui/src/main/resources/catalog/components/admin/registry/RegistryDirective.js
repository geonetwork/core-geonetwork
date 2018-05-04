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
  module.directive('gnRegistryBrowser', [
    'gnLangs', 'gnRegistryService', '$rootScope', '$translate',
    function(gnLangs, gnRegistryService, $rootScope, $translate) {
      return {
        restrict: 'A',
        replace: true,
        // scope: {},
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

          /**
           * Select the UI language if available or
           * the first one.
           */
          function selectPreferredLanguage() {
            var preferredLang = gnLangs.getIso2Lang(gnLangs.getCurrent());

            for (var i = 0; i < scope.languages.length; i ++) {
              if (preferredLang === scope.languages[i].key) {
                scope.selectedLanguages[scope.languages[i].key] = true;
                return;
              }
            }
            scope.selectedLanguages[scope.languages[0].key] = true;
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
                  selectPreferredLanguage();

                  gnRegistryService.loadItemClass(
                    scope.registryUrl, getMainLanguage()).then(
                    function (itemClass) {
                      scope.itemClass = itemClass;

                      if (itemClass.length > 0) {
                        scope.selectedClassUrl = itemClass[0];
                        loadCollection();
                      } else {
                        $rootScope.$broadcast('StatusUpdated', {
                          title: $translate.instant('registryNoClassFound'),
                          timeout: 3,
                          type: 'warning'});
                      }
                    }, function () {
                      $rootScope.$broadcast('StatusUpdated', {
                        title: $translate.instant('registryFailedToLoadClass'),
                        timeout: 3,
                        type: 'danger'});
                    }
                  );
                } else {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('registryNoLanguage'),
                    timeout: 3,
                    type: 'warning'});
                }
              }
            );
          }

          scope.$watch('registryUrl', function (n, o) {
            if (n !== o && n != null) {
              init();
            }
          });

          scope.$watch('selectedClassUrl', function (n, o) {
            if (scope.registryUrl != '' && n !== o && n != null) {
              loadCollection();
            }
          });

          function compareItem(a,b) {
            if (a[scope.selectedClass].label.text <
              b[scope.selectedClass].label.text)
              return -1;
            if (a[scope.selectedClass].label.text >
              b[scope.selectedClass].label.text)
              return 1;
            return 0;
          }

          // Some classes are directly a list of concept.
          // This is the case of INSPIRE themes for example.
          // TODO: Should be identified by analysing registry response ?
          scope.isSimple = false;
          function isSimpleList() {
            if (scope.selectedClass.match(
              'theme|applicationschema|featureconcept|' +
                'document|glossary|layer|media-types|producers')) {
              scope.isSimple = true;
            } else {
              scope.isSimple = false;
            }
            return scope.isSimple;
          }

          function loadCollection() {
            scope.loadingCollection = true;
            scope.selectedClass =
              scope.selectedClassUrl.key.substring(
                scope.selectedClassUrl.key.lastIndexOf('/') + 1);


            if (isSimpleList()) {
              scope.selectedCollection = scope.selectedClassUrl.key;
              scope.loadingCollection = false;
            } else {
              scope.itemCollection = [];
              gnRegistryService.loadItemCollection(
                scope.selectedClassUrl.key,
                getMainLanguage()).then(
                function (itemCollection) {
                  if (angular.isUndefined(itemCollection.data.register)) {
                    $rootScope.$broadcast('StatusUpdated', {
                      title: $translate.instant('registryNoItemFound'),
                      timeout: 3,
                      type: 'warning'});
                  } else {
                    scope.itemCollection =
                      itemCollection.data.register.containeditems;
                    scope.itemCollection.sort(compareItem);
                    scope.selectedCollection =
                      scope.itemCollection[0][scope.selectedClass].id;
                  }
                  scope.loadingCollection = false;
                }, function(error) {
                  scope.loadingCollection = false;
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('registryFailedToLoadItem'),
                    timeout: 3,
                    type: 'danger'});
                }
              );
            }
          }

          init();
        }
      };
    }]);
})();
