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


  goog.require('gn_popup');
  goog.require('gn_share_service');

  var module = angular.module('gn_share_directive', ['gn_share_service', 'gn_popup']);

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
    'gnShareService', 'gnShareConstants', 'gnConfig', 'gnUtilityService', '$translate', '$filter',
    function(gnShareService, gnShareConstants, gnConfig, gnUtilityService, $translate, $filter) {

      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/common/share/partials/' +
            'panel.html',
        scope: {
          id: '=gnShare',
          batch: '@gnShareBatch',
          selectionBucket: '@'
        },
        link: function(scope) {
          var translations = null, isBatch = scope.batch === 'true';
          $translate(['privilegesUpdated',
            'privilegesUpdatedError']).then(function(t) {
            translations = t;
          });


          scope.onlyUserGroup = gnConfig['system.metadataprivs.usergrouponly'];
          scope.disableAllCol = gnShareConstants.disableAllCol;
          scope.displayProfile = gnShareConstants.displayProfile;
          scope.icons = gnShareConstants.icons;

          angular.extend(scope, {
            batch: isBatch,
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
            scope.privileges = data.privileges;
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

          scope.isAllowed = function(group, key) {
            return true; // TODO
          };
          scope.checkAll = function(group) {
            angular.forEach(group.operations, function(value, key) {
              if (scope.isAllowed(group, key)) {
                group.operations[key] = group.isCheckedAll === true;
              }
              $('[name=' + group.group + '-' + key + ']').addClass('ng-dirty');
            });
          };

          scope.sortByLabel = function(group) {
            return group.label[scope.lang];
          };

          scope.reset = function() {
            $('#opsForm').find('input.ng-dirty').each(function(idx, el) {
              el.checked = false;
              $(el).removeClass('ng-dirty');
            });
          };

          scope.save = function(replace) {

            if (!replace) {
              var updateCheckBoxes = [];
              $('#opsForm input.ng-dirty[type=checkbox][data-ng-model]')
                  .each(function(c, el) {
                    updateCheckBoxes.push($(el).attr('name'));
                  });
              angular.forEach(scope.privileges, function(value, group) {
                var keyPrefix = value.group + '-';
                angular.forEach(value.operations, function(value, op) {
                  var key = keyPrefix + op;
                  if ($.inArray(key, updateCheckBoxes) === -1) {
                    delete scope.privileges[group].operations[op];
                  }
                });
              });
            }
            return gnShareService.savePrivileges(
                isBatch ? undefined : scope.id,
                isBatch ? scope.selectionBucket : undefined,
                scope.privileges,
                scope.user,
                replace).then(
                function(response) {
                  if (response.data !== '') {
                    scope.processReport = response.data;

                    // A report is returned
                    gnUtilityService.openModal({
                      title: translations.privilegesUpdated,
                      content: '<div gn-batch-report="processReport"></div>',
                      className: 'gn-privileges-popup',
                      onCloseCallback: function() {
                        scope.$emit('PrivilegesUpdated', true);
                        scope.processReport = null;
                      }
                    }, scope, 'PrivilegesUpdated');
                  } else {
                    scope.$emit('PrivilegesUpdated', true);
                    scope.$emit('StatusUpdated', {
                      msg: translations.privilegesUpdated,
                      timeout: 0,
                      type: 'success'});
                  }

                }, function(response) {
                  scope.$emit('PrivilegesUpdated', false);
                  scope.$emit('StatusUpdated', {
                    title: translations.privilegesUpdatedError,
                    error: response.data,
                    timeout: 0,
                    type: 'danger'});
                });
          };

          scope.pFilter = '';
          scope.pFilterFn = function(v) {
            if (scope.pFilter === '') return true;
            var v = $translate.instant('group-' + v.group);
            return v.toLowerCase().indexOf(
                scope.pFilter.toLocaleLowerCase()) >= 0;
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
              return $translate.instant('group-' + g.group);
            }
            else if (scope.sorter.predicate == 'p') {
              return g.userProfile;
            }
            else {
              return g.operations[scope.sorter.predicate];
            }
          };

        }
      };
    }]);
})();
