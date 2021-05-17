(function() {
  // this is an ugly hack to allow loading this script outside of the wro4j pipeline
  typeof goog !== 'undefined' && goog.provide('gn_sxt_formatter_utils');

  var MAX_FIELD_HEIGHT_PX = 50;

  /**
   * This namespace provides lightweight utilities for static formatters (thus not relying on angularjs)
   */
  sxtFormatterUtils = {
    /**
     * This will process the targetted elements to shorten the
     * ones that are too long, and offer a "read more" option
     * @param {string} selector CSS selector
     */
    processCollapsableField: function (selector) {
      document.querySelectorAll(selector).forEach(function (element) {
        var elHeight = element.getBoundingClientRect().height;
        if (elHeight < MAX_FIELD_HEIGHT_PX) {
          return;
        }

        console.log('processing collapsable field:', element);
      });
    }
  }

})();
