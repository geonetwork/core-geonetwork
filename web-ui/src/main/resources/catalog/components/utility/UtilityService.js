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

      
      /**
       * Parse boolean value in object
       */
      var parseBoolean = function (object) {
      angular.forEach(object, function(value, key) {
        if (typeof value == 'string') {
          if (value == 'true' || value == 'false') {
            object[key] = (value == 'true');
          } else if (value == 'on' || value == 'off') {
            object[key] = (value == 'on');
          } 
        } else {
          parseBoolean(value);
        }
      });
    };
      
      return {
          scrollTo: scrollTo,
          serialize: serialize,
          parseBoolean: parseBoolean
      };
  };


  module.factory('gnUtilityService', gnUtilityService);
  
  module.filter('gnFromNow', function() {
    return function(dateString) {
      return moment(new Date(dateString)).fromNow()
    };
  });
})();
