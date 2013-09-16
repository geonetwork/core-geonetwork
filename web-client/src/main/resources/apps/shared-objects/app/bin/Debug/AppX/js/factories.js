'use strict';

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('SharedObjects.factories', []).
  factory('commonProperties', ['$window', '$http', function ($window, $http) {
      var loadRecords = function ($scope) {
          $http({ method: 'GET', url: '../../../srv/'+$scope.language+'/reusable.list.js' }).
              success(function (data, status, headers, config) {
                  for (var i = 0; i < data.length; i++) {
                      if (data[i].desc) {
                          data[i].desc = data[i].desc.replace(/\&lt;/g, '<').replace(/\&gt;/g, '>');
                      } else {
                          data[i].desc = 'No description provided';
                      }
                  }


                   $scope.data = data;

              }).
              error(function (data, status, headers, config) {
                  alert("An error occurred when loading shared objects: " + data);
              });
      };
     
      return {
          add: function ($scope, $routeParams) {
              $scope.type = $window.location.href.split("#")[1].split("/")[2];
              $scope.validated = $routeParams.validated;
              $scope.isValidated = $routeParams.validated === 'validated';
              if ($scope.isValidated) {
                  $scope.validatedTitle = 'Validated';
              } else {
                  $scope.validatedTitle = 'Non-validated';
              }

              $scope.translate = Geonet.translate;
              $scope.language = Geonet.language;
              $scope.filter = "";

              $scope.data = [];

              loadRecords($scope);

              $scope.loadReferencedMetadata = function (id, divId) {
                  $http({ method: 'GET', url: '../../../srv/' + $scope.language + '/reusable.references?id='+id+"&type="+$scope.type }).
             success(function (data, status, headers, config) {
                 $('#'+divId).replaceWith(data);
             }).
             error(function (data, status, headers, config) {
                 alert("An error occurred when loading shared objects: " + data);
             });
              }
          }

      }
  }]);
