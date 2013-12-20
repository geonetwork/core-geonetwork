(function() {
  goog.provide('gn_adminmetadata_controller');


  var module = angular.module('gn_adminmetadata_controller',
      []);


  /**
   * GnAdminMetadataController provides administration tools
   * for metadata and templates
   */
  module.controller('GnAdminMetadataController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnMetadataManagerService',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnMetadataManagerService, 
            gnSearchManagerService, 
            gnUtilityService) {

      $scope.pageMenu = {
        folder: 'metadata/',
        defaultTab: 'metadata-and-template',
        tabs:
            [{
              type: 'metadata-and-template',
              label: 'metadataAndTemplates',
              icon: 'fa-archive',
              href: '#/metadata/metadata-and-template'
            },{
              type: 'template-sort',
              label: 'sortTemplate',
              icon: 'fa-sort',
              href: '#/metadata/template-sort'
            }]
      };

      $scope.schemas = [];
      $scope.selectedSchemas = [];
      $scope.loadReport = null;
      $scope.loadTplReport = null;
      $scope.tplLoadRunning = false;
      $scope.sampleLoadRunning = false;

      function loadSchemas() {
        $http.get('admin.schema.list@json').success(function(data) {
          for (var i = 0; i < data.length; i++) {
            $scope.schemas.push(data[i]['#text'].trim());
          }
          $scope.schemas.sort();

          // Trigger load action according to route params
          launchActions();
        });
      }

      function launchActions() {
        // Select schema
        if ($routeParams.schema === 'all') {
          $scope.selectAllSchemas(true);
        } else if ($routeParams.schema !== undefined) {
          $scope.selectSchema($routeParams.schema.split(','));
        }

        // Load
        if ($routeParams.metadataAction ===
            'load-samples') {
          $scope.loadSamples();
        } else if ($routeParams.metadataAction ===
            'load-templates') {
          $scope.loadTemplates();
        } else if ($routeParams.metadataAction ===
            'load-samples-and-templates') {
          $scope.loadSamples();
          $scope.loadTemplates();
        }
      }

      selectSchema = function(schema) {
        var idx = $scope.selectedSchemas.indexOf(schema);
        if (idx === -1) {
          $scope.selectedSchemas.push(schema);
        } else {
          $scope.selectedSchemas.splice(idx, 1);
        }
      };

      /**
       * Select one or more schemas. Schema parameter
       * could be string or array.
       */
      $scope.selectSchema = function(schema) {
        if (Array.isArray(schema)) {
          $.each(schema, function(index, value) {
            selectSchema(value);
          });
        } else {
          selectSchema(schema);
        }
        $scope.loadReport = null;
        $scope.loadTplReport = null;
      };


      $scope.isSchemaSelected = function(schema) {
        return $scope.selectedSchemas.indexOf(schema) !== -1;
      };

      $scope.selectAllSchemas = function(selectAll) {
        if (selectAll) {
          $scope.selectedSchemas = $scope.schemas;
        } else {
          $scope.selectedSchemas = [];
        }
        $scope.loadReport = null;
        $scope.loadTplReport = null;
      };

      $scope.loadTemplates = function() {
        $scope.tplLoadRunning = true;
        $http.get('admin.load.templates@json?schema=' +
            $scope.selectedSchemas.join(',')
        ).success(function(data) {
          $scope.loadTplReport = data;
          $scope.tplLoadRunning = false;
        }).error(function(data) {
          $scope.tplLoadRunning = false;
        });
      };

      $scope.loadSamples = function() {
        $scope.sampleLoadRunning = true;
        $http.get('admin.load.samples@json?file_type=mef&uuidAction=overwrite' +
                '&schema=' +
            $scope.selectedSchemas.join(',')
        ).success(function(data) {
          $scope.loadReport = data;
          $scope.sampleLoadRunning = false;
        }).error(function(data) {
          $scope.sampleLoadRunning = false;
        });
      };


      $scope.templates = null;

      var loadTemplates = function() {
        $http.get('admin.templates.list@json')
        .success(function(data) {
              $scope.templates = data;
            });
      };

      $scope.saveOrder = function(formId) {
        $http.get('admin.templates.save.order?' + $(formId).serialize())
          .success(function(data) {
              loadTemplates();
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('saveTemplateOrderError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.sortOrder = function(item) {
        return parseInt(item.displayorder, 10);
      };

      if ($routeParams.tab === 'template-sort') {
        loadTemplates();
      } else {
        loadSchemas();
      }

    }]);

})();
