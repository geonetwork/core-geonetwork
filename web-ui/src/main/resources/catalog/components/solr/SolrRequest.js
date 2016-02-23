(function() {
  goog.provide('gn_solr_request');

  var module = angular.module('gn_solr_request', []);

  var solrRequestEvents = {
      search: 'search'
  };

  var FACET_RANGE_COUNT = 5;

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
    this.filteredDocTypeFieldsInfo;

    /**
     * @type {Object}
     * The solr url request, as an object with `baseUrl`, `facets` and `stats`
     * properties
     */
    this.uri;

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
            attrName: customF[i],
            idxName: docF[i],
            isRange: docF[i] == 'PRL_CPRL_s', //docF[i].endsWith('_i'),
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
        this.uri = this.getBaseRequest(options);
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

  geonet.GnSolrRequest.prototype.searchWithFacets = function(params, any) {
    var needStats = false;
    this.filteredDocTypeFieldsInfo.forEach(function(field) {
      if(field.isRange) {
        needStats = true;
        return false;
      }
    });

    if(needStats) {
      console.log('range');

      var url = this.getSearchUrl_(params, any) + this.uri.stats;

      return this.$http.get(url).then(angular.bind(this,
          function(solrResponse) {
            var params = this.createFacetSpecFromStats_(solrResponse.data);
      }));

    }
    else {
      return this.search(params, any, {facets:true});
    }
  };

  /**
   * Update the baseUrl with search params. Search params can be on any field,
   * or for a specific field.
   *
   * @param params
   * @param any {string} text search on any field
   * @return {string} the updated url
   * @private
   */
  geonet.GnSolrRequest.prototype.getSearchUrl_ = function(params, any) {

    var fieldsQ = [];
    angular.forEach(params, function(field, fieldName) {
      var valuesQ = [];
      for (var p in field.values) {
        valuesQ.push(fieldName + ':"' + p + '"');
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
    var url = this.uri.baseUrl;
    if (fieldsQ.length) {
      url = url.replace('&q=', '&q=' +
          encodeURIComponent(fieldsQ.join(' ') + ' +'));
    }
    return url;
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
  geonet.GnSolrRequest.prototype.search = function(params, any, config_opt) {

    var config = config_opt || {};
    var url = this.getSearchUrl_(params, any);

    if(config.facets) {
      url += this.uri.facets;
    }
    if(config.stats) {
      url += this.uri.stats;
    }

    return this.$http.get(url).then(angular.bind(this,
        function(solrResponse) {

          this.count = solrResponse.data.response.numFound;
          this.sendEvent('search', {
            sender: this,
            url: url,
            records: solrResponse.data.response.docs
          });
          return {
            facetConfig: this.createFacetData_(solrResponse.data),
            count: this.count
          };
    }));
  };

  geonet.GnSolrRequest.prototype.getBaseRequest = function(options) {
    var facetParams = '',
        statParams = '';

    var url = this.buildSolrUrl({
      rows: 0,
      q: this.config.docIdField + ':"' + this.config.idDoc(options) + '"',
      wt: 'json'
    });

    if(this.config.facets) {
      facetParams += '&facet=true';
      facetParams += '&facet.mincount=1';
      this.filteredDocTypeFieldsInfo.forEach(function(field) {
        facetParams += '&facet.field=' + field.idxName;
      });
    }

    if(this.config.stats) {
      statParams += '&stats=true';
      this.filteredDocTypeFieldsInfo.forEach(function(field) {
        if(field.isRange) {
          statParams += '&stats.field=' + field.idxName;
        }
      });
    }

    return {
      baseUrl: url,
      facets: facetParams,
      stats: statParams
    }
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
      if (fields[i].attrName == name ||
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

  geonet.GnSolrRequest.prototype.createFacetData_ = function(solrData) {
    var fields = [];
    for (var kind in solrData.facet_counts) {
      var facetType = this.getFacetType_(kind);
      for (var fieldProp in solrData.facet_counts[kind]) {
        var field = solrData.facet_counts[kind][fieldProp];
        var fNameObj = this.getIdxNameObj_(fieldProp);
        var facetField = {
          name: fieldProp,
          label: fNameObj.label || fNameObj.attrName,
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

  geonet.GnSolrRequest.prototype.createFacetSpecFromStats_ = function(solrData) {
    var fields = [];
    for (var fieldProp in solrData.stats.stats_fields) {
      var field = solrData.stats.stats_fields[fieldProp];
      fields.push({
        "facet.range": fieldProp,
        "facet.range.start": field.min,
        "facet.range.end": field.max,
        "facet.range.gap": (field.max-field.min) / FACET_RANGE_COUNT
      });
    }
    console.log(fields);

    return fields;
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
