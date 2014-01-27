(function() {
  goog.provide('gn_utility_service');

  var module = angular.module('gn_utility_service', []);

  var gnUtilityService = function() {
    /**
       * Scroll page to element.
       */
    var scrollTo = function(elementId, offset, duration, easing) {
      $(document.body).animate({scrollTop: (offset ?
          $(elementId).offset().top :
          $(elementId).position().top)
      },
      duration, easing);
    };

    /**
     * Return true if element is in browser viewport
     */
    var isInView = function(elem) {
      var docViewTop = $(window).scrollTop();
      var docViewBottom = docViewTop + window.innerHeight;

      var elemTop = parseInt($(elem).offset().top, 10);
      var elemBottom = parseInt(elemTop + $(elem).height(), 10);

      return ( // bottom of element in view
              elemBottom < docViewBottom &&
              elemBottom > docViewTop) ||
          // top of element in view
          (elemTop > docViewTop && elemTop < docViewBottom) ||
              // contains view
              (elemTop < docViewTop && elemBottom > docViewBottom);
    };

    /**
       * Serialize form including unchecked checkboxes.
       * See http://forum.jquery.com/topic/jquery-serialize-unchecked-checkboxes
       */
    var serialize = function(formId) {
      var form = $(formId), uc = [];
      $(':checkbox:not(:checked)', form).each(function() {
        uc.push(encodeURIComponent(this.name) + '=false');
      });
      return form.serialize() +
          (uc.length ? '&' + uc.join('&').replace(/%20/g, '+') : '');
    };


    /**
       * Parse boolean value in object
       */
    var parseBoolean = function(object) {
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
    /**
       * Converts a value to a string appropriate for entry
       * into a CSV table.  E.g., a string value will be surrounded by quotes.
       * @param {string|number|object} theValue
       * @param {string} sDelimiter The string delimiter.
       *    Defaults to a double quote (") if omitted.
       */
    function toCsvValue(theValue, sDelimiter) {
      var t = typeof (theValue), output;

      if (typeof (sDelimiter) === 'undefined' || sDelimiter === null) {
        sDelimiter = '"';
      }

      if (t === 'undefined' || t === null) {
        output = '';
      } else if (t === 'string') {
        output = sDelimiter + theValue + sDelimiter;
      } else {
        output = String(theValue);
      }

      return output;
    }
    /**
       * https://gist.github.com/JeffJacobson/2770509
       * Converts an array of objects (with identical schemas)
       * into a CSV table.
       *
       * @param {Array} objArray An array of objects.
       *    Each object in the array must have the same property list.
       * @param {string} sDelimiter The string delimiter.
       *    Defaults to a double quote (") if omitted.
       * @param {string} cDelimiter The column delimiter.
       *    Defaults to a comma (,) if omitted.
       * @return {string} The CSV equivalent of objArray.
       */
    function toCsv(objArray, sDelimiter, cDelimiter) {
      var i, l, names = [], name, value, obj, row, output = '', n, nl;

      // Initialize default parameters.
      if (typeof (sDelimiter) === 'undefined' || sDelimiter === null) {
        sDelimiter = '"';
      }
      if (typeof (cDelimiter) === 'undefined' || cDelimiter === null) {
        cDelimiter = ',';
      }

      for (i = 0, l = objArray.length; i < l; i += 1) {
        // Get the names of the properties.
        obj = objArray[i];
        row = '';
        if (i === 0) {
          // Loop through the names
          for (name in obj) {
            if (obj.hasOwnProperty(name)) {
              names.push(name);
              row += [sDelimiter, name, sDelimiter, cDelimiter].join('');
            }
          }
          row = row.substring(0, row.length - 1);
          output += row;
        }

        output += '\n';
        row = '';
        for (n = 0, nl = names.length; n < nl; n += 1) {
          name = names[n];
          value = obj[name];
          if (n > 0) {
            row += ',';
          }
          row += toCsvValue(value, '"');
        }
        output += row;
      }

      return output;
    }
    /**
       * Get a URL parameter
       */
    var getUrlParameter = function(parameterName) {
      var parameterValue = null;
      angular.forEach(window.location
                  .search.replace('?', '').split('&'),
          function(value) {
            if (value.indexOf(parameterName) === 0) {
              parameterValue = value.split('=')[1];
            }
            // TODO; stop loop when found
          });
      return parameterValue;
    };
    return {
      scrollTo: scrollTo,
      isInView: isInView,
      serialize: serialize,
      parseBoolean: parseBoolean,
      toCsv: toCsv,
      getUrlParameter: getUrlParameter
    };
  };


  module.factory('gnUtilityService', gnUtilityService);

  module.filter('gnFromNow', function() {
    return function(dateString) {
      return moment(new Date(dateString)).fromNow();
    };
  });
})();
