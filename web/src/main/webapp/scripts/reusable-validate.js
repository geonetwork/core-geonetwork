var locUrl;
var editWin;
var msgWin;
var msgWinSubmit;
var grid;
var rejectBtnDefaultTxt;
var dataStore;
var metadataWindow;

function get(prefix, id) {
    var div = Ext.get(prefix + id);
    if (div == null) {
        div = Ext.get(prefix + "contacts");
    }
    return div;
}

function show(id, buttonTxt) {
    get("nav_", id).radioClass("blue-content");
    Ext.get('validate').show();
    Ext.get('edit').show();
    Ext.get('reject').update(rejectBtnDefaultTxt);
    dataStore.proxy.conn.url = locUrl + '/reusable.non_validated.list?type=' + id;
    dataStore.load();

    if (id == 'deleted') {
        Ext.get('validate').hide();
        Ext.get('edit').hide();
        Ext.get('reject').update(buttonTxt);
    }
}

function showDeletePage(buttonTxt) {
    show('deleted', buttonTxt);
}

function editWindows(url) {
    var localPrefix = "local://";
    if (url.indexOf(localPrefix) == 0) {
        url = Env.host + Env.locService + '/' + url.substring(localPrefix.length);
    }
    editWin = window.open(url,"_reusableObjectsEdit");
}

function checked() {
    var results = "";
    grid.getSelectionModel().each(function (record) {
        if (results.length > 0) results += ','
        results += record.data.id;

    });
    return results;
}

function reject(button, submitLabel, cancelLabel) {
    var numChecked = checked();

    if (numChecked == 0) {
        return;
    }
    if (currentPage() == 'deleted') {
        var box = Ext.MessageBox.confirm("Delete Object?", "Are you sure you want to delete the selected objects?", function (choice) {
            if (choice == 'yes') {
                performOperation("reusable.delete");
            }
        });
    } else {
        if (msgWin == null) {
            msgWin = new Ext.Window({
                applyTo: 'msg_win',
                layout: 'fit',
                width: 500,
                modal: true,
                height: 300,
                closeAction: 'hide',
                plain: true,
                items: new Ext.Panel({
                    applyTo: 'msg-panel',
                    deferredRender: false,
                    border: false
                }),

                buttons: [{
                    text: submitLabel,
                    handler: function () {
                        msgWin.hide();
                        var p = Ext.get('reusable_msg');
                        var msg = p.getValue(false);
                        performOperation("reusable.reject", msg);
                    }
                }, {
                    text: cancelLabel,
                    handler: function () {
                        msgWin.hide();
                    }
                }]
            });
        }

        msgWin.show(Ext.get(button));
    }
}

function currentPage() {
    return Ext.query("li.blue-content").first().id.substring(4);

}

function performOperation(operation, msg, noSelection) {
    noSelection = noSelection === undefined ? false : noSelection;
    var params = [];
    params.id = checked();
    if (params.id.length == 0 && !noSelection) return;
    var page = currentPage();
    params.type = page;

    if (msg != null) {
        params.msg = msg;
    }

    var box = Ext.MessageBox.wait("Operation", "Please wait...", "Running...");

    Ext.Ajax.request({
        url: locUrl + "/" + operation,
        method: 'GET',
        params: params,
        timeout: 60000,
        success: function (response, request) {
            box.hide();
            load(locUrl + "/reusable.non_validated.admin?page=" + page);
        },
        failure: function (request) {
            box.hide();
        }
    });
}

function validate() {
    performOperation("reusable.validate");
}

function replaceDuplicates() {
    performOperation("reusable.duplicates.find", "", true);
}

function edit() {
    var sm = grid.getSelectionModel();
    var selected = sm.getSelected();
    sm.selectRecords([selected]);
    editWindows(selected.data.url);

}

Ext.onReady(function () {
    Ext.get("banner-img2").remove();
    Ext.QuickTips.init();
    grid = createGrid();
    createContainer(Ext.get('grid-panel'), grid);
    pageInit();
});

function createContainer(target, grid) {
    var embeddedColumns = new Ext.Container({
        autoEl: {},
        layout: 'column',
        renderTo: target,
        defaults: {
            xtype: 'container',
            autoEl: {},
            layout: 'fit',
            columnWidth: 1,
            style: {
                padding: '10px'
            }
        },
        items: [grid]
    });

}

function createGrid() {

    var xg = Ext.grid;

    dataStore = new Ext.data.Store({
        reader: new Ext.data.XmlReader({
            record: 'record',
            id: 'id'
        }, ['id', 'url', 'desc', 'date']),
        url: locUrl + '/reusable.non_validated.list?type=contacts',
        sortInfo: {
            field: 'desc',
            direction: 'ASC'
        }
    });
    
    var cbxSm = new xg.CheckboxSelectionModel();
    
    var expander = new xg.RowExpander({
        remoteDataMethod: loadReferencingMetadata
    });

    var grid = new xg.GridPanel({
        ds: dataStore,
        cm: new xg.ColumnModel([
            cbxSm, 
            expander,
            {
                id: 'id',
                header: "Id",
                width: 50,
                fixed: true,
                sortable: true,
                dataIndex: 'id'
            }, {
                id: 'date',
                header: "Date",
                width: 150,
                fixed: true,
                sortable: true,
                dataIndex: 'date'
            }, {
                id: 'desc',
                header: "Data",
                width: 600,
                sortable: true,
                dataIndex: 'desc'
            }
        ]),
        viewConfig: {
            forceFit: true
        },
        selModel: cbxSm,
        autoHeight: true,
        plugins: [expander],
        hideHeaders: true,
        stripeRows: true,
        loadMask: true,
        frame: false,
        border: true
    });

    return grid;
}


function loadReferencingMetadata(record, body) {
    var rowHeight = Ext.get(grid.view.getRow(dataStore.indexOf(record))).getHeight();

    var store = new Ext.data.Store({
        url: locUrl + '/reusable.references?id=' + record.data['id'] + '&type=' + currentPage(),
        reader: new Ext.data.XmlReader({
            record: 'record',
            id: 'id'
        }, ['id', 'title', 'name', 'email'])
    });

    var innerGrid = new Ext.grid.GridPanel({
        store: store,
        columns: [{
            header: "id",
            width: 50,
            dataIndex: 'id',
            sortable: true
        }, {
            header: "title",
            width: 500,
            fixed: true,
            dataIndex: 'title',
            sortable: true
        }, {
            header: "name",
            width: 120,
            fixed: true,
            dataIndex: 'name',
            sortable: true
        }, {
            header: "email",
            width: 120,
            fixed: true,
            dataIndex: 'email',
            sortable: true
        }],
        viewConfig: {
            forceFit: true
        },
        autoExpandColumn: 'title',
        renderTo: body,
        disableSelection: false,
        width: grid.getInnerWidth(),
        height: rowHeight,
        hideHeaders: true,
        loadMask: true
    });

    store.on('load', function (store, records, option) {
        calculatedHeight = records.length * rowHeight;

        if (calculatedHeight < rowHeight) {
            calculatedHeight = rowHeight;
        }

        innerGrid.setHeight(calculatedHeight);
    });

    innerGrid.on("rowdblclick", function (grid, row, event) {
        event.stopPropagation();
        showMetadata(grid.view.getRow(row), store.getAt(row));
    });
    innerGrid.on("mouseover", function (event, obj) {
        event.stopPropagation();
        grid.suspendEvents();
    });
    innerGrid.on("mouseout", function (event, obj) {
        event.stopPropagation();
        grid.resumeEvents();
    });


    store.load();
};

function showMetadata(row, record) {

    Ext.MessageBox.show({
        title: record.data.title,
        progressText: 'Loading...',
        width: 300,
        wait: true,
        waitConfig: {
            interval: 200
        },
        animEl: 'mb7'
    });

    Ext.Ajax.request({
        url: locUrl + "/metadata.show.embedded",
        params: {
            id: record.data.id,
            currTab: 'simple'
        },
        success: function (response, request) {
            Ext.MessageBox.hide();
            if (!metadataWindow) {
                metadataWindow = new Ext.Window({
                    title: record.data.title,
                    html: response.responseText,
                    bodyStyle: 'overflow-y:auto; width: 90%',
                    constrain: true,
                    maximizable: true,
                    width: 600,
                    height: 500,
                    onEsc: function () {
                        metadataWindow = null;
                        metadataWindow.hide();
                    },
                    listeners: {
                        close: function () {
                            metadataWindow = null;
                        },
                        show: function () {
                            searchTools.initMapDiv();
                        }
                    }
                });
            }
            metadataWindow.show(row);
        }
    });
}