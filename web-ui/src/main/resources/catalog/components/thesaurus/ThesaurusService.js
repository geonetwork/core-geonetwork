(function() {
  goog.provide('gn_thesaurus_service');

  var module = angular.module('gn_thesaurus_service', []);

  module.factory('Keyword', function() {
    function Keyword(k) {
      this.props = jQuery.extend(true, {}, k);
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
      this.props = jQuery.extend(true, {}, k);
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
            return {
              DEFAULT_NUMBER_OF_RESULTS: 200,
              /**
               * Request the XML for the thesaurus and its keywords
               * in a specific format (based on the transformation).
               *
               * eg. to-iso19139-keyword for default form.
               */
              getXML: function(thesaurus, 
                  keywordUris, transformation) {
                // http://localhost:8080/geonetwork/srv/eng/
                // xml.keyword.get?thesaurus=external.place.regions&id=&
                // multiple=false&transformation=to-iso19139-keyword&
                var defer = $q.defer();
                var url = gnUrlUtils.append('thesaurus.keyword',
                    gnUrlUtils.toKeyValue({
                      thesaurus: thesaurus,
                      id: keywordUris instanceof Array ?
                          keywordUris.join(',') : keywordUris || '',
                      multiple: keywordUris instanceof Array ? 'true' : 'false',
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
               * Create an XML fragment to be inserted in a form field.
               * The element name will be the parent element of the
               * snippet provided. It has to be in the gmd: namespace.
               *
               * TODO : could be nice to have namespaces as global constant
               */
              buildXML: function(elementName, snippet) {
                var tokens = [
                  '<', elementName,
                  " xmlns:gmd='http://www.isotc211.org/2005/gmd'>",
                  snippet, '</', elementName, '>'];
                return tokens.join('');
              },
              /**
               * Build a field name for an XML field
               * TODO: move to editor service
               */
              buildXMLFieldName: function(elementRef, elementName) {
                var t = ['_X', elementRef,
                         '_', elementName.replace(':', 'COLON')];
                return t.join('');
              },
              /**
               * Get thesaurus list.
               */
              getAll: function(schema) {
                var defer = $q.defer();
                $http.get('thesaurus@json?' +
                    'element=gmd:descriptiveKeywords&schema=' +
                    (schema || 'iso19139')).
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

              getKeywords: function(filter, thesaurus, max, typeSearch) {
                var defer = $q.defer();
                var url = gnUrlUtils.append('keywords@json',
                    gnUrlUtils.toKeyValue({
                      pNewSearch: 'true',
                      pTypeSearch: typeSearch || 1,
                      pThesauri: thesaurus,
                      pMode: 'searchBox',
                      maxResults: max,
                      pKeyword: filter || ''
                    })
                    );
                $http.get(url).
                    success(function(data, status) {
                      var listOfKeywords = [];
                      angular.forEach(data[0], function(k) {
                        listOfKeywords.push(new Keyword(k));
                      });
                      defer.resolve(listOfKeywords);
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
