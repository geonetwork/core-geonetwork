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
  goog.provide('gn_owscontext_directive');

  var module = angular.module('gn_owscontext_directive', []);

  module.controller('gnsMapSearchController', [
    '$scope', 'gnRelatedResources',
    function($scope, gnRelatedResources) {
      $scope.resultTemplate = '../../catalog/components/' +
          'search/resultsview/partials/viewtemplates/grid4maps.html';

      $scope.loadMap = function(map, md) {
        gnRelatedResources.getAction('MAP')(map, md);
      };

      $scope.searchObj = {
        permalink: false,
        filters: {
          type: 'interactiveMap'
        },
        hitsperpageValues: 2,
        params: {
          sortBy: 'title',
          from: 1,
          to: 2
        }
      };
    }]);

  function readAsText(f, callback) {
    try {
      var reader = new FileReader();
      reader.readAsText(f);
      reader.onload = function(e) {
        if (e.target && e.target.result) {
          callback(e.target.result);
        } else {
          console.error('File could not be loaded');
        }
      };
      reader.onerror = function(e) {
        console.error('File could not be read');
      };
    } catch (e) {
      console.error('File could not be read');
    }
  }

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnOwsContext
   *
   * @description
   * Panel to load or export an OWS Context
   */
  module.directive('gnOwsContext', [
    'gnViewerSettings',
    'gnOwsContextService',
    'gnGlobalSettings',
    '$translate',
    '$rootScope',
    '$http',
    '$q',
    function(gnViewerSettings, gnOwsContextService, gnGlobalSettings,
        $translate, $rootScope, $http, $q) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/owscontext/' +
            'partials/owscontext.html',
        scope: {
          user: '=',
          map: '='
        },
        link: function(scope, element, attrs) {
          scope.mapFileName = $translate.instant('mapFileName');

          function getMapFileName() {
            return $translate.instant('mapFileName') +
                '-z' + scope.map.getView().getZoom() +
                '-c' + scope.map.getView().getCenter().join('-');
          };

          function getMapAsImage($event) {
            var defer = $q.defer();
            scope.mapFileName = getMapFileName();

            scope.map.once('postcompose', function(event) {
              var canvas = event.context.canvas;
              var data = canvas.toDataURL('image/png');
              defer.resolve(data);
            });
            scope.map.renderSync();
            return defer.promise;
          };

          function openContent($event, data) {
            var el = $($event.target);
            if (!el.is('a')) {
              el = el.parent();
            }
            if (el.is('a')) {
              el.attr('href', data);

            }
          };

          scope.save = function($event) {
            scope.mapFileName = getMapFileName();

            var xml = gnOwsContextService.writeContext(scope.map);
            var str = new XMLSerializer().serializeToString(xml);
            var base64 = base64EncArr(strToUTF8Arr(str));

            openContent($event, 'data:text/xml;base64,' + base64);
          };

          scope.saveMapAsImage = function($event) {
            getMapAsImage($event).then(function(data) {
              openContent($event, data);
            });
          };


          scope.isSaveMapInCatalogAllowed =
              gnGlobalSettings.gnCfg.mods.map.isSaveMapInCatalogAllowed || true;

          scope.mapUuid = null;

          var defaultMapProps = {
            title: '',
            recordAbstract: ''
          };

          scope.mapProps = angular.extend({}, defaultMapProps);

          scope.saveInCatalog = function($event) {
            var defer = $q.defer();

            getMapAsImage($event).then(function(data) {
              scope.mapUuid = null;

              // Map as OWS context
              var xml = gnOwsContextService.writeContext(scope.map);
              scope.mapProps.xml =
                  new XMLSerializer().serializeToString(xml);
              scope.mapProps.filename = getMapFileName() + '.ows';

              // Map as image
              scope.mapProps.overviewFilename = getMapFileName() + '.png';
              scope.mapProps.overview =
                  data.replace('data:image/png;base64,', '');

              return $http.post('../api/records/importfrommap',
                  $.param(scope.mapProps), {
                    headers: {'Content-Type':
                      'application/x-www-form-urlencoded'}
                  }).then(
                  function(response) {
                    var report = response.data.metadataInfos;
                    scope.mapUuid = report[Object.keys(report)[0]][0].message;
                    scope.mapProps = angular.extend({}, defaultMapProps);
                    defer.resolve(response);
                  },
                  function(data) {
                    console.warn(data);
                    defer.reject(response);
                  }
              );
            });

            return defer.promise;
          };

          scope.reset = function() {
            $rootScope.$broadcast('owsContextReseted');

            gnOwsContextService.loadContextFromUrl(
                gnViewerSettings.defaultContext,
                scope.map,
                gnViewerSettings.additionalMapLayers);
          };

          var fileInput = element.find('input[type="file"]')[0];
          element.find('.import').click(function() {
            fileInput.click();
          });

          //TODO: don't trigger if we load same file twice
          angular.element(fileInput).bind('change', function(changeEvent) {
            if (fileInput.files.length > 0) {
              readAsText(fileInput.files[0], function(text) {
                gnOwsContextService.loadContext(text, scope.map);
                scope.$digest();
              });
            }
            $('#owc-file-input')[0].value = '';
          });

          // load context from url or from storage
          var key = 'owsContext_' +
              window.location.host + window.location.pathname;
          var storage = gnViewerSettings.storage ?
              window[gnViewerSettings.storage] : window.localStorage;
          if (gnViewerSettings.owsContext) {
            gnOwsContextService.loadContextFromUrl(gnViewerSettings.owsContext,
                scope.map);
          } else if (storage.getItem(key)) {
            var c = storage.getItem(key);
            gnOwsContextService.loadContext(c, scope.map);
          } else if (gnViewerSettings.defaultContext) {
            gnOwsContextService.loadContextFromUrl(
                gnViewerSettings.defaultContext,
                scope.map,
                gnViewerSettings.additionalMapLayers);
          }

          // store the current context in local storage to reload it
          // automatically on next connexion
          $(window).on('unload', function() {
            gnOwsContextService.saveToLocalStorage(scope.map);
          });
        }
      };
    }
  ]);
})();
