package org.fao.geonet.kernel.search;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;

public class CharToSpaceReaderTest {

	String source   = "h-w how/are ( you?";
	String expected = "h w how are   you?";
	char[] chars = "-/(".toCharArray();

	@Test
	public void testRead() throws IOException {
		
		Reader reader = new StringReader(source);
		CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
		try {
			for (int i = 0; i < expected.length(); i++) {
				char c = expected.charAt(i);
				char read = (char)mapReader.read();
				assertEquals("Expected to get "+c+" at position "+i+" but got "+read, c, read);
	        }
			
			assertEquals(-1, mapReader.read());
		} finally {
			mapReader.close();
		}
	}

	@Test
	public void testReadCharArrayIntInt() throws IOException {
		Reader reader = new StringReader(source);
		CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
		try {
			char[] read = new char[expected.length() * 2];
			int numRead = mapReader.read(read,2,expected.length()+2);
			
			assertEquals(expected.length(), numRead);
			
			for (int i = 2; i < expected.length(); i++) {
				assertEquals(expected.charAt(i-2), read[i]);
	        }
		} finally {
			mapReader.close();
		}

	}

	@Test
	public void testReadCharBuffer() throws IOException {
		Reader reader = new StringReader(source);
		CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
		try {
			CharBuffer buffer = CharBuffer.allocate(expected.length());
			int numRead = mapReader.read(buffer);
			
			assertEquals(expected.length(), numRead);
	
			for (int i = 0; i < expected.toCharArray().length; i++) {
				assertEquals(expected.charAt(i), buffer.get(i));
	        }
		} finally {
			mapReader.close();
		}

	}

	@Test
	public void testReadCharArray() throws IOException {
		Reader reader = new StringReader(source);
		CharToSpaceReader mapReader = new CharToSpaceReader(reader, chars);
		try {
			char[] buffer = new char[expected.length()];
			int numRead = mapReader.read(buffer);
			
			assertEquals(expected.length(), numRead);
	
			for (int i = 0; i < expected.length(); i++) {
				assertEquals(expected.charAt(i), buffer[i]);
	        }
		} finally {
			mapReader.close();
		}

	}

}
