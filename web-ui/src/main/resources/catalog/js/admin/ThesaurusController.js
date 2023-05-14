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
  goog.provide("gn_thesaurus_controller");

  goog.require("gn_multilingual_field_directive");
  goog.require("gn_registry_directive");

  var module = angular.module("gn_thesaurus_controller", [
    "blueimp.fileupload",
    "gn_registry_directive",
    "gn_multilingual_field_directive"
  ]);

  /**
   * GnThesaurusController provides managment of thesaurus
   * by adding new local thesaurus, uploading from SKOS file or by URL.
   *
   * Keyword can be searched and added to local thesaurus.
   *
   * Limitations:
   *  * Only keyword in the GUI language will be displayed
   * and edited. TODO
   *  * Keyword relation can't be modified.
   *  * Thesaurus properties once created can't be modified
   *
   */
  module.controller("GnThesaurusController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    "$q",
    "gnConfig",
    "gnConfigService",
    "gnSearchManagerService",
    "gnUtilityService",
    "gnGlobalSettings",
    function (
      $scope,
      $http,
      $rootScope,
      $translate,
      $q,
      gnConfig,
      gnConfigService,
      gnSearchManagerService,
      gnUtilityService,
      gnGlobalSettings
    ) {
      $scope.gnConfig = gnConfig;
      $scope.thesaurusNamespace = "";
      gnConfigService.load().then(function (c) {
        $scope.thesaurusNamespace =
          gnConfig["system.metadata.thesaurusNamespace"] ||
          "https://registry.geonetwork-opensource.org/{{type}}/{{filename}}";
      });

      $scope.angularEquals = angular.equals;

      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

      /**
       * Type of relations in SKOS thesaurus
       */
      var relationTypes = ["broader", "narrower", "related"];

      /**
       * The list of thesaurus
       */
      $scope.thesaurus = [];
      $scope.thesaurusTypes = ["external", "local"];
      /**
       * The currently selected thesaurus
       */
      $scope.thesaurusSelected = null;
      /**
       * A suggested namespace for new thesaurus based
       * on other thesaurus properties
       */
      $scope.thesaurusSuggestedNs = "";
      $scope.customNamespace = false;
      /**
       * The thesaurus URL to upload
       */
      $scope.thesaurusUrl = "";

      /**
       * The current list of keywords
       */
      $scope.keywords = {};
      /**
       * The selected keyword
       */
      $scope.keywordSelected = null;
      /**
       * The list of relation for the selected keyword
       */
      $scope.keywordSelectedRelation = {};
      /**
       * A suggestion for the new keyword identifier
       * based on keyword properties
       */
      $scope.keywordSuggestedUri = "";
      /**
       * The current keyword filter
       */
      $scope.keywordFilter = "";

      $scope.maxNumberOfKeywords = 50;
      $scope.availableResultCounts = [50, 100, 200, 500, 1000];
      $scope.setResultsCount = function (count) {
        $scope.maxNumberOfKeywords = count;
      };

      $scope.recordsRelatedToThesaurus = 0;

      /**
       * Language switch for keyword list
       */
      $scope.currentLangShown = $scope.lang;
      $scope.availableLangs = gnGlobalSettings.gnCfg.mods.header.languages;
      $scope.switchLang = function (lang3) {
        $scope.currentLangShown = lang3;
      };

      /**
       * Language list for gn-multilingual-directive
       */
      $scope.langList = angular.copy($scope.availableLangs);
      angular.forEach($scope.langList, function (lang2, lang3) {
        $scope.langList[lang3] = "#" + lang2;
      });

      /**
       * The type of thesaurus import. Could be new, file or url.
       */
      $scope.importAs = null;

      var defaultMaxNumberOfKeywords = 50,
        creatingThesaurus = false, // Keyword creation in progress ?
        creatingKeyword = false, // Thesaurus creation in progress ?
        selectedKeywordOldId = null; // Keyword id before starting editing

      $scope.searching = false;

      /**
       * Select a thesaurus and search its keywords.
       */
      $scope.selectThesaurus = function (t) {
        creatingThesaurus = false;
        $scope.thesaurusSelected = t;

        $("#keywordFilter").focus();
        searchThesaurusKeyword();
      };

      /**
       * Search thesaurus keyword based on filter and max number
       */
      searchThesaurusKeyword = function () {
        $scope.searching = true;
        if ($scope.thesaurusSelected) {
          // list of ui languages; we want the keyword info in all these
          // put the current lang first to be used for sorting
          var langsList = [$scope.currentLangShown];
          Object.keys($scope.availableLangs).forEach(function (lang3) {
            if (langsList.indexOf(lang3) === -1) {
              langsList.push(lang3);
            }
          });

          $scope.recordsRelatedToThesaurus = 0;
          $http
            .get(
              "../api/registries/vocabularies/search?type=CONTAINS" +
                "&thesaurus=" +
                $scope.thesaurusSelected.key +
                "&uri=*" +
                encodeURIComponent($scope.keywordFilter) +
                "*" +
                "&rows=" +
                ($scope.maxNumberOfKeywords || defaultMaxNumberOfKeywords) +
                "&q=" +
                (encodeURI($scope.keywordFilter) || "*") +
                "&pLang=" +
                langsList.join(",")
            )
            .then(function (response) {
              $scope.keywords = response.data;
              var idTokens = $scope.thesaurusSelected.key.split(".");
              $http
                .post("../api/search/records/_search", {
                  query: {
                    exists: {
                      field: "th_" + idTokens[idTokens.length - 1]
                    }
                  },
                  from: 0,
                  size: 0
                })
                .then(function (r) {
                  $scope.recordsRelatedToThesaurus = parseInt(r.data.hits.total.value);
                });
            })
            .finally(function () {
              $scope.searching = false;
            });
        }
      };

      $scope.thesaurusTypeBySchema = [];
      function loadKeywordTypeForSchema(schema) {
        var url =
          "../api/standards/" +
          schema.id +
          "/codelists/" +
          schema.ns +
          ":MD_KeywordTypeCode";
        $http.get(url, { cache: true }).then(function (r) {
          angular.forEach(r.data, function (o, k) {
            $scope.thesaurusTypeBySchema.push({
              schema: schema.id,
              key: k,
              label: o
            });
          });
        });
      }
      function loadKeywordTypeCodes() {
        var schemas = [
          { id: "iso19139", ns: "gmd" },
          { id: "iso19115-3.2018", ns: "mri" }
        ];
        $scope.thesaurusTypeBySchema = [];
        for (var i = 0; i < schemas.length; i++) {
          loadKeywordTypeForSchema(schemas[i]);
        }
      }

      /**
       * Add a new local thesaurus and open the modal
       */
      $scope.addThesaurus = function (type) {
        creatingThesaurus = true;

        loadKeywordTypeCodes();

        $scope.thesaurusImportType = "theme";
        $scope.thesaurusImportOrigin = $scope.thesaurusTypes[0];
        $scope.importAs = type;
        $scope.thesaurusSelected = {
          title: "",
          description: "",
          filename: "",
          defaultNamespace: "",
          dname: "theme",
          type: "local"
        };

        $scope.registryUrl = "";
        $scope.selectedClass = "";
        $scope.selectedCollection = "";
        $scope.itemClass = [];
        $scope.languages = [];
        $scope.itemCollection = [];
        $scope.item = [];
        $scope.selectedLanguages = {};

        $scope.clear($scope.queue);

        $("#thesaurusModal").modal();
        $("#thesaurusModal").on("shown.bs.modal", function () {
          var id =
            $scope.importAs === "new"
              ? "#gn-thesaurus-title"
              : $scope.importAs === "file"
              ? "#gn-thesaurus-file"
              : "#gn-thesaurus-url";

          $(id).focus();
        });
      };

      /**
       * Build a namespace based on the namespace defined in settings.
       * eg. http://localhost:8080/thesaurus/theme/commune
       */
      $scope.computeThesaurusNs = function () {
        if (!creatingThesaurus) {
          return;
        }

        $scope.thesaurusSuggestedNs =
          $scope.thesaurusSelected && $scope.thesaurusNamespace
            ? $scope.thesaurusNamespace
                .replace("{{type}}", $scope.thesaurusSelected.dname)
                .replace(
                  "{{filename}}",
                  $scope.thesaurusSelected.filename.replace(/[^\d\w]/gi, "")
                )
            : "";

        if (!$scope.customNamespace) {
          $scope.useSuggestedNs();
        }
      };

      $scope.$watch("thesaurusSelected.filename", $scope.computeThesaurusNs);
      $scope.$watch("thesaurusSelected.dname", $scope.computeThesaurusNs);
      $scope.$watch("customNamespace", $scope.computeThesaurusNs);

      /**
       * Use the suggested namespace for the new thesaurus
       */
      $scope.useSuggestedNs = function () {
        $scope.thesaurusSelected.defaultNamespace = $scope.thesaurusSuggestedNs;
      };

      /**
       * Create the thesaurus in the catalog, close the modal on success
       * and refresh the list.
       */
      $scope.createThesaurus = function () {
        var hasMultilingualTitlesValues = !_.values(
          $scope.thesaurusSelected.multilingualTitles
        ).every(_.isEmpty);

        if (!hasMultilingualTitlesValues) {
          alert($translate.instant("thesaurusTitleRequired"));
          return;
        }

        var thesaurusToCreate = Object.assign({}, $scope.thesaurusSelected);

        // Change the keys from iso3lang to iso2lang
        thesaurusToCreate.multilingualTitles = _.mapKeys(
          thesaurusToCreate.multilingualTitles,
          function (value, key) {
            return $scope.availableLangs[key];
          }
        );

        thesaurusToCreate.multilingualDescriptions = _.mapKeys(
          thesaurusToCreate.multilingualDescriptions,
          function (value, key) {
            return $scope.availableLangs[key];
          }
        );

        $http.put("../api/registries/vocabularies", thesaurusToCreate).then(
          function (response) {
            $scope.thesaurusSelected = null;
            $("#thesaurusModal").modal("hide");
            loadThesaurus();
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("thesaurusCreationError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      /**
       * Updates the thesaurus titles and descriptions, for local thesaurus.
       */
      $scope.updateThesaurus = function () {
        var hasMultilingualTitlesValues = !_.values(
          $scope.thesaurusSelected.multilingualTitles
        ).every(_.isEmpty);

        if (!hasMultilingualTitlesValues) {
          alert($translate.instant("thesaurusTitleRequired"));
          return;
        }

        var thesaurusToUpdate = {
          multilingualTitles: {},
          multilingualDescriptions: {}
        };

        // Change the keys from iso3lang to iso2lang
        thesaurusToUpdate.multilingualTitles = _.mapKeys(
          $scope.thesaurusSelected.multilingualTitles,
          function (value, key) {
            return $scope.availableLangs[key];
          }
        );

        thesaurusToUpdate.multilingualDescriptions = _.mapKeys(
          $scope.thesaurusSelected.multilingualDescriptions,
          function (value, key) {
            return $scope.availableLangs[key];
          }
        );

        $http
          .put(
            "../api/registries/vocabularies/" + $scope.thesaurusSelected.key,
            thesaurusToUpdate
          )
          .then(
            function (response) {
              $scope.thesaurusSelected = null;
              $("#thesaurusModal").modal("hide");
              loadThesaurus();
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("thesaurusUpdateError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      /**
       * Thesaurus uploaded with success, close the modal,
       * refresh the list.
       */
      uploadThesaurusDone = function (data) {
        $scope.clear($scope.queue);
        $scope.thesaurusUrl = "";

        // Don't close automatically the dialog for INSPIRE Registry.
        // Usually the user needs to import several INSPIRE thesaurus
        // and if closing the dialog, all the configuration for the
        // URL end-point and languages need to be done every time.
        if ($scope.importAs !== "registry") {
          $("#thesaurusModal").modal("hide");
        }

        loadThesaurus();
      };

      /**
       * Thesaurus uploaded with error, broadcast it.
       */
      uploadThesaurusError = function (e, data) {
        var r = data.jqXHR || data;
        if (r.status === 201) {
          uploadThesaurusDone(data);
        } else {
          $rootScope.$broadcast("StatusUpdated", {
            title: $translate.instant("thesaurusUploadError"),
            error:
              r.responseJSON ||
              $(r.responseText).find("description").text() ||
              $(r.data).find("description").text() ||
              r.data,
            timeout: 0,
            type: "danger"
          });

          loadThesaurus();
        }
      };

      /**
       * Thesaurus upload options.
       */
      $scope.thesaurusUploadOptions = {
        autoUpload: false,
        //        TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
        done: uploadThesaurusDone,
        fail: uploadThesaurusError
      };

      /**
       * Upload the file or URL in the catalog thesaurus repository
       */
      $scope.importThesaurus = function (formId) {
        // Uploading
        $(formId)[0].enctype = $scope.importAs === "file" ? "multipart/form-data" : "";
        if ($scope.importAs === "file") {
          // unset registry URL value which may contains a ? added in ng-options
          $(formId)[0].registryUrl.value = "";
          $scope.submit();
          var defer = $q.defer();
          defer.resolve();
          return defer.promise;
        } else {
          return $http
            .put("../api/registries/vocabularies?" + $(formId).serialize())
            .then(uploadThesaurusDone, function (r) {
              uploadThesaurusError(null, r);
            });
        }
      };

      /**
       * Ask for confirmation to delete the thesaurus
       */
      $scope.deleteThesaurus = function (e) {
        $scope.delEntryId = $scope.thesaurusSelected.key;
        $("#gn-confirm-delete").modal("show");
      };

      /**
       * Remove a thesaurus from the catalog thesaurus repository
       * (this is done after a confirm dialog)
       */
      $scope.confirmDeleteThesaurus = function () {
        $http.delete("../api/registries/vocabularies/" + $scope.delEntryId).then(
          function (response) {
            $scope.thesaurusSelected = null;
            $scope.delEntryId = null;
            loadThesaurus();
          },
          function (response) {
            $scope.delEntryId = null;
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("thesaurusDeleteError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      $scope.reindexRecords = function () {
        gnSearchManagerService
          .indexSetOfRecords({
            thesaurusIdentifier: $scope.thesaurusSelected.key
          })
          .then(function (data) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("indexingRecordsRelatedToTheThesaurus"),
              timeout: 2
            });
          });
      };

      /**
       * Thesaurus creation on going
       */
      $scope.isNew = function () {
        return creatingThesaurus;
      };
      /**
       * Keyword creation on going
       */
      $scope.isNewKeyword = function () {
        return creatingKeyword;
      };

      /**
       * Search relation for each types for the current keyword
       */
      searchRelation = function (k) {
        $scope.keywordSelectedRelation = {};
        $.each(relationTypes, function (index, value) {
          $http
            .get(
              "thesaurus.keyword.links?_content_type=json&" +
                "request=" +
                value +
                "&thesaurus=" +
                $scope.thesaurusSelected.key +
                "&id=" +
                encodeURIComponent(k.uri)
            )
            .then(function (response) {
              $scope.keywordSelectedRelation[value] = response.data.descKeys;
            });
        });
      };

      function findKeywordByUri(uri) {
        for (var i = 0; i < $scope.keywords.length; i++) {
          if ($scope.keywords[i].uri === uri) {
            return $scope.keywords[i];
          }
        }
        return undefined;
      }

      /**
       * Edit an existing keyword, open the modal, search relations
       */
      $scope.editKeyword = function (k) {
        $scope.keywordSelected = angular.copy(
          angular.isObject(k) ? k : findKeywordByUri(k)
        );
        $scope.keywordSelected.oldId = $scope.keywordSelected.uri;

        // create geo object (if not already there)
        $scope.keywordSelected.geo = $scope.keywordSelected.geo || {
          east: k.coordEast,
          north: k.coordNorth,
          south: k.coordSouth,
          west: k.coordWest
        };

        creatingKeyword = false;
        $("#keywordModal").modal();
        searchRelation($scope.keywordSelected);
      };

      /**
       * Edit a new keyword and open the modal
       */
      $scope.addKeyword = function () {
        creatingKeyword = true;
        $scope.keywordSuggestedUri = "";
        $scope.keywordSelected = {
          uri:
            $scope.thesaurusSelected.defaultNamespace +
            ($scope.thesaurusSelected.defaultNamespace.indexOf("#") === -1 ? "#" : "") +
            gnUtilityService.randomUuid(),
          value: "",
          values: {},
          definition: "",
          definitions: {},
          defaultLang: $scope.lang
        };
        if ($scope.isPlaceType()) {
          $scope.keywordSelected.geo = {
            west: "0",
            south: "0",
            east: "0",
            north: "0"
          };
        }
        $("#keywordModal").modal();
      };

      /**
       * Build keyword POST body message
       */
      buildKeywordXML = function (keywordObject) {
        var geoxml = $scope.isPlaceType()
          ? "<west>" +
            keywordObject.geo.west +
            "</west>" +
            "<south>" +
            keywordObject.geo.south +
            "</south>" +
            "<east>" +
            keywordObject.geo.east +
            "</east>" +
            "<north>" +
            keywordObject.geo.north +
            "</north>"
          : "";

        // build localized values xml
        var localizedValues = "";
        Object.keys(keywordObject.values).forEach(function (lang3) {
          localizedValues +=
            "<loc_" +
            lang3 +
            "_label>" +
            keywordObject.values[lang3] +
            "</loc_" +
            lang3 +
            "_label>";
        });

        // build localized definitions xml
        var localizedDefinitions = "";
        Object.keys(keywordObject.definitions).forEach(function (lang3) {
          localizedDefinitions +=
            "<loc_" +
            lang3 +
            "_definition>" +
            keywordObject.definitions[lang3] +
            "</loc_" +
            lang3 +
            "_definition>";
        });

        var xml =
          "<request><newid>" +
          keywordObject.uri +
          "</newid>" +
          "<refType>" +
          $scope.thesaurusSelected.dname +
          "</refType>" +
          "<namespace>" +
          $scope.thesaurusSelected.defaultNamespace +
          "</namespace>" +
          "<ref>" +
          $scope.thesaurusSelected.key +
          "</ref>" +
          "<oldid>" +
          (keywordObject.oldId || keywordObject.uri) +
          "</oldid>" +
          localizedValues +
          localizedDefinitions +
          geoxml +
          "</request>";

        return xml;
      };

      /**
       * Create the keyword in the thesaurus
       */
      $scope.createKeyword = function () {
        $http
          .post(
            "thesaurus.keyword.add?_content_type=json",
            buildKeywordXML($scope.keywordSelected),
            { headers: { "Content-type": "application/xml" } }
          )
          .then(
            function (response) {
              var response = response.data[0];
              if (response && response["@message"]) {
                var statusConfig = {
                  title: $translate.instant("keywordCreationError"),
                  msg: response["@message"],
                  timeout: 0,
                  type: "danger"
                };
                $rootScope.$broadcast("StatusUpdated", statusConfig);
              } else {
                $scope.keywordSelected = null;
                $("#keywordModal").modal("hide");
                searchThesaurusKeyword();
                creatingKeyword = false;
              }
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("keywordCreationError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      /**
       * Update the keyword in the thesaurus
       */
      $scope.updateKeyword = function () {
        $http
          .post("thesaurus.keyword.update", buildKeywordXML($scope.keywordSelected), {
            headers: { "Content-type": "application/xml" }
          })
          .then(
            function (response) {
              $scope.keywordSelected = null;
              $("#keywordModal").modal("hide");
              searchThesaurusKeyword();
              selectedKeywordOldId = null;
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("keywordUpdateError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      /**
       * Remove a keyword
       */
      $scope.deleteKeyword = function (k) {
        $scope.keywordToDelete = k;
        $("#gn-confirm-delete-keyword").modal("show");
      };
      $scope.confirmDeleteKeyword = function () {
        var k = $scope.keywordToDelete;
        $http
          .get(
            "thesaurus.keyword.remove?pThesaurus=" +
              k.thesaurusKey +
              "&id=" +
              encodeURIComponent(k.uri)
          )
          .then(
            function (response) {
              searchThesaurusKeyword();
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("keywordDeleteError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          )
          .finally(function () {
            $scope.keywordToDelete = null;
          });
      };

      /**
       * Build a keyword identifier based on thesaurus
       * namespace and keyword label
       */
      $scope.computeKeywordId = function () {
        var defaultLabel =
          $scope.keywordSelected.values[$scope.keywordSelected.defaultLang] || "";
        $scope.keywordSuggestedUri =
          $scope.thesaurusSelected.defaultNamespace +
          ($scope.thesaurusSelected.defaultNamespace.indexOf("#") === -1 ? "#" : "") +
          defaultLabel.replace(/[^\d\w]/gi, "");
      };

      /**
       * Use the suggestion for the keyword uri
       */
      $scope.useSuggestedUri = function () {
        $scope.keywordSelected.uri = $scope.keywordSuggestedUri;
      };

      /**
       * Is the thesaurus a place type thesaurus
       */
      $scope.isPlaceType = function () {
        if ($scope.thesaurusSelected) {
          return (
            $scope.thesaurusSelected.key &&
            $scope.thesaurusSelected.key.indexOf(".place.") !== -1
          );
        } else {
          return false;
        }
      };

      /**
       * Is the thesaurus external (ie. readonly keywords).
       */
      $scope.isExternal = function () {
        if ($scope.thesaurusSelected) {
          return $scope.thesaurusSelected.type === "external";
        } else {
          return false;
        }
      };

      /**
       * When updating keyword search params: refresh list
       */
      $scope.$watch(
        function () {
          return (
            $scope.maxNumberOfKeywords +
            "##" +
            $scope.keywordFilter +
            "##" +
            $scope.currentLangShown
          );
        },
        function () {
          searchThesaurusKeyword();
        }
      );

      /**
       * Load the list of thesaurus from the server
       */
      function loadThesaurus() {
        $http.get("thesaurus?_content_type=json").then(
          function (response) {
            $scope.thesaurus = response.data[0];

            // {"iso2lang": "iso3lang", ...
            var invertedAvailableLangs = _.invert($scope.availableLangs);

            for (var i = 0; i < $scope.thesaurus.length; i++) {
              // JSON output is returning empty values as empty array
              $scope.thesaurus[i].title = Array.isArray($scope.thesaurus[i].title)
                ? ""
                : $scope.thesaurus[i].title;
              $scope.thesaurus[i].description = Array.isArray(
                $scope.thesaurus[i].description
              )
                ? ""
                : $scope.thesaurus[i].description;

              // Convert the multilingual titles object from {"lang": "iso2lang", "title": "value", ...
              // to the format required by the multilingual field directive: {"iso3lang": "value", ...
              $scope.thesaurus[i].multilingualTitles = _.zipObject(
                _.map($scope.thesaurus[i].multilingualTitles, function (t) {
                  return invertedAvailableLangs[t.lang];
                }),
                _.map($scope.thesaurus[i].multilingualTitles, "title")
              );

              // Add missing languages with empty values to the multilingual titles object
              var availableLangsEmptyValues = _.zipObject(
                _.keys($scope.availableLangs),
                _.map(Array(_.keys($scope.availableLangs).length), function () {
                  return "";
                })
              );

              $scope.thesaurus[i].multilingualTitles = _.merge(
                availableLangsEmptyValues,
                $scope.thesaurus[i].multilingualTitles
              );

              // Convert the multilingual descriptions object from {"lang": "iso2lang", "title": "value", ...
              // to the format required by the multilingual field directive: {"iso3lang": "value", ...
              $scope.thesaurus[i].multilingualDescriptions = _.zipObject(
                _.map($scope.thesaurus[i].multilingualDescriptions, function (t) {
                  return invertedAvailableLangs[t.lang];
                }),
                _.map($scope.thesaurus[i].multilingualDescriptions, "description")
              );

              availableLangsEmptyValues = _.zipObject(
                _.keys($scope.availableLangs),
                _.map(Array(_.keys($scope.availableLangs).length), function () {
                  return "";
                })
              );

              // Add missing languages with empty values to the multilingual titles object
              $scope.thesaurus[i].multilingualDescriptions = _.merge(
                availableLangsEmptyValues,
                $scope.thesaurus[i].multilingualDescriptions
              );

              var hasMultilingualTitlesValues = !_.values(
                $scope.thesaurus[i].multilingualTitles
              ).every(_.isEmpty);
              var hasMultilingualDescValues = !_.values(
                $scope.thesaurus[i].multilingualDescriptions
              ).every(_.isEmpty);

              if (!hasMultilingualTitlesValues) {
                if ($scope.thesaurus[i].type == "external") {
                  // Clear the multilingual title object to display a single textarea control with the description
                  $scope.thesaurus[i].multilingualTitles = {};
                } else {
                  // Assign the title if available as the default language multilingual title
                  $scope.thesaurus[i].multilingualTitles["eng"] =
                    $scope.thesaurus[i].title;
                }
              }

              if (!hasMultilingualDescValues) {
                if ($scope.thesaurus[i].type == "external") {
                  // Clear the multilingual description object to display a single textarea control with the description
                  $scope.thesaurus[i].multilingualDescriptions = {};
                } else {
                  // Assign the description if available as the default language multilingual description
                  $scope.thesaurus[i].multilingualDescriptions["eng"] =
                    $scope.thesaurus[i].description;
                }
              }
            }
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("thesaurusListError"),
              error: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      }

      loadThesaurus();

      // clear selected keyword on modal close
      $("#keywordModal").on("hide.bs.modal", function () {
        $scope.keywordSelected = null;
      });
    }
  ]);
})();
