(function() {
  goog.provide('gn_batch_process_button');

  var module = angular.module('gn_batch_process_button', []);

  /**
   * Create a batch processing button.
   *
   * TODO: Add process parameters when needed ?
   */
  module.directive('gnBatchProcessButton',
      ['$rootScope', '$timeout', '$http', '$parse',
        'gnBatchProcessing',
        function($rootScope, $timeout, $http, $parse,
       gnBatchProcessing) {

         return {
           restrict: 'A',
           replace: true,
           scope: {
             processId: '@gnBatchProcessButton',
             params: '@'
           },
           templateUrl: '../../catalog/components/edit/' +
           'batchprocessbutton/partials/' +
           'batchprocessbutton.html',
           link: function(scope, element, attrs) {
             // TODO: handle process parameters.
             scope.paramList = angular.fromJson(scope.params);
             scope.process = function() {
               var params = {
                 process: scope.processId
               };
               angular.extend(params, scope.paramList);
               gnBatchProcessing.runProcessMd(params);
             };
           }
         };
       }]);
})();
