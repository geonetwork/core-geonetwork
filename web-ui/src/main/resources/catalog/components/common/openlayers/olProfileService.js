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
  goog.provide("gn_olProfileService");

  var module = angular.module("gn_olProfileService", []);

  /**
   * Provides a D3js component to be used to draw an elevation
   * profile chart.
   *
   *     var selection = d3.select('#element_id');
   *     var profile = profileService({
   *       distanceExtractor: function (item) {return item['dist'];},
   *       linesConfiguration: {
   *         'lineZ1': {
   *           zExtractor: function (item) {return item['values']['z1'];}
   *         },
   *         'lineZ2': {
   *           color: '#00F',
   *           zExtractor: function (item) {return item['values']['z2'];}
   *         }
   *       },
   *       hoverCallback: function(point, dist, xUnits, elevations, yUnits) {
   *         console.log(point.x, point.y);
   *       },
   *       outCallback: function() {
   *         console.log("out");
   *       }
   *     });
   *     selection.datum(data).call(profile);
   *
   * The selection data must be an array.
   * The layout for the items of this array is unconstrained: the distance values
   * is extracted using the distanceExtractor config option and multiples z values
   * can be displayed by providing multiple linesConfiguration with its specific
   * zExtractor.
   * Optionally you can provide a color in your linesConfiguration. A line without
   * color will be red. Each linesConfiguration name is used as class for its
   * respective line. So you can pass a styleDefs config option (inline css) to
   * customize the line or all the chart.
   * Optionally, POIs can be displayed and depend on a poiExtractor
   * config option.
   *
   * The data below will work for the above example:
   *
   *     [
   *         {
   *             "y": 199340,
   *             "values": {"z1": 788.7, "z2": 774.2},
   *             "dist": 0.0,
   *             "x": 541620
   *         }, ...
   *     ]
   *
   * @constructor
   * @struct
   * @return {Object} D3js component.
   * @param {ngeox.profile.ProfileOptions} options Profile options.
   * @export
   */
  var profileService = function (options) {
    /**
     * Whether the simplified profile should be shown.
     * @type {boolean}
     */
    var light = options.light !== undefined ? options.light : false;

    /**
     * The values for margins around the chart defined in pixels.
     */
    var margin = light
      ? { top: 0, right: 0, bottom: 0, left: 0 }
      : { top: 10, right: 20, bottom: 30, left: 40 };

    /**
     * Hover callback function.
     * @type {function(Object, number, string, Object.<string, number>, string)}
     */
    var hoverCallback =
      options.hoverCallback !== undefined ? options.hoverCallback : null;

    /**
     * Out callback function.
     * @type {function()}
     */
    var outCallback = options.outCallback !== undefined ? options.outCallback : null;

    /**
     * Distance data extractor used to get the dist values.
     */
    var distanceExtractor = options.distanceExtractor;

    /**
     * Line configuration object.
     */
    var linesConfiguration = options.linesConfiguration;

    /**
     * Number of differents configurations for the line.
     */
    var numberOfLines = Object.keys(linesConfiguration).length;

    /**
     * Method to get the coordinate in pixels from a distance.
     */
    var bisectDistance = d3.bisector(function (d) {
      return distanceExtractor(d);
    }).left;

    /**
     * POI data extractor.
     */
    var poiExtractor = options.poiExtractor;

    /**
     * Optional SVG inline style.
     */
    var styleDefs = options.styleDefs;

    /**
     * @type {number}
     */
    var poiLabelAngle = options.poiLabelAngle !== undefined ? options.poiLabelAngle : -60;

    /**
     * @type {Object.<string, string>}
     */
    var i18n = options.i18n || {};

    /**
     * @type {string}
     */
    var xAxisLabel = i18n.xAxis || "Distance";

    /**
     * @type {string}
     */
    var yAxisLabel = i18n.yAxis || "Elevation";

    /**
     * @type {ngeox.profile.ProfileFormatter}
     */
    var formatter = {
      /**
       * @param {number} dist Distance.
       * @param {string} units Units.
       * @return {string} Distance.
       */
      xhover: function (dist, units) {
        return parseFloat(dist.toPrecision(3)) + " " + units;
      },
      /**
       * @param {number} ele Elevation.
       * @param {string} units Units.
       * @return {string} Elevation.
       */
      yhover: function (ele, units) {
        return Math.round(ele) + " m";
      },
      /**
       * @param {number} dist Distance.
       * @param {string} units Units.
       * @return {string|number} Distance.
       */
      xtick: function (dist, units) {
        return dist;
      },
      /**
       * @param {number} ele Elevation.
       * @param {string} units Units.
       * @return {string|number} Elevation.
       */
      ytick: function (ele, units) {
        return ele;
      }
    };

    if (options.formatter !== undefined) {
      object.assign(formatter, options.formatter);
    }

    /**
     * @type {boolean}
     */
    var lightXAxis = options.lightXAxis !== undefined ? options.lightXAxis : false;

    // Objects shared with the showPois function
    /**
     * @type {Object}
     */
    var svg;

    /**
     * D3 x scale.
     */
    var x;

    /**
     * D3 y scale.
     */
    var y;

    /**
     * Scale modifier to allow customizing the x and y scales.
     */
    var scaleModifier = options.scaleModifier;

    var g;

    /**
     * Height of the chart in pixels
     */
    var height;

    /**
     * Width of the chart in pixels
     */
    var width;

    /**
     * Factor to determine whether to use 'm' or 'km'.
     */
    var xFactor;

    /**
     * Distance units. Either 'm' or 'km'.
     */
    var xUnits;

    /**
     * D3 extent of the distance.
     */
    var xDomain;

    var profile = function (selection) {
      selection.each(function (data) {
        d3.select(this).selectAll("svg").remove();
        if (data === undefined) {
          return;
        }

        width = Math.max(this.clientWidth - margin.right - margin.left, 0);
        x = d3.scale.linear().range([0, width]);

        height = Math.max(this.clientHeight - margin.top - margin.bottom, 0);
        y = d3.scale.linear().range([height, 0]);

        var xAxis = d3.svg.axis().scale(x).orient("bottom");
        var yAxis = d3.svg.axis().scale(y).orient("left");

        var area;
        if (numberOfLines === 1) {
          area = d3.svg
            .area()
            .x(function (d) {
              return x(distanceExtractor(d));
            })
            .y0(height)
            .y1(function (d) {
              var firstLineName = Object.keys(linesConfiguration)[0];
              return y(linesConfiguration[firstLineName].zExtractor(d));
            });
        }

        // Select the svg element, if it exists.
        svg = d3.select(this).selectAll("svg").data([data]);

        // Otherwise, create the skeletal chart.
        var svgEnter = svg.enter().append("svg");
        if (styleDefs !== undefined) {
          svgEnter
            .append("defs")
            .append("style")
            .attr("type", "text/css")
            .text(styleDefs);
        }
        var gEnter = svgEnter.append("g");
        clearPois();

        gEnter.style("font", "11px Arial");

        if (numberOfLines === 1) {
          gEnter
            .append("path")
            .attr("class", "area")
            .style("fill", "rgba(222, 222, 222, 0.5)");
        }

        gEnter.insert("g", ":first-child").attr("class", "grid-y");

        if (!light) {
          gEnter
            .append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")");

          gEnter
            .append("text")
            .attr("class", "x label")
            .attr("text-anchor", "end")
            .attr("x", width - 4)
            .attr("y", height - 4);

          gEnter.append("g").attr("class", "y axis");

          gEnter
            .append("text")
            .attr("class", "y label")
            .attr("text-anchor", "end")
            .attr("y", 6)
            .attr("dy", ".75em")
            .attr("transform", "rotate(-90)")
            .style("fill", "grey")
            .text(yAxisLabel + " [m]");

          gEnter
            .append("g")
            .attr("class", "metas")
            .attr("transform", "translate(" + (width + 3) + ", 0)");
        }

        gEnter.append("g").attr("class", "pois");

        var xHover = gEnter.append("g").attr("class", "x grid-hover");
        xHover.append("svg:line").attr("stroke-dasharray", "5,5");
        xHover.append("text");

        gEnter
          .append("rect")
          .attr("class", "overlay")
          .attr("width", width)
          .attr("height", height)
          .style("fill", "none")
          .style("pointer-events", "all");

        // Update the outer dimensions.
        svg
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom);

        // Update the inner dimensions.
        g = svg
          .select("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        xDomain = d3.extent(data, function (d) {
          return distanceExtractor(d);
        });
        x.domain(xDomain);

        // Return an array with the min and max value of the min/max values of
        // each lines.
        var yDomain = (function () {
          var elevationsValues = [];
          var extent, name;
          // Get min/max values (extent) of each lines.
          for (name in linesConfiguration) {
            extent = d3.extent(data, function (d) {
              return linesConfiguration[name].zExtractor(d);
            });
            elevationsValues = elevationsValues.concat(extent);
          }
          return [
            Math.min.apply(null, elevationsValues),
            Math.max.apply(null, elevationsValues)
          ];
        })();

        y.domain(yDomain);

        // set the ratio according to the horizontal distance
        if (scaleModifier !== undefined) {
          scaleModifier(x, y, width, height);
        } else {
          // By default, add a small padding so that it looks nicer
          var padding = (yDomain[1] - yDomain[0]) * 0.1;
          y.domain([yDomain[0] - padding, yDomain[1] + padding]);
        }

        // Update the area path.
        if (numberOfLines === 1) {
          g.select(".area").transition().attr("d", area);
        }

        // Set style and update the lines paths and y hover guides for each lines.
        var line, name, yHover;
        for (name in linesConfiguration) {
          // Set style of each line and add a class with its respective name.
          gEnter
            .append("path")
            .attr("class", "line " + name)
            .style("stroke", linesConfiguration[name].color || "#F00")
            .style("fill", "none");

          // Set y hover guides
          yHover = gEnter.append("g").attr("class", "y grid-hover " + name);
          yHover.append("svg:line").attr("stroke-dasharray", "5,5");
          yHover.append("text");

          // Configure the d3 line.
          line = d3.svg
            .line()
            .x(function (d) {
              return x(distanceExtractor(d));
            })
            .y(function (d) {
              return y(linesConfiguration[name].zExtractor(d));
            });

          // Update path for the line.
          g.select(".line." + name)
            .transition()
            .attr("d", line);
        }

        if (xDomain[1] > 2000) {
          xFactor = 1000;
          xUnits = "km";
        } else {
          xFactor = 1;
          xUnits = "m";
        }

        if (!light) {
          xAxis.tickFormat(function (d) {
            return formatter.xtick(d / xFactor, xUnits);
          });
          if (lightXAxis) {
            xAxis.tickValues([0, x.domain()[1]]);
          }

          yAxis.tickFormat(function (d) {
            return formatter.ytick(d, "m");
          });

          g.select(".x.axis").transition().call(xAxis);

          g.select(".x.label")
            .text(xAxisLabel + " [" + xUnits + "]")
            .style("fill", "grey")
            .style("shape-rendering", "crispEdges");

          // Avoid too much lines with overlapping labels in small profiles
          if (height / 15 < 10) {
            yAxis.ticks(height / 15);
          }

          g.select(".y.axis").transition().call(yAxis);
        }

        g.select(".grid-y")
          .transition()
          .call(yAxis.tickSize(-width, 0).tickFormat(""))
          .selectAll(".tick line")
          .style("stroke", "#ccc")
          .style("opacity", 0.7);

        g.selectAll(".axis")
          .selectAll("path, line")
          .style("fill", "none")
          .style("stroke", "#000")
          .style("shape-rendering", "crispEdges");

        g.selectAll(".grid-hover line").style("stroke", "#222").style("opacity", 0.8);

        g.select(".overlay").on("mouseout", mouseout).on("mousemove", mousemove);

        function mousemove() {
          var mouseX = d3.mouse(this)[0];
          var x0 = x.invert(mouseX);

          profile.highlight(x0);
        }

        function mouseout() {
          profile.clearHighlight();
        }
      });
    };

    /**
     * Remove any highlight.
     * Fire the outCallback callback.
     */
    profile.clearHighlight = function () {
      g.selectAll(".grid-hover").style("display", "none");
      outCallback.call(null);
    };

    /**
     * Highlight the given distance and corresponding elevation on chart.
     * Fire the hoverCallback callback with corresponding point.
     * @param {number} distance Distance.
     */
    profile.highlight = function (distance) {
      var data = svg.datum();
      var i = bisectDistance(data, distance);
      if (i >= data.length) {
        return;
      }

      var point = data[i];
      var dist = distanceExtractor(point);
      var elevation;
      var elevations = [];
      var elevationsRef = {};
      var lineName;

      for (lineName in linesConfiguration) {
        elevation = linesConfiguration[lineName].zExtractor(point);
        elevations.push(elevation);
        elevationsRef[lineName] = elevation;
        g.select(".y.grid-hover." + lineName)
          .style("display", "inline")
          .select("line")
          .attr("x1", x(0))
          .attr("y1", y(elevation))
          .attr("x2", width)
          .attr("y2", y(elevation));
      }

      g.select(".x.grid-hover")
        .style("display", "inline")
        .select("line")
        .attr("x1", x(dist))
        .attr("y1", height)
        .attr("x2", x(dist))
        .attr("y2", y(Math.max.apply(null, elevations)));

      var right = dist > xDomain[1] / 2;
      var xtranslate = x(dist);
      xtranslate += right ? -10 : 10;

      g.select(".x.grid-hover text")
        .text(formatter.xhover(dist / xFactor, xUnits))
        .style("text-anchor", right ? "end" : "start")
        .attr("transform", "translate(" + xtranslate + "," + (height - 10) + ")");

      var yUnits = "m";
      // Display altitude on guides only if there is one line.
      if (numberOfLines === 1) {
        g.select(".y.grid-hover text")
          .text(formatter.yhover(elevations[0], "m"))
          .style("text-anchor", right ? "end" : "start")
          .attr(
            "transform",
            "translate(" + xtranslate + "," + (y(elevations[0]) - 10) + ")"
          );
      }
      hoverCallback.call(null, point, dist / xFactor, xUnits, elevationsRef, yUnits);
    };

    profile.showPois = function (pois) {
      pois = pois !== undefined ? pois : [];
      if (pois.length === 0 || poiExtractor !== undefined) {
        console.warn(
          "Will nos show Pois, because there are none and poiExtractor is Undefined"
        );
        return;
      }

      var pe = poiExtractor;
      var g = svg.select("g");
      var profileData = svg.datum();
      var ps = g.select(".pois");

      var p = ps.selectAll(".poi").data(pois, function (d) {
        var i = bisectDistance(profileData, Math.round(pe.dist(d) * 10) / 10, 1);
        var point = profileData[i];
        if (point) {
          var lineName;
          var elevations = [];
          for (lineName in linesConfiguration) {
            elevations.push(linesConfiguration[lineName].zExtractor(point));
          }
          var z = Math.max.apply(null, elevations);
          pe.z(d, z);
        }
        return pe.id(d);
      });

      var poiEnterG = p.enter().append("g").attr("class", "poi");

      poiEnterG
        .append("text")
        .attr("x", light ? 0 : 9)
        .attr("dy", ".35em")
        .attr("text-anchor", light ? "middle" : "start");

      poiEnterG.append("line").style("shape-rendering", "crispEdges");

      p.style("opacity", 0).transition().duration(1000).delay(100).style("opacity", 1);

      p.selectAll("text")
        .attr("transform", function (d) {
          if (light) {
            return ["translate(", x(pe.dist(d)), ",", y(pe.z(d)) - 10, ")"].join("");
          } else {
            return [
              "translate(",
              x(pe.dist(d)),
              ",",
              y(pe.z(d)) - 20,
              ") rotate(",
              poiLabelAngle,
              ")"
            ].join("");
          }
        })
        .text(function (d) {
          return pe.sort(d) + (light ? "" : " - " + pe.title(d));
        });

      p.selectAll("line")
        .style("stroke", "grey")
        .attr("x1", function (d) {
          return x(pe.dist(d));
        })
        .attr("y1", function (d) {
          return y(y.domain()[0]);
        })
        .attr("x2", function (d) {
          return x(pe.dist(d));
        })
        .attr("y2", function (d) {
          return y(pe.z(d));
        });

      // remove unused pois
      p.exit().remove();
    };

    function clearPois() {
      profile.showPois([]);
    }

    return profile;
  };

  module.value("olProfileService", profileService);
})();
