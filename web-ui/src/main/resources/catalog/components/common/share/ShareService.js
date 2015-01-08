(function() {
  goog.provide('gn_share_service');

  var module = angular.module('gn_share_service', []);

  module.value('gnShareConstants', {
    // Customize column to be displayed and the order
    // TODO: Move config to DB using order in operations table
    columnOrder: ['0', '5', '1', '2', '3'],
    internalOperations: ['2', '3'],
    internalGroups: ['-1', '0', '1'],
    internalGroupsProfiles: ['Administrator', 'Reviewer'],
    // Use topGroups to place those groups with internet, intranet groups
    // on top of the privileges panel.
    // TODO: Move config to DB using isTopGroups in groups table
    // or using reserved column ?
    topGroups: []
  });


  /**
   * @ngdoc service
   * @kind function
   * @name gn_share_service.service:gnShareService
   * @requires gnShareConstants
   * @requires $q
   * @requires $http
   *
   * @description
   * The `gnShareService` service provides all tools required to manage
   * privileges on metadata records. Privileges define which type operation
   * (eg. view, download, edit) a group of user can do.
   */
  module.factory('gnShareService', [
    '$q', '$http', 'gnShareConstants',
    function($q, $http, gnShareConstants) {
      var isAdminOrReviewer = function(userProfile, groupOwner, privileges) {
        if ($.inArray(userProfile,
            gnShareConstants.internalGroupsProfiles) === -1) {
          return false;
        } else if (userProfile === 'Administrator') {
          return true;
        } else {
          var ownerGroupInfo = $.grep(privileges, function(g) {
            return g.id === groupOwner;
          });
          var profiles = ownerGroupInfo[0].userProfile;
          // Check profile for the group where the metadata was created
          if (angular.isArray(profiles)) {
            return $.inArray('Reviewer', profiles) !== -1;
          } else {
            return profiles === 'Reviewer';
          }
        }
      };

      return {
        /**
         * @ngdoc method
         * @methodOf gn_share_service.service:gnShareService
         * @name gnShareService#isAdminOrReviewer
         *
         * @description
         * Check if current user is Administrator or Reviewer
         * of the group the metadata was created in.
         *
         * @param {string} The user profile
         * @param {string} The group the metadata was created in
         * (ie. groupOwner)
         * @param {Object} The privileges definition for a record.
         * The object contains the list of operations allowed
         * and the user profile for each group.
         *
         * @return {boolean} true if Administrator or Reviewer
         */
        isAdminOrReviewer: isAdminOrReviewer,

        /**
         * @ngdoc method
         * @methodOf gn_share_service.service:gnShareService
         * @name gnShareService#loadPrivileges
         *
         * @description
         * Load privileges for a metadata record
         *
         * @param {string} The metadata identifier
         * @param {string} The user profile
         *
         * @return {HttpPromise} Future object which return an Object
         * containing groups, operations and isAdminOrReviewer properties.
         */
        loadPrivileges: function(metadataId, userProfile) {
          var defer = $q.defer();
          var url = angular.isDefined(metadataId) ?
              'md.privileges?_content_type=json&id=' + metadataId :
              'md.privileges.batch?_content_type=json';

          $http.get(url)
            .success(function(data) {
                var groups = data !== 'null' ? data.group : null;
                if (data == null) {
                  return;
                }

                // Promote custom topgroups
                angular.forEach(groups, function(g) {
                  if ($.inArray(g.id, gnShareConstants.topGroups) !== -1) {
                    g.reserved = 'true';
                  }
                  g.isCheckedAll = false;
                  // Format privileges information
                  // Could be an object with a on property:
                  //   "oper":       [
                  //   {
                  //     "id": "0",
                  //     "on": []
                  //   },
                  //
                  //   or a table of string and one dimenstion array
                  //   "oper":       [
                  //   "0",
                  //   ["1"],
                  //   ....
                  g.privileges = {};
                  angular.forEach(g.oper, function(o) {
                    var key, value = false, disabled = false;
                    if (angular.isObject(o) && !angular.isArray(o)) {
                      key = o.id;
                      value = o.on !== undefined;
                    } else {
                      key = o[0] || o;
                      value = false;
                    }

                    if ($.inArray(g.id,
                        gnShareConstants.internalGroups) !== -1 &&
                        $.inArray(userProfile,
                        gnShareConstants.internalGroupsProfiles) === -1) {
                      disabled = true;
                    }

                    g.privileges[key] = {value: value, disabled: disabled};
                  });
                });

                var operations = [];
                if (gnShareConstants.columnOrder) {
                  angular.forEach(gnShareConstants.columnOrder,
                      function(operationId) {
                        var operationFound =
                            $.grep(data.operations, function(o) {
                          return o.id === operationId;
                        });
                        operations.push(operationFound[0]);
                      });
                } else {
                  operations = data !== 'null' ? data.operations : null;
                }
                defer.resolve({
                  groups: groups,
                  operations: operations,
                  isAdminOrReviewer:
                      isAdminOrReviewer(userProfile, data.groupOwner, groups)});
              });
          return defer.promise;
        },

        /**
         * @ngdoc method
         * @methodOf gn_share_service.service:gnShareService
         * @name gnShareService#savePrivileges
         *
         * @description
         * Save privileges for a metadata record
         *
         * @param {string} The metadata identifier
         * @param {Object} The groups definition. The privileges
         * property is used to build request.
         *
         * @return {HttpPromise} Future object.
         */
        savePrivileges: function(metadataId, groups) {
          var defer = $q.defer();
          var params = {};
          var url;

          if(angular.isDefined(metadataId)) {
            url = 'md.privileges.update';
            params.id = metadataId;
          }
          else {
            url = 'md.privileges.batch.update';
          }
          angular.forEach(groups, function(g) {
            angular.forEach(g.privileges, function(p, key) {
              if (p.value === true) {
                params['_' + g.id + '_' + key] = 'on';
              }
            });
          });
          //TODO: fix service that crash with _content_type parameter
          $http.get(url, {params: params})
            .success(function(data) {
                defer.resolve(data);
              }).error(function(data) {
                defer.reject(data);
              });
          return defer.promise;
        }
      };
    }
  ]);
})();
