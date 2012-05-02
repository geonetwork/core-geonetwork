Ext.namespace("GeoNetwork");
Ext.namespace("UserXsl");

GeoNetwork.UserXsl = Ext.extend(Ext.Panel,
    {
      store : undefined,
      view : undefined,
      uploadForm : undefined,
      tb: undefined,
      height : 500,
      autoWidth : true,
      layout : 'border',
      /**
       * private: method[initComponent] Initializes the user xsl view
       */
      initComponent : function(renderTo) {
        this.renderTo = renderTo;
        
        var formats = Ext.data.Record.create([ {
          name : 'name',
          mapping : ''
        } ]);
        var lp = this;
        this.store = new Ext.data.Store({
          autoDestroy : true,
          proxy : new Ext.data.HttpProxy({
            method : 'GET',
            url : 'metadata.formatter.list',
            disableCaching : false
          }),
          reader : new Ext.data.XmlReader({
            record : 'formatter',
            id : 'formatter'
          }, formats),
          fields : [ 'name' ]
        });
        this.tb = new Ext.Toolbar({
          disabled : true,
          items : [{
                xtype: 'tbtext',
                text : 'Selected stylesheet:'
            }, {
	            xtype : 'button',
	            text : 'Edit',
	            listeners : {
	              click : function() {
	                this.edit();
	              },
	              scope : lp
	            }
            }, {
	            xtype : 'button',
	            text : 'Delete',
	            listeners : {
	              click : function() {
	                this.removeSelectedFormat();
	              },
	              scope : lp
	            }
            }, {
              xtype : 'button',
              text : 'Download',
              listeners : {
                click : function() {
                  this.download();
                },
                scope : lp
              }
          }]
        });
         var tpl = new Ext.XTemplate(
            '<tpl for="."><div class="user-xsl-wrap"><div id="{name}" class="user-xsl">',
            '<span>{name}</span></div></div>',
            '</tpl>');
        
        this.view = new Ext.DataView({
          store : this.store,
          tpl : tpl,
          singleSelect : true,
          selectedClass : 'user-xsl-selected',
          overClass : 'user-xsl-over',
          itemSelector : 'div.user-xsl-wrap',
          autoScroll : true,
          listeners : {
        	dblclick: function(){
              this.edit();
            },
            selectionchange : function() {
              var selection = this.view.getSelectedIndexes();
              if (selection.length > 0) {
                this.tb.enable();
              } else {
                this.tb.disable();
              }
            },
            scope : lp
          }
        });
        

        this.items = [ new Ext.Panel({
          title : 'Metadata Formatting StyleSheets',
          region : 'center',
          bbar : this.tb,
          split : true,
          border : true,
          items : [ this.view ]
        }), this.getUploadForm() ];

        this.store.load();

        GeoNetwork.UserXsl.superclass.initComponent.call(this);
      },
      onDestroy : function() {
        GeoNetwork.UserXsl.superclass.onDestroy.apply(
            this, arguments);
      },
      download: function() {
          var lp = this;
          var selection = this.view.getSelectedIndexes();
          var record = this.view.getStore().getAt(selection[0]);
          var name = record.get('name');
          window.open('metadata.formatter.download?id=' + escape(name),'_download_xsl','');
      },
      filesPanel: null,
      edit: function() {
          var lp = this;
          var selection = this.view.getSelectedIndexes();
          var record = this.view.getStore().getAt(selection[0]);
          var name = record.get('name');
          if (GeoNetwork.UserXsl.filesPanel != null) {
        	  GeoNetwork.UserXsl.filesPanel.destroy();
        	  GeoNetwork.UserXsl.filesPanel = null;
          }
          
    	  GeoNetwork.UserXsl.filesPanel = new Ext.Window({
              title: 'Formatter Files',
              layout: 'border',
      	      width: 500,
              height: 500,
              items: [{
                  xtype: 'treepanel',
                  id: 'tree-panel',
                  region: 'center',
                  margins: '2 2 0 2',
                  autoScroll: true,
      	          rootVisible: false,
      	          root: new Ext.tree.AsyncTreeNode(),
                  loader: new Ext.ux.tree.XmlTreeLoader({
                    dataUrl: Env.locService+"/metadata.formatter.files?id="+escape(name)
                  }),
      	        listeners: {
      	            'dblclick': function(tp){
      	            	var path = tp.attributes.path;
      	            	var path = tp.attributes.path;
      	            	window.open('metadata.formatter.edit?fname='+escape(path)+'&id='+escape(name),'_edit_xsl','');
      	            }
      	        }
              }]
          });
    	  GeoNetwork.UserXsl.filesPanel.show();
      },
      removeSelectedFormat: function() {
        var lp = this;
        var selection = this.view.getSelectedIndexes();
        var record = this.view.getStore().getAt(selection[0]);
        var name = record.get('name');
        Ext.Ajax.request({
          method:'GET',
          url : 'metadata.formatter.remove?id=' + escape(name),
          success : function(form, action) {
            lp.store.reload();
          },
          failure : function(response) {
            Ext.Msg.alert('Error', response.responseText);
          }
        });
      },
      getUploadForm : function() {
        var the = this;
        this.uploadForm = new Ext.FormPanel(
            {
              title : 'Upload StyleSheet',
              fileUpload : true,
              region : 'west',
              minWidth : 250,
              width : 250,
              split : true,
			  errorReader: new Ext.data.XmlReader({
		          record : 'result',
		          success: '@id'
		      }, [
		          'id'
		      ]),
              items : [{
                xtype : 'fileuploadfield',
                id : 'file',
                allowBlank : false,
                emptyText : 'Select Stylesheet for upload',
                hideLabel : true,
                name : 'fname'
              }],
              buttons : [ {
                text : translate('upload'),
                scope : the,
                handler : function() {
                  if (this.uploadForm.getForm().isValid()) {
                    this.uploadForm.getForm().submit({
                      url : 'metadata.formatter.register',
                      scope : this,
                      success : function(response) {
                        this.store.reload();
                      },
                      failure : function(response) {
                        Ext.Msg .alert('Error',response.responseText);
                      }
                    });
                  }
                }
              }]
            });
        return this.uploadForm;
      }
});

Ext.reg('gn_userxslmanagerpanel', GeoNetwork.UserXsl);
