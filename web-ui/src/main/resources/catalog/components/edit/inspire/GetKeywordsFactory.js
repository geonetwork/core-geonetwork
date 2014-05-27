(function() {
  'use strict';
  goog.provide('inspire_get_keywords_factory');

  var module = angular.module('inspire_get_keywords_factory', []);

  module.factory('inspireGetKeywordsFactory', [ '$http', '$q', function($http, $q) {
    return  function(url, thesaurus) {
        var deferred = $q.defer();

        var serviceAndParams = 'xml.search.keywords@json?pNewSearch=true&pLanguage=*&pThesauri=';
        $http.get(url + serviceAndParams + thesaurus).success(function(data) {
          data = data[0];
          var i, j, raw, words, word;
          var keywords = [];
          for (i = 0; i < data.length; i++) {
            raw = data[i];
            words = {};
            for (j = 0; j < raw.values.length; j ++) {
              word = raw.values[j];
              words[word['@language']] = word['#text'];
            }
            keywords.push({
              code: raw.uri,
              words: words
            });
          }
          deferred.resolve(keywords);
        }).error(function (data) {
          deferred.reject(data);
        });
        return deferred.promise;
      };
  }]);
}());

