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
            if (elem.code > "" || attribute.designationObject || attribute.designation) {
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
