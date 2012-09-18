package org.fao.geonet.kernel.diff;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Representation of a position of a node in a tree.
 *
 * @author heikki doeleman
 */
public class NodePosition {
    String position;
    String[] positionParts;

    /**
     *
     * @param position
     */
    NodePosition(String position) {
        this.position = position;
        this.positionParts = position.split(":");
    }

    /**
     *
     * @param positionParts
     */
    NodePosition(String[] positionParts) {
        this.positionParts = positionParts;
        this.position = createPositionFromParts(positionParts);
    }

    /**
     *
     * @param positionParts
     * @return
     */
    private String createPositionFromParts(String[] positionParts) {
        String pos = "";
        for(int i = 0; i < positionParts.length; i++) {
            if(i < positionParts.length-1) {
                pos += positionParts[i] + ':';
            }
            else {
                pos += positionParts[i];
            }
        }
        return pos;
    }

    /**
     *
     * @return
     */
    public String toString() {
        return this.position;
    }

    /**
     *
     * @param index
     */
    public void incrementAt(int index) {
        int toInc = Integer.parseInt(this.positionParts[index]);
        this.positionParts[index] = "" + ++toInc;
        this.position = createPositionFromParts(this.positionParts);
    }

    /**
     *
     * @param index
     */
    public void decrementAt(int index) {
        int toDec = Integer.parseInt(this.positionParts[index]);
        this.positionParts[index] = "" + --toDec;
        this.position = createPositionFromParts(this.positionParts);
    }

    /**
     * The so-called Interesting Set is the set of a node, its preceding siblings, its ancestors and its ancestors' preceding siblings. This set
     * is interesting because any inserts or deletes in this set impact the position of this node.
     * @return
     */
    public Set<String> interestingSet() {
        Set<String> interestingSet = new HashSet<String>();
        Scanner scanner = new Scanner(this.position).useDelimiter(":");
        String prev = "";
        boolean first = true;
        while(scanner.hasNext()) {
            int p = scanner.nextInt();
            String prevBefore = prev;
            if(first) {
                prev = prev + p;
            }
            else {
                prev = prev + ':' + p;
            }
            for(int i = 0; i <= p; i++) {
                if(first) {
                    interestingSet.add(prevBefore + i);
                    first = false;
                }
                else {
                    interestingSet.add(prevBefore + ':' + i);
                }
            }
        }
        return interestingSet;
    }

}