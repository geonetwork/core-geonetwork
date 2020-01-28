(function() {
  goog.provide('sxt_annotations_service');

  var module = angular.module('sxt_annotations_service', []);

  /**
   * Service for interacting with the Sextant Annotations API
   */
  module.service('sxtAnnotationsService', ['$http', '$q',
    function($http, $q) {
      /**
       * Returns a given annotation object by UUID, or null if not existent
       * @param {string} uuid
       * @returns {Promise<Annotation|null>}
       */
      this.getAnnotation = function(uuid) {
        return $http.get('../api/annotations/' + uuid, {
          headers: { accept: 'application/json' }
        })
          .then(function (response) {
            return response.data;
          }, function () {
            return null;
          });
      }

      /**
       * Creates a new annotation object
       * @param {Annotation} annotation
       * @returns {Promise<Annotation|null>}
       */
      this.createAnnotation = function(annotation) {
        return $http.put('../api/annotations', annotation, {
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          }
        })
          .then(function (annotation) {
            return annotation;
          }, function () {
            return null;
          });
      }

      /**
       * Updates an annotation object based on UUID
       * @param {Annotation} annotation
       * @returns {Promise<Annotation|null>}
       */
      this.updateAnnotation = function(annotation) {
        return $http.put('../api/annotations/' + annotation.uuid, annotation, {
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          }
        })
          .then(function (annotation) {
            return annotation;
          }, function () {
            return null;
          });
      }
    }]
  );

})();
