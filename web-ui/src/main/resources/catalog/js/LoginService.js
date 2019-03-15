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

  goog.provide('gn_login_service');

  var module = angular.module('gn_login_service', []);

  /**
   * Take care of sign in/out
   */
  module.factory('gnLoginService',
      ['$http',
       function($http) {

          // Enable login in third party app. eg. geoserver
          // var thirdPartyAuthApp = {
          //   signin: '/geoserver/j_spring_security_check',
          //   signout: '/geoserver/j_spring_security_logout'
          // };
          var thirdPartyAuthApp = undefined;

          return {
            /**
             * Sign in first in the thirdparty app if one provided,
             * and then in the catalogue.
             *
             * @param formId
             * @param u
             * @param p
             */
            signin: function(formId, u, p) {
              formId = '#' + formId;
              if (thirdPartyAuthApp) {
                $http.post(window.location.origin + thirdPartyAuthApp.signin,
                  $.param({
                    username: u,
                    password: p
                  }), {
                    headers: {
                      'Content-Type':
                        'application/x-www-form-urlencoded'
                    }
                  }).then(function (r) {
                  $(formId).get(0).submit()
                }, function (r) {
                    console.warn(
                      "Failed to authenticate on third party app using URL "
                      + thirdPartyAuthApp.signin
                      + ". Response status is " + r.status);
                  $(formId).get(0).submit()
                });
              } else {
                $(formId).get(0).submit()
              }
            },
            /**
             * Sign out in third party app first if any, then sign out from the catalogue.
             * @param url
             */
            signout: function(url) {
              if (thirdPartyAuthApp) {
                $http.get(window.location.origin + thirdPartyAuthApp.signout).then(
                  function (r) {
                    window.location = url;
                  },
                  function (r) {
                    window.location = url;
                  }
                );
              } else {
                window.location = url;
              }
            }
          };
       }]);

})();
