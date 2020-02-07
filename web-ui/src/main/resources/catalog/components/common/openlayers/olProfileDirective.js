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

  goog.provide('gn_olProfileDirective')

  goog.require('gn_olProfileService');

  var module = angular.module('gn_olProfileDirective', ['gn_olProfileService'])

  /**
   * Provides a directive used to insert an elevation profile chart
   * in the DOM.
   *
   * Example:
   *
   *      <div ol-profile="ctrl.profileData"
   *        ol-profile-options="ctrl.profileOptions"
   *        ol-profile-pois="ctrl.profilePois">
   *      </div>
   *
   * Where "ctrl.profileOptions" is of type {@link ngeox.profile.ProfileOptions};
   * "ctrl.profileData" and "ctrl.profilePois" are arrays which will be
   * processed by {@link ngeox.profile.ElevationExtractor} and
   * {@link ngeox.profile.PoiExtractor}.
   *
   * See our live example: {@link ../examples/profile.html}
   *
   * @htmlAttribute {?Object} ol-profile The profile data.
   * @htmlAttribute {ngeox.profile.ProfileOptions} ol-profile-options The options.
   * @htmlAttribute {?Array} ol-profile-pois The data for POIs.
   * @htmlAttribute {*} ol-profile-highlight Any property on the scope which
   * evaluated value may correspond to distance from origin.
   * @param {Debounce} gnDebounce Debounce service.
   * @return {angular.Directive} Directive Definition Object.
   * @ngInject
   * @ngdoc directive
   * @ngname olProfile
   */
  var profileDirective = ['gnDebounce', 'olProfileService', function(gnDebounce, olProfileService) {
    return {
      restrict: 'A',
      link:
        /**
         * @param {angular.Scope} scope Scope.
         * @param {angular.JQLite} element Element.
         * @param {angular.Attributes} attrs Attributes.
         */
        function(scope, element, attrs) {

          var optionsAttr = attrs['olProfileOptions'];

          var selection = d3.select(element[0]);
          var profile, elevationData, poiData;

          scope.$watchCollection(optionsAttr, function(newVal) {

            var options = goog.cloneObject(newVal);

            if (options !== undefined) {

              // proxy the hoverCallback and outCallbackin order to be able to
              // call $applyAsync
              //
              // We're using $applyAsync here because the callback may be
              // called inside the Angular context. For example, it's the case
              // when the user hover's the line geometry on the map and the
              // profileHighlight property is changed.
              //
              // For that reason we use $applyAsync instead of $apply here.

              if (options.hoverCallback !== undefined) {
                var origHoverCallback = options.hoverCallback;
                options.hoverCallback = function() {
                  origHoverCallback.apply(null, arguments);
                  scope.$applyAsync();
                };
              }

              if (options.outCallback !== undefined) {
                var origOutCallback = options.outCallback;
                options.outCallback = function() {
                  origOutCallback();
                  scope.$applyAsync();
                };
              }

              profile = olProfileService(options);
              refreshData();
            }
          });

          scope.$watch(attrs['olProfile'], function(newVal, oldVal) {
            elevationData = newVal;
            refreshData();
          });

          scope.$watch(attrs['olProfilePois'], function(newVal, oldVal) {
            poiData = newVal;
            refreshData();
          });

          scope.$watch(attrs['olProfileHighlight'],
            function(newVal, oldVal) {
              if (newVal === undefined) {
                return;
              }
              if (newVal > 0) {
                profile.highlight(newVal);
              } else {
                profile.clearHighlight();
              }
            });

          goog.events.listen(window, goog.events.EventType.RESIZE,
            gnDebounce(refreshData, 50, true),
            false, this);

          function refreshData() {
            if (profile !== undefined) {
              selection.datum(elevationData).call(profile);
              if (elevationData !== undefined) {
                profile.showPois(poiData);
              }
            }
          }
        }
    };
  }];
  module.directive('olProfile', profileDirective);
})()
