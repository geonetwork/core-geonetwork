(function() {
  goog.provide('gn_catalog_service');

  goog.require('gn_urlutils_service');

  var module = angular.module('gn_catalog_service', [
    'gn_urlutils_service'
  ]);

  module.provider('gnNewMetadata', function() {
    this.$get = ['$http', '$location', 'gnUrlUtils',
                 function($http, $location, gnUrlUtils) {
        return {
          createNewMetadata: function(id, groupId, fullPrivileges, 
              template, tab) {
            var url = gnUrlUtils.append('md.create@json',
                gnUrlUtils.toKeyValue({
                  group: groupId,
                  id: id,
                  isTemplate: template || 'n',
                  fullPrivileges: fullPrivileges || true
                })
                );

            $http.get(url).success(function(data) {
              $location.path('/metadata/' + data.id);
            });
            // TODO : handle creation error
          }
        };
      }];
  });

  module.provider('gnBatchProcessing', function() {
    this.$get = ['$http', '$location', 'gnUrlUtils',
                 function($http, $location, gnUrlUtils) {

        var processing = true;
        var processReport = null;
        return {

          /**
           * Run process md.processing.new
           */
          runProcessNew: function(params) {
            var url = gnUrlUtils.append('md.processing.new?',
                gnUrlUtils.toKeyValue(params));

            $http.get(url).success(function(data) {
              console.log('md.processing.new success');
            });
          },

          // TODO : write batch processing service here
          // from adminTools controller
          runProcess: function(formId) {
            processing = true;
            processReport = null;
            $http.get('md.processing.batch@json?' +
                    $(formId).serialize())
              .success(function(data) {
                  processReport = data;
                  processReportWarning = data.notFound != 0 ||
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
          }
        };
      }];
  });
})();
