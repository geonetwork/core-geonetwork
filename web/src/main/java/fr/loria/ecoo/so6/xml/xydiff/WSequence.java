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

public class WSequence {
    public int data;
    public double weight;

    public WSequence(int d, double w) {
        this.data = d;
        this.weight = w;
    }

    public int getData() {
        return this.data;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setData(int data) {
        this.data = data;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
