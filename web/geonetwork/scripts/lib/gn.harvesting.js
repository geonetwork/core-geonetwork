ker.include('harvesting/harvester.js');ker.include('harvesting/harvester-model.js');ker.include('harvesting/harvester-view.js');ker.include('harvesting/geonet/geonetwork.js');ker.include('harvesting/geonet20/geonetwork20.js');ker.include('harvesting/webdav/webdav.js');ker.include('harvesting/csw/csw.js');ker.include('harvesting/ogcwxs/ogcwxs.js');ker.include('harvesting/oaipmh/oaipmh.js');ker.include('harvesting/arcsde/arcsde.js');ker.include('harvesting/filesystem/filesystem.js');ker.include('harvesting/model.js');ker.include('harvesting/view.js');ker.include('harvesting/util.js');var harvesting=null;function init()
{harvesting=new Harvesting();ker.loadMan.wait(harvesting);}
function Harvesting()
{var loader=new XMLLoader(Env.locUrl+'/xml/harvesting.xml');var model=new Model(loader);var view=new View(loader);var geonet=new Geonetwork(loader);var geonet20=new Geonetwork20(loader);var webdav=new WebDav(loader);var csw=new Csw(loader);var oaipmh=new OaiPmh(loader);var ogcwxs=new OgcWxs(loader);var arcsde=new Arcsde(loader);var filesystem=new Filesystem(loader);this.geonet=geonet;this.geonet20=geonet20;this.webdav=webdav;this.csw=csw;this.oaipmh=oaipmh;this.ogcwxs=ogcwxs;this.arcsde=arcsde;this.filesystem=filesystem;this.init=init;this.refresh=refresh;this.remove=remove;this.start=start;this.stop=stop;this.run=run;this.edit=edit;this.update=update;this.show=show;this.newNode=newNode;function init()
{view.register(geonet);view.register(geonet20);view.register(webdav);view.register(csw);view.register(ogcwxs);view.register(oaipmh);view.register(arcsde);view.register(filesystem);view.show(SHOW.LIST);refresh();}
function refresh()
{view.removeAll();model.getNodes(ker.wrap(this,refresh_OK));}
function refresh_OK(nodes)
{var entries=xml.children(nodes);for(var i=0;i<entries.length;i++)
view.append(entries[i]);}
function remove()
{var idList=view.getIdList();if(idList.length==0)
alert(loader.getText('pleaseSelect'));else
{if(confirm(loader.getText('confirmRemove'))==false)
return;model.removeNodes(idList,ker.wrap(this,remove_OK));}}
function remove_OK(idList)
{for(var i=0;i<idList.length;i++)
{view.remove(idList[i].ID);}}
function start()
{var idList=view.getIdList();if(idList.length==0)
alert(loader.getText('pleaseSelect'));else
model.startNodes(idList,ker.wrap(this,start_OK));}
function start_OK(idList)
{for(var i=0;i<idList.length;i++)
{var id=idList[i].ID;var status=idList[i].STATUS;if(status=='ok'||status=='already-active')
{view.unselect(id);view.setStarted(id);}}}
function stop()
{var idList=view.getIdList();if(idList.length==0)
alert(loader.getText('pleaseSelect'));else
model.stopNodes(idList,ker.wrap(this,stop_OK));}
function stop_OK(idList)
{for(var i=0;i<idList.length;i++)
{var id=idList[i].ID;var status=idList[i].STATUS;if(status=='ok'||status=='already-inactive')
{view.unselect(id);view.setStopped(id);}}}
function run()
{var idList=view.getIdList();if(idList.length==0)
alert(loader.getText('pleaseSelect'));else
model.runNodes(idList,ker.wrap(this,run_OK));}
function run_OK(idList)
{for(var i=0;i<idList.length;i++)
{var id=idList[i].ID;var status=idList[i].STATUS;if(status=='ok'||status=='already-running')
{view.unselect(id);view.setRunning(id);}}}
function edit(id)
{model.getNode(id,ker.wrap(view,view.edit));}
function update()
{var request=view.getUpdateRequest();if(request==null)
return;if(view.isAdding())
model.addNode(request,ker.wrap(this,update_OK));else
model.updateNode(request,ker.wrap(this,update_OK));}
function update_OK(node)
{if(view.isAdding())view.append(node);else view.refresh(node);view.show(SHOW.LIST);}
function show(panel)
{view.show(panel);}
function newNode()
{view.newNode();}}
function Harvester()
{var currId=null;this.getType=function(){}
this.getLabel=function(){}
this.getEditPanel=function(){}
this.getResultTip=function(){}
this.init=function()
{this.view.init();}
this.setData=function(node)
{currId=node.getAttribute('id');this.view.setData(node);}
this.setEmpty=function()
{currId="";this.view.setEmpty();}
this.getUpdateRequest=function()
{if(!this.view.isDataValid())
return null;var data=this.view.getData();data.ID=currId;data.TYPE=this.getType();return this.model.getUpdateRequest(data);}}
function HarvesterModel()
{this.substituteCommon=function(data,request)
{var list=data.PRIVILEGES;var text='';if(list!=null)
{for(var i=0;i<list.length;i++)
{var groupID=list[i].GROUP;var operList=list[i].OPERATIONS;text+='<group id="'+groupID+'">';for(var j=0;j<operList.length;j++)
text+='<operation name="'+operList[j]+'"/>';text+='</group>';}
request=str.replace(request,'{PRIVIL_LIST}',text);}
text='';list=data.CATEGORIES;if(list!=null)
{for(var i=0;i<list.length;i++)
text+='<category id="'+list[i].ID+'"/>';request=str.replace(request,'{CATEG_LIST}',text);}
return request;}}
function HarvesterView()
{var prefix='???';var privilTransf=null;var resultTransf=null;this.setPrefix=function(p)
{prefix=p;}
this.setPrivilTransf=function(transf)
{privilTransf=transf;}
this.setResultTransf=function(transf)
{resultTransf=transf;}
this.getResultTip=function(node)
{return resultTransf.transformToText(node);}
this.setEmptyCommon=function()
{$(prefix+'.name').value='';if($(prefix+'.useAccount')){$(prefix+'.useAccount').checked=true;$(prefix+'.username').value='';$(prefix+'.password').value='';}
$(prefix+'.oneRunOnly').checked=false;$(prefix+'.every.days').value='0';$(prefix+'.every.hours').value='1';$(prefix+'.every.mins').value='30';this.removeAllGroupRows();this.unselectCategories();}
this.setDataCommon=function(node)
{var site=node.getElementsByTagName('site')[0];var options=node.getElementsByTagName('options')[0];hvutil.setOption(site,'name',prefix+'.name');hvutil.setOptionIfExists(site,'use',prefix+'.useAccount');hvutil.setOptionIfExists(site,'username',prefix+'.username');hvutil.setOptionIfExists(site,'password',prefix+'.password');hvutil.setOption(options,'oneRunOnly',prefix+'.oneRunOnly');var every=new Every(hvutil.find(options,'every'));$(prefix+'.every.days').value=every.days;$(prefix+'.every.hours').value=every.hours;$(prefix+'.every.mins').value=every.mins;}
this.getDataCommon=function()
{var days=$F(prefix+'.every.days');var hours=$F(prefix+'.every.hours');var mins=$F(prefix+'.every.mins');var data;if($(prefix+'.useAccount')){data={NAME:$F(prefix+'.name'),USE_ACCOUNT:$(prefix+'.useAccount').checked,USERNAME:$F(prefix+'.username'),PASSWORD:$F(prefix+'.password'),EVERY:Every.build(days,hours,mins),ONE_RUN_ONLY:$(prefix+'.oneRunOnly').checked}}
else{data={NAME:$F(prefix+'.name'),EVERY:Every.build(days,hours,mins),ONE_RUN_ONLY:$(prefix+'.oneRunOnly').checked}}
return data;}
this.isDataValidCommon=function()
{var days=$F(prefix+'.every.days');var hours=$F(prefix+'.every.hours');var mins=$F(prefix+'.every.mins');if(Every.build(days,hours,mins)==0)
{alert(loader.getText('everyZero'));return false;}
return true;}
this.clearGroups=function()
{$(prefix+'.groups').options.length=0;}
this.addGroup=function(id,label)
{gui.addToSelect(prefix+'.groups',id,label);}
this.getSelectedGroups=function()
{var ctrl=$(prefix+'.groups');var result=[];for(var i=0;i<ctrl.options.length;i++)
if(ctrl.options[i].selected)
result.push(ctrl.options[i]);return result;}
this.addEmptyGroupRows=function(groups)
{for(var i=0;i<groups.length;i++)
{var option=groups[i];var doc=Sarissa.getDomDocument();var group=doc.createElement('group');var groupId=option.value;group.setAttribute('id',groupId);this.addGroupRow(group);}}
this.addGroupRow=function(group)
{var id=group.getAttribute('id');var name='???';var ctrl=$(prefix+'.groups');var list=$(prefix+'.privileges').getElementsByTagName('TR');for(var i=1;i<list.length;i++)
{var groupID=list[i].getAttribute('id').split('.')[2];if(id==groupID)
return;}
for(var i=0;i<ctrl.options.length;i++)
if(ctrl.options[i].value==id)
{name=xml.textContent(ctrl.options[i]);break;}
group.setAttribute('name',name);var xslRes=privilTransf.transform(group);gui.appendTableRow(prefix+'.privileges',xslRes);}
this.removeGroupRow=function(groupId)
{Element.remove(groupId);}
this.removeAllGroupRows=function()
{gui.removeAllButFirst(prefix+'.privileges');}
this.addGroupRows=function(node)
{var privil=node.getElementsByTagName('privileges');if(privil.length==0)
return;var list=privil[0].getElementsByTagName('group');for(var i=0;i<list.length;i++)
this.addGroupRow(list[i]);}
this.getPrivileges=function()
{var data=[];var list=$(prefix+'.privileges').getElementsByTagName('TR');for(var i=1;i<list.length;i++)
{var trElem=list[i];var inputList=trElem.getElementsByTagName('INPUT');var groupData=[];var groupID=trElem.getAttribute('id').split('.')[2];for(var j=0;j<inputList.length;j++)
if(inputList[j].checked)
groupData.push(inputList[j].name);if(groupData.length!=0)
data.push({GROUP:groupID,OPERATIONS:groupData});}
return data;}
this.clearCategories=function()
{$(prefix+'.categories').options.length=0;}
this.addCategory=function(id,label)
{gui.addToSelect(prefix+'.categories',id,label);}
this.getSelectedCategories=function()
{var ctrl=$(prefix+'.categories');var result=[];for(var i=0;i<ctrl.options.length;i++)
if(ctrl.options[i].selected)
result.push({ID:ctrl.options[i].value});return result;}
this.unselectCategories=function()
{var ctrl=$(prefix+'.categories');for(var i=0;i<ctrl.options.length;i++)
ctrl.options[i].selected=false;}
this.selectCategories=function(node)
{var categs=node.getElementsByTagName('categories');if(categs.length==0)
return;var list=categs[0].getElementsByTagName('category');for(var i=0;i<list.length;i++)
selectCategory(list[i]);}
function selectCategory(categ)
{var id=categ.getAttribute('id');var ctrl=$(prefix+'.categories');for(var i=0;i<ctrl.options.length;i++)
if(ctrl.options[i].value==id)
{ctrl.options[i].selected=true;return;}}}
function Model(xmlLoader)
{var loader=xmlLoader;this.getNodes=getNodes;this.getNode=getNode;this.removeNodes=removeNodes;this.startNodes=startNodes;this.stopNodes=stopNodes;this.runNodes=runNodes;this.addNode=addNode;this.updateNode=updateNode;function getNodes(callBack)
{this.getNodesCB=callBack;ker.send('xml.harvesting.get','<request/>',ker.wrap(this,getNodes_OK));}
function getNodes_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotGet'),xmlRes);else
this.getNodesCB(xmlRes);}
function getNode(id,callBack)
{var req=ker.createRequest('id',id);this.getNodeCB=callBack;ker.send('xml.harvesting.get',req,ker.wrap(this,getNode_OK));}
function getNode_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotGet'),xmlRes);else
this.getNodeCB(xmlRes);}
function removeNodes(idList,callBack)
{var request=ker.createRequest('id',idList);this.removeNodesCB=callBack;ker.send('xml.harvesting.remove',request,ker.wrap(this,removeNodes_OK));}
function removeNodes_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRemove'),xmlRes);else
this.removeNodesCB(buildIdList(xmlRes));}
function startNodes(idList,callBack)
{var request=ker.createRequest('id',idList);this.startNodesCB=callBack;ker.send('xml.harvesting.start',request,ker.wrap(this,startNodes_OK));}
function startNodes_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotStart'),xmlRes);else
this.startNodesCB(buildIdList(xmlRes));}
function stopNodes(idList,callBack)
{var request=ker.createRequest('id',idList);this.stopNodesCB=callBack;ker.send('xml.harvesting.stop',request,ker.wrap(this,stopNodes_OK));}
function stopNodes_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotStop'),xmlRes);else
this.stopNodesCB(buildIdList(xmlRes));}
function runNodes(idList,callBack)
{var request=ker.createRequest('id',idList);this.runNodesCB=callBack;ker.send('xml.harvesting.run',request,ker.wrap(this,runNodes_OK));}
function runNodes_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRun'),xmlRes);else
this.runNodesCB(buildIdList(xmlRes));}
function addNode(request,callBack)
{this.addNodeCB=callBack;ker.send('xml.harvesting.add',request,ker.wrap(this,addNode_OK));}
function addNode_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotAdd'),xmlRes);else
this.addNodeCB(xmlRes);}
function updateNode(request,callBack)
{this.updateNodeCB=callBack;ker.send('xml.harvesting.update',request,ker.wrap(this,updateNode_OK));}
function updateNode_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotUpdate'),xmlRes);else
this.updateNodeCB(xmlRes);}
function buildIdList(xmlRes)
{var ids=xmlRes.getElementsByTagName('id');var res=[];for(var i=0;i<ids.length;i++)
{var id=ids[i].firstChild.nodeValue;var status=ids[i].getAttribute('status');res.push({ID:id,STATUS:status});}
return res;}}
var hvutil=new Object();hvutil.setOption=function(node,name,ctrlId)
{var value=hvutil.find(node,name);var ctrl=$(ctrlId);var type=ctrl.getAttribute('type');if(value==null)
throw'Cannot find node with name : '+name;if(!ctrl)
throw'Cannot find control with id : '+ctrlId;if(type=='checkbox')ctrl.checked=(value=='true');else ctrl.value=value;}
hvutil.setOptionIfExists=function(node,name,ctrlId)
{var value=hvutil.find(node,name);var ctrl=$(ctrlId);if(!ctrl)return;var type=ctrl.getAttribute('type');if(value==null)
return;if(type=='checkbox')ctrl.checked=(value=='true');else ctrl.value=value;}
hvutil.find=function(node,name)
{var array=[node];while(array.length!=0)
{node=array.shift();if(node.nodeName==name)
return xml.textContent(node);node=node.firstChild;while(node!=null)
{if(node.nodeType==Node.ELEMENT_NODE)
array.push(node);node=node.nextSibling;}}
return null;}
function Every(every)
{if(typeof every=='string')
every=parseInt(every);this.mins=every%60;every-=this.mins;this.hours=every/60%24;this.days=(every-this.hours*60)/1440;}
Every.build=function(days,hours,mins)
{if(typeof days=='string')
days=parseInt(days);if(typeof hours=='string')
hours=parseInt(hours);if(typeof mins=='string')
mins=parseInt(mins);return days*1440+hours*60+mins;}
var SHOW=new Object();SHOW.LIST=new Object();SHOW.ADD=new Object();SHOW.EDIT=new Object();function View(xmlLoader)
{var rowTransf=new XSLTransformer('harvesting/client-node-row.xsl',xmlLoader);var errTransf=new XSLTransformer('harvesting/client-error-tip.xsl',xmlLoader);var panelSwitcher=new TabSwitcher(['listPanel','addPanel','editPanel'],['listButtons','addButtons','editButtons']);var editSwitcher=new TabSwitcher([]);var loader=xmlLoader;var addingFlag=false;var harvesters={};var currHarv=null;this.register=register;this.show=show;this.isAdding=isAdding;this.unselect=unselect;this.setStarted=setStarted
this.setStopped=setStopped;this.setRunning=setRunning;this.getIdList=getIdList;this.remove=remove;this.removeAll=removeAll;this.refresh=refresh;this.append=append;this.newNode=newNode;this.edit=edit;this.getUpdateRequest=getUpdateRequest;function register(harvester)
{var type=harvester.getType();var label=harvester.getLabel();var panel=harvester.getEditPanel();harvesters[type]=harvester;editSwitcher.add(panel);harvester.init();gui.addToSelect('add.type',type,label);}
function show(obj)
{if(obj==SHOW.LIST)
panelSwitcher.show('listPanel','listButtons');else if(obj==SHOW.ADD)
panelSwitcher.show('addPanel','addButtons');else if(obj==SHOW.EDIT)
panelSwitcher.show('editPanel','editButtons');else
throw'Unknown object to show : '+obj;}
function isAdding(){return addingFlag;}
function unselect(id)
{$(id).getElementsByTagName('input')[0].checked=false;}
function setStarted(id)
{var img=xml.getElementById($(id),'status');img.setAttribute('src',Env.url+'/images/clock.png');}
function setStopped(id)
{var img=xml.getElementById($(id),'status');img.setAttribute('src',Env.url+'/images/fileclose.png');}
function setRunning(id)
{var img=xml.getElementById($(id),'status');img.setAttribute('src',Env.url+'/images/exec.png');}
function getIdList()
{var rows=$('table').getElementsByTagName('tr');var idList=new Array();for(var i=1;i<rows.length;i++)
{var inputs=rows[i].getElementsByTagName('input');if(inputs[0].checked)
idList.push(rows[i].id);}
return idList;}
function remove(id)
{Element.remove(id);}
function removeAll()
{gui.removeAllButFirst('table');}
function refresh(node)
{var id=node.getAttribute('id');var xslRes=rowTransf.transform(node);gui.replaceTableRow(id,xslRes);setStatusTip(node);setErrorTip(node);}
function append(node)
{var xslRes=rowTransf.transform(node);gui.appendTableRow('table',xslRes);setStatusTip(node);setErrorTip(node);}
function setStatusTip(node)
{var id=node.getAttribute('id');var code=getStatusCode(node);var img=xml.getElementById($(id),'status');var tip=loader.eval('statusTip/'+code);new Tooltip(img,tip);}
function getStatusCode(node)
{var status=xml.evalXPath(node,'options/status');var running=xml.evalXPath(node,'info/running');if(status=='inactive')
return status;if(running=='true')
return'running';return status;}
function setErrorTip(node)
{var error=node.getElementsByTagName('error')[0];var tip=null;if(xml.children(error).length==0)
{var type=node.getAttribute('type');var harv=harvesters[type];if(harv==null)alert('Harvesting module not found!');else tip=harv.getResultTip(node);}
else
{tip=errTransf.transformToText(node);}
var id=node.getAttribute('id');var img=xml.getElementById($(id),'error');new Tooltip(img,tip);}
function newNode()
{addingFlag=true;var type=$('add.type').value;var harv=harvesters[type];if(harv==null)
alert('Harvesting module not found!');else
{harv.setEmpty();editSwitcher.show(harv.getEditPanel());currHarv=harv;show(SHOW.EDIT);}}
function edit(node)
{addingFlag=false;var type=node.getAttribute('type');var harv=harvesters[type];if(harv==null)
alert('Harvesting module not found!');else
{harv.setData(node);editSwitcher.show(harv.getEditPanel());currHarv=harv;show(SHOW.EDIT);}}
function getUpdateRequest()
{return currHarv.getUpdateRequest();}}
arcsde.View=function(xmlLoader)
{HarvesterView.call(this);var privilTransf=new XSLTransformer('harvesting/arcsde/client-privil-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/arcsde/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);this.setPrefix('arcsde');this.setPrivilTransf(privilTransf);this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.clearIcons=clearIcons;this.addIcon=addIcon;this.removeAllGroupRows=function(){}
this.unselectCategories=function(){};Event.observe('arcsde.icon','change',ker.wrap(this,updateIcon));function init()
{valid.add([{id:'arcsde.name',type:'length',minSize:1,maxSize:200},{id:'arcsde.server',type:'length',minSize:1,maxSize:500},{id:'arcsde.port',type:'integer',minSize:1,maxSize:10},{id:'arcsde.username',type:'length',minSize:1,maxSize:500},{id:'arcsde.password',type:'length',minSize:1,maxSize:500},{id:'arcsde.database',type:'length',minSize:1,maxSize:500},{id:'arcsde.every.days',type:'integer',minValue:0,maxValue:99},{id:'arcsde.every.hours',type:'integer',minValue:0,maxValue:23},{id:'arcsde.every.mins',type:'integer',minValue:0,maxValue:59}]);}
function setEmpty()
{this.setEmptyCommon();var icons=$('arcsde.icon').options;for(var i=0;i<icons.length;i++)
if(icons[i].value=='default.gif')
{icons[i].selected=true;break;}
updateIcon();}
function setData(node)
{this.setDataCommon(node);var name=node.getElementsByTagName('name')[0];var server=node.getElementsByTagName('server')[0];var port=node.getElementsByTagName('port')[0];var username=node.getElementsByTagName('username')[0];var password=node.getElementsByTagName('password')[0];var database=node.getElementsByTagName('database')[0];hvutil.setOption(node,'icon','arcsde.icon');hvutil.setOption(node,'server','arcsde.server');hvutil.setOption(node,'port','arcsde.port');hvutil.setOption(node,'username','arcsde.username');hvutil.setOption(node,'password','arcsde.password');hvutil.setOption(node,'database','arcsde.database');this.removeAllGroupRows();this.addGroupRows(node);this.unselectCategories();this.selectCategories(node);updateIcon();}
function getData()
{var data=this.getDataCommon();data.SERVER=$F('arcsde.server');data.PORT=$F('arcsde.port');data.USERNAME=$F('arcsde.username');data.PASSWORD=$F('arcsde.password');data.DATABASE=$F('arcsde.database');data.ICON=$F('arcsde.icon');data.PRIVILEGES=this.getPrivileges();data.CATEGORIES=this.getSelectedCategories();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function clearIcons()
{$('arcsde.icon').options.length=0;}
function addIcon(file)
{gui.addToSelect('arcsde.icon',file,file);}
function updateIcon()
{var icon=$F('arcsde.icon');var image=$('arcsde.icon.image');image.setAttribute('src',Env.url+'/images/harvesting/'+icon);}}
wd.View=function(xmlLoader)
{HarvesterView.call(this);var privilTransf=new XSLTransformer('harvesting/webdav/client-privil-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/webdav/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;this.setPrefix('wd');this.setPrivilTransf(privilTransf);this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.clearIcons=clearIcons;this.addIcon=addIcon;Event.observe('wd.icon','change',ker.wrap(this,updateIcon));function init()
{valid.add([{id:'wd.name',type:'length',minSize:1,maxSize:200},{id:'wd.url',type:'length',minSize:1,maxSize:200},{id:'wd.url',type:'url'},{id:'wd.username',type:'length',minSize:0,maxSize:200},{id:'wd.password',type:'length',minSize:0,maxSize:200},{id:'wd.every.days',type:'integer',minValue:0,maxValue:99},{id:'wd.every.hours',type:'integer',minValue:0,maxValue:23},{id:'wd.every.mins',type:'integer',minValue:0,maxValue:59}]);shower=new Shower('wd.useAccount','wd.account');}
function setEmpty()
{this.setEmptyCommon();$('wd.url').value='';$('wd.recurse').checked=false;$('wd.validate').checked=false;var icons=$('wd.icon').options;for(var i=0;i<icons.length;i++)
if(icons[i].value=='default.gif')
{icons[i].selected=true;break;}
shower.update();updateIcon();}
function setData(node)
{this.setDataCommon(node);var site=node.getElementsByTagName('site')[0];var options=node.getElementsByTagName('options')[0];hvutil.setOption(site,'url','wd.url');hvutil.setOption(site,'icon','wd.icon');hvutil.setOption(options,'validate','wd.validate');hvutil.setOption(options,'recurse','wd.recurse');this.removeAllGroupRows();this.addGroupRows(node);this.unselectCategories();this.selectCategories(node);shower.update();updateIcon();}
function getData()
{var data=this.getDataCommon();data.URL=$F('wd.url');data.ICON=$F('wd.icon');data.VALIDATE=$('wd.validate').checked;data.RECURSE=$('wd.recurse').checked;data.PRIVILEGES=this.getPrivileges();data.CATEGORIES=this.getSelectedCategories();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function clearIcons()
{$('wd.icon').options.length=0;}
function addIcon(file)
{gui.addToSelect('wd.icon',file,file);}
function updateIcon()
{var icon=$F('wd.icon');var image=$('wd.icon.image');image.setAttribute('src',Env.url+'/images/harvesting/'+icon);}}
ker.include('harvesting/z3950/model.js');ker.include('harvesting/z3950/view.js');var z3950=new Object();function Z3950(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new z3950.Model(loader);var view=new z3950.View(loader);this.addGroupRow=addGroupRow;this.removeGroupRow=view.removeGroupRow;this.getResultTip=view.getResultTip;this.model=model;this.view=view;this.getType=function(){return"z3950";}
this.getLabel=function(){return loader.eval("info[@type='z3950']/long");}
this.getEditPanel=function(){return"z39.editPanel";}
this.init=function()
{this.view.init();model.retrieveGroups(ker.wrap(this,init_groups_OK));model.retrieveCategories(ker.wrap(this,init_categ_OK));model.retrieveIcons(ker.wrap(this,init_icons_OK));}
function init_groups_OK(data)
{view.clearGroups();for(var i=0;i<data.length;i++)
view.addGroup(data[i].id,data[i].label[Env.lang]);}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++)
view.addCategory(data[i].id,data[i].label[Env.lang]);}
function init_icons_OK(data)
{view.clearIcons();for(var i=0;i<data.length;i++)
view.addIcon(data[i]);}
function addGroupRow()
{var groups=view.getSelectedGroups();if(groups.length==0)alert(loader.getText('pleaseSelectGroup'));else view.addEmptyGroupRows(groups);}}
ker.include('harvesting/csw/model.js');ker.include('harvesting/csw/view.js');var csw=new Object();function Csw(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new csw.Model(loader);var view=new csw.View(loader);this.addSearchRow=view.addEmptySearch;this.removeSearchRow=view.removeSearch;this.addGroupRow=addGroupRow;this.removeGroupRow=view.removeGroupRow;this.getResultTip=view.getResultTip;this.model=model;this.view=view;this.getType=function(){return"csw";}
this.getLabel=function(){return loader.eval("info[@type='csw']/long");}
this.getEditPanel=function(){return"csw.editPanel";}
this.init=function()
{this.view.init();model.retrieveGroups(ker.wrap(this,init_groups_OK));model.retrieveCategories(ker.wrap(this,init_categ_OK));model.retrieveIcons(ker.wrap(this,init_icons_OK));}
function init_groups_OK(data)
{view.clearGroups();for(var i=0;i<data.length;i++)
view.addGroup(data[i].id,data[i].label[Env.lang]);}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++)
view.addCategory(data[i].id,data[i].label[Env.lang]);}
function init_icons_OK(data)
{view.clearIcons();for(var i=0;i<data.length;i++)
view.addIcon(data[i]);}
function addGroupRow()
{var groups=view.getSelectedGroups();if(groups.length==0)alert(loader.getText('pleaseSelectGroup'));else view.addEmptyGroupRows(groups);}}
z3950.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;var callBackF=null;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.retrieveIcons=retrieveIcons;this.getUpdateRequest=getUpdateRequest;function retrieveGroups(callBack)
{new InfoService(loader,'groups',callBack);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function retrieveIcons(callBack)
{callBackF=callBack;var request=ker.createRequest('type','icons');ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveIcons_OK));}
function retrieveIcons_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var data=[];var list=xml.children(xml.children(xmlRes)[0]);for(var i=0;i<list.length;i++)
data.push(xml.textContent(list[i]));callBackF(data);}}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <icon>{ICON}</icon>'+'      <account>'+'        <use>{USE_ACCOUNT}</use>'+'        <username>{USERNAME}</username>'+'        <password>{PASSWORD}</password>'+'      </account>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'    </options>'+'    <privileges>'+'       {PRIVIL_LIST}'+'    </privileges>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';}
ogcwxs.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;var callBackF=null;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.retrieveIcons=retrieveIcons;this.getUpdateRequest=getUpdateRequest;function retrieveGroups(callBack)
{new InfoService(loader,'groups',callBack);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function retrieveIcons(callBack)
{callBackF=callBack;var request=ker.createRequest('type','icons');ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveIcons_OK));}
function retrieveIcons_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var data=[];var list=xml.children(xml.children(xmlRes)[0]);for(var i=0;i<list.length;i++)
data.push(xml.textContent(list[i]));callBackF(data);}}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <ogctype>{OGCTYPE}</ogctype>'+'      <url>{CAPAB_URL}</url>'+'      <icon>{ICON}</icon>'+'      <account>'+'        <use>{USE_ACCOUNT}</use>'+'        <username>{USERNAME}</username>'+'        <password>{PASSWORD}</password>'+'      </account>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'      <lang>{LANG}</lang>'+'      <topic>{TOPIC}</topic>'+'      <createThumbnails>{CREATETHUMBNAILS}</createThumbnails>'+'      <useLayer>{USELAYER}</useLayer>'+'      <useLayerMd>{USELAYERMD}</useLayerMd>'+'      <datasetCategory>{DATASETCATEGORY}</datasetCategory>'+'    </options>'+'    <privileges>'+'       {PRIVIL_LIST}'+'    </privileges>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';}
gn20.View=function(xmlLoader)
{HarvesterView.call(this);var searchTransf=new XSLTransformer('harvesting/geonet20/client-search-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/geonet20/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;this.setPrefix('gn20');this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.getSiteId=getSiteId;this.clearSiteId=clearSiteId;this.addEmptySearch=addEmptySearch;this.addSearch=addSearch;this.removeSearch=removeSearch;this.removeAllSearch=removeAllSearch;this.removeAllGroupRows=function(){}
this.unselectCategories=function(){};function init()
{valid.add([{id:'gn20.name',type:'length',minSize:1,maxSize:200},{id:'gn20.host',type:'length',minSize:1,maxSize:200},{id:'gn20.host',type:'hostname'},{id:'gn20.port',type:'integer',minValue:80,maxValue:65535,empty:true},{id:'gn20.servlet',type:'length',minSize:1,maxSize:200},{id:'gn20.username',type:'length',minSize:0,maxSize:200},{id:'gn20.password',type:'length',minSize:0,maxSize:200},{id:'gn20.every.days',type:'integer',minValue:0,maxValue:99},{id:'gn20.every.hours',type:'integer',minValue:0,maxValue:23},{id:'gn20.every.mins',type:'integer',minValue:0,maxValue:59}]);shower=new Shower('gn20.useAccount','gn20.account');}
function setEmpty()
{this.setEmptyCommon();removeAllSearch();$('gn20.host').value='';$('gn20.port').value='';$('gn20.servlet').value='';clearSiteId();shower.update();}
function setData(node)
{this.setDataCommon(node);var site=node.getElementsByTagName('site')[0];var searches=node.getElementsByTagName('searches')[0];hvutil.setOption(site,'host','gn20.host');hvutil.setOption(site,'port','gn20.port');hvutil.setOption(site,'servlet','gn20.servlet');removeAllSearch();var list=searches.getElementsByTagName('search');for(var i=0;i<list.length;i++)
addSearch(list[i]);clearSiteId();shower.update();}
function getData()
{var data=this.getDataCommon();data.HOST=$F('gn20.host');data.PORT=$F('gn20.port');data.SERVLET=$F('gn20.servlet');var searchData=[];var searchList=xml.children($('gn20.searches'));for(var i=0;i<searchList.length;i++)
{var divElem=searchList[i];searchData.push({TEXT:xml.getElementById(divElem,'gn20.text').value,TITLE:xml.getElementById(divElem,'gn20.title').value,ABSTRACT:xml.getElementById(divElem,'gn20.abstract').value,KEYWORDS:xml.getElementById(divElem,'gn20.keywords').value,DIGITAL:xml.getElementById(divElem,'gn20.digital').checked,HARDCOPY:xml.getElementById(divElem,'gn20.hardcopy').checked,SITE_ID:divElem.getAttribute('id')});}
data.SEARCH_LIST=searchData;return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function getSiteId()
{return $F('gn20.siteId');}
function clearSiteId()
{$('gn20.siteId').value='';}
function addEmptySearch(siteId)
{var doc=Sarissa.getDomDocument();var xmlSearch=doc.createElement('search');var xmlSiteId=doc.createElement('siteId');doc.appendChild(xmlSearch);xmlSearch.appendChild(xmlSiteId);xmlSiteId.appendChild(doc.createTextNode(siteId));addSearch(xmlSearch);}
function addSearch(xmlSearch)
{var siteId=xml.evalXPath(xmlSearch,'siteId');var html=searchTransf.transformToText(xmlSearch);var div=xml.getElementById($('gn20.searches'),siteId);if(div!=null)
return;new Insertion.Bottom('gn20.searches',html);valid.add([{id:'gn20.text',type:'length',minSize:0,maxSize:200},{id:'gn20.title',type:'length',minSize:0,maxSize:200},{id:'gn20.abstract',type:'length',minSize:0,maxSize:200},{id:'gn20.keywords',type:'length',minSize:0,maxSize:200}],siteId);}
function removeSearch(siteId)
{valid.removeByParent(siteId);Element.remove(siteId);}
function removeAllSearch()
{$('gn20.searches').innerHTML='';valid.removeByParent();}}
ogcwxs.View=function(xmlLoader)
{HarvesterView.call(this);var privilTransf=new XSLTransformer('harvesting/ogcwxs/client-privil-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/ogcwxs/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;var currSearchId=0;this.setPrefix('ogcwxs');this.setPrivilTransf(privilTransf);this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.clearIcons=clearIcons;this.addIcon=addIcon;Event.observe('ogcwxs.icon','change',ker.wrap(this,updateIcon));function init()
{valid.add([{id:'ogcwxs.name',type:'length',minSize:1,maxSize:200},{id:'ogcwxs.capabUrl',type:'length',minSize:1,maxSize:200},{id:'ogcwxs.capabUrl',type:'url'},{id:'ogcwxs.username',type:'length',minSize:0,maxSize:200},{id:'ogcwxs.password',type:'length',minSize:0,maxSize:200},{id:'ogcwxs.every.days',type:'integer',minValue:0,maxValue:99},{id:'ogcwxs.every.hours',type:'integer',minValue:0,maxValue:23},{id:'ogcwxs.every.mins',type:'integer',minValue:0,maxValue:59}]);shower=new Shower('ogcwxs.useAccount','ogcwxs.account');}
function setEmpty()
{this.setEmptyCommon();$('ogcwxs.useLayer').checked=false;$('ogcwxs.useLayerMd').checked=false;$('ogcwxs.createThumbnails').checked=false;$('ogcwxs.ogctype').value='WMS111';$('ogcwxs.lang').value='eng';$('ogcwxs.topic').value='';$('ogcwxs.capabUrl').value='';var icons=$('ogcwxs.icon').options;for(var i=0;i<icons.length;i++)
if(icons[i].value=='default.gif')
{icons[i].selected=true;break;}
shower.update();updateIcon();}
function setData(node)
{this.setDataCommon(node);var site=node.getElementsByTagName('site')[0];var options=node.getElementsByTagName('options')[0];hvutil.setOption(site,'url','ogcwxs.capabUrl');hvutil.setOption(site,'ogctype','ogcwxs.ogctype');hvutil.setOption(site,'icon','ogcwxs.icon');hvutil.setOption(options,'topic','ogcwxs.topic');hvutil.setOption(options,'createThumbnails','ogcwxs.createThumbnails');hvutil.setOption(options,'useLayer','ogcwxs.useLayer');hvutil.setOption(options,'useLayerMd','ogcwxs.useLayerMd');hvutil.setOption(options,'lang','ogcwxs.lang');hvutil.setOption(options,'datasetCategory','ogcwxs.datasetCategory');this.removeAllGroupRows();this.addGroupRows(node);this.unselectCategories();this.selectCategories(node);shower.update();updateIcon();}
function getData()
{var data=this.getDataCommon();data.CAPAB_URL=$F('ogcwxs.capabUrl');data.ICON=$F('ogcwxs.icon');data.OGCTYPE=$F('ogcwxs.ogctype');data.LANG=$F('ogcwxs.lang');data.TOPIC=$F('ogcwxs.topic');data.DATASETCATEGORY=($F('ogcwxs.datasetCategory')==null?'':$F('ogcwxs.datasetCategory'));data.CREATETHUMBNAILS=$('ogcwxs.createThumbnails').checked;data.USELAYER=$('ogcwxs.useLayer').checked;data.USELAYERMD=$('ogcwxs.useLayerMd').checked;data.PRIVILEGES=this.getPrivileges();data.CATEGORIES=this.getSelectedCategories();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function clearIcons()
{$('ogcwxs.icon').options.length=0;}
function addIcon(file)
{gui.addToSelect('ogcwxs.icon',file,file);}
function updateIcon()
{var icon=$F('ogcwxs.icon');var image=$('ogcwxs.icon.image');image.setAttribute('src',Env.url+'/images/harvesting/'+icon);}}
ker.include('harvesting/filesystem/model.js');ker.include('harvesting/filesystem/view.js');var filesystem=new Object();function Filesystem(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new filesystem.Model(loader);var view=new filesystem.View(loader);this.addGroupRow=addGroupRow;this.removeGroupRow=view.removeGroupRow;this.getResultTip=view.getResultTip;this.model=model;this.view=view;this.getType=function(){return"filesystem";}
this.getLabel=function(){return loader.eval("info[@type='filesystem']/long");}
this.getEditPanel=function(){return"filesystem.editPanel";}
this.init=function()
{this.view.init();model.retrieveGroups(ker.wrap(this,init_groups_OK));model.retrieveCategories(ker.wrap(this,init_categ_OK));model.retrieveIcons(ker.wrap(this,init_icons_OK));}
function init_groups_OK(data)
{view.clearGroups();for(var i=0;i<data.length;i++)
view.addGroup(data[i].id,data[i].label[Env.lang]);}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++)
view.addCategory(data[i].id,data[i].label[Env.lang]);}
function init_icons_OK(data)
{view.clearIcons();for(var i=0;i<data.length;i++)
view.addIcon(data[i]);}
function addGroupRow()
{var groups=view.getSelectedGroups();if(groups.length==0)alert(loader.getText('pleaseSelectGroup'));else view.addEmptyGroupRows(groups);}}
filesystem.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;var callBackF=null;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.retrieveIcons=retrieveIcons;this.getUpdateRequest=getUpdateRequest;function retrieveGroups(callBack)
{new InfoService(loader,'groups',callBack);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function retrieveIcons(callBack)
{callBackF=callBack;var request=ker.createRequest('type','icons');ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveIcons_OK));}
function retrieveIcons_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var data=[];var list=xml.children(xml.children(xmlRes)[0]);for(var i=0;i<list.length;i++)
data.push(xml.textContent(list[i]));callBackF(data);}}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <directoryname>{DIRECTORYNAME}</directoryname>'+'      <recurse>{RECURSE}</recurse>'+'      <nodelete>{NODELETE}</nodelete>'+'      <icon>{ICON}</icon>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'    </options>'+'    <privileges>'+'       {PRIVIL_LIST}'+'    </privileges>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';}
arcsde.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;var callBackF=null;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.retrieveIcons=retrieveIcons;this.getUpdateRequest=getUpdateRequest;function retrieveGroups(callBack)
{new InfoService(loader,'groups',callBack);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function retrieveIcons(callBack)
{callBackF=callBack;var request=ker.createRequest('type','icons');ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveIcons_OK));}
function retrieveIcons_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var data=[];var list=xml.children(xml.children(xmlRes)[0]);for(var i=0;i<list.length;i++)
data.push(xml.textContent(list[i]));callBackF(data);}}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <server>{SERVER}</server>'+'      <port>{PORT}</port>'+'      <username>{USERNAME}</username>'+'      <password>{PASSWORD}</password>'+'      <database>{DATABASE}</database>'+'      <icon>{ICON}</icon>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'    </options>'+'    <privileges>'+'       {PRIVIL_LIST}'+'    </privileges>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';}
ker.include('calendar/calendar.js');ker.include('calendar/calendar-setup.js');ker.include('calendar/lang/calendar-en.js');ker.include('harvesting/oaipmh/model.js');ker.include('harvesting/oaipmh/view.js');var oaipmh=new Object();function OaiPmh(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new oaipmh.Model(loader);var view=new oaipmh.View(loader);this.addSearchRow=view.addEmptySearch;this.addGroupRow=addGroupRow;this.removeGroupRow=view.removeGroupRow;this.getResultTip=view.getResultTip;this.retrieveInfo=retrieveInfo;this.model=model;this.view=view;this.getType=function(){return"oaipmh";}
this.getLabel=function(){return loader.eval("info[@type='oaipmh']/long");}
this.getEditPanel=function(){return"oai.editPanel";}
this.init=function()
{this.view.init();model.retrieveGroups(ker.wrap(this,init_groups_OK));model.retrieveCategories(ker.wrap(this,init_categ_OK));model.retrieveIcons(ker.wrap(this,init_icons_OK));}
function init_groups_OK(data)
{view.clearGroups();for(var i=0;i<data.length;i++)
view.addGroup(data[i].id,data[i].label[Env.lang]);}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++)
view.addCategory(data[i].id,data[i].label[Env.lang]);}
function init_icons_OK(data)
{view.clearIcons();for(var i=0;i<data.length;i++)
view.addIcon(data[i]);}
function addGroupRow()
{var groups=view.getSelectedGroups();if(groups.length==0)alert(loader.getText('pleaseSelectGroup'));else view.addEmptyGroupRows(groups);}
function retrieveInfo()
{var url=view.getUrl();if((url.indexOf('http://')!=0)&&(url.indexOf('https://')!=0))
alert(loader.getText('supplyUrl'));else
model.retrieveInfo(url,ker.wrap(view,view.setInfo));}}
ker.include('harvesting/geonet20/model.js');ker.include('harvesting/geonet20/view.js');var gn20=new Object();function Geonetwork20(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new gn20.Model(loader);var view=new gn20.View(loader);this.addSearchRow=addSearchRow;this.removeSearchRow=view.removeSearch;this.getResultTip=view.getResultTip;this.model=model;this.view=view;this.getType=function(){return"geonetwork20";}
this.getLabel=function(){return loader.eval("info[@type='geonetwork20']/long");}
this.getEditPanel=function(){return"gn20.editPanel";}
function addSearchRow()
{var siteId=view.getSiteId();if(siteId=='')
alert(loader.getText('pleaseSpecifySiteId'));else
{view.addEmptySearch(siteId);view.clearSiteId();}}}
csw.View=function(xmlLoader)
{HarvesterView.call(this);var searchTransf=new XSLTransformer('harvesting/csw/client-search-row.xsl',xmlLoader);var privilTransf=new XSLTransformer('harvesting/csw/client-privil-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/csw/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;var currSearchId=0;this.setPrefix('csw');this.setPrivilTransf(privilTransf);this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.clearIcons=clearIcons;this.addIcon=addIcon;this.addEmptySearch=addEmptySearch;this.removeSearch=removeSearch;Event.observe('csw.icon','change',ker.wrap(this,updateIcon));function init()
{valid.add([{id:'csw.name',type:'length',minSize:1,maxSize:200},{id:'csw.capabUrl',type:'length',minSize:1,maxSize:200},{id:'csw.capabUrl',type:'url'},{id:'csw.username',type:'length',minSize:0,maxSize:200},{id:'csw.password',type:'length',minSize:0,maxSize:200},{id:'csw.every.days',type:'integer',minValue:0,maxValue:99},{id:'csw.every.hours',type:'integer',minValue:0,maxValue:23},{id:'csw.every.mins',type:'integer',minValue:0,maxValue:59}]);shower=new Shower('csw.useAccount','csw.account');}
function setEmpty()
{this.setEmptyCommon();removeAllSearch();$('csw.capabUrl').value='';var icons=$('csw.icon').options;for(var i=0;i<icons.length;i++)
if(icons[i].value=='default.gif')
{icons[i].selected=true;break;}
shower.update();updateIcon();}
function setData(node)
{this.setDataCommon(node);var site=node.getElementsByTagName('site')[0];var searches=node.getElementsByTagName('searches')[0];hvutil.setOption(site,'capabilitiesUrl','csw.capabUrl');hvutil.setOption(site,'icon','csw.icon');var list=searches.getElementsByTagName('search');removeAllSearch();for(var i=0;i<list.length;i++)
addSearch(list[i]);this.removeAllGroupRows();this.addGroupRows(node);this.unselectCategories();this.selectCategories(node);shower.update();updateIcon();}
function getData()
{var data=this.getDataCommon();data.CAPAB_URL=$F('csw.capabUrl');data.ICON=$F('csw.icon');var searchData=[];var searchList=xml.children($('csw.searches'));for(var i=0;i<searchList.length;i++)
{var divElem=searchList[i];searchData.push({ANY_TEXT:xml.getElementById(divElem,'csw.anytext').value,TITLE:xml.getElementById(divElem,'csw.title').value,ABSTRACT:xml.getElementById(divElem,'csw.abstract').value,SUBJECT:xml.getElementById(divElem,'csw.subject').value});}
data.SEARCH_LIST=searchData;data.PRIVILEGES=this.getPrivileges();data.CATEGORIES=this.getSelectedCategories();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function clearIcons()
{$('csw.icon').options.length=0;}
function addIcon(file)
{gui.addToSelect('csw.icon',file,file);}
function updateIcon()
{var icon=$F('csw.icon');var image=$('csw.icon.image');image.setAttribute('src',Env.url+'/images/harvesting/'+icon);}
function addEmptySearch()
{var doc=Sarissa.getDomDocument();var search=doc.createElement('search');addSearch(search);}
function addSearch(search)
{var id=''+currSearchId++;search.setAttribute('id',id);var html=searchTransf.transformToText(search);new Insertion.Bottom('csw.searches',html);valid.add([{id:'csw.anytext',type:'length',minSize:0,maxSize:200},{id:'csw.title',type:'length',minSize:0,maxSize:200},{id:'csw.abstract',type:'length',minSize:0,maxSize:200},{id:'csw.subject',type:'length',minSize:0,maxSize:200}],id);}
function removeSearch(id)
{valid.removeByParent(id);Element.remove(id);}
function removeAllSearch()
{$('csw.searches').innerHTML='';valid.removeByParent();}}
gn.View=function(xmlLoader)
{HarvesterView.call(this);var searchTransf=new XSLTransformer('harvesting/geonet/client-search-row.xsl',xmlLoader);var policyTransf=new XSLTransformer('harvesting/geonet/client-policy-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/geonet/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;var sources=null;var currSearchId=0;this.setPrefix('gn');this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.setSources=setSources;this.addEmptySearch=addEmptySearch;this.addSearch=addSearch;this.removeSearch=removeSearch;this.removeAllSearch=removeAllSearch;this.getHostData=getHostData;this.getPolicyGroups=getPolicyGroups;this.getListedPolicyGroups=getListedPolicyGroups;this.addPolicyGroup=addPolicyGroup;this.removePolicyGroup=removePolicyGroup;this.removeAllPolicyGroups=removeAllPolicyGroups;this.findPolicyGroup=findPolicyGroup;this.removeAllGroupRows=function(){}
function init()
{valid.add([{id:'gn.name',type:'length',minSize:1,maxSize:200},{id:'gn.host',type:'length',minSize:1,maxSize:200},{id:'gn.host',type:'hostname'},{id:'gn.port',type:'integer',minValue:80,maxValue:65535,empty:true},{id:'gn.servlet',type:'length',minSize:1,maxSize:200},{id:'gn.username',type:'length',minSize:0,maxSize:200},{id:'gn.password',type:'length',minSize:0,maxSize:200},{id:'gn.every.days',type:'integer',minValue:0,maxValue:99},{id:'gn.every.hours',type:'integer',minValue:0,maxValue:23},{id:'gn.every.mins',type:'integer',minValue:0,maxValue:59}]);shower=new Shower('gn.useAccount','gn.account');gui.setupTooltips(loader.getNode('tips'));}
function setEmpty()
{this.setEmptyCommon();sources=null;removeAllSearch();removeAllPolicyGroups();$('gn.host').value='';$('gn.port').value='';$('gn.servlet').value='';shower.update();}
function setData(node)
{this.setDataCommon(node);sources=null;var site=node.getElementsByTagName('site')[0];var searches=node.getElementsByTagName('searches')[0];var policies=node.getElementsByTagName('groupsCopyPolicy')[0];hvutil.setOption(site,'host','gn.host');hvutil.setOption(site,'port','gn.port');hvutil.setOption(site,'servlet','gn.servlet');var list=searches.getElementsByTagName('search');removeAllSearch();for(var i=0;i<list.length;i++)
addSearch(list[i]);var list=policies.getElementsByTagName('group');removeAllPolicyGroups();for(var i=0;i<list.length;i++)
{var name=list[i].getAttribute('name');var policy=list[i].getAttribute('policy');addPolicyGroup(name,policy);}
this.unselectCategories();this.selectCategories(node);shower.update();}
function getData()
{var data=this.getDataCommon();data.HOST=$F('gn.host');data.PORT=$F('gn.port');data.SERVLET=$F('gn.servlet');var searchData=[];var searchList=xml.children($('gn.searches'));for(var i=0;i<searchList.length;i++)
{var divElem=searchList[i];var sourceElem=xml.getElementById(divElem,'gn.source')
searchData.push({TEXT:xml.getElementById(divElem,'gn.text').value,TITLE:xml.getElementById(divElem,'gn.title').value,ABSTRACT:xml.getElementById(divElem,'gn.abstract').value,KEYWORDS:xml.getElementById(divElem,'gn.keywords').value,DIGITAL:xml.getElementById(divElem,'gn.digital').checked,HARDCOPY:xml.getElementById(divElem,'gn.hardcopy').checked,SOURCE_UUID:$F(sourceElem),SOURCE_NAME:xml.textContent(sourceElem.options[sourceElem.selectedIndex])});}
data.SEARCH_LIST=searchData;data.CATEGORIES=this.getSelectedCategories();data.GROUP_LIST=getPolicyGroups();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function setSources(data)
{sources=data;var searchData=[];var list=xml.children($('gn.searches'));for(var i=0;i<list.length;i++)
{var source=xml.getElementById(list[i],'gn.source');var selUuid=$F(source);clearSources(source);addSource(source,'','',selUuid);for(var j=0;j<data.length;j++)
{var uuid=data[j].uuid;var name=data[j].name;addSource(source,uuid,name,selUuid);}}}
function clearSources(elem){elem.options.length=0;}
function addSource(elem,uuid,label,selUuid)
{gui.addToSelect(elem,uuid,label,uuid==selUuid);}
function addEmptySearch()
{var doc=Sarissa.getDomDocument();var search=doc.createElement('search');if(sources!=null)
{var src=doc.createElement('sources');for(var i=0;i<sources.length;i++)
{var s=doc.createElement('source');s.setAttribute('name',sources[i].name);s.setAttribute('uuid',sources[i].uuid);src.appendChild(s);}
search.appendChild(src);}
doc.appendChild(search);addSearch(search);}
function addSearch(search)
{var id=''+currSearchId++;search.setAttribute('id',id);var html=searchTransf.transformToText(search);new Insertion.Bottom('gn.searches',html);valid.add([{id:'gn.text',type:'length',minSize:0,maxSize:200},{id:'gn.title',type:'length',minSize:0,maxSize:200},{id:'gn.abstract',type:'length',minSize:0,maxSize:200},{id:'gn.keywords',type:'length',minSize:0,maxSize:200}],id);}
function removeSearch(id)
{valid.removeByParent(id);Element.remove(id);}
function removeAllSearch()
{$('gn.searches').innerHTML='';valid.removeByParent();}
function getPolicyGroups()
{var groupData=[];var groupList=$('gn.groups').getElementsByTagName('TR');for(var i=1;i<groupList.length;i++)
{var rowElem=groupList[i];var id=rowElem.getAttribute('id');var name=id.substring(9);var list=rowElem.getElementsByTagName('SELECT');var policy=$F(list[0]);if(policy!='dontCopy')
groupData.push({NAME:name,POLICY:policy});}
return groupData;}
function getListedPolicyGroups()
{var groupData=[];var groupList=$('gn.groups').getElementsByTagName('TR');for(var i=1;i<groupList.length;i++)
{var rowElem=groupList[i];var id=rowElem.getAttribute('id');var name=id.substring(9);groupData.push(name);}
return groupData;}
function addPolicyGroup(name,policy)
{var doc=Sarissa.getDomDocument();var group=doc.createElement('group');group.setAttribute('name',name);group.setAttribute('policy',policy);var xslRes=policyTransf.transform(group);gui.appendTableRow('gn.groups',xslRes);}
function removePolicyGroup(name)
{Element.remove('gn.group.'+name);}
function removeAllPolicyGroups()
{var rows=$('gn.groups').getElementsByTagName('TR');for(var i=rows.length-1;i>0;i--)
Element.remove(rows[i]);}
function findPolicyGroup(name)
{var list=$('gn.groups').getElementsByTagName('TR');for(var i=1;i<list.length;i++)
{var row=list[i];var gname=row.getAttribute('id');if('gn.group.'+name==gname)
return row;}
return null;}
function getHostData()
{var data={HOST:$F('gn.host'),PORT:$F('gn.port'),SERVLET:$F('gn.servlet'),USERNAME:$F('gn.username'),PASSWORD:$F('gn.password'),USE_ACCOUNT:$('gn.useAccount').checked};return data;}}
wd.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;var callBackF=null;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.retrieveIcons=retrieveIcons;this.getUpdateRequest=getUpdateRequest;function retrieveGroups(callBack)
{new InfoService(loader,'groups',callBack);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function retrieveIcons(callBack)
{callBackF=callBack;var request=ker.createRequest('type','icons');ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveIcons_OK));}
function retrieveIcons_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var data=[];var list=xml.children(xml.children(xmlRes)[0]);for(var i=0;i<list.length;i++)
data.push(xml.textContent(list[i]));callBackF(data);}}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <url>{URL}</url>'+'      <icon>{ICON}</icon>'+'      <account>'+'        <use>{USE_ACCOUNT}</use>'+'        <username>{USERNAME}</username>'+'        <password>{PASSWORD}</password>'+'      </account>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'      <recurse>{RECURSE}</recurse>'+'      <validate>{VALIDATE}</validate>'+'    </options>'+'    <privileges>'+'       {PRIVIL_LIST}'+'    </privileges>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';}
ker.include('harvesting/webdav/model.js');ker.include('harvesting/webdav/view.js');var wd=new Object();function WebDav(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new wd.Model(loader);var view=new wd.View(loader);this.addGroupRow=addGroupRow;this.removeGroupRow=view.removeGroupRow;this.getResultTip=view.getResultTip;this.model=model;this.view=view;this.getType=function(){return"webdav";}
this.getLabel=function(){return loader.eval("info[@type='webdav']/long");}
this.getEditPanel=function(){return"wd.editPanel";}
this.init=function()
{this.view.init();model.retrieveGroups(ker.wrap(this,init_groups_OK));model.retrieveCategories(ker.wrap(this,init_categ_OK));model.retrieveIcons(ker.wrap(this,init_icons_OK));}
function init_groups_OK(data)
{view.clearGroups();for(var i=0;i<data.length;i++)
view.addGroup(data[i].id,data[i].label[Env.lang]);}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++)
view.addCategory(data[i].id,data[i].label[Env.lang]);}
function init_icons_OK(data)
{view.clearIcons();for(var i=0;i<data.length;i++)
view.addIcon(data[i]);}
function addGroupRow()
{var groups=view.getSelectedGroups();if(groups.length==0)alert(loader.getText('pleaseSelectGroup'));else view.addEmptyGroupRows(groups);}}
filesystem.View=function(xmlLoader)
{HarvesterView.call(this);var privilTransf=new XSLTransformer('harvesting/filesystem/client-privil-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/filesystem/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;this.setPrefix('filesystem');this.setPrivilTransf(privilTransf);this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.clearIcons=clearIcons;this.addIcon=addIcon;this.removeAllGroupRows=function(){}
this.unselectCategories=function(){};Event.observe('filesystem.icon','change',ker.wrap(this,updateIcon));function init()
{valid.add([{id:'filesystem.name',type:'length',minSize:1,maxSize:200},{id:'filesystem.directoryname',type:'length',minSize:1,maxSize:500},{id:'filesystem.recurse',type:'length',minSize:1,maxSize:10},{id:'filesystem.nodelete',type:'length',minSize:1,maxSize:10},{id:'filesystem.every.days',type:'integer',minValue:0,maxValue:99},{id:'filesystem.every.hours',type:'integer',minValue:0,maxValue:23},{id:'filesystem.every.mins',type:'integer',minValue:0,maxValue:59}]);}
function setEmpty()
{this.setEmptyCommon();var icons=$('filesystem.icon').options;for(var i=0;i<icons.length;i++)
if(icons[i].value=='default.gif')
{icons[i].selected=true;break;}
updateIcon();}
function setData(node)
{this.setDataCommon(node);var name=node.getElementsByTagName('name')[0];var directoryname=node.getElementsByTagName('directory')[0];var recurse=node.getElementsByTagName('recurse')[0];var nodelete=node.getElementsByTagName('nodelete')[0];hvutil.setOption(node,'directory','filesystem.directoryname');hvutil.setOption(node,'recurse','filesystem.recurse');hvutil.setOption(node,'nodelete','filesystem.nodelete');hvutil.setOption(node,'icon','filesystem.icon');this.removeAllGroupRows();this.addGroupRows(node);this.unselectCategories();this.selectCategories(node);updateIcon();}
function getData()
{var data=this.getDataCommon();data.DIRECTORYNAME=$F('filesystem.directoryname');data.RECURSE=$F('filesystem.recurse');data.NODELETE=$F('filesystem.nodelete');data.ICON=$F('filesystem.icon');data.PRIVILEGES=this.getPrivileges();data.CATEGORIES=this.getSelectedCategories();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function clearIcons()
{$('filesystem.icon').options.length=0;}
function addIcon(file)
{gui.addToSelect('filesystem.icon',file,file);}
function updateIcon()
{var icon=$F('filesystem.icon');var image=$('filesystem.icon.image');image.setAttribute('src',Env.url+'/images/harvesting/'+icon);}}
oaipmh.View=function(xmlLoader)
{HarvesterView.call(this);var searchTransf=new XSLTransformer('harvesting/oaipmh/client-search-row.xsl',xmlLoader);var privilTransf=new XSLTransformer('harvesting/oaipmh/client-privil-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/oaipmh/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;var info=null;var currSearchId=0;this.setPrefix('oai');this.setPrivilTransf(privilTransf);this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.clearIcons=clearIcons;this.addIcon=addIcon;this.addEmptySearch=addEmptySearch;this.setInfo=setInfo;this.getUrl=getUrl;Event.observe('oai.icon','change',ker.wrap(this,updateIcon));function init()
{valid.add([{id:'oai.name',type:'length',minSize:1,maxSize:200},{id:'oai.url',type:'length',minSize:1,maxSize:200},{id:'oai.url',type:'url'},{id:'oai.username',type:'length',minSize:0,maxSize:200},{id:'oai.password',type:'length',minSize:0,maxSize:200},{id:'oai.every.days',type:'integer',minValue:0,maxValue:99},{id:'oai.every.hours',type:'integer',minValue:0,maxValue:23},{id:'oai.every.mins',type:'integer',minValue:0,maxValue:59}]);shower=new Shower('oai.useAccount','oai.account');gui.setupTooltips(loader.getNode('tips'));}
function setEmpty()
{this.setEmptyCommon();info=null;removeAllSearch();$('oai.url').value='';$('wd.validate').checked=false;var icons=$('oai.icon').options;for(var i=0;i<icons.length;i++)
if(icons[i].value=='default.gif')
{icons[i].selected=true;break;}
shower.update();updateIcon();}
function setData(node)
{this.setDataCommon(node);info=null;var site=node.getElementsByTagName('site')[0];var searches=node.getElementsByTagName('searches')[0];var options=node.getElementsByTagName('options')[0];hvutil.setOption(site,'url','oai.url');hvutil.setOption(site,'icon','oai.icon');hvutil.setOption(options,'validate','oai.validate');var list=searches.getElementsByTagName('search');removeAllSearch();for(var i=0;i<list.length;i++)
addSearch(list[i]);this.removeAllGroupRows();this.addGroupRows(node);this.unselectCategories();this.selectCategories(node);shower.update();updateIcon();}
function getData()
{var data=this.getDataCommon();data.URL=$F('oai.url');data.ICON=$F('oai.icon');data.VALIDATE=$('oai.validate').checked;var searchData=[];var searchList=xml.children($('oai.searches'));for(var i=0;i<searchList.length;i++)
{var divElem=searchList[i];var id=divElem.getAttribute('id');searchData.push({FROM:$F(id+'.oai.from'),UNTIL:$F(id+'.oai.until'),SET:$F(id+'.oai.set'),PREFIX:$F(id+'.oai.prefix'),STYLESHEET:$F(id+'.oai.stylesheet')});}
data.SEARCH_LIST=searchData;data.PRIVILEGES=this.getPrivileges();data.CATEGORIES=this.getSelectedCategories();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function clearIcons()
{$('oai.icon').options.length=0;}
function addIcon(file)
{gui.addToSelect('oai.icon',file,file);}
function updateIcon()
{var icon=$F('oai.icon');var image=$('oai.icon.image');image.setAttribute('src',Env.url+'/images/harvesting/'+icon);}
function setInfo(data)
{info=data;var searchData=[];var searchList=xml.children($('oai.searches'));for(var i=0;i<searchList.length;i++)
{var divElem=searchList[i];var id=divElem.getAttribute('id');var setName=$F(id+'.oai.set');var mdfName=$F(id+'.oai.prefix');setupCombo(id,setName,mdfName);}}
function setupCombo(id,setName,mdfName)
{var setElem=$(id+'.oai.set');var mdfElem=$(id+'.oai.prefix');clearCombo(setElem);addCombo(setElem,'','',setName);for(var j=0;j<info.SETS.length;j++)
{var name=info.SETS[j].NAME;var label=info.SETS[j].LABEL;addCombo(setElem,name,label,setName);}
clearCombo(mdfElem);for(var j=0;j<info.FORMATS.length;j++)
{var name=info.FORMATS[j];addCombo(mdfElem,name,name,mdfName);}}
function clearCombo(elem){elem.options.length=0;}
function addCombo(elem,name,label,selName)
{gui.addToSelect(elem,name,label,name==selName);}
function addEmptySearch()
{var doc=Sarissa.getDomDocument();var search=doc.createElement('search');addSearch(search);}
function addSearch(search)
{var id=''+currSearchId++;search.setAttribute('id',id);var html=searchTransf.transformToText(search);new Insertion.Bottom('oai.searches',html);Event.observe(id+'.oai.remove','click',ker.wrap(this,function()
{Element.remove(id);}));Calendar.setup({inputField:id+".oai.from",ifFormat:"%Y-%m-%d",button:id+".oai.from.set",showsTime:false,align:"Br",singleClick:true});Event.observe(id+'.oai.from.clear','click',ker.wrap(this,function()
{$(id+'.oai.from').value='';}));Calendar.setup({inputField:id+".oai.until",ifFormat:"%Y-%m-%d",button:id+".oai.until.set",showsTime:false,align:"Br",singleClick:true});Event.observe(id+'.oai.until.clear','click',ker.wrap(this,function()
{$(id+'.oai.until').value='';}));gui.setupTooltip(id+'.oai.remove',loader.evalNode('tips/tip[@id="oai.remove"]'));gui.setupTooltip(id+'.oai.from',loader.evalNode('tips/tip[@id="oai.from"]'));gui.setupTooltip(id+'.oai.from.set',loader.evalNode('tips/tip[@id="oai.from.set"]'));gui.setupTooltip(id+'.oai.from.clear',loader.evalNode('tips/tip[@id="oai.from.clear"]'));gui.setupTooltip(id+'.oai.until',loader.evalNode('tips/tip[@id="oai.until"]'));gui.setupTooltip(id+'.oai.until.set',loader.evalNode('tips/tip[@id="oai.until.set"]'));gui.setupTooltip(id+'.oai.until.clear',loader.evalNode('tips/tip[@id="oai.until.clear"]'));gui.setupTooltip(id+'.oai.set',loader.evalNode('tips/tip[@id="oai.set"]'));gui.setupTooltip(id+'.oai.prefix',loader.evalNode('tips/tip[@id="oai.prefix"]'));var set=xml.evalXPath(search,'set');var prefix=xml.evalXPath(search,'prefix');if(info==null)
{gui.addToSelect(id+'.oai.set','','',false);gui.addToSelect(id+'.oai.prefix','oai_dc','oai_dc',false);if(set!=null&&set!='')
gui.addToSelect(id+'.oai.set',set,set,true);if(prefix!=null&&prefix!='oai_dc')
gui.addToSelect(id+'.oai.prefix',prefix,prefix,true);}
else
{setupCombo(id,set,prefix);}}
function removeAllSearch()
{$('oai.searches').innerHTML='';}
function getUrl()
{return $F('oai.url');}}
ker.include('harvesting/ogcwxs/model.js');ker.include('harvesting/ogcwxs/view.js');var ogcwxs=new Object();function OgcWxs(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new ogcwxs.Model(loader);var view=new ogcwxs.View(loader);this.addGroupRow=addGroupRow;this.removeGroupRow=view.removeGroupRow;this.getResultTip=view.getResultTip;this.model=model;this.view=view;this.getType=function(){return"ogcwxs";}
this.getLabel=function(){return loader.eval("info[@type='ogcwxs']/long");}
this.getEditPanel=function(){return"ogcwxs.editPanel";}
this.init=function()
{this.view.init();model.retrieveGroups(ker.wrap(this,init_groups_OK));model.retrieveCategories(ker.wrap(this,init_categ_OK));model.retrieveIcons(ker.wrap(this,init_icons_OK));}
function init_groups_OK(data)
{view.clearGroups();for(var i=0;i<data.length;i++)
view.addGroup(data[i].id,data[i].label[Env.lang]);}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++){view.addCategory(data[i].id,data[i].label[Env.lang]);gui.addToSelect('ogcwxs.datasetCategory',data[i].id,data[i].label[Env.lang]);}}
function init_icons_OK(data)
{view.clearIcons();for(var i=0;i<data.length;i++)
view.addIcon(data[i]);}
function addGroupRow()
{var groups=view.getSelectedGroups();if(groups.length==0)alert(loader.getText('pleaseSelectGroup'));else view.addEmptyGroupRows(groups);}}
csw.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;var callBackF=null;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.retrieveIcons=retrieveIcons;this.getUpdateRequest=getUpdateRequest;function retrieveGroups(callBack)
{new InfoService(loader,'groups',callBack);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function retrieveIcons(callBack)
{callBackF=callBack;var request=ker.createRequest('type','icons');ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveIcons_OK));}
function retrieveIcons_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var data=[];var list=xml.children(xml.children(xmlRes)[0]);for(var i=0;i<list.length;i++)
data.push(xml.textContent(list[i]));callBackF(data);}}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);var list=data.SEARCH_LIST;var text='';for(var i=0;i<list.length;i++)
text+=str.substitute(searchTemp,list[i]);request=str.replace(request,'{SEARCH_LIST}',text);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <capabilitiesUrl>{CAPAB_URL}</capabilitiesUrl>'+'      <icon>{ICON}</icon>'+'      <account>'+'        <use>{USE_ACCOUNT}</use>'+'        <username>{USERNAME}</username>'+'        <password>{PASSWORD}</password>'+'      </account>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'    </options>'+'    <searches>'+'       {SEARCH_LIST}'+'    </searches>'+'    <privileges>'+'       {PRIVIL_LIST}'+'    </privileges>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';var searchTemp='    <search>'+'      <freeText>{ANY_TEXT}</freeText>'+'      <title>{TITLE}</title>'+'      <abstract>{ABSTRACT}</abstract>'+'      <subject>{SUBJECT}</subject>'+'    </search>';}
oaipmh.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;var callBackF=null;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.retrieveIcons=retrieveIcons;this.getUpdateRequest=getUpdateRequest;this.retrieveInfo=retrieveInfo;function retrieveGroups(callBack)
{new InfoService(loader,'groups',callBack);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function retrieveIcons(callBack)
{callBackF=callBack;var request=ker.createRequest('type','icons');ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveIcons_OK));}
function retrieveIcons_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var data=[];var list=xml.children(xml.children(xmlRes)[0]);for(var i=0;i<list.length;i++)
data.push(xml.textContent(list[i]));callBackF(data);}}
function retrieveInfo(url,callBack)
{callBackF=callBack;var request='<request>'+'   <type url="'+url+'">oaiPmhServer</type>'+'</request>';ker.send('xml.harvesting.info',request,ker.wrap(this,retrieveInfo_OK));}
function retrieveInfo_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(loader.getText('cannotRetrieve'),xmlRes);else
{var info=xml.children(xmlRes)[0];var error=xml.children(info)[0];if(error.nodeName=='error')
{ker.showError(loader.getText('cannotQueryOai'),error);return;}
var mdForm=xml.children(info,'formats')[0];var mdSets=xml.children(info,'sets')[0];var formats=[];var list=xml.children(mdForm);for(var i=0;i<list.length;i++)
formats.push(xml.textContent(list[i]));var sets=[];var list=xml.children(mdSets);for(var i=0;i<list.length;i++)
{var set=list[i];var data={NAME:xml.evalXPath(set,'name'),LABEL:xml.evalXPath(set,'label')};sets.push(data);}
var data={FORMATS:formats,SETS:sets};callBackF(data);}}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);var list=data.SEARCH_LIST;var text='';for(var i=0;i<list.length;i++)
text+=str.substitute(searchTemp,list[i]);request=str.replace(request,'{SEARCH_LIST}',text);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <url>{URL}</url>'+'      <icon>{ICON}</icon>'+'      <account>'+'        <use>{USE_ACCOUNT}</use>'+'        <username>{USERNAME}</username>'+'        <password>{PASSWORD}</password>'+'      </account>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'      <validate>{VALIDATE}</validate>'+'    </options>'+'    <searches>'+'       {SEARCH_LIST}'+'    </searches>'+'    <privileges>'+'       {PRIVIL_LIST}'+'    </privileges>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';var searchTemp='    <search>'+'      <from>{FROM}</from>'+'      <until>{UNTIL}</until>'+'      <set>{SET}</set>'+'      <prefix>{PREFIX}</prefix>'+'      <stylesheet>{STYLESHEET}</stylesheet>'+'    </search>';}
gn20.Model=function(xmlLoader)
{var loader=xmlLoader;this.getUpdateRequest=getUpdateRequest;function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);var list=data.SEARCH_LIST;var text='';for(var i=0;i<list.length;i++)
text+=str.substitute(searchTemp,list[i]);return str.replace(request,'{SEARCHES}',text);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <host>{HOST}</host>'+'      <port>{PORT}</port>'+'      <servlet>{SERVLET}</servlet>'+'      <account>'+'        <use>{USE_ACCOUNT}</use>'+'        <username>{USERNAME}</username>'+'        <password>{PASSWORD}</password>'+'      </account>'+'    </site>'+'    <searches>'+'       {SEARCHES}'+'    </searches>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'    </options>'+'  </node>';var searchTemp='    <search>'+'      <freeText>{TEXT}</freeText>'+'      <title>{TITLE}</title>'+'      <abstract>{ABSTRACT}</abstract>'+'      <keywords>{KEYWORDS}</keywords>'+'      <digital>{DIGITAL}</digital>'+'      <hardcopy>{HARDCOPY}</hardcopy>'+'      <siteId>{SITE_ID}</siteId>'+'    </search>';}
ker.include('harvesting/arcsde/model.js');ker.include('harvesting/arcsde/view.js');var arcsde=new Object();function Arcsde(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new arcsde.Model(loader);var view=new arcsde.View(loader);this.addGroupRow=addGroupRow;this.removeGroupRow=view.removeGroupRow;this.getResultTip=view.getResultTip;this.model=model;this.view=view;this.getType=function(){return"arcsde";}
this.getLabel=function(){return loader.eval("info[@type='arcsde']/long");}
this.getEditPanel=function(){return"arcsde.editPanel";}
this.init=function()
{this.view.init();model.retrieveGroups(ker.wrap(this,init_groups_OK));model.retrieveCategories(ker.wrap(this,init_categ_OK));model.retrieveIcons(ker.wrap(this,init_icons_OK));}
function init_groups_OK(data)
{view.clearGroups();for(var i=0;i<data.length;i++)
view.addGroup(data[i].id,data[i].label[Env.lang]);}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++)
view.addCategory(data[i].id,data[i].label[Env.lang]);}
function init_icons_OK(data)
{view.clearIcons();for(var i=0;i<data.length;i++)
view.addIcon(data[i]);}
function addGroupRow()
{var groups=view.getSelectedGroups();if(groups.length==0)alert(loader.getText('pleaseSelectGroup'));else view.addEmptyGroupRows(groups);}}
z3950.View=function(xmlLoader)
{HarvesterView.call(this);var privilTransf=new XSLTransformer('harvesting/z3950/client-privil-row.xsl',xmlLoader);var resultTransf=new XSLTransformer('harvesting/z3950/client-result-tip.xsl',xmlLoader);var loader=xmlLoader;var valid=new Validator(loader);var shower=null;var currSearchId=0;this.setPrefix('z39');this.setPrivilTransf(privilTransf);this.setResultTransf(resultTransf);this.init=init;this.setEmpty=setEmpty;this.setData=setData;this.getData=getData;this.isDataValid=isDataValid;this.clearIcons=clearIcons;this.addIcon=addIcon;Event.observe('z39.icon','change',ker.wrap(this,updateIcon));function init()
{valid.add([{id:'z39.name',type:'length',minSize:1,maxSize:200},{id:'z39.username',type:'length',minSize:0,maxSize:200},{id:'z39.password',type:'length',minSize:0,maxSize:200},{id:'z39.every.days',type:'integer',minValue:0,maxValue:99},{id:'z39.every.hours',type:'integer',minValue:0,maxValue:23},{id:'z39.every.mins',type:'integer',minValue:0,maxValue:59}]);shower=new Shower('z39.useAccount','z39.account');}
function setEmpty()
{this.setEmptyCommon();var icons=$('z39.icon').options;for(var i=0;i<icons.length;i++)
if(icons[i].value=='default.gif')
{icons[i].selected=true;break;}
shower.update();updateIcon();}
function setData(node)
{this.setDataCommon(node);var site=node.getElementsByTagName('site')[0];var searches=node.getElementsByTagName('searches')[0];hvutil.setOption(site,'icon','z39.icon');this.removeAllGroupRows();this.addGroupRows(node);this.unselectCategories();this.selectCategories(node);shower.update();updateIcon();}
function getData()
{var data=this.getDataCommon();data.ICON=$F('z39.icon');data.PRIVILEGES=this.getPrivileges();data.CATEGORIES=this.getSelectedCategories();return data;}
function isDataValid()
{if(!valid.validate())
return false;return this.isDataValidCommon();}
function clearIcons()
{$('z39.icon').options.length=0;}
function addIcon(file)
{var html='<option value="'+file+'">'+xml.escape(file)+'</option>';new Insertion.Bottom('z39.icon',html);}
function updateIcon()
{var icon=$F('z39.icon');var image=$('z39.icon.image');image.setAttribute('src',Env.url+'/images/harvesting/'+icon);}}
gn.Model=function(xmlLoader)
{HarvesterModel.call(this);var loader=xmlLoader;this.retrieveSources=retrieveSources;this.retrieveGroups=retrieveGroups;this.retrieveCategories=retrieveCategories;this.getUpdateRequest=getUpdateRequest;function retrieveSources(data,callBack)
{this.retrieveSourcesCB=callBack;var url='http://'+data.HOST;if(data.PORT!='')
url+=':'+data.PORT;url+='/'+data.SERVLET+'/srv/'+Env.lang+'/xml.info';new InfoService(loader,'sources',callBack,url);}
function retrieveGroups(data,callBack,username,password)
{this.retrieveGroupsCB=callBack;var url='http://'+data.HOST;if(data.PORT!='')
url+=':'+data.PORT;url+='/'+data.SERVLET+'/srv/'+Env.lang+'/xml.info';new InfoService(loader,'groups',callBack,url,username,password);}
function retrieveCategories(callBack)
{new InfoService(loader,'categories',callBack);}
function getUpdateRequest(data)
{var request=str.substitute(updateTemp,data);var list=data.SEARCH_LIST;var text='';for(var i=0;i<list.length;i++)
text+=str.substitute(searchTemp,list[i]);request=str.replace(request,'{SEARCH_LIST}',text);list=data.GROUP_LIST;text='';for(var i=0;i<list.length;i++)
text+=str.substitute(groupTemp,list[i]);request=str.replace(request,'{GROUP_LIST}',text);return this.substituteCommon(data,request);}
var updateTemp=' <node id="{ID}" type="{TYPE}">'+'    <site>'+'      <name>{NAME}</name>'+'      <host>{HOST}</host>'+'      <port>{PORT}</port>'+'      <servlet>{SERVLET}</servlet>'+'      <account>'+'        <use>{USE_ACCOUNT}</use>'+'        <username>{USERNAME}</username>'+'        <password>{PASSWORD}</password>'+'      </account>'+'    </site>'+'    <options>'+'      <every>{EVERY}</every>'+'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+'    </options>'+'    <searches>'+'       {SEARCH_LIST}'+'    </searches>'+'    <groupsCopyPolicy>'+'       {GROUP_LIST}'+'    </groupsCopyPolicy>'+'    <categories>'+'       {CATEG_LIST}'+'    </categories>'+'  </node>';var searchTemp='    <search>'+'      <freeText>{TEXT}</freeText>'+'      <title>{TITLE}</title>'+'      <abstract>{ABSTRACT}</abstract>'+'      <keywords>{KEYWORDS}</keywords>'+'      <digital>{DIGITAL}</digital>'+'      <hardcopy>{HARDCOPY}</hardcopy>'+'      <source>'+'         <uuid>{SOURCE_UUID}</uuid>'+'         <name>{SOURCE_NAME}</name>'+'      </source>'+'    </search>';var groupTemp='<group name="{NAME}" policy="{POLICY}"/>';}
ker.include('harvesting/geonet/model.js');ker.include('harvesting/geonet/view.js');var gn=new Object();function Geonetwork(xmlLoader)
{Harvester.call(this);var loader=xmlLoader;var model=new gn.Model(loader);var view=new gn.View(loader);this.addSearchRow=view.addEmptySearch;this.removeSearchRow=view.removeSearch;this.getResultTip=view.getResultTip;this.retrieveSources=retrieveSources;this.retrieveGroups=retrieveGroups;this.model=model;this.view=view;this.getType=function(){return"geonetwork";}
this.getLabel=function(){return loader.eval("info[@type='geonetwork']/long");}
this.getEditPanel=function(){return"gn.editPanel";}
this.init=function()
{this.view.init();model.retrieveCategories(ker.wrap(this,init_categ_OK));}
function init_categ_OK(data)
{view.clearCategories();for(var i=0;i<data.length;i++)
view.addCategory(data[i].id,data[i].label[Env.lang]);}
function retrieveSources()
{var data=view.getHostData();if(data.HOST=='')
alert(loader.getText('supplyHost'));else if(data.SERVLET=='')
alert(loader.getText('supplyServlet'));else
model.retrieveSources(data,ker.wrap(view,view.setSources));}
function retrieveGroups()
{var data=view.getHostData();if(data.HOST=='')
alert(loader.getText('supplyHost'));else if(data.SERVLET=='')
alert(loader.getText('supplyServlet'));else
{var cb=ker.wrap(this,retrieveGroups_OK);if(data.USE_ACCOUNT)
model.retrieveGroups(data,cb,data.USERNAME,data.PASSWORD);else
model.retrieveGroups(data,cb);}}
function retrieveGroups_OK(data)
{for(var i=0;i<data.length;i++)
{var remoteGroup=data[i];var policyGroup=view.findPolicyGroup(remoteGroup.name);if(remoteGroup.id=='0')
continue;if(policyGroup==null)
view.addPolicyGroup(remoteGroup.name,'dontCopy');}
var list=view.getListedPolicyGroups();for(var i=0;i<list.length;i++)
if(!existsGroup(list[i],data))
view.removePolicyGroup(list[i]);}
function existsGroup(name,data)
{for(var i=0;i<data.length;i++)
if(name==data[i].name)
return true;return false;}}