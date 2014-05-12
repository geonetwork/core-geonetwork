(function() {
  goog.provide('gn');












  goog.require('gn_cat_controller');
  goog.require('gn_form_fields_directive');
  goog.require('gn_language_switcher');
  goog.require('gn_map');
  goog.require('gn_metadata_manager');
  goog.require('gn_needhelp');
  goog.require('gn_pagination');
  goog.require('gn_search_controller');
  goog.require('gn_search_form_controller');
  goog.require('gn_search_manager');
  goog.require('gn_utility');

  var module = angular.module('gn', [
    'ngRoute',
    'pascalprecht.translate',
    'angular-md5',
    'gn_language_switcher',
    'gn_utility',
    'gn_search_manager',
    'gn_metadata_manager',
    'gn_pagination',
    'gn_cat_controller',
    'gn_search_controller',
    'gn_form_fields_directive',
    'gn_map',
    'gn_search_form_controller',
    'gn_needhelp'
  ]);

  module.constant('$LOCALES', ['core']);


  module.factory('localeLoader', ['$http', '$q', function($http, $q) {
    return function(options) {
      var allPromises = [];
      angular.forEach(options.locales, function(value, index) {
        var langUrl = options.prefix +
            options.key + '-' + value + options.suffix;

        var deferredInst = $q.defer();
        allPromises.push(deferredInst.promise);

        $http({
          method: 'GET',
          url: langUrl
        }).success(function(data, status, header, config) {
          deferredInst.resolve(data);
        }).error(function(data, status, header, config) {
          deferredInst.reject(options.key);
        });
      });

      // Finally, create a single promise containing all the promises
      // for each app module:
      var deferred = $q.all(allPromises);
      return deferred;
    };
  }]);


  // TODO: could be improved instead of putting this in all main modules ?
  module.config(['$translateProvider', '$LOCALES',
                 function($translateProvider, $LOCALES) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);

})();
