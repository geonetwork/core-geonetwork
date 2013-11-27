(function() {
  goog.provide('gn_editor_controller');










  goog.require('gn_fields');
  goog.require('gn_new_metadata_controller');
  goog.require('gn_scroll_spy');
  goog.require('gn_thesaurus');
  goog.require('gn_utility_directive');

  var module = angular.module('gn_editor_controller',
      ['gn_fields', 'gn_new_metadata_controller', 'gn_utility_directive',
       'gn_scroll_spy', 'gn_thesaurus']);

  var tplFolder = '../../catalog/templates/editor/';

  module.config(['$routeProvider', function($routeProvider) {
    $routeProvider.
        when('/metadata/:id', {
          templateUrl: tplFolder + 'editor.html',
          controller: 'GnEditorController'}).
        when('/metadata/:id/tab/:tab', {
          templateUrl: tplFolder + 'editor.html',
          controller: 'GnEditorController'}).
        when('/metadata/:id/tab/:tab/:displayAttributes', {
          templateUrl: tplFolder + 'editor.html',
          controller: 'GnEditorController'}).
        when('/create/from/:id/in/:group/astemplate/:template', {
          templateUrl: tplFolder + 'editor.html',
          controller: 'GnNewMetadataController'}).
        otherwise({
          templateUrl: tplFolder + 'new-metadata.html',
          controller: 'GnNewMetadataController'
        });
  }]);

  /**
   * Metadata editor controller - draft
   */
  module.controller('GnEditorController', [
    '$scope', '$routeParams', '$http', '$rootScope',
    '$translate', '$compile', '$timeout',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, 
        $translate, $compile, $timeout,
            gnMetadataManagerService, 
            gnSearchManagerService, 
            gnUtilityService) {
      $scope.savedStatus = null;
      $scope.savedTime = null;
      $scope.formId = null;

      /**
       * Animation duration for slide up/down
       */
      var duration = 300;
      var saving = false;


      // Controller initialization
      var init = function() {
        $scope.metadataId = $routeParams.id;
        $scope.formId = '#gn-editor-' + $scope.metadataId;
        //gnUtilityService.getUrlParameter('id');
        $scope.metadataUuid = gnUtilityService.getUrlParameter('uuid');
        $scope.tab = $routeParams.tab;
        //gnUtilityService.getUrlParameter('tab');
        $scope.displayAttributes = $routeParams.displayAttributes === 'true';

        $scope.editorConfig = {
          metadataId: $scope.metadataId,
          formId: $scope.formId,
          compileScope: $scope,
          tab: $scope.tab,
          displayAttributes: $scope.displayAttributes
        };
        gnMetadataManagerService
          .startEditing($scope.metadataId, $scope.editorConfig);

        $scope.$watch('displayAttributes', function() {
          $scope.editorConfig.displayAttributes = $scope.displayAttributes;
        });

        // TODO: Check requested metadata exist - return message if it happens
        // Would you like to create a new one ?
        $scope.editorFormUrl = gnMetadataManagerService
          .buildEditUrlPrefix($scope.metadataId, 'md.edit');
      };


      /**
       * When the form is loaded, this function is called.
       * Use it to retrieve form variables or initialize
       * elements eg. tooltip ?
       */
      $scope.formLoaded = function() {
        $scope.editorConfig.metadataType =
            $($scope.formId + ' #template')[0].value;
        $scope.editorConfig.metadataLanguage =
            $($scope.formId + ' #language')[0].value;
        $scope.editorConfig.metadataOtherLanguages =
            $($scope.formId + ' #otherLanguages')[0].value;
        $scope.editorConfig.showValidationErrors =
            $($scope.formId + ' #showvalidationerrors')[0].value;
      };


      /**
       * Update the form according to the target tab
       * properties and save.
       */
      $scope.switchToTab = function(tabIdentifier, mode) {
        //          $scope.tab = tabIdentifier;
        //          FIXME: this trigger an edit
        //          better to use ng-model in the form ?
        $('#currTab')[0].value = tabIdentifier;
        $('#flat')[0].value = mode === 'flat';

        // Make the current form disapearing
        $($scope.formId + ' > fieldset').fadeOut(duration);

        $scope.save(true);
      };

      /**
       * Set type of record. Update the matching form element.
       */
      $scope.setTemplate = function(isTemplate) {
        $scope.editorConfig.metadataType = isTemplate ? 'y' : 'n';
        $('#template')[0].value = $scope.editorConfig.metadataType;
      };
      $scope.isTemplate = function() {
        return $scope.editorConfig.metadataType === 'y';
      };

      /**
       * Display or not attributes editor.
       *
       * if toggle is true, the current value is inverted.
       */
      $scope.toggleAttributes = function(toggle) {
        if (toggle) {
          $scope.displayAttributes = $scope.displayAttributes === false;
        }

        // Update the form to propagate info when saved
        // or tab switch - Needs to be propagated in Update service
        $('#displayAttributes')[0].value = $scope.displayAttributes;

        // Toggle class on all gn-attr widgets
        if ($scope.displayAttributes) {
          $('.gn-attr').removeClass('hidden');
        } else {
          $('.gn-attr').addClass('hidden');
        }
      };
      $scope.checkField = function(name) {
        return gnEditor[name].$error.required ? 'has-error' : '';
      };
      $scope.add = function(ref, name, insertRef, position, attribute) {
        gnMetadataManagerService.add($scope.metadataId, ref, name,
            insertRef, position, attribute);
        return false;
      };
      $scope.addChoice = function(ref, name, insertRef, position) {
        gnMetadataManagerService.addChoice($scope.metadataId, ref, name,
            insertRef, position);
        return false;
      };
      $scope.remove = function(ref, parent) {
        gnMetadataManagerService.remove($scope.metadataId, ref, parent);
      };
      $scope.save = function(refreshForm) {
        gnMetadataManagerService.save($scope.metadataId, refreshForm)
          .then(function(form) {
              $scope.toggleAttributes();
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('saveMetadataSuccess')
              });
            }, function(error) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('saveMetadataError'),
                error: error,
                timeout: 0,
                type: 'danger'});
            });

        return false;
      };

      $scope.getSaveStatus = function() {
        if ($scope.editorConfig.savedTime) {
          return $translate('saveAtimeAgo',
              {timeAgo: moment($scope.editorConfig.savedTime).fromNow()});
        }
      };

      //      // Remove status message after 30s
      //      $scope.$watch('savedStatus', function () {
      //        $timeout(function () {
      //          $scope.savedStatus = '';
      //        }, 30000);
      //      });


      $scope.$on('AddElement', function(event, ref, name, 
          insertRef, position, attribute) {
            $scope.add(ref, name, insertRef, position, attribute);
          });



      $scope.validate = function() {
        $($scope.formId + ' #showvalidationerrors')[0].value = 'true';
        $scope.save(true);
      };


      $scope.highlightRemove = function(ref) {
        var target = $('#gn-el-' + ref);
        target.addClass('text-danger');
      };
      $scope.unhighlightRemove = function(ref) {
        var target = $('#gn-el-' + ref);
        target.removeClass('text-danger');
      };

      init();

    }]);

})();
