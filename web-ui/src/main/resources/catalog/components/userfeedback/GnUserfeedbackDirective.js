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
  goog.provide("gn_userfeedback_directive");

  goog.require("gn_catalog_service");
  goog.require("gn_search_location");

  var module = angular.module("gn_userfeedback_directive", ["vcRecaptcha", "ngMessages"]);

  module.service("gnUserfeedbackService", [
    "$http",
    "$q",
    "$translate",
    "$rootScope",
    function ($http, $q, $translate, $rootScope) {
      this.isBlank = function (str) {
        if (angular.isUndefined(str) || str == null || str == "") {
          return true;
        } else {
          return false;
        }
      };

      this.loadComments = function (metatdataUUID, size) {
        var numberOfCommentsDisplayed = size || -1;
        return $http({
          method: "GET",
          url:
            "../api/records/" +
            metatdataUUID +
            "/userfeedback?size=" +
            numberOfCommentsDisplayed,
          isArray: true
        });
      };

      this.loadRating = function (metatdataUUID) {
        return $http({
          method: "GET",
          url: "../api/records/" + metatdataUUID + "/userfeedbackrating",
          isArray: false
        });
      };

      this.loadRatingCriteria = function () {
        var deferred = $q.defer();
        $http({
          method: "GET",
          url: "../api/userfeedback/ratingcriteria",
          isArray: false,
          cache: true
        }).then(
          function (r) {
            var data = [];
            angular.forEach(r.data, function (c) {
              // By pass internal criteria. ie. average.
              if (c.id !== -1) {
                angular.forEach(c.label, function (value, key) {
                  var token = value.split("#");
                  if (token.length === 2) {
                    c.label[key] = {
                      label: token[0],
                      description: token[1]
                    };
                  } else {
                    c.label[key] = {
                      label: value,
                      description: ""
                    };
                  }
                });
                data.push(c);
              }
            });
            deferred.resolve(data);
          },
          function (r) {
            deferred.reject(r);
          }
        );
        return deferred.promise;
      };

      this.publish = function (id, scope) {
        if (window.confirm($translate.instant("GUFpublishConfirm"))) {
          return $http
            .get("../api/userfeedback/" + id + "/publish")
            .then(function (response) {
              $rootScope.$broadcast("reloadCommentList");
            });
        }
      };

      this.deleteC = function (id, scope) {
        if (window.confirm($translate.instant("GUFdeleteConfirm"))) {
          return $http.delete("../api/userfeedback/" + id).then(function (response) {
            $rootScope.$broadcast("reloadCommentList");
          });
        }
      };
    }
  ]);

  module.directive("gnUserfeedback", [
    "$http",
    "gnUserfeedbackService",
    "Metadata",
    function ($http, gnUserfeedbackService, Metadata) {
      return {
        restrict: "AEC",
        replace: true,
        scope: {
          record: "=gnUserfeedback",
          userName: "@gnUser",
          nbOfComments: "@nbOfComments"
        },
        templateUrl:
          "../../catalog/components/" + "userfeedback/partials/userfeedback.html",
        link: function (scope) {
          var defaultNbOfComments = 3;

          scope.fewCommentsList = [];
          scope.loaded = false;

          scope.ratingCategories = [];
          scope.lang = scope.$parent.$parent.lang;
          gnUserfeedbackService.loadRatingCriteria().then(function (data) {
            scope.ratingCategories = data;
            if (scope.record != null) {
              scope.mdrecord = new Metadata(scope.record);
              refreshList();
            }
          });

          // Wait for the record and userName to be available
          scope.$watch("record", function (n, o) {
            if (n !== o && n !== null && angular.isDefined(n)) {
              scope.mdrecord = new Metadata(n);
              refreshList();
            }
          });
          scope.$watch("userName", function (newValue, oldValue) {
            if (newValue) {
              scope.loggedIn = true;
              scope.authorNameValue = newValue;
            } else {
              scope.loggedIn = false;
            }
          });

          // Listen to the event reloadCommentList
          scope.$on("reloadCommentList", refreshList);

          // Functions
          function refreshList() {
            scope.loaded = false;
            scope.fewCommentsList = [];
            gnUserfeedbackService
              .loadComments(
                scope.mdrecord.uuid,
                scope.nbOfComments || defaultNbOfComments
              )
              .then(
                function (response) {
                  scope.fewCommentsList = [].concat(response.data);
                  scope.loaded = true;
                },
                function (response) {
                  console.log(response.statusText);
                }
              );

            gnUserfeedbackService.loadRating(scope.mdrecord.uuid).then(
              function mySuccess(response) {
                scope.rating = null;
                scope.rating = response.data;
                scope.loaded = true;
              },
              function myError(response) {
                console.log(response.statusText);
              }
            );

            scope.showButtonAllComments = true;
            scope.showModal = false;
          }
        }
      };
    }
  ]);

  module.directive("gnUserfeedbackfull", [
    "$http",
    "gnUserfeedbackService",
    "$translate",
    "$rootScope",
    "Metadata",
    function ($http, gnUserfeedbackService, $translate, $rootScope, Metadata) {
      return {
        restrict: "AEC",
        replace: true,
        scope: {
          record: "=gnUserfeedbackfull",
          userName: "@gnUser"
        },
        templateUrl:
          "../../catalog/components/userfeedback/" + "partials/userfeedbackfull.html",
        link: function (scope) {
          scope.userName = null;

          function initRecord(md) {
            if (scope.record != null) {
              var m = new Metadata(md);
              scope.metatdataUUID = m.uuid;
              scope.metatdataTitle = m.resourceTitle;
              scope.userName = $rootScope.user.username;
            }
          }

          initRecord(scope.record);

          scope.ratingCategories = [];
          scope.lang = scope.$parent.$parent.lang;
          gnUserfeedbackService.loadRatingCriteria().then(function (data) {
            scope.ratingCategories = data;
          });

          scope.$watch("record", function (n, o) {
            if (n !== o && n !== null && angular.isDefined(n)) {
              initRecord(n);
            }
          });

          scope.$watch("userName", function (newValue, oldValue) {
            if (newValue) {
              scope.loggedIn = true;
              scope.authorNameValue = newValue;
            } else {
              scope.loggedIn = false;
            }
          });

          scope.initPopup = function () {
            scope.fullCommentsList = [];
            scope.rating = null;

            gnUserfeedbackService
              .loadComments(scope.metatdataUUID, -1)
              .then(function (response) {
                scope.fullCommentsList = response.data;
              });
            gnUserfeedbackService
              .loadRating(scope.metatdataUUID)
              .then(function (response) {
                scope.rating = response.data;
              });
          };

          scope.publish = function (id) {
            gnUserfeedbackService.publish(id).then(function () {
              scope.initPopup();
            });
          };

          scope.deleteC = function (id) {
            gnUserfeedbackService.deleteC(id).then(function () {
              scope.initPopup();
            });
          };
        }
      };
    }
  ]);

  module.directive("gnUserfeedbacknew", [
    "$http",
    "gnUserfeedbackService",
    "$translate",
    "$q",
    "$rootScope",
    "Metadata",
    "vcRecaptchaService",
    "gnConfig",
    "gnConfigService",
    function (
      $http,
      gnUserfeedbackService,
      $translate,
      $q,
      $rootScope,
      Metadata,
      vcRecaptchaService,
      gnConfig,
      gnConfigService
    ) {
      return {
        restrict: "AEC",
        replace: true,
        scope: {
          record: "=gnUserfeedbacknew"
        },
        templateUrl:
          "../../catalog/components/" + "userfeedback/partials/userfeedbacknew.html",
        link: function (scope) {
          gnConfigService.loadPromise.then(function () {
            scope.recaptchaEnabled =
              gnConfig["system.userSelfRegistration.recaptcha.enable"];
            scope.recaptchaKey =
              gnConfig["system.userSelfRegistration.recaptcha.publickey"];
          });
          scope.resolveRecaptcha = false;
          scope.userName = null;

          function initRecord(md) {
            if (scope.record != null) {
              var m = new Metadata(md);
              scope.metatdataUUID = m.uuid;
              scope.metatdataTitle = m.resourceTitle;
              scope.userName = $rootScope.user.username;
            }
          }

          initRecord(scope.record);

          scope.ratingCategories = [];
          scope.lang = scope.$parent.$parent.lang;
          gnUserfeedbackService.loadRatingCriteria().then(function (data) {
            scope.ratingCategories = data;
          });

          scope.$watch("record", function (n, o) {
            if (n !== o && n !== null && angular.isDefined(n)) {
              initRecord(n);
            }
          });

          scope.$watchCollection("uf.rating", function (n, o) {
            scope.average = null;
            if (n !== o) {
              var total = 0,
                categoryNumber = 0;
              angular.forEach(scope.uf.rating, function (value, key) {
                if (value > 0) {
                  total += value;
                  categoryNumber++;
                }
              });
              scope.uf.ratingAVG = Math.floor(total / categoryNumber);
            }
          });

          scope.$watch("userName", function (newValue, oldValue) {
            if (newValue) {
              scope.loggedIn = true;
              scope.authorNameValue = newValue;
            } else {
              scope.loggedIn = false;
            }
          });

          scope.initPopup = function () {
            if (gnUserfeedbackService.isBlank(scope.metatdataUUID)) {
              console.log("Metadata UUID is null");
              return;
            }

            scope.uf = {
              rating: {},
              ratingAVG: null
            };

            angular.forEach(scope.ratingCategories, function (c) {
              scope.uf.rating[c.id] = null;
            });
          };

          scope.submitForm = function (data) {
            if (scope.recaptchaEnabled) {
              if (vcRecaptchaService.getResponse() === "") {
                scope.resolveRecaptcha = true;

                var deferred = $q.defer();
                deferred.resolve("");
                return deferred.promise;
              }

              scope.resolveRecaptcha = false;
              scope.uf.captcha = vcRecaptchaService.getResponse();
            }

            scope.uf.metadataUUID = scope.metatdataUUID;

            if (angular.isUndefined(scope.metatdataUUID)) {
              console.log("Metadata UUID is null!");
              return;
            }

            $http.post("../api/userfeedback", data).then(function (response) {
              $rootScope.$broadcast("reloadCommentList");
              angular.element("#gn-userfeedback-addcomment").modal("hide");

              if (scope.recaptchaEnabled) {
                vcRecaptchaService.reload();
              }
            });
          };
        }
      };
    }
  ]);

  module.directive("gnUserfeedbacklasthome", [
    "$http",
    "gnUserfeedbackService",
    function ($http, gnUserfeedbackService) {
      return {
        restrict: "AEC",
        replace: true,
        scope: {
          nbOfComments: "@nbOfComments"
        },
        templateUrl:
          "../../catalog/components/userfeedback/partials/userfeedbacklasthome.html",
        link: function (scope, element, attrs) {
          var defaultSize = 6,
            increment = 6;
          scope.lastCommentsList = [];
          scope.allowDelete = attrs.allowDelete == "true" || false;
          scope.allCommentsLoaded = false;
          scope.loadLastComments = function (size) {
            $http({
              method: "GET",
              url: "../api/userfeedback?size=" + size,
              isArray: true
            }).then(
              function mySuccess(response) {
                scope.allCommentsLoaded = response.data.length < size;
                scope.lastCommentsList = response.data;
              },
              function myError(response) {
                console.log(response.statusText);
              }
            );
          };

          scope.deleteC = function (id) {
            gnUserfeedbackService.deleteC(id).then(function () {
              scope.loadLastComments(scope.nbOfComments || defaultSize);
            });
          };

          scope.loadMore = function () {
            scope.loadLastComments(scope.lastCommentsList.length + increment);
          };

          scope.loadLastComments(scope.nbOfComments || defaultSize);
        }
      };
    }
  ]);
})();
