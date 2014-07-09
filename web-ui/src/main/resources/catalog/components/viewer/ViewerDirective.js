(function () {
  goog.provide('gn_viewer_directive');

  var module = angular.module('gn_viewer_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive('gnMainViewer', ['gnHttp',
    function (gnHttp) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/' +
          'partials/mainviewer.html',
        link: function (scope, element, attrs) {
        }
      };
    }])
    .directive('gnMap',
    function () {
      return {
        restrict: 'A',
        scope: {
          map: '=gnMapMap'
        },
        link: function (scope, element, attrs) {
          scope.map.setTarget('map');
        }
      };
    });

})();
