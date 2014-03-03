(function() {
  goog.provide('gn_needhelp_directive');

  var module = angular.module('gn_needhelp_directive', []);

  /**
   * @ngdoc directive
   * @name gn_needhelp_directive.directive:gnNeedHelp
   * @function
   *
   * @description
   * Create a link which open a modal with the requested page.
   * If the page is not found, an alert is displayed.
   *
   *
   * @param {string} gnNeedHelp The documentation page to load
   * without documentation path and html extension. The URL is
   * built from ../../catalog/docs/partials/user-doc-{scope.lang}
   * /{gnNeedHelp}.html
   *
   *
   * @param {boolean} iconOnly Optional parameter. Set to true to
   * display only an icon and no label.
   *
   */
  module.directive('gnNeedHelp', ['$http', '$translate',
    function($http, $translate) {
      // add popup to document body
      var modalObj = angular.element(
          '<div class="modal gn-doc">' +
          '<div class="modal-dialog">' +
          '  <div class="modal-content">' +
          '    <div class="modal-header">' +
          '      <button type="button" class="close" ' +
          '        data-dismiss="modal" aria-hidden="true">&times;</button>' +
          '      <h4 class="modal-title"></h4>' +
          '    </div>' +
          '    <div class="modal-body">' +
          '    </div>' +
          '  </div>' +
          '</div>' +
          '</div>');
      angular.element(window.document.body).append(modalObj);

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/common/needhelp/partials/' +
            'needhelp.html',
        link: function(scope, element, attrs) {
          var docPath = '../../catalog/docs/partials/user-doc-' +
              scope.lang + '/';
          var imgPath = '../../catalog/docs/img';
          var modalBody = $(modalObj).find('.modal-body').get(0);
          scope.iconOnly = attrs.iconOnly === 'true';

          scope.showHelp = function() {
            var helpPageUrl = docPath + attrs.gnNeedHelp + '.html';
            $http.get(helpPageUrl).success(function(data) {
              // Replace image path
              data = data.replace('img src="img',
                  'img src="' + imgPath);
              modalBody.innerHTML = data;
              $(modalObj).modal('toggle');
            }).error(function() {
              modalBody.innerHTML = '<div class="alert alert-danger">' +
                  $translate('docNotFound', {page: helpPageUrl}) +
                  '</div>';
              $(modalObj).modal('toggle');
            });
          };
        }
      };
    }]);
})();
