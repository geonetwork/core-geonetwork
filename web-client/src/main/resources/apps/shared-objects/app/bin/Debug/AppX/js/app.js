'use strict';


// Declare app level module which depends on filters, and services
angular.module('SharedObjects', ['SharedObjects.filters', 'SharedObjects.factories', 'SharedObjects.directives', 'SharedObjects.controllers']).
  config(['$routeProvider', function($routeProvider) {
      $routeProvider.when('/:validated/contacts', { templateUrl: 'partials/shared.html', controller: 'ContactControl' });
      $routeProvider.when('/:validated/formats', { templateUrl: 'partials/shared.html', controller: 'FormatControl' });
      $routeProvider.when('/:validated/extents', { templateUrl: 'partials/shared.html', controller: 'ExtentControl' });
      $routeProvider.when('/:validated/keywords', { templateUrl: 'partials/shared.html', controller: 'KeywordControl' });
      $routeProvider.when('/deleted', { templateUrl: 'partials/deleted.html', controller: 'DeletedControl' });
      $routeProvider.otherwise({ redirectTo: '/nonvalidated/contacts' });
  }]);
