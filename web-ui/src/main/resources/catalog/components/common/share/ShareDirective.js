(function() {
  goog.provide('gn_share_directive');

  goog.require('gn_share_service');

  var module = angular.module('gn_share_directive', ['gn_share_service']);

  /**
   * @ngdoc directive
   * @name gn_share_directive.directive:gnShare
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
    'gnShareService', 'gnShareConstants', '$translate',
    function(gnShareService, gnShareConstants, $translate) {

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
            gnShareService.savePrivileges(scope.id, scope.groups).then(
                function(data) {
                  scope.$emit('PrivilegesUpdated', true);
                  scope.$emit('StatusUpdated', {
                    msg: $translate('privilegesUpdated'),
                    timeout: 2,
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
        }
      };
    }]);
})();
