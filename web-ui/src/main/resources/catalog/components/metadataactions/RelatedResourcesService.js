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
        'gnViewerSettings',
        'olDecorateLayer',
        'gnSearchLocation',
        'gnOwsContextService',
        'gnWfsService',
        'gnAlertService',
        '$filter',
        'gnExternalViewer',
        function(gnMap, gnOwsCapabilities, gnSearchSettings, gnViewerSettings,
            olDecorateLayer, gnSearchLocation, gnOwsContextService,
            gnWfsService, gnAlertService, $filter, gnExternalViewer) {

          this.configure = function(options) {
            angular.extend(this.map, options);
          };

          /**
           * Check if the link contains a valid layer protocol
           * as configured in gnSearchSettings and check if it
           * has a layer name.
           *
           * If not, then only service information is displayed.
           *
           * @param {object} link
           * @return {boolean}
           */
          this.isLayerProtocol = function(link) {
            return Object.keys(link.title).length > 0 &&
               gnSearchSettings.mapProtocols.layers.
               indexOf(link.protocol) > -1;
          };

          var addWMSToMap = gnViewerSettings.resultviewFns.addMdLayerToMap;
          var addEsriRestToMap = gnViewerSettings.resultviewFns.addMdLayerToMap;

          var addWFSToMap = function(link, md) {
            var url = $filter('gnLocalized')(link.url) || link.url;

            var isServiceLink =
               gnSearchSettings.mapProtocols.services.
               indexOf(link.protocol) > -1;

            var isGetFeatureLink =
               (url.toLowerCase().indexOf('request=getfeature') > -1);

            var featureName;
            if (isGetFeatureLink) {
              var name = 'typename';
              var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
              var results = regex.exec(url);

              if (results) {
                featureName = decodeURIComponent(results[1].replace(/\+/g, ' '));
              }
            } else {
              featureName = $filter('gnLocalized')(link.title);
            }

            // if an external viewer is defined, use it here
            if (gnExternalViewer.isEnabled()) {
              gnExternalViewer.viewService({
                id: md ? md.getId() : null,
                uuid: md ? md.getUuid() : null
              }, {
                type: 'wfs',
                url: url,
                name: featureName
              });
              return;
            }
            if (featureName && (!isServiceLink || isGetFeatureLink)) {
              gnMap.addWfsFromScratch(gnSearchSettings.viewerMap,
                  url, featureName, false, md);
            } else {
              gnMap.addOwsServiceToMap(url, 'WFS');
            }
            gnSearchLocation.setMap();
          };


          var addWMTSToMap = gnViewerSettings.resultviewFns.addMdLayerToMap;

          var addTMSToMap = function(link, md) {
            // Link is localized when using associated resource service
            // and is not when using search
            var url = $filter('gnLocalized')(link.url) || link.url;
            gnMap.createLayerFromProperties({type:'tms',url:url},gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();
          };

          function addKMLToMap(record, md) {
            var url = $filter('gnLocalized')(record.url) || record.url;
            gnMap.addKmlToMap(record.name, url,
               gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();
          };

          function addGeoJSONToMap(record, md) {
            var url = $filter('gnLocalized')(record.url) || record.url;
            gnMap.addGeoJSONToMap(record.name, url,
               gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();
          };

          function addMapToMap(record, md) {
            var url = $filter('gnLocalized')(record.url) || record.url;
            gnOwsContextService.loadContextFromUrl(url,
                gnSearchSettings.viewerMap);

            gnSearchLocation.setMap();
          };

          var openMd = function(r, md) {
            return window.location.hash = '#/metadata/' + r.id;
          };

          var openLink = function(record, link) {
            var url = $filter('gnLocalized')(record.url) || record.url;
            if (url && 
                angular.isString(url) && 
                url.match("^(http|ftp|sftp|\\\\|//)")) {
              return window.open(url, '_blank');
            } else if (url && url.indexOf('www.') == 0) {
              return window.open('http://' + url, '_blank');
            } else if (record.title && 
                       angular.isString(record.title) && 
                       record.title.match("^(http|ftp|sftp|\\\\|//)")) {
              return window.location.assign(record.title);
            } else {
              gnAlertService.addAlert({
                msg: 'Unable to open link',
                type: 'success'
              });
            }
          };

          this.map = {
            'WMS' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addWMSToMap
            },
            'WMSSERVICE' : {
              iconClass: 'fa-globe',
              label: 'addServiceLayersToMap',
              action: addWMSToMap
            },
            'WMTS' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addWMTSToMap
            },
            'TMS' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addTMSToMap
            },
            'WFS' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addWFSToMap
            },
            'ESRI:REST' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: addEsriRestToMap
            },
            'ATOM' : {
              iconClass: 'fa-globe',
              label: 'download'
            },
            'WCS' : {
              iconClass: 'fa-globe',
              label: 'fileLink',
              action: null
            },
            'SOS' : {
              iconClass: 'fa-globe',
              label: 'fileLink',
              action: null
            },
            'MAP' : {
              iconClass: 'fa-map',
              label: 'mapLink',
              action: gnExternalViewer.isEnabled() ? null : addMapToMap
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
              action: gnExternalViewer.isEnabled() ? null : addKMLToMap
            },
            'GEOJSON' : {
              iconClass: 'fa-globe',
              label: 'addToMap',
              action: gnExternalViewer.isEnabled() ? null : addGeoJSONToMap
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
            resource.locTitle = $filter('gnLocalized')(resource.title);
            resource.locDescription = $filter('gnLocalized')(resource.description);
            resource.locUrl = $filter('gnLocalized')(resource.url);
            var protocolOrType = resource.protocol + resource.serviceType;
            // Cas for links
            if (angular.isString(protocolOrType) &&
                angular.isUndefined(resource['geonet:info'])) {
              if (protocolOrType.match(/wms/i)) {
                if (this.isLayerProtocol(resource)) {
                  return 'WMS';
                } else {
                  return 'WMSSERVICE';
                }
              } else if (protocolOrType.match(/esri/i)) {
                return 'ESRI:REST';
              } else if (protocolOrType.match(/wmts/i)) {
                return 'WMTS';
              } else if (protocolOrType.match(/tms/i)) {
                return 'TMS';
              } else if (protocolOrType.match(/wfs/i)) {
                return 'WFS';
              } else if (protocolOrType.match(/wcs/i)) {
                return 'WCS';
              } else if (protocolOrType.match(/sos/i)) {
                return 'SOS';
              } else if (protocolOrType.match(/atom/i)) {
                return 'ATOM';
              } else if (protocolOrType.match(/ows-c/i)) {
                return 'MAP';
              } else if (protocolOrType.match(/db:/i)) {
                return 'DB';
              } else if (protocolOrType.match(/file:/i)) {
                return 'FILE';
              } else if (protocolOrType.match(/kml/i)) {
                return 'KML';
              } else if (protocolOrType.match(/geojson/i)) {
                return 'GEOJSON';
              } else if (protocolOrType.match(/download/i)) {
                var url = $filter('gnLocalized')(resource.url) || resource.url;
                if (url.match(/zip/i)) {
                  return 'LINKDOWNLOAD-ZIP';
                } else if (url.match(/pdf/i)) {
                  return 'LINKDOWNLOAD-PDF';
                } else if (url.match(/xml/i)) {
                  return 'LINKDOWNLOAD-XML';
                } else if (url.match(/rdf/i)) {
                  return 'LINKDOWNLOAD-RDF';
                } else {
                  return 'LINKDOWNLOAD';
                }
              } else if (protocolOrType.match(/dataset/i)) {
                return 'LINKDOWNLOAD';
              } else if (protocolOrType.match(/link/i)) {
                return 'LINK';
              } else if (protocolOrType.match(/website/i)) {
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
