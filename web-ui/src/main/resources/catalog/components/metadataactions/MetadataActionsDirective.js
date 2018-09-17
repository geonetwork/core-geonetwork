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
  goog.provide('gn_mdactions_directive');

  goog.require('gn_mdactions_service');

  var module = angular.module('gn_mdactions_directive', []);

  module.directive('gnMetadataStatusUpdater', ['$translate', '$http',
    'gnMetadataManager',
    function($translate, $http, gnMetadataManager) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'statusupdater.html',
        scope: {
          md: '=gnMetadataStatusUpdater'
        },
        link: function(scope) {
          scope.lang = scope.$parent.lang;
          var user = scope.$parent.user;
          scope.newStatus = {value: '0'};

          var metadataId = scope.md.getId();
          function init() {
            return $http.get('md.status.list?' +
                '_content_type=json&id=' + metadataId).
                success(function(data) {
                  scope.status =
                     data !== 'null' ? data.statusvalue : null;

                  angular.forEach(scope.status, function(s) {
                    if (s.on) {
                      scope.newStatus.value = s.id;
                      return;
                    }
                  });
                });
          };

          scope.updateStatus = function() {
            return $http.put('../api/records/' + metadataId +
                '/status?status=' + scope.newStatus.value +
                '&comment=' + (scope.changeMessage || '')
            ).then(
                function(data) {
                  gnMetadataManager.updateMdObj(scope.md);
                  scope.$emit('metadataStatusUpdated', true);
                  scope.$emit('StatusUpdated', {
                    msg: $translate.instant(
                       'metadataStatusUpdatedWithNoErrors'),
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

          scope.cantStatus = function(status) {
            return ((status == 5 || status == 2 || status == 3) &&
                !user.isReviewerOrMore());
          };

          init();
        }
      };
    }]);
  /**
   * @ngdoc directive
   * @name gn_mdactions_directive.directive:gnMetadataCategoryUpdater
   * @restrict A
   * @requires gnMetadataCategoryUpdater
   *
   * @description
   * The `gnMetadataCategoryUpdater` directive provides a
   * dropdown button which allows to set the metadata
   * categories.
   *
   * Don't use this directive more than one time in
   * the same page.
   */
  module.directive('gnMetadataCategoryUpdater', [
    'gnMetadataActions', '$translate', '$http',
    '$rootScope', '$filter', '$timeout',
    function(gnMetadataActions, $translate, $http,
             $rootScope, $filter, $timeout) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'metadatacategoryupdater.html',
        scope: {
          currentCategories: '=gnMetadataCategoryUpdater',
          metadataUuid: '=',
          groupOwner: '=gnGroupOwner'
        },
        link: function(scope, e, attrs) {
          scope.lang = scope.$parent.lang;
          scope.categories = null;
          scope.ids = [];
          scope.tid = 'tagsinput' + Math.floor(Math.random() * 10000);
          scope.mode = attrs['gnMode'] || 'btn';

          var initialCategories = [];
          var tid = '#' + scope.tid;

          scope.updateCategoriesAllowed = function() {
            if (angular.isDefined(scope.groupOwner)) {
              $http.get('../api/groups/' + scope.groupOwner, {cache: true}).
                  success(function(data) {
                    scope.enableallowedcategories =
                        data.enableAllowedCategories;
                    scope.allowedcategories = [];
                    angular.forEach(data.allowedcategories, function(c) {
                      scope.allowedcategories.push(c.id);
                    });
                  });
            }
          };
          scope.updateCategoriesAllowed();

          scope.$watch('groupOwner', function(newvalue, oldvalue) {
            scope.updateCategoriesAllowed();
          });

          var init = function() {
            return $http.get('../api/tags', {cache: true}).
                success(function(data) {
                  var lang = scope.lang;
                  scope.categories = data;
                  angular.forEach(scope.categories, function(c) {
                    if (angular.isDefined(scope.currentCategories) &&
                    scope.currentCategories.values.indexOf(c.name) !== -1) {
                          scope.ids.push(c.id);
                          initialCategories.push(c);
                    }
                    c.langlabel = $filter('gnLocalized')(c.label, lang);
                  });

                  if (scope.mode === 'autocomplete') {
                    initTagInput();
                  }
                });
          };

          function initTagInput() {
            $timeout(function() {
              try {
                var maxNumberOfItems = 1000;

                // Init tag input
                $(tid).tagsinput({
                  itemValue: 'name',
                  itemText: 'langlabel',
                  maxTags: maxNumberOfItems
                });
                var input = $(tid).tagsinput('input');

                // Init data source
                var source = new Bloodhound({
                  datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
                  queryTokenizer: Bloodhound.tokenizers.whitespace,
                  local: scope.categories,
                  limit: maxNumberOfItems
                });
                source.initialize();

                function allOrSearchFn(q, sync) {
                  if (q === '') {
                    sync(source.all());
                    // This is the only change needed to get 'ALL'
                    // items as the defaults
                  } else {
                    source.search(q, sync);
                  }
                }

                // Init autocomplete
                $(input).typeahead({
                  minLength: 0,
                  highlight: true
                }, {
                  name: 'category',
                  source: allOrSearchFn,
                  displayKey: 'langlabel',
                  limit: Infinity
                }).bind('typeahead:selected',
                    $.proxy(function(obj, c) {
                      // Add to tags
                      this.tagsinput('add', c);

                      scope.assign(c);

                      // Clear typeahead
                      this.tagsinput('input').typeahead('val', '');
                    }, $(tid))
                );

                $(tid).on('itemRemoved', function(e) {
                  scope.assign(e.item);
                });

                angular.forEach(initialCategories, function(c) {
                  $(tid).tagsinput('add', c);
                });
              } catch (e) {
                console.warn('No tagsinput for ' + tid +
                    ', error: ' + e.message);
              }
            });
          };

          scope.sortByLabel = function(c) {
            return c.label[scope.lang];
          };

          // Remove or add category to the set of ids
          scope.assign = function(c, event) {
            if (event) {
              event.stopPropagation();
            }
            var existIndex = scope.ids.indexOf(c.id), method = '';
            if (existIndex === -1) {
              method = 'put';
            } else {
              method = 'delete';
            }
            $http[method]('../api/records/' +
                          scope.metadataUuid + '/tags?id=' + c.id)
                .then(function() {
                  if (existIndex === -1) {
                    scope.ids.push(c.id);
                    scope.currentCategories.values.push(c.name);
                  } else {
                    scope.ids.splice(existIndex, 1);

                    angular.forEach(scope.currentCategories.values,
                    function(cat, idx) {
                      if (cat === c.name) {
                        scope.currentCategories.values.splice(idx, 1);
                      }
                    });
                  }
                }, function(response) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('assignCategoryError',
                        {category: c.name}),
                    error: response.error,
                    timeout: 0,
                    type: 'danger'});
                });
            return false;
          };
          init();
        }
      };
    }]);
  /**
   * @ngdoc directive
   * @name gn_mdactions_directive.directive:gnMetadataGroupUpdater
   * @restrict A
   * @requires gnMetadataGroupUpdater
   *
   * @description
   * The `gnMetadataGroupUpdater` directive provides a
   * dropdown button which allows to update the metadata
   * group.
   */
  module.directive('gnMetadataGroupUpdater', [
    'gnMetadataActions', '$translate', '$http', '$rootScope',
    function(gnMetadataActions, $translate, $http, $rootScope) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'metadatagroupupdater.html',
        scope: {
          groupOwner: '=gnMetadataGroupUpdater',
          metadataId: '='
        },
        link: function(scope) {
          scope.lang = scope.$parent.lang;
          scope.groups = null;

          scope.init = function(event) {
            return $http.get('../api/groups?profile=Editor', {cache: true}).
                success(function(groups) {
                  scope.groups = groups;
                });
          };

          scope.sortByLabel = function(group) {
            return group.label[scope.lang];
          };

          scope.assignGroup = function(g, event) {
            event.stopPropagation();
            gnMetadataActions.assignGroup(scope.metadataId, g.id)
                .then(function() {
                  scope.groupOwner = g.id;
                }, function(error) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('changeCategoryError'),
                    error: error,
                    timeout: 0,
                    type: 'danger'});
                });
            return false;
          };

        }
      };
    }]);

  module.directive('gnPermalinkInput', [
    function() {
      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'permalinkinput.html',
        link: function(scope, element, attrs) {
          scope.url = attrs['gnPermalinkInput'];
          scope.copied = false;
          setTimeout(function() {
            element.find(':input').select();
          }, 300);
        }
      };
    }]
  );


  /**
   * @ngdoc directive
   * @name gn_mdactions_directive.directive:gnTransferOwnership
   * @restrict A
   * @requires gnHttp
   *
   * @description
   * The `gnTransferOwnership` directive provides a
   * dropdown button which allows can be added to a metadata actions
   * menu or to a selection menu.  If integrated into a metadata actions
   * menu then the metadata id and the owner of the metadata to be updated
   * must be provided.
   *
   * The metadata id should be the value of the gn-transfer-ownership attribute
   * and the metadata owner id should be the value of the
   * gn-transfer-md-owner attribute
   */
  module.directive('gnTransferOwnership', [
    '$translate', '$http', 'gnHttp', '$rootScope',
    function($translate, $http, gnHttp, $rootScope) {
      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'transferownership.html',
        link: function(scope, element, attrs) {
          var ownerId = parseInt(attrs['gnTransferMdOwner']);
          var groupOwner = parseInt(attrs['gnTransferMdGroupOwner']);
          var bucket = attrs['selectionBucket'];
          var mdUuid = attrs['gnTransferOwnership'];
          scope.selectedUserGroup = null;

          scope.selectUser = function(user) {
            scope.selectedUser = user;
            scope.editorSelectedId = user.id;
            $http.get('../api/users/' + id + '/groups')
                .success(function(data) {
                  var uniqueGroup = {};
                  angular.forEach(data, function(g) {
                    if (!uniqueGroup[g.group.id]) {
                      uniqueGroup[g.group.id] = g.group;
                    }
                  });
                  $scope.editorGroups = uniqueGroup;
                });
          };

          scope.selectGroup = function(group) {
            scope.selectedGroup = group;
          };
          $http.get('../api/users/groups')
              .success(function(data) {
                var uniqueUserGroups = {};
                angular.forEach(data, function(g) {
                  var key = g.groupId + '-' + g.userId;
                  if (!uniqueUserGroups[key]) {
                    uniqueUserGroups[key] = g;
                    uniqueUserGroups[key].groupNameTranslated = g.groupName === 'allAdmins' ?
                        $translate.instant(g.groupName) :
                        $translate.instant('group-' + g.groupId);
                  }
                });
                scope.userGroups = uniqueUserGroups;
                if(scope.userGroups && Object.keys(scope.userGroups).length>0) {
                  scope.userGroupDefined = true;
                } else {
                  scope.userGroupDefined = false;
                }
              });

          scope.save = function() {
            var url = '../api/records/';
            if (bucket != 'null') {
              url += 'ownership?bucket=' + bucket + '&';
            } else {
              url += mdUuid + '/ownership?';
            }
            return $http.put(url +
                'userIdentifier=' + scope.selectedUserGroup.userId +
                '&groupIdentifier=' + scope.selectedUserGroup.groupId)
                .then(function(r) {
                  $rootScope.$broadcast('StatusUpdated', {
                    msg: $translate.instant('transfertPrivilegesFinished', {
                      metadata: r.data.numberOfRecordsProcessed
                    }),
                    timeout: 2,
                    type: 'success'});
                });
          };
        }
      };
    }]
  );

})();
