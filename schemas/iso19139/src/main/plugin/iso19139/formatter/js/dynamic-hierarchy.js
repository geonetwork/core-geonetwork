var linkBlockClass = $('.{{linkBlockClass}}');
var spinner = $('.associated-spinner');

spinner.show();

$.ajax('md.format.xml?xsl=hierarchy_view&id={{metadataId}}', {
  dataType: "text",
  success: function (html) {
    spinner.hide();
    if (!html) {
      return;
    }

    linkBlockClass.replaceWith(html);

    linkBlockClass = $(".{{linkBlockClass}}");

    if (linkBlockClass.find("table").children().length > 0) {
      linkBlockClass.find(".toggler").on('click', formatterSectionTogglersEventHandler);

      $('a[rel = ".container > .associated"]').removeAttribute('disabled');
    } else {
      linkBlockClass.hide();
      console.log($('a[rel = ".container > .associated"]').length)
    }
  },
  error: function (req, status, error) {
    spinner.hide();
    linkBlockClass.html('<h3>Error loading related metadata</h3><p><pre><code>' + error.replace("<", "&lt;") + '</code></pre></p>');
    linkBlockClass.show();
  }
});