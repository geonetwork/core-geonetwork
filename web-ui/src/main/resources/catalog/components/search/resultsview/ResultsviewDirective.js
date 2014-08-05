(function() {
  goog.provide('gn_resultsview');

  var module = angular.module('gn_resultsview', []);

  /**
   * @ngdoc directive
   * @name gn_resultsview.directive:gnResultsTplSwitcher
   *
   * @restrict A
   *
   * @description
   * The `gnResultsTplSwitcher` directive provides a button group
   * switcher to switch between customized views. The customs views
   * are defined in the scope variable `scope.tpls`. This config
   * defines a icon for the button, and a link to the html file
   * representing the desired view.
   *
   * The `gnResultsTplSwitcher` directive is used with the `gnResultsContainer`
   * directive that will load the custom view into its root html
   * element.
   */
  module.directive('gnResultsTplSwitcher', [
    'gnFacetService',
    function(gnFacetService) {

      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/search/resultsview/partials/' +
            'templateswitcher.html',
        scope: {
          'templateUrl': '='
        },
        link: function(scope, element, attrs, controller) {
          scope.tpls = [{
            tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/title.html',
            tooltip: 'Simple',
            icon: 'fa-list'
          }, {
            tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/thumb.html',
            tooltip: 'Thumbnails',
            icon: 'fa-th-list'
          }]
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_resultsview.directive:gnResultsContainer
   *
   * @restrict A
   *
   * @description
   * The `gnResultsContainer` directive is used to load a custom
   * view (defined by a html file path) into its root element.
   */
  module.directive('gnResultsContainer', [
      '$compile',
      'gnMap',
      'gnOlStyles',
    function($compile, gnMap, gnOlStyles) {

      return {
        restrict: 'A',
        scope: {
          searchResults: '=',
          templateUrl: '=',
          map: '='
        },
        link: function(scope, element, attrs, controller) {

          /** Display fa icons for categories */
          scope.catIcons = {
            featureCatalogs: 'fa-table',
            services: 'fa-cog',
            maps: 'fa-globe',
            staticMaps: 'fa-globe',
            datasets: 'fa-file',
            interactiveResources: 'fa-rss'
          };

          scope.hoverOL = new ol.FeatureOverlay({
            style: gnOlStyles.bbox
          });

          /**
           * Draw md bbox on search
           */
          var fo = new ol.FeatureOverlay();
          fo.setMap(scope.map);

          scope.$watchCollection('searchResults.records', function(rec) {
            fo.getFeatures().clear();
            if (!angular.isArray(rec)) {
              return;
            }
            for(var i=0;i<rec.length;i++) {
              var feat = new ol.Feature();
              var extent = gnMap.getBboxFromMd(rec[i]);
              if(extent) {
                var proj = scope.map.getView().getProjection();
                extent = ol.extent.containsExtent(proj.getWorldExtent(), extent) ?
                    ol.proj.transformExtent(extent, 'EPSG:4326', proj) :
                    proj.getExtent();
                var coords = gnMap.getPolygonFromExtent(extent);
                feat.setGeometry(new ol.geom.Polygon(coords));
                fo.addFeature(feat);
              }
            }
            var extent = ol.extent.createEmpty();
            fo.getFeatures().forEach(function(f) {
              ol.extent.extend(extent, f.getGeometry().getExtent());
            });
            if(!ol.extent.isEmpty(extent)) {
              scope.map.getView().fitExtent(extent, scope.map.getSize());
            }
          });

          scope.$watch('templateUrl', function(templateUrl) {
            if (angular.isUndefined(templateUrl)) {
              return;
            }
            var template = angular.element(document.createElement('div'))
            template.attr({
              'ng-include': 'templateUrl'
            });
            element.empty();
            element.append(template);
            $compile(template)(scope);
          });

          scope.hoverOL.setMap(scope.map);
        }
      };
    }]);

  module.directive('gnDisplayextentOnhover', [
    'gnMap',
    function(gnMap) {

      return {
        restrict: 'A',
        link: function(scope, element, attrs, controller) {

          //TODO : change, just apply a style to the feature when featureoverlay is fixed
          var feat = new ol.Feature();

          element.bind('mouseenter', function() {

            var extent = gnMap.getBboxFromMd(scope.md);
            if(extent) {
              var proj = scope.map.getView().getProjection();
              extent = ol.extent.containsExtent(proj.getWorldExtent(), extent) ?
                  ol.proj.transformExtent(extent, 'EPSG:4326', proj) :
                  proj.getExtent();
              var coords = gnMap.getPolygonFromExtent(extent);
              feat.setGeometry(new ol.geom.Polygon(coords));
              scope.hoverOL.addFeature(feat);
            }
          });

          element.bind('mouseleave', function() {
            scope.hoverOL.getFeatures().clear();
          });
        }
      };
    }]);

  module.directive('gnZoomtoOnclick', [
    'gnMap',
    'gnOlStyles',
    function(gnMap, gnOlStyles) {

      return {
        restrict: 'A',
        link: function(scope, element, attrs, controller) {

          var fo = new ol.FeatureOverlay({
            style: gnOlStyles.bbox
          });
          var feat = new ol.Feature();

          element.bind('dblclick', function() {

            var extent = gnMap.getBboxFromMd(scope.md);
            if(extent) {
              var proj = scope.map.getView().getProjection();
              extent = ol.extent.containsExtent(proj.getWorldExtent(), extent) ?
                  ol.proj.transformExtent(extent, 'EPSG:4326', proj) :
                  proj.getExtent();
              scope.map.getView().fitExtent(extent, scope.map.getSize());
            }
          });
        }
      };
    }]);
})();
