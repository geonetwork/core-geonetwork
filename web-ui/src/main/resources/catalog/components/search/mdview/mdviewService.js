(function() {
  goog.provide('gn_mdview_service');

  var module = angular.module('gn_mdview_service', [
  ]);

  module.service('gnMdView', [
    '$location',
    '$rootScope',
    'gnMdFormatter',
    'gnSearchSettings',
    function($location, $rootScope, gnMdFormatter, gnSearchSettings) {

      var lastSearchParams = {};

      this.feedMd = function(index, md, records, mdView) {
        mdView.records = records || mdView.records;
        if (angular.isUndefined(md)) {
          md = mdView.records[index];
        }

        angular.extend(md, {
          links: md.getLinksByType('LINK'),
          downloads: md.getLinksByType('DOWNLOAD'),
          layers: md.getLinksByType('OGC', 'kml'),
          contacts: md.getContacts(),
          overviews: md.getThumbnails() ? md.getThumbnails().list : undefined,
          encodedUrl: encodeURIComponent($location.absUrl())
        });

        mdView.current.record = md;
        mdView.current.index = index;

        // TODO: do not add duplicates
        mdView.previousRecords.push(md);

        // Set the route
        this.setLocationUuid(md.getUuid());
      };

      /**
       * Update location to be /metadata/uuid.
       * Remove the search path and attributes from location too.
       * @param {string} uuid
       */
      this.setLocationUuid = function(uuid) {
        if ($location.path() == '/search') {
          $location.path('/metadata/' + uuid);
          lastSearchParams = angular.copy($location.search());
          $location.search('');
        }
      };

      this.removeLocationUuid = function() {
        if ($location.path() != '/search') {
          $location.path('/search');
          $location.search(lastSearchParams);
        }
      };

      this.initFormatter = function(selector) {
        var loadFormatter = function() {
          var url = $location.path();
          if (url.indexOf('/metadata/') == 0) {
            var uuid = url.substring(10, url.length);
            gnMdFormatter.load(gnSearchSettings.formatter.defaultUrl + uuid,
                selector);
          }
        };
        loadFormatter();
        $rootScope.$on('$locationChangeSuccess', loadFormatter);
      };
    }
  ]);

  module.service('gnMdFormatter', [
    '$rootScope',
    '$http',
    '$compile',
    '$sce',
    function($rootScope, $http, $compile, $sce) {

      this.load = function(url, selector) {
        $http.get(url).then(function(response) {
          var scope = angular.element($(selector)).scope();
          scope.fragment = $sce.trustAsHtml(response.data);
          var el = document.createElement('div');
          el.setAttribute('gn-metadata-display', '');
          $(selector).append(el);
          $compile(el)(scope);
        });
      };
    }
  ]);

})();
