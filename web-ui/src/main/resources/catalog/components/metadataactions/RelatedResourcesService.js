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
  goog.provide("gn_relatedresources_service");

  goog.require("gn_wfs_service");

  var module = angular.module("gn_relatedresources_service", ["gn_wfs_service"]);

  /**
   * Standarizes the way to handle resources. Given a type of resource, you get
   * an icon class and an action.
   *
   * To extend this, use the configure function. For example:
   *
   * $gnRelatedResources.configure({ "PDF" : { iconClass: "pdfClassIcon",
   * action: myCustomFunctionForPDF}, "XLS" : { iconClass: "xlsClassIcon",
   * action: myCustomFunctionForXLS}});
   *
   */
  module.service("gnRelatedResources", [
    "gnMap",
    "gnOwsCapabilities",
    "gnSearchSettings",
    "gnViewerSettings",
    "olDecorateLayer",
    "gnSearchLocation",
    "gnOwsContextService",
    "gnWfsService",
    "gnAlertService",
    "gnConfigService",
    "gnConfig",
    "$filter",
    "gnExternalViewer",
    "gnGlobalSettings",
    function (
      gnMap,
      gnOwsCapabilities,
      gnSearchSettings,
      gnViewerSettings,
      olDecorateLayer,
      gnSearchLocation,
      gnOwsContextService,
      gnWfsService,
      gnAlertService,
      gnConfigService,
      gnConfig,
      $filter,
      gnExternalViewer,
      gnGlobalSettings
    ) {
      this.configure = function (options) {
        angular.extend(this.map, options);
      };

      this.getBadgeLabel = function (mainType, r) {
        if (r.mimeType != undefined && r.mimeType != "") {
          return r.mimeType;
        } else if (r.protocol && r.protocol.indexOf("WWW:DOWNLOAD:") >= 0) {
          return r.protocol.replace("WWW:DOWNLOAD:", "");
        } else if (mainType.match(/W([MCF]|MT)S.*|ESRI:REST/) != null) {
          return mainType.replace("SERVICE", "");
        } else if (mainType.match(/KML|GPX/) != null) {
          return mainType;
        } else {
          return "";
        }
      };

      this.hasAction = function (mainType) {
        var fn = this.map[mainType || "DEFAULT"].action;
        // If function name ends with ToMap do not display the action
        if (
          fn &&
          fn.name &&
          fn.name.match(/.*ToMap$/) &&
          gnGlobalSettings.isMapViewerEnabled === false
        ) {
          return false;
        }
        return angular.isFunction(fn);
      };

      this.gnConfigService = gnConfigService;

      /**
       * Check if the link contains a valid layer protocol
       * as configured in gnSearchSettings and check if it
       * has a layer name.
       *
       * If not, then only service information is displayed.
       *
       * TODO: Would be more precise with a check for the name in Capabilities.
       *
       * @param {object} link
       * @return {boolean}
       */
      this.isLayerProtocol = function (link) {
        return (
          (link.title || link.name) &&
          Object.keys(link.title || link.name).length > 0 &&
          gnSearchSettings.mapProtocols.layers.indexOf(link.protocol) > -1
        );
      };

      var addWMSToMap =
        gnViewerSettings.resultviewFns && gnViewerSettings.resultviewFns.addMdLayerToMap;
      var addEsriRestToMap =
        gnViewerSettings.resultviewFns && gnViewerSettings.resultviewFns.addMdLayerToMap;

      var addWFSToMap =
        gnViewerSettings.resultviewFns &&
        gnViewerSettings.resultviewFns.addMdWFSLayerToMap;

      var addWMTSToMap =
        gnViewerSettings.resultviewFns && gnViewerSettings.resultviewFns.addMdLayerToMap;

      var addTMSToMap = function (link, md) {
        // Link is localized when using associated resource service
        // and is not when using search
        var url = $filter("gnLocalized")(link.url) || link.url;
        gnMap.createLayerFromProperties(
          { type: "tms", url: url },
          gnSearchSettings.viewerMap
        );
        gnSearchLocation.setMap();
      };

      function addKMLToMap(record, md) {
        var url = $filter("gnLocalized")(record.url) || record.url;
        gnMap.addKmlToMap(record.name, url, gnSearchSettings.viewerMap);
        gnSearchLocation.setMap();
      }

      function addGeoJSONToMap(record, md) {
        var url = $filter("gnLocalized")(record.url) || record.url;
        gnMap.addGeoJSONToMap(record.name, url, gnSearchSettings.viewerMap);
        gnSearchLocation.setMap();
      }

      function addMapToMap(record, md) {
        var url = $filter("gnLocalized")(record.url) || record.url;
        gnOwsContextService.loadContextFromUrl(url, gnSearchSettings.viewerMap);

        gnSearchLocation.setMap("legend");
      }

      var openMd = function (r, md, siteUrl) {
        var url = $filter("gnLocalized")(r.url) || r.url;

        if (url.indexOf(siteUrl) == 0) {
          var useCurrentPortal = true;

          if (r && r.origin === "catalog") {
            useCurrentPortal = false;
          }

          if (useCurrentPortal) {
            return (window.location.hash = "#/metadata/" + r.id);
          } else {
            // Replace the portal node with the catalog default node
            var mdUrl =
              window.location.origin +
              window.location.pathname +
              window.location.search +
              "#/metadata/" +
              r.id;
            mdUrl = mdUrl.replace(
              "/" + gnConfig.env.node + "/",
              "/" + gnConfig.env.defaultNode + "/"
            );
            return window.open(mdUrl, "_blank");
          }
        } else {
          return openLink(r);
        }
      };

      var openLink = function (record, link) {
        var url = $filter("gnLocalized")(record.url) || record.url;
        if (url && angular.isString(url) && url.match("^(http|ftp|sftp|\\\\|//)")) {
          return window.open(url, "_blank");
        } else if (url && url.indexOf("www.") == 0) {
          return window.open("http://" + url, "_blank");
        } else if (
          record.title &&
          angular.isString(record.title) &&
          record.title.match("^(http|ftp|sftp|\\\\|//)")
        ) {
          return window.location.assign(record.title);
        } else {
          gnAlertService.addAlert({
            msg: "Unable to open link",
            type: "success"
          });
        }
      };

      this.map = {
        WMS: {
          iconClass: "fa-globe",
          label: "addToMap",
          action: addWMSToMap
        },
        WMSSERVICE: {
          iconClass: "fa-globe",
          label: "addServiceLayersToMap",
          action: addWMSToMap
        },
        WMTS: {
          iconClass: "fa-globe",
          label: "addToMap",
          action: addWMTSToMap
        },
        TMS: {
          iconClass: "fa-globe",
          label: "addToMap",
          action: addTMSToMap
        },
        WFS: {
          iconClass: "fa-globe",
          label: "addToMap",
          action: addWFSToMap
        },
        "ESRI:REST": {
          iconClass: "fa-globe",
          label: "addToMap",
          action: addEsriRestToMap
        },
        ATOM: {
          iconClass: "fa-globe",
          label: "download",
          action: openLink
        },
        WCS: {
          iconClass: "fa-globe",
          label: "fileLink",
          action: null
        },
        SOS: {
          iconClass: "fa-globe",
          label: "fileLink",
          action: null
        },
        MAP: {
          iconClass: "fa-map",
          label: "mapLink",
          action: gnExternalViewer.isEnabled() ? null : addMapToMap
        },
        DB: {
          iconClass: "fa-database",
          label: "dbLink",
          action: null
        },
        FILE: {
          iconClass: "fa-file",
          label: "fileLink",
          action: openLink
        },
        KML: {
          iconClass: "fa-globe",
          label: "addToMap",
          action: gnExternalViewer.isEnabled() ? null : addKMLToMap
        },
        GEOJSON: {
          iconClass: "fa-globe",
          label: "addToMap",
          action: gnExternalViewer.isEnabled() ? null : addGeoJSONToMap
        },
        MDFCATS: {
          iconClass: "fa-table",
          label: "openRecord",
          action: openMd
        },
        MDFAMILY: {
          iconClass: "fa-sitemap",
          label: "openRecord",
          action: openMd
        },
        MDCHILDREN: {
          iconClass: "fa-child",
          label: "openRecord",
          action: openMd
        },
        MDSIBLING: {
          iconClass: "fa-puzzle-piece",
          label: "openRecord",
          action: openMd
        },
        MDSOURCE: {
          iconClass: "fa-sitemap fa-rotate-180",
          label: "openRecord",
          action: openMd
        },
        MDSERVICE: {
          iconClass: "fa-cloud",
          label: "openRecord",
          action: openMd
        },
        MD: {
          iconClass: "fa-file",
          label: "openRecord",
          action: openMd
        },
        LINKDOWNLOAD: {
          iconClass: "fa-download",
          label: "download",
          action: openLink
        },
        "LINKDOWNLOAD-ZIP": {
          iconClass: "fa-file-zip-o",
          label: "download",
          action: openLink
        },
        "LINKDOWNLOAD-PDF": {
          iconClass: "fa-file-pdf-o",
          label: "download",
          action: openLink
        },
        "LINKDOWNLOAD-XML": {
          iconClass: "fa-file-code-o",
          label: "download",
          action: openLink
        },
        "LINKDOWNLOAD-RDF": {
          iconClass: "fa-share-alt",
          label: "download",
          action: openLink
        },
        LINK: {
          iconClass: "fa-link",
          label: "openPage",
          action: openLink
        },
        LEGEND: {
          iconClass: "fa-tint",
          label: "openPage",
          action: openLink
        },
        FEATURECATALOGUE: {
          iconClass: "fa-table",
          label: "openPage",
          action: openLink
        },
        QUALITY: {
          iconClass: "fa-check",
          label: "openPage",
          action: openLink
        },
        DEFAULT: {
          iconClass: "fa-link",
          label: "openPage",
          action: openLink
        }
      };

      this.getClassIcon = function (type) {
        return this.map[type || "DEFAULT"].iconClass || this.map["DEFAULT"].iconClass;
      };

      this.getLabel = function (mainType, type) {
        // Old key before the move to API
        var oldKey = {
          hasfeaturecats: "hasfeaturecat",
          onlines: "onlinesrc",
          siblings: "sibling",
          fcats: "fcat",
          hassources: "hassource"
        };
        return (
          this.map[mainType || "DEFAULT"].label + (oldKey[type] ? oldKey[type] : type)
        );
      };
      this.getAction = function (type) {
        return this.map[type || "DEFAULT"].action;
      };

      this.doAction = function (type, parameters, md) {
        var siteUrlPrefix = this.gnConfigService.getServiceURL();

        var f = this.getAction(type);
        f(parameters, md, siteUrlPrefix);
      };

      this.showMore = function (parameters, md) {
        var siteUrlPrefix = this.gnConfigService.getServiceURL();

        openMd(parameters, md, siteUrlPrefix);
      };

      this.getType = function (resource, type) {
        resource.locTitle = angular.isObject(resource.name)
          ? $filter("gnLocalized")(resource.name)
          : (angular.isObject(resource.title)
              ? $filter("gnLocalized")(resource.title)
              : resource.title) ||
            resource.name ||
            "";
        resource.locDescription = angular.isObject(resource.description)
          ? $filter("gnLocalized")(resource.description)
          : resource.description;
        resource.locUrl = $filter("gnLocalized")(resource.url) || resource.url;
        var protocolOrType = angular.isDefined(resource.protocol)
          ? resource.protocol +
            (angular.isDefined(resource.serviceType) ? resource.serviceType : "")
          : "";

        // Case for links
        if (angular.isString(protocolOrType)) {
          if (resource && resource.function === "legend") {
            return "LEGEND";
          } else if (resource && resource.function === "featureCatalogue") {
            return "FEATURECATALOGUE";
          } else if (resource && resource.function === "dataQualityReport") {
            return "QUALITY";
          }
          if (protocolOrType.match(/wms/i)) {
            if (this.isLayerProtocol(resource)) {
              return "WMS";
            } else {
              return "WMSSERVICE";
            }
          } else if (protocolOrType.match(/download/i)) {
            var url = $filter("gnLocalized")(resource.url) || resource.url || "";
            if (url.match(/sld|qml|lyr/i)) {
              return "LEGEND";
            } else if (url.match(/qgs|mxd|ows/i)) {
              return "MAP";
            } else if (url.match(/zip/i)) {
              return "LINKDOWNLOAD-ZIP";
            } else if (url.match(/pdf/i)) {
              return "LINKDOWNLOAD-PDF";
            } else if (url.match(/xml/i)) {
              return "LINKDOWNLOAD-XML";
            } else if (url.match(/rdf/i)) {
              return "LINKDOWNLOAD-RDF";
            } else {
              return "LINKDOWNLOAD";
            }
          } else if (protocolOrType.match(/esri/i)) {
            return "ESRI:REST";
          } else if (protocolOrType.match(/wmts/i)) {
            return "WMTS";
          } else if (protocolOrType.match(/tms/i)) {
            return "TMS";
          } else if (protocolOrType.match(/wfs/i)) {
            return "WFS";
          } else if (protocolOrType.match(/wcs/i)) {
            return "WCS";
          } else if (protocolOrType.match(/sos/i)) {
            return "SOS";
          } else if (protocolOrType.match(/atom/i)) {
            return "ATOM";
          } else if (protocolOrType.match(/ows-c/i)) {
            return "MAP";
          } else if (protocolOrType.match(/db:/i)) {
            return "DB";
          } else if (protocolOrType.match(/file:/i)) {
            return "FILE";
          } else if (protocolOrType.match(/kml/i)) {
            return "KML";
          } else if (protocolOrType.match(/geojson/i)) {
            return "GEOJSON";
          } else if (protocolOrType.match(/dataset/i)) {
            return "LINKDOWNLOAD";
          } else if (protocolOrType.match(/link/i)) {
            return "LINK";
          } else if (protocolOrType.match(/website/i)) {
            return "LINK";
          }
        }

        // Metadata records
        if (type && type === "parent") {
          return "MDFAMILY";
        } else if (type && type === "children") {
          return "MDCHILDREN";
        } else if (type && type.indexOf("siblings") === 0) {
          return "MDSIBLING";
        } else if (type && type === "services") {
          return "MDSERVICE";
        } else if (type && (type === "sources" || type === "hassources")) {
          return "MDSOURCE";
        } else if (
          type &&
          (type === "associated" || type === "hasfeaturecats" || type === "datasets")
        ) {
          return "MD";
        } else if (type && type === "fcats") {
          return "MDFCATS";
        }

        return "DEFAULT";
      };
    }
  ]);

  /**
   * AngularJS Filter. Filters an array of relations by the given tpye.
   * Uses : relations | relationsfilter:'children children'
   */
  module.filter("gnRelationsFilter", function () {
    return function (relations, types) {
      var result = [];
      var types = types.split(" ");
      angular.forEach(relations, function (rel) {
        if (types.indexOf(rel["@type"]) >= 0) {
          result.push(rel);
        }
      });
      return result;
    };
  });
})();
