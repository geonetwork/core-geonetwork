(function() {
  goog.provide('gn_categories_controller');

  var module = angular.module('gn_categories_controller',
      []);


  /**
   * CategoriesController provides all necessary operations
   * to manage category.
   */
  module.controller('GnCategoriesController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate',
    function($scope, $routeParams, $http, $rootScope, $translate) {

      $scope.categories = null;
      $scope.categorySelected = {id: $routeParams.categoryId};

      $scope.categoryUpdated = false;

      $scope.selectCategory = function(c) {
        $scope.cateroryUpdated = false;
        $scope.categorySelected = c;
      };


      /**
       * Delete a category
       */
      $scope.deleteCategory = function(id) {
        $http.get('admin.category.remove?id=' +
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
        $http.get('admin.category.update?' + $(formId).serialize())
          .success(function(data) {
              $scope.unselectCategory();
              loadCategories();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('categoryUpdated'),
                timeout: 2,
                type: 'success'});
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('categoryUpdateError'),
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

      $scope.unselectCategory = function() {
        $scope.categorySelected = {};
      };
      $scope.updatingCategory = function() {
        $scope.categoryUpdated = true;
      };

      function loadCategories() {
        $http.get('info@json?type=categories').success(function(data) {
          $scope.categories = data.metadatacategory;
        }).error(function(data) {
          // TODO
        });
      }
      loadCategories();
    }]);

})();
