//==============================================================================
//===
//===   Codec (adapted class from druid project http://druid.sf.net)
//===
//===   Copyright (C) by Andrea Carboni.
//===   This file may be distributed under the terms of the GPL license.
//==============================================================================

package org.fao.geonet.apps.setup.druid;

import org.dlib.tools.Util;

//==============================================================================

public class Codec
{
	public static String encodeString(String s)
	{
		StringBuffer sb = new StringBuffer();

		for(int i=0; i<s.length(); i++)
		{
			char c = s.charAt(i);

			if (c >= 32 && c < 126) sb.append(c);
				else                 sb.append("~" + Util.convertToHex(c,4));
		}

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	public static String decodeString(String s)
	{
		StringBuffer sb = new StringBuffer();

		for(int i=0; i<s.length(); i++)
		{
			char c = s.charAt(i);

			if (c == '~')
			{
				String hv = s.substring(i+1, i+5);
				i += 4;

				c = (char) Util.convertFromHex(hv);
			}

			sb.append(c);
		}

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	public static String encodeBytes(byte[] data)
	{
		StringBuffer sb = new StringBuffer();

		for(int i=0; i<data.length; i++)
			sb.append(Util.convertToHex(data[i], 2));

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	public static byte[] decodeBytes(String data)
	{
		byte[] array = new byte[data.length() /2];

		for(int i=0; i<data.length()/2; i++)
		{
			String hv = data.substring(i*2, i*2+2);
			array[i] = (byte) Util.convertFromHex(hv);
		}

		return array;
	}
}

//==============================================================================


