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
  goog.provide('gn_languages_controller');


  var module = angular.module('gn_languages_controller',
      []);


  module.controller('GnLanguagesController', [
    '$scope', '$http', '$rootScope', '$translate',
    function($scope, $http, $rootScope, $translate) {
      $scope.dbLanguages = [];
      $scope.applicationLanguagesNotAlreadyAvailable = [];

      function loadDbLanguages() {
        $http.get('../api/languages').then(function(r) {
          $scope.dbLanguages = r.data;
          $http.get('../api/languages/application').then(function(r) {
            $scope.applicationLanguagesNotAlreadyAvailable = r.data.filter(function(l) {
              return $scope.dbLanguages.find(function(dbL) {
                return dbL.id === l.id}) === undefined;
            });
          });
        });
      }

      $scope.removeLanguage = function(l) {
        $http.delete('../api/languages/' + l.id).then(function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate.instant('language.removed'),
            timeout: 2,
            type: 'success'});
          loadDbLanguages();
        }, function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate.instant('language.removal.error'),
            error: r.data,
            timeout: 0,
            type: 'danger'});
          loadDbLanguages();
        });
      };
      $scope.addLanguage = function(l) {
        $http.put('../api/languages/' + l.id).then(function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate.instant('language.added'),
            timeout: 2,
            type: 'success'});
          loadDbLanguages();
        }, function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate.instant('language.added.error'),
            error: r.data,
            timeout: 0,
            type: 'danger'});
          loadDbLanguages();
        });
      };
      loadDbLanguages();



      $scope.dbTranslations = [];

      function loadDbTranslations() {
        $http.get('../api/i18n/db/custom').then(function(r) {
          $scope.dbTranslations = r.data;
        });
      };
      loadDbTranslations();
    }]);
})();
