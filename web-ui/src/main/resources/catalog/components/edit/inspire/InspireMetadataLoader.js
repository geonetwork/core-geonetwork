(function() {
  goog.provide('inspire-metadata-loader');
  goog.require('inspire_empty_metadata_factory');

  var module = angular.module('inspire_metadata_factory', ['inspire_empty_metadata_factory']);

  module.factory('inspireMetadataLoader', [ 'inspireEmptyMetadataLoader', '$http',
    function(inspireEmptyMetadataLoader, $http) {
    return function(lang, url, mdId) {
        var templateData = inspireEmptyMetadataLoader(lang);
        $http.get(url + 'inspire.edit.model?id=' + mdId).success(function(data){
          angular.copy(data, templateData);
        }).error(function(err){
          alert(err);
        });

        return templateData;
      };
  }]);
})();

