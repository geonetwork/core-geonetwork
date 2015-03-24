(function() {
  goog.provide('gn_formatter_lib');

  gnFormatter = {};
  gnFormatter.formatterSectionTogglersEventHandler = function(e) {
    var thisEl = $(this);
    thisEl.toggleClass('closed');
    var toggleAncestor = thisEl.attr('toggle-ancestor');
    if (!angular.isDefined(toggleAncestor)) {
      toggleAncestor = 1;
    } else {
      toggleAncestor = parseInt(toggleAncestor);
    }
    var ancestor = thisEl;
    while (toggleAncestor > 0) {
      ancestor = ancestor.parent();
      toggleAncestor--;
    }
    var toggleSelector = thisEl.attr('toggle-selector');
    if (toggleSelector) {
      ancestor.nextAll('.target').find(toggleSelector).toggle();
    } else {
      ancestor.nextAll('.target').first().toggle();
    }

    e.preventDefault();
  };

  gnFormatter.formatterOnComplete = function() {
    var navAnchors = $('.view-outline a[rel], .view-outline a[href]');

    $('.gn-metadata-view .toggler').on('click',
        gnFormatter.formatterSectionTogglersEventHandler);

    $.each(navAnchors, function(idx) {
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

  gnFormatter.loadAssociated = function(linkBlockSel, metadataId,
                                        parentUuid, spinnerSel) {
    var linkBlockEl = $(linkBlockSel);

    if (spinnerSel) {
      var spinner = $(spinnerSel);
      spinner.show();
    }

    if (linkBlockEl.length < 1) {
      if (spinner) {
        spinner.hide();
      }
      return;
    }
    var parentParam = '';
    if (angular.isDefined(parentUuid)) {
      parentParam = '&parentUuid=' + parentUuid;
    }

    $.ajax('md.format.xml?xsl=hierarchy_view&id=' + metadataId + parentParam, {
      dataType: 'text',
      success: function(html) {
        if (spinnerSel) {
          spinner.hide();
        }
        if (!html) {
          return;
        }

        var el = linkBlockEl.replaceWith(html);
        linkBlockEl = $('.summary-links-associated-link');

        linkBlockEl.find('.toggler').on('click',
            gnFormatter.formatterSectionTogglersEventHandler);
        if (linkBlockEl.find('table').children().length == 0) {
          linkBlockEl.hide();
          $('a[rel = ".container > .associated"]').attr('disabled', 'disabled');
        }
      },
      error: function(req, status, error) {
        if (spinnerSel) {
          spinner.hide();
        }
        linkBlockEl.html('<h3>Error loading related metadata</h3><p><pre>' +
            '<code>' + error.replace('<', '&lt;') + '</code></pre></p>');
        linkBlockEl.show();
      }
    });
  };

})();
