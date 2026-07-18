/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

  var module = angular.module("gn_date_picker_directive", []);

  /**
   *  Create a widget to handle date composed of
   *  a date input and a time input. It can only be
   *  used to create an ISO date.
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
      // Cache for timezone names list - built once and reused across all directive instances
      var cachedTimezoneNames = null;

      // Pattern to validate (custom) date formats: YYYY, YYYY-MM, YYYY-MM-DD, YYYY-MM-DDTHH:mm:ss
      var datePattern = /^\d{4}(-\d{2}(-\d{2}(T\d{2}:\d{2}:\d{2})?)?)?$/;

      // Pattern to parse complete ISO 8601 datetime string at once
      // Groups: 1=date, 2=time (with optional fractional seconds), 3=timezone
      var dateTimePattern = /^([^T]+)(?:T([^Z+-]+)((?:[+-]\d{2}:?\d{2}|Z)?))?$/;

      // Let Moment.js figure out the user timezone (often same as browser timezone)
      var userTimezone = moment.tz.guess();

      // Use Moment.js to get the timezone offset for a given timezone name
      var getTimeZoneOffset = function (timeZone) {
        var actualTz = timeZone;
        if (userTimezone && timeZone && timeZone.trim().toLowerCase() === "browser") {
          actualTz = userTimezone;
        }
        return moment
          .tz(actualTz)
          .format("ZZ")
          .replace(/([+-]?[0-9]{2})([0-9]{2})/, "$1:$2");
      };

      // Pre-defined timezone names and offsets
      var uiTimezoneName = gnGlobalSettings.gnCfg.mods.global.timezone;
      var uiTimezoneOffset = getTimeZoneOffset(uiTimezoneName);
      var serverTimezoneName = gnConfig["system.server.timeZone"]; // NOTE: this may be "null" if not set!
      var serverTimezoneOffset = getTimeZoneOffset(serverTimezoneName);

      // Build a list of timezone names and offsets for the datepicker
      var buildTimezoneNamesList = function () {
        if (cachedTimezoneNames !== null) {
          return cachedTimezoneNames;
        }

        var timezoneNames = [
          {
            // No timezone
            name: $translate.instant("NoTimezone"),
            offset: ""
          },
          {
            // Recommended timezone first (browser timezone)
            name:
              $translate.instant("CatalogUiTimezone") +
              (uiTimezoneName && uiTimezoneName !== "null" ? " " + uiTimezoneName : ""),
            offset: uiTimezoneOffset
          },
          {
            // User timezone (often same as browser timezone)
            name:
              $translate.instant("YourTimezone") +
              (userTimezone ? " " + userTimezone : ""),
            offset: getTimeZoneOffset(userTimezone)
          },
          {
            // Server timezone
            name:
              $translate.instant("CatalogTimezone") +
              (serverTimezoneName && serverTimezoneName !== "null"
                ? " " + serverTimezoneName
                : ""),
            offset: serverTimezoneOffset
          },
          {
            // Add separator between predefined timezones and all others
            name: "----",
            offset: "" // if user selects this, interpret as "No Timezone"
          }
        ];

        // Add all other available timezones
        _.forEach(moment.tz.names(), function (tz) {
          timezoneNames.push({
            name: tz,
            offset: getTimeZoneOffset(tz)
          });
        });

        cachedTimezoneNames = timezoneNames;
        return timezoneNames;
      };

      // Ensures that truthy values are coerced to boolean true
      var coerceBool = function (val) {
        return val !== "false" && !!val;
      };

      // Date mode constants
      var DATE_MODE = {
        DATE: "date",
        DATETIME: "datetime",
        MONTH: "month",
        YEAR: "year"
      };

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
          // Expose DATE_MODE to scope
          scope.DATE_MODE = DATE_MODE;

          // Check if browser supports HTML5 date/time input types.
          // If not, datetimepicker.js is used (it will not
          // support year or month only mode in this case).
          scope.dateTypeSupported = Modernizr.inputtypes.date;
          scope.isValidDate = true;
          // Hide the datetime option from the date mode picker, if defined
          scope.hideTime = coerceBool(scope.hideTime);
          // Hide the date mode picker, if defined
          scope.hideDateMode = coerceBool(scope.hideDateMode);

          // Watch for external changes and re-coerce
          scope.$watch("hideTime", function (newVal) {
            var coerced = coerceBool(newVal);
            if (newVal !== coerced) {
              scope.hideTime = coerced;
            }
          });
          scope.$watch("hideDateMode", function (newVal) {
            var coerced = coerceBool(newVal);
            if (newVal !== coerced) {
              scope.hideDateMode = coerced;
            }
          });

          // Format date when datetimepicker is used
          scope.formatFromDatePicker = function (date) {
            var format = "YYYY-MM-DDTHH:mm:ss";
            var dateTime = moment(date);
            scope.dateInput = dateTime.format(format);
          };

          // Get cached timezone names list (built once and reused)
          scope.timezoneNames = buildTimezoneNamesList();

          // Returns the first timezone object match based on offset (e.g. "+02:00").
          // If no offset is provided, the "No Timezone" object should be returned.
          var getTimezoneObject = function (offset) {
            offset = offset || ""; // Default to "No Timezone" if not set
            return _.find(scope.timezoneNames, function (tz) {
              return tz.offset === offset;
            });
          };

          scope.year =
            scope.month =
            scope.time =
            scope.date =
            scope.dateDropDownInput =
              "";
          scope.timezoneObj = getTimezoneObject(uiTimezoneOffset);
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
          } else if (scope.value.length === 7) {
            scope.month = moment(scope.value, "YYYY-MM").toDate();
            scope.mode = DATE_MODE.MONTH;
          } else {
            // Value is a ISO 8601 date or datetime - parse all parts excluding fractional seconds
            var match = scope.value.match(dateTimePattern);

            if (match) {
              var datePart = match[1]; // Date part (YYYY-MM-DD)
              var timePart = match[2]; // Time part (HH:mm:ss)
              var tzPart = match[3] || ""; // Timezone part (+HH:mm or Z)

              // Parse and set date
              scope.date = "";
              var dateValue = moment(datePart);
              if (dateValue.isValid()) {
                scope.date = new Date(dateValue.utc().format());
              }

              // Parse and set time and timezone if present
              scope.time = "";
              if (timePart) {
                scope.time = moment(timePart, "HH:mm:ss").toDate();

                var tzOffset = tzPart;
                if (tzOffset) {
                  if (tzOffset === "Z") {
                    // Convert Zulu/UTC time to +00:00 offset
                    tzOffset = "+00:00";
                  } else if (tzOffset.indexOf(":") === -1) {
                    // Normalize timezone format to include colon (e.g., +0100 -> +01:00)
                    tzOffset = tzOffset.slice(0, 3) + ":" + tzOffset.slice(3);
                  }
                }

                // Find the *first* matching timezone object in the list for more consistent selection
                scope.timezoneObj = getTimezoneObject(tzOffset);
                scope.mode = DATE_MODE.DATETIME;
              }
            }
          }

          if (!scope.dateTypeSupported) {
            // Browser does not support HTML5 date/time input types
            scope.dateInput = scope.value;
            scope.dateDropDownInput = scope.value;
          }

          scope.setMode = function (mode) {
            // Called when user changes date mode
            if (mode !== DATE_MODE.DATETIME && scope.mode === DATE_MODE.DATETIME) {
              // Reset time and timezone when switching from datetime to something else
              scope.time = "";
              scope.timezoneObj = getTimezoneObject(uiTimezoneOffset);
            }
            scope.mode = mode;
          };

          var resetDateIfNeeded = function () {
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
              scope.timezoneObj = getTimezoneObject(uiTimezoneOffset);
            }
          };

          // Build xml snippet based on input date.
          var buildDate = function () {
            var tag = scope.tagName !== undefined ? scope.tagName : "gco:Date";
            var namespace = tag.split(":")[0];

            if (!scope.dateTypeSupported) {
              // Check date against simple date pattern
              // to add a CSS class to highlight error.
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
              var time = $filter("date")(scope.time, "HH:mm:ss");
              var tzOffset = scope.timezoneObj ? scope.timezoneObj.offset : "";
              if (tzOffset === "+00:00") {
                // Convert +00:00 offset to Zulu/UTC time
                tzOffset = "Z";
              }
              scope.dateTime = $filter("date")(scope.date, "yyyy-MM-dd");
              scope.dateTime += "T" + time + tzOffset;
            } else {
              // Mode is DATE_MODE.DATE (default)
              scope.dateTime = $filter("date")(scope.date, "yyyy-MM-dd");
            }
            if (tag === "") {
              scope.xmlSnippet = scope.dateTime;
            } else {
              var attribute = "";
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
          scope.$watch("timezoneObj", buildDate);
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
