(function() {
  goog.provide('gn_new_metadata_controller');

  goog.require('gn_catalog_service');

  var module = angular.module('gn_new_metadata_controller',
      ['gn_catalog_service']);

  /**
   * Metadata editor controller - draft
   */
  module.controller('GnNewMetadataController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    'gnNewMetadata',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnMetadataManagerService, 
            gnSearchManagerService, 
            gnUtilityService,
            gnNewMetadata) {

      gnSearchManagerService.search('q@json?template=y&fast=index').
          then(function(data) {

            $scope.mdList = data;

            var types = [];
            for (var i = 0; i < data.metadata.length; i++) {
              var type = data.metadata[i].type;
              if (type instanceof Array) {
                for (var j = 0; j < type.length; j++) {
                  if (types.indexOf(type[j]) < 0) {
                    types.push(type[j]);
                  }
                }
              }
              else if (types.indexOf(type) < 0 && type) {
                types.push(type);
              }
            }
            $scope.mdTypes = types;
          });

      $scope.getTemplateNamesByType = function(type) {
        var tpls = [];
        for (var i = 0; i < $scope.mdList.metadata.length; i++) {
          var mdType = $scope.mdList.metadata[i].type;
          if (mdType instanceof Array) {
            if (mdType.indexOf(type) >= 0) {
              tpls.push($scope.mdList.metadata[i]);
            }
          }
          else if (mdType == type) {
            tpls.push($scope.mdList.metadata[i]);
          }
        }

        $scope.tpls = tpls;
        $scope.activeType = type;
        $scope.activeTpl = null;
        return false;
      };

      $scope.setActiveTpl = function(tpl) {
        $scope.activeTpl = tpl;
      };

      $scope.createNewMetadata = function() {
        gnNewMetadata.createNewMetadata(4, 3);
      };
    }
  ]);
})();
