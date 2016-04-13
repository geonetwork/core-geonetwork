(function() {
  goog.provide('gn_interceptor');

  var module = angular.module('gn_interceptor', []);

  module.factory('interceptor', ['gnGlobalSettings', 'gnUrlUtils',
    function(gnGlobalSettings, gnUrlUtils) {

      var lucene2solrParams = {
        from: 'start',
        to: 'rows',
        uuid: 'metadataIdentifier',
        type: 'resourceType',
        changeDate: '_changeDate',
        rating: '_rating',
        popularity: '_popularity',
        denominator: 'resolutionScaleDenominator',
        title: 'resourceTitle',
        template: '_isTemplate',
        any: '_text_',
        _id: 'id'
      };
      var luceneParamsToSkip = ['_content_type', 'fast', 'sortOrder', 'resultType', 'facet.q'];
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
              wt: 'json',
              q: ''
            };
            if (config.params) {
              var hasTemplateCriteria = false;
              $.each(config.params, function(k, v) {
                if (luceneParamsToSkip.indexOf(k) === -1) {
                  switch (k) {
                    case 'from':
                      solrParams.start = v - 1;
                      break;
                    case 'to':
                      solrParams.rows = v - config.params.from;
                      break;
                    case 'sortBy':
                      // can not sort on multivalued field
                      if (v !== 'relevance') {
                        solrParams.sort =
                            (lucene2solrParams[v] || v) + ' ' +
                            (config.params.sortOrder === 'reverse' ? 'asc' : 'desc');
                      }
                      break;
                    case 'template':
                    case '_isTemplate':
                      solrParams.q += ' +_isTemplate:(' + v + ')';
                      hasTemplateCriteria = true;
                      break;
                    default:
                      if (lucene2solrParams[k] && v !== undefined) {
                          solrParams.q += ' +' + lucene2solrParams[k] + ':"' + v + '"';
                      } else {
                        console.warn('Skipped param: ' + k);
                      }
                  }
                }
                if (solrParams.q === '') {
                  solrParams.q = '*:*';
                }
              });
              // Solr search does not set any defaults.
              if (!hasTemplateCriteria) {
                solrParams.q += ' +_isTemplate:n';
              }
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
            var start = data.response.start + 1;
            var gnResponse = {
              '@from': start,
              '@to': start + docs.length,
              metadata: [],
              summary: {
                '@count': data.response.numFound + ''
              }
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
                category: doc._cat,
                isTemplate: doc._isTemplate,
                'geonet:info':  {
                  id: doc.id,
                  edit: 'true', // Can edit all TODO
                  uuid: doc._uuid,
                  schema: doc._schema
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
