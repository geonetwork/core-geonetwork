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
  goog.provide('gn_solr_request');

  var module = angular.module('gn_solr_request', []);

  var solrRequestEvents = {
    search: 'search'
  };

  var ROWS = 20;
  var MAX_ROWS = 2000;
  var FACET_RANGE_COUNT = 5;
  var FACET_RANGE_DELIMITER = ' - ';

  geonetwork.GnSolrRequest = function(config, $injector) {

    this.ES_URL = config.url + '/_search';

    this.$http = $injector.get('$http');
    this.$q = $injector.get('$q');
    this.$translate = $injector.get('$translate');
    this.gnTreeFromSlash = $injector.get('gnTreeFromSlash');

    this.config = config;

    this.page = {
      start: 0,
      rows: 10
    };

    /**
     * @type {integer}>
     * Total count of the given doc type
     */
    this.totalCount;

    /**
     * @type {Array<Object>}
     * An array of all index fields info for a given doc type.
     */
    this.docTypeFieldsInfo;

    /**
     * @type {Array<Object>
     * `this.docTypeFieldsInfo` filtered through `config.excludedFields`}
     */
    this.filteredDocTypeFieldsInfo = [];

    /**
     * The base solr url for the given solr request, made of solr service url,
     * and document identifier fq param.
     *
     * ex:
     * "../api/0.1/search/query?wt=json&
     *    fq=featureTypeId:http://server/wfs/#LAYER"
     */
    this.baseUrl;

    /**
     * @type {object}
     * Contain current params for the solr request param.
     * Any and Q params will help to generate the Q request param. The solr
     * params object are other request params.
     *
     * {
     *  any: 'river',
     *  qParams: {
     *    t_OPE_NOM_s: {
     *      type: 'field',
     *      values: {
     *        GIT-CPTu01: true
     * }},
     * solrParams: {
     *   facet:true,
     *   facet.field: [
     *     0: 'ft_OPE_COPE_s',
     *     1: 'ft_OPE_NOM_s'
     *   ],
     *   facet.mincount: 1
     *  }},
     *  geometry: ''
     * }
     */
    this.requestParams = {};

    /**
     * Event listener object, for each event key, contains an array of
     * event option (params, callback).
     * @type {Object}
     */
    this.eventsListener = {};

    /**
     * Keep a tracking on solr request states
     * @type {Array<Object>} store all search params as an object
     * @private
     */
    this.states_ = [];

    // Initialize all events
    angular.forEach(solrRequestEvents, function(k) {
      this.eventsListener[k] = [];
    }.bind(this));

  };

  /**
   * Initialize request parameters.
   *
   * @param {object} options
   */
  geonetwork.GnSolrRequest.prototype.init = function(options) {
    this.initBaseRequest_(options);
  };

  /**
   * Get the indexed fields for the given feature. We get an array of both
   * featureType names and indexed names with the suffix.
   *
   * @param {string} featureTypeName featuretype name
   * @param {string} wfsUrl url of the wfs service
   * @return {httpPromise} return array of field names
   */
  geonetwork.GnSolrRequest.prototype.getDocTypeInfo = function(options) {
    var docTypeId = this.config.idDoc(options);
    var defer = this.$q.defer();
    this.$http.post(this.ES_URL, {
      size: 1,
      'query': {
        'query_string': {
          'query': this.config.docTypeIdField + ':"' + docTypeId + '"'
        }
      }
    }).then(angular.bind(this, function(response) {
      var indexInfos = [];
      try {
        var indexInfo = response.data.hits.hits[0]._source;
        var docF = indexInfo.docColumns_s.split('|');
        var customF = indexInfo.ftColumns_s.split('|');

        for (var i = 0; i < docF.length; i++) {
          indexInfos.push({
            label: customF[i],
            idxName: docF[i],
            isRange: docF[i].endsWith('_d'),
            isTree: docF[i].endsWith('_tree'),
            isDateTime: docF[i].endsWith('_dt'),
            isMultiple: docF[i].endsWith('_ss')
          });
        }
        this.docTypeFieldsInfo = indexInfos;
        this.filteredDocTypeFieldsInfo = [];
        indexInfos.forEach(function(field) {
          var f = field.idxName;
          var fname = f.toLowerCase();

          // Set geometry field
          if (['geom', 'the_geom', 'msgeometry'].indexOf(fname) >= 0) {
            this.geomField = field;
          }
          // Set facet fields
          if ($.inArray(fname, this.config.excludedFields) === -1) {
            this.filteredDocTypeFieldsInfo.push(field);
          }
        }, this);

        this.totalCount = indexInfo.totalRecords_i;
        this.initBaseRequest_(options);
      }
      catch (e) {
        var msg = this.$translate.instant('docTypeNotIndexed', {
          id: docTypeId
        });
        defer.reject({statusText: msg});
      }
      defer.resolve(indexInfos);
    }), function(r) {
      if (r.status === 404) {
        defer.reject({statusText: this.$translate.instant('indexNotRunning')});
      } else {
        defer.reject(r);
      }
    });
    return defer.promise;
  };

  geonetwork.GnSolrRequest.prototype.searchWithFacets =
    function(qParams, aggs) {

      if (Object.keys(this.initialParams.stats).length > 0) {

        return this.searchQuiet(qParams, this.initialParams.stats).then(
          function(resp) {
            var statsP = this.createFacetSpecFromStats_(resp.solrData.aggregations);
            return this.search(qParams, angular.extend(
              {}, this.initialParams.facets, statsP, aggs)
            );
          }.bind(this));
      }
      else {
        return this.search(
          qParams,
          angular.extend({}, this.initialParams.facets, aggs));
      }
    };

  /**
   * Search in ES based on current request params.
   *
   * @param {Object} override to override es request params.
   * @returns {angular.Promise}
   */
  geonetwork.GnSolrRequest.prototype.search_es = function(override) {
    var esParams = angular.extend({},this.reqParams, override);
    return this.$http.post(this.ES_URL, esParams).then(function(response) {
      return response.data;
    });
  };

  geonetwork.GnSolrRequest.prototype.search = function(qParams, solrParams) {
    angular.extend(this.requestParams, {
      any: qParams.any,
      qParams: qParams.params,
      solrParams: solrParams,
      geometry: qParams.geometry,
      filter: this.initialParams.filter
    });
    return this.search_(qParams, solrParams);
  };

  /**
   * Run exact same search but ask only for one field, with more results.
   * @param {object} field to get more elements from.
   * @return {promise} The search promise.
   */
  geonetwork.GnSolrRequest.prototype.getFacetMoreResults = function (field) {
    var aggs = {};
    var agg = this.reqParams.aggs[field.name];
    agg[field.type].size += ROWS;
    aggs[field.name] = agg;
    return this.search_({
      any: this.requestParams.any,
      params: this.requestParams.qParams,
      geometry: this.requestParams.geometry
    }, aggs, true);
  };

  geonetwork.GnSolrRequest.prototype.searchQuiet =
    function(qParams, solrParams) {
      return this.search_(qParams, solrParams, true);
    };

  geonetwork.GnSolrRequest.prototype.updateSearch =
    function(params, any, solrParams) {
      return this.search_(
        angular.extend(this.requestParams.qParams, params),
        any,
        angular.extend(this.requestParams.solrParams, solrParams)
      );
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
   * @param {object} params Search params object
   * @param {string} any Filter on any field
   * @return {string} the updated url
   */
  geonetwork.GnSolrRequest.prototype.search_ =
    function(qParams, aggs, quiet) {

      var params = this.buildESParams(qParams, aggs);

      this.reqParams = params;

      return this.$http.post(this.ES_URL, params).then(angular.bind(this,
        function(r) {

          var resp = {
            solrData: r.data,
            records: r.data.hits.hits,
            facets: this.createFacetData_(r.data, params),
            count: r.data.hits.total
          };
          if (!quiet) {
            this.sendEvent('search', angular.extend({}, resp, {
              sender: this
            }));
          }
          return resp;
        }));
    };

  geonetwork.GnSolrRequest.prototype.next = function() {
    this.page = {
      from: this.page.start + ROWS,  // TODO: Max on total
      size: ROWS
    };
    this.search();
  };
  geonetwork.GnSolrRequest.prototype.previous = function() {
    this.page = {
      from: Math.max(this.page.start - ROWS, 0),
      size: ROWS
    };
    this.search();
  };
  /**
   * Init the SolRRequest object values: baseUrl, and initial params for
   * facets and stats. If configuration contains a docIdField then
   * a filter query is added to the parameters.
   *
   * @param {Object} options from SolrRequest object type config.
   * @private
   */
  geonetwork.GnSolrRequest.prototype.initBaseRequest_ = function(options) {
    this.initialParams = angular.extend({}, this.initialParams, {filter: ''});
    if (this.config.docIdField) {
      this.initialParams.filter = '+' + this.config.docIdField +
        ':\"' + this.config.idDoc(options) + '\"';
    }
    this.baseUrl = this.ES_URL;
    this.initBaseParams();
  };

  /**
   * Set the ES request base params for facets and stats.
   * All params form the `aggs` request object.
   * It's done on request init, but can be overwritten by application.
   */
  geonetwork.GnSolrRequest.prototype.initBaseParams = function() {

    var facetParams = {};
    var statParams = {};

    this.filteredDocTypeFieldsInfo.forEach(function(field) {

      // Comes from application profile
      if(field.aggs) {
        field.aggs[Object.keys(field.aggs)[0]].field = field.idxName;
        facetParams[field.idxName || field.label] = field.aggs;
      }
/*
      else if (field.isTree) {

      }
*/
      else if (!field.isRange) {
        facetParams[field.idxName] = {
          terms: {
            field: field.idxName,
            size: field.isDateTime ? MAX_ROWS : ROWS
          }
        };
      }
      else {
        statParams[field.idxName + '_stats'] ={
          extended_stats: {
            field: field.idxName
          }
        };
      }
    });

    this.initialParams = angular.extend({}, this.initialParams, {
      facets: facetParams,
      stats: statParams
    });
  };


  /**
   * Retrieve the index field object from the array given from feature type
   * info. The object contains the feature type attribute name, the solr
   * indexed name, and its label from applicationProfile.
   * You can retrieve this object with the ftName or the docName.
   *
   * @param {string} name
   * @return {*}
   */
  geonetwork.GnSolrRequest.prototype.getIdxNameObj_ = function(name) {
    var fields = this.docTypeFieldsInfo || [];
    for (var i = 0; i < fields.length; i++) {
      if (fields[i].label == name ||
        fields[i].idxName == name) {
        return fields[i];
      }
    }
  };

  /**
   * Create a facet results description object decoded from ES response.
   * The return object is used for the UI to display all facet list.
   * The object is created from histogram, range, terms or trees
   *
   * @param {Object} solrData  solr response object
   * @return {Array} Facet config
   * @private
   */
  geonetwork.GnSolrRequest.prototype.createFacetData_ =
    function(response, requestParam) {

      var fields = [];
      for (var fieldId in response.aggregations) {
        if(fieldId.indexOf('_stats') > 0 ) break;
        var respAgg = response.aggregations[fieldId];
        var reqAgg = requestParam.aggs[fieldId];

        var fNameObj = this.getIdxNameObj_(fieldId);

        var facetField = {
          name: fieldId,
          label: fNameObj && fNameObj.label ? fNameObj.label : fieldId,
          values: [],
          more: respAgg.sum_other_doc_count
        };

        // histogram
        if(reqAgg.hasOwnProperty('histogram')) {
          facetField.type = 'histogram';
          var buckets = respAgg.buckets;
          respAgg.buckets.forEach(function(b, i) {
            var label = '';
            if(i == 0 && respAgg.buckets.length > 1) {
              label = '< ' + respAgg.buckets[i+1].key.toFixed(2);
            }
            else if(i < respAgg.buckets.length -1) {
              label = b.key.toFixed(2)  + FACET_RANGE_DELIMITER +
                respAgg.buckets[i+1].key.toFixed(2)
            }
            else {
              label = '> ' + b.key.toFixed(2)
            }
            facetField.values.push({
              value: label,
              count: b.doc_count
            });
          });
        }
        // ranges
        if(reqAgg.hasOwnProperty('range')) {
          facetField.type = 'range';
          respAgg.buckets.forEach(function(b, i) {
            var label;
            if(b.from && b.to) {
              label = b.from + FACET_RANGE_DELIMITER + b.to;
            }
            else if (b.to) {
              label = '< ' + b.to;
            }
            else if (b.from) {
              label = '> ' + b.from;
            }
            facetField.values.push({
              value: label,
              count: b.doc_count
            });
          });
        }
        // filters - response bucket is object instead of array
        else if(reqAgg.hasOwnProperty('filters')) {
          facetField.type = 'filters';
          for (var p in respAgg.buckets) {
            facetField.values.push({
              value: p,
              query: reqAgg.filters.
                filters[p].query_string.query,
              count: respAgg.buckets[p].doc_count
            });
          }
        }
        else if(fieldId.endsWith('_tree')) {
          facetField.tree = this.gnTreeFromSlash.getTree(respAgg.buckets);
        }
        else if(fieldId.endsWith('_dt')) {
          if(!fNameObj.allDates) {
            fNameObj.allDates = respAgg.buckets.map(function(b) {
              return b.key;
            });
          }
          facetField.dates = fNameObj.allDates;
        }

        // terms
        else if(reqAgg.hasOwnProperty('terms')) {
          facetField.type = 'terms';
          facetField.size = reqAgg.terms.size;
          for (var i = 0; i < respAgg.buckets.length; i++) {
            facetField.values.push({
              value: respAgg.buckets[i].key,
              count: respAgg.buckets[i].doc_count
            });
          }
        }
        fields.push(facetField);
      }
      return fields;
    };

  /**
   * Create solr request parameters to generate range facet from stats result.
   *
   * CARPOOL_d: {
   *   count: 49
   *   max: 2036025
   *   min: 28109
   *   }
   *        =>
   *  {
   *    facet.range:CARPOOL_d
   *    f.CARPOOL_d.facet.range.start:28109
   *    f.CARPOOL_d.facet.range.end:2036025
   *    f.CARPOOL_d.facet.range.gap:401583.2
   * }
   *
   * @param {Object} solrData object return from solr Request
   * @return {{[facet.range]: Array}}
   * @private
   */
  geonetwork.GnSolrRequest.prototype.createFacetSpecFromStats_ =
    function(aggs) {

      var histograms = {};
      for (var fieldProp in aggs) {
        if(fieldProp.indexOf('_stats')) {
          var fieldName = fieldProp.substr(0, fieldProp.length - 6);
          var field = aggs[fieldProp];
          var interval = (field.max - field.min) /
            Math.min(FACET_RANGE_COUNT, field.count);

          if(interval) {
            histograms[fieldName] = {
              histogram: {
                field: fieldName,
                interval: interval
              }
            }
          }
          else {
            histograms[fieldName] = {
              terms: {
                field: fieldName,
                size: ROWS
              }
            }
          }
        }
      }
      return histograms;
    };

  /**
   * Merge the current solr request params with the given ones and build the
   * result url. Doesn't update the state of the solr object.
   * If state param is provided, the url is computed from the state object
   * instead of current solr request params.
   *
   * @param {object} qParams all element that helps to build the `q` param
   * @param {object} solrParams a map with all params name and value for solr
   * @param {undefined|object} state of `this.requestParams`
   * @return {string} The merged url
   */
  geonetwork.GnSolrRequest.prototype.getMergedUrl =
    function(qParams_, solrParams_, state) {

      var p = this.getMergedParams_(qParams_, solrParams_, state);
      return this.getRequestUrl_(p.qParams, p.solrParams);
    };

  /**
   * Get solr request parameters (q and others) from an given state
   * object or the current solr param state, merged with the given parameters.
   *
   * @param {object} qParams all element that helps to build the `q` param
   * @param {object} solrParams a map with all params name and value for solr
   * @param {undefined|object} state of `this.requestParams`
   * @return {string} The merged url
   */
  geonetwork.GnSolrRequest.prototype.getMergedParams_ =
    function(qParams_, solrParams_, state) {

      var baseObj = state || this.requestParams;
      return {
        qParams: this.getMergedQParams_(qParams_, baseObj),
        solrParams: this.getMergedSolrParams_(solrParams_, baseObj)
      };
    };

  //TODO: confusing types, qParams is an object with (params,any,geometry) keys,
  //TODO: while this.requestParams.qParams is just the params object
  //TODO: change the this.requestParams type to reflect all method signatures
  geonetwork.GnSolrRequest.prototype.getMergedQParams_ =
    function(qParams_, baseObj) {

      return {
        params: angular.extend({}, baseObj.qParams, qParams_.params),
        any: qParams_.any || baseObj.any,
        geometry: qParams_.geometry || baseObj.geometry
      };
    };

  geonetwork.GnSolrRequest.prototype.getMergedSolrParams_ =
    function(solrParams_, baseObj) {
      return angular.extend({}, baseObj.solrParams, solrParams_);
    };

  geonetwork.GnSolrRequest.prototype.getRequestUrl_ =
    function(qParams, solrParams) {
      return this.baseUrl + this.buildUrlParams_(qParams, solrParams);
    };

  geonetwork.GnSolrRequest.prototype.buildUrlParams_ =
    function(qParams, solrParams) {
      return this.buildQParam_(qParams) +
        this.parseKeyValue_(solrParams);
    };
  geonetwork.GnSolrRequest.prototype.getSearhQuery =
    function(params) {
      return this.buildQParam_(params, params.qParams);
    };

  /**
   * qParams:
   *   any
   *   geometry
   *   params
   *     type
   *     values
   *
   *
   * @param qParams
   * @param aggs
   */
  geonetwork.GnSolrRequest.prototype.buildESParams =
    function(qParams, aggs) {

      var params = {
        from: this.page.start,
        size: this.page.rows,
        aggs: aggs
      };

      params.query = {
        'bool': {
          'must': [{
            'query_string': {
              query: this.buildQParam_(qParams) || '*:*'
            }
          }]
        }
      };

      if (qParams.geometry) {
        params.query.bool.filter = {
          'geo_shape': {
            'geom': {
              'shape': {
                'type': 'envelope',
                  'coordinates': qParams.geometry
              },
              'relation': 'intersects'
            }
          }
        };
      }

      angular.forEach(qParams.params, function(field, fieldName) {
        if (field.type == 'date' ) {
          var gte, lfe, range = {};
          if (angular.isObject(field.value)) {
            gte = field.value.from;
            lfe = field.value.to;
          }
          else{
            gte = lfe = field.value;
          }
          range[fieldName] = {
            gte : gte,
            lte : lfe,
            format: 'dd-MM-YYYY'
          };
          params.query.bool.must.push({
            range: range
          });
        }
      });

      return params;
    };

  /**
   * Build the qParams string from
   *   params: search params,
   *   any: any field (full text search)
   *   geometry: spatial filter
   *
   * Example:
   * params = {
   *  CARPOOL_d: {
   *   type: "range",
   *   values: {
   *     28109.00 - 429692.20: true
   *   }
   *  },
   *  STATE_NAME_s: {
   *    type: "field",
   *    values: {
   *      Alabama: true
   *    }
   *   }
   *  }
   *
   * any = 'Ala'
   *
   * =>
   *
   * '+(CARPOOL_d:[28109.00 TO 429692.20}) +(STATE_NAME_s:"Alabama") + *Ala*'
   *
   * @param {object} qParams
   * @return {string} the query string
   * @private
   */
  geonetwork.GnSolrRequest.prototype.buildQParam_ = function(qParams) {
    var fieldsQ = [],
      qParam = '',
      any = qParams.any,
      geometry = qParams.geometry;

    // TODO move this in createFacetData_ ? query param
    angular.forEach(qParams.params, function(field, fieldName) {
      var valuesQ = [];
      for (var p in field.values) {
        if (field.type == 'histogram' || field.type == 'range') {
          var value;
          if(p.indexOf(FACET_RANGE_DELIMITER) > 0) {
            value = fieldName +
              ':[' + p.replace(FACET_RANGE_DELIMITER, ' TO ') + '}';
          }
          else {
            value = fieldName + ':' + p.replace(/ /g, '');
          }
        }
        else if(field.type == 'filters') {
          value = field.query;
        }
        else {
          value = fieldName + ':"' + p + '"';
        }
        valuesQ.push(value);
      }
      if (valuesQ.length) {
        fieldsQ.push('+(' + valuesQ.join(' ') + ')');
      }
    });

    angular.forEach(qParams.qParams, function(field, fieldName) {
      var valuesQ = [];
      for (var p in field.values) {
        if (field.type == 'histogram' || field.type == 'range') {
          valuesQ.push(fieldName +
            ':[' + p.replace(FACET_RANGE_DELIMITER, ' TO ') + '}');
        }
        else {
          valuesQ.push(fieldName + ':"' + p + '"');
        }
      }
      if (valuesQ.length) {
        fieldsQ.push('+(' + valuesQ.join(' ') + ')');
      }
    });

    if (any) {
      any.split(' ').forEach(function(v) {
        fieldsQ.push('+*' + v + '*');
      });
    }

    // Search for all if no filter defined
    if (fieldsQ.length === 0) {
      fieldsQ.push('*:*');
    }

    if (this.initialParams.filter != '') {
      fieldsQ.push(this.initialParams.filter);
    }

    var filter = fieldsQ.join(' ');
    qParam += filter;
    return qParam;

  };

  /**
   * Transform params object to url params.
   * If a param is an Array, the url will contain multiple time this param.
   *
   * {
   *  RANGE: 10,
   *  PROF: [2,3]
   * }
   *
   * => '&RANGE=10&PROF=2&PROF=3'
   *
   * @param {Object} params to extract
   * @return {string} url param
   * @private
   */
  geonetwork.GnSolrRequest.prototype.parseKeyValue_ = function(params) {
    var urlParams = '';
    angular.forEach(params, function(v, k) {

      if (angular.isArray(v)) {
        v.forEach(function(f) {
          urlParams += '&' + k + '=' + f;
        });
      } else {
        urlParams += '&' + k + '=' + v;
      }
    });
    return urlParams;
  };


  // === EVENTS LISTENER ====
  geonetwork.GnSolrRequest.prototype.on = function(key, callback, opt_this) {
    this.eventsListener[key].push({callback: callback, this: opt_this});
  };
  geonetwork.GnSolrRequest.prototype.sendEvent = function(key, args) {
    this.eventsListener[key].forEach(angular.bind(this, function(event) {
      event.callback.call(event.this || this, args);
    }));
  };

  // === STATE MANAGEMENT ====
  geonetwork.GnSolrRequest.prototype.pushState = function(state) {
    var state_ = state || angular.copy(this.requestParams);
    this.states_.push(state_);
  };
  geonetwork.GnSolrRequest.prototype.popState = function(state) {
    return this.states_.pop();
  };
  geonetwork.GnSolrRequest.prototype.getState = function(idx) {
    var idx_ = idx || this.states_.length - 1;
    return idx_ >= 0 && this.states_[idx_] || null;
  };

})();
