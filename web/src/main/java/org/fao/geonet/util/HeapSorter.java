//=============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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
package org.fao.geonet.util;

public class HeapSorter {
    private static Comparable[] a;
    private static int n;

    public static void sort(Comparable[] a0) {
        a=a0;
        n=a.length;
        heapsort();
    }

    private static void heapsort() {
        buildheap();
        while (n>1) {
            n--;
            exchange (0, n);
            downheap (0);
        } 
    }

    private static void buildheap() {
        for (int v=n/2-1; v>=0; v--)
            downheap (v);
    }

    private static void downheap(int v) {
        int w=2*v+1;    // first descendant of v
        while (w<n) {
            if (w+1<n)    // is there a second descendant?
                if (a[w+1].compareTo(a[w]) > 0) w++;
            // w is the descendant of v with maximum label

            if (a[v].compareTo(a[w]) >= 0) return;  // v has heap property
            // otherwise
            exchange(v, w);  // exchange labels of v and w
            v=w;        // continue
            w=2*v+1;
        }
    }

    private static void exchange(int i, int j) {
        Comparable t=a[i];
        a[i]=a[j];
        a[j]=t;
    }

}    // end class HeapSorter

