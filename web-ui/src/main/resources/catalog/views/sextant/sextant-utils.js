(function() {

  goog.provide('gn_sxt_utils');

  var module = angular.module('gn_sxt_utils', [
  ]);

  module.service('sxtService', [ function() {

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

      if (md.type) {
        if(md.type.indexOf('dataset')>=0) {
          md.icon = {cls: 'fa-database', title: 'dataset'}
        }
        else if(md.type.indexOf('series')>=0) {
          md.icon = {cls: 'fa-database', title: 'series'}
        }
        else if(md.type.indexOf('software')>=0) {
          md.icon = {cls: 'fa-hdd-o', title: 'software'}
        }
        else if(md.type.indexOf('map')>=0) {
          md.icon = {cls: 'fa-globe', title: 'map'}
        }
        else if(md.type.indexOf('application')>=0) {
          md.icon = {cls: 'fa-hdd-o', title: 'application'}
        }
        else if(md.type.indexOf('basicgeodata')>=0) {
          md.icon = {cls: 'fa-globe', title: 'basicgeodata'}
        }
        else if(md.type.indexOf('service')>=0) {
          md.icon = {cls: 'fa-globe', title: 'service'}
        }
        else if(md.type.indexOf('repository')>=0) {
          md.icon = {cls: 'fa-folder-open', title: 'repository'}
        }
        else if(md.type.indexOf('document')>=0) {
          md.icon = {cls: 'fa-file', title: 'document'}
        }
        else if(md.type.indexOf('initiative')>=0) {
          md.icon = {cls: 'fa-group', title: 'initiative'}
        }
      }

      var thumbs = md.getThumbnails();
      md.thumbnail = thumbs && (thumbs.small || thumbs.big || (
        thumbs.list.length && thumbs.list[0].url
      ));

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

      scope.downloads = [];
      scope.layers = [];

      angular.forEach(md.linksTree, function(transferOptions, i) {

        // get all layers and downloads for this transferOptions
        var layers = md.getLinksByType.apply(md,
          [i+1].concat(layerTypes));

        var downloads = md.getLinksByType.apply(md,
          [i+1].concat(downloadTypes));

        if(downloads.length > 0) {
          // If only one layer, hide any WFS or WCS links unless there are several
          // note: this does not apply if there is only one download link (otherwise we might end up with 0 links)
          // https://gitlab.ifremer.fr/sextant/geonetwork/-/wikis/Catalogue#les-protocoles
          if(layers.length === 1 && downloads.length > 1) {
            var multipleWxS = md.getLinksByType(i+1, '#OGC:WFS', '#OGC:WCS').length > 1;

            if (!multipleWxS) {
              downloads = md.getLinksByType.apply(md,
                [i+1].concat(downloadTypesWithoutWxS));
            }
          }

          scope.downloads = scope.downloads.concat(downloads);
        }
        scope.layers = scope.layers.concat(layers);
      }.bind(this));
    };

  }]);

})();
