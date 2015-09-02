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
        function(gnMap, gnOwsCapabilities, gnSearchSettings, 
            ngeoDecorateLayer, gnSearchLocation, gnOwsContextService,
                 gnWfsService) {

          this.configure = function(options) {
            angular.extend(this.map, options);
          };

          var addWMSToMap = function(link, md) {

            if (link.name &&
                (angular.isArray(link.name) && link.name.length > 0)) {
              angular.forEach(link.name, function(name) {
                gnMap.addWmsFromScratch(gnSearchSettings.viewerMap,
                                  link.url, name, false, md);
              });
            } else if (link.name && !angular.isArray(link.name)) {
              gnMap.addWmsFromScratch(gnSearchSettings.viewerMap,
                 link.url, link.name, false, md);
            } else {
              gnMap.addOwsServiceToMap(link.url, 'WMS');
            }

            gnSearchLocation.setMap();
          };


          var addWFSToMap = function(link, md) {


            if (link.name &&
                (angular.isArray(link.name) && link.name.length > 0)) {
              angular.forEach(link.name, function(name) {
                gnMap.addWfsFromScratch(gnSearchSettings.viewerMap,
                       link.url, name, false, md);
              });
            } else if (link.name && !angular.isArray(link.name)) {
              gnMap.addWfsFromScratch(gnSearchSettings.viewerMap,
                 link.url, link.name, false, md);
            } else {
              gnMap.addOwsServiceToMap(link.url, 'WFS');
            }

            gnSearchLocation.setMap();
          };


          var addWMTSToMap = function(link, md) {

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

          var addKMLToMap = function(record, md) {
            gnMap.addKmlToMap(record.name, record.url,
               gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();
          };

          var addMapToMap = function(record, md) {
            gnOwsContextService.loadContextFromUrl(record.url,
               gnSearchSettings.viewerMap, true);

            gnSearchLocation.setMap();
          };

          var openMd = function(record, md) {
            return window.location.hash = '#/metadata/' +
                (record.uuid || record['geonet:info'].uuid);
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
              label: 'webserviceLink',
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

          this.getLabel = function(type) {
            return this.map[type || 'DEFAULT'].label;
          };
          this.getAction = function(type) {
            return this.map[type || 'DEFAULT'].action;
          };

          this.doAction = function(type, parameters, md) {
            var f = this.getAction(type);
            f(parameters, md);
          };

          this.getType = function(resource) {
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
                return 'LINKDOWNLOAD';
              } else if (protocolOrType.match(/link/i)) {
                return 'LINK';
              }
            }

            // Metadata records
            if (resource['@type'] &&
                (resource['@type'] === 'parent' ||
                 resource['@type'] === 'children')) {
              return 'MDFAMILY';
            } else if (resource['@type'] &&
               (resource['@type'] === 'sibling')) {
              return 'MDSIBLING';
            } else if (resource['@type'] &&
               (resource['@type'] === 'sources' ||
                resource['@type'] === 'hassource')) {
              return 'MDSOURCE';
            } else if (resource['@type'] &&
               (resource['@type'] === 'associated' ||
               resource['@type'] === 'services' ||
               resource['@type'] === 'hasfeaturecat' ||
               resource['@type'] === 'datasets')) {
              return 'MD';
            } else if (resource['@type'] && resource['@type'] === 'fcats') {
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
