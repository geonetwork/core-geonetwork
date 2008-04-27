//==============================================================================
//===
//===   MainFrame
//===
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

package org.fao.geonet.csw.client;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.MultiPanel;
import org.dlib.gui.TPanel;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.jdom.Element;
import javax.swing.JCheckBox;

//==============================================================================

public class MainFrame extends JFrame implements ActionListener, ItemListener
{
	private JTextField     txtHost    = new JTextField("localhost", 15);
	private JTextField     txtPort    = new JTextField("8080");
	private JTextField     txtSrvAddr = new JTextField("/geonetwork/srv/en/csw");
	private JTextField     txtLogAddr = new JTextField("/geonetwork/srv/en/xml.user.login");
	private JTextField     txtUser    = new JTextField("a");
	private JPasswordField txtPass    = new JPasswordField("a");
	private JComboBox      cmbOperat  = new JComboBox();
	private JComboBox      cmbMethod  = new JComboBox();
	private JButton        btnSend    = new JButton("Send");
	private JTextArea      txaLog     = new JTextArea(40,100);
	private JCheckBox      chbSoap    = new JCheckBox("Use SOAP");

	private MultiPanel paramsPanel= new MultiPanel();

	private GetCapabilitiesPanel getCapPanel = new GetCapabilitiesPanel();
	private DescribeRecordPanel  desRecPanel = new DescribeRecordPanel();
	private GetRecordByIdPanel   getByIdPanel= new GetRecordByIdPanel();
	private GetRecordsPanel      getRecsPanel= new GetRecordsPanel();

	private enum Operation { GetCapabilities, DescribeRecord, GetRecordById, GetRecords }

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public MainFrame()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("GeoNetwork CSW 2.0.2 test application");

		//------------------------------------------------------------------------

		btnSend.addActionListener(this);
		btnSend.setActionCommand("send");

		txaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txaLog.setEditable(false);

		for(Operation oper : Operation.values())
			cmbOperat.addItem(oper);

//		cmbOperat.addItem("DescribeRecord");
//		tcbOperat.addItem("GetDomain");
//		cmbOperat.addItem("GetRecordById");
//		cmbOperat.addItem("GetRecords");
//		tcbOperat.addItem("Transaction");
//		tcbOperat.addItem("Harvest");

		cmbMethod.addItem("GET");
		cmbMethod.addItem("POST");

		cmbOperat.addItemListener(this);

		//------------------------------------------------------------------------

		paramsPanel.add(Operation.GetCapabilities.toString(), getCapPanel);
		paramsPanel.add(Operation.DescribeRecord .toString(), desRecPanel);
		paramsPanel.add(Operation.GetRecordById  .toString(), getByIdPanel);
		paramsPanel.add(Operation.GetRecords     .toString(), getRecsPanel);

		//------------------------------------------------------------------------

		getContentPane().add(buildLeftPanel(), BorderLayout.WEST);
		getContentPane().add(buildLogPanel(),  BorderLayout.CENTER);

		pack();
		setVisible(true);
	}

	//---------------------------------------------------------------------------
	//---
	//--- ActionListener
	//---
	//---------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if (cmd.equals("send"))
			handleSend();
	}

	//---------------------------------------------------------------------------

	private void handleSend()
	{
		clearLog();
		CatalogRequest req = buildRequest();

		if (req == null)
		{
			log("Invalid operation");
			return;
		}

		req.setHost(txtHost.getText());

		String port = txtPort.getText();

		if (!port.equals(""))
			req.setPort(Integer.parseInt(port));

		req.setAddress     (txtSrvAddr.getText());
		req.setLoginAddress(txtLogAddr.getText());

		try
		{
			if (txtUser.getText().trim().length() != 0)
			{
				req.login(txtUser.getText(), txtPass.getText());
				log(req.getSentData());
				logLine();
				log(req.getReceivedData());
			}

			Element response = req.execute();
		}
		catch (Exception e)
		{
			log("Exception : "+e);
		}

		logLine();
		log(req.getSentData());
		logLine();
		log(req.getReceivedData());
	}

	//---------------------------------------------------------------------------

	private CatalogRequest buildRequest()
	{
		CatalogRequest request;

		Operation oper = (Operation) cmbOperat.getSelectedItem();

		switch(oper)
		{
			case GetCapabilities:	request = getCapPanel.createRequest();
						break;

			case DescribeRecord:		request = desRecPanel.createRequest();
						break;

//			case GetDomain:	request = getDomPanel.createRequest();
//						break;

			case GetRecordById:		request = getByIdPanel.createRequest();
						break;

			case GetRecords:			request = getRecsPanel.createRequest();
						break;

			default:	return null;
		}

		//--- set request method

		CatalogRequest.Method method = (cmbMethod.getSelectedIndex() == 0)
													? CatalogRequest.Method.GET
													: CatalogRequest.Method.POST;

		request.setMethod(method);
		request.setUseSOAP(chbSoap.isSelected());

		return request;
	}

	//---------------------------------------------------------------------------
	//---
	//--- ItemListener
	//---
	//---------------------------------------------------------------------------

	public void itemStateChanged(ItemEvent e)
	{
		paramsPanel.show(cmbOperat.getSelectedItem().toString());
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private JPanel buildLeftPanel()
	{
		JPanel p = new JPanel();

		FlexLayout flexL = new FlexLayout(1,5);
		flexL.setColProp(0, FlexLayout.EXPAND);
		flexL.setRowProp(4, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0,x",   buildServerPanel());
		p.add("0,1,x",   buildLoginPanel());
		p.add("0,2,x",   buildRequestPanel());
		p.add("0,3,x",   btnSend);
		p.add("0,4,x,x", paramsPanel);

		return p;
	}

	//---------------------------------------------------------------------------

	private JPanel buildServerPanel()
	{
		JPanel p = new TPanel("Server");

		FlexLayout flexL = new FlexLayout(2,4);
		flexL.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0", new JLabel("Host"));
		p.add("0,1", new JLabel("Port"));
		p.add("0,2", new JLabel("CSW service"));
		p.add("0,3", new JLabel("Login service"));

		p.add("1,0,x", txtHost);
		p.add("1,1,x", txtPort);
		p.add("1,2,x", txtSrvAddr);
		p.add("1,3,x", txtLogAddr);

		return p;
	}

	//---------------------------------------------------------------------------

	private JPanel buildLoginPanel()
	{
		JPanel p = new TPanel("Login");

		FlexLayout flexL = new FlexLayout(2,2);
		flexL.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0", new JLabel("Username"));
		p.add("0,1", new JLabel("Password"));

		p.add("1,0,x", txtUser);
		p.add("1,1,x", txtPass);

		return p;
	}

	//---------------------------------------------------------------------------

	private JPanel buildRequestPanel()
	{
		JPanel p = new TPanel("Request");

		FlexLayout flexL = new FlexLayout(2,3);
		flexL.setColProp(1, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0", new JLabel("Operation"));
		p.add("0,1", new JLabel("Method"));

		p.add("1,0,x", cmbOperat);
		p.add("1,1,x", cmbMethod);

		p.add("0,2,x,c,2", chbSoap);

		return p;
	}

	//---------------------------------------------------------------------------

	private JPanel buildLogPanel()
	{
		JPanel p = new TPanel("Communication log");

		FlexLayout flexL = new FlexLayout(1,1);
		flexL.setColProp(0, FlexLayout.EXPAND);
		flexL.setRowProp(0, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0,x,x", new JScrollPane(txaLog));

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Logging facility
	//---
	//---------------------------------------------------------------------------

	private void clearLog()
	{
		txaLog.setText("");
	}

	//---------------------------------------------------------------------------

	private void log(List list)
	{
		for(int i=0; i<list.size(); i++)
		{
			if (list.get(i) instanceof String)
				txaLog.append((String) list.get(i) +"\n");
			else
			{
				try
				{
					String text = new String((byte[])list.get(i), "UTF-8");
					txaLog.append(text);
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	//---------------------------------------------------------------------------

	private void log(String text)
	{
		txaLog.append(text +"\n");
	}

	//---------------------------------------------------------------------------

	private void logLine()
	{
		txaLog.append("==============================================================\n");
	}
}

//==============================================================================

