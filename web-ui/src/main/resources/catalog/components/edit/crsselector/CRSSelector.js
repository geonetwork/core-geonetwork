(function() {
  goog.provide('gn_crs_selector');

  var module = angular.module('gn_crs_selector', []);

  /**
   *
   *
   */
  module.directive('gnCrsSelector',
      ['$rootScope', '$timeout', '$http',
        'gnEditor', 'gnEditorXMLService', 'gnCurrentEdit',
        function($rootScope, $timeout, $http,
            gnEditor, gnEditorXMLService, gnCurrentEdit) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             mode: '@gnCrsSelector',
             elementName: '@',
             elementRef: '@',
             domId: '@'
           },
           templateUrl: '../../catalog/components/edit/' +
           'crsselector/partials/' +
           'crsselector.html',
           link: function(scope, element, attrs) {
             scope.snippet = null;
             scope.snippetRef = gnEditor.
             buildXMLFieldName(scope.elementRef, scope.elementName);

             scope.add = function() {
               gnEditor.add(gnCurrentEdit.id,
                   scope.elementRef, scope.elementName, scope.domId, 'before');
               return false;
             };


             scope.search = function() {
               if (scope.filter) {
                 $http.get('crs.search@json?type=&maxResults=50&name=' +
                      scope.filter).success(
                     function(data) {
                       scope.crsResults = data;
                     });
               }
             };

             // Then register search filter change
             scope.$watch('filter', scope.search);

             scope.addCRS = function(crs) {
               scope.snippet = gnEditorXMLService.buildCRSXML(crs);
               scope.crsResults = [];

               $timeout(function() {
                 // Save the metadata and refresh the form
                 gnEditor.save(gnCurrentEdit.id, true);
               });

               return false;
             };
           }
         };
       }]);
})();
