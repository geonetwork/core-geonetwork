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
  goog.provide("gn_popup_directive");

  var module = angular.module("gn_popup_directive", []);

  module.directive("gnModal", function () {
    return {
      restrict: "A",
      transclude: true,
      scope: {
        toggle: "=gnPopup",
        optionsFunc: "&gnPopupOptions" // Options from directive
      },
      templateUrl: "../../catalog/components/common/popup/" + "partials/popup.html",

      link: function (scope, element, attrs) {
        // Get the popup options
        scope.options = scope.optionsFunc();

        if (!scope.options) {
          scope.options = {
            title: ""
          };
        }

        // Checks if requires a confirm action word to enable the confirm button
        scope.hasConfirmActionWord = angular.isDefined(scope.options.confirmActionWord);

        // User input for confirm action word
        scope.userInput = "";

        element.addClass("gn-popup modal fade");

        // handle close callback
        if (scope.options.closeCallback) {
          element.on("hidden.bs.modal", function () {
            scope.userInput = "";
            scope.options.closeCallback();
          });
        } else {
          element.on("hidden.bs.modal", function () {
            scope.userInput = "";
          });
        }

        /**
         * Checks to disable the confirm button if a confirm action word
         * is defined and the user input does not match it.
         *
         * @returns {boolean}
         */
        scope.disableConfirmButton = function () {
          if (scope.hasConfirmActionWord && scope.options.confirmActionWord !== "") {
            return scope.userInput !== scope.options.confirmActionWord;
          }

          return false;
        };
      }
    };
  });

  module.directive("gnPopup", [
    "$translate",
    function ($translate) {
      return {
        restrict: "A",
        transclude: true,
        scope: {
          toggle: "=gnPopup",
          optionsFunc: "&gnPopupOptions" // Options from directive
        },
        template:
          '<h4 class="popover-title gn-popup-title">' +
          "<span translate>{{options.title}}</span>" +
          '<button type="button" class="close" ng-click="close($event)">' +
          "&times;</button>" +
          '<i class="icon-print gn-popup-print hidden-print" ' +
          'title="{{titlePrint}}" ' +
          'ng-if="options.showPrint" ng-click="print()"></i>' +
          "</h4>" +
          '<div class="popover-content gn-popup-content" ' +
          "ng-transclude>" +
          "</div>",

        link: function (scope, element, attrs) {
          // Get the popup options
          scope.options = scope.optionsFunc();
          scope.titlePrint = $translate.instant("print_action");

          if (!scope.options) {
            scope.options = {
              title: ""
            };
          }

          // Per default hide the print function
          if (!angular.isDefined(scope.options.showPrint)) {
            scope.options.showPrint = false;
          }

          // Move the popup to its original position, only used on desktop
          scope.moveToOriginalPosition = function () {
            element.css({
              left: scope.options.x || $(document.body).width() / 2 - element.width() / 2,
              top: scope.options.y || 60 // 50 is the default size of the header + extra margin
            });
          };

          // Add close popup function
          scope.close =
            scope.options.close ||
            function (event) {
              if (event) {
                event.stopPropagation();
                event.preventDefault();
              }
              if (angular.isDefined(scope.toggle)) {
                scope.toggle = false;
              } else {
                element.hide();
              }
            };

          scope.print =
            scope.options.print ||
            function () {
              var contentEl = element.find(".ga-popup-content");
              gaPrintService.htmlPrintout(contentEl.clone().html());
            };

          element.addClass("popover");

          // Watch the shown property
          scope.$watch("toggle", function (newVal, oldVal) {
            if (newVal != oldVal || newVal != (element.css("display") == "block")) {
              element.toggle(newVal);
              scope.moveToOriginalPosition();
            }
          });
        }
      };
    }
  ]);
})();
