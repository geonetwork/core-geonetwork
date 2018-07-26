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
  goog.provide('gn_batch_service');

  var module = angular.module('gn_batch_service', [
  ]);

  /**
   * @ngdoc service
   * @kind function
   * @name gnBatchProcessing
   * @requires gnHttp
   * @requires gnEditor
   * @requires gnCurrentEdit
   * @requires $q
   *
   * @description
   * The `gnBatchProcessing` service is used to run batch processing service
   * on metadatas during the edition. It is mostly used for the online resources
   * management.
   */
  module.factory('gnBatchProcessing', [
    'gnHttp',
    '$http',
    'gnUrlUtils',
    'gnEditor',
    'gnCurrentEdit',
    '$q',
    function(gnHttp, $http, gnUrlUtils, gnEditor, gnCurrentEdit, $q) {

      var processing = true;
      var processReport = null;
      return {

        /**
         * @name gnBatchProcessing#runProcessMd
         *
         * @description
         * Run process md.processing on the edited
         * metadata after the form has been saved.
         *
         * @param {Object} params to add to the request
         * @param {boolean} skipSave doesn't save the metadata editor.
         *                           Used when executing the process for
         *                           a related metadata to the one being edited,
         *                           like linking a service metadata
         *                           to the related dataset.
         * @return {HttpPromise}
         */
        runProcessMd: function(params, skipSave) {
          if (!params.id && !params.uuid) {
            angular.extend(params, {
              id: gnCurrentEdit.id
            });
          }
          var defer = $q.defer();
          gnEditor.save(false, true)
              .then(function() {
                if (!skipSave) {
                  $http.post('../api/records/' + (params.id || params.uuid) +
                    '/processes/' + params.process + '?' +
                    gnUrlUtils.toKeyValue(params)
                  ).then(function(data) {
                    $http.get('../api/records/' + gnCurrentEdit.id + '/editor' +
                      '?currTab=' + gnCurrentEdit.tab).then(function(data) {
                      var snippet = $(data.data);
                      gnEditor.refreshEditorForm(snippet);
                      defer.resolve(data);
                    });
                  }, function(error) {
                    defer.reject(error);
                  });
                } else {
                  $http.post('../api/records/' + (params.id || params.uuid) +
                    '/processes/' + params.process + '?' +
                    gnUrlUtils.toKeyValue(params)
                  ).then(function(data) {
                    defer.resolve(data);
                  });
                }

              }, function(error) {
                defer.reject(error);
              });
          return defer.promise;
        }
      };
    }]);
})();
