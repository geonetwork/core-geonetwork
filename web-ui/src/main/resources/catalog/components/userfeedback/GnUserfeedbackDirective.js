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
  goog.provide('gn_userfeedback_directive');



  goog.require('gn_catalog_service');
  goog.require('gn_search_location');

  var module = angular.module('gn_userfeedback_directive', ['vcRecaptcha']);

  module.service('gnUserfeedbackService', [
    '$http', '$q',
    function($http, $q) {
      this.isBlank = function(str) {
        if (angular.isUndefined(str) ||
          str == null ||
          str == '') {
          return true;
        } else {
          return false;
        }
      };

      this.loadComments = function(metatdataUUID, size) {
        var numberOfCommentsDisplayed = size || -1;
        return $http({
          method: 'GET',
          url: '../api/records/' + metatdataUUID +
          '/userfeedback?size=' + numberOfCommentsDisplayed,
          isArray: true
        });
      };

      this.loadRating = function(metatdataUUID) {
        return $http({
          method: 'GET',
          url: '../api/records/' + metatdataUUID + '/userfeedbackrating',
          isArray: false
        });
      };

      this.loadRatingCriteria = function() {
        var deferred = $q.defer();
        $http({
          method: 'GET',
          url: '../api/userfeedback/ratingcriteria',
          isArray: false,
          cache: true
        }).then(function(r) {
          var data = [];
          angular.forEach(r.data, function (c) {
            // By pass internal criteria. ie. average.
            if (c.id !== -1) {
              angular.forEach(c.label, function (value, key) {
                var token = value.split('#');
                if (token.length === 2) {
                  c.label[key] = {
                    label: token[0],
                    description: token[1]
                  };
                } else {
                  c.label[key] = {
                    label: value,
                    description: ''
                  };
                }
              });
              data.push(c)
            }
          });
          deferred.resolve(data);
        }, function (r) {
          deferred.reject(r);
        });
        return deferred.promise;
      };
    }]);

  module.directive(
    'gnUserfeedback', ['$http', 'gnUserfeedbackService', 'Metadata',
      function($http, gnUserfeedbackService, Metadata) {
        return {
          restrict: 'AEC',
          replace: true,
          scope: {
            record: '=gnUserfeedback',
            userName: '@gnUser',
            nbOfComments: '@nbOfComments'
          },
          templateUrl: '../../catalog/components/' +
          'userfeedback/partials/userfeedback.html',
          link: function(scope) {
            var defaultNbOfComments = 3;

            scope.fewCommentsList = [];
            scope.loaded = false;

            scope.ratingCategories = [];
            scope.lang = scope.$parent.$parent.lang;
            gnUserfeedbackService.loadRatingCriteria().then(function(data) {
              scope.ratingCategories = data;
              if(scope.record != null) {
                scope.mdrecord = new Metadata(scope.record);
                refreshList();
              }
            });

            // Wait for the record and userName to be available
            scope.$watch('record', function(n, o) {
              if (n !== o && n !== null && angular.isDefined(n)) {
                scope.mdrecord = new Metadata(n);
                refreshList();
              }
            });
            scope.$watch('userName', function(newValue, oldValue) {
              if (newValue) {
                scope.loggedIn = true;
                scope.authorNameValue = newValue;
              } else {
                scope.loggedIn = false;
              }
            });

            // Listen to the event reloadCommentList
            scope.$on('reloadCommentList', refreshList);

            // Functions
            function refreshList() {
              scope.loaded = false;
              scope.fewCommentsList = [];
              gnUserfeedbackService.loadComments(scope.mdrecord.uuid,
                scope.nbOfComments || defaultNbOfComments).then(
                function(response) {
                  scope.fewCommentsList = [].concat(response.data);
                  scope.loaded = true;
                }, function(response) {
                  console.log(response.statusText);
                });

              gnUserfeedbackService.loadRating(scope.mdrecord.uuid).then(
                function mySuccess(response) {
                  scope.rating = null;
                  scope.rating = response.data;
                  scope.loaded = true;
                }, function myError(response) {
                  console.log(response.statusText);
                });

              scope.showButtonAllComments = true;
              scope.showModal = false;
            }
          }
        };
      }]);


  module.directive(
    'gnUserfeedbackfull', ['$http', 'gnUserfeedbackService', '$translate', '$rootScope', 'Metadata',
      function($http, gnUserfeedbackService, $translate, $rootScope, Metadata) {
        return {
          restrict: 'AEC',
          replace: true,
          scope: {
            record: '=gnUserfeedbackfull',
            userName: '@gnUser'
          },
          templateUrl: '../../catalog/components/userfeedback/' +
          'partials/userfeedbackfull.html',
          link: function(scope) {
            function initRecord(md) {
              if (scope.record != null) {
                var m = new Metadata(md);
                scope.metatdataUUID = m.uuid;
                scope.metatdataTitle = m.resourceTitle;
              }
            }


            initRecord(scope.record);

            scope.ratingCategories = [];
            scope.lang = scope.$parent.$parent.lang;
            gnUserfeedbackService.loadRatingCriteria().then(function(data) {
              scope.ratingCategories = data;
            });

            scope.$watch('record', function(n, o) {
              if(n !== o && n !== null && angular.isDefined(n)) {
                initRecord(n);
              }
            });

            scope.$watch('userName', function(newValue, oldValue) {
              if (newValue) {
                scope.loggedIn = true;
                scope.authorNameValue = newValue;
              } else {
                scope.loggedIn = false;
              }
            });

            scope.initPopup = function() {
              scope.fullCommentsList = [];
              scope.rating = null;

              gnUserfeedbackService.loadComments(scope.metatdataUUID,
                -1).then(function(response) {
                scope.fullCommentsList = response.data;
              });
              gnUserfeedbackService.loadRating(scope.metatdataUUID).then(
                function(response) {
                  scope.rating = response.data;
                }
              );
            };

            scope.publish = function(id) {
              if (window.confirm($translate.instant('GUFpublishConfirm'))) {
                $http.get('../api/userfeedback/' + id + '/publish')
                  .success(function(data, status) {
                    scope.initPopup();
                    $rootScope.$broadcast('reloadCommentList');
                  });
              }
            };

            scope.deleteC = function(id) {
              if (window.confirm($translate.instant('GUFdeleteConfirm'))) {
                $http.delete('../api/userfeedback/' + id)
                  .success(function(data) {
                    scope.initPopup();
                    $rootScope.$broadcast('reloadCommentList');
                  });
              }
            };

          }
        };
      }]);

  module.directive(
    'gnUserfeedbacknew', ['$http', 'gnUserfeedbackService', '$translate', '$q',
      '$rootScope', 'Metadata', 'vcRecaptchaService', 'gnConfig',
      function($http, gnUserfeedbackService, $translate, $q,
               $rootScope, Metadata, vcRecaptchaService, gnConfig) {
        return {
          restrict: 'AEC',
          replace: true,
          scope: {
            record: '=gnUserfeedbacknew',
            userName: '@gnUser'
          },
          templateUrl: '../../catalog/components/' +
          'userfeedback/partials/userfeedbacknew.html',
          link: function(scope) {
            scope.recaptchaEnabled =
              gnConfig['system.userSelfRegistration.recaptcha.enable'];
            scope.recaptchaKey =
              gnConfig['system.userSelfRegistration.recaptcha.publickey'];
            scope.resolveRecaptcha = false;

            function initRecord(md) {
              if (scope.record != null) {
                var m = new Metadata(md);
                scope.metatdataUUID = m.uuid;
                scope.metatdataTitle = m.resourceTitle;
              }
            }

            initRecord(scope.record);

            scope.ratingCategories = [];
            scope.lang = scope.$parent.$parent.lang;
            gnUserfeedbackService.loadRatingCriteria().then(function(data) {
              scope.ratingCategories = data;
            });

            scope.$watch('record', function(n, o) {
              if(n !== o && n !== null && angular.isDefined(n)) {
                initRecord(n);
              }
            });

            scope.$watchCollection('uf.rating', function(n, o) {
              scope.average = null;
              if (n !== o) {
                var total = 0, categoryNumber = 0;
                angular.forEach(scope.uf.rating, function (value, key) {
                  if (value > 0) {
                    total += value;
                    categoryNumber ++;
                  }
                });
                scope.uf.ratingAVG = Math.floor(total/categoryNumber);
              }
            });

            scope.$watch('userName', function(newValue, oldValue) {
              if (newValue) {
                scope.loggedIn = true;
                scope.authorNameValue = newValue;
              } else {
                scope.loggedIn = false;
              }
            });

            scope.initPopup = function() {

              if (gnUserfeedbackService.isBlank(scope.metatdataUUID)) {
                console.log('Metadata UUID is null');
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

            scope.submitForm = function(data) {
              if (scope.recaptchaEnabled) {
                if (vcRecaptchaService.getResponse() === '') {
                  scope.resolveRecaptcha = true;

                  var deferred = $q.defer();
                  deferred.resolve('');
                  return deferred.promise;
                }

                scope.resolveRecaptcha = false;
                scope.uf.captcha = vcRecaptchaService.getResponse();
              }


              if (!scope.loggedIn) {

                scope.authorNameError = false;
                scope.authorEmailError = false;
                scope.authorOrganizationError = false;

                if (!data.authorName) {
                  scope.authorNameError = $translate.instant('GUFrequired');

                  return false;
                }
                if (!data.authorEmail) {
                  scope.authorEmailError = $translate.instant('GUFrequired');

                  return false;
                }
                if (scope.uf.authorName.length > 64) {
                  scope.authorNameError = $translate.instant('GUFtooLong');

                  return false;
                }
                if (scope.uf.authorEmail.length > 64) {
                  scope.authorEmailError = $translate.instant('GUFtooLong');

                  return false;
                }

                var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

                if (!re.test(scope.uf.authorEmail)) {
                  scope.authorEmailError =
                    $translate.instant('GUFnotValidFormat');

                  return false;
                }
                if (scope.uf.authorOrganization && scope.uf.authorOrganization.length > 64) {
                  scope.authorOrganizationError =
                    $translate.instant('GUFtooLong');

                  return false;
                }
              }

              scope.uf.metadataUUID = scope.metatdataUUID;

              if (angular.isUndefined(scope.metatdataUUID)) {
                console.log('Metadata UUID is null!');
                return;
              }

              $http.post('../api/userfeedback', data)
                .success(function(data, status) {
                  $rootScope.$broadcast('reloadCommentList');
                  angular.element('#gn-userfeedback-addcomment').modal('hide');

                  if (scope.recaptchaEnabled) {
                    vcRecaptchaService.reload();
                  }
                });
            };

          }
        };
      }]);


  module.directive(
    'gnUserfeedbacklasthome', ['$http',
      function($http) {
        return {
          restrict: 'AEC',
          replace: true,
          scope: {
            nbOfComments: '@nbOfComments'
          },
          templateUrl: '../../catalog/components/userfeedback/partials/userfeedbacklasthome.html',
          link: function(scope) {

            scope.lastCommentsList = [];

            scope.loadLastComments = function() {
              $http({
                method: 'GET',
                url: '../api/userfeedback?size=' + (scope.nbOfComments || 6),
                isArray: true
              }).then(function mySuccess(response) {
                scope.lastCommentsList = [];
                scope.lastCommentsList = scope.lastCommentsList.concat(response.data);
              }, function myError(response) {
                console.log(response.statusText);
              });

            };
            scope.loadLastComments();
          }
        };
      }]);
})();
