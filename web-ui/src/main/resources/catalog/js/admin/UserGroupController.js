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
  goog.provide('gn_usergroup_controller');

  goog.require('gn_dbtranslation');
  goog.require('gn_multiselect');
  goog.require('gn_mdtypewidget');

  var module = angular.module('gn_usergroup_controller', [
    'gn_dbtranslation',
    'gn_multiselect',
    'gn_mdtypewidget',
    'blueimp.fileupload']);


  /**
   * UserGroupController provides all necessary operations
   * to manage users and groups.
   */
  module.controller('GnUserGroupController', [
    '$scope', '$routeParams', '$http', '$rootScope',
    '$translate', '$timeout',
    function($scope, $routeParams, $http, $rootScope,
        $translate, $timeout) {

      $scope.searchObj = {
        params: {
          template: 'y or n or s or t',
          sortBy: 'title'
        }
      };

      $scope.pageMenu = {
        folder: 'usergroup/',
        defaultTab: 'users',
        tabs:
            [{
              type: 'groups',
              label: 'manageGroups',
              icon: 'fa-group',
              href: '#/organization/groups'
            },{
              type: 'users',
              label: 'manageUsers',
              icon: 'fa-user',
              href: '#/organization/users'
            }]
      };

      // The pagination config
      $scope.groupRecordsPagination = {
        pages: -1,
        currentPage: 0,
        hitsPerPage: 10
      };

      // Scope for group
      // List of catalog groups
      $scope.groups = null;
      $scope.groupSelected = {id: $routeParams.userOrGroup};
      // On going changes group ...
      $scope.groupUpdated = false;
      $scope.groupSearch = {};
      $scope.groupusers = null;

      // Scope for user
      // List of catalog users
      $scope.users = null;
      $scope.userSelected = {};
      // List of group for selected user
      $scope.userGroups = {};
      // Indicate if an update is going on
      $scope.userOperation = 'editinfo';
      $scope.userIsAdmin = null;
      $scope.userIsEnabled = null;
      // On going changes for user ...
      $scope.userUpdated = false;
      $scope.passwordCheck = '';

      $scope.isLoadingUsers = false;
      $scope.isLoadingGroups = false;


      // This is to force IE11 NOT to cache json requests
      if (!$http.defaults.headers.get) {
        $http.defaults.headers.get = {};
      }
      $http.defaults.headers.get['Cache-Control'] = 'no-cache';
      $http.defaults.headers.get['Pragma'] = 'no-cache';

      $http.get('../api/tags').
          success(function(data) {
            var nullTag = {id: null, name: '', label: {}};
            nullTag.label[$scope.lang] = '';
            $scope.categories = [nullTag].concat(data);
          });

      function loadGroups() {
        $scope.isLoadingGroups = true;
        // If not send profile, all groups are returned
        var profile = ($scope.user.profile) ?
            '?profile=' + $scope.user.profile : '';


        $http.get('../api/groups' + profile).
            success(function(data) {
              $scope.groups = data;
              angular.forEach($scope.groups, function(u) {
                u.langlabel = getLabel(u);
              });
              $scope.isLoadingGroups = false;
            }).error(function(data) {
              // TODO
              $scope.isLoadingGroups = false;
            }).then(function() {
              // Search if requested group in location is
              // in the list and trigger selection.
              // TODO: change route path when selected (issue - controller is
              // reloaded)
              if ($routeParams.userOrGroup) {
                angular.forEach($scope.groups, function(u) {
                  if (u.name === $routeParams.userOrGroup ||
                      $routeParams.userOrGroup === u.id.toString()) {
                    $scope.selectGroup(u);
                  }
                });
              }
            });
      }

      function loadUsers() {
        $scope.isLoadingUsers = true;
        $http.get('../api/users').success(function(data) {
          $scope.users = data;
          $scope.isLoadingUsers = false;
        }).error(function(data) {
          // TODO
          $scope.isLoadingUsers = false;
        }).then(function() {
          // Search if requested user in location is
          // in the list and trigger user selection.
          if ($routeParams.userOrGroup) {
            angular.forEach($scope.users, function(u) {

              if (u.username === $routeParams.userOrGroup ||
                  $routeParams.userOrGroup === u.id.toString()) {
                $scope.selectUser(u);
              }
            });
          }
        });
      }

      /**
       * Loads the users for a group.
       *
       * @param groupId
       */
      function loadGroupUsers(groupId) {
        $http.get('../api/groups/' + groupId + '/users').
        success(function(data) {
          $scope.groupusers = data;
        }).error(function(data) {
          $scope.groupusers = [];
        });
      }

      /**
       * Add an new user based on the default
       * user config.
       */
      $scope.addUser = function() {
        $scope.unselectUser();
        $scope.userOperation = 'newuser';
        $scope.userSelected = {
          id: '',
          username: '',
          password: '',
          name: '',
          surname: '',
          profile: 'RegisteredUser',
          addresses: [
            {
              address: '',
              city: '',
              state: '',
              zip: '',
              country: ''
            }
          ],
          emailAddresses: [
            ''
          ],
          organisation: '',
          enabled: true
        };

        $scope.userGroups = null;
        $scope.userIsAdmin = false;
        $scope.userIsEnabled = true;

        updateGroupsByProfile($scope.userGroups);

        $timeout(function() {
          $scope.setUserProfile();
          $('#username').focus();
        }, 100);
      };

      /**
       * Remove current user from selection and
       * clear user groups and records.
       */
      $scope.unselectUser = function() {
        $scope.userSelected = null;
        $scope.userGroups = null;
        $scope.userUpdated = false;
        $scope.$broadcast('clearResults');
        $scope.userOperation = 'editinfo';
      };

      /**
       * Select a user and retrieve its groups and
       * metadata records.
       */
      $scope.selectUser = function(u) {
        $scope.userOperation = 'editinfo';
        $scope.userIsAdmin = false;
        $scope.userIsEnabled = true;
        $scope.userSelected = null;
        $scope.userGroups = null;

        $http.get('../api/users/' + u.id)
            .success(function(data) {
              $scope.userSelected = data;
              $scope.userIsAdmin =
                  (data.profile === 'Administrator');

              $scope.userIsEnabled = data.enabled;

              // Load user group and then select user
              $http.get('../api/users/' + u.id + '/groups')
              .success(function(groups) {
                    $scope.userGroups = groups;
                  }).error(function(data) {
                    // TODO
                  });
            }).error(function(data) {
              // TODO
            });



        // Retrieve records in that group
        $scope.$broadcast('resetSearch', {
          template: 'y or n or s or t',
          _owner: u.id,
          sortBy: 'title'
        });

        $scope.userUpdated = false;

        $timeout(function() {
          $('#username').focus();
        }, 100);
      };


      $scope.resetPassword1 = null;
      $scope.resetPassword2 = null;
      $scope.resetPassword = function() {
        $scope.resetPassword1 = null;
        $scope.resetPassword2 = null;
        $('#passwordResetModal').modal();
      };

      $scope.saveNewPassword = function() {
        var params = {
          password: $scope.resetPassword1,
          password2: $scope.resetPassword2
        };

        $http.post('../api/users/' + $scope.userSelected.id +
            '/actions/forget-password',
            $.param(params),
            {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            })
            .success(function(data) {
              $scope.resetPassword1 = null;
              $scope.resetPassword2 = null;
              $('#passwordResetModal').modal('hide');
            }).error(function(data) {
              alert('Error occurred while resetting password: ' +
                  data.error.message);
            });

      };

      /**
       * Check if the groupId is in the user groups
       * list with that profile.
       *
       * Note: A user can have more than one profile
       * for a group.
       */
      $scope.isUserGroup = function(groupId, profile) {
        if ($scope.userGroups) {
          for (var i = 0; i < $scope.userGroups.length; i++) {
            if ($scope.userGroups[i].id.groupId == groupId &&
                $scope.userGroups[i].id.profile == profile) {
              return true;
            }
          }
        }
        return false;
      };

      $scope.groupsByProfile = [];

      /**
       * Returns the list of groups inside "groups" with the selected profile
       */
      var getLabel = function(g) {
        return g.label[$scope.lang] || g.name;
      };

      var profiles = ['Administrator',
        'UserAdmin', 'Reviewer',
        'Editor', 'RegisteredUser',
        'Guest'];

      var updateGroupsByProfile = function(groups) {
        var res = [];
        angular.forEach(profiles, function(profile) {
          res[profile] = [];
          if (groups != null) {
            for (var i = 0; i < groups.length; i++) {
              if (groups[i].id.profile == profile) {
                var g = groups[i].group;
                g.langlabel = getLabel(g);
                res[profile].push(g);
              }
            }
          }
        });

        //We need to change the pointer,
        // not only the value, so ng-options is aware
        $scope.groupsByProfile = res;
      };

      $scope.$watch('userGroups', function(groups) {
        updateGroupsByProfile(groups);
      });

      $scope.sortByLabel = function(group) {
        return group.label[$scope.lang];
      };

      /**
       * Compute user profile based on group/profile select
       * list. This is closely related to the template manipulating
       * element ids.
       *
       * Searching through the list, compute the highest profile
       * for the user and set it.
       * When a user is a reviewer of a group, the corresponding
       * group is also selected in the editor profile list.
       */
      $scope.setUserProfile = function(checked) {
        if (!$scope.userSelected) {
          return;
        }
        // Switch the profile (AFA a watch on userIsAdmin does not work).
        if (checked) {
          $scope.userIsAdmin = !$scope.userIsAdmin;
          angular.forEach(profiles, function(p) {
            $scope.groupsByProfile[p] = [];
          });
        }
        $scope.userUpdated = true;
        if ($scope.userIsAdmin) {
          $scope.userSelected.profile = 'Administrator';
        } else {
          // Define the highest profile for user
          var newprofile = 'RegisteredUser';
          for (var i = 0; i < $scope.profiles.length; i++) {
            var p = $scope.profiles[i];
            if (p !== 'Administrator') {
              // If one of the group is selected, main user profile is updated
              if ($scope.groupsByProfile[p].length > 0) {
                newprofile = $scope.profiles[i];
              }
            }
          }
          $scope.userSelected.profile = newprofile;
        }
      };

      $scope.updatingUser = function() {
        $scope.userUpdated = true;
      };


      function updateProfileRules() {
        $scope.setUserProfile();
      };

      $scope.$watchCollection('groupsByProfile.RegisteredUser',
          updateProfileRules);
      $scope.$watchCollection('groupsByProfile.Editor', updateProfileRules);
      $scope.$watchCollection('groupsByProfile.UserAdmin', updateProfileRules);
      $scope.$watchCollection('groupsByProfile.Reviewer', function(n, o) {
        if (n !== o) {
          for (var j = 0; j < n.length; j++) {
            var g = n[j];
            var gIsAlsoForEditorProfile = false;
            for (var i = 0; i < $scope.groupsByProfile['Editor'].length; i++) {
              var eg = $scope.groupsByProfile['Editor'][i];
              if (eg.id === g.id) {
                gIsAlsoForEditorProfile = true;
                break;
              }
            }
            if (!gIsAlsoForEditorProfile) {
              $scope.groupsByProfile['Editor'].push(g);
            }
          }
          $scope.setUserProfile();
        }
      });
      /**
       * Save a user.
       */
      $scope.saveUser = function(formId) {

        var selectedRegisteredUserGroups = [],
            selectedEditorGroups = [],
            selectedReviewerGroups = [],
            selectedUserAdminGroups = [];

        for (var j = 0;
             j < $scope.groupsByProfile['RegisteredUser'].length; j++) {
          if ($scope.groupsByProfile['RegisteredUser'][j]) {
            selectedRegisteredUserGroups.push(
                $scope.groupsByProfile['RegisteredUser'][j].id);
          }
        }
        for (var j = 0; j < $scope.groupsByProfile['Editor'].length; j++) {
          if ($scope.groupsByProfile['Editor'][j]) {
            selectedEditorGroups.push(
                $scope.groupsByProfile['Editor'][j].id);
          }
        }
        for (var j = 0; j < $scope.groupsByProfile['Reviewer'].length; j++) {
          if ($scope.groupsByProfile['Reviewer'][j]) {
            selectedReviewerGroups.push(
                $scope.groupsByProfile['Reviewer'][j].id);
          }
        }
        for (var j = 0; j < $scope.groupsByProfile['UserAdmin'].length; j++) {
          if ($scope.groupsByProfile['UserAdmin'][j]) {
            selectedUserAdminGroups.push(
                $scope.groupsByProfile['UserAdmin'][j].id);
          }
        }

        var data = angular.extend({}, $scope.userSelected, {
          groupsRegisteredUser: selectedRegisteredUserGroups,
          groupsEditor: selectedEditorGroups,
          groupsReviewer: selectedReviewerGroups,
          groupsUserAdmin: selectedUserAdminGroups
        });

        data.enabled = $scope.userIsEnabled;

        delete data.lastLoginDate;
        delete data.security;

        var url = '';

        if ($scope.userSelected.id) {
          url = '../api/users/' + $scope.userSelected.id;
        } else {
          url = '../api/users';
        }

        $http.put(url,
            data)
            .then(
            function(r) {
              $scope.unselectUser();
              loadUsers();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('userUpdated'),
                timeout: 2,
                type: 'success'});
            },
            function(r) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('userUpdateError'),
                error: r.data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
       * Delete a user.
       */
      $scope.deleteUser = function(formId) {
        $http.delete('../api/users/' +
            $scope.userSelected.id)
            .success(function(data) {
              $scope.unselectUser();
              loadUsers();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('userDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };









      $scope.addGroup = function() {
        $scope.unselectGroup();
        // reset logo upload control
        $scope.clear($scope.queue);
        $scope.groupSelected = {
          id: -99,
          name: '',
          label: {},
          description: '',
          email: '',
          enableAllowedCategories: false,
          allowedCategories: [],
          defaultCategory: null,
          logo: null,
          referrer: null,
          website: null

        };
        $timeout(function() {
          $('#groupname').focus();
        }, 100);
      };


      var uploadGroupLogoDone = function(e, data) {
        $scope.groupSelected.logo= data.files[0].name;
        $scope.clear(data.files[0]);
        createOrModifyGroup();
      };
      var uploadGroupLogoError = function(event, data) {
        var req = data.response().jqXHR;
        var contentType = req.getResponseHeader('Content-Type');
        var errorText = req.responseText;
        var errorCode = null;
        if ('application/json' === contentType) {
          var parsedError = JSON.parse(req.responseText);
        }
        $rootScope.$broadcast('StatusUpdated', {
          title: $translate.instant('groupUpdateError'),
          error: parsedError || errorText,
          timeout: 0,
          type: 'danger'});
      };

      var createOrModifyGroupSuccess = function() {
        $scope.unselectGroup();
        loadGroups();
        $rootScope.$broadcast('StatusUpdated', {
          msg: $translate.instant('groupUpdated'),
          timeout: 2,
          type: 'success'});
      };

      var createOrModifyGroupError = function(data) {
        $rootScope.$broadcast('StatusUpdated', {
          title: $translate.instant('groupUpdateError'),
          error: data,
          timeout: 0,
          type: 'danger'});
      };

      var createOrModifyGroup = function() {
        if (($scope.groupSelected.defaultCategory) &&
            ($scope.groupSelected.defaultCategory.id == null)) {
          $scope.groupSelected.defaultCategory = null;
        }
        $http.put('../api/groups' + (
          $scope.groupSelected.id != -99 ?
            '/' + $scope.groupSelected.id : ''
        ), $scope.groupSelected)
          .success(createOrModifyGroupSuccess)
          .error(createOrModifyGroupError);
      };



      $scope.deleteGroupLogo = function() {
        $scope.groupSelected.logo = null;
        $scope.updatingGroup();
      };

      // upload directive options
      $scope.groupLogoUploadOptions = {
        autoUpload: false,
        url: "../api/logos?_csrf=" + $scope.csrf,
        dataType: "text",
        maxNumberOfFiles: 1,
        done: uploadGroupLogoDone,
        fail: uploadGroupLogoError
      };

      $scope.$on('fileuploadchange', function(e, data) {
        // limit fileupload to only one file.
        angular.forEach($scope.queue, function(item) {
          $scope.clear(item);
        });
      });

      $scope.saveGroup = function() {
        if($scope.queue.length > 0) {
          $scope.submit();
        } else {
          createOrModifyGroup();
        }
      };

      $scope.deleteGroup = function(formId) {
        $http.delete('../api/groups/' +
                $scope.groupSelected.id + '?force=true')
            .success(function(data) {
              $scope.unselectGroup();
              loadGroups();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('groupDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.unselectGroup = function() {
        $scope.groupSelected = null;
        $scope.groupUpdated = false;
        $scope.$broadcast('clearResults');
      };

      $scope.selectGroup = function(g) {
        // groups list is shared between users and groups management
        // for users management the groups get a langlabel property
        // that breaks the group management.
        // TODO: Use custom controllers for groups and users management
        $scope.groupSelected = angular.copy(g);
        $scope.clear($scope.queue);
        delete $scope.groupSelected.langlabel;

        // Retrieve records in that group
        $scope.$broadcast('resetSearch', {
          template: 'y or n or s or t',
          group: g.id,
          sortBy: 'title'
        });

        loadGroupUsers($scope.groupSelected.id);

        $scope.groupUpdated = false;

        $timeout(function() {
          $('#groupname').focus();
        });
      };

      $scope.updatingGroup = function() {
        $scope.groupUpdated = true;
      };

      var userAndGroupInitialized = false;
      var unregister = $scope.$watch('user', function(n, o) {
        if (!userAndGroupInitialized && n && n.profile) {
          userAndGroupInitialized = true;
          unregister();
          loadGroups();
          loadUsers();
        }
      }, true);
    }]);

  module.filter('loggedUserIsUseradminOrMore', function() {
    var searchGroup = function(g, userAdminGroups) {
      var found = false;
      for (var i = 0; i < userAdminGroups.length && !found; i++) {
        found = userAdminGroups[i]['@id'] == g.id;
      }
      return found;
    };

    return function(groups, userAdminGroups, isAdmin) {
      var filtered = [];
      angular.forEach(groups, function(g) {
        if (isAdmin || searchGroup(g, userAdminGroups)) {
          filtered.push(g);
        }
      });

      return filtered;
    };
  });

})();

