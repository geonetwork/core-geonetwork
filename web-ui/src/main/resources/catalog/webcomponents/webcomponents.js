function getTemplate(document, baseUrl, portal, uiConfig) {
  var app = document.createElement("div");
  app.setAttribute("id", "gn-app");
  app.setAttribute("data-ng-controller", "GnCatController");

  var div = document.createElement("div");
  div.setAttribute("data-ng-include", "'" + baseUrl + "/catalog/views/default/templates/index.html'");
  div.setAttribute("class", "gn-full");
  app.appendChild(div);


  var css = document.createElement("link");
  css.setAttribute("href", baseUrl + "/static/gn_search_default.css");
  css.setAttribute("rel", "stylesheet");
  app.appendChild(css);

  ["/static/lib.js"].forEach(function(src) {
    var script = document.createElement("script");
    script.setAttribute("src", baseUrl + src);
    script.setAttribute("onload", "loadDependency()");
    app.appendChild(script);
  });

  var script = document.createElement("script");
  script.setAttribute("type", "text/javascript");

  script.textContent = "" +
    "var root = document.getElementsByTagName('gn-app')[0].shadowRoot;\n" +
    "function loadDependency() {" +
    "  [\"" + baseUrl + "/static/gn_search_default.js\"].forEach(function(src) {\n" +
    "    var script = document.createElement(\"script\");\n" +
    "    script.setAttribute(\"src\", src);\n" +
    "    script.setAttribute(\"onload\", \"bootstrap()\");\n" +
    "    root.appendChild(script);\n" +
    "  });\n" +
    "};" +
    "" +
    "function bootstrap() {" +
    "    config = " + JSON.stringify(uiConfig) + " || {};\n" +
    "console.log(config);\n" +
    "    var cfgModule = angular.module('gn_config', []);\n" +
    "    cfgModule.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings',\n" +
    "      function (gnViewerSettings, gnSearchSettings, gnGlobalSettings) {\n" +
    "        gnGlobalSettings.init(config, '" + baseUrl + "/" + portal + "/', gnViewerSettings, gnSearchSettings);\n" +
    "      }]);" +
    "    angular.bootstrap(root, ['gn_search_default']);" +
    "};";
  app.appendChild(script);


  var style = document.createElement("style");
  style.setAttribute("type", "text/css");
  var css = "@charset \"UTF-8\";[ng\\:cloak],[ng-cloak],[data-ng-cloak],[x-ng-cloak],.ng-cloak,.x-ng-cloak,.ng-hide:not(.ng-hide-animate){display:none !important;}ng\\:form{display:block;}.ng-animate-shim{visibility:hidden;}.ng-anchor{position:absolute;}";
  style.textContent = css;
  app.appendChild(style);

  return app;
}

customElements.define("gn-app",
  class extends HTMLElement {
    constructor() {
      super();

      var baseUrl = this.getAttribute("url") || "/geonetwork";
      var portal = this.getAttribute("portal") || "srv";
      var uiConfig = this.getAttribute("config");
      try {
        uiConfig = JSON.parse(uiConfig);
      } catch (e) {
        console.warn('Invalid configuration: ' + uiConfig + '. Using default.');
        console.warn(e);
        uiConfig = {};
      }

      this.attachShadow({ mode: "open" })
        .appendChild(getTemplate(document, baseUrl, portal, uiConfig));

      var style = document.createElement("style");
      style.setAttribute("type", "text/css");
      var css = "\n" +
        "@font-face {\n" +
        "  font-family: 'FontAwesome';\n" +
        "  src: url('../lib/style/font-awesome/fonts/fontawesome-webfont.eot?v=4.4.0');\n" +
        "  src: url('../lib/style/font-awesome/fonts/fontawesome-webfont.eot?#iefix&v=4.4.0') format('embedded-opentype'), url('../lib/style/font-awesome/fonts/fontawesome-webfont.woff2?v=4.4.0') format('woff2'), url('../lib/style/font-awesome/fonts/fontawesome-webfont.woff?v=4.4.0') format('woff'), url('../lib/style/font-awesome/fonts/fontawesome-webfont.ttf?v=4.4.0') format('truetype'), url('../lib/style/font-awesome/fonts/fontawesome-webfont.svg?v=4.4.0#fontawesomeregular') format('svg');\n" +
        "  font-weight: normal;\n" +
        "  font-style: normal;\n" +
        "}";
      style.textContent = css;
      document.head.appendChild(style);
    }
  }
);
