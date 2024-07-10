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
  goog.provide("gn_es_facet");

  var module = angular.module("gn_es_facet", []);

  var DEFAULT_SIZE = 10;

  module.service("gnESFacet", [
    "gnGlobalSettings",
    "gnFacetTree",
    "gnEsLanguageService",
    function (gnGlobalSettings, gnFacetTree, gnEsLanguageService) {
      var defaultSource = {
        includes: [
          "uuid",
          "id",
          "groupOwner",
          "logo",
          "cat",
          "inspireThemeUri",
          "inspireTheme_syn",
          "cl_topic",
          "resourceType",
          "resourceTitle*",
          "resourceAbstract*",
          "draft",
          "draftId",
          "owner",
          "link",
          "status*",
          "rating",
          "geom",
          "contact*",
          "Org*",
          "isTemplate",
          "valid",
          "isHarvested",
          "dateStamp",
          "documentStandard",
          "standardNameObject.default",
          "cl_status*",
          "mdStatus*"
        ]
      };
      var minimalSource = {
        includes: [
          "id",
          "uuid",
          "resourceTitle*",
          "resourceAbstract*",
          "resourceType",
          "cl_status*"
        ]
      };
      var defaultScriptedFields = {
        // Collect only first overview in search results.
        overview: {
          script: {
            source:
              "return params['_source'].overview == null ? [] : params['_source'].overview.stream().findFirst().orElse([]);"
          }
        }
      };
      this.getDefaultSize = function () {
        return DEFAULT_SIZE;
      };
      this.buildDefaultQuery = function (query, size) {
        return {
          script_fields: defaultScriptedFields,
          _source: minimalSource,
          size: size || 1,
          query: query || {}
        };
      };
      this.configs = {
        search: {
          facets: gnGlobalSettings.gnCfg.mods.search.facetConfig,
          source: defaultSource,
          script_fields: defaultScriptedFields,
          track_total_hits: true
        },
        home: {
          facets: {},
          source: {
            includes: [
              "id",
              "uuid",
              "creat*",
              "cl_topic*",
              "inspire*",
              "resource*",
              "image*",
              "tag*"
            ]
          },
          script_fields: defaultScriptedFields
        },
        recordWithLink: {
          facets: {},
          source: {
            includes: minimalSource.includes.concat("link")
          },
          script_fields: defaultScriptedFields
        },
        editor: {
          facets: gnGlobalSettings.gnCfg.mods.editor.facetConfig,
          source: {
            includes: [
              "id",
              "uuid",
              "creat*",
              "group*",
              "resource*",
              "draft*",
              "owner*",
              "recordOwner",
              "status*",
              "tag*",
              "isTemplate",
              "valid",
              "isHarvested",
              "dateStamp",
              "changeDate",
              "documentStandard",
              "mdStatus*",
              "*inspire*"
            ]
          },
          track_total_hits: true
        },
        harvester: {
          facets: gnGlobalSettings.gnCfg.mods.admin.facetConfig,
          source: {
            includes: ["id", "uuid", "resource*", "isTemplate", "valid", "index*"]
          },
          track_total_hits: true
        },
        directory: {
          facets: gnGlobalSettings.gnCfg.mods.directory.facetConfig,
          source: {
            includes: [
              "id",
              "uuid",
              "creat*",
              "group*",
              "resource*",
              "owner*",
              "recordOwner",
              "status*",
              "isTemplate",
              "valid",
              "isHarvested",
              "changeDate",
              "documentStandard"
            ]
          },
          track_total_hits: true
        },
        directoryInEditor: {
          facets: {
            groupPublished: {
              terms: {
                field: "groupPublished",
                size: 10
              }
            }
          },
          source: {
            includes: [
              "id",
              "uuid",
              "creat*",
              "group*",
              "resource*",
              "owner*",
              "isTemplate",
              "valid"
            ]
          },
          track_total_hits: true
        },
        simplelist: {
          facets: {},
          source: {
            includes: ["id", "uuid", "resource*", "link", "format", "cl_status.key"]
          },
          script_fields: defaultScriptedFields
        },
        recordsWithErrors: {
          facets: {
            indexingErrorType: {
              filters: {
                filters: {
                  errors: {
                    query_string: {
                      query: "-indexingErrorMsg.type:warning"
                    }
                  },
                  warning: {
                    query_string: {
                      query: "+indexingErrorMsg.type:warning"
                    }
                  }
                }
              }
            },
            isHarvested: {
              terms: {
                field: "isHarvested"
              }
            },
            indexingErrorMsg: {
              terms: {
                field: "indexingErrorMsg.string",
                size: 10
              }
            }
          },
          source: {
            includes: ["id", "uuid", "resource*", "index*"]
          },
          track_total_hits: true
        }
      };

      this.getMultilingualFacetFieldName = function (field, languageConfig) {
        if (field.indexOf("${") !== -1) {
          return gnEsLanguageService.injectLanguage(field, languageConfig, false);
        }
        return undefined;
      };

      this.addFacets = function (esParams, type, languageConfig) {
        var esFacet = this,
          aggs =
            typeof type === "string" ? angular.copy(this.configs[type].facets, {}) : type;

        esParams.aggregations = {};
        angular.forEach(aggs, function (config, facetName) {
          if (config.hasOwnProperty("gnBuildFilterForRange")) {
            esParams.aggregations[facetName] = esFacet.gnBuildFilterForRange(config);
          } else {
            if (config.terms) {
              var fieldName = esFacet.getMultilingualFacetFieldName(
                config.terms.field,
                languageConfig
              );
              // facetName = fieldName || facetName;
              config.terms.field = fieldName || config.terms.field;
              if (!config.meta) {
                config.meta = {};
              }
              config.meta.field = config.terms.field;
            }
            esParams.aggregations[facetName] = config;
          }
        });
      };

      this.gnBuildFilterForRange = function (facet) {
        var filters = {},
          config = facet.gnBuildFilterForRange,
          interval = (config.to - config.from) / config.buckets,
          isDate = config.dateFormat;

        for (var i = 0; i < config.buckets; i++) {
          var lower = parseInt(config.from + i * interval),
            upper = parseInt(config.from + (i + 1) * interval),
            key = isDate
              ? moment(lower, config.dateFormat).valueOf()
              : lower + "-" + upper;
          filters[key] = {
            query_string: {
              query: "+" + config.field + ":[" + lower + " TO " + upper + "}"
            }
          };
        }

        return {
          filters: {
            filters: filters
          },
          meta: angular.extend(facet.meta, config)
        };
      };

      this.addSourceConfiguration = function (esParams, type, templateSource) {
        if (type === undefined) {
          type = "simplelist";
        }
        var source =
          typeof type === "string" ? angular.copy(this.configs[type].source, {}) : type;
        if (templateSource) {
          if (templateSource.exclude) {
            source.includes = source.includes.filter(function (field) {
              return templateSource.exclude.indexOf(field) === -1;
            });
          }
        }
        esParams._source = source;
        if (this.configs[type].script_fields) {
          esParams.script_fields = this.configs[type].script_fields;
        }

        // By default limit to 10000.
        // Set to true will be a bit slower
        // See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html#request-body-search-track-total-hits
        if (this.configs[type].track_total_hits) {
          esParams.track_total_hits = this.configs[type].track_total_hits;
        }
      };

      this.getUIModel = function (response, request, configId) {
        var listModel;
        listModel = this.createFacetModel(
          request.aggregations,
          response.data.aggregations,
          undefined,
          undefined,
          configId
        );
        response.data.facets = listModel;
        return response.data;
      };

      this.createFacetModel = function (reqAggs, respAggs, isNested, path, configId) {
        var listModel = [];
        if (respAggs == undefined) {
          return;
        }
        for (var fieldId in reqAggs) {
          var respAgg = respAggs[fieldId];
          var reqAgg = reqAggs[fieldId];
          var fieldConfig =
            configId &&
            this.configs[configId].facets &&
            this.configs[configId].facets[fieldId];

          function facetHasProperty(configId, fieldId, propertyKey) {
            return fieldConfig && fieldConfig[propertyKey];
          }
          var searchFieldId =
            fieldConfig && fieldConfig.meta && fieldConfig.meta.field
              ? fieldConfig.meta.field
              : fieldId;
          var facetModel = {
            key: fieldId,
            userHasRole: fieldConfig && fieldConfig.meta && fieldConfig.meta.userHasRole,
            collapsed: fieldConfig && fieldConfig.meta && fieldConfig.meta.collapsed,
            meta: respAgg.meta,
            config: reqAgg,
            items: [],
            path: (path || []).concat([
              respAgg.meta && respAgg.meta.field ? respAgg.meta.field : searchFieldId
            ])
          };

          if (reqAgg.hasOwnProperty("terms")) {
            if (fieldId.contains("_tree")) {
              facetModel.type = "tree";
              facetModel.items = [];
              gnFacetTree
                .getTree(respAgg.buckets, fieldId, respAgg.meta, reqAgg.terms.missing)
                .then(
                  function (tree) {
                    this.items = tree.items;
                  }.bind(facetModel)
                );
            } else {
              facetModel.type = "terms";
              facetModel.size = reqAgg.terms.size;
              facetModel.more = respAgg.sum_other_doc_count > 0;
              facetModel.less =
                respAgg.buckets &&
                respAgg.buckets.length > Math.min(reqAgg.terms.size, DEFAULT_SIZE);
              facetModel.includeFilter = reqAgg.terms.include !== undefined;
              facetModel.excludeFilter = reqAgg.terms.exclude !== undefined;
              var esFacet = this;
              respAgg.buckets.forEach(function (bucket) {
                if (angular.isDefined(bucket.key)) {
                  var isWildcard =
                      fieldConfig && fieldConfig.meta && fieldConfig.meta.wildcard,
                    key = bucket.key_as_string || bucket.key,
                    isMissingValue = key === reqAgg.terms.missing,
                    itemPath = facetModel.path.concat([
                      isMissingValue
                        ? "#MISSING#"
                        : isWildcard
                        ? (key + "*").replace(" ", "\\\\ ")
                        : key + ""
                    ]);
                  var facet = {
                    value: key,
                    count: bucket.doc_count,
                    path: itemPath
                  };
                  // nesting
                  if (isNested) {
                    facet.isNested = true;
                  }
                  if (reqAgg.hasOwnProperty("aggs")) {
                    var nestAggs = {};
                    for (var indexKey in reqAgg.aggs) {
                      nestAggs[indexKey] = bucket[indexKey];
                    }
                    facet.aggs = esFacet.createFacetModel(
                      reqAgg.aggs,
                      nestAggs,
                      true,
                      itemPath
                    );
                  }
                  facetModel.items.push(facet);
                }
              });
            }
          } else if (
            reqAgg.hasOwnProperty("date_histogram") ||
            reqAgg.hasOwnProperty("auto_date_histogram") ||
            (respAgg.meta && respAgg.meta.vega == "timeline")
          ) {
            var isTimeline = respAgg.meta && respAgg.meta.vega == "timeline";

            facetModel.type = "dates";
            facetModel.dates = [];
            facetModel.datesCount = [];
            if (isTimeline) {
              facetModel.items = [];
              angular.forEach(respAgg.buckets, function (bucket, key) {
                if (bucket.doc_count !== 0) {
                  facetModel.items.push({
                    key_as_string: moment(parseInt(key)).toISOString(),
                    key: parseInt(key),
                    doc_count: bucket.doc_count
                  });
                }
              });
            } else {
              facetModel.items = respAgg.buckets;
            }
            // var dateInterval;
            // var dayDuration = 1000 * 60 * 60 * 24;
            // var intervals = {
            //   year: dayDuration * 365,
            //   month: dayDuration * 30,
            //   week: dayDuration * 7,
            //   day: dayDuration,
            //   s: 1000,
            //   m: 1000 * 60,
            //   h: 1000 * 60 * 24,
            //   d: dayDuration,
            //   M: dayDuration * 30,
            //   y: dayDuration * 365
            // };
            // if (reqAgg.hasOwnProperty('date_histogram')) {
            //   dateInterval = intervals[reqAgg.date_histogram.calendar_interval];
            // } else {
            //   var interval = respAgg.interval;
            //   var unit = interval.substring(interval.length - 1, interval.length);
            //   dateInterval = parseInt(interval.substring(0, interval.length - 1)) * intervals[unit];
            // }
            //
            // for (var p in respAgg.buckets) {
            //   if (!respAgg.buckets[p].key || !respAgg.buckets[p].doc_count) {
            //     continue;
            //   }
            //   facetModel.dates.push(new Date(respAgg.buckets[p].key));
            //   facetModel.datesCount.push({
            //     begin: new Date(respAgg.buckets[p].key),
            //     end: new Date(respAgg.buckets[p].key + dateInterval),
            //     count: respAgg.buckets[p].doc_count
            //   });
            // }
            //
            // if (respAgg.buckets.length > 1) {
            //   facetModel.from = moment(respAgg.buckets[0].key).format('DD-MM-YYYY');
            //   facetModel.to = moment(respAgg.buckets[respAgg.buckets.length - 1].key + dateInterval).format('DD-MM-YYYY');
            // }
          } else if (reqAgg.hasOwnProperty("filters")) {
            facetModel.type = "filters";
            facetModel.size = DEFAULT_SIZE;
            for (var p in respAgg.buckets) {
              facetModel.items.push({
                value: p,
                path: [fieldId, p],
                query_string: reqAgg.filters.filters[p],
                count: respAgg.buckets[p].doc_count
              });
            }
          } else if (reqAgg.hasOwnProperty("histogram")) {
            // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-histogram-aggregation.html
            facetModel.type = "histogram";
            facetModel.size = respAgg.buckets.size;
            if (angular.isDefined(reqAgg.histogram.keyed)) {
              var entries = Object.entries(respAgg.buckets);
              for (var p = 0; p < entries.length; p++) {
                var lowerBound = entries[p][1].key,
                  onlyOneBucket = entries.length === 1,
                  upperBound = onlyOneBucket
                    ? lowerBound + Number(reqAgg.histogram.interval)
                    : entries[p + 1]
                    ? entries[p + 1][1].key
                    : "*";

                facetModel.items.push({
                  value: lowerBound + "-" + upperBound,
                  path: [fieldId, lowerBound],
                  query_string: {
                    query_string: {
                      query:
                        "" +
                        reqAgg.histogram.field +
                        ":" +
                        "[" +
                        lowerBound +
                        " TO " +
                        upperBound +
                        "}"
                    }
                  },
                  count: entries[p][1].doc_count
                });
              }
            } else {
              console.warn(
                "Facet configuration error. Histogram are only supported with keyed mode.",
                "eg. creationYearForResource: {histogram: { " +
                  "field: 'creationYearForResource'," +
                  "interval: 5," +
                  "keyed: true," +
                  "min_doc_count: 1}}"
              );
            }
          }
          listModel.push(facetModel);
        }
        return listModel;
      };
    }
  ]);
})();
