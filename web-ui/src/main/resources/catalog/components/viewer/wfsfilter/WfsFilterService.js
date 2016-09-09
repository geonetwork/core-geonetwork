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
  goog.provide('gn_wfsfilter_service');

  var module = angular.module('gn_wfsfilter_service', [
  ]);


  module.service('wfsFilterService', [
    'gnSolrRequestManager',
    'gnHttp',
    'gnUrlUtils',
    'gnGlobalSettings',
    '$http',
    '$q',
    '$translate',
    function(gnSolrRequestManager, gnHttp, gnUrlUtils, gnGlobalSettings,
             $http, $q, $translate) {

      var solrProxyUrl = gnHttp.getService('solrproxy');

      var solrObject = gnSolrRequestManager.register('WfsFilter', 'facets');

      var buildSolrUrl = function(params) {
        return gnUrlUtils.append(solrProxyUrl + '/query',
            gnUrlUtils.toKeyValue(params));
      };

      var getFacetType = function(solrPropName) {
        var type = '';
        if (solrPropName == 'facet_ranges') {
          type = 'range';
        }
        else if (solrPropName == 'facet_intervals') {
          type = 'interval';
        }
        else if (solrPropName == 'facet_fields') {
          type = 'field';
        }
        else if (solrPropName == 'facet_dates') {
          type = 'date';
        }
        else if (solrPropName == 'facet_heatmaps') {
          type = 'heatmap';
        }
        return type;
      };

      /**
       * Parse the solr response to create the facet UI config object.
       * Solr reponse contains all values for facets fields, and help to build
       * the facet ui.
       *
       * @param {object} solrData response from solr request
       * @return {Array} All definition for each field
       */
      var createFacetConfigFromSolr = function(solrData, docFields) {
        var fields = [];
        for (var kind in solrData.facet_counts) {
          var facetType = getFacetType(kind);
          for (var fieldProp in solrData.facet_counts[kind]) {
            var field = solrData.facet_counts[kind][fieldProp];
            var fNameObj = getIdxNameObj(fieldProp, docFields);
            var facetField = {
              name: fieldProp,
              label: fNameObj.label || fNameObj.label,
              values: [],
              type: facetType
            };

            if (kind == 'facet_ranges') {
              var counts = field.counts;
              for (var i = 0; i < counts.length; i += 2) {
                if (counts[i + 1] > 0) {
                  var label = '';
                  if (i >= counts.length - 2) {
                    label = '> ' + counts[i];
                  }
                  else {
                    label = counts[i] + ',' + counts[i + 2];
                  }
                  facetField.values[label] = counts[i + 1];
                }
              }
              fields.push(facetField);
            }
            else if (kind == 'facet_fields' && field.length > 0) {
              for (var i = 0; i < field.length; i += 2) {
                facetField.values.push({
                  value: field[i],
                  count: field[i + 1]
                });
              }
              fields.push(facetField);
            }
            else if (kind == 'facet_intervals' &&
                Object.keys(field).length > 0) {
              facetField.values = field;
              fields.push(facetField);
            }
          }
        }
        return fields;

      };

      /**
       * Retrieve the index field object from the array given from feature type
       * info. The object contains the feature type attribute name, the solr
       * indexed name, and its label from applicationProfile.
       * You can retrieve this object with the ftName or the docName.
       *
       * @param {string} name
       * @param {object} idxFields
       * @return {*}
       */
      var getIdxNameObj = function(name, idxFields) {
        for (var i = 0; i < idxFields.length; i++) {
          if (idxFields[i].label == name ||
              idxFields[i].idxName == name) {
            return idxFields[i];
          }
        }
      };

      /**
       * Create a SLD filter for the facet rule. Those filters while be
       * gathered to create the full SLD filter config to send to the
       * generateSLD service.
       *
       * @param {string} key index key of the field
       * @param {string} type of the facet field (range, field etc..)
       */
      var buildSldFilter = function(key, type, multiValued) {
        var res;
        if (type == 'interval' || type == 'range') {
          res = {
            filter_type: 'PropertyIsBetween',
            params: key.match(/\d+(?:[.]\d+)*/g)
          };
        }
        else if (type == 'field') {
          res = {
            filter_type: multiValued ? 'PropertyIsLike' : 'PropertyIsEqualTo',
            params: [multiValued ? '*' + key + '*' : key]
          };
        }
        return res;
      };


      /**
       * Create the generateSLD service config from the facet ui state.
       * @param {object} facetState represents the choices from the facet ui
       * @return {object} the sld config object
       */
      this.createSLDConfig = function(facetState) {
        var sldConfig = {
          filters: []
        };

        angular.forEach(facetState, function(attrValue, attrName) {
          var fieldInfo = attrName.match(/ft_(.*)_([a-z]{1})?([a-z]{1})?$/);
          var field = {
            // TODO : remove the field type suffix
            field_name: fieldInfo[1],
            filter: []
          };
          var multiValued = fieldInfo[3] != undefined;
          angular.forEach(attrValue.values, function(v, k) {
            field.filter.push(buildSldFilter(k, attrValue.type, multiValued));
          });
          sldConfig.filters.push(field);
        });
        return sldConfig;
      };

      /**
       * Get the applicationProfile content from the metadata of the given
       * online resource.
       *
       * @param {string} uuid of the metadata
       * @param {string} ftName featuretype name
       * @param {string} wfsUrl url of the wfs service
       */
      this.getApplicationProfile = function(uuid, ftName, wfsUrl, protocol) {
        return $http.post('../api/0.1/records/' + uuid +
            '/query/wfs-indexing-config', {
              url: wfsUrl,
              name: ftName,
              protocol: protocol
            });
      };

      /**
       * Build solr request from config of the applicationProfile.
       * This config determines what fields to have in facet, and gives
       * interval and range properties.
       *
       * @param {Object} config the applicationProfile definition
       * @param {string} ftName featuretype name
       * @param {string} wfsUrl url of the wfs service
       * @param {array} idxFields info about doc fields
       */
      this.solrMergeApplicationProfile = function(fields, newFields) {

        var toRemoveIdx = [];

        fields.forEach(function(field, idx) {
          var keep;
          for (var i = 0; i < newFields.length; i++) {
            if (field.label == newFields[i].name) {
              keep = true;
              if (newFields[i].label) {
                field.label = newFields[i].label[gnGlobalSettings.lang];
              }
              break;
            }
          }
          if (!keep) {
            toRemoveIdx.unshift(idx);
          }
        });

        var allFields = angular.copy(fields);

        toRemoveIdx.forEach(function(i) {
          fields.splice(i, 1);
        });

        return allFields;
      };

      /**
       * Call solr request to get info about facet to build.
       * Then build the facet ui config from the response.
       *
       * @param {string} url of the solr request
       * @param {array} docFields info of indexed fields.
       * @return {httpPromise} return facet ui config
       */
      this.getFacetsConfigFromSolr____ = function(url, docFields) {

        return $http.get(url).then(function(solrResponse) {
          return {
            facetConfig: createFacetConfigFromSolr(solrResponse.data,
                docFields),
            heatmaps: solrResponse.data.facet_counts.facet_heatmaps,
            count: solrResponse.data.response.numFound
          };
        });
      };

      /**
       * Update solr url depending on the current facet ui selection state.
       * Each time a facet is selected, we trigger a new search on the index
       * to build the facet ui again with updated occurencies.
       *
       * Will build the solr Q query like:
       *  +(LABEL_s:"Abyssal" LABEL_s:Infralittoral)
       *  +featureTypeId:*IFR_AAMP_ZONES_BIO_ATL_P
       *
       * @param {string} url of the base solr url
       * @param {object} facetState strcture representing ui selection
       * @param {string} filter the any filter from input
       * @return {string} the updated url
       */
      this.updateSolrUrl___ = function(url, facetState, filter) {
        var fieldsQ = [];

        angular.forEach(facetState, function(field, fieldName) {
          var valuesQ = [];
          for (var p in field.values) {
            valuesQ.push(fieldName + ':"' + p + '"');
          }
          if (valuesQ.length) {
            fieldsQ.push('+(' + valuesQ.join(' ') + ')');
          }
        });
        if (filter) {
          filter.split(' ').forEach(function(v) {
            fieldsQ.push('+*' + v + '*');
          });
        }
        if (fieldsQ.length) {
          url = url.replace('&q=', '&q=' +
              encodeURIComponent(fieldsQ.join(' ') + ' +'));
        }
        return url;
      };

      /**
       * Call generateSLD service to create the SLD and get an url to reach it.
       *
       * @param {Object} rulesObj strcture of the SLD rules to apply
       * @param {string} wmsUrl url of the WMS service
       * @param {string} featureTypeName of the featuretype
       * @return {HttpPromise} promise
       */
      this.getSldUrl = function(rulesObj, wmsUrl, featureTypeName) {

        var params = {
          filters: JSON.stringify(rulesObj),
          url: wmsUrl,
          layers: featureTypeName
        };

        return $http({
          method: 'POST',
          url: '../api/0.1/tools/ogc/sld',
          data: $.param(params),
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
      };

      /**
       * Run the indexation of the feature
       *
       * @param {string} wfs service url
       * @param {string} featuretype name
       * @return {httpPromise} when indexing is done
       */
      this.indexWFSFeatures = function(url, type, idxConfig, uuid, version) {
        return $http.put('../api/0.1/workers/data/wfs/actions/start', {
          url: url,
          typeName: type,
          version: version || '1.0.0',
          tokenize: idxConfig,
          metadataUuid: uuid
        }
        ).then(function(data) {
        }, function(response) {
        });
      };
    }]);
})();
