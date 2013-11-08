package org.fao.geonet.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.fao.geonet.Constants;

/**
 * Allows OutputStream to be mapped to StringBuffer.
 * <p/>
 * Smould IMHO be in Java SDK but somehow isn't.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class StringBufferOutputStream extends OutputStream
{
	private StringBuffer strBuffer;
	private boolean closed = false;

	public StringBufferOutputStream(StringBuffer strBuffer)
	{
		super();
		this.strBuffer = strBuffer;
	}

	/**
	 * method to write a char
	 */
	public void write(int i) throws IOException
	{
		if (closed)
		{
			return;
		}

		strBuffer.append((char) i);
	}

	/**
	 * write an array of bytes
	 */
	public void write(byte[] b, int offset, int length)
			throws IOException
	{
		if (closed)
		{
			return;
		}

		if (b == null)
		{
			throw new NullPointerException("The byte array is null");
		}
		if (offset < 0 || length < 0 || (offset + length) > b.length)
		{
			throw new IndexOutOfBoundsException("offset and length are negative or extend outside array bounds");
		}

		String str = new String(b, offset, length, Charset.forName(Constants.ENCODING));
		strBuffer.append(str);
	}

	public void close()
	{
		strBuffer = null;
		closed = true;
	}

}
