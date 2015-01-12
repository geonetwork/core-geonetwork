var linkBlockClass = $('.{{linkBlockClass}}');

$.ajax('md.format.xml?xsl=hierarchy_view&id={{metadataId}}', {
  dataType: "text",
  success: function (html) {
    if (!html) {
      return;
    }

    linkBlockClass.html(html);

    if (linkBlockClass.children().length > 0) {
      linkBlockClass.show();
      linkBlockClass.find(".toggler").on('click', formatterSectionTogglersEventHandler);
    } else {
      linkBlockClass.hide();
    }
  },
  error: function (req, status, error) {
    linkBlockClass.html('<h3>Error loading related metadata</h3><p><pre><code>' + error.replace("<", "&lt;") + '</code></pre></p>');
    linkBlockClass.show();
  }
});