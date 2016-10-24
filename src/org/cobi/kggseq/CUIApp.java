/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.FloatArrayList; 
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import static org.cobi.kggseq.Constants.VAR_FEATURE_NAMES;

import org.cobi.bayes.Bayes;

import static org.cobi.kggseq.GlobalManager.PLUGIN_PATH;

import org.cobi.kggseq.controller.BinaryGtyProcessor;
import org.cobi.kggseq.controller.CandidateGeneExtender;
import org.cobi.kggseq.controller.GeneAnnotator;
import org.cobi.kggseq.controller.GeneRegionParser;
import org.cobi.kggseq.controller.LinkageFileParser;
import org.cobi.kggseq.controller.PileupFormatParser;
//import org.cobi.kggseq.controller.PileupFormatParser;
import org.cobi.kggseq.controller.SimpleFormatParser;
import org.cobi.kggseq.controller.VariantAnnotator;
import org.cobi.kggseq.controller.VariantFilter;
import org.cobi.kggseq.dialog.PlotShowFrame;
import org.cobi.kggseq.entity.AnnotationSummarySet;
import org.cobi.kggseq.entity.CNVRegionParser;
import org.cobi.kggseq.entity.Chromosome;
import org.cobi.kggseq.entity.CombOrderComparator;
import org.cobi.kggseq.entity.CombOrders;
import org.cobi.kggseq.entity.FiltrationSummarySet;
import org.cobi.kggseq.entity.Genome;
import org.cobi.kggseq.entity.Individual;
import org.cobi.kggseq.entity.PPIGraph;
import org.cobi.kggseq.entity.GeneSet;
import org.cobi.kggseq.entity.ReferenceGenome;
import org.cobi.kggseq.entity.RegressionParams;
import org.cobi.kggseq.entity.Variant;
import org.cobi.kggseq.controller.Phenolyzer;
import org.cobi.kggseq.controller.SKAT;
import org.cobi.kggseq.controller.SequenceRetriever;
import org.cobi.kggseq.controller.VCFParserFast1;
import org.cobi.randomforests.MyRandomForest;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.net.NetUtils;
import org.cobi.util.plot.HistogramPainter;
import org.cobi.util.plot.PValuePainter;
import org.cobi.util.stat.MultipleTestingMethod;
import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.Util;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.esotericsoftware.minlog.Log;

/**
 *
 * @author mxli
 */
public class CUIApp implements Constants {

    Options options;
    private static final Logger LOG = Logger.getLogger(CUIApp.class);

    public CUIApp(Options options) throws Exception {
        this.options = options;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String headInfor = "@----------------------------------------------------------@\n" + "|        " + PREL + "        |     v" + PVERSION + "     |   " + PDATE + "     |\n"
//                + "|----------------------------------------------------------|\n" + "|  (C) 2011 Miaoxin Li,  limx54@yahoo.com                  |\n"
//                + "|----------------------------------------------------------|\n" + "|  For documentation, citation & bug-report instructions:  |\n"
//                + "|        http://statgenpro.psychiatry.hku.hk/kggseq        |\n" + "@----------------------------------------------------------@";

        String headInfor = "@----------------------------------------------------------@\n" + "|        " + "cepip" + "        |     v" + "1.1" + "     |   " + "28/July./2016 " + "     |\n"
                + "|----------------------------------------------------------|\n" + "|  For documentation, citation & bug-report instructions:  |\n"
                + "|        http://jjwanglab.org/cepip                        |\n" + "@----------------------------------------------------------@";

        long time = System.nanoTime();
        Options option = new Options();
        try {
            if (args.length == 1 && !args[0].startsWith("--")) {
                option.readOptions(args[0]);
            } else if (args.length >= 1) {
                option.readOptions(args);
            } else {
                System.out.println("Usage: java -Xmx1g -jar kggseq.jar param.txt\n Or:  java -Xmx1g -jar kggseq.jar [options] ...");
                return;
            }
            String param = option.parseOptions();
            // System.out.println(headInfor);
            LOG.info("\n" + headInfor + "\nEffective settings :\n" + param);

            GlobalManager.initiateVariables(option.refGenomeVersion, option.resourceFolder, option.maxGtyAlleleNum);
            if (option.needRconnection) {
                RConnection rcon = null;
                try {
                    rcon = new RConnection();
                } catch (RserveException ex) {
                    if (ex.getMessage().contains("Cannot connect")) {
                        // System.out.println(ex.getMessage() + "\t" +
                        // ex.getRequestReturnCode() + "\t" +
                        // ex.getRequestErrorDescription());
                        String infor = "Please open your R and type the following commands to allow kggseq to use it:\npack=\"Rserve\";\n"
                                + "if (!require(pack,character.only = TRUE))   { install.packages(pack,dep=TRUE,repos=\'http://cran.us.r-project.org\');   if(!require(pack,character.only = TRUE)) stop(\"Package not found\")   }\n"
                                + "library(\"Rserve\");\nRserve(debug = FALSE, port = 6311, args = NULL)\n"
                                + "\nOR type this command without openning your R: \nR CMD Rserve";
                        //Or  R CMD Rserve

                        LOG.fatal(infor);
                        TimeUnit.SECONDS.sleep(1);
                    }
                    System.exit(1);
                } finally {
                    if (rcon != null) {
                        rcon.close();
                    }
                }
            }
            if (!option.noLibCheck || !option.noResCheck) {
                GlobalManager.checkConnection();
            }
            if (!option.noLibCheck) {
                if (GlobalManager.isConnectInternet) {
                    if (NetUtils.checkLibFileVersion()) {
                        return;
                    }
                }
            }

            if (!option.noResCheck) {
                if (GlobalManager.isConnectInternet) {
                    NetUtils.checkLatestResource(option);
                }
            } else {
                if (GlobalManager.isConnectInternet) {
                    // must check avaible resources
                    NetUtils.checkResourceList(option);
                }
            }

            CUIApp main = new CUIApp(option);
            main.process();
            time = System.nanoTime() - time;
            time = time / 1000000000;
            long min = time / 60;
            long sec = time % 60;
            LOG.info("Elapsed time: " + min + " min. " + sec + " sec.");
            if (option.needLog) {
                String info = "The log information is saved in " + option.outputFileName + ".log" + "\n\n";
                LOG.info(info);
            }
//            LOG.info("\n\n");
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void showPlots(final File[] plotFiles) {
        if (!GraphicsEnvironment.isHeadless()) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    PlotShowFrame psf = new PlotShowFrame();
                    psf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    for (final File file : plotFiles) {
                        if (file == null) {
                            continue;
                        }
                        psf.insertImage2PlottingPane(file);
                    }
                    psf.setVisible(true);
                }
            });
        } else {
            String info = "But no avaible graphics environment to present the figure(s) here!";
            LOG.info(info);
        }
    }

    public void process() throws Exception {
        Genome uniqueGenome = null;
        Set<String> caseSet = null;
        Set<String> controlSet = null;
        Set<String> unkownSet = null;
        List<Individual> subjectList = new ArrayList<Individual>();
        int[] caeSetID = null, controlSetID = null;
        int[] pedEncodeGytIDMap = null;
        boolean needGty = false;
        File finalFilteredInFile = null;
        IntArrayList allEffectIndivIDs = new IntArrayList();
        VCFParserFast1 vsParser = new VCFParserFast1();
        //  VCFParserFast vsParser = new VCFParserFast();
        //  VCFParser vsParser = new VCFParser();
        try {
            if (options.localFilterVCFFileNames != null || options.localHardFilterVCFFileNames != null) {
                List<String> allFiles = new ArrayList<String>();
                if (options.localFilterVCFFileNames != null) {
                    allFiles.addAll(Arrays.asList(options.localFilterVCFFileNames));
                }
                if (options.localHardFilterVCFFileNames != null) {
                    allFiles.addAll(Arrays.asList(options.localHardFilterVCFFileNames));
                }
                for (String localFilterFileName : allFiles) {
                    File localFileFiler = new File(localFilterFileName + ".kggseq.filter.txt");
                    LOG.info("Prepare annotation resources from local VCF file(s)...");
                    // altLocalFilterFiles.add(localFileFiler);
                    // gtyCorrdiates used to keep the sequential order
                    uniqueGenome = vsParser.readVariantGtyFilterOnly(localFilterFileName, options.threadNum, null, options.inputFileName, options.seqQual, options.minMappingQuality, options.maxStandBias,
                            options.maxFisherStandBias, options.maxGtyAlleleNum, options.gtyQual, options.minGtySeqDP, options.maxAltAlleleFracRefHom, options.minAltAlleleFractHet,
                            options.minAltAlleleFractAltHom, options.vcfFilterLabelsIn, options.minOBS, options.sampleMafOver, options.sampleMafLess, options.considerSNV, options.considerIndel,
                            options.gtySecPL, options.gtyBestGP, options.needProgressionIndicator, false, false, false, false, null, null, null, null, null, null);

                    BufferedWriter annovarFilteredInFileWriter = null;
                    if (options.outGZ) {
                        GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(localFileFiler.getCanonicalPath() + ".gz"));
                        annovarFilteredInFileWriter = new BufferedWriter(new OutputStreamWriter(gzOut));
                    } else {
                        annovarFilteredInFileWriter = new BufferedWriter(new FileWriter(localFileFiler));
                    }
                    Chromosome[] chromosomes = uniqueGenome.getChromosomes();

                    for (int chromID = 0; chromID < chromosomes.length; chromID++) {
                        uniqueGenome.loadVariantFromDisk(chromID);
                        List<Variant> chromosomeVar = chromosomes[chromID].variantList;
                        if (chromosomeVar == null || chromosomeVar.isEmpty()) {
                            continue;
                        }
                        uniqueGenome.export2ANNOVARInput(annovarFilteredInFileWriter, chromID);
                        chromosomes[chromID].variantList.clear();
                    }
                    annovarFilteredInFileWriter.close();
                    uniqueGenome.removeTempFileFromDisk();
                    String infor = "The VCF filtration data " + localFilterFileName + " have been converted into a standard filtration dataset of kggseq, "
                            + localFileFiler.getCanonicalPath() + ".\nYou can directly use the standard one next time by \'--local-filter " + localFileFiler.getCanonicalPath()
                            + " \', which is faster!\n\n";
                    LOG.info(infor);
                    //  varFilter.markByANNOVARefFormat(uniqueGenome, localFileFiler.getCanonicalPath(), localFileFiler.getName(), options.needProgressionIndicator);
                }
            }

            if (options.localHardFilterNoGtyVCFFileNames != null || options.localFilterNoGtyVCFFileNames != null) {//To be discussed. 
                List<String> allFiles = new ArrayList<String>();
                if (options.localFilterNoGtyVCFFileNames != null) {
                    allFiles.addAll(Arrays.asList(options.localFilterNoGtyVCFFileNames));
                }
                if (options.localHardFilterNoGtyVCFFileNames != null) {
                    allFiles.addAll(Arrays.asList(options.localHardFilterNoGtyVCFFileNames));
                }
                for (String localFilterFileName : allFiles) {
                    File localFileFiler = new File(localFilterFileName + ".kggseq.filter.txt");
                    LOG.info("Prepare annotation resources from local VCF file(s) with no genotype ...");
//                    altLocalFilterFiles.add(localFileFiler);
                    // gtyCorrdiates used to keep the sequential order
                    uniqueGenome = vsParser.readVariantGtyFilterOnly(options.outputFileName, options.threadNum, null, options.inputFileName, options.seqQual, options.minMappingQuality, options.maxStandBias,
                            options.maxFisherStandBias, options.maxGtyAlleleNum, options.gtyQual, options.minGtySeqDP, options.maxAltAlleleFracRefHom, options.minAltAlleleFractHet,
                            options.minAltAlleleFractAltHom, options.vcfFilterLabelsIn, options.minOBS, options.sampleMafOver, options.sampleMafLess, options.considerSNV, options.considerIndel,
                            options.gtySecPL, options.gtyBestGP, options.needProgressionIndicator, needGty, false, options.needGtyQual, true, subjectList, allEffectIndivIDs, null, null, options.regionsInPos, options.regionsOutPos);

                    BufferedWriter annovarFilteredInFileWriter = null;
                    if (options.outGZ) {
                        GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(localFileFiler.getCanonicalPath() + ".gz"));
                        annovarFilteredInFileWriter = new BufferedWriter(new OutputStreamWriter(gzOut));
                    } else {
                        annovarFilteredInFileWriter = new BufferedWriter(new FileWriter(localFileFiler));
                    }
                    Chromosome[] chromosomes = uniqueGenome.getChromosomes();

                    for (int chromID = 0; chromID < chromosomes.length; chromID++) {
                        uniqueGenome.loadVariantFromDisk(chromID);
                        List<Variant> chromosomeVar = chromosomes[chromID].variantList;
                        if (chromosomeVar == null || chromosomeVar.isEmpty()) {
                            continue;
                        }
                        uniqueGenome.export2ANNOVARInput(annovarFilteredInFileWriter, chromID);
                        chromosomes[chromID].variantList.clear();
                    }
                    annovarFilteredInFileWriter.close();
                    uniqueGenome.removeTempFileFromDisk();
                    String infor = "The VCF filtration data " + localFilterFileName + " have been converted into a standard filtration dataset of kggseq, "
                            + localFileFiler.getCanonicalPath() + ".\nYou can directly use the standard one next time by \'--local-filter " + localFileFiler.getCanonicalPath()
                            + " \', which is faster!\n\n";
                    LOG.info(infor);
                    //  varFilter.markByANNOVARefFormat(uniqueGenome, localFileFiler.getCanonicalPath(), localFileFiler.getName(), options.needProgressionIndicator);
                }
            }

            if (options.inputFormat.endsWith("--vcf-file")) {
                // if pedigree file is specified options.indivPhenos will be
                // ignored
                if (options.pedFile != null) {
                    LinkageFileParser linkPedParser = new LinkageFileParser();
                    linkPedParser.readPedigreeOnly(options.pedFile, subjectList, options.useCompositeSubjectID, options.phenotypeColID);
                    caseSet = new HashSet<String>();
                    controlSet = new HashSet<String>();
                    unkownSet = new HashSet<String>();

                    for (Individual indiv : subjectList) {
                        if (indiv.getAffectedStatus() == 1) {
                            controlSet.add(indiv.getLabelInChip());
                        } else if (indiv.getAffectedStatus() == 2) {
                            caseSet.add(indiv.getLabelInChip());
                        } else {
                            unkownSet.add(indiv.getLabelInChip());
                        }
                    }
                } /*
                 * else if
                 * (options.genetModel.equals("--compound-heterozygosity")
                 * && options.pedFile == null) { throw new Exception(
                 * "Please sepcify the relationship of subjects  by \'--ped-file path/to/pedigree/file\' for compound heterozygosity model checking!"
                 * ); }
                 */ else if (options.indivPhenos != null) {
                    caseSet = new HashSet<String>();
                    controlSet = new HashSet<String>();
                    unkownSet = new HashSet<String>();
                    for (String indivID : options.indivPhenos) {
                        String label = indivID.substring(0, indivID.indexOf(':'));
                        String d = indivID.substring(indivID.indexOf(":") + 1);
                        Individual indiv = new Individual();
                        if (d.charAt(0) == '1') {
                            controlSet.add(label);
                        } else if (d.charAt(0) == '2') {
                            caseSet.add(label);
                        } else {
                            unkownSet.add(label);
                        }
                        indiv.setLabelInChip(label);
                        indiv.setFamilyID(label);
                        indiv.setIndividualID(label);
                        indiv.setDadID("0");
                        indiv.setMomID("0");
                        indiv.setAffectedStatus(d.charAt(0) - '0');
                        subjectList.add(indiv);
                    }
                }

                if (options.pedFile != null || options.indivPhenos != null) {
                    if (caseSet.isEmpty() && controlSet.isEmpty()) {
                        String infor = ("All subjects in phenotype or pedigree file have unknown disease status.\n"
                                + "    Please specify the clear disease status for AT LEAST one subject!!");
                        throw new Exception(infor);
                    }
                }

                if (options.mergeGtyDb != null && options.isPlinkPedOut || options.isPlinkBedOut || options.isBinaryGtyOut) {
                    needGty = true;
                } else if (options.sampleGtyHardFilterCode != null) {
                    for (int i = 0; i < 9; i++) {
                        if (options.sampleGtyHardFilterCode.contains(String.valueOf(i))) {
                            needGty = true;
                            break;
                        }
                    }
                } else if (options.ibsCheckCase >= 0 || options.homozygousRegionCase >= 0) {
                    needGty = true;
                } else if (options.doubleHitGenePhasedFilter || options.doubleHitGeneTriosFilter) {
                    needGty = true;
                } else if (options.skat) {
                    needGty = true;
                }

                boolean needReadsInfor = false;
                // options.sampleVarHardFilterCode
                if (options.sampleGtyHardFilterCode != null && (options.sampleGtyHardFilterCode.contains("7") || options.sampleGtyHardFilterCode.contains("8"))) {
                    needReadsInfor = true;
                }
                pedEncodeGytIDMap = new int[subjectList.size()];
                int sizeIndiv = subjectList.size();
                IntArrayList caeSetID1 = new IntArrayList();
                IntArrayList controlSetID1 = new IntArrayList();
                for (int i = 0; i < sizeIndiv; i++) {
                    if (subjectList.get(i).getAffectedStatus() == 2) {
                        caeSetID1.add(i);
                    } else if (subjectList.get(i).getAffectedStatus() == 1) {
                        controlSetID1.add(i);
                    }
                }
                if (!caeSetID1.isEmpty()) {
                    caeSetID = new int[caeSetID1.size()];
                    for (int i = 0; i < caeSetID.length; i++) {
                        caeSetID[i] = caeSetID1.getQuick(i);
                    }
                }

                if (!controlSetID1.isEmpty()) {
                    controlSetID = new int[controlSetID1.size()];
                    for (int i = 0; i < controlSetID.length; i++) {
                        controlSetID[i] = controlSetID1.getQuick(i);
                    }
                }

                // gtyCorrdiates used to keep the sequential order
                uniqueGenome = vsParser.readVariantGtyFilterOnly(options.outputFileName, options.threadNum, null, options.inputFileName, options.seqQual, options.minMappingQuality, options.maxStandBias,
                        options.maxFisherStandBias, options.maxGtyAlleleNum, options.gtyQual, options.minGtySeqDP, options.maxAltAlleleFracRefHom, options.minAltAlleleFractHet,
                        options.minAltAlleleFractAltHom, options.vcfFilterLabelsIn, options.minOBS, options.sampleMafOver, options.sampleMafLess, options.considerSNV, options.considerIndel,
                        options.gtySecPL, options.gtyBestGP, options.needProgressionIndicator, needGty, needReadsInfor, options.needGtyQual, false, subjectList, allEffectIndivIDs,
                        caeSetID, controlSetID, options.regionsInPos, options.regionsOutPos);
                pedEncodeGytIDMap = vsParser.getPedEncodeGytIDMap();
                /*
                 * if (!subjectMap.isEmpty()) { FileOutputStream objFOut =
                 * new FileOutputStream(outName + ".gty.obj");
                 * BufferedOutputStream objOBfs = new
                 * BufferedOutputStream(objFOut); ObjectOutputStream
                 * localObjOut = new ObjectOutputStream(objOBfs);
                 * 
                 * localObjOut.writeObject(subjectMap); localObjOut.flush();
                 * localObjOut.close(); objOBfs.flush(); objOBfs.close();
                 * objFOut.close(); }
                 */
            } else if (options.inputFormat.endsWith("--tmp-file")) {
                SimpleFormatParser pileParser = new SimpleFormatParser();
                uniqueGenome = pileParser.readCancerGenomeVariantFormat(options.outputFileName, options.inputFileName, options.needProgressionIndicator);
            } else if (options.inputFormat.endsWith("--no-gty-vcf-file")) {
                // gtyCorrdiates used to keep the sequential order
                uniqueGenome = vsParser.readVariantGtyFilterOnly(options.outputFileName, options.threadNum, null, options.inputFileName, options.seqQual, options.minMappingQuality, options.maxStandBias,
                        options.maxFisherStandBias, options.maxGtyAlleleNum, options.gtyQual, options.minGtySeqDP, options.maxAltAlleleFracRefHom, options.minAltAlleleFractHet,
                        options.minAltAlleleFractAltHom, options.vcfFilterLabelsIn, options.minOBS, options.sampleMafOver, options.sampleMafLess, options.considerSNV, options.considerIndel,
                        options.gtySecPL, options.gtyBestGP, options.needProgressionIndicator, needGty, false, options.needGtyQual, true, subjectList, allEffectIndivIDs, null, null, options.regionsInPos, options.regionsOutPos);
            } else if (options.inputFormat.endsWith("--ked-file")) {
                BinaryGtyProcessor bgp = new BinaryGtyProcessor(options.inputFileName);
                bgp.readPedigreeFile(subjectList);
                int[] counts = new int[3];
                Arrays.fill(counts, 0);
                uniqueGenome = bgp.readVariantsMapFile(counts);

                caseSet = new HashSet<String>();
                controlSet = new HashSet<String>();
                unkownSet = new HashSet<String>();

                for (Individual indiv : subjectList) {
                    if (indiv.getAffectedStatus() == 1) {
                        controlSet.add(indiv.getLabelInChip());
                    } else if (indiv.getAffectedStatus() == 2) {
                        caseSet.add(indiv.getLabelInChip());
                    }
                }
                if (options.mergeGtyDb != null && options.isPlinkPedOut || options.isPlinkBedOut || options.isBinaryGtyOut) {
                    needGty = true;
                } else if (options.sampleGtyHardFilterCode != null) {
                    for (int i = 0; i < 9; i++) {
                        if (options.sampleGtyHardFilterCode.contains(String.valueOf(i))) {
                            needGty = true;
                            break;
                        }
                    }
                } else if (options.ibsCheckCase >= 0 || options.homozygousRegionCase >= 0) {
                    needGty = true;
                } else if (options.doubleHitGenePhasedFilter || options.doubleHitGeneTriosFilter) {
                    needGty = true;
                }
                if (needGty) {
                    bgp.readBinaryGenotype(subjectList, uniqueGenome);
                }

                // subject to do something more
                String info = counts[0] + " variant-lines (" + counts[2] + " indels) are scanned; and " + counts[1] + " variants of " + subjectList.size() + " individual(s) are valid in " + options.inputFileName + ".";
                LOG.info(info);
                uniqueGenome.writeChromsomeToDiskClean();
            } else if (options.inputFormat.endsWith("--annovar-file")) {
                PileupFormatParser pileParser = new PileupFormatParser();
                uniqueGenome = pileParser.readVariantAnnovarFormat(options.inputFileName, options.needProgressionIndicator);
                uniqueGenome.writeChromsomeToDiskClean();
            }

            //-----------------------Annotate variants on each chromsome-------------------------------
            VariantAnnotator varAnnoter = new VariantAnnotator();
            VariantFilter varFilter = new VariantFilter();
            FiltrationSummarySet minMissingQCFilter1 = new FiltrationSummarySet("missingQC", uniqueGenome.getVariantFeatureNum());
            minMissingQCFilter1.initiateAMessage(0, "variants are ignored due to the number of heterozygous genotypes <" + options.minHetA
                    + " or that of alternative homozygous genotypes <" + options.minHomA + " in cases.");

            minMissingQCFilter1.initiateAMessage(0, "variants are ignored due to the number of heterozygous genotypes <" + options.minHetU
                    + " or that of alternative homozygous genotypes <" + options.minHomU + " in controls.");
            minMissingQCFilter1.initiateAMessage(0, "variants are ignored due to the number of non-null genotypes in cases <" + options.minOBSA + ".");
            minMissingQCFilter1.initiateAMessage(0, "variants are ignored due to the number of non-null genotypes in controls <" + options.minOBSU + ".");
            minMissingQCFilter1.initiateAMessage(0, "variant(s) are left after filtration according to minimal successful genotype calling rates in patients and healthy individuals.");

            String hardFilterModel = options.sampleGtyHardFilterCode;
            int filterNum = 6;
            boolean[] uniqueFilters = new boolean[2];
            boolean[] genotypeFilters = new boolean[filterNum];
            Arrays.fill(uniqueFilters, false);
            Arrays.fill(genotypeFilters, false);
            boolean filterByModel = false;
            FiltrationSummarySet inheritanceModelFilter2 = null;
            FiltrationSummarySet denovoModelFilter3 = null;
            FiltrationSummarySet doubleHitGeneModelFilter19 = null;
            FiltrationSummarySet doubleHitGeneModelFilter19d1 = null;
            FiltrationSummarySet somaticModelFilter4 = null;
            List<int[]> setSampleIDList = null;
            List<String> setSampleLabelList = new ArrayList<String>();
            String currentLine;

//            AnnotationSummarySet assLFF1=null;
//            if(altLocalFilterFiles!=null && !altLocalFilterFiles.isEmpty()){
//                assLFF1=new AnnotationSummarySet("LocalFileFilter",null,null,0,0,0,uniqueGenome.getVariantFeatureNum());               
//            }
            if (options.sampleGtyHardFilterCode != null && options.sampleGtyHardFilterCode.length() > 0) {
                for (int i = 0; i < filterNum; i++) {
                    String[] cells = options.sampleGtyHardFilterCode.split(",");
                    for (int t = 0; t < cells.length; t++) {
                        int s = Integer.parseInt(cells[t]) - 1;
                        if (s < filterNum) {
                            genotypeFilters[s] = true;
                            filterByModel = true;
                        }
                    }
                }
            }

            if (filterByModel) {
                inheritanceModelFilter2 = new FiltrationSummarySet("InheritanceModel", uniqueGenome.getVariantFeatureNum());
                inheritanceModelFilter2.initiateAMessage(0, "variants are ignored by genotype-based hard-filtering.");
                inheritanceModelFilter2.initiateAMessage(0, "variant(s) are left after filtration according to inheritance mode at genotypes, " + hardFilterModel + "!");
            }

            if (options.sampleGtyHardFilterCode != null && (options.sampleGtyHardFilterCode.contains("7"))) {
                denovoModelFilter3 = new FiltrationSummarySet("DenovoModel", uniqueGenome.getVariantFeatureNum());
                setSampleIDList = new ArrayList<int[]>();
                varFilter.matchTrioSet(subjectList, setSampleIDList);
                if (subjectList.isEmpty()) {
                    String infor = "No recognizable trios! To detect de novo mutation, you have to set the parents-child relationsby the tag --ped-file path/to/file!";
                    throw new Exception(infor);
                }
                uniqueGenome.addVariantFeatureLabel("DenovoMutationEvent");
                denovoModelFilter3.initiateAMessage(0, "variant(s) are left after filtration by denovo mutation!");
            }

            if (options.causingPredType == 2 || (options.sampleGtyHardFilterCode != null && (options.sampleGtyHardFilterCode.contains("8")))) {
                somaticModelFilter4 = new FiltrationSummarySet("SomaticModel", uniqueGenome.getVariantFeatureNum());
            }

            if (options.sampleGtyHardFilterCode != null && (options.sampleGtyHardFilterCode.contains("8"))) {
                // search somatic mutation between cancer tissue and non-cancer tissues
                if (options.indivPairs == null) {
                    String infor = "To detect somatic mutation, you have to set the tumor sample and non-tumor samples pairs by the tag --indiv-pair tumorIndivID1:normalIndivID1,tumorIndivID2:normalIndivID2";
                    throw new Exception(infor);
                }

                // search somatic mutation between cancer tissue and non-cancer tissues
                // match pairs                
                setSampleIDList = new ArrayList<int[]>();
                varFilter.matchTumorNontumorPair(options.indivPairs, subjectList, setSampleIDList, setSampleLabelList);
                if (setSampleIDList.isEmpty()) {
                    List<String> vcfInds = new ArrayList<String>();
                    List<String> pedIndivs = new ArrayList<String>();
                    for (String pair : options.indivPairs) {
                        String[] invdivs = pair.split(":");
                        vcfInds.add(invdivs[0]);
                        vcfInds.add(invdivs[1]);
                    }

                    for (int t = 0; t < subjectList.size(); t++) {
                        pedIndivs.add(subjectList.get(t).getLabelInChip());
                    }

                    String infor = "No recognizable matched Tumor<->Nontumor pairs!\n"
                            + ("The subject IDs in the specified VCF file(s), " + vcfInds.toString() + " are not indentical to those in the phenotype or pedigree file " + pedIndivs.toString() + "!!");
                    throw new Exception(infor);
                }
                somaticModelFilter4.initiateAMessage(0, "variant(s) are left after filtration by somatic mutations!");
                uniqueGenome.addVariantFeatureLabel("#SomaticAltAllele");
                uniqueGenome.addVariantFeatureLabel("TNTRefAltRead,P,OR");
            }

            AnnotationSummarySet assCCUMFV = null;
            if (options.sampleVarHardFilterCode != null) {
                if (options.sampleVarHardFilterCode.equals("case-unique")) {
                    uniqueFilters[0] = true;
                } else if (options.sampleVarHardFilterCode.equals("control-unique")) {
                    uniqueFilters[1] = true;
                }

                if (uniqueFilters[0] || uniqueFilters[1]) {
                    assCCUMFV = new AnnotationSummarySet("casecontrolUniqueModelFilterVar", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }
            }

            AnnotationSummarySet assSVHF = null;
            DoubleArrayList[] varPArray = null;
            if (options.varAssoc) {
//                List<DoubleArrayList> varPArray = varAnnoter.assocTestVar(uniqueGenome);               
                assSVHF = new AnnotationSummarySet("test filter", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("PValueAllelic");
                uniqueGenome.addVariantFeatureLabel("OddAllelic");
                uniqueGenome.addVariantFeatureLabel("PValueDom");
                uniqueGenome.addVariantFeatureLabel("OddDom");
                uniqueGenome.addVariantFeatureLabel("PValueRec");
                uniqueGenome.addVariantFeatureLabel("OddRec");
                uniqueGenome.addVariantFeatureLabel("PValueGeno");

                varPArray = new DoubleArrayList[4];
                for (int i = 0; i < varPArray.length; i++) {
                    varPArray[i] = new DoubleArrayList();
                }
            }

            AnnotationSummarySet[] varaintDBHardFilterFiles5 = null;
            AnnotationSummarySet[] varaintDBFilterFiles6 = null;

            int dbFileSize = options.varaintDBLableHardList != null ? options.varaintDBLableHardList.size() : 0;
            if (dbFileSize > 0) {
                varaintDBHardFilterFiles5 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String dbLabelName = options.varaintDBLableHardList.get(i);
                    varaintDBHardFilterFiles5[i] = new AnnotationSummarySet(dbLabelName, LocalFileFunc.getBufferedReader(GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get(dbLabelName)), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }
            }

            dbFileSize = options.varaintDBLableList != null ? options.varaintDBLableList.size() : 0;
            if (dbFileSize > 0) {
                varaintDBFilterFiles6 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String dbLabelName = options.varaintDBLableList.get(i);
                    varaintDBFilterFiles6[i] = new AnnotationSummarySet(dbLabelName, LocalFileFunc.getBufferedReader(GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get(dbLabelName)), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                    uniqueGenome.addVariantFeatureLabel("altFreq@" + dbLabelName);
                }
            }

            AnnotationSummarySet[] assLocalHardFilterFile5 = null;
            AnnotationSummarySet[] assLocalFilterFile6 = null;

            dbFileSize = options.localHardFilterFileNames != null ? options.localHardFilterFileNames.length : 0;
            if (dbFileSize > 0) {
                assLocalHardFilterFile5 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String strFileName = options.localHardFilterFileNames[i];
                    assLocalHardFilterFile5[i] = new AnnotationSummarySet(strFileName, LocalFileFunc.getBufferedReader(strFileName), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }
            }

            dbFileSize = options.localFilterFileNames != null ? options.localFilterFileNames.length : 0;
            if (dbFileSize > 0) {
                assLocalFilterFile6 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String strFileName = options.localFilterFileNames[i];
                    assLocalFilterFile6[i] = new AnnotationSummarySet(strFileName, LocalFileFunc.getBufferedReader(strFileName), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                    uniqueGenome.addVariantFeatureLabel("altFreq@" + new File(strFileName).getName());
                }
            }

            AnnotationSummarySet[] assLocalHardFilterVCFFile5 = null;
            AnnotationSummarySet[] assLocalFilterVCFFile6 = null;

            dbFileSize = options.localHardFilterVCFFileNames != null ? options.localHardFilterVCFFileNames.length : 0;
            if (dbFileSize > 0) {
                assLocalHardFilterVCFFile5 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String strFileName = options.localHardFilterVCFFileNames[i];
                    if (options.outGZ) {
                        strFileName += ".kggseq.filter.txt.gz";
                    } else {
                        strFileName += ".kggseq.filter.txt";
                    }
                    assLocalHardFilterVCFFile5[i] = new AnnotationSummarySet(strFileName, LocalFileFunc.getBufferedReader(strFileName), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }
            }

            dbFileSize = options.localFilterVCFFileNames != null ? options.localFilterVCFFileNames.length : 0;
            if (dbFileSize > 0) {
                assLocalFilterVCFFile6 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String strFileName = options.localFilterVCFFileNames[i];
                    if (options.outGZ) {
                        strFileName += ".kggseq.filter.txt.gz";
                    } else {
                        strFileName += ".kggseq.filter.txt";
                    }
                    assLocalFilterVCFFile6[i] = new AnnotationSummarySet(strFileName, LocalFileFunc.getBufferedReader(strFileName), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                    uniqueGenome.addVariantFeatureLabel("altFreq@" + new File(options.localFilterVCFFileNames[i]).getName());
                }
            }

            AnnotationSummarySet[] assLocalHardFilterNoGtyVCFFile5 = null;
            AnnotationSummarySet[] assLocalFilterNoGtyVCFFile6 = null;

            dbFileSize = options.localHardFilterNoGtyVCFFileNames != null ? options.localHardFilterNoGtyVCFFileNames.length : 0;
            if (dbFileSize > 0) {
                assLocalHardFilterNoGtyVCFFile5 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String strFileName = options.localHardFilterNoGtyVCFFileNames[i];
                    if (options.outGZ) {
                        strFileName += ".kggseq.filter.txt.gz";
                    } else {
                        strFileName += ".kggseq.filter.txt";
                    }
                    assLocalHardFilterNoGtyVCFFile5[i] = new AnnotationSummarySet(strFileName, LocalFileFunc.getBufferedReader(strFileName), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }
            }

            dbFileSize = options.localFilterNoGtyVCFFileNames != null ? options.localFilterNoGtyVCFFileNames.length : 0;
            if (dbFileSize > 0) {
                assLocalFilterNoGtyVCFFile6 = new AnnotationSummarySet[dbFileSize];
                for (int i = 0; i < dbFileSize; i++) {
                    String strFileName = options.localFilterNoGtyVCFFileNames[i];
                    if (options.outGZ) {
                        strFileName += ".kggseq.filter.txt.gz";
                    } else {
                        strFileName += ".kggseq.filter.txt";
                    }
                    assLocalFilterNoGtyVCFFile6[i] = new AnnotationSummarySet(strFileName, LocalFileFunc.getBufferedReader(strFileName), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                    uniqueGenome.addVariantFeatureLabel("altFreq@" + new File(options.localFilterNoGtyVCFFileNames[i]).getName());
                }
            }

            AnnotationSummarySet assFBAFEM = null;
            AnnotationSummarySet assFBAFIM = null;
            FloatArrayList mafRefList = null;
            if (options.toMAFPlotRef) {
                mafRefList = new FloatArrayList();
            }
            FloatArrayList mafSampleList = null;
            if (options.toMAFPlotSample) {
                mafSampleList = new FloatArrayList();
            }

            if (varaintDBFilterFiles6 != null || options.localFilterFileNames != null || options.localFilterVCFFileNames != null) {
                if (options.isAlleleFreqExcMode) {
                    assFBAFEM = new AnnotationSummarySet("filterByAlleleFreqExcModel", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                } else {
                    assFBAFIM = new AnnotationSummarySet("filterByAlleleFreqIncModel", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }
            }

            FiltrationSummarySet geneDBFilter7 = null;
            int[] variantsCounters = null;
            dbFileSize = options.geneDBLabels != null ? options.geneDBLabels.length : 0;
            ReferenceGenome[] referenceGenomes = null;
            int[] availableFeatureSizeForGeneDB = new int[dbFileSize];
            if (dbFileSize > 0) {
                geneDBFilter7 = new FiltrationSummarySet("Gene DB", uniqueGenome.getVariantFeatureNum());

                geneDBFilter7.initiateAMessage(0, " variant(s) are left after filtering by gene features.");
                variantsCounters = new int[VAR_FEATURE_NAMES.length];
                Arrays.fill(variantsCounters, 0);
                referenceGenomes = new ReferenceGenome[dbFileSize];
                File domainFile = new File(GlobalManager.RESOURCE_PATH + "/UniprotDomain.txt.gz");
                GeneRegionParser grp = new GeneRegionParser();
                for (int i = 0; i < dbFileSize; i++) {
                    String geneDBLabel = options.geneDBLabels[i];
                    String dbFileName = options.PUBDB_FILE_MAP.get(geneDBLabel);
                    File idMapFile = null;
                    if (geneDBLabel.equals("gencode")) {
                        idMapFile = new File(GlobalManager.RESOURCE_PATH + "/UniprotEnsemblMap.tab.gz");
                        referenceGenomes[i] = grp.readRefGeneSeq(GlobalManager.RESOURCE_PATH + "/" + dbFileName, geneDBLabel, options.splicingDis, options.neargeneDis, domainFile, idMapFile);
                        uniqueGenome.setGencodeAnnot(true);
                        availableFeatureSizeForGeneDB[i] = uniqueGenome.getVariantFeatureNum();
                        uniqueGenome.addVariantFeatureLabel("UniProtFeatureForGEncode");
                    } else if (geneDBLabel.equals("refgene")) {
                        uniqueGenome.setRefSeqAnnot(true);
                        availableFeatureSizeForGeneDB[i] = uniqueGenome.getVariantFeatureNum();
                        uniqueGenome.addVariantFeatureLabel("UniProtFeatureForRefGene");
                        idMapFile = new File(GlobalManager.RESOURCE_PATH + "/UniprotRefSeqMap.tab.gz");
                        // unforturately the ucsc file rna fasta file
                        // are not always consistant with the ucsc
                        // chromsome fasta
                        // refGenome =
                        // grp.readRefGeneSeqUcsc(GlobalManager.RESOURCE_PATH
                        // + "/" + dbFileName,
                        // GlobalManager.RESOURCE_PATH + "/" +
                        // "refMrna.fa.gz", geneDBLabel,
                        // options.splicingDis, options.neargeneDis,
                        // domainFile, idMapFile);
                        referenceGenomes[i] = grp.readRefGeneSeq(GlobalManager.RESOURCE_PATH + "/" + dbFileName, geneDBLabel, options.splicingDis, options.neargeneDis, domainFile, idMapFile);
                    } else if (geneDBLabel.equals("ensembl")) {
                        idMapFile = new File(GlobalManager.RESOURCE_PATH + "/UniprotEnsemblMap.tab.gz");
                        referenceGenomes[i] = grp.readRefGeneSeq(GlobalManager.RESOURCE_PATH + "/" + dbFileName, geneDBLabel, options.splicingDis, options.neargeneDis, domainFile, idMapFile);
                        uniqueGenome.setEnsemblAnnot(true);
                        availableFeatureSizeForGeneDB[i] = uniqueGenome.getVariantFeatureNum();
                        uniqueGenome.addVariantFeatureLabel("UniProtFeatureForEnsembl");
                    } else if (geneDBLabel.equals("knowngene")) {
                        idMapFile = new File(GlobalManager.RESOURCE_PATH + "/UniprotUCSCKnownGeneMap.tab.gz");
                        referenceGenomes[i] = grp.readRefGeneSeq(GlobalManager.RESOURCE_PATH + "/" + dbFileName, geneDBLabel, options.splicingDis, options.neargeneDis, domainFile, idMapFile);
                        uniqueGenome.setKnownAnnot(true);
                        availableFeatureSizeForGeneDB[i] = uniqueGenome.getVariantFeatureNum();
                        uniqueGenome.addVariantFeatureLabel("UniProtFeatureForKnownGene");
                    }
                    referenceGenomes[i].setName(geneDBLabel);
                }
            }
//to summarize 
            int somatNumIndex = -1;
            int readInfoIndex = -1;
            List<String> featureLabels = uniqueGenome.getVariantFeatureLabels();
            for (int i = 0; i < featureLabels.size(); i++) {
                if (featureLabels.get(i).equals("#SomaticAltAllele")) {
                    somatNumIndex = i;
                } else if (featureLabels.get(i).equals("TNTRefAltRead,P,OR")) {
                    readInfoIndex = i;
                }
                if (somatNumIndex >= 0 && readInfoIndex >= 0) {
                    break;
                }
            }
            // special consideration for pileup files
            if (somatNumIndex < 0) {
                for (int i = 0; i < featureLabels.size(); i++) {
                    if (featureLabels.get(i).equals("Comments")) {
                        somatNumIndex = i;
                        break;
                    }
                }
            }

            if (somatNumIndex >= 0) {
                uniqueGenome.addGeneFeatureLabel("#DependentVar");
                uniqueGenome.addGeneFeatureLabel("#IndependentVar");
                uniqueGenome.addGeneFeatureLabel("NonsynonymousReadsRatio");
                uniqueGenome.addGeneFeatureLabel("SynonymousReadsRatio");
                if (readInfoIndex > 0) {
                    uniqueGenome.addGeneFeatureLabel("SomatReadsInfor");
                }
            } else {
                uniqueGenome.addGeneFeatureLabel("#DependentVar");
                uniqueGenome.addGeneFeatureLabel("#IndependentVar");
            }

            FiltrationSummarySet dbNSFPAnnot8 = null;
            FiltrationSummarySet dbNSFPPred9 = null;

            //zhicheng noncoding
            FiltrationSummarySet dbNoncodePred9d1 = null;
            String resourcePathList[];
            boolean[] resourceTypeIsRegion;
            MyRandomForest[] myRandomForestList = null;
            Map genicMap = new HashMap<String, Integer>();
            String[] currentLineList = null;
            BufferedReader[] lineReaderList = null;
            Boolean[] isReigonList = null;
            double iniScore[] = null;
            int[] fixedPosition = null;
            int scoreIndexNum = 0;

            // end 
            // LiJun noncoding
            FiltrationSummarySet dbNoncodePred9d2 = null;
            BufferedReader[] lineReaderList9d2 = null;
            Bayes bayesPredictor = new Bayes(options.refGenomeVersion);

            // end
            List<CombOrders> combOrderList = new ArrayList<CombOrders>();
            CombOrders fixedComb = null;
            MyRandomForest myRandomForest = null;

            // dbNSFP3.0
            int[] dbNSFP3ScoreIndexes = new int[]{5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
            int[] dbNSFP3PredicIndex = new int[]{19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
            if (!options.scoreDBLableList.isEmpty()) {
                for (String dbLabelName : options.scoreDBLableList) {
                    if (dbLabelName.equals("noncoding") || dbLabelName.equals("dbncfp")) {
                    	LOG.info("cepip procsssing ...");
                        String comPath = options.refGenomeVersion + "/" + options.refGenomeVersion + "_funcnote";
                        if (options.causingPredType == 1) {
                            dbNoncodePred9d1 = new FiltrationSummarySet("noncode Random Forest", uniqueGenome.getVariantFeatureNum());
                            resourcePathList = new String[]{
                                GlobalManager.RESOURCE_PATH + comPath + "_encode_megamix.bed.gz.DNase-seq.cmp.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_encode_megamix.bed.gz.FAIRE-seq.cmp.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_encode_megamix.bed.gz.Histone.cmp.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_encode_megamix.bed.gz.Tfbs.cmp.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_all.footprints.bed.gz.cmp.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_GWAVA.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_CADD.CScore.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_DANN.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_fathmm-MKL.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_FunSeq.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_FunSeq2.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_GWAS3D.gz",
                                GlobalManager.RESOURCE_PATH + comPath + "_SuRFR.gz"
                            };
                            resourceTypeIsRegion = new boolean[]{true, true, true, true, true, false, false, false, false, false, false, false, false};
                            uniqueGenome.addVariantFeatureLabel("IsComplexDiseasePathogenic");
                            uniqueGenome.addVariantFeatureLabel("RandomForestScore");
                            String fileName = GlobalManager.RESOURCE_PATH + "hgmd_model.obj";
                            FileInputStream objFIn = new FileInputStream(fileName);
                            BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
                            ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
                            myRandomForestList = (MyRandomForest[]) localObjIn.readObject();
                            localObjIn.close();
                            objIBfs.close();
                            objFIn.close();
                            String genicRegion[] = new String[]{"5UTR", "3UTR", "intronic", "upstream", "downstream", "ncRNA", "intergenic"};
                            for (int i = 0; i < genicRegion.length; i++) {
                                genicMap.put(genicRegion[i], i);
                            }

                            LinkedList<String> tmpStringCurrentList = new LinkedList<String>();
                            LinkedList<BufferedReader> tmpLineReadersList = new LinkedList<BufferedReader>();
                            LinkedList<Boolean> isRegionResource = new LinkedList<Boolean>();

                            for (int i = 0; i < resourcePathList.length; i++) {
                                File rsFile = new File(resourcePathList[i]);
                                if (!rsFile.exists()) {
                                    LOG.warn(rsFile.getCanonicalPath() + " does not exist! Scores on this chromosome are ignored!");
                                    return;
                                } else {
                                    try {
                                        BufferedReader lineReader = LocalFileFunc.getBufferedReader(rsFile.getCanonicalPath());
                                        tmpLineReadersList.add(lineReader);
                                        isRegionResource.add(resourceTypeIsRegion[i]);
                                        tmpStringCurrentList.add(lineReader.readLine());
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                        e.printStackTrace();
                                    }
                                }
                            }

                            currentLineList = tmpStringCurrentList.toArray(new String[tmpStringCurrentList.size()]);
                            lineReaderList = tmpLineReadersList.toArray(new BufferedReader[tmpLineReadersList.size()]);
                            isReigonList = isRegionResource.toArray(new Boolean[isRegionResource.size()]);
                            String cells[] = null;
                            String[] currentChrom = new String[isReigonList.length];
                            int[] currentStartPos = new int[isReigonList.length];
                            int[] currentEndPos = new int[isReigonList.length];

                            fixedPosition = new int[isReigonList.length + 1];
                            fixedPosition[0] = 0;
                            List<String> scoreLabels = new ArrayList<String>();
                            for (int i = 0; i < currentLineList.length; i++) {
                                cells = Util.tokenize(currentLineList[i], '\t');
                                int startPosition = 2;
                                currentChrom[i] = cells[0];
                                currentStartPos[i] = Util.parseInt(cells[1]);
                                if (isReigonList[i]) {
                                    startPosition = 3;
                                    currentEndPos[i] = Util.parseInt(cells[2]);
                                }
                                for (int j = startPosition; j < cells.length; j++) {
                                    scoreLabels.add(cells[j]);
                                    scoreIndexNum++;
                                }
                                fixedPosition[i + 1] = cells.length - startPosition + fixedPosition[i];
                                currentLineList[i] = lineReaderList[i].readLine();
                            }
                            if (options.needVerboseNoncode) {
                                uniqueGenome.getVariantScoreLabels().addAll(scoreLabels);
                            } else {
                                int scoreNum = scoreLabels.size();
                                for (int i = scoreNum - 10; i < scoreNum; i++) {
                                    uniqueGenome.addVariantScoreLabel(scoreLabels.get(i));
                                }
                            }
                            scoreLabels.clear();
                            iniScore = new double[scoreIndexNum];

                            Arrays.fill(iniScore, Double.NaN);
                            for (int k = 0; k < fixedPosition.length - 1; k++) {
                                if (isReigonList[k]) {
                                    for (int k2 = fixedPosition[k]; k2 < fixedPosition[k + 1]; k2++) {
                                        iniScore[k2] = 0.0;
                                    }
                                }
                            }
                            dbNoncodePred9d1.initiateAMessage(0, "variants (in");
                            dbNoncodePred9d1.initiateAMessage(0, "genes) are predicted to be complex-disease-pathogenic;");
                            dbNoncodePred9d1.initiateAMessage(0, "variants are predicted to be non-pathogenic according to the Random forest prediction model");
                            dbNoncodePred9d1.initiateAMessage(0, "variant(s) are left after filtered by the complex disease prediction.");
                        } else if (options.causingPredType == 3) {
                            dbNoncodePred9d2 = new FiltrationSummarySet("noncode Bayes model", uniqueGenome.getVariantFeatureNum());
                            bayesPredictor.changeFeatureNum(options.dbncfpFeatureColumn);
                            bayesPredictor.changeCellLineName(options.cellLineName);
                            bayesPredictor.readResource();
                            LinkedList<BufferedReader> tmpLineReadersList = new LinkedList<BufferedReader>();
                            comPath = options.refGenomeVersion + "/" + options.refGenomeVersion + "_funcnote_";
                            for (int i = 0; i < options.dbncfpFeatureColumn.length; i++) {
                                File rsFile = new File(GlobalManager.RESOURCE_PATH + comPath + options.dbncfpFeatureColumn[i] + ".gz");
                                if (!rsFile.exists()) {
                                    LOG.error(options.dbncfpFeatureColumn[i] + " does not exist! Scores on this chromosome are ignored!");
                                } else {
                                    try {
                                        BufferedReader lineReader = LocalFileFunc.getBufferedReader(rsFile.getCanonicalPath());;
                                        tmpLineReadersList.add(lineReader);
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                        e.printStackTrace();
                                    }
                                }
                            }
                            for (int i = 0; i < options.dbncfpFeatureColumn.length; i++) {
                                uniqueGenome.addVariantFeatureLabel(options.dbncfpFeatureColumn[i]);
                            }
                            uniqueGenome.addVariantFeatureLabel("BF");
                            uniqueGenome.addVariantFeatureLabel("Composite_P");
                            uniqueGenome.addVariantFeatureLabel("Cell_P");
                            uniqueGenome.addVariantFeatureLabel("Combine_P");
                            lineReaderList9d2 = tmpLineReadersList.toArray(new BufferedReader[tmpLineReadersList.size()]);
                        }
                    }
                    if (dbLabelName.equals("dbnsfp")) {
                        String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                        String path = GlobalManager.RESOURCE_PATH + "/" + dbFileName + "Y.gz";
                        BufferedReader br = LocalFileFunc.getBufferedReader(path);
                        currentLine = br.readLine();
                        String[] cells = currentLine.split("\t");
                        br.close();

                        dbNSFPAnnot8 = new FiltrationSummarySet("dbNSFP", uniqueGenome.getVariantFeatureNum());
                        dbNSFPAnnot8.initiateAMessage(0, "coding nonsynonymous variants are assigned functional prediction scores.");

                        for (int t = 0; t < dbNSFP3ScoreIndexes.length; t++) {
                            uniqueGenome.getScoreLabels().add(cells[dbNSFP3ScoreIndexes[t]]);
                        }
                        for (int t = 0; t < dbNSFP3PredicIndex.length; t++) {
                            uniqueGenome.addVariantFeatureLabel(cells[dbNSFP3PredicIndex[t]]);
                        }
                        if (options.causingPredType == 0) {
                            // MendelFilter
                            String logitParamFile = GlobalManager.RESOURCE_PATH + "/mendelcausalrare" + options.dbsnfpVersion + ".param.gz";
                            br = LocalFileFunc.getBufferedReader(logitParamFile);
                            String line = null;

                            CombOrderComparator coc = new CombOrderComparator();
                            // String[] names = {"SLR_test_statistic", "SIFT_score",
                            // "Polyphen2_HDIV_score", "Polyphen2_HVAR_score", "LRT_score",
                            // "MutationTaster_score", "MutationAssessor_score", "FATHMM_score",
                            // "GERP++_NR", "GERP++_RS", "phyloP", "29way_logOdds"};
                            boolean isFixed = false;
                            if (!options.predictExplanatoryVar.startsWith("all") && !options.predictExplanatoryVar.startsWith("best")) {
                                isFixed = true;
                            }
                            float aucCutoff = 0.85f;
                            while ((line = br.readLine()) != null) {
                                if (line.trim().length() == 0) {
                                    continue;
                                }
                                cells = line.split("\t");
                                RegressionParams rp = new RegressionParams();

                                String[] values = cells[1].split(";");
                                rp.coef = new double[values.length];
                                for (int i = 0; i < rp.coef.length; i++) {
                                    rp.coef[i] = Double.parseDouble(values[i]);
                                }
                                rp.sampleCase2CtrRatio = Double.parseDouble(cells[2]);
                                rp.optimalCutoff = Double.parseDouble(cells[3].split(";")[0]);
                                rp.truePositiveRate = Double.parseDouble(cells[3].split(";")[1]);
                                rp.trueNegativeRate = Double.parseDouble(cells[3].split(";")[2]);

                                CombOrders co = new CombOrders(cells[0], Double.parseDouble(cells[4]), rp);
                                if (isFixed) {
                                    if (cells[0].equals(options.predictExplanatoryVar)) {
                                        fixedComb = co;
                                        break;
                                    }
                                } else {
                                    if (co.auc < aucCutoff) {
                                        continue;
                                    }
                                    combOrderList.add(co);
                                }

                            }

                            br.close();
                            Collections.sort(combOrderList, coc);
                            dbNSFPPred9 = new FiltrationSummarySet("dbNSFPMendelPred", uniqueGenome.getVariantFeatureNum());
                            uniqueGenome.addVariantFeatureLabel("DiseaseCausalProb_ExoVarTrainedModel");
                            uniqueGenome.addVariantFeatureLabel("IsRareDiseaseCausal_ExoVarTrainedModel");
                            uniqueGenome.addVariantFeatureLabel("BestCombinedTools:OptimalCutoff:TP:TN");

                            dbNSFPPred9.initiateAMessage(0, "variants (in");
                            dbNSFPPred9.initiateAMessage(0, "genes) are predicted to be disease-causal;");
                            dbNSFPPred9.initiateAMessage(0, "variants are predicted to be non-disease-causal according to the Logistic regression prediction model trained by ExoVar dataset (http://statgenpro.psychiatry.hku.hk/limx/kggseq/download/ExoVar.xls)");
                            dbNSFPPred9.initiateAMessage(0, "variant(s) are left after filtered by the disease mutation prediction.");
                        } else if (options.causingPredType == 2) {
                            File fileName = new File(GlobalManager.RESOURCE_PATH + "/CancerRandomForests" + options.dbsnfpVersion + ".obj");
                            if (!fileName.exists()) {
                                throw new Exception("Cannot find data in hard disk!");
                            }

                            // has some unsovled problem
                            /*
                             Kryo kryo = new Kryo();

                             kryo.setReferences(false);
                             kryo.setRegistrationRequired(false);
                             kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
                             //note: it seems the order of registered classes is very very important                           
                             kryo.register(MyRandomTree.class);
                             kryo.register(MyRandomTree[].class);
                             kryo.register(MyRandomForest.class);

                             Input input = new Input(new FileInputStream(fileName), 1024 * 1024);
                             myRandomForest = (MyRandomForest) kryo.readObject(input, MyRandomForest.class);
                             input.close();
                             */
                            FileInputStream objFIn = new FileInputStream(fileName);
                            BufferedInputStream objIBfs = new BufferedInputStream(objFIn);
                            ObjectInputStream localObjIn = new ObjectInputStream(objIBfs);
                            myRandomForest = (MyRandomForest) localObjIn.readObject();
                            localObjIn.close();
                            objIBfs.close();
                            objFIn.close();

                            dbNSFPPred9 = new FiltrationSummarySet("dbNSFPCancerPred", uniqueGenome.getVariantFeatureNum());
                            dbNSFPPred9.initiateAMessage(0, "variants (in");
                            dbNSFPPred9.initiateAMessage(0, "genes) are predicted to be cancer-driver;");
                            dbNSFPPred9.initiateAMessage(0, "variants are predicted to be non-cancer-driver according to a Random Forests prediction model trained by COSMIC dataset (http://cancer.sanger.ac.uk/cancergenome/projects/cosmic/).");
                            dbNSFPPred9.initiateAMessage(0, "variant(s) are left after filtered by the cancer-driver mutation prediction.");

                            uniqueGenome.addVariantFeatureLabel("IsCancerDriver_COSMICTrainedModel");
                            uniqueGenome.addVariantFeatureLabel("RandomForestScore");

                        }
                    }
                }
            }

            AnnotationSummarySet assGVF10 = null;
            if (options.geneVarFilter > 0) {
                assGVF10 = new AnnotationSummarySet("", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
            }

            AnnotationSummarySet assG11 = null;
            AnnotationSummarySet assV12 = null;
            AnnotationSummarySet assPPIG13 = null;
            AnnotationSummarySet assPPIV14 = null;
            AnnotationSummarySet assPWG15 = null;
            AnnotationSummarySet assPWV16 = null;
            PPIGraph ppiTree = null;
            Map<String, GeneSet> mappedPathes = null;
            if (options.candidateGeneSet != null && !options.candidateGeneSet.isEmpty()) {
                assG11 = new AnnotationSummarySet("IsCandidateGene", null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                uniqueGenome.addmRNAFeatureLabel("IsCandidateGene");

                assV12 = new AnnotationSummarySet("IsWithinCandidateGene", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("IsWithinCandidateGene");

                String ppiDBFile = null;
                String genesetDBFile = null;
                if (options.ppidb != null) {
                    ppiDBFile = GlobalManager.RESOURCE_PATH + "/PPI.txt.gz";

                    ppiTree = new PPIGraph(ppiDBFile);
                    ppiTree.readPPIItems();
                    assPPIG13 = new AnnotationSummarySet("PPI.txt.gz", null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                    uniqueGenome.addmRNAFeatureLabel("PPI");

                    assPPIV14 = new AnnotationSummarySet("PPI.txt.gz", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                    uniqueGenome.addVariantFeatureLabel("PPI");
                }

                if (options.genesetdb != null) {
                    if (options.genesetdb.equals("cano")) {
                        genesetDBFile = GlobalManager.RESOURCE_PATH + "/c2.cp.v3.1.symbols.gmt.gz";
                    } else if (options.genesetdb.equals("cura")) {
                        genesetDBFile = GlobalManager.RESOURCE_PATH + "/c2.all.v3.1.symbols.gmt.gz";
                    } else if (options.genesetdb.equals("onco")) {
                        genesetDBFile = GlobalManager.RESOURCE_PATH + "/c6.all.v3.1.symbols.gmt.gz";
                    } else if (options.genesetdb.equals("cmop")) {
                        genesetDBFile = GlobalManager.RESOURCE_PATH + "/c4.all.v3.1.symbols.gmt.gz";
                    } else if (options.genesetdb.equals("onto")) {
                        genesetDBFile = GlobalManager.RESOURCE_PATH + "/c5.all.v3.1.symbols.gmt.gz";
                    }

                    if (genesetDBFile != null) {
                        CandidateGeneExtender candiGeneExtender = new CandidateGeneExtender();
                        candiGeneExtender.setSeedGeneSet(options.candidateGeneSet);
                        candiGeneExtender.loadGeneSetDB(genesetDBFile, 2, 400);
                        mappedPathes = candiGeneExtender.pickRelevantGeneSet();

                        assPWG15 = new AnnotationSummarySet("SharedGeneSet", null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                        uniqueGenome.addmRNAFeatureLabel("SharedGeneSet");

                        assPWV16 = new AnnotationSummarySet("SharedGeneSet", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                        uniqueGenome.addVariantFeatureLabel("SharedGeneSet");
                    } else {
                        String info = "The geneset data set name " + options.genesetdb + " does not exist! So the geneset-based prioritization will be ignored!";
                        LOG.warn(info);
                    }
                }
            }

            AnnotationSummarySet assIBS17d1 = null;

            if (options.ibsCheckCase >= 0 && options.inputFormat.endsWith("--vcf-file")) {
                assIBS17d1 = new AnnotationSummarySet("ibs", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("LongestIBSRegion");
                uniqueGenome.addVariantFeatureLabel("LongestIBSRegionLength(bp)");
            }

            AnnotationSummarySet assHRC17d2 = null;

            if (options.homozygousRegionCase >= 0 && options.inputFormat.endsWith("--vcf-file")) {
                assHRC17d2 = new AnnotationSummarySet("homozygousRegionCase", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("LongestHomozygosityRegion");
                uniqueGenome.addVariantFeatureLabel("LongestHomozygosityRegionLength(bp)");
            }

            // as it is fast, i would prefer to read the lenght always. 
            Map<String, Double> geneLengths = null;
            AnnotationSummarySet assGIS17d3 = null;
            AnnotationSummarySet assGOS17d4 = null;
            if (options.geneDBLabels != null) {
                GeneRegionParser grp = new GeneRegionParser();
                // geneLengths =
                // grp.readRefGeneLength(GlobalManager.RESOURCE_PATH + "/" +
                // options.PUBDB_FILE_MAP.get(options.geneDBLabels[0]),
                // options.splicingDis);
                String[] pathes = new String[options.geneDBLabels.length];
                int ii = 0;
                for (String lbs : options.geneDBLabels) {
                    pathes[ii] = GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get(lbs);
                    ii++;
                }
                geneLengths = grp.readMergeRefGeneCodingLength(pathes, options.splicingDis, true);

                // only keep genes in inGeneSet
                if (!options.inGeneSet.isEmpty()) {
                    assGIS17d3 = new AnnotationSummarySet("", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }

                // only keep genes in outGeneSet
                if (!options.outGeneSet.isEmpty()) {
                    assGOS17d4 = new AnnotationSummarySet("", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                }
            }

            List<String[]> regionItems = null;
            AnnotationSummarySet assIBD17d5 = null;
            if (options.ibdFileName != null) {
                int[] indexes = new int[]{0, 1, 2};
                regionItems = new ArrayList<String[]>();
                File ibdFile = new File(options.ibdFileName);
                LocalFile.retrieveData(ibdFile.getCanonicalPath(), regionItems, indexes, "\t");

                assIBD17d5 = new AnnotationSummarySet(options.ibdFileName, null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("IBDRegion");
                uniqueGenome.addVariantFeatureLabel("IBDRegionAnnot");
            }

            AnnotationSummarySet dbScSNV18 = null;
            if (options.dbscSNVAnnote) {
                String dbLabelName = "dbscSNV";

                dbScSNV18 = new AnnotationSummarySet("dbscSNV", LocalFileFunc.getBufferedReader(GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get(dbLabelName)), new StringBuilder(), 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("ada_score@" + dbLabelName);
                //skip the head line
                dbScSNV18.getBr().readLine();
            }

            Map<String, String> genePubMedID = new HashMap<String, String>();
            String fileName = "HgncGene.txt";
            File resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + fileName);
            Map<String, String[]> geneNamesMap = varAnnoter.readGeneNames(resourceFile.getCanonicalPath());//time-consuming line. 
            List<String[]> hitDisCountsTriosGenes = null;
            List<String[]> hitDisCounTriosReads = null;

            List<int[]> triosIDList = null;
            IntArrayList effectiveIndivIDsTrios = null;
            Set<String> caseDoubleHitTriosGenes = null;
            Set<String> controlDoubleHitTriosGenes = null;
            int pathogenicPredicIndex = -1;

            if (options.doubleHitGeneTriosFilter) {
                hitDisCountsTriosGenes = new ArrayList<String[]>();
                hitDisCounTriosReads = new ArrayList<String[]>();
                doubleHitGeneModelFilter19 = new FiltrationSummarySet("DoubleHitModel", uniqueGenome.getVariantFeatureNum());
                doubleHitGeneModelFilter19.initiateAMessage(0, "variant(s) are left after filtered by the double-hit genes using parents' genotypes.");
                caseDoubleHitTriosGenes = new HashSet<String>();
                controlDoubleHitTriosGenes = new HashSet<String>();
                // cluster counts according to phentoypes
                int indivSize = subjectList.size();

                triosIDList = new ArrayList<int[]>();
                varFilter.matchTrioSet(subjectList, triosIDList);
                if (triosIDList.isEmpty()) {
                    String infor = "No recognizable trios for double-hit gene checking!";
                    LOG.error(infor);
                    return;
                }
                List<int[]> tmpTriosIDList = new ArrayList<int[]>();
                int trioSize = triosIDList.size();
                for (int t = 0; t < 3; t++) {
                    for (int j = 0; j < trioSize; j++) {
                        int[] trio = triosIDList.get(j);
                        Individual mIndiv = subjectList.get(trio[0]);
                        if (mIndiv.getAffectedStatus() == t) {
                            tmpTriosIDList.add(trio);
                        }
                    }
                }
                triosIDList.clear();
                triosIDList.addAll(tmpTriosIDList);
                tmpTriosIDList.clear();

//prepare the output format
                List<String> headActual = new ArrayList<String>();
                headActual.add("Gene");
                headActual.add("PubMed");
                headActual.add("ExonLen");
                headActual.add("CaseSyno");
                headActual.add("CaseNonSyno");
                int effectiveIndivSize = 0;

                if (options.countAllPsudoControl) {
                    headActual.add("PseudoControlSyno");
                    headActual.add("PseudoControlNonSyno");
                    headActual.add("PValue");
                }

                List<String> headPhenotype = new ArrayList<String>();
                headPhenotype.add("Disease");
                headPhenotype.add(".");
                headPhenotype.add(".");
                headPhenotype.add(".");
                headPhenotype.add(".");
                if (options.countAllPsudoControl) {
                    headPhenotype.add(".");
                    headPhenotype.add(".");
                    headPhenotype.add(".");
                }

                effectiveIndivIDsTrios = new IntArrayList();
                int setSize = triosIDList.size();
                for (int j = 0; j < setSize; j++) {
                    Individual mIndiv = subjectList.get(triosIDList.get(j)[0]);
                    if (triosIDList.get(j)[0] < 0 || triosIDList.get(j)[1] < 0 || triosIDList.get(j)[2] < 0) {
                        continue;
                    }

                    effectiveIndivIDsTrios.add(j);
                    headActual.add(mIndiv.getLabelInChip());
                    headPhenotype.add(String.valueOf(mIndiv.getAffectedStatus()));
                    effectiveIndivSize++;
                }
                hitDisCountsTriosGenes.add(headActual.toArray(new String[0]));
                hitDisCountsTriosGenes.add(headPhenotype.toArray(new String[0]));
                hitDisCounTriosReads.add(headActual.toArray(new String[0]));
                hitDisCounTriosReads.add(headPhenotype.toArray(new String[0]));

                if (effectiveIndivSize == 0) {
                    String infor = "No valid trios for double-hit gene checking!";
                    LOG.warn(infor);
                    return;
                }
            }

            List<String[]> doubleHitGenePhasedGenes = null;
            List<String[]> doubleHitGenePhasedReads = null;
            Set<String> caseDoubleHitPhasedGenes = null;
            Set<String> controlDoubleHitPhasedGenes = null;
            if (options.doubleHitGenePhasedFilter) {
                if (caeSetID == null && controlSetID == null) {
                    throw new Exception("'--double-hit-gene-phased-filter' requires case/control phenotype inormation.");
                }
                if (!uniqueGenome.isIsPhasedGty()) {
                    throw new Exception("'--double-hit-gene-phased-filter' does not work for unphased genotyps.");
                }
                doubleHitGeneModelFilter19d1 = new FiltrationSummarySet("DoubleHitModel", uniqueGenome.getVariantFeatureNum());
                doubleHitGeneModelFilter19d1.initiateAMessage(0, "variant(s) are left after filtered by the double-hit genes using phased genotypes.");
                caseDoubleHitPhasedGenes = new HashSet<String>();
                controlDoubleHitPhasedGenes = new HashSet<String>();

                for (int i = 0; i < featureLabels.size(); i++) {
                    if (featureLabels.get(i).equals("IsCancerDriver_COSMICTrainedModel") || featureLabels.get(i).equals("IsRareDiseaseCausal_ExoVarTrainedModel") || featureLabels.get(i).equals("IsComplexDiseasePathogenic")) {
                        pathogenicPredicIndex = i;
                    }
                    if (pathogenicPredicIndex >= 0) {
                        break;
                    }
                }

                doubleHitGenePhasedGenes = new ArrayList<String[]>();
                doubleHitGenePhasedReads = new ArrayList<String[]>();
                List<String> heads1 = new ArrayList<String>();
                heads1.add("Gene");
                heads1.add("PubMed");
                heads1.add("P");
                heads1.add("CountCtl");
                heads1.add("ProbCase");
                heads1.add("CountCase");
                List<String> heads2 = new ArrayList<String>();
                heads2.add("Disease");
                heads2.add(".");
                heads2.add(".");
                heads2.add(".");
                heads2.add(".");
                heads2.add(".");
                int effectiveIndivSize = 0;

                int caseNum = caeSetID == null ? 0 : caeSetID.length;
                int controlNum = controlSetID == null ? 0 : controlSetID.length;

                for (int j = 0; j < controlNum; j++) {
                    int index = controlSetID[j];
                    Individual mIndiv = subjectList.get(index);
                    heads1.add(mIndiv.getLabelInChip());
                    heads2.add(String.valueOf(mIndiv.getAffectedStatus()));
                    effectiveIndivSize++;

                }

                for (int j = 0; j < caseNum; j++) {
                    int index = caeSetID[j];
                    Individual mIndiv = subjectList.get(index);
                    heads1.add(mIndiv.getLabelInChip());
                    heads2.add(String.valueOf(mIndiv.getAffectedStatus()));
                    effectiveIndivSize++;
                }
                if (effectiveIndivSize == 0) {
                    String infor = "No valid genotype information for double-hit gene checking!";
                    LOG.warn(infor);

                }

                doubleHitGenePhasedGenes.add(heads1.toArray(new String[0]));
                doubleHitGenePhasedGenes.add(heads2.toArray(new String[0]));
                doubleHitGenePhasedReads.add(heads1.toArray(new String[0]));
                doubleHitGenePhasedReads.add(heads2.toArray(new String[0]));
            }

            AnnotationSummarySet assGene20 = null;
            AnnotationSummarySet assVariant20 = null;
            if (options.needAnnotateGene) {
                String fileNameHg = "HgncGene.txt";
                //File resourceFileHg = new File(GlobalManager.RESOURCE_PATH + "/" + fileNameHg);
                assGene20 = new AnnotationSummarySet(fileNameHg, null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                uniqueGenome.addmRNAFeatureLabel("GeneDescription");
                uniqueGenome.addmRNAFeatureLabel("Pseudogenes");

                assVariant20 = new AnnotationSummarySet(fileNameHg, null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("GeneDescription");
                uniqueGenome.addVariantFeatureLabel("Pseudogenes");
            }

            AnnotationSummarySet assOmimGene21 = null;
            AnnotationSummarySet assOmimVar21 = null;
            if (options.omimAnnotateGene) {
                //File resourceFile = new File(GlobalManager.RESOURCE_PATH + "/morbidmap.gz");
                assOmimGene21 = new AnnotationSummarySet("morbidmap.gz", null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                uniqueGenome.addmRNAFeatureLabel("DiseaseName(s)MIMid");
                uniqueGenome.addmRNAFeatureLabel("GeneMIMid");

                assOmimVar21 = new AnnotationSummarySet("morbidmap.gz", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("DiseaseName(s)MIMid");
                uniqueGenome.addVariantFeatureLabel("GeneMIMid");
            }

            AnnotationSummarySet assSDA22 = null;
            AnnotationSummarySet assSDF22 = null;
            CNVRegionParser grpSD = null;
            ReferenceGenome refGenomeSD = null;
            if (options.superdupAnnotate) {
                grpSD = new CNVRegionParser();
                refGenomeSD = grpSD.readSuperDupRegions(GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get("superdup"));
                assSDA22 = new AnnotationSummarySet("superdup", null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                uniqueGenome.addmRNAFeatureLabel("SuperDupKValue");
            } else if (options.superdupFilter) {
                grpSD = new CNVRegionParser();
                refGenomeSD = grpSD.readSuperDupRegions(GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get("superdup"));
                assSDF22 = new AnnotationSummarySet("superdup", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
            }

            AnnotationSummarySet assDGV23 = null;
            CNVRegionParser grpDGV = null;
            ReferenceGenome refGenomeDGV = null;
            if (options.dgvcnvAnnotate) {
                grpDGV = new CNVRegionParser();
                refGenomeDGV = grpDGV.readRefCNVSeq(GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get("dgvcnv"));
                assDGV23 = new AnnotationSummarySet("dgvcnv", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("DGVIDs");
                uniqueGenome.addVariantFeatureLabel("CNVSampleSize");
                uniqueGenome.addVariantFeatureLabel("LossCNV");
                uniqueGenome.addVariantFeatureLabel("GainCNV");
            }

            AnnotationSummarySet assOLGF = null;

            IntArrayList caseSubIDs = new IntArrayList();
            IntArrayList controlSubIDs = new IntArrayList();
            if (options.overlappedGeneFilter) {
                assOLGF = new AnnotationSummarySet("overlappedGeneFilter", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                featureLabels = uniqueGenome.getVariantFeatureLabels();
                for (int i = 0; i < featureLabels.size(); i++) {
                    if (featureLabels.get(i).equals("IsCancerDriver_COSMICTrainedModel") || featureLabels.get(i).equals("IsRareDiseaseCausal_ExoVarTrainedModel") || featureLabels.get(i).equals("IsComplexDiseasePathogenic")) {
                        pathogenicPredicIndex = i;
                    }
                    if (pathogenicPredicIndex >= 0) {
                        break;
                    }
                }

                int indivSize = subjectList.size();
                for (int j = 0; j < indivSize; j++) {
                    Individual mIndiv = subjectList.get(j);
                    if (mIndiv.getAffectedStatus() == 2) {
                        caseSubIDs.add(j);
                    } else if (mIndiv.getAffectedStatus() == 1) {
                        controlSubIDs.add(j);
                    }
                }
            }

            AnnotationSummarySet pubmedSearch24_Gene_Ideo = null;
            AnnotationSummarySet pubmedSearch24_Gene = null;
            AnnotationSummarySet pubmedSearch24_Var_Ideo = null;
            AnnotationSummarySet pubmedSearch24_Var = null;
            List<String[]> ideogramItemsGene = new ArrayList<String[]>();
            List<String[]> ideogramItemsVar = new ArrayList<String[]>();

            int driverPredicIndex = -1;
            if (options.searchList != null && !options.searchList.isEmpty()) {
                if (options.pubmedMiningIdeo) {
                    pubmedSearch24_Gene_Ideo = new AnnotationSummarySet("pubmedMiningIdeoGene", null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                    uniqueGenome.addmRNAFeatureLabel("PubMedIDIdeogram");

                    String ideoFileName = GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get("ideogram");
                    File ideoFile = new File(ideoFileName);
                    if (!ideoFile.exists()) {
                        LOG.error(ideoFile.getCanonicalPath() + " does not exist!!");
                        return;//Or ignore this ?
                    }

                    if (options.refGenomeVersion.equals("hg18")) {
                        int[] indexes = new int[]{0, 1, 2, 5, 6};
                        LocalFile.retrieveData(ideoFile.getCanonicalPath(), ideogramItemsGene, indexes, "\t");
                        List<String[]> ideogramItemsTmp = new ArrayList<String[]>();
                        ideogramItemsTmp.addAll(ideogramItemsGene);
                        ideogramItemsGene.clear();
                        for (String[] item : ideogramItemsTmp) {
                            String[] newItem = new String[4];
                            newItem[0] = item[0];
                            newItem[1] = item[1] + item[2];
                            newItem[2] = item[3];
                            newItem[3] = item[4];
                            ideogramItemsGene.add(newItem);
                        }
                    } else if (options.refGenomeVersion.equals("hg19")) {
                        int[] indexes = new int[]{0, 3, 1, 2};
                        LocalFile.retrieveData(ideoFile.getCanonicalPath(), ideogramItemsGene, indexes, "\t");
                        List<String[]> ideogramItemsTmp = new ArrayList<String[]>();
                        ideogramItemsTmp.addAll(ideogramItemsGene);
                        ideogramItemsGene.clear();
                        for (String[] item : ideogramItemsTmp) {
                            String[] newItem = new String[4];
                            if (item[0].length() > 3) {
                                newItem[0] = item[0].substring(3);//This may be wrong if the chromosome doesn't have chr start. 
                            } else {
                                newItem[0] = item[0];
                            }
                            newItem[1] = item[1];
                            newItem[2] = item[2];
                            newItem[3] = item[3];
                            ideogramItemsGene.add(newItem);
                        }
                    }
                }
                if (options.pubmedMiningGene) {
                    pubmedSearch24_Gene = new AnnotationSummarySet("pubmedMiningGene", null, null, 0, 0, 0, uniqueGenome.getmRNAFeatureNum());
                    uniqueGenome.addmRNAFeatureLabel("PubMedIDGene");
                }

                if (options.pubmedMiningIdeo) {
                    pubmedSearch24_Var_Ideo = new AnnotationSummarySet("pubmedMiningIdeoVar", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                    uniqueGenome.addVariantFeatureLabel("PubMedIDIdeogram");

                    String ideoFileName = GlobalManager.RESOURCE_PATH + "/" + options.PUBDB_FILE_MAP.get("ideogram");
                    File ideoFile = new File(ideoFileName);
                    if (!ideoFile.exists()) {
                        LOG.error(ideoFile.getCanonicalPath() + " does not exist!!");
                        return;//or ignore?
                    }

                    for (int i = 0; i < featureLabels.size(); i++) {
                        if (featureLabels.get(i).equals("IsCancerDriver_COSMICTrainedModel") || featureLabels.get(i).equals("IsRareDiseaseCausal_ExoVarTrainedModel") || featureLabels.get(i).equals("IsComplexDiseasePathogenic")) {
                            pathogenicPredicIndex = i;
                        }

                        if (driverPredicIndex >= 0) {
                            break;
                        }
                    }

                    if (options.refGenomeVersion.equals("hg18") || options.refGenomeVersion.equals("hg19")) {
                        int[] indexes = new int[]{0, 1, 2, 5, 6};
                        LocalFile.retrieveData(ideoFile.getCanonicalPath(), ideogramItemsVar, indexes, "\t");
                        List<String[]> ideogramItemsTmp = new ArrayList<String[]>();
                        ideogramItemsTmp.addAll(ideogramItemsVar);
                        ideogramItemsVar.clear();
                        for (String[] item : ideogramItemsTmp) {
                            String[] newItem = new String[4];
                            newItem[0] = item[0];
                            newItem[1] = item[1] + item[2];
                            newItem[2] = item[3];
                            newItem[3] = item[4];
                            ideogramItemsVar.add(newItem);
                        }
                    }
                }

                if (options.pubmedMiningGene) {
                    pubmedSearch24_Var = new AnnotationSummarySet("pubmedMiningVar", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                    uniqueGenome.addVariantFeatureLabel("PubMedIDGene");

                    for (int i = 0; i < featureLabels.size(); i++) {
                        if (featureLabels.get(i).equals("IsCancerDriver_COSMICTrainedModel") || featureLabels.get(i).equals("IsRareDiseaseCausal_ExoVarTrainedModel") || featureLabels.get(i).equals("IsComplexDiseasePathogenic")) {
                            pathogenicPredicIndex = i;
                        }
                        if (driverPredicIndex >= 0) {
                            break;
                        }
                    }
                }
            }

            AnnotationSummarySet assRSID25 = null;
            OpenIntIntHashMap[] altMapID = null;
            if (options.rsid) {
                assRSID25 = new AnnotationSummarySet("RSID", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                File resourceFileRSID = new File(GlobalManager.RESOURCE_PATH + "/" + options.refGenomeVersion + "/b142_SNPChrPosOnRef_GRCh19p105.bcp.gz");
                altMapID = new OpenIntIntHashMap[STAND_CHROM_NAMES.length];
                for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                    altMapID[i] = new OpenIntIntHashMap();
                }
                varAnnoter.readChrPosRs(altMapID, resourceFileRSID);
            }

            AnnotationSummarySet assFS = null;
            SequenceRetriever seqRe = null;
            if (options.flankingSequence > 0) {
                assFS = new AnnotationSummarySet("FlankingSequence", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("FlankingSeq" + options.flankingSequence + "bp");
                seqRe = new SequenceRetriever();

                Chromosome[] chroms = uniqueGenome.getChromosomes();
                List<String[]> downloadList = new ArrayList<String[]>();
                for (int i = 0; i < chroms.length; i++) {
                    if (chroms[i] == null) {
                        continue;
                    }

                    File fastAFile = new File(GlobalManager.RESOURCE_PATH + "/" + options.refGenomeVersion + "/chr" + STAND_CHROM_NAMES[i] + ".fa.gz");

                    if (!fastAFile.exists()) {
                        String[] fileURL = new String[2];
                        fileURL[0] = fastAFile.getCanonicalPath();
                        fileURL[1] = "http://hgdownload.cse.ucsc.edu/goldenPath/" + options.refGenomeVersion + "/chromosomes/chr" + STAND_CHROM_NAMES[i] + ".fa.gz";
                        downloadList.add(fileURL);
                    }
                }
                if (!downloadList.isEmpty()) {
                    NetUtils.downloadFiles(downloadList);
                }
            }

            AnnotationSummarySet assPhenolyzer26 = null;
            Phenolyzer phenolyzer = null;
            HashMap<String, String> hmpPhenolyzer = null;
            if (options.phenolyzer) {
                assPhenolyzer26 = new AnnotationSummarySet("Phenolyzer", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                uniqueGenome.addVariantFeatureLabel("Phenolyzer_Score");
                phenolyzer = new Phenolyzer(options.searchList, options.outputFileName + "phenolyzer/out");
                phenolyzer.downloadPhenolyzer(new File(PLUGIN_PATH + "phenolyzer-master.zip"));
                phenolyzer.runPhenolyzer();
                phenolyzer.parseResult();
                hmpPhenolyzer = phenolyzer.getHashMap();
            }

            SKAT skat = null;
            DoubleArrayList[] genePVList = null;
            if (options.skat) {
                if (subjectList == null | subjectList.isEmpty()) {
                    String info = "No case and control information! Failed in SKAT.";
                    LOG.info(info);
                    options.skat = false;
                } else {
                    skat = new SKAT(options.outputFileName, options.skatCutoff);
//                    int intParallel = options.threadNum >= 10 ? 10 : options.threadNum;
                    skat.startRServe(options.threadNum);
                    int intPhe = -1;
                    if (options.phenotypeColID != null && options.phenotypeColID.containsKey(options.pheItem)) {
                        intPhe = options.phenotypeColID.get(options.pheItem);
                    }
                    double dblPhe[] = skat.getPhenotype(subjectList, options.skatBinary, intPhe);
                    double dblCov[][] = null;
                    if (options.cov) {
                        dblCov = skat.getCovarite(subjectList, options.phenotypeColID, options.covItem);
                    }
                    skat.setPhenotype(dblPhe, dblCov, options.permutePheno);
//                    if (skat.isBoolBinary()) {
                    genePVList = new DoubleArrayList[3];
//                    } else {
//                        genePVList = new DoubleArrayList[2];
//                    }
                    for (int i = 0; i < genePVList.length; i++) {
                        genePVList[i] = new DoubleArrayList();
                    }
                }
            }

            AnnotationSummarySet assPatho27 = null;
            HashMap<String, String[]> hmpPatho = null;
            int intPatho = 0;
            if (options.mendelGenePatho) {
                hmpPatho = new HashMap<String, String[]>();
                assPatho27 = new AnnotationSummarySet("Pathology Gene Pediction:", null, null, 0, 0, 0, uniqueGenome.getVariantFeatureNum());
                String geneMendelPredFilePath = options.PUBDB_FILE_MAP.get("mendelgene");

                BufferedReader br = LocalFileFunc.getBufferedReader(GlobalManager.RESOURCE_PATH + "/" + geneMendelPredFilePath);
                String strLine = br.readLine();
                String[] title = strLine.split("\t");
                intPatho = 9;
                for (int i = 1; i < intPatho; i++) {
                    uniqueGenome.addVariantFeatureLabel(title[i]);
                }

                while ((strLine = br.readLine()) != null) {
                    String[] items = strLine.split("\t");
                    String[] values = new String[intPatho - 1];
                    System.arraycopy(items, 1, values, 0, values.length);
                    hmpPatho.put(items[0], values);
                }
            }

            File annovarFilteredInFile = null;
            BufferedWriter annovarFilteredInFileWriter = null;

            if (options.isANNOVAROut) {
                annovarFilteredInFile = new File(options.outputFileName + ".flt.annovar");
                if (options.outGZ) {
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(annovarFilteredInFile.getCanonicalPath() + ".gz"));
                    annovarFilteredInFileWriter = new BufferedWriter(new OutputStreamWriter(gzOut));
                } else {
                    annovarFilteredInFileWriter = new BufferedWriter(new FileWriter(annovarFilteredInFile));
                }
            }
            BufferedWriter bwGeneVarGroupFile = null;
            if (options.isGeneVarGroupFileOut) {
                if (options.outGZ) {
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".gene.grp.gz"));
                    bwGeneVarGroupFile = new BufferedWriter(new OutputStreamWriter(gzOut));
                } else {
                    bwGeneVarGroupFile = new BufferedWriter(new FileWriter(options.outputFileName + ".gene.grp"));
                }
            }

            BufferedWriter bwMapBed = null;
            int[] savedBinnaryBedVar = new int[2];
            savedBinnaryBedVar[0] = 0;
            savedBinnaryBedVar[1] = 0;
            //RandomAccessFile rafBed = null;
            //FileChannel fileBedStream = null;
            BufferedOutputStream fileBedStream = null;
            WritableByteChannel wbcFileBed = null;

            if (options.isPlinkBedOut) {
                BufferedWriter bwPed = null;
                if (options.outGZ) {
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".bim" + ".gz"));
                    bwMapBed = new BufferedWriter(new OutputStreamWriter(gzOut));
                    GZIPOutputStream gzOut2 = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".fam" + ".gz"));
                    bwPed = new BufferedWriter(new OutputStreamWriter(gzOut2));
                } else {
                    bwMapBed = new BufferedWriter(new FileWriter(options.outputFileName + ".bim"));
                    bwPed = new BufferedWriter(new FileWriter(options.outputFileName + ".fam"));
                }
                //bwMapBed = new BufferedWriter(new FileWriter(options.outputFileName + ".bim"));

                //BufferedWriter bwPed = new BufferedWriter(new FileWriter(options.outputFileName + ".fam"));
                for (Individual indiv : subjectList) {
                    if (indiv == null) {
                        continue;
                    }
                    savedBinnaryBedVar[1]++;
                    bwPed.write(indiv.getFamilyID() + " " + indiv.getIndividualID() + " " + indiv.getDadID()
                            + " " + indiv.getMomID() + " " + indiv.getGender() + " " + indiv.getAffectedStatus());
                    bwPed.write("\n");
                }
                bwPed.close();

                File bedFile = new File(options.outputFileName + ".bed");
                if (bedFile.exists()) {
                    bedFile.delete();
                }
                if (options.outGZ) {
                    wbcFileBed = Channels.newChannel(new FileOutputStream(bedFile.getCanonicalPath() + ".gz"));
                    fileBedStream = new BufferedOutputStream(new GZIPOutputStream(Channels.newOutputStream(wbcFileBed)));
                    //bwFileChannelBed=new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Channels.newOutputStream(wbcFileBed))));
                } else {
                    wbcFileBed = Channels.newChannel(new FileOutputStream(bedFile));
                    fileBedStream = new BufferedOutputStream(Channels.newOutputStream(wbcFileBed));
                }
                //|-magic number-
                //01101100 00011011
                byte byteInfo = (byte) 0x6C;
                fileBedStream.write(byteInfo);
                byteInfo = (byte) 0x1B;
                fileBedStream.write(byteInfo);
                //|-mode-| 00000001 (SNP-major)
                //00000001 
                byteInfo = 1;
                fileBedStream.write(byteInfo);
            }

            BufferedWriter bwMapPed = null;
            int[] savedPedVar = new int[2];
            savedPedVar[0] = 0;
            savedPedVar[1] = 0;
            if (options.isPlinkPedOut) {
                BufferedWriter bwPed = null;
                if (options.outGZ) {
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".map" + ".gz"));
                    bwMapPed = new BufferedWriter(new OutputStreamWriter(gzOut));
                    GZIPOutputStream gzOut2 = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".ped.p" + ".gz"));
                    bwPed = new BufferedWriter(new OutputStreamWriter(gzOut2));
                } else {
                    bwMapPed = new BufferedWriter(new FileWriter(options.outputFileName + ".map"));
                    bwPed = new BufferedWriter(new FileWriter(options.outputFileName + ".ped.p"));
                }
                //bwPed = new BufferedWriter(new FileWriter(options.outputFileName + ".ped.p"));
                for (Individual indiv : subjectList) {
                    if (indiv == null) {
                        continue;
                    }
                    savedPedVar[1]++;
                    bwPed.write(indiv.getFamilyID() + " " + indiv.getIndividualID() + " " + indiv.getDadID()
                            + " " + indiv.getMomID() + " " + indiv.getGender() + " " + indiv.getAffectedStatus());
                    bwPed.write("\n");
                }
                bwPed.close();
            }

            BufferedOutputStream filelKedStream = null;
            WritableByteChannel rafKed = null;
            BufferedWriter bwMapKed = null;
            int[] savedBinnaryKedVar = new int[2];
            savedBinnaryKedVar[0] = 0;

            if (options.isBinaryGtyOut) {
                File kedFile = null;
                if (options.outGZ) {
                    kedFile = new File(options.outputFileName + ".ked.gz");
                    rafKed = Channels.newChannel(new FileOutputStream(kedFile));
                    filelKedStream = new BufferedOutputStream(new GZIPOutputStream(Channels.newOutputStream(rafKed)));
                    //fileChannelKed = new BufferedOutputStream((Channels.newOutputStream(rafKed)));
                    //   filelKedStream = new BufferedOutputStream(new FileOutputStream(bedMergedFile), 10000);

                } else {
                    kedFile = new File(options.outputFileName + ".ked");
                    rafKed = Channels.newChannel(new FileOutputStream(kedFile));
                    filelKedStream = new BufferedOutputStream((Channels.newOutputStream(rafKed)));
                }

                /*
                 ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                 //|-magic number-
                 //10011110 10000010 
                 byte byteInfo = (byte) 0x9E;
                 byteBuffer.put(byteInfo);
                 byteInfo = (byte) 0x82;
                 byteBuffer.put(byteInfo);
                 //|-mode-| Mode of 00000000 indicates unphased genotypes while that of 00000001 indicates the phased genotypes
                 //00000000 
                 if (uniqueGenome.isIsPhasedGty()) {
                 byteInfo = 1;
                 } else {
                 byteInfo = 0;
                 }
                 byteBuffer.put(byteInfo);
                 byteInfo = (byte) 0x82;
                 byteBuffer.put(byteInfo);
                 byteBuffer.put(byteInfo);
                 byteBuffer.putInt(8);
                 byteBuffer.put(byteInfo);
                 byteBuffer.flip();
                 filelKedStream.write(byteBuffer.array());
                 byteBuffer.clear();
                 */
                filelKedStream.write((byte) 0x9E);
                filelKedStream.write((byte) 0x82);
                if (uniqueGenome.isIsPhasedGty()) {
                    filelKedStream.write((byte) 0x1);
                } else {
                    filelKedStream.write((byte) 0x0);
                }

//pedigree 
                BufferedWriter bwPed = null;
                if (options.outGZ) {
                    GZIPOutputStream gzOut2 = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".fam" + ".gz"));
                    bwPed = new BufferedWriter(new OutputStreamWriter(gzOut2));
                } else {
                    bwPed = new BufferedWriter(new FileWriter(options.outputFileName + ".fam"));
                }
                for (Individual indiv : subjectList) {
                    if (indiv == null) {
                        continue;
                    }
                    savedBinnaryKedVar[1]++;
                    bwPed.write(indiv.getFamilyID() + " " + indiv.getIndividualID() + " " + indiv.getDadID()
                            + " " + indiv.getMomID() + " " + indiv.getGender() + " " + indiv.getAffectedStatus());
                    bwPed.write("\n");
                }
                bwPed.close();

                //bwMapKed = new BufferedWriter(new FileWriter(options.outputFileName + ".kim"));//For isBinaryGtyOut. 
                if (options.outGZ) {
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".kim" + ".gz"));
                    bwMapKed = new BufferedWriter(new OutputStreamWriter(gzOut));
                } else {
                    bwMapKed = new BufferedWriter(new FileWriter(options.outputFileName + ".kim"));
                }
            }

            File vcfFilteredInFile = null;
            BufferedWriter vcfFilteredInFileWriter = null;
            if (options.isVCFOut) {
                if (options.outGZ) {
                    vcfFilteredInFile = new File(options.outputFileName + ".flt.vcf.gz");
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(vcfFilteredInFile.getCanonicalPath()));
                    vcfFilteredInFileWriter = new BufferedWriter(new OutputStreamWriter(gzOut));
                } else {
                    vcfFilteredInFile = new File(options.outputFileName + ".flt.vcf");
                    vcfFilteredInFileWriter = new BufferedWriter(new FileWriter(vcfFilteredInFile));
                }
                //vcfFilteredInFileWriter = new BufferedWriter(new FileWriter(vcfFilteredInFile));
                vcfFilteredInFileWriter.write(vsParser.getVcfHead().toString());
                vcfFilteredInFileWriter.write("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT");
                for (Individual indiv : subjectList) {
                    if (!indiv.isHasGenotypes()) {
                        continue;
                    }
                    vcfFilteredInFileWriter.write("\t");
                    vcfFilteredInFileWriter.write(indiv.getLabelInChip());
                }
                vcfFilteredInFileWriter.write("\n");
            }

            boolean needWriteTmp = false;

            BufferedWriter tmpWriter = null;
            if (needWriteTmp) {
                tmpWriter = new BufferedWriter(new FileWriter(options.outputFileName + ".tmp.maf"));
                tmpWriter.write("chr	pos	ref_allele	newbase	classification	count\n");
            }

            BufferedWriter finalExportWriter = null;
            if (options.excelOut) {
                if (options.outGZ) {
                    finalFilteredInFile = new File(options.outputFileName + ".flt.xls.gz");
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(finalFilteredInFile.getCanonicalPath()));
                    finalExportWriter = new BufferedWriter(new OutputStreamWriter(gzOut));
                } else {
                    finalFilteredInFile = new File(options.outputFileName + ".flt.xls");
                    finalExportWriter = new BufferedWriter(new FileWriter(finalFilteredInFile));
                }
            } else {
                if (options.outGZ) {
                    finalFilteredInFile = new File(options.outputFileName + ".flt.txt.gz");
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(finalFilteredInFile.getCanonicalPath()));
                    finalExportWriter = new BufferedWriter(new OutputStreamWriter(gzOut));
                } else {
                    finalFilteredInFile = new File(options.outputFileName + ".flt.txt");
                    finalExportWriter = new BufferedWriter(new FileWriter(finalFilteredInFile));
                }
            }

            Genome refhapGenome = null;
            List<Individual> refIndivList = null;
            BufferedWriter bwMap = null;
            BufferedOutputStream mergedFileStream = null;
            WritableByteChannel mergedFileChannel = null;
            int[] coutVar = new int[1];
            int[] intsSPV = new int[2];
            int[] intsIndiv = new int[1];

            if (options.mergeGtyDb != null && (options.isPlinkPedOut || options.isPlinkBedOut)) {
                refIndivList = new ArrayList<Individual>();
                String vcfFilePath = GlobalManager.RESOURCE_PATH + "/" + options.mergeGtyDb + ".chr_CHROM_.vcf.gz";
                vsParser.extractSubjectIDsVCFInFile(vcfFilePath.replaceAll("_CHROM_", "1"), refIndivList);

                refhapGenome = vsParser.readVariantGtyFilterOnly(null, options.threadNum, null, vcfFilePath, options.seqQual, options.minMappingQuality, options.maxStandBias,
                        options.maxFisherStandBias, options.maxGtyAlleleNum, options.gtyQual, options.minGtySeqDP, options.maxAltAlleleFracRefHom, options.minAltAlleleFractHet,
                        options.minAltAlleleFractAltHom, options.vcfFilterLabelsIn, options.minOBS, options.sampleMafOver, options.sampleMafLess, options.considerSNV, options.considerIndel,
                        options.gtySecPL, options.gtyBestGP, options.needProgressionIndicator, false, false, false, false, null, null, null, null, null, null);

                if (!subjectList.isEmpty() && !refIndivList.isEmpty()) {
                    if (options.isPlinkBedOut) {
                        coutVar[0] = 0;
                        intsIndiv[0] = 0;

                        if (options.outGZ) {
                            GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".merged.bim" + ".gz"));
                            bwMap = new BufferedWriter(new OutputStreamWriter(gzOut));
                        } else {
                            bwMap = new BufferedWriter(new FileWriter(options.outputFileName + ".merged.bim"));
                        }

                        File bedMergedFile = new File(options.outputFileName + ".merged.bed");
                        if (bedMergedFile.exists()) {
                            bedMergedFile.delete();
                        }

                        if (options.outGZ) {
                            mergedFileChannel = Channels.newChannel(new FileOutputStream(bedMergedFile.getCanonicalPath() + ".gz"));
                            mergedFileStream = new BufferedOutputStream(new GZIPOutputStream(Channels.newOutputStream(wbcFileBed)));
                            //bwFileChannelBed=new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Channels.newOutputStream(wbcFileBed))));
                        } else {
                            mergedFileChannel = Channels.newChannel(new FileOutputStream(bedMergedFile));
                            mergedFileStream = new BufferedOutputStream(Channels.newOutputStream(wbcFileBed));
                        }
                        uniqueGenome.exportPlinkBinaryGtyFam(subjectList, refIndivList, options.outputFileName, intsIndiv, options.outGZ);
                    }

                    if (options.isPlinkPedOut) {
                        intsSPV[0] = 0;
                        intsSPV[1] = 0;
                        if (options.outGZ) {
                            GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".merged.map" + ".gz"));
                            bwMap = new BufferedWriter(new OutputStreamWriter(gzOut));
                        } else {
                            bwMap = new BufferedWriter(new FileWriter(options.outputFileName + ".merged.map"));
                        }
                    }
                }
            }

//*********************************************************************************************************************************************************************//
//-------------------------------------------------------------------Start to filter or annotate 
            Chromosome[] chromosomes = uniqueGenome.getChromosomes();

            int leftVar = -1;
            boolean needHead = true;
            List<Variant> chromosomeVarAll = new ArrayList<Variant>();
            int finalVarNum = 0;

            for (int chromID = 0; chromID < chromosomes.length; chromID++) {
                uniqueGenome.loadVariantFromDisk(chromID);//time-consuming part. 
                List<Variant> chromosomeVar = chromosomes[chromID].variantList;
                if (chromosomeVar == null || chromosomeVar.isEmpty()) {
                    continue;
                }

                chromosomeVarAll.addAll(chromosomeVar);

                //More variant QC
                varFilter.sumFilterCaseControlVar(chromosomes[chromID], options.minHetA, options.minHomA, options.minHetU, options.minHomU, options.minOBSA, options.minOBSU, minMissingQCFilter1, mafSampleList);
                if (chromosomeVar.isEmpty()) {
                    //   LOG.info("0 sequence variant(s) are left finally!");
                }

                if (inheritanceModelFilter2 != null) {
                    varFilter.inheritanceModelFilterVar(chromosomes[chromID], uniqueGenome.isIsPhasedGty(), subjectList, pedEncodeGytIDMap, caeSetID, controlSetID, genotypeFilters, hardFilterModel, inheritanceModelFilter2);
                }
                if (denovoModelFilter3 != null) {
                    varFilter.devnoMutationFilterVar(chromosomes[chromID], uniqueGenome.isIsPhasedGty(), subjectList, pedEncodeGytIDMap, controlSetID, setSampleIDList, options.noHomo, denovoModelFilter3);
                }

                if (needGty && options.sampleGtyHardFilterCode != null && (options.sampleGtyHardFilterCode.contains("8"))) {
                    varFilter.somaticMutationFilterVar(chromosomes[chromID], uniqueGenome.isIsPhasedGty(), subjectList, pedEncodeGytIDMap, controlSetID, setSampleIDList, setSampleLabelList, somaticModelFilter4, options.somatReadsP);
                }

                if (options.sampleVarHardFilterCode != null) {
                    if (uniqueFilters[0] || uniqueFilters[1]) {
                        varAnnoter.casecontrolUniqueModelFilterVar(chromosomes[chromID], assCCUMFV, uniqueFilters);
                    }
                }

                if (options.varAssoc) {
//                    varFilter.filterBy4ModelPValue(chromosomes[chromID], assSVHF, thrholds, uniqueGenome);
                    varAnnoter.assocTestVar(chromosomes[chromID], assSVHF, varPArray, uniqueGenome);
                }

                if (varaintDBHardFilterFiles5 != null) {
                    for (int i = 0; i < varaintDBHardFilterFiles5.length; i++) {
                        leftVar = varAnnoter.hardFilterByANNOVARefFormat(chromosomes[chromID], chromID, varaintDBHardFilterFiles5[i], options.needProgressionIndicator);
                        if (leftVar == 0) {
                            break;
                        }
                        chromosomes[chromID].buildVariantIndexMap();
                    }
                }

                if (varaintDBFilterFiles6 != null) {
                    for (int i = 0; i < varaintDBFilterFiles6.length; i++) {
                        varAnnoter.markByANNOVARefFormat(chromosomes[chromID], chromID, varaintDBFilterFiles6[i], options.needProgressionIndicator);
                    }
                }

                if (assLocalHardFilterFile5 != null) {
                    for (int i = 0; i < assLocalHardFilterFile5.length; i++) {
                        leftVar = varAnnoter.hardFilterByANNOVARefFormat(chromosomes[chromID], chromID, assLocalHardFilterFile5[i], options.needProgressionIndicator);
                        if (leftVar == 0) {
                            break;
                        }
                        chromosomes[chromID].buildVariantIndexMap();
                    }
                }

                if (assLocalFilterFile6 != null) {
                    for (int i = 0; i < assLocalFilterFile6.length; i++) {
                        varAnnoter.markByANNOVARefFormat(chromosomes[chromID], chromID, assLocalFilterFile6[i], options.needProgressionIndicator);
                    }
                }

                if (assLocalHardFilterVCFFile5 != null) {
                    for (int i = 0; i < assLocalHardFilterVCFFile5.length; i++) {
                        leftVar = varAnnoter.hardFilterByANNOVARefFormat(chromosomes[chromID], chromID, assLocalHardFilterVCFFile5[i], options.needProgressionIndicator);
                        if (leftVar == 0) {
                            break;
                        }
                        chromosomes[chromID].buildVariantIndexMap();
                    }
                }

                if (assLocalFilterVCFFile6 != null) {
                    for (int i = 0; i < assLocalFilterVCFFile6.length; i++) {
                        varAnnoter.markByANNOVARefFormat(chromosomes[chromID], chromID, assLocalFilterVCFFile6[i], options.needProgressionIndicator);
                    }
                }

                if (assLocalHardFilterNoGtyVCFFile5 != null) {
                    for (int i = 0; i < assLocalHardFilterNoGtyVCFFile5.length; i++) {
                        leftVar = varAnnoter.hardFilterByANNOVARefFormat(chromosomes[chromID], chromID, assLocalHardFilterNoGtyVCFFile5[i], options.needProgressionIndicator);
                        if (leftVar == 0) {
                            break;
                        }
                        chromosomes[chromID].buildVariantIndexMap();
                    }
                }

                if (assLocalFilterNoGtyVCFFile6 != null) {
                    for (int i = 0; i < assLocalFilterNoGtyVCFFile6.length; i++) {
                        varAnnoter.markByANNOVARefFormat(chromosomes[chromID], chromID, assLocalFilterNoGtyVCFFile6[i], options.needProgressionIndicator);
                    }
                }

                if (varaintDBFilterFiles6 != null || options.localFilterFileNames != null || options.localFilterVCFFileNames != null) {
                    if (options.isAlleleFreqExcMode) {
                        varFilter.filterByAlleleFreqExcModel(chromosomes[chromID], assFBAFEM, options.minAlleleFreqExc, mafRefList);
                    } else {
                        varFilter.filterByAlleleFreqIncModel(chromosomes[chromID], assFBAFIM, options.minAlleleFreqInc, options.maxAlleleFreqInc, mafRefList);
                    }
                }

                if (referenceGenomes != null) {
                    for (int i = 0; i < referenceGenomes.length; i++) {
                        varAnnoter.geneFeatureAnnot(chromosomes[chromID], chromID, referenceGenomes[i], options.geneFeatureIn, availableFeatureSizeForGeneDB[i], options.threadNum);
                        // varAnnoter.geneFeatureAnnot(chromosomes[chromID], chromID, referenceGenomes[i], options.geneFeatureIn, availableFeatureSizeForGeneDB[i]);
                    }

                    //this should not be filttered by gene-feature
                    if (somatNumIndex >= 0) {
                        varAnnoter.summarizeSomatNSVarPerGene(chromosomes[chromID], somatNumIndex, readInfoIndex, options.dependentGeneFeature, options.independentGeneFeature);
                    } else {
                        varAnnoter.summarizeAltNSVarPerGene(chromosomes[chromID], options.dependentGeneFeature, options.independentGeneFeature);
                    }
                    varFilter.geneFeatureFilter(chromosomes[chromID], variantsCounters, options.geneFeatureIn, geneDBFilter7);
                }

                if (dbNSFPAnnot8 != null) {
                    for (String dbLabelName : options.scoreDBLableList) {
                        if (dbLabelName.equals("dbnsfp")) {
                            String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                            varAnnoter.readExonicScoreNSFPNucleotideMerge(chromosomes[chromID], GlobalManager.RESOURCE_PATH + "/" + dbFileName, options.refGenomeVersion,
                                    dbNSFP3ScoreIndexes, dbNSFP3PredicIndex, dbNSFPAnnot8, options.needProgressionIndicator);
                        }

                        if (options.causingPredType == 0) {
//riskPredictionRareDiseaseAll(Chromosome chromosome, List<CombOrders> combOrderList, boolean filterNonDisMut, List<String> names, FiltrationSummarySet dbNSFPPred9)
                            if (options.predictExplanatoryVar.startsWith("all")) {
                                varAnnoter.riskPredictionRareDiseaseAll(chromosomes[chromID], combOrderList, options.filterNonDiseaseMut, uniqueGenome.getScoreLabels(), options.threadNum, dbNSFPPred9);
                            } else if (options.predictExplanatoryVar.startsWith("best")) {
                                varAnnoter.riskPredictionRareDiseaseBest(chromosomes[chromID], combOrderList, options.filterNonDiseaseMut, uniqueGenome.getScoreLabels(), dbNSFPPred9);
                            } else {
                                varAnnoter.riskPredictionRareDiseaseFixParam(chromosomes[chromID], fixedComb, options.filterNonDiseaseMut, uniqueGenome.getScoreLabels(), dbNSFPPred9);
                            }
                        } else if (options.causingPredType == 2) {
                            //Chromosome chromosome, MyRandomForest myRandomForest, boolean filterNonDisMut, FiltrationSummarySet dbNSFPPred9
                            varAnnoter.riskPredictionRandomForest(chromosomes[chromID], myRandomForest, options.threadNum, options.filterNonDiseaseMut, dbNSFPPred9);
                        }
                    }
                }

                if (dbNoncodePred9d1 != null) {
                    //varAnnoter.noncodingRandomForest(chromosomes[chromID], needProgressionIndicator, filterNonDisMut, needThreadNumber);
                    currentLineList = varAnnoter.noncodingRandomForest(chromosomes[chromID], options.needProgressionIndicator, options.filterNonDiseaseMut,
                            options.threadNum, isReigonList, iniScore, currentLineList, fixedPosition,
                            lineReaderList, scoreIndexNum, myRandomForestList, genicMap,
                            options.needVerboseNoncode, dbNoncodePred9d1.getAvailableFeatureIndex(), chromID, dbNoncodePred9d1);
                }
                if (dbNoncodePred9d2 != null) {
                    // noncode 4 represents 4 calcualting parameters (BF,Composite_P,Cell_P,Combine_P)
                    varAnnoter.noncodingCellTypeSpecificPrediction(chromosomes[chromID], options.needProgressionIndicator, lineReaderList9d2,
                            bayesPredictor, chromID, dbNoncodePred9d2.getAvailableFeatureIndex(),
                            dbNoncodePred9d2.getAvailableFeatureIndex() + options.dbncfpFeatureColumn.length + 4);
                }
                if (options.geneVarFilter > 0) {
                    varFilter.filterByGeneVarNum(chromosomes[chromID], assGVF10, options.geneVarFilter, uniqueGenome.getVariantFeatureLabels(), uniqueGenome.isIsPhasedGty());
                }

                if (options.candidateGeneSet != null && !options.candidateGeneSet.isEmpty()) {
                    if (uniqueGenome.getGeneNum() > 0) {
                        varAnnoter.canidateGeneExploreGene(chromosomes[chromID], assG11, options.candidateGeneSet);
                    } else if (uniqueGenome.getVarNum() > 0) {
                        varAnnoter.canidateGeneExploreVar(chromosomes[chromID], assV12, options.candidateGeneSet);
                    }

                    if (options.ppidb != null) {
                        if (uniqueGenome.getGeneNum() > 0) {
                            varAnnoter.canidateGenePPIExploreGene(chromosomes[chromID], assPPIG13, options.candidateGeneSet, ppiTree, options.ppiDepth);
                        } else if (uniqueGenome.getVarNum() > 0) {
                            varAnnoter.canidateGenePPIExploreVar(chromosomes[chromID], assPPIV14, options.candidateGeneSet, ppiTree, options.ppiDepth);
                        }
                    }

                    if (mappedPathes != null) {
                        if (uniqueGenome.getGeneNum() > 0) {
                            varAnnoter.canidateGenePathwayExploreGene(chromosomes[chromID], assPWG15, options.candidateGeneSet, mappedPathes);
                        } else if (uniqueGenome.getVarNum() > 0) {
                            varAnnoter.canidateGenePathwayExploreVar(chromosomes[chromID], assPWV16, options.candidateGeneSet, mappedPathes);
                        }
                    }
                }

                if (options.ibsCheckCase >= 0 && options.inputFormat.endsWith("--vcf-file")) {
                    varAnnoter.exploreLongIBSRegion(chromosomes[chromID], assIBS17d1, chromosomeVarAll, options.ibsCheckCase * 1000, uniqueGenome.isIsPhasedGty(), subjectList, pedEncodeGytIDMap, caeSetID);
                }

                if (options.homozygousRegionCase >= 0 && options.inputFormat.endsWith("--vcf-file")) {
                    varAnnoter.exploreLongHomozygosityRegion(chromosomes[chromID], assHRC17d2, chromosomeVarAll, options.homozygousRegionCase * 1000, uniqueGenome.isIsPhasedGty(), subjectList, pedEncodeGytIDMap, caeSetID, controlSetID);
                }

                if (options.geneDBLabels != null) {
                    if (!options.inGeneSet.isEmpty()) {
                        varFilter.keepGenesInSet(chromosomes[chromID], assGIS17d3, options.inGeneSet);
                    }

                    // only keep genes in outGeneSet
                    if (!options.outGeneSet.isEmpty()) {
                        varFilter.keepGenesOutSet(chromosomes[chromID], assGOS17d4, options.outGeneSet);
                    }
                }

                if (options.ibdFileName != null) {
                    varAnnoter.ibdRegionExplore(chromosomes[chromID], assIBD17d5, regionItems);
                }

                if (dbScSNV18 != null) {
                    varAnnoter.dbscSNV(chromosomes[chromID], dbScSNV18, options.needProgressionIndicator);
                }

                // System.out.println(chromID);
                if (doubleHitGeneModelFilter19 != null) {
                    varFilter.doubleHitGeneExploreVarTriosSudoControl(chromosomes[chromID], uniqueGenome.isIsPhasedGty(), subjectList, pedEncodeGytIDMap, triosIDList, effectiveIndivIDsTrios, options.searchList, geneNamesMap, genePubMedID,
                            options.noHomo, hitDisCountsTriosGenes, hitDisCounTriosReads, caseDoubleHitTriosGenes, controlDoubleHitTriosGenes, options.countAllPsudoControl, doubleHitGeneModelFilter19);
                }
                if (doubleHitGeneModelFilter19d1 != null) {
                    varFilter.doubleHitGeneExploreVarPhasedGty(chromosomes[chromID], pedEncodeGytIDMap, caeSetID, controlSetID, options.searchList, geneNamesMap, genePubMedID,
                            options.noHomo, pathogenicPredicIndex, doubleHitGenePhasedGenes, doubleHitGenePhasedReads, caseDoubleHitPhasedGenes, controlDoubleHitPhasedGenes, doubleHitGeneModelFilter19d1);
                }

                if (options.needAnnotateGene) {
                    String fileNameHg = "HgncGene.txt";
                    File resourceFileHg = new File(GlobalManager.RESOURCE_PATH + "/" + fileNameHg);
                    if (uniqueGenome.getGeneNum() > 0) {
                        varAnnoter.pseudogeneAnnotationGene(chromosomes[chromID], assGene20, resourceFileHg.getCanonicalPath());
                    } else if (uniqueGenome.getVarNum() > 0) {
                        varAnnoter.pseudogeneAnnotationVar(chromosomes[chromID], assVariant20, resourceFileHg.getCanonicalPath());
                    }
                }

                if (options.omimAnnotateGene) {
                    File resourceFileOmim = new File(GlobalManager.RESOURCE_PATH + "/morbidmap.gz");
                    if (uniqueGenome.getGeneNum() > 0) {
                        varAnnoter.omimGeneAnnotationGene(chromosomes[chromID], assOmimGene21, resourceFileOmim.getCanonicalPath());
                    } else if (uniqueGenome.getVarNum() > 0) {
                        varAnnoter.omimGeneAnnotationVar(chromosomes[chromID], assOmimVar21, resourceFileOmim.getCanonicalPath());
                    }
                }

                if (options.superdupAnnotate) {
                    varAnnoter.superDupAnnotation(chromosomes[chromID], assSDA22, refGenomeSD);
                } else if (options.superdupFilter) {
                    varFilter.superDupFilter(chromosomes[chromID], assSDF22, refGenomeSD);
                    if (uniqueGenome == null || uniqueGenome.getVarNum() == 0) {
                        LOG.info("0 sequence variant(s) are left finally!");
                        continue;
                    }
                }

                if (options.dgvcnvAnnotate) {
                    varAnnoter.cnvAnnotation(chromosomes[chromID], assDGV23, refGenomeDGV);
                }

                if (options.overlappedGeneFilter) {
                    varAnnoter.overlappedGeneExploreVar(chromosomes[chromID], assOLGF, subjectList, pedEncodeGytIDMap, true, caseSubIDs, controlSubIDs, pathogenicPredicIndex, uniqueGenome);
                }

                if (options.searchList != null && !options.searchList.isEmpty()) {
                    if (uniqueGenome.getGeneNum() > 0) {
                        if (options.pubmedMiningIdeo) {
                            varAnnoter.pubMedIDIdeogramExploreGene(chromosomes[chromID], pubmedSearch24_Gene_Ideo, options.searchList, ideogramItemsGene);
                        }
                        if (options.pubmedMiningGene) {
                            varAnnoter.pubMedIDGeneExploreGene(chromosomes[chromID], pubmedSearch24_Gene, options.searchList, geneNamesMap, genePubMedID);
                        }
                    } else if (uniqueGenome.getVarNum() > 0) {
                        if (options.pubmedMiningIdeo) {
                            varAnnoter.pubMedIDIdeogramExploreVar(chromosomes[chromID], pubmedSearch24_Var_Ideo, options.searchList, ideogramItemsVar, options.refGenomeVersion, true, driverPredicIndex);
                        }
                        if (options.pubmedMiningGene) {
                            varAnnoter.pubMedIDGeneExploreVar(chromosomes[chromID], pubmedSearch24_Var, options.searchList, true, geneNamesMap, genePubMedID, driverPredicIndex);
                        }
                    }
                }

                if (options.rsid) {
                    varAnnoter.addRSID(chromosomes[chromID], assRSID25, altMapID[chromID]);

                }

                if (options.flankingSequence > 0) {
                    seqRe.addFlankingSequences(chromosomes[chromID], assFS, options.flankingSequence, options.refGenomeVersion);
                }
                if (options.phenolyzer) {
                    phenolyzer.addScore(chromosomes[chromID], assPhenolyzer26, hmpPhenolyzer);
                }

                if (options.skat) {
                    skat.getGtyAndRun(chromosomes[chromID], subjectList, pedEncodeGytIDMap, uniqueGenome.isIsPhasedGty(), options.threadNum, genePVList);
                }

                if (options.mendelGenePatho) {
                    varAnnoter.addPatho(chromosomes[chromID], assPatho27, hmpPatho, intPatho);
                }

                if (chromosomeVar.isEmpty()) {
                    continue;
                }
                if (!subjectList.isEmpty()) {
                    if (options.isPlinkBedOut) {
                        uniqueGenome.exportPlinkBinaryGty(chromosomes[chromID], subjectList, pedEncodeGytIDMap, fileBedStream, bwMapBed, savedBinnaryBedVar);
                    }
                    if (options.isBinaryGtyOut) {
                        uniqueGenome.exportKGGSeqBinaryGty(chromosomes[chromID], filelKedStream, bwMapKed, savedBinnaryKedVar);
                    }
                    if (options.isPlinkPedOut) {
                        uniqueGenome.export2FlatTextPlink(chromosomes[chromID], subjectList, pedEncodeGytIDMap, bwMapPed, options.outputFileName, savedPedVar, options.outGZ);
                    }
                }

                if (needWriteTmp) {
                    uniqueGenome.export2ATmpFormat(tmpWriter, chromID);
                }
                if (options.isANNOVAROut) {
                    uniqueGenome.export2ANNOVARInput(annovarFilteredInFileWriter, chromID);
                }

                if (options.isGeneVarGroupFileOut) {
                    uniqueGenome.export2GeneVarGroupFile(bwGeneVarGroupFile, chromID);
                }

                //to release memory, release all lefte variants on this chromosome 
                uniqueGenome.export2FlatText(finalExportWriter, chromID, needHead, options.needRecordAltFreq);//To write the result into a temp file. 

                if (options.needGtyQual) {
                    uniqueGenome.export2VCFFormat(vcfFilteredInFileWriter, chromID);
                }

                if (options.mergeGtyDb != null && (options.isPlinkPedOut || options.isPlinkBedOut)) {
                    //to be finished by Li Jiang
                    refhapGenome.loadVariantFromDisk(chromID);
                    if (!subjectList.isEmpty() && !refIndivList.isEmpty()) {
                        if (options.isPlinkBedOut) {
                            uniqueGenome.exportPlinkBinaryGty(refhapGenome.getChromosomes()[chromID], subjectList, pedEncodeGytIDMap, mergedFileStream, bwMap, coutVar);//Write .bim and .bed file
                        }
                        if (options.isPlinkPedOut) {
                            uniqueGenome.export2FlatTextPlink(refhapGenome.getChromosomes()[chromID], subjectList, pedEncodeGytIDMap, bwMap, options.outputFileName, intsSPV, options.outGZ);//Write .map and .ped file
                        }
                    }
                }

                finalVarNum += chromosomeVar.size();
                chromosomeVar.clear();
                chromosomes[chromID].getPosIndexMap().clear();
                needHead = false;
                chromosomeVarAll.clear();
                System.gc();
            }
            if (needWriteTmp) {
                tmpWriter.close();
            }
//************************************************************************************************************************************************************************//
            if (vcfFilteredInFileWriter != null) {
                vcfFilteredInFileWriter.close();
            }
            finalExportWriter.close();
            if (annovarFilteredInFileWriter != null) {
                annovarFilteredInFileWriter.close();
            }

            if (referenceGenomes != null) {
                for (int i = 0; i < referenceGenomes.length; i++) {
                    referenceGenomes[i] = null;
                }
            }

            if (minMissingQCFilter1 != null) {
                String info = minMissingQCFilter1.toString();
                if (!info.isEmpty()) {
                    LOG.info(info);
                }
                minMissingQCFilter1 = null;
            }

            if (inheritanceModelFilter2 != null) {
                String info = inheritanceModelFilter2.toString();
                if (!info.isEmpty()) {
                    LOG.info(info);
                }
                inheritanceModelFilter2 = null;
            }

            if (denovoModelFilter3 != null) {
                String info = denovoModelFilter3.toString();
                if (!info.isEmpty()) {
                    LOG.info(info);
                }
                denovoModelFilter3 = null;
            }

            if (somaticModelFilter4 != null) {
                String info = somaticModelFilter4.toString();
                if (!info.isEmpty()) {
                    LOG.info(info);
                }
            }

            if (options.sampleVarHardFilterCode != null) {
                if (uniqueFilters[0] || uniqueFilters[1]) {
                    uniqueGenome.setVarNum(assCCUMFV.getAnnotNum());
                    if (uniqueGenome.getVarNum() == 0) {
                        LOG.info("0 sequence variant(s) are left finally!");

                    }
                    String info = assCCUMFV.getAnnotNum() + " variant(s) are left after filtration according to " + options.sampleVarHardFilterCode;
                    LOG.info(info);
                }
            }

            // if (options.sampleVarHardFilterCode != null && options.sampleVarHardFilterCode.equals("association") && options.inputFormat.endsWith("--vcf-file")) 
            if (options.toQQPlot && options.varAssoc) {
                List<String> nameList = new ArrayList<String>();
                nameList.add("Allelic");
                nameList.add("Dominant");
                nameList.add("Recessive");
                nameList.add("Genotypic");
                PValuePainter pvPainter = new PValuePainter(800, 600);
                File plotFile2 = new File(options.outputFileName + ".var.qq.png");
                StringBuilder message = new StringBuilder();

                message.append("Significance level of p value cutoffs for the overal error rate ").append(options.pValueThreshold).append(" :\n");

                double[] thrholds = new double[4];
                for (int i = 0; i < 4; i++) {
                    message.append(" ");
                    if (options.multipleTestingMethod == null || options.multipleTestingMethod.equals("no")) {
                        thrholds[i] = options.pValueThreshold;
                    } else if (options.multipleTestingMethod.equals("benfdr")) {
                        thrholds[i] = MultipleTestingMethod.benjaminiHochbergFDR(options.pValueThreshold, varPArray[i]);
                    } else if (options.multipleTestingMethod.equals("bonhol")) {
                        thrholds[i] = MultipleTestingMethod.bonferroniHolmFWE(options.pValueThreshold, varPArray[i]);
                    } else if (options.multipleTestingMethod.equals("bonf")) {
                        thrholds[i] = options.pValueThreshold / varPArray[i].size();
                    }
                    message.append(nameList.get(i));
                    message.append(": ");
                    message.append(thrholds[i]).append("\n");
                }
                LOG.info(message.substring(0, message.length() - 1));

                pvPainter.drawMultipleQQPlot(Arrays.asList(varPArray), nameList, null, plotFile2.getCanonicalPath(), 1E-10);
                String info = "The QQ plot saved in " + plotFile2.getCanonicalPath();
                LOG.info(info);
                showPlots(new File[]{plotFile2});

            }
            if (options.toQQPlot && options.skat) {
                List<String> nameList = new ArrayList<String>();
//                if (options.skatBinary) {
                nameList.add("SKAT");
                nameList.add("SKATO");
                nameList.add("Burden");
//                } else {
//                    nameList.add("SKAT");
//                    nameList.add("SKATO");
//                }

                PValuePainter pvPainter = new PValuePainter(800, 600);
                File plotFile2 = new File(options.outputFileName + ".skat.gene.qq.png");
                StringBuilder message = new StringBuilder();

                message.append("Significance level of p value cutoffs for SKAT p-values for the overal error rate ").append(options.pValueThreshold).append(" :\n");

                double[] thrholds = new double[4];
                for (int i = 0; i < genePVList.length; i++) {
                    message.append(" ");
                    if (options.multipleTestingMethod == null || options.multipleTestingMethod.equals("no")) {
                        thrholds[i] = options.pValueThreshold;
                    } else if (options.multipleTestingMethod.equals("benfdr")) {
                        thrholds[i] = MultipleTestingMethod.benjaminiHochbergFDR(options.pValueThreshold, genePVList[i]);
                    } else if (options.multipleTestingMethod.equals("bonhol")) {
                        thrholds[i] = MultipleTestingMethod.bonferroniHolmFWE(options.pValueThreshold, genePVList[i]);
                    } else if (options.multipleTestingMethod.equals("bonf")) {
                        thrholds[i] = options.pValueThreshold / genePVList[i].size();
                    }
                    message.append(nameList.get(i));
                    message.append(": ");
                    message.append(thrholds[i]).append("\n");
                }
                LOG.info(message.substring(0, message.length() - 1));

                pvPainter.drawMultipleQQPlot(Arrays.asList(genePVList), nameList, null, plotFile2.getCanonicalPath(), 1E-10);
                String info = "The QQ plot saved in " + plotFile2.getCanonicalPath();
                LOG.info(info);
                showPlots(new File[]{plotFile2});

            }
            double[][] thresholds = {{0, 0.01}, {0.01, 0.02}, {0.02, 0.03}, {0.03, 0.04}, {0.04, 0.05}, {0.05, 0.06}, {0.06, 0.07}, {0.07, 0.08},
            {0.08, 0.09}, {0.09, 0.1}, {0.1, 0.11}, {0.11, 0.12}, {0.12, 0.13}, {0.13, 0.14}, {0.14, 0.15}, {0.15, 0.16}, {0.16, 0.17},
            {0.17, 0.18}, {0.18, 0.19}, {0.19, 0.2}, {0.2, 0.21}, {0.21, 0.22}, {0.22, 0.23}, {0.23, 0.24}, {0.24, 0.25}, {0.25, 0.26},
            {0.26, 0.27}, {0.27, 0.28}, {0.28, 0.29}, {0.29, 0.3}, {0.3, 0.31}, {0.31, 0.32}, {0.32, 0.33}, {0.33, 0.34}, {0.34, 0.35},
            {0.35, 0.36}, {0.36, 0.37}, {0.37, 0.38}, {0.38, 0.39}, {0.39, 0.4}, {0.4, 0.41}, {0.41, 0.42}, {0.42, 0.43}, {0.43, 0.44},
            {0.44, 0.45}, {0.45, 0.46}, {0.46, 0.47}, {0.47, 0.48}, {0.48, 0.49}, {0.49, 0.50001}};
            HistogramPainter pvPainter = new HistogramPainter(800, 600);
            if (options.toMAFPlotSample) {
                final File plotFile = new File(options.outputFileName + ".maf.sample.png");
                pvPainter.drawColorfulHistogramPlot(mafSampleList, thresholds, null, "MAF in reference DB", plotFile.getCanonicalPath());
                String info = "The Histogram plot of MAF is saved in " + plotFile.getCanonicalPath();
                LOG.info(info);
                showPlots(new File[]{plotFile});
            }

            if (varaintDBHardFilterFiles5 != null) {
                for (int i = 0; i < varaintDBHardFilterFiles5.length; i++) {
                    varaintDBHardFilterFiles5[i].getBr().close();
                    String info = varaintDBHardFilterFiles5[i].getLeftNum() + " variant(s) are left after hard filtering in database " + varaintDBHardFilterFiles5[i].getName() + ", which contains " + varaintDBHardFilterFiles5[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    varaintDBHardFilterFiles5[i] = null;
                }
            }

            if (varaintDBFilterFiles6 != null) {
                for (int i = 0; i < varaintDBFilterFiles6.length; i++) {
                    varaintDBFilterFiles6[i].getBr().close();
                    String info = varaintDBFilterFiles6[i].getLeftNum() + " variant(s) exist in " + varaintDBFilterFiles6[i].getName() + ", which contains " + varaintDBFilterFiles6[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    varaintDBFilterFiles6[i] = null;
                }
            }

            if (assLocalHardFilterFile5 != null) {
                for (int i = 0; i < assLocalHardFilterFile5.length; i++) {
                    assLocalHardFilterFile5[i].getBr().close();
                    String info = assLocalHardFilterFile5[i].getLeftNum() + " variant(s) are left after hard filtering in database " + assLocalHardFilterFile5[i].getName() + ", which contains " + assLocalHardFilterFile5[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    assLocalHardFilterFile5[i] = null;
                }
            }

            if (assLocalFilterFile6 != null) {
                for (int i = 0; i < assLocalFilterFile6.length; i++) {
                    assLocalFilterFile6[i].getBr().close();
                    String info = assLocalFilterFile6[i].getLeftNum() + " variant(s) exist in " + assLocalFilterFile6[i].getName() + ", which contains " + assLocalFilterFile6[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    assLocalFilterFile6[i] = null;
                }
            }

            if (assLocalHardFilterVCFFile5 != null) {
                for (int i = 0; i < assLocalHardFilterVCFFile5.length; i++) {
                    assLocalHardFilterVCFFile5[i].getBr().close();
                    String info = assLocalHardFilterVCFFile5[i].getLeftNum() + " variant(s) are left after hard filtering in database " + assLocalHardFilterVCFFile5[i].getName() + ", which contains " + assLocalHardFilterVCFFile5[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    assLocalHardFilterVCFFile5[i] = null;
                }
            }

            if (assLocalFilterVCFFile6 != null) {
                for (int i = 0; i < assLocalFilterVCFFile6.length; i++) {
                    assLocalFilterVCFFile6[i].getBr().close();
                    String info = assLocalFilterVCFFile6[i].getLeftNum() + " variant(s) exist in " + assLocalFilterVCFFile6[i].getName() + ", which contains " + assLocalFilterVCFFile6[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    assLocalFilterVCFFile6[i] = null;
                }
            }

            if (assLocalHardFilterNoGtyVCFFile5 != null) {
                for (int i = 0; i < assLocalHardFilterNoGtyVCFFile5.length; i++) {
                    assLocalHardFilterNoGtyVCFFile5[i].getBr().close();
                    String info = assLocalHardFilterNoGtyVCFFile5[i].getLeftNum() + " variant(s) are left after hard filtering in database " + assLocalHardFilterNoGtyVCFFile5[i].getName() + ", which contains " + assLocalHardFilterNoGtyVCFFile5[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    assLocalHardFilterNoGtyVCFFile5[i] = null;
                }
            }

            if (assLocalFilterNoGtyVCFFile6 != null) {
                for (int i = 0; i < assLocalFilterNoGtyVCFFile6.length; i++) {
                    assLocalFilterNoGtyVCFFile6[i].getBr().close();
                    String info = assLocalFilterNoGtyVCFFile6[i].getLeftNum() + " variant(s) exist in " + assLocalFilterNoGtyVCFFile6[i].getName() + ", which contains " + assLocalFilterNoGtyVCFFile6[i].getTotalNum() + " effective variants.";
                    LOG.info(info);
                    assLocalFilterNoGtyVCFFile6[i] = null;
                }
            }

            if (varaintDBFilterFiles6 != null || options.localFilterFileNames != null || options.localFilterVCFFileNames != null) {
                if (options.isAlleleFreqExcMode) {
                    uniqueGenome.setVarNum(assFBAFEM.getAnnotNum());
                    String info = assFBAFEM.getAnnotNum() + " variant(s) with minor allele frequency [" + 0 + ", " + options.minAlleleFreqExc + ") in the reference datasets above are left!";
                    LOG.info(info);
                } else {
                    uniqueGenome.setVarNum(assFBAFIM.getAnnotNum());
                    String info = assFBAFIM.getAnnotNum() + " variant(s) with minor allele frequency [" + options.minAlleleFreqInc + ", " + options.maxAlleleFreqInc + "] in the reference datasets above are left!";
                    LOG.info(info);
                }

                if (options.toMAFPlotRef) {
                    final File plotFile = new File(options.outputFileName + ".maf.ref.png");
                    pvPainter.drawColorfulHistogramPlot(mafRefList, thresholds, null, "MAF in reference DB", plotFile.getCanonicalPath());
                    String info = "The Histogram plot of MAF is saved in " + plotFile.getCanonicalPath();
                    LOG.info(info);
                    showPlots(new File[]{plotFile});
                }
            }

            if (variantsCounters != null) {
                double totolVarNum = 0;
                for (int num : variantsCounters) {
                    totolVarNum += num;
                }
                StringBuilder info = new StringBuilder();
                info.append("Variants with different gene features:\n");
                for (int i = 0; i < VAR_FEATURE_NAMES.length; i++) {
                    info.append(" ").append(i).append(".").append(VAR_FEATURE_NAMES[i]).append(": ").append(variantsCounters[i]).append(" (")
                            .append(Util.doubleToString(variantsCounters[i] / totolVarNum * 100, 3)).append("%)\n");
                }
                LOG.info(info);
                LOG.info(geneDBFilter7.toString());
            }

            if (dbNSFPAnnot8 != null) {
                String info = dbNSFPAnnot8.toString();
                if (!info.isEmpty()) {
                    LOG.info(info);
                }
            }

            if (dbNSFPPred9 != null) {
                LOG.info(dbNSFPPred9.toString(0, 3, " "));
                String info = dbNSFPPred9.toString(3, 4, "");
                if (!info.isEmpty()) {
                    LOG.info(info);
                }
            }

            if (dbNoncodePred9d1 != null) {
                LOG.info(dbNoncodePred9d1.toString(0, 3, " "));
                String info = dbNoncodePred9d1.toString(3, 4, "");
                if (!info.isEmpty()) {
                    LOG.info(info);
                }
            }

            if (options.geneVarFilter > 0) {
                StringBuilder info = new StringBuilder();
                info.append(assGVF10.getLeftNum()).append(" variant(s) are left after filtered by genes with ").append(options.geneVarFilter).append(" or over variants!");
                LOG.info(info);
                uniqueGenome.setVarNum(assGVF10.getLeftNum());
            }

            if (options.candidateGeneSet != null && !options.candidateGeneSet.isEmpty()) {
                if (uniqueGenome.getGeneNum() > 0) {
                    String strInfo = assG11.getName() + ": " + assG11.getAnnotNum() + " mRNAs belong to the candidate gene set.";
                    LOG.info(strInfo);
                } else if (uniqueGenome.getVarNum() > 0) {
                    String strInfo = assV12.getName() + ": " + assV12.getAnnotNum() + " variants are within candidate gene set.";
                    LOG.info(strInfo);
                }
                if (options.ppidb != null) {
                    if (uniqueGenome.getGeneNum() > 0) {
                        String strInfo = assPPIG13.getName() + ": " + assPPIG13.getAnnotNum() + " mRNAs belong to the candidate gene set.";
                        LOG.info(strInfo);
                    } else if (uniqueGenome.getVarNum() > 0) {
                        LOG.info(assPPIV14.getAnnotNum() + " sequence variant(s) are highlighted by PPI information!");
                    }
                }

                if (options.genesetdb != null) {
                    if (mappedPathes != null) {
                        if (uniqueGenome.getGeneNum() > 0) {
                            LOG.info(assPWG15.getName() + ": " + assPWG15.getAnnotNum() + " mRNAs are involved into the related GeneSets.");
                        } else if (uniqueGenome.getVarNum() > 0) {
                            LOG.info(assPWV16.getAnnotNum() + " sequence variant(s) are highlighted by GeneSet information!");
                        }
                    }
                }
            }

            if (options.ibsCheckCase >= 0 && options.inputFormat.endsWith("--vcf-file")) {
                uniqueGenome.setVarNum(assIBS17d1.getAnnotNum());
                LOG.info(assIBS17d1.getAnnotNum() + " variant(s) are left after filtered by IBS filtering!");
                if (uniqueGenome == null || uniqueGenome.getVarNum() == 0) {
                    LOG.info("0 sequence variant(s) are left finally!");
                    return;
                }
            }

            if (options.homozygousRegionCase >= 0 && options.inputFormat.endsWith("--vcf-file")) {
                uniqueGenome.setVarNum(assHRC17d2.getAnnotNum());
                LOG.info(assHRC17d2.getAnnotNum() + " variant(s) are left after filtered by Homozygosity filtering!");
                if (uniqueGenome == null || uniqueGenome.getVarNum() == 0) {
                    LOG.info("0 sequence variant(s) are left finally!");
                    return;
                }
            }

            // only keep genes in inGeneSet
            if (options.geneDBLabels != null) {
                if (!options.inGeneSet.isEmpty()) {
                    StringBuilder info = new StringBuilder();
                    info.append(assGIS17d3.getAnnotNum()).append(" variant(s) are left after filtering those outside specified genes!");
                    LOG.info(info);
                    uniqueGenome.setVarNum(assGIS17d3.getAnnotNum());
                }

                // only keep genes in outGeneSet
                if (!options.outGeneSet.isEmpty()) {
                    StringBuilder info = new StringBuilder();
                    info.append(assGOS17d4.getAnnotNum()).append(" variant(s) are left after filtering those inside specified genes!");
                    LOG.info(info);
                    uniqueGenome.setVarNum(assGOS17d4.getAnnotNum());
                }
            }

            if (options.ibdFileName != null) {
                LOG.info(assIBD17d5.getLeftNum() + " variant(s) are left after filtered by IBD Region filtering!");
            }

            if (dbScSNV18 != null) {
                dbScSNV18.getBr().close();
                String info = dbScSNV18.getLeftNum() + " variant(s) exist in " + dbScSNV18.getName() + ", which contains " + dbScSNV18.getTotalNum() + " effective variants.";
                LOG.info(info);
                dbScSNV18 = null;
            }

            if (doubleHitGeneModelFilter19 != null) {
                int hitGenenNum = hitDisCountsTriosGenes.size();
                for (int i = 2; i < hitGenenNum; i++) {
                    String[] row = hitDisCountsTriosGenes.get(i);
                    Double lens = geneLengths.get(row[0]);
                    if (lens == null) {
                        row[2] = "0";
                        hitDisCountsTriosGenes.get(i)[2] = row[2];
                        hitDisCounTriosReads.get(i)[2] = row[2];
                    } else {
                        row[2] = String.valueOf(lens);
                        hitDisCountsTriosGenes.get(i)[2] = row[2];
                        hitDisCounTriosReads.get(i)[2] = row[2];
                    }
                }

                StringBuilder info = new StringBuilder();
                String info1 = doubleHitGeneModelFilter19.toString();
                if (info1.isEmpty()) {
                    info.append(info1).append('\n');
                }

                String outFileName = options.outputFileName + ".doublehit.gene.trios.flt";

                List<List<String[]>> arrys = new ArrayList<List<String[]>>();

                arrys.add(hitDisCountsTriosGenes);
                arrys.add(hitDisCounTriosReads);

                if (options.excelOut) {
                    List<String> sheetLabels = new ArrayList<String>();
                    sheetLabels.add("counts");
                    sheetLabels.add("genotypes");

                    File savedFile = new File(outFileName + ".xlsx");
                    LocalExcelFile.writeMultArray2XLSXFile(savedFile.getCanonicalPath(), arrys, sheetLabels, true, 0);
                    info.append("All POSSIBLE double-hit genes are saved in ").append(savedFile.getCanonicalPath()).append(".");
                } else {
                    File savedFile1 = new File(outFileName + ".count.txt");
                    LocalFile.writeData(savedFile1.getCanonicalPath(), arrys.get(0), "\t", false);
                    File savedFile2 = new File(outFileName + ".gty.txt");
                    LocalFile.writeData(savedFile2.getCanonicalPath(), arrys.get(1), "\t", false);
                    info.append("All POSSIBLE double-hit genes are saved in ").append(savedFile1.getCanonicalPath()).append(" and ").append(savedFile2.getCanonicalPath()).append(".");
                }

                info.append("\nThe double-hit genes:\n");
                if (!caseDoubleHitTriosGenes.isEmpty()) {
                    info.append("in cases: ").append(caseDoubleHitTriosGenes.size()).append("\n").append(caseDoubleHitTriosGenes.toString()).append("\n");
                }
                LOG.info(info);
            }

            if (doubleHitGeneModelFilter19d1 != null) {
                /*
                 //assign gene length
                 int hitGenenNum = doubleHitGenePhasedGenes.size();
                 for (int i = 2; i < hitGenenNum; i++) {
                 String[] row = doubleHitGenePhasedGenes.get(i);
                 Double lens = geneLengths.get(row[0]);
                 if (lens == null) {
                 row[2] = "0";
                 doubleHitGenePhasedGenes.get(i)[2] = row[2];
                 doubleHitGenePhasedReads.get(i)[2] = row[2];
                 } else {
                 row[2] = String.valueOf(lens);
                 doubleHitGenePhasedGenes.get(i)[2] = row[2];
                 doubleHitGenePhasedReads.get(i)[2] = row[2];
                 }
                 }
                 */
                StringBuilder info = new StringBuilder();
                String info1 = doubleHitGeneModelFilter19d1.toString();
                if (info1.isEmpty()) {
                    info.append(info1).append('\n');
                }

                String outFileName = options.outputFileName + ".doublehit.gene.phased.flt";

                List<List<String[]>> arrys = new ArrayList<List<String[]>>();

                arrys.add(doubleHitGenePhasedGenes);
                arrys.add(doubleHitGenePhasedReads);

                if (options.excelOut) {
                    List<String> sheetLabels = new ArrayList<String>();
                    sheetLabels.add("counts");
                    sheetLabels.add("genotypes");

                    File savedFile = new File(outFileName + ".xlsx");
                    LocalExcelFile.writeMultArray2XLSXFile(savedFile.getCanonicalPath(), arrys, sheetLabels, true, 0);
                    info.append("All POSSIBLE double-hit genes are saved in ").append(savedFile.getCanonicalPath()).append(".");
                } else {
                    File savedFile1 = new File(outFileName + ".count.txt");
                    LocalFile.writeData(savedFile1.getCanonicalPath(), arrys.get(0), "\t", false);
                    File savedFile2 = new File(outFileName + ".gty.txt");
                    LocalFile.writeData(savedFile2.getCanonicalPath(), arrys.get(1), "\t", false);
                    info.append("All POSSIBLE double-hit genes are saved in ").append(savedFile1.getCanonicalPath()).append(" and ").append(savedFile2.getCanonicalPath()).append(".");
                }
                //controlDoubleHitPhasedGenes
                info.append("\nThe double-hit genes:\n");
                if (!caseDoubleHitPhasedGenes.isEmpty()) {
                    info.append("in cases: ").append(caseDoubleHitPhasedGenes.size()).append("\n").append(caseDoubleHitPhasedGenes.toString()).append("\n");
                }
                LOG.info(info);
            }

            if (options.needAnnotateGene) {
                if (uniqueGenome.getGeneNum() > 0) {
                    String strInfo = assGene20.getName() + ": " + assGene20.getAnnotNum() + " variants are annotated out of " + assGene20.getTotalNum() + " variants.";
                    LOG.info(strInfo);
                } else if (uniqueGenome.getVarNum() > 0) {
                    String strInfo = assVariant20.getName() + ": " + assVariant20.getAnnotNum() + " variants are annotated out of " + assVariant20.getTotalNum() + " variants.";
                    LOG.info(strInfo);
                }
            }

            if (options.omimAnnotateGene) {
                if (uniqueGenome.getGeneNum() > 0) {
                    String strInfo = assOmimGene21.getName() + ": " + assOmimGene21.getAnnotNum() + " variants are annotated out of " + assOmimGene21.getTotalNum() + " variants.";
                    LOG.info(strInfo);
                } else if (uniqueGenome.getVarNum() > 0) {
                    LOG.info(assOmimVar21.getAnnotNum() + " sequence variant(s) are highlighted by OMIM information!");
                }
            }

            if (options.superdupAnnotate) {
                String info = assSDA22.getLeftNum() + " variant(s) are in super-duplicated regions registered in a data set genomicSuperDups table of UCSC";
                LOG.info(info);
            } else if (options.superdupFilter) {
                uniqueGenome.setVarNum(assSDF22.getLeftNum());
                LOG.info(assSDF22.getLeftNum() + " variant(s) are left after filtering those in super-duplicated regions registered in a data set genomicSuperDups table of UCSC!");
            }

            if (options.dgvcnvAnnotate) {
                StringBuilder info = new StringBuilder();
                info.append('\n');
                LOG.info(info);
                LOG.info(assDGV23.getLeftNum() + " variant(s) are in large copy-number variation (CNV) regions registered in Database of Genomic Variants http://projects.tcag.ca/variation/.");
            }

            if (options.overlappedGeneFilter) {
                uniqueGenome.setVarNum(assOLGF.getLeftNum());
                String info = assOLGF.getLeftNum() + " variant(s) are left after filtered by the unique variants on gene level.\n";
                LOG.info(info);
            }

            if (options.searchList != null && !options.searchList.isEmpty()) {
                if (uniqueGenome.getGeneNum() > 0) {
                    if (options.pubmedMiningIdeo) {

                    }
                    if (options.pubmedMiningGene) {
                        LOG.info(pubmedSearch24_Gene.getAnnotNum() + " genes are highlighted by Pubmed information!");
                    }
                } else if (uniqueGenome.getVarNum() > 0) {
                    if (options.pubmedMiningIdeo) {

                    }
                    if (options.pubmedMiningGene) {
                        LOG.info(pubmedSearch24_Var.getAnnotNum() + " sequence variants' genes are highlighted by Pubmed information!");
                    }
                }
            }

            if (options.rsid) {
                String info = assRSID25.getAnnotNum() + " variants are annotated with rsid.";
                LOG.info(info);
            }

            if (options.flankingSequence > 0) {
                String info = assFS.getAnnotNum() + " variants are annotated with flanking sequence.";
            }

            if (options.phenolyzer) {
                String info = assPhenolyzer26.getAnnotNum() + " variants are annotated by phenolyzer.";
                LOG.info(info);
            }

            if (options.skat) {
                skat.closeRServe();
                String geneSumOutFile = options.outputFileName + ".skat.xlsx";
                skat.saveResult2Xlsx(geneSumOutFile);
            }

            if (options.mendelGenePatho) {
                String info = assPatho27.getAnnotNum() + " variants are annotated by pathology gene prediction.";
                LOG.info(info);
            }

            if (options.isPlinkBedOut) {
                if (bwMapBed != null) {
                    bwMapBed.close();
                }
                if (fileBedStream != null) {
                    fileBedStream.close();
                    //rafBed.close();
                }
                String info = null;
                if (options.outGZ) {
                    info = "Genotype of " + savedBinnaryBedVar[0] + " sequence variant(s) and " + subjectList.size() + " individuals are saved \nin "
                            + options.outputFileName + ".fam.gz " + options.outputFileName + ".bim.gz " + options.outputFileName + ".bed.gz " + " with Plink binary genotype format.";
                } else {
                    info = "Genotype of " + savedBinnaryBedVar[0] + " sequence variant(s) and " + subjectList.size() + " individuals are saved \nin "
                            + options.outputFileName + ".fam " + options.outputFileName + ".bim " + options.outputFileName + ".bed " + " with Plink binary genotype format.";
                }
                LOG.info(info);
            }

            if (options.isGeneVarGroupFileOut) {
                if (bwGeneVarGroupFile != null) {
                    bwGeneVarGroupFile.close();
                }
                String info;
                if (options.outGZ) {
                    info = "A group file for gene-based association analysis is produced, " + options.outputFileName + ".gene.grp.gz.";
                } else {
                    info = "A group file for gene-based association analysis is produced, " + options.outputFileName + ".gene.grp.";
                }
                LOG.info(info);
            }

            if (options.isPlinkPedOut) {
                bwMapPed.close();
                //merge   files
                BufferedReader brPed = null;
                if (options.outGZ) {
                    brPed = LocalFileFunc.getBufferedReader(options.outputFileName + ".ped.p.gz");
                } else {
                    brPed = LocalFileFunc.getBufferedReader(options.outputFileName + ".ped.p");
                }

                File[] files = new File[STAND_CHROM_NAMES.length];
                BufferedReader[] brPedGty = new BufferedReader[STAND_CHROM_NAMES.length];
                for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                    if (options.outGZ) {
                        files[i] = new File(options.outputFileName + ".ped." + i + ".gz");
                    } else {
                        files[i] = new File(options.outputFileName + ".ped." + i);
                    }
                    if (!files[i].exists()) {
                        continue;
                    }
                    brPedGty[i] = LocalFileFunc.getBufferedReader(files[i].getCanonicalPath());
                }

//                BufferedWriter bwPed = LocalFileFunc.getBufferedWriter(options.outputFileName + ".ped", false);
                BufferedWriter bwPed = null;
                if (options.outGZ) {
                    GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(options.outputFileName + ".ped" + ".gz"));
                    bwPed = new BufferedWriter(new OutputStreamWriter(gzOut));
                } else {
                    bwPed = LocalFileFunc.getBufferedWriter(options.outputFileName + ".ped", false);
                }

                String line;
                //assume the brPed and brPedGty have the same number of rows
                while ((line = brPed.readLine()) != null) {
                    bwPed.write(line);
                    for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                        if (brPedGty[i] == null) {
                            continue;
                        }
                        bwPed.write(brPedGty[i].readLine());
                    }
                    bwPed.write('\n');
                }
                brPed.close();
                bwPed.close();

                //file.delete();
                for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
                    if (!files[i].exists()) {
                        continue;
                    }
                    brPedGty[i].close();
                    files[i].deleteOnExit();
                    //files[i].delete();
                }

                File filePedP;
                if (options.outGZ) {
                    filePedP = new File(options.outputFileName + ".ped.p.gz");
                } else {
                    filePedP = new File(options.outputFileName + ".ped.p");
                }
                filePedP.deleteOnExit();
                String info = null;
                if (options.outGZ) {
                    info = "Genotype of " + savedPedVar[0] + " sequence variant(s) and " + subjectList.size()
                            + " individuals are saved \nin " + options.outputFileName + ".map.gz " + options.outputFileName + ".ped.gz  with Plink pedigree format.";
                } else {
                    info = "Genotype of " + savedPedVar[0] + " sequence variant(s) and " + subjectList.size()
                            + " individuals are saved \nin " + options.outputFileName + ".map " + options.outputFileName + ".ped  with Plink pedigree format.";
                }
                LOG.info(info);
            }

            if (options.isBinaryGtyOut) {
                if (bwMapKed != null) {
                    bwMapKed.close();
                }

                if (filelKedStream != null) {
                    filelKedStream.close();
                    rafKed.close();
                }
                String info = null;
                if (options.outGZ) {
                    info = "Genotype of " + savedBinnaryKedVar[0] + " sequence variant(s) and " + subjectList.size() + " individuals are saved \nin "
                            + options.outputFileName + ".fam.gz " + options.outputFileName + ".kim.gz " + options.outputFileName + ".ked.gz " + " with KGGSseq binary genotype format.";
                } else {
                    info = "Genotype of " + savedBinnaryKedVar[0] + " sequence variant(s) and " + subjectList.size() + " individuals are saved \nin "
                            + options.outputFileName + ".fam " + options.outputFileName + ".kim " + options.outputFileName + ".ked " + " with KGGSseq binary genotype format.";
                }

                LOG.info(info);
            }
            //to simply the log
            String gzLable = "";
            if (options.outGZ) {
                gzLable = ".gz";
            }
            if (options.mergeGtyDb != null && (options.isPlinkPedOut || options.isPlinkBedOut)) {
                if (!subjectList.isEmpty() && !refIndivList.isEmpty()) {
                    if (options.isPlinkBedOut) {
                        String info = "Genotype of " + coutVar[0] + " sequence variant(s) and " + intsIndiv[0] + " individuals are saved \nin "
                                + options.outputFileName + ".merged.fam" + gzLable + " " + options.outputFileName + ".merged.bim" + gzLable + " " + options.outputFileName + ".merged.bed" + gzLable + " " + " with Plink binary genotype format.";
                        LOG.info(info);
                        bwMap.close();
                        mergedFileChannel.close();
                        mergedFileStream.close();
                        rafKed.close();
                    }

                    if (options.isPlinkPedOut) {
                        String info = "Genotype of " + intsSPV[0] + " sequence variant(s) and " + intsSPV[1]
                                + " individuals are saved \nin " + options.outputFileName + ".merged.map" + gzLable + " " + options.outputFileName + ".merged.ped" + gzLable + " with Plink pedigree format.";
                        LOG.info(info);

                    }
                }
            }
            if (options.isVCFOut) {
                LOG.info("Finally, " + finalVarNum + " variants are saved in " + vcfFilteredInFile.getCanonicalPath() + " with VCF format.");
                vcfFilteredInFileWriter.close();
            }

            if (options.isANNOVAROut) {
                LOG.info("Finally, " + finalVarNum + " variants are saved in " + annovarFilteredInFile.getCanonicalPath() + " with ANNOVAR format.");
                annovarFilteredInFileWriter.close();
            }

            if (options.excelOut) {
                File finalFilteredInFile1 = new File(options.outputFileName + ".flt.xlsx");
                //As the function of appending data into an existing file is very slow; So I just have convert a text file into an excel file 
                LocalExcelFile.convertTextFile2XLSXFile(finalFilteredInFile.getCanonicalPath(), finalFilteredInFile1.getCanonicalPath(), true, 4);
                //Remove the text file to save storage space
                finalFilteredInFile.delete();
                LOG.info("Finally, " + finalVarNum + " variants are saved in " + finalFilteredInFile1.getCanonicalPath() + " with Excel format.\n");
            } else {
                LOG.info("Finally, " + finalVarNum + " variants are saved in " + finalFilteredInFile.getCanonicalPath() + " with flat text format.\n");
            }

            //-----------------------Annotate genes and gene sets on whole genome-------------------------------
            String[] cells = null;
            GeneAnnotator geneAnnotor = new GeneAnnotator();
            Map<String, Map<String, Integer>> cosmicGeneMut = null;
            Map<String, double[]> driverGeneScores = null;
            List<String> scoreNames = new ArrayList<String>();
            String geneCoVarFilePath = options.PUBDB_FILE_MAP.get("cancer.mutsig");

            geneCoVarFilePath = GlobalManager.RESOURCE_PATH + "/" + geneCoVarFilePath;
            driverGeneScores = geneAnnotor.readMutationGeneScore(geneCoVarFilePath, scoreNames);
            if (somaticModelFilter4 != null) {
                if (options.cosmicAnnotate) {
                    fileName = options.PUBDB_FILE_MAP.get("cosmicdb");
                    cosmicGeneMut = geneAnnotor.readCosmicGeneAnnotation(GlobalManager.RESOURCE_PATH + "/" + fileName);
                }
            }

            Map<String, double[]> geneMutRegPValueAllVar = new HashMap<String, double[]>();
            if (options.geneMutationRateTest) {
                geneAnnotor.geneMutationRateTest(uniqueGenome, geneLengths, cosmicGeneMut, driverGeneScores, scoreNames, options.genesetHyperGenePCut, geneMutRegPValueAllVar, options.outputFileName + ".gene.mutation.rate.xlsx");
            }

            String geneSetDBFile = null;
            if (options.enrichmentTestGeneSetDB != null) {
                if (options.enrichmentTestGeneSetDB.equals("cano")) {
                    geneSetDBFile = GlobalManager.RESOURCE_PATH + "/c2.cp.v3.1.symbols.gmt.gz";
                } else if (options.enrichmentTestGeneSetDB.equals("cura")) {
                    geneSetDBFile = GlobalManager.RESOURCE_PATH + "/c2.all.v3.1.symbols.gmt.gz";
                } else if (options.enrichmentTestGeneSetDB.equals("onco")) {
                    geneSetDBFile = GlobalManager.RESOURCE_PATH + "/c6.all.v3.1.symbols.gmt.gz";
                } else if (options.enrichmentTestGeneSetDB.equals("cmop")) {
                    geneSetDBFile = GlobalManager.RESOURCE_PATH + "/c4.all.v3.1.symbols.gmt.gz";
                } else if (options.enrichmentTestGeneSetDB.equals("onto")) {
                    geneSetDBFile = GlobalManager.RESOURCE_PATH + "/c5.all.v3.1.symbols.gmt.gz";
                }
            } else if (options.enrichmentTestGeneSetFile != null) {
                geneSetDBFile = options.enrichmentTestGeneSetFile;
            }

            if (options.genesetEnrichmentTest && geneSetDBFile != null && !geneMutRegPValueAllVar.isEmpty()) {
                geneAnnotor.enrichmentTestGeneSet(geneSetDBFile, geneMutRegPValueAllVar, options.genesetHyperGenePCut, options.genesetSizeMin, options.genesetSizeMax, options.outputFileName + ".geneset.xlsx");
            }

        } finally {
            if (uniqueGenome != null) {
                uniqueGenome.removeTempFileFromDisk();
            }
        }
    }
}
