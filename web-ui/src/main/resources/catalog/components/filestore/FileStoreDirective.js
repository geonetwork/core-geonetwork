/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
              selectCallback: '&',
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
                scope.selectCallback({ selected: r });
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
                gnfilestoreService.updateStatus(r).then(function() {
                  scope.loadMetadataResources();
                }, function(data) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('resourceUploadError'),
                    error: {
                      message: (data.errorThrown || data.statusText) +
                          (angular.isFunction(data.response) ?
                          data.response().jqXHR.responseJSON.message : '')
                    },
                    timeout: 0,
                    type: 'danger'});
                });
              };
              scope.deleteResource = function(r) {
                gnfilestoreService.delete(r).success(
                    scope.loadMetadataResources
                );
              };

              var uploadResourceSuccess = function(data) {
                $rootScope.$broadcast('gnFileStoreUploadDone');
                scope.clear(scope.queue);
              };

              scope.$on('gnFileStoreUploadDone', scope.loadMetadataResources);

              var uploadResourceFailed = function(e, data) {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate.instant('resourceUploadError'),
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
                    url: '../api/0.1/records/' + scope.uuid +
                        '/attachments?visibility=' + defaultStatus,
                    dropZone: $('#' + scope.id),
                    singleUpload: false,
                    // TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
                    done: uploadResourceSuccess,
                    fail: uploadResourceFailed,
                    headers: {'X-XSRF-TOKEN': $rootScope.csrf}
                  };
                }
              });
            }
          };
        }]);
})();
