(function() {
  goog.provide('gn_metadata_manager_service');

  var module = angular.module('gn_metadata_manager_service', []);

  var gnMetadataManagerService = function ($q, $rootScope, $http) {

      var apply = function () {
          $rootScope.$apply();
      };
      var _select = function (uuid, andClearSelection, action) {
          var defer = $q.defer();
          $http.get('metadata.select@json?' + 
                  (uuid ? 'id=' + uuid : '') + 
                  (andClearSelection ? '' : '&selected=' + action)).
              success(function(data, status) {
                defer.resolve(data);
              }).
              error(function(data, status) {
                defer.reject(error);
              });
          return defer.promise;
      };
      var select = function (uuid, andClearSelection) {
          return _select(uuid, andClearSelection, 'add');
      };
      var unselect = function (uuid) {
          return _select(uuid, false, 'remove');
      };
      var selectAll = function () {
          return _select(null, false, 'add-all');
      };
      var selectNone = function () {
          return _select(null, false, 'remove-all');
      };
      return {
          select: select,
          unselect: unselect,
          selectAll: selectAll,
          selectNone: selectNone
      };
  };

  gnMetadataManagerService.$inject = ['$q', '$rootScope', '$http'];

  module.factory('gnMetadataManagerService', gnMetadataManagerService);

})();
