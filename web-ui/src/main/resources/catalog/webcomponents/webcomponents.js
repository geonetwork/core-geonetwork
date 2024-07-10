function validURL(str) {
  try {
    return Boolean(new URL(str));
  } catch(e) {
    return false;
  }
}

function buildBaseUrl(urlAttribute) {
  if (urlAttribute) {
    return urlAttribute;
  } else {
    return window.location.href.replace(/(.*)\/catalog.*/, "$1");
  }
}

var DEFAULT_PORTAL = "srv";

customElements.define(
  "gn-app",
  class extends HTMLElement {
    constructor() {
      super();
    }

    connectedCallback() {
      this.load();
      var baseUrl = buildBaseUrl(this.getAttribute("url"));
      var link = document.createElement("link");
      link.setAttribute("rel", "stylesheet");
      link.setAttribute("href", baseUrl + "/catalog/style/gn_web_components.css");
      document.head.appendChild(link);
    }

    getTemplate(document, baseUrl, portal, uiConfig) {
      var app = document.createElement("div");
      app.setAttribute("id", "gn-app");
      app.setAttribute("data-ng-controller", "GnCatController");

      var div = document.createElement("div");
      div.setAttribute(
        "data-ng-include",
        "'" + baseUrl + "/catalog/views/default/templates/index.html'"
      );
      div.setAttribute("class", "gn-full");
      app.appendChild(div);

      ["gn_search_default.css", "bootstrap-table.min.css", "gn_inspire.css"].forEach(function (src) {
        var css = document.createElement("link");
        css.setAttribute("href", baseUrl + "/static/" + src);
        css.setAttribute("rel", "stylesheet");
        app.appendChild(css);
      });

      ["/static/lib.js"].forEach(function (src) {
        var script = document.createElement("script");
        script.setAttribute("src", baseUrl + src);
        script.setAttribute("onload", "loadDependency()");
        app.appendChild(script);
      });

      var script = document.createElement("script");
      script.setAttribute("type", "text/javascript");

      var gnUrl = baseUrl + "/" + portal + "/";
      script.textContent =
        "" +
        "var gnShadowRoot = document.getElementsByTagName('gn-app')[0].shadowRoot;\n" +
        "function gnBootstrap() {" +
        "    config = " +
        JSON.stringify(uiConfig) +
        " || {};\n" +
        "    var cfgModule = angular.module('gn_config', []);\n" +
        "    cfgModule.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings', '$sceDelegateProvider',\n" +
        "      function (gnViewerSettings, gnSearchSettings, gnGlobalSettings, $sceDelegateProvider) {\n" +
        "        $sceDelegateProvider.trustedResourceUrlList(['self', '" + baseUrl + "/**']);\n" +
        "        gnGlobalSettings.init(config, '" +
        gnUrl + "', '" + gnUrl +
        "', gnViewerSettings, gnSearchSettings);\n" +
        "      }]);" +
        "angular.bootstrap(gnShadowRoot, ['gn_search_default']);" +
        "};" +
        "function loadDependency() {" +
        '  ["' +
        baseUrl +
        '/static/gn_search_default.js"].forEach(function(src) {\n' +
        '    var script = document.createElement("script");\n' +
        '    script.setAttribute("src", src);\n' +
        '    script.setAttribute("onload", "gnBootstrap()");\n' +
        "    gnShadowRoot.appendChild(script);\n" +
        "  });\n" +
        "};";
      app.appendChild(script);

      var style = document.createElement("style");
      style.setAttribute("type", "text/css");
      var css =
        '@charset "UTF-8";[ng\\:cloak],[ng-cloak],[data-ng-cloak],[x-ng-cloak],.ng-cloak,.x-ng-cloak,.ng-hide:not(.ng-hide-animate){display:none !important;}ng\\:form{display:block;}.ng-animate-shim{visibility:hidden;}.ng-anchor{position:absolute;}';
      style.textContent = css;
      app.appendChild(style);

      return app;
    }

    attached = false;

    load() {
      var baseUrl = buildBaseUrl(this.getAttribute("url"));
      var portal = this.getAttribute("portal") || DEFAULT_PORTAL;

      if (!validURL(baseUrl + "/" + portal)) {
        console.warn("Invalid configuration. Check URL");
        return;
      }
      function start(customElements, uiConfig) {
        try {
          uiConfig = JSON.parse(uiConfig);
        } catch (e) {
          console.warn("Invalid configuration: " + uiConfig + ". Using default.");
          console.warn(e);
          uiConfig = {};
        }

        if (typeof angular !== "undefined") {
          // TODO: Could we reload a running angular app?
          // var aApp = angular.element($("#gn-app")).scope();
          // aApp.gnGlobalSettings.init(
          //   config,
          //   "http://localhost:8080/geonetwork/srv/",
          //   gnViewerSettings,
          //   gnSearchSettings
          // )
        } else if (customElements.attached === false) {
          customElements.attachShadow({mode: "open"}).appendChild(
            customElements.getTemplate(document, baseUrl, portal, uiConfig)
          );
          customElements.attached = true;
        }
      }

      var baseUrl = buildBaseUrl(this.getAttribute("url"));
      var portal = this.getAttribute("portal") || DEFAULT_PORTAL;
      var gnUrl = baseUrl + "/" + portal;
      if (!validURL(gnUrl)) {
        console.warn("Invalid configuration. Check URL");
        return;
      }

      var uiConfig = this.getAttribute("config") || "{}";;
      var customElements = this;
      if (uiConfig.match(/^[\w-]*$/) != null) {
        fetch(gnUrl + "/api/ui/" + uiConfig, {
          headers: {
            "Accept": "application/json",
          }
        }).then(function (response) {
          response.json().then(function(json) {
            start(customElements, json.configuration);
          });
        });
      } else {
        start(this, uiConfig);
      }
    }

    static get observedAttributes() {
      return ["config", "portal", "style", "language"];
    }

    attributeChangedCallback(name, oldValue, newValue) {
      if (this.attached !== true || oldValue === newValue) {
        return;
      }
      console.warn("Reloading gn-app webcomponent on attribute change is not supported. Change attributes and reload page or use gn-app-frame.")
    }
  }
);

customElements.define(
  "gn-app-frame",
  class extends HTMLElement {
    constructor() {
      super();
    }

    connectedCallback() {
      this.load();
    }

    attached = false;

    getTemplate(document, baseUrl, portal, language, uiConfig, style) {
      var app = document.createElement("iframe");
      app.setAttribute("name", "gn-app");
      app.setAttribute("style", style);
      var url = baseUrl + "/" + portal + "/" + language + "/catalog.search";
      if (uiConfig != "") {
        if (uiConfig.match(/^[\w-]*$/) != null) {
          url += "?ui=" + uiConfig.trim();
        } else {
          try {
            JSON.parse(uiConfig);
          } catch (e) {
            console.warn(
              "Invalid configuration: " + uiConfig + ". Using default."
            );
            console.warn(e);
            return;
          }
          url += "?uiconfig=" + encodeURIComponent(uiConfig);
        }
      }
      app.setAttribute("src", url);
      return app;
    }

    load() {
      var baseUrl = buildBaseUrl(this.getAttribute("url"));
      var portal = this.getAttribute("portal") || DEFAULT_PORTAL;

      if (!validURL(baseUrl + "/" + portal)) {
        console.warn("Invalid configuration. Check URL");
        return;
      }

      var language = this.getAttribute("language") || "eng";
      if (language.match(/^[a-z]{3}$/) == null) {
        console.warn("Invalid configuration. Check language");
        return;
      }

      var uiConfig = this.getAttribute("config") || "";

      var style =
        this.getAttribute("style") || "width:100%;height:80%;border:none";

      if (this.attached === false) {
        this.attachShadow({ mode: "open" }).appendChild(
          this.getTemplate(document, baseUrl, portal, language, uiConfig, style)
        );
        this.attached = true;
      } else {
        this.shadowRoot.innerHTML = "";
        this.shadowRoot.appendChild(
          this.getTemplate(document, baseUrl, portal, language, uiConfig, style)
        );
      }
    }

    static get observedAttributes() {
      return ["config", "portal", "style", "language"];
    }

    attributeChangedCallback(name, oldValue, newValue) {
      if (this.attached !== true || oldValue === newValue) {
        return;
      }
      if (["config", "portal", "style", "language"].indexOf(name) !== -1) {
        this.load();
      }
    }
  }
);
