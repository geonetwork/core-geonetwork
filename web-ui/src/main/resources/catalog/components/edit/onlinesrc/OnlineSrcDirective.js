/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
  goog.provide("gn_onlinesrc_directive");

  goog.require("ga_print_directive");
  goog.require("gn_utility");
  goog.require("gn_filestore");
  goog.require("gn_urlutils_service");
  goog.require("gn_related_directive");

  var fileUploaderList = [
    "gnOnlinesrc",
    "gnFileStoreService",
    "gnCurrentEdit",
    "$rootScope",
    "$translate",
    function (gnOnlinesrc, gnFileStoreService, gnCurrentEdit, $rootScope, $translate) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/edit/onlinesrc/" + "partials/fileUploader.html",
        scope: {},
        link: function (scope, element, attrs) {
          scope.relations = {};
          scope.uuid = undefined;
          scope.lang = scope.$parent.lang;
          scope.readonly = false;
          scope.numberOfOverviews = parseInt(attrs["numberOfOverviews"]) || Infinity;
          scope.onlinesrcService = gnOnlinesrc;

          scope.defaultType = "thumbnails";
          scope.type = attrs["type"] || scope.defaultType;
          scope.panelMode =
            angular.isDefined(attrs["title"]) && attrs["title"] === "" ? false : true;
          scope.fileTypes = angular.isDefined(attrs["fileTypes"])
            ? attrs["fileTypes"]
            : "";
          scope.protocol = attrs["protocol"] || "WWW:DOWNLOAD";
          scope.isOverview = scope.type === scope.defaultType;
          scope.title = attrs["title"] || (scope.isOverview ? "overview" : "download");
          scope.icon =
            attrs["icon"] || (scope.isOverview ? "gn-icon-thumbnail" : "fa-download");
          scope.btnLabel =
            attrs["btnLabel"] ||
            (scope.isOverview ? "chooseImage" : "chooseFileToUpload");
          scope.removeBtnConfirm = scope.isOverview
            ? "removeThumbnailConfirm"
            : "removeOnlinesrcConfirm";
          scope.removeBtnTitle = scope.isOverview ? "removeThumbnail" : "remove";

          var loadRelations = function () {
            gnOnlinesrc.getAllResources([scope.type]).then(function (data) {
              var res = gnOnlinesrc.formatResources(
                data,
                scope.lang,
                gnCurrentEdit.mdLanguage
              );
              if (angular.isArray(res.relations[scope.type])) {
                scope.relations = scope.isOverview
                  ? res.relations[scope.type]
                  : res.relations[scope.type].filter(function (l) {
                      return l.protocol === scope.protocol;
                    });
              } else {
                scope.relations = {};
              }
            });
          };

          scope.linkUploadedFileToRecord = function (link) {
            var tokens = link.url.split("."),
              params = scope.isOverview
                ? {
                    thumbnail_url: link.url,
                    thumbnail_desc: link.name || "",
                    process: "thumbnail-add",
                    id: gnCurrentEdit.id
                  }
                : {
                    url: link.url,
                    protocol: scope.protocol,
                    process: "onlinesrc-add",
                    id: gnCurrentEdit.id,
                    mimeType: tokens.length ? tokens.pop().toLowerCase() : "",
                    mimeTypeStrategy: "mimeType",
                    name: link.name || link.url.split("/").pop() || ""
                  };
            gnOnlinesrc.add(params);
          };

          scope.removeFile = function (file) {
            var url = file.url[gnCurrentEdit.mdLanguage];
            if (
              url.match(".*/api/records/" + gnCurrentEdit.uuid + "/attachments/.*") ==
              null
            ) {
              // An external URL
              gnOnlinesrc[scope.isOverview ? "removeThumbnail" : "removeOnlinesrc"](
                file
              ).then(function () {
                loadRelations();
              });
            } else {
              // A thumbnail from the filestore
              gnFileStoreService.delete({ url: url }).then(
                function () {
                  // then remove from record
                  gnOnlinesrc[scope.isOverview ? "removeThumbnail" : "removeOnlinesrc"](
                    file
                  ).then(function () {
                    loadRelations();
                  });
                },
                function (r) {
                  // Can be missing in filestore, then remove the dead link from the record
                  gnOnlinesrc[scope.isOverview ? "removeThumbnail" : "removeOnlinesrc"](
                    file
                  ).then(function () {
                    loadRelations();
                  });
                }
              );
            }
          };
          function init(n, o) {
            if (angular.isUndefined(scope.uuid) || n != o) {
              scope.uuid = n;
              loadRelations();
            }
          }
          scope.$watch("gnCurrentEdit.uuid", init);
          scope.$watch("$parent.gnCurrentEdit.uuid", init);
        }
      };
    }
  ];

  /**
   * @ngdoc overview
   * @name gn_onlinesrc
   *
   * @description
   * Provide directives for online resources
   * <ul>
   * <li>gnOnlinesrcList</li>
   * <li>gnAddOnlinesrc</li>
   * <li>gnLinkServiceToDataset</li>
   * <li>gnLinkToMetadata</li>
   * </ul>
   */
  angular
    .module("gn_onlinesrc_directive", [
      "gn_utility",
      "gn_filestore",
      "blueimp.fileupload",
      "ga_print_directive",
      "gn_urlutils_service",
      "gn_related_directive"
    ])
    .directive("gnRemoteRecordSelector", [
      "$http",
      "gnGlobalSettings",
      function ($http, gnGlobalSettings) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/edit/onlinesrc/" +
            "partials/remote-record-selector.html",
          link: function (scope, element, attrs) {
            scope.allowRemoteRecordLink = false;
            if (gnGlobalSettings.gnCfg.mods.editor.allowRemoteRecordLink === false) {
              return;
            } else {
              scope.allowRemoteRecordLink = true;
            }
            scope.remoteRecord = {
              remoteUrl: "",
              title: "",
              uuid: ""
            };
            scope.isRemoteRecordUrlOk = true;
            scope.isRemoteRecordPropertiesExtracted = false;
            scope.selectionList = undefined;

            scope.$on("resetSearch", function (event, args) {
              scope.remoteRecord = {
                remoteUrl: "",
                title: "",
                uuid: ""
              };
            });

            function clearSelection() {
              if (scope.selectionList) {
                scope.selectionList.length = 0;
              }
            }

            function guessContentType() {
              // We may support JSON at some point ?
              return "application/xml";
            }

            function getProperties(doc) {
              scope.isRemoteRecordPropertiesExtracted = true;
              if (angular.isObject(doc)) {
                // JSON doc
              } else if (doc.startsWith("<?xml")) {
                // XML - Support of ISO19139, ISO19110 and ISO19115-3
                try {
                  var parser = new DOMParser(),
                    xml = parser.parseFromString(doc, "text/xml");
                  var titles = xml.evaluate(
                    '//*[local-name(.) = "identificationInfo"]/*' +
                      '/*[local-name(.) = "citation"]/*' +
                      '/*[local-name(.) = "title"]/*/text()|' +
                      '//*[local-name(.) = "FC_FeatureCatalogue"]/*[local-name(.) = "name"]/*/text()',
                    xml,
                    xml.createNSResolver(xml),
                    XPathResult.STRING_TYPE,
                    null
                  );
                  if (titles.stringValue) {
                    scope.remoteRecord.title = titles.stringValue;
                  }

                  var uuid = xml.evaluate(
                    '//*[local-name(.) = "fileIdentifier"]/*/text()|' +
                      '//*[local-name(.) = "metadataIdentifier"]/*/*[local-name(.) = "code"]/*/text()|' +
                      '//*[local-name(.) = "FC_FeatureCatalogue"]/@uuid',
                    xml,
                    xml.createNSResolver(xml),
                    XPathResult.STRING_TYPE,
                    null
                  );
                  if (uuid.stringValue) {
                    scope.remoteRecord.uuid = uuid.stringValue;
                  } else {
                    scope.remoteRecord.uuid = scope.remoteRecord.remoteUrl;
                  }
                } catch (e) {
                  console.warn(e);
                  return false;
                }
              } else if (doc.indexOf("<html") != -1) {
                // Basic support of HTML page eg. GeoNode record page
                // In this case the head/title is considered the record title.
                // No UUID can be easily extracted.
                try {
                  scope.remoteRecord.title = doc.replace(
                    /(.|[\r\n])*<title>(.*)<\/title>(.|[\r\n])*/,
                    "$2"
                  );
                  scope.remoteRecord.uuid = scope.remoteRecord.remoteUrl;

                  if (scope.remoteRecord.title === "") {
                    return false;
                  }
                  // Looking for schema.org tags or json+ld format could also be an option.
                } catch (e) {
                  console.warn(e);
                  return false;
                }
              } else {
                return false;
              }
              return true;
            }

            scope.checkLink = function () {
              scope.resetLink(false);
              if (scope.remoteRecord.remoteUrl.indexOf("http") === 0) {
                return $http
                  .get(scope.remoteRecord.remoteUrl, {
                    headers: { Accept: guessContentType() }
                  })
                  .then(
                    function (response) {
                      scope.isRemoteRecordUrlOk = response.status === 200;
                      if (scope.isRemoteRecordUrlOk) {
                        // Check we can retrieve title
                        scope.isRemoteRecordPropertiesExtracted = getProperties(
                          response.data
                        );
                        if (scope.isRemoteRecordPropertiesExtracted) {
                          scope.updateSelection();
                        }
                      }
                    },
                    function (response) {
                      scope.isRemoteRecordUrlOk = response.status === 500;
                    }
                  );
              }
            };

            scope.updateSelection = function () {
              if (scope.selectionList) {
                scope.selectionList.length = 0;
                scope.selectionList.push(scope.remoteRecord);
              } else if (angular.isFunction(scope.addToSelection)) {
                // sibling mode
                scope.remoteRecord.resourceTitle = scope.remoteRecord.title;
                scope.addToSelection(
                  scope.remoteRecord,
                  scope.config.associationType,
                  scope.config.initiativeType
                );
              }
            };

            scope.resetLink = function (allProperties) {
              scope.selectionList = angular.isDefined(scope.stateObj)
                ? scope.stateObj.selectRecords
                : scope.selectRecords;
              scope.isRemoteRecordUrlOk = true;
              scope.remoteRecord.title = "";
              scope.remoteRecord.uuid = "";
              if (allProperties) {
                scope.remoteRecord.remoteUrl = "";
              }
              clearSelection();
            };
          }
        };
      }
    ])
    /**
     * Simple interface to add or remove file/overview.
     *
     * This directive handle in one step the add to filestore
     * action and the update metadata record steps. User
     * can easily drag & drop file/overview in here.
     *
     * It does not provide the possibility to set
     * name and description. See onlineSrcList directive
     * or full editor mode.
     */
    .directive("gnFileUploader", fileUploaderList)
    .directive("gnOverviewManager", fileUploaderList)

    /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnOnlinesrcList
     *
     * @restrict A
     *
     * @description
     * The `gnOnlinesrcList` directive is used
     * to display the list of
     * all online resources attached to the current metadata.
     * The template will show up a list of all kinds
     * of resource, and
     * links to create new resources of those kinds.
     *
     * The list is shown on directive call, and is
     * refresh on 2 events:
     * <ul>
     *  <li> When the flag onlinesrcService.reload is
     *  set to true, the service
     *    requires a refresh of the list, the directive
     *    here is watching this
     *    value to refresh when it is required.</li>
     *  <li> When the metadata is saved, the
     *  gnCurrentEdit.version is updated and the list
     *  of resources is reloaded.</li>
     * </ul>
     *
     */
    .directive("gnOnlinesrcList", [
      "gnOnlinesrc",
      "gnCurrentEdit",
      "gnRelatedResources",
      "gnConfigService",
      "$filter",
      "gnConfig",
      function (
        gnOnlinesrc,
        gnCurrentEdit,
        gnRelatedResources,
        gnConfigService,
        $filter,
        gnConfig
      ) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/edit/onlinesrc/" + "partials/onlinesrcList.html",
          scope: {
            types: "@"
          },
          link: function (scope, element, attrs) {
            scope.onlinesrcService = gnOnlinesrc;
            scope.gnCurrentEdit = gnCurrentEdit;
            scope.gnRelatedResources = gnRelatedResources;
            scope.allowEdits = true;
            scope.lang = scope.$parent.lang;
            scope.readonly = attrs["readonly"] || false;
            scope.gnCurrentEdit.associatedPanelConfigId = attrs["configId"] || "default";
            scope.relations = [];
            scope.gnCurrentEdit.codelistFilter = attrs["codelistFilter"];
            scope.isMdWorkflowEnableForMetadata =
              gnConfig["metadata.workflow.enable"] &&
              scope.gnCurrentEdit.metadata.draft === "y";
            scope.isDoiApplicableForMetadata =
              gnConfig["system.publication.doi.doienabled"] &&
              scope.gnCurrentEdit.metadata.isTemplate === "n" &&
              scope.gnCurrentEdit.metadata.isPublished() &&
              JSON.parse(scope.gnCurrentEdit.metadata.isHarvested) === false;

            /**
             * Calls service 'relations.get' to load
             * all online resources of the current
             * metadata into the list
             */
            var loadRelations = function () {
              gnOnlinesrc.getAllResources().then(function (data) {
                var res = gnOnlinesrc.formatResources(
                  data,
                  scope.lang,
                  gnCurrentEdit.mdLanguage
                );
                scope.relations = res.relations;
                scope.siblingTypes = scope.siblingTypes;
              });
            };
            scope.isCategoryEnable = function (category) {
              return angular.isUndefined(scope.types)
                ? true
                : category.match(scope.types) !== null;
            };

            /**
             * Doi can be published for a resource if:
             *   - Doi publication is enabled.
             *   - The resource matches doi.org url
             *   - The workflow is not enabled for the metadata and
             *     the metadata is published.
             *
             */
            scope.canPublishDoiForResource = function (resource) {
              var doiKey = gnConfig["system.publication.doi.doikey"];
              return (
                scope.isDoiApplicableForMetadata &&
                resource.lUrl !== null &&
                resource.lUrl.match("doi.org/" + doiKey) !== null &&
                !scope.isMdWorkflowEnableForMetadata
              );
            };

            /**
             * Builds metadata url checking if the resource points to internal or external url.
             *
             * @param resource
             * @returns {string|*}
             */
            scope.buildMetadataLink = function (resource) {
              var baseUrl = gnConfigService.getServiceURL();

              var resourceUrl = resource.url[scope.lang] || resource.url["eng"];

              if (resourceUrl.indexOf(baseUrl) == 0) {
                //return 'catalog.search#/metadata/' + resource.id;
                return "../api/records/" + resource.id;
              } else {
                return resource.url[scope.lang];
              }
            };

            // Reload relations when a directive requires it
            scope.$watch("onlinesrcService.reload", function () {
              if (scope.onlinesrcService.reload) {
                loadRelations();
                scope.onlinesrcService.reload = false;
              }
            });

            loadRelations();

            scope.sortLinksOptions = ["protocol", "lUrl", "title"];
            scope.sortLinksProperty = scope.sortLinksOptions[0];
            scope.sortLinksReverse = true;

            scope.sortLinks = function (g) {
              return (
                $filter("gnLocalized")(g[scope.sortLinksProperty]) ||
                g[scope.sortLinksProperty]
              );
            };

            scope.sortLinksBy = function (p) {
              scope.sortLinksReverse =
                scope.sortLinksProperty !== null && scope.sortLinksProperty === p
                  ? !scope.sortLinksReverse
                  : false;
              scope.sortLinksProperty = p;
            };
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnAddOnlinesrc
     * @restrict A
     * @requires gnOnlinesrc
     * @requires gnOwsCapabilities
     * @requires gnEditor
     * @requires gnCurrentEdit
     *
     * @description
     * The `gnAddOnlinesrc` directive provides a form to add a
     * new online resource
     * to the currend metadata. Depending on the protocol :
     * <ul>
     *  <li>DOWNLOAD : we upload a data from the disk.</li>
     *  <li>OGC:WMS : we call a capabilities on the given url,
     *  then the user can add
     *    several resources (layers) at the same time.</li>
     *  <li>Others : we just fill the form and call a batch processing.</li>
     * </ul>
     *
     * On submit, the metadata is saved, the thumbnail is added, then the form
     * and online resource list are refreshed.
     */
    .directive("gnAddOnlinesrc", [
      "gnOnlinesrc",
      "gnOwsCapabilities",
      "gnWfsService",
      "gnSchemaManagerService",
      "gnEditor",
      "gnCurrentEdit",
      "gnMap",
      "gnMapsManager",
      "gnUrlUtils",
      "gnGlobalSettings",
      "Metadata",
      "$rootScope",
      "$translate",
      "$timeout",
      "$http",
      "$filter",
      "$log",
      "$q",
      function (
        gnOnlinesrc,
        gnOwsCapabilities,
        gnWfsService,
        gnSchemaManagerService,
        gnEditor,
        gnCurrentEdit,
        gnMap,
        gnMapsManager,
        gnUrlUtils,
        gnGlobalSettings,
        Metadata,
        $rootScope,
        $translate,
        $timeout,
        $http,
        $filter,
        $log,
        $q
      ) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/edit/onlinesrc/" + "partials/addOnlinesrc.html",
          link: {
            pre: function preLink(scope) {
              scope.searchObj = {
                internal: true,
                state: { filters: "" },
                params: {}
              };
              scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

              scope.ctrl = {};
            },
            post: function (scope, element, attrs) {
              scope.clearFormOnProtocolChange = !(
                attrs.clearFormOnProtocolChange == "false"
              ); //default to true (old behavior)
              scope.popupid = attrs["gnPopupid"];
              $(scope.popupid).on("hidden.bs.modal", function () {
                scope.$broadcast("onlineSrcDialogHidden", { popupid: scope.popupid });
              });

              scope.config = null;
              scope.linkType = null;

              scope.loaded = false;
              scope.layers = null;
              scope.capabilitiesLayers = null;
              scope.mapId = "gn-thumbnail-maker-map";
              scope.map = null;
              scope.dataFormats = null;

              scope.searchObj = {
                internal: true,
                state: { filters: "" },
                params: {
                  sortBy: "resourceTitleObject.default.sort"
                }
              };

              // This object is used to share value between this
              // directive and the SearchFormController scope that
              // is contained by the directive
              scope.stateObj = {};
              var projectedExtent = null;

              function loadLayers() {
                scope.map.get("creationPromise").then(function () {
                  if (
                    !angular.isArray(scope.map.getSize()) ||
                    scope.map.getSize().indexOf(0) >= 0
                  ) {
                    $timeout(function () {
                      scope.map.updateSize();
                      if (projectedExtent != null) {
                        scope.map.getView().fit(projectedExtent, scope.map.getSize());
                      }
                    }, 300);
                  }
                });

                // Reset map
                angular.forEach(scope.map.getLayers(), function (layer, index) {
                  if (index !== 0) {
                    scope.map.removeLayer(layer);
                  }
                });

                var conf = gnMap.getMapConfig();

                // TODO: Add base layer from config
                // This does not work because createLayerFromProperties
                // return a promise and base layer is added twice.
                // if (conf.useOSM) {
                //   scope.map.addLayer(new ol.layer.Tile({
                //     source:  new ol.source.OSM(),
                //     type: 'base'
                //   }));
                // }
                // else {
                //   conf['map-editor'].layers.forEach(function(layerInfo) {
                //     gnMap.createLayerFromProperties(layerInfo, scope.map)
                //       .then(function(layer) {
                //         if (layer) {
                //           scope.map.addLayer(layer);
                //         }
                //       });
                //   });
                // }
                // Add each WMS layer to the map
                scope.layers = scope.gnCurrentEdit.layerConfig;
                angular.forEach(scope.gnCurrentEdit.layerConfig, function (layer) {
                  scope.map.addLayer(
                    new ol.layer.Tile({
                      source: new ol.source.TileWMS({
                        url: gnUrlUtils.remove(layer.url, ["request"], true),
                        params: {
                          LAYERS: layer.name,
                          URL: gnUrlUtils.remove(layer.url, ["request"], true)
                        }
                      })
                    })
                  );
                });

                var listenerExtent = scope.$watch(
                  "angular.isArray(scope.gnCurrentEdit.extent)",
                  function () {
                    if (angular.isArray(scope.gnCurrentEdit.extent)) {
                      // FIXME : only first extent is took into account
                      var extent =
                        scope.gnCurrentEdit.extent && scope.gnCurrentEdit.extent[0];
                      var proj = ol.proj.get(gnMap.getMapConfig().projection);

                      if (
                        !extent ||
                        !ol.extent.containsExtent(proj.getWorldExtent(), extent)
                      ) {
                        projectedExtent = proj.getExtent();
                      } else {
                        projectedExtent = gnMap.reprojExtent(extent, "EPSG:4326", proj);
                      }
                      scope.map.getView().fit(projectedExtent, scope.map.getSize());

                      //unregister
                      listenerExtent();
                    }
                  }
                );

                // Trigger init of print directive
                scope.mode = "thumbnailMaker";
              }

              scope.generateThumbnail = function () {
                //Added mandatory custom params here to avoid
                //changing other printing services
                jsonSpec = angular.extend(scope.jsonSpec, {
                  hasNoTitle: true
                });

                return $http
                  .put(
                    "../api/records/" +
                      scope.gnCurrentEdit.uuid +
                      "/attachments/print-thumbnail",
                    null,
                    {
                      params: {
                        jsonConfig: angular.fromJson(jsonSpec),
                        rotationAngle: 0
                      }
                    }
                  )
                  .then(function () {
                    $rootScope.$broadcast("gnFileStoreUploadDone");
                  });
              };

              var initThumbnailMaker = function () {
                if (!scope.loaded) {
                  scope.map = gnMapsManager.createMap(
                    gnMapsManager.GENERATE_THUMBNAIL_MAP
                  );

                  // scope.map = new ol.Map({
                  //   layers: [],
                  //   renderer: 'canvas',
                  //   view: new ol.View({
                  //     center: [0, 0],
                  //     projection: gnMap.getMapConfig().projection,
                  //     zoom: 2
                  //   })
                  // });

                  // we need to wait the scope.hidden binding is done
                  // before rendering the map.
                  scope.map.setTarget(scope.mapId);
                  scope.loaded = true;
                }

                scope.$watch("gnCurrentEdit.layerConfig", loadLayers);
              };

              var DEFAULT_CONFIG = {
                sources: {
                  filestore: true
                },
                process: "onlinesrc-add",
                fields: {
                  url: {
                    isMultilingual: false
                  },
                  protocol: {
                    isMultilingual: false
                  },
                  mimeType: {
                    isMultilingual: false
                  },
                  mimeTypeStrategy: {
                    value: "mimeType"
                  },
                  name: {},
                  desc: {},
                  function: {
                    isMultilingual: false
                  }
                }
              };

              // Check which config to load based on the link
              // to edit properties. A match is returned based
              // on link type and config process prefix. If none found
              // return the first config.
              function getTypeConfig(link) {
                for (var i = 0; i < scope.config.types.length; i++) {
                  var c = scope.config.types[i];
                  var p =
                      (c.fields && c.fields.protocol && c.fields.protocol.value) || "",
                    f = (c.fields && c.fields.function && c.fields.function.value) || "",
                    ap =
                      (c.fields &&
                        c.fields.applicationProfile &&
                        c.fields.applicationProfile.value) ||
                      "",
                    nameFieldValue =
                      (c.fields && c.fields.name && c.fields.name.value) || "",
                    // Hardcoded name value in configuration
                    // "fields": {...
                    //   "name": {
                    //     "value": "Other document",
                    //     "hidden": true
                    //   },
                    isNameFieldHidden =
                      (c.fields && c.fields.name && c.fields.name.hidden) || false,
                    hasSameProtocolFunctionAndAppProfile =
                      c.process.indexOf(link.type) === 0 &&
                      p === (link.protocol || "") &&
                      f === (link.function || "") &&
                      ap === (link.applicationProfile || "");
                  if (
                    (hasSameProtocolFunctionAndAppProfile && !isNameFieldHidden) ||
                    (hasSameProtocolFunctionAndAppProfile &&
                      isNameFieldHidden &&
                      nameFieldValue === (link.title[scope.lang] || ""))
                  ) {
                    return c;
                  }
                }

                /* If the schema configuration file for the online
                   resources panel defines a default type, return it
                   instead of DEFAULT_CONFIG

                   schema/config/associated-panel/default.json

                   {"config": {"types": [
                      {
                          "default": true,
                          "label": "addOnlinesrc"
                          ...
                      },
                      {
                          "label": "addThumbnail",
                          ...
                      }]
                  }}
                */
                var defaultSchemaConfigIndex = _.findIndex(
                  scope.config.types,
                  function (t) {
                    return t.default === true;
                  }
                );

                if (defaultSchemaConfigIndex > -1) {
                  return scope.config.types[defaultSchemaConfigIndex];
                } else {
                  return DEFAULT_CONFIG;
                }

                return DEFAULT_CONFIG;
              }

              gnOnlinesrc.register("onlinesrc", function (linkToEditOrType) {
                var linkToEdit = undefined,
                  linkType = undefined;
                if (angular.isDefined(linkToEditOrType)) {
                  if (angular.isObject(linkToEditOrType)) {
                    linkToEdit = linkToEditOrType;
                  } else if (angular.isString(linkToEditOrType)) {
                    linkType = linkToEditOrType;
                  }
                }

                scope.isEditing = angular.isDefined(linkToEdit);
                // Flag used when editing an online resource to prevent the watcher to update the online
                // resource description when loading the dialog.
                scope.processSelectedWMSLayer = false;

                scope.codelistFilter =
                  scope.gnCurrentEdit && scope.gnCurrentEdit.codelistFilter;

                scope.metadataId = gnCurrentEdit.id;
                scope.schema = gnCurrentEdit.schema;

                var init = function () {
                  function getType(linkType) {
                    for (var i = 0; i < scope.config.types.length; i++) {
                      var t = scope.config.types[i];
                      if (t.label === linkType) {
                        return t;
                      }
                    }
                    return scope.config.types[0];
                  }

                  var typeConfig = linkToEdit
                    ? getTypeConfig(linkToEdit)
                    : getType(linkType);

                  scope.dataFormats = gnCurrentEdit.dataFormats;

                  if (gnCurrentEdit.mdOtherLanguages) {
                    scope.mdOtherLanguages = gnCurrentEdit.mdOtherLanguages;
                    scope.mdLangs = JSON.parse(scope.mdOtherLanguages);

                    // not multilingual {"fre":"#"}
                    if (Object.keys(scope.mdLangs).length > 1) {
                      scope.isMdMultilingual = true;
                      scope.mdLang = gnCurrentEdit.mdLanguage;

                      for (var p in scope.mdLangs) {
                        var v = scope.mdLangs[p];
                        if (v.indexOf("#") === 0) {
                          var l = v.substr(1);
                          if (!l) {
                            l = scope.mdLang;
                          }
                          scope.mdLangs[p] = l;
                        }
                      }
                    } else {
                      scope.isMdMultilingual = false;
                    }
                  }

                  scope.config.multilingualFields = [];
                  angular.forEach(typeConfig.fields, function (f, k) {
                    if (scope.isMdMultilingual && f.isMultilingual !== false) {
                      scope.config.multilingualFields.push(k);
                    }
                  });

                  initThumbnailMaker();
                  resetForm();

                  $(scope.popupid).modal("show");

                  if (scope.isEditing) {
                    // If the title object contains more than one value,
                    // Then the record resource is multilingual (and
                    // probably the record also).
                    // scope.isMdMultilingual =
                    //   Object.keys(linkToEdit.title).length > 1 ||
                    //   Object.keys(linkToEdit.description).length > 1;

                    // Create a key which will be sent to XSL processing
                    // for finding which element to edit.
                    var keyName = $filter("gnLocalized")(linkToEdit.title);
                    var keyUrl = $filter("gnLocalized")(linkToEdit.url);
                    if (scope.isMdMultilingual) {
                      // Key in multilingual mode is
                      // the title in the main language
                      keyName = linkToEdit.title[scope.mdLang];
                      keyUrl = linkToEdit.url[scope.mdLang];
                      if (!keyName || !keyUrl) {
                        $log.warn("Failed to compute key for updating the resource.");
                      }
                    }
                    scope.editingKey = [keyUrl, linkToEdit.protocol, keyName].join("");

                    scope.OGCProtocol = checkIsOgc(linkToEdit.protocol);

                    // For multilingual record, build
                    // name and desc based on loc IDs
                    // and no iso3letter code.
                    // If OGC, only take into account, the first element
                    var fields = {
                      name: "title",
                      desc: "description",
                      url: "url"
                    };

                    angular.forEach(fields, function (value, field) {
                      if (scope.isFieldMultilingual(field)) {
                        var e = {};
                        $.each(scope.mdLangs, function (key, v) {
                          e[v] = ""; // default
                          // if key is in the values dictionary
                          if (linkToEdit[fields[field]] && linkToEdit[fields[field]][key])
                            e[v] = linkToEdit[fields[field]][key];
                          // otherwise if v is in values dictionary
                          else if (
                            linkToEdit[fields[field]] &&
                            linkToEdit[fields[field]][v]
                          )
                            e[v] = linkToEdit[fields[field]][v];
                        });
                        fields[field] = e;
                      } else {
                        fields[field] = $filter("gnLocalized")(linkToEdit[fields[field]]);
                      }
                    });

                    scope.params = {
                      linkType: typeConfig,
                      url: fields.url,
                      protocol: linkToEdit.protocol,
                      mimeType: linkToEdit.mimeType,
                      mimeTypeStrategy: "mimeType",
                      name: fields.name,
                      desc: fields.desc,
                      applicationProfile: linkToEdit.applicationProfile,
                      function: linkToEdit.function,
                      selectedLayers: []
                    };
                  } else {
                    scope.editingKey = null;
                    scope.params.linkType = typeConfig;
                    scope.params.protocol = null;
                    scope.params.mimeType = "";
                    scope.mimeTypeStrategy = "mimeType";
                    scope.params.name = "";
                    scope.params.desc = "";
                    initMultilingualFields();
                  }
                  scope.$broadcast("onlineSrcDialogInited", { popupid: scope.popupid });
                };
                function loadConfigAndInit(withInit) {
                  gnSchemaManagerService
                    .getEditorAssociationPanelConfig(
                      gnCurrentEdit.schema,
                      gnCurrentEdit.associatedPanelConfigId
                    )
                    .then(function (r) {
                      scope.config = angular.copy(r.config);
                      scope.config.types = [];
                      for (var i = 0; i < r.config.types.length; i++) {
                        var c = r.config.types[i];
                        if (c.extendWithDataFormats) {
                          var labelPrefix = $translate.instant("recordFormatDownload");
                          for (
                            var j = 0;
                            j < scope.gnCurrentEdit.dataFormats.length;
                            j++
                          ) {
                            var f = scope.gnCurrentEdit.dataFormats[j],
                              option = angular.copy(c);

                            option.label = labelPrefix + f.label;
                            option.fields.protocol.value = f.value;
                            scope.config.types.push(option);
                          }
                        } else {
                          scope.config.types.push(c);
                        }
                      }

                      /**
                       *  Default configuration to handle WMS resources:
                       *
                       *    - resourcename: Add layer names to the name and description fields of the online resources.
                       *    - url: Add layer names to url parameter defined in gnGlobalSettings.gnCfg.mods.search.addWMSLayersToMap.urlLayerParam
                       */
                      if (!scope.config.wmsResources) {
                        scope.config.wmsResources = {};
                        scope.config.wmsResources.addLayerNamesMode = "resourcename";
                      }

                      if (scope.config.wmsResources.addLayerNamesMode == "url") {
                        scope.addLayersInUrl =
                          gnGlobalSettings.gnCfg.mods.search.addWMSLayersToMap
                            .urlLayerParam || "";
                      } else {
                        scope.addLayersInUrl = "";
                      }

                      if (scope.addLayersInUrl == "") {
                        scope.config.wmsResources.addLayerNamesMode = "resourcename";
                      }

                      if (withInit) {
                        init();
                      }
                    });
                }

                scope.$watch("gnCurrentEdit.associatedPanelConfigId", function (n, o) {
                  if (n && n !== o) {
                    loadConfigAndInit(false);
                  }
                });

                loadConfigAndInit(true);
              });

              // mode can be 'url' or 'thumbnailMaker' to init thumbnail panel
              scope.mode = "url";

              // the form parms that will be submited
              scope.params = {};

              // Tells if we need to display layer grid and send
              // layers to the submit
              scope.OGCProtocol = false;

              scope.onlinesrcService = gnOnlinesrc;
              scope.isUrlOk = false;
              scope.setUrl = function (url) {
                scope.params.url = url;
              };

              var resetForm = function () {
                if (scope.params) {
                  scope.params.url = "";
                  scope.params.protocol = "";
                  scope.params.mimeType = "";
                  scope.params.function = "";
                  scope.params.applicationProfile = "";
                  resetProtocol();
                }
              };
              var resetProtocol = function () {
                scope.layers = [];
                scope.capabilitiesLayers = null;
                scope.OGCProtocol = false;
                if (scope.params && !scope.isEditing) {
                  if (scope.clearFormOnProtocolChange) {
                    scope.params.mimeType = "";
                    scope.params.name = "";
                    scope.params.desc = "";
                    initMultilingualFields();
                  } else {
                    initMultilingualFields(["name", "desc"]);
                  }
                  scope.params.selectedLayers = [];
                  scope.params.layers = [];
                }
              };

              //doNotmodifyFields - list of field names
              //   this will NOT update fields in this list.
              var initMultilingualFields = function (doNotModifyFields) {
                scope.config.multilingualFields.forEach(function (f) {
                  if (!doNotModifyFields || !_.includes(doNotModifyFields, f)) {
                    scope.params[f] = {};
                    setParameterValue(f, "");
                  }
                });
              };

              /**
               * Build the multilingual structure if needed for the onlinesrc
               * param (name, desc, url).
               * Struct like {'ger':'', 'eng': ''}
               *
               * @param {String} param
               * @return {*}
               */
              function buildObjectParameter(param) {
                if (angular.isObject(param)) {
                  var name = [];
                  for (var p in param) {
                    name.push(p + "#" + param[p]);
                  }
                  return name.join("|");
                }
                return param;
              }

              /**
               * Set a vlue to a onlinesrc parameter (url, desc, name).
               * Value as string if monolingual, else set to each lang.
               *
               * @param {String} pName name of attribute in `scope.params`
               * @param {string} value of the attribute
               */
              function setParameterValue(pName, value) {
                if (scope.isFieldMultilingual(pName)) {
                  $.each(scope.mdLangs, function (key, v) {
                    scope.params[pName][v] = value;
                  });
                } else {
                  scope.params[pName] = value;
                }
              }

              /**
               *  Add online resource
               *  If it is an upload, then we submit the
               *  form with right content
               *  If it is an URL, we just call a $http.get
               */
              scope.addOnlinesrc = function () {
                scope.config.multilingualFields.forEach(function (f) {
                  scope.params[f] = buildObjectParameter(scope.params[f]);
                });

                var processParams = {};
                angular.forEach(scope.params.linkType.fields, function (value, key) {
                  if (value.param) {
                    processParams[value.param] = scope.params[key];
                  } else {
                    processParams[key] = scope.params[key];
                  }
                });

                if (scope.isEditing) {
                  processParams.updateKey = scope.editingKey;
                }

                // Add list of layers for WMS
                if (scope.params.selectedLayers) {
                  processParams.selectedLayers = scope.params.selectedLayers;
                }
                processParams.process = scope.params.linkType.process;

                processParams.wmsResources = scope.config.wmsResources;
                processParams.addLayersInUrl = scope.addLayersInUrl;

                return scope.onlinesrcService
                  .add(processParams, scope.popupid)
                  .then(function () {
                    resetForm();
                  });
              };

              scope.isWMSProtocol = function () {
                return scope.OGCProtocol == "WMS";
              };

              scope.isWMSProtocolWithLayersInUrl = function () {
                return scope.isWMSProtocol() && scope.addLayersInUrl != "";
              };

              scope.onAddSuccess = function () {
                gnEditor.refreshEditorForm();
                scope.onlinesrcService.reload = true;
              };

              /**
               * loadCurrentLink
               *
               * Call WMS capabilities request with params.url.
               * Update params.layers scope value, that will be also
               * passed to the layers grid directive.
               */
              scope.loadCurrentLink = function (reportError) {
                var withGroupLayers = true;

                // If multilingual or not
                var url = scope.params.url;
                if (angular.isObject(url)) {
                  url = url[scope.ctrl.urlCurLang];
                }

                if (!url) {
                  return $q.reject("");
                }
                if (scope.OGCProtocol) {
                  scope.layers = [];
                  if (scope.OGCProtocol === "WMS") {
                    return gnOwsCapabilities
                      .getWMSCapabilities(url, true)
                      .then(function (capabilities) {
                        scope.layers = [];
                        scope.isUrlOk = true;
                        angular.forEach(capabilities.layers, function (l) {
                          if (
                            withGroupLayers ||
                            (!withGroupLayers && angular.isDefined(l.Name))
                          ) {
                            scope.layers.push(l);
                          }
                        });
                        scope.capabilitiesLayers = capabilities;
                      })
                      .catch(function (error) {
                        scope.isUrlOk = error === 200;
                      });
                  } else if (scope.OGCProtocol === "WMTS") {
                    return gnOwsCapabilities
                      .getWMTSCapabilities(url)
                      .then(function (capabilities) {
                        scope.layers = [];
                        scope.capabilitiesLayers = null;
                        scope.isUrlOk = true;
                        angular.forEach(capabilities.Layer, function (l) {
                          if (angular.isDefined(l.Identifier)) {
                            scope.layers.push({
                              Name: l.Identifier,
                              Title: l.Title
                            });
                          }
                        });
                      })
                      .catch(function (error) {
                        scope.isUrlOk = error === 200;
                      });
                  } else if (scope.OGCProtocol === "WFS") {
                    return gnWfsService
                      .getCapabilities(url)
                      .then(function (capabilities) {
                        scope.layers = [];
                        scope.capabilitiesLayers = null;
                        scope.isUrlOk = true;
                        angular.forEach(
                          capabilities.featureTypeList.featureType,
                          function (l) {
                            if (angular.isDefined(l.name)) {
                              scope.layers.push({
                                Name: l.name.prefix + ":" + l.name.localPart,
                                abstract: angular.isArray(l._abstract)
                                  ? l._abstract[0].value
                                  : l._abstract,
                                Title: angular.isArray(l.title)
                                  ? l.title[0].value
                                  : l.title
                              });
                            }
                          }
                        );
                      })
                      .catch(function (error) {
                        scope.isUrlOk = error === 200;
                      });
                  } else if (scope.OGCProtocol === "WCS") {
                    return gnOwsCapabilities
                      .getWCSCapabilities(url)
                      .then(function (capabilities) {
                        scope.layers = [];
                        scope.isUrlOk = true;
                        angular.forEach(
                          capabilities.contents.coverageSummary,
                          function (l) {
                            if (angular.isDefined(l.identifier)) {
                              scope.layers.push({
                                Name: l.identifier,
                                abstract: angular.isArray(l._abstract)
                                  ? l._abstract[0]["value"]
                                  : "",
                                Title: angular.isArray(l.title)
                                  ? l.title[0]["value"]
                                  : l.identifier
                              });
                            }
                          }
                        );
                      })
                      .catch(function (error) {
                        scope.isUrlOk = error === 200;
                      });
                  }
                } else if (url.indexOf("http") === 0) {
                  return $http.head(scope.onlinesrcService.getApprovedUrl(url)).then(
                    function (response) {
                      scope.isUrlOk = response.status === 200;
                    },
                    function (response) {
                      scope.isUrlOk = response.status === 500;
                    }
                  );
                } else {
                  scope.isUrlOk = true;
                  return $q.reject("");
                }
              };

              function checkIsOgc(protocol) {
                if (scope.config.loadMapCapabilities !== "false") {
                  if (protocol && protocol.indexOf("OGC:WMS") >= 0) {
                    return "WMS";
                  } else if (protocol && protocol.indexOf("OGC:WFS") >= 0) {
                    return "WFS";
                  } else if (protocol && protocol.indexOf("OGC:WMTS") >= 0) {
                    return "WMTS";
                  } else if (protocol && protocol.indexOf("OGC:WCS") >= 0) {
                    return "WCS";
                  }
                }

                return null;
              }

              var processSelectedWMSLayers = function () {
                // Only in layer tree widget
                if (scope.isWMSProtocolWithLayersInUrl()) {
                  // Get the selected layers
                  var selectedLayersNames = [];

                  var params = gnUrlUtils.parseKeyValue(scope.params.url.split("?")[1]);

                  if (params[scope.addLayersInUrl]) {
                    scope.params.selectedLayers = [];
                    selectedLayersNames = params[scope.addLayersInUrl].split(",");
                  }

                  scope.layers &&
                    scope.layers.forEach &&
                    scope.layers.forEach(function (l) {
                      if (selectedLayersNames.indexOf(l.Name) != -1) {
                        scope.params.selectedLayers.push(l);
                      }
                    });

                  scope.params.url = gnUrlUtils.remove(
                    scope.params.url,
                    [scope.addLayersInUrl],
                    true
                  );
                } else {
                  var selectedLayersNames = angular.isObject(scope.params.name)
                    ? []
                    : scope.params.name.split(",");
                  scope.params.selectedLayers = [];
                  scope.layers &&
                    scope.layers.forEach &&
                    scope.layers.forEach(function (l) {
                      if (selectedLayersNames.indexOf(l.Name) != -1) {
                        scope.params.selectedLayers.push(l);
                      }
                    });
                }
              };

              /**
               * On protocol combo Change.
               * Update OGCProtocol values to display or hide
               * layer grid and call or not a getCapabilities.
               */
              scope.$watch("params.protocol", function (n, o) {
                if (!angular.isUndefined(scope.params.protocol) && o !== n) {
                  resetProtocol();
                  scope.OGCProtocol = checkIsOgc(scope.params.protocol);
                  if (
                    scope.OGCProtocol != null &&
                    !scope.isEditing &&
                    scope.clearFormOnProtocolChange
                  ) {
                    // Reset parameter in case of multilingual metadata
                    // Those parameters are object.
                    scope.params.mimeType = "";
                    scope.params.name = "";
                    scope.params.desc = "";
                  }
                  if (
                    scope.params.function === "" &&
                    scope.params.protocol &&
                    scope.params.protocol.indexOf("DOWNLOAD") !== -1
                  ) {
                    scope.params.function = "download";
                  }

                  scope.loadCurrentLink().then(function () {
                    processSelectedWMSLayers();
                  });
                }
              });

              /**
               * On URL change, reload WMS capabilities
               * if the protocol is WMS
               */
              var updateImageTag = function () {
                scope.isImage = false;
                var urls = scope.params.url;
                var curUrl = angular.isObject(urls) ? urls[scope.ctrl.urlCurLang] : urls;

                if (curUrl) {
                  scope.loadCurrentLink().then(function () {
                    // Editing an online resource after saving the metadata doesn't trigger the params.protocol watcher
                    processSelectedWMSLayers();
                  });
                  scope.isImage = curUrl.match(/.*.(png|jpg|jpeg|gif)$/i);
                }
              };
              scope.$watch("params.url", updateImageTag, true);
              scope.$watch("ctrl.urlCurLang", updateImageTag, true);

              /**
               * Concat layer names and title in params names
               * and desc fields.
               * XSL processing tokenize thoses fields and add
               * them to the record.
               */
              scope.$watchCollection("params.selectedLayers", function (n, o) {
                if (
                  scope.config &&
                  scope.config.wmsResources.addLayerNamesMode != "resourcename"
                ) {
                  return;
                }

                if (
                  o !== n &&
                  scope.params.selectedLayers &&
                  scope.params.selectedLayers.length > 0
                ) {
                  // To avoid setting the online resource description to the WMS layer description, when loading
                  // the dialog to edit it, so it is preserved the value from the online resource description.
                  if (scope.isEditing && !scope.processSelectedWMSLayer) {
                    scope.processSelectedWMSLayer = true;
                    return;
                  }

                  var names = [],
                    descs = [];

                  angular.forEach(scope.params.selectedLayers, function (layer) {
                    names.push(layer.Name || layer.name);
                    descs.push(layer.Title || layer.title);
                  });

                  if (scope.isMdMultilingual) {
                    var langCode = scope.mdLangs[scope.mdLang];
                    scope.params.name[langCode] = names.join(",");
                    scope.params.desc[langCode] = descs.join(",");
                  } else {
                    angular.extend(scope.params, {
                      name: names.join(","),
                      desc: descs.join(",")
                    });
                  }
                }
              });

              /**
               * Init link based on linkType configuration.
               * Reset metadata store search, set defaults.
               */
              scope.$watch("params.linkType", function (newValue, oldValue) {
                if (newValue !== oldValue) {
                  scope.config.multilingualFields = [];
                  angular.forEach(newValue.fields, function (f, k) {
                    if (f.isMultilingual !== false) {
                      scope.config.multilingualFields.push(k);
                    }
                  });

                  if (!scope.isEditing) {
                    resetForm();
                    initMultilingualFields();
                  }

                  if (newValue.sources && newValue.sources.metadataStore) {
                    scope.$broadcast(
                      "resetSearch",
                      newValue.sources.metadataStore.params
                    );
                  }

                  if (!scope.isEditing && angular.isDefined(newValue.fields)) {
                    angular.forEach(newValue.fields, function (val, key) {
                      if (angular.isDefined(val.value)) {
                        scope.params[key] = val.value;
                      }
                    });
                  }
                  // Set a default label
                  if (!scope.isEditing && angular.isDefined(newValue.copyLabel)) {
                    setParameterValue(
                      newValue.copyLabel,
                      $translate.instant(newValue.label)
                    );
                  }

                  if (newValue.sources && newValue.sources.thumbnailMaker) {
                    loadLayers();
                  }
                }
              });

              /**
               * Update url and name from uploaded resource.
               * Triggered on file store selection change.
               */
              scope.selectUploadedResource = function (res) {
                if (res && res.url) {
                  var o = {
                    name: decodeURI(res.id.split("/").splice(2).join("/")),
                    url: res.url
                  };
                  ["url", "name"].forEach(function (pName) {
                    setParameterValue(pName, o[pName]);
                  });
                  scope.params.protocol = scope.params.protocol || "WWW:DOWNLOAD";
                }
              };

              scope.$watchCollection("stateObj.selectRecords", function (n, o) {
                if (
                  !angular.isUndefined(scope.stateObj.selectRecords) &&
                  scope.stateObj.selectRecords.length > 0 &&
                  n !== o
                ) {
                  scope.metadataLinks = [];
                  scope.metadataTitle = "";
                  var md = new Metadata(scope.stateObj.selectRecords[0]);
                  var links = md.getLinksByType();
                  setParameterValue("desc", md.resourceTitle);
                  if (angular.isArray(links) && links.length === 1) {
                    scope.params.url = links[0].url;
                  } else {
                    scope.metadataLinks = links;
                    scope.metadataTitle = md.resourceTitle;
                  }
                }
              });

              scope.isFieldMultilingual = function (field) {
                return (
                  scope.isMdMultilingual &&
                  scope.config.multilingualFields &&
                  scope.config.multilingualFields.indexOf(field) >= 0
                );
              };
            }
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnLinkServiceToDataset
     * @restrict A
     * @requires gnOnlinesrc
     * @requires gnOwsCapabilities
     * @requires Metadata
     * @requires gnCurrentEdit
     *
     * @description
     * The `gnLinkServiceToDataset` directive provides a
     * form to either add a service
     * to a metadata of type dataset, or to add a dataset to a
     * metadata of service.
     * The process will update both of the metadatas, the current
     * one and the one it
     * is linked to.
     *
     * On submit, the metadata is saved, the thumbnail is added, then the form
     * and online resource list are refreshed.
     */
    .directive("gnLinkServiceToDataset", [
      "gnOnlinesrc",
      "Metadata",
      "gnOwsCapabilities",
      "gnCurrentEdit",
      "$rootScope",
      "$translate",
      "gnGlobalSettings",
      "gnConfigService",
      function (
        gnOnlinesrc,
        Metadata,
        gnOwsCapabilities,
        gnCurrentEdit,
        $rootScope,
        $translate,
        gnGlobalSettings,
        gnConfigService
      ) {
        return {
          restrict: "A",
          scope: {},
          templateUrl:
            "../../catalog/components/edit/onlinesrc/" +
            "partials/linkServiceToDataset.html",
          compile: function compile(tElement, tAttrs, transclude) {
            return {
              pre: function preLink(scope) {
                scope.searchObj = {
                  internal: true,
                  params: {
                    isTemplate: "n"
                  }
                };
                scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
              },
              post: function postLink(scope, iElement, iAttrs) {
                scope.mode = iAttrs["gnLinkServiceToDataset"];
                scope.popupid = "#linkto" + scope.mode + "-popup";
                scope.alertMsg = null;
                scope.layerSelectionMode = "multiple";
                scope.onlineSrcLink = "";
                scope.addOnlineSrcInDataset = true;

                gnOnlinesrc.register(scope.mode, function () {
                  $(scope.popupid).modal("show");

                  // parameters of the online resource form
                  scope.srcParams = { selectedLayers: [] };

                  var searchParams = {
                    isTemplate: "n"
                  };
                  if (scope.mode === "service") {
                    searchParams.type = scope.mode;
                  } else {
                    // Any records which are not services
                    // ie. dataset, series, ...
                    searchParams["-type"] = "service";
                  }
                  scope.$broadcast("resetSearch", searchParams);
                  scope.layers = [];

                  // Load service layers on load
                  if (scope.mode !== "service") {
                    // If linking a dataset and the service is a WMS
                    // List all layers. The service WMS link can be added
                    // as online source in the target dataset.
                    // TODO: list all URLs if many
                    // TODO: If service URL is added, user need to reload
                    // editor to get URL or current record.
                    var links = [];
                    links = links.concat(
                      gnCurrentEdit.metadata.getLinksByType("ogc", "atom")
                    );
                    if (links.length > 0) {
                      scope.onlineSrcLink = links[0].url;
                      scope.srcParams.protocol = links[0].protocol || "";
                      scope.loadCurrentLink(scope.onlineSrcLink);
                      scope.srcParams.url = scope.onlineSrcLink;
                      scope.srcParams.uuidSrv = gnCurrentEdit.uuid;

                      scope.addOnlineSrcInDataset = true;
                    } else {
                      scope.alertMsg = $translate.instant("linkToServiceWithoutURLError");

                      // If no WMS found, suggest to add a link to the service landing page
                      // Default is false.
                      // Build a link to the service metadata record
                      scope.onlineSrcLink =
                        gnConfigService.getServiceURL() +
                        "api/records/" +
                        gnCurrentEdit.uuid;

                      scope.addOnlineSrcInDataset = false;
                    }
                  }
                });

                // This object is used to share value between this
                // directive and the SearchFormController scope that
                // is contained by the directive
                scope.stateObj = {};
                scope.currentMdTitle = null;

                /**
                 * loadCurrentLink
                 *
                 * Call WMS capabilities on the service metadata URL.
                 * Update params.layers scope value, that will be also
                 * passed to the layers grid directive.
                 */
                scope.loadCurrentLink = function (url) {
                  scope.alertMsg = null;

                  var serviceType = scope.srcParams.protocol.toLowerCase();
                  if (serviceType.indexOf("ogc") !== -1) {
                    return gnOwsCapabilities[
                      serviceType.indexOf("wfs") !== -1
                        ? "getWFSCapabilities"
                        : "getWMSCapabilities"
                    ](url).then(function (capabilities) {
                      scope.layers = [];
                      scope.srcParams.selectedLayers = [];
                      if (capabilities.Layer) {
                        scope.layers.push(capabilities.Layer[0]);
                        angular.forEach(scope.layers[0].Layer, function (l) {
                          scope.layers.push(l);
                          // TODO: We may have more than one level
                        });
                      } else if (capabilities.featureTypeList) {
                        angular.forEach(
                          capabilities.featureTypeList.featureType,
                          function (l) {
                            var name = l.name.prefix + ":" + l.name.localPart;
                            var layer = {
                              Name: name,
                              Title: l.title || name,
                              abstract: l.abstract || ""
                            };

                            scope.layers.push(layer);
                          }
                        );
                      }
                    });
                  }
                };

                /**
                 * Watch the result metadata selection change.
                 * selectRecords is a value of the SearchFormController scope.
                 * On service metadata selection, check if the service has
                 * a WMS URL and send request if yes (then display
                 * layers grid).
                 */
                scope.$watchCollection("stateObj.selectRecords", function () {
                  scope.currentMdTitle = null;
                  if (
                    !angular.isUndefined(scope.stateObj.selectRecords) &&
                    scope.stateObj.selectRecords.length > 0
                  ) {
                    var md = new Metadata(scope.stateObj.selectRecords[0]);
                    scope.currentMdTitle = md.resourceTitle;
                    if (scope.mode === "service") {
                      var links = [];
                      scope.layers = [];
                      scope.srcParams.selectedLayers = [];

                      links = links.concat(md.getLinksByType("ogc", "atom"));
                      scope.srcParams.uuidSrv = md.uuid;
                      scope.srcParams.datasetTitle = gnCurrentEdit.mdTitle;
                      scope.srcParams.identifier =
                        gnCurrentEdit.metadata.identifier &&
                        gnCurrentEdit.metadata.identifier[0]
                          ? gnCurrentEdit.metadata.identifier[0]
                          : "";
                      scope.srcParams.uuidDS = gnCurrentEdit.uuid;
                      //the uuid of the source catalog (harvester)
                      scope.srcParams.source = gnCurrentEdit.metadata.source;

                      scope.srcParams.remote = false;
                      if (links.length > 0) {
                        scope.onlineSrcLink = links[0].url;
                        scope.srcParams.protocol = links[0].protocol || "OGC:WMS";
                        scope.loadCurrentLink(scope.onlineSrcLink);
                        scope.srcParams.url = scope.onlineSrcLink;
                        scope.addOnlineSrcInDataset = true;
                      } else if (md.remoteUrl) {
                        scope.srcParams.name = md.title;
                        scope.srcParams.desc = "";
                        scope.srcParams.protocol = "WWW:LINK";
                        scope.srcParams.remote = true;
                        scope.onlineSrcLink = md.remoteUrl;
                        scope.srcParams.url = scope.onlineSrcLink;
                        scope.addOnlineSrcInDataset = true;
                      } else {
                        scope.srcParams.name = scope.currentMdTitle;
                        scope.srcParams.desc = scope.currentMdTitle;
                        scope.srcParams.protocol = "WWW:LINK-1.0-http--link";
                        scope.onlineSrcLink =
                          gnConfigService.getServiceURL() + "api/records/" + md.uuid;
                        scope.srcParams.url = scope.onlineSrcLink;
                        scope.addOnlineSrcInDataset = false;
                      }
                    } else {
                      var isRemote = angular.isDefined(md.remoteUrl);
                      scope.srcParams.uuidDS = md.uuid;
                      scope.srcParams.remote = isRemote;
                      scope.srcParams.name = isRemote ? md.title : gnCurrentEdit.mdTitle;
                      scope.srcParams.desc = gnCurrentEdit.mdTitle;
                      scope.srcParams.protocol = "WWW:LINK-1.0-http--link";
                      scope.srcParams.url = isRemote ? md.remoteUrl : scope.onlineSrcLink;
                      scope.srcParams.identifier =
                        md.identifier && md.identifier[0] ? md.identifier[0] : "";
                      scope.srcParams.source = md.source;
                    }
                  }
                });

                /**
                 * Call 2 services:
                 *  - link a dataset to a service
                 *  - link a service to a dataset
                 * Hide modal on success.
                 */
                scope.linkTo = function (addOnlineSrcInDataset) {
                  if (scope.mode === "service") {
                    return gnOnlinesrc.linkToService(
                      scope.srcParams,
                      scope.popupid,
                      addOnlineSrcInDataset
                    );
                  } else {
                    return gnOnlinesrc.linkToDataset(
                      scope.srcParams,
                      scope.popupid,
                      addOnlineSrcInDataset
                    );
                  }
                };
              }
            };
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnLinkToMetadata
     * @restrict A
     * @requires gnOnlinesrc
     * @requires $translate
     *
     * @description
     * The `gnLinkServiceToDataset` directive provides
     * a form to link one metadata to
     * another as :
     * <ul>
     *  <li>parent</li>
     *  <li>feature catalog</li>
     *  <li>source dataset</li>
     * </ul>
     * The directive contains a search form allowing one local selection.
     *
     * On submit, the metadata is saved, the link is added,
     * then the form and online resource list are refreshed.
     */
    .directive("gnLinkToMetadata", [
      "gnOnlinesrc",
      "$translate",
      "gnGlobalSettings",
      function (gnOnlinesrc, $translate, gnGlobalSettings) {
        return {
          restrict: "A",
          scope: {},
          templateUrl:
            "../../catalog/components/edit/onlinesrc/" + "partials/linkToMd.html",
          compile: function compile(tElement, tAttrs, transclude) {
            return {
              pre: function preLink(scope) {
                scope.searchObj = {
                  internal: true,
                  any: "",
                  params: {}
                };
                scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
              },
              post: function postLink(scope, iElement, iAttrs) {
                scope.mode = iAttrs["gnLinkToMetadata"];
                scope.popupid = "#linkto" + scope.mode + "-popup";
                scope.btn = {};

                scope.updateParams = function () {
                  scope.searchObj.params.any = scope.searchObj.any;
                };

                /**
                 * Register a method on popup open to reset
                 * the search form and trigger a search.
                 */
                gnOnlinesrc.register(scope.mode, function () {
                  $(scope.popupid).modal("show");
                  var searchParams = {};
                  if (scope.mode === "fcats") {
                    searchParams = {
                      resourceType: "featureCatalog",
                      isTemplate: "n"
                    };
                    scope.btn = {
                      label: $translate.instant("linkToFeatureCatalog")
                    };
                  } else if (scope.mode === "parent") {
                    searchParams = {
                      isTemplate: "n"
                    };
                    scope.btn = {
                      label: $translate.instant("linkToParent")
                    };
                  } else if (scope.mode === "source") {
                    searchParams = {
                      isTemplate: "n"
                    };
                    scope.btn = {
                      label: $translate.instant("linkToSource")
                    };
                  }
                  scope.$broadcast("resetSearch", searchParams);
                });

                scope.gnOnlinesrc = gnOnlinesrc;
              }
            };
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_onlinesrc.directive:gnLinkToSibling
     * @restrict A
     * @requires gnOnlinesrc
     *
     * @description
     * The `gnLinkToSibling` directive provides a form to link siblings to the
     * current metadata. The user need to specify Association type and
     * Initiative type
     * to be able to add a metadata to his selection. The process allow
     * a multiple selection.
     *
     * On submit, the metadata is saved, the resource is associated,
     * then the form
     * and online resource list are refreshed.
     */
    .directive("gnLinkToSibling", [
      "gnOnlinesrc",
      "gnGlobalSettings",
      function (gnOnlinesrc, gnGlobalSettings) {
        return {
          restrict: "A",
          scope: {},
          templateUrl:
            "../../catalog/components/edit/onlinesrc/" + "partials/linktosibling.html",
          compile: function compile(tElement, tAttrs, transclude) {
            return {
              pre: function preLink(scope) {
                scope.ctrl = {};
                scope.searchObj = {
                  internal: true,
                  any: "",
                  defaultParams: {
                    any: "",
                    isTemplate: "n",
                    from: 1,
                    to: 50
                  }
                };
                scope.searchObj.params = angular.extend(
                  {},
                  scope.searchObj.defaultParams
                );

                // Define configuration to restrict search
                // to a subset of records when an initiative type
                // and/or association type is selected.
                // eg. crossReference-study restrict to DC records
                // using _schema=dublin-core
                scope.searchParamsPerType = {
                  //'crossReference-study': {
                  //  _schema: 'dublin-core'
                  //},
                  //'crossReference-*': {
                  //  _isHarvested: 'n'
                  //}
                };

                scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
              },
              post: function postLink(scope, iElement, iAttrs) {
                scope.popupid = iAttrs["gnLinkToSibling"];

                /**
                 * Register a method on popup open to reset
                 * the search form and trigger a search.
                 */
                gnOnlinesrc.register("sibling", function (config) {
                  if (config && !angular.isObject(config)) {
                    config = angular.fromJson(config);
                  }

                  scope.config = {
                    associationTypeForced: angular.isDefined(
                      config && config.associationType
                    ),
                    associationType: (config && config.associationType) || null,
                    initiativeTypeForced: angular.isDefined(
                      config && config.initiativeType
                    ),
                    initiativeType: (config && config.initiativeType) || null
                  };

                  $(scope.popupid).modal("show");

                  scope.$broadcast("resetSearch");
                  scope.selection = [];
                });

                // Clear the search params and input
                scope.clearSearch = function () {
                  $("#siblingdd input").val("");
                  scope.$broadcast("resetSearch");
                };

                // Append * for like search
                scope.updateParams = function () {
                  if (scope.searchObj.any == "") {
                    scope.$broadcast("resetSearch");
                  } else {
                    var addWildcard =
                      scope.searchObj.any.indexOf('"') === -1 &&
                      scope.searchObj.any.indexOf("*") === -1 &&
                      scope.searchObj.any.indexOf("q(") !== 0;
                    scope.searchObj.params.any = addWildcard
                      ? "*" + scope.searchObj.any + "*"
                      : scope.searchObj.any;
                  }
                };

                // Based on initiative type and association type
                // define custom search parameter and refresh search
                var setSearchParamsPerType = function () {
                  var p =
                    scope.searchParamsPerType[
                      scope.config.associationType + "-" + scope.config.initiativeType
                    ];
                  var pall =
                    scope.searchParamsPerType[scope.config.associationType + "-*"];
                  scope.searchObj.params = angular.extend(
                    {},
                    scope.searchObj.defaultParams,
                    angular.isDefined(p) ? p : angular.isDefined(pall) ? pall : {}
                  );
                  scope.$broadcast("resetSearch", scope.searchObj.params);
                };

                scope.config = {
                  associationType: null,
                  initiativeType: null
                };

                scope.$watchCollection("config", function (n, o) {
                  if (n && n !== o) {
                    setSearchParamsPerType();
                  }
                });

                /**
                 * Search a metadata record into the selection.
                 * Return the index or -1 if not present.
                 */
                var findObj = function (md) {
                  for (i = 0; i < scope.selection.length; ++i) {
                    if (scope.selection[i].md === md) {
                      return i;
                    }
                  }
                  return -1;
                };

                /**
                 * Add the result metadata to the selection.
                 * Add it only it associationType & initiativeType are set.
                 * If the metadata alreay exists, it override it with the new
                 * given associationType/initiativeType.
                 */
                scope.addToSelection = function (md, associationType, initiativeType) {
                  if (associationType) {
                    var idx = findObj(md);
                    if (idx < 0) {
                      scope.selection.push({
                        md: md,
                        associationType: associationType,
                        initiativeType: initiativeType || ""
                      });
                    } else {
                      angular.extend(scope.selection[idx], {
                        associationType: associationType,
                        initiativeType: initiativeType || ""
                      });
                    }
                  }
                };

                /**
                 * Remove a record from the selection
                 */
                scope.removeFromSelection = function (obj) {
                  var idx = findObj(obj.md);
                  if (idx >= 0) {
                    scope.selection.splice(idx, 1);
                  }
                };

                /**
                 * Call the batch process to add the sibling
                 * to the current edited metadata.
                 */
                scope.linkToResource = function () {
                  var uuids = [];
                  for (i = 0; i < scope.selection.length; ++i) {
                    var obj = scope.selection[i],
                      parameter =
                        obj.md.uuid +
                        "#" +
                        obj.associationType +
                        "#" +
                        obj.initiativeType +
                        "#" +
                        // Avoid to have separators in title.
                        // Otherwise would need to change API
                        (obj.md.title === undefined
                          ? ""
                          : obj.md.title.replaceAll("#", "").replaceAll(",", " ")) +
                        "#" +
                        (obj.md.remoteUrl === undefined
                          ? ""
                          : obj.md.remoteUrl.replaceAll("#", "%23"));
                    uuids.push(parameter);
                  }
                  var params = {
                    initiativeType: scope.config.initiativeType,
                    associationType: scope.config.associationType,
                    uuids: uuids.join(",")
                  };
                  return gnOnlinesrc.linkToSibling(params, scope.popupid);
                };
              }
            };
          }
        };
      }
    ]);
})();
