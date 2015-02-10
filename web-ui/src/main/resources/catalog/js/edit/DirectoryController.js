(function() {
  goog.provide('gn_directory_controller');

  goog.require('gn_catalog_service');
  goog.require('gn_facets');

  var module = angular.module('gn_directory_controller',
      ['gn_catalog_service', 'gn_facets']);

  /**
   * Controller to create new metadata record.
   */
  module.controller('GnDirectoryController', [
    '$scope', '$routeParams', '$http',
    '$rootScope', '$translate', '$compile',
    'gnSearchManagerService',
    'gnUtilityService',
    'gnEditor',
    'gnCurrentEdit',
    'gnMetadataManager',
    function($scope, $routeParams, $http, 
        $rootScope, $translate, $compile,
            gnSearchManagerService, 
            gnUtilityService,
            gnEditor,
            gnCurrentEdit,
            gnMetadataManager) {

      $scope.isTemplate = 's';
      $scope.hasEntries = false;
      $scope.mdList = null;
      $scope.activeType = null;
      $scope.activeEntry = null;
      $scope.ownerGroup = null;
      $scope.searchObj = {params: {
        _isTemplate: 's',
        any: '*',
        _root: '',
        sortBy: 'title',
        sortOrder: 'reverse',
        resultType: 'subtemplates'
      }};

      $scope.paginationInfo = {
        pages: -1,
        currentPage: 1,
        hitsPerPage: 10
      };

      var dataTypesToExclude = [];

      // A map of icon to use for each types
      var icons = {
        'gmd:CI_ResponsibleParty': 'fa-user',
        'cit:CI_Responsibility': 'fa-user',
        'gmd:MD_Distribution': 'fa-link'
      };

      // List of record type to not take into account
      // Could be avoided if a new index field is created FIXME ?
      var defaultType = 'gmd:CI_ResponsibleParty';
      var unknownType = 'unknownType';
      var fullPrivileges = 'true';

      $scope.selectType = function(type) {
        $scope.activeType = type;
        $scope.getEntries(type);
      };
      $scope.getTypeIcon = function(type) {
        return icons[type] || 'fa-file-o';
      };

      var init = function() {
        $http.get('admin.group.list?_content_type=json', {cache: true}).
            success(function(data) {
              $scope.groups = data !== 'null' ? data : null;

              // Select by default the first group.
              if ($scope.ownerGroup === null && data) {
                $scope.ownerGroup = data[0]['id'];
              }
            });

        searchEntries();
      };

      var searchEntries = function() {
        $scope.tpls = null;
        gnSearchManagerService.search('qi?_content_type=json&' +
            'template=s&fast=index&summaryOnly=true&resultType=subtemplates').
            then(function(data) {
              $scope.$broadcast('setPagination', $scope.paginationInfo);
              $scope.mdList = data;
              $scope.hasEntries = data.count != '0';
              var types = [];
              angular.forEach(data.facet.subTemplateTypes, function(value) {
                if ($.inArray(value, dataTypesToExclude) === -1) {
                  types.push(value['@name']);
                }
              });
              types.sort();
              $scope.mdTypes = types;

              // Select the default one or the first one
              if ($scope.activeType &&
                  $.inArray($scope.activeType, $scope.mdTypes) !== -1) {
                $scope.selectType($scope.activeType);
              } else if (defaultType &&
                  $.inArray(defaultType, $scope.mdTypes) !== -1) {
                $scope.selectType(defaultType);
              } else if ($scope.mdTypes[0]) {
                $scope.selectType($scope.mdTypes[0]);
              } else {
                // No templates available ?
              }
            });
      };

      /**
       * Get all the templates for a given type.
       */
      $scope.getEntries = function(type) {
        if (type) {
          $scope.searchObj.params._root = type;
        }
        $scope.$broadcast('resetSearch', $scope.searchObj.params);
        return false;
      };

      /**
       * Update the form according to the target tab
       * properties and save.
       * FIXME: duplicate from EditorController
       */
      $scope.switchToTab = function(tabIdentifier, mode) {
        //          $scope.tab = tabIdentifier;
        //          FIXME: this trigger an edit
        //          better to use ng-model in the form ?
        $('#currTab')[0].value = tabIdentifier;
        $('#flat')[0].value = mode === 'flat';
        $scope.save(true);
      };
      /**
       * FIXME: duplicate from EditorController
       */
      $scope.add = function(ref, name, insertRef, position, attribute) {
        if (attribute) {
          // save the form and add attribute
          // after save is done. When adding an attribute
          // the snippet returned contains the current field
          // and the newly created attributes.
          // Save to not lose current edits in main field.
          gnEditor.save(false)
            .then(function() {
                gnEditor.add(gnCurrentEdit.id, ref, name,
                    insertRef, position, attribute);
              });
        } else {
          gnEditor.add(gnCurrentEdit.id, ref, name,
              insertRef, position, attribute);
        }
        return false;
      };
      $scope.addChoice = function(ref, name, insertRef, position) {
        gnEditor.addChoice(gnCurrentEdit.id, ref, name,
            insertRef, position);
        return false;
      };
      $scope.remove = function(ref, parent, domRef) {
        gnEditor.remove(gnCurrentEdit.id, ref, parent, domRef);
        return false;
      };
      $scope.removeAttribute = function(ref) {
        gnEditor.removeAttribute(gnCurrentEdit.id, ref);
        return false;
      };
      $scope.save = function(refreshForm) {
        gnEditor.save(refreshForm)
          .then(function(form) {
              $scope.savedStatus = gnCurrentEdit.savedStatus;
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('saveMetadataSuccess'),
                timeout: 2
              });
            }, function(error) {
              $scope.savedStatus = gnCurrentEdit.savedStatus;
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('saveMetadataError'),
                error: error,
                timeout: 0,
                type: 'danger'});
            });
        $scope.savedStatus = gnCurrentEdit.savedStatus;
        return false;
      };
      $scope.close = function() {
        gnEditor.save(false)
          .then(function(form) {
              $scope.gnCurrentEdit = '';
              $scope.selectEntry(null);
              searchEntries();
            }, function(error) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('saveMetadataError'),
                error: error,
                timeout: 0,
                type: 'danger'});
            });

        return false;
      };

      /**
       * When the form is loaded, this function is called.
       * Use it to retrieve form variables or initialize
       * elements eg. tooltip ?
       */
      $scope.onFormLoad = function() {
        gnEditor.onFormLoad();
      };

      // Counter to force editor refresh when
      // switching from one entry to another
      var i = 0;

      /**
       * Open the editor for the selected entry
       */
      $scope.selectEntry = function(e) {
        // TODO: alert when changing from
        // import action to editing to avoid
        // losing information.
        $scope.isImporting = false;
        $scope.activeEntry = e;

        if (e) {
          angular.extend(gnCurrentEdit, {
            id: e['geonet:info'].id,
            formId: '#gn-editor-' + e['geonet:info'].id,
            tab: 'simple',
            displayTooltips: false,
            compileScope: $scope,
            sessionStartTime: moment()
          });

          $scope.gnCurrentEdit = gnCurrentEdit;
          $scope.editorFormUrl = gnEditor
            .buildEditUrlPrefix('md.edit') +
              '&starteditingsession=yes&random=' + i++;
        }
      };

      $scope.isImporting = false;
      $scope.importData = {
        data: null,
        insert_mode: 0,
        template: 's',
        fullPrivileges: 'y'
      };


      $scope.startImportEntry = function() {
        $scope.selectEntry(null);
        $scope.isImporting = true;
        $scope.importData = {
          data: null,
          insert_mode: 0,
          template: 's',
          fullPrivileges: 'y',
          group: $scope.groups[0].id
        };
      };

      $scope.importEntry = function(formId) {
        gnMetadataManager.importMd($scope.importData).then(
            function() {
              searchEntries();
              $scope.isImporting = false;
              $scope.importData = null;
            }
        );
      };

      $scope.startSharing = function() {
        $('#gn-share').modal('show');
      };

      $scope.$on('PrivilegesUpdated', function() {
        $('#gn-share').modal('hide');
      });

      $scope.delEntry = function(e) {
        // md.delete?uuid=b09b1b16-769f-4dad-b213-fc25cfa9adc7
        gnMetadataManager.remove(e['geonet:info'].id).then(searchEntries);
      };

      $scope.copyEntry = function(e) {
        //md.create?id=181&group=2&isTemplate=s&currTab=simple
        gnMetadataManager.copy(e['geonet:info'].id, $scope.ownerGroup,
            fullPrivileges,
            's').then(searchEntries);
      };

      init();
    }
  ]);
})();
