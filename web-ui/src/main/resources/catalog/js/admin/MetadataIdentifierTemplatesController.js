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
            !confirm($translate('unsavedChangesWarning')))
          event.preventDefault();
      });

      $scope.mdIdentifierTemplates = [];
      $scope.mdIdentifierTemplateSelected = {};

      $scope.selectTemplate = function(template) {
        if ($('.ng-dirty').length > 0 && confirm($translate('doSaveConfirm'))) {
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

        $http.get('metadataIdentifierTemplates' +
            '?_content_type=json&userDefinedOnly=true')
            .success(function(data) {
              $scope.mdIdentifierTemplates = data;
            });

      }

      $scope.addMetadataIdentifierTemplate = function() {
        $scope.mdIdentifierTemplateSelected = {
          'id': '',
          'name': '',
          'template': ''
        };
      };

      $scope.deleteMetadataIdentifierTemplate = function(id) {
        $http.delete($scope.url + 'metadataIdentifierTemplates?id=' + id)
            .success(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              loadMetadataUrnTemplates();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('metadataUrnTemplateDeleted'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('metadataUrnTemplateDeletedError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.saveMetadataIdentifierTemplate = function() {

        var params = {
          id: $scope.mdIdentifierTemplateSelected.id,
          name: $scope.mdIdentifierTemplateSelected.name,
          template: $scope.mdIdentifierTemplateSelected.template
        };

        $http.post($scope.url + 'metadataIdentifierTemplates',
            null, {params: params})
            .success(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              loadMetadataUrnTemplates();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('metadataIdentifierTemplateUpdated'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $('.ng-dirty').removeClass('ng-dirty');
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('metadataIdentifier TemplateUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      loadMetadataUrnTemplates();

    }]);
})();
