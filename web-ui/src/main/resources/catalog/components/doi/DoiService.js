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
  goog.provide("gn_doi_service");

  var module = angular.module("gn_doi_service", []);

  /**
   * Service to deal with DOI.
   */
  module.service("gnDoiService", [
    "$http",
    "$q",
    "gnConfig",
    function ($http, $q, gnConfig) {
      /**
       * Returns a promise to validate a metadata to be published on a DOI server.
       *
       * @param id
       * @param doiServerId
       * @returns {*}
       */
      function check(id, doiServerId) {
        return $http.get(
          "../api/records/" + id + "/doi/" + doiServerId + "/checkPreConditions"
        );
      }

      /**
       * Returns a promise to publish a metadata on a DOI server.
       *
       * @param id
       * @param doiServerId
       * @returns {*}
       */
      function create(id, doiServerId) {
        return $http.put("../api/records/" + id + "/doi/" + doiServerId);
      }

      /**
       * Returns a promise to retrieve the list of DOI servers
       * where a metadata can be published.
       *
       * @param metadataId
       * @returns {*}
       */
      function getDoiServersForMetadata(metadataId) {
        return $http.get("../api/doiservers/metadata/" + metadataId, { cache: true });
      }

      /**
       * Return the DOI server with a prefix matching the DOI url.
       */
      function getDoiServerForMetadataAndDoi(metadataId, doiUrl) {
        var deferred = $q.defer();
        getDoiServersForMetadata(metadataId).then(
          function (response) {
            for (var i = 0; i < response.data.length; i++) {
              if (doiUrl.match("doi.org/" + response.data[i].prefix)) {
                deferred.resolve(response.data[i]);
                return;
              }
            }
            deferred.reject(null);
          },
          function () {
            deferred.reject(null);
          }
        );
        return deferred.promise;
      }

      function isDoiApplicableForMetadata(md) {
        return (
          gnConfig["system.publication.doi.doienabled"] &&
          md.isTemplate === "n" &&
          md.isPublished() &&
          JSON.parse(md.isHarvested) === false
        );
      }

      /**
       * Doi can be published for a resource if:
       *   - Doi publication is enabled.
       *   - The resource matches doi.org url
       *   - The workflow is not enabled for the metadata and
       *     the metadata is published.
       *
       */
      function canPublishDoiForResource(md, doiUrl) {
        if (doiUrl == null || doiUrl.indexOf("doi.org/") === -1) {
          return false;
        }

        var doiKey = gnConfig["system.publication.doi.doikey"];
        var isMdWorkflowEnableForMetadata =
          gnConfig["metadata.workflow.enable"] && md.draft === "y";
        return (
          isDoiApplicableForMetadata(md) &&
          doiUrl.match("doi.org/" + doiKey) !== null &&
          !isMdWorkflowEnableForMetadata
        );
      }

      function checkDoiManagementForResource(md, resource) {
        if (resource.locUrl == null || resource.locUrl.indexOf("doi.org/") === -1) {
          return;
        }
        var doiKey = null;
        getDoiServersForMetadata(md.id).then(function (response) {
          var isMdWorkflowEnableForMetadata =
            gnConfig["metadata.workflow.enable"] && md.draft === "y";
          for (var i = 0; i < response.data.length; i++) {
            if (resource.locUrl.match("doi.org/" + response.data[i].prefix)) {
              doiKey = response.data[i].prefix;
              break;
            }
          }
          if (doiKey !== null) {
            if (
              isDoiApplicableForMetadata(md) &&
              resource.locUrl.match("doi.org/" + doiKey) !== null &&
              !isMdWorkflowEnableForMetadata
            ) {
              resource.canManageDoi = true;
            }
          }
        });
      }

      return {
        check: check,
        create: create,
        isDoiApplicableForMetadata: isDoiApplicableForMetadata,
        canPublishDoiForResource: canPublishDoiForResource,
        getDoiServersForMetadata: getDoiServersForMetadata,
        checkDoiManagementForResource: checkDoiManagementForResource,
        getDoiServerForMetadataAndDoi: getDoiServerForMetadataAndDoi
      };
    }
  ]);
})();
