(function() {

  goog.provide('sxt_processespanel');

  var module = angular.module('sxt_processespanel', []);


  module.directive('sxtProcessesPanel', [
    'sxtOgcLinksService',
    function(sxtOgcLinksService) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/processesPanel.html',

        link: function(scope, element) {
          var processFormContainer = $(element).find('.panel-body .sxt-processes-form');

          // switch current process
          scope.$watch('selectedProcess', function (newProcess) {
            if (!newProcess) {
              return;
            }
            processFormContainer.empty();
            sxtOgcLinksService.wpsForm(scope, processFormContainer, newProcess);
          });
        }

      };
    }
  ]);
})();
