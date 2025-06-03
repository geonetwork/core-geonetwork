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
  goog.provide("gn_mdview_directive");

  var module = angular.module("gn_mdview_directive", [
    "ui.bootstrap.tpls",
    "ui.bootstrap.rating"
  ]);

  /**
   * Directive to set the proper link to open
   * a metadata record in the default angular view
   * or using a formatter.
   */
  module.directive("gnMetadataOpen", [
    "gnMdViewObj",
    "gnMdView",
    "gnGlobalSettings",
    "$filter",
    function (gnMdViewObj, gnMdView, gnGlobalSettings, $filter) {
      return {
        restrict: "A",
        scope: {
          md: "=gnMetadataOpen",
          formatter: "=gnFormatter",
          records: "=gnRecords",
          selector: "@gnMetadataOpenSelector",
          appUrl: "@?"
        },
        link: function (scope, element, attrs, controller) {
          scope.$watch("md", function (n, o) {
            if (
              n == null ||
              n == undefined ||
              (n && n.uuid == undefined) ||
              (n && n.remoteUrl !== undefined)
            ) {
              return;
            }

            var formatter =
              scope.formatter === undefined || scope.formatter == ""
                ? undefined
                : scope.formatter.replace("../api/records/{{uuid}}/formatters/", "");

            var hyperlinkTagName = "A";
            if (element.get(0).tagName === hyperlinkTagName) {
              var url = scope.appUrl || window.location.pathname + window.location.search;

              if (
                gnGlobalSettings.gnCfg.mods.recordview.appUrl &&
                gnGlobalSettings.gnCfg.mods.recordview.appUrl.indexOf("http") === 0
              ) {
                url = $filter("setUrlPlaceholder")(
                  gnGlobalSettings.gnCfg.mods.recordview.appUrl
                );
              }
              var url =
                url +
                "#/" +
                (scope.md.draft == "y" ? "metadraf" : "metadata") +
                "/" +
                scope.md.uuid +
                (scope.formatter === undefined || scope.formatter == "" ? "" : formatter);

              element.attr("href", url);
            } else {
              element.on("click", function (e) {
                gnMdView.setLocationUuid(scope.md.uuid, formatter);
              });
            }
            if (scope.records && scope.records.length) {
              gnMdViewObj.records = scope.records;
            } else {
              gnMdViewObj.records = [];
            }
          });
        }
      };
    }
  ]);

  /**
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-mlt-query.html
   */
  module.directive("gnMoreLikeThis", [
    "$http",
    "gnGlobalSettings",
    "Metadata",
    "gnESFacet",
    function ($http, gnGlobalSettings, Metadata, gnESFacet) {
      return {
        scope: {
          md: "=gnMoreLikeThis"
        },
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/search/mdview/partials/" + "morelikethis.html"
          );
        },
        link: function (scope, element, attrs, controller) {
          var initSize = attrs["size"] ? parseInt(attrs["size"]) : 4;
          scope.maxSize = attrs["maxSize"] ? parseInt(attrs["maxSize"]) : 12;
          scope.similarDocuments = [];
          scope.size = initSize;
          scope.pageSize = initSize;
          scope.ofSameType =
            gnGlobalSettings.gnCfg.mods.search.moreLikeThisSameType === true;

          var moreLikeThisQuery = {};
          angular.copy(
            gnGlobalSettings.gnCfg.mods.search.moreLikeThisConfig,
            moreLikeThisQuery
          );

          var datasetTypes = ["nonGeographicDataset", "dataset"],
            resourceTypeMapping = {
              nonGeographicDataset: { label: "dataset", types: datasetTypes },
              dataset: { label: "dataset", types: datasetTypes },
              featureCatalog: {
                label: "records",
                types: ["featureCatalog"].concat(datasetTypes)
              }
            };

          function buildQuery() {
            var query = gnESFacet.buildDefaultQuery(
              {
                bool: {
                  must: [
                    moreLikeThisQuery,
                    { terms: { isTemplate: ["n"] } },
                    // TODO: We may want to use it for subtemplate
                    { terms: { draft: ["n", "e"] } }
                  ],
                  // Exclude self and all related records
                  must_not: [
                    {
                      terms: {
                        uuid: [scope.md.uuid].concat(
                          scope.md.related && scope.md.related.uuids
                            ? scope.md.related.uuids
                            : []
                        )
                      }
                    }
                  ]
                }
              },
              scope.size
            );

            query.query.bool.must[0].more_like_this.like = scope.md.resourceTitle;

            var resourceType = scope.md.resourceType
              ? scope.md.resourceType[0]
              : undefined;
            var filter = [];
            if (scope.ofSameType && resourceType) {
              var mapping = resourceTypeMapping[resourceType];
              scope.label = mapping ? mapping.label : resourceType;
              filter.push({
                terms: { resourceType: mapping ? mapping.types : [resourceType] }
              });
            }

            if (
              gnGlobalSettings.gnCfg.mods.search.moreLikeThisFilter &&
              gnGlobalSettings.gnCfg.mods.search.moreLikeThisFilter != ""
            ) {
              filter.push({
                query_string: {
                  query: gnGlobalSettings.gnCfg.mods.search.moreLikeThisFilter
                }
              });
            }

            if (filter.length > 0) {
              query.query.bool.filter = filter;
            }

            return query;
          }

          scope.moreRecords = function () {
            scope.size += scope.pageSize;
            loadMore();
          };

          function loadMore() {
            if (scope.md == null) {
              return;
            }

            $http.post("../api/search/records/_search", buildQuery()).then(function (r) {
              scope.total = r.data.hits.total.value;
              scope.similarDocuments = r.data.hits.hits.map(function (r) {
                return new Metadata(r);
              });
            });
          }

          scope.$watch("md", function () {
            scope.similarDocuments = [];
            scope.size = initSize;
            scope.pageSize = initSize;
            loadMore();
          });
        }
      };
    }
  ]);

  module.directive("gnDataPreview", [
    "gnMapsManager",
    "gnMap",
    "gnSearchSettings",
    function (gnMapsManager, gnMap, gnSearchSettings) {
      return {
        scope: {
          md: "=gnDataPreview"
        },
        templateUrl: "../../catalog/components/search/mdview/partials/datapreview.html",
        controller: [
          "$scope",
          "$timeout",
          function (scope, $timeout) {
            scope.map = gnMapsManager.createMap(gnMapsManager.SEARCH_MAP);
            scope.hasExtent = false;
            scope.currentLayer = undefined;
            scope.extentLayer = new ol.layer.Vector({
              source: new ol.source.Vector(),
              map: scope.map,
              style: gnSearchSettings.olStyles.mdExtent
            });

            this.addRecordsExtent = function (records) {
              scope.extentLayer.getSource().clear();

              for (var i = 0; i < records.length; i++) {
                var feat = gnMap.getBboxFeatureFromMd(
                  records[i],
                  scope.map.getView().getProjection()
                );
                scope.extentLayer.getSource().addFeature(feat);
                scope.hasExtent = !!feat.getGeometry();
              }

              if (scope.hasExtent) {
                $timeout(function () {
                  scope.map
                    .getView()
                    .fit(scope.extentLayer.getSource().getExtent(), scope.map.getSize());
                }, 100);
              }
            };
          }
        ],
        link: function (scope, element, attrs, ctrl) {
          if (scope.md) {
            scope.map.get("creationPromise").then(function () {
              ctrl.addRecordsExtent([scope.md]);
              scope.md.getLinksByType("OGC:WMS").forEach(function (link) {
                gnMap
                  .addWmsFromScratch(scope.map, link.url, link.name, false, scope.md)
                  .then(function (layer) {
                    if (!scope.currentLayer) {
                      scope.currentLayer = layer;
                    }
                  });
              });
            });
          }
        }
      };
    }
  ]);

  module.directive("gnMetadataDisplay", [
    "gnMdView",
    "gnSearchSettings",
    function (gnMdView, gnSearchSettings) {
      return {
        scope: true,
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/search/mdview/partials/" + "mdpanel.html"
          );
        },
        link: function (scope, element, attrs, controller) {
          var unRegister;

          element.find(".panel-body").append(scope.fragment);
          scope.dismiss = function () {
            unRegister();
            // Do not close parent mdview
            if ($("[gn-metadata-display] ~ [gn-metadata-display]").length == 0) {
              gnMdView.removeLocationUuid();
            }
            element.remove();
            //TODO: is the scope destroyed ?
          };

          if (gnSearchSettings.dismissMdView) {
            scope.dismiss = gnSearchSettings.dismissMdView;
          }
          unRegister = scope.$on("locationBackToSearchFromMdview", function () {
            scope.dismiss();
          });
        }
      };
    }
  ]);

  module.directive("gnMetadataObjectField", [
    function () {
      return {
        templateUrl:
          "../../catalog/components/search/mdview/partials/" + "objectFieldWithLink.html",
        restrict: "A",
        scope: {
          field: "@gnMetadataObjectField",
          record: "="
        }
      };
    }
  ]);

  module.directive("gnMetadataRate", [
    "$http",
    "gnConfig",
    "gnConfigService",
    function ($http, gnConfig, gnConfigService) {
      return {
        templateUrl: "../../catalog/components/search/mdview/partials/" + "rate.html",
        restrict: "A",
        scope: {
          md: "=gnMetadataRate",
          readonly: "@readonly"
        },

        link: function (scope, element, attrs, controller) {
          scope.isRatingEnabled = false;

          gnConfigService.load().then(function (c) {
            var statusSystemRating = gnConfig[gnConfig.key.isRatingUserFeedbackEnabled];
            if (statusSystemRating == "advanced") {
              scope.isUserFeedbackEnabled = true;
            }
            if (statusSystemRating == "basic") {
              scope.isRatingEnabled = true;
            }
          });

          scope.$watch("md", function () {
            scope.rate = scope.md ? scope.md.rating : null;
          });

          scope.rateForRecord = function () {
            return $http
              .put("../api/records/" + scope.md.uuid + "/rate", scope.rate)
              .then(function (response) {
                scope.rate = response.data;
              });
          };
        }
      };
    }
  ]);

  /**
   * Directive to provide 3 visualization modes for metadata contacts
   * in metadata detail page:
   *
   * - 'default': plain list of contacts.
   *
   * - 'role': grouped by role, then by organisation. Example rendering:
   *
   *      Resource provider
   *       Organisation 1
   *       List of users with role
   *       Address organisation 1
   *
   *       Organisation 2
   *       List of users with role
   *       Address organisation 1
   *
   *      Custodian,Distributor
   *       Organisation 1
   *       List of users with role
   *       Address organisation 1
   *
   * - 'org-role': grouped by organisation, then by role. Example rendering:
   *
   *      Organisation 1
   *      Address organisation 1
   *      Resource provider : user1@mail.com
   *      Custodian, Distributor :  user2@mail.com
   *
   *      Organisation 2
   *      Address organisation 2
   *      Resource provider : user3@mail.com
   */
  module.directive("gnMetadataContacts", [
    "$http",
    "$filter",
    "gnGlobalSettings",
    function ($http, $filter, gnGlobalSettings) {
      return {
        templateUrl: "../../catalog/components/search/mdview/partials/contact.html",
        restrict: "A",
        scope: {
          mdContacts: "=gnMetadataContacts",
          // Group by 'default', 'role', 'org-role'
          mode: "@gnMode",
          // 'icon' or 'list' (default)
          layout: "@layout",
          type: "@type"
        },
        link: function (scope, element, attrs, controller) {
          scope.isDefaultContactViewEnabled = function () {
            return gnGlobalSettings.gnCfg.mods.recordview.isDefaultContactViewEnabled;
          };

          if (["default", "role", "org-role"].indexOf(scope.mode) == -1) {
            scope.mode = "default";
          }

          if (scope.type === "metadata") {
            scope.focusOnFilterFieldName = "OrgObject.default";
          } else if (scope.type === "distribution") {
            scope.focusOnFilterFieldName = "OrgForDistributionObject.default";
          } else if (scope.type === "processing") {
            scope.focusOnFilterFieldName = "OrgForProcessingObject.default";
          } else {
            scope.focusOnFilterFieldName = "OrgForResourceObject.default";
          }

          scope.calculateContacts = function () {
            if (scope.mode != "default") {
              var groupByOrgAndMailOrName = function (resources) {
                return _.groupBy(resources, function (contact) {
                  if (contact.email) {
                    return contact.organisation + "#" + contact.email;
                  } else {
                    return contact.organisation + "#" + contact.individual;
                  }
                });
              };

              var aggregateRoles = function (resources) {
                return _.map(resources, function (contact) {
                  var copy = angular.copy(contact[0]);
                  angular.extend(copy, {
                    roles: _.map(contact, "role")
                  });

                  return copy;
                });
              };

              if (scope.mode == "role") {
                var contactsByOrgAndMailOrName = groupByOrgAndMailOrName(
                  scope.mdContacts
                );

                var contactsWithAggregatedRoles = aggregateRoles(
                  contactsByOrgAndMailOrName
                );

                /**
                 * Contacts format:
                 *
                 * {
                 *    {[roles]: [{contact1}, {contact2}, ... },
                 *    {[roles]: [{contact3}, {contact4}, ... },
                 * }
                 *
                 */
                scope.mdContactsByRole = _.groupBy(
                  contactsWithAggregatedRoles,
                  function (c) {
                    return c.roles;
                  }
                );
              } else if (scope.mode == "org-role") {
                /**
                 * Contacts format:
                 *
                 * {
                 *    {organisation1: [{contact1}, {contact2}, ... },
                 *    {organisation2: [{contact3}, {contact4}, ... },
                 * }
                 *
                 */
                scope.orgWebsite = {};
                scope.mdContactsByOrgRole = _.groupBy(
                  scope.mdContacts,
                  function (contact) {
                    if (contact.website !== "") {
                      scope.orgWebsite[contact.organisation] = contact.website;
                    }
                    return contact.organisation;
                  }
                );

                for (var key in scope.mdContactsByOrgRole) {
                  var value = scope.mdContactsByOrgRole[key];

                  var contactsByOrgAndMailOrName = groupByOrgAndMailOrName(value);

                  scope.mdContactsByOrgRole[key] = aggregateRoles(
                    contactsByOrgAndMailOrName
                  );
                }
              }
            }
          };

          /**
           * Splits a comma separated list of role keys and
           * returns a comma separated list of role translations.
           *
           * @param roles
           * @returns {string|*}
           */
          scope.translateRoles = function (roles) {
            if (roles) {
              var rolesList = roles.split(",");
              var roleTranslations = [];

              for (var i = 0; i < rolesList.length; i++) {
                roleTranslations.push($filter("translate")(rolesList[i]));
              }

              return roleTranslations.join(",");
            } else {
              return "";
            }
          };

          scope.$watch("mdContacts", function () {
            scope.calculateContacts();
          });
        }
      };
    }
  ]);

  module.directive("gnMetadataIndividual", [
    "$http",
    "$filter",
    function ($http, $filter) {
      return {
        templateUrl:
          "../../catalog/components/search/mdview/partials/" + "individual.html"
      };
    }
  ]);

  module.directive("gnKeywordBadges", [
    "gnGlobalSettings",
    function (gnGlobalSettings) {
      return {
        templateUrl:
          "../../catalog/components/search/mdview/partials/" + "keywordBadges.html",
        scope: {
          record: "=gnKeywordBadges",
          thesaurus: "=thesaurus"
        },
        link: function (scope, element, attrs) {
          scope.thesaurus = angular.isArray(scope.thesaurus)
            ? scope.thesaurus
            : [scope.thesaurus];
          scope.allKeywords = scope.record && scope.record.allKeywords;
          scope.getOrderByConfig = function (thesaurus) {
            return thesaurus === "th_regions"
              ? ["-group", "default"]
              : gnGlobalSettings.gnCfg.mods.recordview.sortKeywordsAlphabetically
              ? "default"
              : "";
          };
        }
      };
    }
  ]);

  module.directive("gnMetadataSocialLink", [
    "gnMetadataActions",
    "$http",
    function (gnMetadataActions, $http) {
      return {
        templateUrl: "../../catalog/components/search/mdview/partials/social.html",
        scope: {
          md: "=gnMetadataSocialLink"
        },
        link: function (scope, element, attrs) {
          scope.mdService = gnMetadataActions;

          scope.$watch(
            "md",
            function (newVal, oldVal) {
              if (newVal !== null && newVal !== oldVal) {
                $http
                  .get("../api/records/" + scope.md.getUuid() + "/permalink")
                  .then(function (r) {
                    scope.socialMediaLink = r.data;
                  });
              }
            },
            true
          );
        }
      };
    }
  ]);

  module.directive("gnQualityMeasuresTable", [
    function () {
      return {
        templateUrl:
          "../../catalog/components/search/mdview/partials/qualitymeasures.html",
        scope: {
          measures: "=gnQualityMeasuresTable"
        },
        link: function (scope, element, attrs) {
          scope.columnVisibility = {
            name: false,
            description: false,
            value: false,
            type: false,
            date: false
          };
          for (var idx in scope.measures) {
            angular.forEach(Object.keys(scope.columnVisibility), function (p) {
              if (scope.measures[idx][p]) {
                scope.columnVisibility[p] = true;
              }
            });
          }
        }
      };
    }
  ]);
})();
