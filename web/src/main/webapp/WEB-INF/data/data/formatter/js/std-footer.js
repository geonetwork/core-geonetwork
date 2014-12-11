$('.toggler').on('click', function() {
  $(this).toggleClass('closed');
  $(this).parent().nextAll('.target').first().toggle();
});
var navAnchors = $('.view-outline a[rel]');

$.each(navAnchors, function (idx) {
  var rel = $.attr(navAnchors[idx], 'rel');
  if ($(rel).length == 0) {
    $(navAnchors[idx]).attr('disabled', 'disabled');
  }
});

navAnchors.on('click', function(e) {
  var el = $(this);
  $('.gn-metadata-main > .entry').hide();
  $(el.attr('rel')).show();
  $('li.active').removeClass('active');
  el.parent().addClass('active');
  e.preventDefault();
});

navAnchors[0].click();
