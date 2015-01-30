(function() {
  goog.provide('gn_map_directive');

  angular.module('gn_map_directive', [])

    .directive(
      'gnDrawBbox',
      [
       'gnMap',
       function(gnMap) {
         return {
           restrict: 'A',
           replace: true,
           templateUrl: '../../catalog/components/common/map/' +
           'partials/drawbbox.html',
           scope: {
             htopRef: '@',
             hbottomRef: '@',
             hleftRef: '@',
             hrightRef: '@',
             dcRef: '@',
             lang: '=',
             location: '@'
           },
           link: function(scope, element, attrs) {
             scope.drawing = false;
             var mapRef = scope.htopRef || scope.dcRef;
             scope.mapId = 'map-drawbbox-' +
             mapRef.substring(1, mapRef.length);

             /**
              * set dublin-core coverage output
              */
             var setDcOutput = function() {
               if (scope.dcRef) {
                 scope.dcExtent = gnMap.getDcExtent(
                 scope.extent.md,
                 scope.location);
               }
             };

             /**
              * Different projections used in the directive:
              * - md : the proj system in the metadata. It is defined as
              *   4326 by iso19139 schema
              * - map : the projection of the ol3 map, this projection
              *   is set in GN settings
              * - form : projection used for the form, it is chosen
              *   from the combo list.
              */
             scope.projs = {
               list: gnMap.getMapConfig().projectionList,
               md: 'EPSG:4326',
               map: gnMap.getMapConfig().projection,
               form: gnMap.getMapConfig().projectionList[0].code
             };

             scope.extent = {
               md: null,
               map: [],
               form: []
             };

             if (attrs.hleft !== '' && attrs.hbottom !== '' &&
                 attrs.hright !== '' && attrs.htop !== '') {
               scope.extent.md = [
                 parseFloat(attrs.hleft), parseFloat(attrs.hbottom),
                 parseFloat(attrs.hright), parseFloat(attrs.htop)
               ];
             }

             var reprojExtent = function(from, to) {
               scope.extent[to] = gnMap.reprojExtent(
                   scope.extent[from],
                   scope.projs[from], scope.projs[to]
               );
             };

             // Init extent from md for map and form
             reprojExtent('md', 'map');
             reprojExtent('md', 'form');
             setDcOutput();

             scope.$watch('projs.form', function(newValue, oldValue) {
               scope.extent.form = gnMap.reprojExtent(
                   scope.extent.form, oldValue, newValue
               );
             });

             // TODO: move style in db config
             var boxStyle = new ol.style.Style({
               stroke: new ol.style.Stroke({
                 color: 'rgba(255,0,0,1)',
                 width: 2
               }),
               fill: new ol.style.Fill({
                 color: 'rgba(255,0,0,0.3)'
               }),
               image: new ol.style.Circle({
                 radius: 7,
                 fill: new ol.style.Fill({
                   color: 'rgba(255,0,0,1)'
                 })
               })
             });

             var feature = new ol.Feature();
             var source = new ol.source.Vector();
             source.addFeature(feature);

             var bboxLayer = new ol.layer.Vector({
               source: source,
               style: boxStyle
             });

             var map = new ol.Map({
               layers: [
                 gnMap.getLayersFromConfig(),
                 bboxLayer
               ],
               renderer: 'canvas',
               view: new ol.View({
                 center: [0, 0],
                 projection: scope.projs.map,
                 zoom: 2
               })
             });

             var dragbox = new ol.interaction.DragBox({
               style: boxStyle,
               condition: function() {
                  return scope.drawing;
               }
             });

             dragbox.on('boxstart', function(mapBrowserEvent) {
               feature.setGeometry(null);
             });

             dragbox.on('boxend', function(mapBrowserEvent) {
               scope.extent.map = dragbox.getGeometry().getExtent();
               feature.setGeometry(dragbox.getGeometry());

               scope.$apply(function() {
                 reprojExtent('map', 'form');
                 reprojExtent('map', 'md');
                 setDcOutput();
               });
             });

             map.addInteraction(dragbox);

             /**
              * Draw the map extent as a bbox onto the map.
              */
             var drawBbox = function() {
               var coordinates, geom;

               // no geometry
               if (scope.extent.map == null) {
                 return;
               }

               if (gnMap.isPoint(scope.extent.map)) {
                 coordinates = [scope.extent.map[0],
                   scope.extent.map[1]];
                 geom = new ol.geom.Point(coordinates);
               }
               else {
                 coordinates = gnMap.getPolygonFromExtent(
                     scope.extent.map);
                 geom = new ol.geom.Polygon(coordinates);
               }
               feature.setGeometry(geom);
               feature.getGeometry().setCoordinates(coordinates);
               scope.extent.map = geom.getExtent();
             };

             /**
              * When form is loaded
              * - set map div
              * - draw the feature with MD initial coordinates
              * - fit map extent
              */
             scope.$watch('gnCurrentEdit.version', function(newValue) {
               map.setTarget(scope.mapId);
               drawBbox();
               if (gnMap.isValidExtent(scope.extent.map)) {
                 map.getView().fitExtent(scope.extent.map, map.getSize());
               }
             });

             /**
              * Switch mode (panning or drawing)
              */
             scope.drawMap = function() {
               scope.drawing = !scope.drawing;
             };

             /**
              * Called on form input change.
              * Set map and md extent from form reprojection, and draw
              * the bbox from the map extent.
              */
             scope.updateBbox = function() {

               reprojExtent('form', 'map');
               reprojExtent('form', 'md');
               setDcOutput();
               drawBbox();
               map.getView().fitExtent(scope.extent.map, map.getSize());
             };

             /**
              * Callback sent to gn-country-picker directive.
              * Called on region selection from typeahead.
              * Zoom to extent.
              */
             scope.onRegionSelect = function(region) {
               scope.$apply(function() {
                 scope.extent.md = [parseFloat(region.west),
                   parseFloat(region.south),
                   parseFloat(region.east),
                   parseFloat(region.north)];
                 scope.location = region.name;
                 reprojExtent('md', 'map');
                 reprojExtent('md', 'form');
                 setDcOutput();
                 drawBbox();
                 map.getView().fitExtent(scope.extent.map, map.getSize());
               });
             };
           }
         };
       }]);
})();
