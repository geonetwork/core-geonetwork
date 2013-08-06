(function() {
  goog.provide('gn_translation_controller');

  var module = angular.module('gn_translation_controller', []);

  module.controller('GnTranslationController',
      ['$scope', function($scope) {

        $scope.options = {
          langs: [
            {label: 'FR', value: 'fr'},
            {label: 'EN', value: 'en'}
          ],
          fallbackCode: 'en'
        };

      }]);

})();
