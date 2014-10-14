(function(win,doc){  // EUROPA 2013 - HIGH CONTRAST 1.0

	win.contrast ={
		init:function(){var t=document.getElementById("additional-tools");if(t){contrast.setStyle(_$.getCook("altcolor"))}},
		setStyle:function(cls){var b=document.getElementsByTagName('body'),c=(b[0])?b[0].className:"";if(contrast.prevAltCSS){if(b[0]){b[0].className=b[0].className.replace(new RegExp('(\\s|^)'+contrast.prevAltCSS+'(\\s|$)'),'');}}contrast.prevAltCSS=cls;if(b[0]&&cls!="default"&&cls!="undefined"&&cls!=null&&cls!=""){b[0].className +=" "+cls;}if(cls!="undefined"&&cls!=null&&cls!=""){_$.setCook("altcolor",cls);}}
	}
	_$.ready(contrast.init);
	
}(window,document));