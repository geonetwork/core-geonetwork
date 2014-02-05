(function() {
  goog.provide('gn_share_directive');

  var module = angular.module('gn_share_directive', []);

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
             console.log(attrs);

             scope.send = function(formId) {
               $http({
                 url: 'contact.send@json',
                 method: 'POST',
                 data: $(formId).serialize(),
                 headers: {'Content-Type': 'application/x-www-form-urlencoded'}
               }).then(
                   function(response) {
                     console.log(response);
                     if (response.status === 200) {

                     } else {

                     }
                   });
             };
           }
         };
       }]);
})();
