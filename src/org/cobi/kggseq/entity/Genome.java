/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.entity;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.GlobalManager;
import org.cobi.kggseq.controller.BinaryGtyProcessor;
import org.cobi.util.file.LocalFileFunc;

import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.text.LocalFile;
import org.cobi.util.text.Util;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 *
 * @author MX Li
 */
public class Genome implements Constants {

    Kryo kryo = new Kryo();

    Map<String, Integer> chromNameIndexMap = new HashMap<String, Integer>();
    /**
     * @pdOid 919fbd33-2725-4ec8-934c-52dbb4580123
     */
    private String name;
    /**
     * @pdOid 65e6e40e-de4a-4b64-9c2d-c71a70bc879e
     */
    private String storagePath;
    /**
     * @pdRoleInfo migr=no name=Chromosome assc=association14 mult=0..*
     * type=Aggregation
     */
    private Chromosome[] chromosomes;
    private Map<String, int[]> mRNAIndexMap = new HashMap<String, int[]>();

    //private OpenIntIntHashMap[] variantPositionIndexMap = new OpenIntIntHashMap[STAND_CHROM_NAMES.length];
    //a temple Variant to save time when look up a variant by binary search
    Variant tmpVar = new Variant(-1, ".", new String[]{"."});
    Gene tmpGene = new Gene("No", -1, -1);
    List<String> variantScoreLabels = new ArrayList<String>();
    List<String> variantFeatureLabels = new ArrayList<String>();
    List<String> geneFeatureLabels = new ArrayList<String>();
    List<String> geneScoreLabels = new ArrayList<String>();
    int geneNum = 0;
    int varNum = 0;
    boolean isPhasedGty = false;
    final String UNKNOWN_CHROM_NAME0 = "Un";
    final String UNKNOWN_CHROM_NAME1 = "GL";
    boolean refSeqAnnot = false;
    boolean gencodeAnnot = false;
    boolean knownAnnot = false;
    boolean ensemblAnnot = false;
    boolean needAccoundAffect = false;
    boolean needAccoundUnaffect = false;
    boolean needAccoundAll = false;

    public boolean isNeedAccoundAffect() {
        return needAccoundAffect;
    }

    public void setNeedAccoundAffect(boolean needAccoundAffect) {
        this.needAccoundAffect = needAccoundAffect;
    }

    public boolean isNeedAccoundAll() {
        return needAccoundAll;
    }

    public void setNeedAccoundAll(boolean needAccoundAll) {
        this.needAccoundAll = needAccoundAll;
    }

    public boolean isNeedAccoundUnaffect() {
        return needAccoundUnaffect;
    }

    public void setNeedAccoundUnaffect(boolean needAccoundUnaffect) {
        this.needAccoundUnaffect = needAccoundUnaffect;
    }

    public Map<String, Integer> getChromNameIndexMap() {
        return chromNameIndexMap;
    }

    public boolean isKnownAnnot() {
        return knownAnnot;
    }

    public void setKnownAnnot(boolean knownAnnot) {
        this.knownAnnot = knownAnnot;
    }

    public boolean isGencodeAnnot() {
        return gencodeAnnot;
    }

    public boolean isEnsemblAnnot() {
        return ensemblAnnot;
    }

    public void setEnsemblAnnot(boolean ensemblAnnot) {
        this.ensemblAnnot = ensemblAnnot;
    }

    public void setGencodeAnnot(boolean gencodeAnnot) {
        this.gencodeAnnot = gencodeAnnot;
    }

    public boolean isRefSeqAnnot() {
        return refSeqAnnot;
    }

    public void setRefSeqAnnot(boolean refSeqAnnot) {
        this.refSeqAnnot = refSeqAnnot;
    }

    public boolean isIsPhasedGty() {
        return isPhasedGty;
    }

    public void setIsPhasedGty(boolean isPhasedGty) {
        this.isPhasedGty = isPhasedGty;
    }

    public List<String> getVariantScoreLabels() {
        return variantScoreLabels;
    }

    public void addVariantScoreLabel(String score) {
        variantScoreLabels.add(score);
    }

    public int getGeneNum() {
        return geneNum;
    }

    public void setmRNANum(int geneNum) {
        this.geneNum = geneNum;
    }

    public List<String> getVariantFeatureLabels() {
        return variantFeatureLabels;
    }

    public void addVariantFeatureLabel(String labels) {
//        if(variantFeatureLabels.contains(labels))   return;
        variantFeatureLabels.add(labels);
    }

    public int getVariantFeatureNum() {
        return variantFeatureLabels.size();
    }

    public void addVariantFeatureLabels(List<String> labels) {
        variantFeatureLabels.addAll(labels);
    }

    public void addmRNAFeatureLabel(String labels) {
//        if(geneFeatureLabels.contains(labels))  return;
        geneFeatureLabels.add(labels);
    }

    public int getmRNAFeatureNum() {
        return geneFeatureLabels.size();
    }

    public void addmRNAScoreLabel(String labels) {
        geneScoreLabels.add(labels);
    }

    public void addGeneFeatureLabels(List<String> labels) {
        geneFeatureLabels.addAll(labels);
    }

    public void addGeneFeatureLabel(String label) {
        geneFeatureLabels.add(label);
    }

    public List<String> getGeneFeatureLabels() {
        return geneFeatureLabels;
    }

    public Chromosome[] getChromosomes() {
        return chromosomes;
    }

    public String getName() {
        return name;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public int getVarNum() {
        return varNum;
    }

    public void setVarNum(int varNum) {
        this.varNum = varNum;
    }

    public Genome(String name, String storagePath) {
        this.name = name;
        this.storagePath = storagePath + "TMP";

        chromosomes = new Chromosome[STAND_CHROM_NAMES.length];

        for (int i = 0; i < STAND_CHROM_NAMES.length; i++) {
            chromosomes[i] = new Chromosome(STAND_CHROM_NAMES[i], i);
            chromNameIndexMap.put(STAND_CHROM_NAMES[i], i);
        }
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.register(char[].class);
        kryo.register(long[].class);
        kryo.register(float[].class);
        kryo.register(String[].class);
        kryo.register(Variant.class);
        kryo.register(StringBuilder.class);
        kryo.register(String.class, new DeflateSerializer(new DefaultSerializers.StringSerializer()));
    }

    public void loadVariantFromDisk(int chromID) {
        String chrNameP = "Chromosome." + STAND_CHROM_NAMES[chromID];
        List<Variant> varList = chromosomes[chromID].variantList;
        varList.clear();
        Variant var = null;

        try {
            File folder = new File(storagePath);
            if (!folder.exists()) {
                return;
            }
            File[] files = folder.listFiles();
            for (File file : files) {
                if (!file.getName().contains(chrNameP + ".var.obj.")) {
                    continue;
                }
                Input input = new Input(new FileInputStream(file), 1024 * 1024);
                while (!input.eof()) {
                    var = (Variant) kryo.readObject(input, Variant.class);
                    varList.add(var);
                }
                // System.out.println(varList.size());
                input.close();
            }
            Collections.sort(varList, new VariantPositionComparator());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void writeChromsomeToDiskClean() {
        int chromID = -1;
        String chromeName;
        int threadID = 0;
        try {
            for (Chromosome chrome : chromosomes) {
                List<Variant> varList = chrome.variantList;
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

    public void writeChromosomeToDiskClean(List<Individual> subjectList, boolean needGty, boolean needReadInfo, boolean needGtyQaul) throws Exception {
        for (Chromosome chrome : chromosomes) {
            String chromeName = chrome.getName();
            List<Variant> varList = chrome.variantList;
            if (varList.isEmpty()) {
                continue;
            }
            int chromID = chrome.id;
            int fileIndex = 0;
            String chrNameP = "Chromosome." + chromeName;
            File folder = new File(storagePath);
            if (folder.exists()) {
                boolean findOnce = false;
                for (final File fileEntry : folder.listFiles()) {
                    if (!fileEntry.isDirectory()) {
                        String name = fileEntry.getName();
                        if (!name.contains(chrNameP)) {
                            continue;
                        }
                        int index = name.lastIndexOf(".var.obj");
                        if (index < 0) {
                            continue;
                        }
                        index = Integer.parseInt(name.substring(index + 9));
                        if (fileIndex < index) {
                            fileIndex = index;
                        }
                        findOnce = true;
                        // System.out.println(fileEntry.getName());
                    }
                }
                if (findOnce) {
                    fileIndex++;
                }
            } else {
                folder.mkdirs();
            }

            //comments: both Kryo and FSTObjectOutput are excellent tools for Serializationl. However, the former produced slightly smaller file and was slightly faster. So I used Kryo
            File fileName = new File(storagePath + File.separator + chrNameP + ".var.obj." + fileIndex);
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
    }

    public void removeTempFileFromDisk() throws Exception {
        LocalFileFunc.delAll(new File(storagePath));
    }

    public List<String> getScoreLabels() {
        return variantScoreLabels;
    }

    public void addGeneFullChromName(String chromName, mRNA var) {
        chromName = chromName.substring(3);
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            System.err.println("Unrecognized chromosome name: " + chromName);
            return;
        }

        mRNAIndexMap.put(var.refID, new int[]{chromID, chromosomes[chromID].mRNAList.size()});
        chromosomes[chromID].addmRNA(var);

    }

    public void addANullFeature2Variants(String missingVa) {
        int featureNum = variantFeatureLabels.size();
        for (int c = 0; c < chromosomes.length; c++) {
            if (chromosomes[c] != null) {
                for (Variant var : chromosomes[c].variantList) {
                    var.setFeatureValue(featureNum, missingVa);
                }
            }
        }
    }

    public void addVariantList(String chromName, List<Variant> vars) {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            System.err.println("Unrecognized chromosome name: " + chromName);
            return;
        }

        chromosomes[chromID].variantList.addAll(vars);
        chromosomes[chromID].setHasNotOrderVariantList(true);
    }

    public void addVariantList(int chromID, List<Variant> vars) {
        chromosomes[chromID].variantList.addAll(vars);
        chromosomes[chromID].setHasNotOrderVariantList(true);
    }

    public Variant getVariant(String chromName, int index) {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return null;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            //System.err.println("Unrecognized chromosome name: " + chromName);
            return null;
        }
        return chromosomes[chromID].variantList.get(index);
    }

    public Variant[] lookupVariants(String chromName, int sartPostion, int endPostion) throws Exception {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return null;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            //System.err.println("Unrecognized chromosome name: " + chromName);
            return null;
        }
        if (chromosomes[chromID].variantList.isEmpty()) {
            return null;
        }
        if (sartPostion > endPostion) {
            int postion = sartPostion;
            sartPostion = endPostion;
            endPostion = postion;
        }
        int startIndex = -1;
        int endIndex = -1;
        tmpVar.refStartPosition = sartPostion;
        int index = chromosomes[chromID].lookupVariantByList(tmpVar);
        if (index >= 0) {
            startIndex = index - 1;
            while (startIndex >= 0 && chromosomes[chromID].variantList.get(startIndex).refStartPosition == sartPostion) {
                startIndex--;
            }
            startIndex++;
        } else {
            startIndex = -index - 1;
        }
        if (chromosomes[chromID].variantList.size() <= startIndex) {
            return null;
        }
        tmpVar.refStartPosition = endPostion;
        index = chromosomes[chromID].lookupVariantByList(tmpVar);
        if (index >= 0) {
            endIndex = index + 1;
            while (endIndex < chromosomes[chromID].variantList.size() && chromosomes[chromID].variantList.get(endIndex).refStartPosition == endPostion) {
                endIndex++;
            }
        } else {
            endIndex = -index - 1;
        }

        if (endIndex <= startIndex) {
            return null;
        }
        Variant[] selectVar = new Variant[endIndex - startIndex];
        for (int i = startIndex; i < endIndex; i++) {
            selectVar[i - startIndex] = chromosomes[chromID].variantList.get(i);
        }
        return selectVar;
        /*
         * it is very strange to know that variantPositionIndexMap is twice slower than the binnary search to look up a variants when there are 663 variants
         * but donot know what will happen when there are millions of snps
         if (chromosomes[chromID] == null) {
         return null;
         }
         return chromosomes[chromID].variantList.get(variantPositionIndexMap[chromID].get(postion));
         * 
         */
    }

    public int[] lookupVariantIndexes(String chromName, int postion) throws Exception {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return null;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            //System.err.println("Unrecognized chromosome name: " + chromName);
            return null;
        }
        if (chromosomes[chromID].variantList.isEmpty()) {
            return null;
        }
        tmpVar.refStartPosition = postion;
        int index = chromosomes[chromID].lookupVariantByMap(tmpVar);
        if (index < 0) {
            return null;
        }
        int startIndex = index - 1;
        while (startIndex >= 0 && chromosomes[chromID].variantList.get(startIndex).refStartPosition == postion) {
            startIndex--;
        }
        startIndex++;
        int endIndex = index + 1;
        while (endIndex < chromosomes[chromID].variantList.size() && chromosomes[chromID].variantList.get(endIndex).refStartPosition == postion) {
            endIndex++;
        }

        int[] selectVar = new int[endIndex - startIndex];
        for (int i = startIndex; i < endIndex; i++) {
            selectVar[i - startIndex] = i;
        }
        return selectVar;
        /*
         if (variantPositionIndexMap[chromID].containsKey(postion)) {
         return variantPositionIndexMap[chromID].get(postion);
         } else {
         return -1;
         }
         * 
         */
    }

    public List<Variant> getChromVariants(String chromName) throws Exception {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return null;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            //System.err.println("Unrecognized chromosome name: " + chromName);
            return null;
        }
        return chromosomes[chromID].variantList;

    }

    public int lookupVariantIndexMin(String chromName, int postion) throws Exception {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return -1;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            //System.err.println("Unrecognized chromosome name: " + chromName);
            return -1;
        }

        if (chromosomes[chromID].variantList.isEmpty()) {
            return -1;
        }
        tmpVar.refStartPosition = postion;
        int index = chromosomes[chromID].lookupVariantByList(tmpVar);
        if (index < 0) {
            return index;
        }
        int startIndex = index - 1;
        while (startIndex >= 0 && chromosomes[chromID].variantList.get(startIndex).refStartPosition == postion) {
            startIndex--;
        }
        startIndex++;

        return startIndex;
        /*
         if (variantPositionIndexMap[chromID].containsKey(postion)) {
         return variantPositionIndexMap[chromID].get(postion);
         } else {
         return -1;
         }
         * 
         */
    }

    public int lookupVariantIndexMax(String chromName, int postion) throws Exception {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return -1;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            // System.err.println("Unrecognized chromosome name: " + chromName);
            return -1;
        }

        if (chromosomes[chromID].variantList.isEmpty()) {
            return -1;
        }
        tmpVar.refStartPosition = postion;
        int index = chromosomes[chromID].lookupVariantByList(tmpVar);
        if (index < 0) {
            return index;
        }

        int endIndex = index + 1;
        while (endIndex < chromosomes[chromID].variantList.size() && chromosomes[chromID].variantList.get(endIndex).refStartPosition == postion) {
            endIndex++;
        }
        endIndex--;

        return endIndex;
        /*
         if (variantPositionIndexMap[chromID].containsKey(postion)) {
         return variantPositionIndexMap[chromID].get(postion);
         } else {
         return -1;
         }
         * 
         */
    }

    public Variant[] lookupVariants(String chromName, int postion, boolean isIndel, String ref, String[] alt) throws Exception {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return null;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            //System.err.println("Unrecognized chromosome name: " + chromName);
            return null;
        }
        if (chromosomes[chromID].variantList.isEmpty()) {
            return null;
        }
        tmpVar.setIsIndel(isIndel);
        tmpVar.setRefAllele(ref);
        tmpVar.setAltAlleles(alt);
        tmpVar.refStartPosition = postion;

        int index = chromosomes[chromID].lookupVariantByMap(tmpVar);
        if (index < 0) {
            return null;
        } else {
            int startIndex = index - 1;
            while (startIndex >= 0 && chromosomes[chromID].variantList.get(startIndex).refStartPosition == postion) {
                startIndex--;
            }
            startIndex++;
            int endIndex = index + 1;
            while (endIndex < chromosomes[chromID].variantList.size() && chromosomes[chromID].variantList.get(endIndex).refStartPosition == postion) {
                endIndex++;
            }

            Variant[] selectVar = new Variant[endIndex - startIndex];
            for (int i = startIndex; i < endIndex; i++) {
                selectVar[i - startIndex] = chromosomes[chromID].variantList.get(i);
            }
            return selectVar;
        }

        /*
         * it is very strange to know that variantPositionIndexMap is twice slower than the binnary search to look up a variants when there are 663 variants
         * but donot know what will happen when there are millions of snps
         if (chromosomes[chromID] == null) {
         return null;
         }
         return chromosomes[chromID].variantList.get(variantPositionIndexMap[chromID].get(postion));
         * 
         */
    }

    //subject to change because given a start-end position there might be multple transcripts
    public mRNA lookupmRNAFullChromName(String chromName, int start, int end) throws Exception {
        chromName = chromName.substring(3);
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) | chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return null;
        }

        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            // System.err.println("Unrecognized chromosome name: " + chromName);
            return null;
        }
        if (chromosomes[chromID].mRNAList.isEmpty()) {
            return null;
        }
        tmpGene.start = start;
        tmpGene.end = end;

        int index = chromosomes[chromID].lookupmRNA(tmpGene);
        if (index < 0) {
            return null;
        } else {
            return chromosomes[chromID].mRNAList.get(index);
        }
        /*
         * it is very strange to know that variantPositionIndexMap is twice slower than the binnary search to look up a variants when there are 663 variants
         * but donot know what will happen when there are millions of snps
         if (chromosomes[chromID] == null) {
         return null;
         }
         return chromosomes[chromID].variantList.get(variantPositionIndexMap[chromID].get(postion));
         * 
         */
    }

    public mRNA lookupmRNA(String refID) throws Exception {
        if (refID == null) {
            return null;
        }
        int[] posIndex = mRNAIndexMap.get(refID);
        if (posIndex == null) {
            return null;
        }
        return chromosomes[posIndex[0]].mRNAList.get(posIndex[1]);
    }

    public void buildVariantIndexMapOnChromosomes() {
        for (int i = 0; i < chromosomes.length; i++) {
            if (chromosomes[i] != null) {
                chromosomes[i].buildVariantIndexMap();
            }
        }
    }

    public void export2PolyphenInput(String exportPath) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(exportPath));
        int alleleNum = 0;
        int i = 0;
        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (Variant var : chromosomes[chromIndex].variantList) {
                //format:  chr1:1158631 A/C/G/T
                bw.write("chr" + chromosomes[chromIndex].getName());
                bw.write(":");
                bw.write(String.valueOf(var.refStartPosition));
                bw.write(" ");
                bw.write(String.valueOf(var.getRefAllele()));
                String[] alleles = var.getAltAlleles();
                alleleNum = alleles.length;
                for (i = 0; i < alleleNum; i++) {
                    bw.write("/");
                    bw.write(String.valueOf(alleles[0]));
                }
                bw.write("\n");
            }
        }
        bw.close();
    }

    private List<String[]> makeVariantTableList(boolean outAltAf) {
        List<String> colNumNameList = new ArrayList<String>();
        colNumNameList.add("Chromosome");
        colNumNameList.add("StartPosition");
        colNumNameList.add("ReferenceAlternativeAllele");
//        colNumNameList.add("rsID");
//        colNumNameList.add("MostImportantFeatureGene");
//        colNumNameList.add("MostImportantGeneFeature");
        if (refSeqAnnot) {
            colNumNameList.add("RefGeneFeatures");
        }
        if (gencodeAnnot) {
            colNumNameList.add("GENCODEFeatures");
        }

        if (knownAnnot) {
            colNumNameList.add("UCSCKnownGeneFeatures");
        }
        if (ensemblAnnot) {
            colNumNameList.add("EnsemblFeatures");
        }
        if (needAccoundAffect) {
            colNumNameList.add("AffectedRefHomGtyNum");
            colNumNameList.add("AffectedHetGtyNum");
            colNumNameList.add("AffectedAltHomGtyNum");
        }
        if (needAccoundUnaffect) {
            colNumNameList.add("UnaffectedRefHomGtyNum");
            colNumNameList.add("UnaffectedHetGtyNum");
            colNumNameList.add("UnaffectedAltHomGtyNum");
        }
        if (needAccoundAll) {
            colNumNameList.add("AllRefHomGtyNum");
            colNumNameList.add("AllHetGtyNum");
            colNumNameList.add("AllAltHomGtyNum");
        }

        if (outAltAf) {
            colNumNameList.add("MaxDBAltAF");
        }
        int scoreNum = 0;
        int featureNum = 0;
        if (variantScoreLabels != null) {
            colNumNameList.addAll(variantScoreLabels);
            scoreNum = variantScoreLabels.size();
        }
        if (variantFeatureLabels != null) {
            colNumNameList.addAll(variantFeatureLabels);
            featureNum = variantFeatureLabels.size();
        }

        int index = 0;
        StringBuilder sb = new StringBuilder();
        List<String[]> contens = new ArrayList<String[]>();
        contens.add(colNumNameList.toArray(new String[colNumNameList.size()]));
        List<String> cellValueList = new ArrayList<String>();

        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (Variant var : chromosomes[chromIndex].variantList) {
                cellValueList.clear();

                //format:  chr1 1158631 A/C/G/T rs 
                cellValueList.add(chromosomes[chromIndex].getName());
                cellValueList.add(String.valueOf(var.refStartPosition));

                sb.append(var.getRefAllele());
                for (String alle : var.getAltAlleles()) {
                    sb.append('/');
                    sb.append(alle);
                }
                cellValueList.add(sb.toString());
                sb.delete(0, sb.length());
//                cellValueList.add(var.getLabel());
//                cellValueList.add(var.geneSymb);
//                cellValueList.add(org.cobi.kggseq.Constants.VAR_FEATURE_NAMES[var.smallestFeatureID]);
                if (refSeqAnnot) {
                    cellValueList.add(var.getRefGeneAnnot());
                }
                if (gencodeAnnot) {
                    cellValueList.add(var.getgEncodeAnnot());
                }
                if (knownAnnot) {
                    cellValueList.add(var.getKnownGeneAnnot());
                }
                if (ensemblAnnot) {
                    cellValueList.add(var.getEnsemblGeneAnnot());
                }
                if (needAccoundAffect) {
                    cellValueList.add(String.valueOf(var.getAffectedRefHomGtyNum()));
                    cellValueList.add(String.valueOf(var.getAffectedHetGtyNum()));
                    cellValueList.add(String.valueOf(var.getAffectedAltHomGtyNum()));

                }
                if (needAccoundUnaffect) {
                    cellValueList.add(String.valueOf(var.getUnaffectedRefHomGtyNum()));
                    cellValueList.add(String.valueOf(var.getUnaffectedHetGtyNum()));
                    cellValueList.add(String.valueOf(var.getUnaffectedAltHomGtyNum()));
                }
                if (needAccoundAll) {
                    cellValueList.add(String.valueOf(var.getAffectedRefHomGtyNum()));
                    cellValueList.add(String.valueOf(var.getAffectedHetGtyNum()));
                    cellValueList.add(String.valueOf(var.getAffectedAltHomGtyNum()));
                }

                if (outAltAf) {
                    if (var.altAF == -1) {
                        cellValueList.add("N");
                    } else if (Float.isNaN(var.altAF)) {
                        cellValueList.add(".");
                    } else {
                        cellValueList.add(String.valueOf(var.altAF));
                    }

                }

                if (var.scores != null) {
                    for (double sc : var.scores) {
                        if (Double.isNaN(sc)) {
                            cellValueList.add(".");
                        } else {
                            cellValueList.add(String.valueOf(sc));
                        }
                    }
                } else {
                    for (int i = 0; i < scoreNum; i++) {
                        cellValueList.add(".");
                    }
                }

                if (var.featureValues != null) {
                    for (int i = 0; i < featureNum; i++) {
                        cellValueList.add(var.featureValues[i]);
                    }
                } else {
                    for (int i = 0; i < featureNum; i++) {
                        cellValueList.add(".");
                    }
                }
                contens.add(cellValueList.toArray(new String[cellValueList.size()]));
            }
        }
        return contens;
    }

    private List<String[]> makeVariantTableList(int chromIndex, boolean needHead, boolean outAltAf) {
        List<String[]> contens = new ArrayList<String[]>();
        if (needHead) {
            List<String> colNumNameList = new ArrayList<String>();
            colNumNameList.add("Chromosome");
            colNumNameList.add("StartPosition");
            colNumNameList.add("ReferenceAlternativeAllele");
//            colNumNameList.add("rsID");
//            colNumNameList.add("MostImportantFeatureGene");
//            colNumNameList.add("MostImportantGeneFeature");
            if (refSeqAnnot) {
                colNumNameList.add("RefGeneFeatures");
            }
            if (gencodeAnnot) {
                colNumNameList.add("GENCODEFeatures");
            }

            if (knownAnnot) {
                colNumNameList.add("UCSCKnownGeneFeatures");
            }
            if (ensemblAnnot) {
                colNumNameList.add("EnsemblFeatures");
            }
            if (needAccoundAffect) {
                colNumNameList.add("AffectedRefHomGtyNum");
                colNumNameList.add("AffectedHetGtyNum");
                colNumNameList.add("AffectedAltHomGtyNum");
            }
            if (needAccoundUnaffect) {
                colNumNameList.add("UnaffectedRefHomGtyNum");
                colNumNameList.add("UnaffectedHetGtyNum");
                colNumNameList.add("UnaffectedAltHomGtyNum");
            }
            if (needAccoundAll) {
                colNumNameList.add("AllRefHomGtyNum");
                colNumNameList.add("AllHetGtyNum");
                colNumNameList.add("AllAltHomGtyNum");
            }

            if (outAltAf) {
                colNumNameList.add("MaxDBAltAF");
            }

            if (variantScoreLabels != null) {
                colNumNameList.addAll(variantScoreLabels);
            }
            if (variantFeatureLabels != null) {
                colNumNameList.addAll(variantFeatureLabels);
            }
            contens.add(colNumNameList.toArray(new String[colNumNameList.size()]));
        }

        int index = 0;
        int scoreNum = 0;
        int featureNum = 0;
        StringBuilder sb = new StringBuilder();
        if (variantScoreLabels != null) {
            scoreNum = variantScoreLabels.size();
        }
        if (variantFeatureLabels != null) {
            featureNum = variantFeatureLabels.size();
        }
        List<String> cellValueList = new ArrayList<String>();

        if (chromosomes[chromIndex] == null) {
            return contens;
        }
        for (Variant var : chromosomes[chromIndex].variantList) {
            cellValueList.clear();

            //format:  chr1 1158631 A/C/G/T rs 
            cellValueList.add(chromosomes[chromIndex].getName());
            cellValueList.add(String.valueOf(var.refStartPosition));

            sb.append(var.getRefAllele());
            for (String alle : var.getAltAlleles()) {
                sb.append('/');
                sb.append(alle);
            }
            cellValueList.add(sb.toString());
            sb.delete(0, sb.length());
//            cellValueList.add(var.getLabel());
//            cellValueList.add(var.geneSymb);
//            cellValueList.add(org.cobi.kggseq.Constants.VAR_FEATURE_NAMES[var.smallestFeatureID]);
            if (refSeqAnnot) {
                cellValueList.add(var.getRefGeneAnnot());
            }
            if (gencodeAnnot) {
                cellValueList.add(var.getgEncodeAnnot());
            }
            if (knownAnnot) {
                cellValueList.add(var.getKnownGeneAnnot());
            }
            if (ensemblAnnot) {
                cellValueList.add(var.getEnsemblGeneAnnot());
            }
            if (needAccoundAffect) {
                cellValueList.add(String.valueOf(var.getAffectedRefHomGtyNum()));
                cellValueList.add(String.valueOf(var.getAffectedHetGtyNum()));
                cellValueList.add(String.valueOf(var.getAffectedAltHomGtyNum()));

            }
            if (needAccoundUnaffect) {
                cellValueList.add(String.valueOf(var.getUnaffectedRefHomGtyNum()));
                cellValueList.add(String.valueOf(var.getUnaffectedHetGtyNum()));
                cellValueList.add(String.valueOf(var.getUnaffectedAltHomGtyNum()));
            }
            if (needAccoundAll) {
                cellValueList.add(String.valueOf(var.getAffectedRefHomGtyNum()));
                cellValueList.add(String.valueOf(var.getAffectedHetGtyNum()));
                cellValueList.add(String.valueOf(var.getAffectedAltHomGtyNum()));
            }

            if (outAltAf) {
                if (var.altAF == -1) {
                    cellValueList.add("N");
                } else if (Float.isNaN(var.altAF)) {
                    cellValueList.add(".");
                } else {
                    cellValueList.add(String.valueOf(var.altAF));
                }

            }

            if (var.scores != null) {
                for (double sc : var.scores) {
                    if (Double.isNaN(sc)) {
                        cellValueList.add(".");
                    } else {
                        cellValueList.add(String.valueOf(sc));
                    }
                }
            } else {
                for (int i = 0; i < scoreNum; i++) {
                    cellValueList.add(".");
                }
            }

            if (var.featureValues != null) {
                for (int i = 0; i < featureNum; i++) // for (int i = 0; i < featureNum; i++) 
                {
                    cellValueList.add(var.featureValues[i]);
                }
            } else {
                for (int i = 0; i < featureNum; i++) {
                    cellValueList.add(".");
                }
            }
            contens.add(cellValueList.toArray(new String[cellValueList.size()]));
        }

        return contens;
    }

    private List<String[]> makemRNATableList() {
        int socreNum = geneScoreLabels.size();
        int featureNum = geneFeatureLabels.size();
        int altAfColumn = 0;

        int colNum = 5 + altAfColumn + socreNum + featureNum;
        String[] titleNames = new String[colNum];
        int avaibleColNum = 5 + altAfColumn + socreNum;
        titleNames[0] = "mRNAID";
        titleNames[1] = "GeneSymbol";
        titleNames[2] = "Chromosome";
        titleNames[3] = "StartPosition";
        titleNames[4] = "EndPosition";

        for (int i = 0; i < socreNum; i++) {
            titleNames[i + 5 + altAfColumn] = geneScoreLabels.get(i);
        }
        for (int i = 0; i < featureNum; i++) {
            titleNames[i + 5 + altAfColumn + socreNum] = geneFeatureLabels.get(i);
        }

        int index = 0;
        StringBuilder sb = new StringBuilder();
        List<String[]> contens = new ArrayList<String[]>();
        contens.add(titleNames);
        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (mRNA mrna : chromosomes[chromIndex].mRNAList) {
                String[] cells = new String[colNum];
                //format:  chr1 1158531 A/C/G/T rs
                cells[0] = mrna.refID;
                cells[1] = mrna.geneSymb;
                cells[2] = chromosomes[chromIndex].getName();
                cells[3] = String.valueOf(mrna.start);
                cells[4] = String.valueOf(mrna.end);

                avaibleColNum = 5 + altAfColumn;
                if (mrna.testValues != null) {
                    for (double sc : mrna.testValues) {
                        cells[avaibleColNum] = (String.valueOf(sc));
                        avaibleColNum++;
                    }
                }
                avaibleColNum = 5 + socreNum + altAfColumn;
                if (mrna.featureValues != null) {
                    for (String val : mrna.featureValues) {
                        cells[avaibleColNum] = (val);
                        avaibleColNum++;
                    }

                }
                contens.add(cells);
            }
        }

        return contens;
    }

    private List<String[]> makemRNAVariantsTableList(boolean outAltAf) throws Exception {
        int mRNASocreNum = geneScoreLabels.size();
        int mRNAfeatureNum = geneFeatureLabels.size();
        int altAfColumn = 0;

        int varSocreNum = variantScoreLabels.size();
        int varFeatureNum = variantFeatureLabels.size();

        if (outAltAf) {
            altAfColumn = 1;
        }

        int colNum = 5 + mRNASocreNum + mRNAfeatureNum + 4 + altAfColumn + varSocreNum + varFeatureNum;
        String[] titleNames = new String[colNum];
        int avaiblemRNAColNum = 5 + mRNASocreNum;
        int totailmRNAColumn = 5 + mRNASocreNum + mRNAfeatureNum;
        titleNames[0] = "mRNAID";
        titleNames[1] = "GeneSymbol";
        titleNames[2] = "Chromosome";
        titleNames[3] = "mRNAStartPosition";
        titleNames[4] = "mRNAEndPosition";

        for (int i = 0; i < mRNASocreNum; i++) {
            titleNames[i + 5] = geneScoreLabels.get(i);
        }
        for (int i = 0; i < mRNAfeatureNum; i++) {
            titleNames[i + 5 + mRNASocreNum] = geneFeatureLabels.get(i);
        }

        titleNames[totailmRNAColumn] = "VarStartPosition";
        titleNames[totailmRNAColumn + 1] = "ReferenceAlternativeAllele";
        titleNames[totailmRNAColumn + 2] = "GeneFeatures";
        titleNames[totailmRNAColumn + 3] = "GeneFeature";
        if (outAltAf) {
            titleNames[totailmRNAColumn + 4] = "MaxDBAltAF";
        }

        for (int i = 0; i < varSocreNum; i++) {
            titleNames[totailmRNAColumn + i + 4 + altAfColumn] = variantScoreLabels.get(i);

        }

        for (int i = 0; i < varFeatureNum; i++) {
            titleNames[totailmRNAColumn + i + 4 + altAfColumn + varSocreNum] = variantFeatureLabels.get(i);
        }

        int avaibleColNum = 0;
        int index = 0;
        StringBuilder sb = new StringBuilder();
        List<String[]> contens = new ArrayList<String[]>();
        contens.add(titleNames);
        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (mRNA mrna : chromosomes[chromIndex].mRNAList) {
                String[] cells = new String[colNum];
                //format:  chr1 1158531 A/C/G/T rs
                cells[0] = mrna.refID;
                cells[1] = mrna.geneSymb;
                cells[2] = chromosomes[chromIndex].getName();
                cells[3] = String.valueOf(mrna.start);
                cells[4] = String.valueOf(mrna.end);

                avaiblemRNAColNum = 5;
                if (mrna.testValues != null) {
                    for (double sc : mrna.testValues) {
                        cells[avaiblemRNAColNum] = (String.valueOf(sc));
                        avaiblemRNAColNum++;
                    }
                }
                avaiblemRNAColNum = 5 + mRNASocreNum;
                if (mrna.featureValues != null) {
                    for (String val : mrna.featureValues) {
                        cells[avaiblemRNAColNum] = (val);
                        avaiblemRNAColNum++;
                    }

                }

                Variant[] vars = lookupVariants(chromosomes[chromIndex].getName(), mrna.start, mrna.end);
                if (vars == null) {
                    contens.add(cells);
                    continue;
                }
                for (int i = 0; i < vars.length; i++) {
                    Variant var = vars[i];
                    if (i > 0) {
                        cells = new String[colNum];
                        cells[1] = mrna.geneSymb;
                    }
                    //format:  chr1 1158631 A/C/G/T rs 
                    cells[totailmRNAColumn] = String.valueOf(var.refStartPosition);

                    sb.append(var.getRefAllele());
                    for (String alle : var.getAltAlleles()) {
                        sb.append('/');
                        sb.append(alle);
                    }
                    cells[totailmRNAColumn + 1] = sb.toString();
                    // if (mrna.geneSymb==null||!mrna.geneSymb.startsWith("HLA")) continue;

                    sb.delete(0, sb.length());
                    cells[totailmRNAColumn + 2] = var.getLabel();
                    cells[totailmRNAColumn + 3] = org.cobi.kggseq.Constants.VAR_FEATURE_NAMES[var.smallestFeatureID];

                    if (outAltAf) {
                        if (var.altAF == -1) {
                            cells[totailmRNAColumn + 4] = "N";
                        } else if (Float.isNaN(var.altAF)) {
                            cells[totailmRNAColumn + 4] = ".";
                        } else {
                            cells[totailmRNAColumn + 4] = String.valueOf(var.altAF);
                        }
                    }
                    avaibleColNum = totailmRNAColumn + 4 + altAfColumn;
                    if (var.scores != null) {
                        for (double sc : var.scores) {
                            cells[avaibleColNum] = (String.valueOf(sc));
                            avaibleColNum++;
                        }
                    }
                    avaibleColNum = totailmRNAColumn + 4 + altAfColumn + varSocreNum;
                    if (var.featureValues != null) {
                        for (String val : var.featureValues) {
                            cells[avaibleColNum] = (val);
                            avaibleColNum++;
                        }
                    }
                    contens.add(cells);
                }

            }
        }

        return contens;
    }

    public void export2ExelFile(String exportPath, boolean altAf) throws Exception {
        List<String[]> contens = null;
        if (geneNum > 0 && varNum > 0) {
            contens = makemRNAVariantsTableList(altAf);
            LocalExcelFile.writeArray2XLSXFile(exportPath, contens, true, -1, 1);
            // LocalExcelFile.writeArray2ExcelFile(exportPath, contens, true, 1);
        } else if (varNum > 0) {
            contens = makeVariantTableList(altAf);
            LocalExcelFile.writeArray2XLSXFile(exportPath, contens, true, -1, 4);
            // LocalExcelFile.writeArray2ExcelFile(exportPath, contens, true, 1);
        } else if (geneNum > 0) {
            contens = makemRNATableList();

        }
    }

    public void export2ExelFile(String exportPath, int chromID, boolean needHead, boolean altAf) throws Exception {
        List<String[]> contens = null;
        if (geneNum > 0 && varNum > 0) {
            contens = makeVariantTableList(chromID, needHead, altAf);
            LocalExcelFile.writeArray2XLSXFile(exportPath, contens, needHead, -1, 1);
            // LocalExcelFile.writeArray2ExcelFile(exportPath, contens, true, 1);
        } else if (varNum > 0) {
            contens = makeVariantTableList(chromID, needHead, altAf);
            LocalExcelFile.appendArray2XLSXFile(exportPath, contens, needHead, 4);
            // LocalExcelFile.writeArray2ExcelFile(exportPath, contens, true, 1);
        } else if (geneNum > 0) {
            contens = makemRNATableList();

        }
    }

    public void export2FlatText(String exportPath, boolean altAf) throws Exception {
        List<String[]> contens = null;
        if (geneNum > 0 && varNum > 0) {
            contens = makemRNAVariantsTableList(altAf);
            LocalFile.writeData(exportPath, contens, "\t", false);
        } else if (varNum > 0) {
            contens = makeVariantTableList(altAf);
            LocalFile.writeData(exportPath, contens, "\t", false);
        } else if (geneNum > 0) {
            contens = makemRNATableList();
            LocalFile.writeData(exportPath, contens, "\t", false);
        } else {
            String info = "No sequence variant(s) or gene(s) left!";
            System.out.println(info);
        }
    }

    public void export2ATmpFormat(BufferedWriter bw, int chromIndex) throws Exception {
        if (chromosomes[chromIndex] == null) {
            return;
        }
        for (Variant var : chromosomes[chromIndex].variantList) {
            /*
             gene	classification	type	chr	pos	ref_allele	newbase
             NFASC	SNP	Missense_Mutation	1	204939750	C	T
             MSRB2	SNP	Splice_site	10	23393074	G	A
             KIAA1462	SNP	Missense_Mutation	10	30317023	C	G
             PRKCQ	SNP	Silent	10	6527124	C	T
             C10orf62	SNP	Missense_Mutation	10	99350211	G	A
             DDX10	SNP	Missense_Mutation	11	108546431	A	G
             OR52M1	SNP	Missense_Mutation	11	4566854	G	A
             KRT1	SNP	Missense_Mutation	12	53071954	A	T
             RNF17	SNP	Missense_Mutation	13	25404700	G	A
             RTN1	SNP	Silent	14	60194064	C	T
             ATP2A1	SNP	Splice_site	16	28898595	G	A
             TP53	SNP	Missense_Mutation	17	7577121	G	A              
             */

            int hetNum = var.getAffectedHetGtyNum();
            int refHomNum = var.getAffectedRefHomGtyNum();
            int altHomNum = var.getAffectedAltHomGtyNum();
            if (refHomNum < altHomNum) {
                continue;
            }
            if (var.isIndel) {
                continue;
            }
            //only singleton
            if (hetNum + altHomNum * 2 > 1) {
                // continue;
            }

            bw.write(chromosomes[chromIndex].getName());
            bw.write("\t");
            bw.write(String.valueOf(var.refStartPosition));
            bw.write("\t");
            bw.write(var.getRefAllele());
            bw.write("\t");
            bw.write(var.getAltAlleles()[0]);
            bw.write("\t");
            bw.write("SNP\t" + (hetNum + altHomNum * 2) + "\n");


            /*
             for (String altAllele : var.getAltAlleles()) {
             for (int i = 0; i < hetNum; i++) {
             bw.write(chromosomes[chromIndex].getName());
             bw.write("\t");
             bw.write(String.valueOf(var.refStartPosition));
             bw.write("\t");
             bw.write(var.getRefAllele());
             bw.write("\t");
             bw.write(altAllele);
             bw.write("\t");
             bw.write("SNP\n");
             }
             for (int i = 0; i < altHomNum; i++) {
             bw.write(chromosomes[chromIndex].getName());
             bw.write("\t");
             bw.write(String.valueOf(var.refStartPosition));
             bw.write("\t");
             bw.write(var.getRefAllele());
             bw.write("\t");
             bw.write(altAllele);
             bw.write("\t");
             bw.write("SNP\n");
             }
             for (int i = 0; i < altHomNum; i++) {
             bw.write(chromosomes[chromIndex].getName());
             bw.write("\t");
             bw.write(String.valueOf(var.refStartPosition));
             bw.write("\t");
             bw.write(var.getRefAllele());
             bw.write("\t");
             bw.write(altAllele);
             bw.write("\t");
             bw.write("SNP\n");
             }
             }
             */
        }

    }

    public void export2ANNOVARAnnot(BufferedWriter bw, int chromIndex) throws Exception {
        int signNum = 0;
        if (chromosomes[chromIndex] == null) {
            return;
        }

        String ref = null;
        String currChr = chromosomes[chromIndex].getName();
        int standardFinalPos = 0;
        for (Variant var : chromosomes[chromIndex].variantList) {
            ref = var.getRefAllele();
            standardFinalPos = var.refStartPosition;

            for (String alt : var.getAltAlleles()) {
                //Use the format used by ANNOVAR site information
                //  //format:1	45113	-	0TATGG	0.715732
///1	53599	CTA	3	0.890916
//1	223450	CT	2	0.207385
                //only one alternative alleles; the most common  scenario
                if (!var.isIndel) {
                    //substitution 
                    bw.write(currChr);
                    bw.write("\t");
                    bw.write(String.valueOf(standardFinalPos));
                    bw.write("\t");
                    bw.write(String.valueOf(ref));
                    bw.write("\t");
                    bw.write(alt);
                    bw.write("\t");
                    bw.write(String.valueOf(var.altAF));
                    bw.write("\n");
                } else {
                    if (alt.charAt(0) == '+') {
                                //insertion
                                /*examples 
                         insertion1
                         chr1 1900106 . TCT TCTCCT 217 . INDEL;DP=62;AF1=0.5;CI95=0.5,0.5;DP4=17,9,18,12;MQ=60;FQ=217;PV4=0.78,1,1,0.1 GT:PL:DP:SP:GQ 0/1:255,0,255:56:-991149567:99
                                
                         insertion2
                         chr1 109883576 . C CAT 214 . INDEL;DP=15;AF1=1;CI95=1,1;DP4=0,0,1,11;MQ=60;FQ=-70.5 GT:PL:DP:SP:GQ 1/1:255,36,0:12:-991149568:69
                         * 
                         */
                        //Use the format used by ANNOVAR site information
                        //  //format:1	45113	-	0TATGG	0.715732
///1	53599	CTA	3	0.890916
//1	223450	CT	2	0.207385

                        bw.write(currChr);
                        bw.write("\t");
                        //insertion
                        bw.write(String.valueOf(standardFinalPos));
                        bw.write("\t");
                        bw.write("-");
                        bw.write("\t0");
                        bw.write(alt.substring(ref.length()));
                        bw.write("\t");
                        bw.write(String.valueOf(var.altAF));
                        bw.write("\n");
                    } else if (alt.charAt(0) == '-') {
                                //deletion     
                                /*examples
                         deletion1
                         chr1 113659065 . ACTCT ACT 214 . INDEL;DP=61;AF1=1;CI95=1,1;DP4=0,0,22,34;MQ=60;FQ=-204 GT:PL:DP:SP:GQ 1/1:255,169,0:56:-991149568:99
                         deletion2
                         chr1 1289367 . CTG C 101 . INDEL;DP=14;AF1=0.5;CI95=0.5,0.5;DP4=5,2,5,1;MQ=60;FQ=104;PV4=1,0.4,1,1 GT:PL:DP:SP:GQ 0/1:139,0,168:13:-991149568:99
                         */
                        //Note it cannot work for multiple deletion alleles like:chr1	158164305	.	TAA	TA,T
                        //Use the format used by ANNOVAR site information
                        //  //format:1	45113	-	0TATGG	0.715732
///1	53599	CTA	3	0.890916
//1	223450	CT	2	0.207385
                        bw.write(currChr);
                        bw.write("\t");
                        //deletion
                        bw.write(String.valueOf(standardFinalPos + alt.length()));
                        bw.write("\t");
                        bw.write(ref.substring(alt.length()));
                        bw.write("\t");
                        bw.write(String.valueOf(ref.length() - alt.length()));
                        bw.write("\t");
                        bw.write(String.valueOf(var.altAF));
                        bw.write("\n");
                    } else {
                        // String info = "Unexpected (REF	ALT) format when parsing at line " + fileLineCounter + ": " + currentLine;
                        // LOG.error(nex, info);
                        // throw new Exception(info);
                    }
                }

            }
        }

    }

    public void export2GeneVarGroupFile(BufferedWriter bw, int chromIndex) throws Exception {
        int signNum = 0;
        if (chromosomes[chromIndex] == null) {
            return;
        }
        Map<String, List<Variant>> geneVars = new HashMap<String, List<Variant>>();
        for (Variant var : chromosomes[chromIndex].variantList) {
            String strGene = var.geneSymb;
            if (strGene == null) {
                continue;
            }
            List<Variant> vars = geneVars.get(strGene);
            if (vars == null) {
                vars = new ArrayList<Variant>();
                geneVars.put(strGene, vars);
            }
            vars.add(var);
        }

        int intGeneNum = geneVars.size();

        List<List<Variant>> geneVarList = new ArrayList<List<Variant>>(geneVars.values());
        List<Variant> lstVariant;
        String chrName = chromosomes[chromIndex].getName();
        String refAllele;
        for (int i = 0; i < intGeneNum; i++) {
            lstVariant = geneVarList.get(i);
            bw.write(lstVariant.get(0).geneSymb);
            for (Variant var : lstVariant) {
                bw.write(" ");
                bw.write(chrName);
                bw.write(":" + var.refStartPosition);
                refAllele = var.getRefAllele();
                bw.write("_" + refAllele + "/");
//[MARKER_ID_K] is a marker key as a format of [CHROM]:[POS]_[REF]/[ALT] (NOTE THAT THIS IS DIFFERENT FROM TYPICAL VCF MARKER ID field)
                for (String altAllele : var.getAltAlleles()) {
                    if (var.isIndel) {
                        if (altAllele.charAt(0) == '+') {
                            bw.write(refAllele);
                            signNum = altAllele.indexOf('+');
                            bw.write(altAllele.substring(signNum + 1));
                        } else if (altAllele.charAt(altAllele.length() - 1) == '+') {
                            signNum = altAllele.indexOf('+');
                            bw.write(altAllele.substring(0, signNum));
                            bw.write(refAllele);
                        } else if (altAllele.charAt(altAllele.length() - 1) == '-') {
                            signNum = altAllele.indexOf('-');
                            bw.write(altAllele.substring(0, signNum));
                            bw.write(refAllele);
                        } else if (altAllele.charAt(0) == '-') {
                            bw.write(refAllele);
                            signNum = altAllele.indexOf('-');
                            bw.write(altAllele.substring(signNum + 1));
                        }
                    } else {
                        bw.write(altAllele);
                    }

                }
            }
            bw.write("\n");
        }

    }

    public void export2ANNOVARInput(BufferedWriter bw, int chromIndex) throws Exception {
        int signNum = 0;
        if (chromosomes[chromIndex] == null) {
            return;
        }
        String chrName = chromosomes[chromIndex].getName();
        for (Variant var : chromosomes[chromIndex].variantList) {
            /*
             * Chr	Start	End	Ref	Obs	Comments
             1	84647761	84647761	C	T	comments: rs6576700 or SNP_A-1780419, a SNP in Affymetrix SNP arrays
             1	13133880	13133881	TC	-	comments: rs59770105, a 2-bp deletion
             1	11326183	11326183	-	AT	comments: rs35561142, a 2-bp insertion
             1	105293754	105293754	A	ATAAA	comments: rs10552169, a block substitution
             13	19695176	20003944	0	-	comments: a 342kb deletion encompassing GJB6, associated with hearing loss                
             */

            for (String altAllele : var.getAltAlleles()) {
                bw.write(chrName);
                bw.write(" ");

                if (var.isIndel) {
                    signNum = 0;
                    if (altAllele.charAt(0) == '+') {
                        for (int t = 0; t < altAllele.length(); t++) {
                            if (altAllele.charAt(t) == '+') {
                                signNum++;
                            } else {
                                break;
                            }
                        }
                        signNum--;
                        bw.write(String.valueOf(var.refStartPosition + signNum));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition + signNum));
                        bw.write(" ");
                        bw.write("-");
                        bw.write(" ");
                        bw.write(altAllele.substring(signNum + 1));
                    } else if (altAllele.charAt(altAllele.length() - 1) == '+') {
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write("-");
                        bw.write(" ");
                        bw.write(altAllele.substring(1));
                    } else if (altAllele.charAt(altAllele.length() - 1) == '-') {
                        for (int t = 0; t < altAllele.length(); t++) {
                            if (altAllele.charAt(t) != '-') {
                                signNum++;
                            } else {
                                break;
                            }
                        }

                        bw.write(String.valueOf(var.refStartPosition + signNum));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition + altAllele.length() - 1));
                        bw.write(" ");
                        bw.write(var.getRefAllele().substring(signNum));
                        bw.write(" ");
                        bw.write("-");
                    } else if (altAllele.charAt(0) == '-') {
                        for (int t = 0; t < altAllele.length(); t++) {
                            if (altAllele.charAt(t) == '-') {
                                signNum++;
                            } else {
                                break;
                            }
                        }

                        bw.write(String.valueOf(var.refStartPosition + 1));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition + signNum));
                        bw.write(" ");
                        bw.write(var.getRefAllele().substring(0, signNum));
                        bw.write(" ");
                        bw.write("-");
                    }

                } else {
                    bw.write(String.valueOf(var.refStartPosition));
                    bw.write(" ");
                    bw.write(String.valueOf(var.refStartPosition));
                    bw.write(" ");
                    bw.write(String.valueOf(var.getRefAllele()));
                    bw.write(" ");
                    bw.write(altAllele);
                }

                bw.write("\n");
            }
        }

    }

    public void addVariant(String chromName, Variant var) {
        if (chromName.startsWith(UNKNOWN_CHROM_NAME0) || chromName.startsWith(UNKNOWN_CHROM_NAME1)) {
            return;
        }
        Integer chromID = chromNameIndexMap.get(chromName);
        if (chromID == null) {
            System.err.println("Unrecognized chromosome name: " + chromName);
            return;
        }
        chromosomes[chromID].variantList.add(var);
    }

    //put this function in stack to speed up the parsing
    private void tokenize(String string, char delimiter, int maxIndex, String[] temp) {
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

    public void export2VCFFormat(BufferedWriter bw, int chromIndex) throws Exception {
        int signNum = 0;
        if (chromosomes[chromIndex] == null) {
            return;
        }

        List<Variant> varList = chromosomes[chromIndex].variantList;
        if (varList.isEmpty()) {
            return;
        }
        String chrNameP = "Chromosome." + STAND_CHROM_NAMES[chromIndex];
        Variant var = null;
        String currentLine = null;
        String[] cells = new String[5];
        String[] alleles;
        StringBuilder sb = new StringBuilder();
        int len, index;
        try {
            File[] files = new File(storagePath).listFiles();
            for (File file : files) {
                if (!file.getName().contains(chrNameP + ".vcf.gz.")) {
                    continue;
                }
                BufferedReader br = LocalFileFunc.getBufferedReader(file.getCanonicalPath());

                while ((currentLine = br.readLine()) != null) {
                    tokenize(currentLine, '\t', 4, cells);
                    int[] indexes = chromosomes[chromIndex].lookupVariantIndexes(Util.parseInt(cells[1]));
                    if (indexes != null) {
                        for (int i = 0; i < indexes.length; i++) {
                            var = chromosomes[chromIndex].variantList.get(indexes[i]);
                            if (var.getRefAllele().equals(cells[3])) {
                                sb.delete(0, sb.length());
                                alleles = var.getAltAlleles();
                                if (var.isIndel) {
                                    for (String altAllele : alleles) {
                                        sb.append(',');
                                        len = altAllele.length();
                                        signNum = 0;
                                        if (altAllele.charAt(0) == '+') {
                                            for (int t = 0; t < len; t++) {
                                                if (altAllele.charAt(t) == '+') {
                                                    signNum++;
                                                } else {
                                                    break;
                                                }
                                            }
                                            sb.append(var.getRefAllele());
                                            sb.append(altAllele.substring(signNum));
                                        } else if (altAllele.charAt(len - 1) == '+') {
                                            index = altAllele.indexOf('+');
                                            if (index >= 0) {
                                                sb.append(altAllele.substring(0, index));
                                            }
                                            sb.append(var.getRefAllele());
                                        } else if (altAllele.charAt(len - 1) == '-') {
                                            for (int t = 0; t < len; t++) {
                                                if (altAllele.charAt(t) != '-') {
                                                    signNum++;
                                                } else {
                                                    break;
                                                }
                                            }
                                            sb.append(altAllele.substring(0, signNum));
                                        } else if (altAllele.charAt(0) == '-') {
                                            for (int t = 0; t < len; t++) {
                                                if (altAllele.charAt(t) == '-') {
                                                    signNum++;
                                                } else {
                                                    break;
                                                }
                                            }
                                            sb.append(var.getRefAllele().substring(0, signNum));
                                        } else {
                                            sb.append(altAllele);
                                        }
                                    }
                                } else {
                                    sb.append(",");
                                    sb.append(alleles[0]);
                                    for (int j = 1; j < alleles.length; j++) {
                                        sb.append(",");
                                        sb.append(alleles[0]);
                                    }
                                }
                                if (sb.substring(1).equals(cells[4])) {
                                    bw.write(currentLine);
                                    bw.write("\n");
                                }
                            }
                        }
                    }
                }
                br.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void export2FlatText(BufferedWriter bw, int chromID, boolean needHead, boolean altAf) throws Exception {
        List<String[]> contens = null;
        if (!chromosomes[chromID].mRNAList.isEmpty() && !chromosomes[chromID].variantList.isEmpty()) {
            contens = makemRNAVariantsTableList(altAf);
            LocalFile.writeData(bw, contens, "\t");
        } else if (!chromosomes[chromID].variantList.isEmpty()) {
            contens = makeVariantTableList(chromID, needHead, altAf);
            LocalFile.writeData(bw, contens, "\t");
        } else if (geneNum > 0) {
            contens = makemRNATableList();
            LocalFile.writeData(bw, contens, "\t");
        } else {
            // String info = "No sequence variant(s) or gene(s) left!";
            //System.out.println(info);
        }
    }

    /*
     Similar to plink, each binary file set must contain three separate files:
     kggseq.ked      (kggseq binary file, genotype information)
     kggseq.fam      (first six columns of mydata.ped) 
     kggseq.kim      (extended MAP file: two extra cols = allele names) 
     */
    public void exportKGGSeqBinaryGty(Chromosome chromosome, BufferedOutputStream fileChannelKed, BufferedWriter bwMap, int[] savedVar) throws Exception {
        if (chromosome == null) {
            return;
        }

        //ByteBuffer bbuffer = null;
        String xx = chromosome.getName();
        byte[] result = new byte[4];

        int spaceNum = 0;
        for (Variant var : chromosome.variantList) {
            if (spaceNum != var.encodedGty.length) {
                spaceNum = var.encodedGty.length;
                //bbuffer = ByteBuffer.allocate(spaceNum * 4);
            }
            bwMap.write(xx);
            bwMap.write("\t");
            if (var.getLabel() == null) {
                bwMap.write("chr" + xx + ":" + var.refStartPosition + "\t");
            } else {
                String label = var.getLabel();
                if (label.startsWith("rs")) {
                    bwMap.write(label + "\t");
                } else {
                    bwMap.write("chr" + xx + ":" + var.refStartPosition + "\t");
                }
            }
            bwMap.write("0\t");
            bwMap.write(String.valueOf(var.refStartPosition));
            bwMap.write("\t");
            bwMap.write(String.valueOf(var.getRefAllele()));
            bwMap.write("\t");
            bwMap.write(var.getAltAlleles()[0]);
            for (int i = 1; i < var.getAltAlleles().length; i++) {
                bwMap.write(",");
                bwMap.write(var.getAltAlleles()[i]);
            }
            bwMap.write("\n");
            savedVar[0]++;

            for (int i = 0; i < var.encodedGty.length; i++) {
                // bbuffer.putInt(var.encodedGty[i]);
                result[0] = (byte) (var.encodedGty[i] >> 24);
                result[1] = (byte) (var.encodedGty[i] >> 16);
                result[2] = (byte) (var.encodedGty[i] >> 8);
                result[3] = (byte) (var.encodedGty[i] /*>> 0*/);
                //Warning the writ int function only write a byte acuallaly
                fileChannelKed.write(result);
                // System.out.print(Integer.toHexString(var.encodedGty[i]));
            }
            //System.out.println();
            // fileChannelKed.flush();
            // bbuffer.flip();
            // fileChannelKed.write(bbuffer.array());
            // bbuffer.clear();
        }

    }

    public void export2FlatTextPlink(Chromosome chromosome, List<Individual> subjectList, int[] pedEncodeGytIDMap, BufferedWriter bwMap, String exportPath, int[] savedPedVar, boolean outGZ) throws Exception {
        String chrName = chromosome.getName();
        BufferedWriter bwPed = null;
        if (chromosome == null) {
            return;
        }
        if (outGZ) {
            bwPed = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(exportPath + ".ped." + chromosome.getId() + ".gz"))));
        } else {
            bwPed = new BufferedWriter(new FileWriter(exportPath + ".ped." + chromosome.getId()));
        }

        for (Variant var : chromosome.variantList) {
            bwMap.write(chrName);
            bwMap.write("\t");
            if (var.getLabel() == null) {
                bwMap.write("chr" + chrName + ":" + var.refStartPosition + "\t");
            } else {
                String label = var.getLabel();
                if (label.startsWith("rs")) {
                    bwMap.write(label + "\t");
                } else {
                    bwMap.write("chr" + chrName + ":" + var.refStartPosition + "\t");
                }
            }
            bwMap.write("0\t");
            bwMap.write(String.valueOf(var.refStartPosition));
            bwMap.write("\n");
            savedPedVar[0]++;
        }

        int[] gtys = null;
        int alleleNum, base = 2;
        int subID = -1;
        int gtyID = 0;
        for (Individual indiv : subjectList) {
            if (indiv == null) {
                continue;
            }
            subID++;
            gtyID = pedEncodeGytIDMap[subID];
            if (gtyID < 0) {
                for (Variant var : chromosome.variantList) {
                    bwPed.write(" ");
                    bwPed.write("0 0");
                }
                bwPed.write("\n");
                continue;
            }
            for (Variant var : chromosome.variantList) {
                alleleNum = var.getAltAlleles().length + 1;
                bwPed.write(" ");

                if (isPhasedGty) {
                    base = GlobalManager.phasedAlleleBitMap.get(alleleNum);
                    gtys = BinaryGtyProcessor.getPhasedGtyAt(var.encodedGty, alleleNum, base, gtyID);
                } else {
                    base = GlobalManager.unphasedAlleleBitMap.get(alleleNum);
                    gtys = BinaryGtyProcessor.getUnphasedGtyAt(var.encodedGty, alleleNum, base, gtyID);
                }

                if (gtys == null) {
                    bwPed.write("0 0");
                } else {
                    if (gtys[0] == 0) {
                        bwPed.write(String.valueOf(var.getRefAllele()));
                    } else {
                        bwPed.write(var.getAltAlleles()[gtys[0] - 1]);
                    }
                    bwPed.write(" ");
                    if (gtys[1] == 0) {
                        bwPed.write(String.valueOf(var.getRefAllele()));
                    } else {
                        bwPed.write(var.getAltAlleles()[gtys[1] - 1]);
                    }
                }
                //for test
                // bwPed.write(var.refStartPosition + "\n");
            }
            bwPed.write("\n");
        }//time-consuming part
        bwPed.close();
        savedPedVar[1] = subID;

    }

    /* To be done
     ANCESTRYMAP format:
     genotype file: see example.ancestrymapgeno in this directory
     snp file:      see example.snp
     indiv file:    see example.ind
     Note that
     The genotype file contains 1 line per valid genotype.  There are 3 columns:
     1st column is SNP name
     2nd column is sample ID
     3rd column is number of reference alleles (0 or 1 or 2)
     Missing genotypes are encoded by the absence of an entry in the genotype file.
     The snp file contains 1 line per SNP.  There are 6 columns (last 2 optional):
     1st column is SNP name
     2nd column is chromosome.  X chromosome is encoded as 23.
     Also, Y is encoded as 24, mtDNA is encoded as 90, and XY is encoded as 91.
     Note: SNPs with illegal chromosome values, such as 0, will be removed
     3rd column is genetic position (in Morgans).  If unknown, ok to set to 0.0.
     4th column is physical position (in bases)
     Optional 5th and 6th columns are reference and variant alleles.
     For monomorphic SNPs, the variant allele can be encoded as X (unknown).
     The indiv file contains 1 line per individual.  There are 3 columns:
     1st column is sample ID.  Length is limited to 39 characters, including
     the family name if that will be concatenated.
     2nd column is gender (M or F).  If unknown, ok to set to U for Unknown.
     3rd column is a label which might refer to Case or Control status, or
     might be a population group label.  If this entry is set to "Ignore", 
     then that individual and all genotype data from that individual will be
     removed from the data set in all convertf output.
     The name "ANCESTRYMAP format" is used for historical reasons only.  This
     software is completely independent of our 2004 ANCESTRYMAP software.
    
     EIGENSTRAT format: used by eigenstrat program
     genotype file: see example.eigenstratgeno
     snp file:      see example.snp (same as above)
     indiv file:    see example.ind (same as above)
     Note that
     The genotype file contains 1 line per SNP.  
     Each line contains 1 character per individual:
     0 means zero copies of reference allele.
     1 means one copy of reference allele.
     2 means two copies of reference allele.
     9 means missing data.
     The program ind2pheno.perl in this directory will convert from 
     example.ind to the example.pheno file needed by the EIGENSTRAT software.
     The syntax is "./ind2pheno.perl example.ind example.pheno".
    
     */
    public void exportEigenStratGty(List<Individual> subjectList, String exportPath) throws Exception {
        if (subjectList == null || subjectList.isEmpty()) {
            return;
        }
        File kedFile = new File(exportPath + ".bed");
        if (kedFile.exists()) {
            kedFile.delete();
        }
        RandomAccessFile rafKed = new RandomAccessFile(kedFile, "rw");
        FileChannel fileChannel = rafKed.getChannel();
        BufferedWriter bwPed = new BufferedWriter(new FileWriter(exportPath + ".fam"));
        BufferedWriter bwMap = new BufferedWriter(new FileWriter(exportPath + ".bim"));

        int varsNum = 0;
        int indivNum = 0;

        /*
         *
         * The autosomes should be coded 1 through 22. The following other codes can be used to specify other chromosome types:
        
         X    X chromosome                    -> 23
         Y    Y chromosome                    -> 24
         XY   Pseudo-autosomal region of X    -> 25
         MT   Mitochondrial                   -> 26
        
         */
        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (Variant var : chromosomes[chromIndex].variantList) {

                //Important: ingnore the variants with more thant 2 alleles
                if (var.getAltAlleles().length > 1) {
                    continue;
                }
                bwMap.write(String.valueOf(chromIndex + 1));

                bwMap.write("\t");
                if (var.getLabel() == null) {
                    bwMap.write("chr" + chromosomes[chromIndex].getName() + ":" + var.refStartPosition + "\t");
                } else {
                    String label = var.getLabel();
                    if (label.startsWith("rs")) {
                        bwMap.write(label + "\t");
                    } else {
                        bwMap.write("chr" + chromosomes[chromIndex].getName() + ":" + var.refStartPosition + "\t");
                    }
                }
                bwMap.write("0\t");
                bwMap.write(String.valueOf(var.refStartPosition));
                bwMap.write("\t");
                bwMap.write(String.valueOf(var.getRefAllele()));
                bwMap.write("\t");
                bwMap.write(var.getAltAlleles()[0]);
                bwMap.write("\n");
                varsNum++;
            }
        }
        bwMap.close();

        for (Individual indiv : subjectList) {
            if (indiv == null) {
                continue;
            }
            indivNum++;
            bwPed.write(indiv.getFamilyID() + " " + indiv.getIndividualID() + " " + indiv.getDadID()
                    + " " + indiv.getMomID() + " " + indiv.getGender() + " " + indiv.getAffectedStatus());
            bwPed.write("\n");
        }
        bwPed.close();

        int bufSize = 1024;

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufSize);

        //|-magic number-
        //01101100 00011011
        byte byteInfo = (byte) 0x6C;
        byteBuffer.put(byteInfo);
        byteInfo = (byte) 0x1B;
        byteBuffer.put(byteInfo);
        //|-mode-| 00000001 (SNP-major)
        //00000001 
        byteInfo = 1;

        byteBuffer.put(byteInfo);
        byteBuffer.flip();
        fileChannel.write(byteBuffer);
        byteBuffer.clear();

        //temperally store the genotype information 
        boolean[] bits = new boolean[5];
        int indivIndex = 8;
        int leftCapcitiy = bufSize;
        int end = 0;
        byte x;
        /*
         missing	Reference homozygous	Heterozygous 	Alternative homozygous
         VCF genotype	./.	0/0	0/1	1/1
         Bits	        00  	01	10	11
         Order	0	1	2	3        
         */

        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (Variant var : chromosomes[chromIndex].variantList) {

//Important: ingnore the variants with more thant 2 alleles
                if (var.getAltAlleles().length > 1) {
                    continue;
                }


                /*
                 * 00  Homozygote "1"/"1"
                 01  Heterozygote
                 11  Homozygote "2"/"2"
                 10  Missing genotype
                 */
//renew the bytes for each variant, each byte for 4 subjects at most
                indivIndex = 4;
                for (Individual indiv : subjectList) {
                    if (indiv == null) {
                        continue;
                    }

                    if (isPhasedGty) {
                        /*
                         missing	Reference homozygous	Heterozygous 	Heterozygous 	Alternative homozygous
                         VCF genotype	.|.	0|0	0|1	1|0	1|1
                         Bits      	000  	001	010	011	100
                         Order	0	1	2	3	4        
                         */
                        // indiv.markerGtySetArray[chromIndex].getPhasedGtyBitAt(var.genotypeIndex, -1, 2, bits);
                        //due to over-flow problme we nee this
                        byteInfo = (byte) ((byteInfo >>> 2) & 0X3F);
                        //Homozygote
                        if (!bits[0] && !bits[1] && bits[2]) {
                        } else if (bits[0] && !bits[1] && !bits[2]) {
                            //Homozygote                          
                            byteInfo = (byte) (byteInfo | 0XC0);
                        } else if ((bits[0] && !bits[1] && bits[2]) || (bits[0] && !bits[1] && !bits[2])) {
                            //Heterozygote
                            //annoyed!!! The plink binary annotation is different from plink excutable tool for the 01 10 definition
                            byteInfo = (byte) (byteInfo | 0X80);
                        } else if (!bits[0] && !bits[1] && !bits[2]) {
                            //missing   
                            byteInfo = (byte) (byteInfo | 0X40);
                        }
                    } else {
//to be modified                       
                        //due to over-flow problme we nee this
                        byteInfo = (byte) ((byteInfo >>> 2) & 0X3F);
                        //Homozygote
                        if (!bits[0] && bits[1]) {
                        } else if (bits[0] && bits[1]) {
                            //Homozygote                          
                            byteInfo = (byte) (byteInfo | 0XC0);
                        } else if (bits[0] && !bits[1]) {
                            //Heterozygote
                            //annoyed!!! The plink binary annotation is different from plink excutable tool for the 01 10 definition
                            byteInfo = (byte) (byteInfo | 0X80);
                        } else if (!bits[0] && !bits[1]) {
                            //missing   
                            byteInfo = (byte) (byteInfo | 0X40);
                        }
                    }
                    indivIndex--;
                    if (indivIndex == 0) {
                        byteBuffer.put(byteInfo);
                        leftCapcitiy--;
                        indivIndex = 4;
                        if (leftCapcitiy == 0) {
                            byteBuffer.flip();
                            fileChannel.write(byteBuffer);
                            byteBuffer.clear();
                            leftCapcitiy = bufSize;
                        }
                        // if (indID.equals("C")) 
                        //System.out.println(BitByteUtil.byteToBinaryString(byteInfo));
                    }
                }
//Subject to debug. do not know why it is not correct
                if (indivIndex != 4) {
                    //offset the missing so that the genotypes can start from the fist position 
                    //for (int i=0;i<indivIndex;i++) byteInfo = (byte) ((byteInfo >>> 2) & 0X3F);
                    byteInfo = (byte) (byteInfo >>> (indivIndex * 2));
                    // System.out.println(end + " : " + BitByteUtil.byteToBinaryString(byteInfo));
                    //end++; 
                    byteBuffer.put(byteInfo);
                    leftCapcitiy--;
                }
                if (leftCapcitiy != bufSize) {
                    byteBuffer.flip();
                    fileChannel.write(byteBuffer);
                    byteBuffer.clear();
                    leftCapcitiy = bufSize;
                }
            }
        }

        fileChannel.close();
        rafKed.close();
        String info = "Genotype of " + varsNum + " sequence variant(s) and " + indivNum + " individuals are saved \nin "
                + exportPath + ".fam " + exportPath + ".bim " + exportPath + ".bed " + " with Plink binary genotype format.";
        System.out.println(info);
    }

    public void export2ANNOVARInput(String exportPath) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(exportPath));
        int signNum = 0;
        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (Variant var : chromosomes[chromIndex].variantList) {
                /*
                 * Chr	Start	End	Ref	Obs	Comments
                 1	84647761	84647761	C	T	comments: rs6576700 or SNP_A-1780419, a SNP in Affymetrix SNP arrays
                 1	13133880	13133881	TC	-	comments: rs59770105, a 2-bp deletion
                 1	11326183	11326183	-	AT	comments: rs35561142, a 2-bp insertion
                 1	105293754	105293754	A	ATAAA	comments: rs10552169, a block substitution
                 13	19695176	20003944	0	-	comments: a 342kb deletion encompassing GJB6, associated with hearing loss                
                 */

                for (String altAllele : var.getAltAlleles()) {
                    bw.write(chromosomes[chromIndex].getName());
                    bw.write(" ");

                    if (var.isIndel) {
                        signNum = 0;
                        if (altAllele.charAt(0) == '+') {
                            for (int t = 0; t < altAllele.length(); t++) {
                                if (altAllele.charAt(t) == '+') {
                                    signNum++;
                                } else {
                                    break;
                                }
                            }
                            signNum--;
                            bw.write(String.valueOf(var.refStartPosition + signNum));
                            bw.write(" ");
                            bw.write(String.valueOf(var.refStartPosition + signNum));
                            bw.write(" ");
                            bw.write("-");
                            bw.write(" ");
                            bw.write(altAllele.substring(signNum + 1));
                        } else if (altAllele.charAt(altAllele.length() - 1) == '+') {
                            bw.write(String.valueOf(var.refStartPosition));
                            bw.write(" ");
                            bw.write(String.valueOf(var.refStartPosition));
                            bw.write(" ");
                            bw.write("-");
                            bw.write(" ");
                            bw.write(altAllele.substring(1));
                        } else if (altAllele.charAt(altAllele.length() - 1) == '-') {
                            for (int t = 0; t < altAllele.length(); t++) {
                                if (altAllele.charAt(t) != '-') {
                                    signNum++;
                                } else {
                                    break;
                                }
                            }

                            bw.write(String.valueOf(var.refStartPosition + signNum));
                            bw.write(" ");
                            bw.write(String.valueOf(var.refStartPosition + altAllele.length() - 1));
                            bw.write(" ");
                            bw.write(var.getRefAllele().substring(signNum));
                            bw.write(" ");
                            bw.write("-");
                        } else if (altAllele.charAt(0) == '-') {
                            for (int t = 0; t < altAllele.length(); t++) {
                                if (altAllele.charAt(t) == '-') {
                                    signNum++;
                                } else {
                                    break;
                                }
                            }

                            bw.write(String.valueOf(var.refStartPosition + 1));
                            bw.write(" ");
                            bw.write(String.valueOf(var.refStartPosition + signNum));
                            bw.write(" ");
                            bw.write(var.getRefAllele().substring(0, signNum));
                            bw.write(" ");
                            bw.write("-");
                        }

                    } else {
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write(String.valueOf(var.getRefAllele()));
                        bw.write(" ");
                        bw.write(altAllele);
                    }

                    bw.write("\n");
                }
            }
        }
        bw.close();
    }

    public void export2EnsembleInput(String exportPath) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(exportPath));
        int i = 0;
        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (Variant var : chromosomes[chromIndex].variantList) {
                /*
                
                 1  909238  909238  G/C  +
                 3  361464  361464  A/-  +
                 5  121187650  121188519  DUP
                
                 */

                for (String altAllele : var.getAltAlleles()) {
                    bw.write(chromosomes[chromIndex].getName());
                    bw.write(" ");
                    if (altAllele.charAt(0) == '+') {
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write("-");
                        bw.write(" ");
                        bw.write(altAllele.substring(1));
                        bw.write(" ");
                        bw.write("+");
                    } else if (altAllele.charAt(0) == '-') {
                        bw.write(String.valueOf(var.refStartPosition + 1));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition + altAllele.length() - 1));
                        bw.write(" ");
                        bw.write(altAllele.substring(1));
                        bw.write(" ");
                        bw.write("-");
                        bw.write(" ");
                        bw.write("+");
                    } else {
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write(String.valueOf(var.refStartPosition));
                        bw.write(" ");
                        bw.write(String.valueOf(var.getRefAllele()));
                        bw.write("/");
                        bw.write(altAllele);
                        bw.write(" ");
                        bw.write("+");
                    }
                    bw.write("\n");
                }
            }
        }
        bw.close();
    }

    public void export2SeattleSeqInput(String exportPath) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(exportPath));
        for (int chromIndex = 0; chromIndex < STAND_CHROM_NAMES.length; chromIndex++) {
            if (chromosomes[chromIndex] == null) {
                continue;
            }
            for (Variant var : chromosomes[chromIndex].variantList) {
                //format: 4 white-space-separated columns in the input file, in the order chromosome (chr1 or chr2 or ... or chr22 or chrX or chrY), genomic coordinate (human NCBI 36, 1-based), reference base, Maq base (using ambiguity codes).
                bw.write("chr" + chromosomes[chromIndex].getName());
                bw.write(" ");
                bw.write(String.valueOf(var.refStartPosition));
                bw.write(" ");
                bw.write(String.valueOf(var.getRefAllele()));
                bw.write(" ");
                bw.write(String.valueOf(var.getAltAlleles()[0]));
                bw.write("\n");
            }
        }
        bw.close();
    }

    public void exportPlinkBinaryGtyFam(List<Individual> subjectList, List<Individual> refSubjectList, String exportPath, int[] intsIndiv, boolean toCompress) throws Exception {
        int indivNum = 0;
        if (subjectList == null || subjectList.isEmpty()) {
            return;
        }
        BufferedWriter bwPed = null;
        if (toCompress) {
            GZIPOutputStream gzOut = new GZIPOutputStream(new FileOutputStream(exportPath + ".merged.fam.gz"));
            bwPed = new BufferedWriter(new OutputStreamWriter(gzOut));
        } else {
            bwPed = new BufferedWriter(new FileWriter(exportPath + ".merged.fam"));
        }

        for (Individual indiv : subjectList) {
            if (indiv == null) {
                continue;
            }
            indivNum++;
            bwPed.write(indiv.getFamilyID() + " " + indiv.getIndividualID() + " " + indiv.getDadID()
                    + " " + indiv.getMomID() + " " + indiv.getGender() + " " + indiv.getAffectedStatus());
            bwPed.write("\n");
        }

        for (Individual indiv : refSubjectList) {
            if (indiv == null) {
                continue;
            }
            indivNum++;
            bwPed.write(indiv.getFamilyID() + " " + indiv.getIndividualID() + " " + indiv.getDadID()
                    + " " + indiv.getMomID() + " " + indiv.getGender() + " " + indiv.getAffectedStatus());
            bwPed.write("\n");
        }
        bwPed.close();
        intsIndiv[0] = indivNum;
//        String info = indivNum + " individuals are saved in " + exportPath + ".merged.fam " + " with Plink binary genotype format.";
//        System.out.println(info);        
    }

    public void exportPlinkBinaryGty(Chromosome chromosome, List<Individual> subjectList, int[] pedEncodeGytIDMap, BufferedOutputStream fileChannel, BufferedWriter bwMap, int[] coutVar) throws Exception {
        if (subjectList == null || subjectList.isEmpty()) {
            return;
        }
        if (chromosome == null) {
            return;
        }

        /*
         *
         * The autosomes should be coded 1 through 22. The following other codes can be used to specify other chromosome types:
        
         X    X chromosome                    -> 23
         Y    Y chromosome                    -> 24
         XY   Pseudo-autosomal region of X    -> 25
         MT   Mitochondrial                   -> 26        
         */
        int chromIndex = chromosome.getId();

        for (Variant var : chromosome.variantList) {
            //Important: ingnore the variants with more thant 2 alleles
            if (var.getAltAlleles().length > 1) {
                continue;
            }
            bwMap.write(String.valueOf(chromIndex + 1));

            bwMap.write("\t");
            if (var.getLabel() == null) {
                bwMap.write("chr" + chromosomes[chromIndex].getName() + ":" + var.refStartPosition + "\t");
            } else {
                String label = var.getLabel();
                if (label.startsWith("rs")) {
                    bwMap.write(label + "\t");
                } else {
                    bwMap.write("chr" + chromosomes[chromIndex].getName() + ":" + var.refStartPosition + "\t");
                }
            }
            bwMap.write("0\t");
            bwMap.write(String.valueOf(var.refStartPosition));
            bwMap.write("\t");
            bwMap.write(String.valueOf(var.getRefAllele()));
            bwMap.write("\t");
            bwMap.write(var.getAltAlleles()[0]);
            bwMap.write("\n");
            coutVar[0]++;
        }

        int bufSize = 1024;
        byte[] byteBuffer = new byte[bufSize];
        byte byteInfo = (byte) 0x0;

        //temperally store the genotype information 
        boolean[] bits = new boolean[3];
        int indivIndex = 8;
        int fillPos = 0;
        int indivSize = subjectList.size();
        /*
         missing	Reference homozygous	Heterozygous 	Alternative homozygous
         VCF genotype	./.	0/0	0/1	1/1
         Bits	        00  	01	10	11
         Order	0	1	2	3        
         */
        int alleleNum;
        int indivI = 0;
        int subID;
        for (Variant var : chromosome.variantList) {
//Important: ingnore the variants with more thant 2 alleles
            if (var.getAltAlleles().length > 1) {
                continue;
            }

            alleleNum = var.getAltAlleles().length + 1;

            /*
             * 00  Homozygote "1"/"1"
             01  Heterozygote
             11  Homozygote "2"/"2"
             10  Missing genotype
             */
//renew the bytes for each variant, each byte for 4 subjects at most
            indivIndex = 4;

            for (indivI = 0; indivI < indivSize; indivI++) {
                subID = pedEncodeGytIDMap[indivI];
                if (subID >= 0) {
                    if (isPhasedGty) {
                        /*
                         missing	Reference homozygous	Heterozygous 	Heterozygous 	Alternative homozygous
                         VCF genotype	.|.	0|0	0|1	1|0	1|1
                         Bits      	000  	001	010	011	100
                         Order	0	1	2	3	4        
                         */
                        BinaryGtyProcessor.getPhasedGtyAt(var.encodedGty, 3, subID, bits);
                        //due to over-flow problme we nee this
                        byteInfo = (byte) ((byteInfo >>> 2) & 0X3F);
                        //Homozygote
                        if (!bits[0] && !bits[1] && bits[2]) {
                        } else if (bits[0] && !bits[1] && !bits[2]) {
                            //Homozygote                          
                            byteInfo = (byte) (byteInfo | 0XC0);
                        } else if ((!bits[0] && bits[1] && !bits[2]) || (!bits[0] && bits[1] && bits[2])) {
                            //Heterozygote
                            //annoyed!!! The plink binary annotation is different from plink excutable tool for the 01 10 definition
                            byteInfo = (byte) (byteInfo | 0X80);
                        } else if (!bits[0] && !bits[1] && !bits[2]) {
                            //missing   
                            byteInfo = (byte) (byteInfo | 0X40);
                        }
                    } else {
                        // http://pngu.mgh.harvard.edu/~purcell/plink/binary.shtml
                        /* 
                         01101100
                         HGFEDCBA

                                AB   00  -- homozygote (first)
                              CD     11  -- other homozygote (second)
                            EF       01  -- heterozygote (third)
                         GH         10  -- missing genotype (fourth)
                         missing	Reference homozygous	Heterozygous	Alternative homozygous
                         VCF genotype	./.	0/0	0/1	1/1
                         Bits    	00	01	10	11
                         Decimal	0	1	2	3                         
                         */
                        BinaryGtyProcessor.getUnphasedGtyAt(var.encodedGty, 2, subID, bits);
                        //due to over-flow problme we nee this
                        byteInfo = (byte) ((byteInfo >>> 2) & 0X3F);
                       // String s1 = String.format("%8s", Integer.toBinaryString(byteInfo & 0xFF)).replace(' ', '0');
                        //System.out.println(s1);
                        //Homozygote
                        if (!bits[0] && bits[1]) {
                        } else if (bits[0] && bits[1]) {
                            //Homozygote                          
                            byteInfo = (byte) (byteInfo | 0XC0);
                        } else if (bits[0] && !bits[1]) {
                            //Heterozygote
                            //annoyed!!! The plink binary annotation is different from plink excutable tool for the 01 10 definition
                            byteInfo = (byte) (byteInfo | 0X80);
                        } else if (!bits[0] && !bits[1]) {
                            //missing   
                            byteInfo = (byte) (byteInfo | 0X40);
                        }
                        //s1 = String.format("%8s", Integer.toBinaryString(byteInfo & 0xFF)).replace(' ', '0');
                        //System.out.println(s1);
                    }
                } else {
                    byteInfo = (byte) (byteInfo | 0X40);
                }

                indivIndex--;
                if (indivIndex == 0) {
                    byteBuffer[fillPos] = byteInfo;
                    fillPos++;
                    indivIndex = 4;
                    if (fillPos == bufSize) {
                        for (int t = 0; t < fillPos; t++) {
                            fileChannel.write(byteBuffer[t]);
                        }
                        fillPos = 0;
                    }
                    // if (indID.equals("C")) 
                    //System.out.println(BitByteUtil.byteToBinaryString(byteInfo));
                }
            }
//Subject to debug. do not know why it is not correct
            if (indivIndex != 4) {
                //offset the missing so that the genotypes can start from the fist position 
                //for (int i=0;i<indivIndex;i++) byteInfo = (byte) ((byteInfo >>> 2) & 0X3F);
                byteInfo = (byte) (byteInfo >>> (indivIndex * 2));
                // System.out.println(end + " : " + BitByteUtil.byteToBinaryString(byteInfo));
                //end++; 
                byteBuffer[fillPos] = byteInfo;
                fillPos++;
            }
            if (fillPos != 0) {
                for (int t = 0; t < fillPos; t++) {
                    fileChannel.write(byteBuffer[t]);
                }
                fillPos = 0;
            }
        }

    }

}
