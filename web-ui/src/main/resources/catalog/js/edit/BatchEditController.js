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
    'gnMetadataActions',
    'gnGlobalSettings',
    'Metadata',
    function($scope, $location, $rootScope, $translate, $q, $http,
        gnSearchSettings, gnMetadataActions, gnGlobalSettings, Metadata) {
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
            $http.get('md.selected?_content_type=json').success(function(uuids) {
              $http.get('q?_content_type=json&fast=index&_uuid=' + uuids.join(' or ')).success(
                function (data) {
                  // TODO: If too many records - only list the first 20.
                  $scope.selectedRecords = {records: []};
                  for (var i = 0; i < data.metadata.length; i++) {
                    $scope.selectedRecords.records.push(new Metadata(data.metadata[i]));
                  }
                  $scope.selectedRecords.count = data.count;
                  $scope.selectedRecords.facet = data.facet;
                  $scope.selectedRecords.dimension = data.dimension;
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
      $scope.selectedRecords = [];

      // TODO: Move config to schema plugins
      // * group field by sections
      // * use directive when relevant (eg. date, contact, bbox)
      // * add option to search & replace ?
      // * add option to drop a field ?
      $scope.fieldConfig = [{
        'xpath': 'gmd:identificationInfo/gmd:MD_DataIdentification/' +
            'gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString',
        'label': 'title',
        'mandatory': true,
        'type': 'text',
        'value': null
      },{
        'xpath': 'gmd:identificationInfo/gmd:MD_DataIdentification/' +
        'gmd:pointOfContact',
        'label': 'resourceContact',
        'type': 'data-gn-contact-picker',
        'value': null
      },{
        'xpath': 'gmd:language/language/LanguageCode/@codeListValue',
        'label': 'mdLanguage',
        'type': 'data-gn-language-picker',
        'value': null
      }];

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

      $scope.selectedRecords = null;
      $scope.markFieldAsDeleted = function(field) {
        // TODO
      };
      $scope.applyChanges = function() {
        var params = {}, i = 0;
        angular.forEach($scope.fieldConfig, function(field) {
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
    }
  ]);
})();
