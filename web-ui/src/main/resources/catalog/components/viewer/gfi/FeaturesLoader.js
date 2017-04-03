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
  geonetwork.GnFeaturesSOLRLoader = function(config, $injector) {
    geonetwork.GnFeaturesLoader.call(this, config, $injector);

    this.layer = config.layer;
    this.coordinates = config.coordinates;
    this.solrObject = config.solrObject;
  };

  geonetwork.inherits(geonetwork.GnFeaturesSOLRLoader,
      geonetwork.GnFeaturesLoader);


  geonetwork.GnFeaturesSOLRLoader.prototype.getBsTableConfig = function() {
    var $q = this.$injector.get('$q');
    var defer = $q.defer();
    var $filter = this.$injector.get('$filter');

    var pageList = [5, 10, 50, 100],
        columns = [],
        solr = this.solrObject,
        map = this.map,
        fields = solr.indexFields || solr.filteredDocTypeFieldsInfo;

    fields.forEach(function(field) {
      if ($.inArray(field.idxName, this.excludeCols) === -1) {
        columns.push({
          field: field.idxName,
          title: field.label,
          titleTooltip: field.label,
          sortable: true,
          formatter: function(val, row, index) {
            var text = (val) ? val.toString() : '';
            text = $filter('linky')(text, '_blank');
            text = text.replace(/>(.)*</,
                ' ' + 'target="_blank">' + linkTpl + '<'
                );
            return text;
          }
        });
      }
    });

    // get an update solr request url with geometry filter based on a point
    var url = this.coordinates ?
        this.solrObject.getMergedUrl({}, {
          pt: ol.proj.transform(this.coordinates,
              map.getView().getProjection(), 'EPSG:4326').reverse().join(','),
          //5 pixels radius tolerance
          d: map.getView().getResolution() / 400,
          sfield: solr.geomField.idxName
        }, this.solrObject.getState()) +
        '&fq={!geofilt sfield=' + solr.geomField.idxName + '}' :
            this.solrObject.getMergedUrl({}, {}, this.solrObject.getState());

    url = url.replace('rows=0', '');
    if (url.indexOf('&q=') === -1) {
      url += '&q=*:*';
    }
    this.loading = true;
    defer.resolve({
      url: url,
      queryParams: function(p) {
        var params = {
          rows: p.limit,
          start: p.offset
        };
        if (p.sort) {
          params.sort = p.sort + ' ' + p.order;
        }
        return params;
      },
      //data: scope.data.response.docs,
      responseHandler: function(res) {
        this.count = res.response.numFound;
        return {
          total: res.response.numFound,
          rows: res.response.docs
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
      totalRows: this.solrObject.totalCount,
      pageSize: pageList[1],
      pageList: pageList
    });
    return defer.promise;
  };

  geonetwork.GnFeaturesSOLRLoader.prototype.getCount = function() {
    return this.count;
  };

  geonetwork.GnFeaturesSOLRLoader.prototype.getFeatureFromRow = function(row) {
    var geom = row[this.solrObject.geomField.idxName];
    if (angular.isArray(geom)) {
      geom = geom[0];
    }
    geom = new ol.format.WKT().readFeature(geom, {
      dataProjection: 'EPSG:4326',
      featureProjection: this.map.getView().getProjection()
    });
    return geom;
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
