(function() {
  goog.provide('gn_searchlayerformap_directive');

  var module = angular.module('gn_searchlayerformap_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnSearchLayerForMap
   *
   * @description
   * Panel to search for WMS layer describe in the catalog
   */
  module.directive('gnSearchLayerForMap', [
    'gnOwsCapabilities',
    'gnMap',
    '$translate',
    'Metadata',
    'gnRelatedResources',
    'gnSearchSettings',
    'gnGlobalSettings',
    function(gnOwsCapabilities, gnMap, $translate, Metadata,
             gnRelatedResources, gnSearchSettings, gnGlobalSettings) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/searchlayerformap/' +
            'partials/searchlayerformap.html',
        scope: {
          map: '=gnSearchLayerForMap'
        },
        controller: ['$scope',
          function($scope) {
            $scope.searchObj = {
              permalink: false,
              hitsperpageValues: gnSearchSettings.hitsperpageValues,
              sortbyValues: gnSearchSettings.sortbyValues,
              params: {
                protocol: 'OGC:WMS*',
                from: 1,
                to: 9
              }
            };
            $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

            $scope.paginationInfo = {
              hitsPerPage: gnSearchSettings.hitsperpageValues[0]
            };

          }],
        link: function(scope, element, attrs) {
          scope.filterTopic = function(topic) {
            delete scope.searchObj.params.topicCat;
            scope.searchObj.params.topicCat = topic;
          };
          scope.addToMap = function(link) {
            gnRelatedResources.getAction('WMS')(link);
          };
          scope.zoomToLayer = function(md) {
            var extent = gnMap.getBboxFromMd(md);
            if (extent) {
              var proj = scope.map.getView().getProjection();
              extent = ol.extent.containsExtent(proj.getWorldExtent(), extent) ?
                  ol.proj.transformExtent(extent, 'EPSG:4326', proj) :
                  proj.getExtent();
              scope.map.getView().fit(extent, scope.map.getSize());
            }
          };
          scope.getMetadata = function(md) {
            var m = new Metadata(md);
            m.relevantLinks = m.getLinksByType('WMS');
            return m;
          };
        }
      };
    }]);
})();
