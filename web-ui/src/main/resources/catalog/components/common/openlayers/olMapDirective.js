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

  goog.provide('gn_olMapDirective')

  var module = angular.module('gn_olMapDirective', [])

  /**
   * Provides a directive used to insert a user-defined OpenLayers
   * map in the DOM. The directive does not create an isolate scope.
   *
   * Example:
   *
   *      <div ol-map="ctrl.map"></div>
   *
   * See our live examples:
   * {@link ../examples/permalink.html}
   * {@link ../examples/simple.html}
   *
   * @htmlAttribute {ol.Map} ol-map The map.
   * @return {angular.Directive} Directive Definition Object.
   * @ngInject
   * @ngdoc directive
   * @ngname olMap
   */
  var mapDirective = function() {
    return {
      restrict: 'A',
      link:
        /**
         * @param {angular.Scope} scope Scope.
         * @param {angular.JQLite} element Element.
         * @param {angular.Attributes} attrs Attributes.
         */
        function(scope, element, attrs) {
          var attr = 'olMap';
          var prop = attrs[attr];

          var map = scope.$eval(prop);
          map.setTarget(element[0]);
        }
    };
  };

  module.directive('olMap', mapDirective)
})()
