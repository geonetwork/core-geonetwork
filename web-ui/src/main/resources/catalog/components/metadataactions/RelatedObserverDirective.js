/**
 * Created by juanluisrp on 30/08/2016.
 */
(function () {
  goog.provide('gn_related_observer_directive');

  var module = angular.module('gn_related_observer_directive', []);


  module.directive('gnRelatedObserver', [
    function () {

      function GnRelatedObserverController(scope, $rootScope) {
        this.managedAnnotations = [];
        this.currentRequests = [];
        this.scope = scope.$parent;
        this.scope.gnRelatedLoadFinished = true;
        this.scope.relatedsFound = false;
        this.rootScope = $rootScope;
      }

      GnRelatedObserverController.prototype.registerGnRelated = function(gnRelated) {
        this.managedAnnotations.push(gnRelated);
      };

      GnRelatedObserverController.prototype.unregisterGnRelated = function(gnRelated) {
        this.managedAnnotations = $.grep(this.managedAnnotations, function(elem) {
          return elem != gnRelated;
        })
      };

      GnRelatedObserverController.prototype.startGnRelatedRequest = function(gnRelated) {
        if(this.scope.gnRelatedLoadFinished) {
          this.scope.relatedsFound = false;
        }
        this.currentRequests.push(gnRelated);
        this.scope.gnRelatedLoadFinished = false;
      };

      GnRelatedObserverController.prototype.finishRequest = function(gnRelatedElement, found) {
        var index = $.inArray(gnRelatedElement, this.currentRequests);
        if (index != -1) {
          this.currentRequests.splice(index, 1);
          if (found) {
            this.scope.relatedsFound = true;
          }
        }
        this.updateGnRelatedLoadFinished();
      };

      GnRelatedObserverController.prototype.updateGnRelatedLoadFinished = function() {
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
        templateUrl: '../../catalog/components/metadataactions/partials/relatedObserverTemplate.html',
        scope: {},
        controller: ['$scope', '$rootScope', GnRelatedObserverController],
        controllerAs: 'observerController',
        link: function(scope, element, attrs, controller) {

        }
      };
    }
  ]);
})();
