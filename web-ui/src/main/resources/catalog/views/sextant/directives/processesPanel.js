(function() {

  goog.provide('sxt_processespanel');

  var module = angular.module('sxt_processespanel', []);


  module.directive('sxtProcessesPanel', [
    'gnViewerSettings',
    'sxtOgcLinksService',
    function(gnViewerSettings, sxtOgcLinksService) {
      return {
        restrict: 'A',
        scope: true,
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/processesPanel.html',

        link: function(scope, element) {
          // processes (these come from API settings)
          scope.processes = gnViewerSettings.processes;
          scope.selectedProcess = scope.processes && scope.processes[0];

          var processFormContainer = $(element).find('.panel-body .sxt-processes-form');

          // switch current process
          scope.switchProcess = function (newProcess) {
            scope.selectedProcess = newProcess;
            scope.showWPS();
          }

          // show process function & switch tab
          scope.showWPS = function() {
            var process = scope.selectedProcess;
            if (!process) {
              console.error('Invalid process:', process);
            }
            processFormContainer.empty();
            process.layer = process.layer || new ol.layer.Image();   // to avoid errors with wps directive
            sxtOgcLinksService.wpsForm(scope, processFormContainer, process);
          };

          // initially show first process available
          scope.showWPS();
        }

      };
    }
  ]);
})();
