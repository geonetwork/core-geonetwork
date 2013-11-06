(function() {
  goog.provide('gn_new_metadata_controller');


  var module = angular.module('gn_new_metadata_controller',
      []);

  var tplFolder = '../../catalog/templates/editor/';

  /**
   * Metadata editor controller - draft
   */
  module.controller('GnNewMetadataController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnMetadataManagerService, 
            gnSearchManagerService, 
            gnUtilityService) {
      
      gnSearchManagerService.search('q@json?template=y&fast=index').then(function(data) {
        $scope.mdList = data;
        
        var types = [];
        for(var i=0;i<data.metadata.length;i++) {
          if(data.metadata[i].type instanceof Array) {
            for(var j=0;j<data.metadata[i].type.length;j++) {
              if(types.indexOf(data.metadata[i].type[j]) < 0) {
                types.push(data.metadata[i].type[j]);
              }
            }
          }
          else if(types.indexOf(data.metadata[i].type) < 0) {
            types.push(data.metadata[i].type);
          }
        }
        //TODO : extract types from result
        $scope.mdTypes = types;
      });

      $scope.getTemplateNamesByType = function(type) {
        var names = [];
        for(var i=0;i<$scope.mdList.metadata.length;i++) {
          if($scope.mdList.metadata[i].type == type) {
            names.push($scope.mdList.metadata[i].title);
          }
        }
        //TODO : extract types from result
        $scope.tplNames = names;
      };
    }
  ]);
  
  module.directive('groupsCombo', 
    function($http) {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/templates/utils/groupsCombo.html',
      controller: function($scope, $translate) {
        $http.get('admin.group.list@json').success(function(data) {
          $scope.groups = data !== 'null' ? data : null;
      });

      }
      
    };
  });
})();
