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
  goog.provide('gn_related_observer_directive');

  var module = angular.module('gn_related_observer_directive', []);


  module.directive('gnRelatedObserver', [
    function() {

      function GnRelatedObserverController(scope, $rootScope) {
        this.managedAnnotations = [];
        this.currentRequests = [];
        this.scope = scope.$parent;
        this.scope.gnRelatedLoadFinished = true;
        this.scope.relatedsFound = false;
        this.rootScope = $rootScope;
      }

      GnRelatedObserverController.prototype.registerGnRelated =
          function(gnRelated) {
        this.managedAnnotations.push(gnRelated);
      };

      GnRelatedObserverController.prototype.unregisterGnRelated =
          function(gnRelated) {
        this.managedAnnotations = $.grep(this.managedAnnotations,
            function(elem) {
              return elem != gnRelated;
            });
      };

      GnRelatedObserverController.prototype.startGnRelatedRequest =
          function(gnRelated) {
        if (this.scope.gnRelatedLoadFinished) {
          this.scope.relatedsFound = false;
        }
        this.currentRequests.push(gnRelated);
        this.scope.gnRelatedLoadFinished = false;
      };

      GnRelatedObserverController.prototype.finishRequest =
          function(gnRelatedElement, found) {
        var index = $.inArray(gnRelatedElement, this.currentRequests);
        if (index != -1) {
          this.currentRequests.splice(index, 1);
          if (found) {
            this.scope.relatedsFound = true;
          }
        }
        this.updateGnRelatedLoadFinished();
      };

      GnRelatedObserverController.prototype.updateGnRelatedLoadFinished =
          function() {
        if (this.currentRequests.length == 0) {
          this.scope.gnRelatedLoadFinished = true;
          if (!this.scope.relatedsFound) {
            this.rootScope.$broadcast('tabChangeRequested', 'general');
          }
        }
      };



      return {
        restrict: 'A',
        transclude: true,
        templateUrl: '../../catalog/components/' +
            'metadataactions/partials/relatedObserverTemplate.html',
        scope: {},
        controller: ['$scope', '$rootScope',
          GnRelatedObserverController],
        controllerAs: 'observerController',
        link: function(scope, element, attrs, controller) {

        }
      };
    }
  ]);
})();
