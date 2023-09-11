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
  goog.provide("gn_scroll_spy_directive");

  goog.require("gn_utility_service");

  var module = angular.module("gn_scroll_spy_directive", []);

  /**
   * Scroll spy navigation bar. Only cover 2 levels of fieldsets.
   *
   * Fieldset could be directly nested ie. fieldset > fieldset
   * or allDepth parameter is set to true to skip on level of
   * fieldset ie. fieldset > fieldset (skipped) > fieldset. This
   * mode correspond to an editor in non flat mode.
   *
   * The watch attribute define a scope variable to watch before
   * initialization. Initialization is done once.
   *
   * By default, the scroll spy div is displayed. Use collapse
   * attribute to collapsed it.
   */
  module.directive("gnScrollSpy", [
    "gnUtilityService",
    "$timeout",
    function (gnUtilityService, $timeout) {
      return {
        restrict: "A",
        replace: false,
        scope: {
          id: "@gnScrollSpy",
          watch: "=",
          depth: "@",
          allDepth: "@",
          collapse: "@"
        },
        templateUrl:
          "../../catalog/components/common/scrollspy/partials/" + "scrollspy.html",
        link: function (scope, element, attrs) {
          var counter = 0,
            depth = scope.depth || 2,
            rootElementDepth = 0,
            isInView = gnUtilityService.isInView,
            childrenSearch =
              scope.allDepth == "true"
                ? "fieldset > legend"
                : "fieldset > fieldset > legend";

          scope.scrollTo = gnUtilityService.scrollTo;
          // Ordered list in an array of elements to spy
          scope.spyElems = [];
          scope.isEnabled = false;

          var previousLabel = "";
          var registerSpy = function () {
            var id = $(this).attr("id"),
              currentDepth = $(this).parents("fieldset").length - 1 - rootElementDepth;

            if (currentDepth <= depth) {
              // Get the element id or create an id for the element to spy
              if (!id) {
                id = scope.id + "-" + currentDepth + "-" + counter++;
                $(this).attr("id", id);
              }

              // Spy link configuration
              var spy = {
                id: "#" + id,
                label: $(this).text(),
                elem: $(this).parent(),
                active: false,
                children: [] // May contain children
              };

              // Root element registration
              if (currentDepth === 0) {
                scope.spyElems.push(spy);
              } else {
                // Children registration
                // Skip on fieldset if requested
                var parent =
                  scope.allDepth == "true" ? $(this) : $(this).parent("fieldset");
                var parentFieldsetId = parent
                  .parent("fieldset")
                  .parent("fieldset")
                  .children("legend")
                  .attr("id");
                if (parentFieldsetId) {
                  var parentSpy = $.grep(scope.spyElems, function (spy) {
                    return spy.id === "#" + parentFieldsetId;
                  });

                  // Only register section if not the same
                  // label as the previous one. This may happen
                  // a lot for service metadata record with numbers
                  // of coupledResource or operatesOn elements.
                  // Provide navigation to the first element only.
                  if (previousLabel != spy.label) {
                    // Add the child
                    parentSpy[0] && parentSpy[0].children.push(spy);
                    previousLabel = spy.label;
                  }
                }
              }

              $(this).parent().find(childrenSearch).each(registerSpy);
            }
          };

          scope.toggle = function () {
            scope.isEnabled = !scope.isEnabled;
          };

          // Look for fieldsets and register spy
          var init = function () {
            // Remove current spy elements
            while (scope.spyElems.length) {
              scope.spyElems.pop();
            }

            var rootElement = $("#" + scope.id);

            // Get the number of fieldset above the current element
            // to compute depth later.
            rootElementDepth = rootElement.parents("fieldset").length;
            if (rootElement.prop("tagName") === undefined) {
              console.log(scope.id + " element is not available for scroll spy.");
              return;
            }
            if (rootElement.prop("tagName").toLowerCase() === "fieldset") {
              rootElementDepth++;
            }

            // Spy only first level of fieldsets
            rootElement
              .find("> fieldset > legend, > div > fieldset > legend")
              .each(registerSpy);
            $(window).scroll(function () {
              if (scope.isEnabled) {
                scope.$apply(function () {
                  // Check position of each first and second
                  // level element to spy and activate them
                  // if in the current viewport.
                  angular.forEach(scope.spyElems, function (spy) {
                    spy.active = isInView(spy.elem) ? true : false;
                    spy.children &&
                      angular.forEach(spy.children, function (child) {
                        child.active = isInView(child.elem) ? true : false;
                      });
                  });
                });
              }
            });
          };

          // Watch a model value before trigger spy initialization
          // This is required as the scrollspy need the element
          // to be available in the DOM to be initialized.
          if (scope.watch) {
            scope.$watch(
              "watch",
              function (n, o) {
                // Wait for the template to render
                // FIXME: may not work properly ?
                if (n !== o) {
                  $timeout(function () {
                    init();
                  }, 200);
                }
              },
              true
            );
          } else {
            init();
          }
        }
      };
    }
  ]);
})();
