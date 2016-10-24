/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq;

import cern.colt.map.OpenIntIntHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.math.CombinationGenerator;
import static org.cobi.util.math.CombinationGenerator.toBinaryArray;
import static org.cobi.util.math.CombinationGenerator.toBinaryString;

/**
 *
 * @author mxli
 */
public class GlobalManager implements Constants {

    private static final Logger LOG = Logger.getLogger(GlobalManager.class);
    public static String LOCAL_FOLDER = "./";
    public static String RESOURCE_PATH = null;
    public static String PLUGIN_PATH = LOCAL_FOLDER + "plugin/";
    public static String LOCAL_COPY_FOLDER = null;
    public static Map<String, Character> codonTable = new HashMap<String, Character>();
    public static Map<String, Byte> VarFeatureIDMap = new HashMap<String, Byte>();
    public static boolean isConnectInternet = false;

    public static Map<Integer, boolean[]> unphasedGtyCodingMap = new HashMap<Integer, boolean[]>();
    public static Map<String, int[]> codingUnphasedGtyCodingMap = new HashMap<String, int[]>();
    public static Map<Integer, boolean[]> phasedGtyCodingMap = new HashMap<Integer, boolean[]>();
    public static Map<String, int[]> codingPhasedGtyCodingMap = new HashMap<String, int[]>();

    public static OpenIntIntHashMap unphasedAlleleBitMap = new OpenIntIntHashMap();
    public static OpenIntIntHashMap phasedAlleleBitMap = new OpenIntIntHashMap();
    public static List<String> pubMedFilter;
    //& operators
    public static byte[] byteOpers = new byte[]{-128, 64, 32, 16, 8, 4, 2, 1};
    public static int[] intOpers = new int[32];

    public static void initiateVariables(String rfv, String customedResourcePath, int maxGtyAlleleNum) {
        File path = new File(CUIApp.class.getResource("/resource/").getFile());

        //normally, it will be like this file:/D:/home/mxli/MyJava/KGGSeq/dist/kggseq.jar!/resource/
        try {
            //System.out.println(path.getAbsolutePath());
            path = path.getParentFile();
            //System.out.println(path.getAbsolutePath());
            String pathName = path.getAbsolutePath();
            int index1 = pathName.indexOf("build\\classes");
            if (index1 > 0) {
                path = path.getParentFile().getParentFile();
                if (path.exists()) {
                    LOCAL_FOLDER = path.getCanonicalPath();
                }
            } else {

                index1 = pathName.indexOf("/file:");
                if (index1 >= 0) {
                    int index2 = pathName.lastIndexOf('/');
                    pathName = pathName.substring(index1 + 6, index2);
                    path = new File(pathName);
                    if (path.exists()) {
                        LOCAL_FOLDER = pathName;
                    }
                } else {
                    index1 = pathName.indexOf("\\file:");
                    int index2 = pathName.lastIndexOf("\\");
                    pathName = pathName.substring(index1 + 7, index2);
                    //System.out.println(pathName);
                    path = new File(pathName);
                    if (path.exists()) {
                        LOCAL_FOLDER = pathName;
                    }
                }

            }

            PLUGIN_PATH = LOCAL_FOLDER + "/plugin/";

            // System.out.println(RESOURCE_PATH);
            // System.out.println(LOCAL_FOLDER);
            if (customedResourcePath != null) {
                RESOURCE_PATH = customedResourcePath + "/";

            } else {
                RESOURCE_PATH = LOCAL_FOLDER + "/resources/";
            }
            LOCAL_COPY_FOLDER = LOCAL_FOLDER + "/updated/";

            codonTable.put("TTT", 'F');
            codonTable.put("TTC", 'F');
            codonTable.put("TTA", 'L');
            codonTable.put("TTG", 'L');
            codonTable.put("CTT", 'L');
            codonTable.put("CTC", 'L');
            codonTable.put("CTA", 'L');
            codonTable.put("CTG", 'L');
            codonTable.put("ATT", 'I');
            codonTable.put("ATC", 'I');
            codonTable.put("ATA", 'I');
            codonTable.put("ATG", 'M');
            codonTable.put("GTT", 'V');
            codonTable.put("GTC", 'V');
            codonTable.put("GTA", 'V');
            codonTable.put("GTG", 'V');
            codonTable.put("TCT", 'S');
            codonTable.put("TCC", 'S');
            codonTable.put("TCA", 'S');
            codonTable.put("TCG", 'S');
            codonTable.put("CCT", 'P');
            codonTable.put("CCC", 'P');
            codonTable.put("CCA", 'P');
            codonTable.put("CCG", 'P');
            codonTable.put("ACT", 'T');
            codonTable.put("ACC", 'T');
            codonTable.put("ACA", 'T');
            codonTable.put("ACG", 'T');
            codonTable.put("GCT", 'A');
            codonTable.put("GCC", 'A');
            codonTable.put("GCA", 'A');
            codonTable.put("GCG", 'A');
            codonTable.put("TAT", 'Y');
            codonTable.put("TAC", 'Y');
            codonTable.put("TAA", '*');
            codonTable.put("TAG", '*');
            codonTable.put("CAT", 'H');
            codonTable.put("CAC", 'H');
            codonTable.put("CAA", 'Q');
            codonTable.put("CAG", 'Q');
            codonTable.put("AAT", 'N');
            codonTable.put("AAC", 'N');
            codonTable.put("AAA", 'K');
            codonTable.put("AAG", 'K');
            codonTable.put("GAT", 'D');
            codonTable.put("GAC", 'D');
            codonTable.put("GAA", 'E');
            codonTable.put("GAG", 'E');
            codonTable.put("TGT", 'C');
            codonTable.put("TGC", 'C');
            codonTable.put("TGA", '*');
            codonTable.put("TGG", 'W');
            codonTable.put("CGT", 'R');
            codonTable.put("CGC", 'R');
            codonTable.put("CGA", 'R');
            codonTable.put("CGG", 'R');
            codonTable.put("AGT", 'S');
            codonTable.put("AGC", 'S');
            codonTable.put("AGA", 'R');
            codonTable.put("AGG", 'R');
            codonTable.put("GGT", 'G');
            codonTable.put("GGC", 'G');
            codonTable.put("GGA", 'G');
            codonTable.put("GGG", 'G');

            for (int i = 0; i < intOpers.length; i++) {
                intOpers[i] = 1 << (31 - i);
                //   System.out.println(Integer.toBinaryString(intOpers[i]));
            }
            for (int i = 0; i < VAR_FEATURE_NAMES.length; i++) {
                VarFeatureIDMap.put(VAR_FEATURE_NAMES[i], (byte) i);
            }

            //----Parepare genotype coding
            CombinationGenerator x;
            int[] indices = null;

            for (int alleleNum = 2; alleleNum <= maxGtyAlleleNum; alleleNum++) {
                // String[] gtys = new String[alleleNum * (alleleNum + 1) / 2];
                int[] gtys = new int[alleleNum * (alleleNum + 1) / 2];
                String bits = Integer.toBinaryString(gtys.length);
                int base = bits.length();
                unphasedAlleleBitMap.put(alleleNum, base);
                x = new CombinationGenerator(alleleNum, 2);
                int s = 0;
                Arrays.fill(gtys, 0);

                while (x.hasMore()) {
                    indices = x.getNext();
                    if (indices[1] - indices[0] == 1) {
                        gtys[s] = indices[0] | (indices[0] << 8) | (alleleNum << 16);
                        s++;
                    }
                    //please note the order
                    gtys[s] = indices[0] | (indices[1] << 8) | (alleleNum << 16);
                    s++;
                }
                //genotype coding
                gtys[s] = indices[1] | (indices[1] << 8) | (alleleNum << 16);

                s = 0;

                for (s = 0; s < gtys.length; s++) {
                    int str = gtys[s];
                    String bitS = CombinationGenerator.toBinaryString(s + 1, base);
                    unphasedGtyCodingMap.put(str, CombinationGenerator.toBinaryArray(s + 1, base));
                    int[] alleles = new int[2];
                    alleles[0] = str & 0XFF;
                    alleles[1] = str & 0XFF00;
                    alleles[1] = alleles[1] >>> 8;
                    codingUnphasedGtyCodingMap.put(bitS + ":" + alleleNum, alleles);
                }
            }
            int str;
            //   String str = codingUnphasedGtyCodingMap.get("01:2");
            //  System.out.println(str);

            for (int alleleNum = 2; alleleNum <= maxGtyAlleleNum; alleleNum++) {
                int[] gtys = new int[alleleNum * (alleleNum)];
                String bits = Integer.toBinaryString(gtys.length);
                int base = bits.length();
                phasedAlleleBitMap.put(alleleNum, base);
                int s = 0;
                Arrays.fill(gtys, 0);
                for (int i = 0; i < alleleNum; i++) {
                    for (int j = 0; j < alleleNum; j++) {
                        gtys[s] = i | (j << 8) | (alleleNum << 16);
                        s++;
                    }
                }
                s = 0;

                for (s = 0; s < gtys.length; s++) {
                    str = gtys[s];
                    String bitS = toBinaryString(s + 1, base);

                    phasedGtyCodingMap.put(str, toBinaryArray(s + 1, base));

                    int[] alleles = new int[2];
                    alleles[0] = str & 0XFF;
                    alleles[1] = str & 0XFF00;
                    alleles[1] = alleles[1] >>> 8;
                    codingPhasedGtyCodingMap.put(bitS + ":" + alleleNum, alleles);
                    //  System.out.println(str + "\t" + bitS);
                }
            }

            pubMedFilter = new ArrayList<String>();
            pubMedFilter.add("gene");
            pubMedFilter.add("genes");
            pubMedFilter.add("mRNA");
            pubMedFilter.add("protein");
            pubMedFilter.add("proteins");
            pubMedFilter.add("transcription");
            pubMedFilter.add("transcript");
            pubMedFilter.add("transcripts");
            pubMedFilter.add("expressed");
            pubMedFilter.add("expression");
            pubMedFilter.add("expressions");
            pubMedFilter.add("locus");
            pubMedFilter.add("loci");
            pubMedFilter.add("SNP");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void checkConnection() {
        String url = KGGSeq_URL + URL_FILE_PATHES[0];
        try {
            isConnectInternet = HttpClient4API.checkConnection(url);
            if (!GlobalManager.isConnectInternet) {
                //String msg = "Sorry, I cannot connect to website to update kggseq and relevant resources! Please check your internet configurations!\n";

                //LOG.error(msg);
                // System.err.println(msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
