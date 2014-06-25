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
      'gnNcWmsConst',
    function (gnHttp, gnNcWms, gnNcWmsConst) {
      return {
        restrict: 'A',
        scope: {
          layer: '=',
          map:'='
        },
        templateUrl: '../../catalog/components/viewer/ncwms/' +
          'partials/ncwmstools.html',
        link: function (scope, element, attrs) {

          goog.asserts.assertInstanceof(scope.layer, ol.layer.Layer);
          goog.asserts.assertInstanceof(scope.map, ol.Map);

          var drawInteraction, featureOverlay;
          scope.constants = gnNcWmsConst;

          // 2010-12-06T12:00:00.000Z/2010-12-31T12:00:00.000Z
          var parseTimeSeries = function(s) {
            s = s.trim();
            var as = s.split('/');
            scope.tsfromD = moment(new Date(as[0])).format('YYYY-MM-DD');
            scope.tstoD = moment(new Date(as[1])).format('YYYY-MM-DD');
          };
          parseTimeSeries(scope.layer.ncInfo.dimensions.time.values[0]);

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


          var activateInteraction = function(activeTool) {
            if(!activeTool) {
              featureOverlay.setMap(null);
              delete featureOverlay;
              scope.map.removeInteraction(drawInteraction);
              delete drawInteraction;
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
                    window.open(gnNcWms.getNcwmsServiceUrl(
                            scope.layer,
                            scope.map.getView().getProjection(),
                            evt.feature.getGeometry().getCoordinates(),
                            activeTool),
                        '_blank',
                        'menubar=no, status=no, scrollbars=no, menubar=no, width=600, height=400');
                  }, this);

              scope.map.addInteraction(drawInteraction);
            }
          };

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

  /*
   jQuery UI Slider plugin wrapper
   */
  module.value('uiSliderConfig',{}).directive('uiSlider', ['uiSliderConfig', '$timeout', function(uiSliderConfig, $timeout) {
    uiSliderConfig = uiSliderConfig || {};
    return {
      require: 'ngModel',
      compile: function () {
        return function (scope, elm, attrs, ngModel) {

          function parseNumber(n, decimals) {
            return (decimals) ? parseFloat(n) : parseInt(n);
          };

          var options = angular.extend(scope.$eval(attrs.uiSlider) || {}, uiSliderConfig);
          // Object holding range values
          var prevRangeValues = {
            min: null,
            max: null
          };

          // convenience properties
          var properties = ['min', 'max', 'step'];
          var useDecimals = (!angular.isUndefined(attrs.useDecimals)) ? true : false;

          var init = function() {
            // When ngModel is assigned an array of values then range is expected to be true.
            // Warn user and change range to true else an error occurs when trying to drag handle
            if (angular.isArray(ngModel.$viewValue) && options.range !== true) {
              console.warn('Change your range option of ui-slider. When assigning ngModel an array of values then the range option should be set to true.');
              options.range = true;
            }

            // Ensure the convenience properties are passed as options if they're defined
            // This avoids init ordering issues where the slider's initial state (eg handle
            // position) is calculated using widget defaults
            // Note the properties take precedence over any duplicates in options
            angular.forEach(properties, function(property) {
              if (angular.isDefined(attrs[property])) {
                options[property] = parseNumber(attrs[property], useDecimals);
              }
            });

            elm.slider(options);
            init = angular.noop;
          };

          // Find out if decimals are to be used for slider
          angular.forEach(properties, function(property) {
            // support {{}} and watch for updates
            attrs.$observe(property, function(newVal) {
              if (!!newVal) {
                init();
                elm.slider('option', property, parseNumber(newVal, useDecimals));
                ngModel.$render();
              }
            });
          });
          attrs.$observe('disabled', function(newVal) {
            init();
            elm.slider('option', 'disabled', !!newVal);
          });

          // Watch ui-slider (byVal) for changes and update
          scope.$watch(attrs.uiSlider, function(newVal) {
            init();
            if(newVal != undefined) {
              elm.slider('option', newVal);
            }
          }, true);

          // Late-bind to prevent compiler clobbering
          $timeout(init, 0, true);

          // Update model value from slider
          elm.bind('slide', function(event, ui) {
            ngModel.$setViewValue(ui.values || ui.value);
            scope.$apply();
          });

          // Update slider from model value
          ngModel.$render = function() {
            init();
            var method = options.range === true ? 'values' : 'value';

            if (!options.range && isNaN(ngModel.$viewValue) && !(ngModel.$viewValue instanceof Array)) {
              ngModel.$viewValue = 0;
            }
            else if (options.range && !angular.isDefined(ngModel.$viewValue)) {
              ngModel.$viewValue = [0,0];
            }

            // Do some sanity check of range values
            if (options.range === true) {

              // Check outer bounds for min and max values
              if (angular.isDefined(options.min) && options.min > ngModel.$viewValue[0]) {
                ngModel.$viewValue[0] = options.min;
              }
              if (angular.isDefined(options.max) && options.max < ngModel.$viewValue[1]) {
                ngModel.$viewValue[1] = options.max;
              }

              // Check min and max range values
              if (ngModel.$viewValue[0] > ngModel.$viewValue[1]) {
                // Min value should be less to equal to max value
                if (prevRangeValues.min >= ngModel.$viewValue[1])
                  ngModel.$viewValue[0] = prevRangeValues.min;
                // Max value should be less to equal to min value
                if (prevRangeValues.max <= ngModel.$viewValue[0])
                  ngModel.$viewValue[1] = prevRangeValues.max;
              }

              // Store values for later user
              prevRangeValues.min = ngModel.$viewValue[0];
              prevRangeValues.max = ngModel.$viewValue[1];

            }
            elm.slider(method, ngModel.$viewValue);
          };

          scope.$watch(attrs.ngModel, function() {
            if (options.range === true) {
              ngModel.$render();
            }
          }, true);

          function destroy() {
            elm.slider('destroy');
          }
          elm.bind('$destroy', destroy);
        };
      }
    };
  }]);
})();
