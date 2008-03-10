//=============================================================================
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

package org.fao.gast.lib;

import java.awt.Component;
import javax.swing.JOptionPane;
import jeeves.exceptions.JeevesException;

//=============================================================================

public class GuiLib
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void showError(Component c, Throwable t)
	{
		String title   = t.getClass().getSimpleName();
		String message = t.getMessage();

		if (message == null || message.length() == 0)
			message = "<no message>";

		if (t instanceof JeevesException)
		{
			Object obj = ((JeevesException) t).getObject();

			if (obj != null)
				message += "\n\n"+ obj.toString();
		}

		JOptionPane.showMessageDialog(c, message, title, JOptionPane.ERROR_MESSAGE);
		t.printStackTrace();
	}

	//---------------------------------------------------------------------------

	public void showError(Component c, String message)
	{

		JOptionPane.showMessageDialog(c, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	//---------------------------------------------------------------------------

	public void showInfo(Component c, String message)
	{
		JOptionPane.showMessageDialog(c, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	//---------------------------------------------------------------------------

	public boolean confirm(Component c, String message)
	{
		int res = JOptionPane.showConfirmDialog(c, message, "Confirmation", JOptionPane.YES_NO_OPTION);

		return (res == JOptionPane.YES_OPTION);
	}
}

//=============================================================================

