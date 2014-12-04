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
