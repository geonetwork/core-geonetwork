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
        function(gnMap, gnOwsCapabilities, gnSearchSettings, $scope) {

          this.configure = function(options) {
            angular.extend(this.map, options);
          };

          var addWMSToMap = function(link) {
            gnOwsCapabilities.getWMSCapabilities(link.url).then(
               function(capObj) {
                 var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                 link.name, capObj);
                 var layer = gnMap.addWmsToMapFromCap(
                 $scope.searchObj.viewerMap, layerInfo);
               });
          };

          var addWFSToMap = function(md) {
            gnMap.addWfsToMap(gnSearchSettings.viewerMap, {
              'LAYERS' : md.name
            }, {
              'url' : md.url
            });
          };

          var addKMLToMap = function(md) {
            gnMap.addKmlToMap(md.name, md.url, gnSearchSettings.viewerMap);
          };

          var openMd = function(md) {
            return window.location.hash = '#/metadata/' +
                (md.uuid || md['geonet:info'].uuid);
          };

          var openLink = function(link) {
            if (link.url.indexOf('http') == 0 ||
                link.url.indexOf('ftp') == 0) {
              return window.location.assign(link.url);
            } else {
              return window.location.assign(link.title);
            }
          };

          this.map = {
            'WMS' : {
              iconClass: 'fa fa-link',
              action: addWMSToMap
            },
            'WFS' : {
              iconClass: 'fa fa-link',
              action: addWFSToMap
            },
            'KML' : {
              iconClass: 'fa fa-link',
              action: addKMLToMap
            },
            'CATALOG' : {
              iconClass: 'fa fa-table',
              action: openLink
            },
            'MD' : {
              iconClass: 'fa fa-files-o',
              action: openMd
            },
            'LINK' : {
              iconClass: 'fa fa-link',
              action: openLink
            },
            'DEFAULT' : {
              iconClass: '',
              action: openLink
            }
          };

          this.getClassIcon = function(type) {
            return this.map[type].iconClass ||
                this.map['DEFAULT'].iconClass;
          };

          this.getAction = function(type) {
            return this.map[type].action || this.map['DEFAULT'].action;
          };

          this.doAction = function(type, parameters) {
            var f = this.getAction(type);
            f(parameters);
          };

          this.getType = function(resource) {
            if ((resource.protocol && resource.protocol.contains('WMS')) ||
                (resource.serviceType && resource.serviceType
                          .contains('WMS'))) {
              return 'WMS';
            } else if ((resource.protocol && resource.protocol
                      .contains('WFS')) ||
               (resource.serviceType && resource.serviceType
                          .contains('WFS'))) {
              return 'WFS';
            } else if ((resource.protocol && resource.protocol
                      .contains('KML')) ||
               (resource.serviceType && resource.serviceType
                          .contains('KML'))) {
              return 'KML';
            } else if (resource.protocol &&
                (resource.protocol.contains('DOWNLOAD') ||
                    resource.protocol.contains('LINK'))) {
              return 'LINK';
            } else if (resource['@type'] &&
                (resource['@type'] === 'sibling' ||
                    resource['@type'] === 'parent' ||
                    resource['@type'] === 'associated' ||
                    resource['@type'] === 'datasets')) {
              return 'MD';
            } else if (resource['@type'] && resource['@type'] === 'fcats') {
              return 'CATALOG';
            }

            console.log(resource);
            return 'DEFAULT';
          };
        }
          ]);
})();
