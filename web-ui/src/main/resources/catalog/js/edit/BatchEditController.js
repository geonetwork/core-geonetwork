(function() {
  goog.provide('gn_batchedit_controller');



  goog.require('gn_mdactions_service');
  goog.require('gn_search');
  goog.require('gn_search_form_controller');

  var module = angular.module('gn_batchedit_controller',
      ['gn_search', 'gn_search_form_controller', 'gn_mdactions_service']);


  module.controller('GnBatchEditSearchController', [
    '$scope',
    '$location',
    '$rootScope',
    '$translate',
    '$q',
    '$http',
    'gnSearchSettings',
    'gnSearchManagerService',
    'gnMetadataActions',
    'gnGlobalSettings',
    'Metadata',
    function($scope, $location, $rootScope, $translate, $q, $http,
        gnSearchSettings, gnSearchManagerService,
             gnMetadataActions, gnGlobalSettings, Metadata) {
      $scope.onlyMyRecord = false;
      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.defaultSearchObj = {
        permalink: false,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        params: {
          sortBy: 'changeDate',
          _isTemplate: 'y or n',
          editable: 'true',
          resultType: $scope.facetsSummaryType,
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, $scope.defaultSearchObj);

      $scope.toggleOnlyMyRecord = function() {
        $scope.onlyMyRecord = !$scope.onlyMyRecord;
      };
      var setOwner = function() {
        $scope.searchObj.params['_owner'] = $scope.user.id;
      };
      var unsetOwner = function() {
        delete $scope.searchObj.params['_owner'];
      };
      $scope.$watch('onlyMyRecord', function(value) {
        if (!$scope.searchObj) {
          return;
        }

        value ? setOwner() : unsetOwner();
      });

      $scope.$watch('searchResults.selectedCount',
        function (newvalue, oldvalue) {
          if (oldvalue != newvalue) {
            $http.get('md.selected?_content_type=json').
              success(function(uuids) {
              $http.get('q?_content_type=json&_isTemplate=y or n or s&' +
                          'fast=index&_uuid=' + uuids.join(' or ')).success(
                function (data) {
                  $scope.selectedRecords = gnSearchManagerService.format(data);
                  // TODO: If too many records - only list the first 20.
                });
            });
          }
        });
      /***
       * Get current selection which returns the list of uuids.
       * Then search those records.
       *
       */
      $scope.searchSelection = function(params) {
        $http.get('md.selected?_content_type=json').success(function(uuids) {
          $scope.searchObj.params = angular.extend({
            _uuid: uuids.join(' or ')
          },
          $scope.defaultSearchObj.params);
          $scope.triggerSearch();
        });
      };
    }
  ]);
  module.controller('GnBatchEditController', [
    '$scope',
    '$location',
    '$http',
    'gnSearchSettings',
    function($scope, $location, $http, gnSearchSettings) {

      gnSearchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/titlewithselection.html',
        tooltip: 'List',
        icon: 'fa-list'
      }];

      $scope.selectedStep = 1;
      $scope.selectedStep1Tab = 1;
      $scope.fieldConfig = null;
      $scope.changes = [];

      /**
       * Add field with only one value allowed.
       */
      $scope.putChange = function (field, $event) {
        var index = $scope.changes.length;

        if ($event.target.value === '') {
          $scope.removeChange(field);
        } else {
          for (var j = 0; j < $scope.changes.length; j++) {
            if($scope.changes[j].xpath === field.xpath) {
              index = j;
              break;
            }
          }
          insertChange(field, $event.target.value, index);
        }
      };
      /**
       * Add field with multiple value allowed.
       */
      $scope.addChange = function (field, $event) {
        insertChange(field, $event.target.value, $scope.changes.length);
      };
      /**
       * Remove field. If value is undefined, remove all changes for that field.
       */
      $scope.removeChange = function (field, value) {
        for (var j = 0; j < $scope.changes.length; j++) {
          if($scope.changes[j].xpath === field.xpath &&
            (value === undefined || $scope.changes[j].value === value)) {
            $scope.changes.splice(j, 1);
            return;
          }
        }
      };
      var insertChange = function (field, value, index) {
        $scope.changes[index] = {
          field: field.name,
          xpath: field.xpath,
          value: value
        };
      };

      gnSearchSettings.resultTemplate =
          gnSearchSettings.resultViewTpls[0].tplUrl;

      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType = 'manager';

      gnSearchSettings.sortbyValues = [{
        sortBy: 'relevance',
        sortOrder: ''
      }, {
        sortBy: 'changeDate',
        sortOrder: ''
      }, {
        sortBy: 'title',
        sortOrder: 'reverse'
      }];

      gnSearchSettings.hitsperpageValues = [20, 50, 100];

      gnSearchSettings.paginationInfo = {
        hitsPerPage: gnSearchSettings.hitsperpageValues[1]
      };

      function init() {
        $http({
          method: 'GET',
          url: 'md.edits.batch.config'
        }).success(function (data) {
          $scope.fieldConfig = data.iso19139;
        }).error(function(response) {
          console.log(response);
        });
      }

      $scope.markFieldAsDeleted = function(field) {
        // TODO
      };
      $scope.applyChanges = function() {
        var params = {}, i = 0;
        angular.forEach($scope.changes, function(field) {
          // TODO: How to drop a field value ?
          if (field.value != null) {
            params['xpath_' + i] = field.xpath;
            params['search_' + i] = '';
            params['replace_' + i] = field.value;
            i++;
          }
        });

        $http({
          method: 'POST',
          url: 'md.edits.batch?_content_type=json',
          data: $.param(params),
          headers: {'Content-Type':
                'application/x-www-form-urlencoded'}
        }).success(function(data) {
          console.log(data);
        }).error(function(response) {
          console.log(response);
        });
      };

     init();
    }
  ]);
})();
