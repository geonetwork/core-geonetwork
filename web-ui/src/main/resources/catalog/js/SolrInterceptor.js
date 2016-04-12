(function() {
  goog.provide('gn_solr_interceptor');

  var module = angular.module('gn_solr_interceptor', []);

  module.factory('solrInterceptor', ['gnGlobalSettings',
    function(gnGlobalSettings) {

      var lucene2solrParams = {
        from: 'start',
        to: 'rows'
      };
      var luceneParamsToSkip = ['_content_type', 'fast'];
      return {
        'request': function(config) {
          if (config.url === 'q') {
            config.isSolrRequest = true;
            config.url = '../api/0.1/search/query';
            var solrParams = {
              wt: 'json',
              fq: 'docType:metadata'
            };
            console.log(config);
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
          return config;
        },
        'response': function(response) {
          if (response.config.isSolrRequest) {
            console.log(response);
            var data = response.data;
            var docs = data.response.docs;
            var start = data.responseHeader.start + 1;
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
                'abstract': doc.resourceAbstract[0],
                lineage: doc.lineage,
                type: doc.resourceType[0],
                keyword: doc.tag,
                'geonet:info':  {
                  _id: doc.id,
                  edit: 'true', // Can edit all TODO
                  uuid: doc.metadataIdentifier[0]
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
      $httpProvider.interceptors.push('solrInterceptor');
    }]);
})();
