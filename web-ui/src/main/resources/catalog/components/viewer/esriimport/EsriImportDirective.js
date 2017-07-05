(function() {
  goog.provide('gn_esriimport');

  var module = angular.module('gn_esriimport', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnEsriImport
   * 
   * @description Panel to load ESRI capabilities service and pick layers. The
   *              server list is given in global properties.
   */
  module
      .directive(
          'gnEsriImport',
          [
              '$http',
              'gnMap',
              '$translate',
              '$timeout',
              'gnAlertService',
              function($http, gnMap, $translate, $timeout, gnAlertService) {
                return {
                  restrict : 'A',
                  replace : true,
                  templateUrl : '../../catalog/components/viewer/esriimport/'
                      + 'partials/import.html',
                  scope : {
                    map : '=gnEsriImportMap'
                  },
                  link : function(scope, element, attrs) {
                    scope.loading = false;

                    // This event focus on map, display the import tab and ask
                    // the rest service for
                    var event = 'requestCapLoadESRI';
                    scope.$on(event, function(e, url) {
                      // Open layer selection;
                      $('button[rel=#addLayers]').click();

                      // Open Esri
                      $('a', $('li[active="addLayerTabs.esri"]')).click();
                      scope.setUrl(url);
                    });

                    scope.setUrl = function(srv) {
                      scope.url = angular.isObject(srv) ? srv.url : srv;
                      scope.serviceDesc = angular.isObject(srv) ? srv : null;
                      scope.load();
                    };

                    scope.handle = function(layer, evt) {
                      if (layer.type.startsWith('Feature')) {
                        gnMap.addEsriFToMap(scope.url, layer.name, layer.id,
                            layer.serverType, scope.map, layer.label);
                        gnAlertService.addAlert({
                          msg : 'Added layer : <strong>' + layer.label
                              + '</strong>',
                          type : 'success'
                        });
                      } else if (layer.type.startsWith('Raster')) {
                        gnMap.addEsriIToMap(scope.url, layer.name, layer.id,
                            layer.serverType, scope.map, layer.label);
                        gnAlertService.addAlert({
                          msg : 'Added layer : <strong>' + layer.label
                              + '</strong>',
                          type : 'success'
                        });
                      } else {
                        gnAlertService.addAlert({
                          msg : 'Unknown type of layer ' + layer.type,
                          type : 'error'
                        });
                      }
                      evt.stopPropagation();
                    };

                    scope.load = function() {
                      if (scope.url) {
                        scope.loading = true;

                        if (!scope.url.endsWith("/")) {
                          scope.url += "/";
                        }

                        scope.capability = [];

                        var processService = function(url, service) {

                          if (!url.endsWith("/")) {
                            url = url + "/";
                          }

                          url = url + service.name + "/" + service.type + "/";

                          $http.get(
                              '../../proxy?url='
                                  + encodeURIComponent(url
                                      + "?f=json&pretty=true"), {
                                cache : true
                              }).success(function(capability) {
                            if (capability.layers) {
                              capability.layers.forEach(function(layer) {
                                processLayer(url, service, layer);
                              });
                            }
                          });
                        };

                        var processLayer = function(url, service, layer) {

                          $http
                              .get(
                                  '../../proxy?url='
                                      + encodeURIComponent(url + "/" + layer.id
                                          + "?f=json&pretty=true"), {
                                    cache : true
                                  })
                              .success(
                                  function(lay) {

                                    var l = {
                                      "label" : layer.name + " " + service.name,
                                      "name" : service.name,
                                      "id" : layer.id + "",
                                      "url" : url,
                                      "type" : lay.type,
                                      "serverType" : service.type
                                    };

                                    // Check it has the right type, projection
                                    // and operation
                                    if (l.type
                                        && l.type.startsWith("Feature")
                                        && lay.capabilities.indexOf("Query") >= 0
                                        && lay.extent.spatialReference.latestWkid == '3857') {
                                      scope.capability.push(l);
                                    } else if (l.type
                                        && l.type.startsWith("Raster")
                                        && lay.capabilities.indexOf("Map") >= 0
                                        && lay.extent.spatialReference.latestWkid == '3857') {
                                      scope.capability.push(l);
                                    }
                                    scope.loading = false;

                                  });
                        }

                        var processFolder = function(url) {

                          if (!url.endsWith("/")) {
                            url = url + "/";
                          }

                          $http.get(
                              '../../proxy?url='
                                  + encodeURIComponent(url
                                      + "?f=json&pretty=true"), {
                                cache : true
                              }).success(function(capability) {
                            if (capability.folders) {
                              capability.folders.forEach(function(folder) {
                                processFolder(url + folder);
                              });
                            }

                            if (capability.services) {
                              capability.services.forEach(function(service) {
                                processFolder(scope.url + service.name);
                                processService(scope.url, service);
                              });
                            }
                          });
                        };

                        if (scope.url.endsWith("/rest/")) {
                          scope.url += "services/";
                        }

                        if (!scope.url.endsWith("/rest/services/")) {
                          var index = scope.url.indexOf("/rest/services/");
                          var scopeurl = scope.url;
                          scope.url = scope.url.substr(0, index + 15);
                          processFolder(scopeurl);
                        } else {
                          processFolder(scope.url);

                        }

                      }
                    };
                  }
                };
              } ]);
})();
