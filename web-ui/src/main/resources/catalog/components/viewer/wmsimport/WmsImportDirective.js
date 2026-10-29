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
  goog.provide("gn_wmsimport");

  var module = angular.module("gn_wmsimport", []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive("gnWmsImport", [
    "gnOwsCapabilities",
    "gnEsriUtils",
    "gnAlertService",
    "gnMap",
    "$translate",
    "$timeout",
    "gnESClient",
    "Metadata",
    "gnViewerSettings",
    "gnGlobalSettings",
    "gnSearchSettings",
    function (
      gnOwsCapabilities,
      gnEsriUtils,
      gnAlertService,
      gnMap,
      $translate,
      $timeout,
      gnESClient,
      Metadata,
      gnViewerSettings,
      gnGlobalSettings,
      gnSearchSettings
    ) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/viewer/wmsimport/" + "partials/wmsimport.html",
        scope: {
          map: "=gnWmsImportMap",
          url: "=?gnWmsImportUrl"
        },
        controller: [
          "$scope",
          function ($scope) {
            /**
             * Transform a capabilities layer into an ol.Layer
             * and add it to the map.
             *
             * @param {Object} getCapLayer
             * @param {string} name of the style to use
             * @return {*}
             */
            this.addLayer = function (getCapLayer, style) {
              getCapLayer.version = $scope.capability.version;
              getCapLayer.capRequest = $scope.capability.Request;

              //check if proxy is needed
              var url = $scope.url.split("/");
              getCapLayer.useProxy = false;
              url = url[0] + "/" + url[1] + "/" + url[2] + "/";
              if ($.inArray(url + "#GET", gnGlobalSettings.requireProxy) >= 0) {
                getCapLayer.useProxy = true;
              }
              if ($scope.format == "wms") {
                var layer = gnMap.addWmsToMapFromCap(
                  $scope.map,
                  getCapLayer,
                  $scope.url,
                  style
                );
                gnAlertService.addAlert(
                  {
                    msg: $translate.instant("layerAdded", {
                      layer: layer.get("label"),
                      extent: layer.get("cextent").toString()
                    }),
                    type: "success"
                  },
                  15
                );
                gnMap.feedLayerMd(layer);
                return layer;
              } else if ($scope.format == "wfs") {
                var layer = gnMap.addWfsToMapFromCap($scope.map, getCapLayer, $scope.url);
                gnMap.feedLayerMd(layer);
                return layer;
              } else if ($scope.format == "wmts") {
                return gnMap.addWmtsToMapFromCap(
                  $scope.map,
                  getCapLayer,
                  $scope.capability
                );
              } else {
                console.log($scope.format + " not supported");
              }
            };

            this.addEsriRestLayer = function (getCapLayer) {
              return gnMap.addEsriRestLayer(
                $scope.map,
                $scope.url + "/" + getCapLayer.id,
                null,
                null,
                null
              );
            };
          }
        ],
        link: function (scope, element, attrs) {
          scope.loading = false;
          scope.error = { wms: null, wmts: null, wfs: null, esrirest: null };
          scope.format = attrs["gnWmsImport"] != "" ? attrs["gnWmsImport"] : "all";
          scope.serviceDesc = null;
          scope.servicesList = gnViewerSettings.servicesUrl[scope.format];
          scope.catServicesList = [];
          var type = scope.format.toUpperCase();

          //Update require proxy
          //this is done because gnGlobalSettings is not configured at this point
          scope.$watch("gnGlobalSettings.requireProxy", function (settings) {
            scope.requireProxy = gnGlobalSettings.requireProxy;
          });

          function addLinks(md, type) {
            angular.forEach(md.getLinksByType(type), function (link) {
              if (link.url) {
                scope.catServicesList.push({
                  title: md.resourceTitle,
                  uuid: md.uuid,
                  name: link.name,
                  desc: link.desc,
                  type: type,
                  url: link.url
                });
              }
            });
          }
          // Get the list of services registered in the catalog
          if (attrs.servicesListFromCatalog) {
            // FIXME: Only load the first 100 services
            var query = {
              from: 0,
              size: 100,
              sort: [{ "resourceTitleObject.default.sort": "asc" }],
              query: {
                bool: {
                  must: [
                    {
                      query_string: {
                        query:
                          "+isTemplate:n " +
                          '+serviceType:("OGC:WMS" OR "OGC:WFS" OR "OGC:WMTS")'
                      }
                    }
                  ]
                }
              }
            };
            if (gnSearchSettings.filters) {
              query.query.bool.filter = gnSearchSettings.filters;
            }
            gnESClient.search(query).then(function (data) {
              angular.forEach(data.hits.hits, function (record) {
                var md = new Metadata(record);
                if (scope.format === "all") {
                  addLinks(md, "wms");
                  addLinks(md, "wfs");
                } else {
                  addLinks(md, scope.format);
                }
              });
            });
          }

          scope.setUrl = function (srv) {
            scope.url = angular.isObject(srv) ? srv.url : srv;
            type = (angular.isObject(srv) && srv.type) || type;
            scope.serviceDesc = angular.isObject(srv) ? srv : null;
            scope.load();
          };

          scope.load = function () {
            if (scope.url) {
              scope.loading = true;
              scope.error[type] = null;
              scope.capability = null;
              scope.capabilityEsriRest = null;
              if (type.toLowerCase() === "esrirest") {
                gnEsriUtils.getCapabilities(scope.url).then(
                  function (capability) {
                    scope.loading = false;
                    scope.capabilityEsriRest = capability;
                  },
                  function (error) {
                    scope.loading = false;
                    scope.error[type] = error;
                  }
                );
              } else {
                gnOwsCapabilities["get" + type.toUpperCase() + "Capabilities"](
                  scope.url
                ).then(
                  function (capability) {
                    scope.loading = false;
                    scope.capability = capability;
                  },
                  function (error) {
                    scope.loading = false;
                    scope.error[type] = error;
                  }
                );
              }
            }
          };

          // reset a service URL and clear the result list
          scope.reset = function () {
            scope.loading = false;
            scope.capability = null;
            scope.capabilityEsriRest = null;
            scope.serviceDesc = null;
            scope.url = "";
          };

          // watch url as input
          scope.$watch("url", function (value) {
            if (value) {
              scope.setUrl({
                url: value,
                type: scope.format
              });
            }
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc service
   * @name gn_wmsimport.service:gnAddImportedLayer
   *
   * @description
   * Add a layer loaded from a file to the map: show it in the layer manager,
   * zoom to it and confirm with an alert.
   */
  module.factory("gnAddImportedLayer", [
    "olDecorateLayer",
    "gnAlertService",
    "$translate",
    function (olDecorateLayer, gnAlertService, $translate) {
      /**
       * @param {ol.layer.Base} layer
       * @param {ol.map} map
       * @param {ol.Extent=} extent in map projection, defaults to the source extent
       */
      return function (layer, map, extent) {
        extent = extent || layer.getSource().getExtent();
        // An empty layer (eg. KMZ entry without features) has no extent to zoom to
        var hasExtent = !ol.extent.isEmpty(extent);
        olDecorateLayer(layer);
        layer.displayInLayerManager = true;
        map.getLayers().push(layer);
        if (hasExtent) {
          // Used by the layer manager "Zoom to layer extent"
          layer.set("cextent", extent);
          map.getView().fit(extent, map.getSize());
        }

        gnAlertService.addAlert({
          msg: $translate.instant(hasExtent ? "layerAdded" : "layerAddedNoExtent", {
            layer: layer.get("label"),
            extent: extent.toString()
          }),
          type: "success"
        });
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnKmlImport
   *
   * @description
   * Panel to load KML and KMZ files. You could load them with file input or
   * drag & drop them in the map.
   */

  module.directive("gnKmlImport", [
    "gnAlertService",
    "$translate",
    "gnAddImportedLayer",
    function (gnAlertService, $translate, gnAddImportedLayer) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/viewer/wmsimport/" + "partials/kmlimport.html",
        scope: {
          map: "=gnKmlImportMap"
        },
        controllerAs: "kmlCtrl",
        controller: [
          "$scope",
          "$http",
          "gnGlobalSettings",
          function ($scope, $http, gnGlobalSettings) {
            /**
             * Create new vector Kml file from url and add it to
             * the Map.
             *
             * @param {string} url remote url of the kml file
             * @param {ol.map} map
             */
            this.addKml = function (url, map) {
              $scope.kmlLoading = true;
              $scope.kmlError = null;
              $http
                .get(gnGlobalSettings.proxyUrl + encodeURIComponent(url))
                .then(function (response) {
                  var features = new ol.format.KML().readFeatures(response.data, {
                    featureProjection: $scope.map.getView().getProjection(),
                    dataProjection: "EPSG:4326"
                  });
                  // Not a KML (eg. an HTML error page), or a KML without
                  // features OpenLayers can show (eg. only network links)
                  if (features.length === 0) {
                    var isKml =
                      typeof response.data === "string" &&
                      /<kml[\s>]/i.test(response.data);
                    $scope.kmlError = $translate.instant(
                      isKml ? "kmlNoFeatures" : "kmlNotKml"
                    );
                    return;
                  }
                  var vector = new ol.layer.Vector({
                    source: new ol.source.Vector({ features: features }),
                    getinfo: true,
                    label: $translate.instant("kmlFile", {
                      layer: url.split("?")[0].split("/").pop()
                    })
                  });
                  $scope.addToMap(vector, map);
                  $scope.url = "";
                })
                .catch(function (error) {
                  // $http rejects with the proxy response and its HTTP status
                  $scope.kmlError = $translate.instant(
                    { 403: "fileAccessRefused", 404: "kmlNotFound" }[
                      error && error.status
                    ] || "kmlLoadError"
                  );
                })
                .finally(function () {
                  $scope.kmlLoading = false;
                });
            };

            $scope.addToMap = gnAddImportedLayer;
          }
        ],
        link: function (scope, element, attrs) {
          /** File drag & drop support */
          var dragAndDropInteraction = new ol.interaction.DragAndDrop({
            formatConstructors: [
              ol.format.GPX,
              ol.format.GeoJSON,
              ol.format.KML,
              ol.format.TopoJSON
            ]
          });

          var onError = function (msg) {
            gnAlertService.addAlert({
              msg: $translate.instant("mapImportFailure"),
              type: "danger"
            });
          };

          scope.map.getInteractions().push(dragAndDropInteraction);
          dragAndDropInteraction.on("addfeatures", function (event) {
            if (!event.features || event.features.length == 0) {
              onError();
              scope.$apply();
              return;
            }

            var vectorSource = new ol.source.Vector({
              features: event.features,
              projection: event.projection
            });

            var layer = new ol.layer.Vector({
              source: vectorSource,
              getinfo: true,
              label: $translate.instant("localLayerFile", { layer: event.file.name })
            });
            scope.addToMap(layer, scope.map);
            scope.$apply();
          });

          var requestFileSystem =
            window.webkitRequestFileSystem ||
            window.mozRequestFileSystem ||
            window.requestFileSystem;
          var unzipProgress = document.createElement("progress");
          var fileInput = element.find('input[type="file"]')[0];

          var model = (function () {
            var URL = window.webkitURL || window.mozURL || window.URL;

            return {
              getEntries: function (file, onend) {
                zip.createReader(
                  new zip.BlobReader(file),
                  function (zipReader) {
                    zipReader.getEntries(onend);
                  },
                  onerror
                );
              },
              getEntryFile: function (entry, creationMethod, onend, onprogress) {
                var writer, zipFileEntry;

                function getData() {
                  entry.getData(
                    writer,
                    function (blob) {
                      var blobURL = URL.createObjectURL(blob);
                      onend(blobURL);
                    },
                    onprogress
                  );
                }
                writer = new zip.BlobWriter();
                getData();
              }
            };
          })();

          scope.onEntryClick = function (entry, evt) {
            model.getEntryFile(
              entry,
              "Blob",
              function (blobURL) {
                entry.loading = true;
                scope.$apply();
                var source = new ol.source.Vector();
                $.ajax(blobURL).then(function (response) {
                  var format = new ol.format.KML();
                  var features = format.readFeatures(response, {
                    featureProjection: scope.map.getView().getProjection()
                  });
                  source.addFeatures(features);
                });

                var vector = new ol.layer.Vector({
                  label: $translate.instant("localLayerFile", { layer: entry.filename }),
                  getinfo: true,
                  source: source
                });
                var listenerKey = vector.getSource().on("change", function (evt) {
                  if (vector.getSource().getState() == "ready") {
                    ol.Observable.unByKey(listenerKey);
                    scope.addToMap(vector, scope.map);
                    entry.loading = false;
                  } else if (vector.getSource().getState() == "error") {
                  }
                  scope.$apply();
                });
              },
              function (current, total) {
                unzipProgress.value = current;
                unzipProgress.max = total;
                evt.target.appendChild(unzipProgress);
              }
            );
          };

          angular.element(fileInput).bind("change", function (changeEvent) {
            if (fileInput.files.length > 0) {
              model.getEntries(fileInput.files[0], function (entries) {
                scope.kmzEntries = entries;
                scope.$apply();
              });
            }
            $("#kmz-file-input")[0].value = "";
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnCogImport
   *
   * @description
   * Panel to load a Cloud Optimized GeoTIFF (COG) file from a URL.
   */
  module.directive("gnCogImport", [
    "$http",
    "$q",
    "$translate",
    "gnGlobalSettings",
    "gnAddImportedLayer",
    function ($http, $q, $translate, gnGlobalSettings, gnAddImportedLayer) {
      // ol.source.GeoTIFF reads files with the global GeoTIFF from geotiff.js.
      // Only this panel needs it, so load it on first use instead of with
      // every page. The page layout sets gnGlobalSettings.geotiffUrl: the
      // "geotiff" wro4j bundle, or the file itself in debug mode.
      var geoTiffLoaded = null;
      var loadGeoTiff = function () {
        if (window.GeoTIFF) {
          return $q.resolve();
        }
        geoTiffLoaded =
          geoTiffLoaded ||
          $q(function (resolve, reject) {
            var script = document.createElement("script");
            var done = function (loaded) {
              clearTimeout(timeout);
              script.onload = script.onerror = null;
              if (loaded) {
                resolve();
              } else {
                script.remove();
                geoTiffLoaded = null;
                reject({ key: "cogLoadError" });
              }
            };
            // A download that hangs ends as an error instead of a spinner
            var timeout = setTimeout(function () {
              done(false);
            }, 30000);
            script.onload = function () {
              done(true);
            };
            script.onerror = function () {
              done(false);
            };
            script.src =
              gnGlobalSettings.geotiffUrl || "../../catalog/lib/geotiff/geotiff.js";
            document.head.appendChild(script);
          });
        return geoTiffLoaded;
      };

      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/viewer/wmsimport/" + "partials/cogimport.html",
        scope: {
          map: "=gnCogImportMap"
        },
        controllerAs: "cogCtrl",
        controller: [
          "$scope",
          function ($scope) {
            /**
             * OpenLayers normalizes a band without statistics against its
             * data type range (eg. 0-65535), which draws 16-bit or float files
             * black. Compute the min/max of single band files from the
             * smallest overview (not a mask), or from the image itself when
             * it is small.
             *
             * @param {string} url
             * @param {function} loader the source loader (see readCog)
             * @return {Promise} resolved with {min, max}, or null when the file
             *  has several bands, 8-bit values, statistics or no small overview.
             */
            var readMinMax = function (url, loader) {
              var tiff, image;
              // Same client as ol.source.GeoTIFF builds for a custom loader
              var client = {
                url: url,
                request: function (options) {
                  return loader(
                    url,
                    options && options.headers,
                    options && options.signal
                  ).then(function (response) {
                    return angular.extend(response, {
                      getHeader: function (name) {
                        return response.headers.get(name);
                      },
                      getData: function () {
                        return response.arrayBuffer();
                      }
                    });
                  });
                }
              };
              return $q
                .when(GeoTIFF.fromCustomClient(client))
                .then(function (t) {
                  tiff = t;
                  return tiff.getImage(0);
                })
                .then(function (i) {
                  image = i;
                  return $q.all([image.getGDALMetadata(0), tiff.getImageCount()]);
                })
                .then(function (result) {
                  var metadata = result[0] || {};
                  // 8-bit files are drawn as they are, like in other viewers
                  // (eg. greyscale, hillshade).
                  if (
                    image.getSamplesPerPixel() !== 1 ||
                    image.getBitsPerSample() <= 8 ||
                    metadata.STATISTICS_MAXIMUM !== undefined
                  ) {
                    return null;
                  }
                  var others = [];
                  for (var n = 1; n < result[1]; n++) {
                    others.push(tiff.getImage(n));
                  }
                  return $q.all(others).then(function (images) {
                    // The smallest overview: a reduced resolution image that is
                    // not a mask (NewSubfileType 1, masks have bit 4).
                    var smallest = images
                      .filter(function (overview) {
                        var type = overview.fileDirectory.getValue("NewSubfileType") || 0;
                        return type & 1 && !(type & 4);
                      })
                      .reduce(function (a, b) {
                        return b.getWidth() < a.getWidth() ? b : a;
                      }, image);
                    // ponytail: a large file without (small) overviews would be
                    // read in full, skip it.
                    if (smallest.getWidth() * smallest.getHeight() > 4e6) {
                      return null;
                    }
                    return $q.when(smallest.readRasters({ samples: [0] }));
                  });
                })
                .then(function (rasters) {
                  if (!rasters) {
                    return null;
                  }
                  var noData = image.getGDALNoData();
                  var data = rasters[0];
                  // Float32 values are rounded: compare with the noData value
                  // rounded the same way (eg. -9999.9).
                  if (noData !== null && data instanceof Float32Array) {
                    noData = Math.fround(noData);
                  }
                  var min = Infinity;
                  var max = -Infinity;
                  for (var i = 0; i < data.length; i++) {
                    var value = data[i];
                    if (value !== noData && !isNaN(value)) {
                      min = Math.min(min, value);
                      max = Math.max(max, value);
                    }
                  }
                  return min < max ? { min: min, max: max } : null;
                });
            };

            /**
             * Read the header of a Cloud Optimized GeoTIFF.
             *
             * @param {string} url
             * @param {ol.map} map
             * @return {Promise} resolved with the source, its extent in the map
             *  projection and release(), to call after the first draw to free
             *  the downloaded responses. Rejected with an object whose `key` is the error
             *  message key when the file is not usable, or null for fetch errors;
             *  `answered` is true when the server replied with an HTTP error.
             */
            var readCog = function (url, map) {
              // The header is read by the source, by readMinMax and by the source
              // created with min/max, and the overview read by readMinMax can be
              // drawn first. Share the responses read while the file is opened,
              // so each range is downloaded once. Once opened, cached responses
              // are served once more, then dropped; release() drops the rest
              // after the first draw (OpenLayers keeps the tiles it draws).
              var cache = {};
              var opened = false;
              // Mark requests that failed without an answer (eg. no CORS headers):
              // only those can work through the proxy.
              var request = function (url, init) {
                return fetch(url, init).catch(function (error) {
                  error.requestFailed = true;
                  throw error;
                });
              };
              // A cached range, or a response that was not cached (see below)
              var toResponse = function (cached) {
                return cached instanceof Response
                  ? cached
                  : new Response(cached.data.slice(0), cached.init);
              };
              var loader = function (url, headers, signal) {
                var key = JSON.stringify(headers);
                if (opened) {
                  var cached = cache[key];
                  delete cache[key];
                  return cached
                    ? cached.then(toResponse)
                    : request(url, { headers: headers, signal: signal });
                }
                cache[key] =
                  cache[key] ||
                  request(url, { headers: headers }).then(function (response) {
                    // Only read partial content. A server that ignores Range
                    // sends the whole file, and geotiff.js rejects any other
                    // answer from its status alone: stop the download.
                    if (response.status !== 206) {
                      delete cache[key];
                      if (response.body) {
                        response.body.cancel();
                      }
                      return response;
                    }
                    return response.arrayBuffer().then(function (data) {
                      return {
                        data: data,
                        init: {
                          status: response.status,
                          statusText: response.statusText,
                          headers: response.headers
                        }
                      };
                    });
                  });
                return cache[key].then(toResponse);
              };
              var createSource = function (minMax) {
                return new ol.source.GeoTIFF({
                  sources: [angular.extend({ url: url, loader: loader }, minMax)],
                  convertToRGB: "auto"
                });
              };
              // getView() never rejects when the file cannot be fetched: the source state does.
              var getView = function (source) {
                return $q(function (resolve, reject) {
                  source.getView().then(resolve, reject);
                  source.once("change", function () {
                    if (source.getState() === "error") {
                      reject(source.getError());
                    }
                  });
                });
              };
              var source = createSource();
              return getView(source).then(
                function (view) {
                  var mapProjection = map.getView().getProjection();
                  if (!ol.proj.getTransform(view.projection, mapProjection)) {
                    source.dispose();
                    return $q.reject({
                      key: "cogUnknownProjection",
                      code: view.projection.getCode()
                    });
                  }
                  var extent = ol.proj.transformExtent(
                    view.extent,
                    view.projection,
                    mapProjection
                  );
                  return readMinMax(url, loader)
                    .then(function (minMax) {
                      if (!minMax) {
                        return source;
                      }
                      var stretched = createSource(minMax);
                      return getView(stretched).then(
                        function () {
                          source.dispose();
                          return stretched;
                        },
                        function () {
                          stretched.dispose();
                          return source;
                        }
                      );
                    })
                    .catch(function () {
                      return source;
                    })
                    .then(function (cogSource) {
                      opened = true;
                      return {
                        source: cogSource,
                        extent: extent,
                        release: function () {
                          cache = {};
                        }
                      };
                    });
                },
                function (error) {
                  source.dispose();
                  var message = (error && error.message) || "";
                  return $q.reject({
                    key: /byte order|magic number/i.test(message)
                      ? "cogNotTiff"
                      : /full file/i.test(message)
                      ? "cogNoRangeRequests"
                      : null,
                    answered: /error fetching data/i.test(message),
                    requestFailed: !!(error && error.requestFailed)
                  });
                }
              );
            };

            /**
             * Fetch errors carry no HTTP status: ask for one byte to know why.
             *
             * @param {string} url
             * @param {Object} keyByStatus error message key by HTTP status
             * @return {Promise} always rejected with the error message key
             */
            var rejectWithStatus = function (url, keyByStatus) {
              return $http.get(url, { headers: { Range: "bytes=0-0" } }).then(
                function () {
                  return $q.reject({ key: "cogLoadError" });
                },
                function (response) {
                  return $q.reject({
                    key: keyByStatus[response.status] || "cogLoadError"
                  });
                }
              );
            };

            /**
             * Create a raster layer from a Cloud Optimized GeoTIFF url
             * and add it to the Map. The file is read with HTTP range
             * requests: directly if the server allows CORS, otherwise
             * through the proxy.
             *
             * @param {string} url remote url of the COG file
             * @param {ol.map} map
             */
            this.addCog = function (url, map) {
              var proxyUrl = gnGlobalSettings.proxyUrl + encodeURIComponent(url);
              $scope.cogLoading = true;
              $scope.cogError = null;
              loadGeoTiff()
                .then(function () {
                  return readCog(url, map);
                })
                .catch(function (error) {
                  // A file that is not usable, or a server that answered with an
                  // HTTP error, stays so through the proxy: only retry when the
                  // request itself failed (eg. no CORS headers).
                  if (error.key) {
                    return $q.reject(error);
                  }
                  if (error.answered) {
                    return rejectWithStatus(url, { 404: "cogNotFound" });
                  }
                  return error.requestFailed
                    ? readCog(proxyUrl, map)
                    : $q.reject({ key: "cogLoadError" });
                })
                .catch(function (error) {
                  return error.key
                    ? $q.reject(error)
                    : rejectWithStatus(proxyUrl, {
                        403: "fileAccessRefused",
                        404: "cogNotFound"
                      });
                })
                .then(
                  function (cog) {
                    var layer = new ol.layer.WebGLTile({
                      source: cog.source,
                      // Tiles are padded with 0 past the image edges, which
                      // draws black when the file has no nodata value.
                      extent: cog.extent,
                      label: $translate.instant("cogFile", {
                        layer: url.split("?")[0].split("/").pop()
                      })
                    });
                    gnAddImportedLayer(layer, map, cog.extent);
                    map.once("rendercomplete", cog.release);
                    $scope.cogUrl = "";
                  },
                  function (error) {
                    $scope.cogError = $translate.instant(error.key, error);
                  }
                )
                .finally(function () {
                  $scope.cogLoading = false;
                });
            };
          }
        ]
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnCapTreeCol
   *
   * @description
   * Directive to manage a collection of nested layers from
   * the capabilities document. This directive works with
   * gnCapTreeElt directive.
   */
  module.directive("gnCapTreeCol", [
    "$translate",
    function ($translate) {
      var label = $translate.instant("filter");

      return {
        restrict: "E",
        replace: true,
        scope: {
          collection: "="
        },
        template:
          "<ul class='gn-layer-tree'><li data-ng-show='collection.length > 10' >" +
          "<div class='input-group input-group-sm'><span class='input-group-addon'><i class='fa fa-filter'></i></span>" +
          "<input class='form-control' aria-label='" +
          label +
          "' data-ng-model-options='{debounce: 200}' data-ng-model='layerSearchText'/></div>" +
          "</li>" +
          '<gn-cap-tree-elt ng-repeat="member in collection | filter:layerSearchText | orderBy: \'Title\'" member="member">' +
          "</gn-cap-tree-elt></ul>"
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnCapTreeElt
   *
   * @description
   * Directive to manage recursively nested layers from a capabilities
   * document. Will call its own template to display the layer but also
   * call back the gnCapTreeCol for all its children.
   */
  module.directive("gnCapTreeElt", [
    "$compile",
    "$translate",
    "gnAlertService",
    function ($compile, $translate, gnAlertService) {
      return {
        restrict: "E",
        require: "^gnWmsImport",
        replace: true,
        scope: {
          member: "="
        },
        templateUrl: "../../catalog/components/viewer/wmsimport/" + "partials/layer.html",
        link: function (scope, element, attrs, controller) {
          var el = element;

          scope.toggleNode = function (evt) {
            el.find(".fa")
              .first()
              .toggleClass("fa-folder-open-o")
              .toggleClass("fa-folder-o");
            el.children("ul").toggle();
            evt.stopPropagation();
          };

          scope.addLayer = function (c) {
            controller.addLayer(scope.member, c ? c : null);
          };

          scope.isParentNode = angular.isDefined(scope.member.Layer);

          // Add all subchildren
          if (angular.isArray(scope.member.Layer)) {
            element.append(
              "<gn-cap-tree-col " + "collection='member.Layer'></gn-cap-tree-col>"
            );
            $compile(element.find("gn-cap-tree-col"))(scope);
          }
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnEsriRestCapTreeCol
   *
   * @description
   * Directive to manage a collection of nested layers from
   * the ESRI REST capabilities document. This directive works with
   * gnEsriCapTreeElt directive.
   */
  module.directive("gnEsriRestCapTreeCol", [
    function () {
      return {
        restrict: "E",
        replace: true,
        scope: {
          collection: "=",
          filterLabel: "@"
        },
        template:
          "<ul class='gn-layer-tree'><li data-ng-show='collection.length > 10' >" +
          "<div class='input-group input-group-sm'><span class='input-group-addon'><i class='fa fa-filter'></i></span>" +
          "<input class='form-control' aria-label='{{ ::filterLabel }}' data-ng-model-options='{debounce: 200}' data-ng-model='layerSearchText'/></div>" +
          "</li>" +
          '<gn-esri-rest-cap-tree-elt ng-repeat="member in collection | filter:layerSearchText | orderBy: \'name\'" member="member">' +
          "</gn-esri-rest-cap-tree-elt></ul>"
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnEsriCapTreeElt
   *
   * @description
   * Directive to manage layers from an ESRI REST capabilities
   * document. Will call its own template to display the layer.
   */
  module.directive("gnEsriRestCapTreeElt", [
    "$compile",
    "$translate",
    "gnAlertService",
    function ($compile, $translate, gnAlertService) {
      return {
        restrict: "E",
        require: "^gnWmsImport",
        replace: true,
        scope: {
          member: "="
        },
        templateUrl:
          "../../catalog/components/viewer/wmsimport/" + "partials/esrilayer.html",
        link: function (scope, element, attrs, controller) {
          var el = element;

          scope.toggleNode = function (evt) {
            el.find(".fa")
              .first()
              .toggleClass("fa-folder-open-o")
              .toggleClass("fa-folder-o");
            el.children("ul").toggle();
            evt.stopPropagation();
          };

          scope.addLayer = function () {
            controller.addEsriRestLayer(scope.member);
          };
        }
      };
    }
  ]);

  module.directive("gnLayerStyles", [
    function () {
      return {
        restrict: "A",
        templateUrl: "../../catalog/components/viewer/wmsimport/partials/styles.html",
        scope: {
          styles: "=gnLayerStyles",
          onClick: "&gnLayerStylesOnClick",
          current: "=gnLayerStylesCurrent",
          // 'select' or default is list
          layout: "@gnLayerStylesLayout",
          // dropdown open to the left or right
          dropdownDirection: "@gnLayerStylesDropdownDirection"
        },
        link: function (scope) {
          scope.data = { currentStyle: scope.current };
          scope.dropdownDirection = scope.dropdownDirection || "left";
          scope.$watch("data.currentStyle", function (n, o) {
            if (n && n !== o) {
              scope.clickFn(n);
            }
          });
          scope.clickFn = function (s) {
            scope.onClick({ style: s });
          };
        }
      };
    }
  ]);
})();
