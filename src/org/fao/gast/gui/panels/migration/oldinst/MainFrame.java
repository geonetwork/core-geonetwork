//==============================================================================

package org.fao.gast.gui.panels.migration.oldinst;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.dlib.gui.FlexLayout;
import org.dlib.gui.TPanel;
import org.fao.geonet.apps.common.SimpleLogger;

//==============================================================================

public class MainFrame extends JFrame implements SimpleLogger
{
	private JTextArea  txaLog     = new JTextArea(30,40);

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public MainFrame()
	{
		txaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txaLog.setEditable(false);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private JPanel buildLogPanel()
	{
		JPanel p = new TPanel("Migration log");

		FlexLayout flexL = new FlexLayout(1,2);
		flexL.setColProp(0, FlexLayout.EXPAND);
		flexL.setRowProp(0, FlexLayout.EXPAND);
		p.setLayout(flexL);

		p.add("0,0,x,x", new JScrollPane(txaLog));

		return p;
	}

	//---------------------------------------------------------------------------
	//---
	//--- SimpleLogger interface
	//---
	//---------------------------------------------------------------------------

	public void clear()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				txaLog.setText("");
			}
		});
	}

	//---------------------------------------------------------------------------

	public void logInfo(final String message)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				txaLog.append("| I | "+ message +"\n");
			}
		});
	}

	//---------------------------------------------------------------------------

	public void logError(final String message)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				txaLog.append("|EEE| "+ message +"\n");
			}
		});
	}

	//---------------------------------------------------------------------------

	public void finish()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
//				enableControls(true);
			}
		});
	}
}

//==============================================================================

