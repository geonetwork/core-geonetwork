(function () {
  goog.provide('gn_ncwms_directive');

  var module = angular.module('gn_ncwms_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_ncwms_directive.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive('gnNcwmsTransect', [
      'gnHttp',
      'gnNcWms',
      'gnPopup',
    function (gnHttp, gnNcWms, gnPopup) {
      return {
        restrict: 'A',
        scope: {
          layer: '=',
          map:'='
        },
        templateUrl: '../../catalog/components/viewer/ncwms/' +
          'partials/ncwmstools.html',
        link: function (scope, element, attrs) {

          var drawInteraction, featureOverlay;
          scope.constants = gnNcWmsConst;

          // 2010-12-06T12:00:00.000Z/2010-12-31T12:00:00.000Z
          var parseTimeSeries = function(s) {
            s = s.trim();
            var as = s.split('/');
            scope.tsfromD = moment(new Date(as[0])).format('YYYY-MM-DD');
            scope.tstoD = moment(new Date(as[1])).format('YYYY-MM-DD');
          };

          var getDimensionValue = function(ncInfo, name) {
            var value;
            if (angular.isArray(ncInfo.Dimension)) {
              for (var i = 0; i < ncInfo.Dimension.length; i++) {
                if (ncInfo.Dimension[i].name == name) {
                  value = ncInfo.Dimension[i].values;
                  break;
                }
              }
            }
            else if (angular.isObject(ncInfo.Dimension) &&
                ncInfo.Dimension.name == name) {
              value = ncInfo.Dimension.values[0] ||
                  ncInfo.Dimension.values;
            }
            return value;
          };
          parseTimeSeries(getDimensionValue(scope.layer.ncInfo, 'time'));

          scope.elevations = getDimensionValue(scope.layer.ncInfo, 'elevation').split(',');

          /**
           * Just manage active button in ui.
           * Values of activeTool can be 'time', 'profile', 'transect'
           * @param activeTool
           */
          scope.setActiveTool = function(activeTool) {
            if(scope.activeTool == activeTool) {
              scope.activeTool = undefined;
            } else {
              scope.activeTool = activeTool;
            }
            activateInteraction(scope.activeTool);
          };

          var resetInteraction = function() {
            if(featureOverlay) {
              featureOverlay.setMap(null);
              delete featureOverlay;
            }
            if(drawInteraction) {
              scope.map.removeInteraction(drawInteraction);
              delete drawInteraction;
            };
          };

          var activateInteraction = function(activeTool) {
            if(!activeTool) {
              resetInteraction();
            }
            else {
              var type = 'Point';
              if(activeTool == 'transect') {
                type = 'LineString';
              }

              if(!featureOverlay) {
                featureOverlay = new ol.FeatureOverlay();
                featureOverlay.setMap(scope.map);
              }
              if(drawInteraction) {
                scope.map.removeInteraction(drawInteraction);
              }

              drawInteraction = new ol.interaction.Draw({
                features: featureOverlay.getFeatures(),
                type: type
              });

              drawInteraction.on('drawend',
                  function(evt) {

                    gnPopup.create({
                      title: activeTool,
                      url : gnNcWms.getNcwmsServiceUrl(
                          scope.layer,
                          scope.map.getView().getProjection(),
                          evt.feature.getGeometry().getCoordinates(),
                          activeTool),
                      content: '<div class="gn-popup-iframe" style="width:400px">' +
                          '<iframe frameBorder="0" border="0" style="width:100%;height:100%;" src="{{options.url}}" ></iframe>' +
                          '</div>'
                    });
                    scope.$apply(function() {
                      scope.activeTool = undefined;
                    });
                  }, this);

              scope.map.addInteraction(drawInteraction);
            }
          };
          var disableInteractionWatchFn = function(nv, ov) {
            if(!nv) {
              resetInteraction();
              scope.activeTool = undefined;
            }
          };
          scope.$watch('layer.showInfo', disableInteractionWatchFn);
          scope.$watch('layer.visible', disableInteractionWatchFn);
          scope.$watch('layer', disableInteractionWatchFn);

          /**
           * init source layer params object
           */
          var initNcwmsParams = function() {

            scope.params = scope.layer.getSource().getParams() || {};

            var colors = scope.layer.getSource().getParams().COLORSCALERANGE.split(',');
            scope.colorscalerange = [colors[0], colors[1]];

            if(angular.isUndefined(scope.params.LOGSCALE)) {
              scope.params.LOGSCALE = false;
            }
          };

          scope.onColorscaleChange = function(v) {
            var colorange;
            if(angular.isString(v) && v == 'auto') {
              colorange = v;
            }
            else if(angular.isArray(v) && v.length == 2) {
              colorange = v[0] + ',' + v[1];
            }
            if(colorange) {
              scope.params.COLORSCALERANGE = colorange;
              scope.updateLayerParams();
            }
          };

          scope.updateLayerParams = function() {
            scope.layer.getSource().updateParams(scope.params);
          };

          initNcwmsParams();
        }
      };
    }]);
})();
