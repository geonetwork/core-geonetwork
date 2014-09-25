(function() {

  goog.provide('gn_search_sextant');

  goog.require('gn_search');
  goog.require('gn_search_sextant_config');
  goog.require('sxt_panier_directive');

  var module = angular.module('gn_search_sextant', [
    'gn_search',
    'gn_search_sextant_config',
    'sxt_panier_directive'
  ]);

  module.controller('gnsSextant', [
    '$scope',
    '$location',
    'suggestService',
    '$http',
    'gnSearchSettings',
    'gnMap',
    function($scope, $location, suggestService, $http, gnSearchSettings, gnMap) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;

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
        },
        panier:{
          title: 'Panier',
          active: false
        }};

      $scope.addLayerToMap = function(number) {
        $scope.mainTabs.map.titleInfo = '  (+' + number + ')';
      };

      $scope.$on('addLayerFromMd', function(evt, getCapLayer) {
        gnMap.addWmsToMapFromCap(viewerMap, getCapLayer);
      });

      $scope.displayMapTab = function() {
        if(viewerMap.getSize()[0] == 0 || viewerMap.getSize()[1] == 0){
          setTimeout(function(){
            viewerMap.updateSize();
          }, 0);
        }
        $scope.mainTabs.map.titleInfo = '';
      };

      $scope.displayPanierTab = function() {
        $scope.$broadcast('renderPanierMap');
        $scope.mainTabs.panier.titleInfo = '';
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

      angular.extend($scope.searchObj, {
        advancedMode: false,
        viewerMap: viewerMap,
        searchMap: searchMap,
        panier: []
      });
    }]);

  module.controller('gnsSextantSearch', [
      '$scope',
      'gnOwsCapabilities',
      'gnMap',
    function($scope, gnOwsCapabilities, gnMap) {

      $scope.resultviewFns = {
        addMdLayerToMap: function(link) {
          gnOwsCapabilities.getCapabilities(link.url).then(function(capObj) {
            var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(link.name, capObj);
            gnMap.addWmsToMapFromCap($scope.searchObj.viewerMap, layerInfo);
          });

        },
        addMdLayerToPanier: function(link,md) {
          md.url = link.url;
          $scope.searchObj.panier.push(md);
        }
      };
    }]);

    })();
