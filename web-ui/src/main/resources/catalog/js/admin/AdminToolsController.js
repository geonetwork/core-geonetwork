(function() {
  goog.provide('gn_admintools_controller');


  goog.require('gn_search');
  goog.require('gn_search_form_controller');

  var module = angular.module('gn_admintools_controller',
      ['gn_search', 'gn_search_form_controller']);


  module.controller('GnAdminToolsSearchController', [
    '$scope', 'gnSearchSettings',
    function($scope, gnSearchSettings) {

      var defaultSearchObj = {
        permalink: false,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        params: {
          sortBy: 'changeDate',
          _isTemplate: 'y or n',
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, defaultSearchObj);

      $scope.setTemplate = function(params) {
        var values = [];
        if ($('#batchSearchTemplateY')[0].checked) values.push('y');
        if ($('#batchSearchTemplateN')[0].checked) values.push('n');
        if ($('#batchSearchTemplateS')[0].checked) values.push('s');
        $scope.searchObj.params._isTemplate = values.join(' or ');
      };


    }]);

  /**
   * GnAdminToolsController provides administration tools
   */
  module.controller('GnAdminToolsController', [
    '$scope', '$http', '$rootScope', '$translate', '$compile',
    '$q', '$timeout', '$routeParams', '$location',
    'gnSearchManagerService',
    'gnUtilityService', 'gnSearchSettings',
    function($scope, $http, $rootScope, $translate, $compile, 
        $q, $timeout, $routeParams, $location,
            gnSearchManagerService, 
            gnUtilityService, gnSearchSettings) {


      $scope.pageMenu = {
        folder: 'tools/',
        defaultTab: 'index',
        tabs:
            [{
              type: 'index',
              label: 'indexAdmin',
              icon: 'fa-search',
              href: '#/tools/index'
            },{
              type: 'batch',
              label: 'batchProcess',
              icon: 'fa-medkit',
              href: '#/tools/batch'
            },{
              type: 'transferownership',
              label: 'transfertPrivs',
              href: '#/tools/transferownership'
            }]
      };

      /**
       * True if currently processing records
       */
      $scope.processing = false;

      /**
       * The full text search filter
       * TODO : could be nice to filter by type of record
       * and schema
       */
      $scope.recordsToProcessFilter = '';

      /**
       * The process report returned by the batch
       * processing service with information about the
       * number of records processed, or not, process not found, ...
       */
      $scope.processReport = null;

      /**
       * True if no process found, or privileges issues.
       */
      $scope.processReportWarning = false;

      /**
       * The list of records to be processed
       */
      $scope.recordsToProcess = null;
      $scope.numberOfRecordsProcessed = null;

      /**
       * The selected process
       */
      $scope.selectedProcess = null;
      $scope.data = {};

      /**
       * The list of batch process available.
       * Stored in batch-process-cfg.json.
       * TODO: Move to server side configuration.
       * TODO: i18n should not be shared in en-admin.json
       *
       */
      $scope.batchProcesses = null;

      /**
       * Search filter selected for metadata and template type
       */
      $scope.batchSearchTemplateY = true;
      $scope.batchSearchTemplateN = true;
      $scope.batchSearchTemplateS = false;
      $scope.batchSearchGroups = {};
      $scope.batchSearchUsers = {};
      $scope.batchSearchCategories = {};

      $scope.editors = {};
      $scope.groupinfo = {};
      $scope.editorSelectedId = null;
      $scope.editorGroups = {};



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
        hitsPerPage: gnSearchSettings.hitsperpageValues[0]
      };

      function loadEditors() {
        $http.get('admin.ownership.editors?_content_type=json')
            .success(function(data) {
              $scope.editors = data;
            });
      }
      $scope.selectUser = function(id) {
        $scope.editorSelectedId = id;
        $http.get('admin.usergroups.list?_content_type=json&id=' + id)
            .success(function(data) {
              var uniqueGroup = {};
              angular.forEach(data, function(value) {
                if (!uniqueGroup[value.id]) {
                  uniqueGroup[value.id] = value;
                }
              });
              $scope.editorGroups = uniqueGroup;
            }).error(function(data) {
            });

        $http.get('admin.ownership.groups?_content_type=json&id=' + id)
          .success(function(data) {
              // If user does not have group and only one
              // target group, a simple object is returned
              // and it should be a target group ? FIXME
              if (!data.group && !data.targetGroup) {
                data.group = data;
                data.targetGroup = data;
              }
              // Make all group and targetGroup arrays.
              $scope.groupinfo = {
                group: [].concat(data.group),
                targetGroup: [].concat(data.targetGroup)
              };
            });
      };
      $scope.transfertList = {};

      $scope.tranferOwnership = function(sourceGroup) {
        var params = $scope.transfertList[sourceGroup];

        // check params.targetGroup.id and params.targetEditor defined

        var xml = '<request><sourceUser>' + $scope.editorSelectedId +
            '</sourceUser><sourceGroup>' + sourceGroup +
            '</sourceGroup><targetUser>' + params.targetEditor +
            '</targetUser><targetGroup>' + params.targetGroup.id +
            '</targetGroup></request>';

        params.running = true;
        $http.post('admin.ownership.transfer?_content_type=json', xml, {
          headers: {'Content-type': 'application/xml'}
        }).success(function(data) {
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate('transfertPrivilegesFinished',
                {privileges: data.privileges, metadata: data.metadata}),
            timeout: 2,
            type: 'success'});
          params.running = false;
        }).error(function(data) {
          // TODO
          params.running = false;
        });
      };
      $scope.isRunning = function(sourceGroup) {
        return $scope.transfertList[sourceGroup].running;
      };

      function loadProcessConfig() {
        $http.get($scope.base + 'config/batch-process-cfg.json')
        .success(function(data) {
              $scope.batchProcesses = data.config;

              $timeout(initProcessByRoute);
            });
      }

      function loadGroups() {
        $http.get('admin.group.list?_content_type=json').
            success(function(data) {
              $scope.batchSearchGroups = data;
            }).error(function(data) {
              // TODO
            });
      }
      function loadUsers() {
        $http.get('admin.user.list?_content_type=json').
            success(function(data) {
              $scope.batchSearchUsers = data;
            }).error(function(data) {
              // TODO
            });
      }

      function loadCategories() {
        $http.get('info?_content_type=json&type=categories').
            success(function(data) {
              $scope.batchSearchCategories = data.metadatacategory;
            }).error(function(data) {
              // TODO
            });
      }

      /**
       * Check process progress every ...
       */
      var processCheckInterval = 1000;


      function checkLastBatchProcessReport() {
        // Check if processing
        return $http.get('md.processing.batch.report?_content_type=json').
            success(function(data, status) {
              if (data != 'null') {
                $scope.processReport = data;
                $scope.numberOfRecordsProcessed = data['@processedRecords'];
              }
              if ($scope.processReport &&
                      $scope.processReport['@running'] == 'true') {
                $timeout(checkLastBatchProcessReport, processCheckInterval);
              }
            });
      }

      $scope.runProcess = function(formId) {
        $scope.runProcess(formId, undefined, undefined);
      };

      $scope.runProcess = function(formId, process, testMode) {

        var formParams = $(formId).serialize();
        if (testMode != undefined) {
          formParams += '&test=' + testMode;
        }

        var service = '';
        if (process != undefined) {
          service = process;
        } else {
          service = 'md.processing.batch?_content_type=json';
        }

        $scope.processing = true;
        $scope.processReport = null;
        $http.get(service + '&' +
            formParams)
          .success(function(data) {
              $scope.processReport = data;
              $scope.processReportWarning = data.notFound != 0 ||
                  data.notOwner != 0 ||
                  data.notProcessFound != 0 ||
                  data.metadataErrorReport.metadataErrorReport.length != 0;
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('processFinished'),
                timeout: 2,
                type: 'success'});
              $scope.processing = false;

              angular.forEach($scope.processReport.changed, function(c) {
                if (c.change && !angular.isArray(c.change)) {
                  c.change = [c.change];
                  delete c.changedval;
                  delete c.fieldid;
                  delete c.originalval;
                }
              });


              // Turn off batch report checking for search and replace mode
              // AFA as report is not properly set in session
              // https://github.com/geonetwork/core-geonetwork/issues/828
              if (service.indexOf('md.searchandreplace') === -1) {
                checkLastBatchProcessReport();
              }
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('processError'),
                error: data,
                timeout: 0,
                type: 'danger'});
              $scope.processing = false;
            });

        gnUtilityService.scrollTo('#gn-batch-process-report');
        // FIXME
        if (service.indexOf('md.searchandreplace') === -1) {
          $timeout(checkLastBatchProcessReport, processCheckInterval);
        }
      };

      loadGroups();
      loadUsers();
      loadCategories();
      loadEditors();

      // TODO: Should only do that if batch process is the current page
      loadProcessConfig();
      checkLastBatchProcessReport();

      var initProcessByRoute = function() {
        if ($routeParams.tab === 'batch') {
          // Check if we should select all record
          if ($routeParams.selectAll) {
            // Check if we should select all record
            $scope.$broadcast('resetSearch');
          }
          if ($routeParams.processId) {
            // Select a process defined in the route
            angular.forEach($scope.batchProcesses, function(p) {
              if (p.key === $routeParams.processId) {
                // For each process parameter check if param
                // defined in the location search section
                angular.forEach(p.params, function(param) {
                  var urlParam = $location.search()[param.name];
                  if (urlParam) {
                    param.value = urlParam;
                  }
                });
                $scope.selectedProcess = p;
              }
            });
          }
        }
      };


      // Indexing management

      /**
       * Inform if indexing is ongoing or not
       */
      $scope.isIndexing = false;

      /**
       * Number of records in the index
       */
      $scope.numberOfIndexedRecords = null;

      /**
       * Check index every ...
       */
      var indexCheckInterval = 5000;

      /**
       * Get number of record in the index and
       * then check if indexing is ongoing or not every
       * indexCheckInterval. Stop when not indexing.
       *
       * TODO: Could we kill the check when switching to somewhere else?
       */
      function checkIsIndexing() {
        // Check if indexing
        return $http.get('info?_content_type=json&type=index').
            success(function(data, status) {
              $scope.isIndexing = data.index == 'true';
              if ($scope.isIndexing) {
                $timeout(checkIsIndexing, indexCheckInterval);
              }
              // Get the number of records (template, records, subtemplates)
              $http.get('qi?_content_type=json&' +
                 'template=y or n or s&summaryOnly=true').
                 success(function(data, status) {
                   $scope.numberOfIndexedRecords = data[0]['@count'];
                 });
            });
      }

      checkIsIndexing();

      $scope.rebuildIndex = function() {
        return $http.get('admin.index.rebuild?reset=yes')
            .success(function(data) {
              checkIsIndexing();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.optimizeIndex = function() {
        return $http.get('admin.index.optimize')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('indexOptimizationInProgress'),
                timeout: 2,
                type: 'success'});
              // TODO: Does this is asynch and make the search unavailable?
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.reloadLuceneConfig = function() {
        return $http.get('admin.index.config.reload')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('luceneConfigReloaded'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.clearXLinkCache = function() {
        return $http.get('admin.index.rebuildxlinks')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('xlinkCacheCleared'),
                timeout: 2,
                type: 'success'});
              // TODO: Does this is asynch and make the search unavailable?
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.clearFormatterCache = function() {
        return $http.get('admin.format.clear')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('formatterCacheCleared'),
                timeout: 2,
                type: 'success'});
              // TODO: Does this is asynch and make the search unavailable?
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('formatCacheClearFailure'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };






      $scope.replacer = {};
      $scope.replacer.group = '';
      $scope.replacer.element = '';
      $scope.replacer.elements = [];
      $scope.replacer.replacements = [];
      $scope.data.replacementsConfig = [];

      $scope.addReplacement = function() {
        if (!$scope.replacer.replacements) {
          $scope.replacer.replacements = [];
        }
        $scope.replacer.replacements.push({
          'package': $scope.replacer.group,
          'element': $scope.replacer.element,
          'searchval': $scope.replacer.searchval,
          'replaceval': $scope.replacer.replaceval
        });

        $scope.replacer.group = '';
        $scope.replacer.element = '';
        $scope.replacer.searchval = '';
        $scope.replacer.replaceval = '';
      };

      $scope.removeReplacement = function(replacement) {
        $scope.replacer.replacements.splice(
            $scope.replacer.replacements.indexOf(replacement), 1);
      };

      $scope.loadReplacementConfig = function() {
        try {
          $scope.replacer.replacements =
              angular.fromJson($scope.data.replacementsConfig);
        } catch (e) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('error'),
            error: e,
            timeout: 0,
            type: 'danger'});
        }
      };

      $scope.$watch('replacer.group', function(newValue, oldValue) {

        // Ignore empty value: in initial setup and
        // if form already mirrors new value.
        if ((newValue === '') || (newValue === oldValue)) {
          return;
        }

        $scope.replacer.elements =
            $scope.selectedProcess.config[newValue].elements;
      });


      $scope.$watch('data.selectedProcess', function(newValue, oldValue) {
        // Ignore empty value: in initial setup and
        // if form already mirrors new value.
        if ((newValue === '') || (newValue === oldValue)) {
          return;
        }
        $scope.selectedProcess = newValue;
        $scope.processReport = null;
      });

    }]);

})();
