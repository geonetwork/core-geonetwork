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
    '$q',
    '$http',
    'suggestService',
    'gnSearchSettings',
    'gnRegionService',
    function($scope, $q, $http, suggestService, gnSearchSettings, gnRegionService) {

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

      $scope.keywordsOptions = {
        mode: 'remote',
        remote: {
          url : suggestService.getUrl('QUERY', 'keyword', 'STARTSWITHFIRST'),
          filter: suggestService.bhFilter,
          wildcard: 'QUERY'
        }
      };

      $scope.orgNameOptions = {
        mode: 'remote',
        remote: {
          url : suggestService.getUrl('QUERY', 'orgName', 'STARTSWITHFIRST'),
          filter: suggestService.bhFilter,
          wildcard: 'QUERY'
        }
      };

      $scope.categoriesOptions = {
        mode: 'prefetch',
        promise: (function(){
          var defer = $q.defer();
          $http.get(suggestService.getInfoUrl('categories')).success(function(data) {
            var res = [];
            for(var i=0; i<data.metadatacategory.length;i++) {
              res.push({
                id: data.metadatacategory[i]['@id'],
                name : data.metadatacategory[i].label.eng
              })
            }
            defer.resolve(res);
          });
          return defer.promise;
        })()
      };

    }]);
})();
