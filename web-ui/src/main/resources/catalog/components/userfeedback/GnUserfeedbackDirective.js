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
  goog.require('gn_userfeedback_controller');

  var module = angular.module('gn_userfeedback_directive', ['gn_userfeedback_controller']);

  module.directive(
      'gnUserfeedback', ['$http', 'gnSearchLocation', 'gnConfig',
        function($http, gnSearchLocation, gnConfig) {
          return {
            restrict: 'AEC',
            replace: true,
            controller: 'gnUserfeedbackController',
            scope: {
              parentUuid: '@gnUserfeedback',
              userName: '@gnUser'
            },
            templateUrl: '../../catalog/components/userfeedback/partials/userfeedback.html',
            link: function(scope) {


              scope.$watch('parentUuid', function(newValue, oldValue) {
                scope.loadComments(newValue);
              });

              scope.$watch('userName', function(newValue, oldValue) {
                if (newValue) {
                  scope.loggedIn = true;
                  scope.authorNameValue = newValue;
                } else {
                  scope.loggedIn = false;
                }
              });

              scope.loadComments = function(id) {
                scope.fewCommentsList = [];
                scope.rating = null;

                scope.metatdataUUID = id;

                if (angular.isUndefined(scope.metatdataUUID) || scope.metatdataUUID == null || scope.metatdataUUID == '') {
                  console.log('Metadata UUID is null!');
                  return;
                }

                $http({
                  method: 'GET',
                  url: '../api/userfeedback?target=' + scope.metatdataUUID + '&maxnumber=3',
                  isArray: true
                }).then(function mySuccess(response) {
                  scope.fewCommentsList = [];
                  scope.fewCommentsList = scope.fewCommentsList.concat(response.data);
                  scope.showButtonAllComments = true;
                  scope.showModal = false;
                }, function myError(response) {
                  console.log('gnUserfeedback.loadComments ' + scope.metatdataUUID);
                  console.log(response.statusText);
                });


                $http({
                  method: 'GET',
                  url: '../api/records/' + scope.metatdataUUID + '/userfeedbackrating',
                  isArray: false
                }).then(function mySuccess(response) {
                  scope.rating = null;
                  scope.rating = response.data;
                }, function myError(response) {
                  console.log('gnUserfeedback.loadComments ' + scope.metatdataUUID);
                  console.log(response.statusText);
                });
              };
            }

          };
        }]);


  module.directive(
      'gnUserfeedbackfull', ['$http', 'gnSearchLocation', '$window', '$translate', 'gnConfig',
        function($http, gnSearchLocation, $window, $translate, gnConfig) {
          return {
            restrict: 'AEC',
            replace: true,
            controller: 'gnUserfeedbackControllerFull',
            scope: {
              parentUuid: '@gnUserfeedbackfull',
              userName: '@gnUser'
            },
            templateUrl: '../../catalog/components/userfeedback/partials/userfeedbackfull.html',
            link: function(scope) {



              scope.$watch('parentUuid', function(newValue, oldValue) {

                scope.metatdataUUID = newValue;

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

                if (angular.isUndefined(scope.metatdataUUID) || scope.metatdataUUID == null || scope.metatdataUUID == '') {
                  console.log('Metadata UUID is null!');
                  return;
                }

                $http({
                  method: 'GET',
                  url: '../api/userfeedback?full=true&target=' + scope.metatdataUUID,
                  isArray: true
                }).then(function mySuccess(response) {
                  scope.fullCommentsList = [];
                  scope.fullCommentsList = scope.fullCommentsList.concat(response.data);
                  scope.showButtonAllComments = true;
                  scope.showModal = false;
                }, function myError(response) {
                  console.log('gnUserfeedbackfull.initPopup ' + scope.metatdataUUID);
                  console.log(response.statusText);
                });

                $http({
                  method: 'GET',
                  url: '../api/records/' + scope.metatdataUUID + '/userfeedbackrating',
                  isArray: false
                }).then(function mySuccess(response) {
                  scope.rating = response.data;
                }, function myError(response) {
                  console.log('gnUserfeedbackfull.initPopup ' + scope.metatdataUUID);
                  console.log(response.statusText);
                });
              };

              scope.publish = function(id) {
                if (window.confirm($translate.instant('GUFpublishConfirm'))) {
                  console.log('PUBLISHED ' + id);

                  $http.get('../api/userfeedback/' + id + '/publish').success(function(data, status) {
                    console.log(data);
                    $window.location.reload();
                  });
                }
              };

              scope.deleteC = function(id) {
                if (window.confirm($translate.instant('GUFdeleteConfirm'))) {
                  console.log('DELETED ' + id);

                  $http.delete('../api/userfeedback/' + id).success(function(data, status) {
                    console.log(data);
                    $window.location.reload();
                  });


                }
              };

            }
          };
        }]);

  module.directive(
      'gnUserfeedbacknew', ['$http', 'gnSearchLocation', '$window', '$translate', 'gnConfig',
        function($http, gnSearchLocation, $window, $translate, gnConfig) {
          return {
            restrict: 'AEC',
            replace: true,
            controller: 'gnUserfeedbackControllerNew',
            scope: {
              parentUuid: '@gnUserfeedbacknew',
              userName: '@gnUser'
            },
            templateUrl: '../../catalog/components/userfeedback/partials/userfeedbacknew.html',
            link: function(scope) {

              scope.$watch('parentUuid', function(newValue, oldValue) {
                scope.metatdataUUID = newValue;
              });

              scope.$watch('userName', function(newValue, oldValue) {
                if (newValue) {
                  scope.loggedIn = true;
                  scope.authorNameValue = newValue;
                } else {
                  scope.loggedIn = false;
                }
              });

              // When the user wants to stay anonymous the credentials
              // should be hidden,
              // and the option to show the username should be hidden
              scope.toggleCredentials = function() {

                $('.anonymous').each(function(index) {
                  if ($(this).is(':visible')) {
                    $(this).hide();
                  } else {
                    $(this).show();
                  }
                });

              };

              scope, showPopover = function(info) {

                alert(info);

              };

              scope.initPopup = function() {

                if (angular.isUndefined(scope.metatdataUUID) || scope.metatdataUUID == null || scope.metatdataUUID == '') {
                  console.log('Metadata UUID is null!');
                  return;
                }

                $http({
                  method: 'GET',
                  url: '../api/records/' + scope.metatdataUUID + '/userfeedbackrating',
                  isArray: false
                }).then(function mySuccess(response) {
                  scope.rating = response.data;
                }, function myError(response) {
                  console.log('gnUserfeedbacknew.initPopup ' + scope.metatdataUUID);
                  console.log(response.statusText);
                });
              };

              // For update the average shown on the form
              scope.updateRate = function() {

                var tot = 0;
                var i = 0;

                if (scope.uf.ratingCOMPLETE > 0) {
                  tot = tot + scope.uf.ratingCOMPLETE;
                  i++;
                }
                if (scope.uf.ratingREADABILITY > 0) {
                  tot = tot + scope.uf.ratingREADABILITY;
                  i++;
                }
                if (scope.uf.ratingFINDABILITY > 0) {
                  tot = tot + scope.uf.ratingFINDABILITY;
                  i++;
                }
                if (scope.uf.ratingDATAQUALITY > 0) {
                  tot = tot + scope.uf.ratingDATAQUALITY;
                  i++;
                }
                if (scope.uf.ratingSERVICEQUALITY > 0) {
                  tot = tot + scope.uf.ratingSERVICEQUALITY;
                  i++;
                }
                if (scope.uf.ratingOTHER > 0) {
                  tot = tot + scope.uf.ratingOTHER;
                  i++;
                }

                if (tot > 0) {
                  scope.uf.ratingAVG = Math.floor(tot / i);
                } else {
                  scope.uf.ratingAVG = 0;
                }


              };

              function validateEmail(email) {
                var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
                return re.test(email);
              }

              scope.submitForm = function(data, modal) {

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
                    scope.authorEmailError = $translate.instant('GUFnotValidFormat');

                    return false;
                  }
                  if (scope.uf.authorOrganization.length > 64) {
                    scope.authorOrganizationError = $translate.instant('GUFtooLong');

                    return false;
                  }
                }

                scope.uf.metadataUUID = scope.metatdataUUID;

                if (angular.isUndefined(scope.metatdataUUID)) {
                  console.log('Metadata UUID in null!');
                  return;
                }

                $http.post('../api/userfeedback', data).success(function(data, status) {
                  console.log(data);
                  $window.location.reload();
                });
              };

            }
          };
        }]);


  module.directive(
      'gnUserfeedbacklasthome', ['$http', 'gnSearchLocation', 'gnConfig',
        function($http, gnSearchLocation, gnConfig) {
          return {
            restrict: 'AEC',
            replace: true,
            controller: 'gnUserfeedbackControllerLast',
            scope: {},
            templateUrl: '../../catalog/components/userfeedback/partials/userfeedbacklasthome.html',
            link: function(scope) {

              scope.lastCommentsList = [];

              scope.loadLastComments = function() {
                $http({
                  method: 'GET',
                  url: '../api/userfeedback?maxnumber=6',
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
