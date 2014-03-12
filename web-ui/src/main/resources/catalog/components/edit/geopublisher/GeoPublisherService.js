(function() {
  goog.provide('gn_geopublisher_service');


  var module = angular.module('gn_geopublisher_service', [
  ]);

  module.factory('gnGeoPublisher', [
    'gnCurrentEdit',
    'gnHttp',
    function(gnCurrentEdit, gnHttp) {

      return {

        getList: function() {
          return gnHttp.callService('geoserverNodes', {
            action: 'LIST'
          });
        },

        checkNode: function(node, fileName) {
          if (node) {
            return gnHttp.callService('geoserverNodes', {
              metadataId: gnCurrentEdit.id,
              access: 'private',
              action: 'GET',
              nodeId: node,
              file: fileName
            });
          }
        },

        publishNode: function(node, fileName,
                              title, moreInfo) {
          if (node) {
            return gnHttp.callService('geoserverNodes', {
              metadataId: gnCurrentEdit.id,
              metadataUuid: gnCurrentEdit.uuid,
              metadataTitle: title,
              metadataAbstract: moreInfo,
              access: 'private',
              action: 'CREATE',
              nodeId: node,
              file: fileName
            });
          }
        },

        unpublishNode: function(node, fileName) {
          if (node) {
            return gnHttp.callService('geoserverNodes', {
              metadataId: gnCurrentEdit.id,
              access: 'private',
              action: 'DELETE',
              nodeId: node,
              file: fileName
            }).success(function(data) {
            });
          }
        }
      };
    }]);
})();
