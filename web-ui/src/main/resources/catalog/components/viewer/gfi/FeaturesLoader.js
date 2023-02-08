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
  goog.provide("gn_featurestable_loader");

  var module = angular.module("gn_featurestable_loader", []);

  var linkTpl =
    '<span class="fa-stack">' +
    '<i class="fa fa-square fa-stack-2x"></i>' +
    '<i class="fa fa-link fa-stack-1x fa-inverse"></i>' +
    "</span>";

  geonetwork.inherits = function (childCtor, parentCtor) {
    function tempCtor() {}
    tempCtor.prototype = parentCtor.prototype;
    childCtor.superClass_ = parentCtor.prototype;
    childCtor.prototype = new tempCtor();
    childCtor.prototype.constructor = childCtor;
  };

  /**
   * @abstract
   * @constructor
   */
  geonetwork.GnFeaturesLoader = function (config, $injector) {
    this.$injector = $injector;
    this.$http = this.$injector.get("$http");
    this.urlUtils = this.$injector.get("gnUrlUtils");
    this.featureService = this.$injector.get("gnFeaturesTableService");

    this.layer = config.layer;
    this.map = config.map;

    this.excludeCols = [];
  };
  geonetwork.GnFeaturesLoader.prototype.load = function () {};
  geonetwork.GnFeaturesLoader.prototype.loadAll = function () {};
  geonetwork.GnFeaturesLoader.prototype.getBsTableConfig = function () {};

  geonetwork.GnFeaturesLoader.prototype.isLoading = function () {
    return this.loading;
  };

  /**
   *
   * @constructor
   */
  geonetwork.GnFeaturesGFILoader = function (config, $injector) {
    geonetwork.GnFeaturesLoader.call(this, config, $injector);

    this.coordinates = config.coordinates;
  };

  geonetwork.inherits(geonetwork.GnFeaturesGFILoader, geonetwork.GnFeaturesLoader);

  geonetwork.GnFeaturesGFILoader.prototype.loadAll = function () {
    var layer = this.layer,
      map = this.map,
      coordinates = this.coordinates;

    var uuid;
    if (layer.get("md")) {
      uuid = layer.get("md").uuid;
    } else if (layer.get("metadataUuid")) {
      uuid = layer.get("metadataUuid");
    }

    var infoFormat = false;

    //check if infoFormat is available in getCapabilities
    if (
      layer.get("capRequest") &&
      layer.get("capRequest").GetFeatureInfo &&
      angular.isArray(layer.get("capRequest").GetFeatureInfo.Format) &&
      layer.get("capRequest").GetFeatureInfo.Format.length > 0
    ) {
      if ($.inArray(infoFormat, layer.get("capRequest").GetFeatureInfo.Format) == -1) {
        // Search for available formats friendly to us
        // using plain standard EMACS Javascript
        var friendlyFormats = layer
          .get("capRequest")
          .GetFeatureInfo.Format.filter(function (el) {
            return (
              el.match(/application\/(geojson|json|geo\+json|vnd.ogc.gml)|text\/xml/i) !=
              null
            );
          });

        if (friendlyFormats.length > 0) {
          //take any of them
          infoFormat = friendlyFormats[0];
        }

        //Heavy failback: take any available format
        //we will deal later with this unknown and
        //trust OpenLayers know how to deal with it
        if (
          !infoFormat &&
          layer.get("capRequest").GetFeatureInfo.Format.length &&
          layer.get("capRequest").GetFeatureInfo.Format.length > 0
        ) {
          layer.infoFormat = layer.get("capRequest").GetFeatureInfo.Format[0];
        }
      }
    }

    //Did we get anything from getCapabilities?
    if (infoFormat) {
      layer.infoFormat = infoFormat;
    }

    var uri = layer
      .getSource()
      .getFeatureInfoUrl(
        coordinates,
        map.getView().getResolution(),
        map.getView().getProjection(),
        { INFO_FORMAT: infoFormat }
      );
    uri += "&FEATURE_COUNT=2147483647";

    this.loading = true;
    this.promise = this.$http
      .get(uri, {
        data: "",
        headers: {
          "Content-Type": "text/plain"
        }
      })
      .then(
        function (response) {
          if (infoFormat && infoFormat.match(/application\/(geo|geo\+)json/i) != null) {
            var jsonf = new ol.format.GeoJSON();
            var features = [];
            response.data.features.forEach(function (f) {
              features.push(jsonf.readFeature(f));
            });
            this.features = features;
          } else if (
            infoFormat &&
            infoFormat.match(/application\/vnd.ogc.gml|text\/xml/i) != null
          ) {
            var format = new ol.format.WMSGetFeatureInfo();
            this.features = format.readFeatures(response.data, {
              featureProjection: map.getView().getProjection()
            });
          } else {
            //Ooops, unknown format.
            console.warn("Unknown format for GetFeatureInfo " + infoFormat);

            //Try anyway with the default one and cross fingers
            var format = new ol.format.WMSGetFeatureInfo();
            this.features = format.readFeatures(response.data, {
              featureProjection: map.getView().getProjection()
            });
          }

          this.loading = false;
          return this.features;
        }.bind(this),
        function () {
          this.loading = false;
          this.error = true;
        }.bind(this)
      );

    this.dictionary = null;
    if (uuid) {
      this.dictionary = this.featureService
        .loadFeatureCatalogue(uuid, layer.get("md"))
        .then(function (catalogue) {
          return catalogue;
        });
    }
  };

  geonetwork.GnFeaturesGFILoader.prototype.formatUrlValues_ = function (url) {
    return '<a href="' + url + '" target="_blank">' + linkTpl + "</a>";
  };

  geonetwork.GnFeaturesGFILoader.prototype.getBsTableConfig = function () {
    var pageList = [5, 10, 50, 100];
    var exclude = ["FID", "boundedBy", "the_geom", "thegeom"];
    var $filter = this.$injector.get("$filter");
    var $q = this.$injector.get("$q");
    var that = this;

    var promises = [this.promise, this.dictionary];

    return $q.all(promises).then(function (data) {
      features = data[0];
      dictionary = data[1];

      if (!features || features.length == 0) {
        return;
      }

      var data = features.map(function (f) {
        var obj = f.getProperties();
        Object.keys(obj).forEach(function (key) {
          if (exclude.indexOf(key) == -1) {
            var value = obj[key];
            if (!(obj[key] instanceof Object)) {
              //Make sure it is a string and not a number
              obj[key] = obj[key] + "";

              // Linky directive may create invalid link if a full HTTP
              // URL is provided with character like ")" which will be
              // considered as text (eg. Kibana link with filters).
              if (that.urlUtils.isValid(obj[key])) {
                obj[key] = that.formatUrlValues_(obj[key]);
              } else {
                obj[key] = $filter("linky")(obj[key], "_blank");
              }
              if (obj[key]) {
                obj[key] = obj[key].replace(
                  />(.)*</,
                  " " + 'target="_blank">' + linkTpl + "<"
                );
              }
              // TODO: if a dictionary is provided, translate keys by label ? eg. F = Forest
            } else {
              // Exclude objects which will not be displayed properly
              exclude.push(key);
            }
          }
        });
        return obj;
      });

      var columns = Object.keys(features[0].getProperties()).map(function (x) {
        return {
          field: x.toLowerCase(),
          title: x,
          titleTooltip: x,
          sortable: true,
          visible: exclude.indexOf(x) == -1
        };
      });

      if (dictionary != null) {
        for (var i = 0; i < columns.length; i++) {
          var fieldSpec = dictionary[columns[i]["field"]];
          if (angular.isDefined(fieldSpec)) {
            columns[i]["title"] = fieldSpec.name;
            columns[i]["titleTooltip"] = fieldSpec.definition || fieldSpec.name;
          }
        }
      }

      return {
        columns: columns,
        data: data,
        pagination: true,
        pageSize: pageList[1],
        pageList: pageList,
        undefinedText: ""
      };
    });
  };

  geonetwork.GnFeaturesGFILoader.prototype.getCount = function () {
    if (!this.features) {
      return 0;
    }
    return this.features.length;
  };

  geonetwork.GnFeaturesGFILoader.prototype.getFeatureFromRow = function (row) {
    var geoms = ["the_geom", "thegeom", "boundedBy", "geometry"];
    for (var i = 0; i < geoms.length; i++) {
      var geom = row[geoms[i]];
      if (geoms[i] == "boundedBy" && jQuery.isArray(geom)) {
        if (geom[0] == geom[2] && geom[1] == geom[3]) {
          geom = new ol.geom.Point([geom[0], geom[1]]);
        } else {
          geom = new ol.geom.Polygon.fromExtent(geom);
        }
        if (this.projection) {
          geom = geom.transform(this.projection, this.map.getView().getProjection());
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
  geonetwork.GnFeaturesINDEXLoader = function (config, $injector) {
    geonetwork.GnFeaturesLoader.call(this, config, $injector);

    this.layer = config.layer;
    this.coordinates = config.coordinates;
    this.indexObject = config.indexObject;
  };

  geonetwork.inherits(geonetwork.GnFeaturesINDEXLoader, geonetwork.GnFeaturesLoader);

  /**
   * Format an url type attribute to a html link <a href=...">.
   * @return {*}
   * @private
   */
  geonetwork.GnFeaturesINDEXLoader.prototype.formatUrlValues_ = function (url) {
    var $filter = this.$injector.get("$filter");

    url = this.fillUrlWithFilter_(url);
    var link = $filter("linky")(url, "_blank");
    if (link != url) {
      link = link.replace(/>(.)*</, " " + 'target="_blank">' + linkTpl + "<");
    }
    return link;
  };

  geonetwork.GnFeaturesINDEXLoader.prototype.loadAll = function () {
    var layer = this.layer,
      uuid;
    if (layer.get("md")) {
      uuid = layer.get("md").uuid;
    } else if (layer.get("metadataUuid")) {
      uuid = layer.get("metadataUuid");
    }

    if (uuid) {
      this.dictionary = this.featureService
        .loadFeatureCatalogue(uuid, layer.get("md"))
        .then(function (catalogue) {
          return catalogue;
        });
    } else {
      var $q = this.$injector.get("$q");
      this.dictionary = $q.when(undefined);
    }
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
  geonetwork.GnFeaturesINDEXLoader.prototype.fillUrlWithFilter_ = function (url) {
    var indexFilters = this.indexObject.getState();

    var URL_SUBSTITUTE_PREFIX = "filtre_";
    var regex = /\$\{(\w+)\}/g;
    var placeholders = [];
    var urlFilters = [];
    var paramsToAdd = {};
    var match;

    while ((match = regex.exec(url))) {
      placeholders.push(match[0]);
      urlFilters.push(match[1].substring(URL_SUBSTITUTE_PREFIX.length, match[1].length));
    }

    urlFilters.forEach(
      function (p, i) {
        var name = p;
        var idxName = this.indexObject.getIdxNameObj_(name).idxName;
        var fValue = indexFilters.qParams[idxName];
        url = url.replace(placeholders[i], "");

        if (fValue) {
          paramsToAdd[name] = Object.keys(fValue.values)[0];
        }
      }.bind(this)
    );

    return this.urlUtils.append(url, this.urlUtils.toKeyValue(paramsToAdd));
  };

  geonetwork.GnFeaturesINDEXLoader.prototype.getBsTableConfig = function () {
    var $q = this.$injector.get("$q");
    var defer = $q.defer();

    return this.dictionary.then(
      function (dictionary) {
        var pageList = [100, 500, 1000],
          columns = [],
          index = this.indexObject,
          map = this.map,
          fields = index.indexFields || index.filteredDocTypeFieldsInfo;

        fields.forEach(
          function (field) {
            if ($.inArray(field.idxName, this.excludeCols) === -1) {
              var fieldSpec = (dictionary && dictionary[field.name]) || {};
              columns.push({
                field: field.idxName,
                title: fieldSpec.name || field.label,
                titleTooltip: fieldSpec.definition
                  ? fieldSpec.definition + " (" + field.name + ")"
                  : field.label,
                sortable: true,
                formatter: function (val, row, index) {
                  var outputValue = val;
                  if (this.urlUtils.isValid(val)) {
                    outputValue = this.formatUrlValues_(val);
                  }
                  return outputValue;
                }.bind(this)
              });
            }
          }.bind(this)
        );

        // get an update index request url with geometry filter based on a point
        var url = this.indexObject.baseUrl;
        var state = angular.extend({}, this.indexObject.getState());
        state.params = state.qParams;
        var coordinates = this.coordinates;

        this.loading = true;
        defer.resolve({
          url: url,
          contentType: "application/json",
          method: "POST",
          queryParams: function (p) {
            var queryObject = this.indexObject.buildESParams(
              state,
              {},
              p.offset || 0,
              p.limit || 10000
            );
            if (p.sort) {
              queryObject.sort = [];
              var sort = {};
              sort[p.sort] = { order: p.order };
              queryObject.sort.push(sort);
            }
            if (coordinates) {
              var radius = map.getView().getResolution() / 0.4; // meters
              var minLonMaxLat = ol.proj.transform(
                [coordinates[0] - radius, coordinates[1] + radius],
                map.getView().getProjection(),
                "EPSG:4326"
              );
              var maxLonMinLat = ol.proj.transform(
                [coordinates[0] + radius, coordinates[1] - radius],
                map.getView().getProjection(),
                "EPSG:4326"
              );
              queryObject.query.bool.filter = {
                geo_shape: {
                  geom: {
                    shape: {
                      type: "envelope",
                      coordinates: [minLonMaxLat, maxLonMinLat]
                    },
                    relation: "intersects"
                  }
                }
              };
            }
            return JSON.stringify(queryObject);
          }.bind(this),
          responseHandler: function (res) {
            this.count = res.hits.total.value;
            var rows = [];
            for (var i = 0; i < res.hits.hits.length; i++) {
              rows.push(res.hits.hits[i]._source);
            }
            return {
              total: res.hits.total.value,
              rows: rows
            };
          }.bind(this),
          onSort: function () {
            this.loading = true;
          }.bind(this),
          onLoadSuccess: function () {
            this.loading = false;
            this.error = false;
          }.bind(this),
          onLoadError: function () {
            this.loading = false;
            this.error = true;
          }.bind(this),
          columns: columns,
          pagination: true,
          sidePagination: "server",
          totalRows: this.indexObject.totalCount,
          pageSize: pageList[1],
          pageList: pageList
        });
        return defer.promise;
      }.bind(this)
    );
  };

  geonetwork.GnFeaturesINDEXLoader.prototype.getCount = function () {
    return this.count;
  };

  geonetwork.GnFeaturesINDEXLoader.prototype.getFeatureFromRow = function (row) {
    var geom = row[this.indexObject.geomField.idxName];
    if (angular.isArray(geom)) {
      geom = geom[0];
    }
    geom = new ol.format.GeoJSON().readGeometry(geom, {
      dataProjection: "EPSG:4326",
      featureProjection: this.map.getView().getProjection()
    });
    return new ol.Feature({ geometry: geom });
  };

  /**
   * Features loader tailored for the ESRI ArcGis REST API
   * @constructor
   */
  geonetwork.GnFeaturesESRILoader = function (config, $injector) {
    geonetwork.GnFeaturesGFILoader.call(this, config, $injector);

    this.coordinates = config.coordinates;
  };

  geonetwork.inherits(geonetwork.GnFeaturesESRILoader, geonetwork.GnFeaturesGFILoader);

  geonetwork.GnFeaturesESRILoader.prototype.loadAll = function () {
    var layer = this.layer;
    var map = this.map;
    var coordinates = this.coordinates;

    var uuid;
    if (layer.get("md")) {
      uuid = layer.get("md").uuid;
    } else if (layer.get("metadataUuid")) {
      uuid = layer.get("metadataUuid");
    }

    this.loading = true;
    var mapExtent = map.getView().calculateExtent();
    var mapSize = map.getSize();
    var layerId = layer.getSource().getParams().LAYERS;

    var layerParam = "top"; // only the top most features will be returned
    if (!!layerId) {
      layerParam = "all:" + layerId; // look into the specified layer instead
    }

    // we use the identify operation on the image service, see:
    // https://developers.arcgis.com/rest/services-reference/identify-map-service-.htm
    var identifyUrl =
      layer.getSource().getUrl() +
      "/identify?geometryType=esriGeometryPoint&geometry=" +
      coordinates[0] +
      "," +
      coordinates[1] +
      "&tolerance=4&mapExtent=" +
      mapExtent.join(",") +
      "&imageDisplay=" +
      mapSize.join(",") +
      ",96" +
      "&f=json&layers=" +
      layerParam;

    var format = new ol.format.EsriJSON();

    this.promise = this.$http.get(identifyUrl).then(
      function (response) {
        this.loading = false;
        this.features = response.data.results.map(function (result) {
          return format.readFeature(result, {
            featureProjection: map.getView().getProjection()
          });
        });
        return this.features;
      }.bind(this)
    );
    this.dictionary = null;
    if (uuid) {
      this.dictionary = this.featureService
        .loadFeatureCatalogue(uuid, layer.get("md"))
        .then(function (catalogue) {
          return catalogue;
        });
    }
  };

  /**
   *
   * @constructor
   */
  var GnFeaturesTableLoaderService = function ($injector) {
    this.$injector = $injector;
  };
  GnFeaturesTableLoaderService.prototype.createLoader = function (type, config) {
    var constructor = geonetwork["GnFeatures" + type.toUpperCase() + "Loader"];
    if (!angular.isFunction(constructor)) {
      console.warn("Cannot find constructor for loader type : " + type);
    }
    return new constructor(config, this.$injector);
  };
  module.service("gnFeaturesTableLoader", ["$injector", GnFeaturesTableLoaderService]);
})();
