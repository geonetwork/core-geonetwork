(function () {
  goog.provide("sxt_social");

  var module = angular.module("sxt_social", []);

  module.directive("sxtSocial", [
    function () {
      return {
        restrict: "E",
        scope: false,
        templateUrl: "../../catalog/views/sextant/directives/" + "partials/social.html"
      };
    }
  ]);
})();
