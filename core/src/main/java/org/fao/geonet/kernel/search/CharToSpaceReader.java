package org.fao.geonet.kernel.search;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

import bak.pcj.set.CharOpenHashSet;

/**
 * Maps a set characters from one to a space. 
 * 
 * The purpose is to effectively ignore certain characters when analyzing input for lucene.  
 * Since spaces are used to split tokens by Standard tokenizers, this reader will convert the
 * set of input characters to spaces so that they will be effectively ignored.
 *  
 * @author jeichar
 */
class CharToSpaceReader extends FilterReader {
	
	private CharOpenHashSet set;
	char space = ' ';
	
	/**
	 * @param reader
	 * @param charsToSetAsSpaces characters to convert to spaces
	 */
	public CharToSpaceReader(Reader reader, char[] charsToSetAsSpaces) {
	    super(reader);
	    this.set = new CharOpenHashSet(charsToSetAsSpaces);
    }

	@Override
	public int read(char[] in, int start, int end) throws IOException {
		int read = super.read(in, start, end);
		for (int i = start; i < read ; i++) {
	        if(set.contains(in[i])) {
	        	in[i] = space;
	        }
        }
		return read;
	}

	@Override
	public int read() throws IOException {
		int read = super.read();
		char asChar = (char)read;
		if(asChar == read && set.contains(asChar)) return ' ';
		else return read;
	}
	
}
