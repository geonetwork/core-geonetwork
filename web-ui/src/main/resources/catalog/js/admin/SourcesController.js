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
  goog.provide('gn_sources_controller');


  var module = angular.module('gn_sources_controller',
      []);

  /**
   * GnSystemSettingsController provides management interface
   * for catalog settings.
   *
   * TODO:
   *  * Add custom forms for some settings (eg. contact for CSW,
   *  Metadata views > default views, Search only in requested language)
   */
  module.controller('GnSourcesController', [
    '$scope', '$http', '$rootScope', '$translate',
    function($scope, $http, $rootScope, $translate) {

      $scope.$on('$locationChangeStart', function(event) {
        if ($('.ng-dirty').length > 0 &&
            !confirm($translate('unsavedChangesWarning')))
          event.preventDefault();
      });

      $scope.sources = [];
      $scope.sourcesSelected = null;
      $scope.dirty = false;
      $http.get('info?_content_type=json&type=languages', {cache: true}).
          success(function(data) {
            $scope.languages = data.language;
          });

      $scope.selectSource = function(source) {
        if ($('.ng-dirty').length > 0 && confirm($translate('doSaveConfirm'))) {
          $scope.saveSources(false);
        }
        $scope.sourcesSelected = source;
        $('.ng-dirty').removeClass('ng-dirty');
        angular.forEach($scope.languages, function(lang) {
          if (source.label[lang.id] === undefined) {
            source.label[lang.id] = source.name;
          }
        });
      };
      /**
         * Load catalog settings as a flat list and
         * extract firs and second level sections.
         *
         * Form field name is also based on settings
         * key replacing "/" by "." (to not create invalid
         * element name in XML Jeeves request element).
         */
      function loadSources() {

        $http.get('info?type=sources&_content_type=json')
            .success(function(data) {
              $scope.sources = data.sources;
            });

      }

      /**
         * Save the form containing all settings. When saved,
         * broadcast success status and reload catalog info.
         */
      $scope.saveSources = function() {
        var formId = '#source' + $scope.sourcesSelected.name;
        var data = [];
        for (var l in $scope.sourcesSelected.label) {
          if ($scope.sourcesSelected.label.hasOwnProperty(l)) {
            data.push('translations-' + l + '=' +
                $scope.sourcesSelected.label[l]);
          }
        }

        $http.post($scope.url + 'source/' +
            encodeURIComponent($scope.sourcesSelected.uuid),
            data.join('&'), {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            })
            .success(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('settingsUpdated'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate('settingsUpdateError'),
                    error: data,
                    timeout: 0,
                    type: 'danger'});
                });
      };
      loadSources();

    }]);

})();
