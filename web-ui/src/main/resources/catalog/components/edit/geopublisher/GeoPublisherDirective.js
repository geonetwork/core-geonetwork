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
  goog.provide("gn_geopublisher_directive");

  goog.require("gn_owscontext_service");

  /**
   */
  angular
    .module("gn_geopublisher_directive", [
      "gn_owscontext_service",
      "pascalprecht.translate"
    ])
    .directive("gnGeoPublisher", [
      "gnMap",
      "gnOwsContextService",
      "gnOnlinesrc",
      "gnMapsManager",
      "gnGeoPublisher",
      "gnEditor",
      "gnCurrentEdit",
      "$timeout",
      "$translate",
      "$rootScope",
      function (
        gnMap,
        gnOwsContextService,
        gnOnlinesrc,
        gnMapsManager,
        gnGeoPublisher,
        gnEditor,
        gnCurrentEdit,
        $timeout,
        $translate,
        $rootScope
      ) {
        return {
          restrict: "A",
          replace: true,
          templateUrl:
            "../../catalog/components/edit/geopublisher/" + "partials/geopublisher.html",
          scope: {
            config: "@"
          },
          link: function (scope, element, attrs) {
            scope.resources = angular.fromJson(scope.config);
            scope.hidden = true;
            scope.loaded = false;
            scope.hasStyler = false;
            scope.nodes = null;
            scope.gsNode = null;
            var map;
            gnGeoPublisher.getList().then(function (response) {
              var data = response.data;

              if (data != null) {
                scope.nodes = data;
                scope.gsNode = data[0];
              }
            });

            var init = function () {
              map = gnMapsManager.createMap(gnMapsManager.EDITOR_MAP);

              // we need to wait the scope.hidden binding is done
              // before rendering the map.
              map.setTarget(scope.mapId);

              // TODO : Zoom to all extent if more than one defined
              if (
                angular.isArray(gnCurrentEdit.extent) &&
                gnCurrentEdit.extent.length > 0
              ) {
                var mdExtent = gnMap.reprojExtent(
                  gnCurrentEdit.extent[0],
                  "EPSG:4326",
                  gnMap.getMapConfig().projection
                );
                // check that the extent is valid, see #1308
                if (mdExtent.filter(isFinite).length == 4) {
                  map.getView().fit(mdExtent, map.getSize());
                }
              }

              /**
               * Protocols defined for the option
               * 'link service to the metadata'.
               */
              scope.protocols = {
                wms: {
                  checked: true,
                  label: "OGC:WMS"
                },
                wfs: {
                  checked: false,
                  label: "OGC:WFS"
                },
                wcs: {
                  checked: false,
                  label: "OGC:WCS"
                }
              };
            };

            /**
             * Update checkbox for a specific protocol.
             * Just used to manage checkbox in a dropdown.
             */
            scope.setCheckBox = function (p, evt) {
              scope.protocols[p].checked = !scope.protocols[p].checked;
              evt.stopPropagation();
            };

            /**
             * Link the node service as onlinesrc of the metadata,
             * depending on scope.protocols options.
             */
            scope.linkService = function () {
              var snippet = gnOnlinesrc.addFromGeoPublisher(
                scope.wmsLayerName,
                scope.resource.title,
                scope.gsNode,
                scope.protocols
              );

              var snippetRef = gnEditor.buildXMLFieldName(scope.refParent, "gmd:onLine");
              var formId = "#gn-editor-" + gnCurrentEdit.id;
              var form = $(formId);
              if (form) {
                var field = $(
                  '<textarea style="display:none;" name="' +
                    snippetRef +
                    '">' +
                    snippet +
                    "</textarea>"
                );
                form.append(field);
              }

              $timeout(function () {
                gnEditor.save(true).then(
                  function () {
                    // success. Nothing to do.
                  },
                  function (rejectedValue) {
                    $rootScope.$broadcast("StatusUpdated", {
                      title: $translate.instant("runServiceError"),
                      error: rejectedValue,
                      timeout: 0,
                      type: "danger"
                    });
                  }
                );
              });
            };
            scope.openStyler = function () {
              window.open(
                scope.gsNode.stylerurl +
                  "?namespace=" +
                  scope.gsNode.namespacePrefix +
                  "&layer=" +
                  scope.wmsLayerName
              );
            };

            scope.layer = null;
            /**
             * Layer is available, add it to the map
             */
            var readResponse = function (data, action) {
              scope.statusCode = data;
              gnMap
                .addWmsFromScratch(map, scope.gsNode.wmsurl, scope.layerName, false)
                .then(
                  function (o) {
                    if (o.layer) {
                      gnMap.zoomLayerToExtent(o.layer, map);
                      scope.layer = o.layer;
                    }
                  },
                  function (o) {
                    if (o.layer) {
                      gnMap.zoomLayerToExtent(o.layer, map);
                      scope.layer = o.layer;
                    }
                  }
                );
              scope.isPublished = true;
            };

            /**
             * Called on init and on combobox selection.
             * Set gnNode to the current Node and will
             * call checkNode service.
             */
            scope.$watch("gsNode", function (n, o) {
              if (n != o) {
                scope.checkNode(scope.gsNode.id);
                scope.hasStyler = !angular.isArray(scope.gsNode.stylerurl);
              }
            });

            /**
             * Check the status of the selected node.
             * Return an error status if not published or
             * a layer configuration if published.
             */
            scope.checkNode = function () {
              if (angular.isUndefined(scope.name)) {
                return;
              }
              if (scope.layer !== null) {
                map.removeLayer(scope.layer);
                scope.layer = null;
              }
              scope.isPublished = false;
              return gnGeoPublisher.checkNode(scope.gsNode.id, scope.name).then(
                function (r) {
                  if (r.status === 404) {
                    scope.statusCode = r.data.description;
                    scope.isPublished = false;
                  } else {
                    readResponse(r.data, "check");
                  }
                },
                function (r) {
                  // Service returns text.
                  // Extract exception message from this
                  scope.statusCode =
                    r.data && r.data.description ? r.data.description : r.status;
                  scope.isPublished = false;
                }
              );
            };

            /**
             * Publish the layer on the gsNode
             */
            scope.publish = function () {
              return gnGeoPublisher
                .publishNode(
                  scope.gsNode.id,
                  scope.name,
                  scope.resource.title,
                  scope.resource["abstract"]
                )
                .then(
                  function (response) {
                    readResponse(response.data, "publish");
                  },
                  function (response) {
                    scope.statusCode = response.data.description;
                    scope.isPublished = false;
                  }
                );
            };

            /**
             * Unpublish the layer on the gsNode
             */
            scope.unpublish = function () {
              if (scope.layer != null) {
                map.removeLayer(scope.layer);
              }
              return gnGeoPublisher.unpublishNode(scope.gsNode.id, scope.name).then(
                function (response) {
                  scope.statusCode = response.data;
                  scope.isPublished = false;
                },
                function (response) {
                  scope.statusCode = response.data.description;
                  scope.isPublished = false;
                }
              );
            };

            /**
             * Show or hide the panel.
             * If first show, load the map and the whole
             * directive.
             */
            scope.showPanel = function (r) {
              // Improve open/close TODO
              if (r != null) {
                scope.hidden = false;
              }
              scope.mapId = "map-geopublisher";
              // FIXME: only one publisher in a page ?
              scope.ref = r.ref;
              scope.refParent = r.refParent;
              scope.name = r.name;
              scope.resource = r;

              if (!scope.loaded) {
                // Let the div be displayed and then
                // init the map.
                $timeout(function () {
                  init();
                });
                scope.loaded = true;
              }

              // Build layer name based on file name
              scope.layerName = r.name.replace(/.zip$|.gpkg$|.tif$|.tiff$|.ecw$/, "");
              scope.wmsLayerName = scope.layerName;
              if (scope.layerName.match("^jdbc")) {
                scope.wmsLayerName = scope.layerName.split("#")[1];
              } else if (scope.layerName.match("^file")) {
                scope.wmsLayerName = scope.layerName
                  .replace(/.*\//, "")
                  .replace(/.zip$|.gpkg$|.tif$|.tiff$|.ecw$/, "");
              }
            };
          }
        };
      }
    ]);
})();
