(function() {

  goog.provide('sxt_compositeLayer');

  var module = angular.module('sxt_compositeLayer', []);

  var defaultTemplate =
    '<div class="panel panel-default">' +
    '  <div class="panel-heading">Informations' +
    '    <a class="close" style="line-height: 1.9em;">&times</a>' +
    '  </div>'+
    '  <div class="panel-body" style="max-width: 50em; max-height: 30em; font-size: 0.9em; overflow: auto;">'+
    '    {ATTRIBUTES} '+
    '  </div>'+
    '</div>';

  var defaultHoverTemplate =
    '<div class="panel panel-default">' +
    '  <div class="panel-body" style="max-width: 50em; max-height: 30em; font-size: 0.9em; overflow: auto;">'+
    '    {ATTRIBUTES} '+
    '  </div>'+
    '</div>';

  /**
   * This regex is used to loop through all the attribute tokens
   * in a tooltip template, and either replace them with the value
   * on the feature, or simply remove the token altogether.
   * Tokens are expected to be like so: {ATTR_NAME}
   */
  var TOOLTIP_ATTR_REGEX = /{(\w*?)}/g;

  /**
   * This regex is used to insert a summary of all attributes value in the template.
   */
  var TOOLTIP_ATTR_SUMMARY_REGEX = /{ATTRIBUTES}/g;

  var formatHoverTemplate = function(tpl) {
    tpl = tpl.replace(/\r\n|\r|\n/g, '<br>')
    tpl = tpl.replace(/\*\*(.*?)\*\*/gm, '<strong>$1</strong>')
    return tpl

  }
  /**
   * @ngdoc service
   * @kind function
   * @name sxtCompositeLayer
   *
   * @description
   * Creates a layer group from a WMS layer and show the indexed data
   * either in heatmap or image, with or without tooltip, depending
   * on the feature count in the viewport
   */
  module.factory('sxtCompositeLayer', [
    '$http', 'olDecorateLayer', '$translate', 'gnHeatmapService', 'gnIndexRequestManager', '$filter',
    function($http, olDecorateLayer, $translate, gnHeatmapService, gnIndexRequestManager, $filter) {
      var BUFFER_RATIO = 1;
      var indexObject = gnIndexRequestManager.register('WfsFilter', 'compositeLayer');
      var GeoJSON = new ol.format.GeoJSON();

      return {
        init: function (layer, map, featureType, heatmapMinCount, tooltipMaxCount, tooltipTemplate, tooltipHoverTemplate) {
          var me = this;

          // create base group & copy properties
          var group = new ol.layer.Group();
          group.setProperties(layer.getProperties());
          group.setZIndex(layer.getZIndex());
          group.setOpacity(layer.getOpacity());
          group.setVisible(layer.getVisible());
          group.name = layer.name;
          group.visible = layer.visible;
          group.displayInLayerManager = layer.displayInLayerManager;
          group.set('originalWms', layer);
          group.set('tooltipsVisible', false);
          group.getSource = function() { return false; };
          olDecorateLayer(group);

          // add to map instead of WMS
          map.getLayers().remove(layer);
          group.getLayers().push(layer);
          map.getLayers().push(group);

          // heatmap & tooltip layers
          var heatmapSource = new ol.source.Vector({
            features: []
          });
          var heatmapLayer = new ol.layer.Vector({
            source: heatmapSource,
            style: gnHeatmapService.getCellStyle()
          });
          var tooltipSource = new ol.source.Vector({
            features: []
          });
          var tooltipLayer = new ol.layer.Vector({
            source: tooltipSource,
            style: this.getFeatureBaseStyle()
          });
          group.getLayers().push(heatmapLayer);
          group.getLayers().push(tooltipLayer);

          // add popover for feature info
          var heatmapOverlay = new ol.Overlay({
            element: $('<div class="heatmap-overlay"></div>')[0],
            positioning: 'top-left',
            stopEvent: false,
            offset: [20, 20]
          });
          map.addOverlay(heatmapOverlay);

          // add popover for feature info
          var tooltipOverlay = new ol.Overlay({
            element: $('<div class="tooltip-overlay composite-popup"></div>')[0],
            positioning: 'top-left',
            stopEvent: true,
            offset: [20, 20]
            // autoPan: true,
            // autoPanAnimation: {
            //   duration: 250
            // }
          });
          map.addOverlay(tooltipOverlay);

          var cellHoverStyle = gnHeatmapService.getCellHoverStyle();
          var hoveredCell = null;
          var featureHoverStyle = this.getFeatureHoverStyle();
          var featureSelectedStyle = this.getFeatureSelectedStyle();
          var hoveredFeature = null;
          var selectedFeature = null;

          function showHeatmapTooltip(feature) {
            var center =
              ol.extent.getCenter(feature.getGeometry().getExtent());
            var topleft =
              ol.extent.getTopLeft(feature.getGeometry().getExtent());
            heatmapOverlay.setPosition([center[0], topleft[1]]);
            heatmapOverlay.getElement().innerText =
              $translate.instant('featureCount') + ': '
              + feature.get('count');
          }
          function hideHeatmapTooltip() {
            heatmapOverlay.setPosition();
          }
          function handleHeatmapHover(feature) {
            if (hoveredCell) {
              hoveredCell.setStyle(null);
            }
            hoveredCell = feature;
            hoveredCell.setStyle(cellHoverStyle(feature));
            showHeatmapTooltip(hoveredCell);
          }
          function handleHeatmapHoverNoHit() {
            if (hoveredCell) {
              hoveredCell.setStyle(null);
            }
            hideHeatmapTooltip();
          }
          function showFeatureTooltip(feature, sticky, coordinate) {
            // position overlay on pointer
            coordinate = coordinate || tooltipOverlay.get('_gn_overlay_position')
            tooltipOverlay.set('_gn_overlay_position', coordinate)
            tooltipOverlay.setPosition(coordinate);

            // read the feature's attributes & render them using the tooltip template
            const clickMode = sticky
            var props = feature.getProperties();
            var html
            if (!clickMode && tooltipHoverTemplate) {
              html = defaultHoverTemplate.replace(TOOLTIP_ATTR_SUMMARY_REGEX, tooltipHoverTemplate)
              html = formatHoverTemplate(html)
            } else {
              html = tooltipTemplate || defaultTemplate
            }

            // replace whole attributes summary
            var attributesSummaryHtml;
            var matches;
            var token;
            while (!!(matches = TOOLTIP_ATTR_SUMMARY_REGEX.exec(html))) {
              token = matches[0];
              attributesSummaryHtml = attributesSummaryHtml || me.getFeatureAttributesHtml(feature);
              html = html.replace(token, attributesSummaryHtml);
            }

            // replace individual attributes
            matches = html.matchAll(TOOLTIP_ATTR_REGEX)
            for (var match of matches) {
              token = match[0];
              var attrName = match[1];
              html = html.replace(token, props[attrName] || '');
            }
            tooltipOverlay.getElement().innerHTML = html;

            // hide close btn
            var closeBtn = tooltipOverlay.getElement().querySelector('.close');
            if (closeBtn) {
              closeBtn.style.display = sticky ? 'block' : 'none';
              if (sticky) {
                closeBtn.onclick = function() {
                  handleFeatureClickNoHit();
                  closeBtn.blur();
                  return false;
                }
              }
            }
          }
          function hideFeatureTooltip() {
            tooltipOverlay.setPosition();
          }
          function handleFeatureHover(feature, coordinate) {
            if (hoveredFeature) {
              hoveredFeature.setStyle(null);
            }
            if (feature !== selectedFeature) {
              hoveredFeature = feature;
              hoveredFeature.setStyle(featureHoverStyle);
              if (!selectedFeature) {
                showFeatureTooltip(hoveredFeature, false, coordinate);
              }
            } else {
              hoveredFeature = null;
            }
          }
          function handleFeatureHoverNoHit() {
            if (hoveredFeature) {
              hoveredFeature.setStyle(null);
            }
            if (!selectedFeature) {
              hideFeatureTooltip();
            }
          }
          function handleFeatureClick(feature, coordinate) {
            if (selectedFeature) {
              selectedFeature.setStyle(null);
            }
            if (hoveredFeature) {
              hoveredFeature.setStyle(null);
              hoveredFeature = null;
            }
            selectedFeature = feature;
            selectedFeature.setStyle(featureSelectedStyle);
            showFeatureTooltip(selectedFeature, true, coordinate);
          }
          function handleFeatureClickNoHit() {
            if (selectedFeature) {
              selectedFeature.setStyle(null);
              selectedFeature = null;
            }
            hideFeatureTooltip();
          }

          // looks for a feature with the same id as the selected one
          function handleFeatureRefresh() {
            if (!selectedFeature) {
              return;
            }
            var id = selectedFeature.getId();
            selectedFeature.setStyle(null);
            selectedFeature = null;

            var newSelected = tooltipSource.getFeatureById(id);
            if (newSelected) {
              selectedFeature = newSelected;
              selectedFeature.setStyle(featureSelectedStyle);
              showFeatureTooltip(selectedFeature, true);
            }
          }

          map.on('pointermove', function(evt) {
            var heatmapHit;
            var featureHit;
            var hit = map.forEachFeatureAtPixel(evt.pixel,
              function(feature, layer) {
                if (layer === heatmapLayer) {
                  handleHeatmapHover(feature);
                  heatmapHit = true;
                } else {
                  handleFeatureHover(feature, evt.coordinate);
                  featureHit = true;
                }
                return true;
              }, {
                layerFilter: function (layer) {
                  return layer === tooltipLayer || layer === heatmapLayer;
                }
              });

            if (!heatmapHit) {
              handleHeatmapHoverNoHit()
            }
            if (!featureHit) {
              handleFeatureHoverNoHit()
            }
            map.getTargetElement().style.cursor = hit ? 'pointer' : 'default';
          });

          map.on('singleclick', function(evt) {
            var hit = map.forEachFeatureAtPixel(evt.pixel,
              function (feature) {
                handleFeatureClick(feature, evt.coordinate);
                return true;
              },
              undefined,
              function (layer) {
                return layer === tooltipLayer;
              });

            if (!hit) {
              handleFeatureClickNoHit();
            }
          });

          function refresh() {
            me.requestCount(featureType, map, layer)
              .then(function (count) {
                if (count > heatmapMinCount) {
                  layer.setVisible(false);
                  gnHeatmapService.requestHeatmapData(
                    featureType,
                    map,
                    me.getQueryParamsFromLayer(layer))
                    .then(function (cells) {
                      // add cells as features
                      heatmapSource.clear();
                      heatmapSource.addFeatures(cells);
                    });
                } else {
                  layer.setVisible(true);
                  heatmapSource.clear();
                  hideHeatmapTooltip();
                }
                if (count <= tooltipMaxCount) {
                  group.set('tooltipsVisible', true);
                  me.requestFeatures(featureType, map, layer, count).then(
                    function (features) {
                      tooltipSource.clear();
                      tooltipSource.addFeatures(features);
                      handleFeatureRefresh();
                    }
                  );
                } else {
                  group.set('tooltipsVisible', false);
                  tooltipSource.clear();
                  hideFeatureTooltip();
                }
              });
          }

          map.on('moveend', refresh);
          refresh();
        },

        getQueryObject: function(featureType, map, layer, params) {

          var bufferedSize = map.getSize().map(function (value) {
            return value * BUFFER_RATIO;
          });
          var extent = ol.proj.transformExtent(
            map.getView().calculateExtent(bufferedSize),
            map.getView().getProjection().getCode(),
            "EPSG:4326");
          var zoom = map.getView().getZoom();

          // viewbox filter
          var topLeft = ol.extent.getTopLeft(extent);
          var bottomRight = ol.extent.getBottomRight(extent);

          // cap extent values to world map
          if (bottomRight[0] < topLeft[0]) {
            bottomRight[0] += 360;
          }
          var viewWidth = Math.min(360, bottomRight[0] - topLeft[0]);
          topLeft[0] = Math.min(Math.max(topLeft[0], -180), 180 - viewWidth);
          topLeft[1] = Math.min(Math.max(topLeft[1], -90), 90);
          bottomRight[0] = topLeft[0] + viewWidth;
          bottomRight[1] = Math.min(Math.max(bottomRight[1], -90), 90);
          const queryExtent = [[topLeft[0], topLeft[1]], [bottomRight[0], bottomRight[1]]]

          return {
            query: {
              bool: {
                must: [{
                  query_string: {
                    query: params
                  }

                }, {
                  match_phrase: {
                    featureTypeId: {
                      query: encodeURIComponent(featureType)
                    }
                  }
                }, {
                  'geo_shape': {
                    'geom': {
                      'shape': {
                        'type': 'envelope',
                        'coordinates': queryExtent
                      },
                      'relation': 'intersects'
                    }
                  }
                }]
              }
            }
          };
        },
        getQueryParamsFromLayer: function(layer) {
          var params = '*:*';
          if (layer && layer.get('indexObject') && layer.get('indexObject').requestParams.qParams) {
            params = layer.get('indexObject').buildQParam_({
              params: layer.get('indexObject').requestParams.qParams,
              any: null,
              geometry: null
            });
          }
          return params;
        },
        requestCount: function (featureType, map, layer) {
          var reqParams = this.getQueryObject(featureType, map, layer, this.getQueryParamsFromLayer(layer));
          // trigger search on ES
          var url = indexObject.ES_URL.replace('_search', '_count');
          return $http.post(url, reqParams)
            .then(function (response) {
              return response.data.count;
            });
          },

        requestFeatures: function (featureType, map, layer, size) {
          var reqParams = this.getQueryObject(featureType, map, layer, this.getQueryParamsFromLayer(layer));
          reqParams.size = size;
          // trigger search on ES
          return $http.post(indexObject.ES_URL, reqParams)
            .then(function (response) {
              if (!response.data.hits.total) {
                return [];
              }
              return response.data.hits.hits.map(function(hit) {
                var source = hit._source;
                var props = {};
                for (var key in source) {
                  var fieldInfo = key.match(/ft_(.*?)_([a-z]+)(?:_(tree))?$/);
                  if (fieldInfo) {
                    props[fieldInfo[1]] = source[key];
                  }
                }
                var geom = source.geom;
                if (angular.isArray(geom)) {
                  geom = geom[0];
                }
                props.geometry = GeoJSON.readGeometry(geom, {
                  dataProjection: 'EPSG:4326',
                  featureProjection: map.getView().getProjection()
                });

                // create feature with cell data
                var feature = new ol.Feature(props);
                feature.setId(hit._id);
                return feature;
              });
            });
        },

        getFeatureAttributesHtml: function (feature) {
          var result = '';
          var props = feature.getProperties();

          function processValue(value) {
            if (typeof value !== 'string') {
              return value;
            }
            var link = $filter('linky')(value, '_blank');
            if (link) {
              return link;
            } else {
              return value;
            }
          }

          for (var prop in props) {
            if (feature.getGeometryName() === prop) {
              continue;
            }
            var currentAttribute = '<tr><td>' + prop + '</td>' +
                '<td>' + processValue(props[prop]) + '</td>' +
              '</tr>';
            result += currentAttribute;
          }
          return '<table>' +
            result +
            '</table>';
        },

        getFeatureBaseStyle: function () {
          return new ol.style.Style({
            image: new ol.style.Circle({
              radius: 6,
              fill: new ol.style.Fill({
                color: 'rgba(255, 255, 255, 0.01)'
              })
            }),
            fill: new ol.style.Fill({
              color: 'rgba(255, 255, 255, 0.05)'
            }),
            stroke: new ol.style.Stroke({
              color: 'rgba(255, 255, 255, 0.01)',
              width: 1
            }),
            zIndex: Infinity
          })
        },

        getFeatureHoverStyle: function () {
          return new ol.style.Style({
            image: new ol.style.Circle({
              radius: 6,
              fill: new ol.style.Fill({
                color: '#becad3'
              }),
              stroke: new ol.style.Stroke({
                color: 'white',
                width: 3
              })
            }),
            stroke: new ol.style.Stroke({
              color: 'white',
              width: 3
            }),
            fill: new ol.style.Fill({
              color: 'rgba(255, 255, 255, 0.01)'
            }),
            zIndex: Infinity
          })
        },

        getFeatureSelectedStyle: function () {
          return new ol.style.Style({
            image: new ol.style.Circle({
              radius: 6,
              fill: new ol.style.Fill({
                color: '#5cff6c'
              }),
              stroke: new ol.style.Stroke({
                color: 'white',
                width: 3
              })
            }),
            zIndex: Infinity
          })
        }
      }
    }
  ]);
})();
