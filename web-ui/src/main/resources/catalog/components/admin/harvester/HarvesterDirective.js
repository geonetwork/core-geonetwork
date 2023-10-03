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
  goog.provide("gn_harvester_directive");

  var module = angular.module("gn_harvester_directive", []);

  /**
   * Display harvester identification section with
   * name, group and icon
   */
  module.directive("gnHarvesterIdentification", [
    "$http",
    "$rootScope",
    function ($http, $rootScope) {
      return {
        restrict: "A",
        replace: true,
        transclude: true,
        scope: {
          harvester: "=gnHarvesterIdentification"
          //               lang: '@lang'
        },
        templateUrl:
          "../../catalog/components/admin/harvester/partials/" + "identification.html",
        link: function (scope, element, attrs) {
          scope.lang = "eng"; // FIXME
          scope.hideIconPicker = true; // hide or show the icon picker
          scope.openTranslationModal = function () {
            var translations = scope.harvester.site.translations;
            if (translations === undefined || angular.isArray(translations)) {
              translations = {};
              scope.harvester.site.translations = translations;
            }

            for (var i = 0; i < scope.languages.length; i++) {
              if (translations[scope.languages[i].id] === undefined) {
                translations[scope.languages[i].id] = scope.harvester.site.name;
              }
            }
            $("#translationModal").modal("show");
          };
          $http
            .get("admin.harvester.info?type=icons&_content_type=json", { cache: true })
            .then(function (response) {
              scope.icons = response.data[0];
            });
          $http
            .get("info?_content_type=json&type=languages", { cache: true })
            .then(function (response) {
              scope.languages = response.data.language;
            });
        }
      };
    }
  ]);

  /**
   * Display account when remote login is used.
   */
  module.directive("gnHarvesterAccount", [
    function () {
      return {
        restrict: "A",
        replace: true,
        transclude: true,
        scope: {
          harvester: "=gnHarvesterAccount"
        },
        templateUrl: "../../catalog/components/admin/harvester/partials/account.html",
        link: function (scope, element, attrs) {}
      };
    }
  ]);
  /**
   * Display harvester schedule configuration.
   */
  module.directive("gnHarvesterSchedule", [
    "gnConfig",
    "$translate",
    function (gnConfig, $translate) {
      return {
        restrict: "A",
        replace: true,
        transclude: true,
        scope: {
          harvester: "=gnHarvesterSchedule"
        },
        templateUrl:
          "../../catalog/components/admin/harvester/partials/" + "schedule.html",
        link: function (scope, element, attrs) {
          scope.cronExp = [
            "0 0 12 * * ?",
            "0 15 10 * * ?",
            "0 0/5 14 * * ?",
            "0 15 10 ? * MON-FRI",
            "0 15 10 15 * ?"
          ];
          scope.timeZone = gnConfig["system.server.timeZone"];
          if (scope.timeZone) {
            scope.timeZoneOffset =
              "(GMT" + moment.tz(scope.timeZone).format("Z / z") + ")";
          }
          scope.setSchedule = function (exp) {
            scope.harvester.options.every = exp;
          };
        }
      };
    }
  ]);

  /**
   * Display harvester privileges configuration with
   * publish to all or by group.
   *
   * This direcive does not provide definition of
   * download and interactive privileges. TODO
   * see if this is necessary. Download maybe.
   * interactive may be better handled by using withheld
   * attribute.
   *
   * TODO: this directive could also be used
   * for metadata privileges. To be improved.
   */
  module.directive("gnHarvesterPrivileges", [
    "$http",
    "$translate",
    "$rootScope",
    "gnShareConstants",
    function ($http, $translate, $rootScope, gnShareConstants) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          harvester: "=gnHarvesterPrivileges",
          lang: "@"
        },
        templateUrl:
          "../../catalog/components/admin/harvester/partials/" + "privileges.html",
        link: function (scope, element, attrs) {
          scope.selectedPrivileges = {};

          var getPrivilege = function (groupId) {
            return {
              "@id": groupId,
              operation: [
                {
                  "@name": "view"
                },
                {
                  "@name": "dynamic"
                },
                {
                  "@name": "download"
                }
              ]
            };
          };
          var defaultPrivileges = [getPrivilege(1)];

          // deal with order by
          scope.sorter = null;
          scope.setSorter = function (g) {
            if (scope.sorter == "name") return g.label ? g.label[scope.lang] : g.name;
            else if (scope.sorter == "checked") return scope.selectedPrivileges[g["@id"]];
            else return 0;
          };

          var internalGroups = gnShareConstants.internalGroups;

          scope.keepInternalGroups = function (g) {
            if (internalGroups.includes(parseInt(g["@id"]))) return true;
            else return false;
          };
          scope.removeInternalGroups = function (g) {
            if (internalGroups.includes(parseInt(g["@id"]))) return false;
            else return true;
          };
          function loadGroups() {
            $http
              .get("info?_content_type=json&" + "type=groupsIncludingSystemGroups", {
                cache: true
              })
              .then(function (response) {
                scope.groups = response.data !== "null" ? response.data.group : null;
              });
          }

          var initHarvesterPrivileges = function () {
            angular.forEach(scope.harvester.privileges, function (g) {
              scope.selectedPrivileges[g["@id"]] = true;
            });
          };

          var init = function () {
            scope.selectedPrivileges = {};

            loadGroups();

            // If only one privilege config
            // and group name is equal to Internet (ie. 1)
            if (
              scope.harvester.privileges &&
              scope.harvester.privileges.length === 1 &&
              scope.harvester.privileges[0]["@id"] == "1"
            ) {
              $("#gn-harvester-visible-all").button("toggle");
            }

            initHarvesterPrivileges();

            scope.$watchCollection("selectedPrivileges", function () {
              scope.harvester.privileges = [];
              angular.forEach(scope.selectedPrivileges, function (value, key) {
                if (value) {
                  scope.harvester.privileges.push(getPrivilege(key));
                }
              });
            });
          };

          scope.$watch("harvester", init);
        }
      };
    }
  ]);

  module.directive("gnLogoPicker", [
    "$http",
    "$translate",
    "$rootScope",
    function ($http, $translate, $rootScope) {
      return {
        restrict: "A",
        replace: false,
        scope: {
          logo: "=gnLogoPicker"
        },
        templateUrl:
          "../../catalog/components/admin/harvester/partials/" + "logopicker.html",

        link: function (scope, element, attrs) {
          $http
            .get("admin.harvester.info?type=icons&_content_type=json", { cache: true })
            .then(function (response) {
              scope.icons = response.data[0];
            });

          /**
           * Set the icon
           * @param {String} icon Icon name
           */
          scope.setIcon = function (icon) {
            scope.logo = icon;
          };

          /**
           * Open the Logo picker modal dialog
           */
          scope.openLogoPicker = function () {
            $("#logo-picker-modal").modal("show");
          };

          /**
           * Close the Logo picker modal dialog
           */
          scope.closeLogoPicker = function () {
            $("#logo-picker-modal").modal("hide");
          };

          /**
           * Toggle the logo height
           * @param  {String} type Type of logo height selected
           */
          scope.toggleLogoHeight = function (type) {
            scope.logoheightType = type;
          };
        }
      };
    }
  ]);

  /**
   * Extra fields common for all harvesters
   */
  module.directive("gnHarvesterExtras", [
    "$http",
    "$translate",
    "$rootScope",
    function ($http, $translate, $rootScope) {
      return {
        restrict: "A",
        replace: false,
        templateUrl: "../../catalog/components/admin/harvester/partials/" + "extras.html",
        scope: {
          harvester: "=gnHarvesterExtras"
        },
        link: function (scope, element, attrs) {
          $http.get("../api/languages", { cache: true }).then(function (response) {
            scope.languages = response.data;
          });
        }
      };
    }
  ]);

  /**
   * CSW Harvester filter
   */
  module.directive("gnCswHarvesterFilter", [
    "$http",
    "$translate",
    "$rootScope",
    "$timeout",
    function ($http, $translate, $rootScope, $timeout) {
      return {
        restrict: "A",
        replace: false,
        templateUrl:
          "../../catalog/components/admin/harvester/partials/" + "csw-filter.html",
        scope: {
          harvester: "=gnCswHarvesterFilter",
          cswCriteria: "=cswCriteria"
        },
        link: function (scope, element, attrs) {
          scope.enableAddFilter = false;
          scope.cswCriteriaTranslated = [];

          var substringMatcher = function (strs) {
            return function findMatches(q, cb) {
              var matches, substringRegex;

              // an array that will be populated with substring matches
              matches = [];

              // regex used to determine if a string contains the substring `q`
              substringRegex = new RegExp(q, "i");

              // iterate through the pool of strings and for any string that
              // contains the substring `q`, add it to the `matches` array
              $.each(strs, function (i, str) {
                if (substringRegex.test(str)) {
                  matches.push(str);
                }
              });

              cb(matches);
            };
          };

          /**
           * Adds typeahead to a csw filter field.
           *
           * @param fieldId
           */
          var configureTypeaheadField = function (fieldId) {
            var field = $(fieldId);

            // Required, otherwise when change the harvester url keeps the old values
            field.typeahead("destroy");

            field.typeahead(
              {
                minLength: 0,
                hint: true,
                highlight: true
              },
              {
                source: substringMatcher(scope.cswCriteriaTranslated),
                limit: Infinity
              }
            );

            field.bind("typeahead:selected", function () {
              field.trigger("change");
            });

            // To select the suggested value, otherwise is lost
            field.bind("typeahead:change", function () {
              field.trigger("change");
            });
          };

          /**
           * Configure the csw filter fields to use typeahead.
           *
           * @param fieldId
           */
          var configureHarvesterFilters = function () {
            if (angular.isDefined(scope.harvester.filters)) {
              $timeout(function () {
                for (var i = 0; i < scope.harvester.filters.length; i++) {
                  var fieldId = "#tagsinput_" + i;
                  configureTypeaheadField(fieldId);
                }
              }, 0);
            }
          };

          scope.addFilter = function () {
            var condition = "AND";

            if (
              angular.isUndefined(scope.harvester.filters) ||
              scope.harvester.filters.length == 0
            ) {
              scope.harvester.filters = [];
              condition = "";
            }

            scope.harvester.filters.push({
              field: scope.cswCriteriaTranslated[0],
              operator: "LIKE",
              value: "",
              condition: condition
            });

            $timeout(function () {
              var fieldId = "#tagsinput_" + (scope.harvester.filters.length - 1);
              configureTypeaheadField(fieldId);
            }, 0);
          };

          scope.removeFilter = function (index) {
            scope.harvester.filters.splice(index, 1);

            if (scope.harvester.filters.length > 0) {
              scope.harvester.filters[0].condition = "";
            }
          };

          scope.$watch(
            "harvester.filters",
            function (newCol, oldCol, scope) {
              // Checks for the filter collection reference changes.
              // Required to add typeahead to the filters after saving the harvester.
              configureHarvesterFilters();
            },
            false
          );

          scope.$watch(
            "cswCriteria",
            function (newCol, oldCol, scope) {
              if (scope.cswCriteria && scope.cswCriteria.length > 0) {
                scope.enableAddFilter = true;
                scope.cswCriteriaTranslated = [];

                for (var i = 0; i < scope.cswCriteria.length; i++) {
                  scope.cswCriteriaTranslated.push(
                    $translate.instant(scope.cswCriteria[i].replace("__", ":"))
                  );
                }

                configureHarvesterFilters();
              } else {
                scope.enableAddFilter = false;
              }
            },
            true
          );
        }
      };
    }
  ]);
})();
