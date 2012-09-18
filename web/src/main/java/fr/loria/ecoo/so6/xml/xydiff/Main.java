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

import fr.loria.ecoo.so6.xml.node.Document;

import java.util.Collection;
import java.util.Iterator;


public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 3) {
            System.err.println("The Main Class, must be three arguments:" + "\nfirst source file" + "\nsecond " +
                    "detination file" +
                    "\nthird name of the delta file");
        }
        else {
            XyDiff xydiff = new XyDiff(args[0], args[1]);

            DeltaConstructor c = xydiff.diff();

            Collection commands = c.getXMLCommand();

            for(Iterator iter = commands.iterator(); iter.hasNext(); ) {
                System.out.println(iter.next());
            }

            Document delta = c.getDeltaDocument();
            delta.save(args[2], false);
        }
    }
}
