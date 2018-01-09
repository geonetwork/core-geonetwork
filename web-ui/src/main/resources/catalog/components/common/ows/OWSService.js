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
  goog.require('GML_2_1_2');
  goog.require('GML_3_1_1');
  goog.require('OWS_1_0_0');
  goog.require('SMIL_2_0');
  goog.require('SMIL_2_0_Language');
  goog.require('WFS_1_0_0');
  goog.require('WFS_1_1_0');
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
      [XLink_1_0, OWS_1_0_0,
       Filter_1_1_0,
       GML_3_1_1,
       SMIL_2_0, SMIL_2_0_Language,
       WFS_1_1_0],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows/1.1': 'ows',
          'http://www.opengis.net/wfs': 'wfs'
        }
      }
      );
  var unmarshaller100 = context100.createUnmarshaller();
  var unmarshaller110 = context110.createUnmarshaller();


  module.provider('gnOwsCapabilities', function() {
    this.$get = ['$http', '$q', '$translate',
      'gnUrlUtils', 'gnGlobalSettings',
      function($http, $q, $translate,
               gnUrlUtils, gnGlobalSettings) {

        var displayFileContent = function(data) {
          var parser = new ol.format.WMSCapabilities();
          var result = parser.read(data);

          var layers = [];
          var url = result.Capability.Request.GetMap.
              DCPType[0].HTTP.Get.OnlineResource;


          // Push all leaves into a flat array of Layers
          // Also adjust crs (by inheritance) and url
          var getFlatLayers = function(layer, inheritedCrs) {
            if (angular.isArray(layer)) {
              for (var i = 0, len = layer.length; i < len; i++) {
                getFlatLayers(layer[i], inheritedCrs);
              }
            } else if (angular.isDefined(layer)) {
              // replace with complete CRS list if available
              if (layer.CRS && layer.CRS.length > inheritedCrs.length) {
                inheritedCrs = layer.CRS;
              }

              // add to flat layer array if we're on a leave (layer w/o child)
              layer.url = url;
              layer.CRS = inheritedCrs;
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

        var parseWFSCapabilities = function(data) {
          var version = '1.1.0';

          try {
            //First cleanup not supported INSPIRE extensions:
            var xml = $.parseXML(data);
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
            }

            return xfsCap;

            /* if (xfsCap.exception != undefined) {
              //defer.reject({msg: 'wfsGetCapabilitiesFailed',
              //  owsExceptionReport: xfsCap});
              return xfsCap;
            }
            else {
              //defer.resolve(xfsCap);
              return xfsCap;
            }*/
          } catch (e) {
            //alert('WFS version not supported.');
            //defer.reject({msg: 'wfsGetCapabilitiesFailed',
            // owsExceptionReport: e.message});
            //return e.message;
          }

          //result.contents.Layer = result.contents.layers;
          //result.Contents.operationsMetadata = result.OperationsMetadata;
          //return result.Contents;
        };

        var mergeParams = function(url, Params) {
          //merge URL parameters with indicated ones
          var parts = url.split('?');
          var urlParams = angular.isDefined(parts[1]) ?
              gnUrlUtils.parseKeyValue(parts[1]) : {};
          for (var p in Params) {
            urlParams[p] = Params[p];
          }
          return gnUrlUtils.append(parts[0], gnUrlUtils.toKeyValue(urlParams));
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

          getWMSCapabilities: function(url) {
            var defer = $q.defer();
            if (url) {
              url = mergeDefaultParams(url, {
                service: 'WMS',
                request: 'GetCapabilities'
              });

              //send request and decode result
              if (true) {
                $http.get(url, {
                  cache: true
                })
                    .success(function(data) {
                      try {
                        defer.resolve(displayFileContent(data));
                      } catch (e) {
                        defer.reject(
                        $translate.instant('failedToParseCapabilities'));
                      }
                    })
                    .error(function(data, status) {
                      defer.reject(
                      $translate.instant('checkCapabilityUrl',
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
                REQUEST: 'GetCapabilities',
                service: 'WMTS'
              });

              if (gnUrlUtils.isValid(url)) {

                $http.get(url, {
                  cache: true
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
                REQUEST: 'GetCapabilities',
                service: 'WFS',
                version: version
              });

              if (gnUrlUtils.isValid(url)) {
                $http.get(url, {
                  cache: true
                })
                    .success(function(data, status, headers, config) {
                      var xfsCap = parseWFSCapabilities(data);

                      if (!xfsCap || xfsCap.exception != undefined) {
                        defer.reject({msg: 'wfsGetCapabilitiesFailed',
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

          getLayerExtentFromGetCap: function(map, getCapLayer) {
            var extent = null;
            var layer = getCapLayer;
            var proj = map.getView().getProjection();

            //var ext = layer.BoundingBox[0].extent;
            //var olExtent = [ext[1],ext[0],ext[3],ext[2]];
            // TODO fix using layer.BoundingBox[0].extent
            // when sextant fix his capabilities

            var bboxProp;
            ['EX_GeographicBoundingBox', 'WGS84BoundingBox'].forEach(
                function(prop) {
                  if (angular.isArray(layer[prop])) {
                    bboxProp = layer[prop];
                  }
                });

            if (bboxProp) {
              extent = ol.extent.containsExtent(proj.getWorldExtent(),
                      bboxProp) ?
                      ol.proj.transformExtent(bboxProp, 'EPSG:4326', proj) :
                      proj.getExtent();
            } else if (angular.isArray(layer.BoundingBox)) {
              for (var i = 0; i < layer.BoundingBox.length; i++) {
                var bbox = layer.BoundingBox[i];
                // Use the bbox with the code matching the map projection
                // or the first one.
                if (bbox.crs === proj.getCode() ||
                    layer.BoundingBox.length === 1) {

                  extent =
                      ol.extent.containsExtent(
                          proj.getWorldExtent(),
                          bbox.extent) ?
                          ol.proj.transformExtent(bbox.extent,
                      bbox.crs || 'EPSG:4326', proj) :
                          proj.getExtent();
                  break;
                }
              }
            }
            return extent;
          },

          getLayerInfoFromCap: function(name, capObj, uuid) {
            var needles = [];
            var layers = capObj.layers || capObj.Layer;

            //non namespaced lowercase name
            nameNoNamespace = name.split(':')[
                name.split(':').length - 1].toLowerCase();

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
              if (name == capName || nameNoNamespace == capNameNoNamespace) {
                layers[i].nameToUse = capName;
                if (capObj.version) {
                  layers[i].version = capObj.version;
                }
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
              if (capObj.version) {
                needles[0].version = capObj.version;
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
