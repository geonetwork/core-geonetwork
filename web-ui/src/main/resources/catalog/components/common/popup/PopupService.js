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
            '<div class="modal fade in">' +
            '<div class="modal-dialog in">' +
            '  <div class="modal-content">' +
            '    <div class="modal-header">' +
            '      <button type="button" class="close" data-dismiss="modal">' +
            '        &times;</button>' +
            '      <h5 class="modal-title" translate>' +
            '        <span>' + options.title + '</span></h5>' +
            '      </div>' +
            '    <div class="modal-body">' + options.content + '</div>' +
            '  </div>' +
            '</div>' +
            '</div>');

        $(document.body).append(element);
        element.modal();
        element.on('hidden.bs.modal', function() {
          element.remove();
        });
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
