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
	
	var request = str.substitute(requestTemp, { SCHEMA:schema, NAME:name });
	
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
		var msg  = loader.getText('error' +' : '+ err);
		var data = { ERROR : msg };
		
		return str.substitute(errorTemp, data);
	}
	else
	{
		var temp = tooltipTemp;
		var label= xml.evalXPath(node, 'label');
		var descr= xml.evalXPath(node, 'description');
		var cond = xml.evalXPath(node, 'condition');
		
		if (cond == null)
			cond = '';
			
		var data = { LABEL: label, DESCRIPTION : descr, CONDITION : cond };
		
		return str.substitute(tooltipTemp, data);
	}
}

//=====================================================================================

var requestTemp =
'<request xmlns:gmd="http://www.isotc211.org/2005/gmd"'+
'         xmlns:gts="http://www.isotc211.org/2005/gts"'+ 
'         xmlns:gml="http://www.opengis.net/gml"'+
'         xmlns:dc = "http://purl.org/dc/elements/1.1/">'+
'   <element schema="{SCHEMA}" name="{NAME}"/>'+
'</request>';

//=====================================================================================

var tooltipTemp=
'   <b>{LABEL}</b>'+
'   <br>'+
'   {DESCRIPTION}'+
'   <br>'+
'   <font color="#C00000">{CONDITION}</font>';

//=====================================================================================

var errorTemp=
'   <font color="#C00000">{ERROR}</font>';

//=====================================================================================
} 
