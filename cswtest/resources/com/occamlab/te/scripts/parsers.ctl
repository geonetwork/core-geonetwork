<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
 <!-- Global parsers, always included in the source when compiling a test suite -->
<ctl:package 
 xmlns:parsers="http://www.occamlab.com/te/parsers"
 xmlns:ctl="http://www.occamlab.com/ctl">
 
	<ctl:parser name="parsers:CDataParser">
		<ctl:java class="com.occamlab.te.parsers.CDataParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:HTTPParser">
		<ctl:java class="com.occamlab.te.parsers.HTTPParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:ImageParser">
		<ctl:java class="com.occamlab.te.parsers.ImageParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:NullParser">
		<ctl:java class="com.occamlab.te.parsers.NullParser" method="parse"/>
	</ctl:parser>

	<ctl:parser name="parsers:ZipParser">
		<ctl:java class="com.occamlab.te.parsers.ZipParser" method="parse"/>
	</ctl:parser>
	
	<ctl:parser name="parsers:XMLValidatingParser">
		<ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true"/>
	</ctl:parser>

	<ctl:parser name="parsers:SchematronValidatingParser">
		<ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" method="parse" initialized="true"/>
	</ctl:parser>
	
</ctl:package>
