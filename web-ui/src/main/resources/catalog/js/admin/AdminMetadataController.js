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

      var templateFolder = '../../catalog/templates/admin/metadata/';
      var availableTemplates = [
        'metadata-and-template'
      ];

      $scope.defaultMetadataTab = 'metadata-and-template';

      $scope.getTemplate = function() {
        $scope.type = $scope.defaultMetadataTab;
        if (availableTemplates.indexOf($routeParams.metadataTab) > -1) {
          $scope.type = $routeParams.metadataTab;
        }
        return templateFolder + $scope.type + '.html';
      };


      $scope.schemas = [];
      $scope.selectedSchemas = [];
      $scope.loadReport = null;
      $scope.loadTplReport = null;

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
          $scope.selectSchema($routeParams.schema);
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

      $scope.selectSchema = function(schema) {
        var idx = $scope.selectedSchemas.indexOf(schema);
        if (idx === -1) {
          $scope.selectedSchemas.push(schema);
        } else {
          $scope.selectedSchemas.splice(idx, 1);
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
        $http.get('admin.load.templates@json?schema=' +
            $scope.selectedSchemas.join(',')
        ).success(function(data) {
          $scope.loadTplReport = data;
        });
      };

      $scope.loadSamples = function() {
        $http.get('admin.load.samples@json?file_type=mef&uuidAction=overwrite' +
                '&schema=' +
            $scope.selectedSchemas.join(',')
        ).success(function(data) {
          $scope.loadReport = data;
        });
      };


      loadSchemas();

    }]);

})();
