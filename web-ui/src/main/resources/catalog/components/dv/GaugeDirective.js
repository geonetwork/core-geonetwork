(function() {
  goog.provide('gn_gauge_directive');

  var module = angular.module('gn_gauge_directive', []);

  module.directive('gnGauge', function() {
    var gauges = [];

    /**
         * Create and render a gauge
         */
    function createGauge(config) {
      var gauge, gaugeConfig = {
        size: config.size || 120,
        label: config.label,
        min: config.min || 0,
        max: config.max || 100,
        minorTicks: config.ticks || 5
      };

      var range = gaugeConfig.max - gaugeConfig.min;
      gaugeConfig.yellowZones = [{
        from: gaugeConfig.min + range * 0.75,
        to: gaugeConfig.min + range * 0.9
      }];
      gaugeConfig.redZones = [{
        from: gaugeConfig.min + range * 0.9,
        to: gaugeConfig.max
      }];

      gauge = new Gauge(config.id, gaugeConfig);
      gauge.render();
      return gauge;
    }


    return {
      restrict: 'A',
      require: '?ngModel',
      link: function(scope, element, attrs) {
        // TODO : How to handle directive constraints ?
        if (!attrs.id) {
          console.log('Define an ID attribute to locate ' +
                  'the target element to put the gauge in.');
        }
        scope.$watch(attrs.ngModel, function() {
          var value = scope.$eval(attrs.gnGaugeValue);
          if (value) {
            scope.gauges[attrs.id] = createGauge({id: attrs.id,
              label: attrs.gnGauge,
              min: scope.$eval(attrs.gnGaugeMin),
              max: scope.$eval(attrs.gnGaugeMax)
            });
            scope.gauges[attrs.id].redraw(value);
          }
        });
      }
    };
  });
})();
