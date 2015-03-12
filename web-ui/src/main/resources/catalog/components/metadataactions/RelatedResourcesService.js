(function() {
  goog.provide('gn_relatedresources_service');

  var module = angular.module('gn_relatedresources_service', []);

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
        function(gnMap, gnOwsCapabilities, gnSearchSettings, 
            ngeoDecorateLayer, gnSearchLocation) {

          this.configure = function(options) {
            angular.extend(this.map, options);
          };

          var addWMSToMap = function(link) {

            if (link.name &&
                (angular.isArray(link.name) && link.name.length > 0)) {
              angular.forEach(link.name, function(name) {
                gnOwsCapabilities.getWMSCapabilities(link.url).then(
                   function(capObj) {
                     var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                     name, capObj);
                     gnMap.addWmsToMapFromCap(
                     gnSearchSettings.viewerMap, layerInfo, capObj);
                   });
              });
              gnSearchLocation.setMap();
            } else if (link.name && !angular.isArray(link.name)) {
              gnOwsCapabilities.getWMSCapabilities(link.url).then(
                  function(capObj) {
                    var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                   link.name, capObj);
                    gnMap.addWmsToMapFromCap(
                        gnSearchSettings.viewerMap, layerInfo, capObj);
                  });
              gnSearchLocation.setMap();
            } else {
              gnMap.addOwsServiceToMap(link.url, 'WMS');
            }
          };


          var addWMTSToMap = function(link) {

            if (link.name &&
                (angular.isArray(link.name) && link.name.length > 0)) {
              angular.forEach(link.name, function(name) {
                gnOwsCapabilities.getWMTSCapabilities(link.url).then(
                   function(capObj) {
                     var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                     name, capObj);
                     gnMap.addWmtsToMapFromCap(
                     gnSearchSettings.viewerMap, layerInfo, capObj);
                   });
              });
              gnSearchLocation.setMap();
            } else if (link.name && !angular.isArray(link.name)) {
              gnOwsCapabilities.getWMTSCapabilities(link.url).then(
                  function(capObj) {
                    var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                   link.name, capObj);
                    gnMap.addWmtsToMapFromCap(
                        gnSearchSettings.viewerMap, layerInfo, capObj);
                  });
              gnSearchLocation.setMap();
            } else {
              gnMap.addOwsServiceToMap(link.url, 'WMTS');
            }
          };

          var addWFSToMap = function(md) {
            //TODO open dialog to download features
            gnSearchLocation.setMap();
          };

          var addKMLToMap = function(md) {
            gnMap.addKmlToMap(md.name, md.url, gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();
          };

          var openMd = function(md) {
            return window.location.hash = '#/metadata/' +
                (md.uuid || md['geonet:info'].uuid);
          };

          var openLink = function(link) {
            if (link.url.indexOf('http') == 0 ||
                link.url.indexOf('ftp') == 0) {
              return window.open(link.url, '_blank');
            } else {
              return window.location.assign(link.title);
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
              iconClass: 'fa-link',
              label: 'webserviceLink',
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

          this.doAction = function(type, parameters, uuid) {
            var f = this.getAction(type);
            f(parameters, uuid);
          };

          this.getType = function(resource) {
            var protocolOrType = resource.protocol + resource.serviceType;
            if (angular.isString(protocolOrType)) {
              if (protocolOrType.match(/wms/i)) {
                return 'WMS';
              } else if (protocolOrType.match(/wmts/i)) {
                return 'WMTS';
              } else if (protocolOrType.match(/wfs/i)) {
                return 'WFS';
              } else if (protocolOrType.match(/kml/i)) {
                return 'KML';
              } else if (protocolOrType.match(/download/i)) {
                return 'LINKDOWNLOAD';
              } else if (protocolOrType.match(/link/i)) {
                return 'LINK';
              }
            }

            if (resource['@type'] &&
                (resource['@type'] === 'parent' ||
                    resource['@type'] === 'children')) {
              return 'MDFAMILY';
            } else if (resource['@type'] &&
               (resource['@type'] === 'sibling')) {
              return 'MDSIBLING';
            } else if (resource['@type'] &&
               (resource['@type'] === 'sources')) {
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
})();
