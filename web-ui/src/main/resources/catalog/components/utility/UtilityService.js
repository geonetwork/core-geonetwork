(function() {
  goog.provide('gn_utility_service');

  var module = angular.module('gn_utility_service', []);

  var gnUtilityService = function () {
      /**
       * Scroll page to element. 
       */
      var scrollTo = function (elementId, offset, duration, easing) {
          $(document.body).animate({scrollTop: (offset ?
                  $(elementId).offset().top :
                  $(elementId).position().top)
                  }, 
                  duration, easing);
      };
      return {
          scrollTo: scrollTo
      };
  };


  module.factory('gnUtilityService', gnUtilityService);

})();
