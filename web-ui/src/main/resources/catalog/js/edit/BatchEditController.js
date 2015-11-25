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
    '$compile',
    'gnSearchSettings',
    'gnCurrentEdit'
  ,
  function($scope, $location, $http, $compile,
           gnSearchSettings, gnCurrentEdit) {

    gnSearchSettings.resultViewTpls = [{
      tplUrl: '../../catalog/components/search/resultsview/' +
      'partials/viewtemplates/titlewithselection.html',
      tooltip: 'List',
      icon: 'fa-list'
    }];

    $scope.selectedStep = 1;
    $scope.fieldConfig = null;
    $scope.changes = [];
    // Map of xpath / extent
    $scope.xmlExtents = {};
    $scope.xmlContacts = {};
    var xpathCounter = 0;
    $scope.insertModes = ['gn_add', 'gn_replace', 'gn_delete', 'gn_delete_all'];
    $scope.currentXpath = {};
    $scope.defaultCurrentXpath = {
      field: '',
      xpath: '',
      value: '',
      insertMode: 'gn_add'
    };
    $scope.currentXpath = angular.copy($scope.defaultCurrentXpath, {});

    gnCurrentEdit = {
      schema: 'iso19139'
    };

    $scope.setStep = function(step) {
      $scope.selectedStep = step;
    };

    $scope.$watchCollection('xmlExtents', function (newValue, oldValue) {
      angular.forEach($scope.xmlExtents, function (value, xpath) {
        $scope.putChange({
          name: 'extent',
          xpath: xpath
        }, {
          target: {
            value: value
          }
        });
      });
    });

    $scope.addContactCb = function (scope, record, role) {
      var field = angular.fromJson(scope.attrs['field']);
      if (!$scope.xmlContacts[field.name]) {
        $scope.xmlContacts[field.name] = {
          field: field,
          values: []
        };
      };
      $scope.xmlContacts[field.name].values.push({
        title: record.title + (role ? ' - ' + role : ''),
        xml: scope.snippet
      });

      $scope.addChange(field, {
        target: {
          value: scope.snippet
        }
      });
    };

    $scope.removeContact = function (field, contact) {
      $scope.removeChange(field.xpath, contact.xml);
      for (var j = 0; j < $scope.xmlContacts[field.name].values.length; j++) {
        if($scope.xmlContacts[field.name].values[j].xml === contact) {
          $scope.xmlContacts[field.name].values.splice(j, 1);
          return;
        }
      }
    };

    $scope.$watch('selectedStep', function (newValue, oldValue) {
      if (newValue === 2) {
        // Initialize map size when tab is rendered.
        var map = $('div.gn-drawmap-panel').data('map');
        if (!angular.isArray(
            map.getSize()) || map.getSize()[0] == 0) {
          setTimeout(function () {
            map.updateSize();
          });
        }
      }
    });

    /**
     * Add field with only one value allowed.
     */
    $scope.putChange = function (field, $event) {
      var index = $scope.changes.length;

      if ($event && $event.target && $event.target.value === '') {
        $scope.removeChange(field.xpath);
      } else {
        for (var j = 0; j < $scope.changes.length; j++) {
          if($scope.changes[j].xpath === field.xpath) {
            index = j;
            break;
          }
        }
        insertChange(field.name, field.xpath, field.template,
          $event.target.value, index);
      }
    };
    /**
     * Add field with multiple value allowed.
     */
    $scope.addChange = function (field, $event) {
      insertChange(field.name, field.xpath, field.template,
        $event.target.value, $scope.changes.length);
    };
    /**
     * Remove field. If value is undefined, remove all changes for that field.
     */
    $scope.removeChange = function (xpath, value) {
      for (var j = 0; j < $scope.changes.length; j++) {
        if($scope.changes[j].xpath === xpath &&
          (value === undefined || $scope.changes[j].value === value)) {
          $scope.changes.splice(j, 1);
          return;
        }
      }
    };
    var insertChange = function (field, xpath, template, value,
                                 index, insertMode, isXpath) {
      $scope.changes[index] = {
        field: field,
        insertMode: insertMode || field.insertMode,
        xpath: xpath,
        value: template && value !== '' ?
          template.replace('{{value}}', value) :
          value,
        isXpath: isXpath || false
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
        url: 'md.edit.batch.config'
      }).success(function (data) {
        $scope.fieldConfig = data.iso19139;
      }).error(function(response) {
        console.log(response);
      });
    }

    $scope.resetChanges = function() {
      $scope.changes = [];
      $scope.xmlExtents = {};
      $scope.xmlContacts = {};
    };

    $scope.addOrUpdateXpathChange = function () {
      var c = $scope.currentXpath;
      xpathCounter ++;
      if (c.field == '') {
        c.field = 'XPath_' + xpathCounter;
      }

      insertChange(c.field, c.xpath, '', c.value,
                  $scope.changes.length, c.insertMode, true);

      $scope.currentXpath = angular.copy($scope.defaultCurrentXpath, {});
    };
    $scope.removeXpathChange = function (c) {
      $scope.removeChange(c.xpath, c.value);
      xpathCounter --;
    };
    $scope.editXpathChange = function (c) {
      $scope.removeChange(c.xpath, c.value);
      $scope.currentXpath = c;
    };

    $scope.isXpath = function (value) {
      return value.isXpath || false;
    };

    $scope.markFieldAsDeleted = function(field, mode) {
      field.isDeleted = !field.isDeleted;
      field.value = '';
      $scope.removeChange(field);
      if (field.isDeleted) {
        insertChange(field.name, field.xpath, field.template,
          '', $scope.changes.length,
          mode || 'gn_delete_all');
      }
    };

    $scope.applyChanges = function() {
      var params = {}, i = 0;
      angular.forEach($scope.changes, function(field) {
        if (field.value != null) {
          var value = field.value, xpath = field.xpath;
          if (field.insertMode != null) {
            value = '<' + field.insertMode + '>' +
              field.value +
              '</' + field.insertMode + '>';
          } else {
            value = value;
          }

          params['xpath_' + i] = xpath;
          params['search_' + i] = ''; // TODO: unused
          params['replace_' + i] = value;
          i++;
        }
      });

      $http({
        method: 'POST',
        url: 'md.edit.batch?_content_type=json',
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
}());
