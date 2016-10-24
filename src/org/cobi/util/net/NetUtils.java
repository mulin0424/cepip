/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.cobi.kggseq.Constants;
import org.cobi.kggseq.GlobalManager;
import org.cobi.kggseq.Options;
import org.cobi.util.download.stable.DownloadTaskEvent;
import org.cobi.util.download.stable.DownloadTaskListener;
import org.cobi.util.download.stable.HttpClient4API;
import org.cobi.util.download.stable.HttpClient4DownloadTask;
import org.cobi.util.file.Tar;
import org.cobi.util.file.Zipper;

/**
 *
 * @author mxli
 */
public class NetUtils implements Constants {

    public static void updateLocal() throws Exception {
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File copiedFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            File targetFile = new File(GlobalManager.LOCAL_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            //a file with size less than 1k is not normal
            if (copiedFile.length() > 1024 && copiedFile.length() != targetFile.length()) {
                copyFile(targetFile, copiedFile);
            }
        }
    }

    public static void copyFile(File targetFile, File sourceFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!sourceFile.exists()) {
                return;
            }

            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    targetFile.deleteOnExit();
                    //System.err.println("Cannot delete " + targetFile.getCanonicalPath());
                }
            }
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024 * 5];
            int size;
            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
                out.flush();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public static boolean needUpdate() throws Exception {
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            if (newLibFile.exists()) {
                long fileSize = newLibFile.length();
                String url = GlobalManager.KGGSeq_URL + URL_FILE_PATHES[i];
                long netFileLen = HttpClient4API.getContentLength(url);
                if (netFileLen <= 1024) {
                    return false;
                }
                if (fileSize != netFileLen) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean checkLibFileVersion() throws Exception {
        List<String> updatedLocalFiles = new ArrayList<String>();
        List<String> updatedURLFiles = new ArrayList<String>();
        boolean hasUpdated = false;
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            if (!newLibFile.exists()) {
                updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                updatedURLFiles.add(URL_FILE_PATHES[i]);
            } else {
                long fileSize = newLibFile.length();
                String url = GlobalManager.KGGSeq_URL + URL_FILE_PATHES[i];

                long netFileLen = HttpClient4API.getContentLength(url);
                if (netFileLen <= 0) {
                    updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                    updatedURLFiles.add(URL_FILE_PATHES[i]);
                }
                if (fileSize != netFileLen) {
                    updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                    updatedURLFiles.add(URL_FILE_PATHES[i]);
                }
            }
        }

        if (!updatedLocalFiles.isEmpty()) {
            int MAX_TASK = 1;
            int runningThread = 0;
            ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
            CompletionService serv = new ExecutorCompletionService(exec);
            System.out.println("Updating libraries...");
            int filesNum = updatedLocalFiles.size();

            for (int i = 0; i < filesNum; i++) {
                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(GlobalManager.KGGSeq_URL + updatedURLFiles.get(i), 10);
                File newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + updatedLocalFiles.get(i));
                File libFolder = newLibFile.getParentFile();
                if (!libFolder.exists()) {
                    libFolder.mkdirs();
                }
                task.setLocalPath(newLibFile.getCanonicalPath());

                final String dbLabel = newLibFile.getName();
                task.addTaskListener(new DownloadTaskListener() {

                    @Override
                    public void autoCallback(DownloadTaskEvent event) {
                        int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                        String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                        System.out.print(infor);
                        char[] bs = new char[infor.length()];
                        Arrays.fill(bs, '\b');
                        System.out.print(bs);
                    }

                    @Override
                    public void taskCompleted() throws Exception {
                        String msg1 = dbLabel + " has been downloaded!";
                        System.out.println(msg1);
                    }
                });
                runningThread++;
                TimeUnit.MILLISECONDS.sleep(500);
                serv.submit(task);
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
                hasUpdated = true;
            }
            exec.shutdown();
            System.out.println("The library of has been updated! Please re-initiate this application!");
            updateLocal();
        }
        return hasUpdated;
    }

    public static void checkResourceList(final Options options) {
        int MAX_TASK = 1;
        boolean toDownload = false;
        ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
        CompletionService serv = new ExecutorCompletionService(exec);
        int runningThread = 0;

        try {
            long startTime = System.currentTimeMillis();
            List<String> checkLabels = new ArrayList<String>();
            checkLabels.addAll(options.varaintDBLableList);
            checkLabels.addAll(options.varaintDBLableHardList);
            if (options.geneDBLabels != null) {
                checkLabels.addAll(Arrays.asList(options.geneDBLabels));
            }

            if (options.scoreDBLableList.contains("dbnsfp")) {
                String dbFileName = options.PUBDB_FILE_MAP.get("dbnsfp");
                String url = options.PUBDB_URL_MAP.get("dbnsfp");
                for (int j = 0; j < 24; j++) {
                    String newLabel = "dbnsfp" + STAND_CHROM_NAMES[j];
                    options.PUBDB_FILE_MAP.put(newLabel, dbFileName + STAND_CHROM_NAMES[j] + ".gz");
                    options.PUBDB_URL_MAP.put(newLabel, url + STAND_CHROM_NAMES[j] + ".gz");
                    checkLabels.add(newLabel);
                }
            }
            String refGenomeVersion = options.refGenomeVersion;
            if (options.scoreDBLableList.contains("noncoding")) {
                // download resources from gene-feature specific annotation function
                String[] annotationScore = {"funcnote_CADD.CScore.gz", "funcnote_DANN.gz", "funcnote_fathmm-MKL.gz", "funcnote_FunSeq.gz",
                    "funcnote_FunSeq2.gz", "funcnote_GWAS3D.gz", "funcnote_GWAVA.gz", "funcnote_SuRFR.gz", "funcnote_all.footprints.bed.gz.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.DNase-seq.cmp.gz", "funcnote_encode_megamix.bed.gz.FAIRE-seq.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.Histone.cmp.gz", "funcnote_encode_megamix.bed.gz.Tfbs.cmp.gz"};
                for (int j = 0; j < annotationScore.length; j++) {
                    String newLabel = "noncoding" + j;
                    options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    checkLabels.add(newLabel);
                }
                options.PUBDB_FILE_MAP.put("hgmd_model", "hgmd_model.obj");
                options.PUBDB_URL_MAP.put("hgmd_model", KGGSeq_URL + "/download/resources/modelFile/" + "hgmd_model.obj");
                checkLabels.add("hgmd_model");
            }

            if (options.scoreDBLableList.contains("dbncfp")) {
                // download resources for cell-type specific annotation function
                String[] annotationScore = new String[]{"funcnote_CADD_cscore.gz", "funcnote_CADD_PHRED.gz", "funcnote_DANN_score.gz", "funcnote_Fathmm_MKL_score.gz",
                    "funcnote_FunSeq_score.gz", "funcnote_FunSeq2_score.gz", "funcnote_GWAS3D_score.gz", "funcnote_GWAVA_region_score.gz",
                    "funcnote_GWAVA_TSS_score.gz", "funcnote_GWAVA_unmatched_score.gz", "funcnote_SuRFR_score.gz"};
                for (int j = 0; j < 11; j++) {
                    String newLabel = "regulartory_pathogenic" + j;
                    options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    checkLabels.add(newLabel);
                }
                String newLabel = "cell_signal";
                options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                checkLabels.add(newLabel);
                options.PUBDB_FILE_MAP.put("all_causal_distribution", "all_causal_distribution.zip");
                options.PUBDB_URL_MAP.put("all_causal_distribution", KGGSeq_URL + "/download/resources/hg19/" + "all_causal_distribution.zip");
                options.PUBDB_FILE_MAP.put("all_neutral_distribution", "all_neutral_distribution.zip");
                options.PUBDB_URL_MAP.put("all_neutral_distribution", KGGSeq_URL + "/download/resources/hg19/" + "all_neutral_distribution.zip");
                checkLabels.add("all_causal_distribution");
                checkLabels.add("all_neutral_distribution");
            }

            if (options.dgvcnvAnnotate) {
                checkLabels.add("dgvcnv");
            }
            if (options.mergeGtyDb != null) {
                String piResource = options.PUBDB_URL_MAP.get(options.mergeGtyDb);
                String url = "http://statgenpro.psychiatry.hku.hk/limx/genotypes/";

                if (piResource != null) {
                    if (piResource.contains("_CHROM_")) {
                        for (int j = 0; j < 23; j++) {
                            String newLabel = "mergeddb" + STAND_CHROM_NAMES[j];
                            //remove resources/
                            options.PUBDB_FILE_MAP.put(newLabel, piResource.substring(10).replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            options.PUBDB_URL_MAP.put(newLabel, url + piResource.replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            checkLabels.add(newLabel);
                            // System.out.println(piResource);                            
                        }
                        options.mergeGtyDb = piResource.substring(0, piResource.indexOf(".chr_"));
                        options.mergeGtyDb = options.mergeGtyDb.substring(10);
                    } else {
                        String newLabel = "mergeddb";
                        options.PUBDB_URL_MAP.put(newLabel, url + piResource);
                        checkLabels.add(newLabel);
                        options.mergeGtyDb = piResource.substring(10);
                        options.PUBDB_FILE_MAP.put(newLabel, options.mergeGtyDb);
                        //remove the tar lable
                        options.mergeGtyDb = options.mergeGtyDb.substring(0, options.mergeGtyDb.lastIndexOf('.'));
                    }
                }
            }

            //force to download the small database 
            checkLabels.add("cano");
            checkLabels.add("cura");
            checkLabels.add("onco");
            checkLabels.add("cmop");
            checkLabels.add("onto");

            checkLabels.add("morbidmap");

            checkLabels.add("string");
            checkLabels.add("ideogram");

            checkLabels.add("proteindomain");
            checkLabels.add("uniportrefseqmap");
            checkLabels.add("uniportgencodemap");
            checkLabels.add("uniportucscgenemap");

            checkLabels.add("mendelcausalrare.param");
            checkLabels.add("cancer.param");
            checkLabels.add("cancer.mutsig");
            checkLabels.add("mendelgene");

            checkLabels.add("cancer.null.driver.score");
            if (options.rsid) {
                checkLabels.add("rsid");
            }
            if (options.dbscSNVAnnote) {
                checkLabels.add("dbscSNV");
            }
            if (options.cosmicAnnotate) {
                checkLabels.add("cosmicdb");
            }
            if (options.isTFBSCheck) {
                checkLabels.add("tfbs");
            }
            if (options.isEnhancerCheck) {
                checkLabels.add("enhancer");
            }

            if (options.superdupAnnotate || options.superdupFilter) {
                checkLabels.add("superdup");
            }

            List<String> toDownloadList = new ArrayList<String>();

            boolean toDownloadHGNC = false;
            String hgncFileName = "HgncGene.txt";
            File resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);

            if (!resourceFile.exists()) {
                toDownloadHGNC = true;
                toDownloadList.add(hgncFileName);
            } else {
                //one month later
                long time = resourceFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                Date fileData = new Date(time * 1000);
                Date today = new Date();
                //if too small or too early
                if (resourceFile.length() < 3.2 * 1024 * 1024 || today.after(fileData)) {
                    //an incomplete file
                    resourceFile.delete();
                    toDownloadList.add(hgncFileName);
                    toDownloadHGNC = true;
                }
            }
            for (String dbLabelName : checkLabels) {
                String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + dbFileName);
                // System.out.println(resourceFile.getCanonicalPath());
                if (resourceFile.exists()) {
                    continue;
                }
                toDownloadList.add(dbLabelName);
            }

            if (!GlobalManager.isConnectInternet && !toDownloadList.isEmpty()) {
                String infor = "KGGSeq stopped due to lack of the following resource data:";
                System.out.println(infor);

                for (String dbLabelName : toDownloadList) {
                    System.out.println(dbLabelName);
                }
                System.exit(1);
            } else if (toDownloadList.isEmpty()) {
                return;
            }

            //downloading does not support multiple thread 
            String url = "http://www.genenames.org/cgi-bin/hgnc_downloads?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";

            if (toDownloadHGNC) {
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);
                String msg1 = "Donwloading HGNC gene annotations ... ";
                System.out.println(msg1);
                File parePath = resourceFile.getParentFile();
                if (!parePath.exists()) {
                    parePath.mkdirs();
                }
                HttpClient4API.simpleRetriever(url, resourceFile.getCanonicalPath());
                toDownload = true;
            }
            int i = 0;
            for (String dbLabelName : checkLabels) {
                String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + dbFileName);
                if (resourceFile.exists()) {
                    continue;
                } else {
                    File parePath = resourceFile.getParentFile();
                    if (!parePath.exists()) {
                        parePath.mkdirs();
                    }
                }
                if (i == 0) {
                    System.out.println("Downloading resource " + dbLabelName);
                }
                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(options.PUBDB_URL_MAP.get(dbLabelName), 20);
                task.setDataMd5(HttpClient4API.getContent(options.PUBDB_URL_MAP.get(dbLabelName) + ".md5"));
                File filePath = new File(GlobalManager.RESOURCE_PATH);
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                task.setLocalPath(resourceFile.getCanonicalPath());
                final String dbLabel = dbLabelName;
                task.addTaskListener(new DownloadTaskListener() {

                    @Override
                    public void autoCallback(DownloadTaskEvent event) {
                        int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                        String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                        System.out.print(infor);
                        char[] bs = new char[infor.length()];
                        Arrays.fill(bs, '\b');
                        System.out.print(bs);
                    }

                    @Override
                    public void taskCompleted() throws Exception {
                        // File savedFile = new File(task.getLocalPath()); 
                        if (task.getLocalPath().endsWith(".tar")) {
                            Tar.untar(task.getLocalPath(), GlobalManager.RESOURCE_PATH + options.refGenomeVersion + File.separator);
                            File file = new File(task.getLocalPath());
                            // file.delete();
                        } else if (task.getLocalPath().endsWith(".zip")) {
                            Zipper ziper = new Zipper();
                            File file = new File(task.getLocalPath());
                            ziper.extractZip(task.getLocalPath(), file.getParent() + File.separator);
                            // file.delete();
                        }

                        /**
                         * File file = new File(task.getLocalPath() + ".md5");
                         * // if file doesnt exists, then create it if
                         * (!file.exists()) { file.createNewFile(); }
                         *
                         * FileWriter fw = new
                         * FileWriter(file.getAbsoluteFile()); BufferedWriter bw
                         * = new BufferedWriter(fw);
                         * bw.write(task.getDataMd5()); bw.close();
                         */
                        String msg1 = "Resource " + dbLabel + " has been downloaded!";
                        System.out.println(msg1);
                    }
                });
                runningThread++;
                TimeUnit.MILLISECONDS.sleep(500);
                serv.submit(task);
                toDownload = true;
                i++;
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
            }
            exec.shutdown();

            if (toDownload) {
                StringBuilder inforString = new StringBuilder();
                inforString.append("The lapsed time for downloading is : ");
                long endTime = System.currentTimeMillis();
                inforString.append((endTime - startTime) / 1000.0);
                inforString.append(" Seconds.\n");
                System.out.println(inforString.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void downloadFiles(List<String[]> fileURLList) {
        int MAX_TASK = 1;
        boolean toDownload = false;
        ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
        CompletionService serv = new ExecutorCompletionService(exec);
        int runningThread = 0;
        int i = 0;
        try {
            long startTime = System.currentTimeMillis();

            for (String[] fileURL : fileURLList) {
                File resourceFile = new File(fileURL[0]);
                if (resourceFile.exists()) {
                    long fileSize = resourceFile.length();
                    long netFileLen = HttpClient4API.getContentLength(fileURL[1]);
                    if (netFileLen <= 1024 || fileSize == netFileLen) {
                        continue;
                    }
                }

                if (i == 0) {
                    System.out.println("Downloading resources ...");
                }
                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(fileURL[1], 20);

                if (!resourceFile.getParentFile().exists()) {
                    resourceFile.getParentFile().mkdirs();
                }
                task.setLocalPath(resourceFile.getCanonicalPath());
                final String dbLabel = resourceFile.getName();
                task.addTaskListener(new DownloadTaskListener() {

                    @Override
                    public void autoCallback(DownloadTaskEvent event) {
                        int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                        String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                        System.out.print(infor);
                        char[] bs = new char[infor.length()];
                        Arrays.fill(bs, '\b');
                        System.out.print(bs);
                    }

                    @Override
                    public void taskCompleted() throws Exception {
                        String msg1 = "Resource " + dbLabel + " has been downloaded!";
                        System.out.println(msg1);
                    }
                });
                runningThread++;
                TimeUnit.MILLISECONDS.sleep(500);
                serv.submit(task);
                toDownload = true;

                i++;
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
            }
            exec.shutdown();
            if (toDownload) {
                StringBuilder inforString = new StringBuilder();
                inforString.append("The lapsed time for downloading is : ");
                long endTime = System.currentTimeMillis();
                inforString.append((endTime - startTime) / 1000.0);
                inforString.append(" Seconds.\n");
                System.out.println(inforString.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void checkLatestResource(final Options options) {
        int MAX_TASK = 2;
        boolean toDownload = false;
        ExecutorService exec = Executors.newFixedThreadPool(MAX_TASK);
        CompletionService serv = new ExecutorCompletionService(exec);
        int runningThread = 0;
        int i = 0;
        try {
            long startTime = System.currentTimeMillis();
            List<String> checkLabels = new ArrayList<String>();
            checkLabels.addAll(options.varaintDBLableList);
            checkLabels.addAll(options.varaintDBLableHardList);

            if (options.geneDBLabels != null) {
                checkLabels.addAll(Arrays.asList(options.geneDBLabels));
            }

            if (options.scoreDBLableList.contains("dbnsfp")) {
                String dbFileName = options.PUBDB_FILE_MAP.get("dbnsfp");
                String url = options.PUBDB_URL_MAP.get("dbnsfp");
                for (int j = 0; j < 24; j++) {
                    String newLabel = "dbnsfp" + STAND_CHROM_NAMES[j];
                    options.PUBDB_FILE_MAP.put(newLabel, dbFileName + STAND_CHROM_NAMES[j] + ".gz");
                    options.PUBDB_URL_MAP.put(newLabel, url + STAND_CHROM_NAMES[j] + ".gz");
                    checkLabels.add(newLabel);
                }
            }

            if (options.mergeGtyDb != null) {
                String piResource = options.PUBDB_URL_MAP.get(options.mergeGtyDb);
                String url = "http://statgenpro.psychiatry.hku.hk/limx/genotypes/";

                if (piResource != null) {
                    if (piResource.contains("_CHROM_")) {
                        for (int j = 0; j < 23; j++) {
                            String newLabel = "mergeddb" + STAND_CHROM_NAMES[j];
                            //remove resources/
                            options.PUBDB_FILE_MAP.put(newLabel, piResource.substring(10).replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            options.PUBDB_URL_MAP.put(newLabel, url + piResource.replaceAll("_CHROM_", STAND_CHROM_NAMES[j]));
                            checkLabels.add(newLabel);
                            // System.out.println(piResource);                            
                        }
                        options.mergeGtyDb = piResource.substring(0, piResource.indexOf(".chr_"));
                        options.mergeGtyDb = options.mergeGtyDb.substring(10);
                    } else {
                        String newLabel = "mergeddb";
                        options.PUBDB_URL_MAP.put(newLabel, url + piResource);
                        checkLabels.add(newLabel);
                        options.mergeGtyDb = piResource.substring(10);
                        options.PUBDB_FILE_MAP.put(newLabel, options.mergeGtyDb);
                        //remove the tar lable
                        options.mergeGtyDb = options.mergeGtyDb.substring(0, options.mergeGtyDb.lastIndexOf('.'));
                    }
                }
            }

            String refGenomeVersion = options.refGenomeVersion;
            if (options.scoreDBLableList.contains("noncoding")) {
                // download resources from gene-feature specific annotation function
                String[] annotationScore = {"funcnote_CADD.CScore.gz", "funcnote_DANN.gz", "funcnote_fathmm-MKL.gz", "funcnote_FunSeq.gz",
                    "funcnote_FunSeq2.gz", "funcnote_GWAS3D.gz", "funcnote_GWAVA.gz", "funcnote_SuRFR.gz", "funcnote_all.footprints.bed.gz.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.DNase-seq.cmp.gz", "funcnote_encode_megamix.bed.gz.FAIRE-seq.cmp.gz",
                    "funcnote_encode_megamix.bed.gz.Histone.cmp.gz", "funcnote_encode_megamix.bed.gz.Tfbs.cmp.gz"};
                for (int j = 0; j < annotationScore.length; j++) {
                    String newLabel = "noncoding" + j;
                    options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    checkLabels.add(newLabel);
                }
                options.PUBDB_FILE_MAP.put("hgmd_model", "hgmd_model.obj");
                options.PUBDB_URL_MAP.put("hgmd_model", KGGSeq_URL + "/download/resources/modelFile/" + "hgmd_model.obj");
                checkLabels.add("hgmd_model");
            }

            if (options.scoreDBLableList.contains("dbncfp")) {
                // download resources for cell-type specific annotation function
                String[] annotationScore = new String[]{"funcnote_CADD_cscore.gz", "funcnote_CADD_PHRED.gz", "funcnote_DANN_score.gz", "funcnote_Fathmm_MKL_score.gz",
                    "funcnote_FunSeq_score.gz", "funcnote_FunSeq2_score.gz", "funcnote_GWAS3D_score.gz", "funcnote_GWAVA_region_score.gz",
                    "funcnote_GWAVA_TSS_score.gz", "funcnote_GWAVA_unmatched_score.gz", "funcnote_SuRFR_score.gz"};
                for (int j = 0; j < 11; j++) {
                    String newLabel = "regulartory_pathogenic" + j;
                    options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/" + refGenomeVersion + "_" + annotationScore[j]);
                    checkLabels.add(newLabel);
                }
                String newLabel = "cell_signal";
                options.PUBDB_FILE_MAP.put(newLabel, refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                options.PUBDB_URL_MAP.put(newLabel, KGGSeq_URL + "/download/resources/" + refGenomeVersion + "/all_cell_signal/" + options.cellLineName + ".zip");
                checkLabels.add(newLabel);
                options.PUBDB_FILE_MAP.put("all_causal_distribution", "all_causal_distribution.zip");
                options.PUBDB_URL_MAP.put("all_causal_distribution", KGGSeq_URL + "/download/resources/" + "all_causal_distribution.zip");
                options.PUBDB_FILE_MAP.put("all_neutral_distribution", "all_neutral_distribution.zip");
                options.PUBDB_URL_MAP.put("all_neutral_distribution", KGGSeq_URL + "/download/resources/" + "all_neutral_distribution.zip");
                checkLabels.add("all_causal_distribution");
                checkLabels.add("all_neutral_distribution");
            }

            if (options.dgvcnvAnnotate) {
                checkLabels.add("dgvcnv");
            }

            //force to download the small database
            checkLabels.add("cano");
            checkLabels.add("cura");
            checkLabels.add("onco");
            checkLabels.add("cmop");
            checkLabels.add("onto");

            checkLabels.add("morbidmap");

            checkLabels.add("string");
            checkLabels.add("ideogram");
            checkLabels.add("proteindomain");
            checkLabels.add("uniportrefseqmap");
            checkLabels.add("uniportgencodemap");
            checkLabels.add("uniportucscgenemap");
            checkLabels.add("mendelcausalrare.param");
            checkLabels.add("cancer.param");
            checkLabels.add("cancer.mutsig");
            checkLabels.add("mendelgene");
            checkLabels.add("cancer.null.driver.score");
            if (options.rsid) {
                checkLabels.add("rsid");
            }
            if (options.dbscSNVAnnote) {
                checkLabels.add("dbscSNV");
            }
            if (options.cosmicAnnotate) {
                checkLabels.add("cosmicdb");
            }

            if (options.isTFBSCheck) {
                checkLabels.add("tfbs");
            }
            if (options.isEnhancerCheck) {
                checkLabels.add("enhancer");
            }

            if (options.superdupAnnotate || options.superdupFilter) {
                checkLabels.add("superdup");
            }

            boolean toDownloadHGNC = false;
            String hgncFileName = "HgncGene.txt";
            File resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);

            if (!resourceFile.exists()) {
                toDownloadHGNC = true;
            } else {
                //half an year later
                long time = resourceFile.lastModified() / 1000 + 30 * 24 * 60 * 60;
                Date fileData = new Date(time * 1000);
                Date today = new Date();
                //if too small or too early
                if (resourceFile.length() < 3.2 * 1024 * 1024 || today.after(fileData)) {
                    //an incomplete file
                    resourceFile.delete();
                    toDownloadHGNC = true;
                }
            }

            //downloading does not support multiple thread 
            String url = "http://www.genenames.org/cgi-bin/hgnc_downloads?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&status=Approved&status_opt=2&where=&order_by=gd_hgnc_id&format=text&limit=&hgnc_dbtag=on&submit=submit";

            if (toDownloadHGNC) {
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + hgncFileName);
                String msg1 = "Donwloading HGNC gene annotations ... ";

                File parePath = resourceFile.getParentFile();
                if (!parePath.exists()) {
                    parePath.mkdirs();
                }
                HttpClient4API.simpleRetriever(url, resourceFile.getCanonicalPath());
                toDownload = true;
            }

            if (!checkLabels.isEmpty()) {
                System.out.println("Checking the latest resources (to disable this checking, use --no-resource-check option)...");
            }
            for (String dbLabelName : checkLabels) {
                String dbFileName = options.PUBDB_FILE_MAP.get(dbLabelName);
                resourceFile = new File(GlobalManager.RESOURCE_PATH + "/" + dbFileName);
                //System.out.println(resourceFile.getCanonicalPath());
                if (resourceFile.exists()) {
                    long fileSize = resourceFile.length();
                    long netFileLen = HttpClient4API.getContentLength(options.PUBDB_URL_MAP.get(dbLabelName));
                    if (netFileLen <= 1024 || fileSize == netFileLen) {
                        continue;
                    }
                }

                if (i == 0) {
                    System.out.println("Downloading resources ...");
                }

                final HttpClient4DownloadTask task = new HttpClient4DownloadTask(options.PUBDB_URL_MAP.get(dbLabelName), 20);
                task.setDataMd5(HttpClient4API.getContent(options.PUBDB_URL_MAP.get(dbLabelName) + ".md5"));
                File filePath = new File(GlobalManager.RESOURCE_PATH);
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                task.setLocalPath(resourceFile.getCanonicalPath());
                final String dbLabel = dbLabelName;
                task.addTaskListener(new DownloadTaskListener() {

                    @Override
                    public void autoCallback(DownloadTaskEvent event) {
                        int progess = (int) (event.getTotalDownloadedCount() * 100.0 / event.getTotalCount());
                        String infor = progess + "%     Realtime Speed:" + event.getRealTimeSpeed() + " Global Speed:" + event.getGlobalSpeed();
                        System.out.print(infor);
                        char[] bs = new char[infor.length()];
                        Arrays.fill(bs, '\b');
                        System.out.print(bs);
                    }

                    @Override
                    public void taskCompleted() throws Exception {
                        // File savedFile = new File(task.getLocalPath()); 
                        if (task.getLocalPath().endsWith(".tar")) {
                            File file = new File(task.getLocalPath());
                            Tar.untar(task.getLocalPath(), file.getParent() + File.separator);

                            // file.delete();
                        } else if (task.getLocalPath().endsWith(".zip")) {
                            Zipper ziper = new Zipper();
                            File file = new File(task.getLocalPath());
                            ziper.extractZip(task.getLocalPath(), file.getParent() + File.separator);
                            //  file.delete();
                        }
                        String msg1 = "Resource " + dbLabel + " has been downloaded!";
                        System.out.println(msg1);
                    }
                });
                runningThread++;
                TimeUnit.MILLISECONDS.sleep(500);
                serv.submit(task);
                toDownload = true;

                i++;
            }
            for (int index = 0; index < runningThread; index++) {
                Future task = serv.take();
                String download = (String) task.get();
            }
            exec.shutdown();
            if (toDownload) {
                StringBuilder inforString = new StringBuilder();
                inforString.append("The lapsed time for downloading is : ");
                long endTime = System.currentTimeMillis();
                inforString.append((endTime - startTime) / 1000.0);
                inforString.append(" Seconds.\n");
                System.out.println(inforString.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
