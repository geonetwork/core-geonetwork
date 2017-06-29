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
    'gnIndexRequestManager',
    'gnHttp',
    'gnUrlUtils',
    'gnGlobalSettings',
    '$http',
    '$q',
    '$translate',
    function(gnIndexRequestManager, gnHttp, gnUrlUtils, gnGlobalSettings,
             $http, $q, $translate) {

      var indexProxyUrl = gnHttp.getService('indexproxy');

      var indexObject = gnIndexRequestManager.register('WfsFilter', 'facets');

      var buildIndexUrl = function(params) {
        return gnUrlUtils.append(indexProxyUrl + '/query',
            gnUrlUtils.toKeyValue(params));
      };

      this.registerEsObject = function(url, ftName) {
        return gnIndexRequestManager.register('WfsFilter', url + '#' + ftName);
      };
      this.getEsObject = function(url, ftName) {
        return gnIndexRequestManager.get('WfsFilter', url + '#' + ftName);
      };


      /**
       * Retrieve the index field object from the array given from feature type
       * info. The object contains the feature type attribute name, the index
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
       * Create an array of SLD filters for the facet rule. Those filters will
       * be gathered to create the full SLD filter config to send to the
       * generateSLD service.
       *
       * @param {string} key index key of the field
       * @param {string} type of the facet field (range, field etc..)
       * @return {Array} an array containing the filters
       */
      var buildSldFilter = function(name, value, type, multiValued) {
        var filterFields = [];

        // date
        if (type == 'date' || type == 'rangeDate') {

          // Transforms date format: dd-MM-YYYY > YYYY-MM-dd (ISO)
          // TODO: externalize this?
          function transformDate(d) {
            return d.substr(6) + '-' + d.substr(3, 2) + '-' + d.substr(0, 2);
          }

          filterFields.push({
            field_name: name,
            filter: [{
              filter_type: 'PropertyIsGreaterThanOrEqualTo',
              params: [transformDate(value.from)]
            }]
          }, {
            field_name: name,
            filter: [{
              filter_type: 'PropertyIsLessThanOrEqualTo',
              params: [transformDate(value.to)]
            }]
          });
        }

        // numeric range
        else if (type == 'range') {
          filterFields.push({
            field_name: name,
            filter: [{
              filter_type: 'PropertyIsGreaterThanOrEqualTo',
              params: [value.from]
            }]
          }, {
            field_name: name,
            filter: [{
              filter_type: 'PropertyIsLessThanOrEqualTo',
              params: [value.to]
            }]
          });
        }

        // strings
        else if (type == 'terms') {
          var filters = [];

          angular.forEach(value, function(v, k) {
            filters.push({
              filter_type: multiValued ? 'PropertyIsLike' : 'PropertyIsEqualTo',
              params: [multiValued ? '*' + k + '*' : k]
            });
          });

          filterFields.push({
            field_name: name,
            filter: filters
          });
        }

        return filterFields;
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
          // fetch field info from attr name (expects 'ft_xxx_yy')
          var fieldInfo = attrName.match(/ft_(.*)_([a-z]{1})?([a-z]{1})?$/);
          var fieldName = fieldInfo ? fieldInfo[1] : attrName;

          // multiple values
          if (attrValue.values && Object.keys(attrValue.values).length) {
            var multiValued = fieldInfo[3] != undefined;

            Array.prototype.push.apply(sldConfig.filters, buildSldFilter(
                fieldName, attrValue.values, attrValue.type, multiValued));
          }

          // single value
          else if (attrValue.value) {
            Array.prototype.push.apply(sldConfig.filters, buildSldFilter(
                fieldName, attrValue.value, attrValue.type, false));
          }
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
       * Merge the fields with definition from application Profile.
       * Fields could be replaced or just updated, regarding to
       * `extendOnly` property.
       *
       * @param {Array} fields index fields definition
       * @param {Object} appProfile Config object.
       */
      this.indexMergeApplicationProfile = function(fields, appProfile) {

        var toRemoveIdx = [], mergedF = [];
        var newFields = appProfile.fields;
        var tokenizedFields = appProfile.tokenizedFields || [];

        var getNewFieldIdx = function(field) {
          for (var i = 0; i < newFields.length; i++) {
            if (field.label == newFields[i].name) {
              return i;
            }
          }
          return -1;
        };

        // Merge field objects and detect if we need to remove some
        fields.forEach(function(field, idx) {
          var keep;

          var newFieldIdx = getNewFieldIdx(field);
          if (newFieldIdx >= 0) {
            var newField = newFields[newFieldIdx];
            keep = true;
            mergedF.push(field.label);
            if (newField.label) {
              field.label = newField.label[gnGlobalSettings.lang];
            }
            field.aggs = newField.aggs;
            field.display = newField.display;

            // add a flag for tokenized fields
            field.isTokenized = tokenizedFields[field.name] != null;
          }
          if (!keep) {
            toRemoveIdx.unshift(idx);
          }
        });


        var allFields = angular.copy(fields);

        if (!appProfile.extendOnly) {
          toRemoveIdx.forEach(function(i) {
            fields.splice(i, 1);
          });
        }

        // Add appProfile extra fields
        newFields.forEach(function(f) {
          if (mergedF.indexOf(f.name) < 0) {
            f.label = f.name;
            fields.push(f);
          }
        });

        if (!appProfile.extendOnly) {
          fields.sort(function(a, b) {
            return getNewFieldIdx(a) - getNewFieldIdx(b);
          });
        }


        return allFields;
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
      this.indexWFSFeatures = function(
          url, type, idxConfig, treeFields, uuid, version) {
        return $http.put('../api/0.1/workers/data/wfs/actions/start', {
          url: url,
          typeName: type,
          version: version || '1.1.0',
          tokenizedFields: idxConfig,
          treeFields: treeFields,
          metadataUuid: uuid
        }
        ).then(function(data) {
        }, function(response) {
        });
      };

      this.toCQL = function(esObj) {

        var state = esObj.getState();
        var where;

        if (!state.any) {
          where = [];
          angular.forEach(state.qParams, function(fObj, fName) {
            var config = esObj.getIdxNameObj_(fName);
            console.log(config);
            var clause = [];
            var values = fObj.values;
            if (config.isDate) {
              where.concat([
                '(' + fName + '>' + values.from + ')',
                '(' + fName + '<' + values.to + ')'
              ]);
              return;
            }
            angular.forEach(values, function(v, k) {
              clause.push(
                  (config.isTokenized) ?
                  '(' + fName + " LIKE '%" + k + "%')" :
                  '(' + fName + '=' + k + ')'
              );
            });
            if (clause.length == 0) return;
            where.push('(' + clause.join(' OR ') + ')');
          });
          return where.join(' AND ');
        } else {
          esObj.search_es({
            size: scope.count || 10000,
            aggs: {}
          }).then(function(data) {
            var where = data.hits.hits.map(function(res) {
              return res._id;
            });
            return where.join(' OR ');
          });
        }
      };

      this.toUrlParams = function(esObj) {

        var state = esObj.getState();

        if (!state.any) {
          var where = [];
          angular.forEach(state.qParams, function(fObj, fName) {
            var clause = [];
            angular.forEach(fObj.values, function(v, k) {
              clause.push(fName + '=' + k);
            });
            where.push('(' + clause.join(' OR ') + ')');
          });
          return (where.join(' AND '));
        }
        else {
          esObj.search_es({
            size: scope.count || 10000,
            aggs: {}
          }).then(function(data) {
            var where = data.hits.hits.map(function(res) {
              return res._id;
            });
            return (where.join(' OR '));
          });
        }
      };

      /**
       * takes an ElasticSearch request object as input and ouputs a simplified
       * object with properties describing names and values of the defined
       * filters.
       * note: only qParams are taken into account.
       *
       * @param {Object} elasticSearchObject
       */
      this.toObjectProperties = function(esObject) {
        var state = esObject.getState();
        var result = {};

        if (state) {
          // add query params (qParams)
          angular.forEach(state.qParams, function(fObj, fName) {
            // only the last value found will be kept
            angular.forEach(fObj.values, function(value, key) {
              result[fName] = key;
            });
          });

          // add geometry
          if (state.geometry) {
            result.geometry = state.geometry[0][0] + ',' +
                state.geometry[1][1] + ',' +
                state.geometry[1][0] + ',' +
                state.geometry[0][1];
          }
        }

        return result;
      };

    }]);
})();
