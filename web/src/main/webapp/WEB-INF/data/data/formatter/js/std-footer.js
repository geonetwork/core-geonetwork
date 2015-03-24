(function () {

  try {
    gnFormatter.formatterOnComplete();
  } catch (e) {
    gnFormatter = {};
    gnFormatter.formatterSectionTogglersEventHandler = function (e) {
      var thisEl = $(this);
      thisEl.toggleClass('closed');
      var toggleAncestor = thisEl.attr('toggle-ancestor');
      if (angular.isDefined(toggleAncestor)) {
        toggleAncestor = 1;
      } else {
        toggleAncestor = parseInt(toggleAncestor);
      }
      var ancestor = thisEl;
      while (toggleAncestor > 0) {
        ancestor = ancestor.parent();
        toggleAncestor--;
      }
      debugger;
      var toggleSelector = thisEl.attr('toggle-selector');
      if (toggleSelector) {
        ancestor.nextAll('.target').find(toggleSelector).toggle();
      } else {
        ancestor.nextAll('.target').first().toggle();
      }

      e.preventDefault();
    };

    gnFormatter.formatterOnComplete = function () {
      var navAnchors = $('.view-outline a[rel], .view-outline a[href]');

      $('.gn-metadata-view .toggler').on('click', gnFormatter.formatterSectionTogglersEventHandler);

      $.each(navAnchors, function (idx) {
        var rel = $($.attr(navAnchors[idx], 'rel'));
        var href = $.attr(navAnchors[idx], 'href');
        if (rel.length == 0 && !href) {
          $(navAnchors[idx]).attr('disabled', 'disabled');
        }
      });

      var selectGroup = function (el) {
        $('.container > .entry').hide();
        $(el.attr('rel')).show();
        $('li.active').removeClass('active');
        el.parent().addClass('active');
      };
      navAnchors.on('click', function (e) {
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

    gnFormatter.formatterOnComplete();
  }
})();
