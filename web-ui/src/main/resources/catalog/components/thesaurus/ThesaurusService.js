/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_thesaurus_service');

  var module = angular.module('gn_thesaurus_service', []);

  module.factory('Keyword', function() {
    function Keyword(k) {
      this.props = $.extend(true, {}, k);
      this.label = this.getLabel();
      this.tagClass = 'label label-info gn-line-height';
    };
    Keyword.prototype = {
      getId: function() {
        return this.props.uri;
      },
      getLabel: function() {
        return this.props.value['#text'] || this.props.value;
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
      },
      get: function() {
        return this.props;
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
                thesaurus, lang, max, typeSearch, outputLang) {
              var parameters = {
                type: typeSearch || 'CONTAINS',
                thesaurus: thesaurus,
                rows: max,
                q: filter || '',
                uri: ('*' + filter + '*') || '',
                lang: lang || 'eng'
              };
              if (outputLang) {
                parameters['pLang'] = outputLang;
              }
              return gnUrlUtils.append('../api/registries/vocabularies/search',
                  gnUrlUtils.toKeyValue(parameters)
              );
            };


            var parseKeywordsResponse = function(data, dataToExclude) {
              var listOfKeywords = [];
              angular.forEach(data, function(k) {
                if (k.value) {
                  listOfKeywords.push(new Keyword(k));
                }
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

            // Get Top Concept from thesaurus
            var getTopConcept = function(thesaurus) {
              var defer = $q.defer();
              var url = gnUrlUtils.append(
                  'thesaurus.topconcept?_content_type=json',
                  gnUrlUtils.toKeyValue({
                    thesaurus: thesaurus
                  })
                  );
              $http.get(url, { cache: true }).
                  success(function(data, status) {
                    if (data != null && data.narrower) {
                      defer.resolve(data);
                    } else {
                      // not a top concept
                      defer.reject();
                    }
                  }).
                  error(function(data, status) {
                    //                TODO handle error
                    defer.reject();
                  });
              return defer.promise;
            };

            // Drive JSKOS API with these functions
            function getConcept(thesaurus, keywordUris) {
              var defer = $q.defer();
              var url = gnUrlUtils.append(
                  'thesaurus.concept?_content_type=json',
                  gnUrlUtils.toKeyValue({
                    thesaurus: thesaurus,
                    id: keywordUris instanceof Array ?
                        keywordUris.join(',') : keywordUris || ''
                  })
                  );
              $http.get(url, { cache: true }).
                  success(function(data, status) {
                    defer.resolve(data);
                  }).
                  error(function(data, status) {
                    //                TODO handle error
                    //                defer.reject(error);
                  });
              return defer.promise;
            };

            // expected to promise one JSKOS concept
            // [concept](http://gbv.github.io/jskos/jskos.html#concepts)
            var lookupURI = function(thesaurus, keywordUri) {
              var deferred = $q.defer();
              // first get concept, then get links to other concepts
              getConcept(thesaurus, keywordUri).then(function(c) {
                deferred.resolve(c);
              });
              return deferred.promise;
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
                        config.lang,
                        config.max || this.DEFAULT_NUMBER_OF_RESULTS,
                        undefined,
                        config.outputLang),
                    filter: function(data) {
                      if (config.orderById == 'true') {
                        data.sort(function (a, b) {
                          var nameA = a.uri.toUpperCase();
                          var nameB = b.uri.toUpperCase();
                          if (nameA < nameB) {
                            return -1;
                          }
                          if (nameA > nameB) {
                            return 1;
                          }
                          return 0;
                        });
                      }
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
                  transformation: transformation || 'to-iso19139-keyword'
                };
                if (lang) {
                  params.lang = lang;
                }
                if (textgroupOnly) {
                  params.textgroupOnly = textgroupOnly;
                }
                var url = gnUrlUtils.append(
                '../api/registries/vocabularies/keyword',
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
                $http.get('thesaurus?_content_type=json&' +
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
              getKeywords: function(filter, thesaurus, lang, max, typeSearch) {
                var defer = $q.defer();
                var url = getKeywordsSearchUrl(filter,
                    thesaurus, lang, max, typeSearch);
                $http.get(url, { cache: true }).
                    success(function(data, status) {
                      defer.resolve(parseKeywordsResponse(data));
                    }).
                    error(function(data, status) {
                      //                TODO handle error
                      //                defer.reject(error);
                    });
                return defer.promise;
              },

              // ConceptScheme API for JSKOS
              lookupURI: lookupURI,
              lookupNotation: null,
              lookupLabel: null,
              getTopConcept: getTopConcept,
              suggest: null
            };
          }];
      });
})();
