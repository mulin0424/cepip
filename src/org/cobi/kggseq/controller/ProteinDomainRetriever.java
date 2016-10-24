/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.cobi.kggseq.entity.ProteinDomain;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.text.Util;

/**
 *
 * @author mxli
 */
public class ProteinDomainRetriever {

    // private static final Log LOG = Log.getInstance(ProteinDomainRetriever.class);
    private static final Logger LOG = Logger.getLogger(ProteinDomainRetriever.class);

    public void compileJohnData(String inputFileName, String outputFileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        Set<String> uniprotIDs = new HashSet<String>();
        List<String[]> rows = new ArrayList<String[]>();
        String line = null;
        while ((line = br.readLine()) != null) {
            //line = line.trim();
            //System.out.println(line);
            if (line.trim().length() == 0) {
                continue;
            }
            String[] cells = line.split("\t", -1);
            uniprotIDs.add(cells[0]);
            rows.add(cells);
        }
        br.close();
        // produced a Uniprot ID's for Mapping  at http://www.uniprot.org/
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
        for (String id : uniprotIDs) {
            bw.write(id);
            bw.write("\n");
        }
        bw.close();
    }

    public void readProteinDomains(String inputIDMapFileName, String inputDomianFileName, Map<String, String> refSeqUniportIDMap, Map<String, List<ProteinDomain>> uniportIDDomainMap) throws Exception {

        BufferedReader br = LocalFileFunc.getBufferedReader(inputIDMapFileName);

        String line = null;
        int index = 0;
        //skip the title line

        br.readLine();
        while ((line = br.readLine()) != null) {
            //line = line.trim();
            //System.out.println(line);
            if (line.trim().length() == 0) {
                continue;
            }

            String[] cells = line.split("\t", -1);
            index = cells[1].indexOf('.');
            if (index >= 0) {
                cells[1] = cells[1].substring(0, index);
            }
            refSeqUniportIDMap.put(cells[1], cells[0]);
            //uniprotIDs.add(cells[2]);
            //rows.add(cells);
        }
        br.close();

        br = LocalFileFunc.getBufferedReader(inputDomianFileName);

        //skip the title line
        br.readLine();
        int startPos = 0;
        int endPos = 0;
        while ((line = br.readLine()) != null) {
            //line = line.trim();
            //System.out.println(line);
            if (line.trim().length() == 0) {
                continue;
            }
            String[] cells = line.split("\t", -1);
            startPos = -1;
            endPos = -1;

            if (Util.isNumeric(cells[4])) {
                startPos = Util.parseInt(cells[4]);
            }
            if (Util.isNumeric(cells[5])) {
                endPos = Util.parseInt(cells[5]);
            }
            ProteinDomain pd = new ProteinDomain(cells[1], cells[2], cells[3], startPos, endPos);
            //System.out.println(cells[0]);
            List<ProteinDomain> pdList = uniportIDDomainMap.get(cells[0]);
            if (pdList == null) {
                pdList = new ArrayList<ProteinDomain>();
                uniportIDDomainMap.put(cells[0], pdList);
            }

            pdList.add(pd);
        }

        br.close();
       // LOG.info("There are " + uniportIDDomainMap.size() + " proteins having domain information in the Uniprot database.");
    }

    public static void main(String[] args) {
        try {
            ProteinDomainRetriever pr = new ProteinDomainRetriever();
            pr.compileJohnData("D:\\home\\mxli\\MyJava\\KGGSeq\\resources\\UniprotDomain.txt", "D:\\home\\mxli\\MyJava\\KGGSeq\\resources\\id.txt");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
