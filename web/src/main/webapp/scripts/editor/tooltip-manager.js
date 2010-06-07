//=====================================================================================
//===
//=== Editor's tooltip manager class
//===
//=====================================================================================

//ker.include('editor/tooltip.js');

var tipMan = null;

//=====================================================================================

function init()
{
	tipMan = new TooltipManager();
	
	//--- waits for all files to be loaded
	ker.loadMan.wait(tipMan);
}

//=====================================================================================

function TooltipManager() 
{
	var loader = new XMLLoader(Env.locUrl +'/xml/editor.xml');

	//--- public methods

	this.init = init;

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init() 
{
	var list = document.getElementsByTagName('SPAN');
	
	for (var i=0; i<list.length; i++)
	{
		var id = list[i].getAttribute('id');
		
		if (id != null) {
			if (id.startsWith('tip.')) new Tooltip(loader, list[i]);
		}
	}
}

//=====================================================================================
}
