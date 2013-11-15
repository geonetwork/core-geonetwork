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
      var serviceUrl = 'qi@json?fast=index';
      $scope.resultRecords = [];
      $scope.resultCount = 0;

      var composeUrl = function() {
        var url = serviceUrl;
        for (param in $scope.params) {
          url = gnUrlUtils.append(url,
              param + '=' + $scope.params[param]);
        }
        return url;
      };

      $scope.triggerSearch = function() {
        gnSearchManagerService.search(composeUrl()).then(
            function(data) {
              $scope.resultRecords = data.metadata;
              $scope.resultCount = data.count;
            });
      };

      $scope.triggerSearch();
    }
  ]);
})();
