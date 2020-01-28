(function() {
  goog.provide('sxt_annotations_directive');

  var module = angular.module('sxt_annotations_directive', []);

  /**
   * Directive for editing shared annotations, optionally linked to
   * a XML metadata record.
   */
  module.directive('sxtAnnotationsEditor', ['sxtAnnotationsService',
    function(sxtAnnotationsService) {
      return {
        restrict: 'E',
        scope: {
          map: '=map'
        },
        templateUrl: '../../catalog/views/sextant/annotations/partials/annotationsEditor.html',
        link: function(scope, element, attrs) {
          // TEMP: target a new UUID everytime
          var UUID = 'unique-uuid' + Math.floor(Math.random() * 10000);

          /**
           * @type {boolean}
           */
          scope.loadingAnnotation = true;

          /**
           * @type {Annotation}
           */
          scope.currentAnnotation = null;

          sxtAnnotationsService.getAnnotation(UUID)
            .then(function(annotation) {
              scope.loadingAnnotation = false;
              scope.currentAnnotation = annotation;
            })

          scope.initAnnotation = function() {
            scope.loadingAnnotation = true;
            sxtAnnotationsService.createAnnotation({
              uuid: UUID
            })
              .then(function(annotation) {
                scope.loadingAnnotation = false;
                scope.currentAnnotation = annotation;
              })
          }
        }
      };
    }]);

})();
