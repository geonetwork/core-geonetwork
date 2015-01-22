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
    'gnSearchSettings',
    function(gnSearchSettings) {

      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/search/resultsview/partials/' +
            'templateswitcher.html',
        scope: {
          'templateUrl': '='
        },
        link: function(scope, element, attrs, controller) {
          scope.tpls = gnSearchSettings.resultViewTpls;
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
    'gnOwsCapabilities',
    'gnSearchSettings',
    'gnMetadataActions',
    function($compile, gnMap, gnOwsCapabilities, gnSearchSettings,
             gnMetadataActions) {

      return {
        restrict: 'A',
        scope: true,
        link: function(scope, element, attrs, controller) {

          scope.mdService = gnMetadataActions;
          scope.map = scope.$eval(attrs.map);
          //scope.searchResults = scope.$eval(attrs.searchResults);

          /** Display fa icons for categories
           * TODO: Move to configuration */
          scope.catIcons = {
            featureCatalogs: 'fa-table',
            services: 'fa-cog',
            maps: 'fa-globe',
            staticMaps: 'fa-globe',
            datasets: 'fa-file',
            interactiveResources: 'fa-rss'
          };

          if (scope.map) {
            scope.hoverOL = new ol.FeatureOverlay({
              style: gnSearchSettings.olStyles.mdExtentHighlight
            });

            /**
             * Draw md bbox on search
             */
            var fo = new ol.FeatureOverlay({
              style: gnSearchSettings.olStyles.mdExtent
            });
            fo.setMap(scope.map);
          }

          scope.$watchCollection('searchResults.records', function(rec) {

            //scroll to top
            element.animate({scrollTop: top});

            // get md extent boxes
            if (scope.map) {
              fo.getFeatures().clear();

              if (!angular.isArray(rec) ||
                  angular.isUndefined(scope.map.getTarget())) {
                return;
              }
              for (var i = 0; i < rec.length; i++) {
                var feat = new ol.Feature();
                var extent = gnMap.getBboxFromMd(rec[i]);
                if (extent) {
                  var proj = scope.map.getView().getProjection();
                  extent = ol.extent.containsExtent(proj.getWorldExtent(),
                      extent) ?
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
              if (!ol.extent.isEmpty(extent)) {
                scope.map.getView().fitExtent(extent, scope.map.getSize());
              }
            }
          });

          scope.$watch('resultTemplate', function(templateUrl) {

            if (angular.isUndefined(templateUrl)) {
              return;
            }
            var template = angular.element(document.createElement('div'));
            template.attr({
              'ng-include': 'resultTemplate'
            });
            element.empty();
            element.append(template);
            $compile(template)(scope);
          });

          //TODO: remove this is defined in custom controllers
          scope.addToMap = function(link) {
            gnOwsCapabilities.getWMSCapabilities(link.url).then(
                function(capObj) {
                  var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                      link.name, capObj);
                  scope.$emit('addLayerFromMd', layerInfo);
                });

          };

          scope.zoomToMdExtent = function(md, map) {
            var extent = gnMap.getBboxFromMd(md);
            if (extent) {
              var proj = map.getView().getProjection();
              extent = ol.extent.containsExtent(proj.getWorldExtent(), extent) ?
                  ol.proj.transformExtent(extent, 'EPSG:4326', proj) :
                  proj.getExtent();
              map.getView().fitExtent(extent, map.getSize());
            }
          };


          if (scope.map) {
            scope.hoverOL.setMap(scope.map);
          }
        }
      };
    }]);

  /**
   * As we cannot use nested ng-repeat on a getLinksByType()
   * function, we have to load them once into the scope on rendering.
   */
  module.directive('gnFixMdlinks', [
    function() {

      return {
        restrict: 'A',
        scope: false,
        link: function(scope) {
          scope.links = scope.md.getLinksByType('LINK');
          scope.downloads = scope.md.getLinksByType('DOWNLOAD');
          scope.layers = scope.md.getLinksByType('OGC', 'kml');

        }
      };
    }]);

  module.directive('gnDisplayextentOnhover', [
    'gnMap',
    function(gnMap) {

      return {
        restrict: 'A',
        link: function(scope, element, attrs, controller) {

          //TODO : change, just apply a style to the feature when
          // featureoverlay is fixed
          var feat = new ol.Feature();

          element.bind('mouseenter', function() {

            var extent = gnMap.getBboxFromMd(scope.md);
            if (extent) {
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
    function(gnMap) {

      return {
        restrict: 'A',
        link: function(scope, element, attrs, controller) {

          element.bind('dblclick', function() {
            scope.zoomToMdExtent(scope.md, scope.map);
          });
        }
      };
    }]);

})();
