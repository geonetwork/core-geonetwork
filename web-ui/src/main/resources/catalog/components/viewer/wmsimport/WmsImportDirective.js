(function () {
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
    'gnMapConfig',
    function (gnOwsCapabilities, gnMap, $translate, gnMapConfig) {
    return {
      restrict: 'A',
      replace: true,
      templateUrl: '../../catalog/components/viewer/wmsimport/' +
        'partials/wmsimport.html',
      scope: {
        map: '=gnWmsImportMap'
      },
      controller: ['$scope', function($scope){

        /**
         * Transform a capabilities layer into an ol.Layer
         * and add it to the map.
         *
         * @param getCapLayer
         * @returns {*}
         */
        this.addLayer = function(getCapLayer) {

          var legend, attribution, metadata;
          if (getCapLayer) {
            var layer = getCapLayer;

            // TODO: parse better legend & attribution
            if(angular.isArray(layer.Style) && layer.Style.length > 0) {
              legend = layer.Style[layer.Style.length-1].LegendURL[0].OnlineResource;
            }
            if(angular.isDefined(layer.Attribution) ) {
              if(angular.isArray(layer.Attribution)){

              } else {
                attribution = layer.Attribution.Title;
              }
            }
            if(angular.isArray(layer.MetadataURL)) {
              metadata = layer.MetadataURL[0].OnlineResource;
            }

            return gnMap.addWmsToMap($scope.map, {
                    LAYERS: layer.Name
                  }, {
                    url: layer.url,
                    label: layer.Title,
                    attribution: attribution,
                    legend: legend,
                    metadata: metadata,
                    extent: gnOwsCapabilities.getLayerExtentFromGetCap($scope.map, layer)
                  }
              );
          }
        };
      }],
      link: function (scope, element, attrs) {

        scope.format =  attrs['gnWmsImport'];
        scope.servicesList = gnMapConfig.servicesUrl[scope.format];

        scope.loading = false;

        scope.load = function (url) {
          scope.loading = true;
          gnOwsCapabilities.getCapabilities(url)
            .then(function (capability) {
              scope.loading = false;
              scope.capability = capability;
            });
        };
      }
    };
  }]);

  module.directive('gnKmlImport', [
    'goDecorateLayer',
      'gnAlertService',
    function (goDecorateLayer, gnAlertService) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wmsimport/' +
            'partials/kmlimport.html',
        scope: {
          map: '=gnKmlImportMap'
        },
        controllerAs: 'kmlCtrl',
        controller: [ '$scope', function($scope) {

          /**
           * Create new vector Kml file from url and add it to
           * the Map.
           *
           * @param url remote url of the kml file
           * @param map
           */
          this.addKml = function(url, map) {

            if(url == '') {
              $scope.validUrl = true;
              return;
            }

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
            goDecorateLayer(layer);
            layer.displayInLayerManager = true;
            map.getLayers().push(layer);
            map.getView().fitExtent(layer.getSource().getExtent(),
                map.getSize());

            gnAlertService.addAlert({
              msg: 'Une couche ajoutée : <strong>'+layer.get('label')+'</strong>',
              type: 'success'
            });
          };
        }],
        link: function (scope, element, attrs) {

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

          scope.map.getInteractions().push(dragAndDropInteraction);
          dragAndDropInteraction.on('addfeatures', function(event) {
            if (!event.features || event.features.length == 0) {
              gnAlertService.addAlert({
                msg: 'Import impossible',
                type: 'danger'
              });
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
    function () {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          collection: '='
        },
        template: "<ul class='list-group'><gn-cap-tree-elt ng-repeat='member in collection' member='member'></gn-cap-tree-elt></ul>"
      }
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
    function ($compile) {
    return {
      restrict: "E",
      require: '^gnWmsImport',
      replace: true,
      scope: {
        member: '='
      },
      template: "<li class='list-group-item' ng-click='toggleNode($event)'><label>" +
            "<span class='fa fa-plus-square-o'  ng-if='isParentNode()'></span>" +
            "<input type='checkbox' ng-if='!isParentNode()' data-ng-model='inmap' data-ng-change='select()'>" +
          " {{member.Title}}</label></li>",
      link: function (scope, element, attrs, controller) {
        var el = element;
        if (angular.isArray(scope.member.Layer)) {
          element.append("<gn-cap-tree-col class='list-group' collection='member.Layer'></gn-cap-tree-col>");
          $compile(element.contents())(scope);
        }
        scope.select = function() {
          controller.addLayer(scope.member);
        };
        scope.toggleNode = function(evt) {
          el.find('.fa').first().toggleClass('fa-plus-square-o').toggleClass('fa-minus-square-o');
          el.children('ul').toggle();
          evt.stopPropagation();
          return false;
        };
        scope.isParentNode = function() {
          return angular.isDefined(scope.member.Layer);
        }
      }
    }
  }]);
})();
