(function() {
  goog.provide('gn_filestore_directive');

  /**
   */
  angular.module('gn_filestore_directive', [
    'blueimp.fileupload'
  ])

  .directive('gnFileStore', [
        'gnFileStoreService',
        '$translate',
        '$rootScope',
        '$parse',
        function(gnfilestoreService, $translate, $rootScope, $parse) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/filestore/' +
                'partials/filestore.html',
            scope: {
              uuid: '=gnFileStore',
              current: '=',
              filter: '='
            },
            link: function(scope, element, attrs, controller) {
              scope.autoUpload =
                  angular.isUndefined(attrs['autoUpload']) ||
                  attrs['autoUpload'] == 'true';
              var defaultStatus =
                  angular.isUndefined(attrs['defaultStatus']) ?
                  'public' : attrs['defaultStatus'];

              scope.setResource = function(r) {
                scope.current = r;
              };
              scope.id = Math.random();
              scope.metadataResources = [];

              scope.loadMetadataResources = function() {
                gnfilestoreService.get(scope.uuid, scope.filter).success(
                    function(data) {
                      scope.metadataResources = data;
                    }
                );
              };
              scope.setResourceStatus = function(r) {
                gnfilestoreService.updateStatus(r).success(
                    scope.loadMetadataResources
                );
              };
              scope.deleteResource = function(r) {
                gnfilestoreService.delete(r).success(
                    scope.loadMetadataResources
                );
              };

              var uploadResourceSuccess = function(data) {
                $rootScope.$broadcast('gnfilestoreUploadDone');
                scope.clear(scope.queue);
              };

              scope.$on('gnfilestoreUploadDone', scope.loadMetadataResources);

              var uploadResourceFailed = function(e, data) {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate('resourceUploadError'),
                  error: {
                    message: data.errorThrown +
                        angular.isDefined(
                        data.response().jqXHR.responseJSON.message) ?
                        data.response().jqXHR.responseJSON.message : ''
                  },
                  timeout: 0,
                  type: 'danger'});
              };

              scope.$watch('filter', function(newValue, oldValue) {
                if (angular.isDefined(scope.uuid) &&
                    newValue != oldValue) {
                  scope.loadMetadataResources();
                }
              });
              scope.$watch('uuid', function(newValue, oldValue) {
                if (angular.isDefined(scope.uuid) &&
                    newValue != oldValue) {

                  scope.loadMetadataResources();

                  scope.queue = [];
                  scope.filestoreUploadOptions = {
                    autoUpload: scope.autoUpload,
                    url: '../api/metadata/' + scope.uuid +
                        '/resources?share=' + defaultStatus,
                    dropZone: $('#' + scope.id),
                    singleUpload: false,
                    // TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
                    done: uploadResourceSuccess,
                    fail: uploadResourceFailed
                  };
                }
              });
            }
          };
        }]);
})();
