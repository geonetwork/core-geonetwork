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
          templateUrl: tplFolder + 'newMetadata.html',
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

        // TODO: Check requested metadata exist - return message if it happens
        // Would you like to create a new one ?
        $scope.editorFormUrl = buildEditUrlPrefix('md.edit');
      };




      var buildEditUrlPrefix = function(service) {
        var params = [service, '?id=', $scope.metadataId];
        $scope.tab && params.push('&currTab=',
                                     $scope.tab);
        $scope.displayAttributes && params.push('&displayAttributes=',
                                                   $scope.displayAttributes);
        return params.join('');
      };


      /**
       * When the form is loaded, this function is called.
       * Use it to retrieve form variables or initialize
       * elements eg. tooltip ?
       */
      $scope.formLoaded = function() {
        $scope.metadataType = $($scope.formId + ' #template')[0].value;
        $scope.metadataLanguage = $($scope.formId + ' #language')[0].value;
        $scope.metadataOtherLanguages = $($scope.formId + ' #otherLanguages')[0].value;
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
      $scope.setTemplate = function (isTemplate) {
        $scope.metadataType = isTemplate ? 'y' : 'n';
        $('#template')[0].value = $scope.metadataType;
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
       * When attribute is expanded, the returned element contains the field
       * and the element is replaced.
       */
      $scope.add = function(ref, name, insertRef, position, attribute) {
        // for element: md.elem.add?id=1250&ref=41&name=gmd:presentationForm
        // for attribute md.elem.add?id=19&ref=42&name=gco:nilReason
        //                  &child=geonet:attribute

        var attributeAction = attribute ? '&child=geonet:attribute' : '';

        $http.get(buildEditUrlPrefix('md.element.add') +
                '&ref=' + ref + '&name=' + name + attributeAction)
                .success(function(data) {
              // Append HTML snippet after current element - compile Angular
              var target = $('#gn-el-' + insertRef);
              var snippet = $(data);

              if (attribute) {
                target.replaceWith(snippet);
              } else {
                snippet.css('display', 'none');   // Hide
                target[position || 'after'](snippet); // Insert
                snippet.slideDown(duration, function() {});   // Slide

                // Remove the Add control from the current element
                var addControl = $('#gn-el-' + insertRef + ' .gn-add');
              }

              $compile(snippet)($scope); // Compile

            }).error(function(data) {
              console.log(data);
            });
      };


      $scope.addChoice = function(ref, parent, name, insertRef, position) {
        // md.elem.add?id=1250&ref=41&name=gmd:presentationForm
        $http.get(buildEditUrlPrefix('md.element.add') +
                  '&ref=' + ref +
                  '&name=' + parent +
                  '&child=' + name).success(function(data) {
          // Append HTML snippet after current element - compile Angular
          var target = $('#gn-el-' + insertRef);
          var snippet = $(data);

          target[position || 'before'](snippet);

          $compile(snippet)($scope); // Compile
        }).error(function(data) {
          console.log(data);
        });
      };



      /**
       * Save the metadata record currently in editing session.
       *
       * If refreshForm is true, then will also update the current form.
       * This is required while switching tab for example. Update the tab
       * value in the form and trigger save to update the view.
       */
      $scope.save = function(refreshForm) {
        if (saving) {
          return;
        }


        saving = true;
        $scope.savedStatus = $translate('saving');

        $http.post(
            refreshForm ? 'md.edit.save' : 'md.edit.saveonly',
            $($scope.formId).serialize(),
            {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).success(function(data) {
          if (refreshForm) {
            var snippet = $(data);
            $($scope.formId).replaceWith(snippet);
            $compile(snippet)($scope);

            $scope.toggleAttributes();
          }

          $scope.savedStatus = $translate('allChangesSaved');
          $scope.savedTime = moment();

          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('saveMetadataSuccess')
          });

          // FIXME : This should go somewhere else ?
          //          console.log($('.gn-tooltip'));
          //          $('#gn-tooltip').tooltip();

          saving = false;
        }).error(function(data) {
          saving = false;
          $scope.savedTime = moment();
          $scope.saveStatus = $translate('saveMetadataError');
          console.log(data);

          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('saveMetadataError'),
            error: data,
            timeout: 0,
            type: 'danger'});
        });
      };

      $scope.getSaveStatus = function() {
        if ($scope.savedTime) {
          return $translate('saveAtimeAgo',
              {timeAgo: moment($scope.savedTime).fromNow()});
        }
      };

      //      // Remove status message after 30s
      //      $scope.$watch('savedStatus', function () {
      //        $timeout(function () {
      //          $scope.savedStatus = '';
      //        }, 30000);
      //      });

      // Broadcast event to SaveEdits from form's directive
      $scope.$on('SaveEdits', function(event, refreshForm) {
        $scope.save(refreshForm);
      });

      $scope.$on('AddElement', function(event, ref, name, 
          insertRef, position, attribute) {
            $scope.add(ref, name, insertRef, position, attribute);
          });



      $scope.reset = function() {

      };


      $scope.highlightRemove = function(ref) {
        var target = $('#gn-el-' + ref);
        target.addClass('text-danger');
        target.find('legend').addClass('text-danger');
      };
      $scope.unhighlightRemove = function(ref) {
        var target = $('#gn-el-' + ref);
        target.removeClass('text-danger');
        target.find('legend').removeClass('text-danger');
      };

      init();

    }]);

})();
