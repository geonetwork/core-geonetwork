(function() {
  goog.provide('gn_classificationSystems_controller');

  var module = angular.module('gn_classificationSystems_controller',
      []);


  /**
   * classificationSystemsController provides all necessary operations
   * to manage users and groups.
   */
  module.controller('GnClassificationSystemsController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile) {


      var templateFolder = '../../catalog/templates/admin/classificationSystems/';
      var availableTemplates = [
        'categories', 'thesaurus', 'directory'
      ];

      // By default display categories tab
      $scope.defaultclassificationSystemsTab = 'categories';

      $scope.getTemplate = function() {
        $scope.type = $scope.defaultclassificationSystemsTab;
        if (availableTemplates.indexOf($routeParams.classificationSystemsTab) > -1) {
          $scope.type = $routeParams.classificationSystemsTab;
        }
        return templateFolder + $scope.type + '.html';
      };

      $scope.categories = null;
      $scope.categorySelected = {id: $routeParams.categoryId};
     
      $scope.selectCategory = function (c) {
    	  console.log(c);
    	  $scope.categorySelected = c;
      }
      function loadCategories() {
        $http.get('xml.info@json?type=categories').success(function(data) {
          $scope.categories = data.categories;
          console.log($scope.categories);
        }).error(function(data) {
          // TODO
        });
      }

      loadCategories();
    }]);

})();
