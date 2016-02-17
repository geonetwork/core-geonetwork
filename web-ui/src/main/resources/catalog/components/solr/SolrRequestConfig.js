(function() {
  goog.provide('gn_solr_request_config');

  var module = angular.module('gn_solr_request_config', []);

  module.factory('gnSolrWfsFilterConfig', ['gnHttp', function(gnHttp) {

    return {
      url: gnHttp.getService('solrproxy'),
      docTypeIdField: 'id',
      docIdField: 'featureTypeId',
      idDoc: function(config) {
        return config.wfsUrl + '#' +
            config.featureTypeName.replace(':', '\\:')
      },
      facets: true,
      stats: true,
      excludedFields: ['geom', 'the_geom', 'ms_geometry',
        'msgeometry', 'id_s', '_version_', 'featuretypeid', 'doctype']
    };
  }]);

})();
