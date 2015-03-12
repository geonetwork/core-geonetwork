(function() {
  goog.provide('gn_formatter_viewer');


  goog.require('gn');
  goog.require('gn_catalog_service');
  goog.require('gn_utility_directive');









  var module = angular.module('gn_formatter_viewer',
      ['ngRoute', 'gn', 'gn_utility_directive', 'gn_catalog_service']);

  // Define the translation files to load
  module.constant('$LOCALES', ['core']);

  module.controller('GnFormatterViewer',
      ['$scope', '$http', '$sce', '$routeParams',
       function($scope, $http, $sce, $routeParams) {
         $scope.metadata = '';
         $scope.loading = true;

         var formatter = $routeParams.formatter;
         var mdId = $routeParams.mdId;

         $http.get('md.format.xml?xsl=' + formatter + '&id=' + mdId).
         success(function(data) {
           $scope.loading = undefined;
           $scope.metadata = $sce.trustAsHtml(data);
         }).error(function(data) {
           $scope.loading = undefined;
           $scope.metadata = $sce.trustAsHtml(data);
         });
       }]);
  module.config(['$routeProvider', function($routeProvider) {
    var tpls = '../../catalog/templates/';

    $routeProvider.when('/:formatter/:mdId', { templateUrl: tpls +
          '/formatter-viewer.html', controller: 'GnFormatterViewer'});
  }]);
})();
