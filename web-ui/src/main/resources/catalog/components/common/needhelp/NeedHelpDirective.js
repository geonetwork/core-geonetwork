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

(function() {
  goog.provide('gn_needhelp_directive');

  var module = angular.module('gn_needhelp_directive', []);

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
  module.directive('gnNeedHelp', ['gnGlobalSettings', 'gnAlertService', '$http', '$q', '$translate',
    function(gnGlobalSettings, gnAlertService, $http, $q, $translate) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/common/needhelp/partials/' +
            'needhelp.html',
        link: function(scope, element, attrs) {
          scope.iconOnly = attrs.iconOnly === 'true';
          var helpBaseUrl = gnGlobalSettings.docUrl ||
              'https://geonetwork-opensource.org/manuals/trunk/';

          var testAndOpen = function(url) {
            var defer = $q.defer();
            $http.head(url).then(function(data) {
              window.open(url, 'gn-documentation');
              defer.resolve(data);
            }, function(data) {
              gnAlertService.addAlert({
                msg: $translate.instant('docPageNotFoundAtUrl') + ' ' + url,
                type: 'warning'
              });
              defer.reject(data);
            });
            return defer.promise;
          };

          scope.showHelp = function() {
            var page = attrs.gnNeedHelp;
            var helpPageUrl = helpBaseUrl + gnGlobalSettings.lang + '/' + page;
            testAndOpen(helpPageUrl).then(function() {}, function() {
              testAndOpen( helpBaseUrl + 'en/' + page)
            });
            return true;
          };
        }
      };
    }]);
})();
