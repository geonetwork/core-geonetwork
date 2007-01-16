//==============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.gast.gui.panels;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TSeparator;
import org.dlib.tools.Util;

//==============================================================================

public abstract class FormPanel extends JPanel implements ActionListener
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public FormPanel()
	{
		FlexLayout fl = new FlexLayout(1,6,0,0);
		fl.setColProp(0, FlexLayout.EXPAND);
		fl.setNullGaps(0,0);

		setLayout(fl);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void init(String title, String descr, List<JButton> buttons, Icon precon,
						  String preconTip)
	{
		labTitle.setText(title);
		labIcon .setIcon(precon);
		labIcon .setToolTipText(preconTip);
		txaDescr.setText("\n"+ formatLines(descr));

		JComponent innerPanel = buildInnerPanel();

		add("0,0,x", buildTitlePanel());
		add("0,1,x", buildDescrPanel());
		add("0,2,x", new TSeparator(TSeparator.HORIZONTAL));

		if (innerPanel != null)
		{
			add("0,3,x", innerPanel);
			add("0,4,x", new TSeparator(TSeparator.HORIZONTAL));
		}

		add("0,5,x", buildButtons(buttons));
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e) {}

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected abstract JComponent buildInnerPanel();

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private JComponent buildTitlePanel()
	{
		FlexLayout fl = new FlexLayout(2,1);
		fl.setColProp(0, FlexLayout.EXPAND);

		JPanel p = new JPanel();
		p.setLayout(fl);
		p.setBackground(Color.LIGHT_GRAY);
		p.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		p.add("0,0,x", labTitle);
		p.add("1,0",   labIcon);

		labTitle.setFont(new Font("Dialog", Font.BOLD, 20));

		return p;
	}

	//---------------------------------------------------------------------------

	private JComponent buildDescrPanel()
	{
		txaDescr.setEditable(false);
		txaDescr.setBackground(new JPanel().getBackground());
		txaDescr.setLineWrap(true);
		txaDescr.setWrapStyleWord(true);

		return txaDescr;
	}

	//---------------------------------------------------------------------------

	private String formatLines(String line)
	{
		line = Util.replaceStr(line, "\r\n", "\n");

		StringTokenizer st = new StringTokenizer(line.trim(), "\n");
		StringBuffer    sb = new StringBuffer();

		while (st.hasMoreTokens())
			sb.append(st.nextToken().trim() +" ");

		return sb.toString() +"\n";
	}

	//---------------------------------------------------------------------------

	private JPanel buildButtons(List<JButton> buttons)
	{
		this.buttons = buttons;

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		for (JButton button : buttons)
			panel.add(button);

		return panel;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private JLabel    labTitle = new JLabel();
	private JLabel    labIcon  = new JLabel();
	private JTextArea txaDescr = new JTextArea(1, 10);

	private List<JButton> buttons;
}

//==============================================================================

