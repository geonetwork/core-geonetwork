LegacyViewerTooltip = {
  toolTipRequestTemp: new Ext.Template(
    '<request>' + 
    '   <element schema="{SCHEMA}" name="{NAME}" context="{CONTEXT}" fullContext="{FULLCONTEXT}" isoType="{ISOTYPE}"/>' + 
    '</request>', {
    compiled: true,
    disableFormats: true
  }),
  toolTipTemp: new Ext.Template(
    '   <b>{LABEL}</b>' + 
    '   <br/>' + 
    '   <span class="tooltipDescription">{DESCRIPTION}</span>' + 
    '   <br/>' + 
    '   <font color="#C00000">{CONDITION}</font>' + 
    '   <i>{HELP}</i>', {
    compiled: true,
    disableFormats: true
  }),
  toolTipTempLink: new Ext.Template(
    '<b>{LABEL}</b>' + 
    '   <br/>' + 
    '   <span class="tooltipDescription">{DESCRIPTION}</span>' + 
    '   <br/>' + 
    '   <font color="#C00000">{CONDITION}</font>' + 
    '   <i>{HELP}</i>' + 
    '   <br/>' + 
    '   <a href="{HELP_LINK}" target="_blank">' + translate('helpLinkTooltip') + '</a>', {
    compiled: true,
    disableFormats: true
  }),
  toolTipErrorTemp: new Ext.Template('   <font color="#C00000">{ERROR}</font>', {
    compiled: true,
    disableFormats: true
  }),
  createTip: function(elem, htmlTip) {    
    var tip = elem.createChild({
      tag: 'div',
      html: htmlTip,
      cls: 'toolTipOverlay'
    });
    
  },
  getHtmlTip: function(node) {
    var err = node.getAttribute('error');

    if (err != null) {
      var msg = 'ERROR : ' + err;
      var data = {ERROR: msg};

      return LegacyViewerTooltip.toolTipErrorTemp.apply(data);
    } else {
      var label = Ext.DomQuery.selectValue('label', node);
      var descr = Ext.DomQuery.selectValue('description', node);
      var cond = Ext.DomQuery.selectValue('condition', node);
      var help = Ext.DomQuery.selectValue('help', node);
      var help_link = Ext.DomQuery.selectValue('help_link', node);

      if (cond == null) cond = '';
      if (help == null) help = '';

      if (help_link != null) {
        var data = {
          LABEL: label,
          DESCRIPTION: descr,
          HELP_LINK: help_link,
          CONDITION: cond,
          HELP: help
        };

        return LegacyViewerTooltip.toolTipTempLink.apply(data);
      } else {
        var data = {
          LABEL: label,
          DESCRIPTION: descr,
          CONDITION: cond,
          HELP: help
        };

        return LegacyViewerTooltip.toolTipTemp.apply(data);
      }
    }
  }
};

function toolTip(spanId) {
  var elem = Ext.get(spanId);

  if (!elem.child('div')) {
    // cant use spanId, IE barfs
    var tokens = elem.getAttribute('id').split('|');
    var schema = tokens[0].substring(5); // remove stip. 
    var name = tokens[1];
    var context = tokens[2];
    var fullContext = tokens[3];
    var isoType = tokens[4];

    var requestData = LegacyViewerTooltip.toolTipRequestTemp.apply({
      SCHEMA: schema,
      NAME: name,
      CONTEXT: context,
      FULLCONTEXT: fullContext,
      ISOTYPE: isoType
    });


    Ext.Ajax.request({
      url: 'xml.schema.info',
      method: 'POST',
      xmlData: requestData,
      headers: {
        'Content-Type': 'text/xml; charset=utf-8'
      },
      success: function(response) {
        Ext.each(Ext.query(".toolTipOverlay"), function(e) {Ext.get(e).setVisible(false)});

        var xmlRes = response.responseXML
        var htmlTip = '';
        if (xmlRes.nodeName == 'error') {
          htmlTip = translate('cannotGetTooltip');
        } else {
          htmlTip = LegacyViewerTooltip.getHtmlTip(xmlRes.getElementsByTagName('element')[0]);
        }

        LegacyViewerTooltip.createTip(elem, htmlTip);
      },
      failure: function(response) {
        var msg = translate('cannotGetTooltip');

        LegacyViewerTooltip.createTip(elem, msg);
      }
    });

  } else {
    var tip = elem.child('div')
    var isVisible = tip.isVisible();
    Ext.each(Ext.query(".toolTipOverlay"), function(e) {Ext.get(e).setVisible(false)});

    if(!isVisible) {
      tip.setVisible(true);
    }
  }
}
