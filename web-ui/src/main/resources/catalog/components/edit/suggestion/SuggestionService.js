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
  goog.provide('gn_suggestion_service');

  var module = angular.module('gn_suggestion_service', [
  ]);

  module.factory('gnSuggestion', [
    'gnBatchProcessing',
    'gnHttp',
    '$http',
    'gnEditor',
    'gnCurrentEdit',
    function(gnBatchProcessing, gnHttp, $http, gnEditor, gnCurrentEdit) {

      var reload = false;
      var current = undefined;
      var callbacks = [];

      /**
       * gnSuggestion service PUBLIC METHODS
       * - load
       *******************************************
       */
      return {

        /**
         * Called on suggestion click in the list.
         * Either open a popup with a form or directly
         * execute the process.
         */
        onSuggestionClick: function(sugg) {
          this.setCurrent(sugg);
          if (sugg.params) {
            $('#runsuggestion-popup').modal('show');
            this.dispatch();
          } else {
            this.runProcess(sugg.process);
          }
        },

        /**
         * Directives can register callback that will be
         * executed when other call the dispatcher.
         * This is use because the suggestion content is in
         * a popup, whom scope can't be accessed by gnSuggestionList
         */
        register: function(cb) {
          callbacks.push(cb);
        },
        dispatch: function() {
          for (var i = 0; i < callbacks.length; ++i) {
            callbacks[i]();
          }
        },

        /**
         * Save current state to share suggestion between all
         * directives.
         */
        setCurrent: function(sugg) {
          current = sugg;
        },
        getCurrent: function() {
          return current;
        },

        /**
         * Call GN service to load all suggestion for the current
         * metadata
         */
        load: function(lang, gurl) {
          return $http.get('../api/records/' + gnCurrentEdit.id + '/processes');
        },

        runProcess: function(service, params) {
          var scope = this;
          if (angular.isUndefined(params)) {
            params = {};
          }
          params.process = service;
          return gnBatchProcessing.runProcessMd(params).then(function(data) {
            scope.reload = true;
          }, function(error) {
            console.warn(error);
            scope.reload = true;
          });
        }
      };
    }]);
})();
