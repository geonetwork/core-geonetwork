(function() {
  goog.provide('gn_vcs_service');

  var module = angular.module('gn_vcs_service', []);


  module.factory('gnVcsService', [
    '$q',
    '$rootScope',
    '$http',
    'gnHttp',
    function($q, $rootScope, $http, gnHttp) {

      var format = function(data) {
        angular.forEach(data.entry, function(log) {
          log.date = log.date.replace(' ', 'T');
          // Add user info
        });
        return data;
      };

      var getLog = function(metadataId) {
        var url = 'versioning.logdata@json';
        if (metadataId) {
          url += '?id=' + metadataId;
        }
        var defer = $q.defer();
        $http.get(url).
            success(function(data, status) {
              defer.resolve(format(data));
            }).
            error(function(data, status) {
              defer.reject(error);
            });
        return defer.promise;
      };

      return {
        getLog: getLog
      };
    }]);
})();
