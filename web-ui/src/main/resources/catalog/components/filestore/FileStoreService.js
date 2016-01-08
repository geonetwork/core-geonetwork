(function() {
  goog.provide('gn_filestore_service');

  var module = angular.module('gn_filestore_service', []);

  module.factory('gnFileStoreService',
      ['$http',
       function($http) {
         return {
           get: function(metadataUuid, filter) {
             return $http.get('../api/metadata/' +
                                  metadataUuid + '/resources', {
               params: {
                 filter: filter
               }
             });
           },
           updateStatus: function(resource) {
             return $http.patch(resource.url + '?share=' +
             (resource.type == 'private' ? 'public' : 'private'));
           },
           delete: function(resource) {
             return $http.delete(resource.url);
           }
         };
       }]);
})();
