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
  goog.provide("gn_mdactions_directive");

  goog.require("gn_mdactions_service");

  var module = angular.module("gn_mdactions_directive", []);

  module.directive("gnMetadataBatchApprove", [
    "$translate",
    "$http",
    "gnMetadataManager",
    "gnUtilityService",
    function ($translate, $http, gnMetadataManager, gnUtilityService) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/metadataactions/partials/" + "batchapprove.html",
        scope: {
          selectionBucket: "@"
        },
        link: function (scope) {
          var translations = null;
          $translate(["metadataApproved"]).then(function (t) {
            translations = t;
          });

          scope.changeMessage = "";
          scope.directApproval = false;

          scope.approve = function () {
            scope.$broadcast("operationOnSelectionStart");

            return $http
              .put("../api/records/approve", {
                bucket: scope.selectionBucket,
                message: scope.changeMessage,
                directApproval: scope.directApproval
              })
              .then(
                function (response) {
                  scope.processReport = response.data;
                  var reportTemplate =
                    "../../catalog/components/utility/" +
                    "partials/batchreport-workflow.html";

                  scope.$broadcast("operationOnSelectionStop");

                  // A report is returned
                  gnUtilityService.openModal(
                    {
                      title: translations.metadataApproved,
                      content:
                        '<div gn-batch-report="processReport" template-url="' +
                        reportTemplate +
                        '"></div>',
                      className: "gn-status-popup",
                      onCloseCallback: function () {
                        scope.$emit("metadataStatusUpdated", true);
                        scope.$emit("StatusUpdated", true);
                        scope.$broadcast("operationOnSelectionStop");
                        scope.processReport = null;
                      }
                    },
                    scope,
                    "StatusUpdated"
                  );
                },
                function (response) {
                  scope.$broadcast("operationOnSelectionStop");
                  scope.$emit("metadataStatusUpdated", false);

                  scope.$emit("StatusUpdated", {
                    title: $translate.instant("metadataStatusUpdatedErrors"),
                    error: response.data,
                    timeout: 0,
                    type: "danger"
                  });
                }
              );
          };
        }
      };
    }
  ]);

  module.directive("gnMetadataBatchSubmit", [
    "$translate",
    "$http",
    "gnMetadataManager",
    "gnUtilityService",
    function ($translate, $http, gnMetadataManager, gnUtilityService) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/metadataactions/partials/" + "batchsubmit.html",
        scope: {
          selectionBucket: "@"
        },
        link: function (scope) {
          var translations = null;
          $translate(["metadataSubmitted"]).then(function (t) {
            translations = t;
          });

          scope.changeMessage = "";

          scope.submit = function () {
            scope.$broadcast("operationOnSelectionStart");

            return $http
              .put("../api/records/submit", {
                bucket: scope.selectionBucket,
                message: scope.changeMessage
              })
              .then(
                function (response) {
                  scope.processReport = response.data;
                  var reportTemplate =
                    "../../catalog/components/utility/" +
                    "partials/batchreport-workflow.html";

                  scope.$broadcast("operationOnSelectionStop");

                  // A report is returned
                  gnUtilityService.openModal(
                    {
                      title: translations.metadataSubmitted,
                      content:
                        '<div gn-batch-report="processReport" template-url="' +
                        reportTemplate +
                        '"></div>',
                      className: "gn-status-popup",
                      onCloseCallback: function () {
                        scope.$emit("metadataStatusUpdated", true);
                        scope.$emit("StatusUpdated", true);
                        scope.$broadcast("operationOnSelectionStop");
                        scope.processReport = null;
                      }
                    },
                    scope,
                    "StatusUpdated"
                  );
                },
                function (response) {
                  scope.$broadcast("operationOnSelectionStop");
                  scope.$emit("metadataStatusUpdated", false);

                  scope.$emit("StatusUpdated", {
                    title: $translate.instant("metadataStatusUpdatedErrors"),
                    error: response.data,
                    timeout: 0,
                    type: "danger"
                  });
                }
              );
          };
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_mdactions_directive.directive:gnMetadataStatusUpdater
   * @restrict A
   * @requires gnMetadataStatusUpdater
   *
   * @description
   * The `gnMetadataStatusUpdater` directive provides a
   * form to update the record status. Status can be related
   * to one of the worflow step and could also be to trigger
   * a action.
   */
  module.directive("gnMetadataStatusUpdater", [
    "$translate",
    "$http",
    "gnMetadataManager",
    function ($translate, $http, gnMetadataManager) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/metadataactions/partials/" + "statusupdater.html",
        scope: {
          md: "=gnMetadataStatusUpdater",
          statusType: "@",
          task: "=",
          statusToSelect: "@"
        },
        link: function (scope) {
          var user = scope.$parent.user;
          var metadataId = scope.md.id;
          var defaultType = "workflow";

          scope.statusType = scope.statusType || defaultType;
          scope.lang = scope.$parent.lang;
          scope.task = angular.isDefined(scope.task) ? scope.task : scope.$parent.task;
          scope.newStatus = {
            status: scope.task ? scope.task.id : -1,
            owner: null,
            dueDate: null,
            changeMessage: ""
          };

          // Retrieve last status to set it in the form
          function init() {
            if (scope.statusType === defaultType) {
              return $http
                .get(
                  "../api/records/" + metadataId + "/status/" + scope.statusType + "/last"
                )
                .then(function (response) {
                  var data = response.data;

                  scope.status = data !== "null" ? data.status : null;
                  scope.newStatus.status = scope.statusToSelect;
                  scope.lastStatus = data.currentStatus.id.statusId;
                });
            } else {
              return $http
                .get("../api/status/" + scope.statusType)
                .then(function (response) {
                  var data = response.data;

                  scope.status = data;
                  scope.newStatus = {
                    status: scope.task ? scope.task.id : 0,
                    owner: null,
                    dueDate: null,
                    changeMessage: ""
                  };
                });
            }
          }

          scope.updateStatus = function () {
            // Assign task owner id if needed
            if (scope.newStatus.owner) {
              scope.newStatus.owner = scope.newStatus.owner.id;
            }
            return $http
              .put("../api/records/" + metadataId + "/status", scope.newStatus)
              .then(
                function (response) {
                  //After the new status is approved, the working copy will get deleted and will not get searched.
                  //The search parameter will need to reset to draft=n
                  if (
                    angular.isDefined(scope.md.draft) &&
                    scope.newStatus.status === "2"
                  ) {
                    scope.md.draft = "n";
                  }
                  gnMetadataManager.updateMdObj(scope.md);
                  scope.$emit("metadataStatusUpdated", true);
                  scope.$emit("StatusUpdated", {
                    msg: $translate.instant("metadataStatusUpdatedWithNoErrors"),
                    timeout: 2,
                    type: "success"
                  });
                },
                function (response) {
                  scope.$emit("metadataStatusUpdated", false);
                  scope.$emit("StatusUpdated", {
                    title: $translate.instant("metadataStatusUpdatedErrors"),
                    error: response.data,
                    timeout: 0,
                    type: "danger"
                  });
                }
              );
          };

          var statusApproved = 2;
          var statusSubmitted = 3;
          var statusRejected = 5;
          scope.cantChangeStatus = function (status) {
            return (
              (status == statusRejected ||
                status == statusApproved ||
                status == statusSubmitted) &&
              !user.isReviewerOrMore()
            );
          };

          init();
        }
      };
    }
  ]);

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
  module.directive("gnMetadataCategoryUpdater", [
    "gnMetadataActions",
    "$translate",
    "$http",
    "$rootScope",
    "$filter",
    "$timeout",
    function (gnMetadataActions, $translate, $http, $rootScope, $filter, $timeout) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/metadataactions/partials/" +
          "metadatacategoryupdater.html",
        scope: {
          currentCategories: "=gnMetadataCategoryUpdater",
          metadataUuid: "=",
          groupOwner: "=gnGroupOwner"
        },
        link: function (scope, e, attrs) {
          scope.lang = scope.$parent.lang;
          scope.categories = null;
          scope.ids = [];
          scope.tid = "tagsinput" + Math.floor(Math.random() * 10000);
          scope.mode = attrs["gnMode"] || "btn";

          var initialCategories = [];
          var tid = "#" + scope.tid;

          scope.updateCategoriesAllowed = function () {
            if (angular.isDefined(scope.groupOwner)) {
              $http
                .get("../api/groups/" + scope.groupOwner, { cache: true })
                .then(function (response) {
                  var data = response.data;

                  scope.enableallowedcategories = data.enableAllowedCategories;
                  scope.allowedcategories = [];
                  angular.forEach(data.allowedCategories, function (c) {
                    scope.allowedcategories.push(c.id);
                  });
                });
            }
          };
          scope.updateCategoriesAllowed();

          scope.$watch("groupOwner", function (newvalue, oldvalue) {
            scope.updateCategoriesAllowed();
          });

          scope.$watch("currentCategories", function (newvalue, oldvalue) {
            init();
          });

          var init = function () {
            return $http.get("../api/tags", { cache: true }).then(function (response) {
              var lang = scope.lang;
              scope.categories = response.data;
              angular.forEach(scope.categories, function (c) {
                if (
                  angular.isDefined(scope.currentCategories) &&
                  scope.currentCategories.values.indexOf(c.name) !== -1
                ) {
                  scope.ids.push(c.id);
                  initialCategories.push(c);
                }
                c.langlabel = $filter("gnLocalized")(c.label, lang);
              });

              if (scope.mode === "autocomplete") {
                initTagInput();
              }
            });
          };

          function initTagInput() {
            $timeout(function () {
              try {
                var maxNumberOfItems = 1000;

                // Init tag input
                $(tid).tagsinput({
                  itemValue: "name",
                  itemText: "langlabel",
                  maxTags: maxNumberOfItems
                });
                var input = $(tid).tagsinput("input");

                // Init data source
                var source = new Bloodhound({
                  datumTokenizer: Bloodhound.tokenizers.obj.whitespace("name"),
                  queryTokenizer: Bloodhound.tokenizers.whitespace,
                  local: scope.categories,
                  limit: maxNumberOfItems
                });
                source.initialize();

                function allOrSearchFn(q, sync) {
                  if (q === "") {
                    sync(source.all());
                    // This is the only change needed to get 'ALL'
                    // items as the defaults
                  } else {
                    source.search(q, sync);
                  }
                }

                // Init autocomplete
                $(input)
                  .typeahead(
                    {
                      minLength: 0,
                      highlight: true
                    },
                    {
                      name: "category",
                      source: allOrSearchFn,
                      displayKey: "langlabel",
                      limit: Infinity
                    }
                  )
                  .bind(
                    "typeahead:selected",
                    $.proxy(function (obj, c) {
                      // Add to tags
                      this.tagsinput("add", c);

                      scope.assign(c);

                      // Clear typeahead
                      this.tagsinput("input").typeahead("val", "");
                    }, $(tid))
                  );

                $(tid).on("itemRemoved", function (e) {
                  scope.assign(e.item);
                });

                angular.forEach(initialCategories, function (c) {
                  $(tid).tagsinput("add", c);
                });
              } catch (e) {
                console.warn("No tagsinput for " + tid + ", error: " + e.message);
              }
            });
          }

          scope.sortByLabel = function (c) {
            return c.label[scope.lang];
          };

          // Remove or add category to the set of ids
          scope.assign = function (c, event) {
            if (event) {
              event.stopPropagation();
            }
            var existIndex = scope.ids.indexOf(c.id),
              method = "";
            if (existIndex === -1) {
              method = "put";
            } else {
              method = "delete";
            }
            $http[method](
              "../api/records/" + scope.metadataUuid + "/tags?id=" + c.id
            ).then(
              function () {
                if (existIndex === -1) {
                  scope.ids.push(c.id);
                  scope.currentCategories.values.push(c.name);
                } else {
                  scope.ids.splice(existIndex, 1);

                  angular.forEach(scope.currentCategories.values, function (cat, idx) {
                    if (cat === c.name) {
                      scope.currentCategories.values.splice(idx, 1);
                    }
                  });
                }
              },
              function (response) {
                $rootScope.$broadcast("StatusUpdated", {
                  title: $translate.instant("assignCategoryError", { category: c.name }),
                  error: response.data,
                  timeout: 0,
                  type: "danger"
                });
              }
            );
            return false;
          };
          init();
        }
      };
    }
  ]);
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
  module.directive("gnMetadataGroupUpdater", [
    "gnMetadataActions",
    "$translate",
    "$http",
    "$rootScope",
    function (gnMetadataActions, $translate, $http, $rootScope) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/metadataactions/partials/" +
          "metadatagroupupdater.html",
        scope: {
          groupOwner: "=gnMetadataGroupUpdater",
          metadataId: "="
        },
        link: function (scope) {
          scope.lang = scope.$parent.lang;
          scope.groups = null;

          scope.init = function (event) {
            return $http
              .get("../api/groups?profile=Editor", { cache: true })
              .then(function (response) {
                scope.groups = response.data;
              });
          };

          scope.sortByLabel = function (group) {
            return group.label[scope.lang];
          };

          scope.assignGroup = function (g, event) {
            event.stopPropagation();
            gnMetadataActions.assignGroup(scope.metadataId, g.id).then(
              function () {
                scope.groupOwner = g.id;
              },
              function (error) {
                $rootScope.$broadcast("StatusUpdated", {
                  title: $translate.instant("error"),
                  error: error,
                  timeout: 0,
                  type: "danger"
                });
              }
            );
            return false;
          };
        }
      };
    }
  ]);

  module.directive("gnPermalinkInput", [
    function () {
      return {
        restrict: "A",
        replace: false,
        templateUrl:
          "../../catalog/components/metadataactions/partials/permalinkinput.html",
        link: function (scope, element, attrs) {
          scope.url = attrs["gnPermalinkInput"];
          scope.copied = false;
          setTimeout(function () {
            element.find(":input").select();
          }, 300);
        }
      };
    }
  ]);

  module.directive("gnMetadataCitation", [
    "$translate",
    "$http",
    "gnAlertService",
    function ($translate, $http, gnAlertService) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/metadataactions/partials/" + "citation.html",
        scope: {
          md: "=gnMetadataCitation",
          format: "@"
        },
        link: function (scope) {
          scope.defaultFormat = "html";
          scope.currentFormat = null;
          scope.formats = [];
          scope.citationText = "";
          scope.citationAvailable = false;
          scope.isCode = false;

          function buildUrl() {
            return "../api/records/" + scope.md.uuid + "/formatters/citation?format=";
          }

          scope.getCitation = function (format) {
            return $http
              .get(buildUrl() + format, {
                cache: true,
                headers: { Accept: format === "?" ? "application/json" : "text/plain" }
              })
              .then(
                function (r) {
                  if (format === "?") {
                    scope.formats = [];
                    for (var i = 0; i < r.data.length; i++) {
                      var f = r.data[i],
                        prefix = "cite.format.",
                        translation = $translate.instant(prefix + f),
                        translationFound = translation.indexOf(prefix) === -1,
                        help = $translate.instant(prefix + f + ".help"),
                        helpFound = translation.indexOf(prefix) === -1;
                      scope.formats.push({
                        id: f,
                        label: translationFound ? translation : f,
                        help: helpFound ? help : ""
                      });
                    }
                  } else {
                    scope.currentFormat = format;
                    scope.isCode = ["ris", "bibtex"].indexOf(format) != -1;
                    scope.citationText = r.data;
                    scope.citationAvailable = true;
                  }
                },
                function (r) {
                  scope.citationAvailable = false;
                }
              );
          };

          function loadCitation() {
            scope.citationAvailable = false;
            scope.getCitation("?").then(function () {
              scope.getCitation(scope.format || scope.defaultFormat);
            });
          }

          if (scope.md) {
            loadCitation();
          }

          scope.$watchCollection("md", function (n, o) {
            if (n && n !== o) {
              loadCitation();
            }
          });
        }
      };
    }
  ]);

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
  module.directive("gnTransferOwnership", [
    "$translate",
    "$http",
    "gnHttp",
    "$rootScope",
    "gnUtilityService",
    function ($translate, $http, gnHttp, $rootScope, gnUtilityService) {
      return {
        restrict: "A",
        replace: false,
        templateUrl:
          "../../catalog/components/metadataactions/partials/" + "transferownership.html",
        link: function (scope, element, attrs) {
          var ownerId = parseInt(attrs["gnTransferMdOwner"]);
          var groupOwner = parseInt(attrs["gnTransferMdGroupOwner"]);
          var bucket = attrs["selectionBucket"];
          var mdUuid = attrs["gnTransferOwnership"];
          scope.selectedUserGroup = null;
          scope.groupsLoaded = false;
          scope.userGroupDefined = false;
          scope.userGroups = null;

          scope.ownerUserName = "";
          scope.ownerGroupName = "";

          if (ownerId) {
            $http.get("../api/users/" + ownerId).then(function (response) {
              scope.ownerUserName = response.data.username;
            });
          }

          if (groupOwner) {
            $http.get("../api/groups/" + groupOwner).then(function (response) {
              scope.ownerGroupName = response.data.name;
            });
          }

          scope.selectGroup = function (group) {
            scope.selectedGroup = group;
          };
          $http
            .get("../api/users/groups")
            .then(function (response) {
              var uniqueUserGroups = {};
              angular.forEach(response.data, function (g) {
                var key = g.groupId + "-" + g.userId;
                if (!uniqueUserGroups[key]) {
                  uniqueUserGroups[key] = g;
                  uniqueUserGroups[key].groupNameTranslated =
                    g.groupName === "allAdmins"
                      ? $translate.instant(g.groupName)
                      : $translate.instant("group-" + g.groupId);
                }
              });

              // Sort by group name and user name
              var sortedKeys = Object.keys(uniqueUserGroups).sort(function (a, b) {
                var ka =
                  uniqueUserGroups[a].groupNameTranslated +
                  "|" +
                  uniqueUserGroups[a].userName;
                var kb =
                  uniqueUserGroups[b].groupNameTranslated +
                  "|" +
                  uniqueUserGroups[b].userName;

                return ka.localeCompare(kb);
              });

              scope.userGroups = {};
              angular.forEach(sortedKeys, function (g) {
                scope.userGroups[g] = uniqueUserGroups[g];
              });

              if (scope.userGroups && Object.keys(scope.userGroups).length > 0) {
                scope.userGroupDefined = true;
              } else {
                scope.userGroupDefined = false;
              }
            })
            .finally(function () {
              scope.groupsLoaded = true;
            });

          scope.save = function () {
            if (!scope.selectedUserGroup) {
              return;
            }
            var url = "../api/records/";
            if (bucket != "null") {
              url += "ownership?bucket=" + bucket + "&";
            } else {
              url += mdUuid + "/ownership?";
            }
            return $http
              .put(
                url +
                  "userIdentifier=" +
                  scope.selectedUserGroup.userId +
                  "&groupIdentifier=" +
                  scope.selectedUserGroup.groupId
              )
              .then(function (r) {
                var msg = $translate.instant("transfertPrivilegesFinished", {
                  metadata: r.data.numberOfRecordsProcessed
                });

                scope.processReport = r.data;

                // A report is returned
                gnUtilityService.openModal(
                  {
                    title: msg,
                    content: '<div gn-batch-report="processReport"></div>',
                    className: "gn-privileges-popup",
                    onCloseCallback: function () {
                      if (bucket != "null") {
                        scope.$emit("search", true);
                        scope.$broadcast("operationOnSelectionStop");
                      }
                      scope.$emit("TransferOwnershipDone", true);
                      scope.processReport = null;
                    }
                  },
                  scope,
                  "TransferOwnershipDone"
                );
              });
          };
        }
      };
    }
  ]);
})();
