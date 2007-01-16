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

package org.fao.gast.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.treeview.TreeView;
import org.dlib.gui.treeview.TreeViewNode;
import org.dlib.gui.treeview.TreeViewSelEvent;
import org.dlib.gui.treeview.TreeViewSelListener;

//==============================================================================

public class ViewPanel extends JPanel
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ViewPanel()
	{
		FlexLayout flexL = new FlexLayout(1,1);
		flexL.setColProp(0, FlexLayout.EXPAND);
		flexL.setRowProp(0, FlexLayout.EXPAND);
		setLayout(flexL);
		setMinimumSize(new Dimension(100, 50));

		tree.addSelectionListener(selList);
		tree.setCellRenderer(renderer);
		tree.setRootVisible(false);
		tree.setShowRootHandles(false);

		add("0,0,x,x", tree);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Object addContainer(String label, Icon icon)
	{
		label = "<html><b>"+ label +"</b>";

		TreeViewNode node = new TreeViewNode(label);

		tree.getRootNode().addChild(node);
		hmNodeImages.put(node, icon);

		return node;
	}

	//---------------------------------------------------------------------------

	public void addForm(Object container, String id, String label, Icon icon)
	{
		TreeViewNode node = new TreeViewNode(label);
		node.setUserData(id);

		((TreeViewNode) container).addChild(node);

		hmNodeImages.put(node, icon);

		tree.setRootNode(tree.getRootNode());
		tree.getRootNode().expand(true, 3);
	}

	//---------------------------------------------------------------------------

	public void setActionListener(ActionListener al)
	{
		this.al = al;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private TreeView tree = new TreeView(false);

	private ActionListener al;

	private HashMap<TreeViewNode, Icon> hmNodeImages = new HashMap<TreeViewNode, Icon>();

	//---------------------------------------------------------------------------

	private TreeViewSelListener selList = new TreeViewSelListener()
	{
		public void nodeSelected(TreeViewSelEvent e)
		{
			TreeViewNode node = e.getSelectedNode();

			String id = (node == null)
								? null
								: (String) node.getUserData();

			al.actionPerformed(new ActionEvent(tree, 0, id));
		}
	};

	//---------------------------------------------------------------------------

	private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
	{
		public Component getTreeCellRendererComponent(JTree tree, Object value,
									boolean sel, boolean exp, boolean leaf,
									int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
			setIcon(hmNodeImages.get(value));

			return this;
		}
	};
}

//==============================================================================


