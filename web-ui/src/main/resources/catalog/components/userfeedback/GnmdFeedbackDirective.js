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
  goog.provide("gn_md_feedback_directive");

  var module = angular.module("gn_md_feedback_directive", []);

  /**
   * Send email to metadata contact or catalog administrator
   * with feedback about a metadata record.
   */
  module.directive("gnMdFeedback", [
    "$http",
    "gnConfig",
    "vcRecaptchaService",
    "$translate",
    "gnConfigService",
    function ($http, gnConfig, vcRecaptchaService, $translate, gnConfigService) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          md: "=gnMdFeedback"
        },
        templateUrl: "../../catalog/components/userfeedback/partials/mdFeedback.html",
        link: function postLink(scope, element, attrs) {
          scope.showLabel = attrs.showLabel;

          scope.isUserMetadataFeedbackEnabled = false;

          gnConfigService.load().then(function (c) {
            scope.isUserMetadataFeedbackEnabled =
              gnConfig[gnConfig.key.isMetadataFeedbackEnabled];
            scope.recaptchaEnabled =
              gnConfig["system.userSelfRegistration.recaptcha.enable"];
            scope.recaptchaKey =
              gnConfig["system.userSelfRegistration.recaptcha.publickey"];
          });

          scope.resolveRecaptcha = false;

          scope.mdFeedbackOpen = false;
          scope.toggle = function () {
            scope.mdFeedbackOpen = !scope.mdFeedbackOpen;
          };

          function init() {
            if (scope.md === null) {
              return;
            }

            // Select all record's contact with an email
            var contacts = [];
            scope.contactList = [];
            if (scope.md.allContacts) {
              contacts = contacts.concat(scope.md.allContacts.metadata);
              contacts = contacts.concat(scope.md.allContacts.resource);
            }
            for (var i = 0; i < contacts.length; i++) {
              if (contacts[i].email !== "") {
                contacts[i].selected = true;
                scope.contactList.push(contacts[i]);
              }
            }
            scope.updateList();
          }

          scope.$watch("md", function (n, o) {
            if (n !== o && scope.md != null) {
              init();
            }
          });

          scope.updateList = function () {
            var selected = [];
            for (var i = 0; i < scope.contactList.length; i++) {
              if (scope.contactList[i].selected) {
                selected.push(scope.contactList[i].email);
              }
            }
            scope.sendTo = selected.join(",");
          };

          init();

          scope.send = function (form, formId, uuid) {
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
                url: "../api/records/" + uuid + "/alert",
                method: "POST",
                data: $(formId).serialize(),
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded"
                }
              }).then(function (response) {
                // TODO: report no email sent
                if (response.status === 201) {
                  scope.success = true;
                  scope.$emit("StatusUpdated", {
                    msg: $translate.instant("feebackSent"),
                    timeout: 2,
                    type: "success"
                  });
                  $("*[name=gnFeedbackForm]").get(0).reset();
                  scope.mdFeedbackOpen = false;
                } else {
                  scope.success = false;
                }
              });
            }
          };

          $(element)
            .find(".modal")
            .on("hidden.bs.modal", function () {
              scope.$apply(function () {
                $("*[name=gnFeedbackForm]").get(0).reset();
                scope.mdFeedbackOpen = false;
              });
            });

          scope.$watch("mdFeedbackOpen", function (value) {
            if (value == true) $(element).find(".modal").modal("show");
            else $(element).find(".modal").modal("hide");
          });
        }
      };
    }
  ]);
})();
