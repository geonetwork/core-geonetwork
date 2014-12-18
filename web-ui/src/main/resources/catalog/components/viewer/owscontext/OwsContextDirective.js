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
   * @name gn_owscontext_directive.directive:gnOwsContext
   *
   * @description
   * Panel to load or export an OWS Context
   */
  module.directive('gnOwsContext', [
    'gnViewerSettings',
    'gnOwsContextService',
    function(gnViewerSettings, gnOwsContextService) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/owscontext/' +
            'partials/owscontext.html',
        scope: {
          map: '='
        },
        link: function(scope, element, attrs) {

          scope.save = function($event) {
            var xml = gnOwsContextService.writeContext(scope.map);

            var str = new XMLSerializer().serializeToString(xml);
            var base64 = base64EncArr(strToUTF8Arr(str));
            $($event.target).attr('href', 'data:text/xml;base64,' + base64);
          };

          var fileInput = element.find('input[type="file"]')[0];
          element.find('.import').click(function() {
            fileInput.click();
          });

          //TODO: don't trigger if we load same file twice
          angular.element(fileInput).bind('change', function(changeEvent) {
            scope.$apply(function() {
              if (fileInput.files.length > 0) {
                readAsText(fileInput.files[0], function(text) {
                  gnOwsContextService.loadContext(text, scope.map);
                });
              }
            });
          });

          // load context from url or from storage
          if (gnViewerSettings.owsContext) {
            gnOwsContextService.loadContextFromUrl(gnViewerSettings.owsContext,
                scope.map, true);
          } else if (window.localStorage.getItem('owsContext')) {
            var c = window.localStorage.getItem('owsContext');
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
