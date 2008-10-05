//==============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.gast.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import jeeves.utils.Xml;
import org.fao.gast.gui.panels.FormPanel;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;

//==============================================================================

public class GuiBuilder
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GuiBuilder(String appPath, ViewPanel view, WorkPanel work)
	{
		this.appPath = appPath;

		viewPanel = view;
		workPanel = work;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void build(String guiFile, Locale locale) throws Exception 
	{
		Element root = Xml.loadFile(appPath + File.separator + guiFile);

		ResourceBundle resourceBundle = lookupResourceBundle(appPath
				+ File.separator + guiFile, locale);

		localize(root, resourceBundle);

		packag = root.getChild("class").getAttributeValue("package");

		for (Object precon : root.getChildren("precon"))
			addPrecon((Element) precon);

		for (Object cont : root.getChildren("container"))
			addContainer((Element) cont);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private ResourceBundle lookupResourceBundle(String guiFile, Locale locale) throws IOException 
	{

		String[] parts = { locale.getLanguage(), locale.getCountry(),
				locale.getVariant() };
		String baseString = guiFile;
		if (guiFile.indexOf('.') > 0) {
			baseString = guiFile.substring(0, guiFile.lastIndexOf("."));
		}

		for (int i = parts.length; i >= 0; i--) {
			ResourceBundle bundle = locateResourceBundleFile(baseString, parts,
					i);
			if (bundle != null) {
				return bundle;
			}
		}
		throw new IllegalStateException("Unable to find gui.xml file");
	}

	public ResourceBundle locateResourceBundleFile(String appPath,
			String[] args, int toUse) throws IOException 
	{
		StringBuilder builder = new StringBuilder(appPath);

		for (int i = 0; i < toUse; i++) {
			if (args[i] != null) {
				builder.append('_');
				builder.append(args[i]);
			}
		}

		builder.append(".properties");
		if (new java.io.File(builder.toString()).exists()) {
			return new PropertyResourceBundle(
					new FileInputStream(builder.toString()));
		}
		return null;
	}
	
	private void localize(Element elem, ResourceBundle resourceBundle) 
	{
		List attributes = elem.getAttributes();
		for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
			Attribute attribute = (Attribute) iterator.next();
			attribute.setValue(localize(attribute.getValue(), resourceBundle));
		}
		if (elem.getText() != null && elem.getTextTrim().length() > 0)
			elem.setText(localize(elem.getText(), resourceBundle));

		List children = elem.getChildren();
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Content child = (Content) iterator.next();
			if (child instanceof Element) {
				localize((Element) child, resourceBundle);
			}
		}
	}

	private String localize(String string, ResourceBundle resourceBundle) 
	{
		if (string == null) {
			return null;
		}
		String localized = string;
		while (true) {
			Matcher matcher = LOCALIZATION_KEY.matcher(localized);

			if (!matcher.find()) {
				return localized;
			}

			String group = matcher.group(1);
			String replacement = resourceBundle.getString(group);
			localized = localized.replaceAll("\\$\\{" + group + "\\}",
					replacement);
		}
	}

	private void addPrecon(Element precon)
	{
		Precon p = new Precon();

		p.type = precon.getAttributeValue("type");
		p.image= retrieveImage(precon.getChildText("image"));
		p.tip  = precon.getChildText("tip");

		hmPrecons.put(p.type, p);
	}

	//---------------------------------------------------------------------------

	private void addContainer(Element cont) throws Exception
	{
		String image = cont.getChildText("image");
		String label = cont.getChildText("label");

		Object node = viewPanel.addContainer(label, retrieveImage(image));

		for (Object form : cont.getChildren("form"))
			addForm(node, (Element) form);
	}

	//---------------------------------------------------------------------------

	private void addForm(Object cont, Element form) throws Exception
	{
		String id    = form.getChildText("id");
		String image = form.getChildText("image");
		String label = form.getChildText("label");
		String title = form.getChildText("title");
		String clazz = form.getChildText("class");
		String descr = form.getChildText("description");
		
		String param = form.getChildText("param");

		FormPanel     formPanel= buildForm(clazz, param);
		System.out.println(" Form Panel : " + id + " type is : " + formPanel.getClass().getName());
		
		List<JButton> buttons  = buildButtons(form.getChildren("button"), formPanel);
		Precon        precon   = getPrecon(form.getChild("precon"));

		formPanel.init(title, descr, buttons, precon.image, precon.tip);
		workPanel.add(id, formPanel);
		viewPanel.addForm(cont, id, label, retrieveImage(image));
	}

	//---------------------------------------------------------------------------

	private FormPanel buildForm(String className, String param) throws Exception
	{
		Class clazz = Class.forName(packag +"."+className);
		
		FormPanel fp = null;
		
		if (param != null)
			fp = ((Constructor<FormPanel>) clazz.getConstructor(String.class)).newInstance(param);
		else 
			fp = (FormPanel) clazz.newInstance();

		return fp; 
	}

	//---------------------------------------------------------------------------

	private List<JButton> buildButtons(List buttons, FormPanel form)
	{
		ArrayList<JButton> al = new ArrayList<JButton>();

		for (Object button : buttons)
			al.add(buildButton((Element) button, form));

		return al;
	}

	//---------------------------------------------------------------------------

	private JButton buildButton(Element button, FormPanel form)
	{
		String image = button.getChildText("image");
		String label = button.getChildText("label");
		String action= button.getChildText("action");

		JButton btn = new JButton(label, retrieveImage(image));
		btn.setActionCommand(action);
		btn.addActionListener(form);

		return btn;
	}

	//---------------------------------------------------------------------------

	private Precon getPrecon(Element precon)
	{
		String type = precon.getAttributeValue("type");
		Precon p    = hmPrecons.get(type);

		return p;
	}

	//---------------------------------------------------------------------------

	private Icon retrieveImage(String image)
	{
		if (image == null)
			return null;

		if (hmImages.containsKey(image))
			return hmImages.get(image);

		ImageIcon icon = new ImageIcon(appPath +"/gast/images/" +image);
		hmImages.put(image, icon);

		return icon;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String appPath;
	private String packag;

	private ViewPanel viewPanel;
	private WorkPanel workPanel;

	private HashMap<String, Icon>   hmImages  = new HashMap<String, Icon>();
	private HashMap<String, Precon> hmPrecons = new HashMap<String, Precon>();

	//--------------------------------------------------------------------------
	// -
	// ---
	// --- Constants
	// ---
	//--------------------------------------------------------------------------
	// -

	private static final Pattern LOCALIZATION_KEY = Pattern
			.compile("\\$\\{([\\S&&[^\\$\\{\\}]]+)\\}");
}

//==============================================================================

class Precon
{
	public String type;
	public Icon   image;
	public String tip;
}

//==============================================================================


