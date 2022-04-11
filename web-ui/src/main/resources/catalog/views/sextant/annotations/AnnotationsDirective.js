(function() {
  goog.provide('sxt_annotations_directive');

  var module = angular.module('sxt_annotations_directive', []);

  /**
   * Directive for editing shared annotations, optionally linked to
   * a XML metadata record.
   */
  module.directive('sxtAnnotationsEditor', ['sxtAnnotationsService', '$rootScope', '$timeout',
    function(sxtAnnotationsService, $rootScope, $timeout) {
      var listenerKey;
      /**
       * Copy-pasted from DrawDirective.js
       */
      var createStyleFromConfig = function(feature, styleCfg) {
        var drawType = feature.get('_type');
        var styleObjCfg = {
          fill: new ol.style.Fill({
            color: styleCfg.fill.color
          }),
          stroke: new ol.style.Stroke({
            color: styleCfg.stroke.color,
            width: styleCfg.stroke.width
          })
        };

        // It is a Text feature
        if (drawType === 'text') {
          styleObjCfg.text = new ol.style.Text({
            font: styleCfg.text.font,
            text: styleCfg.text.text,
            fill: new ol.style.Fill({
              color: styleCfg.text.fill.color
            }),
            stroke: (styleCfg.text.stroke &&
              (styleCfg.text.stroke.width > 0)) ?
              new ol.style.Stroke({
                color: styleCfg.text.stroke.color,
                width: styleCfg.text.stroke.width
              }) : undefined
          });
        }
        else if (drawType === 'point') {
          // handle broken annotation for https://gitlab.ifremer.fr/sextant/geonetwork/-/issues/395
          // before this issue's fix, some annotations may be of type "point" without actually having a radius and color
          if (!styleCfg.image) {
            console.warn('Received an invalid annotation object (save again to make this error disappear)', styleCfg);
            styleCfg.image = {
              radius: 7,
              fill: {
                color: '#ffcc33'
              }
            }
          }
          styleObjCfg.image = new ol.style.Circle({
            radius: styleCfg.image.radius,
            fill: new ol.style.Fill({
              color: styleCfg.image.fill.color
            })
          });
        }

        // FIXME: refactor this to share code with DrawDirective!
        if (styleCfg.text) {
          styleObjCfg.text = new ol.style.Text({
            font: styleCfg.text.font,
            offsetY: drawType === 'point' ? 20 : 0,
            text: styleCfg.text.text,
            textAlign: 'center',
            fill: new ol.style.Fill({
              color: styleCfg.image ? styleCfg.image.fill.color : styleCfg.stroke.color
            }),
            stroke:
              new ol.style.Stroke({
                color: '#fff',
                width: 2
              })
          });
        }
        return new ol.style.Style(styleObjCfg);
      };

      var geojson = new ol.format.GeoJSON();
      var init = function init (scope) {
        scope.annotationsUuid = scope.layer.get('annotationsUuid');
        scope.metadataObj = scope.layer.get('md');
        scope.metadataUuid = scope.metadataObj ? scope.metadataObj.uuid : null;

        var user = $rootScope.user;

        if (!scope.annotationsUuid) {
          console.error('Missing annotations UUID on layer');
          return;
        }
        /**
         * @type {ol.layer.Vector}
         */
        scope.annotationsLayer;

        /**
         * @type {boolean}
         */
        scope.loadingAnnotation = true;

        /**
         * @type {boolean}
         */
        scope.updatingAnnotation = false;

        /**
         * @type {Annotation}
         */
        scope.currentAnnotation = null;

        /**
         * @type {string}
         */
        scope.error = null;

        /**
         * Read-only if the annotations has an associated metadata record, but the user
         * do not have edit rights on it
         * @type {boolean}
         */
        scope.readOnly = scope.metadataObj && !(user.canEditRecord && user.canEditRecord(scope.metadataObj));

        /**
         * True if something has changed and should be saved
         * @type {boolean}
         */
        scope.annotationsChanged = false;

        /**
         * Show a success notification
         * @type {boolean}
         */
        scope.saveSuccess = false;


        // initial loading of annotation entity
        sxtAnnotationsService.getAnnotation(scope.annotationsUuid)
          .then(scope.setCurrentAnnotation)

      }
      return {
        restrict: 'AE',
        scope: {
          map: '=map',
          layer: '='
        },
        templateUrl: '../../catalog/views/sextant/annotations/partials/annotationsEditor.html',
        link: function(scope, element, attrs) {
          /**
           * Create an empty annotation entity with the given UUID
           * @param {string} uuid
           */
          scope.initAnnotation = function(uuid) {
            scope.loadingAnnotation = true;
            sxtAnnotationsService.createAnnotation({
              uuid: uuid,
              metadataUuid: scope.metadataUuid || undefined
            })
              .then(scope.setCurrentAnnotation)
          };

          /**
           * Updates the current annotation geometry
           * @param {string} json
           */
          scope.updateAnnotationGeometry = function(json) {
            scope.updatingAnnotation = true;
            scope.error = null;

            sxtAnnotationsService.updateAnnotation({
              uuid: scope.currentAnnotation.uuid,
              geometry: JSON.parse(json),
              metadataUuid: scope.metadataUuid || undefined
            }).then(function (response) {
              scope.updatingAnnotation = false;
              scope.annotationsChanged = false;
              if (response.error) {
                scope.error = response.error;
              } else {
                scope.saveSuccess = true;
                $timeout(function() { scope.saveSuccess = false; }, 2000);
              }
            });
          };

          // set current annotation entity to be modified
          scope.setCurrentAnnotation = function (annotation) {
            scope.loadingAnnotation = false;

            if (!annotation || annotation.error) {
              if (annotation.error && annotation.status !== 404) {
                scope.error = annotation.error;
              }
              scope.currentAnnotation = null;
              return;
            }

            scope.currentAnnotation = annotation;
            scope.annotationsLayer.active = true;
            if (annotation.geometry) {
              var features = geojson.readFeatures(annotation.geometry, {
                dataProjection: 'EPSG:4326',
                featureProjection: scope.map.getView().getProjection().getCode()
              });

              // the draw vector layer expects a ol.Style object on the `_style` key
              angular.forEach(features, function(feature) {
                feature.set('_style', createStyleFromConfig(feature, feature.get('_style')));
              });

              var source = scope.annotationsLayer.getSource();
              source.clear();
              source.addFeatures(features);
            }

            listenerKey = scope.annotationsLayer.getSource().on(['changefeature', 'addfeature', 'removefeature'], function() {
              scope.annotationsChanged = true;
            })
          };

          scope.layer.on('change:visible', function(e) {

            if( scope.layer.get('annotationsUuid') != e.target.get('annotationsUuid')) {
              return
            }
            if (!e.oldValue)  {

              scope.annotationsLayer.setVisible(true);
            }
            else {
              scope.annotationsLayer.setVisible(false);
            }

          });

          scope.$watch("layer", function() {
              init(scope)
          });

          scope.$on('$destroy', function() {
            scope.map.removeLayer(scope.annotationsLayer);
            if (listenerKey) {
              scope.annotationsLayer.getSource().un(listenerKey);
            }
            scope.annotationsLayer.active = false;
          });
        }
      };
    }]);

})();
