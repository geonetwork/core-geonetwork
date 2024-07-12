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

(function () {
  goog.provide("gn_login_controller");

  goog.require("gn_catalog_service");
  goog.require("gn_utility");

  var module = angular.module("gn_login_controller", [
    "gn_utility",
    "gn_catalog_service",
    "vcRecaptcha"
  ]);

  /**
   *    Take care of login action, reset and update password.
   */
  module.controller("GnLoginController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    "$location",
    "$window",
    "$timeout",
    "gnUtilityService",
    "gnConfig",
    "gnGlobalSettings",
    "vcRecaptchaService",
    "$q",
    "gnLangs",
    function (
      $scope,
      $http,
      $rootScope,
      $translate,
      $location,
      $window,
      $timeout,
      gnUtilityService,
      gnConfig,
      gnGlobalSettings,
      vcRecaptchaService,
      $q,
      gnLangs
    ) {
      $scope.formAction = "../../signin#" + $location.url();
      $scope.registrationStatus = null;
      $scope.sendPassword = false;
      $scope.password = null;
      $scope.passwordCheck = null;
      $scope.userToRemind = null;
      $scope.changeKey = null;

      $scope.recaptchaEnabled = gnConfig["system.userSelfRegistration.recaptcha.enable"];
      $scope.recaptchaKey = gnConfig["system.userSelfRegistration.recaptcha.publickey"];
      $scope.resolveRecaptcha = false;

      $scope.redirectUrl = gnUtilityService.getUrlParameter("redirect");
      $scope.signinFailure = gnUtilityService.getUrlParameter("failure");
      $scope.gnConfig = gnConfig;
      $scope.isDisableLoginForm = gnGlobalSettings.isDisableLoginForm;
      $scope.isShowLoginAsLink = gnGlobalSettings.isShowLoginAsLink;
      $scope.isUserProfileUpdateEnabled = gnGlobalSettings.isUserProfileUpdateEnabled;

      // take the bigger of the two values
      $scope.passwordMinLength = Math.max(
        gnConfig["system.security.passwordEnforcement.minLength"],
        6
      );
      $scope.passwordMaxLength = Math.max(
        gnConfig["system.security.passwordEnforcement.maxLength"],
        6
      );
      $scope.passwordPattern = gnConfig["system.security.passwordEnforcement.pattern"];

      function initForm() {
        if ($window.location.pathname.indexOf("new.password") !== -1) {
          // Retrieve username from URL parameter
          $scope.userToRemind = gnUtilityService.getUrlParameter("username");
          $scope.changeKey = gnUtilityService.getUrlParameter("changeKey");
        }

        $scope.retrieveGroups();
      }

      // TODO: https://github.com/angular/angular.js/issues/1460
      // Browser autofill does not work properly
      $timeout(function () {
        $("input[data-ng-model], select[data-ng-model]").each(function () {
          angular.element(this).controller("ngModel").$setViewValue($(this).val());
        });
      }, 300);

      $scope.setSendPassword = function (value) {
        $scope.sendPassword = value;
        $("#username").focus();
      };

      var showForgotPassword = gnUtilityService.getUrlParameter("showforgotpassword");

      if (showForgotPassword && showForgotPassword === "true") {
        $scope.setSendPassword(true);
      }

      /**
       * Register user. An email will be sent to the new
       * user and another to the catalog admin if a profile
       * higher than registered user is requested.
       */
      $scope.userInfo = {
        username: "",
        surname: "",
        name: "",
        email: "",
        organisation: "",
        profile: "RegisteredUser",
        group: "",
        address: {
          address: "",
          city: "",
          country: "",
          state: "",
          zip: ""
        }
      };

      $scope.retrieveGroups = function () {
        $http.get("../api/groups").then(
          function (response) {
            $scope.groups = response.data;
          },
          function (response) {}
        );
      };

      $scope.updateGroupSelection = function () {
        if ($scope.userInfo.profile === "Administrator") {
          $scope.userInfo.group = "";
        }
      };

      $scope.register = function () {
        if ($scope.recaptchaEnabled) {
          if (vcRecaptchaService.getResponse() === "") {
            $scope.resolveRecaptcha = true;

            var deferred = $q.defer();
            deferred.resolve("");
            return deferred.promise;
          }

          $scope.resolveRecaptcha = false;
          $scope.userInfo.captcha = vcRecaptchaService.getResponse();
        }

        $scope.userInfo.email = $scope.userInfo.username;

        return $http
          .put("../api/user/actions/register", $scope.userInfo, {
            headers: {
              "Accept-Language": gnLangs.current
            }
          })
          .then(
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: response.data,
                timeout: 0
              });
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };
      /**
       * Remind user password.
       */
      $scope.remindMyPassword = function () {
        //config.headers['Accept-Language'] = gnLangs.current;
        $http
          .put(
            "../api/user/actions/forgot-password?username=" + $scope.usernameToRemind,
            null,
            {
              headers: {
                "Accept-Language": gnLangs.current
              }
            }
          )
          .then(
            function (response) {
              $scope.sendPassword = false;
              $rootScope.$broadcast("StatusUpdated", {
                title: response.data,
                timeout: 0
              });
              $scope.usernameToRemind = null;
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      /**
       * Change user password.
       */
      $scope.updatePassword = function () {
        $http
          .patch(
            "../api/user/" + $scope.userToRemind,
            {
              password: $scope.password,
              changeKey: $scope.changeKey
            },
            {
              headers: {
                "Accept-Language": gnLangs.current
              }
            }
          )
          .then(
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: response.data,
                timeout: 0
              });
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      $scope.nodeChangeRedirect = function (redirectTo) {
        $http.get("../../signout").then(function (response) {
          window.location.href = redirectTo;
        });
      };

      initForm();
    }
  ]);
})();
