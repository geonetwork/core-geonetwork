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
  goog.provide("gn_new_metadata_controller");

  goog.require("gn_catalog_service");

  var module = angular.module("gn_new_metadata_controller", ["gn_catalog_service"]);

  /**
   * Controller to create new metadata record.
   */
  module.controller("GnNewMetadataController", [
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    "$compile",
    "gnSearchManagerService",
    "gnUtilityService",
    "gnMetadataManager",
    "gnConfigService",
    "gnConfig",
    "Metadata",
    function (
      $scope,
      $routeParams,
      $http,
      $rootScope,
      $translate,
      $compile,
      gnSearchManagerService,
      gnUtilityService,
      gnMetadataManager,
      gnConfigService,
      gnConfig,
      Metadata
    ) {
      $scope.isTemplate = false;
      $scope.hasTemplates = true;
      $scope.filterField = undefined;
      $scope.mdList = null;
      $scope.ownerGroup = null;

      // Used for the metadata identifier fields
      $scope.mdIdentifierTemplateTokens = {};
      $scope.mdIdentifierFieldsFilled = false;

      gnConfigService.load().then(function (c) {
        $scope.generateUuid = gnConfig["system.metadatacreate.generateUuid"];
        $scope.ownerGroup = gnConfig["system.metadatacreate.preferredGroup"];
        $scope.preferredTemplate = gnConfig["system.metadatacreate.preferredTemplate"];
      });

      // A map of icon to use for each types
      var icons = {
        featureCatalog: "gn-icon-featureCatalog",
        service: "gn-icon-service",
        map: "gn-icon-maps",
        staticMap: "gn-icon-staticMap",
        dataset: "gn-icon-dataset",
        series: "gn-icon-series"
      };

      var defaultType = "dataset";
      var unknownType = "unknownType";
      var fullPrivileges = true;

      $scope.getTypeIcon = function (type) {
        return icons[type] || "fa-square-o";
      };

      var init = function () {
        if ($routeParams.id) {
          gnMetadataManager.create(
            $routeParams.id,
            $routeParams.group,
            fullPrivileges,
            $routeParams.template,
            false,
            $routeParams.tab,
            true
          );
        } else {
          // Metadata creation could be on a template
          // or by duplicating an existing record
          var query = [];
          if ($routeParams.childOf || $routeParams.from) {
            query.push({ term: { id: $routeParams.childOf || $routeParams.from } });
          } else {
            query.push({ terms: { isTemplate: ["y"] } });
          }

          var facetConfig = {
            resourceType: {
              terms: {
                field: "resourceType",
                exclude: ["map/static", "theme", "place"],
                missing: "other"
              }
            }
          };
          if (Object.keys(facetConfig).length === 1) {
            $scope.filterField = Object.keys(facetConfig)[0];
            unknownType = facetConfig[Object.keys(facetConfig)[0]].terms.missing;
          }

          $http
            .post("../api/search/records/_search", {
              aggregations: facetConfig,
              query: {
                bool: {
                  must: query
                }
              },
              from: 0,
              size: 1000
            })
            .then(function (r) {
              if (r.data.hits.total.value > 0) {
                for (var i = 0; i < r.data.hits.hits.length; i++) {
                  r.data.hits.hits[i] = new Metadata(r.data.hits.hits[i]);
                }
                $scope.mdList = r.data.hits.hits;

                var types = [];
                if (
                  Object.keys(facetConfig).length === 1 &&
                  r.data.aggregations[$scope.filterField]
                ) {
                  types = r.data.aggregations[$scope.filterField].buckets.map(function (
                    bucket
                  ) {
                    return { key: bucket.key, label: $translate.instant(bucket.key) };
                  });
                }
                $scope.mdTypes = types;

                // Get default template and calculate the template type
                var mdDefaultTemplate = _.find($scope.mdList, function (n) {
                  if (n._id == $scope.preferredTemplate) {
                    return true;
                  }
                });

                var templateToSelect = null;
                if (mdDefaultTemplate) {
                  defaultType =
                    (mdDefaultTemplate[$scope.filterField] &&
                      mdDefaultTemplate[$scope.filterField][0]) ||
                    unknownType;
                  templateToSelect = mdDefaultTemplate;
                }

                // Select the default one or the first one
                if (
                  defaultType &&
                  $scope.mdTypes.filter(function (v) {
                    return v.key === defaultType;
                  }).length === 1
                ) {
                  $scope.getTemplateNamesByType(defaultType, templateToSelect);
                } else if ($scope.mdTypes[0] && $scope.mdTypes[0].key) {
                  $scope.getTemplateNamesByType($scope.mdTypes[0].key);
                } else if ($scope.mdList.length > 0) {
                  $scope.getTemplateNamesByType(undefined, templateToSelect);
                }

                $scope.hasTemplates = true;
              } else {
                $scope.hasTemplates = false;
              }
            });
        }
      };

      // One template, one group, no custom UUID, move to the editor.
      var unregisterFn = $scope.$watch("ownerGroup", function (n, o) {
        moveToEditorCheckFn(n);
      });
      var unregisterMdListFn = $scope.$watchCollection("mdList", function (n, o) {
        moveToEditorCheckFn(n);
      });
      function moveToEditorCheckFn(n) {
        if (n !== null && $scope.mdList != null) {
          if (
            $scope.mdList.length === 1 &&
            $scope.groups.length === 1 &&
            $scope.generateUuid === true
          ) {
            $scope.createNewMetadata(false);
          }
          unregisterFn();
          unregisterMdListFn();
        }
      }

      /**
       * Get all the templates for a given type.
       * Will put this list into $scope.tpls variable.
       */
      $scope.getTemplateNamesByType = function (type, templateToSelect) {
        var tpls = [];
        if (type) {
          for (var i = 0; i < $scope.mdList.length; i++) {
            var md = $scope.mdList[i];
            var mdType = md[$scope.filterField] || unknownType;
            if (mdType instanceof Array) {
              if (mdType.indexOf(type) >= 0) {
                tpls.push(md);
              }
            } else if (mdType == type) {
              tpls.push(md);
            }
          }
        } else {
          tpls = $scope.mdList;
        }

        // Sort template list
        function compare(a, b) {
          if (a.resourceTitle < b.resourceTitle) return -1;
          if (a.resourceTitle > b.resourceTitle) return 1;
          return 0;
        }
        tpls.sort(compare);

        $scope.tplFilter = {
          resourceTitle: ""
        };
        $scope.tpls = tpls;

        var selectedTpl = $scope.tpls[0];
        if (templateToSelect) {
          selectedTpl = _.find($scope.tpls, function (tpl) {
            if (tpl._id == templateToSelect._id) {
              return true;
            }
          });
        }
        $scope.activeType = type;
        $scope.setActiveTpl(selectedTpl);
        return false;
      };

      $scope.setActiveTpl = function (tpl) {
        $scope.activeTpl = tpl;
      };

      if ($routeParams.childOf) {
        $scope.resourceTitle = "createChildOf";
      } else if ($routeParams.from) {
        $scope.resourceTitle = "createCopyOf";
      } else {
        $scope.resourceTitle = "createA";
      }

      $scope.cancelCreateMetadata = function () {
        gnUtilityService.goBack("/board");
      };

      $scope.createNewMetadata = function (isPublic) {
        var metadataUuid = "";

        // If no auto-generated metadata identifier, get the value
        if (!$scope.generateUuid && $scope.mdIdentifierSelectedTemplateId != 1) {
          // Custom identifier
          if ($scope.mdIdentifierSelectedTemplateId == 0) {
            metadataUuid = $scope.urnCustom;

            // Template identifier
          } else {
            metadataUuid = getSelectedMdIdentifierTemplate().template;

            for (var key in $scope.mdIdentifierTemplateTokens) {
              var labelKey = $scope.mdIdentifierTemplateTokens[key].label;
              metadataUuid = metadataUuid.replace(
                "{" + labelKey + "}",
                $scope.mdIdentifierTemplateTokens[key].value
              );
            }
          }
        }

        return gnMetadataManager
          .create(
            $scope.activeTpl.id,
            $scope.ownerGroup,
            isPublic || false,
            $scope.isTemplate,
            $routeParams.childOf ? true : false,
            undefined,
            metadataUuid,
            true
          )
          .catch(function (response) {
            var data = response.data;

            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("createMetadataError"),
              error: data.description ? data.description : data.error,
              timeout: 0,
              type: "danger"
            });
          });
      };

      /**
       * Executed when the metadata identifier template is changed.
       * Creates the model with the tokens of the template,
       * to fill from the template fields in the form.
       *
       */
      $scope.updateMdIdentifierTemplate = function () {
        if ($scope.mdIdentifierSelectedTemplateId <= 1) return;

        var selectedTemplate = getSelectedMdIdentifierTemplate();

        $scope.mdIdSelectedTemplateForLabel = selectedTemplate.template
          .replaceAll("{", " ")
          .replaceAll("}", " ");

        var tokens = selectedTemplate.template.match(/\{(.+?)\}/g);

        $scope.mdIdentifierTemplateTokens = {};

        for (var i = 0; i < tokens.length; i++) {
          var labelValue = tokens[i].replace("{", "").replace("}", "");
          $scope.mdIdentifierTemplateTokens[i] = { label: labelValue, value: "" };
        }
      };

      /**
       * Updates the metadata identifier template label
       * with the values filled by the user.
       *
       */
      $scope.updateMdIdentifierTemplateLabel = function () {
        $scope.mdIdSelectedTemplateForLabel = getSelectedMdIdentifierTemplate().template;

        for (var key in $scope.mdIdentifierTemplateTokens) {
          if ($scope.mdIdentifierTemplateTokens[key].value) {
            var labelKey = $scope.mdIdentifierTemplateTokens[key].label;

            $scope.mdIdSelectedTemplateForLabel =
              $scope.mdIdSelectedTemplateForLabel.replace(
                "{" + labelKey + "}",
                " " + $scope.mdIdentifierTemplateTokens[key].value + " "
              );
          }
        }

        $scope.mdIdSelectedTemplateForLabel = $scope.mdIdSelectedTemplateForLabel
          .replaceAll("{", " ")
          .replaceAll("}", " ");
      };

      /**
       * Function to show the custom metadata idenfifier
       * field or the template URN fields.
       *
       * @return {boolean}
       */
      $scope.showCustomMdIdentifierField = function () {
        if (!$scope.mdIdentifierSelectedTemplateId) return false;

        return $scope.mdIdentifierSelectedTemplateId == 0;
      };

      /**
       * Returns true if all the metadata identifier
       * form fields are filled.
       *
       * For auto-generated metadata identifier returns true.
       *
       * @return {boolean}
       */
      $scope.isMdIdentifierFilled = function () {
        if ($scope.mdIdentifierSelectedTemplateId == 1) return true;
        if ($scope.mdIdentifierSelectedTemplateId == 0) return $scope.urnCustom;

        var fieldsFilled = true;

        for (var key in $scope.mdIdentifierTemplateTokens) {
          if (!$scope.mdIdentifierTemplateTokens[key].value) {
            fieldsFilled = false;
            break;
          }
        }

        return fieldsFilled;
      };

      String.prototype.replaceAll = function (find, replace) {
        var str = this;
        return str.replace(
          new RegExp(find.replace(/[-\/\\^$*+?.()|[\]{}]/g, "\\$&"), "g"),
          replace
        );
      };

      function getSelectedMdIdentifierTemplate(id) {
        var selectedTemplate;

        for (var i = 0; i < $scope.mdIdentifierTemplates.length; i++) {
          if (
            $scope.mdIdentifierTemplates[i].id == $scope.mdIdentifierSelectedTemplateId
          ) {
            selectedTemplate = $scope.mdIdentifierTemplates[i];
            break;
          }
        }
        return selectedTemplate;
      }

      function loadMetadataIdentifierTemplates() {
        $scope.mdIdentifierTemplateSelected = {};

        $http.get("../api/identifiers").then(function (response) {
          $scope.mdIdentifierTemplates = response.data;
        });
      }

      loadMetadataIdentifierTemplates();

      init();
    }
  ]);
})();
