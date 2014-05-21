(function() {
  goog.provide('gn_contactus_directive');

  var module = angular.module('gn_contactus_directive', []);

  /**
   *
   */
  module.directive('gnContactUsForm',
      ['$http',
       function($http) {

         return {
           restrict: 'A',
           replace: true,
           scope: {
             user: '='
           },
           templateUrl: '../../catalog/components/share/' +
           'partials/contactusform.html',
           link: function(scope, element, attrs) {
             scope.send = function(formId) {
               $http({
                 url: 'contact.send@json',
                 method: 'POST',
                 data: $(formId).serialize(),
                 headers: {'Content-Type': 'application/x-www-form-urlencoded'}
               }).then(
                   function(response) {
                     // TODO: report no email sent
                     if (response.status === 200) {
                       scope.success = true;
                     } else {

                     }
                   });
             };
           }
         };
       }]);
})();
