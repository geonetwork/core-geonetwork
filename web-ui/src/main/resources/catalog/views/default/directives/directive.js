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
          scope.showCodeColumn = false;
          angular.forEach(scope.attributeTable, function (elem) {
            if (elem.code > "") {
              scope.showCodeColumn = true;
            }
          });
        }
      };
    }
  ]);

  module.directive("gnLinksBtn", [
    "gnTplResultlistLinksbtn",
    function (gnTplResultlistLinksbtn) {
      return {
        restrict: "E",
        replace: true,
        scope: true,
        templateUrl: gnTplResultlistLinksbtn
      };
    }
  ]);

  module.directive("gnMdActionsMenu", [
    "gnMetadataActions",
    "$http",
    "gnConfig",
    "gnConfigService",
    "gnGlobalSettings",
    "gnLangs",
    function (
      gnMetadataActions,
      $http,
      gnConfig,
      gnConfigService,
      gnGlobalSettings,
      gnLangs
    ) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/views/default/directives/" + "partials/mdactionmenu.html",
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.gnMdActionsMenu);
          scope.formatterList = gnGlobalSettings.gnCfg.mods.search.downloadFormatter;

          scope.tasks = [];
          scope.hasVisibletasks = false;

          gnConfigService.load().then(function (c) {
            scope.isMdWorkflowEnable = gnConfig["metadata.workflow.enable"];

            scope.isMdWorkflowAssistEnable =
              gnGlobalSettings.gnCfg.mods.workflowHelper.enabled;
            scope.workFlowApps =
              gnGlobalSettings.gnCfg.mods.workflowHelper.workflowAssistApps;
            scope.iso2Lang = gnLangs.getIso2Lang(gnLangs.getCurrent());
          });

          scope.buildFormatter = function (url, uuid, isDraft) {
            if (url.indexOf("${uuid}") !== -1) {
              return url.replace("${lang}", scope.lang).replace("${uuid}", uuid);
            } else {
              return (
                "../api/records/" +
                uuid +
                url.replace("${lang}", scope.lang) +
                (url.indexOf("?") !== -1 ? "&" : "?") +
                "approved=" +
                (isDraft != "y")
              );
            }
          };

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
                return gnConfig["system.publication.doi.doienabled"];
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

          loadTasks();

          scope.$watch(attrs.gnMdActionsMenu, function (a) {
            scope.md = a;
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
          field: "="
        },
        link: function linkFn(scope, element, attr) {
          var today = moment();
          scope.relations = ["intersects", "within", "contains"];
          scope.relation = scope.relations[0];
          scope.field = {
            range: {
              resourceTemporalDateRange: {
                gte: null,
                lte: null,
                relation: scope.relation
              }
            }
          };

          scope.setRange = function () {
            scope.field.range.resourceTemporalDateRange.gte = scope.dateFrom;
            scope.field.range.resourceTemporalDateRange.lte = scope.dateTo;
            scope.field.range.resourceTemporalDateRange.relation = scope.relation;
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
