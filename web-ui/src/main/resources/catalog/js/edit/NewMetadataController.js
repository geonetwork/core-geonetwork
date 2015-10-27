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
    'gnSearchManagerService',
    'gnUtilityService',
    'gnMetadataManager',
    'gnConfigService',
    'gnConfig',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnSearchManagerService, 
            gnUtilityService,
            gnMetadataManager,
            gnConfigService,
            gnConfig) {

      $scope.isTemplate = false;
      $scope.hasTemplates = true;
      $scope.mdList = null;

      // Used for the metadata identifier fields
      $scope.mdIdentifierTemplateTokens = {};
      $scope.mdIdentifierFieldsFilled = false;

      gnConfigService.load().then(function(c) {
        $scope.generateUuid = gnConfig['system.metadatacreate.generateUuid'];
      });


      // A map of icon to use for each types
      var icons = {
        featureCatalog: 'fa-table',
        service: 'fa-cog',
        map: 'fa-globe',
        staticMap: 'fa-globe',
        dataset: 'fa-file'
      };

      $scope.$watchCollection('groups', function() {
        if (!angular.isUndefined($scope.groups)) {
          if ($scope.groups.length == 1) {
            $scope.ownerGroup = $scope.groups[0]['@id'];
          }
        }
      });

      // List of record type to not take into account
      // Could be avoided if a new index field is created FIXME ?
      var dataTypesToExclude = ['staticMap', 'theme', 'place'];
      var defaultType = 'dataset';
      var unknownType = 'unknownType';
      var fullPrivileges = true;

      $scope.getTypeIcon = function(type) {
        return icons[type] || 'fa-square-o';
      };

      var init = function() {
        if ($routeParams.id) {
          gnMetadataManager.create(
              $routeParams.id,
              $routeParams.group,
              fullPrivileges,
              $routeParams.template,
              false,
              $routeParams.tab);
        } else {

          // Metadata creation could be on a template
          // or by duplicating an existing record
          var query = '';
          if ($routeParams.childOf || $routeParams.from) {
            query = '_id=' + ($routeParams.childOf || $routeParams.from);
          } else {
            query = 'template=y';
          }


          // TODO: Better handling of lots of templates
          gnSearchManagerService.search('qi@json?' +
              query + '&fast=index&from=1&to=200').
              then(function(data) {

                $scope.mdList = data;
                $scope.hasTemplates = data.count != '0';

                var types = [];
                // TODO: A faster option, could be to rely on facet type
                // But it may not be available
                for (var i = 0; i < data.metadata.length; i++) {
                  var type = data.metadata[i].type || unknownType;
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
                if (defaultType &&
                    $.inArray(defaultType, $scope.mdTypes) > -1) {
                  $scope.getTemplateNamesByType(defaultType);
                } else if ($scope.mdTypes[0]) {
                  $scope.getTemplateNamesByType($scope.mdTypes[0]);
                } else {
                  // No templates available ?
                }
              });
        }
      };

      /**
       * Get all the templates for a given type.
       * Will put this list into $scope.tpls variable.
       */
      $scope.getTemplateNamesByType = function(type) {
        var tpls = [];
        for (var i = 0; i < $scope.mdList.metadata.length; i++) {
          var mdType = $scope.mdList.metadata[i].type || unknownType;
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
        $scope.activeType = type;
        $scope.setActiveTpl($scope.tpls[0]);
        return false;
      };

      $scope.setActiveTpl = function(tpl) {
        $scope.activeTpl = tpl;
      };


      if ($routeParams.childOf) {
        $scope.title = $translate('createChildOf');
      } else if ($routeParams.from) {
        $scope.title = $translate('createCopyOf');
      } else {
        $scope.title = $translate('createA');
      }

      $scope.createNewMetadata = function(isPublic) {
        var metadataUuid = '';

        // If no auto-generated metadata identifier, get the value
        if (!$scope.generateUuid &&
            ($scope.mdIdentifierSelectedTemplateId != 1)) {

          // Custom identifier
          if ($scope.mdIdentifierSelectedTemplateId == 0) {
            metadataUuid = $scope.urnCustom;

          // Template identifier
          } else {
            metadataUuid = getSelectedMdIdentifierTemplate().template;

            for (key in $scope.mdIdentifierTemplateTokens) {
              var labelKey = $scope.mdIdentifierTemplateTokens[key].label;
              metadataUuid = metadataUuid.replace('{' + labelKey + '}',
                  $scope.mdIdentifierTemplateTokens[key].value);
            }
          }

        }

        return gnMetadataManager.create(
            $scope.activeTpl['geonet:info'].id,
            $scope.ownerGroup,
            isPublic || false,
            $scope.isTemplate,
            $routeParams.childOf ? true : false,
            undefined,
            metadataUuid
        ).error(function(data) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('createMetadataError'),
            error: data.error,
            timeout: 0,
            type: 'danger'});
        });
      };

      /**
       * Executed when the metadata identifier template is changed.
       * Creates the model with the tokens of the template,
       * to fill from the template fields in the form.
       *
       */
      $scope.updateMdIdentifierTemplate = function() {
        if ($scope.mdIdentifierSelectedTemplateId <= 1) return;

        var selectedTemplate = getSelectedMdIdentifierTemplate();

        $scope.mdIdSelectedTemplateForLabel = selectedTemplate.template
          .replaceAll('{', ' ').replaceAll('}', ' ');

        var tokens = selectedTemplate.template.match(/\{(.+?)\}/g);

        $scope.mdIdentifierTemplateTokens = {};

        for (var i = 0; i < tokens.length; i++) {
          var labelValue = tokens[i].replace('{', '').replace('}', '');
          $scope.mdIdentifierTemplateTokens[i] = {label: labelValue, value: ''};
        }

      };

      /**
       * Updates the metadata identifier template label
       * with the values filled by the user.
       *
       */
      $scope.updateMdIdentifierTemplateLabel = function() {
        $scope.mdIdSelectedTemplateForLabel =
            getSelectedMdIdentifierTemplate().template;

        for (key in $scope.mdIdentifierTemplateTokens) {
          if ($scope.mdIdentifierTemplateTokens[key].value) {
            var labelKey = $scope.mdIdentifierTemplateTokens[key].label;

            $scope.mdIdSelectedTemplateForLabel =
                $scope.mdIdSelectedTemplateForLabel
                .replace('{' + labelKey + '}',
                ' ' + $scope.mdIdentifierTemplateTokens[key].value + ' ');
          }
        }

        $scope.mdIdSelectedTemplateForLabel =
            $scope.mdIdSelectedTemplateForLabel
            .replaceAll('{', ' ').replaceAll('}', ' ');

      };

      /**
       * Function to show the custom metadata idenfifier
       * field or the template URN fields.
       *
       * @return {boolean}
       */
      $scope.showCustomMdIdentifierField = function() {
        if (!$scope.mdIdentifierSelectedTemplateId) return false;

        return ($scope.mdIdentifierSelectedTemplateId == 0);
      };

      /**
       * Returns true if all the metadata identifier
       * form fields are filled.
       *
       * For auto-generated metadata identifier returns true.
       *
       * @return {boolean}
       */
      $scope.isMdIdentifierFilled = function() {
        if ($scope.mdIdentifierSelectedTemplateId == 1) return true;
        if ($scope.mdIdentifierSelectedTemplateId == 0) return $scope.urnCustom;

        var fieldsFilled = true;

        for (key in $scope.mdIdentifierTemplateTokens) {
          if (!$scope.mdIdentifierTemplateTokens[key].value) {
            fieldsFilled = false;
            break;
          }
        }

        return fieldsFilled;
      };

      String.prototype.replaceAll = function(find, replace) {
        var str = this;
        return str.replace(new RegExp(find
          .replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&'), 'g'), replace);
      };


      function getSelectedMdIdentifierTemplate(id) {
        var selectedTemplate;

        for (var i = 0; i < $scope.mdIdentifierTemplates.length; i++) {
          if ($scope.mdIdentifierTemplates[i].id ==
              $scope.mdIdentifierSelectedTemplateId) {
            selectedTemplate = $scope.mdIdentifierTemplates[i];
            break;
          }
        }
        return selectedTemplate;
      }

      function loadMetadataIdentifierTemplates() {
        $scope.mdIdentifierTemplateSelected = {};

        $http.get('metadataIdentifierTemplates?_content_type=json')
          .success(function(data) {
              $scope.mdIdentifierTemplates = data;

            });

      }

      loadMetadataIdentifierTemplates();

      init();
    }
  ]);
})();
