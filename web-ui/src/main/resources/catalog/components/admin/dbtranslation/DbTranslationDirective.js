(function() {
  goog.provide('gn_dbtranslation_directive');

  var module = angular.module('gn_dbtranslation_directive', []);

  /**
     * Provide a table layout to edit db translations
     * for some types of entries (eg. groups, categories).
     *
     * Changes are saved on keyup event.
     *
     * Usage:
     * <div data-gn-db-translation="groupSelected" data-type="group"></div>
     */
  module.directive('gnDbTranslation', ['$http', '$translate', '$rootScope',
    function($http, $translate, $rootScope) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          type: '@type',
          element: '=gnDbTranslation'
        },
        templateUrl: '../../catalog/components/admin/dbtranslation/partials/' +
            'dbtranslation.html',
        link: function(scope, element, attrs) {

          /**
                 * Save a translation
                 */
          scope.saveTranslation = function(e) {
            // TODO: No need to save if translation not updated

            // TODO : could we use Angular compile here ?
            var xml = '<request><' + scope.type + ' id="{{id}}">' +
                      '<label>' +
                      '<{{key}}>{{value}}</{{key}}>' +
                                  '</label>' +
                      '</' + scope.type + '></request>';

            // id may be in id property (eg. group) or @id (eg. category)
            xml = xml.replace('{{id}}',
                scope.element.id || scope.element['@id'])
                              .replace(/{{key}}/g, e.key)
                              .replace('{{value}}', e.value);
            $http.post('admin.' + scope.type + '.update.labels', xml, {
              headers: {'Content-type': 'application/xml'}
            }).success(function(data) {
            }).error(function(data) {
              // FIXME: XML error to be converted to JSON ?
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate(scope.type + 'TranslationUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
          };
        }
      };
    }]);
})();
