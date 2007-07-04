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
	var list = xml.children(node);
	
	for (var i=0; i<list.length; i++)
	{
		var elem = $(list[i].getAttribute('id'));
		var mesg = xml.toStringCont(list[i]);
		
		new Tooltip(elem, mesg);
	}
}

//=====================================================================================
