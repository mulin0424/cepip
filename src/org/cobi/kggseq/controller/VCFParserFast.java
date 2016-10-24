/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import cern.colt.list.IntArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.zip.ZipException;
import org.apache.log4j.Logger;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.entity.Genome;
import org.cobi.kggseq.entity.Individual;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.text.Util;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.text.ByteInputStream;

/**
 *
 * @author mxli
 */
public class VCFParserFast implements Constants {

    private static final Logger LOG = Logger.getLogger(VCFParserFast.class);
    Map<String, Integer> fullchromNameIndexMap = new HashMap<String, Integer>();
    final String UNKNOWN_CHROM_NAME0 = "Un";
    final String UNKNOWN_CHROM_NAME1 = "GL";
    Map<String, Integer> chromNameIndexMap = new HashMap<String, Integer>();

    static int varNumInRam = 0;
    StringBuilder vcfHead = new StringBuilder();

    public Genome readVariantGtyFilterOnly(String tempFolder, int threadNum, Genome orgGenome, String vAFile,
            double avgSeqQualityThrehsold, double minMappingQual, double maxStrandBias, double maxFisherStrandBias, int maxAlleleNum,
            double gtyQualityThrehsold, int minSeqDepth, double altAlleleFracRefHomThrehsold, double altAlleleFractHetThrehsold,
            double altAlleleFractAltHomThrehsold, Set<String> vcfLableInSet, int minOBS, double sampleMafOver, double sampleMafLess, boolean considerSNP, boolean considerIndel,
            int minSecondPL, double minBestGP, boolean needProgressionIndicator, boolean needGty, boolean needReadsInfor, boolean needGtyQual, boolean noGtyVCF, List<Individual> subjectList, IntArrayList effectIndivID) throws Exception {
        Genome genome = new Genome("UniuqeVariantGenome", tempFolder);

        genome.removeTempFileFromDisk();

        if (subjectList == null) {
            effectIndivID = new IntArrayList();
            subjectList = new ArrayList<Individual>();
        }
        int sizeIndiv = subjectList.size();
        boolean needAccoundAffect = false;
        boolean needAccoundUnaffect = false;
        for (int i = 0; i < sizeIndiv; i++) {
            if (subjectList.get(i).getAffectedStatus() == 2) {
                needAccoundAffect = true;
            } else if (subjectList.get(i).getAffectedStatus() == 1) {
                needAccoundUnaffect = true;
            }
            if (needAccoundAffect && needAccoundUnaffect) {
                break;
            }
        }
        genome.setNeedAccoundAffect(needAccoundAffect);
        genome.setNeedAccoundUnaffect(needAccoundUnaffect);
        if (!needAccoundUnaffect && !needAccoundAffect) {
            genome.setNeedAccoundAll(false);
        }
        int acceptVarNum = 0;
        //  _CHROM_
        if (vAFile.contains("_CHROM_")) {
            for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                if (orgGenome != null) {
                    List<Variant> vars = orgGenome.getChromVariants(STAND_CHROM_NAMES[i]);
                    if (vars == null || vars.isEmpty()) {
                        continue;
                    }
                }
                File dataFile = new File(vAFile.replaceAll("_CHROM_", STAND_CHROM_NAMES[i]));
                if (!dataFile.exists()) {
                    LOG.warn(dataFile.getCanonicalPath() + " does not exist!");
                    continue;
                }
                acceptVarNum += readVariantsInFileOnlyFastToken(threadNum, orgGenome, dataFile, genome, avgSeqQualityThrehsold, minMappingQual, maxStrandBias, maxFisherStrandBias, maxAlleleNum, gtyQualityThrehsold, minSeqDepth,
                        altAlleleFracRefHomThrehsold, altAlleleFractHetThrehsold, altAlleleFractAltHomThrehsold, vcfLableInSet,
                        minOBS, sampleMafOver, sampleMafLess, considerSNP, considerIndel, minSecondPL, minBestGP, needProgressionIndicator,
                        needGty, needReadsInfor, needGtyQual, noGtyVCF, subjectList, effectIndivID);

            }
        } else if (vAFile.indexOf('[') >= 0 && vAFile.indexOf(']') > 0) {
            int index1 = vAFile.indexOf('[');
            int index2 = vAFile.indexOf(']');
            String[] chroms = vAFile.substring(index1 + 1, index2).split(",");
            for (int i = 0; i < chroms.length; i++) {
                if (orgGenome != null) {
                    List<Variant> vars = orgGenome.getChromVariants(chroms[i]);
                    if (vars == null || vars.isEmpty()) {
                        continue;
                    }
                }
                File dataFile = new File(vAFile.substring(0, index1) + chroms[i] + vAFile.substring(index2 + 1));
                if (!dataFile.exists()) {
                    LOG.error(dataFile.getCanonicalPath() + " does not exist!");
                    continue;
                }

                acceptVarNum += readVariantsInFileOnlyFastToken(threadNum, orgGenome, dataFile, genome, avgSeqQualityThrehsold, minMappingQual, maxStrandBias, maxFisherStrandBias, maxAlleleNum, gtyQualityThrehsold, minSeqDepth,
                        altAlleleFracRefHomThrehsold, altAlleleFractHetThrehsold, altAlleleFractAltHomThrehsold, vcfLableInSet,
                        minOBS, sampleMafOver, sampleMafLess, considerSNP, considerIndel, minSecondPL, minBestGP,
                        needProgressionIndicator, needGty, needReadsInfor, needGtyQual, noGtyVCF, subjectList, effectIndivID);
            }
        } else {
            File dataFile = new File(vAFile);
            //readVariantsInFileOnlyFastToken
            acceptVarNum = readVariantsInFileOnlyFastToken(threadNum, orgGenome, dataFile, genome, avgSeqQualityThrehsold, minMappingQual, maxStrandBias, maxFisherStrandBias, maxAlleleNum, gtyQualityThrehsold, minSeqDepth,
                    altAlleleFracRefHomThrehsold, altAlleleFractHetThrehsold, altAlleleFractAltHomThrehsold, vcfLableInSet,
                    minOBS, sampleMafOver, sampleMafLess, considerSNP, considerIndel, minSecondPL, minBestGP,
                    needProgressionIndicator, needGty, needReadsInfor, needGtyQual, noGtyVCF, subjectList, effectIndivID);
        }

        // genome.setVarNum(acceptVarNum);
        return genome;
    }

    public int readVariantsInFileOnlyFastToken(int threadNum, Genome orgGenome, File dataFile, final Genome targGenome,
            double avgSeqQualityThrehsold, double minMappingQual, double maxStrandBias, double maxFisherStrandBias, int maxAlleleNum, double gtyQualityThrehsold, int minGtySeqDepth,
            double altAlleleFracRefHomThrehsold, double altAlleleFractHetThrehsold, double altAlleleFractAltHomThrehsold, Set<String> vcLlabelInSet,
            int minOBS, double sampleMafOver, double sampleMafLess, boolean considerSNP, boolean considerIndel, int minSecondPL, double minBestGP, boolean needProgressionIndicator,
            boolean needGty, boolean needReadInfo, boolean needGtyQual, boolean noGtyVCF, final List<Individual> masterSubjectList, IntArrayList effectIndivID) throws Exception {
        int indexCHROM = -1;
        int indexPOS = -1;
        int indexID = -1;
        int indexREF = -1;
        int indexALT = -1;
        int indexQUAL = -1;
        int indexFILTER = -1;
        int indexFORMAT = -1;
        int indexINFO = -1;

        int ignoredLineNumNullCase = 0;
        int ignoredLineNumMinOBS = 0;
        int ignoredLineNumMinMAF = 0;
        int ignoredLineNumMaxMAF = 0;
        int ignoredLineNumNoVar = 0;

        byte[] currentLine = null;

        StringBuilder tmpBuffer = new StringBuilder(100);

        long fileLineCounter = 0;
        int varLineCounter = 0;

        int filterOutLowQualNum = 0;
        int vcfFilterOutNum = 0;

        int vcfIndivNum = 0;

        int ignoredLowQualGtyNum = 0;
        int ignoredLowDepthGtyNum = 0;
        int ignoredBadAltFracGtyNum = 0;
        int ignoredLowPLGtyNum = 0;
        int ignoredLowGPGtyNum = 0;
        int ignoreStrandBiasSBNum = 0;
        int missingGtyNum = 0;
        int formatProbVarNum = 0;

        int ignoreMappingQualNum = 0;
        int ignoreStrandBiasFSNum = 0;
        int ignoredVarBymaxGtyAlleleNum = 0;

        List<String> vcfSubjectIDList = new ArrayList<String>();
        int maxThreadNum = threadNum;
        int rowBufferSizePerThread = 1000000 / maxThreadNum;

        int runningThread = 0;

        ExecutorService exec = Executors.newFixedThreadPool(maxThreadNum);
        final CompletionService<String> serv = new ExecutorCompletionService<String>(exec);
        //   CompletionService serv = new ExecutorCompletionService(exec);
        LOG.info("Reading variants in " + dataFile.getCanonicalPath());
        if (!dataFile.exists()) {
            throw new Exception("No such a file: " + dataFile.getCanonicalPath());
        }

        ByteInputStream br = null;
        if (dataFile.exists()) {
            try {
                br = new ByteInputStream(dataFile, 1024 * 1024 * 100, '\t');
            } catch (ZipException ex) {
                ex.printStackTrace();
            }
        } else {
            throw new Exception("No input file: " + dataFile.getCanonicalPath());
        }

        boolean isPhased = false;
        int[] pedVCFIDMap = null;
        StringBuilder subInfo = new StringBuilder();

        int indelNum = 0, snvNum = 0;
        StringBuilder ignoredVCFIndivLables = new StringBuilder();
        StringBuilder ignoredPedIndivLables = new StringBuilder();

        int effectiveIndivNum = 0;
        int totalPedSubjectNum = 0;
        int ignoredPedIndivNum = 0;
        int ignoredVCFIndivNum = 0;
        int ignoredInproperChromNum = 0;
        boolean matched = false;
        effectIndivID.clear();

        boolean needHead = false;
        int acceptVarNum = 0;
        if (vcfHead.length() == 0 && needGtyQual) {
            needHead = true;
        }
        String tmpStr = null;
        try {
            //skip to the head line 
            while ((currentLine = br.readLine()) != null) {
                //add the end char 
                fileLineCounter++;

                tmpStr = new String(currentLine);
                if (tmpStr.startsWith("#CHROM")) {
                    break;
                }
                if (needHead) {
                    vcfHead.append(tmpStr);
                    vcfHead.append('\n');
                }
            }
            if (currentLine == null) {
                br.getInputStream().close();
                return 0;
            }
            //parse head line
            StringTokenizer st = new StringTokenizer(tmpStr);

            int iCol = 0;
            //#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
//chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 

            pedVCFIDMap = new int[masterSubjectList.size()];
            Arrays.fill(pedVCFIDMap, -9);
//System.out.println(currentLine); 
            while (st.hasMoreTokens()) {
                // System.out.println(st);
                tmpBuffer.delete(0, tmpBuffer.length());
                tmpBuffer.append(st.nextToken().trim());
                //  System.out.println(tmpBuffer); 
                if (tmpBuffer.toString().equals("#CHROM")) {
                    indexCHROM = iCol;
                } else if (tmpBuffer.toString().equals("POS")) {
                    indexPOS = iCol;
                } else if (tmpBuffer.toString().equals("ID")) {
                    indexID = iCol;
                } else if (tmpBuffer.toString().equals("REF")) {
                    indexREF = iCol;
                } else if (tmpBuffer.toString().equals("ALT")) {
                    indexALT = iCol;
                } else if (tmpBuffer.toString().equals("QUAL")) {
                    indexQUAL = iCol;
                } else if (tmpBuffer.toString().equals("FILTER")) {
                    indexFILTER = iCol;
                } else if (tmpBuffer.toString().equals("INFO")) {
                    indexINFO = iCol;
                } else if (tmpBuffer.toString().equals("FORMAT")) {
                    //warning: assume the FORMAT is the last meta column
                    indexFORMAT = iCol;
                    vcfIndivNum = 0;
                    if (noGtyVCF) {
                        break;
                    }
                    while (st.hasMoreTokens()) {
                        tmpBuffer.delete(0, tmpBuffer.length());
                        tmpBuffer.append(st.nextToken().trim());
                        subInfo.append(tmpBuffer);
                        subInfo.append(",");
                        vcfSubjectIDList.add(tmpBuffer.toString());
                        vcfIndivNum++;
                    }

                    effectiveIndivNum = 0;
                    if (!masterSubjectList.isEmpty()) {
                        for (int tt = 0; tt < masterSubjectList.size(); tt++) {
                            Individual indv = masterSubjectList.get(tt);
                            matched = false;
                            for (int s = 0; s < vcfIndivNum; s++) {
                                String label = vcfSubjectIDList.get(s);

                                if (indv.getLabelInChip().equals(label)) {
                                    pedVCFIDMap[tt] = s;
                                    effectIndivID.add(s);

                                    matched = true;
                                    break;
                                }
                            }

                            if (!matched) {
                                ignoredPedIndivLables.append(indv.getLabelInChip()).append(", ");
                                ignoredPedIndivNum++;
                            }
                        }
                    }
                }
                iCol++;
            }

            totalPedSubjectNum = masterSubjectList.size();
            if (totalPedSubjectNum > 0) {
                //just for summary
                for (int s = 0; s < vcfIndivNum; s++) {
                    String label = vcfSubjectIDList.get(s);
                    matched = false;
                    for (int tt = 0; tt < totalPedSubjectNum; tt++) {
                        Individual indv = masterSubjectList.get(tt);
                        if (indv.getLabelInChip().equals(label)) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        ignoredVCFIndivLables.append(label).append(", ");
                        ignoredVCFIndivNum++;
                    }
                }

                effectiveIndivNum = totalPedSubjectNum - ignoredPedIndivNum;
                if (ignoredPedIndivLables.length() > 0) {
                    LOG.info("The following " + ignoredPedIndivNum + " subject(s) in pedigree file or (input subject list) are ignored:\n" + ignoredPedIndivLables.substring(0, ignoredPedIndivLables.length() - 2) + ".");
                }
                if (ignoredVCFIndivLables.length() > 0) {
                    LOG.info("The following " + ignoredVCFIndivNum + " subject(s) in VCF file are ignored:\n" + ignoredVCFIndivLables.substring(0, ignoredVCFIndivLables.length() - 2) + ".");
                }

                if (effectiveIndivNum == 0) {
                    LOG.info("No valid subjects in the VCF file specified!!");
                    return 0;
                }
            } else if (!noGtyVCF) {
                pedVCFIDMap = new int[vcfIndivNum];
                for (int s = 0; s < vcfIndivNum; s++) {
                    Individual indiv = new Individual();
                    indiv.setLabelInChip(vcfSubjectIDList.get(s));
                    indiv.setFamilyID(String.valueOf(s));
                    indiv.setIndividualID(String.valueOf(s));
                    indiv.setDadID("0");
                    indiv.setMomID("0");
                    indiv.setAffectedStatus(0);
                    masterSubjectList.add(indiv);
                    pedVCFIDMap[s] = s;
                    effectIndivID.add(s);
                }
                totalPedSubjectNum = vcfIndivNum;
                effectiveIndivNum = vcfIndivNum;
            }

            if (!noGtyVCF && vcfIndivNum <= 0) {
                //remaining counts 
                pedVCFIDMap = new int[vcfIndivNum];
                for (int s = 0; s < vcfIndivNum; s++) {
                    Individual indiv = new Individual();
                    indiv.setLabelInChip(s + "@" + s);
                    indiv.setFamilyID(String.valueOf(s));
                    indiv.setIndividualID(String.valueOf(s));
                    indiv.setDadID("0");
                    indiv.setMomID("0");
                    indiv.setAffectedStatus(0);
                    masterSubjectList.add(indiv);
                    pedVCFIDMap[s] = s;
                    effectIndivID.add(s);
                }
                totalPedSubjectNum = vcfIndivNum;
                effectiveIndivNum = vcfIndivNum;
            }

            runningThread = 0;
            //I spend 2 days to write a new VCFParseTask which I expected to be faster by modifying the Uitl.tokenize function
            //Unfortunately, the new Task is much slower than
            //VCFParseTask[] parsTaskArray = new VCFParseTask[maxThreadNum];
            VCFParseTaskFast[] parsTaskArray = new VCFParseTaskFast[maxThreadNum];

            for (int s = 0; s < maxThreadNum; s++) {
                parsTaskArray[s] = new VCFParseTaskFast(s);
                // parsTaskArray[s] = new VCFParseTaskFast1(s);
                parsTaskArray[s].setQuantitativeQCParams(avgSeqQualityThrehsold, minMappingQual, maxStrandBias, maxFisherStrandBias, maxAlleleNum, gtyQualityThrehsold, minGtySeqDepth, minGtySeqDepth, altAlleleFracRefHomThrehsold, altAlleleFractHetThrehsold, altAlleleFractAltHomThrehsold, minSecondPL, minBestGP, minOBS, sampleMafOver, sampleMafLess);
                parsTaskArray[s].setColIndex(indexCHROM, indexPOS, indexID, indexREF, indexALT, indexQUAL, indexFILTER, indexINFO, indexFORMAT);
                parsTaskArray[s].setGenotypesAndSubjects(effectIndivID, masterSubjectList, pedVCFIDMap, isPhased);
                parsTaskArray[s].setVcfLabelSet(vcLlabelInSet);
                parsTaskArray[s].setBooleanFilter(considerSNP, considerIndel, needGty, needReadInfo, needGtyQual, noGtyVCF);
                parsTaskArray[s].prepareTempVariables();
                // parsTaskArray[s].addTaskListener(new MyTaskListener(parsTaskArray[s]));
                parsTaskArray[s].setBr(br);

                parsTaskArray[s].setMaxVarNum(rowBufferSizePerThread);
                parsTaskArray[s].setStoragePath(targGenome.getStoragePath());
                serv.submit(parsTaskArray[s]);
                runningThread++;
            }

            for (int s = 0; s < runningThread; s++) {
                Future<String> task = serv.take();
                String infor = task.get();
                //  System.out.println(infor);
            }
            exec.shutdown();
            varLineCounter = 0;
            for (int s = 0; s < maxThreadNum; s++) {
                missingGtyNum += parsTaskArray[s].getMissingGtyNum();
                ignoredLowDepthGtyNum += parsTaskArray[s].getIgnoredLowDepthGtyNum();
                ignoredLowQualGtyNum += parsTaskArray[s].getIgnoredLowQualGtyNum();
                ignoredBadAltFracGtyNum += parsTaskArray[s].getIgnoredBadAltFracGtyNum();
                ignoredLowPLGtyNum += parsTaskArray[s].getIgnoredLowPLGtyNum();
                ignoredLowGPGtyNum += parsTaskArray[s].getIgnoredLowGPGtyNum();

                ignoredInproperChromNum += parsTaskArray[s].getIgnoredInproperChromNum();
                ignoredVarBymaxGtyAlleleNum += parsTaskArray[s].getIgnoredVarBymaxGtyAlleleNum();
                formatProbVarNum += parsTaskArray[s].getFormatProbVarNum();
                vcfFilterOutNum += parsTaskArray[s].getVcfFilterOutNum();
                filterOutLowQualNum += parsTaskArray[s].getFilterOutLowQualNum();
                ignoreMappingQualNum += parsTaskArray[s].getIgnoreMappingQualNum();
                ignoreStrandBiasSBNum += parsTaskArray[s].getIgnoreStrandBiasSBNum();
                ignoreStrandBiasFSNum += parsTaskArray[s].getIgnoreStrandBiasFSNum();
                ignoredLineNumMinOBS += parsTaskArray[s].getIgnoredLineNumMinOBS();
                ignoredLineNumMinMAF += parsTaskArray[s].getIgnoredLineNumMinMAF();
                ignoredLineNumMaxMAF += parsTaskArray[s].getIgnoredLineNumMaxMAF();
                //ignoredLineNumNullCase += parsTaskArray[s].getMissingGtyNum();
                // ignoredLineNumNoVar += parsTaskArray[s].getMissingGtyNum();
                indelNum += parsTaskArray[s].getIndelNum();
                snvNum += parsTaskArray[s].getSnvNum();
                acceptVarNum += parsTaskArray[s].getTotalAcceptVarNum();
                varLineCounter += parsTaskArray[s].getTotalVarNum();
                parsTaskArray[s] = null;
            }
            System.gc();
        } catch (Exception nex) {
            nex.printStackTrace();
            //  String info = nex.toString() + " when parsing at line " + fileLineCounter + ": " + currentLine;
            // LOG.error(nex, info);
            //  throw new Exception(info);
        } finally {
            br.getInputStream().close();
        }

        StringBuilder message = new StringBuilder();
        message.append('\n');
        if (missingGtyNum > 0) {
            message.append(missingGtyNum).append(" missing genotypes are ignored \n");
        }

        if (ignoredLowDepthGtyNum > 0) {
            message.append(ignoredLowDepthGtyNum).append(" genotypes are ignored due to low depth at the site with a genotype <").append(minGtySeqDepth).append('\n');
        }

        if (ignoredLowQualGtyNum > 0) {
            message.append(ignoredLowQualGtyNum).append(" genotypes are ignored due to low quality of specific genotyping quality <").append(gtyQualityThrehsold).append('\n');
        }

        if (ignoredBadAltFracGtyNum > 0) {
            message.append(ignoredBadAltFracGtyNum).append(" genotypes are ignored because the fraction of the reads carrying alternative allele >= ").append(Util.doubleToString(altAlleleFracRefHomThrehsold, 3)).
                    append(" at a reference-allele homozygous genotype and that is <= ").append(Util.doubleToString(altAlleleFractHetThrehsold, 3)).append(" at a heterozygous genotype and that is <= ").
                    append(Util.doubleToString(altAlleleFractAltHomThrehsold, 3)).append(" at an alternative-allele homozygous genotype \n");
        }
        if (ignoredLowPLGtyNum > 0) {
            message.append(ignoredLowPLGtyNum).append(" genotypes are ignored because their second smallest Phred-scaled likelihoods (PL) are < ").append(Util.doubleToString(minSecondPL, 0)).
                    append(".\n");
        }

        if (ignoredLowGPGtyNum > 0) {
            message.append(ignoredLowGPGtyNum).append(" genotypes are ignored because their best genotype probabilities (GP) are < ").append(Util.doubleToString(minBestGP, 3)).
                    append(".\n");
        }
        message.append('\n');

        if (ignoredVarBymaxGtyAlleleNum > 0) {
            message.append(ignoredVarBymaxGtyAlleleNum).append(" variants are ignored because the number of alleles is > ").append(maxAlleleNum).append(";\n");
        }
        if (ignoredInproperChromNum > 0) {
            message.append(ignoredInproperChromNum).append(" variants are ignored probably because of irregular chromosome labels;\n");
        }

        if (formatProbVarNum > 0) {
            message.append(formatProbVarNum).append(" variants are ignored probably because of problematic format;\n");
        }

        if (vcfFilterOutNum > 0) {
            message.append(vcfFilterOutNum).append(" variants are ignored due to the lack of the vcf FILTER in ").append(vcLlabelInSet.toString()).append("\n");
        }

        if (filterOutLowQualNum > 0) {
            message.append(filterOutLowQualNum).append(" variants are ignored due to low sequencing quality (<").append(avgSeqQualityThrehsold).append(")\n");
        }

        if (ignoreMappingQualNum > 0) {
            message.append(ignoreMappingQualNum).append(" variants are ignored due to low RMS mapping quality (<").append(minMappingQual).append(");\n");
        }

        if (ignoreStrandBiasSBNum > 0) {
            message.append(ignoreStrandBiasSBNum).append(" variants are ignored due to large strand bias (").append(maxStrandBias).append(");\n");
        }

        if (ignoreStrandBiasFSNum > 0) {
            message.append(ignoreStrandBiasFSNum).append(" variants are ignored due to large strand bias (>").append(maxFisherStrandBias).append(");\n");
        }

        if (ignoredLineNumMinOBS > 0) {
            message.append(ignoredLineNumMinOBS).append(" variants are ignored due to the number of non-null genotypes in sample <").append(minOBS).append('\n');
        }

        if (ignoredLineNumMinMAF > 0) {
            message.append(ignoredLineNumMinMAF).append(" variants are ignored due to their minor allele frequency (MAF) in sample <=").append(sampleMafOver).append('\n');
        }
        if (ignoredLineNumMaxMAF > 0) {
            message.append(ignoredLineNumMaxMAF).append(" variants are ignored due to their minor allele frequency (MAF) in sample >=").append(sampleMafLess).append('\n');
        }

        if (ignoredLineNumNullCase > 0) {
            message.append(ignoredLineNumNullCase).append(" variants are ignored because patients have missing genotypes\n");
        }
        if (ignoredLineNumNoVar > 0) {
            message.append(ignoredLineNumNoVar).append(" variants are ignored because no subjects have valid genotypes after QC\n");
        }

        if (!considerIndel) {
            message.append(indelNum).append(" Indel variants are ignored\n");
        }
        if (!considerSNP) {
            message.append(snvNum).append(" SNV variants are ignored\n");
        }
        targGenome.setVarNum(acceptVarNum);

        message.append('\n').append(varLineCounter).append(" variant-lines (").append(indelNum).append(" indels").append(") are scanned; and ").append(acceptVarNum).append(" variants of ").append(effectiveIndivNum).append(" individual(s) are valid in ").append(dataFile.getCanonicalPath());
        LOG.info(message);

        //change the order to be consisten with the pedigree file
        // effectIndivID.quickSort();
        return acceptVarNum;
    }

    public StringBuilder getVcfHead() {
        return vcfHead;
    }

    public void setVcfHead(StringBuilder vcfHead) {
        this.vcfHead = vcfHead;
    }

    public void extractSubjectIDsVCFInFile(String vAFile, List<Individual> subList) throws Exception {
        long lineCounter = 0;

        File dataFile = new File(vAFile);
        BufferedReader br = null;
        if (dataFile.exists() && dataFile.getName().endsWith(".zip")) {
            br = LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
        } else {
            if (dataFile.exists() && dataFile.getName().endsWith(".gz")) {
                br = LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
            } else {
                if (dataFile.exists()) {
                    br = LocalFileFunc.getBufferedReader(dataFile.getCanonicalPath());
                } else {
                    throw new Exception("No input file: " + dataFile.getCanonicalPath());
                }
            }
        }
        String currentLine = null;

        //skip to the head line 
        while ((currentLine = br.readLine()) != null) {
            lineCounter++;

            if (currentLine.startsWith("#CHROM")) {
                break;
            }
        }

        //parse head line
        StringTokenizer st = new StringTokenizer(currentLine.trim());
        int iCol = 0;
        //#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  
        //chr1    109     .       A       T       237.97  PASS    AC=21;AF=0.328;AN=64;DP=47;Dels=0.02;HRun=0;HaplotypeScore=1.9147;MQ=44.81;MQ0=48;QD=5.53;SB=-28.76;sumGLbyD=9.00       GT:AD:DP:GQ:PL  0/1:6,1:3:15.67:16,0,64 0/0:3,0:1:3.01:0,3,33 

        int indivNum = 0;
        StringBuilder tmpBuffer = new StringBuilder();
        while (st.hasMoreTokens()) {
            // System.out.println(st);
            tmpBuffer.delete(0, tmpBuffer.length());
            tmpBuffer.append(st.nextToken().trim());
            //  System.out.println(tmpBuffer);
            if (tmpBuffer.toString().equals("FORMAT")) {
                //warning: assume the FORMAT is the last meta column

                while (st.hasMoreTokens()) {
                    indivNum++;
                    tmpBuffer.delete(0, tmpBuffer.length());
                    tmpBuffer.append(st.nextToken().trim());
                    Individual indiv = new Individual();
                    indiv.setIndividualID(tmpBuffer.toString());
                    indiv.setAffectedStatus(1);
                    indiv.setFamilyID(indiv.getIndividualID());
                    indiv.setLabelInChip(indiv.getIndividualID());
                    indiv.setDadID("0");
                    indiv.setMomID("0");
                    subList.add(indiv);
                }
                break;
            }
            iCol++;
        }
        br.close();
    }

}
