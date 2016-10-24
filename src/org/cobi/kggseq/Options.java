/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.Util;

/**
 *
 * @author MX Li
 */
public class Options implements Constants {

    private static final Logger LOG = Logger.getLogger(Options.class);
    public int threadNum = 1;
    String[] options;
    int optionNum;
    public String inputFileName = null;
    public String outputFileName = null;
    public String command = null;
    public String inputFormat = null;
    public String inputFormatSupp = null;
    public String inputFileNameSupp = null;
    public String pedFile = null;
    public Map<String, Integer> phenotypeColID;
    public String sampleVarHardFilterCode = null;
    public String sampleGtyHardFilterCode = null;
    public boolean noLibCheck = true;
    public int minHetA = -9, minHomA = -9, minHetU = -9, minHomU = -9;
    public int minOBSA = -9, minOBSU = -9;
    public int minOBS = -9;
    public List<String> varaintDBLableList = null;
    public List<String> varaintDBLableHardList = null;
    public List<String> scoreDBLableList = null;
    public List<String> referenceGentotypeDBLableList = null;
    public boolean isPolyphen2Out = false;
    public boolean isSeattleSeqOut = false;
    public boolean isANNOVAROut = false;
    public boolean isVCFOut = false;
    public boolean isVCFOutFilterd = false;
    public boolean isPlinkPedOut = false;
    public boolean isPlinkBedOut = false;
    public boolean isBinaryGtyOut = false;
    public boolean isGeneVarGroupFileOut = false;
    public boolean outGZ = false;
    public String refGenomeVersion = "hg19";
    public boolean addHg19 = false;
    public String[] geneDBLabels = null;
    public Map<String, String> PUBDB_URL_MAP = new HashMap<String, String>();
    public Map<String, String> PUBDB_FILE_MAP = new HashMap<String, String>();
    public boolean noResCheck = false;
    public String resourceFolder = null;
    public boolean excelOut = false;
    public Set<String> candidateGeneSet = new HashSet<String>();
    public Set<String> inGeneSet = new HashSet<String>();
    public Set<String> outGeneSet = new HashSet<String>();
    public Set<Byte> geneFeatureIn = new HashSet<Byte>();
    public Set<Byte> dependentGeneFeature = new HashSet<Byte>();
    public Set<Byte> independentGeneFeature = new HashSet<Byte>();
    public float minAlleleFreqInc = 0.0f;
    public float maxAlleleFreqInc = 0.0f;
    public float minAlleleFreqExc = 0.01f;
    public boolean isAlleleFreqExcMode = true;
    public List<String> searchList = new ArrayList<String>();
    public boolean filterNonDiseaseMut = false;
    public int ibsCheckCase = -1;
    public int homozygousRegionCase = -1;
    public boolean needRecordAltFreq = false;
    public String extractVCF = null;
    public String ibdFileName = null;
    public int minGtySeqDP = 4;
    public double seqQual = 50;
    public double gtyQual = 10;
    public int gtySecPL = 20;
    public float gtyBestGP = 0.6f;
    public boolean filterNonTfbs = false;
    public boolean needRecordTfbs = false;
    public boolean isTFBSCheck = false;
    public String[] localFilterFileNames = null;
    public String[] localHardFilterFileNames = null;
    public String[] localFilterVCFFileNames = null;
    public String[] localHardFilterVCFFileNames = null;
    public String[] localFilterNoGtyVCFFileNames = null;
    public String[] localHardFilterNoGtyVCFFileNames = null;
    public String[] indivPhenos = null;
    public String[] indivPairs = null;
    public boolean needAnnotateGene = true;
    public boolean omimAnnotateGene = false;
    public boolean dbscSNVAnnote = false;

    public boolean cosmicAnnotate = false;
    public boolean dgvcnvAnnotate = false;
    public boolean superdupAnnotate = false;
    public boolean superdupFilter = false;
    public boolean dispensableGeneVariants = false;
    public boolean pseudoGeneVariants = false;
    public boolean isEnhancerCheck = false;
    public boolean filterNonEnhancer = false;
    public boolean needRecordEnhancer = false;
    public double pValueThreshold = 0.05;
    public String multipleTestingMethod = "no";

    // Bayes Annotation Source
    public String[] dbncfpFeatureColumn = null;
    public String cellLineName = "GM12878";
    // --------------------------------
    public boolean toQQPlot = false;
    public boolean toMAFPlotRef = false;
    public boolean toMAFPlotSample = false;
    public int neargeneDis = 1000;
    public int splicingDis = 2;
    public String buildverIn = null;
    public String buildverOut = null;
    public int[][] regionsInPos = null;
    public StringBuilder regionsInStr = new StringBuilder();
    public int[][] regionsOutPos = null;
    public StringBuilder regionsOutStr = new StringBuilder();

    public int flankingSequence = 0;
    public String ppidb = null;
    public String genesetdb = null;
    public double genesetHyperGenePCut = 0.1;
    public int genesetSizeMin = 5;
    public int genesetSizeMax = 200;
    public int ppiDepth = 1;
    public float maxAltAlleleFracRefHom = 1.0f;
    public float minAltAlleleFractHet = 0.0f;
    public float minAltAlleleFractAltHom = 0.0f;
    public Set<String> vcfFilterLabelsIn = null;
    public boolean considerSNV = true;
    public boolean considerIndel = false;
    public double minMappingQuality = 20;
    public double maxStandBias = Integer.MAX_VALUE;
    public double maxFisherStandBias = Integer.MAX_VALUE;
    public boolean useCompositeSubjectID = false;
    //  public static String[] REF_CHROM_NAMES = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "M", "1_random", "2_random", "3_random", "4_random", "5_h2_hap1", "5_random", "6_cox_hap1", "6_qbl_hap2", "6_random", "7_random", "8_random", "9_random", "10_random", "11_random", "13_random", "15_random", "16_random", "17_random", "18_random", "19_random", "21_random", "22_h2_hap1", "22_random", "X_random"};
    public short causingPredType = -1;
    public String predictExplanatoryVar = "all";
    public boolean needProgressionIndicator = false;
    public boolean needVerboseNoncode = false;
    public boolean pubmedMiningGene = false;
    public boolean pubmedMiningIdeo = false;
    public boolean driverGeneSetTest = false;
    public String enrichmentTestGeneSetDB = null;
    public String enrichmentTestGeneSetFile = null;
    public double somatReadsP = 0.05;
    public double hwdPCase = 0;
    public double hwdPControl = 0;
    public double hwdPAll = 0;
    public boolean doubleHitGeneTriosFilter = false;
    public boolean doubleHitGenePhasedFilter = false;
    public boolean countGeneVar = false;
    public boolean geneToleranceScore = false;
    public int geneVarFilter = -1;
    public boolean overlappedGeneFilter = false;
    public String mergeGtyDb = null;
    public boolean needGtys = false;
    public boolean needReads = false;
    public boolean noHomo = false;
    public boolean countAllPsudoControl = false;
    public String dbsnfpVersion = "3.0";
    public boolean needLog = true;
    public boolean noQC = true;
    public String missingGty = "./.";
    public double sampleMafOver = -1;
    public double sampleMafLess = 1.1;
    public boolean varAssoc = false;

//----------------------------
    public boolean onlyPositive = true;
    public int maxGtyAlleleNum = 4;
    public boolean needGtyQual = false;

    public boolean needRconnection = false;
//-----------------------------

    public boolean skat = false; // to be false. 
    public boolean skatBinary = false;
    public boolean permutePheno = false;
    public boolean cov = false;
    String[] covItem = null;
    public boolean phe = false;
    public String pheItem = null;
    public int skatCutoff = 0;

    public boolean phenolyzer = false;
    public boolean rsid = false; // to be false. 

    public boolean mendelGenePatho = false;

    public boolean geneMutationRateTest = false;
    public boolean genesetEnrichmentTest = false;

    public Options() {
        scoreDBLableList = new ArrayList<String>();
        varaintDBLableList = new ArrayList<String>();
        varaintDBLableHardList = new ArrayList<String>();
    }

    public String parseOptions() throws Exception {
        int id = -1;
        StringBuilder param = new StringBuilder();
//-----------------Environmental settings---------------------------------
        id = find("--no-lib-check");
        if (id >= 0) {
            noLibCheck = true;
        } else {
            //String info = "To disable library checking, use --no-lib-check.";
            //System.out.println(info);
        }
        id = find("--no-resource-check");
        if (id >= 0) {
            noResCheck = true;
        }

        id = find("--no-progress-check");
        if (id >= 0) {
            needProgressionIndicator = false;
        }

        id = find("--no-log");
        if (id >= 0) {
            needLog = false;
        } else {
            String info = "To disable log function in a text file, use --no-log.";
            System.out.println(info);
        }
        id = find("--nt");
        if (id >= 0) {
            threadNum = (int) Double.parseDouble(options[id + 1]);
            param.append("--nt");
            param.append(' ');
            param.append(threadNum);
            param.append('\n');
        } else {
            threadNum = 1;
            /*
             int cores = Runtime.getRuntime().availableProcessors();
             if (cores > 2) {
             threadNum = cores - 1;
             }
             */
        }

        id = find("--resource");
        if (id >= 0) {
            resourceFolder = options[id + 1];
            param.append("--resource");
            param.append(' ');
            param.append(resourceFolder);
            param.append('\n');
        }

        if (find("--make-filter") < 0) {
            id = find("--buildver");
            if (id >= 0) {
                refGenomeVersion = options[id + 1];
                param.append("--buildver");
                param.append(" ");
                param.append(refGenomeVersion);
                param.append('\n');
            } else {
                String info = "To set reference genome version,use --buildver hgXX; it is \'--buildver hg19\' by default";
                //System.out.println(info);
                //return false;
            }
        }

        id = find("--add-hg19");
        if (id >= 0) {
            param.append("--add-hg19\n");
            addHg19 = true;
        } else {

        }

        PUBDB_URL_MAP.put("dbsnp135", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_snp135.txt.gz");
        PUBDB_URL_MAP.put("dbsnp137", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_snp137.txt.gz");
        PUBDB_URL_MAP.put("dbsnp138", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_snp138.txt.gz");
        PUBDB_URL_MAP.put("dbsnp138nf", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_snp138.nf.txt.gz");
        PUBDB_URL_MAP.put("dbsnp141", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_snp141.txt.gz");
        PUBDB_URL_MAP.put("1kg201204", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_ALL.sites.2012_04.txt.gz");
        PUBDB_URL_MAP.put("1kgasn201204", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_ASN.sites.2012_04.txt.gz");
        PUBDB_URL_MAP.put("1kgeur201204", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_EUR.sites.2012_04.txt.gz");
        PUBDB_URL_MAP.put("1kgamr201204", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_AMR.sites.2012_04.txt.gz");
        PUBDB_URL_MAP.put("1kgafr201204", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_AFR.sites.2012_04.txt.gz");
        PUBDB_URL_MAP.put("1kg201305", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_ALL.sites.2013_05.txt.gz");
        PUBDB_URL_MAP.put("ESP5400", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_ESP5400.snps.txt.gz");
        PUBDB_URL_MAP.put("ESP6500AA", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_ESP6500AA.txt.gz");
        PUBDB_URL_MAP.put("ESP6500EA", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_ESP6500EA.txt.gz");
        PUBDB_URL_MAP.put("exac", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_exac.r0.2.sites.gz");
        PUBDB_URL_MAP.put("dbscSNV", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_mdbscSNV1.1.gz");

        PUBDB_URL_MAP.put("proteindomain", KGGSeq_URL + "download/resources/UniprotDomain.txt.gz");
        PUBDB_URL_MAP.put("uniportrefseqmap", KGGSeq_URL + "download/resources/UniprotRefSeqMap.tab.gz");
        PUBDB_URL_MAP.put("uniportgencodemap", KGGSeq_URL + "download/resources/UniprotEnsemblMap.tab.gz");
        PUBDB_URL_MAP.put("uniportucscgenemap", KGGSeq_URL + "download/resources/UniprotUCSCKnownGeneMap.tab.gz");
        //load buit-in datasets pathes
        if (refGenomeVersion.equals("hg18")) {
            PUBDB_URL_MAP.put("refgene", KGGSeq_URL + "download/resources/hg18/kggseq_hg18_refGene.txt.gz");
            //PUBDB_URL_MAP.put("refgenefa", KGGSeq_URL + "download/resources/hg18/hg18_refGeneMrna.fa.gz");
            PUBDB_URL_MAP.put("gencode", KGGSeq_URL + "download/resources/hg18/kggseq_hg18_GEncode.txt.gz");
            PUBDB_URL_MAP.put("knowngene", KGGSeq_URL + "download/resources/hg18/kggseq_hg18_knownGene.txt.gz");
            //PUBDB_URL_MAP.put("refgene", "http://hgdownload.cse.ucsc.edu/goldenPath/hg18/database/refGene.txt.gz");
            //PUBDB_URL_MAP.put("refgenefa", "http://hgdownload.cse.ucsc.edu/goldenPath/hg18/bigZips/refMrna.fa.gz");

            // PUBDB_URL_MAP.put("refgenefa", "http://www.openbioinformatics.org/annovar/download/hg18_refGeneMrna.fa.gz");
            PUBDB_URL_MAP.put("ideogram", KGGSeq_URL + "download/resources/hg18/hg18_ideogram.gz");

            PUBDB_URL_MAP.put("superdup", KGGSeq_URL + "download/resources/hg18/genomicSuperDups.txt.gz");

            PUBDB_URL_MAP.put("tfbs", KGGSeq_URL + "download/resources/hg18/tfbsConsSites.txt.gz");
            PUBDB_URL_MAP.put("enhancer", KGGSeq_URL + "download/resources/hg18/vistaEnhancers.txt.gz");
            PUBDB_URL_MAP.put("cosmicdb", KGGSeq_URL + "download/resources/hg18/hg18_CosmicV65.tsv.cp.gz");
            PUBDB_URL_MAP.put("dgvcnv", KGGSeq_URL + "download/resources/hg19/NCBI36_hg18_variants_2015-07-23.txt.gz");
        } else if (refGenomeVersion.equals("hg19")) {
            PUBDB_URL_MAP.put("refgene", KGGSeq_URL + "download/resources/hg19/kggseq_hg19_refGene.txt.gz");
            PUBDB_URL_MAP.put("gencode", KGGSeq_URL + "download/resources/hg19/kggseq_hg19_GEncode.txt.gz");
            PUBDB_URL_MAP.put("ensembl", KGGSeq_URL + "download/resources/hg19/kggseq_hg19_ensGene.txt.gz");
            PUBDB_URL_MAP.put("knowngene", KGGSeq_URL + "download/resources/hg19/kggseq_hg19_knownGene.txt.gz");

            PUBDB_URL_MAP.put("superdup", KGGSeq_URL + "download/resources/hg19/genomicSuperDups.txt.gz");
            PUBDB_URL_MAP.put("ideogram", KGGSeq_URL + "download/resources/hg19/hg19_ideogram.gz");

            PUBDB_URL_MAP.put("enhancer", KGGSeq_URL + "download/resources/hg19/vistaEnhancers.txt.gz");
            PUBDB_URL_MAP.put("tfbs", KGGSeq_URL + "download/resources/hg19/tfbsConsSites.txt.gz");

            PUBDB_URL_MAP.put("cosmicdb", KGGSeq_URL + "download/resources/hg19/hg19_CosmicV71.tsv.cp.gz");
            PUBDB_URL_MAP.put("dgvcnv", KGGSeq_URL + "download/resources/hg19/GRCh37_hg19_variants_2015-07-23.txt.gz");

            PUBDB_URL_MAP.put("noncoding1", KGGSeq_URL + "download/resources/hg19/hg19_noncode_region.gz");
            PUBDB_URL_MAP.put("noncoding2", KGGSeq_URL + "download/resources/hg19/hg19_noncode_point.gz");
            PUBDB_URL_MAP.put("rsid", KGGSeq_URL + "download/resources/hg19/b142_SNPChrPosOnRef_GRCh19p105.bcp.gz");

            PUBDB_URL_MAP.put("hapmap2.r22.ceu.hg19", "resources/hg19/hapmap/hapmap2.r22.ceu.hg19.tar");
            PUBDB_URL_MAP.put("hapmap2.r22.chbjpt.hg19", "resources/hg19/hapmap/hapmap2.r22.chbjpt.hg19.tar");
            PUBDB_URL_MAP.put("hapmap2.r22.yri.hg19", "resources/hg19/hapmap/hapmap2.r22.yri.hg19.tar");
            PUBDB_URL_MAP.put("hapmap3.r2.ceu.hg19", "resources/hg19/hapmap/hapmap3.r2.ceu.hg19.tar");
            PUBDB_URL_MAP.put("hapmap3.r2.chbjpt.hg19", "resources/hg19/hapmap/hapmap3.r2.chbjpt.hg19.tar");
            PUBDB_URL_MAP.put("hapmap3.r2.mex.hg19", "resources/hg19/hapmap/hapmap3.r2.mex.hg19.tar");
            PUBDB_URL_MAP.put("hapmap3.r2.tsi.hg19", "resources/hg19/hapmap/hapmap3.r2.tsi.hg19.tar");
            PUBDB_URL_MAP.put("hapmap3.r2.yri.hg19", "resources/hg19/hapmap/hapmap3.r2.yri.hg19.tar");

            PUBDB_URL_MAP.put("1kg.phase1.v3.shapeit2.asn.hg19", "resources/hg19/1kg/p1v3/ASN/1kg.phase1.v3.shapeit2.asn.hg19.chr_CHROM_.vcf.gz");
            PUBDB_URL_MAP.put("1kg.phase1.v3.shapeit2.eur.hg19", "resources/hg19/1kg/p1v3/EUR/1kg.phase1.v3.shapeit2.eur.hg19.chr_CHROM_.vcf.gz");
            PUBDB_URL_MAP.put("1kg.phase1.v3.shapeit2.afr.hg19", "resources/hg19/1kg/p1v3/AFR/1kg.phase1.v3.shapeit2.afr.hg19.chr_CHROM_.vcf.gz");
            PUBDB_URL_MAP.put("1kg.phase1.v3.shapeit2.amr.hg19", "resources/hg19/1kg/p1v3/AMR/1kg.phase1.v3.shapeit2.amr.hg19.chr_CHROM_.vcf.gz");

            PUBDB_URL_MAP.put("1kg.phase3.v5.shapeit2.eas.hg19", "resources/hg19/1kg/p3v5/EAS/1kg.phase3.v5.shapeit2.eas.hg19.chr_CHROM_.vcf.gz");
            PUBDB_URL_MAP.put("1kg.phase3.v5.shapeit2.sas.hg19", "resources/hg19/1kg/p3v5/SAS/1kg.phase3.v5.shapeit2.sas.hg19.chr_CHROM_.vcf.gz");
            PUBDB_URL_MAP.put("1kg.phase3.v5.shapeit2.eur.hg19", "resources/hg19/1kg/p3v5/EUR/1kg.phase3.v5.shapeit2.eur.hg19.chr_CHROM_.vcf.gz");
            PUBDB_URL_MAP.put("1kg.phase3.v5.shapeit2.afr.hg19", "resources/hg19/1kg/p3v5/AFR/1kg.phase3.v5.shapeit2.afr.hg19.chr_CHROM_.vcf.gz");
            PUBDB_URL_MAP.put("1kg.phase3.v5.shapeit2.amr.hg19", "resources/hg19/1kg/p3v5/AMR/1kg.phase3.v5.shapeit2.amr.hg19.chr_CHROM_.vcf.gz");

        } else if (refGenomeVersion.equals("hg19")) {
         
            PUBDB_URL_MAP.put("dgvcnv", KGGSeq_URL + "download/resources/hg19/GRCh38_hg38_variants_2015-07-23.txt.gz");
        } else {
            throw new Exception("No resource data for reference genome in version " + refGenomeVersion);
        }

        //dataset reference genome version free
        PUBDB_URL_MAP.put("dbnsfp", KGGSeq_URL + "download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_mdbNSFP" + dbsnfpVersion + ".chr");
        PUBDB_URL_MAP.put("cura", KGGSeq_URL + "download/resources/c2.all.v3.1.symbols.gmt.gz");
        PUBDB_URL_MAP.put("cano", KGGSeq_URL + "download/resources/c2.cp.v3.1.symbols.gmt.gz");
        PUBDB_URL_MAP.put("onco", KGGSeq_URL + "download/resources/c6.all.v3.1.symbols.gmt.gz");
        PUBDB_URL_MAP.put("cmop", KGGSeq_URL + "download/resources/c4.all.v3.1.symbols.gmt.gz");
        PUBDB_URL_MAP.put("onto", KGGSeq_URL + "download/resources/c5.all.v3.1.symbols.gmt.gz");
        PUBDB_URL_MAP.put("string", KGGSeq_URL + "download/resources/PPI.txt.gz");

        PUBDB_URL_MAP.put("morbidmap", KGGSeq_URL + "download/resources/morbidmap.gz");
        PUBDB_URL_MAP.put("mendelcausalrare.param", KGGSeq_URL + "download/resources/mendelcausalrare" + dbsnfpVersion + ".param.gz");
        //PUBDB_URL_MAP.put("cancer.param", KGGSeq_URL + "download/resources/cancersomaticdriver.param.gz");
        PUBDB_URL_MAP.put("cancer.param", KGGSeq_URL + "download/resources/CancerRandomForests" + dbsnfpVersion + ".zip");
        PUBDB_URL_MAP.put("cancer.mutsig", KGGSeq_URL + "download/resources/mutsig.gene.covariates.txt");
        PUBDB_URL_MAP.put("mendelgene", KGGSeq_URL + "download/resources/ModeSpecificPredicMendelGene.txt.gz");

        PUBDB_URL_MAP.put("cancer.null.driver.score", KGGSeq_URL + "download/resources/nullcancervarscore.txt.gz");
        PUBDB_URL_MAP.put("humdiv.param", KGGSeq_URL + "download/resources/humdiv.param");

        for (Map.Entry<String, String> item : PUBDB_URL_MAP.entrySet()) {
            String fileName = item.getValue().substring(item.getValue().lastIndexOf("resources/") + 10);
            PUBDB_FILE_MAP.put(item.getKey(), fileName);
        }

        //-------------------throw exceptions of obsoleted commands
        if (find("--prioritize-monogen") >= 0) {
            String infor = "--prioritize-monogen is on longer needed since kggseq v0.3.\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }
        if (find("--prioritize-complex") >= 0) {
            String infor = "--prioritize-complex is on longer needed since kggseq v0.3\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }

        if (find("--no-missing-case") >= 0) {
            String infor = "--no-missing-case has been replaced with --min-obsa # since kggseq v0.3\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }
        if (find("--disable-vcf-filter") >= 0) {
            String infor = "--disable-vcf-filter has been replaced with --vcf-filter-in # since kggseq v0.3\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }

        if (find("--ppi") >= 0) {
            String infor = "--ppi has been replaced with --ppi-annot since kggseq v0.3\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }

        if (find("--pathway") >= 0) {
            String infor = "--pathway has been replaced with --geneset-annot since kggseq v0.3\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }

        if (find("--pseudogene-anno") >= 0 || find("--dispengene-anno") >= 0 || find("--tfbs-anno") >= 0 || find("--enhancer-anno") >= 0) {
            String infor = "--pseudogene-anno, --dispengene-anno, --tfbs-anno and --enhancer-anno have been coverd by --genome-annot since kggseq v0.3\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }

        if (find("--pseudogene-anno") >= 0 || find("--dispengene-anno") >= 0 || find("--tfbs-anno") >= 0 || find("--enhancer-anno") >= 0) {
            String infor = "--pseudogene-anno, --dispengene-anno, --tfbs-anno and --enhancer-anno have been coverd by --genome-annot since kggseq v0.3\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }

        if (find("--ibs-check") >= 0) {
            String infor = "--ibs-check has been replaced with --ibs-case-filter since kggseq v0.4\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }
        if (find("--homozygosity-check") >= 0) {
            String infor = "--homozygosity-check has been replaced with --homozygosity-case-filter since kggseq v0.4\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }
        if (find("--double-hit-gene-filter") >= 0) {
            String infor = "--double-hit-gene-filter has been replaced with --double-hit-gene-trio-filter or --double-hit-gene-phased-filter since kggseq v0.4\n"
                    + "Please read the new commands of kggseq\n"
                    + "at http://statgenpro.psychiatry.hku.hk/limx/kggseq/doc/UserManual.html";
            throw new Exception(infor);
        }

//--------------------------------Input/Output formats
        if ((id = find("--assoc-file")) >= 0) {
            inputFormat = "--assoc-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');

            if ((id = find("--v-assoc-file")) >= 0) {
                inputFormatSupp = "--v-assoc-file";
                inputFileNameSupp = options[id + 1];
                param.append(inputFormatSupp);
                param.append(' ');
                param.append(inputFileNameSupp);
                param.append('\n');
            } else if ((id = find("--glm-file")) >= 0) {
                inputFormatSupp = "--glm-file";
                inputFileNameSupp = options[id + 1];
                param.append(inputFormatSupp);
                param.append(' ');
                param.append(inputFileNameSupp);
                param.append('\n');
            }
        } else if ((id = find("--vaast-simple-file")) >= 0) {
            inputFormat = "--vaast-simple-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--vcf-file")) >= 0) {
            inputFormat = "--vcf-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--ked-file")) >= 0) {
            inputFormat = "--ked-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--soap-file")) >= 0) {
            inputFormat = "--soap-file";
            if (true) {
                String infor = "kggseq v0.3+ does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--pileup-file")) >= 0) {
            inputFormat = "--pileup-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--annovar-file")) >= 0) {
            inputFormat = "--annovar-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--annovar-file")) >= 0) {
            inputFormat = "--annovar-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--v-assoc-file")) >= 0) {
            inputFormat = "--v-assoc-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--glm-file")) >= 0) {
            inputFormat = "--glm-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--simple-file")) >= 0) {
            inputFormat = "--simple-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--no-gty-vcf-file")) >= 0 || (id = find("--vcf-file")) >= 0) {  //force no gty file
            inputFormat = "--no-gty-vcf-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else if ((id = find("--tmp-file")) >= 0) {
            inputFormat = "--tmp-file";
            inputFileName = options[id + 1];
            param.append(inputFormat);
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');
        } else {
            // String infor = "No supported input format of variants and/or genotypes!";
            // throw new Exception(infor);
        }

        if (inputFormat != null && (inputFormat.equals("--vcf-file")
                || inputFormat.equals("--soap-file")
                || inputFormat.equals("--pileup-file") || inputFormat.equals("--simple-file"))) {
            id = find("--ped-file");
            if (id >= 0) {
                pedFile = options[id + 1];
                param.append("--ped-file");
                param.append(' ');
                param.append(pedFile);
                param.append('\n');
                id = find("--composite-subject-id");
                if (id >= 0) {
                    useCompositeSubjectID = true;
                    param.append("--composite-subject-id");
                    param.append('\n');
                }
                BufferedReader br = new BufferedReader(new FileReader(pedFile));
                String line = br.readLine();
                if (line.startsWith("#")) {
                    String name;
                    phenotypeColID = new HashMap<String, Integer>();
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    //ingore the firs 4
                    for (int i = 0; i < 4; i++) {
                        tokenizer.nextToken();
                    }
                    id = 0;
                    while (tokenizer.hasMoreTokens()) {
                        name = tokenizer.nextToken();
                        if (phenotypeColID.containsKey(name)) {
                            String infor = "In the pedigree file, there are duplicate phenotype names, " + name;
                            throw new Exception(infor);
                        }
                        phenotypeColID.put(name, id);
                        id++;
                    }
                }
                br.close();
            } else {
                // String infor = "Please sepecify the option \' --indiv-pheno NormalIndiv1:1,Patient2:2,Tome:0 \'";
                //  throw new Exception(infor);
                if (find("--make-filter") < 0) {
                    id = find("--indiv-pheno");
                    if (id >= 0) {
                        indivPhenos = options[id + 1].split(",");
                        param.append("--indiv-pheno");
                        param.append(' ');
                        param.append(options[id + 1]);
                        param.append('\n');
                        for (String indivPheo : indivPhenos) {
                            if (!Pattern.compile("^\\S+:[012]").matcher(indivPheo).find()) {
                                String infor = "The option \'--indiv-pheno\' has incorrect value \'" + indivPheo + "\'; the correct format will be like \'NormalIndiv1:1,Patient1:2,UnknownIndiv1:0 \'";
                                throw new Exception(infor);
                            }
                        }
                    } else {
                        //String infor = "You need specify the phenotypes by the argument \'--ped-file path/to/file\'  or  \'--indiv-pheno NormalIndiv1:1,Patient1:2,UnknownIndiv1:0 \'";
                        //infor = "Warning!!! You do NOT specify the phenotypes by the argument \'--ped-file path/to/file\'  or  \'--indiv-pheno NormalIndiv1:1,Patient1:2,UnknownIndiv1:0 \'. \nSome functions may not work due to lack of phenotype information!";
                        //System.err.println(infor);
                        //throw new Exception(infor);
                    }
                }
            }
        }
        if (inputFormat == null) {
            String infor = "Please specify input variant file by correct options (e.g., --vcf-file or --annovar-file)!";
            throw new Exception(infor);
        }

//a specail tag for somatic mutation identification 
        id = find("--indiv-pair");
        if (id >= 0) {
            indivPairs = options[id + 1].split(",");
            param.append("--indiv-pair");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');
            for (String indivPheo : indivPairs) {
                if (indivPheo.indexOf(":") < 0) {
                    String infor = "The option \'--indiv-pair\' has incorrect value \'" + indivPheo + "\'; the correct format will be like \'--indiv-pair tumorIndivID1:normalIndivID1,tumorIndivID2:normalIndivID2 \'";
                    throw new Exception(infor);
                }
            }
        }
        //----------------------------Output                
        id = find("--out");
        if (id >= 0) {
            outputFileName = options[id + 1];
            param.append("--out");
            param.append(' ');
            param.append(outputFileName);
            param.append('\n');
        } else {
            outputFileName = "./cepip1";
            String infor = "To specify path and prefix name of outputs, add the '--out your/path' option; it is " + outputFileName + " by default";
            //throw new Exception(infor);
            System.out.println(infor);
        }

        id = find("--no-gz");
        if (id >= 0) {
            outGZ = false;
            param.append("--no-gz");
            param.append('\n');
        }

        id = find("--excel");
        if (id >= 0) {
            excelOut = true;
            param.append("--excel");
            param.append('\n');
        } else {
            String infor = "To make output in excel format, add the '--excel' option; it is in text format by default";
            //throw new Exception(infor);
            //System.out.println(infor);
        }

        id = find("--o-polyphen");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--o-polyphen does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            isPolyphen2Out = true;
            param.append("--o-polyphen");
            param.append('\n');
        }
        id = find("--o-seattleseq");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--o-seattleseq does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            isSeattleSeqOut = true;
            param.append("--o-seattleseq");
            param.append('\n');
        }

        id = find("--o-annovar");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--o-annovar does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            isANNOVAROut = true;
            param.append("--o-annovar");
            param.append('\n');
        }

        id = find("--o-vcf");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--o-vcf\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            isVCFOut = true;
            param.append("--o-vcf");
            param.append('\n');
            missingGty = "./.";
            needGtyQual = true;
        }
        id = find("--o-vcf-filtered");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--o-vcf-filtered\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            isVCFOutFilterd = true;
            param.append("--o-vcf-filtered");
            param.append('\n');
            missingGty = "./.";
        }
        id = find("--missing-gty");
        if (id >= 0) {
            missingGty = options[id + 1];
            param.append("--missing-gty ");
            param.append(missingGty);
            param.append('\n');
        }
        id = find("--o-plink-ped");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--ked-file")) {
                String infor = "The \'--o-plink\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            isPlinkPedOut = true;
            param.append("--o-plink-ped");
            param.append('\n');
            needGtys = true;
        }

        id = find("--o-plink-bed");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--o-plink\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            isPlinkBedOut = true;
            param.append("--o-plink-bed");
            param.append('\n');
            needGtys = true;
        }

        id = find("--o-ked");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--ked-file")) {
                String infor = "The \'--o-ked\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            isBinaryGtyOut = true;
            param.append("--o-ked");
            param.append('\n');
            needGtys = true;
        }

        id = find("--o-gene-grp");
        if (id >= 0) {
            isGeneVarGroupFileOut = true;
            param.append("--o-gene-grp");
            param.append('\n');
        }

        id = find("--o-flanking-seq");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--o-flanking-seq does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            flankingSequence = Integer.parseInt(options[id + 1]);
            param.append("--o-flanking-seq");
            param.append(' ');
            param.append(flankingSequence);
            param.append('\n');
        }

        id = find("--no-qc");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--ignore-snv does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            noQC = true;
            param.append("--no-qc");
            param.append('\n');
        } else {
            String infor = "To ignore QCat variants, use the \'--no-qc \' option";
            // System.out.println(infor);
        }

        if (noQC) {
            hwdPControl = 0;
            hwdPCase = 0;
            hwdPAll = 0;

            seqQual = 0;
            minMappingQuality = 0;
            maxStandBias = -1;
            maxFisherStandBias = 0;

            gtyQual = 0;
            gtySecPL = 0;
            gtyBestGP = 0f;
            minGtySeqDP = 0;
            maxGtyAlleleNum = 10;
            maxAltAlleleFracRefHom = 1;
            minAltAlleleFractHet = 0;
            minAltAlleleFractAltHom = 0;
            somatReadsP = 1;
            vcfFilterLabelsIn = null;

            /*
             minHetA = -9;
             minHomA = -9;
             minHetU = -9;
             minHomU = -9;
             minOBSA = -9;
             minOBSU = -9;
             minOBS = -9;
             * 
             */
        } else {
            id = find("--hwe-control");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")) {
                    String infor = "The \'--hwe-control\' now only supports VCF inputs specified by \'--vcf-file\' or \'--pileup-file\'!";
                    throw new Exception(infor);
                }
                hwdPControl = Double.parseDouble(options[id + 1]);
                param.append("--hwe-control");
                param.append(' ');
                param.append(hwdPControl);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--hwe-case");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")) {
                    String infor = "The \'--hwe-case\' now only supports VCF inputs specified by \'--vcf-file\' or \'--pileup-file\'!";
                    throw new Exception(infor);
                }
                hwdPCase = Double.parseDouble(options[id + 1]);
                param.append("--hwe-case");
                param.append(' ');
                param.append(hwdPCase);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--hwe-all");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")) {
                    String infor = "The \'--hwe-all\' now only supports VCF inputs specified by \'--vcf-file\' or \'--pileup-file\'!";
                    throw new Exception(infor);
                }
                hwdPAll = Double.parseDouble(options[id + 1]);
                param.append("--hwe-all");
                param.append(' ');
                param.append(hwdPAll);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

//----------------Quality Control--------------------------
            //genotypes and sequence quality information
            id = find("--seq-qual");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")
                        && !inputFormat.equals("--soap-file") && !inputFormat.equals("--no-genotype-vcf-file")) {
                    String infor = "The \'--seq-qual\' now only supports VCF inputs specified by \'--vcf-file\' or \'--pileup-file\' or \'--soap-file\' or \'--no-genotype-vcf-file\'";
                    throw new Exception(infor);
                }
                seqQual = Double.parseDouble(options[id + 1]);
                param.append("--seq-qual");
                param.append(' ');
                param.append(seqQual);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--seq-mq");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")
                        && !inputFormat.equals("--soap-file") && !inputFormat.equals("--no-genotype-vcf-file")) {
                    String infor = "The \'--seq-mq\' now only supports VCF inputs specified by \'--vcf-file\' or \'--pileup-file\' or \'--soap-file\' or \'--no-genotype-vcf-file\'";
                    throw new Exception(infor);
                }
                minMappingQuality = Double.parseDouble(options[id + 1]);
                param.append("--seq-mq");
                param.append(' ');
                param.append(minMappingQuality);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--seq-sb");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")
                        && !inputFormat.equals("--soap-file") && !inputFormat.equals("--no-genotype-vcf-file")) {
                    String infor = "The \'--seq-sb\' now only supports VCF inputs specified by \'--vcf-file\' or \'--pileup-file\' or \'--soap-file\' or \'--no-genotype-vcf-file\'";
                    throw new Exception(infor);
                }
                maxStandBias = Double.parseDouble(options[id + 1]);
                param.append("--seq-sb");
                param.append(' ');
                param.append(maxStandBias);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--seq-fs");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")
                        && !inputFormat.equals("--soap-file") && !inputFormat.equals("--no-genotype-vcf-file")) {
                    String infor = "The \'--seq-fs\' now only supports VCF inputs specified by \'--vcf-file\' or \'--pileup-file\' or \'--soap-file\' or \'--no-genotype-vcf-file\'";
                    throw new Exception(infor);
                }
                maxFisherStandBias = Double.parseDouble(options[id + 1]);
                param.append("--seq-fs");
                param.append(' ');
                param.append(maxFisherStandBias);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--max-allele");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--pileup-file")
                        && !inputFormat.equals("--soap-file") && !inputFormat.equals("--no-genotype-vcf-file") && !inputFormat.equals("--ked-file")) {
                    String infor = "The \'--max-allele\' now only supports VCF inputs specified by \'--vcf-file\'. ";
                    throw new Exception(infor);
                }
                maxGtyAlleleNum = Integer.parseInt(options[id + 1]);
                param.append("--max-allele");
                param.append(' ');
                param.append(maxGtyAlleleNum);
                param.append('\n');
            } else {
                //String infor = "To set minimal general sequence quality, use the \'--seq-qual PhredQualityScore \' option; it is --seq-qual " + seqQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--gty-qual");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-qual\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                gtyQual = Double.parseDouble(options[id + 1]);
                param.append("--gty-qual");
                param.append(' ');
                param.append(gtyQual);
                param.append('\n');
            } else {
                //String infor = "To set minimal average sequence quality, use the \'--gty-qual PhredQualityScore \' option; it is --seq-qual " + gtyQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--gty-sec-pl");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-sec-pl\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                gtySecPL = Integer.parseInt(options[id + 1]);
                param.append("--gty-sec-pl");
                param.append(' ');
                param.append(gtySecPL);
                param.append('\n');
            } else {
                //String infor = "To set minimal average sequence quality, use the \'--gty-qual PhredQualityScore \' option; it is --seq-qual " + gtyQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--gty-gp");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-gp\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                gtyBestGP = Util.parseFloat(options[id + 1]);
                param.append("--gty-gp");
                param.append(' ');
                param.append(gtyBestGP);
                param.append('\n');
            } else {
                //String infor = "To set minimal average sequence quality, use the \'--gty-qual PhredQualityScore \' option; it is --seq-qual " + gtyQual + " by default.";
                //System.out.println(infor);
            }

            id = find("--gty-dp");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-qual\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                minGtySeqDP = (int) Double.parseDouble(options[id + 1]);
                param.append("--gty-dp");
                param.append(' ');
                param.append(minGtySeqDP);
                param.append('\n');
            } else {
                // String infor = "To set minimal sequence depth at a site, use the \'--gty-dp number \' option; it is --gty-dp " + minGtySeqDP + " by default.";
                // System.out.println(infor);
            }

            id = find("--gty-af-ref");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-af-ref\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                maxAltAlleleFracRefHom = Util.parseFloat(options[id + 1]);
                param.append("--gty-af-ref");
                param.append(' ');
                param.append(maxAltAlleleFracRefHom);
                param.append('\n');
            } else {
                //String infor = "To the maximal fractions of reads supporting each reported alternative allele in reference-allele homozygous genotype, use the \'--gty-af-ref number \' option; it is --gty-af-ref " + maxAltAlleleFracHomRef + " by default.";
                //System.out.println(infor);
            }

            id = find("--gty-af-het");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-af-het\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }

                minAltAlleleFractHet = Util.parseFloat(options[id + 1]);
                param.append("--gty-af-het");
                param.append(' ');
                param.append(minAltAlleleFractHet);
                param.append('\n');
            } else {
                // String infor = "To the minimal fractions of reads supporting each reported alternative allele in all non-reference-allele homozygous or heterozygous genotypes, use the \'--gty-af-alt number \' option; it is --gty-af-alt " + minAltAlleleFractNonHomRef + " by default.";
                // System.out.println(infor);
            }

            id = find("--gty-af-alt");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-af-alt\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                minAltAlleleFractAltHom = Util.parseFloat(options[id + 1]);
                param.append("--gty-af-alt");
                param.append(' ');
                param.append(minAltAlleleFractAltHom);
                param.append('\n');
            } else {
                // String infor = "To the minimal fractions of reads supporting each reported alternative allele in all non-reference-allele homozygous or heterozygous genotypes, use the \'--gty-af-alt number \' option; it is --gty-af-alt " + minAltAlleleFractNonHomRef + " by default.";
                // System.out.println(infor);
            }

            id = find("--gty-somat-p");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--gty-somat-p\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                somatReadsP = Util.parseFloat(options[id + 1]);
                param.append("--gty-somat-p");
                param.append(' ');
                param.append(options[id + 1]);
                param.append('\n');
            } else {
                // String infor = "To the minimal fractions of reads supporting each reported alternative allele in all non-reference-allele homozygous or heterozygous genotypes, use the \'--gty-af-alt number \' option; it is --gty-af-alt " + minAltAlleleFractNonHomRef + " by default.";
                // System.out.println(infor);
            }

            id = find("--vcf-filter-in");
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--vcf-filter-in\' now only supports VCF inputs specified by \'--vcf-file\'";
                    throw new Exception(infor);
                }
                String geneSymbols = options[id + 1];
                StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
                param.append("--vcf-filter-in");
                vcfFilterLabelsIn = new HashSet<String>();
                boolean hasPrint1 = false;
                while (st.hasMoreTokens()) {
                    String fLabel = st.nextToken().toString();
                    if (!vcfFilterLabelsIn.contains(fLabel)) {
                        if (!hasPrint1) {
                            param.append(' ');
                            hasPrint1 = true;
                        } else {
                            param.append(',');
                        }
                        param.append(fLabel);
                        vcfFilterLabelsIn.add(fLabel);
                    }
                }
                param.append('\n');
            }

        }

        id = find("--min-heta");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file")) {
                String infor = "The \'--min-heta\' now only supports inputs specified by \'--vcf-file\' or \'--v-assoc-file\'";
                if (inputFormatSupp != null) {
                    if (!inputFormatSupp.equals("--v-assoc-file")) {
                        throw new Exception(infor);
                    }
                } else {
                    throw new Exception(infor);
                }
            }
            minHetA = (int) Double.parseDouble(options[id + 1]);
            param.append("--min-heta");
            param.append(' ');
            param.append(minHetA);
            param.append('\n');
        } else {
            //String infor = "To set minimal number of heterozygous genotypes in cases, use \'--min-heta\' option; it is --min-heta " + minHetA + " by default.";
            //System.out.println(infor);
        }

        id = find("--min-homa");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file")) {
                String infor = "The \'--min-homa\' now only supports inputs specified by \'--vcf-file\' or \'--v-assoc-file\'";
                if (inputFormatSupp != null) {
                    if (!inputFormatSupp.equals("--v-assoc-file")) {
                        throw new Exception(infor);
                    }
                } else {
                    throw new Exception(infor);
                }
            }
            minHomA = (int) Double.parseDouble(options[id + 1]);
            param.append("--min-homa");
            param.append(' ');
            param.append(minHomA);
            param.append('\n');
        } else {
            //String infor = "To set minimal number of alternative homozygous genotypes in cases, use \'--min-heta\' option; it is --min-homa " + minHomA + " by default.";
            //System.out.println(infor);
        }

        id = find("--min-hetu");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file")) {
                String infor = "The \'--min-hetu\' now only supports inputs specified by \'--vcf-file\' or \'--v-assoc-file\'";
                if (inputFormatSupp != null) {
                    if (!inputFormatSupp.equals("--v-assoc-file")) {
                        throw new Exception(infor);
                    }
                } else {
                    throw new Exception(infor);
                }
            }
            minHetU = (int) Double.parseDouble(options[id + 1]);
            param.append("--min-hetu");
            param.append(' ');
            param.append(minHetU);
            param.append('\n');
        } else {
            //String infor = "To set minimal number of heterozygous genotypes in controls, use \'--min-hetu\' option; it is --min-heta " + minHetU + " by default.";
            //System.out.println(infor);
        }

        id = find("--min-homu");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file")) {
                String infor = "The \'--min-homu\' now only supports inputs specified by \'--vcf-file\' or \'--v-assoc-file\'";
                if (inputFormatSupp != null) {
                    if (!inputFormatSupp.equals("--v-assoc-file")) {
                        throw new Exception(infor);
                    }
                } else {
                    throw new Exception(infor);
                }
            }
            minHomU = (int) Double.parseDouble(options[id + 1]);
            param.append("--min-homu");
            param.append(' ');
            param.append(minHomU);
            param.append('\n');
        } else {
            //String infor = "To set minimal number of alternative homozygous genotypes in controls, use \'--min-heta\' option; it is --min-homu " + minHomU + " by default.";
            //System.out.println(infor);
        }

        id = find("--min-obsa");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file") && !inputFormat.equals("--ked-file")) {
                String infor = "The \'--min-obsa\' now only supports inputs specified by \'--vcf-file\' or \'--v-assoc-file\'";
                if (inputFormatSupp != null) {
                    if (!inputFormatSupp.equals("--v-assoc-file")) {
                        throw new Exception(infor);
                    }
                } else {
                    throw new Exception(infor);
                }
            }
            minOBSA = (int) Double.parseDouble(options[id + 1]);
            param.append("--min-obsa");
            param.append(' ');
            param.append(minOBSA);
            param.append('\n');
        } else {
            //String infor = "To set minimal number of non-null genotypes in cases, use \'--min-obsa\' option; it is --min-obsa " + minOBSA + " by default.";
            //System.out.println(infor);
        }
        id = find("--min-obsu");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file")) {
                String infor = "The \'--min-obsu\' now only supports inputs specified by \'--vcf-file\' or \'--v-assoc-file\'";
                if (inputFormatSupp != null) {
                    if (!inputFormatSupp.equals("--v-assoc-file")) {
                        throw new Exception(infor);
                    }
                } else {
                    throw new Exception(infor);
                }
            }
            minOBSU = (int) Double.parseDouble(options[id + 1]);
            param.append("--min-obsu");
            param.append(' ');
            param.append(minOBSU);
            param.append('\n');
        } else {
            //String infor = "To set minimal number of non-null genotypes in controls, use \'--min-obsu\' option; it is --min-obsu " + minOBSU + " by default.";
            //System.out.println(infor);
        }

        id = find("--min-obs");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file")) {
                String infor = "The \'--min-obs\' now only supports inputs specified by \'--vcf-file\' or \'--v-assoc-file\'";
                if (inputFormatSupp != null) {
                    if (!inputFormatSupp.equals("--v-assoc-file")) {
                        throw new Exception(infor);
                    }
                } else {
                    throw new Exception(infor);
                }
            }
            minOBS = (int) Double.parseDouble(options[id + 1]);
            param.append("--min-obs");
            param.append(' ');
            param.append(minOBS);
            param.append('\n');
        } else {
            //String infor = "To set minimal number of non-null genotypes in cases and controls, use \'--min-obsu\' option; it is --min-obs " + minOBS + " by default.";
            //System.out.println(infor);
        }

        id = find("--filter-sample-maf-le");
        if (id >= 0) {
            sampleMafOver = Util.parseFloat(options[id + 1]);
            param.append("--filter-sample-maf-le");
            param.append(' ');
            param.append(Util.doubleToString(sampleMafOver, 5));
            param.append('\n');
        }

        id = find("--filter-sample-maf-oe");
        if (id >= 0) {
            sampleMafLess = Util.parseFloat(options[id + 1]);
            param.append("--filter-sample-maf-oe");
            param.append(' ');
            param.append(Util.doubleToString(sampleMafLess, 5));
            param.append('\n');
        }

        //------------------------------genetic checking and scanning
        id = find("--genotype-filter");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--ked-file")) {
                String infor = "The \'--genotype-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            sampleGtyHardFilterCode = options[id + 1];
            param.append("--genotype-filter");
            param.append(' ');
            param.append(sampleGtyHardFilterCode);
            param.append('\n');
        } else {
            //String infor = "To do straightforward filtering by genotypes according to inheritance pattern, please specify \'--genotype-filter [numbers]'";
            //System.out.println(infor);
        }

        id = find("--ibs-case-filter");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--ibs-case-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            ibsCheckCase = Integer.parseInt(options[id + 1]);
            param.append("--ibs-case-filter ");
            param.append(ibsCheckCase);
            param.append('\n');
        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }
        id = find("--double-hit-gene-trio-filter");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--double-hit-gene-trio-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            if (find("--db-gene") < 0) {
                String infor = "The \'--double-hit-gene-trio-filter\' requires reference gene database specified by \'--db-gene\'";
                throw new Exception(infor);
            }

            doubleHitGeneTriosFilter = true;
            param.append("--double-hit-gene-trio-filter");
            param.append('\n');
        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--all-snv");
        if (id >= 0) {
            onlyPositive = false;
            param.append("--all-snv");
            param.append('\n');
        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--count-gene-var");
        if (id >= 0) {
            /*
             if (!inputFormat.equals("--vcf-file")) {
             String infor = "The \'--count-gene-var\' now only supports VCF inputs specified by \'--vcf-file\'";
             throw new Exception(infor);
             }
             * 
             */
            if (find("--db-gene") < 0) {
                String infor = "The \'--count-gene-var\' requires reference gene database specified by \'--db-gene\'";
                throw new Exception(infor);
            }
            countGeneVar = true;
            param.append("--count-gene-var");
            param.append('\n');

        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--gene-tol-score");
        if (id >= 0) {
            /*
             if (!inputFormat.equals("--vcf-file")) {
             String infor = "The \'--count-gene-var\' now only supports VCF inputs specified by \'--vcf-file\'";
             throw new Exception(infor);
             }
             * 
             */
            if (find("--db-gene") < 0) {
                String infor = "The \'--gene-tol-score\' requires reference gene database specified by \'--db-gene\'";
                throw new Exception(infor);
            }
            geneToleranceScore = true;
            param.append("--gene-tol-score");
            param.append('\n');

        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--gene-var-filter");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--gene-var-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            if (find("--db-gene") < 0) {
                String infor = "The \'--gene-var-filter\' requires reference gene database specified by \'--db-gene\'";
                throw new Exception(infor);
            }
            geneVarFilter = Integer.parseInt(options[id + 1]);
            param.append("--gene-var-filter ");
            param.append(geneVarFilter);
            param.append('\n');
        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--double-hit-gene-trio-filter-all");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--double-hit-gene-trio-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            if (find("--db-gene") < 0) {
                String infor = "The \'--double-hit-gene-trio-filter\' requires reference gene database specified by \'--db-gene\'";
                throw new Exception(infor);
            }

            doubleHitGeneTriosFilter = true;
            param.append("--double-hit-gene-trio-filter-all");
            param.append('\n');

            //this is for testing a new function only
            countAllPsudoControl = true;

        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }
        id = find("--double-hit-gene-phased-filter");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--double-hit-gene-phased-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }

            if (find("--db-gene") < 0) {
                String infor = "The \'--double-hit-gene-phased-filter\' requires reference gene database specified by \'--db-gene\'";
                throw new Exception(infor);
            }
            doubleHitGenePhasedFilter = true;
            param.append("--double-hit-gene-phased-filter");
            param.append('\n');
        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--ignore-homo");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--ignore-homo\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            noHomo = true;
            param.append("--ignore-homo");
            param.append('\n');
        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--unique-gene-filter");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--unique-gene-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            overlappedGeneFilter = true;
            param.append("--unique-gene-filter");
            param.append('\n');
        } else {
            //String infor = "To check longest ibs region around each variant, use the \'--ibs-check \' option";
            //System.out.println(infor);
        }

        id = find("--homozygosity-case-filter");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "The \'--homozygosity-case-filter\' now only supports VCF inputs specified by \'--vcf-file\'";
                throw new Exception(infor);
            }
            homozygousRegionCase = Integer.parseInt(options[id + 1]);
            param.append("--homozygosity-case-filter ");
            param.append(homozygousRegionCase);
            param.append('\n');
        } else {
            //String infor = "To check longest homozygosity region around each variant in cases, use the \'--homozygosity-check \' option";
            //System.out.println(infor);
        }
        id = find("--ibd-annot");
        if (id >= 0) {
            ibdFileName = options[id + 1];
            param.append("--ibd-annot");
            param.append(' ');
            param.append(ibdFileName);
            param.append('\n');
        }

        //------------------------------Gene feature filtering-------------------
        id = find("--db-gene");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--db-gene does not support inputs specified by " + inputFormat;
                //throw new Exception(infor);
            }
            geneDBLabels = options[id + 1].split(",");

            for (String geneDBLabel : geneDBLabels) {
                if (!PUBDB_FILE_MAP.containsKey(geneDBLabel)) {
                    String infor = "Invalid gene database name " + geneDBLabel + " at  --db-gene option! The valid name is refgene.";
                    throw new Exception(infor);
                }
                /*
                 if (geneDBLabel.equals("gencode") && refGenomeVersion.equals("hg18")) {
                 String infor = "The database \'gencode\' specified by \'--db-gene\' is NOT supported for hg18!";
                 throw new Exception(infor);
                 }
                 * 
                 */
            }

            param.append("--db-gene");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');

            id = find("--gene-feature-in");
            if (id >= 0) {
                String geneSymbols = options[id + 1];
                StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
                param.append("--gene-feature-in");
                boolean hasPrint1 = false;
                while (st.hasMoreTokens()) {
                    byte geneFeature = Byte.parseByte(st.nextToken());
                    if (geneFeature >= VAR_FEATURE_NAMES.length) {
                        String infor = "Invalid gene feature value " + geneFeature + " at --gene-feature-in option";
                        throw new Exception(infor);
                    } else {
                        if (!geneFeatureIn.contains(geneFeature)) {
                            if (!hasPrint1) {
                                param.append(' ');
                                hasPrint1 = true;
                            } else {
                                param.append(',');
                            }
                            param.append(geneFeature);
                            geneFeatureIn.add(geneFeature);
                        }
                    }
                }
                param.append('\n');
            } else {
                //otherwise consider all
                for (byte i = 0; i < VAR_FEATURE_NAMES.length; i++) {
                    geneFeatureIn.add(i);
                }
            }

            id = find("--dependent-gene-feature");
            if (id >= 0) {
                String geneSymbols = options[id + 1];
                StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
                param.append("--dependent-gene-feature");
                boolean hasPrint1 = false;
                while (st.hasMoreTokens()) {
                    byte geneFeature = Byte.parseByte(st.nextToken());
                    if (geneFeature >= VAR_FEATURE_NAMES.length) {
                        String infor = "Invalid gene feature value " + geneFeature + " at --dependent-gene-feature option";
                        throw new Exception(infor);
                    } else {
                        if (!dependentGeneFeature.contains(geneFeature)) {
                            if (!hasPrint1) {
                                param.append(' ');
                                hasPrint1 = true;
                            } else {
                                param.append(',');
                            }
                            param.append(geneFeature);
                            dependentGeneFeature.add(geneFeature);
                        }
                    }
                }
                param.append('\n');
            } else {
                //otherwise consider all non-synonemous
                for (byte i = 0; i < 7; i++) {
                    dependentGeneFeature.add(i);
                }
            }

            id = find("--independent-gene-feature");
            if (id >= 0) {
                String geneSymbols = options[id + 1];
                StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
                param.append("--independent-gene-feature");
                boolean hasPrint1 = false;
                while (st.hasMoreTokens()) {
                    byte geneFeature = Byte.parseByte(st.nextToken());
                    if (geneFeature >= VAR_FEATURE_NAMES.length) {
                        String infor = "Invalid gene feature value " + geneFeature + " at --independent-gene-feature option";
                        throw new Exception(infor);
                    } else {
                        if (!independentGeneFeature.contains(geneFeature)) {
                            if (!hasPrint1) {
                                param.append(' ');
                                hasPrint1 = true;
                            } else {
                                param.append(',');
                            }
                            param.append(geneFeature);
                            independentGeneFeature.add(geneFeature);
                        }
                    }
                }
                param.append('\n');
            } else {
                //otherwise consider  -synonemous
                independentGeneFeature.add((byte) 7);
            }

            id = find("--splicing");
            if (id >= 0) {
                splicingDis = (int) Double.parseDouble(options[id + 1]);
                param.append("--splicing");
                param.append(' ');
                param.append(splicingDis);
                param.append('\n');
            }

            id = find("--neargene");
            if (id >= 0) {
                neargeneDis = (int) Double.parseDouble(options[id + 1]);
                param.append("--neargene");
                param.append(' ');
                param.append(neargeneDis);
                param.append('\n');
            }

        } else {
            // String infor = "To annotate and filter variants by gene information,sepecify the \'--db-gene refgene\' option .";
            // System.out.println(infor);
        }

        //-----------Allele frequency trimming
        id = find("--db-filter");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") && (inputFormatSupp != null && !inputFormatSupp.equals("--v-assoc-file") && !inputFormatSupp.equals("--glm-file")) || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--db-filter does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            String dbNames = options[id + 1];
            StringTokenizer st = new StringTokenizer(dbNames.trim(), ",");
            param.append("--db-filter");
            boolean hasPrint1 = false;
            Set<String> obsoleteLabels = new HashSet<String>();
            obsoleteLabels.add("1kg201011");
            obsoleteLabels.add("1kg201105");
            obsoleteLabels.add("1kg201202");

            int index = 0;
            while (st.hasMoreTokens()) {
                String dbLabel = st.nextToken().toString();
                index = dbLabel.indexOf('_');
                if (index >= 0) {
                    dbLabel = dbLabel.substring(index + 1);
                }
                if (obsoleteLabels.contains(dbLabel)) {
                    String infor = "Sorry, the referece sequence variant database \'" + dbLabel + "\' is not in use anymore at --db-filter option";
                    throw new Exception(infor);
                }
                String dbFileName = PUBDB_FILE_MAP.get(dbLabel);

                if (dbFileName == null) {
                    String infor = "Invalid sequence variant database name " + dbLabel + " at  --db-filter option";
                    throw new Exception(infor);
                } else {
                    if (!hasPrint1) {
                        param.append(' ');
                        hasPrint1 = true;
                    } else {
                        param.append(',');
                    }
                    param.append(dbLabel);
                    varaintDBLableList.add(dbLabel);
                }
            }
            param.append('\n');
            if (index >= 0) {
                System.out.println("The reference genome version (hgX_) is no longer needed for --db-filter since kggseq v0.5!");
            }
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --db-filter dbsnp130,1kgasn2010,1kgceu2010,1kgafr2009,...\'";
            //System.out.println(infor);
        }

        id = find("--db-filter-hard");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") && (inputFormatSupp != null && !inputFormatSupp.equals("--v-assoc-file") && !inputFormatSupp.equals("--glm-file")) || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--db-filter-hard does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            String dbNames = options[id + 1];
            StringTokenizer st = new StringTokenizer(dbNames.trim(), ",");
            param.append("--db-filter-hard");
            boolean hasPrint1 = false;
            Set<String> obsoleteLabels = new HashSet<String>();
            obsoleteLabels.add("1kg201011");
            obsoleteLabels.add("1kg201105");
            obsoleteLabels.add("1kg201202");

            int index = 0;
            while (st.hasMoreTokens()) {
                String dbLabel = st.nextToken().toString();
                index = dbLabel.indexOf('_');
                if (index >= 0) {
                    dbLabel = dbLabel.substring(index + 1);
                }
                if (obsoleteLabels.contains(dbLabel)) {
                    String infor = "Sorry, the referece sequence variant database \'" + dbLabel + "\' is not in use anymore at --db-filter-hard option";
                    throw new Exception(infor);
                }
                String dbFileName = PUBDB_FILE_MAP.get(dbLabel);

                if (dbFileName == null) {
                    String infor = "Invalid sequence variant database name " + dbLabel + " at  --db-filter-hard option";
                    throw new Exception(infor);
                } else {
                    if (!hasPrint1) {
                        param.append(' ');
                        hasPrint1 = true;
                    } else {
                        param.append(',');
                    }
                    param.append(dbLabel);
                    varaintDBLableHardList.add(dbLabel);
                }
            }
            param.append('\n');
            if (index >= 0) {
                System.out.println("The reference genome version (hgX_) is no longer needed for --db-filter-hard since kggseq v0.5!");
            }
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --db-filter-hard dbsnp130,1kgasn2010,1kgceu2010,1kgafr2009,...\'";
            //System.out.println(infor);
        }

        //-----------reference genotypes to be merged
        id = find("--db-merge");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") && (inputFormatSupp != null && !inputFormatSupp.equals("--v-assoc-file") && !inputFormatSupp.equals("--glm-file")) || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--db-merge does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }

            if (!isPlinkPedOut && !isPlinkBedOut) {
                String infor = "--db-merge should be used together with --o-plink-bed or --o-plink-ped";
                throw new Exception(infor);
            }

            mergeGtyDb = options[id + 1];
            if (!PUBDB_URL_MAP.containsKey(mergeGtyDb)) {
                String infor = mergeGtyDb + " is an unknown reference genotype dataset!!";
                throw new Exception(infor);
            }
            param.append("--db-merge");
            param.append(' ');
            param.append(mergeGtyDb);
            param.append('\n');
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --db-filter dbsnp130,1kgasn2010,1kgceu2010,1kgafr2009,...\'";
            //System.out.println(infor);
        }

        id = find("--local-filter");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--local-filter does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            localFilterFileNames = options[id + 1].split(",");
            param.append("--local-filter");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --local-filter /path/of/file1,/path/of/file2 \'";
            //System.out.println(infor);
        }

        id = find("--local-filter-hard");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--local-filter-hard does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            localHardFilterFileNames = options[id + 1].split(",");
            param.append("--local-filter-hard");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --local-filter /path/of/file1,/path/of/file2 \'";
            //System.out.println(infor);
        }

        id = find("--local-filter-vcf");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--local-filter-vcf does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            localFilterVCFFileNames = options[id + 1].split(",");
            param.append("--local-filter-vcf");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --local-filter-vcf /path/of/file1,/path/of/file2 \'";
            //System.out.println(infor);
        }

        id = find("--local-filter-vcf-hard");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--local-filter-vcf does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            localHardFilterVCFFileNames = options[id + 1].split(",");
            param.append("--local-filter-vcf-hard");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --local-filter-vcf /path/of/file1,/path/of/file2 \'";
            //System.out.println(infor);
        }

        id = find("--local-filter-no-gty-vcf");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--local-filter-no-gty-vcf does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            localFilterNoGtyVCFFileNames = options[id + 1].split(",");
            param.append("--local-filter-no-gty-vcf");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');

        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --local-filter-no-genotype-vcf /path/of/file1,/path/of/file2 \'";
            //System.out.println(infor);
        }
        id = find("--local-filter-no-gty-vcf-hard");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--local-filter-no-genotype-vcf does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            localHardFilterNoGtyVCFFileNames = options[id + 1].split(",");
            param.append("--local-filter-no-gty-vcf-hard");
            param.append(' ');
            param.append(options[id + 1]);
            param.append('\n');
        } else {
            //String infor = "To filter out variants exsting in avaible databases, sepecify the option \' --local-filter-no-genotype-vcf /path/of/file1,/path/of/file2 \'";
            //System.out.println(infor);
        }

        if (localFilterNoGtyVCFFileNames != null || localFilterVCFFileNames != null || localFilterFileNames != null || !varaintDBLableList.isEmpty()) {
            id = find("--rare-allele-freq");
            if (id >= 0) {
                minAlleleFreqExc = Util.parseFloat(options[id + 1]);
                param.append("--rare-allele-freq");
                param.append(' ');
                param.append(minAlleleFreqExc);
                param.append('\n');
                //to exclude the boundary, a tecknique render              
                needRecordAltFreq = true;
            } else {
                // String infor = "To keep rare variants when filtering; it is \'-allele-freq " + minAlleleFreq + "\' by default";
                //System.out.println(infor);
                id = find("--allele-freq");
                if (id >= 0) {
                    String freqs = options[id + 1];
                    int ind = freqs.indexOf(",");
                    if (ind < 0) {
                        String infor = "Invalid format for --allele-freq!! An example for this is --allele-freq 0.1,0.9 ";
                        throw new Exception(infor);
                    }
                    param.append("--allele-freq");
                    param.append(' ');
                    param.append(freqs);
                    param.append('\n');
                    needRecordAltFreq = true;
                    minAlleleFreqInc = Util.parseFloat(freqs.substring(0, ind));
                    maxAlleleFreqInc = Util.parseFloat(freqs.substring(ind + 1));
                    isAlleleFreqExcMode = false;
                }
            }
        }
//-------------------Phenotype grouping
        id = find("--var-assoc");
        if (id >= 0) {
            if (id >= 0) {
                if (!inputFormat.equals("--vcf-file")) {
                    String infor = "The \'--var-assoc\' now only supports inputs specified by \'--vcf-file\'!";
                    throw new Exception(infor);
                }
            }
            varAssoc = true;
            param.append("--var-assoc");
            param.append('\n');
            id = find("--p-value-cutoff");
            if (id >= 0) {
                pValueThreshold = Double.parseDouble(options[id + 1]);
                param.append("--p-value-cutoff");
                param.append(' ');
                param.append(pValueThreshold);
                param.append('\n');

                id = find("--multiple-testing");
                if (id >= 0) {
                    multipleTestingMethod = options[id + 1];
                    param.append("--multiple-testing");
                    param.append(' ');
                    param.append(multipleTestingMethod);
                    param.append('\n');
                } else {
                    String infor = "To sepecify a multiple testing approach to filter variants, please use \'--multiple-testing <method name>\';"
                            + " it is --multiple-testing " + multipleTestingMethod;
                    System.out.println(infor);
                }

            } else {
                String infor = "To sepecify p-value threshold to filter variants, please use \'--p-value-cutoff <a number between 0 and 1>\';"
                        + " it is --p-value-cutoff " + pValueThreshold;
                // System.out.println(infor);
            }
        }
        id = find("--filter-model");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file") && !inputFormat.equals("--v-assoc-file") && !inputFormat.equals("--glm-file")
                    && !inputFormat.equals("--assoc-file") && !inputFormat.equals("--vaast-simple-file")) {
                String infor = "The \'--filter-model\' now only supports inputs specified by \'--vcf-file\' "
                        + "or \'--v-assoc-file\'or \'--assoc-file\'or \'--vaast-simple-file\'";
                throw new Exception(infor);
            }
            if (sampleVarHardFilterCode != null) {
                String infor = "The \'--genotype-filter\' cannot be specified with \'--filter-model\' at a time!;";
                throw new Exception(infor);
            }
            sampleVarHardFilterCode = options[id + 1];

            param.append("--filter-model");
            param.append(' ');
            param.append(sampleVarHardFilterCode);
            param.append('\n');

            if (sampleVarHardFilterCode.equals("association")) {
                id = find("--p-value-cutoff");
                if (id >= 0) {
                    pValueThreshold = Double.parseDouble(options[id + 1]);
                    param.append("--p-value-cutoff");
                    param.append(' ');
                    param.append(pValueThreshold);
                    param.append('\n');

                    id = find("--multiple-testing");
                    if (id >= 0) {
                        multipleTestingMethod = options[id + 1];
                        param.append("--multiple-testing");
                        param.append(' ');
                        param.append(multipleTestingMethod);
                        param.append('\n');
                    } else {
                        String infor = "To sepecify a multiple testing approach to filter variants, please use \'--multiple-testing <method name>\';"
                                + " it is --multiple-testing " + multipleTestingMethod;
                        System.out.println(infor);
                    }

                } else {
                    String infor = "To sepecify p-value threshold to filter variants, please use \'--p-value-cutoff <a number between 0 and 1>\';"
                            + " it is --p-value-cutoff " + pValueThreshold;
                    // System.out.println(infor);
                }
                if (find("--min-homa") >= 0 || find("--min-homu") >= 0 || find("--min-heta") >= 0 || find("--min-hetu") >= 0) {
                    String infor = "None of the options \'--min-homa\', \'--min-homu\', \'--min-heta\' and \'--min-hetu' are valid "
                            + "for \'--filter-model association\'!";
                    throw new Exception(infor);
                }
            }

        } else {
            //String infor = "Please sepecify filtration model for variants \'--filter-model case-unique or control-unique or association or no\'";
            //throw new Exception(infor);
        }

        //hard filter by genomic regions
        id = find("--regions-in");
        if (id >= 0) {
            String infor = "\nWrong --regions-in format, the correct one is like: --regions-in chr1,chr2:2323-34434,chr2:43455-345555";
            try {
                String regDef = options[id + 1];
                param.append("--regions-in");
                param.append(' ');
                param.append(regDef);
                param.append('\n');
                String[] r1 = regDef.split(",");
                Map<String, int[]> regionsIn = new HashMap<String, int[]>();

                for (int i = 0; i < r1.length; i++) {
                    r1[i] = r1[i].trim();
                    int index = r1[i].indexOf(':');
                    if (index < 0) {
                        String chrom = r1[i].substring(3);
                        regionsIn.put(chrom, new int[]{-9, -9});
                        continue;
                    }
                    String chrom = r1[i].substring(0, r1[i].indexOf(':'));
                    chrom = chrom.substring(3);
                    int[] localRegion = regionsIn.get(chrom);
                    if (localRegion == null) {
                        localRegion = new int[2];
                    } else {
                        int[] tmpLocalRegion = new int[localRegion.length];
                        System.arraycopy(localRegion, 0, tmpLocalRegion, 0, localRegion.length);

                        localRegion = new int[localRegion.length + 2];
                        System.arraycopy(tmpLocalRegion, 0, localRegion, 0, tmpLocalRegion.length);
                    }

                    r1[i] = r1[i].substring(index + 1);
                    index = r1[i].indexOf("-");
                    if (index >= 0) {
                        int pos1 = (int) Double.parseDouble(r1[i].substring(0, index));
                        int pos2 = (int) Double.parseDouble(r1[i].substring(index + 1));
                        localRegion[localRegion.length - 2] = pos1;
                        localRegion[localRegion.length - 1] = pos2;
                        regionsIn.put(chrom, localRegion);
                    }
                }
                regionsInPos = new int[STAND_CHROM_NAMES.length][];

                for (int t = 0; t < STAND_CHROM_NAMES.length; t++) {
                    int[] physicalRegions = regionsIn.get(STAND_CHROM_NAMES[t]);
                    if (physicalRegions == null) {
                        continue;
                    }
                    int regionNum = physicalRegions.length / 2;

                    regionsInPos[t] = physicalRegions;

                    for (int j = 0; j < regionNum; j++) {
                        if (physicalRegions[j * 2] != -9 || physicalRegions[j * 2 + 1] != -9) {
                            if (physicalRegions[j * 2 + 1] == -9) {
                                physicalRegions[1] = Integer.MAX_VALUE;
                            }
                            regionsInStr.append(" chr");
                            regionsInStr.append(STAND_CHROM_NAMES[t]);
                            regionsInStr.append("[");
                            regionsInStr.append(physicalRegions[j * 2]);
                            regionsInStr.append(",");
                            regionsInStr.append(physicalRegions[j * 2 + 1]);
                            regionsInStr.append("]bp ");
                        } else if (physicalRegions[j * 2] == -9 && physicalRegions[j * 2 + 1] == -9) {

                            physicalRegions[1] = Integer.MAX_VALUE;
                            regionsInStr.append("chr").append(STAND_CHROM_NAMES[t]).append(" ");
                        }
                    }
                }

            } catch (Exception ex) {
                throw new Exception(ex.toString() + "\n" + infor);
            }
        }

        id = find("--regions-out");
        if (id >= 0) {
            String infor = "\nWrong --regions-out format, the correct one is like: --regions-out chr1,chr2:2323-34434,chr2:43455-345555";
            try {
                String regDef = options[id + 1];
                param.append("--regions-out");
                param.append(' ');
                param.append(regDef);
                param.append('\n');
                String[] r1 = regDef.split(",");
                Map<String, int[]> regionsOut = new HashMap<String, int[]>();

                for (int i = 0; i < r1.length; i++) {
                    r1[i] = r1[i].trim();
                    int index = r1[i].indexOf(':');
                    if (index < 0) {
                        String chrom = r1[i].substring(3);
                        regionsOut.put(chrom, new int[]{-9, -9});
                        continue;
                    }
                    String chrom = r1[i].substring(0, r1[i].indexOf(':'));
                    chrom = chrom.substring(3);
                    int[] localRegion = regionsOut.get(chrom);
                    if (localRegion == null) {
                        localRegion = new int[2];
                    } else {
                        int[] tmpLocalRegion = new int[localRegion.length];
                        System.arraycopy(localRegion, 0, tmpLocalRegion, 0, localRegion.length);

                        localRegion = new int[localRegion.length + 2];
                        System.arraycopy(tmpLocalRegion, 0, localRegion, 0, tmpLocalRegion.length);
                    }

                    r1[i] = r1[i].substring(index + 1);
                    index = r1[i].indexOf("-");
                    if (index >= 0) {
                        int pos1 = (int) Double.parseDouble(r1[i].substring(0, index));
                        int pos2 = (int) Double.parseDouble(r1[i].substring(index + 1));
                        localRegion[localRegion.length - 2] = pos1;
                        localRegion[localRegion.length - 1] = pos2;
                        regionsOut.put(chrom, localRegion);
                    }
                }

                regionsOutPos = new int[STAND_CHROM_NAMES.length][];

                for (int t = 0; t < STAND_CHROM_NAMES.length; t++) {
                    int[] physicalRegions = regionsOut.get(STAND_CHROM_NAMES[t]);
                    if (physicalRegions == null) {
                        continue;
                    }
                    int regionNum = physicalRegions.length / 2;

                    regionsOutPos[t] = physicalRegions;

                    for (int j = 0; j < regionNum; j++) {
                        if (physicalRegions[j * 2] != -9 || physicalRegions[j * 2 + 1] != -9) {
                            if (physicalRegions[j * 2 + 1] == -9) {
                                physicalRegions[1] = Integer.MAX_VALUE;
                            }
                            regionsOutStr.append(" chr");
                            regionsOutStr.append(STAND_CHROM_NAMES[t]);
                            regionsOutStr.append("[");
                            regionsOutStr.append(physicalRegions[j * 2]);
                            regionsOutStr.append(",");
                            regionsOutStr.append(physicalRegions[j * 2 + 1]);
                            regionsOutStr.append("]bp ");
                        } else if (physicalRegions[j * 2] == -9 && physicalRegions[j * 2 + 1] == -9) {
                            physicalRegions[1] = Integer.MAX_VALUE;
                            regionsOutStr.append("chr").append(STAND_CHROM_NAMES[t]).append(" ");
                        }
                    }
                }

            } catch (Exception ex) {
                throw new Exception(ex.toString() + "\n" + infor);
            }
        }

        id = find("--genes-in");
        if (id >= 0) {
            String geneSymbols = options[id + 1];
            StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
            param.append("--genes-in");
            boolean hasPrint1 = false;
            while (st.hasMoreTokens()) {
                String dbLabel = st.nextToken().toString();
                if (Util.isNumeric(dbLabel)) {
                    String infor = "Invalid gene symbol name " + dbLabel + " at --genes-in option";
                    throw new Exception(infor);
                } else {
                    if (!inGeneSet.contains(dbLabel)) {
                        if (!hasPrint1) {
                            param.append(' ');
                            hasPrint1 = true;
                        } else {
                            param.append(',');
                        }
                        param.append(dbLabel);
                        inGeneSet.add(dbLabel);
                    }
                }
            }
            param.append('\n');
        }

        id = find("--genes-out");
        if (id >= 0) {
            String geneSymbols = options[id + 1];
            StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
            param.append("--genes-out");
            boolean hasPrint1 = false;
            while (st.hasMoreTokens()) {
                String dbLabel = st.nextToken().toString();
                if (Util.isNumeric(dbLabel)) {
                    String infor = "Invalid gene symbol name " + dbLabel + " at --genes-out option";
                    throw new Exception(infor);
                } else {
                    if (!outGeneSet.contains(dbLabel)) {
                        if (!hasPrint1) {
                            param.append(' ');
                            hasPrint1 = true;
                        } else {
                            param.append(',');
                        }
                        param.append(dbLabel);
                        outGeneSet.add(dbLabel);
                    }
                }
            }
            param.append('\n');
        }

        //hard filter by variant types
        id = find("--ignore-indel");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--ignore-indel does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            considerIndel = false;
            param.append("--ignore-indel");
            param.append('\n');
        } else {
            //String infor = "To ignore Indel variants, use the \'--ignore-indel \' option";
            //System.out.println(infor);
        }

        id = find("--ignore-snv");
        if (id >= 0) {
            if (inputFormat.equals("--assoc-file") || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--ignore-snv does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }
            considerSNV = false;
            param.append("--ignore-snv");
            param.append('\n');
        } else {
            String infor = "To ignore single nucleotide variants, use the \'--ignore-snv \' option";
            // System.out.println(infor);
        }
        if (!considerIndel && !considerSNV) {
            String infor = "You cannot specify both --ignore-indel and --ignore-snv at a time!";
            throw new Exception(infor);
        }

        //------------------------Genomic annotation; 
        id = find("--genome-annot");
        if (id >= 0) {
            param.append("---genome-annot");
            param.append('\n');

            pseudoGeneVariants = true;
            dispensableGeneVariants = true;
            isTFBSCheck = true;
            needRecordTfbs = true;
            isEnhancerCheck = true;
            needRecordEnhancer = true;
        }

        //------------------------Genomic annotation; 
        id = find("--omim-annot");
        if (id >= 0) {
            param.append("--omim-annot");
            param.append('\n');
            omimAnnotateGene = true;
        }

        id = find("--scsnv-annot");
        if (id >= 0) {
            param.append("--scsnv-annot");
            param.append('\n');
            dbscSNVAnnote = true;
        }

        id = find("--cosmic-annot");
        if (id >= 0) {
            param.append("--cosmic-annot");
            param.append('\n');
            cosmicAnnotate = true;
        }

        id = find("--dgv-cnv-annot");
        if (id >= 0) {
            param.append("--dgv-cnv-annot");
            param.append('\n');
            dgvcnvAnnotate = true;
        }

        id = find("--superdup-annot");
        if (id >= 0) {
            param.append("--superdup-annot");
            param.append('\n');
            superdupAnnotate = true;
        }

        id = find("--superdup-filter");
        if (id >= 0) {
            param.append("--superdup-filter");
            param.append('\n');
            superdupFilter = true;
        }

//-----------------------Gene network and Pathway
        id = find("--candi-list");
        if (id >= 0) {
            String geneSymbols = options[id + 1];
            StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
            param.append("--candi-list");
            boolean hasPrint1 = false;
            while (st.hasMoreTokens()) {
                String dbLabel = st.nextToken().toString();
                if (Util.isNumeric(dbLabel)) {
                    String infor = "Invalid gene symbol name " + dbLabel + " at  --candi-list option";
                    throw new Exception(infor);
                } else {
                    if (!candidateGeneSet.contains(dbLabel)) {
                        if (!hasPrint1) {
                            param.append(' ');
                            hasPrint1 = true;
                        } else {
                            param.append(',');
                        }
                        param.append(dbLabel);
                        candidateGeneSet.add(dbLabel);
                    }
                }
            }
            param.append('\n');
        } else {
            id = find("--candi-file");
            if (id >= 0) {
                String geneSymbolsFile = options[id + 1];
                LocalFile.retrieveData(geneSymbolsFile, candidateGeneSet);
                param.append("--candi-file");
                param.append(' ');
                param.append(geneSymbolsFile);
                param.append('\n');
            }
        }
        id = find("--ppi-annot");
        if (id >= 0) {
            if (find("--candi-list") < 0 && find("--candi-file") < 0) {
                String infor = "The candidate genes specified by \'--candi-list\' or \'--candi-file\' are required for \'--ppi-annot\'!";
                throw new Exception(infor);
            }
            ppidb = options[id + 1];
            param.append("--ppi-annot");
            param.append(' ');
            param.append(ppidb);
            param.append('\n');
        }

        id = find("--ppi-depth");
        if (id >= 0) {
            ppiDepth = Integer.parseInt(options[id + 1]);
            param.append("--ppi-depth");
            param.append(' ');
            param.append(ppiDepth);
            param.append('\n');
        }

        id = find("--geneset-annot");
        if (id >= 0) {
            if (find("--candi-list") < 0 && find("--candi-file") < 0) {
                String infor = "The candidate genes specified by \'--candi-list\' or \'--candi-file\' are required for \'--geneset-annot\'!";
                throw new Exception(infor);
            }
            genesetdb = options[id + 1];
            param.append("--geneset-annot");
            param.append(' ');
            param.append(genesetdb);
            param.append('\n');
        }

        id = find("--min-geneset");
        if (id >= 0) {
            genesetSizeMin = Integer.parseInt(options[id + 1]);
            param.append("--min-geneset");
            param.append(' ');
            param.append(genesetSizeMin);
            param.append('\n');
        }

        id = find("--max-geneset");
        if (id >= 0) {
            genesetSizeMax = Integer.parseInt(options[id + 1]);
            param.append("--max-geneset");
            param.append(' ');
            param.append(genesetSizeMax);
            param.append('\n');
        }

//---------------------PubMed literature 
        id = find("--pubmed-mining");
        if (id >= 0) {
            pubmedMiningGene = true;
            pubmedMiningIdeo = true;
            param.append("--pubmed-mining");
            param.append('\n');
        } else {
            if ((id = find("--pubmed-mining-gene")) >= 0) {
                pubmedMiningGene = true;
                param.append("--pubmed-mining-gene");
                param.append('\n');
            } else if ((id = find("--pubmed-mining-ideo")) >= 0) {
                pubmedMiningIdeo = true;
                param.append("--pubmed-mining-ideo");
                param.append('\n');
            }
        }
//        if (pubmedMiningGene || pubmedMiningIdeo) {
//            String geneSymbols = options[id + 1];
//            StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");
//
//            boolean hasPrint1 = false;
//            while (st.hasMoreTokens()) {
//                String dbLabel = st.nextToken().toString().trim();
//
//                if (Util.isNumeric(dbLabel)) {
//                    String infor = "Invalid term " + dbLabel + " at --pubmed-mining option";
//                    throw new Exception(infor);
//                } else {
//                    if (!searchList.contains(dbLabel)) {
//                        if (!hasPrint1) {
//                            param.append(' ');
//                            hasPrint1 = true;
//                        } else {
//                            param.append(',');
//                        }
//                        param.append(dbLabel);
//                        searchList.add(dbLabel.replace('+', ' '));
//                    }
//                }
//            }
//            param.append('\n');
//        }

//---------------------------phenotype-term
        id = find("--phenotype-term");
        if (id >= 0) {
            param.append("--phenotype-term");

            String geneSymbols = options[id + 1];
            StringTokenizer st = new StringTokenizer(geneSymbols.trim(), ",");

            boolean hasPrint1 = false;
            while (st.hasMoreTokens()) {
                String dbLabel = st.nextToken().toString().trim();

                if (Util.isNumeric(dbLabel)) {
                    String infor = "Invalid term " + dbLabel + " at --pubmed-mining option";
                    throw new Exception(infor);
                } else {
                    if (!searchList.contains(dbLabel)) {
                        if (!hasPrint1) {
                            param.append(' ');
                            hasPrint1 = true;
                        } else {
                            param.append(',');
                        }
                        param.append(dbLabel);
                        searchList.add(dbLabel.replace('+', ' '));
                    }
                }
            }
            param.append('\n');
        }

//---------------------------Deleterious and pathogenic prediction at variants;
        id = find("--db-score");
        if (id >= 0) {
            if ((inputFormat.equals("--assoc-file") && inputFormatSupp != null && !inputFormatSupp.equals("--v-assoc-file") && !inputFormatSupp.equals("--glm-file")) || inputFormat.equals("--vaast-simple-file")) {
                String infor = "--db-score does not support inputs specified by " + inputFormat;
                throw new Exception(infor);
            }

            boolean needDBScoreInfo = true;

            String dbNames = options[id + 1];
            StringTokenizer st = new StringTokenizer(dbNames.trim(), ",");
            param.append("--db-score");
            boolean hasPrint1 = false;
            while (st.hasMoreTokens()) {
                String dbLabel = st.nextToken();
                if (dbLabel.equals("noncoding")) {
                    scoreDBLableList.add("noncoding");
                    if (!hasPrint1) {
                        param.append(' ');
                        hasPrint1 = true;
                    } else {
                        param.append(',');
                    }
                    param.append(dbLabel);
                    if ((id = find("--regulatory-causing-predict")) < 0 && (id = find("--complex-pathogenic-predict")) < 0) {
                        String infor = "please provide paramter --complex-pathogenic-predict or --regulatory-causing-predict function";
                        throw new Exception(infor);
                    }
                    if ((id = find("--regulatory-causing-predict")) >= 0 && (id = find("--complex-pathogenic-predict")) >= 0) {
                        String infor = "only one function will be available at each run time";
                        throw new Exception(infor);
                    }
                    if ((id = find("--regulatory-causing-predict")) >= 0) {
                        needDBScoreInfo = false;
                    }
                } else if (dbLabel.equals("dbncfp")) {
                    scoreDBLableList.add("dbncfp");
                    if ((id = find("--regulatory-causing-predict")) >= 0) {
                        needDBScoreInfo = false;
                        needAnnotateGene = false;
                    }else{
                    	 String infor = "please provide paramter --regulatory-causing-predict function and selected combination!";
                         throw new Exception(infor);
                    }
                    param.append(" " + dbLabel);
                } else {
                    String dbFileName = PUBDB_FILE_MAP.get(dbLabel);
                    if (dbFileName == null) {
                        String infor = "Invalid variants database name " + dbLabel + " at  --db-score option";
                        throw new Exception(infor);
                    } else {
                        if (!hasPrint1) {
                            param.append(' ');
                            hasPrint1 = true;
                        } else {
                            param.append(',');
                        }
                        param.append(dbLabel);
                        scoreDBLableList.add(dbLabel);
                    }
                }

            }
            if (needDBScoreInfo) {
                if (geneDBLabels == null) {
                    String infor = "Please specify --db-gene for --db-score !";
                    throw new Exception(infor);
                }
            }
            param.append('\n');
            id = find("--verbose-noncoding");
            if (id >= 0) {
                needVerboseNoncode = true;
                param.append("--verbose-noncoding\n");
            }

            id = find("--cell");
            if (id > 0) {
                cellLineName = options[id + 1];
                param.append("--cell ");
                param.append(cellLineName + "\n");
            }

            String predType = null;
            id = find("--mendel-causing-predict");
            if (id >= 0) {
                causingPredType = 0;
                predType = "--mendel-causing-predict ";
                param.append(predType);
            } else if ((id = find("--regulatory-causing-predict")) > 0) {
                causingPredType = 3;
                String[] sourceName = {"CADD_cscore", "CADD_PHRED", "DANN_score", "FunSeq_score", "FunSeq2_score", "GWAS3D_score", "GWAVA_region_score", "GWAVA_TSS_score", "GWAVA_unmatched_score", "SuRFR_score", "Fathmm_MKL_score"};
                String selectionColumn = options[id + 1];
                param.append("--regulatory-causing-predict ");
                if (selectionColumn.indexOf(",") > 0) {
                    String[] sp = selectionColumn.trim().split(",");
                    dbncfpFeatureColumn = new String[sp.length];
                    for (int i = 0; i < sp.length; i++) {
                        if (Integer.parseInt(sp[i]) > sourceName.length) {
                            throw new Exception("Indexes are too large for current annotation source!");
                        }
                        dbncfpFeatureColumn[i] = sourceName[Integer.parseInt(sp[i]) - 1];
                    }
                    param.append(selectionColumn);
                } else {
                    String[] defaultSourceName = {"CADD_cscore", "DANN_score", "FunSeq_score", "FunSeq2_score", "GWAS3D_score", "GWAVA_TSS_score", "SuRFR_score", "Fathmm_MKL_score"};
                    dbncfpFeatureColumn = defaultSourceName;
                    selectionColumn = "all";
                    param.append("1,3,4,5,6,8,10,11");
                }
                
                //------------------pass dbNCFP score list for allele-specific variant
                if ((id = find("--complex-pathogenic-predict")) >= 0) {
                	
                }
            } else if ((id = find("--complex-pathogenic-predict")) >= 0) {
                causingPredType = 1;
                predType = "--complex-pathogenic-predict";
                param.append(predType);
                param.append('\n');
            } else if ((id = find("--cancer-driver-predict")) >= 0) {
                causingPredType = 2;
                predType = "--cancer-driver-predict";
                param.append(predType);
                param.append('\n');
                needRconnection = true;
            } else if ((id = find("--cancer-causing-predict")) >= 0) {
                causingPredType = 2;
                predType = "--cancer-driver-predict ";
                String infor = "--cancer-causing-predict has been replaced with --cancer-driver-predict";
                throw new Exception(infor);
            } else {
                //Otherwise do nothing
                /*
                 causingPredType = 0;
                 predType = "--mendel-causing-predict ";
                 param.append(predType);
                 predictExplanatoryVar = "all";
                 param.append(predictExplanatoryVar);
                 param.append('\n');
                 */
            }

            if (id >= 0 && causingPredType != 2 && causingPredType != 1 && causingPredType != 3) {
                predictExplanatoryVar = options[id + 1];
                if (predictExplanatoryVar.indexOf("all") >= 0) {
                    predictExplanatoryVar = "all";
                    param.append(predictExplanatoryVar);
                    param.append('\n');
                } else if (predictExplanatoryVar.indexOf("best") >= 0) {
                    predictExplanatoryVar = "best";
                    param.append(predictExplanatoryVar);
                    param.append('\n');
                } else {
                    StringBuilder sb = new StringBuilder();
                    String[] expVar = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
                    String[] inputVar = predictExplanatoryVar.split(",");
                    for (String str1 : expVar) {
                        for (String str2 : inputVar) {
                            if (str1.equals(str2)) {
                                sb.append((Integer.parseInt(str1) - 1));
                                sb.append('-');
                                break;
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        param.append(predictExplanatoryVar);
                        param.append('\n');
                        predictExplanatoryVar = sb.substring(0, sb.length() - 1);
                    } else {
                        String infor = "\'" + predType + "\' has no valid tag(s) " + options[id + 1];
                        throw new Exception(infor);
                        // predictExplanatoryVar = "all";
                    }
                }
            }

            id = find("--filter-nondisease-variant");
            if (id >= 0) {
                filterNonDiseaseMut = true;
                param.append("--filter-nondisease-variant");
                param.append('\n');
            } else {
                //String infor = "To filter out variants estimated to be non-disease causal, sepecify the option \'--filter-nondisease-variant\'";
                //System.out.println(infor);
            }

        } else {
            //String infor = "To quantitatively prioritize variants, sepecify the option \'--db-score dbnsfp,...\'";
            //System.out.println(infor);
        }
        
        //---------------------skat
        id = find("--skat");
        if (id > 0) {
            skat = true;
            param.append("--skat");
            if ((id + 1) != options.length) {
                skatBinary = options[id + 1].equals("binary");
            }
            if (skatBinary) {
                param.append(" binary");
            }

            id = find("--skat-curoff");
            if (id > 0) {
                param.append(" --skat-cutoff ");
                skatCutoff = Integer.valueOf(options[id + 1]);
                param.append(skatCutoff);
            }

            param.append('\n');
            needRconnection = true;
            id = find("--perm-pheno");
            if (id > 0) {
                permutePheno = true;
                param.append("--perm-pheno\n");
            }
        }

        id = find("--phe");
        if (id > 0) {
            phe = true;
            pheItem = options[id + 1];
            param.append("--phe");
            param.append(" ");
            param.append(pheItem);
            param.append("\n");
        }

        id = find("--cov");
        if (id > 0) {
            cov = true;
            covItem = options[id + 1].split(",");
            param.append("--cov");
            param.append(" ");
            param.append(options[id + 1]);
            param.append("\n");
        }

//-------------------path-gene-predict------------------
        id = find("--patho-gene-predict");
        if (id > 0) {
            mendelGenePatho = true;
            param.append("--patho-gene-predict");
            param.append("\n");
        }

        ///////////others
        if (find("--make-filter") >= 0) {
            command = "--make-filter";
            param.append(command);
            param.append('\n');

            id = find("--buildver-in");
            if (id >= 0) {
                buildverIn = options[id + 1];
                param.append("--buildver-in");
                param.append(" ");
                param.append(buildverIn);
                param.append('\n');
            }
            id = find("--buildver-out");
            if (id >= 0) {
                buildverOut = options[id + 1];
                param.append("--buildver-out");
                param.append(" ");
                param.append(buildverOut);
                param.append('\n');
            } else {
                String info = "To change the genome version of input and output physical positions, use --buildver-in hgXX --buildver-out hgXX";
                System.out.println(info);
                //return false;
            }
        }

//---------------------phenolyzer-prediction        
        id = find("--phenolyzer-prediction");
        if (id > 0) {
            phenolyzer = true;
            param.append("--phenolyzer-prediction");
            param.append("\n");
        }

        id = find("--rsid");
        if (id > 0) {
            rsid = true;
            param.append("--rsid");
            param.append('\n');
        }
        id = find("--gene-mutation-rate-test");
        if (id > 0) {
            geneMutationRateTest = true;
            param.append("--gene-mutation-rate-test");
            param.append('\n');
            needRconnection = true;
        }

        // if (causingPredType == 2) 
        {
            id = find("--geneset-db");
            if (id >= 0) {
                enrichmentTestGeneSetDB = options[id + 1];
                param.append("--geneset-db");
                param.append(' ');
                param.append(enrichmentTestGeneSetDB);
                param.append('\n');
            } else if ((id = find("--geneset-file")) >= 0) {
                enrichmentTestGeneSetFile = options[id + 1];
                File file = new File(enrichmentTestGeneSetFile);
                if (!file.exists()) {
                    String infor = "\'" + file.getCanonicalPath() + "\' does not exist! ";
                    throw new Exception(infor);
                }
                param.append("--geneset-file");
                param.append(' ');
                param.append(enrichmentTestGeneSetFile);
                param.append('\n');
            }

            id = find("--geneset-enrichment-test");
            if (id > 0) {
                if (find("--geneset-db") < 0 && find("--geneset-file") < 0) {
                    String infor = "The candidate genes specified by \'--geneset-db\' or \'--geneset-file\' are required for \'--geneset-enrichment-test\'!";
                    throw new Exception(infor);
                }
                genesetEnrichmentTest = true;

                param.append("--geneset-enrichment-test");
                param.append('\n');
            }

            id = find("--geneset-gene-p");
            if (id >= 0) {
                genesetHyperGenePCut = Double.parseDouble(options[id + 1]);
                param.append("--geneset-gene-p");
                param.append(' ');
                param.append(genesetHyperGenePCut);
                param.append('\n');
            }
        }

        //----------------------plotting function-----------------       
        id = find("--qqplot");
        if (id >= 0) {
            if (!varAssoc && !skat && !geneMutationRateTest && !genesetEnrichmentTest) {
                String infor = "--qqplot has to be used jointly with \'--var-assoc\' or \'--skat\' or '--gene-mutation-rate-test' or '--geneset-enrichment-test'!";

                throw new Exception(infor);
            }
            toQQPlot = true;
            param.append("--qqplot");
            param.append('\n');
        } else {
            //String infor = "To draw QQ plot of p-values, please use \'--qqplot \'.";
            //System.out.println(infor);
        }
        id = find("--mafplot-ref");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "--mafplot-ref does only supports inputs specified by \'--vcf-file\'!";
                throw new Exception(infor);
            } else if (inputFormat.equals("--vcf-file")) {
                if (find("--db-filter") < 0 && find("--local-filter") < 0 && find("--local-filter-vcf") < 0) {
                    String infor = "To carry out --mafplot-ref for inputs specified by \'--vcf-file\', "
                            + "you need sepcify refernce variants datasets by \'--db-filter\' or \'--local-filter\' or \'--local-filter-vcf\'!";
                    throw new Exception(infor);
                }
            }
            toMAFPlotRef = true;
            param.append("--mafplot-ref");
            param.append('\n');
        } else {
            String infor = "To draw MAF in a histogram plot, please use \'--mafplot-ref \'.";
            //System.out.println(infor);
        }
        id = find("--mafplot-sample");
        if (id >= 0) {
            if (!inputFormat.equals("--vcf-file")) {
                String infor = "--mafplot-sample does only supports inputs specified by \'--vcf-file\'!";
                throw new Exception(infor);
            }
            toMAFPlotSample = true;
            param.append("--mafplot-sample");
            param.append('\n');
        }

        /*
         *  log4j.rootLogger=INFO,file
        
         #output configuration
        
         log4j.appender.file=org.apache.log4j.FileAppender
         log4j.appender.file.File=kgg.log
         log4j.appender.file.MaxFileSize=1000KB
         log4j.appender.file.MaxBackupIndex=10
         log4j.appender.file.layout=org.apache.log4j.PatternLayout
         log4j.appender.file.layout.ConversionPattern=%-5p %d{yyyy-MM-dd HH:mm:ss} - %m%n
        
        
         log4j.rootLogger=INFO,stdout
        
         #output configuration
        
         log4j.appender.stdout=org.apache.log4j.ConsoleAppender
         log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
         log4j.appender.stdout.layout.ConversionPattern=%-5p %d{yyyy-MM-dd HH:mm:ss} - %m%n 
         */
        Properties pros = new Properties();
        if (needLog) {
            pros.setProperty("log4j.rootLogger", "INFO,stdout,file");
        } else {
            pros.setProperty("log4j.rootLogger", "INFO,stdout");
        }
        pros.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        pros.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        pros.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%-5p %d{yyyy-MM-dd HH:mm:ss} - %m%n");
        if (needLog) {
            pros.setProperty("log4j.appender.file", "org.apache.log4j.FileAppender");
            pros.setProperty("log4j.appender.file.File", outputFileName + ".log");
            // pros.setProperty("log4j.appender.file.Append", "false");
            pros.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
            pros.setProperty("log4j.appender.file.layout.ConversionPattern", "%-5p %d{yyyy-MM-dd HH:mm:ss} - %m%n");
        }

        PropertyConfigurator.configure(pros);

        return param.toString();

    }

    private int find(String opp) {
        for (int i = 0; i < optionNum; i++) {
            if (options[i].equals(opp)) {
                return i;
            }
        }
        return -1;
    }

    public void readOptions(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = "";
        int lineNumber = 0;
        StringBuilder tmpStr = new StringBuilder();
        List<String> optionList = new ArrayList<String>();
        //assume every parameter has a line
        while ((line = br.readLine()) != null) {
            line = line.trim();
            //System.out.println(line);
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            int index = line.indexOf("#");
            if (index >= 0) {
                line = line.substring(0, index);
            }
            //  System.out.println(line);
            StringTokenizer tokenizer = new StringTokenizer(line);
            //sometimes tokenizer.nextToken() can not release memory

            while (tokenizer.hasMoreTokens()) {
                //parameter Name value
                optionList.add(tmpStr.append(tokenizer.nextToken().trim()).toString());
                tmpStr.delete(0, tmpStr.length());
            }
            lineNumber++;
            // System.out.println(line);
        }
        br.close();
        optionNum = optionList.size();
        options = new String[optionNum];
        for (int i = 0; i < optionNum; i++) {
            options[i] = optionList.get(i);
        }
    }

    public void readOptions(String[] args) throws Exception {
        optionNum = args.length;
        options = new String[optionNum];
        System.arraycopy(args, 0, options, 0, optionNum);
    }
}
