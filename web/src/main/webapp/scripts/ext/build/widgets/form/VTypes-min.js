/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.VTypes=function(){var alpha=/^[a-zA-Z_]+$/;var alphanum=/^[a-zA-Z0-9_]+$/;var email=/^(\w+)([-+.][\w]+)*@(\w[-\w]*\.){1,5}([A-Za-z]){2,4}$/;var url=/(((https?)|(ftp)):\/\/([\-\w]+\.)+\w{2,3}(\/[%\-\w]+(\.\w{2,})?)*(([\w\-\.\?\\\/+@&#;`~=%!]*)(\.\w{2,})?)*\/?)/i;return{'email':function(v){return email.test(v);},'emailText':'This field should be an e-mail address in the format "user@domain.com"','emailMask':/[a-z0-9_\.\-@]/i,'url':function(v){return url.test(v);},'urlText':'This field should be a URL in the format "http:/'+'/www.domain.com"','alpha':function(v){return alpha.test(v);},'alphaText':'This field should only contain letters and _','alphaMask':/[a-z_]/i,'alphanum':function(v){return alphanum.test(v);},'alphanumText':'This field should only contain letters, numbers and _','alphanumMask':/[a-z0-9_]/i};}();