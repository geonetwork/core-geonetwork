/*
 * Copyright (C) 2001-2020 Food and Agriculture Organization of the
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
  goog.provide("gn_timezone_selector_directive");

  var module = angular.module("gn_timezone_selector_directive", []);

  /**
   * This directive requires momentjs_timezones and typeahead JS libraries.
   */
  module.directive("gnTimezoneSelector", [
    "$interpolate",
    function ($interpolate) {
      return {
        restrict: "A",
        replace: false,
        transclude: false,
        scope: {
          elementTimezone: "@elementTimezone"
        },
        link: function (scope, element, attrs) {
          var lastValidValue = scope.elementTimezone;
          $(element).val(lastValidValue);

          var userTimezone = moment.tz.guess(),
            timezoneNames = [
              {
                name: userTimezone,
                offset: moment.tz(userTimezone).format("Z / z")
              }
            ];
          _.forEach(moment.tz.names(), function (tz, index, list) {
            timezoneNames.push({
              name: tz,
              offset: moment.tz(tz).format("Z / z")
            });
          });
          var source = new Bloodhound({
            datumTokenizer: function (datum) {
              var name = datum.name + " " + datum.offset;
              var tokens = [name].concat(Bloodhound.tokenizers.nonword(name));
              if (name.indexOf("_") >= 0) {
                tokens.push(name.replace("_", ""));
              }

              var stringSize = name.length;
              //multiple combinations for every available size
              //(eg. dog = d, o, g, do, og, dog)
              for (var size = 2; size <= stringSize; size++) {
                for (var i = 0; i + size <= stringSize; i++) {
                  var currentToken = name.substr(i, size);
                  tokens.push(currentToken);
                  if (currentToken.indexOf("_") > 0) {
                    tokens.push(currentToken.replace("_", " "));
                  }
                }
              }

              return tokens;
            },
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            limit: 100,
            local: timezoneNames
          });

          element.typeahead(
            {
              hint: true,
              highlight: true,
              minLength: 0
            },
            {
              name: "timezones",
              limit: 1000,
              source: source.ttAdapter(),
              display: function (suggestedTz) {
                return suggestedTz.name;
              },
              templates: {
                suggestion: _.template("<div><%- name %> (GMT<%- offset %>)</div>")
              }
            }
          );

          $(element).bind("typeahead:change", function (ev, suggestion) {
            var normalizedTz = _.find(moment.tz.names(), function (tz) {
              return suggestion.toLowerCase() === tz.toLowerCase();
            });

            if (angular.isUndefined(normalizedTz) && suggestion.trim() !== "") {
              normalizedTz = lastValidValue;
            } else if (suggestion.trim() === "") {
              normalizedTz = "";
            }

            $(element).val(normalizedTz.trim());
            lastValidValue = normalizedTz;
          });
        }
      };
    }
  ]);
})();
