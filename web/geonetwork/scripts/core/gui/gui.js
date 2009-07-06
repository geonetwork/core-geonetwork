//=====================================================================================
//===
//=== GUI methods
//===
//=====================================================================================

//--- kernel must be included anyway

ker.include('core/gui/shower.js');
ker.include('core/gui/validator.js');
ker.include('core/gui/tooltip.js');
ker.include('core/gui/tab-switcher.js');

var gui = new Object();

/* Given a table, removes all rows but the first
 */

gui.removeAllButFirst = function(tableId)
{
	var rows = $(tableId).getElementsByTagName('TR');
	
	for (var i=rows.length-1; i>0; i--)
		Element.remove(rows[i]);		
}

/* Creates tooltips for all provided elements in an XML node
 */

gui.setupTooltips = function(node)
{
	var list = xml.children(node);
	
	for (var i=0; i<list.length; i++)
	{
		var id = list[i].getAttribute('id');
		
		gui.setupTooltip(id, list[i]);
	}
}

/* Sets an element tooltip
 *  - id   : the HTML element
 *  - node : an XML node whose content is converted into HTML and taken as the tooltip text
 */

gui.setupTooltip = function(id, node)
{
	var elem = $(id);
	var mesg = xml.toStringCont(node);
			
	if (elem != null)
		new Tooltip(elem, mesg);
}


gui.addToSelect = function(ctrl, value, content, selected)
{
	var node = xml.createElement('option', content);
	node.setAttribute('value', value);
	
	if (selected == true)
		node.setAttribute('selected', 'selected');
		
	$(ctrl).appendChild(node);
}


gui.appendTableRow = function(tableId, node)
{
	if (window.ActiveXObject)
		$(tableId).getElementsByTagName('TBODY')[0].appendChild(node);
	else
	{
		var htmlRow = xml.toString(node);
		new Insertion.Bottom(tableId, htmlRow);
	}
}


gui.replaceTableRow = function(rowId, newNode)
{
	var curr = $(rowId);
	
	if (window.ActiveXObject)
		curr.parentNode.replaceChild(newNode, curr);
	else
		curr.replace(xml.toString(newNode));
}

//=====================================================================================
