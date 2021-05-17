(function() {
  // this is an ugly hack to allow loading this script outside of the wro4j pipeline
  typeof goog !== 'undefined' && goog.provide('gn_sxt_formatter_utils');

  var MAX_HEIGHT_LINE = 5;

  /**
   * @param {HTMLElement} element
   * @param {number} pxSize
   */
  function collapseElement(element, pxSize) {
    var contentChild = element.querySelector('.sxt-collapse-content');
    contentChild.style.maxHeight = pxSize + 'px';
    contentChild.style.overflowY = 'hidden';

    var toggleBtn = element.querySelector('a.sxt-collapse-toggle');
    toggleBtn.textContent = 'read more...';
    element.setAttribute('data-collapsed', '');
  }

  /**
   * @param {HTMLElement} element
   */
  function expandElement(element) {
    var contentChild = element.querySelector('.sxt-collapse-content');
    contentChild.style.removeProperty('max-height');
    contentChild.style.removeProperty('overflow-y');

    var toggleBtn = element.querySelector('a.sxt-collapse-toggle');
    toggleBtn.textContent = 'show less';
    element.removeAttribute('data-collapsed');
  }

  /**
   * Returns the line height in px
   * @param {HTMLElement} element
   */
  function measureLineHeight(element) {
    return parseInt(getComputedStyle(element).getPropertyValue('line-height'), 10);
  }

  /**
   * Returns the inner height (without padding) in px
   * @param {HTMLElement} element
   */
  function measureInnerHeight(element) {
    var height = parseInt(getComputedStyle(element).getPropertyValue('height'), 10);
    var paddingTop = parseInt(getComputedStyle(element).getPropertyValue('padding-top'), 10);
    var paddingBottom = parseInt(getComputedStyle(element).getPropertyValue('padding-bottom'), 10);
    return height - paddingTop - paddingBottom;
  }

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
        var elHeightPx = measureInnerHeight(element);
        var lineHeightPx = measureLineHeight(element);
        if (elHeightPx < lineHeightPx * (MAX_HEIGHT_LINE + 1)) {
          return;
        }
        var maxHeightPx = lineHeightPx * MAX_HEIGHT_LINE;

        // put the element content in a child div
        var contentChild = document.createElement('div');
        contentChild.classList.add('sxt-collapse-content');
        contentChild.innerHTML = element.innerHTML;
        element.innerHTML = '';
        element.appendChild(contentChild);

        // create & append collapse button
        var toggleButton = document.createElement('a');
        toggleButton.setAttribute('href', '');
        toggleButton.classList.add('sxt-collapse-toggle');
        toggleButton.addEventListener('click', function (event) {
          if (element.hasAttribute('data-collapsed')) {
            expandElement(element);
          } else {
            collapseElement(element, maxHeightPx);
          }
          event.preventDefault();
        });
        element.appendChild(toggleButton);

        // element is collapsed initially
        collapseElement(element, maxHeightPx);
      });
    }
  }

})();
