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
  goog.provide('gn_button_directive');

  var module = angular.module('gn_button_directive', []);

  /**
   * Provides two directives: gn-btn-group and gn-btn.
   *
   * The gn-btn-group directive allows creating "toggle" groups. It works with
   * the gn-btn directive.
   *
   * Example:
   *
   *     <div gn-btn-group>
   *       <button gn-btn class="btn" ng-model="ctrl.drawPoint.active"></button>
   *       <button gn-btn class="btn" ng-model="ctrl.drawLine.active"></button>
   *     </div>
   *
   * In that example the gn-btn are combined together in a "toggle group",
   * where activating a button will deactivate the others.
   *
   * One can use `ng-model` directive at the group level in order to know if
   * a button is active.
   *
   * Example:
   *
   *     <div gn-btn-group gn-btn-group-active="ctrl.drawToolActive">
   *
   * See our live example: {@link ../examples/interactionbtngroup.html}
   *
   * @htmlAttribute {*} gn-btn-group-active Any property of the scope.
   * Tells whether at least one button of the group is active.
   * @param {angular.$parse} $parse Angular parse service.
   * @return {angular.Directive} The directive specs.
   * @ngInject
   * @ngdoc directive
   * @ngname gnBtnGroup
   */
  module.directive('gnBtnGroup', ['$parse', function($parse) {
    return {
      restrict: 'A',
      controller: 'gnBtnGroupController',
      link:
        /**
         * @param {!angular.Scope} scope Scope.
         * @param {angular.JQLite} element Element.
         * @param {angular.Attributes} attrs Attributes.
         * @param {!Object} controller Controller.
         */
        function(scope, element, attrs, controller) {
          var setActive = $parse(attrs['gnBtnGroupActive']).assign;

          if (setActive) {
            scope.$watch(function() {
              // return true if at least one button is active otherwise false
              return controller.buttons_.some(function(buttonModel) {
                return (buttonModel(scope) === true);
              });
            }, function(newValue) {
              setActive(scope, newValue);
            });
          }
        }
    };
  }]);


  /**
   * @param {!angular.Scope} $scope Scope.
   * @constructor
   * @struct
   * @ngInject
   * @ngdoc controller
   * @ngname gnBtnGroupController
   */
  var BtnGroupController = function($scope) {
    /**
     * @type {Array.<angular.$parse.Expression>}
     * @private
     */
    this.buttons_ = [];

    /**
     * @type {!angular.Scope}
     * @private
     */
    this.scope_ = $scope;
  };


  /**
   * @param {number} index Index of the button in buttons array.
   */
  BtnGroupController.prototype.activate = function(index) {
    this.buttons_.forEach(function(expressionFn, i) {
      if (i != index) {
        expressionFn.assign(this.scope_, false);
      }
    }, this);
  };


  /**
   * @param {angular.$parse.Expression} expressionFn Expression function.
   * @return {number} Index of the pushed setter.
   */
  BtnGroupController.prototype.addButton = function(expressionFn) {
    this.buttons_.push(expressionFn);
    return this.buttons_.length - 1;
  };

  BtnGroupController['$inject'] = [
    '$scope'
  ];

  module.controller('gnBtnGroupController', BtnGroupController);


  /**
   * The gn-btn allows creating toggle buttons working with ng-model. It is
   * typically used with Bootstrap buttons (`btn`).
   *
   * Example:
   *
   *     <button gn-btn class="btn" ng-model="ctrl.interaction.active"></button>
   *
   * This example is about creating a Bootstrap button that can pressed/depressed
   * to activate/deactivate an OpenLayers 3 interaction.
   *
   * @htmlAttribute {*} ng-model Any property on the scope. Ideally a boolean.
   * @param {angular.$parse} $parse Angular parse service.
   * @return {angular.Directive} The directive specs.
   * @ngInject
   * @ngdoc directive
   * @ngname gnBtn
   */
  module.directive('gnBtn', ['$parse', function($parse) {
    return {
      require: ['?^gnBtnGroup', 'ngModel'],
      restrict: 'A',
      link:
        /**
         * @param {!angular.Scope} scope Scope.
         * @param {angular.JQLite} element Element.
         * @param {angular.Attributes} attrs Attributes.
         * @param {!Array.<!Object>} ctrls Controllers.
         */
        function(scope, element, attrs, ctrls) {
          var buttonsCtrl = ctrls[0];
          var ngModelCtrl = ctrls[1];
          var indexInGroup = -1;

          var ngModelGet = $parse(attrs['ngModel']);
          var ngModelSet = ngModelGet.assign;

          // Set ng-model value to false if undefined
          if (ngModelGet(scope) === undefined) {
            ngModelSet(scope, false);
          }
          if (buttonsCtrl !== null) {
            indexInGroup = buttonsCtrl.addButton(ngModelGet);
          }

          // UI -> model
          element.bind('click', function() {
            scope.$apply(function() {
              ngModelCtrl.$setViewValue(!ngModelCtrl.$viewValue);
              ngModelCtrl.$render();
            });
          });

          // model -> UI
          ngModelCtrl.$render = function() {
            if (ngModelCtrl.$viewValue && buttonsCtrl !== null) {
              buttonsCtrl.activate(indexInGroup);
            }
            element.toggleClass('active', ngModelCtrl.$viewValue);
          };
        }
    };
  }]);
})();
