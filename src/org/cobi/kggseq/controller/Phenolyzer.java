/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import static org.cobi.kggseq.GlobalManager.PLUGIN_PATH;
import org.cobi.kggseq.entity.AnnotationSummarySet;
import org.cobi.kggseq.entity.Chromosome;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.file.LocalFileFunc;
import org.cobi.util.file.Zipper;

/**
 *
 * @author JiangLi
 */
public class Phenolyzer {

    private static final Logger LOG = Logger.getLogger(Phenolyzer.class);
    String strPerlPath;
    String strCMD;
    String fleOutput = "./phenolyzer";
    String strPhenolyzerPath;
    HashMap<String, String> hmpPhenolyzer = null;
    BufferedReader br = null;
    BufferedWriter bw = null;
    List<String> altSearchTerms = null;
//    String strURL = KGGSeq_URL + "download/lib/phenolyzer-master.zip";
    String strURL = "https://github.com/WGLab/phenolyzer/archive/master.zip";

    public Phenolyzer(List<String> searchTerms, String outPath) {
        final int INIT_PROBLEM = 0, WINDOWS = 1, UNIX = 2, POSIX_UNIX = 3, OTHER = 4;
        int os = OTHER;
        try {
            String osName = System.getProperty("os.name");
            if (osName == null) {
                throw new IOException("os.name not found");
            }
            osName = osName.toLowerCase();
            // match  
            if (osName.indexOf("windows") != -1) {
                os = WINDOWS;
                this.strPerlPath = "C:/Strawberry/perl/bin/perl.exe";
            } else if (osName.indexOf("linux") != -1
                    || osName.indexOf("sun os") != -1
                    || osName.indexOf("sunos") != -1
                    || osName.indexOf("solaris") != -1
                    || osName.indexOf("mpe/ix") != -1
                    || osName.indexOf("freebsd") != -1
                    || osName.indexOf("irix") != -1
                    || osName.indexOf("digital unix") != -1
                    || osName.indexOf("unix") != -1
                    || osName.indexOf("mac os x") != -1) {
                os = UNIX;
                this.strPerlPath = "perl";
            } else if (osName.indexOf("hp-ux") != -1
                    || osName.indexOf("aix") != -1) {
                os = POSIX_UNIX;
                this.strPerlPath = "perl";
            } else {
                os = OTHER;
                this.strPerlPath = "perl";
            }

        } catch (Exception ex) {
            os = INIT_PROBLEM;
            LOG.error(ex);
        }
        fleOutput = outPath;
        this.strPhenolyzerPath = PLUGIN_PATH + "phenolyzer-master/disease_annotation.pl";
        altSearchTerms = searchTerms;
    }

    public void runPhenolyzer() {
        try {
//            bw=new BufferedWriter(new FileWriter(fleInput));
//            for(int i=0;i<altSearchTerms.size();i++){
//                bw.write((String) altSearchTerms.get(i));
//                bw.newLine();
//            }
//            bw.close();
            String strItems = "";
            for (int i = 0; i < altSearchTerms.size(); i++) {
                strItems += altSearchTerms.get(i) + ";";
            }
            strItems = strItems.substring(0, strItems.length() - 1);
            File f = new File(fleOutput);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdir();
            }
            String[] params = new String[12];
            params[0] = strPerlPath;
            params[1] = strPhenolyzerPath;
            params[2] = "\"" + strItems + "\"";
            params[3] = "-p";
            params[4] = "-ph";
            params[5] = "-logistic";
            params[6] = "-out";
            params[7] = fleOutput;
            params[8] = "-addon";
            params[9] = "DB_DISGENET_GENE_DISEASE_SCORE,DB_GAD_GENE_DISEASE_SCORE,DB_GENECARDS_GENE_DISEASE_SCORE";
            params[10] = "-addon_weight";
            params[11] = "0.25";

            // strCMD = strPerlPath + " " + strPhenolyzerPath + " \"" + strItems + "\" -p -ph -logistic -out " + fleOutput + " -addon DB_DISGENET_GENE_DISEASE_SCORE,DB_GAD_GENE_DISEASE_SCORE,DB_GENECARDS_GENE_DISEASE_SCORE -addon_weight 0.25";
            // System.out.println(strCMD);
//            String strTest="C:\\Strawberry\\perl\\bin\\perl.exe D:\\01WORK\\KGGseq\\software\\KGGseq\\plugin\\phenolyzer-master\\disease_annotation.pl 'alzheimer;crohn' -p -ph -logistic -out testPhenolyzer_New\\testPhenolyzerphenolyzer\\out";
//            System.out.println(strTest);
            // Process pr = Runtime.getRuntime().exec(strCMD);
            //Process pr = new ProcessBuilder(strCMD).start();
            Process pr = Runtime.getRuntime().exec(params);

            String line;

            try {
                BufferedReader inputError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                while (((line = inputError.readLine()) != null)) {
                    //  System.out.println(line);
                }

                int exitVal = pr.waitFor();
                pr.destroy();
                inputError.close();

                if (exitVal != 0) {
                    StringBuilder comInfor = new StringBuilder();
                    for (String param : params) {
                        comInfor.append(param);
                        comInfor.append(" ");
                    }
                    LOG.info("Phenolyzer run unsuccessfully by the command: " + comInfor.toString());
                }

            } catch (Exception ex) {
                LOG.error(ex);
            }

//            try (BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
//                while ((line = input.readLine()) != null) {
//                    System.out.println(line);
//                }
//            }
//            System.out.println("Done.");
//            pr.waitFor();
//            pr.destroy();
        } catch (IOException ex) {
            LOG.error(ex);
        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    public void parseResult() {
        try {
//            if(fleInput==null || !fleInput.exists()) return;
            hmpPhenolyzer = new HashMap<String, String>();
            br = new BufferedReader(new FileReader(fleOutput + ".final_gene_list"));

            String strLine;
//            while((strLine=br.readLine())!=null){
//                if(strLine.contains("Normalized score")){
//                    String[] strItems=strLine.split("\t");
//                    double[] dblScore=new double[2];
//                    dblScore[0]=Double.parseDouble(strItems[1].substring(18));
//                    dblScore[1]=Double.parseDouble(strItems[2].substring(11));
//                    hmpPhenolyzer.put(strItems[0], dblScore);
//                }
//            }

            while ((strLine = br.readLine()) != null) {
                String[] strItems = strLine.split("\t");
                hmpPhenolyzer.put(strItems[1], strItems[3]);
            }
            br.close();
            File f = new File(fleOutput);

            if (f.getParentFile().exists()) {
                LocalFileFunc.delAll(f.getParentFile());
            }
        } catch (FileNotFoundException ex) {
            LOG.info(ex);

        } catch (IOException ex) {
            LOG.info(ex);
        }
    }

    public HashMap<String, String> getHashMap() {
        return this.hmpPhenolyzer;
    }

    public void addScore(Chromosome chromosome, AnnotationSummarySet ass, HashMap<String, String> hmpPhenolyzer) {
        int intNum = 0;
        if (chromosome == null) {
            return;
        }

        int varFeatureNum = ass.getAvailableFeatureIndex();
        for (Variant var : chromosome.variantList) {
            if (var.geneSymb == null) {
                var.setFeatureValue(varFeatureNum, null);
//                var.setFeatureValue(ass.getAvailableFeatureIndex() + 1, null);
                continue;
            }
            String strGene = var.geneSymb.toUpperCase();
            if (hmpPhenolyzer.containsKey(strGene)) {
                var.setFeatureValue(varFeatureNum, hmpPhenolyzer.get(strGene));
                intNum++;
            } else {
                var.setFeatureValue(varFeatureNum, ".");
            }
        }

        ass.setAnnotNum(ass.getAnnotNum() + intNum);
        ass.setTotalNum(ass.getTotalNum() + chromosome.mRNAList.size());
        ass.setLeftNum(ass.getLeftNum() + chromosome.mRNAList.size() - intNum);
    }

    public void downloadPhenolyzer() {
        File fleOutputDir = new File(PLUGIN_PATH + "phenolyzer-master.zip");
        try {
            if (!fleOutputDir.getParentFile().exists()) {
                fleOutputDir.getParentFile().mkdirs();
            }
            if (!fleOutputDir.exists()) {

                HttpClient4API.simpleRetriever(strURL, fleOutputDir.getCanonicalPath());
                Zipper ziper = new Zipper();
                ziper.extractZip(fleOutputDir.getCanonicalPath(), fleOutputDir.getParentFile().getCanonicalPath() + File.separator);
            }
        } catch (Exception ex) {
            LOG.error(ex);
        }

    }

    public void downloadPhenolyzer(File fleOutput) throws InterruptedException, Exception {
        try {
            //        File fleOutput=new File("D:\\01WORK\\KGGseq\\tool\\HttpsDownload\\phenolyzer-master.zip");
            if (!fleOutput.getParentFile().exists()) {
                fleOutput.getParentFile().mkdirs();
            }
            boolean needDownload = false;
            if (!fleOutput.exists()) {
                needDownload = true;
            } else {
                //half an year later
                long time = fleOutput.lastModified() / 1000 + 30 * 24 * 60 * 60;
                Date fileData = new Date(time * 1000);
                Date today = new Date();
                //if too small or too early
                if (fleOutput.length() < 3.2 * 1024 * 1024 || today.after(fileData)) {
                    //an incomplete file
                    fleOutput.delete();
                    needDownload = true;
                }
            }

            if (needDownload) {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet(strURL);
                HttpResponse response = httpclient.execute(httpget);
                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();

                long longSize = entity.getContentLength();
                long longAdd = 0;
                if (entity != null) {
                    try (InputStream in = entity.getContent()) {
                        BufferedInputStream bis = new BufferedInputStream(in);
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fleOutput));
                        int intCount;
                        //                    NumberFormat nf=NumberFormat.getPercentInstance();
                        //                    nf.setMinimumFractionDigits(2);
                        //                    nf.setMaximumIntegerDigits(3);
                        //                    System.out.println();
                        byte[] buffer = new byte[10 * 1024];
                        while ((intCount = bis.read(buffer)) != -1) {
                            //                        for(int i=0;i<7;i++)    System.out.print("\b");
                            //                        Thread.sleep(100);
                            //                        System.out.print("\r");
                            bos.write(buffer, 0, intCount);
                            longAdd++;
                            //                        double dbl=longAdd/longSize;
                            //                        System.out.print(nf.format(dbl));
                        }
                        bis.close();
                        bos.close();

                        Zipper ziper = new Zipper();
                        ziper.extractZip(fleOutput.getCanonicalPath(), fleOutput.getParentFile().getCanonicalPath() + File.separator);

                    } catch (IOException ex) {
                        LOG.error(ex);
                    } catch (RuntimeException ex) {
                        httpget.abort();
                    }
                    httpclient.close();
                }
            } else {
               // Zipper ziper = new Zipper();
                //  ziper.extractZip(fleOutput.getCanonicalPath(), fleOutput.getParentFile().getCanonicalPath() + File.separator);
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Phenolyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
