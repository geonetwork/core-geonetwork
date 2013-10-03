(function() {
    goog.provide('gn_language_switcher_directive');

    var module = angular.module('gn_language_switcher_directive', []);

    module.directive('gnLanguageSwitcher', [function() {
        
        return {
            restrict : 'A',
            replace: true,
            transclude: true,
            scope: {
            },
            templateUrl: '../../catalog/components/languageswitcher/partials/' +
              'languageswitcher.html',
            link : function(scope, element, attrs) {
            	// TODO : get list from server side
                scope.languages = {'fr': 'fre', 'en': 'eng'};
                scope.switchLanguage = function(key) {
//                      Update translate with no page reload 
//                      $translate.uses(key);
                        var url = location.href.split('/');
                        url[5] = key;
                        console.log(url.join('/'));
                        location.href = url.join('/');
                    };
            }
        };
    }]);
})();
