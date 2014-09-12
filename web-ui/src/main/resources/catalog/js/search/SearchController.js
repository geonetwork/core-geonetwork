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
    '$location',
    'suggestService',
    '$http',
    'gnSearchConfig',
    function($scope, $location, suggestService, $http, gnSearchConfig) {

    }]);
})();
