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

(function() {
  goog.provide('gn_external_viewer');

  goog.require('gn_ows');


  var module = angular.module('gn_external_viewer', [
    'gn_ows'
  ]);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_external_viewer.service:gnExternalViewer
   *
   * @description
   * The `gnExternalViewer` service is responsible for redirecting users to
   * a third party viewer app when this feature is enabled in the UI settings.
   */
  module.service('gnExternalViewer', [
    '$window',
    'gnMap',
    '$location',
    function($window, gnMap, $location) {
      /**
       * Url pattern for metadata page
       * @type {string}
       */
      var baseMdUrl = $location.absUrl().split('#')[0] + '#/metadata/';

      return {
        /**
         * @ngdoc method
         * @methodOf gn_external_viewer.service:gnExternalViewer
         * @name gnExternalViewer#isEnabled
         *
         * @description
         * Simple check against UI settings to see if an external viewer is
         * enabled.
         *
         * @return {boolean} true if enabled
         */
        isEnabled: function() {
          return !!gnMap.getMapConfig().externalViewer.enabled;
        },

        /**
         * @ngdoc method
         * @methodOf gn_external_viewer.service:gnExternalViewer
         * @name gnExternalViewer#openService
         *
         * @description
         * Does the actual redirection inc. url parameters replacement.
         *
         * @param {Object} md expected properties: uuid, id, url
         * @param {Object} service expected properties: url, name, type, title
         */
        viewService: function(md, service) {
          var settings = gnMap.getMapConfig().externalViewer;
          if (!this.isEnabled()) { return; }

          var mdUrl = md.uuid ? baseMdUrl + md.uuid : '';

          var url = settings.urlTemplate
            .replace('${md.id}', md.id || '')
            .replace('${md.uuid}', md.uuid || '')
            .replace('${md.url}', mdUrl)
            .replace('${service.url}', service.url || '')
            .replace('${service.type}', service.type || '')
            .replace('${service.name}', service.name || '')
            .replace('${service.title}', service.title || '');

          $window.open(url, settings.openNewWindow ? '_blank' : undefined);
        }
      };
    }
  ]);
})();
