/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.controller;

import cern.colt.list.DoubleArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.cobi.kggseq.GlobalManager;
import org.cobi.kggseq.entity.Chromosome;
import org.cobi.kggseq.entity.Individual;
import org.cobi.kggseq.entity.Variant;
import org.cobi.util.text.LocalExcelFile;
import org.cobi.util.text.StringArrayDoubleComparator;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author JiangLi
 */
public class SKAT {
 
    RConnection rcon;
    int intParallel;
    Map<String, List<Variant>> geneVars;

    List<int[][]> lstVariant;
    List<String[]> genePValueList;
    double[] dblPhe;
    boolean boolBinary;
    int intBufferSize;
    String strPrefix;
    private static final Logger LOG = Logger.getLogger(SKAT.class);
    int cutoff=5;

    public SKAT(String strOutput, int cutoff) throws RserveException {
        this.rcon = new RConnection();
        this.rcon.eval("pack=\"SKAT\"; if (!require(pack,character.only = TRUE)) { install.packages(pack,dep=TRUE,repos='http://cran.us.r-project.org');if(!require(pack,character.only = TRUE)) stop(\"Package not found\")}");
        this.rcon.eval("pack=\"snow\"; if (!require(pack,character.only = TRUE)) { install.packages(pack,dep=TRUE,repos='http://cran.us.r-project.org');if(!require(pack,character.only = TRUE)) stop(\"Package not found\")}");
        this.rcon.eval("library(SKAT)");
        this.rcon.eval("library(snow)");
//        this.rcon.eval("Rserve()");

        lstVariant = new ArrayList<int[][]>();
        genePValueList = new ArrayList<String[]>();
        geneVars = new HashMap<String, List<Variant>>();
        strPrefix = "skat" + (new File(strOutput).getName()) + (int) (Math.random() * 1000);
//        strPrefix="lj";
        this.cutoff=cutoff;
    }

    public void startRServe(int intParallel) {
        try {
            this.intParallel = intParallel;
            rcon.voidEval(strPrefix + "cl<-makeCluster(" + intParallel + ")");
        } catch (RserveException ex) {
            LOG.error(ex);
        }
    }

    public void closeRServe() {
        try {
            this.intParallel = 0;
            rcon.voidEval("stopCluster(" + strPrefix + "cl)");
        } catch (RserveException ex) {
            LOG.error(ex);
        }
    }

    public double[] getPhenotype(List<Individual> subjectList, boolean boolBinary, int intPhe) {
        Boolean[] marker = isBinaryTrait(subjectList, intPhe);
        if (-1 == intPhe) {
            this.boolBinary = boolBinary;
            dblPhe = new double[subjectList.size()];
            for (int i = 0; i < subjectList.size(); i++) {
                if (!marker[1]) {
                    dblPhe[i] = subjectList.get(i).getAffectedStatus() - 1;
                } else {
                    dblPhe[i] = subjectList.get(i).getAffectedStatus();
                }
            }
            return dblPhe;
        } else {
            this.boolBinary = boolBinary && marker[0];
            dblPhe = new double[subjectList.size()];
            for (int i = 0; i < subjectList.size(); i++) {
                if (this.boolBinary) {
                    if (marker[1]) {
                        dblPhe[i] = subjectList.get(i).getTraits()[intPhe];
                    } else {
                        dblPhe[i] = subjectList.get(i).getTraits()[intPhe] - 1;
                    }
                } else {
                    dblPhe[i] = subjectList.get(i).getTraits()[intPhe];
                }
            }
            return dblPhe;
        }
    }

    public void setPhenotype(double[] dblPhe, double[][] dblCov, boolean permutPheno) {
        try {
            //Set Phenotype.
            rcon.assign(strPrefix + "y", dblPhe);
            if (permutPheno) {
                rcon.voidEval(strPrefix + "y <- sample(" + strPrefix + "y)");
            }
            //Set covariate. 
            double[] dblMerge;
            if (dblCov != null) {
                dblMerge = new double[dblCov.length * dblCov[0].length];
                for (int j = 0; j < dblCov[0].length; j++) {
                    for (int i = 0; i < dblCov.length; i++) {
                        dblMerge[j * dblCov.length + i] = dblCov[i][j];
                    }
                }
                rcon.assign(strPrefix + "lj", dblMerge);
                String strCommand = strPrefix + "X<-matrix(data=" + strPrefix + "lj, nrow=" + dblCov.length + ", ncol=" + dblCov[0].length + ")";
                rcon.voidEval(strCommand);
            }
            if (boolBinary) {
                String strCommand = strPrefix + "obj<-SKAT_Null_Model(" + strPrefix + "y ~ " + (dblCov == null ? 1 : (strPrefix + "X")) + ", out_type=\"D\")";
                rcon.voidEval(strCommand);
            } else {
                String strCommand = strPrefix + "obj<-SKAT_Null_Model(" + strPrefix + "y ~ " + (dblCov == null ? 1 : (strPrefix + "X")) + ", out_type=\"C\")";
                rcon.voidEval(strCommand);
                //   System.out.println(strCommand);
            }

        } catch (REngineException ex) {
            LOG.error(ex);
        }
    }

    private double[] getPValue(List<int[][]> altZ) throws REngineException, REXPMismatchException {
        double[] dblResult = null;
        try {
            rcon.voidEval(strPrefix + "lstZ<-list()");
            for (int k = 0; k < altZ.size(); k++) {
                int[][] Z = altZ.get(k);
                int[] intDim = new int[2];
                intDim[0] = Z.length;
                intDim[1] = Z[0].length;
                int[] intVector = new int[intDim[0] * intDim[1]];
                for (int i = 0; i < intDim[0]; i++) {
                    for (int j = 0; j < intDim[1]; j++) {
                        intVector[i * intDim[1] + j] = Z[i][j];
                    }
                }
                rcon.assign(strPrefix + "numVector", intVector);
                String strCommand = strPrefix + "lstZ[[" + (k + 1) + "]]<-t(matrix(" + strPrefix + "numVector,nrow=" + intDim[0] + ",ncol=" + intDim[1] + ",byrow=T))";
                rcon.voidEval(strCommand);
//                System.out.println(strCommand);
            }
            String strCommandA;
            String strCommandB;
            if (boolBinary) {
                /*
                 This function implements six methods (method.bin) to compute p-values: 1) Efficient resampling
                 (ER); 2) Quantile adjusted moment matching (QA); 3) Moment matching adjustment (MA); 4) No
                 adjustment (UA); 5) Adaptive ER (ER.A); and 6) Hybrid. "Hybrid" selects a method based on the
                 total minor allele count (MAC), the number of individuals with minor alleles (m), and the degree of
                 case-control imbalance.*/
                String pc = "Hybrid";
                String strCommand = strPrefix + "numResult<-numeric()";
                rcon.voidEval(strCommand);
                strCommandA = strPrefix + "lstResult<-parLapply(" + strPrefix + "cl," + strPrefix + "lstZ,SKATBinary," + strPrefix + "obj,method=\"SKAT\", method.bin=\"" + pc + "\")";
                strCommandB = strPrefix + "numResult<-c(" + strPrefix + "numResult,sapply(" + strPrefix + "lstResult,fun<-function(x){x$p.value}))";
                rcon.voidEval(strCommandA);//Time-consuming. 
                rcon.voidEval(strCommandB);
                strCommandA = strPrefix + "lstResult<-parLapply(" + strPrefix + "cl," + strPrefix + "lstZ,SKATBinary," + strPrefix + "obj,method=\"SKATO\", method.bin=\"" + pc + "\")";
                rcon.voidEval(strCommandA);//Time-consuming. 
                rcon.voidEval(strCommandB);
                strCommandA = strPrefix + "lstResult<-parLapply(" + strPrefix + "cl," + strPrefix + "lstZ,SKATBinary," + strPrefix + "obj,method=\"Burden\", method.bin=\"" + pc + "\")";
                rcon.voidEval(strCommandA);//Time-consuming. 
                rcon.voidEval(strCommandB);
            } else {
                String strCommand = strPrefix + "numResult<-numeric()";
                rcon.voidEval(strCommand);
                strCommandA = strPrefix + "lstResult<-parLapply(" + strPrefix + "cl," + strPrefix + "lstZ,SKAT," + strPrefix + "obj,method=\"davies\")";
                strCommandB = strPrefix + "numResult<-c(" + strPrefix + "numResult,sapply(" + strPrefix + "lstResult,fun<-function(x){x$p.value}))";
//                System.out.println(strCommandA);
//                System.out.println(strCommandB);
                rcon.voidEval(strCommandA);
                rcon.voidEval(strCommandB);

                strCommandA = strPrefix + "lstResult<-parLapply(" + strPrefix + "cl," + strPrefix + "lstZ,SKAT," + strPrefix + "obj,method=\"optimal.adj\")";
//                System.out.println(strCommandA);
//                System.out.println(strCommandB);
                rcon.voidEval(strCommandA);
                rcon.voidEval(strCommandB);

                strCommandA = strPrefix + "lstResult<-parLapply(" + strPrefix + "cl," + strPrefix + "lstZ,SKAT," + strPrefix + "obj,r.corr=1)";
//                System.out.println(strCommandA);
//                System.out.println(strCommandB);
                rcon.voidEval(strCommandA);
                rcon.voidEval(strCommandB);
            }

            //rcon.assign(".temp", "lstResult<-parLapply(cl,lstZ,SKAT,obj)");
            //REXP r=rcon.parseAndEval("try(eval(parse(text=.temp)),silent=T)");
            //if (r.inherits("try-error")) System.err.println("Error: "+r.toString());
            dblResult = rcon.eval(strPrefix + "numResult").asDoubles();
        } catch (RserveException ex) {
            LOG.error(ex);
        }
        return dblResult;
    }

    public void getGtyAndRun(Chromosome chromosome, List<Individual> subjectList, int[] pedEncodeGytIDMap, boolean isPhased, int intNT, DoubleArrayList[] pvList) {
        if (chromosome == null) {
            System.out.println("No variants are left! SKAT failed!");
        } else {
            geneVars.clear();
             
            //this setting is too strong, leading too many Rscript process created. 
            if (intNT < 100) {
                intNT = intNT*3;
            }
            
            for (Variant var : chromosome.variantList) {
                if (var.encodedGty == null) {
                    continue;
                }
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
            
            //Filter genes with variants smaller than cutoff. 
            Iterator<String> itr=geneVars.keySet().iterator();
            while(itr.hasNext()){
                String str=itr.next();
                int count=geneVars.get(str).size();
                if(count<cutoff){
//                    System.out.println(str+" "+count);
                    itr.remove();
                }               
            }

            int intGeneNum = geneVars.size();
            int intL;
            List<List<Variant>> geneVarList = new ArrayList<List<Variant>>(geneVars.values());
            String[] geneSymbs = new String[intNT];
            int[] varNum = new int[intNT];
            int s = 0;
            try {
                for (int i = 0; i < intGeneNum; i += intNT) {
                    lstVariant.clear();
                    for (int j = 0; j < intNT; j++) {
                        s = i + j;
                        if (s >= intGeneNum) {
                            break;
                        }
                        List<Variant> vars = geneVarList.get(s);
                        int[][] encodeGyts = new int[vars.size()][];
                        int t = 0;
                        for (Variant var : vars) {
                            encodeGyts[t] = getGenotype(var, isPhased, subjectList, pedEncodeGytIDMap);
                            t++;
                        }
                        lstVariant.add(encodeGyts);
                        geneSymbs[j] = vars.get(0).geneSymb;
                        varNum[j] = vars.size();
//                        System.out.println(geneSymbs[j]);
                    }
                    if (lstVariant.size() != intParallel) {
                        this.closeRServe();
                        this.startRServe(lstVariant.size());
                    }
                    double[] dblP = getPValue(lstVariant);
                    if (dblP == null) {
                        continue;
                    }
                    intL = lstVariant.size();
//                    if (boolBinary) {
                    for (int j = 0; j < dblP.length / 3; j++) {
//                    System.out.println(altGene.get(j + i * intParallel));
//                    System.out.println(dblP[j]);
//                    System.out.println(dblP[j + intL]);
//                    System.out.println(dblP[j + intL * 2]);
                        String[] temp = new String[5];
                        temp[0] = geneSymbs[j];
                        temp[1] = String.valueOf(varNum[j]);
                        temp[2] = String.valueOf(dblP[j]);
                        temp[3] = String.valueOf(dblP[j + intL]);
                        temp[4] = String.valueOf(dblP[j + intL * 2]);
                        pvList[0].add(dblP[j]);
                        pvList[1].add(dblP[j + intL]);
                        pvList[2].add(dblP[j + intL * 2]);
                        genePValueList.add(temp);
//                            System.out.println(temp[0]+"-----"+temp[1]+"-----"+temp[2]+"-----"+temp[3]+"-----"+temp[4]);
                    }
//                    } else {
//                        for (int j = 0; j < dblP.length / 2; j++) {
//                            String[] temp = new String[4];
//                            temp[0] = geneSymbs[j];
//                            temp[1] = String.valueOf(varNum[j]);
//                            temp[2] = String.valueOf(dblP[j]);
//                            temp[3] = String.valueOf(dblP[j + intL]);
//                            pvList[0].add(dblP[j]);
//                            pvList[1].add(dblP[j + intL]);
////                    System.out.println(temp[0]);
////                    System.out.println(temp[1]);
//                            genePValueList.add(temp);
//                        }
//                    }
                }
            } catch (REngineException ex) {
                LOG.error(ex);
            } catch (REXPMismatchException ex) {
                LOG.error(ex);
            }

        }
    }

    public void saveResult2Xlsx(String outPath) {
        try {
            Collections.sort(genePValueList, new StringArrayDoubleComparator(3));
//            if (boolBinary) {
            genePValueList.add(0, new String[]{"Gene", "#Var", "SKAT_P", "SKATO_P", "Burden_P"});
//            } else {
//                genePValueList.add(0, new String[]{"Gene", "#Var", "SKAT_P", "SKATO_P"});
//            }
            LocalExcelFile.writeArray2XLSXFile(outPath, genePValueList, true, -1, 0);
            String info = "The gene-based association test p-values by SKAT are saved in " + (new File(outPath)).getCanonicalPath() + "!";

            LOG.info(info);
            genePValueList.clear();
        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    private int[] getGenotype(Variant var, boolean isPhased, List<Individual> subjectList, int[] pedEncodeGytIDMap) {
        int[] gty = null;
        int alleleNum = var.getAltAlleles().length + 1;
        int base = 0;
        int subNum = subjectList.size();
        int[] alleleNums = new int[subNum];
        if (isPhased) {
            base = GlobalManager.phasedAlleleBitMap.get(alleleNum);
        } else {
            base = GlobalManager.unphasedAlleleBitMap.get(alleleNum);
        }
        int gtyID = 0;
        for (int subID = 0; subID < subNum; subID++) {
            gtyID = pedEncodeGytIDMap[subID];
            if (gtyID < 0) {
                alleleNums[subID] = 9;
                continue;
            }
            if (isPhased) {
                gty = BinaryGtyProcessor.getPhasedGtyAt(var.encodedGty, alleleNum, base, gtyID);
            } else {
                gty = BinaryGtyProcessor.getUnphasedGtyAt(var.encodedGty, alleleNum, base, gtyID);
            }
            if (gty == null) {
                alleleNums[subID] = 9;
            } else if (gty[0] == gty[1]) {
                if (gty[0] == 0) {
                    alleleNums[subID] = 0;
                } else {
                    alleleNums[subID] = 2;
                }
            } else {
                alleleNums[subID] = 1;
            }
        }
        return alleleNums;
    }

    public Boolean[] isBinaryTrait(List<Individual> subjectList, int intPhe) {
        Boolean[] marker = new Boolean[2];
        marker[0] = true;//whether the trait is binary? T:binary F:continous
        marker[1] = true;//whether the binary denotated 0/1? T:0/1 F:1/2
        if (-1 == intPhe) {
            for (int i = 0; i < subjectList.size(); i++) {
                int temp = subjectList.get(i).getAffectedStatus();
                if (temp == 2) {
                    marker[1] = false;
                }
            }
            return marker;
        }
        for (int i = 0; i < subjectList.size(); i++) {
            double temp = subjectList.get(i).getTraits()[intPhe];
            if (temp != 0 && temp != 1 && temp != 2) { //!(temp==0 || temp==1 || temp=2)
                marker[0] = false;
                return marker;
            }
            if (temp == 2) {
                marker[1] = false;
            }
        }
        return marker;
    }

    public double[][] getCovarite(List<Individual> subjectList, Map<String, Integer> phenotypeColID, String[] covItem) {
        ArrayList<Integer> alt = new ArrayList<Integer>();
        for (String str : covItem) {
            int index = phenotypeColID.get(str) == null ? -1 : phenotypeColID.get(str);
            if (index == -1) {
                String out = str + " is a wrong name for covariate and has been skipped!";
                LOG.info(out);
            } else {
                alt.add(index);
            }
        }

        if (alt.isEmpty()) {
            String info = "No covariates are set!";
            LOG.info(info);
            return null;
        } else {
            double[][] dblCov = new double[subjectList.size()][alt.size()];
            for (int i = 0; i < subjectList.size(); i++) {
                for (int j = 0; j < alt.size(); j++) {
                    dblCov[i][j] = subjectList.get(i).getTraits()[alt.get(j)];
                }
            }
            return dblCov;
        }
    }

    public boolean isBoolBinary() {
        return boolBinary;
    }

}
