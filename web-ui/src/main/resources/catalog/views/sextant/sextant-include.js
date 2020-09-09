(function() {

  goog.provide('sextant_include');

  var scriptEl = document.currentScript;

  function copyScriptAttribute(targetEl, attrName) {
    if (scriptEl.hasAttribute(attrName)) {
      targetEl.setAttribute(attrName, scriptEl.getAttribute(attrName))
    }
  }

  var selector = scriptEl.getAttribute('selector');
  var staticUrl = scriptEl.getAttribute('src').replace('sextant_include.js', '');

  var root = document.querySelector(selector);

  if (!root) {
    console.error('Invalid selector for Sextant root: ', selector);
    return;
  }

  var libs = document.createElement('script');
  var libName = 'lib.js';
  if (scriptEl.hasAttribute('no-jquery')) {
    libName = 'lib_nojquery.js';
  } else if (scriptEl.hasAttribute('no-bootstrap')) {
    libName = 'lib_nobs.js';
  }
  libs.src = staticUrl + libName;
  root.appendChild(libs);

  var sextant = document.createElement('script');
  sextant.src = staticUrl + 'sextant_api.js';

  // copy input attributes to the script which loads Sextant
  copyScriptAttribute(sextant, 'catalog')
  copyScriptAttribute(sextant, 'max-sm')
  copyScriptAttribute(sextant, 'max-md')
  copyScriptAttribute(sextant, 'size')
  copyScriptAttribute(sextant, 'size-diff')

  root.appendChild(sextant);
})();
