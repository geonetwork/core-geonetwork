
var editI18n = {
    
    init: function(key, defaultLang, textAreaChangeCallback/*may be null*/){
    	if(defaultLang === 'ger') {
    		defaultLang = 'deu';
    	}
        var code = key+defaultLang.substring(0,2).toUpperCase();
        editI18n.showDesc(code);
        
        Ext.get('option'+code).dom.selected=true;

        var choice = Ext.get("langSelector"+key);
        choice.on('change',  function(ev){
            var option = choice.dom.options[choice.dom.selectedIndex];
            editI18n.showDesc(option.id.substring(6));
        } );

        if(textAreaChangeCallback){
            var desc = Ext.query("input."+key);
            for( var i=0; i < desc.length; i++){
                Ext.get(desc[i]).on('keyup', textAreaChangeCallback);
            }
        }
    },
    
    showDesc: function(code){
        var key = code.substring(0,code.length-2); 
        var desc = Ext.query("input."+key);
        for( var i=0; i < desc.length; i++){
            if(desc[i].id == code){
                desc[i].style.display ='block';
                desc[i].focus()
            }else{ 
                desc[i].style.display ='none';
            }
        }
    }
    
}