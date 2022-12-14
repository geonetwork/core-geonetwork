(function () {
  goog.provide("gn_sxt_utils");

  goog.require("gn_sxt_legacy_facet_mapping");

  var module = angular.module("gn_sxt_utils", ["gn_sxt_legacy_facet_mapping"]);

  module.service("sxtService", [
    "SEXTANT_LEGACY_FACET_MAPPING",
    "gnLangs",
    "gnGlobalSettings",
    "$http",
    "$q",
    function (SEXTANT_LEGACY_FACET_MAPPING, gnLangs, gnGlobalSettings, $http, $q) {
      var panierEnabled =
        typeof sxtSettings === "undefined" ||
        !angular.isUndefined(sxtSettings.tabOverflow.panier);

      var directDownloadTypes = [
        "#WWW:DOWNLOAD-1.0-link--download",
        "#WWW:DOWNLOAD-1.0-http--download",
        "#WWW:OPENDAP",
        "#MYO:MOTU-SUB",
        "#WWW:FTP"
      ];

      // Sextant / Exact match on protocol
      // https://gitlab.ifremer.fr/sextant/geonetwork/-/issues/542#note_95919
      var allDownloadTypes = gnGlobalSettings.gnCfg.mods.search.linkTypes.downloads.map(
        function (p) {
          return "#" + p;
        }
      );

      var downloadTypes = panierEnabled ? allDownloadTypes : directDownloadTypes;

      var downloadTypesWithoutWxS = downloadTypes.filter(function (protocol) {
        return protocol !== "#OGC:WFS" && protocol !== "#OGC:WCS";
      });

      var layerTypes = gnGlobalSettings.gnCfg.mods.search.linkTypes.layers;

      this.feedMd = function (scope) {
        var md = scope.md;

        var status = md.mdStatus;
        var user = scope.user;
        scope.cantStatus =
          user &&
          (status == 4 || status == 2 || status == 3) &&
          user.isReviewerOrMore &&
          !user.isReviewerOrMore();

        scope.unFilteredlinks = md.getLinksByType("LINK");
        // we do not want to display the following protocols
        // link https://forge.ifremer.fr/mantis/view.php?id=40721
        scope.links = [];
        scope.unFilteredlinks.forEach(function (link) {
          if (
            link.protocol !== "NETWORK:LINK" &&
            link.protocol !== "WWW:DOWNLOAD-1.0-link--download"
          ) {
            scope.links.push(link);
          }
        });

        scope.downloads = [];
        scope.layers = [];

        // an array of group indices, e.g. [0, 1, 2]
        var linkGroups = md.link
          ? md.link
              .map(function (link) {
                return link.group;
              })
              .filter(function (link, i, arr) {
                return link != undefined && arr.indexOf(link) === i;
              })
          : [];

        angular.forEach(
          linkGroups,
          function (group) {
            // get all layers and downloads for this transferOptions
            var layers = md.getLinksByType.apply(md, [group].concat(layerTypes));

            var downloads = md.getLinksByType.apply(md, [group].concat(downloadTypes));

            if (downloads.length > 0) {
              // If only one layer, hide any WFS or WCS links unless there are several
              // note: this does not apply if there is only one download link (otherwise we might end up with 0 links)
              // https://gitlab.ifremer.fr/sextant/geonetwork/-/wikis/Catalogue#les-protocoles
              if (layers.length === 1 && downloads.length > 1) {
                var multipleWxS =
                  md.getLinksByType(group, "#OGC:WFS", "#OGC:WCS").length > 1;

                if (!multipleWxS) {
                  downloads = md.getLinksByType.apply(
                    md,
                    [group].concat(downloadTypesWithoutWxS)
                  );
                }
              }

              scope.downloads = scope.downloads.concat(downloads);
            }
            scope.layers = scope.layers.concat(layers);
          }.bind(this)
        );
      };

      /**
       * This transforms the legacy (sextant v6 and below) facet format to the new ES-compatible format
       * Existing ES facets will be removed.
       * If the facet fails to be migrated, a warning is shown in the console
       * @param {Object} esFacetConfig
       * @param {Array} sxtFacetConfig
       */
      this.migrateLegacyFacetConfigToEs = function (esFacetConfig, sxtFacetConfig) {
        function addEsFacetConfig(sxtFacet) {
          try {
            // the facet has language variants (e.g. inspire theme)
            if (sxtFacet.langs) {
              sxtFacet.key = sxtFacet.langs[gnLangs.current];
            }

            var esFacetTemplate = SEXTANT_LEGACY_FACET_MAPPING[sxtFacet.key];

            // generic facet if not defined
            if (!esFacetTemplate) {
              esFacetTemplate = {};
              esFacetTemplate[sxtFacet.key] = {
                terms: {
                  field: sxtFacet.key,
                  size: 300
                }
              };
            }

            var esFacetName = Object.keys(esFacetTemplate)[0];
            var esFacet = angular.extend({}, esFacetTemplate);

            esFacet[esFacetName].meta = angular.extend(
              {
                displayFilter: !!sxtFacet.filter,
                collapsed: !sxtFacet.opened,
                labels: sxtFacet.labels
              },
              esFacetTemplate[esFacetName].meta
            );

            var orderedAlphabetical = sxtFacet.orderBy === "alphabetical";
            if (orderedAlphabetical) {
              esFacet[esFacetName].meta.orderByTranslation = true;
              esFacet[esFacetName].terms.order = { _key: "asc" };
            }

            if (sxtFacet.include) {
              esFacet[esFacetName].terms.include = sxtFacet.include;
            }
            if (sxtFacet.exclude) {
              esFacet[esFacetName].terms.exclude = sxtFacet.exclude;
            }

            angular.extend(esFacetConfig, esFacet);
          } catch (e) {
            console.warn(
              "A legacy Sextant v6 facet could not be migrated to v7\n" +
                "The following error was thrown: " +
                e.message,
              sxtFacet
            );
          }
        }
        // clean existing facets, add new ones
        for (var key in esFacetConfig) {
          delete esFacetConfig[key];
        }
        for (var i = 0; i < sxtFacetConfig.length; i++) {
          addEsFacetConfig(sxtFacetConfig[i]);
        }
      };

      /**
       * This produces a list of ids to be used as a filter in the group facet, based on the
       * specified groups in `configWhat` API setting.
       * @param {string} configWhat
       * @return {Promise<string[]>}
       */
      this.getGroupIdsFromConfigWhat = function (configWhat) {
        var defer = $q.defer();
        $http.get("../../srv/api/groups").success(function (groups) {
          var groupNames = configWhat.split(",");
          var filter = groups
            .filter(function (group) {
              return groupNames.indexOf(group.name) > -1;
            })
            .map(function (group) {
              return group.id.toString();
            });
          defer.resolve(filter);
        });
        return defer.promise;
      };
    }
  ]);
})();
