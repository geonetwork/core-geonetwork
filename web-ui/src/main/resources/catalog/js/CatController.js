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
  goog.provide("gn_cat_controller");

  goog.require("gn_admin_menu");
  goog.require("gn_external_viewer");
  goog.require("gn_history");
  goog.require("gn_saved_selections");
  goog.require("gn_search_manager");
  goog.require("gn_session_service");
  goog.require("gn_alert");
  goog.require("gn_es");

  var module = angular.module("gn_cat_controller", [
    "gn_search_manager",
    "gn_session_service",
    "gn_admin_menu",
    "gn_saved_selections",
    "gn_external_viewer",
    "gn_history",
    "gn_alert",
    "gn_es"
  ]);

  module.constant("gnSearchSettings", {});
  module.constant("gnViewerSettings", {});
  module.constant(
    "gnGlobalSettings",
    (function () {
      var defaultConfig = {
        langDetector: {
          fromHtmlTag: false,
          regexp: "^(?:/.+)?/.+/([a-z]{2,3})/.+",
          default: "eng"
        },
        nodeDetector: {
          regexp: "^(?:/.+)?/(.+)/[a-z]{2,3}/.+",
          default: "srv"
        },
        serviceDetector: {
          regexp: "^(?:/.+)?/.+/[a-z]{2,3}/(.+)",
          default: "catalog.search"
        },
        baseURLDetector: {
          regexp: "^((?:/.+)?)+/.+/[a-z]{2,3}/.+",
          default: "/geonetwork"
        },
        mods: {
          global: {
            humanizeDates: true,
            dateFormat: "DD-MM-YYYY",
            timezone: "Browser" // Default to browser timezone
          },
          footer: {
            enabled: true,
            showSocialBarInFooter: true,
            showApplicationInfoAndLinksInFooter: true,
            footerCustomMenu: [], // List of static pages identifiers to display
            rssFeeds: [
              {
                // List of rss feeds links to display when the OGC API Records service is enabled
                url: "f=rss&sortby=-createDate&limit=30",
                label: "lastCreatedRecords"
              }
              // , {
              //   url: "f=rss&sortby=-publicationDateForResource&limit=30",
              //   label: "lastPublishedRecords"
              // }
            ]
          },
          header: {
            enabled: true,
            languages: {
              eng: "en",
              cat: "ca",
              chi: "zh",
              cze: "cs",
              dan: "da",
              ger: "de",
              fre: "fr",
              spa: "es",
              ice: "is",
              ita: "it",
              dut: "nl",
              kor: "ko",
              por: "pt",
              rus: "ru",
              slo: "sk",
              fin: "fi",
              swe: "sv",
              wel: "cy"
            },
            isLogoInHeader: false,
            logoInHeaderPosition: "left",
            fluidHeaderLayout: true,
            showGNName: true,
            isHeaderFixed: false,
            showPortalSwitcher: true,
            topCustomMenu: [] // List of static pages identifiers to display
          },
          cookieWarning: {
            enabled: true,
            cookieWarningMoreInfoLink: "",
            cookieWarningRejectLink: ""
          },
          home: {
            enabled: true,
            appUrl: "../../{{node}}/{{lang}}/catalog.search#/home",
            showSocialBarInFooter: true,
            showMosaic: true,
            showMaps: true,
            facetConfig: {
              "th_httpinspireeceuropaeutheme-theme_tree.key": {
                terms: {
                  field: "th_httpinspireeceuropaeutheme-theme_tree.key",
                  size: 34
                  // "order" : { "_key" : "asc" }
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-2x pull-left gn-icon iti-",
                    expression: "http://inspire.ec.europa.eu/theme/(.*)"
                  },
                  orderByTranslation: true
                }
              },
              "cl_topic.key": {
                terms: {
                  field: "cl_topic.key",
                  size: 20
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-2x pull-left gn-icon-"
                  },
                  orderByTranslation: true
                }
              },
              // 'OrgForResource': {
              //   'terms': {
              //     'field': 'OrgForResourceObject',
              //     'include': '.*',
              //     'missing': '- No org -',
              //     'size': 15
              //   }
              // },
              resourceType: {
                terms: {
                  field: "resourceType",
                  size: 10
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-2x pull-left gn-icon-"
                  }
                }
              }
            },
            fluidLayout: true
          },
          search: {
            enabled: true,
            appUrl: "../../{{node}}/{{lang}}/catalog.search#/search",
            hitsperpageValues: [30, 60, 120],
            paginationInfo: {
              hitsPerPage: 30
            },
            // Full text on all fields
            // 'queryBase': '${any}',
            // Full text but more boost on title match
            // * Search in languages depending on the strategy selected
            queryBase:
              'any.${searchLang}:(${any}) OR any.common:(${any}) OR resourceTitleObject.${searchLang}:(${any})^2 OR resourceTitleObject.\\*:"${any}"^6',
            queryBaseOptions: {
              default_operator: "AND"
            },
            // TODO: Exact match should not even analyze
            // so we could create an exact field not analyzed in the index maybe?
            queryExactMatch:
              'any.${searchLang}:"${any})" OR any.common:"${any}" OR resourceTitleObject.\\*:"${any}"^2',
            // * Force UI language - in this case set languageStrategy to searchInUILanguage
            // and disable language options in searchOptions
            // 'queryBase': 'any.${uiLang}:(${any}) any.common:(${any}) resourceTitleObject.${uiLang}:(${any})^2',
            // * Search in French fields (with french analysis)
            // 'queryBase': 'any.langfre:(${any}) any.common:(${any}) resourceTitleObject.langfre:(${any})^2',
            queryTitle: "resourceTitleObject.\\*:(${any})",
            queryTitleExactMatch: 'resourceTitleObject.\\*:"${any}"',
            searchOptions: {
              fullText: true,
              titleOnly: true,
              exactMatch: true,
              language: true
            },
            // The language strategy define how to search on multilingual content.
            // It also applies to aggregation using ${aggLanguage} substitute.
            // Language strategy can be:
            // * searchInUILanguage: search in UI languages
            // eg. full text field is any.langfre if French, aggLanguage is uiLanguage.
            // * searchInAllLanguages: search using any.* fields, aggLanguage is default
            // (no analysis is done, more records are returned)
            // * searchInDetectedLanguage: restrict the search to the language detected
            // based on user search. aggLanguage is detectedLanguage.
            // If language detection fails, search in all languages and aggLanguage is uiLanguage
            // * searchInThatLanguage: Force a language using searchInThatLanguage:fre
            // 'languageStrategy': 'searchInThatLanguage:fre',
            // aggLanguage is forcedLanguage.
            languageStrategy: "searchInAllLanguages",
            // Limit language detection to some languages only.
            // If empty, the list of languages in catalogue records is used
            // and if none found, mods.header.languages is used.
            languageWhitelist: [],
            // Score query may depend on where we are in the app?
            scoreConfig: {
              // Score experiments:
              // a)Score down old records
              // {
              //   "gauss": {
              //     "dateStamp": {
              //       "scale":  "200d"
              //     }
              //   }
              // }
              // b)Promote grids!
              // "boost": "5",
              // "functions": [
              //   {
              //     "filter": { "match": { "cl_spatialRepresentationType.key": "vector" } },
              //     "random_score": {},
              //     "weight": 23
              //   },
              //   {
              //     "filter": { "match": { "cl_spatialRepresentationType.key": "grid" } },
              //     "weight": 42
              //   }
              // ],
              // "max_boost": 42,
              // "score_mode": "max",
              // "boost_mode": "multiply",
              // "min_score" : 42
              // "script_score" : {
              //   "script" : {
              //     "source": "_score"
              //     // "source": "Math.log(2 + doc['rating'].value)"
              //   }
              // }
              boost: "5",
              functions: [
                {
                  filter: { match: { resourceType: "series" } },
                  weight: 1.5
                },
                // Boost down member of a series
                {
                  filter: { exists: { field: "parentUuid" } },
                  weight: 0.3
                },
                // Boost down obsolete and superseded records
                {
                  filter: { match: { "cl_status.key": "obsolete" } },
                  weight: 0.2
                },
                {
                  filter: { match: { "cl_status.key": "superseded" } },
                  weight: 0.3
                },
                // {
                //   "filter": { "match": { "cl_resourceScope": "service" } },
                //   "weight": 0.8
                // },
                // Start boosting down records more than 3 months old
                {
                  gauss: {
                    changeDate: {
                      scale: "365d",
                      offset: "90d",
                      decay: 0.5
                    }
                  }
                }
              ],
              score_mode: "multiply"
            },
            autocompleteConfig: {
              query: {
                bool: {
                  must: [
                    {
                      multi_match: {
                        query: "",
                        type: "bool_prefix",
                        fields: [
                          "resourceTitleObject.${searchLang}^6",
                          "resourceAbstractObject.${searchLang}^.5",
                          "tag",
                          "uuid",
                          "resourceIdentifier"
                          // "anytext",
                          // "anytext._2gram",
                          // "anytext._3gram"
                        ]
                      }
                    }
                  ]
                }
              },
              _source: ["resourceTitle*", "resourceType"],
              size: 20
            },
            moreLikeThisSameType: true,
            moreLikeThisConfig: {
              more_like_this: {
                fields: [
                  "resourceTitleObject.default",
                  "resourceAbstractObject.default",
                  "tag.raw"
                ],
                like: null,
                min_term_freq: 1,
                min_word_length: 3,
                max_query_terms: 35,
                // "analyzer": "english",
                minimum_should_match: "70%"
              }
            },
            facetTabField: "",
            // Enable vega only if using vega facet type
            // See https://github.com/geonetwork/core-geonetwork/pull/5349
            isVegaEnabled: true,
            facetConfig: {
              resourceType: {
                terms: {
                  field: "resourceType"
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-fw gn-icon-"
                  }
                }
              },
              // Use .default for not multilingual catalogue with one language only UI.
              // 'cl_spatialRepresentationType.default': {
              //   'terms': {
              //     'field': 'cl_spatialRepresentationType.default',
              //     'size': 10
              //   }
              // },
              // Use .key for codelist for multilingual catalogue.
              // The codelist translation needs to be loaded in the client app. See GnSearchModule.js
              "cl_spatialRepresentationType.key": {
                terms: {
                  field: "cl_spatialRepresentationType.key",
                  size: 10
                }
              },
              format: {
                terms: {
                  field: "format"
                },
                meta: {
                  collapsed: true
                }
              },
              availableInServices: {
                filters: {
                  //"other_bucket_key": "others",
                  // But does not support to click on it
                  filters: {
                    availableInViewService: {
                      query_string: {
                        query: "+linkProtocol:/OGC:WMS.*/"
                      }
                    },
                    availableInDownloadService: {
                      query_string: {
                        query: "+linkProtocol:/OGC:WFS.*/"
                      }
                    }
                  }
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-fw ",
                    map: {
                      availableInViewService: "fa-globe",
                      availableInDownloadService: "fa-download"
                    }
                  }
                }
              },
              // GEMET configuration for non multilingual catalog
              "th_gemet_tree.default": {
                terms: {
                  field: "th_gemet_tree.default",
                  size: 100,
                  order: { _key: "asc" },
                  include: "[^^]+^?[^^]+"
                  // Limit to 2 levels
                }
              },
              // GEMET configuration for multilingual catalog
              // The key is translated on client side by loading
              // required concepts
              // 'th_gemet_tree.key': {
              //   'terms': {
              //     'field': 'th_gemet_tree.key',
              //     'size': 100,
              //     "order" : { "_key" : "asc" },
              //     "include": "[^\^]+^?[^\^]+"
              //     // Limit to 2 levels
              //   }
              // },
              // (Experimental) A tree field which contains a URI
              // eg. http://www.ifremer.fr/thesaurus/sextant/theme#52
              // but with a translation which contains a hierarchy with a custom separator
              // /Regulation and Management/Technical and Management Zonations/Sensitive Zones
              // 'th_sextant-theme_tree.key': {
              //   'terms': {
              //     'field': 'th_sextant-theme_tree.key',
              //     'size': 100,
              //     "order" : { "_key" : "asc" }
              //   },
              //   'meta': {
              //     'translateOnLoad': true,
              //     'treeKeySeparator': '/'
              //   }
              // },

              "th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default":
                {
                  terms: {
                    field:
                      "th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default",
                    size: 100,
                    order: { _key: "asc" }
                  }
                },
              "th_httpinspireeceuropaeutheme-theme_tree.key": {
                terms: {
                  field: "th_httpinspireeceuropaeutheme-theme_tree.key",
                  size: 34
                  // "order" : { "_key" : "asc" }
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-fw gn-icon iti-",
                    expression: "http://inspire.ec.europa.eu/theme/(.*)"
                  }
                }
              },
              tag: {
                terms: {
                  field: "tag.${aggLang}",
                  include: ".*",
                  size: 10
                },
                meta: {
                  caseInsensitiveInclude: true
                }
              },
              "th_regions_tree.default": {
                terms: {
                  field: "th_regions_tree.default",
                  size: 100,
                  order: { _key: "asc" }
                  //"include": "EEA.*"
                }
              },
              // "resolutionScaleDenominator": {
              //   "terms": {
              //     "field": "resolutionScaleDenominator",
              //     "size": 20,
              //     "order": {
              //       "_key": "asc"
              //     }
              //   }
              // },
              resolutionScaleDenominator: {
                histogram: {
                  field: "resolutionScaleDenominator",
                  interval: 10000,
                  keyed: true,
                  min_doc_count: 1
                },
                meta: {
                  collapsed: true
                }
              },
              // "serviceType": {
              //   'collapsed': true,
              //   "terms": {
              //     "field": "serviceType",
              //     "size": 10
              //   }
              // },
              // "resourceTemporalDateRange": {
              //   "date_histogram": {
              //     "field": "resourceTemporalDateRange",
              //     "fixed_interval": "1900d",
              //     "min_doc_count": 1
              //   }
              // },
              creationYearForResource: {
                histogram: {
                  field: "creationYearForResource",
                  interval: 5,
                  keyed: true,
                  min_doc_count: 1
                },
                meta: {
                  collapsed: true
                }
              },
              // "creationYearForResource": {
              //   "terms": {
              //     "field": "creationYearForResource",
              //     "size": 10,
              //     "order": {
              //       "_key": "desc"
              //     }
              //   }
              // },
              OrgForResource: {
                terms: {
                  field: "OrgForResourceObject.${aggLang}",
                  // field: "OrgForResourceObject.default",
                  // field: "OrgForResourceObject.langfre",
                  include: ".*",
                  size: 20
                },
                meta: {
                  // Always display filter even no more elements
                  // This can be used when all facet values are loaded
                  // with a large size and you want to provide filtering.
                  // 'displayFilter': true,
                  caseInsensitiveInclude: true
                  // decorator: {
                  //   type: 'img',
                  //   map: {
                  //     'EEA': 'https://upload.wikimedia.org/wikipedia/en/thumb/7/79/EEA_agency_logo.svg/220px-EEA_agency_logo.svg.png'
                  //   }
                  // }
                }
              },
              "cl_maintenanceAndUpdateFrequency.key": {
                terms: {
                  field: "cl_maintenanceAndUpdateFrequency.key",
                  size: 10
                },
                meta: {
                  collapsed: true
                }
                // },
                // Don't forget to enable Vega to use interactive graphic facets.
                // See isVegaEnabled property.
                // 'cl_status.key': {
                //   'terms': {
                //     'field': 'cl_status.key',
                //     'size': 10
                //   },
                //   'meta': {
                //     // 'vega': 'bar'
                //     'vega': 'arc'
                //   }
                // },
                //
                // 'resourceTemporalDateRange': {
                //   'gnBuildFilterForRange': {
                //     field: "resourceTemporalDateRange",
                //     buckets: 2021 - 1970,
                //     dateFormat: 'YYYY',
                //     dateSelectMode: 'years',
                //     vegaDateFormat: '%Y',
                //     from: 1970,
                //     to: 2021,
                //     mark: 'area'
                //   },
                //   'meta': {
                //     'vega': 'timeline'
                //   }
                // },
                // 'dateStamp' : {
                //   'auto_date_histogram' : {
                //     'field' : 'dateStamp',
                //     'buckets': 50
                //   },
                //   "meta": {
                //     'userHasRole': 'isReviewerOrMore',
                //     'collapsed': true
                //   }
              }
            },
            filters: null,
            // 'filters': [{
            //     "query_string": {
            //       "query": "-resourceType:service"
            //     }
            //   }],
            sortbyValues: [
              {
                sortBy: "relevance",
                sortOrder: ""
              },
              {
                sortBy: "changeDate",
                sortOrder: "desc"
              },
              {
                sortBy: "createDate",
                sortOrder: "desc"
              },
              {
                sortBy: "resourceTitleObject.default.sort",
                sortOrder: ""
              },
              {
                sortBy: "rating",
                sortOrder: "desc"
              },
              {
                sortBy: "popularity",
                sortOrder: "desc"
              }
            ],
            sortBy: "relevance",
            resultViewTpls: [
              {
                tplUrl:
                  "../../catalog/components/" +
                  "search/resultsview/partials/viewtemplates/grid.html",
                tooltip: "Grid",
                icon: "fa-th",
                related: []
              },
              {
                tplUrl:
                  "../../catalog/components/" +
                  "search/resultsview/partials/viewtemplates/list.html",
                tooltip: "List",
                icon: "fa-bars",
                related: ["parent", "children", "services", "datasets"]
              },
              {
                tplUrl:
                  "../../catalog/components/" +
                  "search/resultsview/partials/viewtemplates/table.html",
                tooltip: "Table",
                icon: "fa-table",
                related: [],
                source: {
                  exclude: ["resourceAbstract*", "Org*", "contact*"]
                }
              }
            ],
            // Optional. If not set, the first resultViewTpls is used.
            resultTemplate:
              "../../catalog/components/" +
              "search/resultsview/partials/viewtemplates/grid.html",
            searchResultContact: "OrgForResource",
            formatter: {
              list: [
                {
                  label: "defaultView",
                  // Conditional views can be used to configure custom
                  // formatter to use depending on metadata properties.
                  // 'views': [{
                  //   'if': {'standardName': 'ISO 19115-3 - Emodnet Checkpoint - Targeted Data Product'},
                  //   'url' : '/formatters/xsl-view?root=div&view=advanced'
                  // }, {
                  //   'if': {
                  //     'standardName': [
                  //       'ISO 19115:2003/19139 - EMODNET - BATHYMETRY',
                  //       'ISO 19115:2003/19139 - EMODNET - HYDROGRAPHY']
                  //   },
                  //   'url' : '/formatters/xsl-view?root=div&view=emodnetHydrography'
                  // }, {
                  //   'if': {'documentStandard': 'iso19115-3.2018'},
                  //   'url' : '/dada'
                  // }],
                  url: ""
                },
                {
                  label: "full",
                  url: "/formatters/xsl-view?root=div&view=advanced"
                }
              ]
            },
            downloadFormatter: [
              {
                label: "exportMEF",
                url: "/formatters/zip?withRelated=false",
                class: "fa-file-zip-o"
              },
              {
                label: "exportPDF",
                url: "/formatters/xsl-view?output=pdf&language=${lang}",
                class: "fa-file-pdf-o"
              },
              {
                label: "exportXML",
                // 'url' : '/formatters/xml?attachment=false',
                url: "/formatters/xml",
                class: "fa-file-code-o"
              } /*,
              {
                label: "exportDCAT",
                url: "/geonetwork/api/collections/main/items/${uuid}?f=dcat",
                class: "fa-file-code-o"
              }*/
            ],
            // Deprecated (use configuration on resultViewTpls)
            grid: {
              related: ["parent", "children", "services", "datasets"]
            },
            linkTypes: {
              links: ["LINK"],
              downloads: ["WWW:DOWNLOAD", "WWW:OPENDAP", "WWW:FTP", "KML"],
              // 'downloadServices': [
              //   'OGC:WFS',
              //   'OGC:WCS',
              //   'ATOM'
              // ],
              layers: [
                "OGC:WMS",
                // 'OGC:WFS',
                "OGC:WMTS",
                "ESRI:REST"
              ],
              maps: ["ows"]
            },
            isFilterTagsDisplayedInSearch: true,
            searchMapPlacement: "results", // results, facets or none
            showStatusFooterFor: "historicalArchive,obsolete,superseded",
            showBatchDropdown: true,
            usersearches: {
              enabled: false,
              includePortals: true,
              displayFeaturedSearchesPanel: false
            },
            savedSelection: {
              enabled: false
            },
            addWMSLayersToMap: {
              urlLayerParam: ""
            }
          },
          map: {
            enabled: true,
            appUrl: "../../{{node}}/{{lang}}/catalog.search#/map",
            externalViewer: {
              enabled: false,
              enabledViewAction: false,
              baseUrl: "http://www.example.com/viewer",
              urlTemplate:
                "http://www.example.com/viewer?url=${service.url}&type=${service.type}&layer=${service.title}&lang=${iso2lang}&title=${md.defaultTitle}",
              openNewWindow: false,
              valuesSeparator: ","
            },
            is3DModeAllowed: false,
            singleTileWMS: true,
            isSaveMapInCatalogAllowed: true,
            isExportMapAsImageEnabled: false,
            isAccessible: false,
            storage: "sessionStorage",
            bingKey: "",
            listOfServices: {
              wms: [],
              wmts: [],
              wps: []
            },
            // wpsSource: ["list", "url", "recent"],
            wpsSource: ["url", "recent"],
            projection: "EPSG:3857",
            projectionList: [
              {
                code: "urn:ogc:def:crs:EPSG:6.6:4326",
                label: "WGS84 (EPSG:4326)"
              },
              {
                code: "EPSG:3857",
                label: "Google mercator (EPSG:3857)"
              }
            ],
            switcherProjectionList: [
              {
                code: "EPSG:3857",
                label: "Google mercator (EPSG:3857)"
              }
            ],
            disabledTools: {
              processes: false,
              addLayers: false,
              projectionSwitcher: false,
              layers: false,
              legend: false,
              filter: false,
              contexts: false,
              print: false,
              mInteraction: false,
              graticule: false,
              mousePosition: true,
              syncAllLayers: false,
              drawVector: false
            },
            defaultTool: "layers",
            defaultToolAfterMapLoad: "layers",
            graticuleOgcService: {},
            "map-viewer": {
              context: "../../map/config-viewer.xml",
              extent: [0, 0, 0, 0],
              layers: []
            },
            "map-search": {
              context: "../../map/config-viewer.xml",
              extent: [0, 0, 0, 0],
              layers: [],
              geodesicExtents: false
            },
            "map-editor": {
              context: "",
              extent: [0, 0, 0, 0],
              layers: [{ type: "osm" }]
            },
            "map-thumbnail": {
              context: "../../map/config-viewer.xml",
              extent: [0, 0, 0, 0],
              layers: []
            },
            autoFitOnLayer: false
          },
          geocoder: {
            enabled: true,
            appUrl: "https://secure.geonames.org/searchJSON"
          },
          recordview: {
            isSocialbarEnabled: true,
            showStatusWatermarkFor: "",
            showStatusTopBarFor: "",
            showCitation: {
              enabled: false,
              // if: {'documentStandard': ['iso19115-3.2018']}
              if: { resourceType: ["series", "dataset", "nonGeographicDataset"] }
            },
            sortKeywordsAlphabetically: true,
            mainThesaurus: ["th_gemet", "th_gemet-theme"],
            locationThesaurus: [
              "th_regions",
              "th_httpinspireeceuropaeumetadatacodelistSpatialScope-SpatialScope"
            ],
            internalThesaurus: [],
            collectionTableConfig: {
              labels: "title,cl_status,format,download,WMS,WFS,Atom,Links",
              columns:
                "resourceTitle,cl_status[0].key,format,link/protocol:WWW:DOWNLOAD.*,link/protocol:OGC:WMS,link/protocol:OGC:WFS,link/protocol:atom:feed,link/protocol:WWW:LINK.*"
            },
            distributionConfig: {
              // 'layout': 'tabset',
              layout: "",
              sections: [
                {
                  filter:
                    "protocol:OGC:WMS|OGC:WMTS|ESRI:.*|atom.*|REST|OGC API Maps|OGC API Records",
                  title: "API"
                },
                {
                  filter:
                    "protocol:OGC:WFS|OGC:WCS|.*DOWNLOAD.*|DB:.*|FILE:.*|OGC API Features|OGC API Coverages",
                  title: "download"
                },
                { filter: "function:legend", title: "mapLegend" },
                {
                  filter: "function:featureCatalogue",
                  title: "featureCatalog"
                },
                {
                  filter: "function:dataQualityReport",
                  title: "quality"
                },
                {
                  filter:
                    "-protocol:OGC.*|REST|ESRI:.*|atom.*|.*DOWNLOAD.*|DB:.*|FILE:.* AND -function:legend|featureCatalogue|dataQualityReport",
                  title: "links"
                }
              ]
            },
            relatedFacetConfig: {
              cl_status: {
                terms: {
                  field: "cl_status.default",
                  order: { _key: "asc" }
                }
              },
              creationYearForResource: {
                terms: {
                  field: "creationYearForResource",
                  size: 100,
                  order: { _key: "asc" }
                }
              },
              cl_spatialRepresentationType: {
                terms: {
                  field: "cl_spatialRepresentationType.default",
                  order: { _key: "asc" }
                }
              },
              format: {
                terms: {
                  field: "format",
                  order: { _key: "asc" }
                }
              }
            }
          },
          editor: {
            enabled: true,
            appUrl: "../../{{node}}/{{lang}}/catalog.edit",
            isUserRecordsOnly: false,
            minUserProfileToCreateTemplate: "",
            isFilterTagsDisplayed: false,
            fluidEditorLayout: true,
            createPageTpl: "../../catalog/templates/editor/new-metadata-horizontal.html",
            editorIndentType: "",
            allowRemoteRecordLink: true,
            facetConfig: {
              resourceType: {
                terms: {
                  field: "resourceType"
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-fw gn-icon-"
                  }
                }
              },
              mdStatus: {
                terms: {
                  field: "statusWorkflow",
                  size: 20
                },
                meta: {
                  field: "statusWorkflow"
                }
              },
              "cl_status.key": {
                terms: {
                  field: "cl_status.key",
                  size: 15
                }
              },
              valid: {
                terms: {
                  field: "valid",
                  size: 10
                }
              },
              valid_inspire: {
                terms: {
                  field: "valid_inspire",
                  size: 10
                },
                meta: {
                  collapsed: true
                }
              },
              sourceCatalogue: {
                terms: {
                  field: "sourceCatalogue",
                  size: 100,
                  include: ".*"
                },
                meta: {
                  orderByTranslation: true,
                  filterByTranslation: true,
                  displayFilter: true,
                  collapsed: true
                  // decorator: {
                  //   type: "img",
                  //   path: "../../images/logos/{key}.png"
                  // }
                }
              },
              groupOwner: {
                terms: {
                  field: "groupOwner",
                  size: 200,
                  include: ".*"
                },
                meta: {
                  orderByTranslation: true,
                  filterByTranslation: true,
                  displayFilter: true,
                  collapsed: true
                }
              },
              recordOwner: {
                terms: {
                  field: "recordOwner",
                  size: 5,
                  include: ".*"
                },
                meta: {
                  collapsed: true
                }
              },
              isPublishedToAll: {
                terms: {
                  field: "isPublishedToAll",
                  size: 2
                },
                meta: {
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-fw ",
                    map: {
                      false: "fa-lock",
                      true: "fa-lock-open"
                    }
                  }
                }
              },
              groupPublishedId: {
                terms: {
                  field: "groupPublishedId",
                  size: 200,
                  include: ".*"
                },
                meta: {
                  orderByTranslation: true,
                  filterByTranslation: true,
                  displayFilter: true,
                  collapsed: true
                }
              },
              documentStandard: {
                terms: {
                  field: "documentStandard",
                  size: 10
                },
                meta: {
                  collapsed: true
                }
              },
              isHarvested: {
                terms: {
                  field: "isHarvested",
                  size: 2
                },
                meta: {
                  collapsed: true,
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-fw ",
                    map: {
                      false: "fa-folder",
                      true: "fa-cloud"
                    }
                  }
                }
              },
              isTemplate: {
                terms: {
                  field: "isTemplate",
                  size: 5
                },
                meta: {
                  collapsed: true,
                  decorator: {
                    type: "icon",
                    prefix: "fa fa-fw ",
                    map: {
                      n: "fa-file-text",
                      y: "fa-file"
                    }
                  }
                }
              }
            }
          },
          directory: {
            sortbyValues: [
              {
                sortBy: "relevance",
                sortOrder: ""
              },
              {
                sortBy: "changeDate",
                sortOrder: "desc"
              },
              {
                sortBy: "resourceTitleObject.default.sort",
                sortOrder: ""
              },
              {
                sortBy: "recordOwner",
                sortOrder: ""
              },
              {
                sortBy: "valid",
                sortOrder: "desc"
              }
            ],
            sortBy: "relevance",
            facetConfig: {
              valid: {
                terms: {
                  field: "valid",
                  size: 10
                }
              },
              groupOwner: {
                terms: {
                  field: "groupOwner",
                  size: 10
                }
              },
              recordOwner: {
                terms: {
                  field: "recordOwner",
                  size: 10
                }
              },
              groupPublished: {
                terms: {
                  field: "groupPublished",
                  size: 10
                }
              },
              isHarvested: {
                terms: {
                  field: "isHarvested",
                  size: 2
                }
              }
            },
            // Add some fuzziness when search on directory entries
            // but boost exact match.
            queryBase:
              'any.${searchLang}:(${any}) OR any.common:(${any}) OR resourceTitleObject.${searchLang}:"${any}"^10 OR resourceTitleObject.${searchLang}:(${any})^5 OR resourceTitleObject.${searchLang}:(${any}~2)'
          },
          admin: {
            enabled: true,
            appUrl: "../../{{node}}/{{lang}}/admin.console",
            facetConfig: {
              availableInServices: {
                filters: {
                  //"other_bucket_key": "others",
                  // But does not support to click on it
                  filters: {
                    availableInViewService: {
                      query_string: {
                        query: "+linkProtocol:/OGC:WMS.*/"
                      }
                    },
                    availableInDownloadService: {
                      query_string: {
                        query: "+linkProtocol:/OGC:WFS.*/"
                      }
                    }
                  }
                }
              },
              resourceType: {
                terms: {
                  field: "resourceType"
                },
                meta: {
                  vega: "arc"
                }
              },
              "tag.default": {
                terms: {
                  field: "tag.default",
                  size: 10
                },
                meta: {
                  vega: "arc"
                }
              },
              indexingErrorMsg: {
                terms: {
                  field: "indexingErrorMsg",
                  size: 12
                }
              }
            }
          },
          authentication: {
            enabled: true,
            signinUrl: "../../{{node}}/{{lang}}/catalog.signin",
            signoutUrl: "../../signout"
          },
          page: {
            enabled: true,
            appUrl: "../../{{node}}/{{lang}}/catalog.search#/page"
          },
          workflowHelper: {
            enabled: false,
            workflowAssistApps: [{ appUrl: "", appLabelKey: "" }]
          }
        }
      };

      return {
        proxyUrl: "",
        locale: {},
        isMapViewerEnabled: false,
        requireProxy: [],
        gnCfg: angular.copy(defaultConfig),
        gnUrl: "",
        docUrl: "https://docs.geonetwork-opensource.org/latest/{lang}",
        //docUrl: '../../doc/',
        modelOptions: {
          updateOn: "default blur",
          debounce: {
            default: 300,
            blur: 0
          }
        },
        stopKeyList: [
          "langDetector",
          "nodeDetector",
          "serviceDetector",
          "baseURLDetector",
          "languages",
          "languageWhitelist",
          "hitsperpageValues",
          "sortbyValues",
          "wpsSource",
          "resultViewTpls",
          "formatter",
          "downloadFormatter",
          "related",
          "linkTypes",
          "usersearches",
          "savedSelection",
          "listOfServices",
          "showCitation",
          "externalViewer",
          "map-viewer",
          "map-search",
          "map-editor",
          "map-thumbnail",
          "projectionList",
          "switcherProjectionList",
          "cookieWarning",
          "facetConfig",
          "searchOptions",
          "graticuleOgcService",
          "geocoder",
          "disabledTools",
          "filters",
          "scoreConfig",
          "autocompleteConfig",
          "moreLikeThisConfig",
          "relatedFacetConfig",
          "mainThesaurus",
          "internalThesaurus",
          "locationThesaurus",
          "distributionConfig",
          "collectionTableConfig",
          "queryBaseOptions",
          "workflowAssistApps"
        ],
        current: null,
        isDisableLoginForm: false,
        isShowLoginAsLink: false,
        isUserProfileUpdateEnabled: true,
        isUserGroupUpdateEnabled: true,
        init: function (
          configOverlay,
          gnUrl,
          nodeUrl,
          gnViewerSettings,
          gnSearchSettings
        ) {
          this.applyConfig(configOverlay !== null ? configOverlay : {});
          this.setLegacyOption(gnViewerSettings, gnSearchSettings);
          this.gnUrl = gnUrl || "../";
          this.nodeUrl = nodeUrl || "../";
          this.proxyUrl = this.gnUrl + "../proxy?url=";
        },
        setLegacyOption: function (gnViewerSettings, gnSearchSettings) {
          gnViewerSettings.mapConfig = this.gnCfg.mods.map;
          angular.extend(gnSearchSettings, this.gnCfg.mods.search);
          this.isMapViewerEnabled = this.gnCfg.mods.map.enabled;
          gnViewerSettings.bingKey = this.gnCfg.mods.map.bingKey;
          gnViewerSettings.defaultContext =
            gnViewerSettings.mapConfig["map-viewer"].context;
          gnViewerSettings.geocoder =
            this.gnCfg.mods.geocoder.appUrl || defaultConfig.mods.geocoder.appUrl;

          // Map protocols used to load layers/services in the map viewer
          gnSearchSettings.mapProtocols = {
            layers: [
              "OGC:WMS",
              "OGC:3DTILES",
              "OGC:COG",
              "OGC:WMTS",
              "OGC:WMS-1.1.1-http-get-map",
              "OGC:WMS-1.3.0-http-get-map",
              "OGC:WFS",
              "ESRI:REST"
            ],
            services: [
              "OGC:WMS-1.3.0-http-get-capabilities",
              "OGC:WMS-1.1.1-http-get-capabilities",
              "OGC:WMTS-1.0.0-http-get-capabilities",
              "OGC:WFS-1.0.0-http-get-capabilities"
            ]
          };
        },
        getObjectKeysPaths: function (obj, stopKeyList, allLevels, prefix) {
          var keys = Object.keys(obj);
          var that = this;
          prefix = prefix ? prefix + "." : "";
          return keys.reduce(function (result, key) {
            if (
              angular.isObject(obj[key]) &&
              Object.keys(obj[key]).length > 0 &&
              (stopKeyList === undefined ||
                (stopKeyList && stopKeyList.indexOf(key) === -1))
            ) {
              if (allLevels) {
                result.push(prefix + key);
              }
              result = result.concat(
                that.getObjectKeysPaths(obj[key], stopKeyList, allLevels, prefix + key)
              );
            } else if (key !== "mods") {
              result.push(prefix + key);
            }
            return result;
          }, []);
        },
        deleteValueByPath: function (obj, path) {
          var i;
          path = path.split(".");
          for (i = 0; i < path.length - 1; i++) obj = obj[path[i]];

          delete obj[path[i]];
          return obj;
        },
        applyConfig: function (configOverlay, runAllChecks) {
          var pathToUpdate = this.getObjectKeysPaths(
            configOverlay,
            this.stopKeyList,
            false
          );
          for (var i = 0; i < pathToUpdate.length; i++) {
            var p = pathToUpdate[i],
              o = _.get(configOverlay, p);
            if (o !== undefined) {
              _.set(this.gnCfg, p, o);
            }
            if (runAllChecks) {
              var optionInDefaultConfig = _.get(defaultConfig, p);
              if (optionInDefaultConfig === undefined) {
                console.warn(
                  "Path " +
                    p +
                    " not found in default configuration. Check your custom configuration.",
                  config
                );
              }
            }
          }
        },
        cleanConfig: function (config) {
          var pathToClean = this.getObjectKeysPaths(
            defaultConfig,
            this.stopKeyList,
            true
          );
          for (var i = 0; i < pathToClean.length; i++) {
            var p = pathToClean[i],
              optionInDefault = _.get(defaultConfig, p),
              option = _.get(config, p);
            if (
              option !== undefined &&
              JSON.stringify(option) === JSON.stringify(optionInDefault)
            ) {
              this.deleteValueByPath(config, p);
            }
          }
          var pathToRemove = this.getObjectKeysPaths(config, this.stopKeyList, true);
          for (var i = 0; i < pathToRemove.length; i++) {
            var p = pathToRemove[i],
              pathToken = p.split("."),
              option = _.get(config, p);
            if (angular.isObject(option) && Object.keys(option).length === 0) {
              var key = pathToken.pop();
              var parent = _.get(config, pathToken.join("."));
              delete parent[key];
            }
          }
          return config;
        },
        getDefaultConfig: function () {
          return angular.copy(defaultConfig);
        },
        getProxyUrl: function () {
          return this.proxyUrl;
        },
        getDefaultResultTemplate: function () {
          if (this.gnCfg.mods.search.resultTemplate) {
            for (var i = 0; i < this.gnCfg.mods.search.resultViewTpls.length; i++) {
              if (
                this.gnCfg.mods.search.resultViewTpls[i].tplUrl ==
                this.gnCfg.mods.search.resultTemplate
              ) {
                return this.gnCfg.mods.search.resultViewTpls[i];
              }
            }
          }
          return this.gnCfg.mods.search.resultViewTpls[0];
        },
        // Removes the proxy path and decodes the layer url,
        // so the layer can be printed with MapFish.
        // Otherwise Mapfish rejects it, due to relative url.
        getNonProxifiedUrl: function (url) {
          if (url.indexOf(this.proxyUrl) > -1) {
            return decodeURIComponent(url.replace(this.proxyUrl, ""));
          } else {
            return url;
          }
        }
      };
    })()
  );

  module.constant("gnLangs", {
    langs: {},
    current: null,
    detectLang: function (detector, gnGlobalSettings) {
      // If already detected
      if (gnGlobalSettings.iso3lang) {
        return gnGlobalSettings.iso3lang;
      }

      var iso2lang, iso3lang;

      // Init language list
      this.langs = gnGlobalSettings.gnCfg.mods.header.languages;

      // Detect language from HTML lang tag, regex on URL
      if (detector) {
        if (detector.fromHtmlTag) {
          iso2lang = $("html").attr("lang").substr(0, 2);
        } else if (detector.regexp) {
          var res = new RegExp(detector.regexp).exec(location.pathname);
          if (angular.isArray(res)) {
            var urlLang = res[1];
            if (this.isValidIso2Lang(urlLang)) {
              iso2lang = urlLang;
            } else if (this.isValidIso3Lang(urlLang)) {
              iso2lang = this.getIso2Lang(urlLang);
            } else {
              console.warn("URL lang '" + urlLang + "' is not a valid language code.");
            }
          }
        } else if (detector.default) {
          iso2lang = detector.default;
        }
        iso3lang = this.getIso3Lang(iso2lang || detector.default);
      }
      this.current = iso3lang || "eng";

      // Set locale to global settings. This is
      // used by locale loader.
      gnGlobalSettings.iso3lang = this.current;
      gnGlobalSettings.lang = this.getIso2Lang(this.current);
      gnGlobalSettings.locale = {
        iso3lang: this.current
      };
      return this.current;
    },
    getCurrent: function () {
      return this.current;
    },
    isValidIso3Lang: function (lang) {
      return angular.isDefined(this.langs[lang]);
    },
    isValidIso2Lang: function (lang) {
      for (var p in this.langs) {
        if (this.langs[p] === lang) {
          return true;
        }
      }
      return false;
    },
    getIso2Lang: function (iso3lang) {
      return this.langs[iso3lang] || "en";
    },
    getIso3Lang: function (iso2lang) {
      for (var p in this.langs) {
        if (this.langs[p] === iso2lang) {
          return p;
        }
      }
      return "eng";
    }
  });

  /**
   * The catalogue controller takes care of
   * loading site information, check user login state
   * and a facet search to get main site information.
   *
   * A body-level scope makes sense for example:
   *
   *  <body ng-controller="GnCatController">
   */
  module.controller("GnCatController", [
    "$scope",
    "$http",
    "$q",
    "$rootScope",
    "$translate",
    "gnSearchManagerService",
    "gnConfigService",
    "gnConfig",
    "gnGlobalSettings",
    "$location",
    "gnUtilityService",
    "gnSessionService",
    "gnLangs",
    "gnAdminMenu",
    "gnViewerSettings",
    "gnSearchSettings",
    "$cookies",
    "gnExternalViewer",
    "gnAlertService",
    "gnESFacet",
    function (
      $scope,
      $http,
      $q,
      $rootScope,
      $translate,
      gnSearchManagerService,
      gnConfigService,
      gnConfig,
      gnGlobalSettings,
      $location,
      gnUtilityService,
      gnSessionService,
      gnLangs,
      gnAdminMenu,
      gnViewerSettings,
      gnSearchSettings,
      $cookies,
      gnExternalViewer,
      gnAlertService,
      gnESFacet
    ) {
      $scope.version = "0.0.1";
      var defaultNode = "srv";

      // Links for social media
      $scope.socialMediaLink = $location.absUrl();
      $scope.getPermalink = gnUtilityService.displayPermalink;
      $scope.fluidEditorLayout = gnGlobalSettings.gnCfg.mods.editor.fluidEditorLayout;
      $scope.fluidHeaderLayout = gnGlobalSettings.gnCfg.mods.header.fluidHeaderLayout;
      $scope.showGNName = gnGlobalSettings.gnCfg.mods.header.showGNName;
      $scope.isHeaderFixed = gnGlobalSettings.gnCfg.mods.header.isHeaderFixed;
      $scope.isLogoInHeader = gnGlobalSettings.gnCfg.mods.header.isLogoInHeader;
      $scope.isFooterEnabled = gnGlobalSettings.gnCfg.mods.footer.enabled;

      // If gnLangs current already set by config, do not use URL
      $scope.langs = gnGlobalSettings.gnCfg.mods.header.languages;
      $scope.lang = gnLangs.detectLang(null, gnGlobalSettings);
      $scope.iso2lang = gnLangs.getIso2Lang($scope.lang);

      $scope.rssFeeds = gnGlobalSettings.gnCfg.mods.footer.rssFeeds;

      $scope.getSocialLinksVisible = function () {
        var onMdView = $location.absUrl().indexOf("/metadata/") > -1;
        return !onMdView && gnGlobalSettings.gnCfg.mods.footer.showSocialBarInFooter;
      };

      $scope.getApplicationInfoVisible = function () {
        return gnGlobalSettings.gnCfg.mods.footer.showApplicationInfoAndLinksInFooter;
      };

      function detectNode(detector) {
        if (detector.regexp) {
          var res = new RegExp(detector.regexp).exec(location.pathname);
          if (angular.isArray(res)) {
            return res[1];
          }
        }
        return detector.default || defaultNode;
      }

      function detectService(detector) {
        if (detector.regexp) {
          var res = new RegExp(detector.regexp).exec(location.pathname);
          if (angular.isArray(res)) {
            return res[1];
          }
        }
        return detector.default;
      }

      function detectBaseURL(detector) {
        if (detector.regexp) {
          var res = new RegExp(detector.regexp).exec(location.pathname);
          if (angular.isArray(res)) {
            return res[1];
          }
        }
        return detector.default || "geonetwork";
      }
      $scope.nodeId = detectNode(gnGlobalSettings.gnCfg.nodeDetector);
      $scope.isDefaultNode = $scope.nodeId === defaultNode;
      $scope.service = detectService(gnGlobalSettings.gnCfg.serviceDetector);
      $scope.redirectUrlAfterSign = window.location.href;

      gnGlobalSettings.nodeId = $scope.nodeId;
      gnGlobalSettings.isDefaultNode = $scope.isDefaultNode;
      gnConfig.env = gnConfig.env || {};
      gnConfig.env.node = $scope.nodeId;
      gnConfig.env.defaultNode = defaultNode;
      gnConfig.env.baseURL = detectBaseURL(gnGlobalSettings.gnCfg.baseURLDetector);

      $scope.signoutUrl =
        gnGlobalSettings.gnCfg.mods.authentication.signoutUrl +
        "?redirectUrl=" +
        window.location.href.slice(
          0,
          window.location.href.indexOf(gnConfig.env.node) + gnConfig.env.node.length
        );

      // Lang names to be displayed in language selector
      $scope.langLabels = {
        eng: "English",
        dut: "Nederlands",
        fre: "Français",
        ger: "Deutsch",
        kor: "한국의",
        spa: "Español",
        por: "Portuguesa",
        cat: "Català",
        cze: "Czech",
        ita: "Italiano",
        fin: "Suomeksi",
        ice: "Íslenska",
        rus: "русский",
        chi: "中文",
        slo: "Slovenčina",
        swe: "Svenska",
        dan: "Dansk",
        wel: "Cymraeg"
      };
      $scope.url = "";
      $scope.gnUrl = gnGlobalSettings.gnUrl;
      $scope.gnCfg = gnGlobalSettings.gnCfg;
      $scope.proxyUrl = gnGlobalSettings.proxyUrl;
      $scope.logoPath = gnGlobalSettings.gnUrl + "../images/harvesting/";
      $scope.isMapViewerEnabled = gnGlobalSettings.isMapViewerEnabled;
      $scope.isDebug = window.location.search.indexOf("debug") !== -1;
      $scope.isDisableLoginForm = gnGlobalSettings.isDisableLoginForm;
      $scope.isShowLoginAsLink = gnGlobalSettings.isShowLoginAsLink;
      $scope.isUserProfileUpdateEnabled = gnGlobalSettings.isUserProfileUpdateEnabled;
      $scope.isUserGroupUpdateEnabled = gnGlobalSettings.isUserGroupUpdateEnabled;
      $scope.isExternalViewerEnabled = gnExternalViewer.isEnabled();
      $scope.externalViewerUrl = gnExternalViewer.getBaseUrl();
      $scope.publicationOptions = [];

      $http.get("../api/records/sharing/options").then(function (response) {
        $scope.publicationOptions = response.data;
      });

      $scope.isSelfRegisterPossible = function () {
        return gnConfig["system.userSelfRegistration.enable"];
      };

      $scope.isHostDefined = function () {
        return gnConfig["system.feedback.mailServer.hostIsDefined"];
      };

      $scope.layout = {
        hideTopToolBar: false
      };

      /**
       * CSRF support
       */

      //Comment the following lines if you want to remove csrf support
      $http.defaults.xsrfHeaderName = "X-XSRF-TOKEN";
      $http.defaults.xsrfCookieName = "XSRF-TOKEN";
      $scope.$watch(
        function () {
          return $cookies.get($http.defaults.xsrfCookieName);
        },
        function (value) {
          $rootScope.csrf = value;
        }
      );
      //If no csrf, ask for one:
      if (!$rootScope.csrf) {
        $http.get("../api/me");
      }
      //Comment the upper lines if you want to remove csrf support

      /**
       * Number of selected metadata records.
       * Only one selection per session is allowed.
       */
      $scope.selectedRecordsCount = 0;

      /**
       * An ordered list of profiles
       */
      $scope.profiles = [
        "RegisteredUser",
        "Editor",
        "Reviewer",
        "UserAdmin",
        "Administrator"
      ];
      $scope.info = {};
      $scope.user = {};
      $rootScope.user = $scope.user;
      $scope.authenticated = false;
      $scope.initialized = false;

      /**
       * Keep a reference on main cat scope
       * @return {*}
       */
      $scope.getCatScope = function () {
        return $scope;
      };

      gnConfigService.load().then(function (c) {
        // Config loaded
        if (proj4 && angular.isArray(gnConfig["map.proj4js"])) {
          angular.forEach(gnConfig["map.proj4js"], function (item) {
            proj4.defs(item.code, item.value);
          });
          ol.proj.proj4.register(proj4);
        }
      });

      // login url for inline signin form in top toolbar
      $scope.signInFormAction = "../../signin#" + $location.url();

      // when the login input have focus, do not close the dropdown/popup
      $scope.focusLoginPopup = function () {
        $(".signin-dropdown #inputUsername, .signin-dropdown #inputPassword").one(
          "focus",
          function () {
            $(this).parents(".dropdown-menu").addClass("show");
          }
        );
        $(".signin-dropdown #inputUsername, .signin-dropdown #inputPassword").one(
          "blur",
          function () {
            $(this).parents(".dropdown-menu").removeClass("show");
          }
        );
      };

      /**
       * Catalog facet summary providing
       * a global overview of the catalog content.
       */
      $scope.searchInfo = {};

      var defaultStatus = {
        title: "",
        link: "",
        msg: "",
        error: "",
        type: "info",
        timeout: -1
      };

      $scope.loadCatalogInfo = function () {
        var promiseStart = $q.when("start");

        // Retrieve site information
        // TODO: Add INSPIRE, harvester, ... information
        var catInfo = promiseStart.then(function (value) {
          return $http.get("../api/site").then(
            function (response) {
              $scope.info = response.data;
              // Add the last time catalog info where updated.
              // That could be useful to append to catalog image URL
              // in order to trigger a reload of the logo when info are
              // reloaded.
              $scope.info["system/site/lastUpdate"] = new Date().getTime();
              $scope.initialized = true;
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                id: "catalogueStatus",
                title: $translate.instant("somethingWrong"),
                msg: $translate.instant("msgNoCatalogInfo"),
                type: "danger"
              });
            }
          );
        });

        // Utility functions for user
        var userFn = {
          isAnonymous: function () {
            return angular.isUndefined(this);
          },
          isConnected: function () {
            return !this.isAnonymous();
          },
          canCreateTemplate: function () {
            var profile =
                gnGlobalSettings.gnCfg.mods.editor.minUserProfileToCreateTemplate || "",
              fnName =
                profile !== ""
                  ? "is" + profile[0].toUpperCase() + profile.substring(1) + "OrMore"
                  : "";
            return angular.isFunction(this[fnName]) ? this[fnName]() : this.isConnected();
          },
          canImportMetadata: function () {
            var profile = gnConfig["metadata.import.userprofile"] || "Editor",
              fnName =
                profile !== ""
                  ? "is" + profile[0].toUpperCase() + profile.substring(1) + "OrMore"
                  : "";
            return angular.isFunction(this[fnName]) ? this[fnName]() : false;
          },
          canBatchEditMetadata: function () {
            var profile = gnConfig["metadata.batchediting.accesslevel"] || "Editor",
              fnName =
                profile !== ""
                  ? "is" + profile[0].toUpperCase() + profile.substring(1) + "OrMore"
                  : "";
            return angular.isFunction(this[fnName]) ? this[fnName]() : false;
          },
          canViewMetadataHistory: function () {
            var profile = gnConfig["metadata.history.accesslevel"] || "Editor",
              fnName =
                profile !== ""
                  ? "is" + profile[0].toUpperCase() + profile.substring(1) + "OrMore"
                  : "";
            if (profile === "RegisteredUser") {
              return true;
            }
            return angular.isFunction(this[fnName]) ? this[fnName]() : false;
          },
          canDeletePublishedMetadata: function () {
            var profile =
                gnConfig["metadata.delete.profilePublishedMetadata"] || "Editor",
              fnName =
                profile !== ""
                  ? "is" + profile[0].toUpperCase() + profile.substring(1) + "OrMore"
                  : "";
            return angular.isFunction(this[fnName]) ? this[fnName]() : false;
          },
          canPublishMetadata: function () {
            var profile =
                gnConfig["metadata.publication.profilePublishMetadata"] || "Reviewer",
              fnName =
                profile !== ""
                  ? "is" + profile[0].toUpperCase() + profile.substring(1) + "OrMore"
                  : "";
            return angular.isFunction(this[fnName]) ? this[fnName]() : false;
          },
          canUnpublishMetadata: function () {
            var profile =
                gnConfig["metadata.publication.profileUnpublishMetadata"] || "Reviewer",
              fnName =
                profile !== ""
                  ? "is" + profile[0].toUpperCase() + profile.substring(1) + "OrMore"
                  : "";
            return angular.isFunction(this[fnName]) ? this[fnName]() : false;
          },
          // The md provide the information about
          // if the current user can edit records or not
          // based on record operation allowed. See edit property.
          canEditRecord: function (md) {
            if (!md || this.isAnonymous()) {
              return false;
            }

            // A second filter is for harvested record
            // if the catalogue admin defined that those
            // records could be harvested.
            if (md.isHarvested && JSON.parse(md.isHarvested) == true) {
              return gnConfig["system.harvester.enableEditing"] === true && md.edit;
            }
            return md.edit;
          },
          // Privileges management may be allowed for harvested records.
          canManagePrivileges: function (md) {
            if (
              md &&
              md.isHarvested &&
              JSON.parse(md.isHarvested) == true &&
              gnConfig["system.harvester.enablePrivilegesManagement"] === true &&
              md.edit
            ) {
              return true;
            }
            return this.canEditRecord(md);
          }
        };
        // Build is<ProfileName> and is<ProfileName>OrMore functions
        // This are not group specific, so not usable on metadata
        angular.forEach($scope.profiles, function (profile) {
          userFn["is" + profile] = function () {
            return profile === this.profile;
          };
          userFn["is" + profile + "OrMore"] = function () {
            var profileIndex = $scope.profiles.indexOf(profile),
              allowedProfiles = [];
            angular.copy($scope.profiles, allowedProfiles);
            allowedProfiles.splice(0, profileIndex);
            return allowedProfiles.indexOf(this.profile) !== -1;
          };
        });

        // Retrieve user information if catalog is online
        // append a random number to avoid caching in IE11
        var userLogin = catInfo.then(function (value) {
          return $http
            .get("../api/me?_random=" + Math.floor(Math.random() * 10000))
            .then(function (response) {
              var me = response.data;
              if (angular.isObject(me)) {
                me["isAdmin"] = function (groupId) {
                  return me.admin;
                };

                angular.forEach($scope.profiles, function (profile) {
                  // Builds is<ProfileName>ForGroup methods
                  // to check the profile in the group
                  me["is" + profile + "ForGroup"] = function (groupId) {
                    if ("Administrator" == profile) {
                      return me.admin;
                    }
                    if (
                      me["groupsWith" + profile] &&
                      me["groupsWith" + profile].indexOf(Number(groupId)) !== -1
                    ) {
                      return true;
                    }
                    return false;
                  };
                });
                angular.extend($scope.user, me);
                angular.extend($scope.user, userFn);
                $scope.authenticated = true;
              } else {
                $scope.authenticated = false;
                $scope.user = undefined;
              }
            });
        });

        // Retrieve the publication options
        userLogin.then(function (value) {
          if ($scope.user && $scope.user.isReviewerOrMore()) {
            $http.get("../api/records/sharing/options").then(function (response) {
              $scope.publicationOptions = response.data;
            });
          }
        });

        // Retrieve main search information
        var searchInfo = userLogin.then(function (value) {
          // Check index status.
          $http.get("../api/site/index/status").then(function (r) {
            gnConfig["indexStatus"] = r.data;

            if (r.data.state.id.match(/^(green|yellow)$/) == null) {
              $rootScope.$broadcast("StatusUpdated", {
                id: "indexStatus",
                title: r.data.state.title,
                error: {
                  message: r.data.message
                },
                // type: r.data.state.icon,
                type: "danger"
              });
            } else {
              var query = {
                bool: { must: { query_string: { query: "+isTemplate:n" } } }
              };
              if (gnGlobalSettings.gnCfg.mods.search.filters) {
                query.bool.filter = gnGlobalSettings.gnCfg.mods.search.filters;
              }
              return $http
                .post("../api/search/records/_search", {
                  size: 0,
                  track_total_hits: true,
                  query: query,
                  aggs: gnGlobalSettings.gnCfg.mods.home.facetConfig
                })
                .then(function (r) {
                  $scope.searchInfo = r.data;
                  var keys = Object.keys(gnGlobalSettings.gnCfg.mods.home.facetConfig);
                  var selectedFacet = keys[0];

                  for (var i = 0; i < keys.length; i++) {
                    if (
                      $scope.searchInfo.aggregations[keys[i]].buckets.length > 0 ||
                      Object.keys($scope.searchInfo.aggregations[keys[i]]).length > 0
                    ) {
                      selectedFacet = keys[i];
                      break;
                    }
                  }
                  $scope.homeFacet = {
                    list: keys,
                    key: selectedFacet,
                    lastKey: keys.length > 1 ? keys[keys.length - 1] : undefined,
                    config: gnGlobalSettings.gnCfg.mods.home.facetConfig,
                    facets: gnESFacet.createFacetModel(
                      gnGlobalSettings.gnCfg.mods.home.facetConfig,
                      r.data.aggregations,
                      undefined,
                      undefined
                    )
                  };
                });
            }
          });
        });
      };
      $scope.userAdminMenu = gnAdminMenu.UserAdmin;
      $scope.adminMenu = gnAdminMenu.Administrator;
      $scope.$on("loadCatalogInfo", function (event, status) {
        $scope.loadCatalogInfo();
      });

      $scope.allowPublishInvalidMd = function () {
        return gnConfig["metadata.workflow.allowPublishInvalidMd"];
      };

      $scope.allowPublishNonApprovedMd = function () {
        return gnConfig["metadata.workflow.allowPublishNonApprovedMd"];
      };

      $scope.getPublicationOptionClass = function (
        md,
        user,
        isMdWorkflowEnable,
        pubOption
      ) {
        var publicationOptionTitle = $scope.getPublicationOptionTitle(
          md,
          user,
          isMdWorkflowEnable,
          pubOption
        );
        switch (publicationOptionTitle) {
          case "mdnonapprovedcantpublish":
          case "mdinvalidcantpublish":
            return "disabled";
            break;
          default:
            return "";
        }
      };

      // Function to get the title name to be used when displaying the publish item in the menu
      $scope.getPublicationOptionTitle = function (
        md,
        user,
        isMdWorkflowEnable,
        pubOption
      ) {
        var publicationOptionTitle = "";
        if (!md.isPublished(pubOption)) {
          if (md.isValid()) {
            publicationOptionTitle = "mdvalid";
          } else {
            if (
              isMdWorkflowEnable &&
              md.isWorkflowEnabled() &&
              $scope.allowPublishInvalidMd() === false &&
              pubOption.name === "default"
            ) {
              publicationOptionTitle = "mdinvalidcantpublish";
            } else {
              if (!md.hasValidation()) {
                publicationOptionTitle = "mdnovalidation";
              } else {
                publicationOptionTitle = "mdinvalid";
              }
            }
          }
          // if we are not using a disabled option like mdinvalidcantpublish then check if there is an approval
          if (
            publicationOptionTitle != "mdinvalidcantpublish" &&
            isMdWorkflowEnable &&
            md.isWorkflowEnabled() &&
            md.mdStatus != 2 &&
            $scope.allowPublishNonApprovedMd() === false &&
            pubOption.name === "default"
          ) {
            publicationOptionTitle = "mdnonapprovedcantpublish";
          }
        }
        return publicationOptionTitle;
      };

      $scope.$on("StatusUpdated", function (event, status) {
        var statusToApply = {};
        $.extend(statusToApply, defaultStatus, status);

        if ($scope.showHealthIndexError !== true) {
          gnAlertService.addAlert(statusToApply, statusToApply.timeout);
        }
      });

      gnSessionService.scheduleCheck($scope.user);
      $scope.session = gnSessionService.getSession();

      $scope.loadCatalogInfo();

      $scope.healthCheck = {};
      // Flag to show the health index error panel
      // By default hidden, only to be displayed if the
      // health check for the index returns an error.
      $scope.showHealthIndexError = false;

      function healthCheckStatus(r) {
        var data = r.data,
          isCritical = r.config.url.indexOf("critical") !== -1;
        angular.forEach(data, function (o) {
          $scope.healthCheck[o.name] = o.status === "OK";
        });

        if (isCritical) {
          $scope.showHealthIndexError =
            !$scope.healthCheck ||
            ($scope.healthCheck && $scope.healthCheck.IndexHealthCheck == false);
        }
      }
      $http.get("../../criticalhealthcheck").then(healthCheckStatus, healthCheckStatus);
      $http.get("../../warninghealthcheck").then(healthCheckStatus, healthCheckStatus);
    }
  ]);
})();
