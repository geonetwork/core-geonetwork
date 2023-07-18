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

(function () {
  goog.provide("gn_locale");
  goog.require("gn_cat_controller");

  var module = angular.module("gn_locale", [
    "pascalprecht.translate",
    "angular-md5",
    "gn_cat_controller"
  ]);

  module.constant("$LOCALE_MAP", function (threeCharLang) {
    var specialCases = {
      spa: "es",
      ger: "de",
      bra: "pt_BR",
      swe: "sv",
      tur: "tr",
      por: "pt",
      gre: "el",
      per: "fa",
      chi: "zh",
      pol: "pl",
      wel: "cy",
      dut: "nl",
      ice: "is",
      ita: "it",
      slo: "sk"
    };
    var lang = specialCases[threeCharLang];
    if (angular.isDefined(lang)) {
      return lang;
    }

    return threeCharLang.substring(0, 2) || "en";
  });
  module.constant("$LOCALES", []);

  module.factory("inlineLoaderFactory", [
    "$q",
    function ($q) {
      return function (options) {
        var deferred = $q.defer();
        deferred.resolve(options[options.key]);
        return deferred.promise;
      };
    }
  ]);

  module.factory("localeLoader", [
    "$http",
    "$q",
    "gnLangs",
    "$translate",
    "$timeout",
    function ($http, $q, gnLangs, $translate, $timeout) {
      return function (options) {
        function buildUrl(prefix, lang, value, suffix) {
          if (value.indexOf("/") === 0) {
            return value.substring(1).replace("{{lang}}", gnLangs.getIso2Lang(lang));
          } else if (value.indexOf("|") > -1) {
            /* Allows to configure locales for custom views,
               providing the path and the locale type
               separated by a |:

             module.config(['$LOCALES', function($LOCALES) {
              $LOCALES.push('../../catalog/views/sdi/locales/|search');
             }]);

             */
            var localPrefix = value.split("|")[0];
            var localValue = value.split("|")[1];
            return localPrefix + gnLangs.getIso2Lang(lang) + "-" + localValue + suffix;
          } else {
            return prefix + gnLangs.getIso2Lang(lang) + "-" + value + suffix;
          }
        }
        var allPromises = [];

        angular.forEach(options.locales, function (value, index) {
          /* Check value for:
              1) Relative url to the translation file: use that url.
                  Configured with:

                  module.config(['$LOCALES', function($LOCALES) {
                    $LOCALES.push('../../catalog/locales/en-data.json');
                  }]);

              2) Relative url with locate type (usually loaded from a custom view):
                  use buildUrl to create the url. Configured with:

                  module.config(['$LOCALES', function($LOCALES) {
                    $LOCALES.push('../../catalog/views/myview/locales/|core'');
                  }]);

              3) Non relative url, usually for language packs:
                  use buildUrl to create the url.

                  /../api/i18n/packages/search

          */
          var langUrl =
            value.startsWith("../") && value.indexOf("|") == -1
              ? value
              : buildUrl(options.prefix, options.key, value, options.suffix);

          var deferredInst = $q.defer();
          allPromises.push(deferredInst.promise);

          $http({
            method: "GET",
            url: langUrl,
            headers: {
              "Accept-Language": options.key
            },
            cache: true
          }).then(
            function (response) {
              deferredInst.resolve(response.data);
            },
            function () {
              // Load english locale file if not available
              var url = buildUrl(options.prefix, "en", value, options.suffix);
              $http({
                method: "GET",
                url: url
              }).then(
                function (response) {
                  deferredInst.resolve(response.data);
                },
                function () {
                  deferredInst.resolve({});
                }
              );
            }
          );
        });

        // Finally, create a single promise containing all the promises
        // for each app module:
        var deferred = $q.all(allPromises);

        return deferred;
      };
    }
  ]);

  module.config([
    "$translateProvider",
    "$LOCALES",
    "gnGlobalSettings",
    "gnLangs",
    function ($translateProvider, $LOCALES, gnGlobalSettings, gnLangs) {
      $translateProvider.useLoader("localeLoader", {
        locales: $LOCALES,
        prefix: (gnGlobalSettings.locale.path || "../../") + "catalog/locales/",
        suffix: ".json"
      });

      gnLangs.detectLang(gnGlobalSettings.gnCfg.langDetector, gnGlobalSettings);
      gnLangs.provider = $translateProvider;

      $translateProvider.preferredLanguage(gnGlobalSettings.iso3lang);
      //$translateProvider.useSanitizeValueStrategy('escape');
      $translateProvider.useSanitizeValueStrategy("escapeParameters");
      // $translateProvider.useSanitizeValueStrategy('sanitizeParameters');

      moment.locale(gnGlobalSettings.lang);
    }
  ]);
})();
