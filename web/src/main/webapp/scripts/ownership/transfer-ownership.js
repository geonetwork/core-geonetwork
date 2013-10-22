//=====================================================================================
//===
//=== Transfer ownership class
//===
//=====================================================================================

ker.include('ownership/model.js');
ker.include('ownership/view.js');

var ownership = null;

//=====================================================================================

function init()
{
	ownership = new TransferOwnership();
	
	//--- waits for all files to be loaded
	ker.loadMan.wait(ownership);
}

//=====================================================================================

function TransferOwnership()
{
	var loader = new XMLLoader(Env.locUrl +'/xml/transfer-ownership.xml');
	var model  = new Model(loader);
	var view   = new View(loader);

	Event.observe('source.user', 'change', ker.wrap(this, sourceUserChange));
	
	//--- public methods

	this.init     = init;
	this.transfer = transfer;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	view.clearGroupList();
	model.getEditors(ker.wrap(this, init_OK));
}

//-------------------------------------------------------------------------------------

function init_OK(data)
{
	if (data.length == 0)
		alert(loader.getText('noEditors'));
	else
		for (var i=0; i<data.length; i++)
			view.addSourceUser(data[i].id, data[i].surname +' '+ data[i].name +' ('+ data[i].username +')');
}

//=====================================================================================

function transfer(groupId)
{
	var tr = $(groupId);
	
	var sourceGrp = tr.getAttribute('id');
	var sourceUsr = $F('source.user');
	var targetGrp = $F(xml.getElementById(tr, 'target.group'));
	var targetUsr = $F(xml.getElementById(tr, 'target.user'));
	
	if (targetUsr == null)
		alert(loader.getText('noUser'));
	else
		model.transfer(sourceUsr, sourceGrp, targetUsr, targetGrp, ker.wrap(this, function(xmlRes)
		{
			var data =
			{
				PRIV : xml.evalXPath(xmlRes, 'privileges'),
				MD   : xml.evalXPath(xmlRes, 'metadata')
			};
			
			var msg = str.substitute(loader.getText('result'), data);
			
			alert(msg);
			
			if (sourceGrp != targetGrp)
				Element.remove(tr);
		}));
}

//=====================================================================================
//=== Listener
//=====================================================================================

function sourceUserChange(event)
{
	view.clearGroupList();
	
	var userId = $F('source.user');
	
	if (userId != '')
		model.getUserGroups(userId, ker.wrap(view, view.addGroupRows));
}

//=====================================================================================
}
