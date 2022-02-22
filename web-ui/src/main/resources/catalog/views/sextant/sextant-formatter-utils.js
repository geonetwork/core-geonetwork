(function() {
  // this is an ugly hack to allow loading this script outside of the wro4j pipeline
  if (typeof goog !== 'undefined') { goog.provide('gn_sxt_formatter_utils'); }

  var MAX_HEIGHT_LINE = 5;

  /**
   * @param {HTMLElement} element
   * @param {number} pxSize
   */
  function collapseElement(element, pxSize) {
    var contentChild = element.querySelector('.sxt-collapse-content');
    contentChild.style.maxHeight = pxSize + 'px';
    contentChild.style.overflowY = 'hidden';

    var toggleBtn = element.querySelector('.sxt-collapse-toggle');
    toggleBtn.innerHTML = '<span class="fa fa-lg fa-plus-circle"></span>';
    element.setAttribute('data-collapsed', '');

    toggleBtn.removeAttribute('background');
  }

  /**
   * @param {HTMLElement} element
   */
  function expandElement(element) {
    var contentChild = element.querySelector('.sxt-collapse-content');
    contentChild.style.removeProperty('max-height');
    contentChild.style.removeProperty('overflow-y');

    var toggleBtn = element.querySelector('.sxt-collapse-toggle');
    toggleBtn.innerHTML = '<span class="fa fa-lg fa-minus-circle"></span>';
    element.removeAttribute('data-collapsed');
    toggleBtn.style.background = getParentBackgroundStyle(element);
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
   * Returns the background style using the parent element color
   * @param {string} background css value
   */
  function getParentBackgroundStyle(parentElement) {
    var parentBgColor = getComputedStyle(parentElement).backgroundColor;
    var baseColor = '255, 255, 255';
    var matches = /^rgba?\(([0-9]+, [0-9]+, [0-9]+)/.exec(parentBgColor);
    if (matches && parentBgColor !== 'rgba(0, 0, 0, 0)') {
      baseColor = matches[1];
    }
    return 'linear-gradient(0deg, rgba(' + baseColor + ', 1) 40%, rgba(' + baseColor + ', 0))';
  }

  /**
   * Creates a toggle button and position it in the parent element
   * @param {HTMLElement} parentElement
   */
  function createToggleButton(parentElement) {
    var toggleButton = document.createElement('a');
    toggleButton.setAttribute('href', '');
    toggleButton.classList.add('sxt-collapse-toggle', 'text-right');
    toggleButton.style.display = 'block';
    toggleButton.style.position = 'absolute';
    toggleButton.style.bottom = '0';
    toggleButton.style.left = '0';
    toggleButton.style.right = '0';
    toggleButton.style.padding = '0.5em 0.5em 0.5em 0.5em';

    // get parent background color to determine the gradient
    toggleButton.style.background = getParentBackgroundStyle(parentElement);
    parentElement.appendChild(toggleButton);
    return toggleButton;
  }

  /**
   * This namespace provides lightweight utilities for static formatters (thus not relying on angularjs)
   */
  sxtFormatterUtils = {
    /**
     * This will process the targeted elements to shorten the
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
        contentChild.style.paddingBottom = '0.5em';
        element.innerHTML = '';
        element.style.position = 'relative';
        element.appendChild(contentChild);
        if (!getComputedStyle(element).position) {
          element.style.position = 'relative';
        }

        var toggleButton = createToggleButton(element);
        toggleButton.addEventListener('click', function (event) {
          if (element.hasAttribute('data-collapsed')) {
            expandElement(element);
          } else {
            collapseElement(element, maxHeightPx);
          }
          event.preventDefault();
        });

        // element is collapsed initially
        collapseElement(element, maxHeightPx);
      });
    }
  }

})();
