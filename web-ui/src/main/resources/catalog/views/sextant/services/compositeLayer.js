(function() {

  goog.provide('sxt_compositeLayer');

  var module = angular.module('sxt_compositeLayer', []);

  var defaultTemplate =
    '<div class="panel panel-default">' +
    '  <div class="panel-heading">Informations' +
    '    <a class="close" style="line-height: 1.9em;">&times</a>' +
    '  </div>'+
    '  <div class="panel-body" style="max-width: 30em; max-height: 30em; font-size: 0.9em; overflow: auto;">'+
    '    {ATTRIBUTES} '+
    '  </div>'+
    '</div>';


  /**
   * This regex is used to loop through all the attribute tokens
   * in a tooltip template, and either replace them with the value
   * on the feature, or simply remove the token altogether.
   * Tokens are expected to be like so: {ATTR_NAME}
   */
  var TOOLTIP_ATTR_REGEX = /{(.+)}/g;

  /**
   * This regex is used to insert a summary of all attributes value in the template.
   */
  var TOOLTIP_ATTR_SUMMARY_REGEX = /{ATTRIBUTES}/g;

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
    '$http', 'ngeoDecorateLayer', '$translate', 'gnHeatmapService', 'gnIndexRequestManager',
    function($http, ngeoDecorateLayer, $translate, gnHeatmapService, gnIndexRequestManager) {
      var BUFFER_RATIO = 1;
      var indexObject = gnIndexRequestManager.register('WfsFilter', 'compositeLayer');
      var GeoJSON = new ol.format.GeoJSON();

      return {
        init: function (layer, map, featureType, heatmapMinCount, tooltipMaxCount, tooltipTemplate) {
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
          group.getSource = function() { return false; };
          ngeoDecorateLayer(group);

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
            opacity: 0
          });
          group.getLayers().push(heatmapLayer);
          group.getLayers().push(tooltipLayer);

          // add popover for feature info
          var heatmapOverlay = new ol.Overlay({
            element: $('<div class="heatmap-overlay"></div>')[0],
            positioning: 'bottom-center',
            stopEvent: false,
            offset: [0, -2]
          });
          map.addOverlay(heatmapOverlay);

          // add popover for feature info
          var tooltipOverlay = new ol.Overlay({
            element: $('<div class="tooltip-overlay composite-popup"></div>')[0],
            positioning: 'bottom-center',
            stopEvent: true,
            offset: [0, -2]
            // autoPan: true,
            // autoPanAnimation: {
            //   duration: 250
            // }
          });
          map.addOverlay(tooltipOverlay);

          var cellHoverStyle = gnHeatmapService.getCellHoverStyle();
          var hoveredFeature = null;
          var selectedFeature = null;

          function resetTooltips(clearSelected) {
            heatmapOverlay.setPosition();
            if (clearSelected) {
              selectedFeature = null;
            }
            if (!selectedFeature) {
              tooltipOverlay.setPosition();
            }
            if (hoveredFeature) {
              hoveredFeature.setStyle(null);
              hoveredFeature = null;
            }
          }

          // show a tooltip when hovering a heatmap cell, with the feature count in it
          var hoverHeatmapFeature = function(feature) {
            if (hoveredFeature) {
              hoveredFeature.setStyle(null);
            }
            hoveredFeature = feature;
            hoveredFeature.setStyle(cellHoverStyle(feature));

            // position overlay on feature
            var center =
              ol.extent.getCenter(feature.getGeometry().getExtent());
            var topleft =
              ol.extent.getTopLeft(feature.getGeometry().getExtent());
            heatmapOverlay.setPosition([center[0], topleft[1]]);
            heatmapOverlay.getElement().innerText =
              $translate.instant('featureCount') + ': '
              + feature.get('count');
          };

          // show a tooltip when hovering an actual feature, generated from the feature properties
          var hoverTooltipFeature = function(feature, clicked) {
            if (selectedFeature && !clicked) {
              return;
            }

            if (!clicked) {
              hoveredFeature = feature;
            } else {
              selectedFeature = feature;
            }

            // position overlay on feature
            var center =
              ol.extent.getCenter(feature.getGeometry().getExtent());
            var topleft =
              ol.extent.getTopLeft(feature.getGeometry().getExtent());
            tooltipOverlay.setPosition([center[0], topleft[1]]);

            // read the feature's attributes & render them using the tooltip template
            var props = feature.getProperties();
            var html = tooltipTemplate || defaultTemplate;

            // replace whole attributes summary
            var attributesSummaryHtml;
            var matches;
            var token;
            while (!!(matches = TOOLTIP_ATTR_SUMMARY_REGEX.exec(html))) {
              token = matches[0];
              attributesSummaryHtml = attributesSummaryHtml || this.getFeatureAttributesHtml(feature);
              html = html.replace(token, attributesSummaryHtml);
            }

            // replace individual attributes
            while (!!(matches = TOOLTIP_ATTR_REGEX.exec(html))) {
              token = matches[0];
              var attrName = matches[1];
              html = html.replace(token, props[attrName] || '');
            }
            tooltipOverlay.getElement().innerHTML = html;

            // hide close btn
            var closeBtn = tooltipOverlay.getElement().querySelector('.close');
            if (closeBtn) {
              closeBtn.style.display = clicked ? 'block' : 'none';
              if (clicked) {
                closeBtn.onclick = function() {
                  tooltipOverlay.setPosition();
                  closeBtn.blur();
                  return false;
                }
              }
            }
          }.bind(this);

          map.on('pointermove', function(evt) {
            var hit = map.forEachFeatureAtPixel(evt.pixel,
              function(feature, layer) {
                if (layer === heatmapLayer) {
                  hoverHeatmapFeature(feature);
                } else {
                  hoverTooltipFeature(feature);
                }
                return true;
              },
              undefined,
              function (layer) {
                return layer === tooltipLayer || layer === heatmapLayer;
              });

            if (!hit) {
              resetTooltips();
            }
            map.getTargetElement().style.cursor = hit ? 'pointer' : 'default';
          });

          map.on('click', function(evt) {
            resetTooltips();

            var hit = map.forEachFeatureAtPixel(evt.pixel,
              function (feature) {
                hoverTooltipFeature(feature, true);
                return true;
              },
              undefined,
              function (layer) {
                return layer === tooltipLayer;
              });

            if (!hit) {
              selectedFeature = null;
              resetTooltips(true);
            }
          });

          var me = this;
          function refresh() {
            me.requestCount(featureType, map)
              .then(function (count) {
                if (count > heatmapMinCount) {
                  layer.setVisible(false);
                  gnHeatmapService.requestHeatmapData(featureType, map)
                    .then(function (cells) {
                      // add cells as features
                      heatmapSource.clear();
                      resetTooltips(true);
                      heatmapSource.addFeatures(cells);
                    });
                } else {
                  layer.setVisible(true);
                  heatmapSource.clear();
                  resetTooltips();
                }
                if (count <= tooltipMaxCount) {
                  me.requestFeatures(featureType, map, count).then(
                    function (features) {
                      tooltipSource.clear();
                      tooltipSource.addFeatures(features);
                    }
                  );
                } else {
                  tooltipSource.clear();
                }
              });
          }

          map.on('moveend', refresh);
          refresh();
        },

        getQueryObject: function(featureType, map) {
          var bufferedSize = map.getSize().map(function (value) {
            return value * BUFFER_RATIO;
          });
          var extent = map.getView().calculateExtent(bufferedSize);

          // viewbox filter
          var topLeft = ol.proj.toLonLat(ol.extent.getTopLeft(extent));
          var bottomRight = ol.proj.toLonLat(ol.extent.getBottomRight(extent));

          // cap extent values to world map
          if (bottomRight[0] < topLeft[0]) {
            bottomRight[0] += 360;
          }
          var viewWidth = Math.min(360, bottomRight[0] - topLeft[0]);
          topLeft[0] = Math.min(Math.max(topLeft[0], -180), 180 - viewWidth);
          topLeft[1] = Math.min(Math.max(topLeft[1], -90), 90);
          bottomRight[0] = topLeft[0] + viewWidth;
          bottomRight[1] = Math.min(Math.max(bottomRight[1], -90), 90);

          return {
            query: {
              bool: {
                must: [{
                  match_all: {}
                }, {
                  match_phrase: {
                    featureTypeId: {
                      query: encodeURIComponent(featureType)
                    }
                  }
                }, {
                  geo_bounding_box: {
                    location: {
                      top_left: topLeft,
                      bottom_right: bottomRight
                    }
                  }
                }]
              }
            }
          };
        },

        requestCount: function (featureType, map) {
          var reqParams = this.getQueryObject(featureType, map);

          // trigger search on ES
          var url = indexObject.ES_URL.replace('_search', '_count');
          return $http.post(url, reqParams)
            .then(function (response) {
              return response.data.count;
            });
        },

        requestFeatures: function (featureType, map, size) {
          var reqParams = this.getQueryObject(featureType, map);
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
                return new ol.Feature(props);
              });
            });
        },

        getFeatureAttributesHtml: function (feature) {
          var result = '';
          var props = feature.getProperties();
          for (var prop in props) {
            if (feature.getGeometryName() === prop) {
              continue;
            }
            var currentAttribute = '<li>' + prop + '<br>' + props[prop] + '</li>';
            result += currentAttribute;
          }
          return '<ul>' + result + '</ul>';
        }
      }
    }
  ]);
})();
