/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import java.io.BufferedReader;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.cobi.kggseq.entity.Genome;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.text.Util;

/**
 *
 * @author mxli
 */
public class SimpleFormatParser {

    private static final Logger LOG = Logger.getLogger(SimpleFormatParser.class);

    public Genome readCancerGenomeVariantFormat(String tmpGenomePath, String vAFile, boolean needProgressionIndicator) throws Exception {

        String currentLine = null;
        String currChr = null;
        StringBuilder tmpBuffer = new StringBuilder();
        long lineCounter = 0;

        File dataFile = new File(vAFile);
        if (!dataFile.exists()) {
            throw new Exception("No input of variants: " + dataFile.getCanonicalPath());
        }

        BufferedReader br = LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());

        int makerPostionStart = 0;
        int makerPostionEnd = 0;

        String ref = null;
        String alt = null;
        boolean incomplete = true;
        int acceptVarNum = 0;

        int iCol = 0;
        //temp variables
        int indelNum = 0;
        boolean isIndel = false;

        StringBuilder sb = new StringBuilder();
        String comments = null;
        Integer count;

        Map<String, Integer> varCountMap = new HashMap<String, Integer>();
        int avialbleFeatureIndex = 0;
        try {
            iCol = 0;
//skip head line
            currentLine = br.readLine();

            int indexCHROM = -1;
            int indexPosStart = -1;
            int indexPosEnd = -1;
            int indexREF = -1;
            int indexALT = -1;
            int indexType = -1;
            int indexCount = -1;

            StringTokenizer st = new StringTokenizer(currentLine.trim());
            while (st.hasMoreTokens()) {
                String ts = st.nextToken().trim();
                if (ts.equals("chr")) {
                    indexCHROM = iCol;
                } else if (ts.equals("pos")) {
                    indexPosStart = iCol;
                    indexPosEnd = iCol;
                } else if (ts.equals("ref_allele")) {
                    indexREF = iCol;
                } else if (ts.equals("newbase")) {
                    indexALT = iCol;
                } else if (ts.equals("classification")) {
                    indexType = iCol;
                } else if (ts.equals("count")) {
                    indexCount = iCol;
                }
                iCol++;
            }

            int maxColNum = indexCHROM;
            maxColNum = Math.max(maxColNum, indexPosStart);
            maxColNum = Math.max(maxColNum, indexPosEnd);
            maxColNum = Math.max(maxColNum, indexREF);
            maxColNum = Math.max(maxColNum, indexALT);
            maxColNum = Math.max(maxColNum, indexType);
            maxColNum = Math.max(maxColNum, indexCount);

            currentLine = br.readLine();
            if (currentLine == null) {
                return null;
            }
            do {
                lineCounter++;
                if (needProgressionIndicator && lineCounter % 10000 == 0) {
                    String prog = String.valueOf(lineCounter);
                    System.out.print(prog);
                    char[] backSpaces = new char[prog.length()];
                    Arrays.fill(backSpaces, '\b');
                    System.out.print(backSpaces);
                }
                st = new StringTokenizer(currentLine.trim());
               // System.out.println(currentLine);

                //initialize varaibles
                incomplete = true;
                currChr = null;
                makerPostionStart = -1;
                makerPostionEnd = -1;
                ref = null;
                alt = null;
                isIndel = false;
                comments = null;
                count = null;
                /*
                 0	1	2	3	4	5	6	7	8	9	10
                 ttype	patient	gene	classification	type	chr	pos	ref_allele	newbase	context65	cons46
                 BRCA	BR-0001	Unknown	SNP	IGR	1	102094413	T	C	62	50
                 BRCA	BR-0001	Unknown	SNP	IGR	1	105351609	T	C	49	50
                 BRCA	BR-0001	Unknown	SNP	IGR	1	105803301	T	G	49	51
                 BRCA	BR-0001	Unknown	SNP	IGR	1	106065970	G	A	34	51
                 BRCA	BR-0001	Unknown	SNP	IGR	1	106147918	T	C	49	52
                 BRCA	BR-0001	Unknown	SNP	IGR	1	111047687	A	G	1	55
                 BRCA	BR-0001	Unknown	DEL	IGR	1	111460251	TTTTTGTTTTTTTG	-	64	27
                 BRCA	BR-0001	RSBN1	SNP	Intron	1	114350413	G	A	48	39
                 BRCA	BR-0001	DENND2C	DEL	Intron	1	115091329	A	-	16	50
                
                 */
                for (iCol = 0; iCol <= maxColNum; iCol++) {
                    if (st.hasMoreTokens()) {
                        tmpBuffer.delete(0, tmpBuffer.length());
                        tmpBuffer.append(st.nextToken().trim());
                        if (iCol == indexCHROM) {
                            currChr = tmpBuffer.toString();
                        } else if (iCol == indexPosStart) {
                            makerPostionStart = Util.parseInt(tmpBuffer.toString());
                            makerPostionEnd = makerPostionStart;
                        } else if (iCol == indexPosEnd) {
                        } else if (iCol == indexREF) {
                            ref = tmpBuffer.toString();
                        } else if (iCol == indexALT) {
                            alt = tmpBuffer.toString();
                        } else if (iCol == indexType) {
                            comments = tmpBuffer.toString();
                            if (!comments.equals("SNP")) {
                                break;
                            }
                        } else if (iCol == indexCount) {
                            count = Integer.parseInt(tmpBuffer.toString());
                        }

                    } else {
                        break;
                    }
                    if (iCol >= maxColNum) {
                        incomplete = false;
                    }
                }

                if (incomplete) {
                    continue;
                }

                //format in KGG
                if (currChr.toLowerCase().startsWith("chr")) {
                    currChr = currChr.substring(3);
                }
                if (currChr.equals("23")) {
                    currChr = "X";
                } else if (currChr.equals("24")) {
                    currChr = "Y";
                } else if (currChr.equals("25")) {
                    currChr = "M";
                }
                if (ref.equals("-") || alt.equals("-")) {
                    continue;
                }

                String label = currChr + ":" + makerPostionStart + ":" + ref + ":" + alt;

                Integer count1 = varCountMap.get(label);
                if (count1 == null) {
                    if (count == null) {
                        varCountMap.put(label, 1);
                    } else {
                        varCountMap.put(label, count);
                    }
                } else {
                    if (count == null) {
                        varCountMap.put(label, count1 + 1);
                    } else {
                        varCountMap.put(label, count + count1);
                    }
                }
                acceptVarNum++;
               // if (acceptVarNum==10000) break;
            } while ((currentLine = br.readLine()) != null);

        } catch (NumberFormatException nex) {
            String info = nex.toString() + " when parsing at line " + lineCounter + ": " + currentLine;
            // LOG.error(nex, info);
            throw new Exception(info);
        }
        br.close();

        StringBuilder message = new StringBuilder();
        message.append('\n').append(lineCounter).append(" lines are scanned;  and ").append(acceptVarNum).append(" variants (").append(indelNum).append(" indels) are valid in ").append(vAFile);
        LOG.info(message);
        if (acceptVarNum == 0) {
            System.exit(1);
        }

        Genome genome = new Genome("Simple", tmpGenomePath);
        genome.addVariantFeatureLabel("Comments");

        for (Map.Entry<String, Integer> item : varCountMap.entrySet()) {
            String[] varInfo = item.getKey().split(":");
            makerPostionStart = Util.parseInt(varInfo[1]);
            Variant var = new Variant(makerPostionStart, varInfo[2], new String[]{varInfo[3]});
            var.setFeatureValue(avialbleFeatureIndex, item.getValue().toString());
            genome.addVariant(varInfo[0], var);
        }

        genome.buildVariantIndexMapOnChromosomes();
        genome.setVarNum(acceptVarNum);
        varCountMap.clear();
        genome.removeTempFileFromDisk();
        genome.writeChromsomeToDiskClean();
        return genome;
    }

}
