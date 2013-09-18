'use strict';

/* Controllers */

angular.module('SharedObjects.controllers', []).
  controller('ContactControl', function ($scope, $routeParams, commonProperties) {
      commonProperties.addValidated($scope, $routeParams);
      commonProperties.add($scope, $routeParams);
      $scope.edit = function (row) {
          $scope.open(row.url);
      };
      if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_contact';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_contact';
      }
      $scope.startCreateNew = function () {
          open($scope.baseUrl + '/shared.user.edit?closeOnSavevalidated=y&operation=newuser', '_sharedObject');
      };
      $scope.includeRowPartial = 'row-formless.html';
  })
  .controller('FormatControl', function ($scope, $routeParams, $http, commonProperties) {
      commonProperties.addValidated($scope, $routeParams);
      commonProperties.add($scope, $routeParams);
      $scope.format = {
          name: '',
          version: ''
      };
      if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_format';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_format';
      }

      $scope.edit = function (row) {
          $http.get($scope.baseUrl + '/json.format.get?id=' + row.id)
          .success(function (data) {
              $scope.format.name = data.name;
              $scope.format.version = data.version;
              $('#editModal').modal('show');
          });
      };
      $scope.createNewObject = function () {
          $scope.performOperation({
              method: 'GET',
              url: $scope.baseUrl + '/format',
              params: {
                  action: 'PUT',
                  name: $scope.format.name,
                  version: $scope.format.version,
                  validated: 'y'
              }
          }).
          success(function () {
              $scope.format.name = '';
              $scope.format.version = '';
          });
          $scope.includeRowPartial = 'row-format.html';
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
      $scope.edit = function (row) {
          $scope.open(row.url);
      };
      $scope.startCreateNew = function () {
          open($scope.baseUrl + '/extent.edit?crs=EPSG:21781&typename=gn:xlinks&id=&wfs=default&modal', '_sharedObject');
      };

  })
  .controller('KeywordControl', function ($scope, $routeParams, $http, commonProperties) {
      commonProperties.addValidated($scope, $routeParams);
      commonProperties.add($scope, $routeParams);
      if ($scope.isValid) {
          $scope.luceneIndexField = 'V_invalid_xlink_keyword';
      } else {
          $scope.luceneIndexField = 'V_valid_xlink_keyword';
      }
      $scope.keyword = {
          eng: {label: '', desc: ''},
          fre: { label: '', desc: '' },
          ger: { label: '', desc: '' },
          ita: { label: '', desc: '' },
          roh: { label: '', desc: '' }
      }

      $scope.edit = function (row) {
          var parts = row.url.substring(row.url.indexOf('?') + 1).split(/\&/g, 2);
          var thesaurus = '';
          var id = ''
          
          for (var i = 0; i < parts.length; i++) {
              if (parts[i].indexOf('thesaurus=') > -1) {
                  thesaurus = decodeURIComponent(parts[i].split(/=/, 2)[1]);
              }

              if (parts[i].indexOf('id=') > -1) {
                  id = decodeURIComponent(parts[i].split(/=/, 2)[1]);
              }
          }

          $http({
              method: 'GET',
              url: $scope.baseUrl + '/json.keyword.get',
              params: {
                  lang: 'eng,fre,ger,roh,ita',
                  id: id,
                  thesaurus: thesaurus
              }
          })
          .success(function (data) {
              $scope.finishEdit = function () {
                  $scope.submitEdit(thesaurus, id);
              }
              for (var lang in $scope.keyword) {
                  $scope.keyword[lang].label = data[lang].label;
                  $scope.keyword[lang].desc = data[lang].definition;
              }
              $('#editModal').modal('show');
          });
      };

      var createUpdateParams = function () {
          var params = {
              ref: 'local._none_.geocat.ch',
              refType: '_none_',
              namespace: 'http://custom.shared.obj.ch/concept#',
              id: ''
          };

          var isEmpty = true;
          for (var lang in $scope.keyword) {
              if ('' !== $scope.keyword[lang].label) {
                  isEmpty = false;
                  params['loc_' + lang + '_label'] = $scope.keyword[lang].label;
              }
              if ('' !== $scope.keyword[lang].desc) {
                  isEmpty = false;
                  params['loc_' + lang + '_definition'] = $scope.keyword[lang].desc;
              }
          }

          return params;
      };

      $scope.submitEdit = function (thesaurus, id) {
          var params = createUpdateParams();
          var parts = id.split('#', 2);
          params.newid = parts[1];
          params.oldid = parts[1];
          params.namespace = parts[0];

          params.ref = thesaurus;

          $scope.performOperation({
              method: 'POST',
              url: $scope.baseUrl + '/thesaurus.updateelement',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              data: $.param(params)
          });
      };

      $scope.createNewObject = function () {
          var params = createUpdateParams();

          if (!isEmpty) {
              $scope.performOperation({
                  method: 'POST',
                  url: $scope.baseUrl + '/thesaurus.addelement',
                  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                  data: $.param(params)
              }).
              success(function () {
                  for (var lang in $scope.keyword) {
                      $scope.keyword[lang].label = '';
                      $scope.keyword[lang].desc = '';
                  }
              });
          } else {
              $('#editModal').modal('hide');
          }
      }
  })
  .controller('DeletedControl', function ($scope, $routeParams, commonProperties) {
      $scope.type = 'deleted';
      $scope.isDeletePage = true;
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
          $scope.performUpdateOperation('reusable.delete');
      }

  });