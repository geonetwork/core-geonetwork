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

  var FACET_RANGE_COUNT = 5;
  var FACET_RANGE_DELIMITER = ' - ';

  geonet.GnSolrRequest = function(config, $injector) {
    this.$http = $injector.get('$http');
    this.$q = $injector.get('$q');
    this.$translate = $injector.get('$translate');
    this.urlUtils = $injector.get('gnUrlUtils');

    this.config = config;

    /**
     * @type {integer}>
     * Total count of the given doc type
     */
    this.totalCount;

    /**
     * @type {Array<Object}>}
     * An array of all index fields info for a given doc type.
     */
    this.docTypeFieldsInfo;

    /**
     * @type {Array<Object}>
     * `this.docTypeFieldsInfo` filtered through `config.excludedFields`
     */
    this.filteredDocTypeFieldsInfo = [];

    this.baseUrl;

    /**
     * Event listener object, for each event key, contains an array of
     * event option (params, callback).
     * @type {Object}
     */
    this.eventsListener = {};

    // Initialize all events
    angular.forEach(solrRequestEvents, function(k) {
      this.eventsListener[k] = [];
    }.bind(this));

  };

  geonet.GnSolrRequest.prototype.buildSolrUrl = function(params) {
    return this.urlUtils.append(this.config.url + '/query',
        this.urlUtils.toKeyValue(params));
  };


  /**
   * Initialize request parameters.
   *
   * @param {object} options
   */
  geonet.GnSolrRequest.prototype.init = function(options) {
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
  geonet.GnSolrRequest.prototype.getDocTypeInfo = function(options) {
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
          if ($.inArray(fname, this.config.excludedFields) === -1) {
            this.filteredDocTypeFieldsInfo.push(field);
          }
        }, this);

        this.totalCount = indexInfo.totalRecords_i;
        this.initBaseRequest_(options);
      }
      catch (e) {
        var msg = this.$translate('docTypeNotIndexed', {
          id: docTypeId
        });
        defer.reject({statusText: msg});
      }
      defer.resolve(indexInfos);
    }), function(r) {
      if (r.status === 404) {
        defer.reject({statusText: this.$translate('indexNotRunning')});
      } else {
        defer.reject(r);
      }
    });
    return defer.promise;
  };

  geonet.GnSolrRequest.prototype.searchWithFacets =
      function(params, any, solrParams) {

    if (this.initialParams.stats['stats.field'].length > 0) {

      return this.search(params, any, this.initialParams.stats).then(
          function(resp) {
            var statsP = this.createFacetSpecFromStats_(resp.solrData);
            return this.search(params, any, angular.extend(
                {}, this.initialParams.facets, statsP, solrParams)
            );
          }.bind(this));
    }
    else {
      return this.search(
          params,
          any,
          angular.extend({}, this.initialParams.facets, solrParams));
    }
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
  geonet.GnSolrRequest.prototype.search = function(params, any, solrParams) {

    var url = this.getSearchUrl_(params, any);
    url += this.parseKeyValue_(solrParams);

    return this.$http.get(url).then(angular.bind(this,
        function(solrResponse) {

          var resp = {
            solrData: solrResponse.data,
            records: solrResponse.data.response.docs,
            facets: this.createFacetData_(solrResponse.data),
            count: solrResponse.data.response.numFound
          };
          this.sendEvent('search', angular.extend({}, resp, {
            sender: this
          }));
          return resp;
        }));
  };

  /**
   * Init the SolRRequest object values: baseUrl, and initial params for
   * facets and stats.
   *
   * @param {Object} options from SolrRequest object type config.
   * @private
   */
  geonet.GnSolrRequest.prototype.initBaseRequest_ = function(options) {

    var url = this.buildSolrUrl({
      rows: 0,
      q: this.config.docIdField + ':"' + this.config.idDoc(options) + '"',
      wt: 'json'
    });
    this.baseUrl = url;
    this.initBaseParams();
  };

  /**
   * set the solr request base params for facets and stats.
   * It's dont on request init, but can be overwritten by application.
   */
  geonet.GnSolrRequest.prototype.initBaseParams = function() {

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
   * Update the baseUrl with search params. Search params can be on any field,
   * or for a specific field.
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
   * 'q=+(CARPOOL_d:[28109.00 TO 429692.20}) +(STATE_NAME_s:"Alabama") + *Ala*
   *
   * @param {*} params
   * @param {string} any text search on any field
   * @return {string} the updated url
   * @private
   */
  geonet.GnSolrRequest.prototype.getSearchUrl_ = function(params, any) {

    var fieldsQ = [];
    angular.forEach(params, function(field, fieldName) {
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
    var url = this.baseUrl;
    if (fieldsQ.length) {
      url = url.replace('&q=', '&q=' +
          encodeURIComponent(fieldsQ.join(' ') + ' +'));
    }
    return url;
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
  geonet.GnSolrRequest.prototype.getIdxNameObj_ = function(name) {
    var fields = this.docTypeFieldsInfo;
    for (var i = 0; i < fields.length; i++) {
      if (fields[i].label == name ||
          fields[i].idxName == name) {
        return fields[i];
      }
    }
  };

  geonet.GnSolrRequest.prototype.getFacetType_ = function(solrPropName) {
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
  geonet.GnSolrRequest.prototype.createFacetData_ = function(solrData) {
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
  geonet.GnSolrRequest.prototype.createFacetSpecFromStats_ =
      function(solrData) {
    var fields = {
      'facet.range': []
    };
    for (var fieldProp in solrData.stats.stats_fields) {
      var field = solrData.stats.stats_fields[fieldProp];
      fields['facet.range'].push(fieldProp);
      fields['f.' + fieldProp + '.facet.range.start'] = field.min;
      fields['f.' + fieldProp + '.facet.range.end'] = field.max;
      fields['f.' + fieldProp + '.facet.range.gap'] =
          (field.max - field.min) / FACET_RANGE_COUNT;
    }
    return fields;
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
  geonet.GnSolrRequest.prototype.parseKeyValue_ = function(params) {
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

  geonet.GnSolrRequest.prototype.on = function(key, callback, opt_this) {
    this.eventsListener[key].push({callback: callback, this: opt_this});
  };

  geonet.GnSolrRequest.prototype.sendEvent = function(key, args) {
    this.eventsListener[key].forEach(angular.bind(this, function(event) {
      event.callback.call(event.this || this, args);
    }));
  };

})();
