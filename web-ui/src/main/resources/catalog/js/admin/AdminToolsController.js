(function() {
  goog.provide('gn_admintools_controller');


  goog.require('gn_search');

  var module = angular.module('gn_admintools_controller',
      ['gn_search']);


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
      $scope.searchObj = {
        permalink: false,
        hitsperpageValues: [20, 50, 100],
        params: {
          sortBy: 'changeDate',
          _isTemplate: 'y or n',
          from: 1,
          to: 9
        }
      };
      $scope.resultTemplate = '../../catalog/' +
          'components/search/resultsview/' +
          'partials/viewtemplates/title.html';
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
        $scope.searchObj.params._isTemplate = values.join(' or ');
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





      // Dependent groups batch replace
      $scope.batchReplacerGroups = {
        'metadata': {
          'elements': [
            'id.contact.individualName',
            'id.contact.organisationName',
            'id.contact.voicePhone',
            'id.contact.faxPhone',
            'id.contact.address',
            'id.contact.city',
            'id.contact.province',
            'id.contact.postalCode',
            'id.contact.country',
            'id.contact.email',
            'id.contact.or.url',
            'id.contact.or.ap',
            'id.contact.or.name',
            'id.contact.or.description',
            'id.contact.hoursOfService',
            'id.contact.contactInstructions'
          ]
        },
        'data-identification': {
          'elements': [
            'id.dataid.abstract',
            'id.dataid.purpose',
            'id.dataid.keyword',
            'id.dataid.citation.individualName',
            'id.dataid.citation.organisationName',
            'id.dataid.citation.voicePhone',
            'id.dataid.citation.faxPhone',
            'id.dataid.citation.address',
            'id.dataid.citation.city',
            'id.dataid.citation.province',
            'id.dataid.citationt.postalCode',
            'id.dataid.citation.country',
            'id.dataid.citation.email',
            'id.dataid.citation.or.url',
            'id.dataid.citation.or.ap',
            'id.dataid.citation.or.name',
            'id.dataid.citation.or.description',
            'id.dataid.citation.hoursOfService',
            'id.dataid.citation.contactInstructions',
            'id.dataid.poc.individualName',
            'id.dataid.poc.organisationName',
            'id.dataid.poc.voicePhone',
            'id.dataid.poc.faxPhone',
            'id.dataid.poc.address',
            'id.dataid.poc.city',
            'id.dataid.poc.province',
            'id.dataid.poc.postalCode',
            'id.dataid.poc.country',
            'id.dataid.poc.email',
            'id.dataid.poc.or.url',
            'id.dataid.poc.or.ap',
            'id.dataid.poc.or.name',
            'id.dataid.poc.or.description',
            'id.dataid.poc.hoursOfService',
            'id.dataid.poc.contactInstructions',

            'id.dataid.resc.gc.useLimitation',
            'id.dataid.resc.lc.useLimitation',
            'id.dataid.resc.lc.otherConstraints',
            'id.dataid.resc.sc.useLimitation',
            'id.dataid.resc.otherConstraints'
          ]
        },
        'service-identification': {
          'elements': [
            'id.serviceid.abstract',
            'id.serviceid.purpose',
            'id.serviceid.citation.individualName',
            'id.serviceid.citation.organisationName',
            'id.serviceid.citation.voicePhone',
            'id.serviceid.citation.faxPhone',
            'id.serviceid.citation.address',
            'id.serviceid.citation.city',
            'id.serviceid.citation.province',
            'id.serviceid.citationt.postalCode',
            'id.serviceid.citation.country',
            'id.serviceid.citation.email',
            'id.serviceid.citation.or.url',
            'id.serviceid.citation.or.ap',
            'id.serviceid.citation.or.name',
            'id.serviceid.citation.or.description',
            'id.serviceid.citation.hoursOfService',
            'id.serviceid.citation.contactInstructions',
            'id.serviceid.poc.individualName',
            'id.serviceid.poc.organisationName',
            'id.serviceid.poc.voicePhone',
            'id.serviceid.poc.faxPhone',
            'id.serviceid.poc.address',
            'id.serviceid.poc.city',
            'id.serviceid.poc.province',
            'id.serviceid.poc.postalCode',
            'id.serviceid.poc.country',
            'id.serviceid.poc.email',
            'id.serviceid.poc.or.url',
            'id.serviceid.poc.or.ap',
            'id.serviceid.poc.or.name',
            'id.serviceid.poc.or.description',
            'id.serviceid.poc.hoursOfService',
            'id.serviceid.poc.contactInstructions',
            'id.serviceid.connectpoint.url',
            'id.serviceid.connectpoint.ap',
            'id.serviceid.connectpoint.name',
            'id.serviceid.connectpoint.description'
          ]

        },
        'maintenance-information': {
          'elements': [
            'mi.contact.individualName',
            'mi.contact.organisationName',
            'mi.contact.voicePhone',
            'mi.contact.faxPhone',
            'mi.contact.address',
            'mi.contact.city',
            'mi.contact.province',
            'mi.contact.postalCode',
            'mi.contact.country',
            'mi.contact.email',
            'mi.contact.or.url',
            'mi.contact.or.ap',
            'mi.contact.or.name',
            'mi.contact.or.description',
            'mi.contact.hoursOfService',
            'mi.contact.contactInstructions'
          ]
        },
        'content-information': {
          'elements': [
            'ci.citation.individualName',
            'ci.citation.organisationName',
            'ci.citation.voicePhone',
            'ci.citation.faxPhone',
            'ci.citation.address',
            'ci.citation.city',
            'ci.citation.province',
            'ci.citation.postalCode',
            'ci.citation.country',
            'ci.citation.email',
            'ci.citation.or.url',
            'ci.citation.or.ap',
            'ci.citation.or.name',
            'ci.citation.or.description',
            'ci.citation.hoursOfService',
            'ci.citation.contactInstructions'
          ]

        },
        'distribution-information': {
          'elements': [
            'di.contact.individualName',
            'di.contact.organisationName',
            'di.contact.voicePhone',
            'di.contact.faxPhone',
            'di.contact.address',
            'di.contact.city',
            'di.contact.province',
            'di.contact.postalCode',
            'di.contact.country',
            'di.contact.email',
            'di.contact.hoursOfService',
            'di.contact.contactInstructions',
            'di.fees',
            'di.transferOptions.url',
            'di.transferOptions.ap',
            'di.transferOptions.name',
            'di.transferOptions.description'
          ]
        }

      };


      $scope.replacer = {};
      $scope.replacer.group = '';
      $scope.replacer.element = '';
      $scope.replacer.elements = [];
      $scope.replacer.replacements = [];

      $scope.addReplacement = function() {
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

      $scope.$watch('replacer.group', function(newValue, oldValue) {

        // Ignore empty value: in initial setup and
        // if form already mirrors new value.
        if ((newValue === '') || (newValue === oldValue)) {
          return;
        }

        $scope.replacer.elements =
            $scope.batchReplacerGroups[newValue].elements;
      });


      $scope.$watch('selectedProcess', function(newValue, oldValue) {

        // Ignore empty value: in initial setup and
        // if form already mirrors new value.
        if ((newValue === '') || (newValue === oldValue)) {
          return;
        }

        $scope.processReport = null;
      });

    }]);

})();
