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

	goog.require('gn_search_location');
	goog.require('gn_userfeedback_controller');

	var module = angular.module('gn_userfeedback_directive', ['gn_userfeedback_controller']);

	module.directive(
			'gnUserfeedback', ['$http', 'gnSearchLocation',
				function($http, gnSearchLocation) {
				return {
					restrict: 'AEC',
					replace: true,
					controller: 'gnUserfeedbackController',
					scope: {
						parentUuid: '@gnUserfeedback'
					},
					templateUrl: '../../catalog/components/userfeedback/partials/userfeedback.html',
					link: function(scope) {

						scope.$watch("parentUuid",function(newValue,oldValue) {
							scope.loadComments(newValue);
						});
						
						
						scope.loadComments = function(id) {
							scope.fewCommentsList = [];   	
							scope.rating;

							scope.metatdataUUID = id;          		


							$http({
								method : "GET",
								url : "../api/userfeedback?target="+scope.metatdataUUID+"&maxnumber=3",
								isArray: true
							}).then(function mySuccess(response) {
								scope.fewCommentsList = scope.fewCommentsList.concat(response.data);
								scope.showButtonAllComments = true;
								scope.showModal = false;
							}, function myError(response) {
								console.log(response.statusText);
							});

							$http({
								method : "GET",
								url : "../api/metadata/" + scope.metatdataUUID + "/userfeedbackrating",
								isArray: false
							}).then(function mySuccess(response) {
								scope.rating = response.data;        	 
							}, function myError(response) {
								console.log(response.statusText);
							});
						}
					}
				};
			}]);


	module.directive(
			'gnUserfeedbackfull', ['$http', 'gnSearchLocation',
				function($http, gnSearchLocation) {
				return {
					restrict: 'AEC',
					replace: true,
					controller: 'gnUserfeedbackControllerFull',
					scope: {
						parentUuid: '@gnUserfeedbackfull'
					},
					templateUrl: '../../catalog/components/userfeedback/partials/userfeedbackfull.html',
					link: function(scope) {

						scope.$watch("parentUuid",function(newValue,oldValue) {
							scope.metatdataUUID = newValue;
						});

						scope.initPopup = function() {
							scope.fullCommentsList = [];   	
							scope.rating;

							$http({
								method : "GET",
								url : "../api/userfeedback?full=true&target="+scope.metatdataUUID,
								isArray: true
							}).then(function mySuccess(response) {
								scope.fullCommentsList = scope.fullCommentsList.concat(response.data);
								scope.showButtonAllComments = true;
								scope.showModal = false;
							}, function myError(response) {
								console.log(response.statusText);
							});

							$http({
								method : "GET",
								url : "../api/metadata/" + scope.metatdataUUID + "/userfeedbackrating",
								isArray: false
							}).then(function mySuccess(response) {
								scope.rating = response.data;        	 
							}, function myError(response) {
								console.log(response.statusText);
							});
						}
					}
				};
			}]);

	module.directive(
			'gnUserfeedbacknew', ['$http', 'gnSearchLocation', '$window',
				function($http, gnSearchLocation, $window) {
				return {
					restrict: 'AEC',
					replace: true,
					controller: 'gnUserfeedbackControllerNew',
					scope: {
						parentUuid: '@gnUserfeedbacknew'
					},
					templateUrl: '../../catalog/components/userfeedback/partials/userfeedbacknew.html',
					link: function(scope) {
						scope.$watch("parentUuid",function(newValue,oldValue) {
							scope.metatdataUUID = newValue;
						});					
						
						scope.initPopup = function() {
							$http({
								method : "GET",
								url : "../api/metadata/" + scope.metatdataUUID + "/userfeedbackrating",
								isArray: false
							}).then(function mySuccess(response) {
								scope.rating = response.data;        	 
							}, function myError(response) {
								console.log(response.statusText);
							});
						}
						
						scope.submitForm = function() {
							
							scope.uf.metadataUUID = scope.metatdataUUID;
							
							var data = scope.uf;  
							
							console.log(data);
							
							$http.post("../api/userfeedback", data); 
							
							$window.location.reload();
						}
					}
				};
			}]);





})();
