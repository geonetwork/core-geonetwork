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
  goog.provide('gn_language_switcher_directive');

  var module = angular.module('gn_language_switcher_directive',
      ['pascalprecht.translate']);

  module.directive('gnLanguageSwitcher', ['$translate', 'gnGlobalSettings', 'gnLangs',
    function($translate, gnGlobalSettings, gnLangs) {

      return {
        restrict: 'A',
        replace: false,
        transclude: true,
        scope: {
          langs: '=',
          langLabels: '=',
          lang: '=gnLanguageSwitcher'
        },
        template:
            '<select class="form-control" ' +
            ' aria-label=' + "{{'languageSwitcher'|translate}}" + '"' +
            ' data-ng-show="isHidden()" ' +
            ' data-ng-model="lang" ' +
            ' data-ng-options="key as langLabels[key] ' +
            ' for (key, value) in langs"/>',
        link: function(scope) {
          scope.$watch('lang', function(value, o) {

            var urlLang = gnLangs.detectLang(
              gnGlobalSettings.gnCfg.langDetector,
              gnGlobalSettings
            );

            if (value !== urlLang) {
              location.href = location.href.replace('\/' + urlLang + '\/', '\/' + value + '\/');


              //if (value !== o) {
              //  url[5] = value;  // Use ISO3 code
              // if (window.history.pushState) {
              //     // Update translate with no page reload
              //     // And adding an history state to update browser URL
              //     $translate.uses(scope.langs[value]); // Use ISO2 code
              //     window.history.pushState(null, null, url.join('/'));
              // } else {
              // trigger a reload
              //location.href = url.join('/');
              // }
              if (moment) {
                moment.locale(scope.langs[value]);
              }
            }
          });

          scope.isHidden = function() {
            return Object.keys(scope.langs).length > 1;
          };
        }
      };
    }]);
})();
