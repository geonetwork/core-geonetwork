(function() {
  goog.provide('gn_search_manager_service');

  var module = angular.module('gn_search_manager_service', []);

  var gnSearchManagerService = function ($q, $rootScope, $http) {
      
      /**
       * Utility to format a search response. JSON response
       * when containing one element will not make an array.
       * Tidy the JSON to be always the same if one or more 
       * elements.
       */
      var format = function (data) {
          // Retrieve facet and add name as property and remove @count
          var facets = {}, results = -1;
          
          // Cleaning facets
          for (var facet in data.summary) {
              if (facet != '@count') {
                  facets[facet] = data.summary[facet];
                  facets[facet].name = facet;
              } else {
                  // Number of results
                  results = data.summary[facet];
              }
          }
          
          if (data.metadata) {
              // Retrieve metadata
              for (var i=0; i < data.metadata.length || 
                             (!$.isArray(data.metadata) && i < 1 ); i++) {
                  var metadata = $.isArray(data.metadata) ? data.metadata[i] : data.metadata;
                  // Fix thumbnail, link which might be string or array of string
                  if (typeof metadata.image === 'string') {
                      metadata.image = [metadata.image];
                  }
                  if (typeof metadata.link === 'string') {
                      metadata.link = [metadata.link];
                  }
              }
          }
          return {
            facet: facets,
            count: results,
            metadata: (data.metadata && data.metadata.length ? data.metadata : [data.metadata])
          };
          
      };
      
      /**
       * Run a search.
       */
      var search = function (url) {
          var defer = $q.defer();
          $http.get(url).
              success(function(data, status) {
                defer.resolve(format(data));
              }).
              error(function(data, status) {
                defer.reject(error);
              });
          return defer.promise;
      };
      return {
          search: search
      };
  };

  gnSearchManagerService.$inject = ['$q', '$rootScope', '$http'];

  module.factory('gnSearchManagerService', gnSearchManagerService);

})();
