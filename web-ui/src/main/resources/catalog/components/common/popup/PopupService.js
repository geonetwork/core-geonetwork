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
  goog.provide('gn_popup_service');

  goog.require('gn_draggable_directive');

  var module = angular.module('gn_popup_service', [
    'gn_draggable_directive'
  ]);

  module.factory('gnPopup', [
    '$compile',
    '$rootScope',
    '$sce',
    function($compile, $rootScope, $sce) {

      var Popup = function(options, scope) {

        // Create the popup element with its content to the HTML page
        var element = angular.element(
            '<div gn-popup="toggle" ' +
            'gn-popup-options="options" ' +
            'gn-draggable=".gn-popup-title">' +
            options.content +
            '</div>'
            );

        if (options.className) {
          element.addClass(options.className);
        }

        // Pass some popup functions for clients to be used in content
        var popup = this;
        options.open = function() {popup.open();};
        options.close = function() {popup.close();};
        options.destroy = function() {popup.destroy();};

        // Create scope, compile and link
        this.scope = (scope || $rootScope).$new();
        this.scope.options = options;
        this.scope.toggle = true;

        if (options.url) {
          this.scope.options.url = $sce.trustAsResourceUrl(options.url);
        }
        this.element = $compile(element)(this.scope);
        this.destroyed = false;

        // Attach popup to body element
        var target = options.target || document.body;
        $(target).append(this.element);
      };

      Popup.prototype.open = function(scope) {
        // Show the popup
        this.element.show();
      };

      Popup.prototype.close = function() {
        this.element.hide();

        var onCloseCallback = this.scope.options.onCloseCallback;
        if (angular.isFunction(onCloseCallback)) {
          onCloseCallback(this);
        }
        var destroyOnClose = this.scope.options.destroyOnClose;
        if (destroyOnClose !== false) {
          this.destroy();
        }
      };

      Popup.prototype.destroy = function() {
        this.scope.$destroy();
        this.scope = null;
        this.element.remove();
        this.element = null;
        this.destroyed = true;
      };

      var Modal = function(options, scope) {
        var element = angular.element('' +
            '<div class="modal fade in ' + (options.class || '') + '">' +
            '  <div class="modal-dialog in">' +
            '    <div class="modal-content">' +
            '      <div class="modal-header">' +
            '        <button type="button" class="close" ' +
            '                data-dismiss="modal">' +
            '          &times;</button>' +
            '        <h5 class="modal-title" translate>' +
            '          <span>' + options.title + '</span></h5>' +
            '      </div>' +
            '      <div class="modal-body">' + options.content + '</div>' +
            '    </div>' +
            '  </div>' +
            '</div>');

        var newScope = scope || $rootScope.$new(),
            scopeProvided = angular.isDefined(scope);
        element = $compile(element)(newScope);

        $(document.body).append(element);
        element.modal();
        element.on('hidden.bs.modal', function() {
          $(document.body).removeClass('modal-open');
          element.modal('hide');
          $('body > .modal-backdrop').remove();
          element.remove();
          if (!scopeProvided) {
            newScope.$destroy();
          }

          if (options.onCloseCallback) {
            options.onCloseCallback();
          }
        });
        return element;
      };
      return {
        create: function(options, scope) {
          return new Popup(options, scope);
        },
        createModal: function(options, scope) {
          return new Modal(options, scope);
        }
      };
    }]);
})();
