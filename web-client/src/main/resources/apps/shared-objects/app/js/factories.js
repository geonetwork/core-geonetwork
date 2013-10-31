'use strict';

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('SharedObjects.factories', []).
  factory('commonProperties', ['$window', '$http', function ($window, $http) {
      var loadRecords = function ($scope) {
          var validated = $scope.isValidated ? 'true' : 'false';
          $http({ method: 'GET', url: $scope.baseUrl + '/reusable.list.js?validated=' + validated + '&type=' + $scope.type }).
              success(function (data, status, headers, config) {
                  if (data.indexOf("<") != 0) {
                      for (var i = 0; i < data.length; i++) {
                          if (data[i].url) {
                              data[i].url = data[i].url.replace(/local:\/\//g, '');
                          }
                          if (data[i].desc) {
                              data[i].desc = data[i].desc.replace(/\&lt;/g, '<').replace(/\&gt;/g, '>');
                          } else {
                              data[i].desc = 'No description provided';
                          }
                      }

                      $scope.data = data;
                  }

              }).
              error(function (data, status, headers, config) {
                  alert("An error occurred when loading shared objects");
              });
      };

      return {
          addValidated: function ($scope, $routeParams) {
              $scope.type = $window.location.href.split("#")[1].split("/")[2];
              $scope.validated = $routeParams.validated;
              $scope.isValidated = $routeParams.validated === 'validated';
              if ($scope.isValidated) {
                  $scope.validatedTitle = Geonet.translate('reusable_validated');
              } else {
                  $scope.validatedTitle = Geonet.translate('reusable_nonValidated');
              }
          },
          add: function ($scope) {
              
              $scope.translate = Geonet.translate;
              $scope.language = Geonet.language;
              $scope.search = {
                  search: ''
              };
              $scope.edit = {
                  
              };
              var baseUrl = '../../../srv/' + $scope.language;
              $scope.baseUrl = baseUrl;
              $scope.filter = "";
              $scope.selected = null;
              $scope.select = function (row) {
                  $scope.selected = row;
              }
              $scope.data = [];
              $scope.metadata = [];

              loadRecords($scope);

              $scope.reloadData = function () { loadRecords($scope); };

              $scope.loadReferencedMetadata = function (id, collapseDiv, containerDivId) {
                  $('.in').collapse('hide');

                  $('#' + collapseDiv).collapse('show');
                  $http({ method: 'GET', url: baseUrl + '/reusable.references?id=' + id + "&type=" + $scope.type + '&validated=' + $scope.isValidated }).
                     success(function (data, status, headers, config) {
                         $scope.metadata[id] = data;
                         $('#' + containerDivId).remove();
                     }).
                     error(function (data, status, headers, config) {
                         alert("An error occurred when loading referenced metadata");
                     });
              };
              $scope.edit = function (row) {
                  $scope.open(row.url);
              };
              $scope.open = function (url, params) {
                  var finalUrl = baseUrl + '/' + url;
                  if (params) {
                      if (finalUrl.indexOf("?") > -1) {
                          finalUrl += "&" + jQuery.param(params);
                      } else {
                          finalUrl += "?" + jQuery.param(params);
                      }
                  }
                  window.open(finalUrl, '_sharedTab');
              };
              $scope.editTitle = Geonet.translate('createNewSharedObject').replace(/\%objtype\%/g, Geonet.translate($scope.type))
              $scope.editTitle = Geonet.translate('createNewSharedObject').replace(/\%objtype\%/g, Geonet.translate($scope.type))
              $scope.startCreateNew = function () {
                  $scope.finishEdit = $scope.createNewObject;
                  $('#editModal').modal('show');
              };
              $scope.performOperation = function (requestObject) {
                  $('.modal').modal('hide');
                  var executeModal = $('#executingOperation');

                  executeModal.modal('show');
                  var promise = $http(requestObject)
                  .success(function (data, status, headers, config) {
                      executeModal.modal('hide');
                      loadRecords($scope);
                  })
                  .error(function (data, status, headers, config) {
                      executeModal.modal('hide');
                      alert('An error occurred during validation');
                  });

                  return promise;
              }
              $scope.reject = { message: '' };
              $scope.performUpdateOperation = function (service) {
                  var params = { type: $scope.type, id: $scope.selected.id, isValidObject: $scope.isValidated, msg: $scope.reject.message };

                  if ($scope.message) {
                      params.msg = $scope.message;
                  };
                  $scope.performOperation({
                      method: 'GET',
                      url: baseUrl + '/' + service,
                      params: params
                  });
              };


              $scope.alert = function (name) {
                  alert(name);
              };
              $scope.reloadOnWindowClosed = function (win) {
                  var intervalId = setInterval(function() {
                      if (win.closed) {
                          clearInterval(intervalId);
                          $scope.reloadData();
                      }
                  }, 100);
              };
          }

      }
  }]);
