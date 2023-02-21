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

(function () {
  goog.provide("gn_adminmetadata_controller");

  goog.require("gn_schematronadmin_controller");

  var module = angular.module("gn_adminmetadata_controller", [
    "gn_schematronadmin_controller"
  ]);

  /**
   * GnAdminMetadataController provides administration tools
   * for metadata and templates
   */
  module.controller("GnAdminMetadataController", [
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    "$compile",
    "gnSearchManagerService",
    "gnGlobalSettings",
    "gnUtilityService",
    function (
      $scope,
      $routeParams,
      $http,
      $rootScope,
      $translate,
      $compile,
      gnSearchManagerService,
      gnGlobalSettings,
      gnUtilityService
    ) {
      $scope.pageMenu = {
        folder: "metadata/",
        defaultTab: "metadata-and-template",
        tabs: [
          {
            type: "metadata-and-template",
            label: "metadataAndTemplates",
            icon: "fa-archive",
            href: "#/metadata/metadata-and-template"
          },
          {
            type: "standards",
            label: "standards",
            icon: "fa-puzzle-piece",
            href: "#/metadata/standards"
          },
          {
            type: "formatter",
            label: "metadataFormatter",
            icon: "fa-eye",
            href: "#/metadata/formatter"
          },
          {
            type: "schematron",
            label: "schematron",
            icon: "fa-check",
            href: "#/metadata/schematron"
          },
          {
            type: "metadata-identifier-templates",
            icon: "fa-key",
            label: "manageMetadataIdentifierTemplates",
            href: "#/metadata/metadata-identifier-templates"
          }
        ]
      };

      $scope.schemas = [];
      $scope.selectedSchemas = [];
      $scope.loadReport = null;
      $scope.loadTplReport = null;
      $scope.tplLoadRunning = false;
      $scope.sampleLoadRunning = false;
      $scope.searchObj = {
        internal: true,
        any: "",
        defaultParams: {
          any: "",
          isTemplate: "n",
          from: 1,
          to: 50
        }
      };
      $scope.searchObj.params = angular.extend({}, $scope.searchObj.defaultParams);

      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

      function loadSchemas() {
        $http.get("../api/standards").then(function (response) {
          $scope.schemas = response.data;

          // Trigger load action according to route params
          launchActions();
        });
      }

      function launchActions() {
        // Select schema
        if ($routeParams.schema === "all") {
          $scope.selectAllSchemas(true);
        } else if ($routeParams.schema !== undefined) {
          $scope.selectSchema($routeParams.schema.split(","));
        }

        // Load
        if ($routeParams.metadataAction === "load-samples") {
          $scope.loadSamples();
        } else if ($routeParams.metadataAction === "load-templates") {
          $scope.loadTemplates();
        } else if ($routeParams.metadataAction === "load-samples-and-templates") {
          $scope.loadSamples();
          $scope.loadTemplates();
        }
      }

      selectSchema = function (schema) {
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
      $scope.selectSchema = function (schema) {
        if (Array.isArray(schema)) {
          $.each(schema, function (index, value) {
            selectSchema(value);
          });
        } else {
          selectSchema(schema);
        }
        $scope.loadReport = null;
        $scope.loadTplReport = null;
      };

      $scope.isSchemaSelected = function (schema) {
        return $scope.selectedSchemas.indexOf(schema) !== -1;
      };

      $scope.selectAllSchemas = function (selectAll) {
        $scope.selectedSchemas = [];
        if (selectAll) {
          $.each($scope.schemas, function (index, value) {
            selectSchema(value.name);
          });
        }
        $scope.loadReport = null;
        $scope.loadTplReport = null;
      };

      $scope.loadTemplates = function () {
        $scope.tplLoadRunning = true;
        $http
          .put(
            "../api/records/templates?schema=" + $scope.selectedSchemas.join("&schema=")
          )
          .then(
            function (response) {
              $scope.loadTplReport = response.data;
              $scope.tplLoadRunning = false;
            },
            function (response) {
              $scope.tplLoadRunning = false;
            }
          );
      };

      $scope.loadSamples = function () {
        $scope.sampleLoadRunning = true;
        $http
          .put("../api/records/samples?schema=" + $scope.selectedSchemas.join("&schema="))
          .then(
            function (response) {
              $scope.loadReport = response.data;
              $scope.sampleLoadRunning = false;
            },
            function (response) {
              $scope.sampleLoadRunning = false;
            }
          );
      };

      $scope.templates = null;

      $scope.sortOrder = function (item) {
        return parseInt(item.displayorder, 10);
      };

      $scope.formatterSelected = null;
      $scope.formatters = [];
      $scope.formatterFiles = [];
      $scope.metadataId = "";

      loadFormatter = function () {
        $scope.formatters = [];
        $http.get("../api/formatters").then(
          function (response) {
            var data = response.data;

            if (data !== "null") {
              $scope.formatters = data.formatters;
            }
          },
          function (response) {
            // TODO
          }
        );
      };

      /**
       * Callback when error uploading file.
       */
      loadFormatterError = function (e, data) {
        if (data.jqXHR.status === 201) {
          loadFormatter();
          return;
        }
        $rootScope.$broadcast("StatusUpdated", {
          title: $translate.instant("formatterUploadError"),
          error: data.jqXHR.responseJSON,
          timeout: 0,
          type: "danger"
        });
      };
      /**
       * Configure logo uploader
       */
      $scope.formatterUploadOptions = {
        autoUpload: true,
        done: loadFormatter,
        fail: loadFormatterError
      };

      $scope.listFormatterFiles = function (f) {
        $scope.formatterFiles = [];

        var url = "../api/formatters/" + f.schema + "/" + f.id + "/files";
        $http.get(url).then(
          function (response) {
            var data = response.data;

            if (data !== "null") {
              // Format files
              angular.forEach(data.file ? data.file : data, function (file) {
                file.dir = "."; // File from root directory
                file["@path"] = file["@name"];
                $scope.formatterFiles.push(file);
              });
              angular.forEach(data.dir, function (dir) {
                // One file only, convert to array
                if (dir.file) {
                  if (!angular.isArray(dir.file)) {
                    dir.file = [dir.file];
                  }
                }
                angular.forEach(dir.file, function (file) {
                  file.dir = dir["@name"];
                  $scope.formatterFiles.push(file);
                });
              });
              $scope.selectedFile = $scope.formatterFiles[0];
            }
          },
          function (response) {
            // TODO
          }
        );
      };

      $scope.selectFormatter = function (f) {
        $scope.formatterSelected = f;
        $scope.listFormatterFiles(f);
      };

      $scope.downloadFormatter = function (f) {
        var url = "../api/formatters/" + f.schema + "/" + f.id;
        location.replace(url, "_blank");
      };

      $scope.formatterDelete = function (f) {
        var url = "../api/formatters/" + f.schema + "/" + f.id;
        $http.delete(url).then(
          function (response) {
            $scope.formatterSelected = null;
            loadFormatter();
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("formatterRemovalError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      $scope.$watch("selectedFile", function () {
        if ($scope.selectedFile) {
          $http({
            url:
              "../api/formatters/" +
              $scope.formatterSelected.schema +
              "/" +
              $scope.formatterSelected.id +
              "/files/" +
              $scope.selectedFile["@path"],
            method: "GET"
          }).then(function (response) {
            $scope.formatterFile = response.data;
          });
        }
      });

      $scope.saveFormatterFile = function (formId) {
        $http({
          url:
            "../api/formatters/" +
            $scope.formatterSelected.schema +
            "/" +
            $scope.formatterSelected.id +
            "/files/" +
            $scope.selectedFile["@path"],
          method: "POST",
          data: $(formId).serialize(),
          headers: { "Content-Type": "application/x-www-form-urlencoded" }
        }).then(function (response) {
          if (response.status === 201) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("formatterFileUpdated", {
                file: $scope.selectedFile["@name"]
              }),
              timeout: 2,
              type: "success"
            });
          } else {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("formatterFileUpdateError", {
                file: $scope.selectedFile["@name"]
              }),
              error: data,
              timeout: 0,
              type: "danger"
            });
          }
        });
      };

      $scope.previewOn = function (uuid) {
        $scope.metadataId = uuid;
      };

      $scope.updateParams = function () {
        if ($scope.searchObj.any == "") {
          $scope.$broadcast("resetSearch");
        } else {
          var addWildcard =
            $scope.searchObj.any.indexOf('"') === -1 &&
            $scope.searchObj.any.indexOf("*") === -1 &&
            $scope.searchObj.any.indexOf("q(") !== 0;
          $scope.searchObj.params.any = addWildcard
            ? "*" + $scope.searchObj.any + "*"
            : $scope.searchObj.any;
        }
      };

      $scope.clearSearch = function () {
        $scope.$broadcast("resetSearch");
      };

      $scope.testFormatter = function (mode) {
        var url =
          "../api/records/" +
          $scope.metadataId +
          "/formatters/" +
          $scope.formatterSelected.id +
          (mode == "XML" ? "?output=xml" : "");

        if (mode == "DEBUG") {
          url += "&debug=true";
        }

        window.open(url, "_blank");
      };

      if ($routeParams.tab === "formatter") {
        loadFormatter();
      } else if ($routeParams.schemaName || $routeParams.tab === "schematron") {
        $routeParams.tab = "schematron";
      } else {
        loadSchemas();
      }
    }
  ]);
})();
