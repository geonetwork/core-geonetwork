(function() {

  goog.provide('gn_search_controller');

  goog.require('gn_searchsuggestion_service');

  var module = angular.module('gn_search_controller',[
    'ui.bootstrap.typeahead',
    'gn_searchsuggestion_service'
  ]);

  /**
   * Main search controller attached to the first element of the
   * included html file from the base-layout.xsl output.
   */
  module.controller('GnSearchController', [
    '$scope',
    'suggestService',
    'gnSearchSettings',
    function($scope, suggestService, gnSearchSettings) {

      /** Object to be shared through directives and controllers */
      $scope.searchObj = {
        params: {},
        permalink: true
      };

      /** Facets configuration */
      $scope.facetsConfig = gnSearchSettings.facetsConfig;

      /* Pagination configuration */
      $scope.paginationInfo = gnSearchSettings.paginationInfo;

      /* Default result view template */
      $scope.resultTemplate = gnSearchSettings.resultViewTpls[1].tplUrl;

      $scope.getAnySuggestions = function(val) {
        return suggestService.getAnySuggestions(val);
      };


    }]);
})();
