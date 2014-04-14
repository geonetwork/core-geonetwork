Ext.onReady(function(){

    // create the Data Store
    var store = new Ext.data.JsonStore({
        root: 'entry',
        totalProperty: 'totalCount',
        idProperty: 'threadid',
        remoteSort: true,
        fields: [
            'date', 'user', 'ip', 'action', 'subject', 'id', 'title'
        ],

        proxy: new Ext.data.HttpProxy({
            url: 'versioning.logdata@json'
        })
    });

    var pagingBar = new Ext.PagingToolbar({
        pageSize: 30,
        store: store,
        displayInfo: true,
        displayMsg: 'Showing metadata actions {0} - {1} from {2}',
        emptyMsg: "No metadata actions to show",
        id: "pagingToolbar",
        onClick : function(which){
              var store = this.store;
              switch(which){
                  case "first":
                      this.doLoad(0);
                  break;
                  case "prev":
                      this.doLoad(Math.max(0, this.cursor-this.pageSize));
                  break;
                  case "next":
                      this.doLoad(this.cursor+this.pageSize);
                  break;
                  case "last":
                      var total = store.getTotalCount();
                      var extra = total % this.pageSize;
                      var lastStart = extra ? (total - extra) : total-this.pageSize;
                      this.doLoad(lastStart);
                  break;
                  case "refresh":
                      this.doRefresh(this.cursor);
                  break;
              }
          },
          doLoad : function(start){
              var o = {}, pn = this.paramNames;
              o[pn.start] = start;
              o[pn.limit] = this.pageSize;
              o['refresh'] = 'false';
              if(this.fireEvent('beforechange', this, o) !== false){
                  this.store.load({params:o});
              }
          },
          doRefresh : function(start){
              var o = {}, pn = this.paramNames;
              o[pn.start] = start;
              o[pn.limit] = this.pageSize;
              if(this.fireEvent('beforechange', this, o) !== false){
                  this.store.load({params:o});
              }
          }
    });

    var grid = new Ext.grid.GridPanel({
        el:'topic-grid',
        width:900,
        height:720,
        title:'Metadata actions',
        store: store,
        trackMouseOver:false,
        disableSelection:true,
        loadMask: true,

        // grid columns
        columns:[{
             header: "Date and hour (BRT)",
             dataIndex: 'date',
             width: 115,
             sortable: true
         },{
            header: "User",
            dataIndex: 'user',
            width: 70,
            sortable: true
        },{
            header: "IP",
            dataIndex: 'ip',
            width: 90,
            sortable: true
        },{
              header: "Action",
              dataIndex: 'action',
              width: 70,
              sortable: true
          },{
              header: "Subject",
              dataIndex: 'subject',
              width: 80,
              sortable: true
          },{
              header: "ID",
              dataIndex: 'id',
              width: 30,
              sortable: true
          },{
             header: "Title",
             dataIndex: 'title',
             width: 390,
             sortable: true
                     }],
        // paging bar on the bottom
        bbar: pagingBar
    });

    // render it
    grid.render();

    // trigger the data store load
    store.load({params:{start:0, limit:30}});
});

Ext.ux.SliderTip = Ext.extend(Ext.Tip, {
    minWidth: 10,
    offsets : [0, -10],
    init : function(slider){
        slider.on('dragstart', this.onSlide, this);
        slider.on('drag', this.onSlide, this);
        slider.on('dragend', this.hide, this);
        slider.on('destroy', this.destroy, this);
    },

    onSlide : function(slider){
        this.show();
        this.body.update(this.getText(slider));
        this.doAutoWidth();
        this.el.alignTo(slider.thumb, 'b-t?', this.offsets);
    },

    getText : function(slider){
        return slider.getValue();
    }
});
