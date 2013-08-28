(function() {
  goog.provide('gn_thesaurus_controller');


  var module = angular.module('gn_thesaurus_controller',
      []);


  /**
   * GnThesaurusController manage thesaurus
   *
   */
  module.controller('GnThesaurusController', [
    '$scope', '$http',
    function($scope, $http) {

      $scope.thesaurus = {};
      $scope.keywords = {};
      $scope.thesaurusSelected = null;
      $scope.thesaurusSelectedActivated = false;
      $scope.maxNumberOfKeywords = 50;
      $scope.keywordFilter = '';

      var defaultMaxNumberOfKeywords = 50;

      $scope.selectThesaurus = function(t) {
        $scope.thesaurusSelected = t;
        $scope.thesaurusSelectedActivated = t.activated == 'y';
        $scope.searchThesaurusKeyword();
      };


      $scope.searchThesaurusKeyword = function() {
        $http.get('keywords@json?pNewSearch=true&pTypeSearch=1' +
                  '&pThesauri=' + $scope.thesaurusSelected.key +
                  '&pMode=searchBox' +
                  '&maxResults=' +
                  ($scope.maxNumberOfKeywords || defaultMaxNumberOfKeywords) +
                  '&pKeyword=' + ($scope.keywordFilter || '*')
        ).success(function(data) {
          $scope.keywords = data[0];
        });
      };


      $scope.deleteThesaurus = function() {
        $http.get('thesaurus.remove?ref=' +
                  $scope.thesaurusSelected.key)
          .success(function(data) {
              $scope.thesaurusSelected = null;
              loadThesaurus();
            })
          .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('thesaurusDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.enableThesaurus = function() {
        $http.get('thesaurus.activate@json?' +
                'ref=' + $scope.thesaurusSelected.key +
                '&activated=' +
                    ($scope.thesaurusSelectedActivated ? 'y' : 'n')
        ).success(function(data) {
          // TODO
        });
      };

      $scope.$watch('maxNumberOfKeywords', function() {
        $scope.searchThesaurusKeyword();
      });

      $scope.$watch('keywordFilter', function() {
        $scope.searchThesaurusKeyword();
      });

      function loadThesaurus() {
        $http.get('thesaurus@json').success(function(data) {
          $scope.thesaurus = data[0];
        });
      }

      loadThesaurus();

    }]);

})();
