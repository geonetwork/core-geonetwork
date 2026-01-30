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
  goog.provide("gn_date_picker_directive");

  const module = angular.module("gn_date_picker_directive", []);

  /**
   *  Create a widget to handle date composed of
   *  a date input and a time input. It can only be
   *  used to create an ISO date. It hides the
   *  need of choosing from ISO type date or datetime.
   *
   *  It's also useful as html datetime input are not
   *  yet widely supported.
   */
  module.directive("gnDatePicker", [
    "$http",
    "$rootScope",
    "$filter",
    "$timeout",
    "gnSchemaManagerService",
    "gnCurrentEdit",
    "gnConfig",
    "gnGlobalSettings",
    "$translate",
    function (
      $http,
      $rootScope,
      $filter,
      $timeout,
      gnSchemaManagerService,
      gnCurrentEdit,
      gnConfig,
      gnGlobalSettings,
      $translate
    ) {
      return {
        restrict: "A",
        scope: {
          value: "@gnDatePicker",
          label: "@",
          elementName: "@",
          elementRef: "@",
          id: "@",
          tagName: "@",
          indeterminatePosition: "@",
          required: "@",
          hideTime: "@",
          hideDateMode: "@"
        },
        templateUrl:
          "../../catalog/components/edit/datepicker/partials/" + "datepicker.html",
        link: function (scope, element, attrs) {
          // Define DATE_MODE constant
          const DATE_MODE = {
            DATE: "date",
            DATETIME: "datetime",
            MONTH: "month",
            YEAR: "year"
          };
          // Expose DATE_MODE to scope
          scope.DATE_MODE = DATE_MODE;

          const coerceBool = function (val) {
            return val !== "false" && !!val;
          };

          // Check if browser support date type or not to
          // HTML date and time input types.
          // If not datetimepicker.js is used (it will not
          // support year or month only mode in this case)
          scope.dateTypeSupported = Modernizr.inputtypes.date;
          scope.isValidDate = true;
          scope.hideTime = coerceBool(scope.hideTime);
          // Hide the date mode picker: date / datetime / month-year / year
          scope.hideDateMode = coerceBool(scope.hideDateMode);

          // Watch for external changes and re-coerce
          scope.$watch("hideTime", function (newVal) {
            const coerced = coerceBool(newVal);
            if (newVal !== coerced) {
              scope.hideTime = coerced;
            }
          });
          scope.$watch("hideDateMode", function (newVal) {
            const coerced = coerceBool(newVal);
            if (newVal !== coerced) {
              scope.hideTime = coerced;
            }
          });

          const userTimezone = moment.tz.guess();
          const uiTimezone = gnGlobalSettings.gnCfg.mods.global.timezone;
          const serverTimezone = gnConfig["system.server.timeZone"];
          const datePattern = new RegExp(
            "^\\d{4}$|" +
              "^\\d{4}-\\d{2}$|" +
              "^\\d{4}-\\d{2}-\\d{2}$|" +
              "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$"
          );

          const getTimeZoneOffset = function (timeZone) {
            let actualTz = timeZone;
            if (timeZone && timeZone.trim().toLowerCase() === "browser") {
              actualTz = userTimezone;
            }
            return moment
              .tz(actualTz)
              .format("ZZ")
              .replace(/([+-]?[0-9]{2})([0-9]{2})/, "$1:$2");
          };

          scope.timezone = getTimeZoneOffset(uiTimezone) || serverTimezone || "";
          scope.uiTimeZoneEqualToServer = serverTimezone === uiTimezone;
          scope.hideTimezone = scope.uiTimeZoneEqualToServer;

          // Format date when datetimepicker is used.
          scope.formatFromDatePicker = function (date) {
            const format = "YYYY-MM-DDTHH:mm:ss";
            const dateTime = moment(date);
            scope.dateInput = dateTime.format(format);
          };

          // Define timezone options
          scope.timezoneNames = [
            {
              // No timezone
              name: $translate.instant("NoTimezone"),
              offset: ""
            },
            {
              // User timezone
              name:
                $translate.instant("YourTimezone") +
                (userTimezone ? " " + userTimezone : ""),
              offset: getTimeZoneOffset(userTimezone)
            },
            {
              // Server timezone
              name:
                $translate.instant("CatalogTimezone") +
                (serverTimezone ? " " + serverTimezone : ""),
              offset: getTimeZoneOffset(serverTimezone)
            },
            {
              // Recommended timezone (browser timezone)
              name:
                $translate.instant("CatalogUiTimezone") +
                (uiTimezone ? " " + uiTimezone : ""),
              offset: getTimeZoneOffset(uiTimezone)
            },
            {
              // Add separator between predefined timezones and all others
              name: "----",
              offset: ""
            }
          ];
          // Add all available timezones
          _.forEach(moment.tz.names(), function (tz) {
            scope.timezoneNames.push({
              name: tz,
              offset: getTimeZoneOffset(tz)
            });
          });

          scope.year =
            scope.month =
            scope.time =
            scope.date =
            scope.dateDropDownInput =
              "";
          scope.mode = DATE_MODE.DATE; // Default mode is date only
          scope.withIndeterminatePosition = attrs.indeterminatePosition !== undefined;

          // Default date is empty
          // Compute mode based on date length. The format
          // is always ISO YYYY-MM-DDTHH:mm:ss
          if (!scope.value) {
            scope.value = "";
          } else if (scope.value.length === 4) {
            scope.year = parseInt(scope.value);
            scope.mode = DATE_MODE.YEAR;
            scope.hideTimezone = true;
          } else if (scope.value.length === 7) {
            scope.month = moment(scope.value, "YYYY-MM").toDate();
            scope.mode = DATE_MODE.MONTH;
            scope.hideTimezone = true;
          } else {
            // Value is a date or datetime
            const isDateTime = scope.value.indexOf("T") !== -1;
            const tokens = scope.value.split("T");

            // Default to empty string and prevent 'Invalid Date' string to xmlSnippet
            scope.date = "";
            if (moment(isDateTime ? tokens[0] : scope.value).isValid()) {
              scope.date = new Date(
                moment(isDateTime ? tokens[0] : scope.value)
                  .utc()
                  .format()
              );
            }

            // Process time part (if defined)
            let time = tokens[1];
            if (time !== undefined) {
              scope.time = isDateTime ? moment(time, "HH:mm:ss").toDate() : undefined;
              console.log("parsed time:", scope.time);
              scope.timezone = time.slice(8);
              scope.hideTimezone =
                scope.uiTimeZoneEqualToServer &&
                getTimeZoneOffset(uiTimezone) === scope.timezone;
              scope.mode = DATE_MODE.DATETIME;
            } else {
              scope.time = "";
              scope.timezone = "";
              scope.hideTimezone = scope.uiTimeZoneEqualToServer;
            }
          }
          if (!scope.dateTypeSupported) {
            scope.dateInput = scope.value;
            scope.dateDropDownInput = scope.value;
          }

          scope.setMode = function (mode) {
            // Called when user changes date mode
            scope.mode = mode;
            scope.hideTimezone = mode === DATE_MODE.YEAR || mode === DATE_MODE.MONTH;
          };

          const resetDateIfNeeded = function () {
            // Reset date if indeterminate position is now or unknown
            if (
              scope.withIndeterminatePosition &&
              (scope.indeterminatePosition === "now" ||
                scope.indeterminatePosition === "unknown")
            ) {
              scope.dateInput = "";
              scope.date = "";
              scope.year = "";
              scope.month = "";
              scope.time = "";
              scope.timezone = "";
            }
          };

          // Build xml snippet based on input date.
          const buildDate = function () {
            let tag = scope.tagName !== undefined ? scope.tagName : "gco:Date";
            const namespace = tag.split(":")[0];

            if (!scope.dateTypeSupported) {
              // Check date against simple date pattern
              // to add a css class to highlight error.
              // Input will be saved anyway.
              scope.isValidDate = scope.dateInput.match(datePattern) !== null;

              if (scope.dateInput === undefined) {
                return;
              } else {
                tag =
                  scope.tagName !== undefined
                    ? scope.tagName
                    : scope.dateInput.indexOf("T") === -1
                    ? "gco:Date"
                    : "gco:DateTime";
              }
              scope.dateTime = scope.dateInput;
            } else if (scope.mode === DATE_MODE.YEAR) {
              scope.dateTime = scope.year;
            } else if (scope.mode === DATE_MODE.MONTH) {
              scope.dateTime = $filter("date")(scope.month, "yyyy-MM");
            } else if (scope.mode === DATE_MODE.DATETIME && scope.time) {
              tag = scope.tagName !== undefined ? scope.tagName : "gco:DateTime";
              let time = $filter("date")(scope.time, "HH:mm:ss");
              // TODO: Set seconds, Timezone ?
              scope.dateTime = $filter("date")(scope.date, "yyyy-MM-dd");
              scope.dateTime += "T" + time + scope.timezone;
            } else {
              scope.dateTime = $filter("date")(scope.date, "yyyy-MM-dd");
            }
            if (tag === "") {
              scope.xmlSnippet = scope.dateTime;
            } else {
              let attribute = "";
              if (scope.withIndeterminatePosition && scope.indeterminatePosition !== "") {
                attribute =
                  ' indeterminatePosition="' + scope.indeterminatePosition + '"';
              }

              if (scope.dateTime == null) {
                scope.dateTime = "";
              }

              scope.xmlSnippet =
                "<" +
                tag +
                " xmlns:" +
                namespace +
                '="' +
                gnSchemaManagerService.findNamespaceUri(namespace, gnCurrentEdit.schema) +
                '"' +
                attribute +
                ">" +
                scope.dateTime +
                "</" +
                tag +
                ">";
            }
          };

          scope.$watch("date", buildDate);
          scope.$watch("time", buildDate);
          scope.$watch("timezone", buildDate);
          scope.$watch("year", buildDate);
          scope.$watch("month", buildDate);
          scope.$watch("dateInput", buildDate);
          scope.$watch("indeterminatePosition", buildDate);
          scope.$watch("indeterminatePosition", resetDateIfNeeded);
          scope.$watch("xmlSnippet", function () {
            if (scope.id) {
              // This is required on init to have the optionally
              // templateFieldDirective initialized first so
              // that the template is properly computed.
              $timeout(function () {
                $(scope.id).val(scope.xmlSnippet).change();
              });
            }
          });

          buildDate();
        }
      };
    }
  ]);
})();
