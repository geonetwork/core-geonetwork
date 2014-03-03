(function() {
  goog.provide('gn_classification_controller');


  goog.require('gn_categories_controller');
  goog.require('gn_thesaurus_controller');

  var module = angular.module('gn_classification_controller',
      ['gn_thesaurus_controller', 'gn_categories_controller']);


  /**
   *
   */
  module.controller('GnClassificationController',
      ['$scope', '$http',
       function($scope, $http) {

          $scope.pageMenu = {
           folder: 'classification/',
           defaultTab: 'thesaurus',
           tabs:
           [{
             type: 'thesaurus',
             label: 'manageThesaurus',
             icon: 'fa-archive',
             href: '#/classification/thesaurus'
           },{
              type: 'directory',
              label: 'manageDirectory',
              icon: 'fa-list-ul',
              href: 'catalog.edit#/directory'
            },{
             type: 'categories',
             label: 'manageCategory',
             icon: 'fa-tags',
             href: '#/classification/categories'
           }]
          };

       }]);

})();
