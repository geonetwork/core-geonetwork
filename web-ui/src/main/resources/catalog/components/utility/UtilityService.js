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
  goog.provide('gn_utility_service');

  goog.require('gn_popup');

  var module = angular.module('gn_utility_service', ['gn_popup']);

  module.factory('RecursionHelper', ['$compile', function($compile) {
    return {
      /**
       * Manually compiles the element, fixing the recursion loop.
       * @param {Object} element
       * @param {Function} [link] A post-link function, or an object
       * with function(s) registered via pre and post properties.
       * @return {Object} An object containing the linking functions.
       */
      compile: function(element, link) {
        // Normalize the link parameter
        if (angular.isFunction(link)) {
          link = { post: link };
        }

        // Break the recursion loop by removing the contents
        var contents = element.contents().remove();
        var compiledContents;
        return {
          pre: (link && link.pre) ? link.pre : null,
          /**
           * Compiles and re-adds the contents
           */
          post: function(scope, element) {
            // Compile the contents
            if (!compiledContents) {
              compiledContents = $compile(contents);
            }
            // Re-add the compiled contents to the element
            compiledContents(scope, function(clone) {
              element.append(clone);
            });

            // Call the post-linking function, if any
            if (link && link.post) {
              link.post.apply(null, arguments);
            }
          }
        };
      }
    };
  }]);

  module.factory('gnUtilityService',
      ['gnPopup', '$translate', '$location',
        function(gnPopup, $translate, $location) {
        /**
       * Scroll page to element.
       */
        var scrollTo = function(elementId, offset, duration, easing) {
          var top = 0, e = $(elementId);
          if (elementId !== undefined &&  e.length == 1) {
            top = offset ?
             e.offset().top :
             e.position().top;
          }
          $('body,html').animate({scrollTop: top},
           duration, easing, function () {
              if (e.length == 1) {
                e.fadeOut('slow', function () {e.fadeIn()});
              }
            });
          $location.search('scrollTo', elementId);
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
          return form.serialize().replace(/=on&/g, '=true&').
           replace(/=on$/, '=true') +
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
          return parameterValue != null ?
          decodeURIComponent(parameterValue) : undefined;
        };

        var CSVToArray = function(strData, strDelimiter) {
          strDelimiter = (strDelimiter || ',');
          var objPattern = new RegExp(
           (
              '(\\' + strDelimiter + '|\\r?\\n|\\r|^)' +
              '(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|' +
              '([^\"\\' + strDelimiter + '\\r\\n]*))'
           ),
           'gi'
           );
          var arrData = [[]];
          var arrMatches = null;
          while (arrMatches = objPattern.exec(strData)) {
            var strMatchedDelimiter = arrMatches[1];
            if (
             strMatchedDelimiter.length &&
             strMatchedDelimiter !== strDelimiter
            ) {
              arrData.push([]);
            }

            var strMatchedValue;
            if (arrMatches[2]) {
              strMatchedValue = arrMatches[2].replace(
               new RegExp('\"\"', 'g'),
               '\"');
            } else {
              strMatchedValue = arrMatches[3];
            }
            arrData[arrData.length - 1].push(strMatchedValue);
          }
          return (arrData);
        };

        /**
     * If object property is not an array, make it an array
     * @param {Object} object
     * @param {String} key
     * @param {String|Object} value
     * @param {String} propertyName
     */
        var formatObjectPropertyAsArray = function(object,
         key, value,
         propertyName) {
          if (key === propertyName && !$.isArray(object[key])) {
            object[key] = [value];
          }
        };

        /**
     * Traverse an object tree
     *
     * @param {Object} o The object
     * @param {Function} func  The function to apply to all object properties
     * @param {String|Object|Array} args  The argument to pass to the function.
     * @return {Object} the object (optionnaly affected by the function)
     */
        function traverse(o, func, args) {
          for (var i in o) {
            func.apply(this, [o, i, o[i], args]);
            if (o[i] !== null && typeof(o[i]) == 'object') {
              //going on step down in the object tree!!
              traverse(o[i], func, args);
            }
          }
          return o;
        };

        function randomUuid() {
          return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
           /[xy]/g,
           function(c) {
             var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
             return v.toString(16);
           });
        }


        /**
     * Get html formatter link for the given md
     * @param {String} title
     * @param {String} url
     */
        function getPermalink(title, url) {
          gnPopup.createModal({
            title: $translate.instant('permalinkTo', {title: title}),
            content: '<div gn-permalink-input="' + url + '"></div>'
          });
        };

        return {
          scrollTo: scrollTo,
          isInView: isInView,
          serialize: serialize,
          parseBoolean: parseBoolean,
          traverse: traverse,
          formatObjectPropertyAsArray: formatObjectPropertyAsArray,
          toCsv: toCsv,
          CSVToArray: CSVToArray,
          getUrlParameter: getUrlParameter,
          randomUuid: randomUuid,
          getPermalink: getPermalink
        };
      }]);


  module.filter('gnFromNow', function() {
    return function(dateString) {
      return moment(new Date(dateString)).fromNow();
    };
  });

  /**
   * Return the object value in requested lang or the first value.
   */
  module.filter('gnLocalized', ['gnGlobalSettings', function(gnGlobalSettings) {
    return function(obj, lang) {
    lang = lang || gnGlobalSettings.iso3lang;
      if (angular.isObject(obj)) {
        return obj[lang] ? obj[lang] : (obj[Object.keys(obj)[0]] || '');
      } else {
        return '';
      }
    };
  }]);

  module.factory('gnRegionService', [
    '$q',
    '$http',
    function($q, $http) {

      /**
      * Array of available region type
      * [{
      *   id: 'id="http://geonetwork-opensource.org/regions#country'
      *   name: 'country'
      * }]
      */
      var regionsList = [];
      var listDefer;

      return {

        regionsList: regionsList,

        /**
        * Load a region of the given type
        * Return an array of the region labels in
        * the given language.
        */
        loadRegion: function(type, lang) {
          var defer = $q.defer();

          $http.get('../api/regions', {
            params: {
              categoryId: type.id
            },
            cache: true
          }).success(function(response) {
            var data = response.region;

            // Compute default name and add a
            // tokens element which is used for filter
            angular.forEach(data, function(country) {
              country.tokens = [];
              angular.forEach(country.label, function(label) {
                country.tokens.push(label);
              });
              country.name = country.label[lang] || country.label[0];
            });
            defer.resolve(data);
          });

          return defer.promise;
        },

        /**
        * Load the list of all types of region.
        * Return an array of region composed by an id and a name.
        * See regionList variable.
        */
        loadList: function() {
          if (!listDefer) {
            listDefer = $q.defer();
            $http.get('../api/regions/types').success(function(data) {
              angular.forEach(data, function(value, key) {
                if (value.id) {
                  var tokens = value.id.split('#'),
                      asHash = tokens.length > 0,
                      name = asHash ? tokens[1] : value.id;
                  regionsList.push({
                    id: value.id,
                    name: name,
                    label: value.label || name
                  });
                }
              });
              listDefer.resolve(regionsList);
            });
          }
          return listDefer.promise;
        }
      };
    }]);

  module.service('gnTreeFromSlash', [function() {
    var findChild = function(node, name) {
      var n;
      if (node.nodes) {
        for (var i = 0; i < node.nodes.length; i++) {
          n = node.nodes[i];
          if (name == n.name) {
            return n;
          }
        }
      }
    };
    var sortNodeFn = function(a, b) {
      var aName = a.name;
      var bName = b.name;
      if (aName < bName) return -1;
      if (aName > bName) return 1;
      return 0;
    };

    var createNode = function(node, g, index, e) {
      var group = g[index];
      if (group) {
        var newNode = findChild(node, group);
        if (!newNode) {
          newNode = {
            name: group,
            value: group
            //selected: themesInSearch.indexOf(t['@name']) >= 0 ? true : false
          };
          if (!node.nodes) node.nodes = [];
          node.nodes.push(newNode);
          //node.nodes.sort(sortNodeFn);
        }
        createNode(newNode, g, index + 1, e);
      } else {
        node.key = e.key;
        node.count = e.doc_count;
      }
    };

    this.getTree = function(list) {
      var tree = {
        nodes: []
      };
      list.forEach(function(e) {
        var name = e.key;
        var g = name.split('/');
        createNode(tree, g, 0, e);
      });
      return tree;
    };
  }]);
})();
