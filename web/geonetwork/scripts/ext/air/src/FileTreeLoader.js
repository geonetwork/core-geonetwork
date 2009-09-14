/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.tree.LocalTreeLoader = Ext.extend(Ext.tree.TreeLoader, {
    requestData : function(node, callback){
        if(this.fireEvent("beforeload", this, node, callback) !== false){
            var p = Ext.urlDecode(this.getParams(node));
            var response = this.dataFn(node);
            this.processResponse(response, node, callback);
            this.fireEvent("load", this, node, response);			
        }else{
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if(typeof callback == "function"){
                callback();
            }
        }
    },	
    processResponse : function(o, node, callback){
        try {
            node.beginUpdate();
            for(var i = 0, len = o.length; i < len; i++){
                var n = this.createNode(o[i]);
                if(n){
                    node.appendChild(n);
                }
            }
            node.endUpdate();
            if(typeof callback == "function"){
                callback(this, node);
            }
        }catch(e){
            this.handleFailure(response);
        }
    },
    load : function(node, callback){
        if(this.clearOnLoad){
            while(node.firstChild){
                node.removeChild(node.firstChild);
            }
        }
        if(this.doPreload(node)){ // preloaded json children
            if(typeof callback == "function"){
                callback();
            }
        }else if(this.dataFn||this.fn){
            this.requestData(node, callback);
        }
    }		
});

/**
 * @cfg {air.File} directory
 * Initial directory to load the FileTree from
 */
Ext.air.FileTreeLoader = Ext.extend(Ext.tree.LocalTreeLoader, {
    extensionFilter: false,
    dataFn: function(currNode) {
        var currDir;
        if (currNode.attributes.url) {
                currDir = this.directory.resolvePath(currNode.attributes.url);
        } else {
                currDir = this.directory;
        }
        var files = []; 
        var c = currDir.getDirectoryListing();
        for (i = 0; i < c.length; i++) {
            if (c[i].isDirectory || this.extensionFilter === false || this.extensionFilter === c[i].extension)
            files.push({
                text: c[i].name,
                url: c[i].url,
                extension: c[i].extension,
                leaf: !c[i].isDirectory
            });
        }
        return files;			
    }
});