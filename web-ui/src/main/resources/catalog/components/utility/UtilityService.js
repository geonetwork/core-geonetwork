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
      
      /**
       * Serialize form including unchecked checkboxes.
       * See http://forum.jquery.com/topic/jquery-serialize-unchecked-checkboxes
       */
      var serialize = function (formId) {
          var form = $(formId), uc = [];
          $(':checkbox:not(:checked)', form).each(function(){
            uc.push(encodeURIComponent(this.name) + '=false');
          });
          return form.serialize() + 
            (uc.length ? '&' + uc.join('&').replace(/%20/g, "+") : '');
      };
      
      return {
          scrollTo: scrollTo,
          serialize: serialize
      };
  };


  module.factory('gnUtilityService', gnUtilityService);

})();
