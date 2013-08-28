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
      
      $scope.categoryUpdated = false;
     
      $scope.selectCategory = function (c) {
    	  console.log(c);
    	  $scope.categorySelected = c;
      };
      
            
      /**
       * Delete a category  
       */
      $scope.deleteCategory = function(id) {
    	  
    	  console.log($scope.categoryId);
        $http.get($scope.url + 'admin.category.remove?id=' +
        		 id)
        .success(function(data) {
        	  $scope.unselectCategory();
        	  loadCategories();
            })
        .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('categoryDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };
      
      /**
       * Save a category  
       */
      $scope.saveCategory = function(formId) {  	  
          $http.get($scope.url + 'admin.category.update?' + $(formId).serialize())
          .success(function(data) {
                $scope.unselectCategory();
                loadCategories();
                $rootScope.$broadcast('StatusUpdated', {
                  msg: $translate('groupUpdated'),
                  timeout: 2,
                  type: 'success'});
              })
          .error(function(data) {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate('groupUpdateError'),
                  error: data,
                  timeout: 0,
                  type: 'danger'});
              });
        };
      
     $scope.addCategory = function() {
          $scope.unselectCategory();
          $scope.categorySelected = {
            '@id': '',
            name: ''
          };
        };
      
        $scope.unselectCategory =function() {
        	$scope.categorySelected = {};
        }
        $scope.updatingCategory = function() {
            $scope.cateroryUpdated = true;
          };
      
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
