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
       * Possible option: hideExecuteButton
       *
       * @param scope
       * @param element
       * @param {Object} wpsLink
       * @param options
       */
      this.wpsForm = function(scope, element, wpsLink, options) {

        scope.wpsLink = wpsLink;
        var el = angular.element('' +
          '<gn-wps-process-form map="map" ' +
          (options && options.hideExecuteButton ? 'hide-execute-button="true" ' : '') +
          'data-wps-link="wpsLink">' +
          '</gn-wps-process-form>');

        $compile(el)(scope);
        element.append(el);
      }

    }]);
})();
