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

(function () {
  goog.provide("gn_default_gazetteer_default_factory");

  var module = angular.module("gn_default_gazetteer_default_factory", []);

  module.provider("gnDefaultGazetteer", function () {
    return {
      $get: [
        "$http",
        "gnGlobalSettings",
        "gnViewerSettings",
        "gnGetCoordinate",
        function ($http, gnGlobalSettings, gnViewerSettings, gnGetCoordinate) {
          var zoomTo = function (extent, map) {
            map.getView().fit(extent, map.getSize());
          };
          return {
            onClick: function (scope, loc, map) {
              zoomTo(loc.extent, map);
              scope.query = loc.name;
              scope.collapsed = true;
            },
            search: function (scope, loc, query) {
              var lang = gnGlobalSettings.lang;

              if (query.length < 1) {
                scope.results = [];
                return;
              }
              var coord = gnGetCoordinate(
                scope.map.getView().getProjection().getWorldExtent(),
                query
              );

              if (coord) {
                function moveTo(map, zoom, center) {
                  var view = map.getView();

                  view.setZoom(zoom);
                  view.setCenter(center);
                }
                moveTo(
                  scope.map,
                  5,
                  ol.proj.transform(
                    coord,
                    "EPSG:4326",
                    scope.map.getView().getProjection()
                  )
                );
                return;
              }
              var formatter = function (loc) {
                var props = [];
                ["toponymName", "adminName1", "countryName"].forEach(function (p) {
                  if (loc[p]) {
                    props.push(loc[p]);
                  }
                });
                return props.length == 0 ? "" : "—" + props.join(", ");
              };

              var url = gnViewerSettings.geocoder;
              $http
                .get(url, {
                  params: {
                    lang: lang,
                    style: "full",
                    type: "json",
                    maxRows: 10,
                    name_startsWith: query,
                    username: "georchestra"
                  }
                })
                .then(function (response) {
                  var loc;
                  scope.results = [];
                  for (var i = 0; i < response.data.geonames.length; i++) {
                    loc = response.data.geonames[i];
                    if (loc.bbox) {
                      scope.results.push({
                        name: loc.name,
                        formattedName: formatter(loc),
                        extent: ol.proj.transformExtent(
                          [loc.bbox.west, loc.bbox.south, loc.bbox.east, loc.bbox.north],
                          "EPSG:4326",
                          scope.map.getView().getProjection()
                        )
                      });
                    }
                  }
                });
            }
          };
        }
      ]
    };
  });
})();
