//==============================================================================

package org.fao.gast.gui.panels.migration.oldinst;

import java.awt.Dialog;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.dlib.gui.FlexLayout;

//==============================================================================

public class UserDialog extends JDialog implements ActionListener
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public UserDialog(Dialog d, List<Owner> candid, List<Owner> admins, List<Owner> groups)
	{
		super(d, "Choose user", true);

		this.candid = candid;

		for (Owner o : admins)
			this.admins.add(new Entity(o.user, o.name));

		for (Owner o : groups)
			this.groups.add(new Entity(o.group, o.groupName));

		selAdmin = this.admins.get(0);
		selGroup = this.groups.get(0);

		Panel p = new Panel();

		FlexLayout fl = new FlexLayout(2,6);
		fl.setColProp(1, FlexLayout.EXPAND);
		fl.setRowProp(1, FlexLayout.EXPAND);

		p.setLayout(fl);
		p.add("0,0,x,c,2", new JLabel("Candidates found"));
		p.add("0,1,x,x,2", new JScrollPane(jlCandid));
		p.add("0,2,x,c,2", chUseAdm);
		p.add("0,3",       new JLabel("User"));
		p.add("1,3,x",     cbAdmins);
		p.add("0,4",       new JLabel("Group"));
		p.add("1,4,x",     cbGroups);
		p.add("1,5,c",     btOk);

		btOk.addActionListener(this);

		getContentPane().add(p);

		jlCandid.setModel(candidModel);
		cbAdmins.setModel(adminsModel);
		cbGroups.setModel(groupsModel);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Owner run()
	{
		pack();
		setSize(400, 400);
		setVisible(true);

		if (chUseAdm.isSelected())
			return new Owner(selAdmin.id, selGroup.id);

		return (Owner) jlCandid.getSelectedValue();
	}

	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		setVisible(false);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private List<Owner>  candid;
	private List<Entity> admins = new ArrayList<Entity>();
	private List<Entity> groups = new ArrayList<Entity>();

	//---------------------------------------------------------------------------

	private ListModel candidModel = new ListModel()
	{
		public int getSize() { return candid.size(); }

		public Object getElementAt(int index) { return candid.get(index); }

		public void addListDataListener(ListDataListener l)	{}
		public void removeListDataListener(ListDataListener l){}
	};

	//---------------------------------------------------------------------------

	private ComboBoxModel adminsModel = new ComboBoxModel()
	{

		public void setSelectedItem(Object anItem)
		{
			selAdmin = (Entity) anItem;
		}

		public Object getSelectedItem() { return selAdmin; }

		public int getSize() { return admins.size(); }

		public Object getElementAt(int index) { return admins.get(index); }

		public void addListDataListener(ListDataListener l)	{}
		public void removeListDataListener(ListDataListener l){}
	};

	//---------------------------------------------------------------------------

	private ComboBoxModel groupsModel = new ComboBoxModel()
	{

		public void setSelectedItem(Object anItem)
		{
			selGroup = (Entity) anItem;
		}

		public Object getSelectedItem() { return selGroup; }

		public int getSize() { return groups.size(); }

		public Object getElementAt(int index) { return groups.get(index); }

		public void addListDataListener(ListDataListener l)	{}
		public void removeListDataListener(ListDataListener l){}
	};

	//---------------------------------------------------------------------------

	private Entity selAdmin;
	private Entity selGroup;

	private JList     jlCandid = new JList();
	private JComboBox cbAdmins = new JComboBox();
	private JComboBox cbGroups = new JComboBox();
	private JCheckBox chUseAdm = new JCheckBox("Use one of the following user/group");
	private JButton   btOk     = new JButton("Ok");
}

//==============================================================================

class Entity
{
	public String id;
	public String name;

	public Entity(String id, String name)
	{
		this.id   = id;
		this.name = name;
	}

	public String toString() { return name; }
}

//==============================================================================

