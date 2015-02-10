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
            link: function(scope) {
              scope.showErrors = false;
              scope.showSuccess = false;
              scope.gnCurrentEdit = gnCurrentEdit;
              scope.loading = false;
              scope.ruleTypes = [];

              scope.load = function() {
                scope.numberOfRules = 0;
                scope.ruleTypes = [];
                scope.hasErrors = false;
                scope.hasSuccess = false;
                scope.loading = true;

                gnValidation.get().then(function(ruleTypes) {
                  var optional = [];
                  angular.forEach(ruleTypes, function(ruleType) {
                    if (ruleType.requirement !== 'REQUIRED') {
                      optional.push(ruleType);
                    } else {
                      scope.ruleTypes.push(ruleType);
                    }

                    ruleType.error = parseInt(ruleType.error);
                    ruleType.expanded = false;

                    scope.hasErrors = scope.hasErrors || ruleType.error > 0;
                    angular.forEach(ruleType.patterns, function(pat) {
                      scope.numberOfRules +=
                          pat.rules ? pat.rules.length : 0;
                    });
                  });

                  scope.ruleTypes = scope.ruleTypes.concat(optional);
                  scope.hasSuccess = scope.ruleTypes.length > 0;
                  scope.loading = false;
                });
              };

              scope.labelImportanceClass = function(type) {
                if (type.error === 0) {
                  return 'label-success';
                } else if (type.requirement === 'REQUIRED') {
                  return 'label-danger';
                }
                return 'label-info';
              };

              scope.toggleShowErrors = function() {
                scope.showErrors = !scope.showErrors;
              };
              scope.toggleShowSuccess = function() {
                scope.showSuccess = !scope.showSuccess;
              };

              scope.getClass = function(type) {
                if (scope.numberOfRules > 0) {
                  if (type === 'icon') {
                    return scope.hasErrors ?
                        'fa-thumbs-o-down' : 'fa-thumbs-o-up';
                  }
                  return scope.hasErrors ? 'panel-danger' : 'panel-success';
                }
                if (type === 'icon') {
                  return 'fa-check';
                } else {
                  return '';
                }
              };

              // When saving is done, refresh validation report
              scope.$watch('gnCurrentEdit.saving', function(newValue) {
                if (newValue === false && gnCurrentEdit.showValidationErrors) {
                  scope.load();
                }
              });
            }
          };
        }]);
}());
