(function() {

  goog.provide('gn_search_controller');

  goog.require('gn_searchsuggestion_service');
  goog.require('gn_search_customui');

  var module = angular.module('gn_search_controller',[
    'ui.bootstrap.typeahead',
    'gn_searchsuggestion_service',
    'gn_search_customui'
  ]);

  module.constant('gnOlStyles',{
    bbox: new ol.style.Style({
      stroke: new ol.style.Stroke({
        color: 'rgba(255,0,0,1)',
        width: 2
      }),
      fill: new ol.style.Fill({
        color: 'rgba(255,0,0,0.3)'
      }),
      image: new ol.style.Circle({
        radius: 7,
        fill: new ol.style.Fill({
          color: 'rgba(255,0,0,1)'
        })
      })
    })
  });

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

      var viewerMap = gnSearchConfig.viewerMap;
      var searchMap = gnSearchConfig.searchMap;

      $scope.mainTabs = {
      home :{
        title: 'Home',
        titleInfo: '',
        active: true
      },
      search: {
        title: 'Search',
        titleInfo: '',
        active: false
      },
      map:{
        title: 'Map',
        active: false
      }};

      $scope.addLayerToMap = function(number) {
        $scope.mainTabs.map.titleInfo = '  (+' + number + ')';
      };

      $scope.displayMapTab = function() {
        if(viewerMap.getSize()[0] == 0 || viewerMap.getSize()[1] == 0){
          setTimeout(function(){
            viewerMap.updateSize();
          }, 0);
        }
        $scope.mainTabs.map.titleInfo = '';
      };

///////////////////////////////////////////////////////////////////
      $scope.getAnySuggestions = function(val) {
        var url = suggestService.getUrl(val, 'anylight',
            ('STARTSWITHFIRST'));

        return $http.get(url, {
        }).then(function(res){
          return res.data[1];
        });
      };

      $scope.$watch('searchObj.advancedMode', function(val) {
        if(val && (searchMap.getSize()[0] == 0 || searchMap.getSize()[1] == 0)){
          setTimeout(function(){
            searchMap.updateSize();
          }, 0);
        }
      });

///////////////////////////////////////////////////////////////////

      /** Object to be shared through directives and controllers */
      $scope.searchObj = {
        params: {},
        permalink: true,
        advancedMode: false,
        viewerMap: viewerMap,
        searchMap: searchMap
      };

      /** Facets configuration */
      $scope.facetsConfig = {
        keyword: 'keywords',
        orgName: 'orgNames',
        denominator: 'denominator',
        format: 'formats',
        createDateYear: 'createDateYears'
      };

      /* Pagination configuration */
      $scope.paginationInfo = {
        hitsPerPage: 3
      };
      $scope.resultTemplate = '../../catalog/components/search/resultsview/partials/viewtemplates/thumb.html';
    }]);

})();
