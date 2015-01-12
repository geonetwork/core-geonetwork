(function() {
  goog.provide('gn_mdview_service');

  var module = angular.module('gn_mdview_service', [
  ]);

  module.service('gnMdView', ['$location',
      function($location) {

        this.feedMd = function(index, md, records, mdView) {
          mdView.records = records || mdView.records;
          if(angular.isUndefined(md)) {
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
        };
      }
  ]);
})();
