(function() {
  goog.provide('gn_mdactions_service');
  goog.require('gn_share');


  var module = angular.module('gn_mdactions_service', [
    'gn_share'
  ]);

  module.service('gnMetadataActions', [
    'gnHttp',
    'gnMetadataManager',
    'gnAlertService',
    'gnPopup',
    function(gnHttp, gnMetadataManager, gnAlertService, gnPopup) {

      var windowName = 'geonetwork';
      var windowOption = '';

      var alertResult = function(msg) {
        gnAlertService.addAlert({
          msg: msg,
          type: 'success'
        });
      };

      var openModal = function(content) {
        gnPopup.create({
          title: 'privileges',
          content: content
        });
      };

      var callBatch = function(service) {
        gnHttp.callService(service).then(function(data) {
          alertResult(data.data);
        });
      };

      /**
       * Duplicate a metadata that can be a new child of the source one.
       * @param {string} id
       * @param {boolean} child
       */
      var duplicateMetadata = function(id, child) {
        var url = 'catalog.edit#/';
        if (id) {
          if (child) {
            url += 'create?childOf=' + id;
          } else {
            url += 'create?from=' + id;
          }
        }
        window.open(url, '_blank');
      };

      /**
       * Export as PDF (one or selection). If params is search object, we check
       * for sortBy and sortOrder to process the print. If it is a string
       * (uuid), we print only one metadata.
       * @param {Object|string} params
       */
      this.metadataPrint = function(params) {
        var url;
        if (angular.isObject(params) && params.sortBy) {
          url = gnHttp.getService('mdGetPDFSelection');
          url += '?sortBy=' + params.sortBy;
          if (params.sortOrder) {
            url += '&sortOrder=' + params.sortOrder;
          }
        }
        else if (angular.isString(params)) {
          url = gnHttp.getService('mdGetPDF');
          url += '?uuid=' + params;
        }
        if (url) {
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
        if (md) {
          gnMetadataManager.remove(md.getId());
        }
        else {
          // TODO: see how to manage the refresh
          callBatch('mdDeleteBatch').then(function() {
            gnHttp.callService('mdSelect', {
              selected: 'remove-all'
            });
          });
        }
      };

      this.openPrivilegesPanel = function(md, scope) {
        gnPopup.create({
          title: 'privileges',
          content: '<div gn-share="' + md.getId() + '"></div>'
        }, scope);
      };

      this.openPrivilegesBatchPanel = function(scope) {
        gnPopup.create({
          title: 'privileges',
          content: '<div gn-share="" gn-share-batch="true"></div>'
        }, scope);
      };

      /**
       * Duplicate the given metadata. Open the editor in new page.
       * @param {string} md
       */
      this.duplicate = function(md) {
        duplicateMetadata(md.getId(),false);
      };

      /**
       * Create a child of the given metadata. Open the editor in new page.
       * @param {string} md
       */
      this.createChild = function(md) {
        duplicateMetadata(md.getId(),true);
      };

      /**
       * Update publication on metadata (one or selection).
       * If a md is provided, it update publication of the given md, depending
       * on its current state. If no metadata is given, it updates the
       * publication on all selected metadata to the given flag (on|off).
       * @param {Object|undefined} md
       * @param {string} flag
       * @return {*}
       */
      this.publish = function(md, flag) {

        if (md) {
          flag = md.isPublished() ? 'off' : 'on';
        }
        var publishFlag = {
          _1_0: flag,
          _1_1: flag,
          _1_5: flag,
          _1_6: flag
        };

        if (angular.isDefined(md)) {
          return gnHttp.callService('mdPrivileges', angular.extend(
              publishFlag, {
                update: true,
                id: md.getId()
              })).then(function(data) {
            alertResult('publish');
            md.publish();
          });
        } else {
          return gnHttp.callService('mdPrivilegesBatch', publishFlag);
        }
      };
    }]);
})();
