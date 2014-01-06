(function() {
  goog.provide('gn_validation_report_directive');

  goog.require('gn_utility');


  /**
   */
  angular.module('gn_validation_report_directive', [
    'gn_utility'
  ])
  .directive('gnValidationReport', ['gnValidation',
        function(gnValidation) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/validationreport/' +
                'partials/validationreport.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.showErrorsOnly = false;

              gnValidation.get().then(function(rules) {
                scope.rules = rules;
                scope.ruleTypes = {};

                angular.forEach(scope.rules, function(rule) {
                  if (scope.ruleTypes[rule['@group']] === undefined) {
                    scope.ruleTypes[rule['@group']] = {
                      id: rule['@group'],
                      label: rule.label || rule['@group'],
                      error: 0,
                      success: 0,
                      total: 0};
                  }

                  scope.ruleTypes[rule['@group']][rule['@type']] ++;
                  scope.ruleTypes[rule['@group']].total++;
                });
              });

              scope.toggleShowErrors = function() {
                scope.showErrorsOnly = !scope.showErrorsOnly;
              };

              scope.filterByType = function(type) {
                var rules = [];

                for (var i = 0; i < scope.rules.length; i++) {
                  var s = scope.rules[i];
                  if (s['@group'] === type.id) {
                    rules.push(s);
                  }
                }
                return rules;
              };
            }
          };
        }]);
})();
