/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import cern.colt.list.IntArrayList;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import org.apache.log4j.Logger;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.GlobalManager;
import org.cobi.kggseq.entity.Chromosome;
import org.cobi.kggseq.entity.Genome;
import org.cobi.kggseq.entity.Individual;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.file.LocalFileFunc;

/**
 *
 * @author mxli
 */
public class BinaryGtyProcessor implements Constants {

    private static final Logger LOG = Logger.getLogger(BinaryGtyProcessor.class);
    protected String pedigreeFileName;
    protected String mapFileName;
    //if kggseqBinaryFileName is not null, it must be a kggseq binary file
    protected String kggseqBinaryFileName;

//tmp variants
    static int byteIndex1;
    static int byteIndex2;
    static int bitNum;
    static boolean[] bits = new boolean[1000];
    static StringBuilder stringBuilder = new StringBuilder();

    public BinaryGtyProcessor(String prefixName) {
        this.pedigreeFileName = prefixName + ".fam";
        this.mapFileName = prefixName + ".kim";
        this.kggseqBinaryFileName = prefixName + ".ked";
    }

    static public int[] getUnphasedGtyAt(int[] gtys, int alleleNum, int base, int indivID) {
        bitNum = base * indivID;
        for (int i = 0; i < base; i++) {
            byteIndex1 = (bitNum + i) / 32;
            byteIndex2 = (bitNum + i) % 32;
            bits[i] = (gtys[byteIndex1] & GlobalManager.intOpers[byteIndex2]) == GlobalManager.intOpers[byteIndex2];
        }
        switch (base) {
            case 2:
                /*
                 missing	  Reference homozygous	 Heterozygous 	Alternative homozygous
                 VCF genotype	./.	             0/0	       0/1	         1/1
                 Bits           00                    01	       10	         11
                 Order	         0	              1	                2	         3        
                 */
                if (bits[0] && bits[1]) {
                    return new int[]{1, 1};
                } else if (!bits[0] && bits[1]) {
                    return new int[]{0, 0};
                } else if (bits[0] && !bits[1]) {
                    return new int[]{0, 1};
                } else if (!bits[0] && !bits[1]) {
                    return null;
                }
                break;
            default:
                stringBuilder.delete(0, stringBuilder.length());
                for (int i = 0; i < base; i++) {
                    if (bits[i]) {
                        stringBuilder.append(1);
                    } else {
                        stringBuilder.append(0);
                    }
                }
                stringBuilder.append(':').append(alleleNum);
                int[] alleles = GlobalManager.codingUnphasedGtyCodingMap.get(stringBuilder.toString());
                // String infor = "Sorry!!! squence variants with over 4 alleles are not supported and will be ignored!";
                // System.out.println(infor);
                return alleles;
        }
        return null;
    }

    static public void getPhasedGtyAt(int[] gtys, int base, int indivID, boolean[] bits1) {
        bitNum = base * indivID;
        for (int i = 0; i < base; i++) {
            byteIndex1 = (bitNum + i) / 32;
            byteIndex2 = (bitNum + i) % 32;
            bits1[i] = (gtys[byteIndex1] & GlobalManager.intOpers[byteIndex2]) == GlobalManager.intOpers[byteIndex2];
        }
    }

    static public int[] getPhasedGtyAt(int[] gtys, int alleleNum, int base, int indivID) {
        bitNum = base * indivID;
        for (int i = 0; i < base; i++) {
            byteIndex1 = (bitNum + i) / 32;
            byteIndex2 = (bitNum + i) % 32;
            bits[i] = (gtys[byteIndex1] & GlobalManager.intOpers[byteIndex2]) == GlobalManager.intOpers[byteIndex2];
        }
        switch (base) {
            case 2:
                /*       
                 missing	Reference homozygous	Heterozygous 	Heterozygous 	Alternative homozygous
                 VCF genotype	.|.	0|0	0|1	1|0	1|1
                 Bits	        000  	001	010	011	100
                 Order	0	1	2	3	4                
               
                 II.II Tri-allelic sequence variant (4 bits)
                 missing 	Reference homozygous 	Heterozygous 	Heterozygous 	Heterozygous 	Heterozygous 	Alternative homozygous
                 VCF genotype 	.|. 	0|0 	0|1 	0|2 	1|0 	1|1 	1|2
                 Bits      	000 	0001 	0010 	0011 	0100 	0101 	0110
                 Decimal 	0 	1 	2 	3 	4 	5 	6
                 Heterozygous 	Heterozygous 	Alternative homozygous
                 VCF genotype 	2|0 	2|1 	2|2
                 Bits     	0111 	1000 	1001
                 Decimal 	7 	8 	9     
                 */
                if (!bits[0] && !bits[1] && bits[2]) {
                    return new int[]{0, 0};
                } else if (!bits[0] && bits[1] && !bits[2]) {
                    return new int[]{0, 1};
                } else if (!bits[0] && bits[1] && bits[2]) {
                    return new int[]{1, 0};
                } else if (bits[0] && !bits[1] && !bits[2]) {
                    return new int[]{1, 1};
                } else if (!bits[0] && !bits[1] && !bits[2]) {
                    return null;
                }
                break;
            default:
                stringBuilder.delete(0, stringBuilder.length());
                for (int i = 0; i < base; i++) {
                    if (bits[i]) {
                        stringBuilder.append(1);
                    } else {
                        stringBuilder.append(0);
                    }
                }
                stringBuilder.append(':').append(alleleNum);
                int[] alleles = GlobalManager.codingPhasedGtyCodingMap.get(stringBuilder.toString());
                // String infor = "Sorry!!! squence variants with over 4 alleles are not supported and will be ignored!";
                // System.out.println(infor);
                return alleles;
        }
        return null;
    }

    static public void getUnphasedGtyAt(int[] gtys, int base, int indivID, boolean[] bits1) {
        bitNum = base * indivID;
        for (int i = 0; i < base; i++) {
            byteIndex1 = (bitNum + i) / 32;
            byteIndex2 = (bitNum + i) % 32;
            bits1[i] = (gtys[byteIndex1] & GlobalManager.intOpers[byteIndex2]) == GlobalManager.intOpers[byteIndex2];
        }
    }

    public BinaryGtyProcessor(String pedigreeFileName, String mapFileName, String kggseqBinaryFileName) {
        this.pedigreeFileName = pedigreeFileName;
        this.mapFileName = mapFileName;
        this.kggseqBinaryFileName = kggseqBinaryFileName;
    }

    public boolean avaibleFiles() {
        File file = new File(pedigreeFileName);
        if (!file.exists()) {
            return false;
        }
        file = new File(mapFileName);
        if (!file.exists()) {
            return false;
        }
        file = new File(kggseqBinaryFileName);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    public void readPedigreeFile(List<Individual> indivList) throws Exception {
        StringBuilder tmpBuffer = new StringBuilder();
        String line = null;
        String delimiter = "\t\" \",/";
        BufferedReader br = null;
        File file = new File(pedigreeFileName + ".gz");
        if (file.exists()) {
            br = LocalFileFunc.getBufferedReader(file.getCanonicalPath());
        } else {
            file = new File(pedigreeFileName);
            if (file.exists()) {
                br = LocalFileFunc.getBufferedReader(file.getCanonicalPath());
            } else {
                LOG.error(file.getCanonicalPath() + " does not exist!");
                return;
            }
        }

        while ((line = br.readLine()) != null) {
            line = line.toUpperCase();
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
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(tokenizer.nextToken().trim());
            indiv.setGender(Integer.valueOf(tmpBuffer.toString()));
            // indiv.setLabelInChip(indiv.getFamilyID() + "@*@" + indiv.getIndividualID());
            indiv.setLabelInChip(indiv.getIndividualID());
            if (tokenizer.hasMoreTokens()) {
                indiv.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
            }

            //System.out.println(indiv.getLabelInChip());
            indivList.add(indiv);
        }
        br.close();
    }

    // public void calcualte
    public boolean readBinaryGenotype(List<Individual> subjectList, Genome genome) throws Exception {
        int indiSize = subjectList.size();
        IntArrayList caseSetID = new IntArrayList();
        IntArrayList controlSetID = new IntArrayList();

        for (int i = 0; i < indiSize; i++) {
            if (subjectList.get(i).getAffectedStatus() == 2) {
                caseSetID.add(i);
            } else if (subjectList.get(i).getAffectedStatus() == 1) {
                controlSetID.add(i);
            }
        }
        int subID = 0;
        int caseNum = caseSetID.size();
        int controlNum = controlSetID.size();

        String info = ("Reading genotype bit-file from [ " + kggseqBinaryFileName + " ] \n");
        //openkggseqPedFormat
        DataInputStream in = null;
        in = new DataInputStream(new BufferedInputStream(new FileInputStream(kggseqBinaryFileName)));

        /*
         File file = new File(kggseqBinaryFileName + ".gz");
         if (file.exists()) {
         in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file.getCanonicalPath()))));
         } else {
         file = new File(kggseqBinaryFileName);
         if (file.exists()) {
         in = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getCanonicalPath())));
         } else {
         LOG.error(file.getCanonicalPath() + " does not exist!");
         return false;
         }
         }
         */
        byte bt1 = in.readByte();
        byte bt2 = in.readByte();
        // System.out.println(bytesToHexString(new byte[]{bt1}));
        // System.out.println(bytesToHexString(new byte[]{bt2}));
        // printBitSet(b); 
        // Magic numbers for .ked file: 10011110 10000010  =9E 82
        if (bt1 != -98 || bt2 != -126) {
            throw new Exception("The " + kggseqBinaryFileName + " is not a valid kggseq binary file!!!");
        }
        boolean isPhased = false;
        bt1 = in.readByte();
        if (bt1 == 1) {
            isPhased = true;
            genome.setIsPhasedGty(true);
        }

        int indiviNum = subjectList.size();
        int alleleNum = 0;
        Chromosome[] chroms = genome.getChromosomes();
        int base = 0;
        int bitNum = 0;
        int intNum = 0;
        int[] gtys = null;
        boolean needAccoundAffect = false;
        boolean needAccoundUnaffect = false;
        int g11 = 0;
        int g12 = 0;
        int g22 = 0;
        if (caseNum > 0) {
            needAccoundAffect = true;
        }
        if (controlNum > 0) {
            needAccoundUnaffect = true;
        }

        for (int chromID = 0; chromID < chroms.length; chromID++) {
            if (chroms[chromID] == null) {
                continue;
            }
            for (Variant var : chroms[chromID].variantList) {
                alleleNum = var.getAltAlleles().length + 1;

                if (isPhased) {
                    //to do 
                } else {
                    base = GlobalManager.unphasedAlleleBitMap.get(alleleNum);
                    bitNum = base * indiviNum;
                    intNum = bitNum / 32;
                    if (bitNum % 32 != 0) {
                        intNum++;
                    }
                    var.encodedGty = new int[intNum];
                    for (int i = 0; i < intNum; i++) {
                        var.encodedGty[i] = in.readInt();
                    }
                }
                if (!needAccoundAffect && !needAccoundUnaffect) {
                    g11 = 0;
                    g12 = 0;
                    g22 = 0;
                    for (int j = 0; j < indiSize; j++) {
                        subID = j;
                        if (isPhased) {
                            //gtys = subjectList.get(subID).markerGtySetArray.getPhasedGtyAt(var.genotypeIndex, var.getAltAlleles().length + 1);
                        } else {
                            gtys = BinaryGtyProcessor.getUnphasedGtyAt(var.encodedGty, alleleNum, base, subID);
                        }
                        if (gtys == null) {
                            continue;
                        }
                        if (gtys[0] != gtys[1]) {
                            g12++;
                        } else {
                            if (gtys[0] == 0) {
                                g11++;
                            } else {
                                g22++;
                            }
                        }
                    }
                    var.setAffectedRefHomGtyNum(g11);
                    var.setAffectedHetGtyNum(g12);
                    var.setAffectedAltHomGtyNum(g22);
                } else {
                    if (needAccoundAffect) {
                        g11 = 0;
                        g12 = 0;
                        g22 = 0;
                        for (int j = 0; j < caseNum; j++) {
                            subID = caseSetID.getQuick(j);
                            if (isPhased) {
                                //gtys = subjectList.get(subID).markerGtySetArray.getPhasedGtyAt(var.genotypeIndex, var.getAltAlleles().length + 1);
                            } else {
                                gtys = BinaryGtyProcessor.getUnphasedGtyAt(var.encodedGty, alleleNum, base, subID);
                            }
                            if (gtys == null) {
                                continue;
                            }
                            if (gtys[0] != gtys[1]) {
                                g12++;
                            } else {
                                if (gtys[0] == 0) {
                                    g11++;
                                } else {
                                    g22++;
                                }
                            }
                        }
                        var.setAffectedRefHomGtyNum(g11);
                        var.setAffectedHetGtyNum(g12);
                        var.setAffectedAltHomGtyNum(g22);
                    }

                    if (needAccoundUnaffect) {
                        g11 = 0;
                        g12 = 0;
                        g22 = 0;
                        for (int i = 0; i < controlNum; i++) {
                            subID = controlSetID.getQuick(i);
                            if (isPhased) {
                                //gtys = subjectList.get(subID).markerGtySetArray.getPhasedGtyAt(var.genotypeIndex, var.getAltAlleles().length + 1);
                            } else {
                                gtys = BinaryGtyProcessor.getUnphasedGtyAt(var.encodedGty, alleleNum, base, subID);
                            }
                            if (gtys == null) {
                                continue;
                            }
                            if (gtys[0] != gtys[1]) {
                                g12++;
                            } else {
                                if (gtys[0] == 0) {
                                    g11++;
                                } else {
                                    g22++;
                                }
                            }
                        }
                        var.setUnaffectedRefHomGtyNum(g11);
                        var.setUnaffectedHetGtyNum(g12);
                        var.setUnaffectedAltHomGtyNum(g22);
                    }
                }

            }

        }
        in.close();
        return false;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public Genome readVariantsMapFile(int[] counts) throws Exception {
        BufferedReader br = null;
        File file = new File(mapFileName + ".gz");
        if (file.exists()) {
            br = LocalFileFunc.getBufferedReader(file.getCanonicalPath());
        } else {
            file = new File(mapFileName);
            if (file.exists()) {
                br = LocalFileFunc.getBufferedReader(file.getCanonicalPath());
            } else {
                LOG.error(file.getCanonicalPath() + " does not exist!");
                return null;
            }
        }

        String line = null;

        int chromIndex = 0;
        int labelIndex = 1;
        int positionIndex = 3;
        int refIndex = 4;
        int altIndex = 5;
        int maxIndex = altIndex;

        Genome genome = new Genome("KGGSeqGenome", "ked");
        genome.removeTempFileFromDisk();
        String chrom;
        int position = -1;
        String label = null;
        String ref = null;
        String alt = null;
        StringBuilder tmpBuffer = new StringBuilder();
        int index;
        int effectiveSNPNum = 0;
        int lineCounter = 0;
        int indelNum = 0;
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                lineCounter++;
                StringTokenizer tokenizer = new StringTokenizer(line);
                index = 0;

                chrom = null;
                position = -1;
                label = null;
                ref = null;
                alt = null;
                while (tokenizer.hasMoreTokens()) {
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(tokenizer.nextToken().trim());

                    if (index == chromIndex) {
                        chrom = tmpBuffer.toString();
                    } else if (index == positionIndex) {
                        position = Integer.parseInt(tmpBuffer.toString());
                    } else if (index == labelIndex) {
                        label = tmpBuffer.toString();
                    } else if (index == refIndex) {
                        ref = tmpBuffer.toString();
                    } else if (index == altIndex) {
                        alt = tmpBuffer.toString();
                    }

                    if (index == maxIndex) {
                        break;
                    }
                    index++;
                }

                effectiveSNPNum++;
                if (alt.indexOf('+') >= 0 || alt.indexOf('-') >= 0) {
                    indelNum++;
                }
                Variant var = new Variant(position, ref, alt.split(","));

                var.setLabel(label);
                genome.addVariant(chrom, var);
            }

            counts[0] = lineCounter;
            counts[1] = effectiveSNPNum;
            counts[2] = indelNum;

            /*
             StringBuilder runningInfo = new StringBuilder();
             runningInfo.append("The number of SNPs  in map file ");
             runningInfo.append(mapFile.getName());
             runningInfo.append(" is ");
             runningInfo.append(effectiveSNPNum);
             runningInfo.append(".");
             */
        } finally {
            br.close();
        }
        genome.setVarNum(effectiveSNPNum);
        return genome;
    }

    public static void main(String[] args) {
        try {
            //  byte byteInfo=-128;
            //  System.out.println(BitByteUtil.byteToBinaryString((byte) (byteInfo )));
            //                   System.out.println(BitByteUtil.byteToBinaryString((byte) (byteInfo>>> 2&0X3F)));                             
            List<Individual> indivList = new ArrayList<Individual>();
            BinaryGtyProcessor bgp = new BinaryGtyProcessor("./kggseq");
            bgp.readPedigreeFile(indivList);
            Genome genome = bgp.readVariantsMapFile(null);
            bgp.readBinaryGenotype(indivList, genome);
            //   genome.export2FlatTextPlink(indivList, "./test");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
