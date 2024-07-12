/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
  goog.provide("gn_contactus_directive");

  var module = angular.module("gn_contactus_directive", []);

  /**
   *
   */
  module.directive("gnContactUsForm", [
    "$http",
    "$translate",
    "vcRecaptchaService",
    "gnConfigService",
    "gnConfig",
    function ($http, $translate, vcRecaptchaService, gnConfigService, gnConfig) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          user: "="
        },
        templateUrl:
          "../../catalog/components/contactus/" + "partials/contactusform.html",
        link: function (scope, element, attrs) {
          gnConfigService.load().then(function (c) {
            scope.recaptchaEnabled =
              gnConfig["system.userSelfRegistration.recaptcha.enable"];
            scope.recaptchaKey =
              gnConfig["system.userSelfRegistration.recaptcha.publickey"];
          });

          scope.resolveRecaptcha = false;

          function initModel() {
            scope.feedbackModel = {
              name: scope.user.name,
              email: scope.user.email,
              org: "",
              comments: ""
            };
          }

          initModel();

          scope.send = function (form, formId) {
            if (scope.recaptchaEnabled) {
              if (vcRecaptchaService.getResponse() === "") {
                scope.resolveRecaptcha = true;

                var deferred = $q.defer();
                deferred.resolve("");
                return deferred.promise;
              }
              scope.resolveRecaptcha = false;
              scope.captcha = vcRecaptchaService.getResponse();
              $("#recaptcha").val(scope.captcha);
            }

            if (form.$valid) {
              $http({
                url: "../api/site/userfeedback",
                method: "POST",
                data: $(formId).serialize(),
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded"
                }
              }).then(
                function (response) {
                  scope.$emit("StatusUpdated", {
                    msg: $translate.instant("feebackSent"),
                    timeout: 2,
                    type: "info"
                  });
                  initModel();
                },
                function (response) {
                  scope.success = false;
                  scope.$emit("StatusUpdated", {
                    msg: $translate.instant("feebackSentError"),
                    timeout: 0,
                    type: "danger"
                  });
                }
              );
            }
          };
        }
      };
    }
  ]);
})();
