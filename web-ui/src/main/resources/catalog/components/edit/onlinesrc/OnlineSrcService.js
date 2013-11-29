(function() {
  goog.provide('gn_onlinesrc_service');

  var module = angular.module('gn_onlinesrc_service', [
  ]);

  module.factory('gnOnlinesrc', ['gnBatchProcessing',
    function(gnBatchProcessing) {
    
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
          process: processName,
          url: encodeURIComponent(params.url)
        });
        return setLayersParams(params);
      }
     
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
        if(angular.isArray(params.layers) &&
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
      
      return {
        getAllResources: function() {

          // TODO
          $http({method: 'post', url: 'md.relations.get@json',
            headers: {'Content-type': 'application/xml'},
            params: {
              fast: false
            }}).success(function(data) {
          });
        },

        /**
           * Call md.processing.new in mode 'parent-add'
           * to link a parent to the edited metadata
           */
        linkToParent: function(records) {
          if (records) {
            gnBatchProcessing.runProcessMd({
              parentUuid: records[0]['geonet:info'].uuid,
              process: 'parent-add'
            }).then(function() {
              //TODO close modal
            });
          }
        },
        
        /**
         * Prepare parameters and call batch
         * request from the gnBatchProcessing service
         */
        addOnlinesrc: function(params) {
          gnBatchProcessing.runProcessMd(
              setParams('onlinesrc-add',params));
        }
      };
    }]);
})();
