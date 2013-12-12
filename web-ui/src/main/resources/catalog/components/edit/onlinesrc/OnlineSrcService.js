(function() {
  goog.provide('gn_onlinesrc_service');

  var module = angular.module('gn_onlinesrc_service', [
  ]);

  module.factory('gnOnlinesrc', [
    'gnBatchProcessing',
    'gnHttp',
    'gnMetadataManagerService',
    '$q',
    function(gnBatchProcessing, gnHttp, gnMetadataManagerService, $q) {

      /**
     * Prepare batch process request parameters.
     *   - get parameters from onlinesrc form
     *   - add process name
     *   - encode URL
     *   - update name and desc if we add layers
     */
      var setParams = function(processName, formParams) {
        var params = angular.copy(formParams);
        angular.extend(params, {
          process: processName
        });
        if (!angular.isUndefined(params.url)) {
          params.url = encodeURIComponent(params.url);
        }
        return setLayersParams(params);
      };

      /**
      * Prepare name and description parameters
      * if we are adding resource with layers.
      *
      * Parse all selected layers, extract name
      * and title to build name and desc params like
      *   name : name1,name2,name3
      *   desc : title1,title2,title3
      */
      var setLayersParams = function(params) {
        if (angular.isArray(params.layers) &&
            params.layers.length > 0) {
          var names = [],
              descs = [];

          angular.forEach(params.layers, function(layer) {
            names.push(layer.name);
            descs.push(encodeURIComponent(layer.title));
          });

          angular.extend(params, {
            name: names.join(','),
            desc: descs.join(',')
          });
        }
        delete params.layers;
        return params;
      };

      /**
       * Parse XML result of md.relations.get service.
       * Return an array of relations objects
       */
      var parseRelations = function(data) {

        var relations = {};
        var xmlDoc = $.parseXML(data);
        var $xml = $(xmlDoc);
        var xmlRel = $xml.find('relation');

        angular.forEach(xmlRel, function(rel) {

          var getChildValue = function(node, id) {
            if (node.getElementsByTagName(id) &&
                node.getElementsByTagName(id)[0]) {
              return rel.getElementsByTagName(id)[0].textContent;
            }
            return undefined;
          };

          var type = rel.getAttribute('type');
          if (!relations[type]) {
            relations[type] = [];
          }
          relations[type].push({
            id: getChildValue(rel, 'id'),
            uuid: getChildValue(rel, 'uuid'),
            title: getChildValue(rel, 'title'),
            'abstract': getChildValue(rel, 'abstract'),
            type: type,
            subtype: rel.getAttribute('subtype')
          });
        });
        return relations;
      };

      /**
       * gnOnlinesrc service PUBLIC METHODS
       * - getAllResources
       * - addOnlinesrc
       * - linkToParent
       * - linkToDataset
       * - linkToService
       * - removeThumbnail
       *******************************************
       */
      return {

        /**
         * This value is watched from gnOnlinesrcList directive
         * to reload online resources list when it is true
         */
        reload: false,

        /**
         * Get all online resources for the current edited
         * metadata.
         */
        getAllResources: function() {

          var defer = $q.defer();

          gnHttp.callService('getRelations', {
            fast: false,
            id: gnMetadataManagerService.getCurrentEdit().metadataId
          }, {
            method: 'post',
            headers: {
              'Content-type': 'application/xml'
            }
          }).success(function(data) {
            defer.resolve(parseRelations(data));
          });
          return defer.promise;
        },

        /**
         * Prepare parameters and call batch
         * request from the gnBatchProcessing service
         */
        addOnlinesrc: function(params) {
          return gnBatchProcessing.runProcessMd(
              setParams('onlinesrc-add', params));
        },

        /**
         *
         */
        addThumbnailByURL: function(params) {
          return gnBatchProcessing.runProcessMd(
              setParams('thumbnail-add', params));
        },
        /**
         * Call md.processing.new in mode 'parent-add'
         * to link a parent to the edited metadata
         *
         * records is an array of metadatas.
         */
        linkToParent: function(records) {
          if (records && records[0]) {
            return gnBatchProcessing.runProcessMd({
              parentUuid: records[0]['geonet:info'].uuid,
              process: 'parent-add'
            });
          }
        },

        linkToDataset: function(params) {
          var qParams = setParams('dataset-add', params);

          return gnBatchProcessing.runProcessMdXml({
            scopedName: qParams.name,
            //            uuidref: gnMetadataManagerService.
            //            getCurrentEdit().metadataUuid,
            uuidref: '424d86a7-1d73-4e4b-bf50-c670936eb086',
            uuid: qParams.uuid,
            process: qParams.process
          });
        },

        /**
         * Call md.processing.new in mode 'parent-add'
         * to link a service to the edited metadata
         */
        linkToService: function(params) {
          var qParams = setParams('service-add', params);

          qParams.scopedName = qParams.name;
          delete qParams.name;

          qParams.uuidref = qParams.uuid;
          delete qParams.uuid;

          return gnBatchProcessing.runProcessMd(qParams);
        },

        /**
         * Remove a thumbnail from metadata.
         * Type large or small is specified in parameter.
         * The onlinesrc panel is reloaded after removal.
         */
        removeThumbnail: function(thumb) {
          var scope = this;

          // It is a url thumbnail
          if (thumb.id.indexOf('resources.get') < 0) {
            gnBatchProcessing.runProcessMd(
                setParams('thumbnail-remove', {
                  id: gnMetadataManagerService.
                      getCurrentEdit().metadataId,
                  thumbnail_url: thumb.id
                })).then(function() {
              scope.reload = true;
            });
          }
          // It is an uploaded tumbnail
          else {
            gnHttp.callService('removeThumbnail', {
              type: (thumb.title === 'thumbnail' ? 'small' : 'large'),
              id: gnMetadataManagerService.getCurrentEdit().metadataId,
              version: $(gnMetadataManagerService.getCurrentEdit().
                  formId).find('input[id="version"]').val()
            }).success(function() {
              // Reload online resources list
              scope.reload = true;
            });
          }
        },

        removeOnlinesrc: function(onlinesrc) {
          var scope = this;

          gnBatchProcessing.runProcessMd(
              setParams('onlinesrc-remove', {
                id: gnMetadataManagerService.
                    getCurrentEdit().metadataId,
                url: onlinesrc.id,
                name: onlinesrc['abstract']
              })).then(function() {
            scope.reload = true;
          });
        }

      };
    }]);
})();
