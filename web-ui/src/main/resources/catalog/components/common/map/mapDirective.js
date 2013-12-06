(function() {
  goog.provide('gn_map_directive');

  angular.module('gn_map_directive', [])

    .directive(
      'gnDrawBbox',
      [
       function() {
         return {
           restrict: 'A',
           templateUrl: '../../catalog/components/common/map/' +
           'partials/drawbbox.html',
           scope: {},
           link: function(scope, element, attrs) {

           }
         };
       }]);
})();
