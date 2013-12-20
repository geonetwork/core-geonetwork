(function() {
  goog.provide('gn_admintools_controller');


  var module = angular.module('gn_admintools_controller',
      []);


  /**
   * GnAdminToolsController provides administration tools
   */
  module.controller('GnAdminToolsController', [
    '$scope', '$http', '$rootScope', '$translate', '$compile',
    '$q', '$timeout',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $http, $rootScope, $translate, $compile, 
        $q, $timeout,
            gnMetadataManagerService, 
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
       * The pagination config
       */
      $scope.recordsToProcessPagination = {
        pages: -1,
        currentPage: 0,
        hitsPerPage: 10
      };
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
                angular.forEach(data, function (value) {
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

      // TODO: Should only do that if batch process is the current page
      loadProcessConfig();
      checkLastBatchProcessReport();
      loadGroups();
      loadUsers();
      loadCategories();
      loadEditors();

      $scope.recordsToProcessSearchFor = function(e) {
        $scope.recordsToProcessFilter = (e ? e.target.value : '');
        $scope.recordsToProcessPagination.currentPage = 0;
        $scope.recordsToProcessSearch();
      };

      // Run a search according to paging option
      // TODO Search criteria logic should probably move
      // to some kind of SearchManager module
      // FIXME : can't watch the recordsToProcessFilter changes ?
      $scope.recordsToProcessSearch = function() {
        var pageOptions = $scope.recordsToProcessPagination,
            criteria = [
                        {fast: 'index'},
                        {from:
                    (pageOptions.currentPage * pageOptions.hitsPerPage + 1)},
                        {to:
                    (pageOptions.currentPage + 1) * pageOptions.hitsPerPage},
                        {any: $scope.recordsToProcessFilter}],
            fields = {'#gn-batchSearchTemplateY': 'y',
                      '#gn-batchSearchTemplateN': 'n',
                      '#gn-batchSearchTemplateS': 's'};
        templateValues = [];

        angular.forEach(fields, function(value, key) {
          $(key)[0] && $(key)[0].checked && templateValues.push(value);
        });
        criteria.push({template: templateValues.join(' or ')});

        if ($('#gn-batchSearchGroupOwner')[0]) {
          var g = $('#gn-batchSearchGroupOwner')[0]
                        .options[$('#gn-batchSearchGroupOwner')[0]
                            .selectedIndex]
                        .value;
          g !== '' && criteria.push({group: g});
        }

        if ($('#gn-batchSearchOwner')[0]) {
          var g = $('#gn-batchSearchOwner')[0]
                        .options[$('#gn-batchSearchOwner')[0].selectedIndex]
                        .value;
          g !== '' && criteria.push({_owner: g});
        }
        if ($('#gn-batchSearchCategory')[0]) {
          var g = $('#gn-batchSearchCategory')[0]
                        .options[$('#gn-batchSearchCategory')[0].selectedIndex]
                        .value;
          g !== '' && criteria.push({category: g});
        }
        var params = '';
        angular.forEach(criteria, function(value) {
          angular.forEach(value, function(val, key) {
            params += key + '=' + val + '&';
          });
        });
        gnSearchManagerService.search('q@json?' + params)
          .then(function(data) {
              $scope.recordsToProcess = data;
              $scope.recordsToProcessPagination.pages = Math.round(
                  data.count /
                  $scope.recordsToProcessPagination.hitsPerPage, 0);
            }, function(data) {
              // TODO
            });
      };

      // When the current page or search criteria change trigger the search
      angular.forEach(['recordsToProcessPagination.currentPage'],
          function(value) {
            $scope.$watch(value, function() {
              $scope.recordsToProcessSearch();
            });
          });

      // Search when typing a new filter FIXME ?
      //      $scope.$watch('recordsToProcessFilter', function() {
      //          console.log('changed' + $scope.recordsToProcessFilter);
      //          $scope.recordsToProcessSearch();
      //        });

      // Clear current selection and then search for all by default
      gnMetadataManagerService.selectNone().then(function() {
        $scope.recordsToProcessSearch();
      });




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
