/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;
import org.apache.log4j.Logger;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.GlobalManager;
import org.cobi.kggseq.entity.Genome;
import org.cobi.kggseq.entity.Individual;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.text.Util;
import org.cobi.util.thread.Task;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 *
 * @author mxli
 */
public class VCFParseTask extends Task implements Callable<String>, Constants {
    
    private static final Logger LOG = Logger.getLogger(VCFParseTask.class);
    Map<String, Integer> chromNameIndexMap = new HashMap<String, Integer>();
    final String UNKNOWN_CHROM_NAME0 = "Un";
    final String UNKNOWN_CHROM_NAME1 = "GL";
    List<Variant>[] varChroms = null;
    
    String storagePath;
    Kryo kryo = new Kryo();

    //As array is much faster than list; I try to use array when it does not was
    int threadID = -1;
    IntArrayList caeSetID;
    IntArrayList controlSetID;
    
    int indexCHROM;
    int indexPOS;
    int indexID;
    int indexREF;
    int indexALT;
    int indexQUAL;
    int indexFILTER;
    int indexFORMAT;
    int indexINFO;
    
    double avgSeqQualityThrehsold;
    double minMappingQual;
    double maxStrandBias;
    double maxFisherStrandBias;
    double gtyQualityThrehsold;
    int minGtySeqDepth;
    int minSeqDepth;
    double altAlleleFracRefHomThrehsold;
    double altAlleleFractHetThrehsold;
    double altAlleleFractAltHomThrehsold;
    int minSecondPL;
    double minBestGP;
    int minOBS;
    double sampleMafOver;
    double sampleMafLess;
    int maxGtyAlleleNum;
    
    Set<String> vcfLabelSet;
    
    boolean noGtyVCF;
    boolean considerSNP;
    boolean considerIndel;
    boolean needGty;
    boolean needReadsInfor;
    boolean needGtyQual;

    //result variables
    int ignoredLowQualGtyNum = 0;
    int ignoredLowDepthGtyNum = 0;
    int ignoredBadAltFracGtyNum = 0;
    int ignoredLowPLGtyNum = 0;
    int ignoredLowGPGtyNum = 0;
    int ignoreStrandBiasSBNum = 0;
    int missingGtyNum = 0;
    int formatProbVarNum = 0;
    int filterOutLowQualNum = 0;
    int vcfFilterOutNum = 0;
    int ignoredLineNumMinOBS = 0;
    int ignoredLineNumMinMAF = 0;
    int ignoredLineNumMaxMAF = 0;
    int nonRSVariantNum = 0;
    int ignoreMappingQualNum = 0;
    int ignoreStrandBiasFSNum = 0;
    int indelNum = 0, snvNum = 0;
    int ignoredInproperChromNum = 0;
    int ignoredVarBymaxGtyAlleleNum = 0;
    
    int totalVarNum = 0;
    int totalAcceptVarNum = 0;
    //temp variables to save time
    boolean checkVCFfilter = false;
    double sampleMafOverC = 0;
    double sampleMafLessC = 0;
    int[] gtyQuality = null;
    String[] gtys = null;
    int[] gtyDepth = null;
    String[] readCounts = null;
    String[] gtyQualDescrip = null;
    float[] readFractions = null;
    int[] secondMostGtyPL = null;
    int[] bestGtyGP = null;
    boolean needAccoundAffect = false;
    boolean needAccoundUnaffect = false;
    boolean needMAFQCOver = false;
    boolean needMAFQCLess = false;
    double maf = 0;
    boolean hasOrginalGenome = false;
    int effectiveIndivNum = 0;
    int totalPedSubjectNum = 0;
    int maxVcfIndivNum = 0;
    int maxEffectiveColVCF = -1;
    
    int controlSize;
    int caseSize;
    
    String currChr = null;
    int makerPostion = 0;
    String varLabel = null;
    
    int obsS;
    String ref = null;
    String alt = null;
    boolean incomplete = true;
    
    double avgSeqQuality = 0;
    double mappingQual;
    double strandBias;
    
    int gtyIndexInInfor = -1;
    int gtyQualIndexInInfor = -1;
    int gtyDepthIndexInInfor = -1;
    int gtyAlleleDepthIndexInInfor = -1;
    int gtyAltAlleleFracIndexInInfor = -1;
    int gtyPLIndexInInfor = -1;
    int gtyGPIndexInInfor = -1;
    
    boolean hasIndexGT = false;
    boolean hasIndexGQ = false;
    boolean hasIndexDP = false;
    boolean hasIndexAD = false;
    boolean hasIndexFA = false;
    boolean hasIndexPL = false;
    boolean hasIndexGP = false;
    
    BufferedReader br;
    int maxVarNum;

    //temp variables
    int iGty = 0;
    int index1 = 0;
    int index2 = 0;
    
    int t = 0;
    int pl = 0;
    int p = 0;
    
    int index = 0;
    int g11 = 0, g12 = 0, g22 = 0;
    int indexA, indexB;
    boolean isLowQualBreak = false;
    
    boolean isIndel = false;
    boolean isInvalid = false;
    int alleleNum = 0;
    
    int maxIndex2 = -1;
    int ii = 0;
    String tmpStr = null;
    String depA = null;
    String depB = null;
    StringBuilder tmpSB = new StringBuilder();
    BufferedWriter[] vcfWriters;
    
    public String getStoragePath() {
        return storagePath;
    }
    
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
    
    public int getTotalVarNum() {
        return totalVarNum;
    }
    
    public int getTotalAcceptVarNum() {
        return totalAcceptVarNum;
    }
    
    public void setBr(BufferedReader br) {
        this.br = br;
    }
    
    public void setMaxVarNum(int maxVarNum) {
        this.maxVarNum = maxVarNum;
    }
    
    public int getFormatProbVarNum() {
        return formatProbVarNum;
    }
    
    public int getFilterOutLowQualNum() {
        return filterOutLowQualNum;
    }
    
    public int getVcfFilterOutNum() {
        return vcfFilterOutNum;
    }
    
    public int getMissingGtyNum() {
        return missingGtyNum;
    }
    
    public int getIgnoredLowQualGtyNum() {
        return ignoredLowQualGtyNum;
    }
    
    public int getIgnoredLowDepthGtyNum() {
        return ignoredLowDepthGtyNum;
    }
    
    public int getIgnoredBadAltFracGtyNum() {
        return ignoredBadAltFracGtyNum;
    }
    
    public int getIgnoredLowPLGtyNum() {
        return ignoredLowPLGtyNum;
    }
    
    public int getIgnoredLowGPGtyNum() {
        return ignoredLowGPGtyNum;
    }
    
    public int getIgnoreStrandBiasSBNum() {
        return ignoreStrandBiasSBNum;
    }
    
    public int getIgnoredLineNumMinOBS() {
        return ignoredLineNumMinOBS;
    }
    
    public int getIgnoredLineNumMinMAF() {
        return ignoredLineNumMinMAF;
    }
    
    public int getIgnoredLineNumMaxMAF() {
        return ignoredLineNumMaxMAF;
    }
    
    public void setIgnoredLineNumMaxMAF(int ignoredLineNumMaxMAF) {
        this.ignoredLineNumMaxMAF = ignoredLineNumMaxMAF;
    }
    
    public int getIgnoredVarBymaxGtyAlleleNum() {
        return ignoredVarBymaxGtyAlleleNum;
    }
    
    public int getIgnoreMappingQualNum() {
        return ignoreMappingQualNum;
    }
    
    public int getIgnoreStrandBiasFSNum() {
        return ignoreStrandBiasFSNum;
    }
    
    public int getIndelNum() {
        return indelNum;
    }
    
    public int getSnvNum() {
        return snvNum;
    }
    
    public int getIgnoredInproperChromNum() {
        return ignoredInproperChromNum;
    }
    
    public void prepareTempVariables() {
        checkVCFfilter = false;
        if (vcfLabelSet != null) {
            checkVCFfilter = true;
        }
        sampleMafOverC = 1 - sampleMafOver;
        sampleMafLessC = 1 - sampleMafLess;
        needAccoundAffect = false;
        if (!caeSetID.isEmpty()) {
            needAccoundAffect = true;
        }
        needAccoundUnaffect = false;
        if (!controlSetID.isEmpty()) {
            needAccoundUnaffect = true;
        }
        if (orgGenome != null) {
            hasOrginalGenome = true;
        }
        
        if (sampleMafOver >= 0) {
            needMAFQCOver = true;
        }
        if (sampleMafLess < 0.5) {
            needMAFQCLess = true;
        }
        
        effectiveIndivNum = effectIndivIDInVCF.size();
        totalPedSubjectNum = pedVCFIDMap.length;
        maxEffectiveColVCF = indexCHROM;
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexPOS);
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexID);
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexREF);
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexALT);
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexQUAL);
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexFILTER);
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexFORMAT);
        maxEffectiveColVCF = Math.max(maxEffectiveColVCF, indexINFO);
        
        if (!noGtyVCF) {
            for (int i = 0; i < effectIndivIDInVCF.size(); i++) {
                if (maxEffectiveColVCF < effectIndivIDInVCF.getQuick(i) + indexFORMAT + 1) {
                    maxEffectiveColVCF = effectIndivIDInVCF.getQuick(i) + indexFORMAT + 1;
                    maxVcfIndivNum = effectIndivIDInVCF.getQuick(i);
                }
            }
            
            maxVcfIndivNum += 1;
            
            gtyQuality = new int[maxVcfIndivNum];
            gtys = new String[maxVcfIndivNum];
            gtyDepth = new int[maxVcfIndivNum];
            //read counts 0, 1,2,3
            readCounts = new String[maxVcfIndivNum];
            gtyQualDescrip = new String[maxVcfIndivNum];
            secondMostGtyPL = new int[maxVcfIndivNum];
            readFractions = new float[maxVcfIndivNum];
            bestGtyGP = new int[maxVcfIndivNum];
            
            Arrays.fill(readFractions, Float.NaN);
            controlSize = controlSetID.size();
            caseSize = caeSetID.size();
        }
        
    }
    
    private final void tokenize(String string, char delimiter, int maxIndex, String[] temp) {
        int wordCount = 0;
        int i = 0;
        int j = string.indexOf(delimiter);
        
        while (j >= 0) {
            temp[wordCount] = string.substring(i, j);
            if (wordCount >= maxIndex) {
                wordCount++;
                break;
            }
            wordCount++;
            i = j + 1;
            j = string.indexOf(delimiter, i);
            
        }
        if (wordCount <= maxIndex) {
            if (i < string.length()) {
                temp[wordCount++] = string.substring(i);
            }
        }
    }
    
    public void tokenizeIngoreConsec(String string, char delimiter, String[] temp) {
        int wordCount = 0;
        int i = 0;
        int j = string.indexOf(delimiter);
        
        while (j >= 0) {
            if (i < j) {
                temp[wordCount++] = string.substring(i, j);
            }
            i = j + 1;
            j = string.indexOf(delimiter, i);
        }
        
        if (i < string.length()) {
            temp[wordCount++] = string.substring(i);
        }
    }
    
    public int parseVariantsInFileOnlyFastToken() {
        String currentLine = null;
        int acceptVarNum = 0;
        int base = 0;
        try {

            //at most use 5 bits represent a genotype
        /*
             2 bits for an unphased -genotype of a bi-allelic sequence variants
             3 bits for a phased -genotype of a bi-allelic sequence variants
             3 bits for an unphased -genotype of a tri-allelic sequence variants
             4 bits for a phased -genotype of a tri-allelic sequence variants
             4 bits for an unphased -genotype of a quad-allelic sequence variants
             5 bits for a phased -genotype of a quad-allelic sequence variants        
             */
            /*
             if (caseIDSet.isEmpty()) {
             throw new Exception("It seems that you have no specified patients (labeled with \'2\') in your sample!"
             + " You need patient samples to proceed on KGGSeq!");
             }
             * 
             */
            //unfornatuely, mine splitter is faster than guava
            //Splitter niceCommaSplitter = Splitter.on('\t').limit(maxEffectiveColVCF + 1);
            //String[] cells = new String[maxEffectiveColVCF + 1];
            currentLine = br.readLine();
            if (currentLine == null) {
                return 0;
            }
            
            int bitNum;
            int byteIndex1;
            int byteIndex2;
            //warning if all of the genotypes are missing, it will have a problem.
            //decide the whether genotypes are phased or not //at most consider 3 alternative alleles
            if (currentLine.indexOf("0|0") >= 0 || currentLine.indexOf("0|1") >= 0 || currentLine.indexOf("1|0") >= 0 || currentLine.indexOf("0|2") >= 0 || currentLine.indexOf("2|0") >= 0 || currentLine.indexOf("0|3") >= 0 || currentLine.indexOf("3|0") >= 0
                    || currentLine.indexOf("1|1") >= 0 || currentLine.indexOf("1|2") >= 0 || currentLine.indexOf("2|1") >= 0 || currentLine.indexOf("1|3") >= 0 || currentLine.indexOf("3|1") >= 0
                    || currentLine.indexOf("2|2") >= 0 || currentLine.indexOf("2|3") >= 0 || currentLine.indexOf("3|2") >= 0
                    || currentLine.indexOf("3|3") >= 0 || currentLine.indexOf("0|0") >= 0) {
                isPhased = true;
            }
            final String[] cells = Util.tokenize(currentLine, '\t', maxEffectiveColVCF);
            
            do {
                totalVarNum++;
                // System.out.println(currentLine); 
                if (currentLine.isEmpty()) {
                    continue;
                }

                // st = new StringTokenizer(currentLine, delimiter);
                tokenize(currentLine, '\t', maxEffectiveColVCF, cells);

                // String[] cells = currentLine.split(currentLine);
                if (cells.length < 2) {
                    //cells = Util.tokenizeIngoreConsec(currentLine, ' ', maxEffectiveColVCF);
                    continue;
                }
                currChr = cells[indexCHROM];
                if (currChr.startsWith(UNKNOWN_CHROM_NAME0) || currChr.startsWith(UNKNOWN_CHROM_NAME1)) {
                    ignoredInproperChromNum++;
                    break;
                }

                //Mitochondrion
                if (currChr.contains("T") || currChr.contains("t")) {
                    currChr = "M";
                } else {
                    if (currChr.charAt(0) == 'c' || currChr.charAt(0) == 'C') {
                        currChr = currChr.substring(3);
                    }
                }
                makerPostion = Util.parseInt(cells[indexPOS]);

               
                 if (makerPostion == 142728364) {                    
                 int sss = 0;
             
                 } 
                varLabel = cells[indexID];
                if (varLabel.charAt(0) != 'r') {
                    nonRSVariantNum++;
                }
                ref = cells[indexREF];
                alt = cells[indexALT];
                //for reference data, sometimes we do not have alternative alleles
                if (alt.equals(".")) {
                    continue;
                }
                isInvalid = false;
                
                String[] altAlleles = Util.tokenize(alt, ',');
                alleleNum = altAlleles.length + 1;
                if (alleleNum > maxGtyAlleleNum) {
                    ignoredVarBymaxGtyAlleleNum++;
                    continue;
                }
                isIndel = false;
                for (int ss = 0; ss < altAlleles.length; ss++) {
                    alt = altAlleles[ss];
                    //only one alternative alleles; the most common  scenario
                    if (ref.length() == alt.length()) {
                        //substitution
                        //now it can sonsider double polymorphsom
                        altAlleles[ss] = alt;
                    } else if (ref.length() < alt.length()) {
                        //insertion
                                /*examples 
                         insertion1
                         chr1 1900106 . TCT TCTCCT 217 . INDEL;DP=62;AF1=0.5;CI95=0.5,0.5;DP4=17,9,18,12;MQ=60;FQ=217;PV4=0.78,1,1,0.1 GT:PL:DP:SP:GQ 0/1:255,0,255:56:-991149567:99
                        
                         insertion2
                         chr1 109883576 . C CAT 214 . INDEL;DP=15;AF1=1;CI95=1,1;DP4=0,0,1,11;MQ=60;FQ=-70.5 GT:PL:DP:SP:GQ 1/1:255,36,0:12:-991149568:69
                         * 
                         */
                        //for Indel TTCC TT--
                        //for Insertion T +TTTT
                        tmpSB.delete(0, tmpSB.length());
                        if (alt.startsWith(ref)) {
                            for (t = ref.length(); t > 0; t--) {
                                tmpSB.append('+');
                            }
                            tmpSB.append(alt.substring(ref.length()));
                            altAlleles[ss] = tmpSB.toString();
                        } else if (alt.endsWith(ref)) {
                            tmpSB.append(alt.substring(0, alt.length() - ref.length()));
                            for (t = ref.length(); t > 0; t--) {
                                tmpSB.append('+');
                            }
                            altAlleles[ss] = tmpSB.toString();
                        }
                        isIndel = true;
                    } else if (ref.length() > alt.length()) {
                        //deletion     
                                /*examples
                         deletion1
                         chr1 113659065 . ACTCT ACT 214 . INDEL;DP=61;AF1=1;CI95=1,1;DP4=0,0,22,34;MQ=60;FQ=-204 GT:PL:DP:SP:GQ 1/1:255,169,0:56:-991149568:99
                         deletion2
                         chr1 1289367 . CTG C 101 . INDEL;DP=14;AF1=0.5;CI95=0.5,0.5;DP4=5,2,5,1;MQ=60;FQ=104;PV4=1,0.4,1,1 GT:PL:DP:SP:GQ 0/1:139,0,168:13:-991149568:99
                         */
                        //Note it can work for multiple deletion alleles like:chr1	158164305	.	TAA	TA,T

                        //for Indel TTCC TT--
                        //for Insertion T +TTTT
                        tmpSB.delete(0, tmpSB.length());
                        if (ref.startsWith(alt)) {
                            tmpSB.append(alt);
                            for (t = ref.length() - alt.length(); t > 0; t--) {
                                tmpSB.append('-');
                            }
                            altAlleles[ss] = tmpSB.toString();
                        } else if (ref.endsWith(alt)) {
                            for (t = ref.length() - alt.length(); t > 0; t--) {
                                tmpSB.append('-');
                            }
                            tmpSB.append(alt);
                            altAlleles[ss] = tmpSB.toString();
                        }
                        
                        isIndel = true;
                    } else {
                        StringBuilder info = new StringBuilder("Unexpected (REF	ALT) format when parsing line :" + currentLine);
                        LOG.warn(info);
                        isInvalid = true;
                        // throw new Exception(info.toString());
                    }
                }
                if (isInvalid) {
                    continue;
                }
                
                if (isIndel) {
                    indelNum++;
                } else {
                    snvNum++;
                }
                
                if (!considerSNP || !considerIndel) {
                    //a lazy point 
                    incomplete = true;

                    //only consider Indel
                    if (!considerSNP && isIndel) {
                        incomplete = false;
                    } else if (!considerIndel && !isIndel) {
                        incomplete = false;
                    }
                    
                    if (incomplete) {
                        continue;
                    }
                }
                
                if (hasOrginalGenome) {
                    Variant[] vars = orgGenome.lookupVariants(currChr, makerPostion, isIndel, ref, altAlleles);
                    if (vars == null) {
                        continue;
                    }
                }

                //initialize varaibles
                incomplete = true;
                obsS = 0;
                hasIndexGT = false;
                hasIndexGQ = false;
                hasIndexDP = false;
                hasIndexAD = false;
                hasIndexFA = false;
                isLowQualBreak = false;
                
                hasIndexPL = false;
                hasIndexGP = false;
                gtyPLIndexInInfor = -1;
                gtyGPIndexInInfor = -1;
                gtyIndexInInfor = -1;
                gtyQualIndexInInfor = -1;
                gtyDepthIndexInInfor = -1;
                gtyAlleleDepthIndexInInfor = -1;
                gtyAltAlleleFracIndexInInfor = -1;
                mappingQual = Integer.MAX_VALUE;
                strandBias = Integer.MIN_VALUE;

//#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
//chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 
//chr1	53598	.	CCTA	C	447.88	PASS	AC=2;AF=1.00;AN=2;DP=0;Dels=0.50;HRun=0;HaplotypeScore=0.0000;MQ=20.50;MQ0=5;QD=40.72;SB=-138.61;sumGLbyD=46.54	GT:AD:DP:GQ:PL	./.	1/1:2,1:0:3.01:66,3,0
                if (checkVCFfilter && cells[indexFILTER].length() > 1 && !vcfLabelSet.contains(cells[indexFILTER])) {
                    vcfFilterOutNum++;
                    isLowQualBreak = true;
                    continue;
                }
                
                if (cells.length > indexQUAL && avgSeqQualityThrehsold > 0) {
                    if (Util.isNumeric(cells[indexQUAL])) {
                        avgSeqQuality = Double.parseDouble(cells[indexQUAL]);
                    } else {
                        //sometimes . denotes for ignored sequence qaulity information
                        avgSeqQuality = Integer.MAX_VALUE;
                    }
                    
                    if (avgSeqQuality < avgSeqQualityThrehsold) {
                        filterOutLowQualNum++;
                        isLowQualBreak = true;
                        continue;
                    }
                }
                
                if (minMappingQual > 0) {
                    index1 = cells[indexINFO].indexOf("MQ=");
                    if (index1 >= 0) {
                        index1 += 3;
                        index2 = index1 + 1;
                        while (index2 < cells[indexINFO].length() && cells[indexINFO].charAt(index2) != ';') {
                            index2++;
                        }
                        if (Util.isNumeric(cells[indexINFO].substring(index1, index2))) {
                            mappingQual = Util.parseInt(cells[indexINFO].substring(index1, index2));
                        }
                        if (mappingQual < minMappingQual) {
                            ignoreMappingQualNum++;
                            isLowQualBreak = true;
                            continue;
                        }
                    }
                }
                //I do notknow the minimun threshold for this 
                if (cells.length > indexINFO) {
                    if (maxStrandBias > 0) {
                        index1 = cells[indexINFO].indexOf("SB=");
                        if (index1 >= 0) {
                            index1 += 3;
                            index2 = index1 + 1;
                            while (index2 < cells[indexINFO].length() && cells[indexINFO].charAt(index2) != ';') {
                                index2++;
                            }
                            if (Util.isNumeric(cells[indexINFO].substring(index1, index2))) {
                                strandBias = Util.parseFloat(cells[indexINFO].substring(index1, index2));
                            }
                            
                            if (strandBias > maxStrandBias) {
                                ignoreStrandBiasSBNum++;
                                isLowQualBreak = true;
                                continue;
                            }
                        }
                    }
                    
                    if (maxFisherStrandBias > 0) {
                        index1 = cells[indexINFO].indexOf("FS=");
                        if (index1 >= 0) {
                            index1 += 3;
                            index2 = index1 + 1;
                            while (index2 < cells[indexINFO].length() && cells[indexINFO].charAt(index2) != ';') {
                                index2++;
                            }
                            if (Util.isNumeric(cells[indexINFO].substring(index1, index2))) {
                                strandBias = Util.parseFloat(cells[indexINFO].substring(index1, index2));
                            }
                            
                            if (strandBias > maxFisherStrandBias) {
                                ignoreStrandBiasFSNum++;
                                isLowQualBreak = true;
                                continue;
                            }
                        }
                    }
                }
                
                Integer chromID = chromNameIndexMap.get(currChr);
                if (chromID == null) {
                    //System.err.println("Unrecognized chromosome name: " + currChr);
                    continue;
                }
                //it is no gty vcf
                if (indexFORMAT < 0 || noGtyVCF) {
                    Variant var = new Variant(makerPostion, ref, altAlleles);
                    var.setIsIndel(isIndel);
                    var.setLabel(varLabel);
                    varChroms[chromID].add(var);
                    
                    acceptVarNum++;
                    if (acceptVarNum >= maxVarNum) {
                        totalAcceptVarNum += acceptVarNum;
                        writeChromosomeToDiskClean();
                        acceptVarNum = 0;
                    }
                    continue;
                }
                
                if (maxVcfIndivNum > 0) {
                    Arrays.fill(gtys, null);
                    if (needGtyQual) {
                        Arrays.fill(gtyQualDescrip, null);
                    }
                }

                //System.out.println(currentLine);
                //StringTokenizer st1 = new StringTokenizer(tmpBuffer.toString(), ":");
                String[] cells1 = Util.tokenize(cells[indexFORMAT], ':');
                ii = 0;
                maxIndex2 = 0;
                for (ii = 0; ii < cells1.length; ii++) {
                    if (cells1[ii].equals("GT")) {
                        gtyIndexInInfor = ii;
                        hasIndexGT = true;
                        maxIndex2 = ii;
                    } else if (cells1[ii].equals("GQ")) {
                        if (gtyQualityThrehsold > 0) {
                            gtyQualIndexInInfor = ii;
                            hasIndexGQ = true;
                            maxIndex2 = ii;
                            Arrays.fill(gtyQuality, 0);
                        }
                    } else if (cells1[ii].equals("DP")) {
                        if (minGtySeqDepth > 0) {
                            gtyDepthIndexInInfor = ii;
                            hasIndexDP = true;
                            maxIndex2 = ii;
                            Arrays.fill(gtyDepth, 0);
                        }
                    } else if (cells1[ii].equals("AD")) {
                        if (altAlleleFracRefHomThrehsold < 1 || altAlleleFractHetThrehsold > 0 || altAlleleFractAltHomThrehsold > 0 || needReadsInfor) {
                            gtyAlleleDepthIndexInInfor = ii;
                            hasIndexAD = true;
                            maxIndex2 = ii;
                            Arrays.fill(readCounts, null);
                            Arrays.fill(readFractions, Float.NaN);
                        }
                    } else if (cells1[ii].equals("FA")) {
                        if (altAlleleFracRefHomThrehsold < 1 || altAlleleFractHetThrehsold > 0 || altAlleleFractAltHomThrehsold > 0) {
                            gtyAltAlleleFracIndexInInfor = ii;
                            hasIndexFA = true;
                            maxIndex2 = ii;
                            Arrays.fill(readFractions, Float.NaN);
                        }
                    } else if (cells1[ii].equals("PL")) {
                        if (minSecondPL > 0) {
                            gtyPLIndexInInfor = ii;
                            hasIndexPL = true;
                            maxIndex2 = ii;
                            Arrays.fill(secondMostGtyPL, 0);
                        }
                    } else if (cells1[ii].equals("GP")) {
                        if (minBestGP > 0) {
                            gtyGPIndexInInfor = ii;
                            hasIndexGP = true;
                            maxIndex2 = ii;
                            Arrays.fill(bestGtyGP, 0);
                        }
                    }
                }

                //1/1:0,2:2:6.02:70,6,0	./.
                indexA = -1;
                int s = 0;
                final String[] cellsB = new String[maxIndex2 + 1];
                for (int k = 0; k < effectiveIndivNum; k++) {
                    iGty = effectIndivIDInVCF.getQuick(k);
                    s = iGty + indexFORMAT + 1;
                    
                    if (cells[s].charAt(0) == '"') {
                        cells[s] = cells[s].substring(1, cells[s].length() - 1);
                    }
                    if (cells[s].charAt(0) == '.') {
                        gtys[iGty] = null;
                        continue;
                    }
                    
                    tokenize(cells[s], ':', maxIndex2, cellsB);
                    if (gtyIndexInInfor >= 0) {
                        gtys[iGty] = cellsB[gtyIndexInInfor];
                        if (gtys[iGty].length() == 1) {
                            if (isPhased) {
                                gtys[iGty] = gtys[iGty] + "|" + gtys[iGty];
                            } else {
                                gtys[iGty] = gtys[iGty] + "/" + gtys[iGty];
                            }
                        }
                    }
                    if (gtys[iGty].charAt(0) == '.') {
                        gtys[iGty] = null;
                        continue;
                    }
                    if (needGtyQual) {
                        gtyQualDescrip[iGty] = cells[s];
                    }
                    if (gtyQualIndexInInfor >= 0) {
                        if (cellsB[gtyQualIndexInInfor].charAt(0) == '.') {
                            gtyQuality[iGty] = 0;
                        } else {
                            gtyQuality[iGty] = Util.parseInt(cellsB[gtyQualIndexInInfor]);
                        }                        
                       // System.out.println(gtyQuality[iGty]);
                    }
                    if (gtyDepthIndexInInfor >= 0) {
                        if (cellsB[gtyDepthIndexInInfor].charAt(0) == '.') {
                            gtyDepth[iGty] = 0;
                        } else {
                            gtyDepth[iGty] = Util.parseInt(cellsB[gtyDepthIndexInInfor]);
                            
                            if (indexA == -1 && depB != null && gtyAlleleDepthIndexInInfor != -1 && gtyDepthIndexInInfor > gtyAlleleDepthIndexInInfor) {
                                readCounts[iGty] = String.valueOf(gtyDepth[iGty] - Util.parseInt(depB)) + "," + depB;
                            }
                        }
                        
                    }
                    if (gtyAlleleDepthIndexInInfor >= 0) {
                        if (cellsB[gtyAlleleDepthIndexInInfor].charAt(0) == '.') {
                            readCounts[iGty] = null;
                        } else {
                            String allRead = cellsB[gtyAlleleDepthIndexInInfor];
                            indexA = allRead.indexOf(',');
                            //sometimes AD only has alternative allele counts; we just ignore this case
                            if (indexA == -1) {
                                depB = allRead;
                                if (gtyDepthIndexInInfor < gtyAlleleDepthIndexInInfor) {
                                    readCounts[iGty] = String.valueOf(gtyDepth[iGty] - Util.parseInt(depB)) + "," + depB;
                                }
                            } else {
                                indexB = allRead.lastIndexOf(',');
                                //when more than 2 alleles only consider the first ahd the last allele                                                    
                                depA = allRead.substring(0, indexA);
                                depB = allRead.substring(indexB + 1);
                                if (depA.equals(".")) {
                                    depA = "0";
                                }
                                if (depB.equals(".")) {
                                    depB = "0";
                                }
                                readCounts[iGty] = depA + "," + depB;
                            }
                            
                        }
                    }
                    
                    if (gtyAltAlleleFracIndexInInfor >= 0) {
                        if (cellsB[gtyAltAlleleFracIndexInInfor].charAt(0) == '.') {
                            readFractions[iGty] = Float.NaN;
                        } else {
                            tmpStr = cellsB[gtyAltAlleleFracIndexInInfor];
                            if (tmpStr.indexOf(',') >= 0) {
                                index2 = index1 + tmpStr.indexOf(',');
                            }
                            readFractions[iGty] = Util.parseFloat(cellsB[gtyAltAlleleFracIndexInInfor]);
                        }
                    }
                    if (gtyPLIndexInInfor >= 0) {
                        String val = cellsB[gtyPLIndexInInfor];
                        if (val.charAt(0) != '.') {
                            String[] pls = val.split(",");
                            t = 0;
                            pl = 0;
                            if (!pls[t].equals(".")) {
                                secondMostGtyPL[iGty] = Util.parseInt(pls[t]);
                                if (secondMostGtyPL[iGty] == 0) {
                                    secondMostGtyPL[iGty] = Integer.MAX_VALUE;
                                }
                            } else {
                                secondMostGtyPL[iGty] = Integer.MAX_VALUE;
                            }
                            
                            for (t = 1; t < pls.length; t++) {
                                if (pls[t].equals(".")) {
                                    continue;
                                }
                                if (pls[t].equals("0")) {
                                    continue;
                                }
                                pl = Util.parseInt(pls[t]);
                                
                                if (pl < secondMostGtyPL[iGty]) {
                                    secondMostGtyPL[iGty] = pl;
                                }
                            }
                        }
                    }
                    if (gtyGPIndexInInfor >= 0) {
                        String val = cellsB[gtyGPIndexInInfor];
                        if (val.charAt(0) != '.') {
                            String[] pls = val.split(",");
                            t = 0;
                            p = 0;
                            
                            if (!pls[t].equals(".")) {
                                bestGtyGP[iGty] = Util.parseInt(pls[t]);
                            }
                            
                            for (t = 1; t < pls.length; t++) {
                                if (pls[t].equals(".")) {
                                    continue;
                                }
                                if (pls[t].equals("0")) {
                                    continue;
                                }
                                p = Util.parseInt(pls[t]);
                                if (p > bestGtyGP[iGty]) {
                                    bestGtyGP[iGty] = p;
                                }
                            }
                        }
                    }
                }

                /*
                 if (!isLowQualBreak && currChr.indexOf(UNKNOWN_CHROM_NAME0) < 0 && currChr.indexOf(UNKNOWN_CHROM_NAME1) < 0 && hasAlt) {
                 formatProbVarNum++;
                 LOG.error("Format error at line " + fileLineCounter + ": " + currentLine);
                 continue;
                 }
                 * 
                 */
                //QC
                for (int k = totalPedSubjectNum - 1; k >= 0; k--) {
                    index = pedVCFIDMap[k];
                    
                    if (index < 0) {
                        continue;
                    }

                    //ignore variants with missing genotypes
                    if (gtys[index] == null || gtys[index].charAt(0) == '.') {
                        gtys[index] = null;
                        missingGtyNum++;
                        continue;
                    }
                    
                    if (hasIndexGQ && gtyQuality[index] < gtyQualityThrehsold) {
                        gtys[index] = null;
                        ignoredLowQualGtyNum++;
                        continue;
                    }
                    if (hasIndexDP && gtyDepth[index] < minGtySeqDepth) {
                        gtys[index] = null;
                        ignoredLowDepthGtyNum++;
                        continue;
                    }
                    if (hasIndexPL && secondMostGtyPL[index] < minSecondPL) {
                        ignoredLowPLGtyNum++;
                        gtys[index] = null;
                        continue;
                    }
                    if (hasIndexGP && bestGtyGP[index] < minBestGP) {
                        ignoredLowGPGtyNum++;
                        gtys[index] = null;
                        continue;
                    }
                    
                    if (hasIndexAD || hasIndexFA) {
                        if (gtys[index].charAt(0) == '0' && gtys[index].charAt(2) == '0') {
                            if (altAlleleFracRefHomThrehsold < 1) {
                                //the AD infor may be missing
                                if (readCounts[index] == null && Float.isNaN(readFractions[index])) {
                                    continue;
                                }
                                if (Float.isNaN(readFractions[index])) {
                                    String[] counts = readCounts[index].split(",");
                                    float bac = Util.parseFloat(counts[1]);
                                    readFractions[index] = bac / (bac + Util.parseFloat(counts[0]));
                                }
                                
                                if (Float.isNaN(readFractions[index])) {
                                    gtys[index] = null;
                                    ignoredBadAltFracGtyNum++;
                                    continue;
                                } else if (readFractions[index] > altAlleleFracRefHomThrehsold) {
                                    gtys[index] = null;
                                    ignoredBadAltFracGtyNum++;
                                    continue;
                                }
                                
                            }
                        } else if (gtys[index].charAt(0) != gtys[index].charAt(2)) {
                            if (altAlleleFractHetThrehsold > 0) {
                                //the AD infor may be missing
                                if (readCounts[index] == null && Float.isNaN(readFractions[index])) {
                                    continue;
                                }
                                if (Float.isNaN(readFractions[index])) {
                                    String[] counts = readCounts[index].split(",");
                                    float bac = Util.parseFloat(counts[1]);
                                    readFractions[index] = bac / (bac + Util.parseFloat(counts[0]));
                                }
                                if (Float.isNaN(readFractions[index])) {
                                    gtys[index] = null;
                                    ignoredBadAltFracGtyNum++;
                                    continue;
                                } else if (readFractions[index] < altAlleleFractHetThrehsold) {
                                    gtys[index] = null;
                                    ignoredBadAltFracGtyNum++;
                                    continue;
                                }
                            }
                            
                        } else {
                            if (altAlleleFractAltHomThrehsold > 0) {
                                //the AD infor may be missing
                                if (readCounts[index] == null && Float.isNaN(readFractions[index])) {
                                    continue;
                                }
                                if (Float.isNaN(readFractions[index])) {
                                    String[] counts = readCounts[index].split(",");
                                    float bac = Util.parseFloat(counts[1]);
                                    readFractions[index] = bac / (bac + Util.parseFloat(counts[0]));
                                }
                                if (Float.isNaN(readFractions[index])) {
                                    gtys[index] = null;
                                    ignoredBadAltFracGtyNum++;
                                    continue;
                                } else if (readFractions[index] < altAlleleFractAltHomThrehsold) {
                                    gtys[index] = null;
                                    ignoredBadAltFracGtyNum++;
                                    continue;
                                }
                            }
                        }
                    }
                    obsS++;
                }


                /*
                 if (obsS == 0) {
                 ignoredLineNumNoVar++;
                 continue;
                 }
                 * 
                 */
                if (obsS < minOBS) {
                    ignoredLineNumMinOBS++;
                    continue;
                }
                
                if ((!needAccoundAffect && !needAccoundUnaffect) || needMAFQCOver || needMAFQCLess) {
                    g11 = 0;
                    g12 = 0;
                    g22 = 0;
                    
                    for (int i = 0; i < totalPedSubjectNum; i++) {
                        int idLabel = pedVCFIDMap[i];
                        if (idLabel < 0) {
                            continue;
                        }
                        if (gtys[idLabel] == null || gtys[idLabel].charAt(0) == '.') {
                            continue;
                        }
                        if (gtys[idLabel].length() == 1) {
                            if (gtys[idLabel].charAt(0) == '0') {
                                g11++;
                            } else {
                                g22++;
                            }
                        } else {
                            if (gtys[idLabel].charAt(0) != gtys[idLabel].charAt(2)) {
                                g12++;
                            } else if (gtys[idLabel].charAt(0) == '0') {
                                g11++;
                            } else {
                                g22++;
                            }
                        }
                    }
                }
                if (needMAFQCOver || needMAFQCLess) {
                    maf = (g12 * 0.5 + g22) / (g11 + g12 + g22);
                    if (needMAFQCOver) {
                        if (maf <= sampleMafOver || maf >= sampleMafOverC) {
                            ignoredLineNumMinMAF++;
                            continue;
                        }
                    }
                    if (needMAFQCLess) {
                        if (maf >= sampleMafLess && maf <= sampleMafLessC) {
                            ignoredLineNumMaxMAF++;
                            continue;
                        }
                    }
                }
                
                Variant var = new Variant(makerPostion, ref, altAlleles);
                var.setIsIndel(isIndel);
                var.setLabel(varLabel);
                varChroms[chromID].add(var);
                
                if (needGtyQual) {
                    for (t = 0; t < 8; t++) {
                        vcfWriters[chromID].write(cells[t]);
                        vcfWriters[chromID].write('\t');
                    }
                    vcfWriters[chromID].write(cells[8]);
                }
                int subjectNum = subjectList.size();
                if (needGty) {
                    int byteNum = 0;
                    //as the genotypes may be use for other purpose so we need record it before filtering 
                    if (!isPhased) {
                        base = GlobalManager.unphasedAlleleBitMap.get(alleleNum);
                        bitNum = base * subjectNum;
                        
                        if (bitNum % 32 != 0) {
                            byteNum = bitNum / 32 + 1;
                        } else {
                            byteNum = bitNum / 32;
                        }
                        
                        var.encodedGty = new int[byteNum];
                        Arrays.fill(var.encodedGty, 0);
                        for (index = 0; index < totalPedSubjectNum; index++) {
                            int idLabel = pedVCFIDMap[index];
                            if (idLabel < 0) {
                                continue;
                            }

                            /*
                             missing	Reference homozygous	Heterozygous 	Alternative homozygous
                             VCF genotype	./.	0/0	0/1	1/1
                             Bits	00  	01	10	11
                             Order	0	1	2	3        
                             */

                            /*
                             missing	Reference homozygous	Heterozygous 	Heterozygous	Alternative homozygous	Heterozygous	Alternative homozygous
                             VCF genotype	./.	0/0	0/1	0/2	1/1	1/2	2/2
                             Bits	        000	001	010	011	100	101	110
                             Order	0	1	2	3	4	5	6 
                             */
                            /*
                             I.III Quad-allelic sequence variant (4 bits)
                             missing 	Reference homozygous 	Heterozygous 	Heterozygous 	Heterozygous 	Alternative homozygous 	Heterozygous
                             VCF genotype 	./. 	0/0 	0/1 	0/2 	0/3 	1/1 	1/2
                             Bits 	      000 	0001 	0010 	0011 	0100 	0101 	0110
                             Decimal 	0 	1 	2 	3 	4 	5 	6
                             Heterozygous 	Alternative homozygous 	Heterozygous 	Alternative homozygous
                             VCF genotype 	1/3 	2/2 	2/3 	3/3
                             Bits 	     0111 	1000 	1001 	1010
                             Decimal 	7 	8 	9 	10                               
                             */
                            bitNum = base * index;
                            if (gtys[idLabel] == null || gtys[idLabel].charAt(0) == '.') {
                                //missing value         
                                continue;
                            } else {
                                switch (alleleNum) {
                                    case 2:
                                        /*
                                         missing	Reference homozygous	Heterozygous 	Alternative homozygous
                                         VCF genotype	./.	0/0	0/1	1/1
                                         Bits	00  	01	10	11
                                         Order	0	1	2	3        
                                         */

                                        //to speedup the analysis
                                        if (gtys[idLabel].charAt(0) == '0' && gtys[idLabel].charAt(2) == '0') {
                                            
                                            byteIndex1 = (bitNum + 1) / 32;
                                            byteIndex2 = (bitNum + 1) % 32;
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            //System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                        } else if (gtys[idLabel].charAt(0) == '0' && gtys[idLabel].charAt(2) == '1' || gtys[idLabel].charAt(0) == '1' && gtys[idLabel].charAt(2) == '0') {
                                            byteIndex1 = (bitNum) / 32;
                                            byteIndex2 = (bitNum) % 32;
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                        } else if (gtys[idLabel].charAt(0) == '1' && gtys[idLabel].charAt(2) == '1') {
                                            byteIndex1 = (bitNum) / 32;
                                            byteIndex2 = (bitNum) % 32;
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            byteIndex1 = (bitNum + 1) / 32;
                                            byteIndex2 = (bitNum + 1) % 32;
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                        }
                                        break;
                                    default:
                                        boolean[] bits = GlobalManager.unphasedGtyCodingMap.get(gtys[idLabel] + ":" + alleleNum);
                                        for (int i = 0; i < base; i++) {
                                            if (bits[i]) {
                                                byteIndex1 = (bitNum + i) / 32;
                                                byteIndex2 = (bitNum + i) % 32;
                                                var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            }
                                        }
                                }
                            }
                        }
                    } else {
                        base = GlobalManager.phasedAlleleBitMap.get(alleleNum);
                        bitNum = base * subjectNum;
                        
                        if (bitNum % 32 != 0) {
                            byteNum = bitNum / 32 + 1;
                        } else {
                            byteNum = bitNum / 32;
                        }
                        
                        var.encodedGty = new int[byteNum];
                        Arrays.fill(var.encodedGty, 0);
                        
                        for (index = 0; index < totalPedSubjectNum; index++) {
                            int idLabel = pedVCFIDMap[index];
                            if (idLabel < 0) {
                                continue;
                            }
                            bitNum = base * index;
                            if (gtys[idLabel] == null || gtys[idLabel].charAt(0) == '.') {
                                //missing value         
                                continue;
                            } else {
                                switch (alleleNum) {
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

                                        //to speedup the analysis
                                        if (gtys[idLabel].charAt(0) == '0' && gtys[idLabel].charAt(2) == '0') {
                                            byteIndex1 = (bitNum + 2) / 32;
                                            byteIndex2 = (bitNum + 2) % 32;
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            //System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                        } else if (gtys[idLabel].charAt(0) == '0' && gtys[idLabel].charAt(2) == '1') {
                                            byteIndex1 = (bitNum + 1) / 32;
                                            byteIndex2 = (bitNum + 1) % 32;
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                        } else if (gtys[idLabel].charAt(0) == '1' && gtys[idLabel].charAt(2) == '0') {
                                            byteIndex1 = (bitNum + 1) / 32;
                                            byteIndex2 = (bitNum + 1) % 32;
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            byteIndex1 = (bitNum + 2) / 32;
                                            byteIndex2 = (bitNum + 2) % 32;
                                            // System.out.println(Integer.toBinaryString(var.encodedGty[byteIndex1]));
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                        } else if (gtys[idLabel].charAt(0) == '1' && gtys[idLabel].charAt(2) == '1') {
                                            byteIndex1 = (bitNum) / 32;
                                            byteIndex2 = (bitNum) % 32;
                                            var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                        }
                                        break;
                                    default:
                                        boolean[] bits = GlobalManager.phasedGtyCodingMap.get(gtys[idLabel] + ":" + alleleNum);
                                        for (int i = 0; i < base; i++) {
                                            if (bits[i]) {
                                                byteIndex1 = (bitNum + i) / 32;
                                                byteIndex2 = (bitNum + i) % 32;
                                                var.encodedGty[byteIndex1] = var.encodedGty[byteIndex1] | GlobalManager.intOpers[byteIndex2];
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
                
                if (needReadsInfor) {
                    var.readInfor = new char[subjectNum * 2];
                    //two chars for a gty 
                    for (index = 0; index < totalPedSubjectNum; index++) {
                        int idLabel = pedVCFIDMap[index];
                        if (idLabel < 0) {
                            continue;
                        }
                        if (readCounts[idLabel] == null) {
                            continue;
                        }
                        int delemiterIndex = readCounts[idLabel].indexOf(',');
                        if (delemiterIndex >= 0) {
                            var.readInfor[index + index] = Util.parseChar(readCounts[idLabel].substring(0, delemiterIndex));
                            var.readInfor[index + index + 1] = Util.parseChar(readCounts[idLabel].substring(delemiterIndex + 1));
                        }
                        
                    }
                }
                if (needGtyQual) {
                    for (index = 0; index < totalPedSubjectNum; index++) {
                        int idLabel = pedVCFIDMap[index];
                        vcfWriters[chromID].write("\t");
                        if (idLabel < 0) {
                            vcfWriters[chromID].write("./.");
                            continue;
                        }
                        
                        if (gtys[idLabel] == null) {
                            vcfWriters[chromID].write("./.");
                            continue;
                        }
                        vcfWriters[chromID].write(gtyQualDescrip[idLabel]);
                    }
                    vcfWriters[chromID].write("\n");
                }
                
                if (needAccoundAffect) {
                    g11 = 0;
                    g12 = 0;
                    g22 = 0;
                    
                    for (int i = 0; i < caseSize; i++) {
                        int idLabel = pedVCFIDMap[caeSetID.getQuick(i)];
                        if (idLabel < 0) {
                            continue;
                        }
                        if (gtys[idLabel] == null || gtys[idLabel].charAt(0) == '.') {
                            continue;
                        }
                        
                        if (gtys[idLabel].charAt(0) != gtys[idLabel].charAt(2)) {
                            g12++;
                        } else if (gtys[idLabel].charAt(0) == '0') {
                            g11++;
                        } else {
                            g22++;
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
                    
                    for (int i = 0; i < controlSize; i++) {
                        int idLabel = pedVCFIDMap[controlSetID.getQuick(i)];
                        if (idLabel < 0) {
                            continue;
                        }
                        if (gtys[idLabel] == null || gtys[idLabel].charAt(0) == '.') {
                            continue;
                        }
                        
                        if (gtys[idLabel].charAt(0) != gtys[idLabel].charAt(2)) {
                            g12++;
                        } else if (gtys[idLabel].charAt(0) == '0') {
                            g11++;
                        } else {
                            g22++;
                        }
                    }
                    
                    var.setUnaffectedRefHomGtyNum(g11);
                    var.setUnaffectedHetGtyNum(g12);
                    var.setUnaffectedAltHomGtyNum(g22);
                }
                
                if (!needAccoundAffect && !needAccoundUnaffect) {
                    var.setAffectedRefHomGtyNum(g11);
                    var.setAffectedHetGtyNum(g12);
                    var.setAffectedAltHomGtyNum(g22);
                }
                acceptVarNum++;
                if (acceptVarNum >= maxVarNum) {
                    totalAcceptVarNum += acceptVarNum;
                    writeChromosomeToDiskClean();
                    acceptVarNum = 0;
                }
                // if(acceptVarNum>900) break;
            } while ((currentLine = br.readLine()) != null);
            
            if (acceptVarNum > 0) {
                writeChromosomeToDiskClean();
                totalAcceptVarNum += acceptVarNum;
                acceptVarNum = 0;
            }
        } catch (Exception nex) {
            nex.printStackTrace();
            String info = nex.toString() + " when parsing at line : " + currentLine;
            LOG.error(info);
            isInvalid = true;
            // throw new Exception(info);
            // LOG.error(nex, info);

        }

        //change the order to be consisten with the pedigree file
        // effectIndivIDInVCF.quickSort();
        return acceptVarNum;
    }
    
    public void writeChromosomeToDiskClean() {
        int chromID = -1;
        String chromeName;
        try {
            for (List<Variant> varList : varChroms) {
                chromID++;
                chromeName = STAND_CHROM_NAMES[chromID];
                if (varList.isEmpty()) {
                    continue;
                }
                int fileIndex = -1;
                String chrNameP = "Chromosome." + chromeName;
                chrNameP = threadID + "." + chrNameP;
                File fileName = null;
                File folder = new File(storagePath);
                if (folder.exists()) {
                    do {
                        fileIndex++;
                        fileName = new File(storagePath + File.separator + chrNameP + ".var.obj." + fileIndex);
                    } while (fileName.exists());
                } else {
                    fileIndex++;
                    folder.mkdirs();
                }

                //comments: both Kryo and FSTObjectOutput are excellent tools for Serializationl. However, the former produced slightly smaller file and was slightly faster. So I used Kryo
                fileName = new File(storagePath + File.separator + chrNameP + ".var.obj." + fileIndex);
                //slower
                //  kryo.setInstantiatorStrategy(new SerializingInstantiatorStrategy());            
                Output output = new Output(new FileOutputStream(fileName), 1024 * 1024);
                //  output.setBuffer(buffer);

                for (Variant var : varList) {
                    kryo.writeObject(output, var);
                }
                output.flush();
                output.close();
                varList.clear();
            }
            System.gc();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void setBooleanFilter(boolean considerSNP, boolean considerIndel, boolean needGty, boolean needReadsInfor, boolean needGtyQual, boolean noGtyVCF) {
        this.considerSNP = considerSNP;
        this.considerIndel = considerIndel;
        this.needGty = needGty;
        this.needReadsInfor = needReadsInfor;
        this.needGtyQual = needGtyQual;
        this.noGtyVCF = noGtyVCF;
    }
    
    IntArrayList effectIndivIDInVCF;
    int[] pedVCFIDMap;
    List<Individual> subjectList;
    boolean isPhased = false;
    Genome orgGenome;
    
    public List<Variant>[] getVarChroms() {
        return varChroms;
    }
    
    public List<Individual> getSubjectList() {
        return subjectList;
    }
    
    public void setOrgGenome(Genome orgGenome) {
        this.orgGenome = orgGenome;
    }
    
    public void setGenotypesAndSubjects(IntArrayList effectIndivID, List<Individual> subjectList1, int[] pedVCFIDMap,
            boolean isPhased) {
        this.effectIndivIDInVCF = effectIndivID;
        this.isPhased = isPhased;
        this.subjectList = new ArrayList<Individual>();
        int sizeIndiv = subjectList1.size();
        for (int s = 0; s < sizeIndiv; s++) {
            Individual indiv0 = subjectList1.get(s);
            Individual indiv = new Individual();
            indiv.setLabelInChip(indiv0.getLabelInChip());
            indiv.setFamilyID(indiv0.getFamilyID());
            indiv.setIndividualID(indiv0.getIndividualID());
            indiv.setDadID(indiv0.getDadID());
            indiv.setMomID(indiv0.getMomID());
            indiv.setAffectedStatus(indiv0.getAffectedStatus());
            subjectList.add(indiv);
        }
        
        caeSetID = new IntArrayList();
        controlSetID = new IntArrayList();
        for (int i = 0; i < sizeIndiv; i++) {
            if (subjectList.get(i).getAffectedStatus() == 2) {
                caeSetID.add(i);
            } else if (subjectList.get(i).getAffectedStatus() == 1) {
                controlSetID.add(i);
            }
        }
        this.pedVCFIDMap = pedVCFIDMap;
    }
    
    public VCFParseTask(int threadID) {
        this.threadID = threadID;
        varChroms = new ArrayList[STAND_CHROM_NAMES.length];
        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            varChroms[i] = new ArrayList<>();
        }
        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            chromNameIndexMap.put(STAND_CHROM_NAMES[i], i);
        }
        
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.register(char[].class);
        kryo.register(long[].class);
        kryo.register(float[].class);
        kryo.register(String[].class);
        
        kryo.register(StringBuilder.class);
        kryo.register(Variant.class);
        kryo.register(OpenIntIntHashMap.class);
        kryo.register(String.class, new DeflateSerializer(new StringSerializer()));
        
    }
    
    public void setVcfLabelSet(Set<String> vcfLabelSet) {
        this.vcfLabelSet = vcfLabelSet;
    }
    
    public void setQuantitativeQCParams(double avgSeqQualityThrehsold, double minMappingQual, double maxStrandBias, double maxFisherStrandBias, int maxGtyAlleleNum, double gtyQualityThrehsold, int minGtySeqDepth, int minSeqDepth, double altAlleleFracRefHomThrehsold, double altAlleleFractHetThrehsold, double altAlleleFractAltHomThrehsold, int minSecondPL, double minBestGP, int minOBS, double sampleMafOver, double sampleMafLess) {
        this.avgSeqQualityThrehsold = avgSeqQualityThrehsold;
        this.minMappingQual = minMappingQual;
        this.maxStrandBias = maxStrandBias;
        this.maxFisherStrandBias = maxFisherStrandBias;
        this.gtyQualityThrehsold = gtyQualityThrehsold;
        this.minGtySeqDepth = minGtySeqDepth;
        this.minSeqDepth = minSeqDepth;
        this.altAlleleFracRefHomThrehsold = altAlleleFracRefHomThrehsold;
        this.altAlleleFractHetThrehsold = altAlleleFractHetThrehsold;
        this.altAlleleFractAltHomThrehsold = altAlleleFractAltHomThrehsold;
        this.minSecondPL = minSecondPL;
        this.minBestGP = minBestGP;
        this.minOBS = minOBS;
        this.sampleMafOver = sampleMafOver;
        this.sampleMafLess = sampleMafLess;
        
        this.maxGtyAlleleNum = maxGtyAlleleNum;
    }
    
    public void setColIndex(int indexCHROM, int indexPOS, int indexID, int indexREF, int indexALT, int indexQUAL, int indexFILTER, int indexINFO, int indexFORMAT) {
        this.indexCHROM = indexCHROM;
        this.indexPOS = indexPOS;
        this.indexID = indexID;
        this.indexREF = indexREF;
        this.indexALT = indexALT;
        this.indexQUAL = indexQUAL;
        this.indexFILTER = indexFILTER;
        this.indexFORMAT = indexFORMAT;
        this.indexINFO = indexINFO;
    }
    
    @Override
    public String call() throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            if (needGtyQual) {
                vcfWriters = new BufferedWriter[STAND_CHROM_NAMES.length];
                for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                    String chrNameP = "Chromosome." + STAND_CHROM_NAMES[i];
                    chrNameP = threadID + "." + chrNameP;
                    int fileIndex = -1;
                    
                    File fileName = null;
                    File folder = new File(storagePath);
                    if (folder.exists()) {
                        do {
                            fileIndex++;
                            fileName = new File(storagePath + File.separator + chrNameP + ".vcf.gz." + fileIndex);
                            //the initial size of the compressed file is 
                            if (fileName.length() <= 20) {
                                break;
                            }
                        } while (fileName.exists());
                    } else {
                        fileIndex++;
                        folder.mkdirs();
                    }

                    //comments: both Kryo and FSTObjectOutput are excellent tools for Serializationl. However, the former produced slightly smaller file and was slightly faster. So I used Kryo
                    fileName = new File(storagePath + File.separator + chrNameP + ".vcf.gz." + fileIndex);
                    
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(fileName));
                    vcfWriters[i] = new BufferedWriter(new OutputStreamWriter(gzOut));
                }
            }
        } catch (Exception ex) {
            LOG.error(ex);
        }
        parseVariantsInFileOnlyFastToken();
        
        fireTaskComplete();
        try {
            if (needGtyQual) {
                for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                    vcfWriters[i].close();
                }
            }
        } catch (Exception ex) {
            LOG.error(ex);
        }
        String info = "Elapsed time: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds.";
        //  System.out.println(info);
        //return info;
        return info;
        // 
    }
    
}
