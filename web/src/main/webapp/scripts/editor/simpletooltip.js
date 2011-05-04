/* Handle tooltips in a less complicated manner than tooltip-manager.js 
 * These simple click-on-tips are used by the metadata viewer/editor - 
 * simpletooltips have the prefix stip and use a manually defined onclick 
 * event to call the toolTip function that reads the tip from the server */

function toolTip(spanId)
{
	elem = $(spanId);
	if (elem.childElements().length == 0) {
		// cant use spanId, IE barfs
		var tokens = elem.getAttribute('id').split('|'); 
		var schema = tokens[0].substring(5); // remove stip. 
		var name   = tokens[1];
		var context = tokens[2];
        var fullContext = tokens[3];
        var isoType = tokens[4];

		var request = str.substitute(toolTipRequestTemp, { SCHEMA:schema, NAME:name, CONTEXT: context,
            FULLCONTEXT: fullContext, ISOTYPE: isoType });
		
		ker.send('xml.schema.info', request, ker.wrap(this, function(xmlRes) {
			var htmlTip = '';
			tip = document.createElement('div');
			tip.className     = 'toolTipOverlay';
			
			if (xmlRes.nodeName == 'error') {
				//ker.showError(translate('cannotGetTooltip'), xmlRes);
				htmlTip = translate('cannotGetTooltip');
			} else {
				htmlTip= getHtmlTip(xmlRes.getElementsByTagName('element')[0]);
			}
			tip.innerHTML     = htmlTip;
			elem.appendChild(tip);
			
		}));	
	} else { 
		childs = elem.childElements();
		childs[0].toggle();
	}
}

//=========================================================================

function getHtmlTip(node)
{
	var err = node.getAttribute('error');
	
	if (err != null)
	{
		var temp = toolTipErrorTemp;
		var msg  = 'ERROR : '+err;
		var data = { ERROR : msg };
		
		return str.substitute(toolTipErrorTemp, data);
	}
	else
	{
		var temp = toolTipTemp;
		var label= xml.evalXPath(node, 'label');
		var descr= xml.evalXPath(node, 'description');
		var cond = xml.evalXPath(node, 'condition');
		var help = xml.evalXPath(node, 'help');
        var help_link = xml.evalXPath(node, 'help_link');
		
		if (cond == null)
			cond = '';
		if (help == null)
			help = '';
			
        if (help_link != null) {
		    var data = { LABEL: label, DESCRIPTION : descr, HELP_LINK: help_link, CONDITION : cond, HELP : help };

		    return str.substitute(toolTipTempLink, data);
        } else {
            var data = { LABEL: label, DESCRIPTION : descr, CONDITION : cond,  HELP : help };

		    return str.substitute(toolTipTemp, data);
        }
	}
}

//=========================================================================
/**
 */
var toolTipRequestTemp =
'<request>'+ 
'   <element schema="{SCHEMA}" name="{NAME}" context="{CONTEXT}" fullContext="{FULLCONTEXT}" isoType="{ISOTYPE}"/>'+
'</request>';

//=====================================================================================

var toolTipTemp =
	'   <b>{LABEL}</b>'+
	'   <br/>'+
	'   <span class="tooltipDescription">{DESCRIPTION}</span>'+
	'   <br/>'+
	'   <font color="#C00000">{CONDITION}</font>'+
	'   <i>{HELP}</i>';

//=====================================================================================

var toolTipTempLink =  toolTipTemp +
'   <br/>'+
'   <a href="{HELP_LINK}" target="_blank">' + translate('helpLinkTooltip') + '</a>';

//=====================================================================================

var toolTipErrorTemp=
'   <font color="#C00000">{ERROR}</font>';

//=====================================================================================
