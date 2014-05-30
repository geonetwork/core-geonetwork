(function() {
  'use strict';
  goog.provide('inspire_get_extents_factory');

  var module = angular.module('inspire_get_extents_factory', []);

  module.factory('inspireGetExtentsFactory', ['$http', '$q', function($http, $q) {
    return  function($scope, url, query) {
        var deferred = $q.defer();
        $scope.loadingExtents = true;
        var serviceAndParams = 'xml.regions.list@json?maxRecords=20&label='+query;
        $http.get(url +serviceAndParams).success(function(data) {
          var i, region, regions = [];

          if (data.region) {
            for (i = 0; i < data.region.length; i++) {
              region = data.region[i];
              regions.push({
                geom: region['@id'],
                description: region.label
              });
            }
          }
          $scope.loadingExtents = false;
          deferred.resolve(regions);
        }).error(function (data) {
          $scope.loadingExtents = false;
          deferred.reject(data);
        });
        return deferred.promise;
      };
  }]);
}());

