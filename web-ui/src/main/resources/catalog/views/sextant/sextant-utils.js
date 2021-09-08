(function() {

  goog.provide('gn_sxt_utils');

  goog.require('gn_sxt_legacy_facet_mapping');

  var module = angular.module('gn_sxt_utils', ['gn_sxt_legacy_facet_mapping']);

  module.service('sxtService', ['SEXTANT_LEGACY_FACET_MAPPING', function(SEXTANT_LEGACY_FACET_MAPPING) {

    var panierEnabled = typeof sxtSettings === 'undefined' || !angular.isUndefined(sxtSettings.tabOverflow.panier);

    var directDownloadTypes = [
      '#WWW:DOWNLOAD-1.0-link--download',
      '#WWW:DOWNLOAD-1.0-http--download',
      '#WWW:OPENDAP',
      '#MYO:MOTU-SUB',
      '#WWW:FTP'
    ];

    var allDownloadTypes = [
      '#FILE',
      '#DB',
      '#COPYFILE',
      '#WWW:DOWNLOAD-1.0-link--download',
      '#WWW:DOWNLOAD-1.0-http--download',
      '#WWW:OPENDAP',
      '#MYO:MOTU-SUB',
      '#WWW:FTP',
      '#OGC:WFS',
      '#OGC:WCS'
    ];

    var downloadTypes = panierEnabled ? allDownloadTypes : directDownloadTypes;

    var downloadTypesWithoutWxS = downloadTypes.filter(function (protocol) {
      return protocol !== '#OGC:WFS' && protocol !== '#OGC:WCS';
    });

    var layerTypes =  ['#OGC:WMTS', '#OGC:WMS', '#OGC:WMS-1.1.1-http-get-map',
      '#OGC:OWS-C'];

    this.feedMd = function(scope) {
      var md = scope.md;

      if (md.resourceType) {
        if(md.resourceType.indexOf('dataset')>=0) {
          md.icon = {cls: 'fa-database', title: 'dataset'}
        }
        else if(md.resourceType.indexOf('series')>=0) {
          md.icon = {cls: 'fa-database', title: 'series'}
        }
        else if(md.resourceType.indexOf('software')>=0) {
          md.icon = {cls: 'fa-hdd-o', title: 'software'}
        }
        else if(md.resourceType.indexOf('map')>=0) {
          md.icon = {cls: 'fa-globe', title: 'map'}
        }
        else if(md.resourceType.indexOf('application')>=0) {
          md.icon = {cls: 'fa-hdd-o', title: 'application'}
        }
        else if(md.resourceType.indexOf('basicgeodata')>=0) {
          md.icon = {cls: 'fa-globe', title: 'basicgeodata'}
        }
        else if(md.resourceType.indexOf('service')>=0) {
          md.icon = {cls: 'fa-globe', title: 'service'}
        }
        else if(md.resourceType.indexOf('repository')>=0) {
          md.icon = {cls: 'fa-folder-open', title: 'repository'}
        }
        else if(md.resourceType.indexOf('document')>=0) {
          md.icon = {cls: 'fa-file', title: 'document'}
        }
        else if(md.resourceType.indexOf('initiative')>=0) {
          md.icon = {cls: 'fa-group', title: 'initiative'}
        }
      }

      var status = md.mdStatus;
      var user = scope.user;
      scope.cantStatus = user && ((status == 4 || status == 2 || status == 3)
        && user.isReviewerOrMore && !user.isReviewerOrMore());


      scope.unFilteredlinks = md.getLinksByType('LINK');
      // we do not want to display the following protocols
      // link https://forge.ifremer.fr/mantis/view.php?id=40721
      scope.links = [];
      scope.unFilteredlinks.forEach(function(link){
        if (link.protocol !== 'NETWORK:LINK' && link.protocol !== 'WWW:DOWNLOAD-1.0-link--download'){
          scope.links.push(link)
        }
      });

      scope.downloads = md.getLinksByType.apply(md, downloadTypes);
      scope.layers = md.getLinksByType.apply(md, layerTypes);
    };

    /**
     * This transforms the legacy (sextant v6 and below) facet format to the new ES-compatible format
     * Existing ES facets will be removed.
     * If the facet fails to be migrated, a warning is shown in the console
     * @param {Object} esFacetConfig
     * @param {Array} sxtFacetConfig
     */
    this.migrateLegacyFacetConfigToEs = function (esFacetConfig, sxtFacetConfig) {
      function addEsFacetConfig(sxtFacet) {
        try {
          var esFacetTemplate = SEXTANT_LEGACY_FACET_MAPPING[sxtFacet.key];
          var esFacetName = Object.keys(esFacetTemplate)[0];
          var esFacet = angular.extend({}, esFacetTemplate);
          esFacet[esFacetName].meta = angular.extend({
            displayFilter: !!sxtFacet.filter,
            collapsed: !sxtFacet.opened,
            labels: sxtFacet.labels
          }, esFacetTemplate[esFacetName].meta);
          angular.extend(esFacetConfig, esFacet);
        } catch(e) {
          console.warn('A legacy Sextant v6 facet could not be migrated to v7\n'+
            'The following error was thrown: ' + e.message, sxtFacet)
        }
      }
      // clean existing facets, add new ones
      for (var key in esFacetConfig) {
        delete esFacetConfig[key];
      }
      for (var i = 0; i < sxtFacetConfig.length; i++) {
        addEsFacetConfig(sxtFacetConfig[i]);
      }
    };

  }]);

})();
