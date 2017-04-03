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
       */
      this.wpsForm = function(scope, element, wpsLink) {

        scope.wpsLink = wpsLink;
        var el = angular.element('' +
          '<gn-wps-process-form map="map" ' +
          'data-wps-link="wpsLink">' +
          '</gn-wps-process-form>');

        $compile(el)(scope);
        element.append(el);
      }

    }]);
})();
