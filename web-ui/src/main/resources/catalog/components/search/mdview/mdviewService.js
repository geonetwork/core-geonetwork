(function() {
  goog.provide('gn_mdview_service');

  var module = angular.module('gn_mdview_service', [
  ]);

  module.value('gnMdViewObj', {
    previousRecords: [],
    current: {
      record: null,
      index: null
    }
  });

  module.service('gnMdView', [
    'gnSearchLocation',
    '$rootScope',
    'gnMdFormatter',
    'Metadata',
    'gnMdViewObj',
    'gnSearchManagerService',
    'gnSearchSettings',
    'gnUrlUtils',
    function(gnSearchLocation, $rootScope, gnMdFormatter, Metadata,
             gnMdViewObj, gnSearchManagerService, gnSearchSettings,
             gnUrlUtils) {

      // Keep where the metadataview come from to get back on close
      $rootScope.$on('$locationChangeStart', function(o, v) {
        if (!gnSearchLocation.isMdView()) {
          gnMdViewObj.from = gnSearchLocation.path();
        }
      });

      this.feedMd = function(index, md, records) {
        gnMdViewObj.records = records || gnMdViewObj.records;
        if (angular.isUndefined(md)) {
          md = gnMdViewObj.records[index];
        }

        angular.extend(md, {
          links: md.getLinksByType('LINK'),
          downloads: md.getLinksByType('DOWNLOAD'),
          layers: md.getLinksByType('OGC', 'kml'),
          contacts: md.getContacts(),
          overviews: md.getThumbnails() ? md.getThumbnails().list : undefined,
          encodedUrl: encodeURIComponent(gnSearchLocation.absUrl())
        });

        gnMdViewObj.current.record = md;
        gnMdViewObj.current.index = index;

        // TODO: do not add duplicates
        gnMdViewObj.previousRecords.push(md);

        // Set the route
        this.setLocationUuid(md.getUuid());
      };

      /**
       * Update location to be /metadata/uuid.
       * Remove the search path and attributes from location too.
       * @param {string} uuid
       */
      this.setLocationUuid = function(uuid) {
        gnSearchLocation.setUuid(uuid);
      };

      /**
       * Called when you want to pass from mdview uuid url back to search.
       * It change path back to search and inject the last parameters saved
       * at last search.
       */
      this.removeLocationUuid = function() {
        if(gnMdViewObj.from && gnMdViewObj.from != gnSearchLocation.SEARCH) {
          gnSearchLocation.path(gnMdViewObj.from);
        }
        else {
          gnSearchLocation.restoreSearch();
        }
      };

      /**
       * Init the mdview behavior linked on $location.
       * At start and $location change, the uuid is extracted
       * from the url and the md is loaded. If the md was already loaded
       * by a previous search, we use this object, otherwise we launch
       * a new search to retrieve this md.
       */
      this.initMdView = function() {
        var that = this;
        var loadMdView = function(oldUrl, newUrl) {
          var uuid = gnSearchLocation.getUuid();
          if (uuid) {
            if (!gnMdViewObj.current.record ||
                gnMdViewObj.current.record.getUuid() != uuid) {

              // Check if the md is in current search
              if (angular.isArray(gnMdViewObj.records)) {
                for (var i = 0; i < gnMdViewObj.records.length; i++) {
                  var md = gnMdViewObj.records[i];
                  if (md.getUuid() == uuid) {
                    that.feedMd(i, md, gnMdViewObj.records);
                    return;
                  }
                }
              }

              // get a new search to pick the md
              gnSearchManagerService.gnSearch({
                uuid: uuid,
                fast: 'index',
                _content_type: 'json'
              }).then(function(data) {
                if (data.metadata.length == 1) {
                  data.metadata[0] = new Metadata(data.metadata[0]);
                  that.feedMd(0, undefined, data.metadata);
                }
              });
            }
          }
          else {
            gnMdViewObj.current.record = null;
          }
        };
        loadMdView(); // To manage uuid on page loading
        $rootScope.$on('$locationChangeSuccess', loadMdView);
      };

      this.initFormatter = function(selector) {
        var loadFormatter = function() {
          var uuid = gnSearchLocation.getUuid();
          if (uuid) {
            gnMdFormatter.load(gnSearchSettings.formatter.defaultUrl + uuid,
                selector);
          }
        };
        loadFormatter();
        $rootScope.$on('$locationChangeSuccess', loadFormatter);
      };

      /**
       * Open a metadata just from info in the layer. If the metadata comes
       * from the catalog, then the layer 'md' property contains the gn md
       * object. If not, we search the md in the catalog to open it.
       * @param {ol.layer} layer
       */
      this.openMdFromLayer = function(layer) {
        var md = layer.get('md');
        if (!md && layer.get('metadataUrl')) {

          var mdUrl = gnUrlUtils.urlResolve(layer.get('metadataUrl'));
          if (mdUrl.host == gnSearchLocation.host()) {
            gnSearchLocation.setUuid(layer.get('metadataUuid'));
          } else {
            window.open(layer.get('metadataUrl'), '_blank');
          }
        }
        else {
          this.feedMd(0, md, [md]);
        }
      };
    }
  ]);

  module.service('gnMdFormatter', [
    '$rootScope',
    '$http',
    '$compile',
    '$sce',
    'gnAlertService',
    function($rootScope, $http, $compile, $sce, gnAlertService) {

      this.load = function(url, selector) {
        $rootScope.$broadcast('mdLoadingStart');
        $http.get(url).then(function(response) {
          $rootScope.$broadcast('mdLoadingEnd');
          var scope = angular.element($(selector)).scope();
          scope.fragment = $sce.trustAsHtml(response.data);
          var el = document.createElement('div');
          el.setAttribute('gn-metadata-display', '');
          $(selector).append(el);
          $compile(el)(scope);
        }, function() {
          $rootScope.$broadcast('mdLoadingEnd');
          gnAlertService.addAlert({
            msg: 'Erreur de chargement de la métadonnée.',
            type: 'danger'
          });
        });
      };
    }
  ]);

})();
