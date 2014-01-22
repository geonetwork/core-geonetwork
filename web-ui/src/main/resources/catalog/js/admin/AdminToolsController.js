(function() {
  goog.provide('gn_admintools_controller');


  var module = angular.module('gn_admintools_controller',
      []);


  /**
   * GnAdminToolsController provides administration tools
   */
  module.controller('GnAdminToolsController', [
    '$scope', '$http', '$rootScope', '$translate', '$compile',
    '$q', '$timeout', '$routeParams', '$location',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $http, $rootScope, $translate, $compile, 
        $q, $timeout, $routeParams, $location,
            gnSearchManagerService, 
            gnUtilityService) {


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

      function loadEditors() {
        $http.get('admin.ownership.editors@json')
            .success(function(data) {
              $scope.editors = data;
            });
      }
      $scope.selectUser = function(id) {
        $scope.editorSelectedId = id;
        $http.get('admin.usergroups.list@json?id=' + id)
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

        $http.get('admin.ownership.groups@json?id=' + id)
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
        $http.post('admin.ownership.transfer@json', xml, {
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
        $http.get('admin.group.list@json').success(function(data) {
          $scope.batchSearchGroups = data;
        }).error(function(data) {
          // TODO
        });
      }
      function loadUsers() {
        $http.get('admin.user.list@json').success(function(data) {
          $scope.batchSearchUsers = data;
        }).error(function(data) {
          // TODO
        });
      }

      function loadCategories() {
        $http.get('info@json?type=categories').success(function(data) {
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
        return $http.get('md.processing.batch.report@json').
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
        $scope.processing = true;
        $scope.processReport = null;
        $http.get('md.processing.batch@json?' +
                $(formId).serialize())
          .success(function(data) {
              $scope.processReport = data;
              $scope.processReportWarning = data.notFound != 0 ||
                  data.notOwner != 0 ||
                  data.notProcessFound != 0;
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('processFinished'),
                timeout: 2,
                type: 'success'});
              $scope.processing = false;

              checkLastBatchProcessReport();
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
        $timeout(checkLastBatchProcessReport, processCheckInterval);
      };

      loadGroups();
      loadUsers();
      loadCategories();
      loadEditors();

      // TODO: Should only do that if batch process is the current page
      loadProcessConfig();
      checkLastBatchProcessReport();

      $scope.setTemplate = function(params) {
        var values = [];
        if ($('#batchSearchTemplateY')[0].checked) values.push('y');
        if ($('#batchSearchTemplateN')[0].checked) values.push('n');
        if ($('#batchSearchTemplateS')[0].checked) values.push('s');
        params._isTemplate = values.join(' or ');
      };

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
      var indexCheckInterval = 1000;

      /**
       * Get number of record in the index and
       * then check if indexing is ongoing or not every
       * indexCheckInterval. Stop when not indexing.
       *
       * TODO: Could we kill the check when switching to somewhere else?
       */
      function checkIsIndexing() {
        // Check if indexing
        return $http.get('info@json?type=index').
            success(function(data, status) {
              $scope.isIndexing = data.index == 'true';
              if ($scope.isIndexing) {
                $timeout(checkIsIndexing, indexCheckInterval);
              }
              // Get the number of records (template, records, subtemplates)
              $http.get('qi@json?template=y or n or s&summaryOnly=true').
                 success(function(data, status) {
                   $scope.numberOfIndexedRecords = data[0]['@count'];
                 });
            });
      }

      checkIsIndexing();

      $scope.rebuildIndex = function() {
        $http.get('admin.index.rebuild?reset=yes')
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
        $http.get('admin.index.optimize')
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
        $http.get('admin.index.config.reload')
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
        $http.get('admin.index.rebuildxlinks')
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

    }]);

})();
