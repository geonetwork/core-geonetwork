(function() {
  goog.provide('gn_editor_controller');


  var module = angular.module('gn_editor_controller',
      []);


  /**
   * Metadata editor controller - draft
   */
  module.controller('GnEditorController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnMetadataManagerService, 
            gnSearchManagerService, 
            gnUtilityService) {

      $scope.metadataUuid = 'da165110-88fd-11da-a88f-000d939bc5d8';
      $scope.metadataId = '1246';

      $scope.getEditorForm = function() {
        return 'md.edit?id=' + $scope.metadataId;
      };

      $scope.remove = function(ref, parent) {
        // _dc=1380712127274&id=1250&ref=50&parent=41
        $http.get('md.element.remove@json?id=' + $scope.metadataId +
                '&ref=' + ref + '&parent=' + parent).success(function(data) {
          var target = $('#gn-el-' + ref);
          target.slideUp(300, function() { $(this).remove();});
          // TODO: Take care of moving the + sign
        }).error(function(data) {
          console.log(data);
        });
      };

      $scope.add = function(ref, name, insertAfterRef) {
        // metadata.elem.add.new?_dc=1380712397073&&id=1250&ref=41&name=gmd:presentationForm
        $http.get('md.element.add?id=' + $scope.metadataId +
                '&ref=' + ref + '&name=' + name).success(function(data) {
          console.log(data);
          // Append HTML snippet after current element - compile Angular
          var target = $('#gn-el-' + insertAfterRef);
          target.after(data);
        }).error(function(data) {
          console.log(data);
        });
      };

      $scope.save = function() {
        $http.post('md.edit.save',
            $('#gn-editor-' + $scope.metadataId).serialize(),
            {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).success(function(data) {
          console.log(data);
          // Insert HTML snippet - compile Angular
        }).error(function(data) {
          console.log(data);
        });
      };
      $scope.reset = function() {

      };
    }]);

})();
