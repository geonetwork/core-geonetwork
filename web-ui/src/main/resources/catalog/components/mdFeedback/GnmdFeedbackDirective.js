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
  goog.provide('gn_md_feedback_directive');

  var module = angular.module('gn_md_feedback_directive', []);

  module
      .directive(
          'gnMdfeedback',
          function() {
            return {
              restrict: 'EC',
              replace: true,
              templateUrl:
              '../../catalog/components/mdFeedback/partials/mdFeedback.html',
              link: function postLink(scope, element, attrs) {

                scope.showLabel = attrs.showLabel;

                $(element).find('.modal').on('hidden.bs.modal', function() {
                  scope.$apply(function() {
                    scope.mdFeedbackOpen = false;
                  });
                });

                scope.$watch('mdFeedbackOpen', function(value) {
                  if (value == true)
                    $(element).find('.modal').modal('show');
                  else
                    $(element).find('.modal').modal('hide');
                });
              }
            };
          });

})();
