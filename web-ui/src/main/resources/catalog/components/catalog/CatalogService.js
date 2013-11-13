(function() {
  goog.provide('gn_catalog_service');

  goog.require('gn_urlutils_service');

  var module = angular.module('gn_catalog_service', [
    'gn_urlutils_service'
  ]);

  module.provider('gnNewMetadata', function() {
    this.$get = ['$http', '$location', 'gnUrlUtils',
                 function($http, $location, gnUrlUtils) {
        return {
          createNewMetadata: function(id, groupId, fullPrivileges, 
              template, tab) {
            var url = gnUrlUtils.append('md.create@json',
                gnUrlUtils.toKeyValue({
                  group: groupId,
                  id: id,
                  isTemplate: template || 'n',
                  fullPrivileges: fullPrivileges || true
                })
                );

            $http.get(url).success(function(data) {
              $location.path('/metadata/' + data.id);
            });
            // TODO : handle creation error
          }
        };
      }];
  });
})();
