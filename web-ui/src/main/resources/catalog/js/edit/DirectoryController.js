(function() {
  goog.provide('gn_directory_controller');

  goog.require('gn_catalog_service');

  var module = angular.module('gn_directory_controller',
      ['gn_catalog_service']);

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
      // TODO: Use paging
      $scope.maxEntries = 500;

      var dataTypesToExclude = [];

      // A map of icon to use for each types
      var icons = {
        'gmd:CI_ResponsibleParty': 'fa-user',
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
        $http.get('admin.group.list@json').success(function(data) {
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
        gnSearchManagerService.search('qi@json?' +
            'template=s&fast=index&from=1&to=' + $scope.maxEntries).
            then(function(data) {

              $scope.mdList = data;
              $scope.hasEntries = data.count != '0';

              var types = [];
              // TODO: A faster option, could be to rely on facet type
              // But it may not be available
              for (var i = 0; i < data.metadata.length; i++) {
                var type = data.metadata[i].root || unknownType;
                if (type instanceof Array) {
                  for (var j = 0; j < type.length; j++) {
                    if ($.inArray(type[j], dataTypesToExclude) === -1 &&
                        $.inArray(type[j], types) === -1) {
                      types.push(type[j]);
                    }
                  }
                } else if ($.inArray(type, dataTypesToExclude) === -1 &&
                    $.inArray(type, types) === -1) {
                  types.push(type);
                }
              }
              types.sort();
              $scope.mdTypes = types;

              // Select the default one or the first one
              if (defaultType && $.inArray(defaultType, $scope.mdTypes)) {
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
       * Will put this list into $scope.tpls variable.
       */
      $scope.getEntries = function(type) {
        var tpls = [];
        for (var i = 0; i < $scope.mdList.metadata.length; i++) {
          var mdType = $scope.mdList.metadata[i].root || unknownType;
          if (mdType instanceof Array) {
            if (mdType.indexOf(type) >= 0) {
              tpls.push($scope.mdList.metadata[i]);
            }
          } else if (mdType == type) {
            tpls.push($scope.mdList.metadata[i]);
          }
        }

        // Sort template list
        function compare(a, b) {
          if (a.title < b.title)
            return -1;
          if (a.title > b.title)
            return 1;
          return 0;
        }
        tpls.sort(compare);

        $scope.tpls = tpls;
        $scope.activeTpl = null;
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
      $scope.save = function(refreshForm) {
        gnEditor.save(refreshForm)
          .then(function(form) {
              $scope.savedStatus = gnCurrentEdit.savedStatus;
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('saveMetadataSuccess')
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
        $scope.importData.group = $scope.groups[0].id;
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
