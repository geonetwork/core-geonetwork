(function() {
  goog.provide('gn_mdactions_service');

  var module = angular.module('gn_mdactions_service', []);

  module.service('gnMetadataActions', ['gnHttp',

    function(gnHttp) {

      this.metadataPrint = function(uuid) {
        var url = gnHttp.getService('mdGetPDF') + '?uuid=' + uuid;
        location.replace(url);
      };

      this.metadataRDF = function(uuid) {
        var url = gnHttp.getService('mdGetRDF') + '?uuid=' + uuid;
        location.replace(url);
      };

      this.metadataMEF = function(uuid) {
        var url = gnHttp.getService('mdGetMEF') + '?version=2&uuid=' + uuid;
        location.replace(url);
      };

      this.publish = function(md) {
        var published = md.isPublished(),
            flag = published ? 'off' : 'on';

        return gnHttp.callService('mdPrivileges', {
          update: true,
          id: md.getId(),
          _1_0:  flag,
          _1_1: flag,
          _1_5: flag,
          _1_6: flag
        });
      };
    }]);
})();
