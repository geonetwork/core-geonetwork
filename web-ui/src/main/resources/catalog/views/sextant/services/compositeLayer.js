(function() {

  goog.provide('sxt_compositeLayer');

  var module = angular.module('sxt_compositeLayer', []);

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

      return {
        init: function (layer, map, featureType, heatmapMinCount, tooltipMaxCount) {
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
            source: tooltipSource
          });
          group.getLayers().push(heatmapLayer, tooltipLayer);

          // add an interaction for cell hovering
          var heatmapInteraction = new ol.interaction.Select({
            condition: ol.events.condition.pointerMove,
            style: gnHeatmapService.getCellHoverStyle(),
            layers: [heatmapLayer]
          });
          map.addInteraction(heatmapInteraction);

          // add popover for feature info
          var heatmapOverlay = new ol.Overlay({
            element: $('<div class="heatmap-overlay"></div>')[0],
            positioning: 'bottom-center',
            stopEvent: false,
            offset: [0, -2]
          });
          map.addOverlay(heatmapOverlay);
          heatmapInteraction.on('select', function (event) {
            var selected = event.selected[0];

            // hide if no feature hovered; else move overlay on hovered feature
            if (!selected) {
              heatmapOverlay.setPosition();
            } else {
              var center =
                ol.extent.getCenter(selected.getGeometry().getExtent());
              var topleft =
                ol.extent.getTopLeft(selected.getGeometry().getExtent());
              heatmapOverlay.setPosition([center[0], topleft[1]]);
              heatmapOverlay.getElement().innerText =
                $translate.instant('featureCount') + ': '
                + selected.get('count');
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
                      heatmapInteraction.getFeatures().clear();
                      heatmapOverlay.setPosition();
                      heatmapSource.addFeatures(cells);
                    });
                } else {
                  layer.setVisible(true);
                  heatmapSource.clear();
                  heatmapOverlay.setPosition();
                }

                if (count <= tooltipMaxCount) {
                  me.requestFeatures(featureType, map, count).then(
                    function (results) {
                      // TEMP
                      console.log(results)
                    }
                  )
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
            },
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
              return response.data.hits;
            });
        }
      }
    }
  ]);
})();
