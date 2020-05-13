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
    'gnGlobalSettings',
    'gnLangs',
    '$filter',
    '$location',
    function($window, gnGlobalSettings, gnLangs, $filter, $location) {
      /**
       * Url pattern for metadata page
       * @type {string}
       */
      var baseMdUrl = $location.absUrl().split('#')[0] + '#/metadata/';

      /**
       * Settings related to external viewer
       * @type {Object}
       */
      var settings = gnGlobalSettings.gnCfg.mods.map.externalViewer;

      return {
        /**
         * @ngdoc method
         * @methodOf gn_external_viewer.service:gnExternalViewer
         * @name gnExternalViewer#isEnabled
         *
         * @description
         * Simple check against UI settings to see if an external viewer is
         * enabled. If no base URL is defined, the feature will be disabled.
         *
         * @return {boolean} true if enabled
         */
        isEnabled: function() {
          return !!settings.enabled && !!settings.baseUrl &&
            !!settings.urlTemplate;
        },
        isEnabledViewAction: function() {
          return !!settings.enabledViewAction && !!settings.urlTemplate;
        },

        /**
         * @ngdoc method
         * @methodOf gn_external_viewer.service:gnExternalViewer
         * @name gnExternalViewer#getBaseUrl
         *
         * @description
         * Returns the base URL as defined in the settings; will return
         * an empty string if disabled.
         *
         * @return {string} empty if disabled
         */
        getBaseUrl: function() {
          return this.isEnabled() ? settings.baseUrl : '';
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
          if (!(this.isEnabled() || this.isEnabledViewAction())) { return; }

          md.url = md.uuid ? baseMdUrl + md.uuid : '';

          this._toView.push({
            md: md,
            service: service
          });

          // ask for a commit (will be executed once all services are requested
          var commitFunction = this._commit.bind(this);
          setTimeout(commitFunction);
        },

        _toView: [],

        /**
         * internal method: called once all services have been requested
         */
        _commit: function() {
          if (!this._toView.length) { return; }

          var getValues = function(object, key) {
            return this._toView.map(function (entry) {
              var value = entry[object][key];
              return encodeURIComponent($filter('gnLocalized')(value) || value || '');
            }).join(settings.valuesSeparator || ',') || ''
          }.bind(this);
          var url = settings.urlTemplate
            .replace('${iso2lang}', gnLangs.getIso2Lang())
            .replace('${iso3lang}', gnLangs.getIso3Lang())
            .replace('${md.id}', getValues('md', 'id'))
            .replace('${md.uuid}', getValues('md', 'uuid'))
            .replace('${md.defaultTitle}', getValues('md', 'defaultTitle'))
            .replace('${md.url}', getValues('md', 'url'))
            .replace('${service.url}', getValues('service', 'url'))
            .replace('${service.type}', getValues('service', 'type'))
            .replace('${service.name}', getValues('service', 'name'))
            .replace('${service.title}', getValues('service', 'title'));

          settings.openNewWindow ? $window.open(url, '_blank') :
            $window.location = url;

          // reset list of services to view
          this._toView.length = 0;
        }
      };
    }
  ]);
})();
