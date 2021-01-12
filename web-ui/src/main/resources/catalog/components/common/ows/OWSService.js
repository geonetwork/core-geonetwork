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
  goog.provide('gn_ows_service');

  goog.require('Filter_1_0_0');
  goog.require('Filter_1_1_0');
  goog.require('Filter_2_0');
  goog.require('GML_2_1_2');
  goog.require('GML_3_1_1');
  goog.require('OWS_1_0_0');
  goog.require('OWS_1_1_0');
  goog.require('SMIL_2_0');
  goog.require('SMIL_2_0_Language');
  goog.require('WFS_1_0_0');
  goog.require('WFS_1_1_0');
  goog.require('WFS_2_0');
  goog.require('WCS_1_1');
  goog.require('XLink_1_0');

  var module = angular.module('gn_ows_service', [
  ]);

  // WFS Client
  // Jsonix wrapper to read or write WFS response or request
  var context100 = new Jsonix.Context(
      [XLink_1_0, OWS_1_0_0, Filter_1_0_0,
       GML_2_1_2, SMIL_2_0, SMIL_2_0_Language,
       WFS_1_0_0],
      {
        namespacePrefixes: {
          'http://www.opengis.net/wfs': 'wfs'
        }
      }
      );
  var context110 = new Jsonix.Context(
      [XLink_1_0, OWS_1_1_0, OWS_1_0_0,
       Filter_1_1_0,
       GML_3_1_1,
       SMIL_2_0, SMIL_2_0_Language,
       WFS_1_1_0, WCS_1_1],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows/1.1': 'ows',
          'http://www.opengis.net/wfs': 'wfs',
          'http://www.opengis.net/wcs': 'wcs'
        }
      }
      );
  var context20 = new Jsonix.Context(
      [XLink_1_0, OWS_1_1_0,
       SMIL_2_0, SMIL_2_0_Language,
       Filter_2_0, GML_3_1_1, WFS_2_0],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows/1.1': 'ows',
          'http://www.opengis.net/wfs/2.0': 'wfs',
          'http://www.opengis.net/fes/2.0':'fes'
        }
      }
      );
  var unmarshaller100 = context100.createUnmarshaller();
  var unmarshaller110 = context110.createUnmarshaller();
  var unmarshaller20 = context20.createUnmarshaller();
  var cachedGetCapabilitiesUrls = {};
  // var timeout = -1;
  var timeout = 60 * 1000;

  module.provider('gnOwsCapabilities', function() {
    this.$get = ['$http', '$q', '$translate',
      'gnUrlUtils', 'gnGlobalSettings',
      function($http, $q, $translate,
               gnUrlUtils, gnGlobalSettings) {

        var getLayerBounds = function(layer) {
          var bboxProp;
          ['EX_GeographicBoundingBox', 'WGS84BoundingBox'].forEach(
            function(prop) {
              if (angular.isArray(layer[prop])) {
                bboxProp = layer[prop];
              }
            });
          return bboxProp;
        };

        var getWMSCapabilities = function(data) {
          var ol_parser = new ol.format.WMSCapabilities();
          var result = ol_parser.read(data);
          var ol_layer = result.Capability.Layer;

          // If no CRS property is available, we're probably dealing with an older WMS (e.g. 1.1.1)
          // WMS versions prior to 1.3.0 are not (fully) supported by OL's WMSCapabilities parser
          if (!result.Capability.Layer.CRS) {
            var parser = new X2JS();
            var jsonData = parser.xml_str2json(data);

            for (var p in jsonData) {
              // Get root element
              if (jsonData.hasOwnProperty(p)) {
                var layer = jsonData[p].Capability.Layer;

                // Copy SRS to Layer.CRS
                ol_layer.CRS = layer.SRS || layer._SRS;

                // Copy _SRS to BoundingBox.crs for each BoundingBox (if equal array length)
                if (layer.BoundingBox && layer.BoundingBox.length > 0 &&
                  layer.BoundingBox.length === ol_layer.BoundingBox.length) {

                  for (var i = 0; i < layer.BoundingBox.length; i++) {
                    var bbox = layer.BoundingBox[i];
                    ol_layer.BoundingBox[i].crs = bbox._SRS || bbox.SRS;
                  }
                }

                // Create EX_GeographicBoundingBox extent from LatLonBoundingBox properties
                if (layer.LatLonBoundingBox) {
                  var minx = layer.LatLonBoundingBox._minx;
                  var miny = layer.LatLonBoundingBox._miny;
                  var maxx = layer.LatLonBoundingBox._maxx;
                  var maxy = layer.LatLonBoundingBox._maxy;
                  if (angular.isDefined(minx) && angular.isDefined(miny) &&
                      angular.isDefined(maxx) && angular.isDefined(maxy)) {
                    ol_layer.EX_GeographicBoundingBox = ol.extent.boundingExtent([
                      [parseFloat(minx), parseFloat(miny)],
                      [parseFloat(maxx), parseFloat(maxy)]
                    ]);
                  }
                }
                break;
              }
            }
          }

          return result;
        };

        var parseWMSCapabilities = function(data, withGroupLayer, getCapabilitiesUrl) {

          var resolvedUrl = gnUrlUtils.urlResolve(getCapabilitiesUrl);
          resolvedUrl.port = '';
          getCapabilitiesUrl = resolvedUrl.href;

          if (!cachedGetCapabilitiesUrls.hasOwnProperty(getCapabilitiesUrl)) {
            cachedGetCapabilitiesUrls[getCapabilitiesUrl] = getWMSCapabilities(data);
          }

          var result = angular.copy(cachedGetCapabilitiesUrls[getCapabilitiesUrl], {});
          var layers = [];
          var url = result.Capability.Request.GetMap.
              DCPType[0].HTTP.Get.OnlineResource;
          var bbox = getLayerBounds(result.Capability.Layer);

          // Push all leaves into a flat array of Layers
          // Also adjust CRS (by inheritance) and set URL and geographic bounds
          var getFlatLayers = function(layer, inheritedCrs) {
            if (angular.isArray(layer)) {
              if (withGroupLayer && layer.Name) {
                layers.push(layer);
              }
              for (var i = 0, len = layer.length; i < len; i++) {
                getFlatLayers(layer[i], inheritedCrs);
              }
            } else if (angular.isDefined(layer)) {

              // replace with complete CRS list if available
              if (layer.CRS && layer.CRS.length > inheritedCrs.length) {
                inheritedCrs = layer.CRS;
              }

              // add to flat layer array if we're on a leaf (layer w/o child)
              layer.url = url;
              layer.CRS = inheritedCrs;
              layer.EX_GeographicBoundingBox = layer.EX_GeographicBoundingBox || bbox;
              layers.push(layer);

              // make sure Layer element is an array
              if (layer.Layer && !angular.isArray(layer.Layer)) {
                layer.Layer = [layer.Layer];
              }
              // process recursively on child layers
              getFlatLayers(layer.Layer, inheritedCrs);
            }
          };

          getFlatLayers(result.Capability.Layer, []);
          if (!angular.isArray(result.Capability.Layer)) {
            result.Capability.Layer = [result.Capability.Layer];
          }
          result.Capability.layers = layers;
          result.Capability.version = result.version;
          return result.Capability;
        };

        var parseWMTSCapabilities = function(data) {
          var parser = new ol.format.WMTSCapabilities();
          var result = parser.read(data);

          //result.contents.Layer = result.contents.layers;
          result.Contents.operationsMetadata = result.OperationsMetadata;
          return result.Contents;
        };

        var parseWCSCapabilities = function(data) {
          var version = '1.1.1';

          try {
            var xml = $.parseXML(data);
            var xfsCap = unmarshaller110.unmarshalDocument(xml).value;
            return xfsCap;
          } catch (e){
            console.warn(e);
          }
        };

        var parseWFSCapabilities = function(data) {
          var version = '1.1.0';

          try {

            var xml = $.parseXML(data);
            var version = $(xml).find(":first-child").attr("version");

            //First cleanup not supported INSPIRE extensions:
            if (xml.getElementsByTagName('ExtendedCapabilities').length > 0) {
              var cleanup = function(i, el) {
                if (el.tagName.endsWith('ExtendedCapabilities')) {
                  el.parentNode.removeChild(el);
                } else {
                  $.each(el.children, cleanup);
                }
              };

              $.each(xml.childNodes[0].children, cleanup);
            }

            //Now process the capabilities
            var xfsCap;

            if (version === '1.1.0') {
              xfsCap = unmarshaller110.unmarshalDocument(xml).value;
            } else if (version === '1.0.0') {
              xfsCap = unmarshaller100.unmarshalDocument(xml).value;
            } else if (version === '2.0.0') {
              xfsCap = unmarshaller20.unmarshalDocument(xml).value;
            } else {
              console.warn('WFS version '+version+' not supported.');
            }

            if (xfsCap.exception != undefined) {
              console.log(xfsCap.exception);
              return xfsCap;
            } else {
              return xfsCap;
            }
          } catch (e) {
            console.warn(e.message);
            return e.message;
          }
        };

        var mergeParams = function(url, Params, excludedParams) {
          //merge URL parameters with indicated ones
          var parts = url.split('?');
          var combinedParams = {};
          var urlParams = angular.isDefined(parts[1]) ?
              gnUrlUtils.parseKeyValue(parts[1]) : {};

          for (var p in urlParams) {
            if (!angular.isArray(excludedParams) ||
              (excludedParams.findIndex &&
               excludedParams.findIndex(function(item) {
                  return p.toLowerCase() === item.toLowerCase();}) === -1)) {
              combinedParams[p] = urlParams[p];
            }
          }
          for (var p in Params) {
            combinedParams[p] = Params[p];
          }

          return gnUrlUtils.append(parts[0], gnUrlUtils.toKeyValue(combinedParams));
        };

        var mergeDefaultParams = function(url, defaultParams) {
          //merge URL parameters with default ones
          var parts = url.split('?');
          var urlParams = angular.isDefined(parts[1]) ?
              gnUrlUtils.parseKeyValue(parts[1]) : {};

          for (var p in urlParams) {
            defaultParams[p] = urlParams[p];
            if (defaultParams.hasOwnProperty(p.toLowerCase()) &&
              p != p.toLowerCase()) {
              delete defaultParams[p.toLowerCase()];
            }
          }

          return gnUrlUtils.append(parts[0],
              gnUrlUtils.toKeyValue(defaultParams));
        };

        return {
          mergeDefaultParams: mergeDefaultParams,
          mergeParams: mergeParams,

          getWMSCapabilities: function(url, withGroupLayer) {
            var defer = $q.defer();
            if (url) {
              url = mergeDefaultParams(url, {
                service: 'WMS',
                request: 'GetCapabilities'
              });

              //send request and decode result
              if (true) {
                $http.get(url, {
                  cache: true,
                  timeout: timeout
                })
                    .success(function(data) {
                      try {
                        defer.resolve(parseWMSCapabilities(data, withGroupLayer, url));
                      } catch (e) {
                        defer.reject(
                        $translate.instant('failedToParseCapabilities'));
                      }
                    })
                    .error(function(data, status) {
                      defer.reject(
                      $translate.instant(
                        status === 401 ? 'checkCapabilityUrlUnauthorized' : 'checkCapabilityUrl',
                      {url: url, status: status}));
                    });
              }
            }
            else {
              defer.reject();
            }
            return defer.promise;
          },

          getWMTSCapabilities: function(url) {
            var defer = $q.defer();
            if (url) {
              url = mergeDefaultParams(url, {
                request: 'GetCapabilities',
                service: 'WMTS'
              });

              if (gnUrlUtils.isValid(url)) {

                $http.get(url, {
                  cache: true,
                  timeout: timeout
                })
                    .success(function(data, status, headers, config) {
                      if (data) {
                        defer.resolve(parseWMTSCapabilities(data));
                      }
                      else {
                        defer.reject();
                      }
                    })
                    .error(function(data, status, headers, config) {
                      defer.reject(status);
                    });
              }
            }
            return defer.promise;
          },

          getWFSCapabilities: function(url, version) {
            var defer = $q.defer();
            if (url) {
              defaultVersion = '1.1.0';
              version = version || defaultVersion;
              url = mergeDefaultParams(url, {
                request: 'GetCapabilities',
                service: 'WFS',
                version: version
              });

              if (gnUrlUtils.isValid(url)) {
                $http.get(url, {
                  cache: true,
                  timeout: timeout
                })
                    .success(function(data, status, headers, config) {
                      var xfsCap = parseWFSCapabilities(data);

                      if (!xfsCap || xfsCap.exception != undefined) {
                        defer.reject({msg: $translate.instant('wfsGetCapabilitiesFailed'),
                          owsExceptionReport: xfsCap});
                      } else {
                        defer.resolve(xfsCap);
                      }

                    })
                    .error(function(data, status, headers, config) {
                      defer.reject($translate.instant('wfsGetCapabilitiesFailed'));
                    });
              }
            }
            return defer.promise;
          },

          getWCSCapabilities: function(url, version) {
            var defer = $q.defer();
            if (url) {
              defaultVersion = '1.1.0';
              version = version || defaultVersion;
              url = mergeDefaultParams(url, {
                REQUEST: 'GetCapabilities',
                service: 'WCS',
                version: version
              });

              if (gnUrlUtils.isValid(url)) {
                $http.get(url, {
                  cache: true
                })
                    .success(function(data, status, headers, config) {
                      var xfsCap = parseWCSCapabilities(data);

                      if (!xfsCap || xfsCap.exception != undefined) {
                        defer.reject({msg: 'wcsGetCapabilitiesFailed',
                          owsExceptionReport: xfsCap});
                      } else {
                        defer.resolve(xfsCap);
                      }

                    })
                    .error(function(data, status, headers, config) {
                      defer.reject(status);
                    });
              }
            }
            return defer.promise;
          },

          getWmsLayerExtentFromGetCap: function(mapProj, getCapLayer, patchedProj) {
            var extent = null;
            var layer = getCapLayer;
            var projCode = mapProj.getCode();
            var wmsProj = projCode;

            // Try and fetch extent from layer for current CRS first
            if (angular.isArray(layer.BoundingBox)) {
              for (var i = 0; i < layer.BoundingBox.length; i++) {
                var bbox = layer.BoundingBox[i];

                if (!bbox.extent) {
                  continue;
                }

                if (bbox.crs === projCode || (projCode === 'EPSG:3857' && 
                   (bbox.crs === 'EPSG:3857' || bbox.crs === 'EPSG:900913' ||
                    bbox.crs === 'EPSG:3785' || bbox.crs === 'EPSG:102113'))) {
                  // Get the bbox matching the map projection
                  // Also match to Web Mercator alternatives
                  extent = bbox.extent;
                  wmsProj = bbox.crs;
                } 
              }
            }

            if (extent) {
              if (layer.version && layer.version >= '1.3.0' && projCode !== 'EPSG:3857') {
                // Reverse extent coordinates for non-Web Mercator projections and new WMS versions
                extent = extent.reverse();
              }
              // Reproject to (patched) map extent
              extent = ol.proj.transformExtent(extent, patchedProj, wmsProj, 8);
              if (projCode !== 'EPSG:3857') {
                // Output (patched) map extent for WMS on-the-fly reprojection
                wmsProj = patchedProj;
              }
            } 

            return {
              projection: wmsProj,
              extent: extent
            };
          },

          getLayerInfoFromCap: function(layerName, capObj, uuid) {
            var needles = [];
            var layers = capObj.layers || capObj.Layer;

            // Layer name may be a list of comma separated layers
            layerList = layerName.split(',');

            for (var j = 0; j < layerList.length; j ++) {
              var name = layerList[j];
              //non namespaced lowercase name
              nameNoNamespace = name.split(':')[
                  name.split(':').length - 1].toLowerCase();

              capabilityLayers:
              for (var i = 0; i < layers.length; i++) {
                //Add Info for Requests:
                if (capObj.Request) {
                  layers[i].capRequest = capObj.Request;
                }

                //check layername
                var lId = layers[i].Identifier;
                var capName = layers[i].Name ||
                    (lId && angular.isArray(lId) ? lId[0] : lId) || '',
                    capNameNoNamespace;
                //non namespaced lowercase capabilities name
                if (capName) {
                  capNameNoNamespace = capName.split(':')[
                      capName.split(':').length - 1].toLowerCase();
                }

                //either names match or non namespaced names
                // note: these matches are put at the beginning of the needles array
                if (name == capName || nameNoNamespace == capNameNoNamespace) {
                  layers[i].nameToUse = capName;
                  if (capObj.version) {
                    layers[i].version = capObj.version;
                  }
                  needles.unshift(layers[i]);
                  break capabilityLayers;
                }

                //check dataset identifer match
                // note: these matches are put at the end of the needles array
                // because they are lower priority than the layername matches
                // and the loop is not stopping after them
                if (uuid != null) {
                  if (angular.isArray(layers[i].Identifier)) {
                    for (var c = 0; c < layers[i].Identifier.length; c++) {
                      if (layers[i].Identifier[c] == uuid) {
                        needles.push(layers[i]);
                      }
                    }
                  }
                  if (angular.isArray(layers[i].MetadataURL)) {
                    for (var c = 0; c < layers[i].MetadataURL.length; c++) {
                      var mdu = layers[i].MetadataURL[c];
                      if (mdu && mdu.OnlineResource &&
                        mdu.OnlineResource.indexOf(uuid) > 0) {
                        needles.push(layers[i]);
                      }
                    }
                  }
                }
              }
            }

            //FIXME: remove duplicates
            if (needles.length >= layerList.length) {
              if (capObj.version) {
                needles[0].version = capObj.version;
              }
              // Multiple layers from the same service
              if (layerName.indexOf(',') !== -1) {
                // Parameters 'styles' and 'layers' should have the same number of values.
                needles[0].Name = layerName;
                needles[0].Title = needles.map(function(l) {return l.Title}).join(', ');
                needles[0].Style = new Array(layerList.length).join(',');
              }
              return needles[0];
            }
            else {
              return;
            }
          },

          getLayerInfoFromWfsCap: function(name, capObj, uuid) {
            var needles = [];
            var layers = capObj.featureTypeList.featureType;

            for (var i = 0, len = layers.length; i < len; i++) {
              //check layername
              if (name == layers[i].name.localPart ||
                  name == layers[i].name.prefix + ':' +
                  layers[i].name.localPart ||
                  name == layers[i].Name) {
                return layers[i];
              }

              //check title
              if (name == layers[i].title || name == layers[i].Title) {
                return layers[i];
              }

              //check dataset identifer match
              if (uuid != null) {
                if (angular.isArray(layers[i].Identifier)) {
                  angular.forEach(layers[i].Identifier, function(id) {
                    if (id == uuid) {
                      needles.push(layers[i]);
                    }
                  });
                }
              }

              //check uuid from metadata url
              if (uuid != null) {
                if (angular.isArray(layers[i].MetadataURL)) {
                  angular.forEach(layers[i].MetadataURL, function(mdu) {
                    if (mdu && mdu.OnlineResource &&
                        mdu.OnlineResource.indexOf(uuid) > 0) {
                      needles.push(layers[i]);
                    }
                  });
                }
              }
            }

            //FIXME: allow multiple, remove duplicates
            if (needles.length > 0) {
              return needles[0];
            }
            else {
              return;
            }
          }
        };
      }];
  });
})();
