(function() {
  goog.provide('gn_usergroup_controller');

  var module = angular.module('gn_usergroup_controller',
      []);


  /**
   *
   */
  module.controller('GnUserGroupController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile) {
      var templateFolder = 'templates/admin/usergroup/';
      var availableTemplates = [
        'users', 'groups'
      ];

      // TODO : should provide paging
      $scope.maxHitsForPreview = 50;

      $scope.groups = null;
      $scope.groupRecords = null;
      $scope.groupUpdated = false;
      $scope.groupSearch = {};
      $scope.groupSelected = {id: $routeParams.groupId};


      $scope.users = null;
      $scope.userOperation = 'editinfo';
      $scope.userIsAdmin = false;
      $scope.userUpdated = false;
      $scope.userSelected = {};
      $scope.userGroups = {};

      function loadGroups() {
        $http.get($scope.url + 'xml.group.list@json').success(function(data) {
          $scope.groups = data;
        }).error(function(data) {
          // TODO
        });
      }
      function loadUsers() {
        $http.get($scope.url + 'xml.user.list@json').success(function(data) {
          $scope.users = data;
        }).error(function(data) {
          // TODO
        });
      }

      $scope.defaultUserGroupTab = 'groups';

      $scope.getTemplate = function() {
        $scope.type = $scope.defaultUserGroupTab;
        if (availableTemplates.indexOf($routeParams.userGroupTab) > -1) {
          $scope.type = $routeParams.userGroupTab;
        }
        return templateFolder + $scope.type + '.html';
      };



      /**
       * Add an new user
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
          organisation: ''
        };
      };

      $scope.unselectUser = function() {
        $scope.userSelected = null;
        $scope.userGroups = null;
        $scope.userUpdated = false;
        $scope.userRecords = null;
        $scope.userOperation = 'editinfo';
      };

      $scope.selectUser = function(u) {
        // Load user group and then select user
        $http.get($scope.url + 'xml.usergroups.list@json?id=' + u.id)
                .success(function(data) {
              $scope.userGroups = data;
              $scope.userSelected = u;
              $scope.userIsAdmin = u.profile === 'Administrator';
            }).error(function(data) {
              // TODO
            });
        // Retrieve records in that group
        $http.get($scope.url + 'q@json?fast=index&template=y or n&_owner=' +
                u.id + '&sortBy=title&from=1&to=' + $scope.maxHitsForPreview)
                .success(function(data) {
              $scope.userRecords = data.metadata;
            }).error(function(data) {
              // TODO
            });
        $scope.userUpdated = false;
      };

      $scope.isUserGroup = function(groupId, profile) {
        for (var i = 0; i < $scope.userGroups.length; i++) {
          if ($scope.userGroups[i].id == groupId &&
              $scope.userGroups[i].profile == profile) {
            return true;
          }
        }
        return false;
      };

      $scope.setUserProfile = function(isAdmin) {
        $scope.userUpdated = true;
        if (isAdmin) {
          // Unselect all groups option
          for (var i = 0; i < $scope.profiles.length; i++) {
            if ($scope.profiles[i] !== 'Administrator') {
              $('#groups_' + $scope.profiles[i])[0].selectedIndex = -1;
            }
          }
          $scope.userSelected.profile =
              $scope.userIsAdmin ? 'Administrator' : $scope.profiles[0];
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


      $scope.saveUser = function(formId) {
        $http.get($scope.url + 'admin.user.update?' + $(formId).serialize())
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

      $scope.deleteUser = function(formId) {
        $http.get($scope.url + 'admin.user.remove?id=' +
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
      };

      $scope.saveGroup = function(formId) {
        $http.get($scope.url + 'admin.group.update?' + $(formId).serialize())
        .success(function(data) {
              $scope.unselectGroup();
              loadGroups();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('groupUpdated'),
                timeout: 2,
                type: 'success'});
            })
        .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('groupUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.deleteGroup = function(formId) {
        $http.get($scope.url + 'admin.group.remove?id=' +
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

      /**
       * Save a translation
       */
      $scope.saveTranslation = function(e) {
        // TODO: No need to save if translation not updated

        // Save value in translation
        // TODO : could we use Angular compile here ?
        var xml = "<request><group id='{{id}}'>" +
            '<label>' +
            '<{{key}}>{{value}}</{{key}}>' +
                        '</label>' +
            '</group></request>';
        xml = xml.replace('{{id}}', $scope.groupSelected.id)
                    .replace(/{{key}}/g, e.key)
                    .replace('{{value}}', e.value);
        $http.post($scope.url + 'xml.group.update', xml, {
          headers: {'Content-type': 'application/xml'}
        }).success(function(data) {
        }).error(function(data) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('groupTranslationUpdateError'),
            error: data,
            timeout: 0,
            type: 'danger'});
        });
      };
      /**
       * Return true if selected group has metadata record.
       * This information could be useful to disable a delete button
       * for example.
       */
      $scope.groupHasRecords = function() {
        return $scope.groupRecords && $scope.groupRecords.length > 0;
      };

      $scope.unselectGroup = function() {
        $scope.groupSelected = null;
        $scope.groupUpdated = false;
        $scope.groupRecords = null;
      };

      $scope.selectGroup = function(g) {
        $scope.groupSelected = g;
        // Retrieve records in that group
        $http.get($scope.url + 'q@json?fast=index&template=y or n&group=' +
                g.id + '&sortBy=title&from=1&to=' + $scope.maxHitsForPreview)
                .success(function(data) {
              $scope.groupRecords = data.metadata;
            }).error(function(data) {
              // TODO
            });
        $scope.groupUpdated = false;
      };

      $scope.updatingGroup = function() {
        $scope.groupUpdated = true;
      };

      loadGroups();
      loadUsers();
    }]);

})();
