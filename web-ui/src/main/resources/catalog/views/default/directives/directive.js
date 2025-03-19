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
  goog.provide("gn_search_default_directive");

  var module = angular.module("gn_search_default_directive", []);

  module.directive("gnInfoList", [
    "gnMdView",
    function (gnMdView) {
      return {
        restrict: "A",
        replace: true,
        templateUrl: "../../catalog/views/default/directives/" + "partials/infolist.html",
        link: function linkFn(scope, element, attr) {
          scope.showMore = function (isDisplay) {
            var div = $("#gn-info-list" + this.md.uuid);
            $(div.children()[isDisplay ? 0 : 1]).addClass("hidden");
            $(div.children()[isDisplay ? 1 : 0]).removeClass("hidden");
          };
          scope.go = function (uuid) {
            gnMdView(index, md, records);
            gnMdView.setLocationUuid(uuid);
          };
        }
      };
    }
  ]);

  module.directive("gnAttributeTableRenderer", [
    "gnMdView",
    function (gnMdView) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/views/default/directives/" + "partials/attributetable.html",
        scope: {
          attributeTable: "=gnAttributeTableRenderer"
        },
        link: function linkFn(scope, element, attrs) {
          if (
            angular.isDefined(scope.attributeTable) &&
            !angular.isArray(scope.attributeTable)
          ) {
            scope.attributeTable = [scope.attributeTable];
          }
          scope.columnVisibility = {
            code: false
          };
          angular.forEach(scope.attributeTable, function (elem) {
            if (elem.code > "") {
              scope.columnVisibility.code = true;
            }
          });
        }
      };
    }
  ]);

  module.directive("gnApplicationBanner", [
    "gnConfig",
    "gnConfigService",
    function (gnConfig, gnConfigService) {
      return {
        restrict: "E",
        replace: true,
        scope: true,
        templateUrl:
          "../../catalog/views/default/directives/partials/applicationBanner.html",
        link: function linkFn(scope) {
          gnConfigService.load().then(function (c) {
            scope.isBannerEnabled = gnConfig["system.banner.enable"];
          });
        }
      };
    }
  ]);

  module.directive("gnLinksBtn", [
    "gnTplResultlistLinksbtn",
    "gnMetadataActions",
    function (gnTplResultlistLinksbtn, gnMetadataActions) {
      return {
        restrict: "E",
        replace: true,
        scope: true,
        templateUrl: gnTplResultlistLinksbtn,
        link: function linkFn(scope) {
          scope.gnMetadataActions = gnMetadataActions;
        }
      };
    }
  ]);

  module.directive("gnMdActionsMenu", [
    "gnMetadataActions",
    "$http",
    "$q",
    "gnConfig",
    "gnConfigService",
    "gnGlobalSettings",
    "gnLangs",
    function (
      gnMetadataActions,
      $http,
      $q,
      gnConfig,
      gnConfigService,
      gnGlobalSettings,
      gnLangs
    ) {
      return {
        restrict: "A",
        replace: true,
        templateUrl: "../../catalog/views/default/directives/partials/mdactionmenu.html",
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.gnMdActionsMenu);
          scope.formatterList = gnGlobalSettings.gnCfg.mods.search.downloadFormatter;

          scope.tasks = [];
          scope.hasVisibletasks = false;

          scope.doiServers = [];

          gnConfigService.load().then(function (c) {
            scope.isMdWorkflowEnable = gnConfig["metadata.workflow.enable"];

            scope.isMdWorkflowAssistEnable =
              gnGlobalSettings.gnCfg.mods.workflowHelper.enabled;
            scope.workFlowApps =
              gnGlobalSettings.gnCfg.mods.workflowHelper.workflowAssistApps;
            scope.iso2Lang = gnLangs.getIso2Lang(gnLangs.getCurrent());
          });

          scope.status = undefined;

          scope.buildFormatter = function (url, uuid, isDraft) {
            if (url.indexOf("${uuid}") !== -1) {
              return url.replace("${lang}", scope.lang).replace("${uuid}", uuid);
            } else {
              return (
                "../api/records/" +
                uuid +
                url.replace("${lang}", scope.lang) +
                (isDraft == "y"
                  ? (url.indexOf("?") !== -1 ? "&" : "?") + "approved=false"
                  : "")
              );
            }
          };

          function loadWorkflowStatus() {
            return $http
              .get("../api/status/workflow", { cache: true })
              .then(function (response) {
                scope.status = {};
                response.data.forEach(function (s) {
                  scope.status[s.name] = s.id;
                });

                scope.statusEffects = {
                  editor: [
                    {
                      from: "draft",
                      to: "submitted"
                    },
                    {
                      from: "retired",
                      to: "draft"
                    },
                    {
                      from: "submitted",
                      to: "draft"
                    }
                  ],
                  reviewer: [
                    {
                      from: "draft",
                      to: "submitted"
                    },
                    {
                      from: "submitted",
                      to: "approved"
                    },
                    {
                      from: "submitted",
                      to: "draft"
                    },
                    {
                      from: "draft",
                      to: "approved"
                    },
                    {
                      from: "approved",
                      to: "retired"
                    },
                    {
                      from: "retired",
                      to: "draft"
                    }
                  ]
                };
              });
          }

          function loadTasks() {
            return $http
              .get("../api/status/task", { cache: true })
              .then(function (response) {
                scope.tasks = response.data;
                scope.getVisibleTasks();
              });
          }

          scope.getVisibleTasks = function () {
            $.each(scope.tasks, function (i, t) {
              scope.hasVisibletasks =
                scope.taskConfiguration[t.name] &&
                scope.taskConfiguration[t.name].isVisible &&
                scope.taskConfiguration[t.name].isVisible();
            });
          };

          scope.taskConfiguration = {
            doiCreationTask: {
              isVisible: function (md) {
                return scope.doiServers.length > 0;
              },
              isApplicable: function (md) {
                // TODO: Would be good to return why a task is not applicable as tooltip
                // TODO: Add has DOI already
                return (
                  md &&
                  md.isPublished() &&
                  md.isTemplate === "n" &&
                  JSON.parse(md.isHarvested) === false
                );
              }
            }
          };

          /**
           * Display the publication / un-publication option. Checks:
           *   - User can review the metadata.
           *   - It's not a draft.
           *   - Retired metadata can't be published.
           *   - The user profile can publish / unpublish the metadata.
           * @param md
           * @param user
           * @returns {*|boolean|false|boolean}
           */
          scope.displayPublicationOption = function (md, user, pubOption) {
            return (
              md &&
              md.canReview &&
              md.draft != "y" &&
              md.mdStatus != 3 &&
              ((md.isPublished(pubOption) && user.canUnpublishMetadata()) ||
                (!md.isPublished(pubOption) && user.canPublishMetadata()))
            );
          };

          /**
           * Checks if the workflow option for the specified step should be displayed for the given user.
           *
           * Checks:
           * - The user is logged in.
           * - The metadata record is not null.
           * - The user can edit the metadata record.
           * - The metadata record has a group owner.
           * - The user is an admin or is an editor or more for the group owner.
           * - The metadata record has a status.
           * - The step has a 'from' status.
           * - The 'from' status of the step exists in the status object.
           * - The 'from' status of the step matches the status of the metadata record.
           * - Workflow is enabled in the configuration.
           * - The metadata record has workflow enabled.
           *
           * @param {Object} step - The workflow step to check.
           * @param {Object} user - The user for whom the check is being performed.
           * @returns {boolean} - True if the workflow option should be displayed, false otherwise.
           */
          scope.displayWorkflowStepOption = function (step, user) {
            return (
              user.id &&
              scope.md &&
              user.canEditRecord(scope.md) &&
              scope.md.groupOwner &&
              (user.isAdmin() || user.isEditorOrMoreForGroup(scope.md.groupOwner)) &&
              scope.md.mdStatus &&
              scope.status &&
              step.from &&
              scope.status[step.from] &&
              scope.md.mdStatus == scope.status[step.from] &&
              scope.isMdWorkflowEnable &&
              scope.md.isWorkflowEnabled()
            );
          };

          /**
           * Checks if any workflow option should be displayed for the given user.
           *
           * @param {Object} user - The user for whom the check is being performed.
           * @returns {boolean} - True if any workflow option should be displayed, false otherwise.
           */
          scope.anyWorkflowOptionDisplayed = function (user) {
            if (scope.displayEnableWorkflowOption(user)) {
              return true;
            }
            return scope
              .getStatusEffects(user)
              .some((step) => scope.displayWorkflowStepOption(step, user));
          };

          /**
           * Retrieves the workflow status effects for the given user based on their role.
           *
           * @param {Object} user - The user for whom the workflow status effects are being retrieved.
           * @returns {Array} - An array of workflow status effects applicable to the user.
           */
          scope.getStatusEffects = function (user) {
            const isReviewer =
              user.isAdmin() || user.isReviewerForGroup(scope.md.groupOwner);
            return scope.statusEffects[isReviewer ? "reviewer" : "editor"];
          };

          /**
           * Can workflow be enabled for this metadata record and user?
           *
           * Checks:
           *  - Workflow is enabled in the configuration.
           *  - The metadata record is not null.
           *  - The metadata record does not have workflow enabled.
           *  - The user is logged in.
           *  - The user is an admin or can edit the metadata record.
           *  - The metadata record has a group owner.
           *  - The group matching regex is set.
           *  - The group owner name matches the group matching regex.
           *
           * @param user The user who wants to enable the workflow
           * @returns {boolean} True if the workflow can be enabled, false otherwise
           */
          scope.displayEnableWorkflowOption = function (user) {
            return (
              scope.isMdWorkflowEnable &&
              scope.md &&
              !scope.md.isWorkflowEnabled() &&
              user.id &&
              (user.isAdmin() || user.canEditRecord(scope.md)) &&
              scope.ownerGroupName &&
              scope.workflowGroupMatchingRegex &&
              scope.ownerGroupName.match(scope.workflowGroupMatchingRegex)
            );
          };

          loadTasks();
          loadWorkflowStatus();

          scope.$watch(attrs.gnMdActionsMenu, function (a) {
            scope.md = a;

            if (scope.md) {
              $http
                .get("../api/doiservers/metadata/" + scope.md.id)
                .then(function (response) {
                  scope.doiServers = response.data;
                });
              if (scope.md.groupOwner) {
                // Load the group owner name when the metadata record changes
                $http
                  .get("../api/groups/" + scope.md.groupOwner, { cache: true })
                  .then(function (response) {
                    if (response.data.name) {
                      scope.ownerGroupName = response.data.name;
                    }
                  });
              }
            }
          });

          scope.getScope = function () {
            return scope;
          };
        }
      };
    }
  ]);

  module.directive("gnPeriodChooser", [
    function () {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/views/default/directives/" + "partials/periodchooser.html",
        scope: {
          label: "@gnPeriodChooser",
          dateFrom: "=",
          dateTo: "="
        },
        link: function linkFn(scope, element, attr) {
          var today = moment();
          scope.format = "YYYY-MM-DD";
          scope.options = [
            "today",
            "yesterday",
            "thisWeek",
            "thisMonth",
            "last3Months",
            "last6Months",
            "thisYear"
          ];
          scope.setPeriod = function (option) {
            if (option === "today") {
              var date = today.format(scope.format);
              scope.dateFrom = date;
            } else if (option === "yesterday") {
              var date = today.clone().subtract(1, "day").format(scope.format);
              scope.dateFrom = date;
              scope.dateTo = today.format(scope.format);
              return;
            } else if (option === "thisWeek") {
              scope.dateFrom = today.clone().startOf("week").format(scope.format);
            } else if (option === "thisMonth") {
              scope.dateFrom = today.clone().startOf("month").format(scope.format);
            } else if (option === "last3Months") {
              scope.dateFrom = today
                .clone()
                .startOf("month")
                .subtract(3, "month")
                .format(scope.format);
            } else if (option === "last6Months") {
              scope.dateFrom = today
                .clone()
                .startOf("month")
                .subtract(6, "month")
                .format(scope.format);
            } else if (option === "thisYear") {
              scope.dateFrom = today.clone().startOf("year").format(scope.format);
            }
            scope.dateTo = today.clone().add(1, "day").format(scope.format);
          };
        }
      };
    }
  ]);

  /**
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/range.html
   */
  module.directive("gnDateRangeFilter", [
    function () {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/views/default/directives/" + "partials/dateRangeFilter.html",
        scope: {
          label: "@gnDateRangeFilter",
          field: "=",
          fieldName: "="
        },
        link: function linkFn(scope, element, attr) {
          var today = moment();
          scope.relations = ["intersects", "within", "contains"];
          scope.relation = scope.relations[0];
          scope.field.range = scope.field.range || {};
          scope.field.range[scope.fieldName] = {
            gte: null,
            lte: null,
            relation: scope.relation
          };

          scope.setRange = function () {
            scope.field.range[scope.fieldName].gte = scope.dateFrom;
            scope.field.range[scope.fieldName].lte = scope.dateTo;
            scope.field.range[scope.fieldName].relation = scope.relation;
          };

          scope.format = "YYYY-MM-DD";
          scope.options = [
            "today",
            "yesterday",
            "thisWeek",
            "thisMonth",
            "last3Months",
            "last6Months",
            "thisYear"
          ];
          scope.setPeriod = function (option) {
            if (option === "today") {
              var date = today.format(scope.format);
              scope.dateFrom = date;
            } else if (option === "yesterday") {
              var date = today.clone().subtract(1, "day").format(scope.format);
              scope.dateFrom = date;
              scope.dateTo = today.format(scope.format);
              return;
            } else if (option === "thisWeek") {
              scope.dateFrom = today.clone().startOf("week").format(scope.format);
            } else if (option === "thisMonth") {
              scope.dateFrom = today.clone().startOf("month").format(scope.format);
            } else if (option === "last3Months") {
              scope.dateFrom = today
                .clone()
                .startOf("month")
                .subtract(3, "month")
                .format(scope.format);
            } else if (option === "last6Months") {
              scope.dateFrom = today
                .clone()
                .startOf("month")
                .subtract(6, "month")
                .format(scope.format);
            } else if (option === "thisYear") {
              scope.dateFrom = today.clone().startOf("year").format(scope.format);
            }
            scope.dateTo = today.clone().add(1, "day").format(scope.format);
            scope.setRange();
          };
          scope.$watch("dateFrom", function (n, o) {
            if (n !== o) {
              scope.setRange();
            }
          });
          scope.$watch("dateTo", function (n, o) {
            if (n !== o) {
              scope.setRange();
            }
          });

          scope.$on("beforeSearchReset", function () {
            scope.dateFrom = null;
            scope.dateTo = null;
            scope.relation = scope.relations[0];
          });
        }
      };
    }
  ]);
})();
