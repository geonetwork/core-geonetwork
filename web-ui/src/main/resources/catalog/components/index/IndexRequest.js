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

(function () {
  goog.provide("gn_index_request");

  var module = angular.module("gn_index_request", []);

  var indexRequestEvents = {
    search: "search"
  };

  var ROWS = 20;
  var MAX_ROWS = 2000;
  var FACET_TREE_ROWS = 1000;
  var FACET_RANGE_COUNT = 5;
  var FACET_RANGE_DELIMITER = " - ";

  geonetwork.gnIndexRequest = function (config, $injector) {
    this.ES_URL = config.url + "?_=_search";

    this.$http = $injector.get("$http");
    this.$q = $injector.get("$q");
    this.$translate = $injector.get("$translate");
    this.gnFacetTree = $injector.get("gnFacetTree");

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
     * The base index url for the given index request,
     * made of index service url,
     * and document identifier fq param.
     *
     * ex:
     * "../api/search/query?wt=json&
     *    fq=featureTypeId:http://server/wfs/#LAYER"
     */
    this.baseUrl;

    /**
     * @type {object}
     * Contain current params for the index request param.
     * Any and Q params will help to generate the Q request param. The index
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
     * indexParams: {
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
     * Keep a tracking on index request states
     * @type {Array<Object>} store all search params as an object
     * @private
     */
    this.states_ = [];

    this.initialParams = {};

    // Initialize all events
    angular.forEach(
      indexRequestEvents,
      function (k) {
        this.eventsListener[k] = [];
      }.bind(this)
    );
  };

  /**
   * Initialize request parameters.
   *
   * @param {object} options
   */
  geonetwork.gnIndexRequest.prototype.init = function (options) {
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
  geonetwork.gnIndexRequest.prototype.getDocTypeInfo = function (options) {
    this.config.docTypeId = this.config.idDoc(options);
    var defer = this.$q.defer();
    this.$http
      .post(this.ES_URL, {
        size: 1,
        query: {
          query_string: {
            query: this.config.docTypeIdField + ':"' + this.config.docTypeId + '"'
          }
        }
      })
      .then(
        angular.bind(this, function (response) {
          var indexInfos = [];
          try {
            var indexInfo = response.data.hits.hits[0]._source;
            var docF = indexInfo.docColumns_s.split("|");
            var customF = indexInfo.ftColumns_s.split("|");

            for (var i = 0; i < docF.length; i++) {
              indexInfos.push({
                label: customF[i],
                name: customF[i],
                idxName: docF[i],
                isRange: docF[i].endsWith("_d"),
                isTree: docF[i].endsWith("_tree"),
                isDateTime: docF[i].endsWith("_dt"),
                isMultiple: docF[i].endsWith("_ss")
              });
            }
            this.docTypeFieldsInfo = indexInfos;
            this.filteredDocTypeFieldsInfo = [];
            indexInfos.forEach(function (field) {
              var f = field.idxName;
              var fname = f.toLowerCase();

              // Set geometry field
              if (["geom", "the_geom", "msgeometry"].indexOf(fname) >= 0) {
                this.geomField = field;
              }
              // Set facet fields
              if ($.inArray(fname, this.config.excludedFields) === -1) {
                this.filteredDocTypeFieldsInfo.push(field);
              }
            }, this);

            this.totalCount = indexInfo.totalRecords_i;
            this.isPointOnly = indexInfo.isPointOnly;
            this.initBaseRequest_(options);
          } catch (e) {
            var msg = this.$translate.instant("docTypeNotIndexed", {
              id: this.config.docTypeId
            });
            defer.reject({ statusText: msg });
          }
          defer.resolve(indexInfos);
        }),
        function (r) {
          if (r.status === 404) {
            defer.reject({ statusText: this.$translate.instant("indexNotRunning") });
          } else {
            defer.reject(r);
          }
        }.bind(this)
      );
    return defer.promise;
  };

  geonetwork.gnIndexRequest.prototype.searchWithFacets = function (qParams, aggs) {
    if (this.initialParams.stats && Object.keys(this.initialParams.stats).length > 0) {
      angular.forEach(
        this.initialParams.stats,
        function (value, key) {
          if (key == "undefined") {
            delete this.initialParams.stats[key];
          }
        }.bind(this)
      );
      return this.searchQuiet(qParams, this.initialParams.stats).then(
        function (resp) {
          var statsP = this.createFacetSpecFromStats_(resp.indexData.aggregations);

          var rangeDateP = this.createFacetSpecFromDateRanges_(
            resp.indexData.aggregations,
            qParams
          );

          return this.search(
            qParams,
            angular.merge({}, this.initialParams.facets, rangeDateP, statsP, aggs)
          );
        }.bind(this)
      );
    } else {
      return this.search(qParams, angular.merge({}, this.initialParams.facets, aggs));
    }
  };

  /**
   * Search in ES based on current request params.
   *
   * @param {Object} override to override es request params.
   * @return {angular.Promise}
   */
  geonetwork.gnIndexRequest.prototype.search_es = function (override) {
    var esParams = angular.extend({}, this.reqParams, override);
    return this.$http.post(this.ES_URL, esParams).then(function (response) {
      return response.data;
    });
  };

  geonetwork.gnIndexRequest.prototype.search = function (qParams, indexParams) {
    angular.extend(this.requestParams, {
      any: qParams.any,
      qParams: qParams.params,
      indexParams: indexParams,
      geometry: qParams.geometry,
      filter: this.initialParams.filter
    });
    return this.search_(qParams, indexParams);
  };

  /**
   * Run exact same search but ask only for one field, with more results.
   * @param {object} field to get more elements from.
   * @return {promise} The search promise.
   */
  geonetwork.gnIndexRequest.prototype.getFacetMoreResults = function (field) {
    var aggs = {};
    var agg = this.reqParams.aggs[field.name];
    agg[field.type].size += ROWS;
    aggs[field.name] = agg;

    var params = angular.copy(this.requestParams.qParams);
    if (params) delete params[field.name];

    return this.search_(
      {
        any: this.requestParams.any,
        params: params,
        geometry: this.requestParams.geometry
      },
      aggs,
      false,
      true
    );
  };

  geonetwork.gnIndexRequest.prototype.searchQuiet = function (qParams, indexParams) {
    return this.search_(qParams, indexParams, true);
  };

  geonetwork.gnIndexRequest.prototype.updateSearch = function (params, any, indexParams) {
    return this.search_(
      angular.extend(this.requestParams.qParams, params),
      any,
      angular.extend(this.requestParams.indexParams, indexParams)
    );
  };

  /**
   * Update index url depending on the current facet ui selection state.
   * Each time a facet is selected, we trigger a new search on the index
   * to build the facet ui again with updated occurencies.
   *
   * Will build the index Q query like:
   *  +(LABEL_s:"Abyssal" LABEL_s:Infralittoral)
   *  +featureTypeId:*IFR_AAMP_ZONES_BIO_ATL_P
   *
   * @param {object} params Search params object
   * @param {string} any Filter on any field
   * @return {string} the updated url
   */
  geonetwork.gnIndexRequest.prototype.search_ = function (
    qParams,
    aggs,
    quiet,
    doNotSaveParams
  ) {
    var params = this.buildESParams(qParams, aggs);

    if (!doNotSaveParams) this.reqParams = params;

    return this.$http.post(this.ES_URL, params).then(
      angular.bind(this, function (r) {
        var resp = {
          indexData: r.data,
          records: r.data.hits.hits,
          facets: quiet ? undefined : this.createFacetData_(r.data, params),
          count: r.data.hits.total.value
        };
        if (!quiet) {
          this.sendEvent(
            "search",
            angular.extend({}, resp, {
              sender: this
            })
          );
        }
        return resp;
      })
    );
  };

  geonetwork.gnIndexRequest.prototype.next = function () {
    this.page = {
      from: this.page.start + ROWS, // TODO: Max on total
      size: ROWS
    };
    this.search();
  };
  geonetwork.gnIndexRequest.prototype.previous = function () {
    this.page = {
      from: Math.max(this.page.start - ROWS, 0),
      size: ROWS
    };
    this.search();
  };
  /**
   * Init the IndexRequest object values: baseUrl, and initial params for
   * facets and stats. If configuration contains a docIdField then
   * a filter query is added to the parameters.
   *
   * @param {Object} options from IndexRequest object type config.
   * @private
   */
  geonetwork.gnIndexRequest.prototype.initBaseRequest_ = function (options) {
    this.initialParams = angular.extend({}, this.initialParams, { filter: "" });
    if (this.config.docIdField) {
      this.initialParams.filter =
        "+" + this.config.docIdField + ':"' + this.config.idDoc(options) + '"';
    }
    this.baseUrl = this.ES_URL;
    this.initBaseParams();
  };

  /**
   * Set the ES request base params for facets and stats.
   * All params form the `aggs` request object.
   * It's done on request init, but can be overwritten by application.
   */
  geonetwork.gnIndexRequest.prototype.initBaseParams = function () {
    var facetParams = {};
    var statParams = {};
    var rangeDates = {};

    this.filteredDocTypeFieldsInfo.forEach(
      function (field) {
        // Not required for hidden fields
        if (field.hidden) {
          return;
        }

        // Comes from application profile
        if (field.aggs) {
          field.aggs[Object.keys(field.aggs)[0]].field = field.idxName;
          facetParams[field.idxName || field.label] = field.aggs;
        }
        /*
        else if (field.isTree) {

        }
      */

        // Keep info of date range, like field for max and min dates
        // Also add info in stats to get all date values.
        else if (field.type == "rangeDate") {
          var rangeDate = {};
          ["minField", "maxField"].forEach(
            function (k) {
              var fObj = this.getIdxNameObj_(field[k]);
              if (fObj) {
                statParams[fObj.idxName + "stats"] = {
                  stats: {
                    field: fObj.idxName
                  }
                };
                rangeDate[k] = fObj.idxName;
              }
            }.bind(this)
          );
          rangeDates[field.name] = rangeDate;
        } else if (!field.isRange) {
          if (angular.isDefined(field.idxName)) {
            facetParams[field.idxName] = {
              terms: {
                field: field.idxName,
                size: field.isDateTime ? MAX_ROWS : field.isTree ? FACET_TREE_ROWS : ROWS
              }
            };
            // if (!field.isDateTime) {
            if (field.idxName.match(/^ft_.*_s$/)) {
              // ignore empty strings
              // include/exclude settings as they can only be applied to string fields
              facetParams[field.idxName].terms["exclude"] = "";
            }
          }
        } else {
          statParams[field.idxName + "_stats"] = {
            extended_stats: {
              field: field.idxName
            }
          };
        }
      }.bind(this)
    );

    this.initialParams = angular.extend({}, this.initialParams, {
      facets: facetParams,
      stats: statParams,
      rangeDates: rangeDates
    });
  };

  /**
   * Retrieve the index field object from the array given from feature type
   * info. The object contains the feature type attribute name, the index
   * indexed name, and its label from applicationProfile.
   * You can retrieve this object with the ftName or the docName.
   *
   * @param {string} name
   * @return {*}
   */
  geonetwork.gnIndexRequest.prototype.getIdxNameObj_ = function (name) {
    var fields = this.filteredDocTypeFieldsInfo || [];
    for (var i = 0; i < fields.length; i++) {
      if (fields[i].name == name || fields[i].idxName == name) {
        return fields[i];
      }
    }

    fields = this.docTypeFieldsInfo || [];
    for (var i = 0; i < fields.length; i++) {
      if (fields[i].name == name || fields[i].idxName == name) {
        return fields[i];
      }
    }
  };

  /**
   * Create a facet results description object decoded from ES response.
   * The return object is used for the UI to display all facet list.
   * The object is created from histogram, range, terms or trees
   *
   * @param {Object} indexData  index response object
   * @return {Array} Facet config
   * @private
   */
  geonetwork.gnIndexRequest.prototype.createFacetData_ = function (
    response,
    requestParam
  ) {
    var fields = [];
    for (var fieldId in response.aggregations) {
      if (fieldId.indexOf("_stats") > 0) break;
      if (fieldId.indexOf("bbox_") === 0) continue;
      var respAgg = response.aggregations[fieldId];
      var reqAgg = requestParam.aggs[fieldId];

      var fNameObj = this.getIdxNameObj_(fieldId);

      var facetField = {
        name: fieldId,
        label: fNameObj && fNameObj.label ? fNameObj.label : fieldId,
        display: fNameObj ? fNameObj.display : "",
        type: fNameObj ? fNameObj.type : undefined,
        values: [],
        more: respAgg.sum_other_doc_count
      };

      // histogram
      if (reqAgg.hasOwnProperty("histogram")) {
        facetField.type = "histogram";
        var buckets = respAgg.buckets;
        respAgg.buckets.forEach(function (b, i) {
          var label = "";
          if (i == 0 && respAgg.buckets.length > 1) {
            label = "< " + respAgg.buckets[i + 1].key.toFixed(2);
          } else if (i < respAgg.buckets.length - 1) {
            label =
              b.key.toFixed(2) +
              FACET_RANGE_DELIMITER +
              respAgg.buckets[i + 1].key.toFixed(2);
          } else {
            label = ">= " + b.key.toFixed(2);
          }
          facetField.values.push({
            value: label,
            count: b.doc_count
          });
        });
      }
      // ranges
      if (reqAgg.hasOwnProperty("range")) {
        facetField.type = "range";
        respAgg.buckets.forEach(function (b, i) {
          var label;
          if (b.from && b.to) {
            label = b.from + FACET_RANGE_DELIMITER + b.to;
          } else if (b.to) {
            label = "< " + b.to;
          } else if (b.from) {
            label = "> " + b.from;
          }
          facetField.values.push({
            value: label,
            count: b.doc_count
          });
        });
      } else if (fieldId.endsWith("_tree")) {
        facetField.tree = this.gnFacetTree.getTree(respAgg.buckets);
      }
      // date types
      else if (fieldId.endsWith("_dt") || facetField.type == "rangeDate") {
        if (facetField.type == "rangeDate") {
          fNameObj.isDateTime = true;
          var rangebuckets = [];
          for (var p in respAgg.buckets) {
            var b = respAgg.buckets[p];
            b.key = parseInt(p);
            rangebuckets.push(b);
          }
          respAgg.buckets = rangebuckets;
        } else {
          facetField.type = "date";
        }

        // no date in bucket: do nothing
        if (!respAgg.buckets.length) {
          facetField.dates = [];
          facetField.datesCount = [];
        } else {
          facetField.display = facetField.display || "form";
          var bucketDates = respAgg.buckets.sort(function (a, b) {
            return a.key - b.key;
          });

          if (!fNameObj.allDates) {
            fNameObj.allDates = bucketDates.map(function (b) {
              return b.key;
            });
          }
          facetField.dates = fNameObj.allDates;

          if (facetField.display == "graph" && bucketDates.length > 0) {
            facetField.datesCount = [];
            for (var i = 0; i < bucketDates.length; i++) {
              facetField.datesCount.push({
                value: bucketDates[i].key,
                values: bucketDates[i].key,
                count: bucketDates[i].doc_count
              });
            }
          }
        }
      }
      // filters - response bucket is object instead of array
      else if (reqAgg.hasOwnProperty("filters")) {
        facetField.type = "filters";
        var empty = true;
        for (var p in respAgg.buckets) {
          // results are found for this query: add a value to the facet
          if (respAgg.buckets[p].doc_count > 0) {
            var o = {
              value: p,
              count: respAgg.buckets[p].doc_count
            };
            empty = false;
            if (reqAgg.filters.filters[p].query_string) {
              o.query = reqAgg.filters.filters[p].query_string.query;
            }
            facetField.values.push(o);
          }
        }

        // no value was found: skip this field
        if (empty) {
          facetField = null;
        }
      }

      // terms
      else if (reqAgg.hasOwnProperty("terms")) {
        facetField.type = "terms";
        facetField.size = reqAgg.terms.size;
        for (var i = 0; i < respAgg.buckets.length; i++) {
          facetField.values.push({
            value: respAgg.buckets[i].key,
            count: respAgg.buckets[i].doc_count
          });
        }
      }

      // do not add if undefined (this allows skipping field)
      if (facetField) {
        fields.push(facetField);
      }
    }

    // Sort facets depending on application profile order if any
    if (this.fieldsOrder_) {
      fields.sort(
        function (a, b) {
          return this.fieldsOrder_.indexOf(a.name) - this.fieldsOrder_.indexOf(b.name);
        }.bind(this)
      );
    }
    return fields;
  };

  /**
   * Create index request parameters to generate range facet from stats result.
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
   * @param {Object} indexData object return from index Request
   * @return {{[facet.range]: Array}}
   * @private
   */
  geonetwork.gnIndexRequest.prototype.createFacetSpecFromStats_ = function (aggs) {
    var histograms = {};
    for (var fieldProp in aggs) {
      if (fieldProp.indexOf("_stats") > -1) {
        var fieldName = fieldProp.substr(0, fieldProp.length - 6);
        var field = aggs[fieldProp];
        var interval = (field.max - field.min) / Math.min(FACET_RANGE_COUNT, field.count);

        if (interval) {
          histograms[fieldName] = {
            histogram: {
              field: fieldName,
              interval: interval
            }
          };
        } else {
          histograms[fieldName] = {
            terms: {
              field: fieldName,
              size: ROWS
            }
          };
        }
      }
    }
    return histograms;
  };

  /**
   * Build ES aggs params (filters) from both dates values.
   * The params is a filter, where all entries are a combo of 2 single ranges.
   * @param {Object} aggs
   * @param {Object} params
   * @return {{}}
   * @private
   */
  geonetwork.gnIndexRequest.prototype.createFacetSpecFromDateRanges_ = function (
    aggs,
    params
  ) {
    var filters = {};
    var ret = {};

    angular.forEach(this.initialParams.rangeDates, function (v, k) {
      if (v.minField == undefined) return;

      var initialParam = params.params && params.params[k];

      var minFstats = aggs[v.minField + "stats"];
      var maxFstats = aggs[v.maxField + "stats"];

      var minDate = initialParam
        ? moment(initialParam.values.from, "DD-MM-YYYY")
        : moment(minFstats.min);
      var maxDate = initialParam
        ? moment(initialParam.values.to, "DD-MM-YYYY")
        : moment(maxFstats.max);

      var nbOfDays = maxDate.diff(minDate, "days"),
        interval = "weeks",
        daysByGroup = 7;
      if (nbOfDays > 6000) {
        interval = "years";
        daysByGroup = 365;
      } else if (nbOfDays > 3000) {
        interval = "months";
        daysByGroup = 30.5;
      }
      var dateBuckets = [];
      for (var i = 0; i < nbOfDays / daysByGroup; i++) {
        dateBuckets.push(minDate.clone().add(i, interval).valueOf());
      }
      dateBuckets.forEach(
        function (date, i, array) {
          var rangeSpec = {
            bool: {
              must: [{ range: {} }, { range: {} }]
            }
          };
          rangeSpec.bool.must[0].range[v.minField] = {
            lte: i < array.length - 1 ? array[i + 1] : date,
            format: "epoch_millis"
          };
          rangeSpec.bool.must[1].range[v.maxField] = {
            gte: date,
            format: "epoch_millis"
          };
          filters[date] = rangeSpec;
        }.bind(this)
      );

      ret[k] = {
        filters: {
          filters: filters
        }
      };
    });

    return ret;
  };

  /**
   * Merge the current index request params with the given ones and build the
   * result url. Doesn't update the state of the index object.
   * If state param is provided, the url is computed from the state object
   * instead of current index request params.
   *
   * @param {object} qParams all element that helps to build the `q` param
   * @param {object} indexParams a map with all params name and value for index
   * @param {undefined|object} state of `this.requestParams`
   * @return {string} The merged url
   */
  geonetwork.gnIndexRequest.prototype.getMergedUrl = function (
    qParams_,
    indexParams_,
    state
  ) {
    var p = this.getMergedParams_(qParams_, indexParams_, state);
    return this.getRequestUrl_(p.qParams, p.indexParams);
  };

  /**
   * Get index request parameters (q and others) from an given state
   * object or the current index param state, merged with the given parameters.
   *
   * @param {object} qParams all element that helps to build the `q` param
   * @param {object} indexParams a map with all params name and value for index
   * @param {undefined|object} state of `this.requestParams`
   * @return {string} The merged url
   */
  geonetwork.gnIndexRequest.prototype.getMergedParams_ = function (
    qParams_,
    indexParams_,
    state
  ) {
    var baseObj = state || this.requestParams;
    return {
      qParams: this.getMergedQParams_(qParams_, baseObj),
      indexParams: this.getMergedindexParams_(indexParams_, baseObj)
    };
  };

  //TODO: confusing types, qParams is an object with (params,any,geometry) keys,
  //TODO: while this.requestParams.qParams is just the params object
  //TODO: change the this.requestParams type to reflect all method signatures
  geonetwork.gnIndexRequest.prototype.getMergedQParams_ = function (qParams_, baseObj) {
    return {
      params: angular.extend({}, baseObj.qParams, qParams_.params),
      any: qParams_.any || baseObj.any,
      geometry: qParams_.geometry || baseObj.geometry
    };
  };

  geonetwork.gnIndexRequest.prototype.getMergedindexParams_ = function (
    indexParams_,
    baseObj
  ) {
    return angular.extend({}, baseObj.indexParams, indexParams_);
  };

  geonetwork.gnIndexRequest.prototype.getRequestUrl_ = function (qParams, indexParams) {
    return this.baseUrl + this.buildUrlParams_(qParams, indexParams);
  };

  geonetwork.gnIndexRequest.prototype.buildUrlParams_ = function (qParams, indexParams) {
    return this.buildQParam_(qParams) + this.parseKeyValue_(indexParams);
  };
  geonetwork.gnIndexRequest.prototype.getSearchQuery = function (params) {
    return this.buildQParam_(params, params.qParams);
  };

  /**
   * Put in `fieldsOrder_` the order of the fields to display in facets.
   * This order comes from application profile if not extended.
   */
  geonetwork.gnIndexRequest.prototype.setFielsdOrder = function () {
    this.fieldsOrder_ = [];
    this.filteredDocTypeFieldsInfo.forEach(
      function (f) {
        this.fieldsOrder_.push(f.idxName || f.name);
      }.bind(this)
    );
  };

  /**
   * qParams:
   *   any
   *   geometry
   *   params
   *     type
   *     values
   */
  geonetwork.gnIndexRequest.prototype.buildESParams = function (
    qParams,
    aggs,
    start,
    rows
  ) {
    var params = {
      from: start !== undefined ? start : this.page.start,
      size: rows !== undefined ? rows : this.page.rows,
      aggs: aggs,
      track_total_hits: true
    };

    params.query = {
      bool: {
        must: [
          {
            query_string: {
              query: this.buildQParam_(qParams) || "*:*"
            }
          }
        ]
      }
    };

    if (qParams.geometry) {
      params.query.bool.filter = {
        geo_shape: {
          geom: {
            shape: {
              type: "envelope",
              coordinates: qParams.geometry
            },
            relation: "intersects"
          }
        }
      };
    }

    angular.forEach(
      qParams.params,
      function (field, fieldName) {
        if (field.type == "date" || field.type == "rangeDate") {
          var gte,
            lte,
            range = {};
          var date = field.value || field.values;
          if (angular.isObject(date)) {
            gte = date.from;
            lte = date.to;
          } else {
            gte = lte = date;
          }
          if (field.type == "date") {
            range[fieldName] = {
              gte: gte,
              lte: lte,
              format: "dd-MM-yyyy"
            };
            params.query.bool.must.push({
              range: range
            });
          } else {
            range[this.initialParams.rangeDates[fieldName].minField] = {
              lte: lte,
              format: "dd-MM-yyyy"
            };
            params.query.bool.must.push({
              range: range
            });
            range = {};
            range[this.initialParams.rangeDates[fieldName].maxField] = {
              gte: gte,
              format: "dd-MM-yyyy"
            };
            params.query.bool.must.push({
              range: range
            });
          }
        }
      }.bind(this)
    );

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
  geonetwork.gnIndexRequest.prototype.buildQParam_ = function (qParams) {
    var fieldsQ = [],
      qParam = "",
      any = qParams.any,
      geometry = qParams.geometry;

    // TODO move this in createFacetData_ ? query param
    angular.forEach(qParams.params, function (field, fieldName) {
      var valuesQ = [];
      if (field.type == "date" || field.type == "rangeDate") {
        return;
      }
      var value;
      for (var p in field.values) {
        // ignore undefined values
        if (field.values[p] === undefined) {
          continue;
        }

        if (field.type == "histogram" || field.type == "range") {
          if (p.indexOf(FACET_RANGE_DELIMITER) > -1) {
            value = fieldName + ":[" + p.replace(FACET_RANGE_DELIMITER, " TO ") + "}";
          } else {
            value = fieldName + ":" + p.replace(/ /g, "");
          }
        } else if (field.type == "filters") {
          value = field.query;
        } else {
          value = fieldName + ':"' + p + '"';
        }
        valuesQ.push(value);
      }
      if (valuesQ.length) {
        fieldsQ.push("+(" + valuesQ.join(" ") + ")");
      }
    });

    angular.forEach(qParams.qParams, function (field, fieldName) {
      var valuesQ = [];
      if (field.type == "date" || field.type == "rangeDate") {
        return;
      }
      var value;
      for (var p in field.values) {
        // ignore undefined values
        if (field.values[p] === undefined) {
          continue;
        }

        if (field.type == "histogram" || field.type == "range") {
          if (p.indexOf(FACET_RANGE_DELIMITER) > 0) {
            value = fieldName + ":[" + p.replace(FACET_RANGE_DELIMITER, " TO ") + "}";
          } else {
            value = fieldName + ":" + p.replace(/ /g, "");
          }
        } else if (field.type == "filters") {
          value = field.query;
        } else {
          value = fieldName + ':"' + p + '"';
        }
        valuesQ.push(value);
      }
      if (valuesQ.length) {
        fieldsQ.push("+(" + valuesQ.join(" ") + ")");
      }
    });

    if (any) {
      any.split(" ").forEach(function (v) {
        fieldsQ.push("+*" + v + "*");
      });
    }
    if (this.initialParams.filter) {
      fieldsQ.push(this.initialParams.filter);
    }

    // Search for all if no filter defined
    var filter;
    if (fieldsQ.length === 0) {
      fieldsQ.push("*:*");
      filter = fieldsQ.join(" ");
    } else {
      filter = "(" + fieldsQ.join(") AND (") + ")";
    }

    // FIXME: This sounds useless as we already built filter based on fieldsQ
    if (this.initialParams.filter != "") {
      fieldsQ.push(this.initialParams.filter);
    }

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
  geonetwork.gnIndexRequest.prototype.parseKeyValue_ = function (params) {
    var urlParams = "";
    angular.forEach(params, function (v, k) {
      if (angular.isArray(v)) {
        v.forEach(function (f) {
          urlParams += "&" + k + "=" + f;
        });
      } else {
        urlParams += "&" + k + "=" + v;
      }
    });
    return urlParams;
  };

  // === EVENTS LISTENER ====
  geonetwork.gnIndexRequest.prototype.on = function (key, callback, opt_this) {
    this.eventsListener[key].push({ callback: callback, this: opt_this });
  };
  geonetwork.gnIndexRequest.prototype.sendEvent = function (key, args) {
    this.eventsListener[key].forEach(
      angular.bind(this, function (event) {
        event.callback.call(event.this || this, args);
      })
    );
  };

  // === STATE MANAGEMENT ====
  geonetwork.gnIndexRequest.prototype.pushState = function (state) {
    var state_ = state || angular.copy(this.requestParams);
    this.states_.push(state_);
  };
  geonetwork.gnIndexRequest.prototype.popState = function () {
    return this.states_.pop();
  };
  geonetwork.gnIndexRequest.prototype.getState = function (idx) {
    var idx_ = idx || this.states_.length - 1;
    return (idx_ >= 0 && this.states_[idx_]) || null;
  };

  if (!String.prototype.endsWith) {
    String.prototype.endsWith = function (searchString, position) {
      var subjectString = this.toString();
      if (
        typeof position !== "number" ||
        !isFinite(position) ||
        Math.floor(position) !== position ||
        position > subjectString.length
      ) {
        position = subjectString.length;
      }
      position -= searchString.length;
      var lastIndex = subjectString.lastIndexOf(searchString, position);
      return lastIndex !== -1 && lastIndex === position;
    };
  }
})();
