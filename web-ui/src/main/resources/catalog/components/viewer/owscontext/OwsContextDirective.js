(function() {
  goog.provide('gn_owscontext_directive');

  var module = angular.module('gn_owscontext_directive', []);

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
    'gnConfig',
    '$translate',
    '$rootScope',
    '$http',
    function(gnViewerSettings, gnOwsContextService, gnConfig,
        $translate, $rootScope, $http) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/owscontext/' +
            'partials/owscontext.html',
        scope: {
          user: '=',
          map: '='
        },
        link: function(scope, element, attrs) {
          scope.mapFileName = $translate('mapFileName');
          scope.save = function($event) {
            scope.mapFileName = $translate('mapFileName') +
                '-z' + scope.map.getView().getZoom() +
                '-c' + scope.map.getView().getCenter().join('-');

            var xml = gnOwsContextService.writeContext(scope.map);
            var str = new XMLSerializer().serializeToString(xml);
            var base64 = base64EncArr(strToUTF8Arr(str));

            var el = $($event.target);
            if (!el.is('a')) {
              el = el.parent();
            }
            if (el.is('a')) {
              el.attr('href', 'data:text/xml;base64,' + base64);

            }
          };

          scope.isSaveMapInCatalogAllowed =
              gnConfig['map.isSaveMapInCatalogAllowed'];
          scope.mapUuid = null;
          scope.mapProps = {
            map_title: '',
            map_abstract: ''
          };
          scope.saveInCatalog = function($event) {
            scope.mapUuid = null;
            var xml = gnOwsContextService.writeContext(scope.map);
            scope.mapProps.map_string =
                new XMLSerializer().serializeToString(xml);
            scope.mapProps.map_filename = $translate('mapFileName') +
                '-z' + scope.map.getView().getZoom() +
                '-c' + scope.map.getView().getCenter().join('-') + '.ows';
            return $http.post('map.import?_content_type=json',
                $.param(scope.mapProps), {
                  headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                }).then(
                function(response) {
                  scope.mapUuid = response.data[0];
                },
                function(data) {
                  console.warn(data);
                }
            );
          };
          scope.reset = function() {
            $rootScope.$broadcast('owsContextReseted');
            gnOwsContextService.loadContextFromUrl(
                gnViewerSettings.defaultContext,
                scope.map);
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
          var storage = gnViewerSettings.storage ?
              window[gnViewerSettings.storage] : window.localStorage;
          if (gnViewerSettings.owsContext) {
            gnOwsContextService.loadContextFromUrl(gnViewerSettings.owsContext,
                scope.map, true);
          } else if (storage.getItem('owsContext')) {
            var c = storage.getItem('owsContext');
            gnOwsContextService.loadContext(c, scope.map);
          } else if (gnViewerSettings.defaultContext) {
            gnOwsContextService.loadContextFromUrl(
                gnViewerSettings.defaultContext,
                scope.map);
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
