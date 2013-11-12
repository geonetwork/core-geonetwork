(function() {
  goog.provide('gn_catalog_service');

  goog.require('gn_urlutils_service');

  var module = angular.module('gn_catalog_service', [
    'gn_urlutils_service'
  ]);

  module.provider('gnNewMetadata', function() {
    this.$get = ['$http', 'gnUrlUtils', function($http, gnUrlUtils) {
      return {
        createNewMetadata: function(id, groupId) {
          var url = gnUrlUtils.append('metadata.create.new',
              gnUrlUtils.toKeyValue({
                        group: groupId,
                        id: id,
                        isTemplate: 'n',
                        fullPrivileges: true
              })
                    );

          $http.get(url).success(function(data) {
            console.log('md creted with id = ' + id +
                ' and groupid = ' + groupId);
          });
        }
      };
    }];
  });
})();
