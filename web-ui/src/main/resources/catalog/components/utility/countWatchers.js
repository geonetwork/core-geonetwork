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

/**
 * @fileoverview This script provides a window.countWatchers function that
 * the number of Angular watchers in the page.
 *
 * You can do `countWatchers()` in a console to know the current number of
 * watchers.
 *
 * To display the number of watchers every 5 seconds in the console:
 *
 * setInterval(function(){console.log(countWatchers())}, 5000);
 */
(function () {
  goog.provide("gn_count_watchers");

  var root = angular.element(document.getElementsByTagName("body"));

  var countWatchers_ = function (element, scopes, count) {
    var scope;
    scope = element.data().$scope;
    if (scope && !(scope.$id in scopes)) {
      scopes[scope.$id] = true;
      if (scope.$$watchers) {
        count += scope.$$watchers.length;
      }
    }
    scope = element.data().$isolateScope;
    if (scope && !(scope.$id in scopes)) {
      scopes[scope.$id] = true;
      if (scope.$$watchers) {
        count += scope.$$watchers.length;
      }
    }
    angular.forEach(element.children(), function (child) {
      count = countWatchers_(angular.element(child), scopes, count);
    });
    return count;
  };

  window.countWatchers = function () {
    return countWatchers_(root, {}, 0);
  };
})();
