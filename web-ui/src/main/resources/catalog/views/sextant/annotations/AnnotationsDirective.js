(function() {
  goog.provide('sxt_annotations_directive');

  var module = angular.module('sxt_annotations_directive', []);

  /**
   * Directive for editing shared annotations, optionally linked to
   * a XML metadata record.
   */
  module.directive('sxtAnnotationsEditor', ['sxtAnnotationsService', '$rootScope', '$timeout',
    function(sxtAnnotationsService, $rootScope, $timeout) {
      return {
        restrict: 'E',
        scope: {
          map: '=map',
          layer: '='
        },
        templateUrl: '../../catalog/views/sextant/annotations/partials/annotationsEditor.html',
        link: function(scope, element, attrs) {
          scope.annotationsUuid = scope.layer.get('annotationsUuid');
          scope.metadataObj = scope.layer.get('md');
          scope.metadataUuid = scope.metadataObj ? scope.metadataObj.getUuid() : null;

          var user = $rootScope.user;

          if (!scope.annotationsUuid) {
            console.error('Missing annotations UUID on layer');
            return;
          }

          var geojson = new ol.format.GeoJSON();

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

          /**
           * Copy-pasted from DrawDirective.js
           */
          function createStyleFromConfig(feature, styleCfg) {
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
            if (feature.get('name')) {
              styleObjCfg.text = new ol.style.Text({
                font: styleCfg.text.font,
                text: feature.get('name'),
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
            else if (feature.getGeometry().getType() == 'Point') {
              styleObjCfg.image = new ol.style.Circle({
                radius: styleCfg.image.radius,
                fill: new ol.style.Fill({
                  color: styleCfg.image.fill.color
                })
              });
            }
            return new ol.style.Style(styleObjCfg);
          }

          var listenerKey;

          // set current annotation entity to be modified
          function setCurrentAnnotation(annotation) {
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
              source.addFeatures(features);
            }

            listenerKey = scope.annotationsLayer.getSource().on(['changefeature', 'addfeature', 'removefeature'], function() {
              scope.annotationsChanged = true;
            })
          }

          // initial loading of annotation entity
          sxtAnnotationsService.getAnnotation(scope.annotationsUuid)
            .then(setCurrentAnnotation)

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
              .then(setCurrentAnnotation)
          }

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
          }

          scope.$on('$destroy', function() {
            scope.map.removeLayer(scope.annotationsLayer);
            scope.annotationsLayer.getSource().unByKey(listenerKey);
            scope.annotationsLayer.active = false;
          });
        }
      };
    }]);

})();
