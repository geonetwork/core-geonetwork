(function() {
  goog.provide('sxt_ogclinks_service');

  var module = angular.module('sxt_ogclinks_service', [
  ]);


  module.service('sxtOgcLinksService', [
    '$compile',

    function($compile) {

      /**
       * compile wfs form directive that loads a describe process and generate
       * a form.
       * The given scope must contains the map.
       *
       * @param scope
       * @param element
       * @param {Object} wpsLink
       * @param {Object} options can contain: hideExecuteButton (bool)
       * @param {Object} wfsLink optional WFS link for input overload based
       *  on filter values
       */
      this.wpsForm = function(scope, element, wpsLink, wfsLink, options) {

        scope.wpsLink = wpsLink;
        if (wfsLink) { scope.wfsLink = wfsLink; }

        var el = angular.element('' +
          '<gn-wps-process-form map="map" ' +
          'data-wps-link="wpsLink" ' +
          (options && options.hideExecuteButton ? 'hide-execute-button="true" ' : '') +
          (wfsLink ? 'data-wfs-link="wfsLink" ' : '') +
          '/>');

        $compile(el)(scope);
        element.append(el);
      }

    }]);
})();
