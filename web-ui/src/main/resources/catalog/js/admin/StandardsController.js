(function() {
  goog.provide('gn_standards_controller');


  var module = angular.module('gn_standards_controller',
      []);


  /**
   * GnStandardsController provides administration tools
   * for standards.
   *
   * TODO: More testing required on add/update action
   *
   */
  module.controller('GnStandardsController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnSearchManagerService, 
            gnUtilityService) {

      $scope.pageMenu = {
        folder: 'standards/',
        defaultTab: 'standards',
        tabs: []
      };

      $scope.schemas = [];

      function loadSchemas() {
        $http.get('admin.schema.list@json').success(function(data) {
          for (var i = 0; i < data.length; i++) {
            $scope.schemas.push({id: data[i]['#text'].trim(), props: data[i]});
          }
          $scope.schemas.sort();
        });
      }

      $scope.addStandard = function(formId, action) {
        $http.get('admin.schema.' + action + '?' + $(formId).serialize())
          .success(function(data) {
              loadSchemas();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('standardAdded'),
                timeout: 2,
                type: 'success'});
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('standardAddError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.removeStandard = function(s) {
        $http.get('admin.schema.remove@json?schema=' + s)
          .success(function(data) {
              if (data['@status'] === 'error') {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate('standardsDeleteError'),
                  msg: data['@message'],
                  timeout: 0,
                  type: 'danger'});
              } else {
                loadSchemas();
              }
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('standardsDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      loadSchemas();

    }]);

})();
