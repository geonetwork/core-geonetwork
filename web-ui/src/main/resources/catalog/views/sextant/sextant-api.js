(function() {

  goog.provide('sextant_api');

  goog.require('gn_search_sextant');

  var appRoot = document.currentScript.parentElement;
  var catalogName = document.currentScript.getAttribute('catalog');

  console.log('Loading Sextant...');

  Promise.all([
    $.ajax(sxtGnUrl + 'api/site/settings', { dataType: 'json' }),
    $.ajax(sxtGnUrl + 'api/ui/' + catalogName, { dataType: 'json' })
  ]).then(function(settings) {
    var siteSettings = settings[0];
    var uiSettings = settings[1];

    console.log('settings', siteSettings, uiSettings);

    // TODO: read from ui settings, ie: var theme = uiSettings.sextant && uiSettings.sextant.theme ? uiSettings.sextant.theme || 'default';
    var theme = 'default';
    var stylesheetUrl = sxtGnUrl + '../static/api-' + theme + '.css';

    // add theme-specific stylesheet & preload link
    var preloadLink = document.createElement("link");
    preloadLink.href = stylesheetUrl;
    preloadLink.rel = "preload";
    preloadLink.as = "style";
    appRoot.appendChild(preloadLink);

    var link = document.createElement("link");
    link.href = stylesheetUrl;
    link.rel = "stylesheet";
    link.media = "screen";
    appRoot.appendChild(link);

    // apply settings
    var cfgModule = angular.module('gn_config', []);
    cfgModule.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings',
      function(gnViewerSettings, gnSearchSettings, gnGlobalSettings) {
        gnGlobalSettings.init(uiSettings, sxtGnUrl, gnViewerSettings, gnSearchSettings);
      }]);

    // include sextant API
    var templateInclude = document.createElement("div");
    templateInclude.className = "gn";
    templateInclude.setAttribute("ng-include", "'../../catalog/views/sextant/templates/index.html'");
    templateInclude.setAttribute("ng-controller", "GnCatController");
    appRoot.appendChild(templateInclude);

    // bootstrap app
    angular.bootstrap(appRoot, ['gn_search_sextant']);
  }, function(error) {
    console.error('Failed loading Sextant: ', error);
  });

})();
