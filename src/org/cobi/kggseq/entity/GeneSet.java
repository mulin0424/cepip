// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kggseq.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author mxli
 */
public class GeneSet implements Cloneable {

    /**
     *
     */
    private String ID;
    /**
     *
     */
    private String name;
    /**
     *
     */
    private String URL;
    /**
     *
     */
    private HashSet<String> geneSymbols;
    /**
     *
     */
    private double enrichedPValue = Double.NaN;
    private int totalGeneNum;
    private double wilcoxonPValue = Double.NaN;
    private List<String[]> geneValues = new ArrayList<String[]>();

    public List<String[]> getGeneValues() {
        return geneValues;
    }

    public void setGeneValues(List<String[]> geneValues) {
        this.geneValues = geneValues;
    }

    
    public double getWilcoxonPValue() {
        return wilcoxonPValue;
    }

    public void setWilcoxonPValue(double wilcoxonPValue) {
        this.wilcoxonPValue = wilcoxonPValue;
    }

    public int getTotalGeneNum() {
        return totalGeneNum;
    }

    public void setTotalGeneNum(int totalGeneNum) {
        this.totalGeneNum = totalGeneNum;
    }

    @Override
    public GeneSet clone() throws CloneNotSupportedException {
        GeneSet clone = (GeneSet) super.clone();
        clone.ID = ID;
        clone.name = name;
        clone.URL = URL;
        // make the shallow copy of the object of type Department
        clone.geneSymbols = (HashSet<String>) geneSymbols.clone();
        return clone;
    }

    /**
     * Get the value of enrichedPValue
     *
     * @return the value of enrichedPValue
     */
    public double getEnrichedPValue() {
        return enrichedPValue;
    }

    /**
     * Set the value of enrichedPValue
     *
     * @param enrichedPValue new value of enrichedPValue
     */
    public void setEnrichedPValue(double enrichedPValue) {
        this.enrichedPValue = enrichedPValue;
    }

    public GeneSet(String ID, String name, String URL) {
        this.ID = ID;
        this.name = name;
        this.URL = URL;
        geneSymbols = new HashSet<String>();
    }

    /**
     *
     * @param geneSyb
     */
    public void addGeneSymbol(String geneSyb) {
        geneSymbols.add(geneSyb);
    }

    /**
     *
     * @return
     */
    public HashSet<String> getGeneSymbols() {
        return geneSymbols;
    }

    /**
     *
     * @param geneSymbols
     */
    public void setGeneSymbols(HashSet<String> geneSymbols) {
        this.geneSymbols = geneSymbols;
    }

    /**
     *
     * @return
     */
    public String getGeneSymbolString() {
        StringBuilder strbuf = new StringBuilder();

        strbuf.append('(');
        Iterator<String> it = geneSymbols.iterator();
        while (it.hasNext()) {
            strbuf.append(it.next());
            strbuf.append(", ");
        }

        strbuf.delete(strbuf.length() - 2, strbuf.length());
        strbuf.append(')');
        return strbuf.toString();
    }

    /**
     * Get the value of URL
     *
     * @return the value of URL
     */
    public String getURL() {
        return URL;
    }

    /**
     * Set the value of URL
     *
     * @param URL new value of URL
     */
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of ID
     *
     * @return the value of ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Set the value of ID
     *
     * @param ID new value of ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }
}
