(function() {
  goog.provide('gn_wmsimport_directive');

  var module = angular.module('gn_wmsimport_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnWmsImport', [
    'gnOwsCapabilities',
    'gnMap',
    '$translate',
    'gnViewerSettings',
    function(gnOwsCapabilities, gnMap, $translate, gnViewerSettings) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wmsimport/' +
            'partials/wmsimport.html',
        scope: {
          map: '=gnWmsImportMap'
        },
        controller: ['$scope', function($scope) {

          /**
         * Transform a capabilities layer into an ol.Layer
         * and add it to the map.
         *
         * @param {Object} getCapLayer
         * @return {*}
         */
          this.addLayer = function(getCapLayer) {
            if ($scope.format == 'wms') {
              return gnMap.addWmsToMapFromCap($scope.map, getCapLayer);
            }
            else if ($scope.format == 'wmts') {
              return gnMap.addWmtsToMapFromCap($scope.map, getCapLayer,
                  $scope.capability);
            }
          };
        }],
        link: function(scope, element, attrs) {


          scope.format = attrs['gnWmsImport'];
          scope.servicesList = gnViewerSettings.servicesUrl[scope.format];

          scope.loading = false;

          // This event focus on map, display the WMSImport and request
          // a getCapabilities
          //TODO : to be improved
          var type = scope.format.toUpperCase();
          var event = 'requestCapLoad' + type;
          scope.$on(event, function(e, url) {
            $('#layers').removeClass('force-hide');
            $('[gn-wms-import]').removeClass('collapsed');
            var button = $('[data-gn-import-button=' + type + ']');
            var element = button.parent().parent();

            element.find('.btn-group.flux button').removeClass('active');
            element.addClass('active');
            button.addClass('active');
            element.find('.layers').addClass('collapsed');
            element.find('.panel-carousel').removeClass('collapsed');
            element.find('.unfold').css('opacity', 1);
            element.find('.panel-carousel-container').css('left',
                '-' + (button.index() * 100) + '%');

            scope.url = url;
            scope.load(url);
          });

          scope.load = function(url) {
            scope.loading = true;
            gnOwsCapabilities['get' + scope.format.toUpperCase() +
                'Capabilities'](url).then(function(capability) {
              scope.loading = false;
              scope.capability = capability;
            });
          };
        }
      };
    }]);

  module.directive('gnKmlImport', [
    'ngeoDecorateLayer',
    'gnAlertService',
    function(ngeoDecorateLayer, gnAlertService) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wmsimport/' +
            'partials/kmlimport.html',
        scope: {
          map: '=gnKmlImportMap'
        },
        controllerAs: 'kmlCtrl',
        controller: ['$scope', function($scope) {

          /**
           * Create new vector Kml file from url and add it to
           * the Map.
           *
           * @param {string} url remote url of the kml file
           * @param {ol.map} map
           */
          this.addKml = function(url, map) {

            if (url == '') {
              $scope.validUrl = true;
              return;
            }

            //FIXME use global constant defined in gnGlobalSettings
            var proxyUrl = '../../proxy?url=' + encodeURIComponent(url);
            var kmlSource = new ol.source.KML({
              projection: 'EPSG:3857',
              url: proxyUrl
            });

            var vector = new ol.layer.Vector({
              source: kmlSource,
              label: 'Fichier externe : ' + url.split('/').pop()
            });

            var listenerKey = kmlSource.on('change', function() {
              if (kmlSource.getState() == 'ready') {
                kmlSource.unByKey(listenerKey);
                $scope.addToMap(vector, map);
                $scope.validUrl = true;
                $scope.url = '';
              }
              else if (kmlSource.getState() == 'error') {
                $scope.validUrl = false;
              }
              $scope.$apply();
            });
          };

          $scope.addToMap = function(layer, map) {
            ngeoDecorateLayer(layer);
            layer.displayInLayerManager = true;
            map.getLayers().push(layer);
            map.getView().fitExtent(layer.getSource().getExtent(),
                map.getSize());

            gnAlertService.addAlert({
              msg: 'Une couche ajoutée : <strong>' +
                  layer.get('label') + '</strong>',
              type: 'success'
            });
          };
        }],
        link: function(scope, element, attrs) {

          /** Used for ngClass of the input */
          scope.validUrl = true;

          /** File drag & drop support */
          var dragAndDropInteraction = new ol.interaction.DragAndDrop({
            formatConstructors: [
              ol.format.GPX,
              ol.format.GeoJSON,
              ol.format.KML,
              ol.format.TopoJSON
            ]
          });

          var onError = function(msg) {
            gnAlertService.addAlert({
              msg: 'Import impossible',
              type: 'danger'
            });
          };

          scope.map.getInteractions().push(dragAndDropInteraction);
          dragAndDropInteraction.on('addfeatures', function(event) {
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
              label: 'Fichier local : ' + event.file.name
            });
            scope.addToMap(layer, scope.map);
            scope.$apply();
          });


          var requestFileSystem = window.webkitRequestFileSystem ||
              window.mozRequestFileSystem || window.requestFileSystem;
          var unzipProgress = document.createElement('progress');
          var fileInput = document.getElementById('file-input');

          var model = (function() {
            var URL = window.webkitURL || window.mozURL || window.URL;

            return {
              getEntries: function(file, onend) {
                zip.createReader(new zip.BlobReader(file), function(zipReader) {
                  zipReader.getEntries(onend);
                }, onerror);
              },
              getEntryFile: function(entry, creationMethod, onend, onprogress) {
                var writer, zipFileEntry;

                function getData() {
                  entry.getData(writer, function(blob) {
                    var blobURL = URL.createObjectURL(blob);
                    onend(blobURL);
                  }, onprogress);
                }
                writer = new zip.BlobWriter();
                getData();
              }
            };
          })();

          scope.onEntryClick = function(entry, evt) {
            model.getEntryFile(entry, 'Blob', function(blobURL) {
              entry.loading = true;
              scope.$apply();
              var vector = new ol.layer.Vector({
                label: 'Fichier local : ' + entry.filename,
                source: new ol.source.KML({
                  projection: 'EPSG:3857',
                  url: blobURL
                })
              });
              var listenerKey = vector.getSource().on('change', function(evt) {
                if (vector.getSource().getState() == 'ready') {
                  vector.getSource().unByKey(listenerKey);
                  scope.addToMap(vector, scope.map);
                  entry.loading = false;
                }
                else if (vector.getSource().getState() == 'error') {
                }
                scope.$apply();
              });
            }, function(current, total) {
              unzipProgress.value = current;
              unzipProgress.max = total;
              evt.target.appendChild(unzipProgress);
            });
          };

          scope.uploadKMZ = function() {
            if (fileInput.files.length > 0) {
              model.getEntries(fileInput.files[0], function(entries) {
                scope.kmzEntries = entries;
                scope.$apply();
              });
            }
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnCapTreeCol
   *
   * @description
   * Directive to manage a collection of nested layers from
   * the capabilities document. This directive works with
   * gnCapTreeElt directive.
   */
  module.directive('gnCapTreeCol', [
    function() {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          collection: '='
        },
        template: "<ul class='list-group'><gn-cap-tree-elt " +
            "ng-repeat='member in collection' member='member'>" +
            '</gn-cap-tree-elt></ul>'
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnCapTreeElt
   *
   * @description
   * Directive to manage recursively nested layers from a capabilities
   * document. Will call its own template to display the layer but also
   * call back the gnCapTreeCol for all its children.
   */
  module.directive('gnCapTreeElt', [
    '$compile',
    'gnAlertService',
    function($compile, gnAlertService) {
      return {
        restrict: 'E',
        require: '^gnWmsImport',
        replace: true,
        scope: {
          member: '='
        },
        template: "<li class='list-group-item' ng-click='handle($event)' " +
            "ng-class='(!isParentNode()) ? \"leaf\" : \"\"'><label>" +
            "<span class='fa'  ng-class='isParentNode() ? \"fa-folder-o\" :" +
            " \"fa-plus-square-o\"'></span>" +
            ' {{member.Title || member.title}}</label></li>',
        link: function(scope, element, attrs, controller) {
          var el = element;
          var select = function() {
            controller.addLayer(scope.member);
            gnAlertService.addAlert({
              msg: 'Une couche ajoutée : <strong>' +
                  (scope.member.Title || scope.member.title) + '</strong>',
              type: 'success'
            });
          };
          var toggleNode = function() {
            el.find('.fa').first().toggleClass('fa-folder-o')
                .toggleClass('fa-folder-open-o');
            el.children('ul').toggle();
          };
          if (angular.isArray(scope.member.Layer)) {
            element.append("<gn-cap-tree-col class='list-group' " +
                "collection='member.Layer'></gn-cap-tree-col>");
            $compile(element.contents())(scope);
          }
          scope.handle = function(evt) {
            if (scope.isParentNode()) {
              toggleNode();
            } else {
              select();
            }
            evt.stopPropagation();
          };
          scope.isParentNode = function() {
            return angular.isDefined(scope.member.Layer);
          };
        }
      };
    }]);
})();
