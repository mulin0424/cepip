/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.cobi.kggseq.entity.Individual;

/**
 *
 * @author mxli
 */
public class LinkageFileParser {

    // private static final Log LOG = Log.getInstance(LinkageFileParser.class);
    private static final Logger LOG = Logger.getLogger(LinkageFileParser.class);

    public boolean readPedigreeOnly(String pedFileName, List<Individual> indList, boolean compositeID, Map<String, Integer> phenotypeColID) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(pedFileName));
        Set<String> duplicatedIndiv = new HashSet<>();
        String line = null;
        String delimiter = "\t\" \",/";

        String info = "Encoding pedigree file " + pedFileName + " ...";
        LOG.info(info);
        String indivLabel = null;
        StringBuilder tmpBuffer = new StringBuilder();
        int fileLineCounter = 0;
        StringTokenizer tokenizer;
        //long t3 = System.currentTimeMillis();
        try {
            line = br.readLine();
            //special label 
            if (line.startsWith("#")) {
                line = br.readLine();
            }
            int gtyStaringCol = 0;
            int traitNum = 0;
            if (phenotypeColID != null) {
                traitNum = phenotypeColID.size();
            }
            String v;
            do {
                //line = line.toUpperCase();
                fileLineCounter++;
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                tokenizer = new StringTokenizer(line, delimiter);

                Individual indiv = new Individual();
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(tokenizer.nextToken().trim());
                indiv.setFamilyID(tmpBuffer.toString());
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(tokenizer.nextToken().trim());
                indiv.setIndividualID(tmpBuffer.toString());
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(tokenizer.nextToken().trim());
                indiv.setDadID(tmpBuffer.toString());
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(tokenizer.nextToken().trim());
                indiv.setMomID(tmpBuffer.toString());

//                indiv.setGender(Integer.valueOf(tokenizer.nextToken().trim()));
                if (traitNum > 0) {
                    gtyStaringCol = 0;
                    double[] values = new double[traitNum];

                    while (tokenizer.hasMoreTokens()) {
                        v = tokenizer.nextToken().trim();
                        if (v.equals("NA")) {
                            values[gtyStaringCol] = Double.NaN;
                        } else {
                            values[gtyStaringCol] = Double.parseDouble(v);
                        }

                        gtyStaringCol++;
                    }
                    indiv.setTraits(values);
                    if (values.length > 0) {
                        indiv.setGender((int) values[0]);
                    }
                    if (values.length > 1) {
                        indiv.setAffectedStatus((int) values[1]);
                    }
                } else {
                    if (tokenizer.hasMoreTokens()) {
                        indiv.setGender(Integer.valueOf(tokenizer.nextToken().trim()));
                    }
                    if (tokenizer.hasMoreTokens()) {
                        indiv.setAffectedStatus(Integer.valueOf(tokenizer.nextToken().trim()));
                    }
                }

                // indiv.setLabelInChip(indiv.getFamilyID() + "@*@" + indiv.getIndividualID());
                if (compositeID) {
                    indiv.setLabelInChip(indiv.getFamilyID() + "$" + indiv.getIndividualID());
                } else {
                    indiv.setLabelInChip(indiv.getIndividualID());
                }
                

                indivLabel = indiv.getLabelInChip();
                if (duplicatedIndiv.contains(indivLabel)) {
                    String unexpectInfo = "Duplicated Individuals  in " + pedFileName + " for PedID " + indiv.getFamilyID() + " IndivID " + indiv.getIndividualID();
                    throw new Exception(unexpectInfo);
                }

                //System.out.println(indiv.getLabelInChip());
                indList.add(indiv);
                line = null;
            } while ((line = br.readLine()) != null);
            br.close();
            return true;
        } catch (Exception nex) {
            // nex.printStackTrace();
            info = nex.toString() + " when parsing at line " + fileLineCounter + ": " + line;
            // LOG.error(nex, info);
            throw new Exception(info);
        }

    }

    public boolean readPedigreeOnlyVCFBitGtySet(String pedFileName, List<Individual> indList) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(pedFileName));
        Set<String> duplicatedIndiv = new HashSet<String>();
        String line;
        String delimiter = "\t\" \",/";

        int unexpectedGtyNum = 0;
        String info = "Encoding pedigree file " + pedFileName + " ...";
        LOG.info(info);

        List<String> traitNames = new ArrayList<String>();
        traitNames.add("AffectedStatus");

        String indivLabel = null;
        StringBuilder tmpBuffer = new StringBuilder();
        //long t3 = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            //line = line.toUpperCase();
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, delimiter);

            Individual indiv = new Individual();
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setFamilyID(tmpBuffer.toString());
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setIndividualID(tmpBuffer.toString());
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setDadID(tmpBuffer.toString());
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setMomID(tmpBuffer.toString());

            indiv.setGender(Integer.valueOf(tokenizer.nextToken().trim()));
            indiv.setAffectedStatus(Integer.valueOf(tokenizer.nextToken().trim()));
            // indiv.setLabelInChip(indiv.getFamilyID() + "@*@" + indiv.getIndividualID());
            indiv.setLabelInChip(indiv.getIndividualID());

            /*
             * assume there is only one tait in the pedigree file
             for (int i = 5; i < gtyStaringCol; i++) {
             tmpBuffer.delete(0, tmpBuffer.length());
             tmpBuffer.append(tokenizer.nextToken().trim());
             indiv.addTrait(tmpBuffer.toString());
             }
             */
            indivLabel = indiv.getLabelInChip();
            if (duplicatedIndiv.contains(indivLabel)) {
                String unexpectInfo = "Duplicated Individuals  in " + pedFileName + " for PedID " + indiv.getFamilyID() + " IndivID " + indiv.getIndividualID();
                throw new Exception(unexpectInfo);
            }

            //System.out.println(indiv.getLabelInChip());
            indList.add(indiv);
            tokenizer = null;
            line = null;
        }
        br.close();
        return false;
    }
}
