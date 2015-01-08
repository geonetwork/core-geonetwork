(function() {
  goog.provide('gn_usergroup_controller');

  goog.require('gn_dbtranslation');

  var module = angular.module('gn_usergroup_controller', [
    'gn_dbtranslation',
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
          template: 'y or n',
          sortBy: 'title'
        }
      };

      $scope.pageMenu = {
        folder: 'usergroup/',
        defaultTab: 'groups',
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

      // Scope for user
      // List of catalog users
      $scope.users = null;
      $scope.userSelected = {};
      // List of group for selected user
      $scope.userGroups = {};
      // Indicate if an update is going on
      $scope.userOperation = 'editinfo';
      $scope.userIsAdmin = null;
      // On going changes for user ...
      $scope.userUpdated = false;
      $scope.passwordCheck = '';

      $scope.isLoadingUsers = false;
      $scope.isLoadingGroups = false;



      function loadGroups() {
        $scope.isLoadingGroups = true;
        $http.get('admin.group.list@json').success(function(data) {
          $scope.groups = data !== 'null' ? data : null;
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
              if (u.name === $routeParams.userOrGroup) {
                $scope.selectGroup(u);
              }
            });
          }
        });
      }
      function loadUsers() {
        $scope.isLoadingUsers = true;
        $http.get('admin.user.list@json').success(function(data) {
          $scope.users = data.users;
          $scope.isLoadingUsers = false;
        }).error(function(data) {
          // TODO
          $scope.isLoadingUsers = false;
        }).then(function() {
          // Search if requested user in location is
          // in the list and trigger user selection.
          if ($routeParams.userOrGroup) {
            angular.forEach($scope.users, function(u) {
              if (u.username === $routeParams.userOrGroup) {
                $scope.selectUser(u);
              }
            });
          }
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
          address: '',
          city: '',
          state: '',
          zip: '',
          country: '',
          email: '',
          organisation: '',
          groups: []
        };
        $scope.userGroups = null;
        $scope.userIsAdmin = false;
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
        $scope.userSelected = null;
        $scope.userGroups = null;

        $http.get('admin.user@json?id=' + u.value.id)
          .success(function(data) {
              $scope.userSelected = data;
              $scope.userIsAdmin =
                  (data.profile === 'Administrator');

              // Load user group and then select user
              $http.get('admin.usergroups.list@json?id=' + u.value.id)
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
          template: 'y or n',
          _owner: u.value.id,
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
        var params = {operation: 'resetpw',
          id: $scope.userSelected.id,
          password: $scope.resetPassword1,
          password2: $scope.resetPassword2
        };

        $http.post('user.update@json', null, {params: params})
              .success(function(data) {
              $scope.resetPassword1 = null;
              $scope.resetPassword2 = null;
              $('#passwordResetModal').modal('hide');
            }).error(function(data) {
              // TODO
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
            if ($scope.userGroups[i].id == groupId &&
                $scope.userGroups[i].profile == profile) {
              return true;
            }
          }
        }
        return false;
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
        // Switch the profile (AFA a watch on userIsAdmin does not work).
        if (checked) {
          $scope.userIsAdmin = !$scope.userIsAdmin;
        }
        $scope.userUpdated = true;
        if ($scope.userIsAdmin) {
          // Unselect all groups option
          for (var i = 0; i < $scope.profiles.length; i++) {
            if ($scope.profiles[i] !== 'Administrator') {
              $('#groups_' + $scope.profiles[i])[0].selectedIndex = -1;
            }
          }
          $scope.userSelected.profile = 'Administrator';
        } else {
          // Define the highest profile for user
          var newprofile = 'RegisteredUser';
          for (var i = 0; i < $scope.profiles.length; i++) {
            if ($scope.profiles[i] !== 'Administrator') {
              var groups = $('#groups_' + $scope.profiles[i])[0];
              // If one of the group is selected, main user profile is updated
              if (groups.selectedIndex > -1 &&
                  groups.options[groups.selectedIndex].value != '') {
                newprofile = $scope.profiles[i];
              }
            }
          }
          $scope.userSelected.profile = newprofile;

          // If user is reviewer in one group, he is also editor for that group
          var editorGroups = $('#groups_Editor')[0];
          var reviewerGroups = $('#groups_Reviewer')[0];
          if (reviewerGroups.selectedIndex > -1) {
            for (var j = 0; j < reviewerGroups.options.length; j++) {
              if (reviewerGroups.options[j].selected) {
                editorGroups.options[j].selected = true;
              }
            }
          }
        }
      };

      $scope.updatingUser = function() {
        $scope.userUpdated = true;
      };


      /**
       * Save a user.
       */
      $scope.saveUser = function(formId) {
        $http.get('admin.user.update?' + $(formId).serialize())
        .success(function(data) {
              $scope.unselectUser();
              loadUsers();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('userUpdated'),
                timeout: 2,
                type: 'success'});
            })
        .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('userUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      /**
       * Delete a user.
       */
      $scope.deleteUser = function(formId) {
        $http.get('admin.user.remove?id=' +
                $scope.userSelected.id)
        .success(function(data) {
              $scope.unselectUser();
              loadUsers();
            })
        .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('userDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };









      $scope.addGroup = function() {
        $scope.unselectGroup();
        $scope.groupSelected = {
          id: '',
          name: '',
          description: '',
          email: ''
        };
        $timeout(function() {
          $('#groupname').focus();
        }, 100);
      };


      var uploadImportMdDone = function() {
        angular.element('#group-logo-upload').scope().queue = [];

        $scope.unselectGroup();
        loadGroups();
        $rootScope.$broadcast('StatusUpdated', {
          msg: $translate('groupUpdated'),
          timeout: 2,
          type: 'success'});

      };
      var uploadImportMdError = function(data) {
        $rootScope.$broadcast('StatusUpdated', {
          title: $translate('groupUpdateError'),
          error: data,
          timeout: 0,
          type: 'danger'});
      };

      // upload directive options
      $scope.mdImportUploadOptions = {
        autoUpload: false,
        done: uploadImportMdDone,
        fail: uploadImportMdError
      };

      $scope.saveGroup = function(formId, logoUploadDivId) {
        var uploadScope = angular.element(logoUploadDivId).scope();
        if (uploadScope.queue.length > 0) {
          uploadScope.submit();
        } else {
          $http.get('admin.group.update?' + $(formId).serialize())
          .success(uploadImportMdDone())
          .error(uploadImportMdError);
        }
      };

      $scope.deleteGroup = function(formId) {
        $http.get('admin.group.remove?id=' +
                $scope.groupSelected.id)
        .success(function(data) {
              $scope.unselectGroup();
              loadGroups();
            })
        .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('groupDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.sortByLabel = function(group) {
        return group.label[$scope.lang];
      };
      $scope.unselectGroup = function() {
        $scope.groupSelected = null;
        $scope.groupUpdated = false;
        $scope.$broadcast('clearResults');
      };

      $scope.selectGroup = function(g) {
        $scope.groupSelected = g;

        // Retrieve records in that group
        $scope.$broadcast('resetSearch', {
          template: 'y or n',
          group: g.id,
          sortBy: 'title'
        });
        $scope.groupUpdated = false;

        $timeout(function() {
          $('#groupname').focus();
        });
      };

      $scope.updatingGroup = function() {
        $scope.groupUpdated = true;
      };

      loadGroups();
      loadUsers();
    }]);

})();
