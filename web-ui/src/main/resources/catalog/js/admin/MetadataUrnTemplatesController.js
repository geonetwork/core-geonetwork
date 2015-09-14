(function() {
  goog.provide('gn_metadata_urn_templates_controller');


  var module = angular.module('gn_metadata_urn_templates_controller',
      []);

  /**
   * GnMetadataUrnController provides management interface
   * for metadata .
   *
   */
  module.controller('GnMetadataUrnTemplatesController', [
    '$scope', '$http', '$rootScope', '$translate',
    function($scope, $http, $rootScope, $translate) {

      $scope.$on('$locationChangeStart', function(event) {
        if ($('.ng-dirty').length > 0 &&
            !confirm($translate('unsavedChangesWarning')))
          event.preventDefault();
      });

      $scope.urnTemplates = [];
      $scope.urnTemplateSelected = {};

      $scope.selectTemplate = function(urnTemplate) {
        if ($('.ng-dirty').length > 0 && confirm($translate('doSaveConfirm'))) {
          $scope.saveMetadataUrnTemplate(false);
        }
        $scope.urnTemplateSelected = urnTemplate;
        $('.ng-dirty').removeClass('ng-dirty');

      };

      /**
         * Load catalog settings as a flat list and
         * extract firs and second level sections.
         *
         * Form field name is also based on settings
         * key replacing "/" by "." (to not create invalid
         * element name in XML Jeeves request element).
         */
      function loadUrnTemplates() {
        $scope.urnTemplateSelected = {};

        $http.get('urntemplate?_content_type=json&userDefinedOnly=true')
          .success(function(data) {
              $scope.urnTemplates = data;
            });

      }

      $scope.addMetadataUrnTemplate = function() {
        $scope.urnTemplateSelected = {
          'id': '',
          'name': '',
          'template': ''
        };
      };

      $scope.deleteMetadataUrnTemplate = function(id) {
        $http.delete($scope.url + 'urntemplate?id=' + id)
          .success(function(data) {
            $('.ng-dirty').removeClass('ng-dirty');
            loadUrnTemplates();
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

      $scope.saveMetadataUrnTemplate = function() {

        var params = {
          id: $scope.urnTemplateSelected.id,
          name: $scope.urnTemplateSelected.name,
          template: $scope.urnTemplateSelected.template
        };

        $http.post($scope.url + 'urntemplate', null, {params: params})
          .success(function(data) {
            $('.ng-dirty').removeClass('ng-dirty');
            loadUrnTemplates();
            $rootScope.$broadcast('StatusUpdated', {
              msg: $translate('metadataUrnTemplateUpdated'),
              timeout: 2,
              type: 'success'});
          })
          .error(function(data) {
            $('.ng-dirty').removeClass('ng-dirty');
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate('metadataUrnTemplateUpdateError'),
              error: data,
              timeout: 0,
              type: 'danger'});
          });
      };

      loadUrnTemplates();

    }]);

})();
