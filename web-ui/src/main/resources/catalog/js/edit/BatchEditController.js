(function() {
  goog.provide('gn_batchedit_controller');

  goog.require('gn_mdactions_service');
  goog.require('gn_search');
  goog.require('gn_search_form_controller');

  var module = angular.module('gn_batchedit_controller',
      ['gn_search', 'gn_search_form_controller', 'gn_mdactions_service']);


  /**
   * Search form for batch editing selection of records.
   *
   * Filters on metadata and template (no subtemplate)
   * and only record that current user can edit (ie. editable=true)
   */
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
      // Search parameters and configuration
      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.onlyMyRecord = {
        is: gnGlobalSettings.gnCfg.mods.editor.isUserRecordsOnly
      };
      $scope.isFilterTagsDisplayed =
          gnGlobalSettings.gnCfg.mods.editor.isFilterTagsDisplayed;
      $scope.defaultSearchObj = {
        permalink: false,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        selectionBucket: 'be101',
        filters: gnSearchSettings.filters,
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


      // Only my record toggle
      $scope.toggleOnlyMyRecord = function(callback) {
        $scope.onlyMyRecord.is ? setOwner() : unsetOwner();
        callback();
      };
      var setOwner = function() {
        $scope.searchObj.params['_owner'] = $scope.user.id;
      };
      var unsetOwner = function() {
        delete $scope.searchObj.params['_owner'];
      };
      $scope.$watch('user.id', function(newId) {
        if (angular.isDefined(newId) && $scope.onlyMyRecord.is) {
          setOwner();
        }
      });


      // When selection change get the current selection uuids
      // and populate a list of currently selected records to be
      // display on summary pages
      $scope.$watch('searchResults.selectedCount',
          function(newvalue, oldvalue) {
            if (oldvalue != newvalue) {
              $scope.tooManyRecordInSelForSearch = false;
              $scope.tooManyRecordInSelForSearch = false;
              $scope.selectedRecordsCount = 0;
              $scope.selectedStandards = [];
              $scope.selectedRecords = [];
              $http.get('../api/selections/be101').
                  success(function(uuids) {
                    $scope.selectedRecordsCount = uuids.length;
                    if (uuids.length > 0) {
                      $http.get('q?_content_type=json&_isTemplate=y or n or s&' +
                            'fast=index&resultType=manager&' +
                            '_uuid=' + uuids.join(' or ')).then(
                          function(r) {
                            var data = r.data;
                            $scope.selectedRecords =
                              gnSearchManagerService.format(data);
                            $.each($scope.selectedRecords.dimension,
                              function(idx, dim) {
                                if (dim['@label'] == 'standards') {
                                  $scope.selectedStandards = dim.category;
                                  $scope.isSelectedAMixOfStandards =
                                  $scope.selectedStandards &&
                                  $scope.selectedStandards.length > 1;
                                  return false;
                                }
                              });
                            // TODO: If too many records - only list the first 20.
                          }, function (r) {
                            // Could produce too long URLs 414 (URI Too Long)
                            console.log(r);
                            if (r.status === 414) {
                              $scope.tooManyRecordInSelForSearch = true;
                            } else {
                              console.warn(r);
                            }
                        });
                      }
                  });
            }
          });
      $scope.hasRecordsInStandard = function(standard) {
        var isFound = false;
        // We can't do this check when too many records in selection.
        if ($scope.tooManyRecordInSelForSearch) {
          return true;
        }
        $.each($scope.selectedStandards, function(idx, facet) {
          if (facet['@value'] == standard) {
            isFound = true;
            return false;
          }
        });
        return isFound;
      };

      // Get current selection which returns the list of uuids.
      // Then search those records.
      $scope.searchSelection = function(params) {
        $http.get('../api/selections/be101').success(function(uuids) {
          $scope.searchObj.params = angular.extend({
            _uuid: uuids.join(' or ')
          },
          $scope.defaultSearchObj.params);
          $scope.triggerSearch();
        });
      };
    }
  ]);


  /**
   * Take care of defining changes and applying them.
   */
  module.controller('GnBatchEditController', [
    '$scope',
    '$location',
    '$http',
    '$compile',
    '$httpParamSerializer',
    'gnSearchSettings',
    'gnCurrentEdit',
    'gnSchemaManagerService',
    function($scope, $location, $http, $compile, $httpParamSerializer,
        gnSearchSettings, gnCurrentEdit, gnSchemaManagerService) {

      // Simple tab handling.
      $scope.selectedStep = 1;
      $scope.setStep = function(step) {
        $scope.selectedStep = step;
      };
      $scope.extraParams = {};
      $scope.$watch('selectedStep', function(newValue) {
        if (newValue === 2) {
          // Initialize map size when tab is rendered.
          var map = $('div.gn-drawmap-panel');
          if (map == undefined) {
            return;
          }
          map.each(function(idx, div) {
            var map = $(div).data('map');
            if (!angular.isArray(
                map.getSize()) || map.getSize()[0] == 0) {
              setTimeout(function() {
                map.updateSize();
              });
            }
          });
        }
      });


      // Search setup
      gnSearchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/titlewithselection.html',
        tooltip: 'List',
        icon: 'fa-list'
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


      // TODO: Improve for other standards
      // Object used by directory directive
      gnCurrentEdit = {
        schema: 'iso19139'
      };


      $scope.fieldConfig = null;  // Configuration per standard
      $scope.changes = [];  // List of changes
      // TODO: Add a mode gn_update_only_if_match ?
      $scope.insertModes = ['gn_add', 'gn_replace', 'gn_delete'];

      // Add a change to the list
      var insertChange = function(field, xpath, template, value,
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

      // Add field with multiple value allowed.
      $scope.addChange = function(field, $event) {
        insertChange(field.name, field.xpath, field.template,
            $event.target.value, $scope.changes.length, field.insertMode);
      };

      // Add field with only one value allowed.
      $scope.putChange = function(field, $event) {
        var index = $scope.changes.length;

        if ($event && $event.target && $event.target.value === '') {
          $scope.removeChange(field.xpath);
        } else {
          for (var j = 0; j < $scope.changes.length; j++) {
            if ($scope.changes[j].xpath === field.xpath) {
              index = j;
              break;
            }
          }
          insertChange(field.name, field.xpath, field.template,
              $event.target.value, index, field.insertMode);
        }
      };

      // Remove field. If value is undefined, remove all changes for that field.
      $scope.removeChange = function(xpath, value) {
        for (var j = 0; j < $scope.changes.length; j++) {
          if ($scope.changes[j].xpath === xpath &&
              (value === undefined || $scope.changes[j].value === value)) {
            $scope.changes.splice(j, 1);
            return;
          }
        }
      };

      $scope.resetChanges = function() {
        $scope.changes = [];
        $scope.xmlExtents = {};
        $scope.xmlContacts = {};
        $('#gn-batch-changes input, ' +
          '#gn-batch-changes textarea, ' +
          '#gn-batch-changes select').
            each(function(idx, e) {
              $(e).val('');
            });
      };

      $scope.markFieldAsDeleted = function(field, mode) {
        field.isDeleted = !field.isDeleted;
        field.value = '';
        $scope.removeChange(field);
        if (field.isDeleted) {
          insertChange(field.name, field.xpath, field.template,
              '', $scope.changes.length,
              mode || 'gn_delete');
        }
      };


      // Extents
      $scope.xmlExtents = {};
      $scope.$watchCollection('xmlExtents', function(newValue, oldValue) {
        angular.forEach($scope.xmlExtents, function(value, xpath) {
          $scope.putChange({
            name: 'extent',
            xpath: xpath,
            insertMode: 'gn_create' // TODO: Should come from config
          }, {
            target: {
              value: value
            }
          });
        });
      });



      // Contacts
      $scope.xmlContacts = {};
      $scope.addContactCb = function(scope, record, role) {
        var field = angular.fromJson(scope.attrs['field']);
        if (!$scope.xmlContacts[field.name]) {
          $scope.xmlContacts[field.name] = {
            field: field,
            values: []
          };
        }
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
      $scope.removeContact = function(field, contact) {
        $scope.removeChange(field.xpath, contact.xml);
        for (var j = 0; j < $scope.xmlContacts[field.name].values.length; j++) {
          if ($scope.xmlContacts[field.name].values[j].xml === contact) {
            $scope.xmlContacts[field.name].values.splice(j, 1);
            return;
          }
        }
      };


      // Manual XPath setup
      var xpathCounter = 0; // Counter to identify manual XPath values
      $scope.currentXpath = {}; // The XPath entry manually defined
      $scope.defaultCurrentXpath = {
        field: '',
        xpath: '',
        value: '',
        insertMode: 'gn_add'
      };  // The default value when reset.
      $scope.currentXpath = angular.copy($scope.defaultCurrentXpath, {});

      $scope.addOrUpdateXpathChange = function() {
        var c = $scope.currentXpath;
        xpathCounter++;
        if (c.field == '') {
          c.field = 'XPath_' + xpathCounter;
        }

        insertChange(c.field, c.xpath, '', c.value,
            $scope.changes.length, c.insertMode, true);

        $scope.currentXpath = angular.copy($scope.defaultCurrentXpath, {});
      };
      $scope.removeXpathChange = function(c) {
        $scope.removeChange(c.xpath, c.value);
        xpathCounter--;
      };
      $scope.editXpathChange = function(c) {
        $scope.removeChange(c.xpath, c.value);
        $scope.currentXpath = c;
      };
      $scope.isXpath = function(value) {
        return value.isXpath || false;
      };


      $scope.processReport = null;

      $scope.applyChanges = function() {
        var params = [], i = 0;
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
            params.push({xpath: xpath, value: value});
            i++;
          }
        });

        // TODO: Apply changes to a mix of records is maybe not the best
        // XPath will be applied whatever the standard is.
        var url = '../api/records/batchediting?'
          + $httpParamSerializer({
            'bucket': 'be101',
            'updateDateStamp': $scope.extraParams.updateDateStamp
          });
        return $http.put(url,
            params
        ).success(function(data) {
          $scope.processReport = data;
        }).error(function(response) {
          $scope.processReport = response.data;
        });
      };



      function init() {
        $http.get('../api/standards/batchconfiguration').
            success(function(data) {
              $scope.fieldConfig = data;
              gnSchemaManagerService.getNamespaces();
            }).error(function(response) {
              console.warn(response);
            });
      }
      init();
    }
  ]);
}());
