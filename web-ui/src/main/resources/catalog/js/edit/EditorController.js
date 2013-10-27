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


      $scope.metadataId = gnUtilityService.getUrlParameter('id');
      $scope.metadataUuid = gnUtilityService.getUrlParameter('uuid');
      $scope.resourceId = gnUtilityService.getUrlParameter('resourceId');
      $scope.tab = gnUtilityService.getUrlParameter('tab');
      $scope.flat = gnUtilityService.getUrlParameter('flat');
      // TODO : propagate flat mode to all call

      /**
       * Animation duration for slide up/down
       */
      var duration = 300;

      $scope.getEditorForm = function() {
        // TODO: init by UUID or resourceId
        // TODO: Check requested metadata exist - return message if it happens
        // Would you like to create a new one ?
        return 'md.edit?id=' + $scope.metadataId +
            ($scope.tab ? '&currTab=' + $scope.tab : '');
      };

      $scope.switchToTab = function(tabIdentifier) {
        //          $scope.tab = tabIdentifier;
        //          FIXME: this trigger an edit
        //          better to use ng-model in the form ?
        $('#currTab')[0].value = tabIdentifier;
        $scope.save(true);
      };


      $scope.remove = function(ref, parent) {
        // md.element.remove?id=<metadata_id>&ref=50&parent=41
        // Call service to remove element from metadata record in session
        $http.get('md.element.remove@json?id=' + $scope.metadataId +
                '&ref=' + ref + '&parent=' + parent).success(function(data) {
          // Remove element from the DOM
          var target = $('#gn-el-' + ref);
          target.slideUp(duration, function() { $(this).remove();});

          // TODO: Take care of moving the + sign
        }).error(function(data) {
          console.log(data);
        });
      };

      /**
       * Add another element of the same type to the metadata record.
       *
       * Default position is after. Other value is before.
       */
      $scope.add = function(ref, name, insertRef, position) {
        // md.elem.add?id=1250&ref=41&name=gmd:presentationForm
        $http.get('md.element.add?id=' + $scope.metadataId +
                '&ref=' + ref + '&name=' + name).success(function(data) {
          console.log(data);
          // Append HTML snippet after current element - compile Angular
          var target = $('#gn-el-' + insertRef);
          var snippet = $(data);
          snippet.css('display', 'none');   // Hide
          target[position || 'after'](snippet); // Insert
          snippet.slideDown(duration, function() {});   // Slide

          $compile(snippet)($scope); // Compile

          // Remove the Add control from the current element
          var addControl = $('#gn-el-' + insertRef + ' .gn-add');
          console.log(addControl);

        }).error(function(data) {
          console.log(data);
        });
      };


      $scope.addChoice = function(ref, parent, name, insertRef) {
        // md.elem.add?id=1250&ref=41&name=gmd:presentationForm
        $http.get('md.element.add?id=' + $scope.metadataId +
                  '&ref=' + ref +
                  '&name=' + parent +
                  '&child=' + name).success(function(data) {
          // Append HTML snippet after current element - compile Angular
          var target = $('#gn-el-' + insertRef);
          var snippet = $(data);
          target.replaceWith(snippet); // Replace
          $compile(snippet)($scope); // Compile
        }).error(function(data) {
          console.log(data);
        });
      };

      /**
       * Add a non existing element to the metadata record
       */
      $scope.expand = function() {

      };


      /**
       * Save the metadata record currently in editing session.
       *
       * If refreshForm is true, then will also update the current form.
       * This is required while switching tab for example. Update the tab
       * value in the form and trigger save to update the view.
       */
      $scope.save = function(refreshForm) {
        $http.post(
            refreshForm ? 'md.edit.save' : 'md.edit.saveonly',
            $('#gn-editor-' + $scope.metadataId).serialize(),
            {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).success(function(data) {
          console.log(data);
          if (refreshForm) {
            var snippet = $(data);
            $('#gn-editor-' + $scope.metadataId).replaceWith(snippet);
            $compile(snippet)($scope);
          }
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('saveMetadataSuccess')
          });

        }).error(function(data) {
          console.log(data);

          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('saveMetadataError'),
            error: data,
            timeout: 0,
            type: 'danger'});
        });
      };


      $scope.reset = function() {

      };


      $scope.highlightRemove = function(ref) {
        var target = $('#gn-el-' + ref);
        target.addClass('text-danger');
      };
      $scope.unhighlightRemove = function(ref) {
        var target = $('#gn-el-' + ref);
        target.removeClass('text-danger');
      };
    }]);

})();
