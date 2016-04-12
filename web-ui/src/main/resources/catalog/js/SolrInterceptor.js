(function() {
  goog.provide('gn_interceptor');

  var module = angular.module('gn_interceptor', []);

  module.factory('interceptor', ['gnGlobalSettings', 'gnUrlUtils',
    function(gnGlobalSettings, gnUrlUtils) {

      var lucene2solrParams = {
        from: 'start',
        to: 'rows'
      };
      var luceneParamsToSkip = ['_content_type', 'fast'];
      return {
        'request': function(config) {
          var split = config.url.split(/[?@]/);
          var urlPath = split[0];
          if ((urlPath === 'q') || (urlPath === 'qi')) {
            if (split.length > 1) {
              config.params = gnUrlUtils.parseKeyValue(split[split.length - 1]);
            }
            config.isSolrRequest = true;
            config.url = '../api/0.1/search/records';
            var solrParams = {
              wt: 'json'
            };
            console.log(config);
            if (config.params) {
                $.each(config.params, function(k, v) {
                  if (luceneParamsToSkip.indexOf(k) === -1) {
                    switch (k) {
                      case 'from':
                        solrParams.start = v - 1;
                        break;
                      case 'to':
                        solrParams.rows = v - config.params.from;
                        break;
                      case 'any':
                        if (v !== undefined) {
                          solrParams.q = v;
                        }
                        break;
                      case '_isTemplate':
                        if (v.indexOf(' or ') !== -1) {
                          var values = v.split(' or ');
                          var or = [];
                          for (var j = 0; j < values. length; j++) {
                            or.push(k + ':' + v);
                          }
                          solrParams.q += ' +(' + or.join(' ') + ')';
                        } else {
                          solrParams.q += ' +' + k + ':' + v;
                        }
                        break;
                      case 'uuid':
                        solrParams.q += ' +metadataIdentifier:"' + v + '"';
                        break;
                      default:
                        console.log('Skipped param: ' + k);
                    }
                  }
                  if (solrParams.q === undefined) {
                    solrParams.q = '*:*';
                  }
                });
                config.params = solrParams;
            }
          }
          return config;
        },
        'response': function(response) {
          if (response.config.isSolrRequest) {
            console.log(response);
            var data = response.data;
            var docs = data.response.docs;
            var start = data.responseHeader.params.start + 1;
            var gnResponse = {
              '@from': start,
              '@to': start + docs.length,
              metadata: [],
              summary: []
            };
            for (var i = 0; i < docs.length; i++) {
              var doc = docs[i];
              gnResponse.metadata.push({
                title: doc.resourceTitle,
                'abstract': doc.resourceAbstract,
                lineage: doc.lineage,
                type: doc.resourceType || [],
                image: doc.overviewUrl,
                keyword: doc.tag,
                'geonet:info':  {
                  id: doc.id,
                  edit: 'true', // Can edit all TODO
                  uuid: doc._uuid
                }
              });
            }
            response.data = gnResponse;
          }
          return response;
        }

      };
    }]);

  module.config([
    '$httpProvider',
    'gnGlobalSettings',
    function($httpProvider, gnGlobalSettings) {
      $httpProvider.interceptors.push('interceptor');
    }]);
})();
