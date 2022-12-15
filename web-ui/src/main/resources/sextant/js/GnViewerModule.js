(function () {
  goog.provide("sx_viewer_module");

  goog.require("sx_draggable_directive");
  goog.require("sx_module");
  goog.require("sx_ows");
  goog.require("sx_popup");

  var module = angular.module("gn_viewer", [
    "gn_module",
    "gn_popup",
    "gn_draggable_directive",
    "gn_ows"
  ]);

  // Define the translation files to load
  module.constant("$LOCALES", ["core", "editor"]);

  module.config([
    "$translateProvider",
    "$LOCALES",
    "$LOCALE_MAP",
    function ($translateProvider, $LOCALES, $LOCALE_MAP) {
      $translateProvider.useLoader("localeLoader", {
        locales: $LOCALES,
        prefix: "../../catalog/locales/",
        suffix: ".json"
      });

      var lang = $LOCALE_MAP(location.href.split("/")[5]);
      $translateProvider.preferredLanguage(lang);
      moment.locale(lang);
    }
  ]);
})();
