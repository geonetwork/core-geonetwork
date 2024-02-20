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
  goog.provide("gn_needhelp_directive");

  var module = angular.module("gn_needhelp_directive", []);

  /**
   * @ngdoc directive
   * @name gn_needhelp_directive.directive:gnNeedHelp
   * @function
   *
   * @description
   * Create a link which open a new window with the requested page.
   * If the page is not found in the configuration, an alert
   * is displayed in the browser console.
   *
   *
   * @param {string} gnNeedHelp The documentation page key to load
   * see helpLinks
   *
   *
   * @param {boolean} iconOnly Optional parameter. Set to true to
   * display only an icon and no label.
   *
   */
  module.directive("gnNeedHelp", [
    "gnGlobalSettings",
    "gnAlertService",
    "gnConfigService",
    "gnConfig",
    "$http",
    "$q",
    "$translate",
    function (
      gnGlobalSettings,
      gnAlertService,
      gnConfigService,
      gnConfig,
      $http,
      $q,
      $translate
    ) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/common/needhelp/partials/" + "needhelp.html",
        link: function (scope, element, attrs) {
          scope.iconOnly = attrs.iconOnly === "true";
          scope.documentationLinks = null;

          scope.helpBaseUrl = "https://docs.geonetwork-opensource.org/latest/{lang}";

          gnConfigService.load().then(function (c) {
            var docUrl = gnConfig["system.documentation.url"];

            if (docUrl) {
              scope.helpBaseUrl = docUrl;
            }
          });

          scope.$watch("documentationLinks", function (n, o) {
            if (n !== o && n != null) {
              scope.checkUrl();
            }
          });

          /**
           * load the JSON file with all the documentation links and put the links in the scope
           */
          var loadManualUrls = function () {
            if (!scope.documentationLinks) {
              $http({
                url: "../../config/manual.json",
                method: "GET",
                cache: true
              }).then(function (response) {
                scope.documentationLinks = response.data;
              });
            }
          };

          /**
           * Check if the URL to the help page is found, if not, hide the `help` button
           *
           * @returns {boolean} the url is found or not
           */
          scope.checkUrl = function () {
            var pageId = attrs.gnNeedHelp;
            if (scope.documentationLinks !== null) {
              var page = scope.documentationLinks[pageId];

              return page !== undefined;
            }
            return false;
          };

          var testAndOpen = function (url) {
            var defer = $q.defer();
            $http.head(url).then(
              function (data) {
                window.open(url, "gn-documentation");
                defer.resolve(data);
              },
              function (data) {
                gnAlertService.addAlert({
                  msg: $translate.instant("docPageNotFoundAtUrl") + " " + url,
                  type: "warning"
                });
                defer.reject(data);
              }
            );
            return defer.promise;
          };

          /**
           * Get the URL of the corresponding help page and open it in a new tab
           * @returns {boolean}
           */
          scope.showHelp = function () {
            var pageId = attrs.gnNeedHelp;
            var page = scope.documentationLinks[pageId];
            var baseUrl;

            if (gnGlobalSettings.lang !== "en") {
              baseUrl = scope.helpBaseUrl.replace("{lang}", gnGlobalSettings.lang);
            } else {
              baseUrl = scope.helpBaseUrl.replace("/{lang}", "");
            }

            var helpPageUrl = baseUrl + "/" + page;

            testAndOpen(helpPageUrl).then(
              function () {},
              function () {
                var baseUrl = scope.helpBaseUrl.replace("/{lang}", "");
                var helpPageUrl = baseUrl + "/" + page;

                testAndOpen(helpPageUrl);
              }
            );
            return true;
          };

          loadManualUrls();
        }
      };
    }
  ]);
})();
