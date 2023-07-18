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

(function () {
  goog.provide("gn_share_service");

  var module = angular.module("gn_share_service", []);

  module.value("gnShareConstants", {
    // Customize column to be displayed and the order
    // TODO: Move config to DB using order in operations table
    columnOrder: ["view", "dynamic", "download", "editing", "notify"],
    icons: {
      view: "fa-unlock",
      dynamic: "fa-globe",
      download: "fa-download",
      notify: "fa-envelope",
      process: "fa-cog",
      editing: "fa-pencil"
    },
    internalOperations: ["editing", "notify"],
    internalGroups: [-1, 0, 1],
    internalGroupsProfiles: ["Administrator", "UserAdmin", "Reviewer"],
    // Use topGroups to place those groups with internet, intranet groups
    // on top of the privileges panel.
    // TODO: Move config to DB using isTopGroups in groups table
    // or using reserved column ?
    topGroups: []
  });

  /**
   * @ngdoc service
   * @kind function
   * @name gn_share.service:gnShareService
   * @requires gnShareConstants
   * @requires $q
   * @requires $http
   *
   * @description
   * The `gnShareService` service provides all tools required to manage
   * privileges on metadata records. Privileges define which type operation
   * (eg. view, download, edit) a group of user can do.
   */
  module.factory("gnShareService", [
    "$q",
    "$http",
    "gnShareConstants",
    "gnConfig",
    "gnUrlUtils",
    function ($q, $http, gnShareConstants, gnConfig, gnUrlUtils) {
      var isAdminOrReviewer = function (userProfile, groupOwner, privileges, batchMode) {
        var publicationbyrevieweringroupowneronly =
          gnConfig["system.metadataprivs.publicationbyrevieweringroupowneronly"] === false
            ? false
            : true;

        if ($.inArray(userProfile, gnShareConstants.internalGroupsProfiles) === -1) {
          return false;
        } else if (
          userProfile === "Administrator" ||
          (userProfile === "Reviewer" && batchMode)
        ) {
          return true;
        } else {
          // Check if user is member of groupOwner
          // or check if user is Reviewer and can edit record
          var ownerGroupInfo = $.grep(privileges, function (g) {
            if (publicationbyrevieweringroupowneronly) {
              return g.group == groupOwner;
            } else {
              return (
                g.group == groupOwner ||
                (g.operations.editing && $.inArray("Reviewer", g.userProfiles) !== -1)
              );
            }
          });

          var profiles = [];
          for (var j = 0; j < ownerGroupInfo.length; j++) {
            var groupProfile = ownerGroupInfo[j].userProfiles;
            if (groupProfile) {
              if ($.isArray(groupProfile)) {
                profiles = profiles.concat(groupProfile);
              } else {
                profiles.push(groupProfile);
              }
            }
          }
          // Check profile for the group where the metadata was created
          return $.inArray("Reviewer", profiles) !== -1;
        }
      };

      return {
        /**
         * @ngdoc method
         * @methodOf gn_share.service:gnShareService
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
         * @methodOf gn_share.service:gnShareService
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
        loadPrivileges: function (metadataId, userProfile) {
          var defer = $q.defer();
          var url =
            "../api/records" +
            (angular.isDefined(metadataId) ? "/" + metadataId : "") +
            "/sharing";

          $http.get(url).then(function (response) {
            var data = response.data;

            var groups = data.privileges;

            // Promote custom topgroups
            angular.forEach(groups, function (g) {
              if ($.inArray(g.group, gnShareConstants.topGroups) !== -1) {
                g.reserved = "true";
              }
            });

            var operations = [];
            if (angular.isArray(gnShareConstants.columnOrder)) {
              operations = gnShareConstants.columnOrder;
            } else {
              angular.forEach(data.privileges[0].operations, function (value, key) {
                ops.push(key);
              });
            }

            defer.resolve({
              privileges: groups,
              operations: operations,
              isAdminOrReviewer: isAdminOrReviewer(
                userProfile,
                data.groupOwner,
                groups,
                angular.isUndefined(metadataId)
              )
            });
          });
          return defer.promise;
        },

        publish: function (metadataId, bucket, publish, user, publicationType) {
          var defer = $q.defer();
          var url =
            "../api/records" +
            (angular.isDefined(metadataId) ? "/" + metadataId : "") +
            "/" +
            (publish ? "publish" : "unpublish");

          if (angular.isDefined(bucket)) {
            url = gnUrlUtils.append(url, "bucket=" + bucket);
          }

          if (angular.isDefined(publicationType)) {
            url = gnUrlUtils.append(url, "publicationType=" + publicationType);
          }

          $http.put(url).then(
            function (response) {
              defer.resolve(response);
            },
            function (response) {
              defer.reject(response);
            }
          );

          return defer.promise;
        },

        /**
         * @ngdoc method
         * @methodOf gn_share.service:gnShareService
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
        savePrivileges: function (metadataId, bucket, privileges, user, replace) {
          var defer = $q.defer();
          var url =
            "../api/records" +
            (angular.isDefined(metadataId) ? "/" + metadataId : "") +
            "/sharing";

          if (angular.isDefined(bucket)) {
            url += "?bucket=" + bucket;
          }
          var ops = [];
          angular.forEach(privileges, function (g) {
            // Do not submit internal groups info
            // If user is not allowed.
            var allowed =
              ($.inArray(g.group, gnShareConstants.internalGroups) !== -1 &&
                user.isReviewerOrMore()) ||
              $.inArray(g.group, gnShareConstants.internalGroups) === -1;

            if (allowed) {
              ops.push({
                group: g.group,
                operations: g.operations
              });
            }
          });

          $http
            .put(url, {
              clear: replace,
              privileges: ops
            })
            .then(
              function (response) {
                defer.resolve(response);
              },
              function (response) {
                defer.reject(response);
              }
            );
          return defer.promise;
        }
      };
    }
  ]);
})();
