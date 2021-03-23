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
  goog.provide('gn_search_location');

  var module = angular.module('gn_search_location', []);

  module.service('gnSearchLocation', [
    '$location',
    '$rootScope',
    '$timeout',
    'gnGlobalSettings',
    'gnUrlUtils',
    'gnExternalViewer',
    function($location,
      $rootScope,
      $timeout,
      gnGlobalSettings,
      gnUrlUtils,
      gnExternalViewer) {

      this.SEARCH = '/search';
      this.SEARCHPAGES = /\/search|\/board/;
      this.MAP = '/map';
      this.METADATA = '/metadata/';
      this.DRAFT = '/metadraf/';
      this.HOME = '/home';

      var state = {};
      var that = this;

      /** ---- get methods from $location ---- **/
      this.absUrl = function() {
        return $location.absUrl();
      };
      this.host = function() {
        return $location.host();
      };
      this.path = function(path) {
        return $location.path(path);
      };
      /** ---- **/

      this.isSearch = function(path) {
        return (path || $location.path()).match(this.SEARCHPAGES) !== null;
      };

      this.isMdView = function(path) {
        var p = path || $location.path();
        return p.indexOf(this.METADATA) == 0
            || p.indexOf(this.DRAFT) == 0;
      };

      this.isMap = function() {
        return $location.path() == this.MAP;
      };

      this.isHome = function() {
        return $location.path() == this.HOME;
      };

      this.isUndefined = function() {
        return angular.isUndefined($location.path()) ||
            $location.path() == '';
      };
      this.getFormatter = function() {
        var tokens = $location.path().split('/');
        if (tokens.length > 2 && tokens[3] === 'formatters') {
          return '/formatters/' + $location.url().split('/formatters/')[1];
        } else {
          return undefined;
        }
      };
      this.getFormatterPath = function() {
        var tokens = $location.path().split('/');
        if (tokens.length > 2 && tokens[3] === 'formatters') {
          var formatterInfo =  $location.url().replace("/metadraf/", "").replace("/metadata/", "");
          return '../api/records/' + formatterInfo;
        } else {
          return undefined;
        }
      };
      this.setUuid = function(uuid, formatter) {
        var newPath = (
            $location.path().indexOf(this.DRAFT) == 0 ? this.DRAFT : this.METADATA) +
          uuid +
          (formatter == undefined || formatter == ''? '' : formatter.split('?')[0]);
        if (newPath != $location.path()) {
          this.removeParams();
          if (formatter && formatter.indexOf('?') !== -1) {
            $location.search(gnUrlUtils.parseKeyValue(formatter.split('?')[1]));
          }
          $location.path(newPath);
        }
      };

      this.getUuid = function() {
        if (this.isMdView()) {
          return $location.path().split('/')[2];
        }
      };

      this.setMap = function() {
        if (gnGlobalSettings.isMapViewerEnabled &&
            !gnExternalViewer.isEnabled()) {
          $location.path(this.MAP);
        }
      };

      this.setSearch = function(params) {
        if (!this.isSearch()) {
          $location.path(this.SEARCH);
        }
        if (params) {
          $location.search(params);
        }
      };
      this.removeParams = function() {
        $location.search('');
      };
      this.getParams = function() {
        return $location.search();
      };

      this.setHome = function() {
        $location.path(this.HOME);
        $location.search({});
      };

      this.restoreSearch = function() {
        this.removeParams();
        this.setSearch(state.lastSearchParams);

        //Wait all location search are triggered
        $timeout(function() {
          that.lastSearchUrl = '';
        }, 100);
      };

      this.initTabRouting = function(tabs) {
        var that = this;
        var updateTabs = function() {
          var tab = $location.path().
              match(/^\/([a-zA-Z0-9]*)($|\/.*)/)[1];

          tabs[tab].active = true;
        };
        updateTabs();
        $rootScope.$on('$locationChangeSuccess', updateTabs);
      };


      /**
       * Keep history and state of routing for to keep the search state.
       * Actually, if you had run a search, then moved to another location,
       * when you get back to the search, the params are kept and the search
       * is not fired again.
       */
      var initSearchRouting = function(evt, newUrl, oldUrl) {
        state.old = state.current || {path: ''};
        state.current = {
          params: $location.search(),
          path: $location.path()
        };
        if (state.old.path != that.SEARCH &&
            state.current.path == that.SEARCH) {
          if (that.isMdView(state.old.path)) {
            $rootScope.$broadcast('locationBackToSearchFromMdview');
          }
          $rootScope.$broadcast('locationBackToSearch');
        }
        if (that.isSearch(state.old.path) &&
            !that.isSearch(state.current.path)) {
          state.lastSearchParams = state.old.params;
          that.lastSearchUrl = oldUrl;
        }
      };
      initSearchRouting();
      $rootScope.$on('$locationChangeSuccess', initSearchRouting);

    }
  ]);
})();
