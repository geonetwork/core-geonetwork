/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_adminmetadata_controller');


  goog.require('gn_schematronadmin_controller');

  var module = angular.module('gn_adminmetadata_controller',
      ['gn_schematronadmin_controller']);


  /**
   * GnAdminMetadataController provides administration tools
   * for metadata and templates
   */
  module.controller('GnAdminMetadataController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
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
              type: 'standards',
              label: 'standards',
              icon: 'fa-puzzle-piece',
              href: '#/metadata/standards'
            },{
              type: 'formatter',
              label: 'metadataFormatter',
              icon: 'fa-eye',
              href: '#/metadata/formatter'
            },{
              type: 'schematron',
              label: 'schematron',
              icon: 'fa-check',
              href: '#/metadata/schematron'
            },{
              type: 'metadata-identifier-templates',
              icon: 'fa-key',
              label: 'manageMetadataIdentifierTemplates',
              href: '#/metadata/metadata-identifier-templates'
            }]
      };

      $scope.schemas = [];
      $scope.selectedSchemas = [];
      $scope.loadReport = null;
      $scope.loadTplReport = null;
      $scope.tplLoadRunning = false;
      $scope.sampleLoadRunning = false;

      function loadSchemas() {
        $http.get('../api/standards').
            success(function(data) {
              $scope.schemas = data;

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
        $scope.selectedSchemas = [];
        if (selectAll) {
          $.each($scope.schemas, function(index, value) {
            selectSchema(value.name);
          });
        }
        $scope.loadReport = null;
        $scope.loadTplReport = null;
      };

      $scope.loadTemplates = function() {
        $scope.tplLoadRunning = true;
        $http.put('../api/records/templates?schema=' +
            $scope.selectedSchemas.join('&schema=')
        ).success(function(data) {
          $scope.loadTplReport = data;
          $scope.tplLoadRunning = false;
        }).error(function(data) {
          $scope.tplLoadRunning = false;
        });
      };

      $scope.loadSamples = function() {
        $scope.sampleLoadRunning = true;
        $http.put('../api/records/samples?schema=' +
            $scope.selectedSchemas.join('&schema=')
        ).success(function(data) {
          $scope.loadReport = data;
          $scope.sampleLoadRunning = false;
        }).error(function(data) {
          $scope.sampleLoadRunning = false;
        });
      };


      $scope.templates = null;

      $scope.sortOrder = function(item) {
        return parseInt(item.displayorder, 10);
      };


      $scope.formatterSelected = null;
      $scope.formatters = [];
      $scope.formatterFiles = [];
      $scope.metadataId = '';

      /**
       * Load list of logos
       */
      loadFormatter = function() {
        $scope.formatters = [];
        $http.get('md.formatter.list?_content_type=json').
            success(function(data) {
              if (data !== 'null') {
                $scope.formatters = data.formatters; // TODO: check multiple
              }
            }).error(function(data) {
              // TODO
            });
      };

      /**
       * Callback when error uploading file.
       */
      loadFormatterError = function(e, data) {
        $rootScope.$broadcast('StatusUpdated', {
          title: $translate.instant('formatterUploadError'),
          error: data.jqXHR.responseJSON,
          timeout: 0,
          type: 'danger'});
      };
      /**
       * Configure logo uploader
       */
      $scope.formatterUploadOptions = {
        autoUpload: true,
        done: loadFormatter,
        fail: loadFormatterError
      };

      $scope.listFormatterFiles = function(f) {
        //md.formatter.files?id=sextant
        $scope.formatterFiles = [];

        var url = 'md.formatter.files?_content_type=json&id=' + f.id;
        if (f.schema) {
          url += '&schema=' + f.schema;
        }
        $http.get(url).success(function(data) {
          if (data !== 'null') {
            // Format files
            angular.forEach(data.file, function(file) {
              file.dir = '.'; // File from root directory
              file['@path'] = file['@name'];
              $scope.formatterFiles.push(file);
            });
            angular.forEach(data.dir, function(dir) {
              // One file only, convert to array
              if (dir.file) {
                if (!angular.isArray(dir.file)) {
                  dir.file = [dir.file];
                }
              }
              angular.forEach(dir.file, function(file) {
                file.dir = dir['@name'];
                $scope.formatterFiles.push(file);
              });
            });
            $scope.selectedFile = $scope.formatterFiles[0];
          }
        }).error(function(data) {
          // TODO
        });
      };

      $scope.selectFormatter = function(f) {
        //md.formatter.files?id=sextant
        $scope.formatterSelected = f;
        $scope.listFormatterFiles(f);
      };


      $scope.downloadFormatter = function(f) {
        var url = 'md.formatter.download?id=' + f.id;
        if (f.schema) {
          url += '&schema=' + f.schema;
        }
        location.replace(url, '_blank');
      };

      $scope.formatterDelete = function(f) {
        var url = 'md.formatter.remove?id=' + f.id;
        if (f.schema) {
          url += '&schema=' + f.schema;
        }
        $http.get(url)
            .success(function(data) {
              $scope.formatterSelected = null;
              loadFormatter();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('formatterRemovalError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.$watch('selectedFile', function() {
        if ($scope.selectedFile) {
          var params = {
            id: $scope.formatterSelected.id,
            fname: $scope.selectedFile['@path']
          };
          if ($scope.formatterSelected.schema) {
            params.schema = $scope.formatterSelected.schema;
          }
          $http({
            url: 'md.formatter.edit?_content_type=json',
            method: 'POST',
            data: $.param(params),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          }).success(function(fileContent) {
            $scope.formatterFile = fileContent[0];
          });
        }
      });

      $scope.saveFormatterFile = function(formId) {
        $http({
          url: 'md.formatter.update?_content_type=json',
          method: 'POST',
          data: $(formId).serialize(),
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).then(
            function(response) {
              if (response.status === 200) {
                $rootScope.$broadcast('StatusUpdated', {
                  msg: $translate.instant('formatterFileUpdated',
                      {file: $scope.selectedFile['@name']}),
                  timeout: 2,
                  type: 'success'});
              } else {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate.instant('formatterFileUpdateError',
                      {file: $scope.selectedFile['@name']}),
                  error: data,
                  timeout: 0,
                  type: 'danger'});
              }
            });
      };

      $scope.testFormatter = function(mode) {
        var service = 'md.format.' + (mode == 'HTML' ? 'html' : 'xml');
        var url = service + '?uuid=' + $scope.metadataId +
            '&xsl=' + $scope.formatterSelected.id;
        if ($scope.formatterSelected.schema) {
          url += '&schema=' + $scope.formatterSelected.schema;
        }

        if (mode == 'DEBUG') {
          url += '&debug=true';
        }

        window.open(url, '_blank');
      };

      if ($routeParams.tab === 'formatter') {
        loadFormatter();
      } else if ($routeParams.schemaName || $routeParams.tab === 'schematron') {
        $routeParams.tab = 'schematron';
      } else {
        loadSchemas();
      }

    }]);

})();
