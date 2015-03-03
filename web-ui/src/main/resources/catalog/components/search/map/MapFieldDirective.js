(function() {

  goog.provide('gn_map_field_directive');

  angular.module('gn_map_field_directive', [])

      .directive('gnMapField', [
        'gnMap',
        function(gnMap) {
          return {
            restrict: 'A',
            scope: true,
            templateUrl: '../../catalog/components/search/map/' +
                'partials/mapfield.html',
            compile: function compile(tElement, tAttrs, transclude) {
              return {
                pre: function preLink(scope, iElement, iAttrs, controller) {

                  scope.map = scope.$eval(iAttrs['gnMapField']);
                  scope.gnDrawBboxBtn = iAttrs['gnMapFieldGeom'];
                  scope.gnDrawBboxExtent = iAttrs['gnMapFieldExtent'];

                  // get list of relation types
                  // [overlaps encloses fullyOutsideOf fullyEnclosedWithin
                  // intersection crosses touches within]

                  var opt = scope.$eval(iAttrs['gnMapFieldOpt']) || {};
                  scope.relations = opt.relations;

                  scope.gnMap = gnMap;

                  /**
                   * Fit map view to map projection max extent
                   */
                  scope.maxExtent = function() {
                    scope.map.getView().fitExtent(scope.map.getView().
                            getProjection().getExtent(), scope.map.getSize());
                  };

                  /**
                   * When the geomtry is updated, set this value in
                   * scope.currentExtent and remove relation param if
                   * geometry is null.
                   */
                  scope.$watch(scope.gnDrawBboxBtn, function(v){
                    if(!v) {
                      delete scope.searchObj.params.relation;
                    }
                    scope.currentExtent = scope.$eval(scope.gnDrawBboxBtn);
                  });

                  /**
                   * Set active relation (include, exclude, etc..). Run search
                   * when changed.
                   * @param rel
                   */
                  scope.setRelation = function(rel) {
                    scope.searchObj.params.relation = rel;
                    scope.triggerSearch();
                  };
                }
              };
            }
          };
        }
      ])

      .directive('gnDrawBboxBtn', [
        'ngeoDecorateInteraction',
        '$parse',
        'gnSearchSettings',
        'gnMap',
        function(ngeoDecorateInteraction, $parse, gnSearchSettings, gnMap) {
          return {
            restrict: 'A',
            scope: true,
            controller: ['$scope', function($scope) {
              var dragbox = new ol.interaction.DragBox({
                style: gnSearchSettings.olStyles.drawBbox
              });
              ngeoDecorateInteraction(dragbox, $scope.map);
              dragbox.active = false;
              $scope.map.addInteraction(dragbox);
              $scope.interaction = dragbox;
            }],
            link: function(scope, element, attrs) {

              var parent = scope.$parent.$parent;

              // Assign drawn extent to given scope property
              var bboxGet = $parse(attrs['gnDrawBboxBtn']);
              var bboxSet = bboxGet.assign;

              // Create overlay to persist the bbox
              var feature = new ol.Feature();
              var featureOverlay = new ol.FeatureOverlay({
                style: gnSearchSettings.olStyles.drawBbox
              });
              featureOverlay.setMap(scope.map);
              featureOverlay.addFeature(feature);

              /**
               * Update extent scope value with the WKT polygon
               * @param {ol.geometry} geom
               */
              var updateField = function(geom) {
                feature.setGeometry(geom);

                // Write the extent as 4326 WKT polygon
                var lonlatFeat, writer, wkt;
                lonlatFeat = feature.clone();
                lonlatFeat.getGeometry().transform('EPSG:3857', 'EPSG:4326');
                writer = new ol.format.WKT();
                wkt = writer.writeFeature(lonlatFeat);
                bboxSet(parent, wkt);
              };

              // If given extent coords are given through attributes,
              // display the bbox on the map
              var coords = scope.$eval(attrs['gnDrawBboxExtent']);
              if (coords) {
                updateField(new ol.geom.Polygon(coords));
              }

              scope.interaction.on('boxend', function(mapBrowserEvent) {
                scope.$apply(function() {
                  updateField(scope.interaction.getGeometry());
                });
              });

              // Remove the bbox when the interaction is not active
              scope.$watch('interaction.active', function(v, o) {
                if (!v && o) {
                  feature.setGeometry(null);
                  bboxSet(parent, '');
                  scope.map.render();
                }
              });
            }
          };
        }
      ]);
})();
