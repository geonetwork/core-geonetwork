(function() {
  goog.provide('inspire_get_extents_factory');

  var module = angular.module('inspire_get_extents_factory', []);

  module.factory('inspireGetExtentsFactory', [ '$http', '$q', function($http, $q) {
    return  function(url, query) {
        var deferred = $q.defer();

        var serviceAndParams = 'xml.regions.list@json?maxRecords=20&label='+query;
        $http.get(url +serviceAndParams).success(function(data) {
          var regions = [];

          for (var i = 0; i < data.region.length; i++) {
            var region = data.region[i];
            regions.push({
              geom: region['@id'],
              description: region.label
            });
          }
          deferred.resolve(regions);
        }).error(function (data) {
          deferred.reject(data);
        });
        return deferred.promise;
      };
  }]);
})();

