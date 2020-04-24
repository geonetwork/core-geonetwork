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
  goog.provide('gn_map_directive');

  var METRIC_DECIMALS = 4;
  var DEGREE_DECIMALS = 8;

  var getDigitNumber = function(proj) {
    return proj == 'EPSG:4326' ? DEGREE_DECIMALS : METRIC_DECIMALS;
  };

  angular.module('gn_map_directive', [])
      .directive(
      'gnDrawBbox',
      [
       'gnMap',
       'gnMapsManager',
       'olDecorateInteraction',
       function(gnMap, gnMapsManager, olDecorateInteraction) {
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
             identifierRef: '@',
             identifier: '@',
             descriptionRef: '@',
             description: '@',
             dcRef: '@',
             extentXml: '=?',
             extent: '=?',
             lang: '=',
             schema: '@',
             location: '@'
           },
           link: function(scope, element, attrs) {
             scope.drawing = false;
             var mapRef = scope.htopRef || scope.dcRef || '';
             scope.mapId = 'map-drawbbox-' +
             mapRef.substring(1, mapRef.length);

             // set read only
             scope.readOnly = scope.$eval(attrs['readOnly']);

             var extentTpl = {
               'iso19139': '<gmd:EX_Extent ' +
               'xmlns:gmd="http://www.isotc211.org/2005/gmd" ' +
               'xmlns:gco="http://www.isotc211.org/2005/gco">' +
               '<gmd:geographicElement>' +
               '<gmd:EX_GeographicBoundingBox>' +
               '<gmd:westBoundLongitude><gco:Decimal>{{west}}</gco:Decimal>' +
               '</gmd:westBoundLongitude>' +
               '<gmd:eastBoundLongitude><gco:Decimal>{{east}}</gco:Decimal>' +
               '</gmd:eastBoundLongitude>' +
               '<gmd:southBoundLatitude><gco:Decimal>{{south}}</gco:Decimal>' +
               '</gmd:southBoundLatitude>' +
               '<gmd:northBoundLatitude><gco:Decimal>{{north}}</gco:Decimal>' +
               '</gmd:northBoundLatitude>' +
               '</gmd:EX_GeographicBoundingBox></gmd:geographicElement>' +
               '</gmd:EX_Extent>',
               'iso19115-3': '<gex:EX_Extent ' +
               'xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0" ' +
               'xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0">' +
               '<gex:geographicElement>' +
               '<gex:EX_GeographicBoundingBox>' +
               '<gex:westBoundLongitude><gco:Decimal>{{west}}</gco:Decimal>' +
               '</gex:westBoundLongitude>' +
               '<gex:eastBoundLongitude><gco:Decimal>{{east}}</gco:Decimal>' +
               '</gex:eastBoundLongitude>' +
               '<gex:southBoundLatitude><gco:Decimal>{{south}}</gco:Decimal>' +
               '</gex:southBoundLatitude>' +
               '<gex:northBoundLatitude><gco:Decimal>{{north}}</gco:Decimal>' +
               '</gex:northBoundLatitude>' +
               '</gex:EX_GeographicBoundingBox></gex:geographicElement>' +
               '</gex:EX_Extent>'
             };
             var xmlExtentFn = function(coords, location) {
               if (angular.isArray(coords) &&
               coords.length === 4 &&
               !isNaN(coords[0]) &&
               !isNaN(coords[1]) &&
               !isNaN(coords[2]) &&
               !isNaN(coords[3]) &&
               angular.isNumber(coords[0]) &&
               angular.isNumber(coords[1]) &&
               angular.isNumber(coords[2]) &&
               angular.isNumber(coords[3])) {
                 scope.extentXml = extentTpl[scope.schema || 'iso19139']
                 .replace('{{west}}', coords[0])
                 .replace('{{south}}', coords[1])
                 .replace('{{east}}', coords[2])
                 .replace('{{north}}', coords[3]);
               }
             };

             /**
              * set dublin-core coverage output
              */
             var setDcOutput = function() {
               if (scope.dcRef) {
                 scope.dcExtent = gnMap.getDcExtent(
                 scope.extent.md,
                 scope.location);
               }

               // Format extent values with at least 2 decimals
               if (scope.extent.md) {
                 scope.extent.md.forEach(function (v, i) {
                   if (v != null) {
                     scope.extent.md[i] = v.toFixed(
                       Math.max(2, getPrecision(v)));
                   }
                 });
               }

               xmlExtentFn(scope.extent.md, scope.location);
             };

             /**
              * Get decimal positions for a number.
              *
              * @param num
              * @returns {number}
              */
             var getPrecision = function(num) {
               var s = num + "",
                 d = s.indexOf('.') + 1;

               return !d ? 0 : s.length - d;
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
               form: gnMap.getMapConfig().projectionList[0].code,
               formLabel: gnMap.getMapConfig().projectionList[0].label
             };

             scope.extent = scope.extent || {
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
               var extent = gnMap.reprojExtent(
               scope.extent[from],
               scope.projs[from], scope.projs[to]
               );
               if (extent && extent.map) {
                 var decimals = getDigitNumber(scope.projs.form);
                 scope.extent[to] = extent.map(function(coord) {
                   return coord === null ? null : coord.toFixed(decimals) / 1;
                 });
               }
             };

             // Init extent from md for map and form
             reprojExtent('md', 'map');
             reprojExtent('md', 'form');
             setDcOutput();

             scope.$watch('projs.form', function(newValue, oldValue) {
               var extent = gnMap.reprojExtent(
               scope.extent.form, oldValue, newValue
               );
               if (extent && extent.map) {
                 var decimals = getDigitNumber(scope.projs.form);

                 scope.extent.form = extent.map(function(coord) {
                   return coord.toFixed(decimals) / 1;
                 });
               }
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
             bboxLayer.setZIndex(100);

             var map = gnMapsManager.createMap(gnMapsManager.EDITOR_MAP);
             scope.map = map;
             map.addLayer(bboxLayer);
             element.data('map', map);

             // initialize extent & bbox on map load
             map.get('sizePromise').then(function() {
               drawBbox();

               if (gnMap.isValidExtent(scope.extent.map)) {
                 map.getView().fit(scope.extent.map, map.getSize());
               }
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
               map.getView().fit(scope.extent.map, map.getSize());
             };

             /**
              * Callback sent to gn-country-picker directive.
              * Called on region selection from typeahead.
              * Zoom to extent.
              */
             scope.onRegionSelect = function(region) {
               // Manage regions service and geonames
               var bbox = region.bbox || region;
               scope.$apply(function() {
                 scope.extent.md = [parseFloat(bbox.west),
                   parseFloat(bbox.south),
                   parseFloat(bbox.east),
                   parseFloat(bbox.north)];
                 scope.location = region.name;

                 if (attrs.identifierRef !== undefined) {
                   scope.identifier = region.id;
                 }
                 if (attrs.descriptionRef !== undefined) {
                   scope.description = region.name;
                 }

                 reprojExtent('md', 'map');
                 reprojExtent('md', 'form');
                 setDcOutput();
                 drawBbox();
                 map.getView().fit(scope.extent.map, map.getSize());
               });
             };

             scope.resetRegion = function() {
               element.find('.twitter-typeahead').find('input').val('');
             };
           }
         };
       }]);
})();
