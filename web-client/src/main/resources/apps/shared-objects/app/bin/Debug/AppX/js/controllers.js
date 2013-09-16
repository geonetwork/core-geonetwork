'use strict';

/* Controllers */

angular.module('SharedObjects.controllers', []).
  controller('ContactControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.add($scope, $routeParams);
  })
  .controller('FormatControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.add($scope, $routeParams);
      $scope.data = [];

  })
  .controller('ExtentControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.add($scope, $routeParams);
      $scope.data = [];

  })
  .controller('KeywordControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.add($scope, $routeParams);
      $scope.data = [];

  })
  .controller('DeletedControl', function ($scope) {
      commonProperties.add($scope, $routeParams);
      $scope.data = [];

  });