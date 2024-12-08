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
  goog.provide("gn_facet_directive");

  goog.require("gn_facets");

  var module = angular.module("gn_facet_directive", ["gn_facets"]);

  /**
   * All facet panel
   * @constructor
   */
  var FacetsController = function ($scope, $filter) {
    this.fLvlCollapse = {};
    this.currentFacet;
    this.$scope = $scope;
    this.$filter = $filter;

    $scope.$watch(
      function () {
        return this.list;
      }.bind(this),
      function (newValue) {
        if (!newValue) return;

        for (var i = 0; i < this.list.length; i++) {
          this.fLvlCollapse[this.list[i].key] = angular.isDefined(
            this.fLvlCollapse[this.list[i].key]
          )
            ? this.fLvlCollapse[this.list[i].key]
            : this.list[i].collapsed === true;
        }

        var lastFacet = this.lastUpdatedFacet;

        if (
          this._isNotNestedFacet(lastFacet) &&
          this.searchCtrl.hasFiltersForKey(lastFacet.path[0])
        ) {
          this.list.forEach(
            function (f) {
              if (f.key === lastFacet.key) {
                f.items = lastFacet.items;
              }
            }.bind(this)
          );
          this.lastUpdatedFacet = null;
        }
      }.bind(this)
    );
  };

  FacetsController.prototype.$onInit = function () {};

  FacetsController.prototype.collapseAll = function () {
    for (var i = 0; i < this.list.length; i++) {
      this.fLvlCollapse[this.list[i].key] = true;
    }
  };

  FacetsController.prototype.expandAll = function () {
    for (var i = 0; i < this.list.length; i++) {
      this.fLvlCollapse[this.list[i].key] = false;
    }
  };

  FacetsController.prototype.isVisibleForUser = function (facet) {
    var user = this.$scope.$parent.user;
    if (user && facet.userHasRole && user[facet.userHasRole]) {
      return user[facet.userHasRole]();
    } else {
      return facet.userHasRole ? false : true;
    }
  };

  FacetsController.prototype.loadMoreTerms = function (facet) {
    this.searchCtrl.loadMoreTerms(facet).then(function (terms) {
      angular.merge(facet, terms);
    });
  };

  FacetsController.prototype.loadLessTerms = function (facet) {
    this.searchCtrl.loadLessTerms(facet).then(function (terms) {
      angular.copy(terms, facet);
    });
  };

  FacetsController.prototype.filterTerms = function (facet) {
    if (facet.meta && facet.meta.filterByTranslation) {
      var match = [];
      if (!facet.originalItems) {
        facet.originalItems = facet.items;
      }
      if (facet.include === "") {
        facet.items = facet.originalItems;
        return;
      }
      for (var i = 0; i < facet.originalItems.length; i++) {
        var bucket = facet.originalItems[i],
          t = this.$filter("facetTranslator")(bucket.value, facet.key);
        if (t.match(new RegExp(facet.include, "i")) != null) {
          match.push(bucket);
        }
      }
      facet.items = match;
    } else {
      this.searchCtrl.filterTerms(facet).then(function (terms) {
        angular.merge(facet, terms);
        facet.items = terms.items;
      });
    }
  };

  FacetsController.prototype.filter = function (facet, item) {
    var value = !item.inverted;
    if (facet.type === "terms") {
      facet.include = "";
    } else if (facet.type === "filters" || facet.type === "histogram") {
      value = item.query_string.query_string.query;
      if (item.inverted) {
        value = "-(" + value + ")";
      }
    } else if (facet.type === "tree") {
    }
    this.searchCtrl.updateState(item.path, value);
  };

  FacetsController.prototype.onUpdateDateRange = function (facet, from, to) {
    var query_string =
      from === null && to === null
        ? ""
        : "+" + facet.key + ":[" + (from || "*") + " TO " + (to || "*") + "]";
    // this.$scope.$digest();
    this.searchCtrl.updateState(facet.path, query_string, true);
  };

  FacetsController.prototype._isNotNestedFacet = function (facet) {
    return facet && (facet.type === "terms" || facet.type === "tree") && !facet.aggs;
  };

  FacetsController.$inject = ["$scope", "$filter"];

  // Define the translation group key matching the facet key
  var facetKeyToTranslationGroupMap = new Map([
    ["isTemplate", "recordType"],
    ["groupOwner", "group"],
    ["groupPublishedId", "group"],
    ["sourceCatalogue", "source"]
  ]);

  module.service("gnFacetSorter", [
    "$filter",
    function ($filter) {
      this.sortByTranslation = function (agg, bucket) {
        if (agg && agg.meta && agg.meta.orderByTranslation) {
          return function (facet) {
            return $filter("facetTranslator")(facet.value || facet.key, bucket);
          };
        } else {
          return function (facet) {
            return facet.key;
          };
        }
      };
    }
  ]);

  module.service("gnFacetMetaLabel", [
    "$translate",
    function ($translate) {
      this.getFacetLabel = function (facet) {
        if (!facet || !facet.meta || !facet.meta.labels) {
          return null;
        }
        var currentLang = $translate.use();
        return facet.meta.labels[currentLang] || null;
      };
    }
  ]);

  module.filter("facetTooltip", [
    "$translate",
    "$filter",
    function ($translate, $filter) {
      return function (item) {
        if (item.definition) {
          var key = item.definition + "-tooltip",
            tooltip = $translate.instant(key);
          if (tooltip !== key) {
            return tooltip;
          }
        }
        return $filter("facetTranslator")(item.value);
      };
    }
  ]);

  module.filter("facetTranslator", [
    "$translate",
    "$filter",
    function ($translate, $filter) {
      return function (input, facetKey) {
        if (!input || angular.isObject(input)) {
          return input;
        }

        // Tree aggregation
        if (input.indexOf && input.indexOf("^") !== -1) {
          return input
            .split("^")
            .map(function (t) {
              return $translate.instant(t);
            })
            .join(" / ");
        }

        // A specific facet key eg. "isHarvested-true"
        var translationId =
            (facetKeyToTranslationGroupMap.get(facetKey) || facetKey) + "-" + input,
          translation = undefined;
        if (facetKey === "statusWorkflow") {
          translation = $filter("getStatusLabel")(input);
        } else {
          translation = $translate.instant(translationId);
        }
        if (translation !== translationId) {
          return translation;
        } else {
          // A common translations ?
          translation = $translate.instant(input);
          if (translation != input) {
            return translation;
          }
        }
        return input;
      };
    }
  ]);

  /**
   * Ignore object field suffix
   */
  module.filter("facetKeyTranslator", [
    "$translate",
    function ($translate) {
      return function (input) {
        return $translate.instant(
          input.replace(
            /(?:.key|.default|Object(?:.default|.lang[a-z]{3}(?:.keyword)?)?)$/,
            ""
          )
        );
      };
    }
  ]);

  module.directive("esFacets", [
    "gnFacetSorter",
    "gnSearchSettings",
    "gnFacetMetaLabel",
    function (gnFacetSorter, gnSearchSettings, gnFacetMetaLabel) {
      return {
        restrict: "A",
        controllerAs: "ctrl",
        controller: FacetsController,
        bindToController: true,
        scope: {
          list: "<esFacets",
          tabField: "="
        },
        require: {
          searchCtrl: "^^ngSearchForm"
        },
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/elasticsearch/directives/" + "partials/facets.html"
          );
        },
        link: function (scope, element, attrs) {
          // Applicaton tab field configured
          scope.appTabField = gnSearchSettings.facetTabField;
          // Directive tab field property
          scope.isTabMode = scope.ctrl.tabField !== undefined;
          scope.facetSorter = gnFacetSorter.sortByTranslation;
          scope.getFacetLabel = gnFacetMetaLabel.getFacetLabel;
        }
      };
    }
  ]);

  /**
   * One facet block
   * @param $scope
   * @constructor
   */
  var FacetController = function ($scope, $translate, $filter) {
    this.$scope = $scope;
    this.$translate = $translate;
    this.$filter = $filter;
  };

  FacetController.prototype.$onInit = function () {
    this.item.collapsed = true;
    if (this.facet.type === "tree") {
      this.item.path = [this.facet.key, this.item.key];
      this.item.collapsed = !this.searchCtrl.hasChildInSearch(this.item.path);
    } else {
      this.item.inverted = this.searchCtrl.isNegativeSearch(this.item.path);
    }
  };

  FacetController.prototype.filter = function (facet, item) {
    var value = !item.inverted;
    if (facet.type === "terms") {
      facet.include = "";
      if (!item.isNested) {
        this.facetsCtrl.lastUpdatedFacet = facet;
      }
    } else if (facet.type === "filters" || facet.type === "histogram") {
      value = item.query_string.query_string.query;
      if (item.inverted) {
        value = "-(" + value + ")";
      }
    } else if (facet.type === "tree") {
      this.facetsCtrl.lastUpdatedFacet = facet;
    }

    this.searchCtrl.updateState(item.path, value);
  };

  FacetController.prototype.isInSearch = function (facet, item) {
    return this.searchCtrl.isInSearch(item.path);
  };

  FacetController.prototype.toggleCollapse = function () {
    this.item.collapsed = !this.item.collapsed;
  };

  FacetController.prototype.toggleInvert = function () {
    var item = this.item;
    item.inverted = !item.inverted;
    this.filter(this.facet, item);
  };

  FacetController.$inject = ["$scope", "$translate", "$filter"];

  module.directive("esFacet", [
    "gnLangs",
    function (gnLangs) {
      return {
        restrict: "A",
        replace: true,
        controllerAs: "ctrl",
        controller: FacetController,
        bindToController: true,
        scope: {
          facet: "<esFacet",
          item: "<esFacetItem"
        },
        require: {
          facetsCtrl: "^^esFacets",
          facetCtrl: "?^^esFacet",
          searchCtrl: "^^ngSearchForm"
        },
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/elasticsearch/directives/" + "partials/facet.html"
          );
        },
        link: function (scope, element, attrs) {}
      };
    }
  ]);

  module.directive("esFacetDecorator", [
    function () {
      return {
        restrict: "A",
        replace: false,
        scope: {
          decorator: "=esFacetDecorator",
          key: "="
        },
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/elasticsearch/directives/" +
              "partials/facetDecorator.html"
          );
        },
        link: function (scope, element, attrs) {
          if (scope.decorator) {
            var key = scope.decorator.expression
              ? scope.key.replace(new RegExp(scope.decorator.expression), "$1")
              : scope.key;

            if (scope.decorator.map) {
              key = scope.decorator.map[key] || "";
            }

            if (scope.decorator.type == "img") {
              scope.ext =
                "image/" + (key.substr(key.lastIndexOf(".") + 1, key.length) || "png");
              if (scope.decorator.path) {
                key = scope.decorator.path.replace("{key}", key);
              }
            }

            scope.class = scope.decorator.prefix ? scope.decorator.prefix + key : key;
          }
        }
      };
    }
  ]);

  module.filter("facetBgUrlBuilder", [
    function () {
      return function (key, decorator) {
        if (decorator && decorator.path) {
          return "background-image:url('" + decorator.path.replace("{key}", key) + "')";
        } else if (decorator && decorator.map) {
          return "background-image:url('" + decorator.map[key] + "');";
        }
        return "";
      };
    }
  ]);

  module.filter("facetSearchUrlBuilder", [
    "gnGlobalSettings",
    "$filter",
    function (gnGlobalSettings, $filter) {
      return function (facetValue, key, response, config, missingValue) {
        var field = (response.meta && response.meta.field) || key,
          filter = config.filters
            ? config.filters.filters[facetValue].query_string.query
            : undefined,
          value = response.meta && response.meta.wildcard ? facetValue + "*" : facetValue;

        return (
          $filter("setUrlPlaceholder")(gnGlobalSettings.gnCfg.mods.search.appUrl) +
          '?query_string={"' +
          field +
          '": {"' +
          (value === missingValue ? "%23MISSING%23" : value) +
          '": ' +
          (filter ? '"' + filter + '"' : "true") +
          "}}"
        );
      };
    }
  ]);

  module.filter("facetCssClassCode", [
    function () {
      return function (key, isInspire) {
        if (key) {
          return isInspire
            ? key.slice(key.lastIndexOf("/") + 1)
            : key.replace("/", "").replace(" ", "");
        } else {
          return "";
        }
      };
    }
  ]);

  module.directive("esFacetCards", [
    "gnFacetSorter",
    "gnLangs",
    function (gnFacetSorter, gnLangs) {
      return {
        restrict: "A",
        scope: {
          key: "=esFacetCards",
          homeFacet: "=homeFacet",
          searchInfo: "=searchInfo"
        },
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/elasticsearch/directives/" +
              "partials/facet-cards.html"
          );
        },
        link: function (scope, element, attrs) {
          scope.iso2lang = gnLangs.getIso2Lang(gnLangs.getCurrent());

          function init() {
            scope.missingValue =
              scope.homeFacet.config[scope.key].terms &&
              scope.homeFacet.config[scope.key].terms.missing;
            scope.isInspire = scope.key.indexOf("th_httpinspireeceuropaeutheme") === 0;

            scope.aggregations = {};
            scope.homeFacet.facets.forEach(function (facet) {
              scope.aggregations[facet.key] = facet;
            });
          }

          init();

          scope.facetSorter = gnFacetSorter.sortByTranslation;
          scope.$watch("key", function (n, o) {
            if (n && n !== o) {
              init();
            }
          });
        }
      };
    }
  ]);

  module.service("gnFacetVegaService", function () {
    return {
      check: function () {
        try {
          vegaEmbed;
        } catch (e) {
          console.warn(
            "Interactive graphic for facet not available. Vega is not available.",
            e
          );
          return false;
        }
        return true;
      }
    };
  });

  module.directive("gnFacetTemporalrange", [
    "$timeout",
    "$translate",
    "gnFacetVegaService",
    function ($timeout, $translate, gnFacetVegaService) {
      return {
        restrict: "A",
        replace: true,
        templateUrl: function (elem, attrs) {
          return (
            "../../catalog/components/elasticsearch/directives/" +
            "partials/facet-temporalrange.html"
          );
        },
        scope: {
          facet: "<gnFacetTemporalrange",
          updateCallback: "&callback"
        },
        link: function (scope, element, attrs, controller) {
          if (!gnFacetVegaService.check()) {
            return;
          }

          scope.range = {
            from: null,
            to: null
          };
          scope.signal = null;

          scope.vl = null;
          scope.dateFormat =
            (scope.facet.meta && scope.facet.meta.dateFormat) || "DD-MM-YYYY";
          scope.vegaDateFormat =
            (scope.facet.meta && scope.facet.meta.vegaDateFormat) || "%d-%m-%Y";

          function moment2datePickerFormat(format) {
            // M > m, D > d, Y > y
            // https://momentjs.com/docs/#/displaying/
            // https://bootstrap-datepicker.readthedocs.io/en/latest/options.html#format
            return format.toLowerCase();
          }

          scope.dateRangeConfig = {
            maxViewMode: (scope.facet.meta && scope.facet.meta.dateSelectMode) || "days",
            minViewMode: (scope.facet.meta && scope.facet.meta.dateSelectMode) || "days",
            format: moment2datePickerFormat(scope.dateFormat)
          };
          scope.initialRange = angular.copy(scope.facet.items);

          function buildData() {
            angular.forEach(scope.initialRange, function (d) {
              d.type = "all";
              return d;
            });
            angular.forEach(scope.facet.items, function (d) {
              d.type = "current";
              return d;
            });
            var items = [].concat(scope.initialRange, scope.facet.items);
            return items;
          }
          // Assign the specification to a local variable vlSpec.
          var vlSpec = {
            $schema: "https://vega.github.io/schema/vega-lite/v4.json",
            datasets: {
              facetValues: buildData()
            },
            data: {
              name: "facetValues"
            },
            config: {
              axis: {
                domainColor: "#ddd",
                tickColor: "#ddd"
              }
            },
            vconcat: [
              {
                mark: {
                  type: (scope.facet.meta && scope.facet.meta.mark) || "bar",
                  cornerRadiusEnd: 2
                },
                height: 100,
                // selection: {
                //   pts: {type: "single"}
                // },
                encoding: {
                  x: {
                    field: "key",
                    type: "temporal",
                    timeunit: "milliseconds",
                    bin: {
                      maxbins: 30,
                      extent: {
                        selection: "brush"
                      }
                    },
                    axis: {
                      title: "",
                      labelExpr:
                        "[timeFormat(datum.value, '" + scope.vegaDateFormat + "')]"
                    }
                  },
                  y: {
                    field: "doc_count",
                    type: "quantitative",
                    stack: null,
                    axis: {
                      title: ""
                    }
                  },
                  color: {
                    scale: {
                      domain: ["all", "current"],
                      range: ["#ddd", "#3277B3"]
                    },
                    field: "type",
                    type: "nominal",
                    // condition: {
                    //   selection: "pts"
                    // },
                    // value: "grey",
                    legend: null
                  }
                }
              },
              {
                mark: "bar",
                height: 20,
                selection: {
                  brush: {
                    type: "interval",
                    encodings: ["x"]
                  }
                },
                encoding: {
                  color: {
                    scale: {
                      domain: ["all", "current"],
                      range: ["#ddd", "#3277B3"]
                    },
                    field: "type",
                    type: "nominal",
                    legend: null
                  },
                  x: {
                    field: "key",
                    type: "temporal",
                    timeunit: "milliseconds",
                    axis: {
                      title: $translate.instant("facets.temporalRange.seriesLegend"),
                      titleFontWeight: "normal",
                      titleFontSize: "8"
                    }
                  },
                  y: {
                    field: "doc_count",
                    type: "quantitative",
                    stack: null,
                    axis: {
                      title: ""
                    }
                  }
                }
              }
            ]
          };

          vegaEmbed("#" + scope.facet.key, vlSpec, {
            actions: false
          })
            .then(function (result) {
              scope.vl = result;

              scope.vl.view.addEventListener("click", function (event, item) {
                if (item && item.datum && item.datum.$$hashKey) {
                  // Avoid brush click
                  var vlId = item.datum.$$hashKey,
                    rangeItems = scope.vl.view
                      .data("facetValues")
                      .filter(function (e, i, a) {
                        return (
                          e.type === "current" &&
                          (e.$$hashKey === vlId ||
                            (a[i - 1] && a[i - 1].$$hashKey === vlId))
                        );
                      }, []),
                    selected = item.datum,
                    next = rangeItems[1];

                  var from = selected.key
                      ? moment(selected.key).format(scope.dateFormat)
                      : "*",
                    to =
                      next && next.key ? moment(next.key).format(scope.dateFormat) : "*";
                  $timeout(function () {
                    scope.range = {
                      from: from,
                      to: to
                    };
                  }, 10);
                }
              });

              scope.vl.view.addSignalListener("brush", function (signal, range) {
                if (scope.signal !== null) {
                  range.key = scope.signal.key;
                  scope.signal = null;
                  scope.vl.view.runAsync();
                  return;
                } else {
                  var from = range.key
                      ? moment(range.key[0]).format(scope.dateFormat)
                      : "*",
                    to = range.key ? moment(range.key[1]).format(scope.dateFormat) : "*";

                  if (scope.range.from != from || scope.range.to != to) {
                    $timeout(function () {
                      scope.range = {
                        from: range.key ? from : null,
                        to: range.key ? to : null
                      };
                    }, 10);
                  }
                }
              });

              scope.$watchCollection("facet.items", function (n, o) {
                scope.vl.view.data("facetValues", buildData()).run();
              });
            })
            .catch(console.error);

          scope.filter = function () {
            scope.updateCallback({
              facet: scope.facet,
              from: moment(scope.range.from, scope.dateFormat).toISOString(),
              to: moment(scope.range.to, scope.dateFormat).toISOString()
            });
          };

          scope.reset = function () {
            scope.range.from = undefined;
            scope.range.to = undefined;
          };

          scope.setRange = function () {
            scope.signal =
              (scope.range.from === undefined && scope.range.to === undefined) ||
              (scope.range.from === "" && scope.range.to === "")
                ? {}
                : {
                    key: [
                      moment(scope.range.from, scope.dateFormat).valueOf(),
                      moment(scope.range.to, scope.dateFormat).valueOf()
                    ],
                    update: false
                  };
            if (scope.vl) {
              scope.vl.view.signal("brush", scope.signal);
            }
          };

          scope.$watch("range.from", scope.setRange);
          scope.$watch("range.to", scope.setRange);
          scope.$on("resetSelection", scope.reset);
        }
      };
    }
  ]);

  module.directive("gnFacetVega", [
    "$timeout",
    "$filter",
    "gnFacetVegaService",
    function ($timeout, $filter, gnFacetVegaService) {
      return {
        restrict: "A",
        replace: true,
        templateUrl: function (elem, attrs) {
          return (
            "../../catalog/components/elasticsearch/directives/" +
            "partials/facet-vega.html"
          );
        },
        scope: {
          facet: "<gnFacetVega",
          updateCallback: "&callback"
        },
        link: function (scope, element, attrs, controller) {
          if (!gnFacetVegaService.check()) {
            return;
          }

          scope.signal = null;
          scope.vl = null;
          scope.id = scope.facet.key.replace(".", "");

          function buildLabel() {
            scope.facet.items.map(function (f) {
              f.label = $filter("facetTranslator")(f.value) + " (" + f.count + ")";
            });
          }

          buildLabel();

          var mark = { type: "arc", innerRadius: 50 };
          var encoding = {
            theta: { field: "count", type: "quantitative" },
            color: {
              field: "label",
              type: "nominal",
              legend: { title: "" }
              // scale: {scheme: 'category20b'}
            }
          };
          if (scope.facet.meta && scope.facet.meta.vega === "bar") {
            mark = "bar";
            encoding = {
              y: {
                field: "label",
                type: "nominal",
                axis: { labelAngle: 0, title: "" }
              },
              x: { field: "count", type: "quantitative", axis: { title: "" } }
            };
          }

          var vlSpec = {
            $schema: "https://vega.github.io/schema/vega-lite/v4.json",
            datasets: {
              facetValues: scope.facet.items
            },
            data: {
              name: "facetValues"
            },
            selection: {
              pts: { type: "single" }
            },
            config: {
              axis: {
                domainColor: "#ddd",
                tickColor: "#ddd"
              }
            },
            mark: mark,
            encoding: encoding,
            view: { stroke: null }
          };

          vegaEmbed("#" + scope.id, vlSpec, {
            actions: false
          })
            .then(function (result) {
              scope.vl = result;

              scope.vl.view.addEventListener("click", function (event, item) {
                if (item.datum && item.datum.$$hashKey) {
                  $timeout(function () {
                    scope.updateCallback({
                      facet: scope.facet,
                      item: item.datum
                    });
                  }, 10);
                }
              });

              scope.$watchCollection("facet.items", function (n, o) {
                buildLabel();
                scope.vl.view.data("facetValues", scope.facet.items).run();
              });
            })
            .catch(console.error);
        }
      };
    }
  ]);
})();
