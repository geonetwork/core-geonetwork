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

(function() {
  goog.provide('gn_draggable_directive');

  var module = angular.module('gn_draggable_directive', []);

  /**
   * Directive to make an HTML element draggable.
   *
   * Usage:
   *   <div gn-draggable=".header">
   *     <div class="header"></div>
   *     <div class="content"></div>
   *   </div>
   *
   * You can put a CSS selector in the value of the directive to specify
   * a draggable zone, otherwise the entire element is the draggable zone.
   *
   */
  module.directive('gnDraggable', ['$document',
    function($document) {
      return function(scope, element, attr) {
        var startX = 0, startY = 0, x = null, y = null;

        // Firefox doesn't like transition during drag
        element.addClass('gn-draggable');
        element.css({position: 'absolute'});

        var dragZone = (attr['gnDraggable'] != '') ?
            element.find(attr['gnDraggable']) :
            element;

        if (!dragZone || dragZone.length == 0) {
          dragZone = element;
        }

        dragZone.bind('mousedown', function(evt) {
          var elt = $(evt.target);

          if (x === null) {
            x = element.prop('offsetLeft');
          }

          if (y === null) {
            y = element.prop('offsetTop');
          }

          // preventDefault block user interaction with input field
          if (evt.target.nodeName !== 'INPUT') {
            evt.preventDefault();
          }

          startX = evt.clientX - x;
          startY = evt.clientY - y;
          $document.bind('mousemove', mousemove);
          $document.bind('mouseup', mouseup);
        });

        function mousemove(evt) {
          y = evt.clientY - startY;
          x = evt.clientX - startX;
          element.css({
            margin: 0,
            top: y + 'px',
            left: x + 'px'
          });
        }

        function mouseup() {
          $document.unbind('mousemove', mousemove);
          $document.unbind('mouseup', mousemove);
        }
      }
    }]);
})();
