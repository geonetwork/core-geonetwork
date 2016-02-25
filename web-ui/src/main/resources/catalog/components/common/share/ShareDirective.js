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
  goog.provide('gn_share_directive');

  goog.require('gn_share_service');

  var module = angular.module('gn_share_directive', ['gn_share_service']);

  /**
   * @ngdoc directive
   * @name gn_share.directive:gnShare
   * @restrict A
   * @requires gnShareService
   * @requires gnShareConstants
   * @requires $translate
   *
   * @description
   * The `gnShare` directive provides a form to display,
   * edit and save privileges for a metadata record.
   *
   * TODO: Add batch mode using md.privileges.batch
   * and md.privileges.batch.update services.
   *
   * TODO: User group only privilege
   */
  module.directive('gnShare', [
    'gnShareService', 'gnShareConstants', 'gnConfig', '$translate', '$filter',
    function(gnShareService, gnShareConstants, gnConfig, $translate, $filter) {

      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/common/share/partials/' +
            'panel.html',
        scope: {
          id: '=gnShare',
          batch: '@gnShareBatch'
        },
        link: function(scope) {
          scope.onlyUserGroup = gnConfig['system.metadataprivs.usergrouponly'];
          scope.disableAllCol = gnShareConstants.disableAllCol;
          scope.displayProfile = gnShareConstants.displayProfile;

          angular.extend(scope, {
            batch: scope.batch === 'true',
            lang: scope.$parent.lang,
            user: scope.$parent.user,
            internalOperations: gnShareConstants.internalOperations,
            internalGroups: gnShareConstants.internalGroups,
            internalGroupsProfiles: gnShareConstants.internalGroupsProfiles
          });

          if (angular.isUndefined(scope.id)) {
            scope.alertMsg = true;
          }

          var loadPrivileges;
          var fillGrid = function(data) {
            scope.groups = data.groups;
            scope.operations = data.operations;
            scope.isAdminOrReviewer = data.isAdminOrReviewer;
          };

          if (!scope.batch) {
            loadPrivileges = function() {
              gnShareService.loadPrivileges(scope.id, scope.user.profile).then(
                  fillGrid);
            };
            scope.$watch('id', loadPrivileges);
          }
          else {
            loadPrivileges = function() {
              gnShareService.loadPrivileges(undefined, scope.user.profile).then(
                  fillGrid);
            };
            loadPrivileges();
          }

          scope.checkAll = function(group) {
            angular.forEach(group.privileges, function(p) {
              if (!p.disabled) {
                p.value = group.isCheckedAll === true;
              }
            });
          };

          scope.sortByLabel = function(group) {
            return group.label[scope.lang];
          };

          scope.save = function() {
            return gnShareService.savePrivileges(scope.id,
                                                 scope.groups,
                                                 scope.user).then(
                function(data) {
                  scope.$emit('PrivilegesUpdated', true);
                  scope.$emit('StatusUpdated', {
                    msg: $translate('privilegesUpdated'),
                    timeout: 0,
                    type: 'success'});
                }, function(data) {
                  scope.$emit('PrivilegesUpdated', false);
                  scope.$emit('StatusUpdated', {
                    title: $translate('privilegesUpdatedError'),
                    error: data,
                    timeout: 0,
                    type: 'danger'});
                });
          };

          scope.sorter = {
            predicate: 'g',
            reverse: false
          };

          scope.setSorter = function(pred) {
            if (pred == scope.sorter.predicate) {
              scope.sorter.reverse = !scope.sorter.reverse;
            } else {
              scope.sorter.reverse = false;
              scope.sorter.predicate = pred;
            }
          };

          scope.sortGroups = function(g) {
            if (scope.sorter.predicate == 'g') {
              return g.label[scope.lang];
            }
            else if (scope.sorter.predicate == 'p') {
              return g.userProfile;
            }
            else {
              return g.privileges[scope.sorter.predicate].value;
            }
          };

        }
      };
    }]);
})();
