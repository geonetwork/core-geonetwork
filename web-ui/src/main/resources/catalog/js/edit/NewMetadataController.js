(function() {
  goog.provide('gn_new_metadata_controller');

  goog.require('gn_catalog_service');

  var module = angular.module('gn_new_metadata_controller',
      ['gn_catalog_service']);

  /**
   * Controller to create new metadata record.
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

      $scope.isTemplate = 'n';

      // A map of icon to use for each types
      var icons = {
        featureCatalog: 'fa-table',
        service: 'fa-cog',
        map: 'fa-globe',
        staticMap: 'fa-globe',
        dataset: 'fa-file'
      };

      // List of record type to not take into account
      // Could be avoided if a new index field is created FIXME ?
      var dataTypesToExclude = ['staticMap'];
      var defaultType = 'dataset';

      $scope.getTypeIcon = function(type) {
        return icons[type] || 'fa-square-o';
      };

      var init = function() {
        if ($routeParams.id) {
          gnNewMetadata.createNewMetadata(
              $routeParams.id,
              $routeParams.group,
              $routeParams.template);
        } else {

          gnSearchManagerService.search('qi@json?template=y&fast=index').
              then(function(data) {

                $scope.mdList = data;

                var types = [];
                for (var i = 0; i < data.metadata.length; i++) {
                  var type = data.metadata[i].type;
                  if (type instanceof Array) {
                    for (var j = 0; j < type.length; j++) {
                      if (!$.inArray(type[j], dataTypesToExclude) &&
                          $.inArray(type[j], types)) {
                        types.push(type[j]);
                      }
                    }
                  }
                  else if (types.indexOf(type) < 0 && type) {
                    types.push(type);
                  }
                }
                types.sort();
                $scope.mdTypes = types;

                // Select the default one or the first one
                if (defaultType && $.inArray(defaultType, $scope.mdTypes)) {
                  $scope.getTemplateNamesByType(dataset);
                } else if ($scope.mdTypes[0]) {
                  $scope.getTemplateNamesByType($scope.mdTypes[0]);
                } else {
                  // No templates available ?
                }
              });
        }
      };

      $scope.getTemplateNamesByType = function(type) {
        var tpls = [];
        for (var i = 0; i < $scope.mdList.metadata.length; i++) {
          var mdType = $scope.mdList.metadata[i].type;
          if (mdType instanceof Array) {
            if (mdType.indexOf(type) >= 0) {
              tpls.push($scope.mdList.metadata[i]);
            }
          } else if (mdType == type) {
            tpls.push($scope.mdList.metadata[i]);
          }
        }

        // TODO: Take in account template sort order ?
        function compare(a, b) {
          if (a.title < b.title)
            return -1;
          if (a.title > b.title)
            return 1;
          return 0;
        }
        tpls.sort(compare);

        $scope.tpls = tpls;
        $scope.activeType = type;
        $scope.activeTpl = null;
        return false;
      };

      $scope.setActiveTpl = function(tpl) {
        $scope.activeTpl = tpl;
      };

      $scope.createNewMetadata = function() {
        gnNewMetadata.createNewMetadata(
            $scope.activeTpl['geonet:info'].id,
            $scope.ownerGroup,
            $scope.isTemplate);
      };

      init();
    }
  ]);
})();
