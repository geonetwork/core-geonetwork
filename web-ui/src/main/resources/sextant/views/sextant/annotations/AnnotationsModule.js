(function () {
  goog.provide("sxt_annotations");

  goog.require("sxt_annotations_directive");
  goog.require("sxt_annotations_service");

  angular.module("sxt_annotations", [
    "sxt_annotations_directive",
    "sxt_annotations_service"
  ]);
})();
