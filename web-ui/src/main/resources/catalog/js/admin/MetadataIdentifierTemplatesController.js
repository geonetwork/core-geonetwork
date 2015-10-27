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
