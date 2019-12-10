(function() {

  goog.provide('sextant_include');

  var selector = document.currentScript.getAttribute('selector');
  var catalogName = document.currentScript.getAttribute('catalog');
  var staticUrl = document.currentScript.getAttribute('src').replace('sextant_include.js', '');

  var root = document.querySelector(selector);

  if (!root) {
    console.error('Invalid selector for Sextant root: ', selector);
    return;
  }

  var libs = document.createElement('script');
  libs.src = staticUrl + 'lib.js';
  root.appendChild(libs);

  var sextant = document.createElement('script');
  sextant.src = staticUrl + 'sextant_api.js';
  sextant.setAttribute('catalog', catalogName);
  root.appendChild(sextant);
})();
