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
  goog.provide('gn_featurestable_loader');

  var module = angular.module('gn_featurestable_loader', []);

  var linkTpl = '<span class="fa-stack">' +
      '<i class="fa fa-square fa-stack-2x"></i>' +
      '<i class="fa fa-link fa-stack-1x fa-inverse"></i>' +
      '</span>';

  geonetwork.inherits = function(childCtor, parentCtor) {
    function tempCtor() {
    };
    tempCtor.prototype = parentCtor.prototype;
    childCtor.superClass_ = parentCtor.prototype;
    childCtor.prototype = new tempCtor();
    childCtor.prototype.constructor = childCtor;
  };



  /**
   * @abstract
   * @constructor
   */
  geonetwork.GnFeaturesLoader = function(config, $injector) {
    this.$injector = $injector;
    this.$http = this.$injector.get('$http');
    this.urlUtils = this.$injector.get('gnUrlUtils');
    this.gnProxyUrl = this.$injector.get('gnGlobalSettings').proxyUrl;

    this.layer = config.layer;
    this.map = config.map;

    this.excludeCols = [];
  };
  geonetwork.GnFeaturesLoader.prototype.load = function() {};
  geonetwork.GnFeaturesLoader.prototype.loadAll = function() {};
  geonetwork.GnFeaturesLoader.prototype.getBsTableConfig = function() {};

  geonetwork.GnFeaturesLoader.prototype.isLoading = function() {
    return this.loading;
  };

  geonetwork.GnFeaturesLoader.prototype.proxyfyUrl = function(url) {
    return this.gnProxyUrl + encodeURIComponent(url);
  };

  /**
   *
   * @constructor
   */
  geonetwork.GnFeaturesGFILoader = function(config, $injector) {

    geonetwork.GnFeaturesLoader.call(this, config, $injector);

    this.coordinates = config.coordinates;
  };

  geonetwork.inherits(geonetwork.GnFeaturesGFILoader,
      geonetwork.GnFeaturesLoader);

  geonetwork.GnFeaturesGFILoader.prototype.loadAll = function() {
    var layer = this.layer,
        map = this.map,
        coordinates = this.coordinates;

    var uri = layer.getSource().getGetFeatureInfoUrl(
        coordinates,
        map.getView().getResolution(),
        map.getView().getProjection(),
        {
          INFO_FORMAT: layer.ncInfo ? 'text/xml' : 'application/vnd.ogc.gml'
        }
        );
    uri += '&FEATURE_COUNT=2147483647';

    this.loading = true;
    this.promise = this.$http.get(
        this.proxyfyUrl(uri)).then(function(response) {

          this.loading = false;
          if (layer.ncInfo) {
            var doc = ol.xml.parse(response.data);
            var props = {};
            ['longitude', 'latitude', 'time', 'value'].forEach(function(v) {
              var node = doc.getElementsByTagName(v);
              if (node && node.length > 0) {
                props[v] = ol.xml.getAllTextContent(node[0], true);
              }
            });
            this.features = (props.value && props.value != 'none') ?
                [new ol.Feature(props)] : [];
          } else {
            var format = new ol.format.WMSGetFeatureInfo();
            this.features = format.readFeatures(response.data, {
              featureProjection: map.getView().getProjection()
            });
          }

          return this.features;

        }.bind(this), function() {

          this.loading = false;
          this.error = true;

        }.bind(this));

  };

  geonetwork.GnFeaturesGFILoader.prototype.getBsTableConfig = function() {
    var pageList = [5, 10, 50, 100];
    var exclude = ['FID', 'boundedBy', 'the_geom', 'thegeom'];
    var $filter = this.$injector.get('$filter');

    return this.promise.then(function(features) {
      if (!features || features.length == 0) {
        return;
      }
      var columns = Object.keys(features[0].getProperties()).map(function(x) {
        return {
          field: x,
          title: x,
          titleTooltip: x,
          sortable: true,
          visible: exclude.indexOf(x) == -1
        };
      });

      return {
        columns: columns,
        data: features.map(function(f) {
          var obj = f.getProperties();
          Object.keys(obj).forEach(function(key) {
            if (exclude.indexOf(key) == -1) {
              obj[key] = $filter('linky')(obj[key], '_blank');
              if (obj[key]) {
                obj[key] = obj[key].replace(/>(.)*</, ' ' +
                    'target="_blank">' + linkTpl + '<');
              }
            }
          });
          return obj;
        }),
        pagination: true,
        pageSize: pageList[1],
        pageList: pageList
      };
    });
  };


  geonetwork.GnFeaturesGFILoader.prototype.getCount = function() {
    if (!this.features) {
      return 0;
    }
    return this.features.length;
  };

  geonetwork.GnFeaturesGFILoader.prototype.getFeatureFromRow = function(row) {
    var geoms = ['the_geom', 'thegeom', 'boundedBy'];
    for (var i = 0; i < geoms.length; i++) {
      var geom = row[geoms[i]];
      if (geoms[i] == 'boundedBy' && jQuery.isArray(geom)) {
        if (geom[0] == geom[2] && geom[1] == geom[3]) {
          geom = new ol.geom.Point([geom[0], geom[1]]);
        } else {
          geom = new ol.geom.Polygon.fromExtent(geom);
        }
        if (this.projection) {
          geom = geom.transform(
              this.projection,
              this.map.getView().getProjection()
              );
        }
      }
      if (geom instanceof ol.geom.Geometry) {
        return new ol.Feature({
          geometry: geom
        });
      }
    }
  };

  /**
   *
   * @constructor
   */
  geonetwork.GnFeaturesINDEXLoader = function(config, $injector) {
    geonetwork.GnFeaturesLoader.call(this, config, $injector);

    this.layer = config.layer;
    this.coordinates = config.coordinates;
    this.indexObject = config.indexObject;
  };

  geonetwork.inherits(geonetwork.GnFeaturesINDEXLoader,
      geonetwork.GnFeaturesLoader);

  /**
   * Format an url type attribute to a html link <a href=...">.
   * @return {*}
   * @private
   */
  geonetwork.GnFeaturesINDEXLoader.prototype.formatUrlValues_ = function(url) {
    var $filter = this.$injector.get('$filter');

    url = this.fillUrlWithFilter_(url);
    var link = $filter('linky')(url, '_blank');
    if (link != url) {
      link = link.replace(/>(.)*</,
          ' ' + 'target="_blank">' + linkTpl + '<'
          );
    }
    return link;
  };

  /**
   * Substitutes predefined filter value in urls.
   * http://www.emso-fr.org?${filtre_param_liste}${filtre_param_group_liste} is
   * transformed into
   * http://www.emso-fr.org?param_liste=Escherichia&param_group_liste=Microbio
   * if those value are set in wfsFilter facets search.
   *
   * @param {string} url
   * @return {string} substitued url
   * @private
   */
  geonetwork.GnFeaturesINDEXLoader.prototype.fillUrlWithFilter_ =
      function(url) {

    var indexFilters = this.indexObject.getState();

    var URL_SUBSTITUTE_PREFIX = 'filtre_';
    var regex = /\$\{(\w+)\}/g;
    var placeholders = [];
    var urlFilters = [];
    var paramsToAdd = {};
    var match;

    while (match = regex.exec(url)) {
      placeholders.push(match[0]);
      urlFilters.push(match[1].substring(
          URL_SUBSTITUTE_PREFIX.length, match[1].length));
    }

    urlFilters.forEach(function(p, i) {
      var name = p;
      var idxName = this.indexObject.getIdxNameObj_(name).idxName;
      var fValue = indexFilters.qParams[idxName];
      url = url.replace(placeholders[i], '');

      if (fValue) {
        paramsToAdd[name] = Object.keys(fValue.values)[0];
      }
    }.bind(this));

    return this.urlUtils.append(url, this.urlUtils.toKeyValue(paramsToAdd));
  };

  geonetwork.GnFeaturesINDEXLoader.prototype.getBsTableConfig = function() {
    var $q = this.$injector.get('$q');
    var defer = $q.defer();
    var $filter = this.$injector.get('$filter');

    var pageList = [5, 10, 50, 100],
        columns = [],
        index = this.indexObject,
        map = this.map,
        fields = index.indexFields || index.filteredDocTypeFieldsInfo;

    fields.forEach(function(field) {
      if ($.inArray(field.idxName, this.excludeCols) === -1) {
        columns.push({
          field: field.idxName,
          title: field.label,
          titleTooltip: field.label,
          sortable: true,
          formatter: function(val, row, index) {
            var outputValue = val;
            if (this.urlUtils.isValid(val)) {
              outputValue = this.formatUrlValues_(val);
            }
            return outputValue;
          }.bind(this)
        });
      }
    }.bind(this));

    // get an update index request url with geometry filter based on a point
    var url = this.indexObject.baseUrl;
    var state = this.indexObject.getState();
    var searchQuery = this.indexObject.getSearhQuery(state);

    this.loading = true;
    defer.resolve({
      url: url,
      contentType: 'application/json',
      method: 'POST',
      queryParams: function(p) {

        // TODO: Should use indexObject.search_ ?
        var params = angular.extend({},
            {
              query: {query_string: {query: searchQuery}}},
            {
              size: p.limit,
              from: p.offset
            });
        if (p.sort) {
          params.sort = [];
          var sort = {};
          sort[p.sort] = {'order' : p.order};
          params.sort.push(sort);
        }


        if (state.geometry || this.coordinates) {
          var geomFilter = {};
          if (state.geometry) {
            geomFilter = {'geo_shape': {
              'geom': {
                'shape': {
                  'type': 'envelope',
                  'coordinates': state.geometry
                },
                'relation': 'intersects'
              }
            }
            };
          } else if (this.coordinates) {
            var coords = ol.proj.transform(this.coordinates,
                map.getView().getProjection(), 'EPSG:4326');
            geomFilter = {'geo_distance' : {
              'distance': map.getView().getResolution() / 400 + 'km',
              'geom': {
                'lat': coords[1],
                'lon': coords[0]
              }
            }
            };
          }
          params.query = {
            'bool': {
              'must': {
                'query_string': params.query.query_string || '*:*'
              },
              'filter': geomFilter
            }
          };
        }

        return JSON.stringify(params);
      },
      //data: scope.data.response.docs,
      responseHandler: function(res) {
        this.count = res.hits.total;
        var rows = [];
        for (var i = 0; i < res.hits.hits.length; i++) {
          rows.push(res.hits.hits[i]._source);
        }
        return {
          total: res.hits.total,
          rows: rows
        };
      }.bind(this),
      onSort: function() {
        this.loading = true;
      }.bind(this),
      onLoadSuccess: function() {
        this.loading = false;
        this.error = false;
      }.bind(this),
      onLoadError: function() {
        this.loading = false;
        this.error = true;
      }.bind(this),
      columns: columns,
      pagination: true,
      sidePagination: 'server',
      totalRows: this.indexObject.totalCount,
      pageSize: pageList[1],
      pageList: pageList
    });
    return defer.promise;
  };

  geonetwork.GnFeaturesINDEXLoader.prototype.getCount = function() {
    return this.count;
  };

  geonetwork.GnFeaturesINDEXLoader.prototype.getFeatureFromRow = function(row) {
    var geom = row[this.indexObject.geomField.idxName];
    if (angular.isArray(geom)) {
      geom = geom[0];
    }
    geom = new ol.format.GeoJSON().readGeometry(geom, {
      dataProjection: 'EPSG:4326',
      featureProjection: this.map.getView().getProjection()
    });
    return new ol.Feature({geometry: geom});
  };



  /**
   *
   * @constructor
   */
  var GnFeaturesTableLoaderService = function($injector) {
    this.$injector = $injector;
  };
  GnFeaturesTableLoaderService.prototype.createLoader = function(type, config) {
    var constructor = geonetwork['GnFeatures' + type.toUpperCase() + 'Loader'];
    if (!angular.isFunction(constructor)) {
      console.warn('Cannot find constructor for loader type : ' + type);
    }
    return new constructor(config, this.$injector);
  };
  module.service('gnFeaturesTableLoader', [
    '$injector',
    GnFeaturesTableLoaderService]);

})();
