'use strict';

/* Controllers */

angular.module('SharedObjects.controllers', []).
  controller('ContactControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.addValidated($scope, $routeParams);
      commonProperties.add($scope, $routeParams);
      if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_contact';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_contact';
      }
      $scope.createNewObject = function () {
          open($scope.baseUrl + '/shared.user.edit?closeOnSavevalidated=y&operation=newuser', '_sharedObject');
      };
  })
  .controller('FormatControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.addValidated($scope, $routeParams);
      commonProperties.add($scope, $routeParams);
      if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_format';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_format';
      }
      $scope.createNewObject = function () {
          $('#createFormatModal').modal('show');
      };

  })
  .controller('ExtentControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.addValidated($scope, $routeParams);
      commonProperties.add($scope, $routeParams);
      if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_extent';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_extent';
      }
      $scope.createNewObject = function () {
          open($scope.baseUrl + '/extent.edit?crs=EPSG:21781&typename=gn:xlinks&id=&wfs=default&modal', '_sharedObject');
      };

  })
  .controller('KeywordControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.addValidated($scope, $routeParams);
      commonProperties.add($scope, $routeParams);
      if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_keyword';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_keyword';
      }
      $scope.createNewObject = function () {
          $('#createFormatModal').modal('show');
      };

  })
  .controller('DeletedControl', function ($scope, $routeParams, commonProperties) {
      $scope.type = 'deleted';
      $scope.hideTypeSelector = true;
      $scope.validated = 'validated';
      $scope.isValidated = true;
      $scope.validatedTitle = Geonet.translate('deleted');
      commonProperties.add($scope, $routeParams);
      if ($scope.isValidated) {
          $scope.luceneIndexField = 'V_invalid_xlink_keyword';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_keyword';
      }

      $scope.delete = function () {
          $scope.performOperation('reusable.delete');
      }

  });