var linkBlockClass = $('.{{linkBlockClass}}');
linkBlockClass.hide();


var typeTranslations = {
{{typeTranslations}}
};
$.ajax('xml.relation?id={{metadataId}}&type={{relatedTypes}}', {
  dataType: "json",
  success: function (data) {
    var types = {};
    var placeholder = $('#{{placeholderId}}');

    if (!data || !data.relation) {
      return;
    }
    var relations = data.relation instanceof Array ? data.relation : [data.relation];

    $.each(relations, function (idx, rel) {
      var type = rel['@type'];
      var md;
      if (!rel.metadata) {
        return;
      }
      if (rel.metadata instanceof Array) {
        md = rel.metadata;
      } else {
        md = [rel.metadata];
      }
      $.each(md, function (mdIdx, md) {
        var uuid = md['geonet:info'].uuid;
        var title = md.title ? md.title : md.defaultTitle;
        if (!title) {
          title = uuid;
        }

        var url;
        if (uuid) {
          url = "javascript:window.open('md.format.html?xsl=full_view&schema=iso19139&uuid=" + encodeURIComponent(uuid) + "', '" + uuid + "');"
        } else {
          url = "javascript:alert('{{noUuidInLink}}');"
        }

        var obj = { url: url, title: title};

        if (title && uuid) {
          if (types[type]) {
            types[type].push(obj);
          } else {
            types[type] = [obj];
          }
        }
      });
    });

    $.each(types, function (key, value) {
      var typeTitle = typeTranslations[key] ? typeTranslations[key] : key;
      var html = '<div class="col-xs-12" style="background-color: #F7EEE1;">' +
        '  <img src="{{imagesDir}}' + key + '.png"/>' +
        '  ' + typeTitle + '</div>';
      $.each(value, function (idx, rel) {
        html += '  <div class="col-xs-6 col-md-4"><a href="' + rel.url + '">' + rel.title + '</a></div>';
      });
      placeholder.append(html);
    });

    if (placeholder.children().length > 0) {
      linkBlockClass.show();
    }
  },
  error: function (req, status, error) {
    $('#{{placeholderId}}').append('<h3>Error loading related metadata</h3><p>' + error + '</p>');
    linkBlockClass.show();
  }
});