(function() {
  goog.provide('gn_thesaurus_manager_service');

  var module = angular.module('gn_thesaurus_manager_service', []);

  module.provider('gnThesaurusManagerService',
      function() {
        this.$get = [
          '$q',
          '$rootScope',
          '$http',
          'gnUrlUtils',
          function($q, $rootScope, $http, gnUrlUtils) {
            return {
              getThesaurusSnippet: function(thesaurus, 
                  keywords, transformation) {
                // http://localhost:8080/geonetwork/srv/eng/
                // xml.keyword.get?thesaurus=external.place.regions&id=&
                // multiple=false&transformation=to-iso19139-keyword&
                var defer = $q.defer();
                var url = gnUrlUtils.append('xml.keyword.get@json',
                    gnUrlUtils.toKeyValue({
                      thesaurus: thesaurus,
                      id: keywords || '',
                      multiple: false,
                      transformation: transformation || 'to-iso19139-keyword'
                    })
                    );
                $http.get(url).
                    success(function(data, status) {
                      // TODO: could be a global constant ?
                      var xmlDeclaration =
                          '<?xml version="1.0" encoding="UTF-8"?>';
                      defer.resolve(data.replace(xmlDeclaration, ''));
                    }).
                    error(function(data, status) {
                      //                TODO handle error
                      //                defer.reject(error);
                    });
                return defer.promise;
              },
              /**
               * Get thesaurus list.
               */
              getThesaurusList: function(schema) {
                var defer = $q.defer();
                $http.get('thesaurus@json?' +
                    'element=gmd:descriptiveKeywords&schema=' +
                    (schema || 'iso19139')).
                    success(function(data, status) {
                      defer.resolve(data[0]);
                    }).
                    error(function(data, status) {
                      //                TODO handle error
                      //                defer.reject(error);
                    });
                return defer.promise;

              }
            };
          }];
      });
})();
