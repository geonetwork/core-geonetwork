(function() {
  goog.provide('gn_utility_service');

  var module = angular.module('gn_utility_service', []);

  var gnUtilityService = function () {
      /**
       * Scroll page to element. 
       */
      var scrollTo = function (elementId, duration, easing) {
          $(document.body).animate({scrollTop: $(elementId).offset().top}, 
                  duration, easing);
      };
      return {
          scrollTo: scrollTo
      };
  };


  module.factory('gnUtilityService', gnUtilityService);

})();
