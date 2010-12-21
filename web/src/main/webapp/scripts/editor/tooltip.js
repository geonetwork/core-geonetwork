//=====================================================================================
//===
//=== Tooltip : shows a tooltip for an element of the editor.
//===
//=== Needs:	prototype.js
//===				kernel.js
//=====================================================================================

function Tooltip(ldr, el)
{
	var loader = ldr;
	var elem   = el;

	var shown = false;
	var exited= false;
	var tip   = null;
	var timer = null;
	
	var initDelay = 1000;
	
	Event.observe(elem, 'mouseover', ker.wrap(this, mouseIn));
	Event.observe(elem, 'mouseout',  ker.wrap(this, mouseOut));	

//=====================================================================================

function mouseIn(event)
{
	if (shown)
		return;
		
	var x = Event.pointerX(event) +12;
	var y = Event.pointerY(event) +12;
	
	if (tip == null)
		setupTooltip(x, y);
	else
	{
		tip.style.left = x;
		tip.style.top  = y;
	
		timer = setTimeout(ker.wrap(this, mouseIn_CB), initDelay);	
	}
}

//-------------------------------------------------------------------------------------

function mouseIn_CB()
{
	Element.show(tip);
	
	shown = true;
	timer = null;
}

//=====================================================================================

function mouseOut(event)
{
	exited = true;
	
	if (timer)
	{
		clearTimeout(timer);
		timer = null;
	}
	
	if (!shown)
		return;
		
	Element.hide(tip);	
	
	shown = false;
}

//=====================================================================================

function setupTooltip(x, y)
{
	var id     = elem.getAttribute('id');
	var tokens = id.substring(4).split('|');
	var schema = tokens[0];
	var name   = tokens[1];
	var context = tokens[2];
	var isoType = tokens[3];
	
	var request = str.substitute(requestTemp, { SCHEMA:schema, NAME:name, CONTEXT: context, ISOTYPE: isoType });
	
	exited = false;
	
	ker.send('xml.schema.info', request, ker.wrap(this, function(xmlRes)
	{
		if (xmlRes.nodeName == 'error')
			ker.showError(loader.getText('cannotGet'), xmlRes);
		else
		{
			var htmlTip= getHtmlTip(xmlRes.getElementsByTagName('element')[0]);
			
			tip = document.createElement('div');
			tip.className     = 'tooltip';
			tip.innerHTML     = htmlTip;
			tip.style.display = 'none';
			tip.style.zIndex  = 32000;
		
			document.body.appendChild(tip);
	
			tip.style.left = x;
			tip.style.top  = y;
	
			if (!exited)
				timer = setTimeout(ker.wrap(this, mouseIn_CB), 300);	
		}
	}));	
}

//=====================================================================================

function getHtmlTip(node)
{
	var err = node.getAttribute('error');
	
	if (err != null)
	{
		var temp = errorTemp;
		var msg  = loader.getText('error') +' : '+ err;
		var data = { ERROR : msg };
		
		return str.substitute(errorTemp, data);
	}
	else
	{
		var temp = tooltipTemp;
		var label = xml.evalXPath(node, 'label');
		var descr = xml.evalXPath(node, 'description');
		var cond = xml.evalXPath(node, 'condition');
		var help = xml.evalXPath(node, 'help');
		
		if (cond == null)
			cond = '';
		if (help == null)
			help = '';
		
		var data = { LABEL: label, DESCRIPTION : descr, CONDITION : cond, HELP : help };
		
		return str.substitute(tooltipTemp, data);
	}
}

//=====================================================================================
/**
 * FIXME : Here you need to add any namespace required by metadata schema.
 * How could we define required namespace from registered schemas in the catalogue ?
 * 
 */
var requestTemp =
'<request>'+
'   <element schema="{SCHEMA}" name="{NAME}" context="{CONTEXT}" isoType="{ISOTYPE}"/>'+
'</request>';

//=====================================================================================

var tooltipTemp=
'   <b>{LABEL}</b>'+
'   <br>'+
'   {DESCRIPTION}'+
'   <br>'+
'   <font color="#C00000">{CONDITION}</font>'+
'   <i>{HELP}</i>';

//=====================================================================================

var errorTemp=
'   <font color="#C00000">{ERROR}</font>';

//=====================================================================================
} 
