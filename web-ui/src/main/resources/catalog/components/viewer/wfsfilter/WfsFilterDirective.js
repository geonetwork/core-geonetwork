(function() {
  goog.provide('gn_wfsfilter_directive');

  var module = angular.module('gn_wfsfilter_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacets
   *
   * @description
   */
  module.directive('gnWfsFilterFacets', [
      '$http', 'wfsFilterService', '$q',

    function($http, wfsFilterService, $q) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
            'partials/wfsfilterfacet.html',
        scope: {
          featureTypeName: '@',
          uuid: '@',
          wfsUrl: '@',
          layer: '='
        },
        link: function(scope, element, attrs) {
          scope.contentCollapsed = true;
          /**
           * Create SOLR request to get facets values
           * Check if the feature has an applicationDefinition, else get the
           * indexed fields for the Feature. From this, build the solr request
           * and retrieve the facet config from solr response.
           * This config is stored in `scope.fields` and is used to build
           * the facet UI.
           */
          wfsFilterService.getApplicationProfile(scope.uuid,
              scope.featureTypeName, scope.wfsUrl).success(function(data) {

            var url;
            var defer = $q.defer();
            if(data) {
              url = wfsFilterService.getSolrRequestFromApplicationProfile(
                  data, scope.featureTypeName, scope.wfsUrl);
              defer.resolve(url);
            }
            else {
              wfsFilterService.getWfsIndexFields(
                  scope.featureTypeName, scope.wfsUrl).then(function(fields) {
                    url = wfsFilterService.getSolrRequestFromFields(
                        fields, scope.featureTypeName, scope.wfsUrl);
                    defer.resolve(url);
                  });
            }
            defer.promise.then(function(url) {
                  wfsFilterService.getFacetsConfigFromSolr(url).
                      then(function(facetConfig) {
                        // Describe facets configuration to build the ui
                        scope.fields = facetConfig;
                      });
                }
            );
          });

          // output structure to send to filter service
          scope.output = {};

          /**
           * Update the state of the facet search.
           * The `scope.output` structure represent the state of the facet
           * checkboxes form.
           *
           * @param fieldName index field name
           * @param facetKey facet key for this field
           * @param type facet type
           */
          scope.onCheckboxClick = function(fieldName, facetKey, type) {
            var output = scope.output;
            if(output[fieldName]) {
              if(output[fieldName].values[facetKey]) {
                delete output[fieldName].values[facetKey];
                if(Object.keys(output[fieldName].values).length == 0) {
                  delete output[fieldName];
                }
              }
              else {
                output[fieldName].values[facetKey] = true;
              }
            }
            else {
              output[fieldName] = {
                type: type,
                values: {}
              }
              output[fieldName].values[facetKey] = true;
            }
          };

          /**
           * On filter click, build from the UI the SLD rules config object
           * that will be send to generateSLD service.
           */
          scope.filter = function() {
            var defer = $q.defer();
            var sldConfig = wfsFilterService.createSLDConfig(scope.output);
            if (sldConfig.filters.length > 0) {
              wfsFilterService.getSldUrl(sldConfig, scope.wfsUrl,
                  scope.featureTypeName).success(function(data) {
                    scope.layer.getSource().updateParams({
                      SLD: data.value
                    });
              }).finally(function() {
                    defer.resolve();
                  });
            } else {
              scope.layer.getSource().updateParams({
                SLD: null
              });
              defer.resolve();
            }
            return defer.promise;
          };
        }
      };
    }]);
})();
