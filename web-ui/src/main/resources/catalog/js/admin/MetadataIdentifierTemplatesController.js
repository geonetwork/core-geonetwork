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
  goog.provide('gn_metadata_identifier_templates_controller');


  var module = angular.module('gn_metadata_identifier_templates_controller',
      []);

  /**
   * GnMetadataIdentifierTemplatesController provides management interface
   * for metadata identifier templates.
   *
   */
  module.controller('GnMetadataIdentifierTemplatesController', [
    '$scope', '$http', '$rootScope', '$translate',

    function($scope, $http, $rootScope, $translate) {

      $scope.$on('$locationChangeStart', function(event) {
        if ($('.ng-dirty').length > 0 &&
            !confirm($translate.instant('unsavedChangesWarning')))
          event.preventDefault();
      });

      $scope.mdIdentifierTemplates = [];
      $scope.mdIdentifierTemplateSelected = {};

      $scope.selectTemplate = function(template) {
        if ($('.ng-dirty').length > 0 &&
            confirm($translate.instant('doSaveConfirm'))) {
          $scope.saveMetadataIdentifierTemplate(false);
        }
        $scope.mdIdentifierTemplateSelected = template;
        $('.ng-dirty').removeClass('ng-dirty');

      };

      /**
       * Load metadata identifier templates into an array.
       *
       */
      function loadMetadataUrnTemplates() {
        $scope.mdIdentifierTemplateSelected = {};

        $http.get('../api/identifiers?userDefinedOnly=true')
            .success(function(data) {
              $scope.mdIdentifierTemplates = data;
            });

      }

      $scope.addMetadataIdentifierTemplate = function() {
        $scope.mdIdentifierTemplateSelected = {
          'id': '-99',
          'name': '',
          'template': ''
        };
      };

      $scope.deleteMetadataIdentifierTemplate = function(id) {
        $http.delete('../api/identifiers/' + id)
            .success(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              loadMetadataUrnTemplates();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('metadataUrnTemplateDeleted'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('metadataUrnTemplateDeletedError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.saveMetadataIdentifierTemplate = function() {

        $http.put('../api/identifiers' + (
            $scope.mdIdentifierTemplateSelected.id !== '-99' ?
            '/' + $scope.mdIdentifierTemplateSelected.id : ''
            ),
            $scope.mdIdentifierTemplateSelected)
            .success(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              loadMetadataUrnTemplates();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('metadataIdentifierTemplateUpdated'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant(
                    'metadataIdentifier TemplateUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      loadMetadataUrnTemplates();

    }]);
})();
