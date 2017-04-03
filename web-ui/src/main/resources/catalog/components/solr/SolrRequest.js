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
  var FACET_RANGE_COUNT = 5;
  var FACET_RANGE_DELIMITER = ' - ';

  geonetwork.GnSolrRequest = function(config, $injector) {
    this.$http = $injector.get('$http');
    this.$q = $injector.get('$q');
    this.$translate = $injector.get('$translate');
    this.urlUtils = $injector.get('gnUrlUtils');

    this.config = config;

    this.page = {
      start: 0,
      rows: ROWS
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

  geonetwork.GnSolrRequest.prototype.buildSolrUrl = function(params) {
    return this.urlUtils.append(this.config.url + '/query',
        this.urlUtils.toKeyValue(params));
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
    var url = this.buildSolrUrl({
      rows: 1,
      q: this.config.docTypeIdField + ':"' + docTypeId + '"',
      wt: 'json'
    });
    var defer = this.$q.defer();
    this.$http.get(url).then(angular.bind(this, function(response) {
      var indexInfos = [];
      try {
        var indexInfo = response.data.response.docs[0];
        var docF = indexInfo.docColumns_s.split('|');
        var customF = indexInfo.ftColumns_s.split('|');

        for (var i = 0; i < docF.length; i++) {
          indexInfos.push({
            label: customF[i],
            idxName: docF[i],
            isRange: docF[i].endsWith('_d'),
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
      function(qParams, solrParams) {

    if (this.initialParams.stats['stats.field'].length > 0) {

      return this.searchQuiet(qParams, this.initialParams.stats).then(
          function(resp) {
            var statsP = this.createFacetSpecFromStats_(resp.solrData);
            return this.search(qParams, angular.extend(
                {}, this.initialParams.facets, statsP, solrParams)
            );
          }.bind(this));
    }
    else {
      return this.search(
          qParams,
          angular.extend({}, this.initialParams.facets, solrParams));
    }
  };

  geonetwork.GnSolrRequest.prototype.search = function(qParams, solrParams) {
    angular.extend(this.requestParams, {
      any: qParams.any,
      qParams: qParams.params,
      solrParams: solrParams,
      geometry: qParams.geometry
    });
    return this.search_(qParams, solrParams);
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
      function(qParams, solrParams, quiet) {

    var url = this.getRequestUrl_(
        qParams,
        angular.extend({}, this.page, solrParams)
        );

    return this.$http.get(url).then(angular.bind(this,
        function(solrResponse) {

          var resp = {
            solrData: solrResponse.data,
            records: solrResponse.data.response.docs,
            facets: this.createFacetData_(solrResponse.data),
            count: solrResponse.data.response.numFound
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
      start: this.page.start + ROWS,  // TODO: Max on total
      rows: ROWS
    };
    this.search();
  };
  geonetwork.GnSolrRequest.prototype.previous = function() {
    this.page = {
      start: Math.max(this.page.start - ROWS, 0),
      rows: ROWS
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
    var params = {
      //rows: 0,
      wt: 'json'
    };
    if (this.config.docIdField) {
      params.fq = this.config.docIdField +
          ':"' + this.config.idDoc(options) + '"';
    }
    var url = this.buildSolrUrl(params);
    this.baseUrl = url;
    this.initBaseParams();
  };

  /**
   * Set the solr request base params for facets and stats.
   * It's done on request init, but can be overwritten by application.
   */
  geonetwork.GnSolrRequest.prototype.initBaseParams = function() {

    var facetParams = {
      'facet': true,
      'facet.mincount': 1,
      'facet.field': []
    };
    var statParams = {
      'stats': true,
      'stats.field': []
    };

    this.filteredDocTypeFieldsInfo.forEach(function(field) {
      if (!field.isRange) {
        facetParams['facet.field'].push(field.idxName);
      }
      else {
        statParams['stats.field'].push(field.idxName);
      }
    });

    this.initialParams = {
      facets: facetParams,
      stats: statParams
    };
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

  geonetwork.GnSolrRequest.prototype.getFacetType_ = function(solrPropName) {
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
    return type;
  };

  /**
   * Create a facet results description object decoded from solr response.
   * It get the value from the facet_counts property of the solr response and
   * process all kind of facets (facet_ranges & facet_fields).
   *
   * The return object is used for the UI to display all facet list.
   *
   * @param {Object} solrData  solr response object
   * @return {Array} Facet config
   * @private
   */
  geonetwork.GnSolrRequest.prototype.createFacetData_ = function(solrData) {
    var fields = [];
    for (var kind in solrData.facet_counts) {
      var facetType = this.getFacetType_(kind);
      // TODO: format facet types eg heatmaps
      if (facetType === '') {
        continue;
      }
      for (var fieldProp in solrData.facet_counts[kind]) {
        var field = solrData.facet_counts[kind][fieldProp];
        var fNameObj = this.getIdxNameObj_(fieldProp);
        var facetField = {
          name: fieldProp,
          label: fNameObj && fNameObj.label ? fNameObj.label : fieldProp,
          values: [],
          type: facetType
        };

        if (kind == 'facet_ranges') {
          var counts = field.counts;
          for (var i = 0; i < counts.length; i += 2) {
            if (counts[i + 1] > 0) {
              var label = '';
              if (i >= counts.length - 2) {
                label = '> ' + Number(counts[i]).toFixed(2);
              }
              else {
                label = Number(counts[i]).toFixed(2) + FACET_RANGE_DELIMITER +
                    Number(counts[i + 2]).toFixed(2);
              }
              facetField.values.push({
                value: label,
                count: Number(counts[i + 1]).toFixed(2)
              });
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

        //TODO: manage intervals ?
        /*
        else if (kind == 'facet_intervals' &&
            Object.keys(field).length > 0) {
          facetField.values = field;
          fields.push(facetField);
        }
        */
      }
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
      function(solrData) {
    var fields = {
      'facet.range': []
    };
    for (var fieldProp in solrData.stats.stats_fields) {
      var field = solrData.stats.stats_fields[fieldProp];
      fields['facet.range'].push(fieldProp);
      fields['f.' + fieldProp + '.facet.range.start'] = field.min || 0;
      fields['f.' + fieldProp + '.facet.range.end'] = field.max || 0;
      fields['f.' + fieldProp + '.facet.range.gap'] =
          (field.max - field.min) / FACET_RANGE_COUNT;
    }
    return fields;
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
   * 'q=+(CARPOOL_d:[28109.00 TO 429692.20}) +(STATE_NAME_s:"Alabama") + *Ala*'
   *
   * @param {object} qParams
   * @return {string} the q params string
   * @private
   */
  geonetwork.GnSolrRequest.prototype.buildQParam_ = function(qParams) {
    var fieldsQ = [],
        qParam = '&q=',
        any = qParams.any,
        geometry = qParams.geometry;

    angular.forEach(qParams.params, function(field, fieldName) {
      var valuesQ = [];
      for (var p in field.values) {
        if (field.type == 'range') {
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

    var filter = encodeURIComponent(fieldsQ.join(' '));
    qParam += filter;

    //TODO: not q param but fq now
    if (geometry) {
      qParam += '&fq={!field f=' + this.geomField.idxName +
          '}Intersects(ENVELOPE(' + geometry + '))';
    }

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
