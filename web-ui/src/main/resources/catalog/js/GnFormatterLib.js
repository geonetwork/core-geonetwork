/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function () {
  goog.provide("gn_formatter_lib");

  var gnFormatter = {};
  gnFormatter.formatterSectionTogglersEventHandler = function (e) {
    var thisEl = $(e.currentTarget);
    thisEl.toggleClass("closed");

    e.preventDefault();

    var visible = $('[id="' + thisEl.attr("target") + '"]')
      .toggle()
      .is(":visible");
    return visible;
  };

  gnFormatter.formatterOnComplete = function () {
    var navAnchors = $(".view-outline a[rel], .view-outline a[href]");

    $(".gn-metadata-view .toggler").on(
      "click",
      gnFormatter.formatterSectionTogglersEventHandler
    );

    $.each(navAnchors, function (idx) {
      var rel = $($.attr(navAnchors[idx], "rel"));
      var href = $.attr(navAnchors[idx], "href");
      if (rel.length == 0 && !href) {
        $(navAnchors[idx]).attr("disabled", "disabled");
      }
    });

    var selectGroup = function (el) {
      $(".container > .entry").hide();
      $(el.attr("rel")).show();
      $("li.active").removeClass("active");
      el.parent().addClass("active");
    };
    navAnchors.on("click", function (e) {
      var href = $(this).attr("href");
      if (!href) {
        selectGroup($(this));
        e.preventDefault();
      }
    });

    if (navAnchors.length < 2) {
      $(".view-outline").hide();
    }

    if (navAnchors.length > 0) {
      selectGroup(navAnchors.first());
    }
  };

  gnFormatter.depth = function (linkBlockEl) {
    if (linkBlockEl.length > 0) {
      var inc = 0,
        parent = linkBlockEl.parent();
      if (parent.hasClass("associated-link-row")) {
        inc++;
      }
      return inc + gnFormatter.depth(parent);
    }

    return 0;
  };
  gnFormatter.loadAssociated = function (
    event,
    linkBlockSel,
    metadataId,
    parentUuid,
    spinnerSel
  ) {
    var linkBlockEl = $(linkBlockSel);

    if (angular.isDefined(event)) {
      var isLoading = $("div[associated-loading=loading]").length > 0;
      if (isLoading) {
        return;
      }

      var closeAssociated = linkBlockEl.is(":visible");
      var rowSelector = "div.associated-link-row";
      var loadingDoneSel = "div[associated-loading=done]";
      var parentSelector = linkBlockEl.parent().attr("parent");
      var rows, loaded;
      if (angular.isDefined(parentSelector)) {
        var parent = $(parentSelector);
        rows = parent.find(rowSelector);
        loaded = parent.find(loadingDoneSel);
      } else {
        rows = $(rowSelector);
        loaded = $(loadingDoneSel);
      }
      rows.toggle(false);
      loaded.removeAttr("associated-loading").html("");
      if (closeAssociated) {
        return;
      }
      var associatedLinkAClass = "associated-link-a";
      $("a." + associatedLinkAClass).addClass("disabled");
      gnFormatter.formatterSectionTogglersEventHandler(event);
      linkBlockEl.attr("associated-loading", "loading");
    }

    linkBlockEl.html(
      '<h3><i class="fa fa-sitemap pad-right"></i>' +
        '<i class="fa fa-circle-o-notch fa-spin pad-right associated-spinner"' +
        "></i></h3>"
    );
    if (spinnerSel) {
      var spinner = $(spinnerSel);
      spinner.show();
    }

    var parentParam = "";
    if (angular.isDefined(parentUuid)) {
      parentParam = "&parentUuid=" + parentUuid;
    }

    $.ajax(
      "md.format.xml?xsl=hierarchy_view&skipPopularity=y&uuid=" +
        metadataId +
        parentParam,
      {
        dataType: "text",
        success: function (html) {
          if (spinnerSel) {
            spinner.hide();
          }

          $("a.associated-link-a").removeClass("disabled");
          if (!html) {
            return;
          }
          if (angular.isDefined(event)) {
            linkBlockEl.html(html);
            linkBlockEl.attr("associated-loading", "done");
            linkBlockEl.find("div.associated-link-row").attr("parent", linkBlockSel);
            if (gnFormatter.depth(linkBlockEl) >= 2) {
              linkBlockEl
                .find("a." + associatedLinkAClass)
                .removeClass(associatedLinkAClass)
                .removeAttr("onclick")
                .attr("style", "color:black; text-decoration:initial");
            }
          } else {
            linkBlockEl.replaceWith(html);
            linkBlockEl = $(".summary-links-associated-link");
          }
          var togglerElements = linkBlockEl.find(".toggler");
          togglerElements.off("click", gnFormatter.formatterSectionTogglersEventHandler);
          togglerElements.on("click", gnFormatter.formatterSectionTogglersEventHandler);

          if (linkBlockEl.find("table").children().length == 0) {
            linkBlockEl.hide();
            $('a[rel = ".container > .associated"]').attr("disabled", "disabled");
          }
        },
        error: function (req, status, error) {
          if (spinnerSel) {
            spinner.hide();
          }
          linkBlockEl.html(
            "<h3>Error loading related metadata</h3><p><pre>" +
              "<code>" +
              error.replaceAll("<", "&lt;") +
              "</code></pre></p>"
          );
          linkBlockEl.show();
        }
      }
    );
  };

  gnFormatter.toggleTab = function (tabElementId) {
    var tab = $('.view-outline [id="tab-' + tabElementId + '"] a[rel]');
    if (tab.length > 0) {
      tab.click();
    } else {
      console.warn("Invalid tab requested in URL: " + tabElementId);
    }
  };
})();
