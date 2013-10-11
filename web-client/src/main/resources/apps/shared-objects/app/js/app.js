'use strict';
// Declare app level module which depends on filters, and services
angular.module('SharedObjects', ['SharedObjects.filters', 'SharedObjects.factories', 'SharedObjects.directives', 'SharedObjects.controllers', 'ngRoute']).
  config(['$routeProvider', function($routeProvider) {
      $routeProvider.when('/:validated/contacts', { templateUrl: 'partials/shared.html', controller: 'ContactControl' });
      $routeProvider.when('/:validated/formats', { templateUrl: 'partials/shared.html', controller: 'FormatControl' });
      $routeProvider.when('/:validated/extents', { templateUrl: 'partials/shared.html', controller: 'ExtentControl' });
      $routeProvider.when('/:validated/keywords', { templateUrl: 'partials/shared.html', controller: 'KeywordControl' });
      $routeProvider.when('/deleted', { templateUrl: 'partials/shared.html', controller: 'DeletedControl' });
      $routeProvider.when('/deleted', { templateUrl: 'partials/shared.html', controller: 'DeletedControl' });
      $routeProvider.when('/validated/deleted', { redirectTo: '/validated/contacts' });
      $routeProvider.when('/nonvalidated/deleted', { redirectTo: '/nonvalidated/contacts' });
      $routeProvider.otherwise({ redirectTo: '/nonvalidated/contacts' });
  }]);
