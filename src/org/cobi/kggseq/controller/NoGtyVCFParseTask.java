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
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.entity.Genome;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.text.Util;
import org.cobi.util.thread.Task;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 *
 * @author mxli
 */
public class NoGtyVCFParseTask extends Task implements Callable<String>, Constants {

    private static final Logger LOG = Logger.getLogger(NoGtyVCFParseTask.class);
    Map<String, Integer> chromNameIndexMap = new HashMap<String, Integer>();
    final String UNKNOWN_CHROM_NAME0 = "Un";
    final String UNKNOWN_CHROM_NAME1 = "GL";
    List<Variant>[] varChroms = null;

    String storagePath;
    Kryo kryo = new Kryo();

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

    int maxGtyAlleleNum;

    Set<String> vcfLabelSet;

    boolean considerSNP;
    boolean considerIndel;
    boolean needGty;
    boolean needReadsInfor;
    boolean needGtyQual;

    //result variables   
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
    int maxColNum = 0;
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

        maxColNum = indexCHROM;
        maxColNum = Math.max(maxColNum, indexPOS);
        maxColNum = Math.max(maxColNum, indexID);
        maxColNum = Math.max(maxColNum, indexREF);
        maxColNum = Math.max(maxColNum, indexALT);
        maxColNum = Math.max(maxColNum, indexQUAL);
        maxColNum = Math.max(maxColNum, indexFILTER);
        maxColNum = Math.max(maxColNum, indexFORMAT);
        maxColNum = Math.max(maxColNum, indexINFO);
        maxVcfIndivNum += 1;

        controlSize = controlSetID.size();
        caseSize = caeSetID.size();
    }

    public int parseVariantsInFileOnlyFastToken() {
        String currentLine = null;
        int acceptVarNum = 0;

        boolean hasAlt;
        try {
            do {
                totalVarNum++;
                // System.out.println(currentLine);
                currentLine = currentLine.trim();
                if (currentLine.isEmpty()) {
                    continue;
                }

                // st = new StringTokenizer(currentLine, delimiter);
                String[] cells = Util.tokenize(currentLine, '\t', maxEffectiveColVCF);
                // String[] cells1 = currentLine.split(currentLine);

                if (cells.length < 2) {
                    cells = Util.tokenizeIngoreConsec(currentLine, ' ', maxEffectiveColVCF);
                }
                //initialize varaibles
                incomplete = true;

                currChr = null;
                makerPostion = -1;
                varLabel = null;
                ref = null;
                alt = null;

                isLowQualBreak = false;
                isInvalid = false;

                mappingQual = Integer.MAX_VALUE;
                strandBias = Integer.MIN_VALUE;

//#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
//chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 
//chr1	53598	.	CCTA	C	447.88	PASS	AC=2;AF=1.00;AN=2;DP=0;Dels=0.50;HRun=0;HaplotypeScore=0.0000;MQ=20.50;MQ0=5;QD=40.72;SB=-138.61;sumGLbyD=46.54	GT:AD:DP:GQ:PL	./.	1/1:2,1:0:3.01:66,3,0
                hasAlt = true;
                currChr = cells[indexCHROM];

                if (currChr.indexOf("GL") >= 0) {
                    currChr = currChr.toString();
                    continue;
                }
                if (currChr.indexOf("Un") >= 0) {
                    currChr = currChr.toString();
                    continue;
                }
                //Mitochondrion
                if (currChr.indexOf("T") >= 0 || currChr.indexOf("t") >= 0) {
                    currChr = "M";
                } else {
                    if (currChr.charAt(0) == 'c' || currChr.charAt(0) == 'C') {
                        currChr = currChr.substring(3);
                    } else {
                        currChr = currChr.toString();
                    }
                }

                makerPostion = Util.parseInt(cells[indexPOS]);
                varLabel = cells[indexID];
                if (varLabel.charAt(0) != 'r') {
                    nonRSVariantNum++;
                }
                ref = cells[indexREF];
                alt = cells[indexALT];
                //for reference data, sometimes we do not have alternative alleles
                if (alt.equals(".")) {
                    hasAlt = false;
                    continue;
                }

                if (checkVCFfilter && cells[indexFILTER].length() > 1 && !vcfLabelSet.contains(cells[indexFILTER])) {
                    vcfFilterOutNum++;
                    isLowQualBreak = true;
                    continue;
                }

                if (avgSeqQualityThrehsold > 0) {
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

                if (incomplete) {
                    if (!isLowQualBreak && currChr.indexOf("GL") < 0 && currChr.indexOf("Un") < 0 && hasAlt) {
                        formatProbVarNum++;
                        LOG.error("Format error at line : " + currentLine);
                    }
                    continue;
                }

                isIndel = false;
                String[] cells1 = alt.split(",");
                for (int ss = 0; ss < cells1.length; ss++) {
                    if (ref.length() != 1 || cells1[ss].length() != 1) {
                        isIndel = true;
                        break;
                    }
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

                if (currChr.startsWith(UNKNOWN_CHROM_NAME0) | currChr.startsWith(UNKNOWN_CHROM_NAME1)) {
                    continue;
                }
                Integer chromID = chromNameIndexMap.get(currChr);
                if (chromID == null) {
                    //System.err.println("Unrecognized chromosome name: " + currChr);
                    continue;
                }

                cells1 = alt.split(",");

                //    if (modeMatched && (!caseSharedHomoAllele.isEmpty()||!caseSharedHeteAllele.isEmpty())) {
                //decide substitution, insertion or deletion
                String[] altAlleles = new String[cells1.length];
                isIndel = false;

                for (int ss = 0; ss < cells1.length; ss++) {
                    alt = cells1[ss];
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
                        for (t = ref.length(); t > 0; t--) {
                            tmpSB.append('+');
                        }
                        tmpSB.append(alt.substring(ref.length()));
                        altAlleles[ss] = tmpSB.toString();

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
                        for (t = 0; t < ref.length(); t++) {
                            if (t >= alt.length()) {
                                tmpSB.append('-');
                            } else {
                                tmpSB.append(alt.charAt(t));
                            }
                        }

                        altAlleles[ss] = tmpSB.toString();
                        isIndel = true;
                    } else {
                        String info = "Unexpected (REF	ALT) format when parsing at line : " + currentLine;
                        LOG.info(info);
                        isInvalid = true;
                        //throw new Exception(info);
                    }
                }
                if (isInvalid) {
                    continue;
                }

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
            } while ((currentLine = br.readLine()) != null);

            if (acceptVarNum > 0) {
                writeChromosomeToDiskClean();
                totalAcceptVarNum += acceptVarNum;
                acceptVarNum = 0;
            }
        } catch (Exception nex) {

            String info = nex.toString() + " when parsing at line : " + currentLine;
            //LOG.error(nex, info);
            // throw new Exception(info);
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

    public void setBooleanFilter(boolean considerSNP, boolean considerIndel, boolean needGty, boolean needReadsInfor, boolean needGtyQual) {
        this.considerSNP = considerSNP;
        this.considerIndel = considerIndel;
        this.needGty = needGty;
        this.needReadsInfor = needReadsInfor;
        this.needGtyQual = needGtyQual;
    }

    Genome orgGenome;

    public List<Variant>[] getVarChroms() {
        return varChroms;
    }

    public void setOrgGenome(Genome orgGenome) {
        this.orgGenome = orgGenome;
    }

    public NoGtyVCFParseTask(int threadID) {

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
        kryo.register(String.class, new DeflateSerializer(new DefaultSerializers.StringSerializer()));
    }

    public void setVcfLabelSet(Set<String> vcfLabelSet) {
        this.vcfLabelSet = vcfLabelSet;
    }

    public void setQuantitativeQCParams(double avgSeqQualityThrehsold, double minMappingQual, double maxStrandBias, double maxFisherStrandBias, int maxGtyAlleleNum) {
        this.avgSeqQualityThrehsold = avgSeqQualityThrehsold;
        this.minMappingQual = minMappingQual;
        this.maxStrandBias = maxStrandBias;
        this.maxFisherStrandBias = maxFisherStrandBias;

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
//by default
        parseVariantsInFileOnlyFastToken();

        fireTaskComplete();
        String info = "Elapsed time: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds.";
        //  System.out.println(info);
        //return info;
        return info;
        // 

    }

}
