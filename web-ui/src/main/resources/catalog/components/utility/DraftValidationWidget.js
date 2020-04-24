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
  goog.provide('gn_draftvalidationwidget');

  var module = angular.module('gn_draftvalidationwidget', []);

  module.directive('gnDraftValidationWidget', ['gnSearchManagerService',
    function(gnSearchManagerService) {
      return {
        restrict: 'E',
        scope: {
          metadata: '='
        },
        link: function(scope) {
          gnSearchManagerService.gnSearch({
            uuid: scope.metadata["geonet:info"].uuid,
            _isTemplate: 'y or n',
            _draft: 'y',
            fast: 'index',
            _content_type: 'json'
          }).then(function(data) {
            var i = 0;
            data.metadata.forEach(function (md, index) {
              if(md.draft == 'y') {
                // This will only happen if the draft exists
                // and the user can see it
                scope.draft = data.metadata[i];   
              }
            });
          });
          
        },
        templateUrl: '../../catalog/components/utility/' +
            'partials/draftvalidationwidget.html'
      };
    }
  ]);

})();
