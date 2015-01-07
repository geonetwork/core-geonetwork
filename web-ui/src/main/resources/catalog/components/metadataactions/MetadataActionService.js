(function() {
  goog.provide('gn_mdactions_service');

  var module = angular.module('gn_mdactions_service', []);

  module.service('gnMetadataActions', [
    'gnHttp',
    'gnMetadataManager',
    'gnAlertService',
    function(gnHttp, gnMetadataManager, gnAlertService) {

      var windowName = 'geonetwork';
      var windowOption = '';

      var alertResult = function(msg) {
        gnAlertService.addAlert({
          msg: msg,
          type: 'success'
        });
      };

      var callBatch = function(service) {
        gnHttp.callService(service).then(function(data) {
          alertResult(data.data);
        });
      };

      /**
       * Export as PDF (one or selection). If params is search object, we check
       * for sortBy and sortOrder to process the print. If it is a string
       * (uuid), we print only one metadata.
       * @param {Object|string} params
       */
      this.metadataPrint = function(params) {
        var url;
        if(angular.isObject(params) && params.sortBy) {
          url = gnHttp.getService('mdGetPDFSelection');
          url += '?sortBy=' + params.sortBy;
          if (params.sortOrder) {
            url += '&sortOrder=' + params.sortOrder;
          }
        }
        else if(angular.isString(params) ){
          url = gnHttp.getService('mdGetPDF');
          url += '?uuid=' + params;
        }
        if(url) {
          location.replace(url);
        }
        else {
          console.error('Error while exporting PDF');
        }
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
       * Export to MEF format (one or selection). If uuid is provided, export
       * one metadata, else export the whole selection.
       * @param {string} uuid
       */
      this.metadataMEF = function(uuid) {
        var url = gnHttp.getService('mdGetMEF') + '?version=2';
        url += angular.isDefined(uuid) ?
            '&uuid=' + uuid : '&format=full';

        location.replace(url);
      };

      this.exportCSV = function() {
        window.open(gnHttp.getService('csv'), windowName, windowOption);
      };

      this.deleteMd = function(md) {
        if(md) {
          gnMetadataManager.remove(md.getId());
        }
        else {
          callBatch('mdDeleteBatch');
        }
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
