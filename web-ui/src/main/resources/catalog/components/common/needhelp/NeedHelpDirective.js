(function() {
  goog.provide('gn_needhelp_directive');

  var module = angular.module('gn_needhelp_directive', []);

  /**
   * @ngdoc directive
   * @name gn_needhelp_directive.directive:gnNeedHelp
   * @function
   *
   * @description
   * Create a link which open a new window with the requested page.
   * If the page is not found in the configuration, an alert
   * is displayed in the browser console.
   *
   *
   * @param {string} gnNeedHelp The documentation page key to load
   * see helpLinks
   *
   *
   * @param {boolean} iconOnly Optional parameter. Set to true to
   * display only an icon and no label.
   *
   */
  module.directive('gnNeedHelp', ['$http', '$translate',
    function($http, $translate) {
      // // add popup to document body
      // var modalObj = angular.element(
      //     '<div class="modal gn-doc">' +
      //     '<div class="modal-dialog">' +
      //     '  <div class="modal-content">' +
      //     '    <div class="modal-header">' +
      //     '      <button type="button" class="close" ' +
      //     '  data-dismiss="modal" aria-hidden="true">&times;</button>' +
      //     '      <h4 class="modal-title"></h4>' +
      //     '    </div>' +
      //     '    <div class="modal-body">' +
      //     '    </div>' +
      //     '  </div>' +
      //     '</div>' +
      //     '</div>');
      // angular.element(window.document.body).append(modalObj);

      // Help configuration TODO: move to an external file or db ?
      var helpLinks = {
        helpBaseUrl: 'http://geonetwork-opensource.org/manuals/trunk/',
        defaultLang: 'eng',
        pages: {
          editor: {
            eng: 'eng/users/quickstartguide/new_metadata/index.html',
            fre: 'fra/users/editor/index.html'
          },
          editor_sharing: {
            eng: 'eng/users/managing_metadata/ownership/index.html',
            fre: 'fra/users/editor/metadata_ownership/index.html'
          },
          editor_geopublisher: {
            eng: 'eng/users/quickstartguide/new_metadata/linking.html' +
                '#publish-uploaded-data-as-wms-wfs',
            fre: 'fra/users/editor/metadata_link/linking.html' +
                '#publier-les-donnees-telechargees-en-wms-wfs-wcs'
          },
          admin_settings: {
            eng: 'eng/users/admin/configuration/index.html',
            fre: 'fra/users/admin/configuration/index.html'
          }
        }
      };
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/common/needhelp/partials/' +
            'needhelp.html',
        link: function(scope, element, attrs) {
          //     var docPath = '../../catalog/docs/partials/user-doc-' +
          //         scope.lang + '/';
          //     var imgPath = '../../catalog/docs/img';
          //     var modalBody = $(modalObj).find('.modal-body').get(0);
          scope.iconOnly = attrs.iconOnly === 'true';

          var openPage = function(lang) {
            var page = helpLinks.pages[attrs.gnNeedHelp] &&
                helpLinks.pages[attrs.gnNeedHelp][lang];
            if (page) {
              var helpPageUrl = helpLinks.helpBaseUrl + page;
              window.open(helpPageUrl);
              return true;
            } else {
              console.log('No help page for ' + attrs.gnNeedHelp +
                  ' and language ' + lang + '. Add page to helpLinks');
              return false;
            }
          };

          // Get lang from scope or parent scope
          var lang = scope.lang || scope.$parent.lang;

          scope.showHelp = function() {
            if (!openPage(lang)) {
              openPage(helpLinks.defaultLang);
            }

            //       var helpPageUrl = docPath + attrs.gnNeedHelp + '.html';
            //       $http.get(helpPageUrl).success(function(data) {
            //         // Replace image path
            //         data = data.replace('img src="img',
            //             'img src="' + imgPath);
            //         modalBody.innerHTML = data;
            //         $(modalObj).modal('toggle');
            //       }).error(function() {
            //   modalBody.innerHTML = '<div class="alert alert-danger">' +
            //             $translate('docNotFound', {page: helpPageUrl}) +
            //             '</div>';
            //         $(modalObj).modal('toggle');
            //       });
          };
        }
      };
    }]);
})();
