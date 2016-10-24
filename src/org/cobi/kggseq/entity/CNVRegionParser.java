/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.entity;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.Options;
import org.cobi.util.file.LocalFileFunc;

/**
 *
 * @author mxli
 */
public class CNVRegionParser implements Constants {

    private static final Logger LOG = Logger.getLogger(CNVRegionParser.class);

    /**
     * @return
     * @throws Exception
     * @pdOid f0621cff-9d97-421e-a77a-765bd0938dfb */
    public ReferenceGenome readRefCNVSeq(String vAFile) throws Exception {
        int indexVarID = 0;
        int indexChom = 1;
        int indexStart = 2;
        int indexEnd = 3;
        int indexSubtype = 5;
        int indexSampleSize = 14;
        int indexCbservedgains = 15;
        int indexCbservedloss = 16;


        int maxColNum = indexVarID;
        maxColNum = Math.max(maxColNum, indexChom);
        maxColNum = Math.max(maxColNum, indexStart);
        maxColNum = Math.max(maxColNum, indexEnd);
        maxColNum = Math.max(maxColNum, indexSubtype);
        maxColNum = Math.max(maxColNum, indexSampleSize);
        maxColNum = Math.max(maxColNum, indexCbservedgains);
        maxColNum = Math.max(maxColNum, indexCbservedloss);

        ReferenceGenome genome = new ReferenceGenome(0, 0, 0);
        String currentLine = null;
        String currChr = null;
        StringBuilder tmpBuffer = new StringBuilder();
        long lineCounter = 0;

        File dataFile = new File(vAFile);
        
//        LineReader br = null;
        BufferedReader br=null;
        Set<String> duplCNVs = new HashSet<String>();

        boolean incomplete = true;



        String varID = null;
        int startPos = -1;
        int endPos = -1;
        String subType = null;
        int sampleSize = -1;
        int gainNum = -1;
        int lossNum = -1;
        String[] cells;
        int cnvNum = 0;
        int dupCNVNum = 0;
        try {
            if (dataFile.exists() && dataFile.getName().endsWith(".zip")) {
//                br = new CompressedFileReader(dataFile);
                br=LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
            } else {
                if (dataFile.exists() && dataFile.getName().endsWith(".gz")) {
//                    br = new CompressedFileReader(dataFile);
                    br=LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
                } else {
                    if (dataFile.exists()) {
//                        br = new AsciiLineReader(dataFile);
                        br=LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
                    } else {
                        throw new Exception("No input file: " + dataFile.getCanonicalPath());
                    }
                }
            }

            //skip to the head line 
//            br.readLineS();
            br.readLine();

            while ((currentLine = br.readLine()) != null) {
                lineCounter++;
                //System.out.println(currentLine);
                // StringTokenizer st = new StringTokenizer(currentLine.trim(),"\t");
                cells = currentLine.split("\t", -1);
                //initialize varaibles
                incomplete = true;
                currChr = null;

                varID = null;
                startPos = -1;
                endPos = -1;
                subType = null;
                gainNum = 0;
                lossNum = 0;
                sampleSize = 0;

                maxColNum = indexVarID;
                maxColNum = Math.max(maxColNum, indexChom);
                maxColNum = Math.max(maxColNum, indexStart);
                maxColNum = Math.max(maxColNum, indexEnd);
                maxColNum = Math.max(maxColNum, indexSubtype);
                maxColNum = Math.max(maxColNum, indexCbservedgains);
                maxColNum = Math.max(maxColNum, indexCbservedloss);

                /*
                for (int iCol = 0; iCol <= maxColNum; iCol++) {
                if (st.hasMoreTokens()) {
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(st.nextToken().trim());
                System.out.println(iCol+"  "+tmpBuffer);
                if (iCol == indexVarID) {
                varID = tmpBuffer.toString();
                } else if (iCol == indexStart) {
                startPos = Integer.parseInt(tmpBuffer.toString());
                } else if (iCol == indexChom) {
                currChr = tmpBuffer.toString();
                // currChr = currChr.substring(3);
                } else if (iCol == indexEnd) {
                endPos = Util.parseInt(tmpBuffer.toString());
                } else if (iCol == indexSubtype) {
                subType = (tmpBuffer.toString());
                } else if (iCol == indexCbservedgains) {
                gainNum = Integer.parseInt(tmpBuffer.toString());
                } else if (iCol == indexCbservedloss) {
                lossNum = Integer.parseInt(tmpBuffer.toString());
                }
                } else {
                break;
                }
                if (iCol == maxColNum) {
                incomplete = false;
                }
                }
                
                if (incomplete) {
                continue;
                }
                 */

                varID = cells[indexVarID];
                currChr = cells[indexChom];
                startPos = Integer.parseInt(cells[indexStart]);
                endPos = Integer.parseInt(cells[indexEnd]);
                subType = cells[indexSubtype];
                if (!cells[indexCbservedgains].isEmpty()) {
                    gainNum = Integer.parseInt(cells[indexCbservedgains]);
                }

                if (!cells[indexCbservedloss].isEmpty()) {
                    lossNum = Integer.parseInt(cells[indexCbservedloss]);
                }

                if (!cells[indexSampleSize].isEmpty()) {
                    sampleSize = Integer.parseInt(cells[indexSampleSize]);
                }


                RefCNV mrna = new RefCNV(varID, startPos, endPos, subType, sampleSize, gainNum, lossNum);
                String ids = mrna.getDescription() + ":" + currChr + ":" + mrna.getStart() + ":" + mrna.getEnd();

                int[] poss = genome.getmRNAPos(ids);
                if (poss != null) {
                    mrna = genome.getCNV(poss);
                    if (!currChr.equals(STAND_CHROM_NAMES[poss[0]])) {
                        //note a transcript can be mapped onto multiple locations
                        String info = "Duplicated CNV items: " + mrna.getDescription();
                        System.out.println(info);
                        dupCNVNum++;
                        continue;
                    }
                } else {
                    cnvNum++;
                    genome.addRefCNV(mrna, currChr);
                }
                // System.out.println(currentLine);
            }
        } catch (NumberFormatException nex) {
            String info = nex.toString() + " when parsing at line " + lineCounter + ": " + currentLine;
            // LOG.error(nex, info);
            throw new Exception(info);
        }
        br.close();
        genome.sortCNVMakeIndexonChromosomes();
        LOG.info(cnvNum + " reference CNVs have been read!");
        if (dupCNVNum > 0) {
            LOG.info(dupCNVNum + " duplicated reference CNV items!");
        }
        return genome;
    }

    /**
     * @return
     * @throws Exception
     * @pdOid f0621cff-9d97-421e-a77a-765bd0938dfb */
    public ReferenceGenome readSuperDupRegions(String vAFile) throws Exception {
        int indexVarID = 0;
        int indexChom = 1;
        int indexStart = 2;
        int indexEnd = 3;

        int indexJcK = 28;
        int indexK2K = 29;


        int maxColNum = indexVarID;
        maxColNum = Math.max(maxColNum, indexChom);
        maxColNum = Math.max(maxColNum, indexStart);
        maxColNum = Math.max(maxColNum, indexEnd);

        maxColNum = Math.max(maxColNum, indexJcK);
        maxColNum = Math.max(maxColNum, indexK2K);

        ReferenceGenome genome = new ReferenceGenome(0, 0, 0);
        String currentLine = null;
        String currChr = null;
        StringBuilder tmpBuffer = new StringBuilder();
        long lineCounter = 0;

        File dataFile = new File(vAFile);
//        LineReader br = null;
        BufferedReader br=null;

        Set<String> duplCNVs = new HashSet<String>();

        boolean incomplete = true;



        String varID = null;
        int startPos = -1;
        int endPos = -1;
        String subType = null;
        int sampleSize = -1;
        double jcKScore = -1;
        double k2KScore = -1;
        String[] cells;
        int cnvNum = 0;
        int dupCNVNum = 0;
        try {
            if (dataFile.exists() && dataFile.getName().endsWith(".zip")) {
//                br = new CompressedFileReader(dataFile);
                br=LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
            } else {
                if (dataFile.exists() && dataFile.getName().endsWith(".gz")) {
//                    br = new CompressedFileReader(dataFile);
                     br=LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
                } else {
                    if (dataFile.exists()) {
//                        br = new AsciiLineReader(dataFile);
                        br=LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
                    } else {
                        throw new Exception("No input file: " + dataFile.getCanonicalPath());
                    }
                }
            }

            //skip to the head line 
//            br.readLineS();
            br.readLine();
                   

            while ((currentLine = br.readLine()) != null) {
                lineCounter++;
                //System.out.println(currentLine);
                // StringTokenizer st = new StringTokenizer(currentLine.trim(),"\t");
                cells = currentLine.split("\t", -1);
                //initialize varaibles
                incomplete = true;
                currChr = null;

                varID = null;
                startPos = -1;
                endPos = -1;
                subType = null;
                jcKScore = 0;
                k2KScore = 0;
                sampleSize = 0;

                maxColNum = indexVarID;
                maxColNum = Math.max(maxColNum, indexChom);
                maxColNum = Math.max(maxColNum, indexStart);
                maxColNum = Math.max(maxColNum, indexEnd);

                maxColNum = Math.max(maxColNum, indexJcK);
                maxColNum = Math.max(maxColNum, indexK2K);



                varID = cells[indexVarID];
                currChr = cells[indexChom].substring(3);
                startPos = Integer.parseInt(cells[indexStart]);
                endPos = Integer.parseInt(cells[indexEnd]);

                if (!cells[indexJcK].isEmpty()) {
                    jcKScore = Double.parseDouble(cells[indexJcK]);
                }

                if (!cells[indexK2K].isEmpty()) {
                    k2KScore = Double.parseDouble(cells[indexK2K]);
                }


                RefDup mrna = new RefDup(varID, startPos, endPos, jcKScore, k2KScore);
                String ids = mrna.getDescription() + ":" + currChr + ":" + mrna.getStart() + ":" + mrna.getEnd();

                int[] poss = genome.getmRNAPos(ids);
                if (poss != null) {
                    mrna = genome.getDup(poss);
                    if (!currChr.equals(STAND_CHROM_NAMES[poss[0]])) {
                        //note a transcript can be mapped onto multiple locations
                        String info = "Duplicated SuperDup items: " + mrna.getDescription();
                        System.out.println(info);
                        dupCNVNum++;
                        continue;
                    }
                } else {
                    cnvNum++;
                    genome.addRefDup(mrna, currChr);
                }
                // System.out.println(currentLine);
            }
        } catch (NumberFormatException nex) {
            String info = nex.toString() + " when parsing at line " + lineCounter + ": " + currentLine;
            // LOG.error(nex, info);
            throw new Exception(info);
        }
        br.close();
        genome.sortDupMakeIndexonChromosomes();
       // LOG.info(cnvNum + " reference super duplicate regions have been read!");
        if (dupCNVNum > 0) {
            LOG.info(dupCNVNum + " duplicated reference super duplicate items!");
        }
        return genome;
    }
}
