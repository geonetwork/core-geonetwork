(function () {
  goog.provide("sxt_panier_service");

  var module = angular.module("sxt_panier_service", []);

  module.service("sxtPanierService", [
    "$http",
    "wfsFilterService",
    function ($http, wfsFilterService) {
      var panierUrl = "extractor.doExtract";

      var callExtractService = function (panier) {
        // this object is temporary and will be sent to the extractor
        var panierData = {
          user: panier.user,
          layers: []
        };

        panier.layers.forEach(function (l) {
          var panierLayer = {};
          panierData.layers.push(panierLayer);

          // copying values from original object
          panierLayer.id = l.id;
          panierLayer.input = l.input;
          panierLayer.output = l.output;
          panierLayer.additionalInput = [];

          // include selected WPS processes as additionalInputs
          if (l._element && l._element.processes) {
            l._element.processes.forEach(function (process, index) {
              if (process && process.included) {
                if (!process.executeMessage) {
                  console.warn("WPS process was not initialized, skipping: ", process);
                } else {
                  // add new additionalInput object
                  panierLayer.additionalInput.push({
                    protocol: "WPS",
                    identifier: process.name,
                    linkage: process.url,
                    params: btoa(unescape(encodeURIComponent(process.executeMessage)))
                  });
                }
              }
            });
          }

          // fetch ElasticSearch; if not available, no filters are set
          var es = wfsFilterService.getEsObject(l.input.linkage, l.output.name);
          if (es && l.useFilters) {
            panierLayer.input.filter = wfsFilterService.toCQL(es, true);
          }
        });
        return $http({
          url: panierUrl,
          method: "POST",
          data: panierData,
          headers: { "Content-Type": "application/json" }
        });
      };

      this.extract = function (panier) {
        return callExtractService(panier);
      };

      /**
       * The basket contains only links. This method is used to bind downloads
       * object with respective layers.
       *
       * @param {Object} panier All layers to download.
       * @param {ol.Map} map viewer map.
       */
      this.bindPanierWithLayers = function (panier, map) {
        map.getLayers().forEach(function (layer) {
          var downloads = layer.get("downloads");
          if (downloads) {
            // Find layers binded to download
            panier.forEach(function (panierItem) {
              if (
                downloads.some(function (d) {
                  var l = panierItem.link;
                  return d.url == l.url && d.name == l.name && d.protocol == l.protocol;
                })
              ) {
                // Check if a WFS filter is applied
                var esObject = layer.get("indexObject");
                if (esObject) {
                  var esConfig = esObject.getState();
                  var g = esConfig.geometry;
                  var params = wfsFilterService.toReadableObject(esObject);
                  panierItem.filter = {
                    params: Object.keys(params).length > 0 ? params : null,
                    any: esConfig.any
                  };
                  if (g) {
                    var extent = [g[0][0], g[1][1], g[1][0], g[0][1]];
                    panierItem.filter.extent = extent;
                  }
                }

                var processes = layer.get("processes");

                // only add processes once (with desc as label in the form)
                if (processes && !panierItem.processes) {
                  processes = processes.map(function (process) {
                    process.label = process.desc;
                    return process;
                  });
                  panierItem.processes = processes;
                }
              }
            });
          }
        });
      };

      /**
       * Adds references to WPS processes on the panier item by looking up
       * the links in the same group
       * Note: processes may have already been added with bindPanierWithLayers;
       * in this case, do nothing.
       *
       * @param {Object} panier All layers to download.
       */
      this.addProcessesToItems = function (panier) {
        panier.forEach(function (panierItem) {
          var processes =
            panierItem.md &&
            panierItem.md.getLinksByType(panierItem.link.group, "OGC:WPS");

          // only add processes once (with desc as label in the form)
          if (processes && !panierItem.processes) {
            processes = processes.map(function (process) {
              process.label = process.desc;
              return process;
            });
            panierItem.processes = processes;
          }
        });
      };
    }
  ]);
})();
