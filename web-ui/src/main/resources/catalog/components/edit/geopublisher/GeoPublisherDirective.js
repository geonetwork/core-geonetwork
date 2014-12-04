(function() {
  goog.provide('gn_geopublisher_directive');

  /**
   */
  angular.module('gn_geopublisher_directive', [])
  .directive('gnGeoPublisher', [
        'gnMap',
        'gnOnlinesrc',
        'gnGeoPublisher',
        'gnEditor',
        'gnCurrentEdit',
        '$timeout',
        '$translate',
        function(gnMap, gnOnlinesrc,
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

                scope.selectNode(scope.nodeId);
                // we need to wait the scope.hidden binding is done
                // before rendering the map.
                map.setTarget(scope.mapId);

                // TODO : Zoom to all extent if more than one defined
                if (angular.isArray(gnCurrentEdit.extent)) {
                  map.getView().fitExtent(
                      gnMap.reprojExtent(gnCurrentEdit.extent[0],
                      'EPSG:4326', gnMap.getMapConfig().projection),
                      map.getSize());
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
               * Dirty check if the node is a Mapserver REST API
               * or a GeoServer REST API.
               *
               * @param {Object} gsNode
               * @return {boolean}
               */
              var isMRA = function(gsNode) {
                return gsNode.adminUrl &&
                    gsNode.adminUrl.indexOf('/mra') !== -1;
              };

              /**
               * Build WMS layername based on target map server.
               *
               * @param {Object} gsNode
               */
              var buildLayerName = function(gsNode) {
                // Append prefix for GeoServer.
                if (gsNode && !isMRA(gsNode)) {
                  scope.wmsLayerName = gsNode.namespacePrefix +
                      ':' + scope.wmsLayerName;
                }
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
                  addLayerToMap(data.layer);
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
                buildLayerName(gsNode);
                scope.hasStyler = !angular.isArray(gsNode.stylerUrl);
              };

              /**
               * Check the status of the selected node.
               * Return an error status if not published or
               * a layer configuration if published.
               */
              scope.checkNode = function(nodeId) {
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
                buildLayerName(gsNode);
              };
            }
          };
        }]);
})();
