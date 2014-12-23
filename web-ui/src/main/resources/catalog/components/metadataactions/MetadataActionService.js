(function() {
  goog.provide('gn_mdactions_service');

  var module = angular.module('gn_mdactions_service', []);

  module.service('gnMetadataActions', ['gnHttp',

    function(gnHttp) {

      var windowName = 'geonetwork';
      var windowOption = '';

      /**
       * Export as PDF. If params is search object, we check for sortBy
       * and sortOrder to process the print. If it is a string (uuid), we
       * print only one metadata.
       * @param {Object|string} params
       */
      this.metadataPrint = function(params) {
        var url = gnHttp.getService('mdGetPDF');
        if(angular.isObject(params) && params.sortBy) {
          url += '?sortBy=' + params.sortBy;
          if (params.sortOrder) {
            url += '&sortOrder=' + params.sortOrder;
          }
        }
        else if(angular.isString(params) ){
          url += '?uuid=' + params;
        }
        location.replace(url);
      };

      /**
       * Export one metadata to RDF format.
       * @param {string} uuid
       */
      this.metadataRDF = function(uuid) {
        var url = gnHttp.getService('mdGetRDF') + '?uuid=' + uuid;
        location.replace(url);
      };

      /**
       * Export to MEF format. If uuid is provided, export one metadata,
       * else export the whole selection.
       * @param {string} uuid
       */
      this.metadataMEF = function(uuid) {
        var url = gnHttp.getService('mdGetMEF') + '?version=2';
        url += angular.isDefined(uuid) ?
            '&uuid=' + uuid : '&format=full';

        location.replace(url);
      };

      this.exportCSV = function(uuid) {
        window.open(gnHttp.getService('csv'), windowName, windowOption);
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
