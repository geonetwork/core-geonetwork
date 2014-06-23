(function() {
  goog.provide('gn_draw_directive');

  var module = angular.module('gn_draw_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnDraw', [
    '$rootScope',
    '$timeout',
    '$translate',
    'goDecorateLayer',
    'gaLayerFilters',
    function($rootScope, $timeout, $translate, goDecorateLayer, gaLayerFilters) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/draw/' +
            'partials/draw.html',
        scope: {
          map: '=gnDrawMap',
          options: '=gnDrawOptions',
          isActive: '=gnDrawActive'
        },
        link: function(scope, element, attrs) {
          // Defines static styles
          var white = [255, 255, 255];
          var black = [0, 0, 0];
          var transparent = [0, 0, 0, 0];
          var transparentCircle = new ol.style.Circle({
            radius: 1,
            fill: new ol.style.Fill({color: transparent}),
            stroke: new ol.style.Stroke({color: transparent})
          });

          // Defines directive options
          scope.options = {
            translate: $translate, // For translation of ng-options
            waitClass: 'ga-draw-wait',
            text: '',
            tools: [
              {id: 'point', iconClass: 'fa-bell'},
              {id: 'line', iconClass: 'fa-archive'},
              {id: 'polygon', iconClass: 'icon-ga-polygon'},
              {id: 'text', iconClass: 'icon-ga-text'},
              {id: 'modify', iconClass: 'icon-ga-edit'},
              {id: 'delete', iconClass: 'icon-ga-delete'}
            ],
            colors: [
              {name: 'black', fill: [0, 0, 0], textStroke: white},
              {name: 'blue', fill: [0, 0, 255], textStroke: white},
              {name: 'gray', fill: [128, 128, 128], textStroke: white},
              {name: 'green', fill: [0, 128, 0], textStroke: white},
              {name: 'orange', fill: [255, 165, 0], textStroke: black},
              {name: 'red', fill: [255, 0, 0], textStroke: white},
              {name: 'white', fill: [255, 255, 255], textStroke: black},
              {name: 'yellow', fill: [255, 255, 0], textStroke: black}
            ],
            iconSizes: [
              {label: '24 px', value: [24, 24]},
              {label: '36 px', value: [36, 36]},
              {label: '48 px', value: [48, 48]}
            ],
            icons: [

              // Basics
              {id: 'circle'},
              {id: 'circle-stroked'},
              {id: 'square'},
              {id: 'square-stroked'},
              {id: 'triangle'},
              {id: 'triangle-stroked'},
              {id: 'star'},
              {id: 'star-stroked'},
              {id: 'marker'},
              {id: 'marker-stroked'},
              {id: 'cross'},
              {id: 'disability'},
              {id: 'danger'},

              // Shops
              {id: 'art-gallery'},
              {id: 'alcohol-shop'},
              {id: 'bakery'},
              {id: 'bank'},
              {id: 'bar'},
              {id: 'beer'},
              {id: 'cafe'},
              {id: 'cinema'},
              {id: 'commercial'},
              {id: 'clothing-store'},
              {id: 'grocery'},
              {id: 'fast-food'},
              {id: 'hairdresser'},
              {id: 'fuel'},
              {id: 'laundry'},
              {id: 'library'},
              {id: 'lodging'},
              {id: 'pharmacy'},
              {id: 'restaurant'},
              {id: 'shop'},

              // Transport
              {id: 'airport'},
              {id: 'bicycle'},
              {id: 'bus'},
              {id: 'car'},
              {id: 'ferry'},
              {id: 'london-underground'},
              {id: 'rail'},
              {id: 'rail-above'},
              {id: 'rail-light'},
              {id: 'rail-metro'},
              {id: 'rail-underground'},
              {id: 'scooter'},

              // Sport
              {id: 'america-football'},
              {id: 'baseball'},
              {id: 'basketball'},
              {id: 'cricket'},
              {id: 'golf'},
              {id: 'skiing'},
              {id: 'soccer'},
              {id: 'swimming'},
              {id: 'tennis'},

              // Places
              {id: 'airfield'},
              {id: 'building'},
              {id: 'campsite'},
              {id: 'cemetery'},
              {id: 'city'},
              {id: 'college'},
              {id: 'dog-park'},
              {id: 'embassy'},
              {id: 'farm'},
              {id: 'fire-station'},
              {id: 'garden'},
              {id: 'harbor'},
              {id: 'heliport'},
              {id: 'hospital'},
              {id: 'industrial'},
              {id: 'land-use'},
              {id: 'lighthouse'},
              {id: 'monument'},
              {id: 'minefield'},
              {id: 'museum'},
              {id: 'oil-well'},
              {id: 'park2'},
              {id: 'park'},
              {id: 'parking'},
              {id: 'parking-garage'},
              {id: 'pitch'},
              {id: 'place-of-worship'},
              {id: 'playground'},
              {id: 'police'},
              {id: 'polling-place'},
              {id: 'post'},
              {id: 'religious-christian'},
              {id: 'religious-jewish'},
              {id: 'religious-muslim'},
              {id: 'prison'},
              {id: 'school'},
              {id: 'slaughterhouse'},
              {id: 'theatre'},
              {id: 'toilets'},
              {id: 'town'},
              {id: 'town-hall'},
              {id: 'village'},
              {id: 'warehouse'},
              {id: 'wetland'},
              {id: 'zoo'},


              {id: 'camera'},
              {id: 'chemist'},
              {id: 'dam'},
              {id: 'emergency-telephone'},
              {id: 'entrance'},
              {id: 'heart'},
              {id: 'logging'},
              {id: 'mobilephone'},
              {id: 'music'},
              {id: 'roadblock'},
              {id: 'rocket'},
              {id: 'suitcase'},
              {id: 'telephone'},
              {id: 'waste-basket'},
              {id: 'water'}
            ]
          };

          // Set default color
          scope.options.color = scope.options.colors[5];

          // Set default icon
          scope.options.icon = scope.options.icons[0];

          // Set default icon
          scope.options.iconSize = scope.options.iconSizes[0];

          // Define tools identifiers
          for (var i = 0, ii = scope.options.tools.length; i < ii; i++) {
            var tool = scope.options.tools[i];
            tool.activeKey = 'is' + tool.id.charAt(0).toUpperCase() + tool.id.slice(1) + 'Active';
            tool.cssClass = 'ga-draw-' + tool.id + '-bt';
            tool.title = 'draw_' + tool.id;
            tool.description = 'draw_' + tool.id + '_description';
            tool.instructions = 'draw_' + tool.id + '_instructions';
          }

          // Define icons properties
          for (var i = 0, ii = scope.options.icons.length; i < ii; i++) {
            var icon = scope.options.icons[i];
            icon.url = /*gaGlobalOptions.resourceUrl +*/ 'img/maki/' + icon.id + '-24@2x.png';
          }
          scope.getIconUrl = function(i) {
            return i.url;
          };

          // Rules for the z-index (useful for a correct selection):
          // Sketch features (when modifying): 60
          // Features selected: 50
          // Point with Icon: 40
          // Point with Text: 30
          // Line: 20
          // Polygon: 10
          var ZPOLYGON = 10;
          var ZLINE = 20;
          var ZTEXT = 30;
          var ZICON = 40;
          var ZSELECT = 50;
          var ZSKETCH = 60;

          // Define layer style function
          scope.options.styleFunction = (function() {
            return function(feature, resolution) {

              if (feature.getStyleFunction() &&
                  feature.getStyleFunction()() !== null) {
                return feature.getStyleFunction()(resolution);
              }
              var zIndex = ZPOLYGON;

              // Only update features with new colors if its style is null
              var text, icon;
              var color = scope.options.color;
              var fill = new ol.style.Fill({
                color: color.fill.concat([0.4])
              });
              var stroke = new ol.style.Stroke({
                color: color.fill.concat([1]),
                width: 3
              });
              var sketchCircle = new ol.style.Circle({
                radius: 4,
                fill: fill,
                stroke: stroke
              });

              // Drawing line
              if (scope.options.isLineActive) {
                zIndex = ZLINE;
              }

              // Drawing text
              if ((scope.options.isTextActive ||
                  (scope.options.isModifyActive &&
                  feature.getGeometry() instanceof ol.geom.Point &&
                  feature.get('useText'))) &&
                  angular.isDefined(scope.options.text)) {

                text = new ol.style.Text({
                  font: 'normal 16px Helvetica',
                  text: scope.options.text,
                  fill: new ol.style.Fill({
                    color: stroke.getColor()
                  }),
                  stroke: new ol.style.Stroke({
                    color: color.textStroke.concat([1]),
                    width: 3
                  })
                });
                fill = undefined;
                stroke = undefined;
                zIndex = ZTEXT;
              }
              feature.set('useText', (!!text));

              // Drawing icon
              if ((scope.options.isPointActive ||
                  (scope.options.isModifyActive &&
                  feature.getGeometry() instanceof ol.geom.Point &&
                  feature.get('useIcon'))) &&
                  angular.isDefined(scope.options.icon)) {

                icon = new ol.style.Icon({
                  src: scope.getIconUrl(scope.options.icon),
                  size: scope.options.iconSize.value
                });
                fill = undefined;
                stroke = undefined;
                zIndex = ZICON;
              }
              feature.set('useIcon', (!!icon));

              var styles = [
                new ol.style.Style({
                  fill: fill,
                  stroke: stroke,
                  text: text,
                  image: icon || ((text) ? transparentCircle : sketchCircle),
                  zIndex: zIndex
                })
              ];

              return styles;
            };
          })();

          scope.options.drawStyleFunction = (function() {
            return function(feature, resolution) {
              var styles;
              if (feature.getGeometry().getType() === 'Polygon') {
                styles = [
                  new ol.style.Style({
                    fill: new ol.style.Fill({
                      color: [255, 255, 255, 0.4]
                    }),
                    stroke: new ol.style.Stroke({
                      color: [255, 255, 255, 0],
                      width: 0
                    })
                  })
                ];
              } else {
                styles = scope.options.styleFunction(feature, resolution);
              }
              return styles;
            }
          })();

          scope.options.selectStyleFunction = (function() {
            var fill = new ol.style.Fill({
              color: white.concat([0.4])
            });
            var stroke = new ol.style.Stroke({
              color: white.concat([0.6]),
              width: 3
            });
            var defaultCircle = new ol.style.Circle({
              radius: 4,
              fill: fill,
              stroke: stroke
            });
            var vertexStyle = new ol.style.Style({
              image: new ol.style.Circle({
                radius: 7,
                fill: new ol.style.Fill({
                  color: white.concat([1])
                }),
                stroke: new ol.style.Stroke({
                  color: black.concat([1])
                })
              }),
              zIndex: ZSKETCH
            });

            return function(feature, resolution) {
              if (!feature.getStyleFunction() ||
                  feature.getStyleFunction()() === null) {
                return [vertexStyle];
              }
              var styles = feature.getStyleFunction()(resolution);
              var style = styles[0];
              var text = style.getText();
              if (text) {
                text = new ol.style.Text({
                  font: text.getFont(),
                  text: text.getText(),
                  fill: fill,
                  stroke: stroke
                });
              }

              // When a feature is selected we apply its current style and a white
              // transparent style on top.
              return [
                style,
                new ol.style.Style({
                  fill: fill,
                  stroke: stroke,
                  text: text,
                  image: (text) ? style.getImage() : defaultCircle,
                  zIndex: ZSELECT
                })
              ];
            }
          });

          var draw, modify, select, deregister, sketchFeature, lastActiveTool;
          var map = scope.map;
          var source = new ol.source.Vector();
          var layer = new ol.layer.Vector({
            source: source,
            visible: true,
            style: scope.options.styleFunction
          });
          goDecorateLayer(layer);
          layer.displayInLayerManager = false;
          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilter = gaLayerFilters.selected;


          // Activate the component: active a tool if one was active when draw
          // has been deactivated.
          var activate = function() {
            if (lastActiveTool) {
              activateTool(lastActiveTool);
            }
          };

          // Deactivate the component: remove layer and interactions.
          var deactivate = function() {

            // Deactivate the tool
            if (lastActiveTool) {
              scope.options[lastActiveTool.activeKey] = false;
            }

            // Remove interactions
            deactivateDrawInteraction();
            deactivateSelectInteraction();
            deactivateModifyInteraction();
          };


          // Deactivate other tools
          var activateTool = function(tool) {
            layer.visible = true;

            if (map.getLayers().getArray().indexOf(layer) == -1) {
              map.addLayer(layer);
              // Move draw layer  on each changes in the list of layers
              // in the layer manager.
              scope.$watchCollection('layers | filter:layerFilter',
                  moveLayerOnTop);
            }

            moveLayerOnTop();

            var tools = scope.options.tools;
            for (var i = 0, ii = tools.length; i < ii; i++) {
              scope.options[tools[i].activeKey] = (tools[i].id == tool.id);
            }

            if (tool.id == 'delete') {
              return;
            }

            scope.options.instructions = tool.instructions;
            lastActiveTool = tool;
            setFocus();
          };

          // Set the draw interaction with the good geometry
          var activateDrawInteraction = function(type) {
            deactivateDrawInteraction();
            deactivateSelectInteraction();
            deactivateModifyInteraction();

            draw = new ol.interaction.Draw({
              type: type,
              source: source,
              style: scope.options.drawStyleFunction
            });

            deregister = [
              draw.on('drawstart', function(evt) {
                sketchFeature = evt.feature;
              }),
              draw.on('drawend', function(evt) {
                // Set the definitve style of the feature
                var style = layer.getStyleFunction()(sketchFeature);
                sketchFeature.setStyle(style);
              })
            ];

            if (scope.isActive) {
              map.addInteraction(draw);
            }
          };

          var deactivateDrawInteraction = function() {

            // Remove events
            if (deregister) {
              for (var i = deregister.length - 1; i >= 0; i--) {
                deregister[i].src.unByKey(deregister[i]);
              }
              deregister = null;
            }

            draw = deactivateInteraction(draw);
          };

          // Set the select interaction
          var activateSelectInteraction = function() {
            deactivateDrawInteraction();
            deactivateSelectInteraction();
            deactivateModifyInteraction();

            select = new ol.interaction.Select({
              layer: layer,
              style: scope.options.selectStyleFunction
            });

            if (scope.isActive) {
              map.addInteraction(select);
              select.getFeatures().on('add', updateUseStyles);
              select.getFeatures().on('remove', updateUseStyles);
            }
          };

          var deactivateSelectInteraction = function() {
            scope.useTextStyle = false;
            scope.useIconStyle = false;
            scope.useColorStyle = false;
            select = deactivateInteraction(select);
          };

          // Set the select interaction
          var activateModifyInteraction = function() {
            activateSelectInteraction();

            modify = new ol.interaction.Modify({
              features: select.getFeatures(),
              style: scope.options.selectStyleFunction
            });

            if (scope.isActive) {
              map.addInteraction(modify);
            }
          };

          var deactivateModifyInteraction = function() {
            modify = deactivateInteraction(modify);
          };

          // Deactivate an interaction
          var deactivateInteraction = function(interaction) {
            if (interaction) {
              map.removeInteraction(interaction);
            }
            return undefined;
          };

          // Update selected feature with a new style
          var updateSelectedFeatures = function() {
            if (select) {
              var features = select.getFeatures();
              if (features) {
                features.forEach(function(feature) {
                  // Update the style function of the feature
                  feature.setStyle(function() {return null;});
                  var style = layer.getStyleFunction()(feature);
                  feature.setStyle(style);
                });
              }
            }
          };

          // Determines which styles are used by selected fetures
          var updateUseStyles = function(evt) {
            var features = select.getFeatures().getArray();
            var useTextStyle = false;
            var useIconStyle = false;
            var useColorStyle = false;

            for (var i = 0, ii = features.length; i < ii; i++) {
              var styles = features[i].getStyleFunction()();
              if (styles[0].getImage() instanceof ol.style.Icon) {
                useIconStyle = true;
                continue;
              } else if (styles[0].getText()) {
                useTextStyle = true;
              }
              useColorStyle = true;
            }
            scope.$apply(function() {
              scope.useTextStyle = useTextStyle;
              scope.useIconStyle = useIconStyle;
              scope.useColorStyle = useColorStyle;
            });
          };

          // Delete all features of the layer
          var deleteAllFeatures = function() {
            if (confirm($translate('confirm_remove_all_features'))) {
              layer.getSource().clear();
            }

            // We reactivate the lastActiveTool
            if (lastActiveTool) {
              activateTool(lastActiveTool);
            }
          };


          // Activate/deactivate a tool
          scope.toggleTool = function(tool) {
            if (scope.options[tool.activeKey]) {
              // Deactivate all tools
              deactivate();
              lastActiveTool = undefined;

            } else {
              activateTool(tool);
            }
          };

          // Delete selected features by the edit tool
          scope.deleteFeatures = function() {
            if (confirm($translate('confirm_remove_selected_features')) &&
                select) {
              var features = select.getFeatures();
              if (features) {
                features.forEach(function(feature) {
                  layer.getSource().removeFeature(feature);
                });
                // We reactivate the select interaction instead of clearing
                // directly the selectd features array to avoid an digest cycle
                // error in updateUseStyles function
                activateSelectInteraction();
              }
            }
          };

          scope.aToolIsActive = function() {
            return !!lastActiveTool;
          };

          // Watchers
          scope.$watch('isActive', function(active) {
            $rootScope.isDrawActive = active;
            if (active) {
              activate();
            } else {
              deactivate();
            }
          });

          scope.$watch('options.iconSize', function(active) {
            if (scope.options.isModifyActive) {
              updateSelectedFeatures();
            }
          });

          scope.$watch('options.icon', function(active) {
            if (scope.options.isModifyActive) {
              updateSelectedFeatures();
            }
          });

          scope.$watch('options.color', function(active) {
            if (scope.options.isModifyActive) {
              updateSelectedFeatures();
            }
          });
          scope.$watch('options.text', function(active) {
            if (scope.options.isModifyActive) {
              updateSelectedFeatures();
            }
          });

          scope.$watch('options.isPointActive', function(active) {
            if (active) {
              activateDrawInteraction('Point');
            }
          });
          scope.$watch('options.isLineActive', function(active) {
            if (active) {
              activateDrawInteraction('LineString');
            }
          });
          scope.$watch('options.isPolygonActive', function(active) {
            if (active) {
              activateDrawInteraction('Polygon');
            }
          });
          scope.$watch('options.isTextActive', function(active) {
            if (active) {
              activateDrawInteraction('Point');
            }
          });
          scope.$watch('options.isModifyActive', function(active) {
            if (active) {
              activateModifyInteraction();
            }
          });
          scope.$watch('options.isDeleteActive', function(active) {
            if (active) {
              deleteAllFeatures();
              scope.options.isDeleteActive = false;
            }
          });


          // Utils

          // Focus on the first input.
          var setFocus = function() {
            $timeout(function() {
              var inputs = $(element).find('input, select');
              if (inputs.length > 0) {
                inputs[0].focus();
              }
            });
          };

          // Move the draw layer on top
          var moveLayerOnTop = function() {
            var idx = scope.layers.indexOf(layer);
            if (idx != -1 && idx !== scope.layers.length - 1) {
              map.removeLayer(layer);
              map.addLayer(layer);
            }
          };

        }
      };
    }]);

})();
