(function() {
  goog.provide('inspire-metadata-loader');

  var module = angular.module('inspire_metadata_factory', []);

  module.factory('inspireMetadataLoader', [ '$http', '$q', function($http, $q) {
    return function(url, mdId) {
      var deferred = $q.defer();
      $http.get(url + 'inspire.edit.model?id=mdId').success(function(data){
        $q.resolve(data);
      }).error(function(err){
        alert(err);
      });

      return deferred.promise;
    };
  }]);
})();

