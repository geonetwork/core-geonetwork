Ext.namespace("geocat.edit");

Ext.QuickTips.init();

geocat.edit.scrollTop = function() {
    var windowOffset = window.pageYOffset ? window.pageYOffset : 0;
    var docElScroll = document.documentElement ? document.documentElement.scrollTop : 0;
    var bodyScroll = document.body ? document.body.scrollTop : 0;

    if(docElScroll && (!windowOffset || (windowOffset > docElScroll))) {
	windowOffset = docElScroll;
    }

    return bodyScroll && (!windowOffset || (windowOffset > bodyScroll)) ? bodyScroll : windowOffset;
};

geocat.edit.capitalize = function(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
};

geocat.edit.createSearchCombo = function(params) {
    var record = Ext.data.Record.create([
       {name: 'displayText'},
       {name: 'valid'},
       {name: 'href'}
    ]);
    var store = new Ext.data.Store({
        baseParams: params.baseParams,
        autoLoad: false,
        url: Env.locService+'/'+params.service,
        reader: new Ext.data.XmlReader({
            totalRecords: "results",
            record: "li",
            id: "href"
        }, record)
    });
    var resultTpl = new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<img style="padding: 0px 5px 0px 5px" src="../../images/{[values["valid"] === "true" ? "green-ball.gif" : "red-ball.gif"]}"></img>{displayText}',
        '</div></tpl>'
    );
    var comboParams = {
       triggerAction:'all',
       tpl: resultTpl,
       itemSelector: 'div.search-item',
       typeAhead:false,
       mode:'remote',
       selectOnFocus:true,
       minChars:2,
       forceSelection:true,
       hideTrigger:true,
       store: store,
       valueField: 'href',
       loadingText: translate('loading'),
       displayField: 'displayText',
       listeners: {
           specialkey: function(cb, keyObj) {
               if(keyObj.getCharCode() === Ext.EventObject.ENTER && !params.addButton.disabled) {
                   params.addButton.handler();
               }
           },
           select: function() {params.addButton.enable();},
           change: function(cb,newVal,oldVal) {
               if(newVal && newVal.length > 0) {
                   params.addButton.enable();
               } else {
                   params.addButton.disable();
               }
           }
       }
    };
    Ext.apply(comboParams, params);
    return new Ext.form.ComboBox(comboParams);
};

geocat.edit.createArrayCombo = function(params) {
    var baseParams = {
         store: params.elements,
         editable:false,
         mode: 'local',
         forceSelection: true,
         triggerAction: 'all',
         value: !params.value ? params.elements[0][0] : params.value,
         allowBlank:false
     };
     Ext.apply(baseParams, params);
     return new Ext.form.ComboBox(baseParams);
};

geocat.edit.windows = [];
geocat.edit.windowWidth = 620;
geocat.edit.createWindow = function(obj, params) {
    var form = new Ext.FormPanel({
        frame:false,
        bodyBorder: true,
        labelWidth: 200,
        tbar: [{xtype:'tbtext', text:translate('popXlink.about')}],
        bodyStyle:'padding:5px 5px 0',
        defaults: {width: 380},
        defaultType: 'textfield',
        buttons: params.buttons,
        items: params.items
    });
    var currentWin = new Ext.Window({
        width: geocat.edit.windowWidth,
        height: 300,
        title: params.title,
//        modal: true,
        layout: 'fit',
        items: form,
        closeAction: 'hide'
    });

    geocat.edit.windows.push(obj);
    return currentWin;
};
geocat.edit.hideOtherWindows = function(excludedWindow) {
    for (var i = 0; i < geocat.edit.windows.length; i++) {
        var currWin = geocat.edit.windows[i];
        if (currWin !== excludedWindow) {
            currWin.searchWindow.hide();
        }
    }
};
geocat.edit.showWindow = function(obj) {
    geocat.edit.hideOtherWindows(obj);
    obj.searchWindow.setPosition((document.body.getWidth()/2)-(geocat.edit.windowWidth/2),geocat.edit.scrollTop());
    obj.searchWindow.show(undefined, function(){
        obj.searchCombo.clearValue();
        obj.searchCombo.focus('',10);
    });
};
geocat.edit.resetAll = function() {
    var close = function(obj) {
        if (obj.searchWindow !== null) {
            obj.searchWindow.hide();
            obj.searchWindow.destroy();
            obj.searchWindow = null;
        }
    };

    for (var i = 0; i < geocat.edit.windows.length; i++) {
        close(geocat.edit.windows[i]);
    }
    geocat.edit.windows = [];
};
geocat.edit.addButton = function(handler) {
    return new Ext.Button({
        text: geocat.edit.capitalize(translate('add')),
        iconCls: 'addIcon',
        disabled: true,
        tooltip: translate('popXlink.add.action.tooltip'),
        handler: handler
    });
};
geocat.edit.createButton = function(handler, name) {
    return new Ext.Button({
        text: geocat.edit.capitalize(name || translate('xlink.new')),
        iconCls: 'addIcon',
        tooltip: translate('popXlink.create.action.tooltip'),
        handler: handler
    });
};

geocat.edit.submitXLink = function() {
    if (xlinks.length === 0 || xlinks[0].href === null) {
        alert(translate("noXlink"));
        return;
    }
    
    disableEditForm();
    var eBusy = $('editorBusy');
    if (eBusy) eBusy.show();
    
    var metadataId = document.mainForm.id.value;
    var thisElement = $(dialogRequest.id);
    geocat.edit.doXLinkNewElementAjax(xlinks.length-1,metadataId,thisElement);
};

geocat.edit.doXLinkNewElementAjax = function(index, metadataId, thisElement) {
    var href = escape(xlinks[index].href);
    var pars = "&id=" + metadataId + "&ref=" + dialogRequest.ref + "&name=" + dialogRequest.name + "&href="+href;

    var myAjax = new Ajax.Request(
    getGNServiceURL(dialogRequest.action),
    {
        method: 'get',
        parameters: pars,
        onSuccess: function(req) {
            var eBusy = $('editorBusy');
            if (eBusy) eBusy.hide();
            $('editorOverlay').setStyle({display: "none"});

            if (index > 0) {
                doXLinkNewElementAjax(index - 1,metadataId,thisElement);
            }

            var html = req.responseText;
            
            var what = index === 0 ? dialogRequest.replacement : 'add';
            if (what == 'replace') {
                thisElement.replace(html);
            } else if (what == 'before') {
                thisElement.insert({
                    'before': html
                });
                setAddControls(thisElement.previous(), orElement);
            } else if (what == 'add') {
            	thisElement.insert({
            		'after': html
            	});
            	setAddControls(thisElement.next(), orElement);
            } else {
                alert("doNewElementAjax: invalid what: " + what + " should be replace or add.");
            }
            
            if (index === 0) {
                // Init map if spatial extent editing - usually bounding box or bounding polygon
                if (geocat.edit.Extent.accepts(dialogRequest.name)) {
                	if(typeof(searchTools) != "undefined")
                		searchTools.initMapDiv();
                }

                // Check elements
                validateMetadataFields();

                setBunload(true);
                // reset warning for window destroy
            }

        },
        onFailure: function(req) {
            var eBusy = $('editorBusy');
            if (eBusy) eBusy.hide();

            alert(translate("errorAddElement") + name + translate("errorFromDoc")
                  + " / status " + req.status + " text: " + req.statusText + " - " + translate("tryAgain"));
            setBunload(true);
            // reset warning for window destroy
        }
    }
    );
};
