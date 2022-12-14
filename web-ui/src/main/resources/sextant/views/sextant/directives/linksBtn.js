(function () {
  goog.provide("sxt_linksbtn");

  var module = angular.module("sxt_linksbtn", []);

  module.directive("sxtLinksBtn", [
    "gnSearchSettings",
    function (settings) {
      return {
        restrict: "E",
        replace: true,
        scope: true,
        templateUrl: "../../sextant/views/sextant/directives/" + "partials/linksbtn.html",
        link: function (scope) {
          scope.container = scope.container || ".links";
          scope.isMap =
            (settings.mainTabs && settings.mainTabs.map) || settings.viewerUrl;
        }
      };
    }
  ]);
})();
