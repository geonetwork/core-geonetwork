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
            scope.hoverOL = new ol.layer.Vector({
              source: new ol.source.Vector(),
              style: gnSearchSettings.olStyles.mdExtentHighlight
            });

            /**
             * Draw md bbox on search
             */
            var fo = new ol.layer.Vector({
              source: new ol.source.Vector(),
              map: scope.map,
              style: gnSearchSettings.olStyles.mdExtent
            });
          }

          scope.$watchCollection('searchResults.records', function(rec) {

            //scroll to top
            element.animate({scrollTop: top});

            // get md extent boxes
            if (scope.map) {
              fo.getSource().clear();

              if (!angular.isArray(rec) ||
                  angular.isUndefined(scope.map.getTarget())) {
                return;
              }
              for (var i = 0; i < rec.length; i++) {
                var feat = gnMap.getBboxFeatureFromMd(rec[i],
                    scope.map.getView().getProjection());
                fo.getSource().addFeature(feat);
              }
              var extent = ol.extent.createEmpty();
              fo.getSource().forEachFeature(function(f) {
                var g = f.getGeometry();
                if (g) {
                  ol.extent.extend(extent, g.getExtent());
                }
              });
              if (!ol.extent.isEmpty(extent)) {
                scope.map.getView().fit(extent, scope.map.getSize());
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

          scope.zoomToMdExtent = function(md, map) {
            var feat = gnMap.getBboxFeatureFromMd(md,
                scope.map.getView().getProjection());
            if (feat) {
              map.getView().fit(
                  feat.getGeometry().getExtent(),
                  map.getSize());
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
  module.directive('gnFixMdlinks', [ 'gnSearchSettings',
    function(gnSearchSettings) {

      return {
        restrict: 'A',
        scope: false,
        link: function(scope) {
          var obj = gnSearchSettings.linkTypes;
          for(var p in obj) {
            scope[p] = scope.md.getLinksByType.apply(scope.md, obj[p]);
          }
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

            var feat = gnMap.getBboxFeatureFromMd(scope.md,
                scope.map.getView().getProjection());
            if (feat) {
              scope.hoverOL.getSource().addFeature(feat);
            }
          });

          element.bind('mouseleave', function() {
            scope.hoverOL.getSource().clear();
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
