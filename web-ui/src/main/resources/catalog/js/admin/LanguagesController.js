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
      $scope.newKey = '';

      function loadDbTranslations() {
        $http.get('../api/i18n/db/custom').then(function(r) {
          $scope.dbTranslations = r.data;
        });
      };

      $scope.saveTranslations = function(l) {
        $http.put('../api/i18n/db/translations',
          $scope.dbTranslations).then(function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate.instant('translations.saved'),
            timeout: 2,
            type: 'success'});
          loadDbTranslations();
        }, function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate.instant('translations.save.error'),
            error: r.data,
            timeout: 0,
            type: 'danger'});
        });
      };

      $scope.removeAllTranslations = function(l) {
        $http.delete('../api/i18n/db/translations').then(function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate.instant('translations.allremoved'),
            timeout: 2,
            type: 'success'});
          loadDbTranslations();
        }, function(r) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate.instant('translations.allremoval.error'),
            error: r.data,
            timeout: 0,
            type: 'danger'});
        });
      };

      $scope.synchAllTranslations = function() {
        var translationByKey = _.groupBy($scope.dbTranslations, 'fieldName');
        angular.forEach(translationByKey, function (values, key) {
          var translationByLang = _.groupBy(values, 'langId');
          for (var j = 0; j < $scope.dbLanguages.length; j++) {
            // Add missing
            var lang = $scope.dbLanguages[j].id;
            if (!translationByLang[lang]) {
              $scope.dbTranslations.push({
                fieldName: key,
                langId: lang,
                value: ''
              });
            }
          }
        });
      }

      function buildTranslationObject() {
        var translationObject = {};
        for (var i = 0; i < $scope.dbLanguages.length; i ++) {
          translationObject[$scope.dbLanguages[i].id] = '';
        }
        return translationObject;
      }

      $scope.addTranslation = function(newKey) {
        if(newKey != '') {
          $http.put('../api/i18n/db/translations/' + newKey,
            buildTranslationObject()).then(function(r) {
            $rootScope.$broadcast('StatusUpdated', {
              msg: $translate.instant('translations.saved'),
              timeout: 2,
              type: 'success'});
            $scope.newKey = '';
            loadDbTranslations();
          }, function(r) {
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate.instant('translations.save.error'),
              error: r.data,
              timeout: 0,
              type: 'danger'});
          });
        }
      };

      $scope.removeTranslation = function(key) {
        if(key != '') {
          $http.delete('../api/i18n/db/translations/' + key).then(function(r) {
            $rootScope.$broadcast('StatusUpdated', {
              msg: $translate.instant('translations.removed'),
              timeout: 2,
              type: 'success'});
            $scope.newKey = '';
            loadDbTranslations();
          }, function(r) {
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate.instant('translations.removal.error'),
              error: r.data,
              timeout: 0,
              type: 'danger'});
          });
        }
      };
      loadDbTranslations();
    }]);
})();
