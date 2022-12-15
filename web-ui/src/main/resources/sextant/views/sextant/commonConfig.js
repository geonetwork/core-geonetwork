(function () {
  goog.provide("sx_search_sextant_commonconfig");

  var module = angular.module("gn");

  module.run([
    "gnShareConstants",
    function (gnShareConstants) {
      gnShareConstants.columnOrder = [
        "view",
        "dynamic",
        "download",
        "process",
        "editing"
      ];
      gnShareConstants.disableAllCol = true;
      gnShareConstants.displayProfile = true;
    }
  ]);
})();
