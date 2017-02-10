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
  goog.provide('gn_mdactions_service');




  goog.require('gn_category');
  goog.require('gn_popup');
  goog.require('gn_share');


  var module = angular.module('gn_mdactions_service', [
    'gn_share', 'gn_category', 'gn_popup'
  ]);

  module.service('gnMetadataActions', [
    '$rootScope',
    '$timeout',
    '$location',
    'gnHttp',
    'gnMetadataManager',
    'gnAlertService',
    'gnSearchSettings',
    'gnUtilityService',
    'gnShareService',
    'gnPopup',
    'gnMdFormatter',
    '$translate',
    '$q',
    '$http',
    function($rootScope, $timeout, $location, gnHttp,
             gnMetadataManager, gnAlertService, gnSearchSettings,
             gnUtilityService, gnShareService, gnPopup, gnMdFormatter,
             $translate, $q, $http) {

      var windowName = 'geonetwork';
      var windowOption = '';
      var translations = null;
      $translate(['privilegesUpdated',
        'privilegesUpdatedError']).then(function(t) {
        translations = t;
      });
      var alertResult = function(msg) {
        gnAlertService.addAlert({
          msg: msg,
          type: 'success'
        });
      };

      /**
       * Open a popup and compile object content.
       * Bind to an event to close the popup.
       * @param {Object} o popup config
       * @param {Object} scope to build content uppon
       * @param {string} eventName
       */
      var openModal = function(o, scope, eventName) {
        var popup = gnPopup.create(o, scope);
        var myListener = $rootScope.$on(eventName,
            function(e, o) {
              $timeout(function() {
                popup.close();
              }, 0);
              myListener();
            });
      };

      var callBatch = function(service) {
        return gnHttp.callService(service).then(function(data) {
          alertResult(data.data);
        });
      };

      /**
       * Duplicate a metadata that can be a new child of the source one.
       * @param {string} id
       * @param {boolean} child
       */
      var duplicateMetadata = function(id, child) {
        var url = 'catalog.edit#/';
        if (id) {
          if (child) {
            url += 'create?childOf=' + id;
          } else {
            url += 'create?from=' + id;
          }
        }
        window.open(url, '_blank');
      };

      /**
       * Export as PDF (one or selection). If params is search object, we check
       * for sortBy and sortOrder to process the print. If it is a string
       * (uuid), we print only one metadata.
       * @param {Object|string} params
       */
      this.metadataPrint = function(params, bucket) {
        var url;
        if (angular.isObject(params) && params.sortBy) {
          url = gnHttp.getService('mdGetPDFSelection');
          url += '?sortBy=' + params.sortBy;
          if (params.sortOrder) {
            url += '&sortOrder=' + params.sortOrder;
          }
          url += '&bucket=' + bucket;
          location.replace(url);
        }
        else if (angular.isString(params)) {
          gnMdFormatter.getFormatterUrl(null, null, params).then(function(url) {
            $http.get(url, {
              headers: {
                Accept: 'text/html'
              }
            });
          });
        }
      };

      /**
       * Export one metadata to RDF format.
       * @param {string} uuid
       */
      this.metadataRDF = function(uuid) {
        var url = gnHttp.getService('mdGetRDF') + '?uuid=' + uuid;
        location.replace(url);
      };

      /**
       * Export to MEF format (one or selection). If uuid is provided, export
       * one metadata, else export the whole selection.
       * @param {string} uuid
       */
      this.metadataMEF = function(uuid, bucket) {
        var url = gnHttp.getService('mdGetMEF') + '?version=2';
        url += angular.isDefined(uuid) ?
            '&uuid=' + uuid : '&format=full';
        url += angular.isDefined(bucket) ?
            '&bucket=' + bucket : '';

        location.replace(url);
      };

      this.exportCSV = function(bucket) {
        window.open(gnHttp.getService('csv') +
            '?bucket=' + bucket, windowName, windowOption);
      };
      this.validateMd = function(md, bucket) {
        if (md) {
          return gnMetadataManager.validate(md.getId()).then(function() {
            $rootScope.$broadcast('mdSelectNone');
            $rootScope.$broadcast('search');
          });
        } else {
          return gnHttp.callService('../api/records/validate?' +
              'bucket=' + bucket, null, {
                    method: 'PUT'
                  }).then(function(data) {
            alertResult(data.data);
            $rootScope.$broadcast('mdSelectNone');
            $rootScope.$broadcast('search');
          });
        }
      };

      this.deleteMd = function(md, bucket) {
        if (md) {
          return gnMetadataManager.remove(md.getId()).then(function() {
            $rootScope.$broadcast('mdSelectNone');
            // TODO: Here we may introduce a delay to not display the deleted
            // record in results.
            // https://github.com/geonetwork/core-geonetwork/issues/759
            $rootScope.$broadcast('search');
          });
        }
        else {
          return $http.delete('../api/records?' +
              'bucket=' + bucket).then(function() {
            $rootScope.$broadcast('mdSelectNone');
            $rootScope.$broadcast('search');
          });
        }
      };


      this.openPrivilegesPanel = function(md, scope) {
        openModal({
          title: $translate.instant('privileges') + ' - ' +
              (md.title || md.defaultTitle),
          content: '<div gn-share="' + md.getId() + '"></div>',
          className: 'gn-privileges-popup'
        }, scope, 'PrivilegesUpdated');
      };

      this.openUpdateStatusPanel = function(scope) {
        openModal({
          title: 'updateStatus',
          content: '<div data-gn-metadata-status-updater="md"></div>'
        }, scope, 'metadataStatusUpdated');
      };

      this.startWorkflow = function(md, scope) {
        return $http.put('../api/records/' + md.getId() +
            '/status?status=1&comment=Enable workflow').then(
            function(data) {
              gnMetadataManager.updateMdObj(md);
              scope.$emit('metadataStatusUpdated', true);
              scope.$emit('StatusUpdated', {
                msg: $translate.instant('metadataStatusUpdatedWithNoErrors'),
                timeout: 2,
                type: 'success'});
            }, function(data) {
              scope.$emit('metadataStatusUpdated', false);
              scope.$emit('StatusUpdated', {
                title: $translate.instant('metadataStatusUpdatedErrors'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      this.openPrivilegesBatchPanel = function(scope, bucket) {
        openModal({
          title: 'privileges',
          content: '<div gn-share="" ' +
              'gn-share-batch="true" ' +
              'selection-bucket="' + bucket + '"></div>',
          className: 'gn-privileges-popup'
        }, scope, 'PrivilegesUpdated');
      };
      this.openBatchEditing = function(scope) {
        $location.path('/batchediting');
      };
      this.openCategoriesBatchPanel = function(bucket, scope) {
        openModal({
          title: 'categories',
          content: '<div gn-batch-categories="" ' +
              'selection-bucket="' + bucket + '"></div>'
        }, scope, 'CategoriesUpdated');
      };

      this.openTransferOwnership = function(md, bucket, scope) {
        var uuid = md ? md.getUuid() : '';
        var ownerId = md ? md.getOwnerId() : '';
        var groupOwner = md ? md.getGroupOwner() : '';
        openModal({
          title: 'transferOwnership',
          content: '<div gn-transfer-ownership="' + uuid +
              '" gn-transfer-md-owner="' + ownerId + '" ' +
              '" gn-transfer-md-group-owner="' + groupOwner + '" ' +
              'selection-bucket="' + bucket + '"></div>'
        }, scope, 'TransferOwnership');
      };
      /**
       * Duplicate the given metadata. Open the editor in new page.
       * @param {string} md
       */
      this.duplicate = function(md) {
        duplicateMetadata(md.getId(), false);
      };

      /**
       * Create a child of the given metadata. Open the editor in new page.
       * @param {string} md
       */
      this.createChild = function(md) {
        duplicateMetadata(md.getId(), true);
      };

      /**
       * Update publication on metadata (one or selection).
       * If a md is provided, it update publication of the given md, depending
       * on its current state. If no metadata is given, it updates the
       * publication on all selected metadata to the given flag (on|off).
       * @param {Object|undefined} md
       * @param {string} flag
       * @return {*}
       */
      this.publish = function(md, bucket, flag, scope) {

        if (md) {
          flag = md.isPublished() ? 'off' : 'on';
        }
        var onOrOff = flag === 'on';

        return gnShareService.publish(
            angular.isDefined(md) ? md.getId() : undefined,
            angular.isDefined(md) ? undefined : bucket,
            onOrOff, $rootScope.user)
            .then(
            function(data) {
              scope.$emit('PrivilegesUpdated', true);
              scope.$emit('StatusUpdated', {
                msg: translations.privilegesUpdated,
                timeout: 0,
                type: 'success'});
              if (md) {
                md.publish();
              }
            }, function(data) {
              scope.$emit('PrivilegesUpdated', false);
              scope.$emit('StatusUpdated', {
                title: translations.privilegesUpdatedError,
                error: data,
                timeout: 0,
                type: 'danger'});
            });

      };

      this.assignGroup = function(metadataId, groupId) {
        var defer = $q.defer();
        $http.put('../api/records/' + metadataId +
            '/group', groupId)
            .success(function(data) {
              defer.resolve(data);
            })
            .error(function(data) {
              defer.reject(data);
            });
        return defer.promise;
      };

      this.assignCategories = function(metadataId, categories) {
        var defer = $q.defer();
        $http.get('../records/' + metadataId +
                  '/tags?id=' + categories.join('&id='))
            .success(function(data) {
              defer.resolve(data);
            })
            .error(function(data) {
              defer.reject(data);
            });
        return defer.promise;
      };

      this.startVersioning = function(metadataId) {
        var defer = $q.defer();
        $http.get('md.versioning.start?id=' + metadataId)
            .success(function(data) {
              defer.resolve(data);
            })
            .error(function(data) {
              defer.reject(data);
            });
        return defer.promise;
      };

      /**
       * Get html formatter link for the given md
       * @param {Object} md
       */
      this.getPermalink = function(md) {
        var url = $location.absUrl().split('#')[0] + '#/metadata/' +
            md.getUuid();
        gnUtilityService.getPermalink(md.title || md.defaultTitle, url);
      };
    }]);
})();
