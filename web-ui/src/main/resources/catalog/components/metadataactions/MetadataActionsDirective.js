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
            return $http.get('md.status.update?' +
                '_content_type=json&id=' + metadataId +
                '&changeMessage=' + scope.changeMessage +
                '&status=' + scope.newStatus.value).then(
                function(data) {
                  gnMetadataManager.updateMdObj(scope.md);
                  scope.$emit('metadataStatusUpdated', true);
                  scope.$emit('StatusUpdated', {
                    msg: $translate('metadataStatusUpdatedWithNoErrors'),
                    timeout: 2,
                    type: 'success'});
                }, function(data) {
                  scope.$emit('metadataStatusUpdated', false);
                  scope.$emit('StatusUpdated', {
                    title: $translate('metadataStatusUpdatedErrors'),
                    error: data,
                    timeout: 0,
                    type: 'danger'});
                });
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
   */
  module.directive('gnMetadataCategoryUpdater', [
    'gnMetadataActions', '$translate', '$http', '$rootScope',
    function(gnMetadataActions, $translate, $http, $rootScope) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'metadatacategoryupdater.html',
        scope: {
          currentCategories: '=gnMetadataCategoryUpdater',
          metadataId: '='
        },
        link: function(scope) {
          scope.lang = scope.$parent.lang;
          scope.categories = null;
          scope.ids = [];

          var init = function() {
            return $http.get('info?type=categories&' +
                '_content_type=json', {cache: true}).
                success(function(data) {
                  scope.categories =
                     data !== 'null' ? data.metadatacategory : null;
                  if (angular.isDefined(scope.currentCategories)) {
                    angular.forEach(scope.categories, function(c) {
                      if (scope.currentCategories.indexOf(c.name) !== -1) {
                        scope.ids.push(c['@id']);
                      }
                    });
                  }
                });
          };

          scope.sortByLabel = function(c) {
            return c.label[scope.lang];
          };

          // Remove or add category to the set of ids
          scope.assign = function(c, event) {
            event.stopPropagation();
            var existIndex = scope.ids.indexOf(c['@id']);
            if (existIndex === -1) {
              scope.ids.push(c['@id']);
            } else {
              scope.ids.splice(existIndex, 1);
            }
            gnMetadataActions.assignCategories(scope.metadataId, scope.ids)
              .then(function() {
                  scope.currentCategories.push(c.name);
                }, function(response) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate('assignCategoryError',
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
            return $http.get('info?_content_type=json&' +
                'type=groups&profile=Editor', {cache: true}).
                success(function(data) {
                  scope.groups = data !== 'null' ? data.group : null;
                });
          };

          scope.sortByLabel = function(group) {
            return group.label[scope.lang];
          };

          scope.assignGroup = function(g, event) {
            event.stopPropagation();
            gnMetadataActions.assignGroup(scope.metadataId, g['@id'])
              .then(function() {
                  scope.groupOwner = g['@id'];
                }, function(error) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate('changeCategoryError'),
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
          var mdUuid = attrs['gnTransferOwnership'];
          scope.users = [];
          scope.selectedUser = null;
          scope.groups = [];
          scope.selectedGroup = null;
          scope.userLoading = true;
          scope.groupLoading = false;

          scope.selectUser = function(user) {
            scope.selectedUser = user;
            scope.groupLoading = true;
            $http.get('admin.usergroups.list?_content_type=json&id=' +
                user.id).success(function(groups) {
              scope.groups = [];
              var added = {};
              angular.forEach(groups, function(g) {
                if (!angular.isDefined(added[g.id]) && g.reserved !== 'true') {
                  added[g.id] = true;
                  scope.groups.push({
                    name: g.name,
                    id: g.id,
                    desc: g.description
                  });
                }
              });
            }).error(function(error) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('loadUserGroupsError'),
                timeout: 0,
                type: 'danger'});
            }).then(function() {
              scope.groupLoading = false;
            });
          };

          scope.selectGroup = function(group) {
            scope.selectedGroup = group;
          };
          $http.get('admin.user.list?_content_type=json').
              success(function(data) {
                var isEditor = function(user) {
                  var hasEditorAuth = false;
                  var auths = user.authorities;
                  if (!angular.isArray(auths)) {
                    auths = [auths];
                  }
                  angular.forEach(auths, function(auth) {
                    if (!hasEditorAuth && auth.authority === 'Editor') {
                      hasEditorAuth = true;
                    }
                  });

                  return hasEditorAuth;
                };
                angular.forEach(data.users, function(user) {
                  user = user.value;
                  if (isEditor(user)) {
                    var userObj = {
                      name: user.name + ' ' + user.surname,
                      username: user.username,
                      id: user.id
                    };
                    scope.users.push(userObj);

                    if (user.id === ownerId) {
                      scope.selectUser(userObj);
                    }
                  }
                });
              }).error(function(error) {
                $rootScope.$broadcast('StatusUpdated', {
                  msg: $translate('loadUsersError'),
                  timeout: 0,
                  type: 'danger'});
              }).then(function() {
                scope.userLoading = false;
              });

          var updateSelection = function() {
            return $http({
              method: 'GET',
              url: 'metadata.batch.newowner?userId=' + scope.selectedUser.id +
                  '&groupId=' + scope.selectedGroup.id,
              headers: {
                'Content-Type': 'application/json'
              }
            }).success(function(result) {
              scope.$emit('TransferOwnership', true);
              var msg = $translate('transferOwnershipSuccessMsg', result);
              $rootScope.$broadcast('StatusUpdated', {
                msg: msg,
                timeout: 2,
                type: 'success'});
            }).error(function() {
              scope.$emit('TransferOwnership', false);
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('transferOwnershipError'),
                timeout: 0,
                type: 'danger'});
            });
          };
          scope.save = function() {
            if (scope.selectedUser && scope.selectedGroup) {
              if (angular.isDefined(mdUuid)) {
                return gnHttp.callService('mdSelect', {
                  selected: 'add',
                  id: mdUuid
                }).success(function() {
                  updateSelection();
                });
              } else {
                return updateSelection();
              }
            }
          };
        }
      };
    }]
  );

})();
