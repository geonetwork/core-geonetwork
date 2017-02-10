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
  goog.provide('gn_md_feedback_controller');

  var module = angular.module('gn_md_feedback_controller', []);

  module.controller('gnMdFeedbackController', [
    '$scope', '$http', 'gnConfig',
    function($scope, $http, gnConfig) {
      $scope.isFeedbackEnabled = gnConfig[gnConfig.key.isFeedbackEnabled];

      $scope.mdFeedbackOpen = false;
      $scope.toggle = function() {
        $scope.mdFeedbackOpen = !$scope.mdFeedbackOpen;
      };

      $scope.send = function(form, formId) {
        if (form.$valid) {
          $http({
            url: 'contact.send?_content_type=json',
            method: 'POST',
            data: $(formId).serialize(),
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          }).then(function(response) {
            // TODO: report no email sent
            if (response.status === 200) {
              $scope.success = true;
            } else {
              $scope.success = false;
            }
          });
        }
      };
    }
  ]);

})();
