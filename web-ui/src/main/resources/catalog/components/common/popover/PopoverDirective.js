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
  goog.provide('gn_popover_directive');

  var module = angular.module('gn_popover_directive', []);

  /**
   * Provides a directive used to display a Bootstrap popover.
   *
   *<div gn-popover>
   *  <a gn-popover-anchor class="btn btn-info">anchor 1</a>
   *  <div gn-popover-content>
   *    <ul>
   *      <li>action 1:
   *        <input type="range"/>
   *      </li>
   *    </ul>
   *  </div>
   *</div>
   * @ngdoc directive
   * @ngInject
   * @ngname gnPopover
   * @return {angular.Directive} The Directive Definition Object.
   */
  module.directive('gnPopover', [function() {
    return {
      restrict: 'A',
      scope: true,
      controller: 'GnPopoverController',
      controllerAs : 'popoverCtrl',
      link: function(scope, elem, attrs, gnPopoverCtrl) {
        gnPopoverCtrl.anchorElm.on('hidden.bs.popover', function() {
          /**
           * @type {{inState : Object}}
           */
          var popover = gnPopoverCtrl.anchorElm.data('bs.popover');
          popover['inState'].click = false;
        });

        gnPopoverCtrl.anchorElm.on('inserted.bs.popover', function() {
          gnPopoverCtrl.bodyElm.show();
          gnPopoverCtrl.shown = true;
        });

        gnPopoverCtrl.anchorElm.popover({
          container: 'body',
          html: true,
          content: gnPopoverCtrl.bodyElm,
          placement : attrs['gnPopoverPlacement'] || 'right'
        });

        if (attrs['gnPopoverDismiss']) {
          $(attrs['gnPopoverDismiss']).on('scroll', function() {
            gnPopoverCtrl.dismissPopover();
          });
        }

        scope.$on('$destroy', function() {
          gnPopoverCtrl.anchorElm.popover('destroy');
          gnPopoverCtrl.anchorElm.unbind('inserted.bs.popover');
          gnPopoverCtrl.anchorElm.unbind('hidden.bs.popover');
        });
      }
    };
  }]);

  /**
   * @ngdoc directive
   * @ngInject
   * @ngname gnPopoverAnchor
   * @return {angular.Directive} The Directive Definition Object
   */
  module.directive('gnPopoverAnchor', [function() {
    return {
      restrict: 'A',
      require: '^^gnPopover',
      link: function(scope, elem, attrs, gnPopoverCtrl) {
        gnPopoverCtrl.anchorElm = elem;
      }
    };
  }]);

  /**
   * @ngdoc directive
   * @ngInject
   * @ngname gnPopoverContent
   * @return {angular.Directive} The Directive Definition Object
   */
  module.directive('gnPopoverContent', [function() {
    return {
      restrict: 'A',
      require: '^^gnPopover',
      link : function(scope, elem, attrs, gnPopoverCtrl) {
        gnPopoverCtrl.bodyElm = elem;
        elem.hide();
      }
    };
  }]);

  /**
   * The controller for the 'popover' directive.
   * @constructor
   * @struct
   * @ngInject
   * @export
   * @ngdoc controller
   * @ngname GnPopoverController
   * @param {angular.Scope} $scope Scope.
   */
  var PopoverController = function($scope) {
    /**
     * The state of the popover (displayed or not)
     * @type {boolean}
     * @export
     */
    this.shown = false;

    /**
     * @type {angular.JQLite|undefined}
     * @export
     */
    this.anchorElm = undefined;

    /**
     * @type {angular.JQLite|undefined}
     * @export
     */
    this.bodyElm = undefined;

    function onMouseDown(clickEvent) {
      if (this.anchorElm[0] !== clickEvent.target &&
        this.bodyElm.parent()[0] !== clickEvent.target &
        this.bodyElm.parent().find(clickEvent.target).length === 0 && this.shown) {
        this.dismissPopover();
      }
    }

    angular.element('body').on('mousedown', onMouseDown.bind(this));

    $scope.$on('$destroy', function() {
      angular.element('body').off('mousedown', onMouseDown);
    });

  };

  PopoverController['$inject'] = [
    '$scope'
  ];


  /**
   * Dissmiss popover function
   * @export
   */
  PopoverController.prototype.dismissPopover = function() {
    this.shown = false;
    this.anchorElm.popover('hide');
  };

  module.controller('GnPopoverController', PopoverController);

})();
