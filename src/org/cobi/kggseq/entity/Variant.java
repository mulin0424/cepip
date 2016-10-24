/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.entity;

import java.io.Serializable;

/**
 *
 * @author MX Li
 */
public class Variant implements Serializable {

    private static final long serialVersionUID = 1L;

    public int refStartPosition;
    public String label;
    private String refGeneAnnot;
    private String gEncodeAnnot;
    private String knownGeneAnnot;
    private String ensemblGeneAnnot;
    private String refAllele;
    private String[] altAlleles;
    public float[] scores;
    public String geneSymb;
    public boolean isIndel = false;
    //-1 denotes this SNP does not exist in db; NA means db has this variant but no frequency information
    public float altAF = -1;
    public String[] featureValues;
    public boolean isIBS = false;
    public byte smallestFeatureID = 17;//by default
   
    private int affectedRefHomGtyNum = 0;
    private int affectedHetGtyNum = 0;
    private int affectedAltHomGtyNum = 0;
    private int unaffectedRefHomGtyNum = 0;
    private int unaffectedHetGtyNum = 0;
    private int unaffectedAltHomGtyNum = 0; 
    public int[] encodedGty = null;
    public char[] readInfor = null;
 
 

    public int getAffectedAltHomGtyNum() {
        return affectedAltHomGtyNum;
    }

    public void setAffectedAltHomGtyNum(int affectedAltHomGtyNum) {
        this.affectedAltHomGtyNum = affectedAltHomGtyNum;
    }

    public int getAffectedHetGtyNum() {
        return affectedHetGtyNum;
    }

    public void setAffectedHetGtyNum(int affectedHetGtyNum) {
        this.affectedHetGtyNum = affectedHetGtyNum;
    }

    public int getAffectedRefHomGtyNum() {
        return affectedRefHomGtyNum;
    }

    public void setAffectedRefHomGtyNum(int affectedRefHomGtyNum) {
        this.affectedRefHomGtyNum = affectedRefHomGtyNum;
    }

    public int getUnaffectedAltHomGtyNum() {
        return unaffectedAltHomGtyNum;
    }

    public void setUnaffectedAltHomGtyNum(int unaffectedAltHomGtyNum) {
        this.unaffectedAltHomGtyNum = unaffectedAltHomGtyNum;
    }

    public int getUnaffectedHetGtyNum() {
        return unaffectedHetGtyNum;
    }

    public void setUnaffectedHetGtyNum(int unaffectedHetGtyNum) {
        this.unaffectedHetGtyNum = unaffectedHetGtyNum;
    }

    public int getUnaffectedRefHomGtyNum() {
        return unaffectedRefHomGtyNum;
    }

    public void setUnaffectedRefHomGtyNum(int unaffectedRefHomGtyNum) {
        this.unaffectedRefHomGtyNum = unaffectedRefHomGtyNum;
    }

    public String getEnsemblGeneAnnot() {
        return ensemblGeneAnnot;
    }

    public void setEnsemblGeneAnnot(String ensemblGeneAnnot) {
        this.ensemblGeneAnnot = ensemblGeneAnnot;
    }

    public String[] getFeatureValues() {
        return featureValues;
    }

    public int getAlleleEndPostion(int alleleIndex) throws Exception {
        if (!isIndel) {
            return refStartPosition;
        } else {
            if (alleleIndex >= altAlleles.length) {
                throw new Exception("Allele index is out of boundery " + altAlleles.length);
            }
            String alta = altAlleles[alleleIndex];
            if (alta.startsWith("+")) {
                return refStartPosition + refAllele.length() - 1;
            } else {
                //deletion
                return refStartPosition + refAllele.length() - 1;
            }
        }
    }

    public String getKnownGeneAnnot() {
        return knownGeneAnnot;
    }

    public void setKnownGeneAnnot(String knownGeneAnnot) {
        this.knownGeneAnnot = knownGeneAnnot;
    }

    public String getgEncodeAnnot() {
        return gEncodeAnnot;
    }

    public void setgEncodeAnnot(String gEncodeAnnot) {
        this.gEncodeAnnot = gEncodeAnnot;
    }

    public String getRefGeneAnnot() {
        return refGeneAnnot;
    }

    public void setRefGeneAnnot(String refGeneAnnot) {
        this.refGeneAnnot = refGeneAnnot;
    }

    //for some indels the refStartPosition is acturally not actually mutant allele position when the reference allele have multiple bases
    //this function report one base before the insertion or deletion allele
    public int getAlleleStartPostion(int alleleIndex) {
        if (!isIndel) {
            return refStartPosition;
        } else {
            if (alleleIndex >= altAlleles.length) {
                //  throw new Exception("Allele index is out of boundery " + altAlleles.length);
                return -1;
            }
            String alta = altAlleles[alleleIndex];
            if (alta.startsWith("+")) {
                return refStartPosition + refAllele.length() - 1;
            } else {
                //deletion
                int index = alta.indexOf('-');
                return refStartPosition + index - 1;
            }
        }
    }

    public String getAltAllele(int alleleIndex) {
        String alta = altAlleles[alleleIndex];
        if (!isIndel) {
            return alta;
        } else {
            if (alleleIndex >= altAlleles.length) {
                //  throw new Exception("Allele index is out of boundery " + altAlleles.length);
                return null;
            }
            alta = altAlleles[alleleIndex];
            if (alta.startsWith("+")) {
                return alta.substring(refAllele.length());
            } else if (alta.startsWith("-")) {
                //deletion
                int index = alta.indexOf('-');
                return refAllele.substring(index);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < alta.length(); i++) {
                    if (refAllele.charAt(i) != alta.charAt(i)) {
                        sb.append(alta.charAt(i));
                    }
                }
                return sb.toString();
            }
        }
    }

    public void setIsIndel(boolean isIndel) {
        this.isIndel = isIndel;
    }

    public void setFeatureValue(int index, String val) {
        if (index >= featureValues.length) {
            String[] tmp = new String[index + 5];
            System.arraycopy(featureValues, 0, tmp, 0, featureValues.length);
            featureValues = tmp;
        }
        featureValues[index] = (val);
    }
    

    public Variant() {
        featureValues = new String[0];
    }

    public Variant(int physicalPosition, String refAllele, String[] altAlleles) {
        this.refStartPosition = physicalPosition;
        this.refAllele = refAllele;
        this.altAlleles = altAlleles;
        if (altAlleles != null) {
            for (String s : altAlleles) {
                if (s.length() > 1) {
                    isIndel = true;
                    break;
                }
            }
        }
        featureValues = new String[0];
    }

    public String[] getAltAlleles() {
        return altAlleles;
    }

    public void setAltAlleles(String[] altAlleles) {
        this.altAlleles = altAlleles;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRefAllele() {
        return refAllele;
    }

    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }
    
}
