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
    'gnEditor',
    'gnSearchManagerService',
    'gnConfigService',
    'gnUtilityService',
    'gnCurrentEdit',
    function($scope, $routeParams, $http, $rootScope, 
        $translate, $compile, $timeout,
            gnEditor, 
            gnSearchManagerService, 
            gnConfigService,
            gnUtilityService, gnCurrentEdit) {
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
        gnConfigService.load().then(function(c) {
          // Config loaded
        });

        angular.extend(gnCurrentEdit, {
          id: $routeParams.id,
          formId: '#gn-editor-' + $routeParams.id,
          tab: $routeParams.tab,
          displayAttributes: $routeParams.displayAttributes === 'true',
          compileScope: $scope,
          sessionStartTime: moment()
        });

        // TODO: Check requested metadata exist - return message if it happens
        // Would you like to create a new one ?
        $scope.editorFormUrl = gnEditor
          .buildEditUrlPrefix('md.edit') + '&starteditingsession=yes';
        
        
        window.onbeforeunload = function() {
          // TODO: could be better to provide cancelAndClose and saveAndClose button
          return $translate('beforeUnloadEditor', 
              {timeAgo: moment($scope.editorConfig.savedTime).fromNow()});
        };
      };

      /**
       * When the form is loaded, this function is called.
       * Use it to retrieve form variables or initialize
       * elements eg. tooltip ?
       */
      $scope.onFormLoad = function() {
        gnEditor.onFormLoad();
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
        //        $($scope.formId + ' > fieldset').fadeOut(duration);

        $scope.save(true);
      };

      /**
       * Set type of record. Update the matching form element.
       */
      $scope.setTemplate = function(isTemplate) {
        gnCurrentEdit.mdType = isTemplate ? 'y' : 'n';
        $('#template')[0].value = gnCurrentEdit.mdType;
      };
      $scope.isTemplate = function() {
        return gnCurrentEdit.mdType === 'y';
      };

      /**
       * Display or not attributes editor.
       *
       * if toggle is true, the current value is inverted.
       */
      $scope.toggleAttributes = function(toggle) {
        if (toggle) {
          gnCurrentEdit.displayAttributes =
              gnCurrentEdit.displayAttributes === false;
        }

        // Update the form to propagate info when saved
        // or tab switch - Needs to be propagated in Update service
        $('#displayAttributes')[0].value = gnCurrentEdit.displayAttributes;

        // Toggle class on all gn-attr widgets
        if (gnCurrentEdit.displayAttributes) {
          $('.gn-attr').removeClass('hidden');
        } else {
          $('.gn-attr').addClass('hidden');
        }
      };
      $scope.checkField = function(name) {
        return gnEditor[name].$error.required ? 'has-error' : '';
      };
      $scope.add = function(ref, name, insertRef, position, attribute) {
        gnEditor.add(gnCurrentEdit.id, ref, name,
            insertRef, position, attribute);
        return false;
      };
      $scope.addChoice = function(ref, name, insertRef, position) {
        gnEditor.addChoice(gnCurrentEdit.id, ref, name,
            insertRef, position);
        return false;
      };
      $scope.remove = function(ref, parent) {
        gnEditor.remove(gnCurrentEdit.id, ref, parent);
      };
      $scope.save = function(refreshForm) {
        gnEditor.save(refreshForm)
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

      $scope.cancel = function(refreshForm) {
        gnEditor.cancel(refreshForm)
          .then(function(form) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('cancelMetadataSuccess')
              });
              gnEditor.refreshEditorForm(null, true);
            }, function(error) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('cancelMetadataError'),
                error: error,
                timeout: 0,
                type: 'danger'});
            });
        return false;
      };

      $scope.close = function() {
        gnEditor.save(false)
          .then(function(form) {
              // TODO: Should redirect to main page at some point ?
              window.onbeforeunload = null;
              window.close();
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
        if (gnCurrentEdit.savedTime) {
          return $scope.saveStatus = $translate('saveAtimeAgo',
              {timeAgo: moment(gnCurrentEdit.savedTime).fromNow()});
        }
      };
      $scope.getCancelStatus = function() {
        if (gnCurrentEdit.sessionStartTime) {
          return $scope.cancelStatus =
              $translate('cancelChangesFromNow', {
                timeAgo: moment(gnCurrentEdit.sessionStartTime).fromNow()
              });
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
