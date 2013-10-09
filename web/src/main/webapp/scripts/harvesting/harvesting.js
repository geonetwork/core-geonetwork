//=====================================================================================
//===
//=== Main harvesting class
//===
//=====================================================================================

ker.include('harvesting/harvester.js');
ker.include('harvesting/harvester-model.js');
ker.include('harvesting/harvester-view.js');
ker.include('harvesting/geonet/geonetwork.js');
ker.include('harvesting/geonet20/geonetwork20.js');
ker.include('harvesting/geoPREST/geoPREST.js');
ker.include('harvesting/webdav/webdav.js');
ker.include('harvesting/csw/csw.js');
ker.include('harvesting/ogcwxs/ogcwxs.js');
ker.include('harvesting/thredds/thredds.js');
ker.include('harvesting/wfsfeatures/wfsfeatures.js');
ker.include('harvesting/z3950/z3950.js');
ker.include('harvesting/z3950Config/z3950Config.js');
ker.include('harvesting/oaipmh/oaipmh.js');
ker.include('harvesting/arcsde/arcsde.js');
ker.include('harvesting/filesystem/filesystem.js');
ker.include('harvesting/model.js');
ker.include('harvesting/view.js');
ker.include('harvesting/util.js');

var harvesting = null;

//=====================================================================================

function init()
{
	harvesting = new Harvesting();
	
	//--- waits for all files to be loaded
	ker.loadMan.wait(harvesting);
}

//=====================================================================================

function Harvesting() 
{
	var loader = new XMLLoader(Env.locUrl +'/xml/harvesting.xml');
	var model  = new Model(loader);
	var view   = new View(loader);

	//--- create subsystems and register them

	var geonet   = new Geonetwork(loader);
	var geonet20 = new Geonetwork20(loader);
	var geoPREST = new GeoPREST(loader);
	var webdav   = new WebDav(loader);
	var csw      = new Csw(loader);
	var z3950    = new Z3950(loader);
	var oaipmh   = new OaiPmh(loader);
	var ogcwxs   = new OgcWxs(loader);
	var arcsde   = new Arcsde(loader);
	var thredds  = new Thredds(loader);
	var wfsfeatures = new WfsFeatures(loader);
	var filesystem   = new Filesystem(loader);
	var z3950Config   = new Z3950Config(loader);
	
	//--- public objects

	this.geonet   = geonet;
	this.geonet20 = geonet20;
	this.geoPREST = geoPREST;
	this.webdav   = webdav;
	this.csw      = csw;
	this.z3950    = z3950;
	this.z3950Config = z3950Config;
	this.oaipmh   = oaipmh;
	this.ogcwxs   = ogcwxs;
	this.thredds  = thredds;
	this.wfsfeatures  = wfsfeatures;
	this.arcsde	  = arcsde;
	this.filesystem  = filesystem;
	
	//--- public methods

	this.init    = init;
	this.refresh = refresh;
	this.remove  = remove;
	this.start   = start;
	this.stop    = stop;
	this.run     = run;
	this.edit    = edit;
	this.update  = update;
	this.show    = show;
	this.newNode = newNode;
	this.history = history;
	this.clone   = clone;
	this.cloneCB = cloneCB;
	this.clear      = clear;
    this.updateMail = updateMail;
    this.addMail    = addMail;
    this.removeMail = removeMail;
    this.refreshNotif = refreshNotif;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	view.register(geonet);
	view.register(geonet20);
	view.register(geoPREST);
	view.register(webdav);
	view.register(csw);
	view.register(ogcwxs);
	view.register(thredds);
	view.register(z3950);
	view.register(oaipmh);
	view.register(wfsfeatures);
	view.register(arcsde);
	view.register(filesystem);
	view.register(z3950Config);
	view.show(SHOW.LIST);
	
	refresh();
}

//=====================================================================================

function refresh()
{
    view.removeAll();
    model.getNodes(ker.wrap(this, refresh_OK));
}

//=====================================================================================

function refreshNotif()
{
    
}

//-------------------------------------------------------------------------------------

function refresh_OK(nodes)
{
	var entries = xml.children(nodes);
	
	//--- add all harvesting entries to list

	for (var i=0; i<entries.length; i++)
		view.append(entries[i]);
}

//=====================================================================================

function remove()
{
	var idList = view.getIdList();

	if (idList.length == 0)
		alert(loader.getText('pleaseSelect'));
	else
	{
		if (confirm(loader.getText('confirmRemove')) == false)
			return;
		
		model.removeNodes(idList, ker.wrap(this, remove_OK));
	}
}

//-------------------------------------------------------------------------------------

function remove_OK(idList)
{
	for (var i=0; i<idList.length; i++)
	{
		//--- if the status is not ok we ignore it. Maybe the entry has been already 
		//--- removed or maybe the id is simply wrong. In this case a simple refresh 
		//--- should be enough.
		
		view.remove(idList[i].ID);
	}
}

//=====================================================================================

function start()
{
	var idList = view.getIdList();

	if (idList.length == 0)
		alert(loader.getText('pleaseSelect'));
	else
		model.startNodes(idList, ker.wrap(this, start_OK));
}
	
//-------------------------------------------------------------------------------------

function start_OK(idList)
{
	for (var i=0; i<idList.length; i++)
	{
		var id     = idList[i].ID;
		var status = idList[i].STATUS;

		if (status == 'ok' || status=='already-active')
		{
			view.unselect(id);
			view.setStarted(id);
		}
	}
}

//=====================================================================================

function stop()
{
	var idList = view.getIdList();
	
	if (idList.length == 0)
		alert(loader.getText('pleaseSelect'));
	else
		model.stopNodes(idList, ker.wrap(this, stop_OK));
}

//-------------------------------------------------------------------------------------

function stop_OK(idList)
{
	for (var i=0; i<idList.length; i++)
	{
		var id     = idList[i].ID;
		var status = idList[i].STATUS;

		if (status == 'ok' || status=='already-inactive')
		{
			view.unselect(id);
			view.setStopped(id);
		}
	}
}

//=====================================================================================

function run()
{
	var idList = view.getIdList();
	
	if (idList.length == 0)
		alert(loader.getText('pleaseSelect'));
	else
		model.runNodes(idList, ker.wrap(this, run_OK));
}

//-------------------------------------------------------------------------------------

function run_OK(idList)
{
	for (var i=0; i<idList.length; i++)
	{
		var id     = idList[i].ID;
		var status = idList[i].STATUS;

		if (status == 'ok' || status=='already-running')
		{
			view.unselect(id);
			view.setRunning(id);
		}
	}
}

//=====================================================================================

function clone(id)
{
	var idList = view.getIdList();
	
	if (idList.length == 0) {
		alert(loader.getText('pleaseSelect'));
		return;
	}
	if (idList.length  > 1) {
		alert(loader.getText('onlyOnePlease'));
		return;
	}

	model.cloneNode(idList, ker.wrap(this, cloneCB));
}

//=====================================================================================

function cloneCB(node)
{
	var id = view.append(node);
	harvesting.edit(id);
}

//=====================================================================================

function edit(id)
{
	model.getNode(id, ker.wrap(view, view.edit));
}

//=====================================================================================

function history(url)
{
  var loc = window.location.href;
  if (loc.indexOf("modal") != -1) {
    url += "&modal";
  }
  load(url);
}

//=====================================================================================

function clear(id)
{
    var idList = view.getIdList();

    if (idList.length == 0)
        alert(loader.getText('pleaseSelect'));
    else
    {
        if (confirm(loader.getText('confirmClear')) != false) {
            var m = Ext.get("messages");
            Ext.each(idList, function(id) {
                var tmp = Ext.get("clear-" + id);
                if(tmp) {
                    tmp.remove();
                }
                
                var message = "<div id='clear-" + id + 
                        "' style='width=100%'>" + 
                        loader.getText('clearRequest') + id + "<img src='" +Env.url + "/images/spin.gif'></div>";
                Ext.DomHelper.insertFirst(m, message);
                
                Ext.Ajax.request({
                    url: Env.url +'/srv/eng/xml.harvesting.clear',
                    params: {id: id},
                    success: function(a, b) {
                        var m = Ext.get("clear-" + id);
                        var message = loader.getText('clearResponse') + id;
                        
                        m.update(message);
                        
                        window.setTimeout(function() {
                            Ext.get("clear-" + id).remove();
                        }, 20000);
                    }
                });
            });
        }
    }
   
}

//=====================================================================================

function update()
{
	var request = view.getUpdateRequest();

	if (request == null)
		return;
			
	//--- send add/update request
	
	if (view.isAdding())	
		model.addNode(request, ker.wrap(this, update_OK));
	else
		model.updateNode(request, ker.wrap(this, update_OK));
}

//=====================================================================================

function addMail()
{
    var emails = Ext.get("emails_").getValue().split(",");
    var select = Ext.get("emails");
    Ext.each(emails, function(email) {
        email = email.trim();
        if(email != '') {
            select.dom.options.add(new Option(email, email));
        }
    });
    Ext.get("emails_").dom.value = "";
}

//=====================================================================================

function removeMail()
{
    var select = Ext.get("emails");
    var toRemove = [];
    Ext.each(select.dom.options, function(option) {
        if(option.selected) {
            toRemove.push(option);
        }
    });
    
    Ext.each(toRemove, function(a) {
        a.remove();
    });
}

//=====================================================================================

function updateMail()
{
    
  addMail();
  var e = [];
  Ext.each(Ext.get("emails").dom.options, function(opt) {
      if(opt.value != '') {
          e.push(opt.value);
      }
      });
  Ext.Ajax.request({
      url: Env.url +'/srv/eng/harvesting.notifier.save',
      params: {
              recipient: e,
              template: Ext.get("template").getValue(),
              templateError: Ext.get("templateError").getValue(),
              templateWarning: Ext.get("templateWarning").getValue(),
              enabled: Ext.get("enableMail").dom.checked,
              level1: Ext.get("level1").dom.checked,
              level2: Ext.get("level2").dom.checked,
              level3: Ext.get("level3").dom.checked,
              subject: Ext.get("subject").getValue()
          },
      success: function(a, b) {
          view.show(SHOW.LIST);
      }
  });
}

//-------------------------------------------------------------------------------------

function update_OK(node)
{
	if (view.isAdding())		view.append(node);
		else						view.refresh(node);

	view.show(SHOW.LIST);
}

//=====================================================================================

function show(panel)
{
	view.show(panel);
}

//=====================================================================================

function newNode()
{
	view.newNode();
}

//=====================================================================================
}
