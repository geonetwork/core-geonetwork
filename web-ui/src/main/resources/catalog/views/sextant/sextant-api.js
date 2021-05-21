(function() {

  goog.provide('sextant_api');

  goog.require('gn_search_sextant');

  var appRoot = document.currentScript.parentElement;
  var scriptEl = document.currentScript;

  var catalogId = scriptEl.hasAttribute('portal') ? scriptEl.getAttribute('portal') : null;
  var sxtSize = scriptEl.hasAttribute('size') ? scriptEl.getAttribute('size') : null;
  var sxtSizeDiff = scriptEl.hasAttribute('size-diff') ? scriptEl.getAttribute('size-diff') : null;
  var sizeClass = scriptEl.hasAttribute('max-sm') ? 'sxt-max-sm' :
    scriptEl.hasAttribute('max-md') ? 'sxt-max-md' : '';

  // modify the API url to use the correct catalog
  // TODO: use a token in sxtGnUrl instead of the hardcoded 'srv'
  sxtGnUrl = sxtGnUrl.replace('srv', catalogId);

  console.log('Loading Sextant...');

  // 1. load the portal list
  $.ajax(sxtGnUrl + 'api/sources', { dataType: 'json' })
    .then(function (sources) {
      for (var i = 0; i < sources.length; i++) {
        if (sources[i].uuid === catalogId) {
          return sources[i];
        }
      }
      return sources[0];
    })
    // 2. load site settings and ui settings
    .then(function (portalSetting) {
      var uiConfigName = portalSetting.uiConfig || 'srv';
      console.log('UI config: ', uiConfigName);

      Promise.all([
        $.ajax(sxtGnUrl + 'api/site/settings', {dataType: 'json'}),
        $.ajax(sxtGnUrl + 'api/ui/' + uiConfigName, {dataType: 'json'})
      ]).then(function (settings) {
        var uiSettings = JSON.parse(settings[1].configuration);
        // merge uiconfig.sextant in sxtSettings for backward compatibility
        window.sxtSettings = uiSettings.sextant ? uiSettings.sextant : undefined;
        var theme = uiSettings.sextant && uiSettings.sextant.theme ? uiSettings.sextant.theme : 'default';
        var stylesheetUrl = sxtGnUrl + '../static/api-' + theme + '.css';

        // add theme-specific stylesheet & preload link
        var preloadLink = document.createElement("link");
        preloadLink.href = stylesheetUrl;
        preloadLink.rel = "preload";
        preloadLink.as = "style";
        appRoot.appendChild(preloadLink);
        appRoot.appendChild(preloadLink);

        var link = document.createElement("link");
        link.href = stylesheetUrl;
        link.rel = "stylesheet";
        link.media = "screen";
        appRoot.appendChild(link);

        // apply settings
        var cfgModule = angular.module('gn_config', []);
        cfgModule.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings',
          function (gnViewerSettings, gnSearchSettings, gnGlobalSettings) {
            gnGlobalSettings.init(uiSettings, sxtGnUrl, gnViewerSettings, gnSearchSettings);
          }]);

        // include sextant API
        var templateInclude = document.createElement("div");
        templateInclude.className = "gn " + sizeClass;
        templateInclude.setAttribute("ng-include", "'../../catalog/views/sextant/templates/index.html'");
        templateInclude.setAttribute("ng-controller", "GnCatController");
        templateInclude.setAttribute("sxt-size", sxtSize !== null ? sxtSize : "auto");
        if (sxtSizeDiff !== null) templateInclude.setAttribute("sxt-size-diff", sxtSizeDiff);
        appRoot.appendChild(templateInclude);

        // bootstrap app
        angular.bootstrap(appRoot, ['gn_search_sextant']);
        console.log('Loaded.');
      }, function (error) {
        console.error('Failed loading Sextant: ', error);
      });
    })

})();
