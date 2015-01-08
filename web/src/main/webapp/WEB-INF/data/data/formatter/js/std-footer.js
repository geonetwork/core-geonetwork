var formatterOnComplete = function () {
  var navAnchors = $('.view-outline a[rel]');

  $('.gn-metadata-view .toggler').on('click', function(e) {
    $(this).toggleClass('closed');
    $(this).parent().nextAll('.target').first().toggle();

    e.preventDefault();
  });

  $.each(navAnchors, function (idx) {
    var rel = $.attr(navAnchors[idx], 'rel');
    if ($(rel).length == 0) {
      $(navAnchors[idx]).attr('disabled', 'disabled');
    }
  });

  var selectGroup = function(el) {
    $('.container > .entry').hide();
    $(el.attr('rel')).show();
    $('li.active').removeClass('active');
    el.parent().addClass('active');
  };
  navAnchors.on('click', function(e) {
    selectGroup($(this));
    e.preventDefault();
  });

  if (navAnchors.length < 2) {
    $('.view-outline').hide();
  }

  if (navAnchors.length >0) {
    selectGroup(navAnchors.first());
  }

};

formatterOnComplete();

