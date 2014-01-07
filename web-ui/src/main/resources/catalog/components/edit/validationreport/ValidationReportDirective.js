(function() {
  goog.provide('gn_validation_report_directive');

  goog.require('gn_utility');


  /**
   */
  angular.module('gn_validation_report_directive', [
    'gn_utility'
  ])
  .directive('gnValidationReport', ['gnValidation', 'gnCurrentEdit',
        function(gnValidation, gnCurrentEdit) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/validationreport/' +
                'partials/validationreport.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.showErrorsOnly = true;
              scope.gnCurrentEdit = gnCurrentEdit;


              var init = function() {
                scope.rules = {};
                scope.ruleTypes = {};
                scope.hasErrors = false;

                gnValidation.get().then(function(rules) {
                  scope.rules = rules;
                  var errors = 0;

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
                    if (scope.hasErrors =
                        scope.ruleTypes[rule['@group']].error) {
                      errors++;
                    }
                  });
                  scope.hasErrors = errors > 0;
                });
              };

              scope.toggleShowErrors = function() {
                scope.showErrorsOnly = !scope.showErrorsOnly;
              };

              scope.getClass = function(type) {
                if (scope.rules && scope.rules.length) {
                  if (type === 'icon') {
                    return scope.hasErrors ?
                        'fa-thumbs-o-down' : 'fa-thumbs-o-up';
                  } else {
                    return scope.hasErrors ? 'panel-danger' : 'panel-success';
                  }
                } else {
                  return '';
                }
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

              // When saving is done, refresh validation report
              scope.$watch('gnCurrentEdit.saving', function(newValue) {
                if (newValue === false) {
                  init();
                }
              });
            }
          };
        }]);
})();
