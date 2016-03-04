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
  goog.provide('gn_geopublisher_directive');

  goog.require('gn_owscontext_service');

  /**
   */
  angular.module('gn_geopublisher_directive',
      ['gn_owscontext_service'])
  .directive('gnGeoPublisher', [
        'gnMap',
        'gnOwsContextService',
        'gnOnlinesrc',
        'gnGeoPublisher',
        'gnEditor',
        'gnCurrentEdit',
        '$timeout',
        '$translate',
        function(gnMap, gnOwsContextService, gnOnlinesrc,
            gnGeoPublisher, gnEditor, gnCurrentEdit,
            $timeout, $translate) {
          return {
            restrict: 'A',
            replace: true,
            templateUrl: '../../catalog/components/edit/geopublisher/' +
                'partials/geopublisher.html',
            scope: {
              config: '@'
            },
            link: function(scope, element, attrs) {
              scope.resources = angular.fromJson(scope.config);
              scope.hidden = true;
              scope.loaded = false;
              scope.hasStyler = false;
              scope.nodes = null;

              var map, gsNode;
              gnGeoPublisher.getList().success(function(data) {
                if (data != null) {
                  scope.nodes = data;
                  scope.nodeId = data[0].id;
                }
              });

              var init = function() {
                map = new ol.Map({
                  layers: [
                    gnMap.getLayersFromConfig()
                  ],
                  renderer: 'canvas',
                  view: new ol.View({
                    center: [0, 0],
                    projection: gnMap.getMapConfig().projection,
                    zoom: 2
                  })
                });

                //Uses configuration from database
                if (gnMap.getMapConfig().context) {
                  gnOwsContextService.
                      loadContextFromUrl(gnMap.getMapConfig().context,
                          map, true);
                }

                scope.selectNode(scope.nodeId);
                // we need to wait the scope.hidden binding is done
                // before rendering the map.
                map.setTarget(scope.mapId);

                // TODO : Zoom to all extent if more than one defined
                if (angular.isArray(gnCurrentEdit.extent) &&
                    gnCurrentEdit.extent.length > 0) {
                  var mdExtent = gnMap.reprojExtent(gnCurrentEdit.extent[0],
                      'EPSG:4326', gnMap.getMapConfig().projection);
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
                    label: 'OGC:WMS'
                  },
                  wfs: {
                    checked: false,
                    label: 'OGC:WFS'
                  },
                  wcs: {
                    checked: false,
                    label: 'OGC:WCS'
                  }
                };
              };

              /**
               * Update checkbox for a specific protocol.
               * Just used to manage checkbox in a dropdown.
               */
              scope.setCheckBox = function(p, evt) {
                scope.protocols[p].checked = !scope.protocols[p].checked;
                evt.stopPropagation();
              };

              /**
               * Link the node service as onlinesrc of the metadata,
               * depending on scope.protocols options.
               */
              scope.linkService = function() {
                var snippet =
                    gnOnlinesrc.addFromGeoPublisher(scope.wmsLayerName,
                    scope.resource.title,
                    gsNode, scope.protocols);

                var snippetRef = gnEditor.buildXMLFieldName(
                    scope.refParent, 'gmd:onLine');
                var formId = '#gn-editor-' + gnCurrentEdit.id;
                var form = $(formId);
                if (form) {
                  var field = $('<textarea style="display:none;" name="' +
                      snippetRef + '">' +
                      snippet + '</textarea>');
                  form.append(field);
                }

                $timeout(function() {
                  gnEditor.save(true);
                });
              };
              scope.openStyler = function() {
                window.open(gsNode.stylerUrl +
                    '?namespace=' + gsNode.namespacePrefix +
                    '&layer=' + scope.wmsLayerName);
              };

              /**
               * Add the layer of the node to the current
               * map.
               */
              var addLayerToMap = function(layer) {
                // TODO: drop existing layer before adding new
                map.addLayer(new ol.layer.Tile({
                  source: new ol.source.TileWMS({
                    url: gsNode.wmsUrl,
                    params: {
                      'LAYERS': scope.wmsLayerName
                    }
                  })
                }));
              };

              /**
               * Read geopublisher service repsonse.
               * Add, remove a layer depending of the case.
               * Update status.
               */
              var readResponse = function(data, action) {
                if (data['@status'] == '404') {
                  scope.statusCode = $translate('datasetNotFound');

                  if (scope.isPublished) {
                    map.getLayerGroup().getLayers().pop();
                    scope.statusCode = $translate('unpublishSuccess');
                  }
                  scope.isPublished = false;
                }
                else if (angular.isObject(data.layer)) {
                  gnMap.addWmsFromScratch(map,
                      gsNode.wmsUrl, data.layer.name, false).
                      then(function(layer) {
                        gnMap.zoomLayerToExtent(layer, map);
                      });
                  scope.isPublished = true;
                  if (action == 'check') {
                    scope.statusCode = $translate('datasetFound');
                  } else if (action == 'publish') {
                    scope.statusCode = $translate('publishSuccess');
                  }
                } else if (data['status'] !== '') {
                  if (scope.isPublished) {
                    map.getLayerGroup().getLayers().pop();
                  }
                  scope.statusCode = data['status'];
                  scope.isPublished = false;
                }
              };

              /**
               * Retrieve a node from scope.nodes value
               * by its id.
               */
              var getNodeById = function(id) {
                for (i = 0; i < scope.nodes.length; ++i) {
                  if (scope.nodes[i].id == id) {
                    return scope.nodes[i];
                  }
                }
                return undefined;
              };

              /**
               * Called on init and on combobox selection.
               * Set gnNode to the current Node and will
               * call checkNode service.
               */
              scope.selectNode = function(nodeId) {
                gsNode = getNodeById(nodeId);
                scope.checkNode(nodeId);
                scope.hasStyler = !angular.isArray(gsNode.stylerUrl);
              };

              /**
               * Check the status of the selected node.
               * Return an error status if not published or
               * a layer configuration if published.
               */
              scope.checkNode = function(nodeId) {
                if (scope.isPublished) {
                  map.getLayerGroup().getLayers().pop();
                }
                scope.isPublished = false;
                var p = gnGeoPublisher.checkNode(nodeId, scope.name);
                if (p) {
                  p.success(function(data) {
                    readResponse(data, 'check');
                  });
                }
              };

              /**
               * Publish the layer on the gsNode
               */
              scope.publish = function(nodeId) {
                var p = gnGeoPublisher.publishNode(nodeId,
                    scope.name,
                    scope.resource.title,
                    scope.resource['abstract']);
                if (p) {
                  p.success(function(data) {
                    readResponse(data, 'publish');
                  });
                }
              };

              /**
               * Unpublish the layer on the gsNode
               */
              scope.unpublish = function(nodeId) {
                var p = gnGeoPublisher.unpublishNode(nodeId, scope.name);
                if (p) {
                  p.success(readResponse);
                }
              };

              /**
               * Show or hide the panel.
               * If first show, load the map and the whole
               * directive.
               */
              scope.showPanel = function(r) {
                // Improve open/close TODO
                if (r != null) {
                  scope.hidden = false;
                }
                scope.mapId = 'map-geopublisher';
                // FIXME: only one publisher in a page ?
                scope.ref = r.ref;
                scope.refParent = r.refParent;
                scope.name = r.name;
                scope.resource = r;

                if (!scope.loaded) {
                  // Let the div be displayed and then
                  // init the map.
                  $timeout(function() {
                    init();
                  });
                  scope.loaded = true;
                }

                // Build layer name based on file name
                scope.layerName = r.name
                  .replace(/.zip$|.tif$|.tiff$|.ecw$/, '');
                scope.wmsLayerName = scope.layerName;
                if (scope.layerName.match('^jdbc')) {
                  scope.wmsLayerName = scope.layerName.split('#')[1];
                } else if (scope.layerName.match('^file')) {
                  scope.wmsLayerName = scope.layerName
                    .replace(/.*\//, '')
                    .replace(/.zip$|.tif$|.tiff$|.ecw$/, '');
                }
              };
            }
          };
        }]);
})();
