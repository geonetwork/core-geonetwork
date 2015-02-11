(function() {
  goog.provide('gn_facets_config_service');


  var module = angular.module('gn_facets_config_service', []);

  /**
   * TODO: Translate indexkey/facetkey
   */
  module.factory('gnFacetConfigService', [
    'gnHttp',
    function(gnHttp) {

      var loadConfig = function(summaryType) {
        return gnHttp.callService('facetConfig', {}, {
          cache: true
        }).then(function(data) {
          if (data.status != 200) {
            return;
          }
          if (!data.data.hasOwnProperty(summaryType)) {
            alert('ERROR: The config-summary.xml file does ' +
                "not declare a summary type of: '" + summaryType + "'");
          }
          return data.data[summaryType];
        });
      };

      return {
        loadConfig: loadConfig
      };
    }
  ]);
})();
