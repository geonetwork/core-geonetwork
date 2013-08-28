(function() {
  goog.provide('gn_classification_controller');


  goog.require('gn_categories_controller');
  goog.require('gn_thesaurus_controller');

  var module = angular.module('gn_classification_controller',
      ['gn_thesaurus_controller', 'gn_categories_controller']);


  /**
   *
   */
  module.controller('GnClassificationController',
      ['$scope', '$routeParams', '$http',
       function($scope, $routeParams, $http) {
         var templateFolder = '../../catalog/templates/admin/classification/';
         var availableTemplates = [
           'thesaurus', 'directory', 'categories'
         ];

         $scope.defaultSettingType = 'thesaurus';

         $scope.getTemplate = function() {
           $scope.type = $scope.defaultSettingType;
           if (availableTemplates.indexOf($routeParams.tab) > -1) {
             $scope.type = $routeParams.tab;
           }
           return templateFolder + $scope.type + '.html';
         };
       }]);

})();
