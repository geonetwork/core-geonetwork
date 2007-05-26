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

//=====================================================================================

var gui = new Object();

//=====================================================================================
/* Given a table, removes all rows but the first
 */

gui.removeAllButFirst = function(tableId)
{
	var rows = $(tableId).getElementsByTagName('TR');
	
	for (var i=rows.length-1; i>0; i--)
		Element.remove(rows[i]);		
}

//=====================================================================================
/* Creates tooltips for all provided elements in an XML node
 */

gui.setupTooltips = function(node)
{
	node = node.firstChild;
	
	while (node != null)
	{
		if (node.nodeType == Node.ELEMENT_NODE)
		{
			var elem = $(node.getAttribute('id'));
			var mesg = xml.toStringCont(node);
		
			new Tooltip(elem, mesg);
		}
		
		node = node.nextSibling;
	}
}

//=====================================================================================
