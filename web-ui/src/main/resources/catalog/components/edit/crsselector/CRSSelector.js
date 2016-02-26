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

             // Replace the name attribute with id since this textarea is used only to store the template, we don't wanna submit it
             var textarea = $.find("textarea[name="+scope.snippetRef+"]")[0];
             var elemValue = $(textarea).attr('name');
             $(textarea).removeAttr('name');
             $(textarea).attr('id', elemValue);
             
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

               var textarea = $.find("textarea[id="+scope.snippetRef+"]")[0];
               var xmlSnippet = textarea ? $(textarea).text() : undefined;
               scope.snippet = gnEditorXMLService.buildCRSXML(crs, gnCurrentEdit.schema, xmlSnippet);
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
