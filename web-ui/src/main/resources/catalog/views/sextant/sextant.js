(function() {

  goog.provide('gn_sxt_utils');

  var module = angular.module('gn_sxt_utils', [
  ]);

  module.service('sxtService', [ function() {

    var orderedDownloadTypes = ['#FILE', '#DB', '#COPYFILE',
      '#WWW:DOWNLOAD-1.0-link--download', '#WWW:OPENDAP', '#MYO:MOTU-SUB',
      '#WWW:FTP', '#OGC:WFS', '#OGC:WCS'];

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
      }

      var thumbs = md.getThumbnails();
      md.thumbnail = thumbs && (thumbs.small || thumbs.big || (
          thumbs.list.length && thumbs.list[0].url
        ));

      var status = md.mdStatus;
      var user = scope.user;
      scope.cantStatus = user && ((status == 4 || status == 2 || status == 3)
      && user.isReviewerOrMore && !user.isReviewerOrMore());


      scope.unFilteredlinks = md.getLinksByType('LINK', 'WWW:DOWNLOAD-1.0-http--download')
        // we do not want to display the following protocols
        // link https://forge.ifremer.fr/mantis/view.php?id=40721
      scope.links = []
      scope.unFilteredlinks.forEach(function(link){
         if (link.protocol !== 'NETWORK:LINK'){
          scope.links.push(link)
        };
      })

      scope.downloads = [];
      scope.layers = [];

      angular.forEach(md.linksTree, function(transferOptions, i) {

        // get all layers and downloads for this transferOptions
        var layers = md.getLinksByType.apply(md,
          [i+1].concat(layerTypes));

        var downloads = md.getLinksByType.apply(md,
          [i+1].concat(orderedDownloadTypes));

        if(downloads.length > 0) {
          // If only one layer, we get only one download (we bind them later)
          // We take the first one cause based on types priority
          // https://github.com/camptocamp/sextant-geonetwork/wiki/Catalogue#les-protocoles
          if(layers.length == 1) {

            var d = this.getTopPriorityDownload(downloads);
            if(d) {
              scope.downloads.push(d);
              layers[0].extra = {
                downloads: [d]
              };
            }
          }
          else {
            scope.downloads = scope.downloads.concat(downloads);
          }
        }
        scope.layers = scope.layers.concat(layers);
      }.bind(this));
    };


    this.getTopPriorityDownload = function(downloads) {
      var download;
      loopType:
        for (var i = 0; i < orderedDownloadTypes.length; i ++) {
          var t = orderedDownloadTypes[i];
          loopLink:
            for (var j = 0; j < downloads.length; j ++) {
              var l = downloads[j];
              if (l.protocol == t.substr(1, t.length - 1)) {
                download = l;
                break loopType;
              }
            }
        }
      return download;
    };

  }]);

})();
