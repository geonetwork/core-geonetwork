if (window.sxtApiJQuery) {
  for (var p in window.sxtApiJQuery.fn) {
    if (!jQuery.fn.hasOwnProperty(p)) {
      jQuery.fn[p] = window.sxtApiJQuery.fn[p];
    }

    jQuery.fancybox = window.sxtApiJQuery.fancybox;
  }
}
