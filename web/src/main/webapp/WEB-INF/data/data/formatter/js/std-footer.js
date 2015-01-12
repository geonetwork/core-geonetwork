var formatterSectionTogglersEventHandler = function(e) {
  $(this).toggleClass('closed');
  $(this).parent().nextAll('.target').first().toggle();

  e.preventDefault();
};

var formatterOnComplete = function () {
  var navAnchors = $('.view-outline a[rel], .view-outline a[href]');

  $('.gn-metadata-view .toggler').on('click', formatterSectionTogglersEventHandler);

  $.each(navAnchors, function (idx) {
    var rel = $($.attr(navAnchors[idx], 'rel'));
    var href = $.attr(navAnchors[idx], 'href');
    if (rel.length == 0 && !href) {
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
    var href = $(this).attr('href');
    if (!href) {
      selectGroup($(this));
      e.preventDefault();
    }
  });

  if (navAnchors.length < 2) {
    $('.view-outline').hide();
  }

  if (navAnchors.length > 0) {
    selectGroup(navAnchors.first());
  }

};

formatterOnComplete();

