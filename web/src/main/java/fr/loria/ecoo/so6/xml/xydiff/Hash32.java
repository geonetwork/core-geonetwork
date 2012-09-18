/**
 * JXyDiff: An XML Diff Written in Java
 *
 * Contact: pascal.molli@loria.fr
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of QPL/CeCill
 *
 * See licences details in QPL.txt and CeCill.txt
 *
 * Initial developer: Raphael Tani
 * Initial Developer: Gregory Cobena
 * Initial Developer: Gerald Oster
 * Initial Developer: Pascal Molli
 * Initial Developer: Serge Abiteboul
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package fr.loria.ecoo.so6.xml.xydiff;

import java.util.Hashtable;


/**
 * This class implements the hash functions describes by Bob Jenkins (December, 1996).
 * See http://burtleburtle.net/bob/hash/evahash.html.
 *
 */
public class Hash32 {
    public int value;

    public Hash32() {
        this.value = 0;
    }

    public Hash32(String str) {
        // at the moment use Java String Hash functions...
        this.value = str.hashCode();
    }

    public Hash32(int initialvalue) {
        this.value = initialvalue;
    }

    public Hash32(Hash32 toCopy) {
        this.value = toCopy.value;
    }

    //	   public Hash32(String str) {
    //	byte[] buffer = str.getBytes();
    //	this.value = hash2(buffer, buffer.length, 0);
    //	   }
    public Hash32(long[] buf) {
        String str = buf[0] + "!" + buf[1];
        this.value = str.hashCode();
    }

    public Object clone() {
        return new Hash32(this.value);
    }

    public String toHexString() {
        return Integer.toHexString(this.value);
    }

    public boolean equals(Object o) {
        if(o instanceof Hash32) {
            return this.value == ((Hash32) o).value;
        }

        return false;
    }

    public String toString() {
        return toHexString();
    }

    public static void main(String[] args) {
        System.out.println((new Hash32("TUTU")).equals(new Hash32("TUTU")));

        Hashtable h = new Hashtable();

        Hash32 j = new Hash32("TUTU");

        h.put((new Hash32("TUTU").toString()), "fff");
        System.out.println(h.containsKey((new Hash32("TUTU").toString())));

        //	String str;
        //	str = "OKIDOKI";
        //	System.out.println("hash32('"+str+"') = "+(new Hash32(str).toHexString()));
        //	str = "BLURP";
        //	System.out.println("hash32('"+str+"') = "+(new Hash32(str).toHexString()));
        //	str = "JavaRulezDaWorlDz";
        //	System.out.println("hash32('"+str+"') = "+(new Hash32(str).toHexString()));
        //	Object[] o1 = new Object[2];
        //	o1[0] = new String("1");
        //	o1[1] = new String("2");
        //	Object[] o2 = new Object[2];
        //	o2[0] = new String("1");
        //	o2[1] = new String("2");
        //	System.out.println("hashCode(o1) = "+o1.hashCode());
        //	System.out.println("hashCode(o2) = "+o2.hashCode());
        //	System.out.println("hash32(o1) = "+(new Hash32(o1.hashCode()).toHexString()));
        //	System.out.println("hash32(o2) = "+(new Hash32(o2.hashCode()).toHexString()));
        long[] A = new long[2];
        A[0] = 143;
        A[1] = 34556;
        System.out.println("hash32(A[]) = " + (new Hash32(A).toHexString()));

        long[] B = new long[2];
        B[0] = 143;
        B[1] = 34556;
        System.out.println("hash32(B[]) = " + (new Hash32(B).toHexString()));
    }

    //	   public static int hashsize(int n) {
    //	return (1 << n);
    //	   }
    //	   public static int hashmask(int n) {
    //	return hashsize(n)-1;
    //	   }

    /*
     * -------------------------------------------------------------------- mix -- mix 3 32-bit values reversibly.
     * For every delta with one or two bit set, and
     * the deltas of all three high bits or all three low bits, whether the original value of a,b,
     * c is almost all zero or is uniformly distributed, If mix() is
     * run forward or backward, at least 32 bits in a,b,c have at least 1/4 probability of changing. If mix() is run
     * forward, every bit of c will change between
     * 1/3 and 2/3 of the time. (Well, 22/100 and 78/100 for some 2-bit deltas.) mix() takes 36 machine instructions,
      * but only 18 cycles on a superscalar
     * machine (like a Pentium or a Sparc). No faster mixer seems to work, that's the result of my brute-force search
     * . There were about 2^^68 hashes to choose
     * from. I only tested about a billion of those.
     * --------------------------------------------------------------------
     */

    //	   public static void mix(int a, int b, int c) {
    //	a -= b; a -= c; a ^= (c>>13);
    //	b -= c; b -= a; b ^= (a<<8);
    //	c -= a; c -= b; c ^= (b>>13);
    //	a -= b; a -= c; a ^= (c>>12);
    //	b -= c; b -= a; b ^= (a<<16);
    //	c -= a; c -= b; c ^= (b>>5);
    //	a -= b; a -= c; a ^= (c>>3);
    //	b -= c; b -= a; b ^= (a<<10);
    //	c -= a; c -= b; c ^= (b>>15);
    //	   }

    /* same, but slower, works on systems that might have 8 byte ub4's */

    //	   public static void mix2(int a, int b, int c) {
    //	a -= b; a -= c; a ^= (c>>13);
    //	b -= c; b -= a; b ^= (a<< 8);
    //	c -= a; c -= b; c ^= ((b&0xffffffff)>>13);
    //	a -= b; a -= c; a ^= ((c&0xffffffff)>>12);
    //	b -= c; b -= a; b = (b ^ (a<<16)) & 0xffffffff;
    //	c -= a; c -= b; c = (c ^ (b>> 5)) & 0xffffffff;
    //	a -= b; a -= c; a = (a ^ (c>> 3)) & 0xffffffff;
    //	b -= c; b -= a; b = (b ^ (a<<10)) & 0xffffffff;
    //	c -= a; c -= b; c = (c ^ (b>>15)) & 0xffffffff;
    //	   }

    /*
     * -------------------------------------------------------------------- hash() -- hash a variable-length key into
      * a 32-bit value k : the key (the unaligned
     * variable-length array of bytes) len : the length of the key, counting by bytes level : can be any 4-byte value
      * Returns a 32-bit value. Every bit of the
     * key affects every bit of the return value. Every 1-bit and 2-bit delta achieves avalanche. About 36+6len
     * instructions.
     *
     * The best hash table sizes are powers of 2. There is no need to do mod a prime (mod is sooo slow!). If you need
      * less than 32 bits, use a bitmask. For
     * example, if you need only 10 bits, do h = (h & hashmask(10)); In which case,
     * the hash table should have hashsize(10) elements.
     *
     * If you are hashing n strings (ub1 **)k, do it like this: for (i=0, h=0; i <n; ++i) h = hash( k[i], len[i], h);
     *
     * By Bob Jenkins, 1996. bob_jenkins@burtleburtle.net. You may use this code any way you wish, private,
     * educational, or commercial. It's free.
     *
     * See http://burtleburtle.net/bob/hash/evahash.html Use for hash table lookup,
     * or anything where one collision in 2^32 is acceptable. Do NOT use for
     * cryptographic purposes. --------------------------------------------------------------------
     */

    //	   public static int hash(byte[] k, int length, int initval) {
    //	int len,a,b,c;
    //	// set up the internal state
    //	len = length;
    //	a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    //	c = initval; /* the previous hash value */
    //	int dec = 0;
    //	/*---------------------------------------- handle most of the key */
    //	while (len >= 12) {
    //		a += (k[0+dec] +(k[1+dec]<<8) +(k[2+dec]<<16) +(k[3+dec]<<24));
    //		b += (k[4+dec] +(k[5+dec]<<8) +(k[6+dec]<<16) +(k[7+dec]<<24));
    //		c += (k[8+dec] +(k[9+dec]<<8) +(k[10+dec]<<16)+(k[11+dec]<<24));
    //		mix(a,b,c);
    //		// k += 12; len -= 12;
    //		dec += 3; len -= 12;
    //	}
    //	/*------------------------------------- handle the last 11 bytes */
    //	c += length;
    //	switch(len) { /* all the case statements fall through */
    //	case 11: c+=(k[10+dec]<<24);
    //	case 10: c+=(k[9+dec]<<16);
    //	case 9 : c+=(k[8+dec]<<8);
    //		/* the first byte of c is reserved for the length */
    //	case 8 : b+=(k[7+dec]<<24);
    //	case 7 : b+=(k[6+dec]<<16);
    //	case 6 : b+=(k[5+dec]<<8);
    //	case 5 : b+=k[4+dec];
    //	case 4 : a+=(k[3+dec]<<24);
    //	case 3 : a+=(k[2+dec]<<16);
    //	case 2 : a+=(k[1+dec]<<8);
    //	case 1 : a+=k[0+dec];
    //		/* case 0: nothing left to add */
    //	}
    //	mix(a,b,c);
    //	/*-------------------------------------------- report the result */
    //	return c;
    //	   }

    /*
     * -------------------------------------------------------------------- This works on all machines,
     * is identical to hash() on little-endian machines, and it
     * is much faster than hash(), but it requires -- that the key be an array of ub4's,
     * and -- that all your machines have the same endianness, and -- that the
     * length be the number of ub4's in the key --------------------------------------------------------------------
     */

    //		public static int hash2(byte[] k, int length, int initval) {
    //	int len,a,b,c;
    //	// set up the internal state
    //	len = length;
    //	a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    //	c = initval; /* the previous hash value */
    //	int dec = 0;
    //	/*---------------------------------------- handle most of the key */
    //	while (len >= 3) {
    //		a += k[0+dec];
    //		b += k[1+dec];
    //		c += k[2+dec];
    //		mix(a,b,c);
    //		//k += 3; len -= 3;
    //		dec += 3; len -= 3;
    //	}
    //	/*-------------------------------------- handle the last 2 ub4's */
    //	c += length;
    //	switch(len) { /* all the case statements fall through */
    //		/* c is reserved for the length */
    //	case 2 : b+=k[1];
    //	case 1 : a+=k[0];
    //		/* case 0: nothing left to add */
    //	}
    //	mix(a,b,c);
    //	/*-------------------------------------------- report the result */
    //	return c;
    //	   }

    /*
     * -------------------------------------------------------------------- This is identical to hash() on
     * little-endian machines, and it is much faster than
     * hash(), but a little slower than hash2(), and it requires -- that all your machines be little-endian,
     * for example all Intel x86 chips or all VAXen. It
     * gives wrong results on big-endian machines. --------------------------------------------------------------------
     */

    //	   public static int hash3(byte[] k, int length, int initval) {
    //	int len,a,b,c;
    //	// set up the internal state
    //	len = length;
    //	a = b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
    //	c = initval; /* the previous hash value */
    //	/*---------------------------------------- handle most of the key */
    //	if ((k)&3) {
    //		while (len >= 12) { /* unaligned */
    //		a += (k[0] +(k[1]<<8) +(k[2]<<16) +(k[3]<<24));
    //		b += (k[4] +(k[5]<<8) +(k[6]<<16) +(k[7]<<24));
    //		c += (k[8] +(k[9]<<8) +(k[10]<<16)+(k[11]<<24));
    //		mix(a,b,c);
    //		k += 12; len -= 12;
    //		}
    //	} else {
    //		while (len >= 12) { /* aligned */
    // // to translate ...
    // // a += *(ub4 *)(k+0);
    // // b += *(ub4 *)(k+4);
    // // c += *(ub4 *)(k+8);
    //		mix(a,b,c);
    //		k += 12; len -= 12;
    //		}
    //	}
    //	/*------------------------------------- handle the last 11 bytes */
    //	c += length;
    //	switch(len) { /* all the case statements fall through */
    //	case 11: c+=(k[10]<<24);
    //	case 10: c+=(k[9]<<16);
    //	case 9 : c+=(k[8]<<8);
    //		/* the first byte of c is reserved for the length */
    //	case 8 : b+=(k[7]<<24);
    //	case 7 : b+=(k[6]<<16);
    //	case 6 : b+=(k[5]<<8);
    //	case 5 : b+=k[4];
    //	case 4 : a+=(k[3]<<24);
    //	case 3 : a+=(k[2]<<16);
    //	case 2 : a+=(k[1]<<8);
    //	case 1 : a+=k[0];
    //		/* case 0: nothing left to add */
    //	}
    //	mix(a,b,c);
    //	/*-------------------------------------------- report the result */
    //	return c;
    //	   }
}
