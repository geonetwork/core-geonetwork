(function() {
  goog.provide('gn_thesaurus_service');

  var module = angular.module('gn_thesaurus_service', []);

  module.factory('Keyword', function() {
    function Keyword(k) {
      this.props = $.extend(true, {}, k);
      this.label = this.getLabel();
    };
    Keyword.prototype = {
      getId: function() {
        return this.props.uri;
      },
      getLabel: function() {
        return this.props.value['#text'];
      }
    };

    return Keyword;
  });

  module.factory('Thesaurus', function() {
    function Thesaurus(k) {
      this.props = $.extend(true, {}, k);
    };
    Thesaurus.prototype = {
      getKey: function() {
        return this.props.key;
      },
      getTitle: function() {
        return this.props.title;
      }
    };

    return Thesaurus;
  });

  module.provider('gnThesaurusService',
      function() {
        this.$get = [
          '$q',
          '$rootScope',
          '$http',
          'gnUrlUtils',
          'Keyword',
          'Thesaurus',
          function($q, $rootScope, $http, gnUrlUtils, Keyword, Thesaurus) {
            var getKeywordsSearchUrl = function(filter, 
                thesaurus, max, typeSearch) {
              return gnUrlUtils.append('keywords@json',
                  gnUrlUtils.toKeyValue({
                    pNewSearch: 'true',
                    pTypeSearch: typeSearch || 1,
                    pThesauri: thesaurus,
                    pMode: 'searchBox',
                    maxResults: max,
                    pKeyword: filter || ''
                  })
              );
            };


            var parseKeywordsResponse = function(data, dataToExclude) {
              var listOfKeywords = [];
              angular.forEach(data[0], function(k) {
                listOfKeywords.push(new Keyword(k));
              });

              if (dataToExclude && dataToExclude.length > 0) {
                // Remove from search already selected keywords
                listOfKeywords = $.grep(listOfKeywords, function(n) {
                  var isSelected = false;
                  isSelected = $.grep(dataToExclude, function(s) {
                    return s.getLabel() === n.getLabel();
                  }).length !== 0;
                  return !isSelected;
                });
              }
              return listOfKeywords;
            };

            return {
              /**
               * Number of keywords returned by search (autocompletion
               * or selection, ...)
               */
              DEFAULT_NUMBER_OF_RESULTS: 200,
              /**
               * Number of keywords to display in autocompletion list
               */
              DEFAULT_NUMBER_OF_SUGGESTIONS: 30,
              getKeywordAutocompleter: function(config) {
                var keywordsAutocompleter = new Bloodhound({
                  datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
                  queryTokenizer: Bloodhound.tokenizers.whitespace,
                  limit: config.max || this.DEFAULT_NUMBER_OF_RESULTS,
                  remote: {
                    wildcard: 'QUERY',
                    url: this.getKeywordsSearchUrl('QUERY',
                        config.thesaurusKey || '',
                        config.max || this.DEFAULT_NUMBER_OF_RESULTS),
                    filter: function(data) {
                      return parseKeywordsResponse(data, config.dataToExclude);
                    }
                  }
                });
                keywordsAutocompleter.initialize();
                return keywordsAutocompleter;
              },
              /**
               * Request the XML for the thesaurus and its keywords
               * in a specific format (based on the transformation).
               *
               * eg. to-iso19139-keyword for default form.
               */
              getXML: function(thesaurus, 
                  keywordUris, transformation, lang, textgroupOnly) {
                // http://localhost:8080/geonetwork/srv/eng/
                // xml.keyword.get?thesaurus=external.place.regions&id=&
                // multiple=false&transformation=to-iso19139-keyword&
                var defer = $q.defer();
                var params = {
                  thesaurus: thesaurus,
                  id: keywordUris instanceof Array ?
                      keywordUris.join(',') : keywordUris || '',
                  multiple: keywordUris instanceof Array ? 'true' : 'false',
                  transformation: transformation || 'to-iso19139-keyword'
                };
                if (lang) {
                  params.lang = lang;
                }
                if (textgroupOnly) {
                  params.textgroupOnly = textgroupOnly;
                }
                var url = gnUrlUtils.append('thesaurus.keyword',
                    gnUrlUtils.toKeyValue(params)
                    );
                $http.get(url, { cache: true }).
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
              getAll: function(schema) {
                var defer = $q.defer();
                $http.get('thesaurus@json?' +
                    'element=gmd:descriptiveKeywords&schema=' +
                    (schema || 'iso19139'), { cache: true }).
                    success(function(data, status) {
                      var listOfThesaurus = [];
                      angular.forEach(data[0], function(k) {
                        listOfThesaurus.push(new Thesaurus(k));
                      });
                      defer.resolve(listOfThesaurus);
                    }).
                    error(function(data, status) {
                      //                TODO handle error
                      //                defer.reject(error);
                    });
                return defer.promise;

              },
              getKeywordsSearchUrl: getKeywordsSearchUrl,
              /**
               * Convert JSON response to array of Keyword object.
               * Filter element if dataToExclude parameter defined.
               */
              parseKeywordsResponse: parseKeywordsResponse,
              getKeywords: function(filter, thesaurus, max, typeSearch) {
                var defer = $q.defer();
                var url = getKeywordsSearchUrl(filter,
                    thesaurus, max, typeSearch);
                $http.get(url, { cache: true }).
                    success(function(data, status) {
                      defer.resolve(parseKeywordsResponse(data));
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
