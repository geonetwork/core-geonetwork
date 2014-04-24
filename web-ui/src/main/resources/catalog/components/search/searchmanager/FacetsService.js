(function() {
  goog.provide('gn_facet_service');


  var module = angular.module('gn_facet_service', []);

  /**
   * Contains all facets
   */
  module.value('gnCurrentFacet', {
    facets: {},
    deletedFacets: {}
  });

  /**
   * TODO: Translate indexkey/facetkey
   */
  module.factory('gnFacetService', [
    '$translate',
    'gnCurrentFacet',
    function($translate, gnCurrentFacet) {

      var add = function(field, value, label, reset) {
        var facet = {value: value, label: label || value};
        if (reset) {
          gnCurrentFacet.facets[field] = facet;
        } else {
          if (gnCurrentFacet.facets[field]) {
            var currentValue = gnCurrentFacet.facets[field].value;
            var currentLabel = gnCurrentFacet.facets[field].label;
            gnCurrentFacet.facets[field] =
                {
                  value: currentValue + ' or ' + value,
                  label: currentLabel + $translate('or') + label
                };
          } else {
            gnCurrentFacet.facets[field] = facet;
          }
        }
        return this;
      };
      var remove = function(field) {
        if (field) {
          delete gnCurrentFacet.facets[field];
          gnCurrentFacet.deletedFacets[field] = true;
        }
      };
      return {
        add: add,
        remove: remove
      };
    }
  ]);

})();
