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
  goog.provide('gn_relatedresources_service');

  goog.require('gn_wfs_service');

  var module = angular.module('gn_relatedresources_service',
      ['gn_wfs_service']);

  /**
   * Standarizes the way to handle resources. Given a type of resource, you get
   * an icon class and an action.
   *
   * To extend this, use the configure function. For example:
   *
   * $gnRelatedResources.configure({ "PDF" : { iconClass: "pdfClassIcon",
   * action: myCustomFunctionForPDF}, "XLS" : { iconClass: "xlsClassIcon",
   * action: myCustomFunctionForXLS}});
   *
   */
  module
      .service(
          'gnRelatedResources',
          [
        'gnMap',
        'gnOwsCapabilities',
        'gnSearchSettings',
        'ngeoDecorateLayer',
        'gnSearchLocation',
        'gnOwsContextService',
        'gnWfsService',
        '$filter',
        function(gnMap, gnOwsCapabilities, gnSearchSettings,
            ngeoDecorateLayer, gnSearchLocation, gnOwsContextService,
            gnWfsService, $filter) {

          this.configure = function(options) {
            angular.extend(this.map, options);
          };

          var addWMSToMap = function(link, md) {
            var layerName = $filter('gnLocalized')(link.title);
            if (layerName) {
              gnMap.addWmsFromScratch(gnSearchSettings.viewerMap,
                 link.url, layerName, false, md);
            } else {
              gnMap.addOwsServiceToMap(link.url, 'WMS');
            }

            gnSearchLocation.setMap();
          };


          var addWFSToMap = function(link, md) {
            var ftName = $filter('gnLocalized')(link.title);
            if (ftName) {
              gnMap.addWfsFromScratch(gnSearchSettings.viewerMap,
                 link.url, ftName, false, md);
            } else {
              gnMap.addOwsServiceToMap(link.url, 'WFS');
            }
            gnSearchLocation.setMap();
          };


          function addWMTSToMap(link, md) {

            if (link.name &&
                (angular.isArray(link.name) && link.name.length > 0)) {
              angular.forEach(link.name, function(name) {
                gnOwsCapabilities.getWMTSCapabilities(link.url).then(
                   function(capObj) {
                     var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                     name, capObj, uuid);
                     gnMap.addWmtsToMapFromCap(
                     gnSearchSettings.viewerMap, layerInfo, capObj);
                   });
              });
              gnSearchLocation.setMap();
            } else if (link.name && !angular.isArray(link.name)) {
              gnOwsCapabilities.getWMTSCapabilities(link.url).then(
                  function(capObj) {
                    var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                   link.name, capObj, uuid);
                    gnMap.addWmtsToMapFromCap(
                        gnSearchSettings.viewerMap, layerInfo, capObj);
                  });
              gnSearchLocation.setMap();
            } else {
              gnMap.addOwsServiceToMap(link.url, 'WMTS');
            }
          };

          function addKMLToMap(record, md) {
            gnMap.addKmlToMap(record.name, record.url,
               gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();
          };

          function addMapToMap(record, md) {
            gnOwsContextService.loadContextFromUrl(record.url,
                gnSearchSettings.viewerMap);

            gnSearchLocation.setMap();
          };

          var openMd = function(r, md) {
            return window.location.hash = '#/metadata/' + r.id;
          };

          var openLink = function(record, link) {
            if (record.url.indexOf('http') == 0 ||
                record.url.indexOf('ftp') == 0) {
              return window.open(record.url, '_blank');
            } else {
              return window.location.assign(record.title);
            }
          };

          this.map = {
            'WMS' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addWMSToMap
            },
            'WMTS' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addWMTSToMap
            },
            'WFS' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addWFSToMap
            },
            'WCS' : {
              iconClass: 'fa-globe',
              label: 'fileLink',
              action: null
            },
            'MAP' : {
              iconClass: 'fa-globe',
              label: 'mapLink',
              action: addMapToMap
            },
            'DB' : {
              iconClass: 'fa-database',
              label: 'dbLink',
              action: null
            },
            'FILE' : {
              iconClass: 'fa-file',
              label: 'fileLink',
              action: openLink
            },
            'KML' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addKMLToMap
            },
            'MDFCATS' : {
              iconClass: 'fa-table',
              label: 'openRecord',
              action: openMd
            },
            'MDFAMILY' : {
              iconClass: 'fa-sitemap',
              label: 'openRecord',
              action: openMd
            },
            'MDSIBLING' : {
              iconClass: 'fa-sign-out',
              label: 'openRecord',
              action: openMd
            },
            'MDSOURCE' : {
              iconClass: 'fa-sitemap fa-rotate-180',
              label: 'openRecord',
              action: openMd
            },
            'MD' : {
              iconClass: 'fa-file',
              label: 'openRecord',
              action: openMd
            },
            'LINKDOWNLOAD' : {
              iconClass: 'fa-download',
              label: 'download',
              action: openLink
            },
            'LINKDOWNLOAD-ZIP' : {
              iconClass: 'fa-file-zip-o',
              label: 'download',
              action: openLink
            },
            'LINKDOWNLOAD-PDF' : {
              iconClass: 'fa-file-pdf-o',
              label: 'download',
              action: openLink
            },
            'LINKDOWNLOAD-XML' : {
              iconClass: 'fa-file-code-o',
              label: 'download',
              action: openLink
            },
            'LINKDOWNLOAD-RDF' : {
              iconClass: 'fa-share-alt',
              label: 'download',
              action: openLink
            },
            'LINK' : {
              iconClass: 'fa-link',
              label: 'openPage',
              action: openLink
            },
            'DEFAULT' : {
              iconClass: 'fa-fw',
              label: 'openPage',
              action: openLink
            }
          };

          this.getClassIcon = function(type) {
            return this.map[type || 'DEFAULT'].iconClass ||
                this.map['DEFAULT'].iconClass;
          };

          this.getLabel = function(mainType, type) {
            // Old key before the move to API
            var oldKey = {
              hasfeaturecats: 'hasfeaturecat',
              onlines: 'onlinesrc',
              siblings: 'sibling',
              fcats: 'fcat',
              hassources: 'hassource'
            };
            return this.map[mainType || 'DEFAULT'].label +
                   (oldKey[type] ? oldKey[type] : type);
          };
          this.getAction = function(type) {
            return this.map[type || 'DEFAULT'].action;
          };

          this.doAction = function(type, parameters, md) {
            var f = this.getAction(type);
            f(parameters, md);
          };

          this.getType = function(resource, type) {
            var protocolOrType = resource.protocol + resource.serviceType;
            // Cas for links
            if (angular.isString(protocolOrType) &&
                angular.isUndefined(resource['geonet:info'])) {
              if (protocolOrType.match(/wms/i)) {
                return 'WMS';
              } else if (protocolOrType.match(/wmts/i)) {
                return 'WMTS';
              } else if (protocolOrType.match(/wfs/i)) {
                return 'WFS';
              } else if (protocolOrType.match(/wcs/i)) {
                return 'WCS';
              } else if (protocolOrType.match(/ows-c/i)) {
                return 'MAP';
              } else if (protocolOrType.match(/db:/i)) {
                return 'DB';
              } else if (protocolOrType.match(/file:/i)) {
                return 'FILE';
              } else if (protocolOrType.match(/kml/i)) {
                return 'KML';
              } else if (protocolOrType.match(/download/i)) {
                if (resource.url.match(/zip/i)) {
                  return 'LINKDOWNLOAD-ZIP';
                } else if (resource.url.match(/pdf/i)) {
                  return 'LINKDOWNLOAD-PDF';
                } else if (resource.url.match(/xml/i)) {
                  return 'LINKDOWNLOAD-XML';
                } else if (resource.url.match(/rdf/i)) {
                  return 'LINKDOWNLOAD-RDF';
                } else {
                  return 'LINKDOWNLOAD';
                }
              // Anything that is not matched before, gets the link icon
              } else {
                return 'LINK';
              }
            }

            // Metadata records
            if (type &&
                (type === 'parent' ||
                 type === 'children')) {
              return 'MDFAMILY';
            } else if (type &&
               (type === 'siblings')) {
              return 'MDSIBLING';
            } else if (type &&
               (type === 'sources' ||
                type === 'hassources')) {
              return 'MDSOURCE';
            } else if (type &&
               (type === 'associated' ||
               type === 'services' ||
               type === 'hasfeaturecats' ||
               type === 'datasets')) {
              return 'MD';
            } else if (type && type === 'fcats') {
              return 'MDFCATS';
            }

            return 'DEFAULT';
          };
        }
      ]);

  /**
   * AngularJS Filter. Filters an array of relations by the given tpye.
   * Uses : relations | relationsfilter:'children children'
   */
  module.filter('gnRelationsFilter', function() {
    return function(relations, types) {
      var result = [];
      var types = types.split(' ');
      angular.forEach(relations, function(rel) {
        if (types.indexOf(rel['@type']) >= 0) {
          result.push(rel);
        }
      });
      return result;
    }
  });
})();
