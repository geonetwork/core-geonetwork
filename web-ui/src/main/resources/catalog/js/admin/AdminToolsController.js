(function() {
  goog.provide('gn_admintools_controller');


  var module = angular.module('gn_admintools_controller',
      []);


  /**
   * GnAdminToolsController provides administration tools
   */
  module.controller('GnAdminToolsController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    '$q', '$timeout',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile, 
        $q, $timeout,
            gnMetadataManagerService, 
            gnSearchManagerService, 
            gnUtilityService) {

      var templateFolder = '../../catalog/templates/admin/tools/';
      var availableTemplates = [
        'tools', 'index'
      ];

      $scope.defaultToolTab = 'batch';

      $scope.getTemplate = function() {
        $scope.type = $scope.defaultToolTab;
        if (availableTemplates.indexOf($routeParams.toolTab) > -1) {
          $scope.type = $routeParams.toolTab;
        }
        return templateFolder + $scope.type + '.html';
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
       * TODO : move to a config file or add
       * service to retrieve list from server
       */
      $scope.batchProcesses = [{
        key: 'thumbnails-host-url-relocator',
        params: [
                 {name: 'urlPrefix', value: 'http://oldurl'},
                 {name: 'newUrlPrefix', value: 'http://newurl'}
        ]
      },{
        key: 'keywords-mapper',
        params: [
          {name: 'search', value: 'key1;key2'},
          {name: 'replace', value: 'newkey1;newkey2'}
        ]
      },{
        key: 'contact-updater',
        params: [
          {name: 'emailToSearch', value: '', type: 'email'},
          {name: 'contactAsXML', value: '', type: 'textarea',
            help: 'contactUpdaterXMLParam'}
        ]
      }];




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

              gnUtilityService.scrollTo('#gn-batch-process-report');
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('processError'),
                error: data,
                timeout: 0,
                type: 'danger'});
              $scope.processing = false;
            });

      };

      $scope.recordsToProcessSearchFor = function(e) {
        $scope.recordsToProcessFilter = (e ? e.target.value : '');
        $scope.recordsToProcessPagination.currentPage = 0;
        $scope.recordsToProcessSearch();
      };

      // Run a search according to paging option
      // This should probably move to some kind of SearchManager module
      // FIXME : can't watch the recordsToProcessFilter changes ?
      $scope.recordsToProcessSearch = function() {
        var pageOptions = $scope.recordsToProcessPagination;

        gnSearchManagerService.search('q@json?fast=index' +
            '&any=' + $scope.recordsToProcessFilter +
            '&from=' + (pageOptions.currentPage *
            pageOptions.hitsPerPage + 1) +
            '&to=' + ((pageOptions.currentPage + 1) *
                          pageOptions.hitsPerPage))
          .then(function(data) {
              $scope.recordsToProcess = data.metadata;
              $scope.recordsToProcessPagination.pages = Math.round(
                  data.count /
                  $scope.recordsToProcessPagination.hitsPerPage, 0);
            }, function(data) {
              // TODO
            });
      };
      // When the current page change trigger the search
      $scope.$watch('recordsToProcessPagination.currentPage', function() {
        $scope.recordsToProcessSearch();
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
