(function() {
  goog.provide('gn_ows_directive');

  angular.module('gn_ows_directive', [])

    .directive(
      'gnLayersGrid',
      [
       'gnOwsCapabilities',
       function(gnOwsCapabilities) {
         return {
           restrict: 'A',
           templateUrl: '../../catalog/components/common/ows/' +
           'partials/layersGrid.html',
           scope: {
             selection: '=',
             layers: '='
           },
           link: function(scope, element, attrs) {


             // Manage layers selection
             scope.selectionMode = attrs.gnSelectionMode;
             if (scope.selectionMode) {
               scope.selection = [];
               scope.select = function(layer) {
                 if (scope.selectionMode.indexOf('multiple') >= 0) {
                   if (scope.selection.indexOf(layer) < 0) {
                     scope.selection.push(layer);
                   }
                   else {
                     scope.selection.splice(scope.selection.indexOf(layer), 1);
                   }
                 }
                 else {
                   scope.selection.pop();
                   scope.selection.push(layer);
                 }
               };
             }
           }
         };
       }]);
})();
