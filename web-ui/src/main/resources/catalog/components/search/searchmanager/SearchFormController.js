(function() {
  goog.provide('gn_search_form_controller');




  goog.require('gn_catalog_service');
  goog.require('gn_search_form_results_directive');
  goog.require('gn_urlutils_service');

  var module = angular.module('gn_search_form_controller', [
    'gn_catalog_service',
    'gn_urlutils_service',
    'gn_search_form_results_directive'
  ]);

  /**
   * Controller to create new metadata record.
   */
  module.controller('GnSearchFormController', [
    '$scope',
    'gnSearchManagerService',
    'gnUrlUtils',
    function($scope, gnSearchManagerService, gnUrlUtils) {
      var defaultServiceUrl = 'qi@json';
      var defaultParams = {
        fast: 'index'
      };
      $scope.resultRecords = [];
      $scope.resultCount = 0;

      var composeUrl = function(service) {
        var url = service || defaultServiceUrl;
        $scope.params = $.extend($scope.params, defaultParams);
        for (param in $scope.params) {
          url = gnUrlUtils.append(url,
              param + '=' + $scope.params[param]);
        }
        return url;
      };

      $scope.triggerSearch = function(service) {
        gnSearchManagerService.search(composeUrl(service)).then(
            function(data) {
              $scope.resultRecords = data.metadata;
              $scope.resultCount = data.count;
            });
      };
      $scope.clearResults = function() {
        $scope.resultRecords = null;
        $scope.resultCount = null;
      };

      $scope.$watch('autoSearch', function() {
        if ($scope.autoSearch) {
          $scope.triggerSearch();
        }
      });
    }
  ]);
})();
