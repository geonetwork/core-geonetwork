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
  goog.provide('cookie_warning_directive');

  var module = angular.module('cookie_warning_directive', ['ngCookies']);

  module
      .directive(
          'cookiewarning', ['$window', '$cookies', 'gnGlobalSettings',
            function($window, $cookies, gnGlobalSettings) {
            return {
              restrict: 'AE',
              replace: true,
              scope: {},
              templateUrl:
              '../../catalog/components/cookieWarning/partials/cookieWarning.html',
              link: function(scope, element, attrs, ctrl) {
                scope.cookieWarningEnabledInSettings = gnGlobalSettings.gnCfg.mods.cookieWarning.enabled;
                scope.cookieWarningMoreInfoLink = gnGlobalSettings.gnCfg.mods.cookieWarning.cookieWarningMoreInfoLink;
                scope.cookieWarningRejectLink = gnGlobalSettings.gnCfg.mods.cookieWarning.cookieWarningRejectLink;

                scope.showCookieWarning = function() {
                  return scope.cookieWarningEnabledInSettings
                    && $window.localStorage.getItem('cookiesAccepted') !== 'true';
                };

                scope.acceptCookies = function() {
                  $window.localStorage.setItem('cookiesAccepted', 'true');
                };

                scope.goAway = function() {
                  $window.localStorage.removeItem('cookiesAccepted');

                  angular.forEach($cookies.getAll(), function(value, key) {
                    if (key.indexOf('NAV-') > -1) {
                      $window.sessionStorage.setItem(key, value);
                      $cookies.remove(key);
                    }
                  });

                  if (scope.cookieWarningRejectLink && scope.cookieWarningRejectLink.trim().length !== 0) {
                    $window.location.href = scope.cookieWarningRejectLink;
                  } else {
                    $window.history.back();
                  }
                }
              }
            };
          }]);

})();
