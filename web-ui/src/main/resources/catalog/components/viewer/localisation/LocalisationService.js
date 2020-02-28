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
  goog.provide('gn_localisation_service');

  var module = angular.module('gn_localisation_service', []);

  var DMSDegree = '[0-9]{1,2}[°|º]\\s*';
  var DMSMinute = '[0-9]{1,2}[\'|′]';
  var DMSSecond = '(?:\\b[0-9]+(?:\\.[0-9]*)?|\\.' +
      '[0-9]+\\b)("|\'\'|′′|″)';
  var DMSNorth = '[N]';
  var DMSEast = '[E]';
  var regexpDMSN = new RegExp(DMSDegree +
      '(' + DMSMinute + ')?\\s*' +
      '(' + DMSSecond + ')?\\s*' +
      DMSNorth, 'g');
  var regexpDMSE = new RegExp(DMSDegree +
      '(' + DMSMinute + ')?\\s*' +
      '(' + DMSSecond + ')?\\s*' +
      DMSEast, 'g');
  var regexpDMSDegree = new RegExp(DMSDegree, 'g');
  var regexpCoordinate = new RegExp(
      '^\\s*(\-?[\\d\\.\']+)[\\s,]+(\-?[\\d\\.\']+)');


  /**
   * @ngdoc service
   * @kind function
   * @name gn_viewer.service:gnGetCoordinate
   *
   * @description
   * The `gnGetCoordinate` parses the coordinates given in the localisation
   * input. It detects the format (lat/lon degree meter projection etc..)
   * and try to guess the coordinates projection to return the map extent
   * of the localisation.
   */
  module.value('gnGetCoordinate', function(extent, query) {
    var position;
    var valid = false;

    var matchDMSN = query.match(regexpDMSN);
    var matchDMSE = query.match(regexpDMSE);
    if (matchDMSN && matchDMSN.length == 1 &&
            matchDMSE && matchDMSE.length == 1) {
      var northing = parseFloat(matchDMSN[0].
              match(regexpDMSDegree)[0].
              replace('°' , '').replace('º' , ''));
      var easting = parseFloat(matchDMSE[0].
              match(regexpDMSDegree)[0].
              replace('°' , '').replace('º' , ''));
      var minuteN = matchDMSN[0].match(DMSMinute) ?
              matchDMSN[0].match(DMSMinute)[0] : '0';
      northing = northing +
              parseFloat(minuteN.replace('\'' , '').
                  replace('′' , '')) / 60;
      var minuteE = matchDMSE[0].match(DMSMinute) ?
              matchDMSE[0].match(DMSMinute)[0] : '0';
      easting = easting +
              parseFloat(minuteE.replace('\'' , '').
                  replace('′' , '')) / 60;
      var secondN =
          matchDMSN[0].match(DMSSecond) ?
          matchDMSN[0].match(DMSSecond)[0] : '0';
      northing = northing + parseFloat(secondN.replace('"' , '')
              .replace('\'\'' , '').replace('′′' , '')
              .replace('″' , '')) / 3600;
      var secondE = matchDMSE[0].match(DMSSecond) ?
              matchDMSE[0].match(DMSSecond)[0] : '0';
      easting = easting + parseFloat(secondE.replace('"' , '')
              .replace('\'\'' , '').replace('′′' , '')
              .replace('″' , '')) / 3600;
      position = [easting, northing];
      if (ol.extent.containsCoordinate(
          extent, position)) {
        valid = true;
      }
    }

    var match =
        query.match(regexpCoordinate);
    if (match && !valid) {
      var x = parseFloat(match[1].replace('\'', ''));
      var y = parseFloat(match[2].replace('\'', ''));
      var position =
          [x, y];
      if (ol.extent.containsCoordinate(
          extent, position)) {
        valid = true;
      } else {
        //TODO: don't use hardcoded projections
        position = ol.proj.transform(position,
            'EPSG:3857', 'EPSG:4326');
        if (ol.extent.containsCoordinate(
            extent, position)) {
          valid = true;
        }
      }
    }
    return valid ?
        [Math.round(position[0] * 1000) / 1000,
         Math.round(position[1] * 1000) / 1000] : undefined;
  }
  );

})();
