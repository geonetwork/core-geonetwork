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

import java.util.Vector;


public class CommonSubSequenceAlgorithms {
    /**
     * Here are the headers of various implementation of CSS Algorithms
     * <p/>
     * The Goal of a Common SubSequence Algorithm is, given to input Sequences, to find a subsequence that exists in
     * both of them There may be many solutions,
     * and various algorithms will try to find the longest common subsequence, or the subsequence that has the most
     * important value (if sequence elements are
     * given a weight)
     * <p/>
     * The common subsequence is used in the context of children of a specific nodes. Although some child nodes have
     * the same parent in the old and new version
     * of the document, their position may have changed. We ignore position changes due to operation on their sibling
     * . Still, some nodes may have been
     * reordered, for example nodes 1, 2, 3 may become nodes 2, 3, 1 Finding the longest common subsequence (here 2,
     * 3) gives us the minimal set of move
     * operations that transforms the old sequence of children into the new one. Here: <move node 1 from position 1
     * to position 3>
     * <p/>
     * <p/>
     * Our implementation of Longest Common SubSequence has a time cost in n1*n2, where n1 and n2 are the length of
     * both input sequences The subsequence we
     * identify is the 'longest' if the weight is the same for all children, but in a more general weight is the
     * subsequence which has the most important
     * weight. So the set of operations that transforms the old list of children into the new one is the 'smallest'
     * set of operations, e.g. in general the set
     * of operations with the lowest weight.
     * <p/>
     * The other routine, called easy_css is a quick version which time cost is linear in the size of input sequences
     * . Of course the result is not optimal, and
     * weights are ignored
     * <p/>
     * <p/>
     * USAGE: INPUT-1: S1[1]...S1[S1.size()-1] represent the first sequence (note that S1[0] is ignored) They must be
     * equal to 1, 2, 3, ..., S1.size()-1
     * (because of easy_lcss implementation) INPUT-2: S2[1]...S2[S2.size()-1] represent the second sequence (note
     * that S2[0] is ignored)
     * <p/>
     * RESULT: Both sequences are modified, in that some element values are set to 0. This mean that the element is
     * NOT part of the subsequence. For all other
     * elements, the value has not changed, and the element is part of the identified common subsequence.
     */

    //	   translation done by Tani
    // s1 and s2 are vector of WSequence
    public static void easy_css(Vector s1, Vector s2) {
        int cursor1 = 1;
        int cursor2 = 1;

        while((cursor1 < s1.size()) && (cursor2 < s2.size())) {
            if(((WSequence) s1.get(cursor1)).getData() == 0) {
                //System.out.println("s1[" + cursor1 + "] is already removed");
                cursor1++;
            }
            else {
                if(((WSequence) s2.get(cursor2)).getData() == 0) {
                    //System.out.println("s2[" + cursor2 + "] is already removed ... but how is that possible????");
                    cursor2++;
                }
                else {
                    if(((WSequence) s1.get(cursor1)).getData() == ((WSequence) s2.get(cursor2)).getData()) {
                        //System.out.println("Ok s1[" + cursor1 + "]==s2[" + cursor2 + "]");
                        cursor1++;
                        cursor2++;
                    }
                    else {
                        //System.out.println("Throwing away s2[" + cursor2 + "]");
                        ((WSequence) s1.get(((WSequence) s2.get(cursor2)).getData())).setData(0);
                        ((WSequence) s2.get(cursor2)).setData(0);
                        cursor2++;
                    }
                }
            }
        }
    }

    public static void lcss(Vector s1, Vector s2) {
        // Init
        int l1 = s1.size();
        int l2 = s2.size();
        Double[][] cost = new Double[l2][l1];
        Character[][] origin = new Character[l2][l1];

        // Transforming empty string into empty string has no cost
        cost[0][0] = new Double(0.0);
        origin[0][0] = new Character('Z');

        // Transforming empty parts of S1 into string is just deleting
        for(int i = 1; i < l1; i++) {
            cost[0][i] = new Double(cost[0][i - 1].doubleValue() + ((WSequence) s1.get(i)).getWeight());
            origin[0][i] = new Character('A');
        }

        // Transforming empty string into parts of S2 is just inserting
        for(int j = 1; j < l2; j++) {
            cost[j][0] = new Double(cost[j - 1][0].doubleValue() + ((WSequence) s2.get(j)).getWeight());
            origin[j][0] = new Character('B');
        }

        // Compute paths cost which transforms S1 into S2
        for(int j = 1; j < l2; j++) {
            for(int i = 1; i < l1; i++) {
                //System.out.println("Computing cost(i=" + i + ",j=" + j + ")");
                // Delete item i in S1 and use transformation of S1[1..i-1] into S2[1..j]
                double deleteCost = ((WSequence) s1.get(i)).getWeight() + cost[j][i - 1].doubleValue();

                //System.out.println("Delete cost is " + deleteCost);
                // Use transformation of S1[i..i] into S2[1..j-1] and insert S2[j]
                double insertCost = cost[j - 1][i].doubleValue() + ((WSequence) s2.get(j)).getWeight();

                //System.out.println("Insert cost is " + insertCost);
                if(((WSequence) s1.get(i)).getData() != ((WSequence) s2.get(j)).getData()) {
                    if(deleteCost < insertCost) {
                        cost[j][i] = new Double(deleteCost);
                        origin[j][i] = new Character('A');
                    }
                    else {
                        cost[j][i] = new Double(insertCost);
                        origin[j][i] = new Character('B');
                    }
                }
                else {
                    double keep = cost[j - 1][i - 1].doubleValue();

                    //System.out.println("Keep cost is " + keep);
                    origin[j][i] = new Character(getMin3(deleteCost, insertCost, keep));

                    if(origin[j][i].charValue() == 'A') {
                        cost[i][j] = new Double(deleteCost);
                    }
                    else {
                        if(origin[j][i].charValue() == 'B') {
                            cost[j][i] = new Double(insertCost);
                        }
                        else {
                            if(origin[j][i].charValue() == 'C') {
                                cost[j][i] = new Double(keep);
                            }
                        }
                    }

                    //System.out.println("Cost(i=" + i + ",j=" + j + ") ,via " + cost[j][i].doubleValue() + ",
                    // " + origin[j][i].charValue());
                }
            }
        }

        // Trace best path
        int i = l1 - 1;
        int j = l2 - 1;
        int balance = 0; //Delete/Insert balance

        while((i > 0) || (j > 0)) {
            try {
                if((i < 0) || (j < 0)) {
                    throw new Exception("Inconsistant computing: negative index!");
                }

                //System.out.println("At (i=" + i + ",j=" + j + ") cost= " + cost[j][i].doubleValue() + " origin= " +
                // origin[j][i].charValue());
                switch(origin[j][i].charValue()) {
                    case 'A':

                        //System.out.println("Delete " + ((WSequence) s1.get(i)).getData());
                        ((WSequence) s1.get(i)).setData(0);
                        i--;
                        balance++;

                        break;

                    case 'B':

                        //System.out.println("Insert " + ((WSequence) s2.get(j)).getData());
                        ((WSequence) s2.get(j)).setData(0);
                        j--;
                        balance--;

                        break;

                    case 'C':

                        try {
                            if(((WSequence) s1.get(i)).getData() != ((WSequence) s2.get(j)).getData()) {
                                throw new Exception("Inconsistant computing: Character not equal as supposed!");
                            }

                            //System.out.println("Do nothing on " + ((WSequence) s1.get(i)).getData());
                            i--;
                            j--;

                            break;
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        if(balance != 0) {
            System.err.println("LongestCommonSubSequence Warnig: Insert/Delete balance is not null!");
        }
    }

    public static char getMin3(double a, double b, double c) {
        if(a <= b) {
            if(a <= c) {
                return 'A';
            }

            if(c <= a) {
                return 'C';
            }
        }

        if(b <= c) {
            return 'B';
        }

        return 'C';
    }
}
