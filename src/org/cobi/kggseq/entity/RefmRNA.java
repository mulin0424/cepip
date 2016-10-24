/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.entity;

import cern.colt.list.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.cobi.kggseq.GlobalManager;
import org.cobi.util.text.Util;

/**
 *
 * @author mxli
 */
public class RefmRNA extends SeqSegment {

    //private static final Log LOG = Log.getIninstance(RefmRNA.class);
    private static final Logger LOG = Logger.getLogger(RefmRNA.class);
    private char strand = '0';
    private String refID;
    private int length;
    private String mRnaSequence;
    private int mRnaSequenceStart;
    //Note: The end boundary of each exon is not included in refGene database
    private List<SeqSegment> exons;
    public int codingStart;
    //probably codingEnd is exclusive
    public int codingEnd;
    boolean noCodingExon = true;
    int exonNum = 0;
    int codingStartRelativeSiteInSequence = -1;
    int codingStartSiteExonID = 0;
    IntArrayList intronLength = new IntArrayList();
    List<ProteinDomain> proteinDomainList = null;
    String uniprotID = null;
    String geneSymb = null;
    int[] delSites;
    int[] insSites;
    String delSeq;
    String insSeq;
    boolean multipleMapping = false;

    public String getDelSeq() {
        return delSeq;
    }

    public void setDelSeq(String delSeq) {
        this.delSeq = delSeq;
    }

    public String getInsSeq() {
        return insSeq;
    }

    public void setInsSeq(String insSeq) {
        this.insSeq = insSeq;
    }

    public boolean isMultipleMapping() {
        return multipleMapping;
    }

    public void setMultipleMapping(boolean multipleMapping) {
        this.multipleMapping = multipleMapping;
    }

    public String getGeneSymb() {
        return geneSymb;
    }

    public int[] getDelSites() {
        return delSites;
    }

    public int[] getInsSites() {
        return insSites;
    }

    public void setInsSites(int[] insSites) {
        this.insSites = insSites;
    }

    public void setDelSites(int[] delSites) {
        this.delSites = delSites;
    }

    public int getExonNum() {
        return exonNum;
    }

    public void setExonNum(int exonNum) {
        this.exonNum = exonNum;
    }

    public List<SeqSegment> getExons() {
        return exons;
    }

    public void setExons(List<SeqSegment> exons) {
        this.exons = exons;
    }

    public void setGeneSymb(String geneSymb) {
        this.geneSymb = geneSymb;
    }

    public void setProteinDomainList(List<ProteinDomain> proteinDomainList) {
        this.proteinDomainList = proteinDomainList;
    }

    public void setUniprotID(String uniprotID) {
        this.uniprotID = uniprotID;
    }

    public int getCodingEnd() {
        return codingEnd;
    }

    public int getCodingStart() {
        return codingStart;
    }

    public void setmRnaSequenceStart(int mRnaSequenceStart) {
        this.mRnaSequenceStart = mRnaSequenceStart;
    }

    public void setmRnaSequence(String mRnaSequence) {
        this.mRnaSequence = mRnaSequence;
    }

    public RefmRNA(String refID, int start, int end) {
        super(start, end);
        this.refID = refID;
        this.exons = new ArrayList<SeqSegment>();
    }

    public String getRefID() {
        return refID;
    }

    public void setRefID(String refID) {
        this.refID = refID;
    }

    public RefmRNA(String refID, int start, int end, int codingStart, int codingEnd) {
        super(start, end);
        this.refID = refID;
        this.codingStart = codingStart;
        this.codingEnd = codingEnd;
        this.exons = new ArrayList<SeqSegment>();
        if (codingStart != codingEnd) {
            noCodingExon = false;
        }
    }

    /*
    
     */
    public GeneFeature findFeature(String chr, Variant var, boolean isForwardStrandInput, int upstreamDis, int donwstreamDis, int splicingDis) throws Exception {
        int pos = var.refStartPosition;
        String ref = var.getRefAllele();

        String[] altAllele = new String[var.getAltAlleles().length];
        //only use the values of alt alleles because it may be changed by other functions
        System.arraycopy(var.getAltAlleles(), 0, altAllele, 0, altAllele.length);

        GeneFeature ft = findFeature(chr, pos, ref, altAllele, isForwardStrandInput, upstreamDis, donwstreamDis, splicingDis, geneSymb);

        if (ft != null) {
            ft.setName(geneSymb + ":" + ft.getName());
            return ft;
        } else {
            return null;
        }

    }

    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    public void addExon(SeqSegment exon) {
        exons.add(exon);
        exonNum++;
    }

    public void makeAccuIntronLength() throws Exception {
        if (exons == null || exons.isEmpty()) {
            return;
        }
        int accumIntronLen = 0;
        if (strand == '0') {
            throw new Exception("Unknown strand at " + refID + "; and cannot make AccuExonLength!");
        } else if (strand == '+') {
            SeqSegment exon = exons.get(0);
            //assume the boundary is not inclusive 
            if (codingStart >= exon.start && codingStart <= exon.end) {
                codingStartRelativeSiteInSequence = codingStart - exon.start;
                codingStartSiteExonID = 0;
            }
            accumIntronLen = 0;
            for (int i = 1; i < exonNum; i++) {
                exon = exons.get(i);
                //assume the boundary is not inclusive     
                intronLength.add(exons.get(i).start - exons.get(i - 1).end);
                accumIntronLen += intronLength.get(i - 1);
                if (codingStart >= exon.start && codingStart <= exon.end) {
                    codingStartRelativeSiteInSequence = codingStart - exons.get(0).start - accumIntronLen;
                    codingStartSiteExonID = i;
                }
            }
        } else {
            SeqSegment exon = exons.get(exonNum - 1);
            //assume the boundary is not inclusive 
            if (codingEnd >= exon.start && codingEnd <= exon.end) {
                codingStartRelativeSiteInSequence = exon.end - codingEnd;
                codingStartSiteExonID = exonNum - 1;
            }
            accumIntronLen = 0;
            for (int i = exonNum - 2; i >= 0; i--) {
                exon = exons.get(i);
                //assume the boundary is not inclusive              
                accumIntronLen += (exons.get(i + 1).start - exons.get(i).end);
                if (codingEnd >= exon.start && codingEnd <= exon.end) {
                    codingStartRelativeSiteInSequence = exons.get(exonNum - 1).end - codingEnd - accumIntronLen;
                    codingStartSiteExonID = i;
                }
            }
            for (int i = 1; i < exonNum; i++) {
                intronLength.add(exons.get(i).start - exons.get(i - 1).end);
            }
            //intronLength.reverse();
        }

        /*
         if(this.refID.equals("NM_001128929")){
         int sss=0;
         } 
         */
        //We only need adjust for gaps after the codingStartRelativeSiteInSequence
        //Note the sequences in the kggseq file are cDNA sequence from 5' to 3'. So we ingnore the direction here 
        if (delSites != null) {
            IntArrayList effectiveSites = new IntArrayList();
            StringBuilder effectiveBase = new StringBuilder();
            for (int i = 0; i < delSites.length; i++) {
                if (strand == '+') {
                    //it is very confusing whether to incoude the deletion at tail
                    if (delSites[i] >= codingStartRelativeSiteInSequence) {
                        effectiveSites.add(delSites[i]);
                        effectiveBase.append(delSeq.charAt(i));
                    }
                } else {
                    
                    if (delSites[i] >= codingStartRelativeSiteInSequence) {
                        effectiveSites.add(delSites[i]);
                        effectiveBase.append(delSeq.charAt(i));
                    }
                }
            }
            if (effectiveSites.isEmpty()) {
                delSites = null;
                delSeq = null;
            } else {
                if (delSites.length != effectiveSites.size()) {
                    delSites = new int[effectiveSites.size()];
                    for (int i = 0; i < delSites.length; i++) {
                        delSites[i] = effectiveSites.getQuick(i);
                    }
                    delSeq = effectiveBase.toString();
                }
            }
        }

        if (insSites != null) {
            IntArrayList effectiveSites = new IntArrayList();
            StringBuilder effectiveBase = new StringBuilder();
            for (int i = 0; i < insSites.length; i++) {
                if (strand == '+') {
                    //it is very confusing whether to incoude the deletion at tail
                    if (insSites[i] >= codingStartRelativeSiteInSequence) {
                        effectiveSites.add(insSites[i]);
                        effectiveBase.append(insSeq.charAt(i));
                    }
                } else {
                   
                    if (insSites[i] >= codingStartRelativeSiteInSequence) {
                        effectiveSites.add(insSites[i]);
                        effectiveBase.append(insSeq.charAt(i));
                    }
                }
            }
            if (effectiveSites.isEmpty()) {
                insSites = null;
                insSeq = null;
            } else {
                if (insSites.length != effectiveSites.size()) {
                    insSites = new int[effectiveSites.size()];
                    for (int i = 0; i < insSites.length; i++) {
                        insSites[i] = effectiveSites.getQuick(i);
                    }

                    insSeq = effectiveBase.toString();
                }
            }
        }

    }

    /*
     The possible values of the finding is summarized below:
     Value 	Default precedence 	Explanation
     0	1	2	3	4	5	6
     frameshift	nonframeshift	stoploss	stopgain	missense	synonymous	splicing
     7	8	9	10	11	12	13
     ncRNA	5UTR	3UTR	intronic	upstream	downstream	intergenic
    
     * 
     */
    // this is a function get position in messanger rna in which the intronic regions are excluded    
    public GeneFeature findFeature(String chr, int oldStartPos, String ref, String[] altAlleles, boolean isForwardStrandInput, int upstreamDis, int downstreamDis, int splicingDis, String geneSym) throws Exception {
        if (strand == '0') {
            throw new Exception("Unknown strand at " + refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")"));
        }
        int exonicFeatureID = GlobalManager.VarFeatureIDMap.get("exonic");

        /*
         if (oldStartPos == 77526727) {
         int ssss = 0;
         }
         */
        if (isForwardStrandInput) {
            if (strand == '-') {
                //assum the input allele are all in forward strand
                ref = Util.getReverseComplementalSquences(ref);
                for (int i = 0; i < altAlleles.length; i++) {
                    altAlleles[i] = Util.getReverseComplementalSquences(altAlleles[i]);
                }
            }
        }

        int count = 0;
        int startPos = 0;
        byte[] errorCode = new byte[1];
        errorCode[0] = 0;
        int startAllele = 0;

        List<GeneFeature> gfList = new ArrayList<GeneFeature>();
        for (String allele : altAlleles) {
            if (allele.startsWith("+") || allele.endsWith("+")) {
                startPos = oldStartPos;
                /*
                 count = 0;
                 if (allele.startsWith("+")) {
                 for (int i = 0; i < allele.length(); i++) {
                 if (allele.charAt(i) == '+') {
                 count++;
                 } else {
                 break;
                 }
                 }
                 if (strand == '-') {
                 startPos = oldStartPos - count;
                 } else {
                 startPos = oldStartPos + count;
                 }
                 } else {
                 for (int i = allele.length() - 1; i >= 0; i--) {
                 if (allele.charAt(i) == '+') {
                 count++;
                 } else {
                 break;
                 }
                 }
                 if (strand == '-') {
                 startPos = oldStartPos - count;
                 } else {
                 startPos = oldStartPos + count;
                 }
                 }
                 */
                //it is an insertion
                GeneFeature gf = findCrudeFeature(startPos, upstreamDis, downstreamDis, splicingDis, ref, allele);
                if (gf == null) {
                    continue;
                }

                if (gf.id == exonicFeatureID) {
                    int exonID = Util.parseInt(gf.name.substring(0, gf.name.indexOf(':')));
                    int relativeCodingStartPos = Util.parseInt(gf.name.substring(gf.name.indexOf(':') + 1));
                    //to do something about insertion  
                    GeneFeature gf1 = calculateAminoAcidInsertion(relativeCodingStartPos, ref, allele, startPos, geneSym);
                    gf1.setName(refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonID + ":" + gf1.getName());

                    gfList.add(gf1);
                } else {
                    gfList.add(gf);
                }
            } else if (allele.startsWith("-") || allele.endsWith("-")) {
                startPos = oldStartPos;
                count = 0;
                int delLen = 0;

                for (int i = allele.length() - 1; i >= 0; i--) {
                    if (allele.charAt(i) != '-') {
                        count++;
                    } else {
                        delLen++;
                    }
                }

                startPos = oldStartPos + count;

                GeneFeature gfStart = findCrudeFeature(startPos, upstreamDis, downstreamDis, splicingDis, ref, allele);
                int endPos = 0;

                if (strand == '-') {
                    endPos = startPos - delLen + 1;
                } else {
                    endPos = startPos + delLen;
                }

                GeneFeature gfEnd = findCrudeFeature(endPos, upstreamDis, downstreamDis, splicingDis, ref, allele);
                if (gfStart == null && gfEnd == null) {
                    continue;
                } else if (gfStart != null && gfEnd != null) {
                    if (gfStart.id == gfEnd.id) {
                        if (gfStart.id == exonicFeatureID) {
                            int exonIDStartPos = Util.parseInt(gfStart.name.substring(0, gfStart.name.indexOf(':')));
                            int relativeCodingStartPos = Util.parseInt(gfStart.name.substring(gfStart.name.indexOf(':') + 1));
                            //to do something about deletion  
                            GeneFeature gf1 = calculateAminoAcidDeletion(relativeCodingStartPos, ref, allele, delLen, geneSym);
                            int index = gf1.getName().lastIndexOf(':');
                            if (index < 0) {
                                gf1.setName(refID + ":" + gf1.getName() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos);
                            } else {
                                gf1.setName(refID + ":" + gf1.getName().substring(0, index) + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos + ":" + gf1.getName().substring(index + 1));
                            }

                            gfList.add(gf1);
                        } else {
                            gfList.add(gfStart);
                        }
                    } else {
                        if (gfStart.id == exonicFeatureID) {
                            int exonIDStartPos = Util.parseInt(gfStart.name.substring(0, gfStart.name.indexOf(':')));
                            int relativeCodingStartPos = Util.parseInt(gfStart.name.substring(gfStart.name.indexOf(':') + 1));
                            GeneFeature gf1 = calculateAminoAcidDeletion(relativeCodingStartPos, ref, allele, delLen, geneSym);
                            int index = gf1.getName().lastIndexOf(':');
                            if (gfStart.id != gfEnd.id) {
                                if (index < 0) {
                                    gf1.setName(refID + ":" + gf1.getName() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + +exonIDStartPos + "-" + GlobalManager.VAR_FEATURE_NAMES[gfStart.id]);
                                } else {
                                    gf1.setName(refID + ":" + gf1.getName().substring(0, index) + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos + "-" + GlobalManager.VAR_FEATURE_NAMES[gfStart.id] + ":" + gf1.getName().substring(index + 1));
                                }

                            } else {
                                if (index < 0) {
                                    gf1.setName(refID + ":" + gf1.getName() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos);
                                } else {
                                    gf1.setName(refID + ":" + gf1.getName().substring(0, index) + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos + ":" + gf1.getName().substring(index + 1));
                                }
                            }
                            gfList.add(gf1);
                        } else if (gfEnd.id == exonicFeatureID) {
                            int exonIDStartPos = Util.parseInt(gfEnd.name.substring(0, gfEnd.name.indexOf(':')));
                            int relativeCodingStartPos = Util.parseInt(gfEnd.name.substring(gfEnd.name.indexOf(':') + 1));

                            //note calculateAminoAcidDeletionAtRightTail has not finished yet and simple put is as unknonw
                            gfList.add(gfEnd);
                            // System.out.println(oldStartPos);

                            GeneFeature gf1 = calculateAminoAcidDeletionAtRightTail(relativeCodingStartPos, ref, allele, delLen, geneSym);
                            int index = gf1.getName().lastIndexOf(':');
                            if (gfStart.id != gfEnd.id) {
                                if (index < 0) {
                                    gf1.setName(refID + ":" + gf1.getName() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":" + GlobalManager.VAR_FEATURE_NAMES[gfStart.id] + "-exon" + exonIDStartPos);
                                } else {
                                    gf1.setName(refID + ":" + gf1.getName().substring(0, index) + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":" + GlobalManager.VAR_FEATURE_NAMES[gfStart.id] + "-exon" + exonIDStartPos + ":" + gf1.getName().substring(index + 1));
                                }
                            } else {
                                if (index < 0) {
                                    gf1.setName(refID + ":" + gf1.getName() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos);
                                } else {
                                    gf1.setName(refID + ":" + gf1.getName().substring(0, index) + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos + ":" + gf1.getName().substring(index + 1));
                                }
                            }
                            gfList.add(gf1);

                        } else {
                            //not sure this is correct or not
                            gfList.add(gfStart);
                        }
                    }
                } else if (gfStart != null) {
                    if (gfStart.id == exonicFeatureID) {
                        int exonIDStartPos = Util.parseInt(gfStart.name.substring(0, gfStart.name.indexOf(':')));
                        int relativeCodingStartPos = Util.parseInt(gfStart.name.substring(gfStart.name.indexOf(':') + 1));
                        //to do something about deletion  
                        GeneFeature gf1 = calculateAminoAcidDeletion(relativeCodingStartPos, ref, allele, startPos, geneSym);

                        int index = gf1.getName().lastIndexOf(':');
                        if (index < 0) {
                            gf1.setName(refID + ":" + gf1.getName() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos);
                        } else {
                            gf1.setName(refID + ":" + gf1.getName().substring(0, index) + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonIDStartPos + ":" + gf1.getName().substring(index + 1));
                        }
                        gfList.add(gf1);
                    } else {
                        gfList.add(gfStart);
                    }
                } else {
                    gfList.add(gfEnd);
                }

            } else {
                startPos = oldStartPos;
                startAllele = 0;
                if (ref.length() > 1 && ref.length() == allele.length()) {
                    if (strand == '+') {
                        while (startAllele < ref.length() && ref.charAt(startAllele) == allele.charAt(startAllele)) {
                            startAllele++;
                        }
                        startPos = oldStartPos + startAllele;
                    } else {
                        startAllele = ref.length() - 1;
                        while (startAllele >= 0 && ref.charAt(startAllele) == allele.charAt(startAllele)) {
                            startAllele--;
                        }
                        startPos = oldStartPos + (ref.length() - 1 - startAllele);
                    }
                }

                //it is an sustitution
                GeneFeature gf = findCrudeFeature(startPos, upstreamDis, downstreamDis, splicingDis, ref, allele);
                if (gf == null) {
                    continue;
                }
                if (gf.id == exonicFeatureID) {
                    int exonID = Util.parseInt(gf.name.substring(0, gf.name.indexOf(':')));
                    int relativeCodingStartPos = Util.parseInt(gf.name.substring(gf.name.indexOf(':') + 1));

                    errorCode[0] = 0;
                    GeneFeature gf1 = calculateAminoAcidChange(relativeCodingStartPos, ref.charAt(startAllele), allele.charAt(startAllele), startPos, geneSym, errorCode);
                    if (errorCode[0] == 1) {
                        LOG.warn("The RefmRNA " + refID + " has no sequence data for the variant at chr" + chr + ":" + oldStartPos);
                    } else if (errorCode[0] == 2) {
                        LOG.warn("The RefmRNA " + refID + " has no sequence data for the variant at chr" + chr + ":" + oldStartPos);
                    } else if (errorCode[0] == 3) {
                        LOG.warn("The reference allele " + ref + " of chr" + chr + ":" + oldStartPos + " in the sample data and database are not identical on " + refID + " of " + geneSym);
                    }
                    int index = gf1.getName().lastIndexOf(':');
                    if (index < 0) {
                        gf1.setName(refID + ":" + gf1.getName() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonID);
                    } else {
                        gf1.setName(refID + ":" + gf1.getName().substring(0, index) + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + exonID + ":" + gf1.getName().substring(index + 1));
                    }
                    gfList.add(gf1);
                } else {
                    gfList.add(gf);
                }
            }
        }

        if (gfList.isEmpty()) {
            return null;
        }
        if (gfList.size() == 1) {
            GeneFeature gf = gfList.get(0);
            return gf;
        } else {
            Collections.sort(gfList, new GeneFeatureComparator());
            GeneFeature gf = gfList.get(0);
            if (gf.getInfor() == null) {
                gf.setInfor("");
            }
            for (int i = 1; i < gfList.size(); i++) {
                GeneFeature gf1 = gfList.get(i);
                gf.setName(gf.getName() + "&" + gf1.getName());
                gf.setInfor(gf.getInfor() + (gf1.getInfor() == null ? "" : "&" + gf1.getInfor()));
            }
            return gf;
        }

    }

    public GeneFeature findCrudeFeature(int pos, int upstreamDis, int downstreamDis, int splicingDis, String ref, String alt) throws Exception {
        if (strand == '0') {
            throw new Exception("Unknown strand at " + refID);
        }
        int relativeCodingStartPos = -1;
        int exonIndex = binarySearch(pos, 0, exonNum - 1);
        //  System.out.println(pos);

        //Very important: In UCSC refGene regardless of strand the exon start site is not included . eg. for example in 93615298-93620445, the acual coding region should be 93615299-93620445
        //all coordinates are 1-based
        //note in the refgene database
        //the leftside boundaries of exon region are inclusive and rightside boundaries are exclusive 
        //Since v0.8, we start to use the hgvs format to output the annotation http://www.hgvs.org/mutnomen/examplesDNA.html#sub
        if (strand == '+') {
            if (exonIndex < 0) {
                exonIndex = -exonIndex - 1;
                if (exonIndex == exonNum) {
                    //after all exons                   
                    if (pos > end + downstreamDis) {
                        //intergenic
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("intergenic"), "intergenic");
                    } else {
                        //downstream	12	variant overlaps 1-kb region downtream of transcription end site (use -neargene to change this) 
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("downstream"), refID + ":c.*" + (pos - end) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":downstream");
                    }
                } else if (exonIndex == 0) {
                    if (pos <= start - upstreamDis) {
                        //intergenic
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("intergenic"), "intergenic");
                    } else if (pos <= start) {
                        // upstream	11	variant overlaps 1-kb region upstream of transcription start site 
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("upstream"), refID + ":c.-" + (start - pos) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":upstream");
                    } else if (noCodingExon) {
                        // ncRNA	7	variant overlaps a transcript without coding annotation in the gene definition (see Notes below for more explanation) 
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("ncRNA"), refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= codingStart) {
                        //5UTR	8	variant overlaps a 5' untranslated region 
                        relativeCodingStartPos = codingStart - pos + 1;
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("5UTR"), refID + ":c.-" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR");
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic   
                        //do not know why my input sample always have 1-base shift compared to the refGene coordinates on forward RefmRNA   

                        relativeCodingStartPos = pos - codingStart - 1;
                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), "1:" + relativeCodingStartPos);
                    } else {
                        //it is ver unlikely
                        // 3UTR	9	variant overlaps a 3' untranslated region 
                        relativeCodingStartPos = (pos - codingEnd);
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("3UTR"), refID + ":c.*" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR");
                    }
                } else {
                    //  the shiftBpDel must be between 1 and exonIndex-1
                    if (noCodingExon) {
                        // ncRNA	7	variant overlaps a transcript without coding annotation in the gene definition (see Notes below for more explanation) 
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("ncRNA"), refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= exons.get(exonIndex - 1).end + splicingDis) {
                        //this is a donor                      
                        relativeCodingStartPos = pos - codingStart - 1;
                        for (int i = exonIndex - 1; i > codingStartSiteExonID; i--) {
                            relativeCodingStartPos -= intronLength.getQuick(i - 1);
                        }
                        //splicing	6	variant is within 2-bp of a splicing junction (use -splicing_threshold to change this) 
                        //return new GeneFeature(GlobalManager.VarFeatureIDMap.get("splicing"), refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":splicingGTdonor" + (exonIndex) + "+" + Math.abs(exons.get(exonIndex - 1).end - pos + 1));
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("splicing"), refID + ":c." + relativeCodingStartPos + "+" + Math.abs(pos - exons.get(exonIndex - 1).end) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + (exonIndex + 1) + "GTdonor");

                    } else if (pos <= exons.get(exonIndex).start - splicingDis) {
                        //5UTR	8	variant overlaps a 5' untranslated region 
                        // System.out.println("----------------------\startAllele" + ( exonIndex) + "\startAllele" + (exons.get(exonIndex).start - pos));
                        int offSet1 = exons.get(exonIndex).start - pos;
                        int offSet2 = pos - exons.get(exonIndex - 1).end;
                        relativeCodingStartPos = pos - codingStart - 1;

                        StringBuilder sb = new StringBuilder(":c.");
                        if (offSet1 < offSet2) {
                            for (int i = exonIndex; i > codingStartSiteExonID; i--) {
                                relativeCodingStartPos -= intronLength.getQuick(i - 1);
                            }
                            sb.append(relativeCodingStartPos + offSet1 + 2);
                            sb.append('-');
                            sb.append(offSet1 + 1);
                        } else {
                            for (int i = exonIndex - 1; i > codingStartSiteExonID; i--) {
                                relativeCodingStartPos -= intronLength.getQuick(i - 1);
                            }
                            sb.append(relativeCodingStartPos - offSet2 + 1);
                            sb.append('+');
                            sb.append(offSet2);
                        }
                        sb.append(ref).append(">").append(alt);
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("intronic"), refID + sb.toString() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":intronic" + (exonIndex));
                    } else if (pos <= exons.get(exonIndex).start) {
                        //the 5' and 3' are relative to the closet exon   
                        relativeCodingStartPos = pos - codingStart - 1;
                        for (int i = exonIndex; i > codingStartSiteExonID; i--) {
                            relativeCodingStartPos -= intronLength.getQuick(i - 1);
                        }
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("splicing"), refID + ":c." + (relativeCodingStartPos + exons.get(exonIndex).start - pos + 2) + "-" + (exons.get(exonIndex).start - pos + 1) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + (exonIndex) + "AGacceptor");
                    } else if (pos <= codingStart) {
                        //5UTR	8	variant overlaps a 5' untranslated region 
                        relativeCodingStartPos = codingStart - pos + 1;
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("5UTR"), refID + ":c.-" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR");
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic   
                        //do not know why my input sample always have 1-base shift compared to the refGene coordinates on forward RefmRNA      
                        relativeCodingStartPos = pos - codingStart - 1;
                        for (int i = exonIndex; i > codingStartSiteExonID; i--) {
                            relativeCodingStartPos -= intronLength.getQuick(i - 1);
                        }
                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), (exonIndex + 1) + ":" + relativeCodingStartPos);
                    } else {
                        relativeCodingStartPos = (pos - codingEnd);
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("3UTR"), refID + ":c.*" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR");
                    }
                }
            } else {
                //just at the  leftside boundary which is exclusive
                if (noCodingExon) {
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("ncRNA"), refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                } else if (pos <= codingStart) {
                    relativeCodingStartPos = (codingStart - pos + 1);
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("5UTR"), refID + ":c.-" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR");
                } else if (pos <= codingEnd) {
                    relativeCodingStartPos = pos - codingStart - 1;
                    for (int i = exonIndex; i > codingStartSiteExonID; i--) {
                        relativeCodingStartPos -= intronLength.getQuick(i - 1);
                    }
                    //special coding for the exonic variantsl it will be parsed later on
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), (exonIndex + 1) + ":" + relativeCodingStartPos);
                    //  return new GeneFeature(GlobalManager.VarFeatureIDMap.get("splicing"), refID+":("+exonNum+"Exons"  + (multipleMapping ? "MultiMap)" : ")") + ":3splicing" + (exonIndex + 1));
                } else {
                    relativeCodingStartPos = (pos - codingEnd);
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("3UTR"), refID + ":c.*" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR");
                }
            }
        } else if (strand == '-') {
            if (exonIndex < 0) {
                exonIndex = -exonIndex - 1;

                //after all exons
                if (exonIndex == exonNum) {
                    if (pos <= end + upstreamDis) {
                        // upstream 	11 	variant overlaps 1-kb region downtream of transcription end site (use -neargene to change this)
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("upstream"), refID + ":c.-" + (pos - end) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":upstream");
                    } else {
                        //intergenic
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("intergenic"), "intergenic");
                    }
                } else if (exonIndex == 0) {
                    //the  leftside boundary is exclusive
                    if (pos <= start - downstreamDis) {
                        //intergenic
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("intergenic"), "intergenic");
                    } else if (pos <= start) {
                        // upstream 	12 	variant overlaps 1-kb region upstream of transcription start site
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("downstream"), refID + ":c.*" + (start - pos) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":downstream");
                    } else if (noCodingExon) {
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("ncRNA"), refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= codingStart) {
                        relativeCodingStartPos = codingStart - pos + 1;
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("3UTR"), refID + ":c.*" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR");
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic    
                        relativeCodingStartPos = codingEnd - pos;
                        if (exonNum > 1) {
                            for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                                relativeCodingStartPos -= intronLength.getQuick(i);
                            }
                        }

                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), exonNum + ":" + relativeCodingStartPos);

                    } else {
                        relativeCodingStartPos = (pos - codingEnd);
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("5UTR"), refID + ":c.-" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR");
                    }
                } else {
                    //the shiftBpDel must be between 1 and exonIndex-1
                    if (noCodingExon) {
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("ncRNA"), refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                    } else if (pos <= exons.get(exonIndex - 1).end + splicingDis) {
                        //the 5' and 3' are relative to the closet exon
                        relativeCodingStartPos = codingEnd - pos;
                        for (int i = exonIndex - 1; i < codingStartSiteExonID; i++) {
                            relativeCodingStartPos -= intronLength.getQuick(i);
                        }
                        relativeCodingStartPos++;
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("splicing"), refID + ":c." + (relativeCodingStartPos + (pos - exons.get(exonIndex - 1).end)) + "-" + (pos - exons.get(exonIndex - 1).end) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + (exonNum - exonIndex + 1) + "AGacceptor");
                    } else if (pos <= exons.get(exonIndex).start - splicingDis) {
                        // intronic 	4 	variant overlaps an intron 
                        // System.out.println("----------------------\startAllele" + (exonNum - exonIndex) + "\startAllele" + (exons.get(exonIndex).start - pos));
                        int offSet1 = exons.get(exonIndex).start - pos;
                        int offSet2 = pos - exons.get(exonIndex - 1).end;
                        relativeCodingStartPos = codingEnd - pos;

                        StringBuilder sb = new StringBuilder(":c.");
                        if (offSet1 < offSet2) {
                            for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                                relativeCodingStartPos -= intronLength.getQuick(i);
                            }
                            sb.append(relativeCodingStartPos - offSet1);
                            sb.append('+');
                            sb.append(offSet1 + 1);
                        } else {
                            for (int i = exonIndex - 1; i < codingStartSiteExonID; i++) {
                                relativeCodingStartPos -= intronLength.getQuick(i);
                            }
                            sb.append(relativeCodingStartPos + offSet2 + 1);
                            sb.append('-');
                            sb.append(offSet2);
                        }
                        sb.append(ref).append(">").append(alt);
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("intronic"), refID + sb.toString() + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":intronic" + (exonNum - exonIndex));
                    } else if (pos <= exons.get(exonIndex).start) {
                        //the 5' and 3' are relative to the closet exon
                        relativeCodingStartPos = codingEnd - pos;
                        for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                            relativeCodingStartPos -= intronLength.getQuick(i);
                        }
                        relativeCodingStartPos--;
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("splicing"), refID + ":c." + relativeCodingStartPos + "+" + Math.abs(exons.get(exonIndex).start - pos + 1) + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":exon" + (exonNum - exonIndex) + "GTdonor");
                    } else if (pos <= codingStart) {
                        relativeCodingStartPos = (codingStart - pos);
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("3UTR"), refID + ":c.*" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR");
                    } else if (pos <= codingEnd) {
                        //it must in the coiding region
                        // exonic   
                        //probably because codingEnd is exclusive, no -1 needed as in the forward strand

                        relativeCodingStartPos = codingEnd - pos;
                        for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                            relativeCodingStartPos -= intronLength.getQuick(i);
                        }
                        // exonic  
                        //very important: usaully what we have in sample are alleles in forward strand
                        //but in the database RefmRNA on reverse strand will be on reverse strand before translated to amino accid

                        //special coding for the exonic variantsl it will be parsed later on
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), (exonNum - exonIndex) + ":" + relativeCodingStartPos);

                    } else {
                        relativeCodingStartPos = (pos - codingEnd);
                        return new GeneFeature(GlobalManager.VarFeatureIDMap.get("5UTR"), refID + ":c.-" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR");
                    }
                }
            } else {
                //just at the  rightside boundary which is inclusive for reverse strand mRAN
                if (noCodingExon) {
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("ncRNA"), refID + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":ncRNA");
                } else if (pos <= codingStart) {
                    relativeCodingStartPos = (codingStart - pos) + 1;
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("3UTR"), refID + ":c.*" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":3UTR");
                } else if (pos <= codingEnd) {
                    //it must in the coiding region
                    // exonic   
                    //probably because codingEnd is exclusive, no -1 needed as in the forward strand
                    relativeCodingStartPos = codingEnd - pos;
                    for (int i = exonIndex; i < codingStartSiteExonID; i++) {
                        relativeCodingStartPos -= intronLength.getQuick(i);
                    }
                    // exonic  
                    //very important: usaully what we have in sample are alleles in forward strand
                    //but in the database RefmRNA on reverse strand will be on reverse strand before translated to amino accid 

                    //special coding for the exonic variantsl it will be parsed later on
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), (exonNum - exonIndex) + ":" + relativeCodingStartPos);
                } else {
                    relativeCodingStartPos = (pos - codingEnd);
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("5UTR"), refID + ":c.-" + relativeCodingStartPos + ref + ">" + alt + ":(" + exonNum + "Exons" + (multipleMapping ? "MultiMap)" : ")") + ":5UTR");
                }
            }
        } else {
            //unrecognzed strand infor
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("unknown"), "unknown");
        }
    }

    /*
     nonsynonymous	0	Variants result in a codon coding for a different amino acid (missense) and an amino acid codon to a stop codon (stopgain) and a stop codon to an amino acid codon (stoplos)
     synonymous	1	
     splicing	2	variant is within 2-bp of a splicing junction (use -splicing_threshold to change this) 
     ncRNA	3	variant overlaps a transcript without coding annotation in the gene definition (see Notes below for more explanation) 
     5UTR	4	variant overlaps a 5' untranslated region 
     3UTR	5	variant overlaps a 3' untranslated region 
     intronic	6	variant overlaps an intron 
     upstream	7	variant overlaps 1-kb region upstream of transcription start site 
     downstream	8	variant overlaps 1-kb region downtream of transcription end site (use -neargene to change this) 
     intergenic	9	variant is in intergenic region     
     * 
     */
    public GeneFeature calculateAminoAcidChange(int relativeCodingStartPosInRef, char ref, char alt, int absPos, String geneSym, byte[] errorCode) {
        int shiftBpDel = 0;
        int relativeCodingStartPosIncDNA = relativeCodingStartPosInRef;
        StringBuilder info = new StringBuilder("c.");

        if (delSites != null) {
            if (delSites[0] == 0) {
                //when a deletion starts at 0 position, it is often problematic. So I prefer not to annoate it clearly
                info.append(relativeCodingStartPosIncDNA + 1);
                info.append(ref);
                info.append('>');
                info.append(alt);
                errorCode[0] = 0;
                info.append(":exonic");
                return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
            }
            shiftBpDel = Arrays.binarySearch(delSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpDel < 0) {
                shiftBpDel = -shiftBpDel - 1;
            }
            //because the referenc genome has deletions, we should shif the relative CDNA positions
            relativeCodingStartPosIncDNA += shiftBpDel;

        }

        int shiftBpIns = 0;
        if (insSites != null) {
            shiftBpIns = Arrays.binarySearch(insSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpIns < 0) {
                shiftBpIns = -shiftBpIns - 1;
            }
            relativeCodingStartPosIncDNA -= shiftBpIns;
        }

        int incodonIndex = (relativeCodingStartPosIncDNA) % 3;
        int curCodonStart = relativeCodingStartPosIncDNA - incodonIndex;

        if (incodonIndex < 0) {
            incodonIndex += 3;
        }

        //start with 0
        info.append(relativeCodingStartPosIncDNA + 1);
        info.append(ref);
        info.append('>');
        info.append(alt);
        if (mRnaSequence == null) {
            // System.out.println("The RefmRNA " + refID + " has no sequence data so the variant at site "+absPos+ " have no detailed coding change information");
            //Warning. not sure this is opproperate or not
            errorCode[0] = 1;
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }

        /* //Strange! When I enable the following code, I met a lot of reference allele mistached variants 
         if (delSites != null) {
         shiftBpDel = Arrays.binarySearch(delSites, curCodonStart + codingStartRelativeSiteInSequence);
         if (shiftBpDel < 0) {
         shiftBpDel = -shiftBpDel - 1;
         }
         //because the referenc genome has deletions, we should shif the relative CDNA positions
         }

         if (insSites != null) {
         shiftBpIns = Arrays.binarySearch(insSites, curCodonStart + codingStartRelativeSiteInSequence);
         if (shiftBpIns < 0) {
         shiftBpIns = -shiftBpIns - 1;
         } 
         }
         */
        curCodonStart += codingStartRelativeSiteInSequence;
        //because what we will obtain is the reference DNA so we need go back to the posistions of referce genome to get the correct referecne DNA
        curCodonStart -= shiftBpDel;
        curCodonStart += shiftBpIns;

        //warning: by default it is 4. but this mixed the synonymous and unmapped varaint      
        if (curCodonStart < 0) {
            //warning: this mixed the synonymous and unmapped varaint 
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }

        String codon;
        curCodonStart += 3;
        if (curCodonStart <= mRnaSequence.length()) {
            //mRnaSequence acually saved the cDNA sequence
            codon = mRnaSequence.substring(curCodonStart - 3, curCodonStart).toUpperCase();
        } else {
            errorCode[0] = 2;
            //warning: this mixed the synonymous and unmapped varaint 
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }
        //similar format c.G807A:p.Q269Q
        if (codon.charAt(incodonIndex) != ref) {
            //LOG.warn("The reference alleles in the sample data and database are not identical at site " + absPos + " on " + refID + " of " + geneSym);
            errorCode[0] = 3;
            //System.out.println("The reference alleles in the sample data and database are not identical at site " + absPos + " on " + refID + " of " + geneSym);
            //Warning. not sure this is opproperate or not
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        } else {
            char refP = '?';
            char altP = '?';
            info.append(':');
            info.append("p.");
            if (GlobalManager.codonTable.containsKey(codon)) {
                refP = GlobalManager.codonTable.get(codon);
                info.append(refP);
            } else {
                //Unspecified or unknown amino acid
                info.append('?');
            }
            int codonIndex = (relativeCodingStartPosInRef) / 3 + 1;
            StringBuilder sb = new StringBuilder(codon);
            sb.setCharAt(incodonIndex, alt);

            info.append(codonIndex);
            codon = sb.toString().toUpperCase();
            if (GlobalManager.codonTable.containsKey(codon)) {
                altP = GlobalManager.codonTable.get(codon);
                info.append(altP);
            } else {
                //Unspecified or unknown amino acid
                info.append('?');
            }

            GeneFeature gf = new GeneFeature();
            if (proteinDomainList != null) {
                for (ProteinDomain pd : proteinDomainList) {
                    if (codonIndex >= pd.start && codonIndex <= pd.end) {
                        gf.infor = uniprotID + '|' + pd.toString();
                    }
                }
            }

            if (refP == altP) {
                info.append(":");
                info.append("synonymous");

                gf.id = GlobalManager.VarFeatureIDMap.get("synonymous");
                gf.name = info.toString();
                return gf;
                // return new GeneFeature(GlobalManager.VarFeatureIDMap.get("synonymous"), info.toString());
            } else {
                info.append(":");
                if (codonIndex == 1 && refP == 'M') {
                    info.append("startloss");
                    gf.id = GlobalManager.VarFeatureIDMap.get("startloss");
                    gf.name = info.toString();
                    return gf;
                } else if (refP != '*' && altP != '*') {
                    info.append("missense");

                    gf.id = GlobalManager.VarFeatureIDMap.get("missense");
                    gf.name = info.toString();
                    return gf;
                    //return new GeneFeature(GlobalManager.VarFeatureIDMap.get("missense"), info.toString());
                } else if (refP != '*') {
                    info.append("stopgain");

                    gf.id = GlobalManager.VarFeatureIDMap.get("stopgain");
                    gf.name = info.toString();
                    return gf;
                    // return new GeneFeature(GlobalManager.VarFeatureIDMap.get("stopgain"), info.toString());
                } else {
                    info.append("stoploss");

                    gf.id = GlobalManager.VarFeatureIDMap.get("stoploss");
                    gf.name = info.toString();
                    return gf;
                    //  return new GeneFeature(GlobalManager.VarFeatureIDMap.get("stoploss"), info.toString());
                }
            }
        }
    }

    public GeneFeature calculateAminoAcidDeletion(int relativeCodingStartPosInRef, String ref, String alt, int delSeqLen, String geneSym) {
        int shiftBpDel = 0;
        int relativeCodingStartPosIncDNA = relativeCodingStartPosInRef;

        if (delSites != null) {
            shiftBpDel = Arrays.binarySearch(delSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpDel < 0) {
                shiftBpDel = -shiftBpDel - 1;
            }
            //because the referenc genome has deletions, we should shif the relative CDNA positions
            relativeCodingStartPosIncDNA += shiftBpDel;
        }

        int shiftBpIns = 0;
        if (insSites != null) {
            shiftBpIns = Arrays.binarySearch(insSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpIns < 0) {
                shiftBpIns = -shiftBpIns - 1;
            }
            relativeCodingStartPosIncDNA -= shiftBpIns;
        }

        int incodonIndex = (relativeCodingStartPosIncDNA) % 3;
        int curCodonStart = relativeCodingStartPosIncDNA - incodonIndex;

        if (incodonIndex < 0) {
            incodonIndex += 3;
        }

        StringBuilder info = new StringBuilder("c.");

        if (alt.endsWith("-")) {
            //forward sequence
            int s = ref.length() - delSeqLen - 1;
            if (s >= 0) {
                info.append(ref.charAt(s));
            }

            info.append(relativeCodingStartPosIncDNA + (ref.length() - delSeqLen));
            info.append("del-");
            info.append(ref.substring(ref.length() - delSeqLen));
        } else {
            //reverse sequence
            info.append('?');
            info.append(relativeCodingStartPosIncDNA - (ref.length() - delSeqLen));
            info.append("del-");
            info.append(ref.substring(0, delSeqLen));
        }

        if (mRnaSequence == null) {
            // System.out.println("The RefmRNA " + refID + " has no sequence data so the variant at site "+absPos+ " have no detailed coding change information");
            //Warning. not sure this is opproperate or not
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }

        curCodonStart += codingStartRelativeSiteInSequence;
        //because what we will obtain is the reference DNA so we need go back to the posistions of referce genome to get the correct referecne DNA
        curCodonStart -= shiftBpDel;
        curCodonStart += shiftBpIns;

        if (curCodonStart < 0) {
            //warning: this mixed the synonymous and unmapped varaint 
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }
        String codon;
        curCodonStart += 3;
        if (curCodonStart <= mRnaSequence.length()) {
            codon = mRnaSequence.substring(curCodonStart - 3, curCodonStart).toUpperCase();
        } else {
            //warning: this mixed the synonymous and unmapped varaint 
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }

        /*
         //ignore this, sometimes ref is not avaible for indels         
         if (codon.charAt(incodonIndex) != ref) {
         System.out.println("The reference alleles in the sample data and database are not identical at site " + absPos + " on " + refID + " of " + geneSym);
         //Warning. not sure this is opproperate or not
         return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
         }
         * 
         */
        Character refP = '?';
        info.append(':');
        info.append("p.");
        if (GlobalManager.codonTable.containsKey(codon)) {
            refP = GlobalManager.codonTable.get(codon);
            if (refP == null) {
                System.err.println("Unknown code for " + codon);
                refP = '?';
            }
            info.append(refP);
        } else {
            //Unspecified or unknown amino acid
            info.append('?');
        }

        int codonIndex = (relativeCodingStartPosIncDNA) / 3 + 1;

        info.append(codonIndex);
        GeneFeature gf = new GeneFeature();

        if (proteinDomainList != null) {
            for (ProteinDomain pd : proteinDomainList) {
                if (codonIndex >= pd.start && codonIndex <= pd.end) {
                    gf.infor = uniprotID + '|' + pd.toString();
                }
            }
        }

        if (delSeqLen % 3 != 0) {
            info.append(":frameshift");
            gf.id = GlobalManager.VarFeatureIDMap.get("frameshift");
            gf.name = info.toString();
            return gf;

            /*
             boolean hasStopCoden = false;
             int startCheckSite = 0;
            
             StringBuilder altSB = new StringBuilder();
             if (alt.endsWith("-")) {
             //if this is a forward strand
             //but it looks like the same as the reverse
             if (incodonIndex == 2) {
             startCheckSite = 1;
             } else if (incodonIndex == 1) {
             info.append('?');
             startCheckSite = 2;
             } else if (incodonIndex == 0) {
             startCheckSite = 0;
             }
             altSB.append(ref.substring(ref.length() - delSeqLen));
             } else {
             if (incodonIndex == 2) {
             startCheckSite = 1;
             } else if (incodonIndex == 1) {
             info.append('?');
             startCheckSite = 2;
             } else if (incodonIndex == 0) {
             startCheckSite = 0;
             }
             altSB.append(ref.substring(0, delSeqLen));
             }
            
             for (int i = startCheckSite; i < delSeqLen; i += 3) {
             if (i + 3 > altSB.length()) {
             //Unspecified or unknown amino acid
             info.append('?');
             break;
             }
             codon = altSB.substring(i, i + 3).toUpperCase();
             if (GlobalManager.codonTable.containsKey(codon)) {
             char altP = GlobalManager.codonTable.get(codon);
             if (altP == '*') {
             hasStopCoden = true;
             }
             info.append(altP);
             } else {
             //Unspecified or unknown amino acid
             info.append('?');
             }
             }
             if (hasStopCoden) {
             gf.id = GlobalManager.VarFeatureIDMap.get("stopgain");
             gf.name = info.toString();
             return gf;
             // return new GeneFeature(GlobalManager.VarFeatureIDMap.get("stoploss"), info.toString());
             } else {
             gf.id = GlobalManager.VarFeatureIDMap.get("frameshift");
             gf.name = info.toString();
             return gf;
             //  return new GeneFeature(GlobalManager.VarFeatureIDMap.get("nonframeshift"), info.toString());
             }
             */
        } else {
            //for the forward strand the start positions is before the deletion from the second nucleotide
            info.append("del");
            StringBuilder altSB = new StringBuilder();

            boolean hasStopLoss = false;
            int startCheckSite = 0;
            if (alt.endsWith("-")) {
                //if this is a forward strand
                //but it looks like the same as the reverse
                if (incodonIndex == 2) {
                    startCheckSite = 1;
                } else if (incodonIndex == 1) {
                    info.append('?');
                    startCheckSite = 2;
                } else if (incodonIndex == 0) {
                    startCheckSite = 0;
                }
                altSB.append(ref.substring(ref.length() - delSeqLen));
            } else {
                if (incodonIndex == 2) {
                    startCheckSite = 1;
                } else if (incodonIndex == 1) {
                    info.append('?');
                    startCheckSite = 2;
                } else if (incodonIndex == 0) {
                    startCheckSite = 0;
                }
                altSB.append(ref.substring(0, delSeqLen));
            }

            for (int i = startCheckSite; i < delSeqLen; i += 3) {
                if (i + 3 > altSB.length()) {
                    //Unspecified or unknown amino acid
                    info.append('?');
                    break;
                }
                codon = altSB.substring(i, i + 3).toUpperCase();
                if (GlobalManager.codonTable.containsKey(codon)) {
                    char altP = GlobalManager.codonTable.get(codon);
                    if (altP == '*') {
                        hasStopLoss = true;
                    }
                    info.append(altP);
                } else {
                    //Unspecified or unknown amino acid
                    info.append('?');
                }
            }
            if (hasStopLoss) {
                gf.id = GlobalManager.VarFeatureIDMap.get("stoploss");
                info.append(":stoploss");
                gf.name = info.toString();
                return gf;
                // return new GeneFeature(GlobalManager.VarFeatureIDMap.get("stoploss"), info.toString());
            } else {
                gf.id = GlobalManager.VarFeatureIDMap.get("nonframeshift");
                info.append(":nonframeshift");
                gf.name = info.toString();
                return gf;
                //  return new GeneFeature(GlobalManager.VarFeatureIDMap.get("nonframeshift"), info.toString());
            }
        }

    }

//note:     relativeCodingStartPos is also deleted
    public GeneFeature calculateAminoAcidDeletionAtRightTail(int relativeCodingStartPosInRef, String ref, String alt, int delSeqLen, String geneSym) {
        int shiftBpDel = 0;
        int relativeCodingStartPosIncDNA = relativeCodingStartPosInRef;

        if (delSites != null) {
            shiftBpDel = Arrays.binarySearch(delSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpDel < 0) {
                shiftBpDel = -shiftBpDel - 1;
            }
            //because the referenc genome has deletions, we should shif the relative CDNA positions
            relativeCodingStartPosIncDNA += shiftBpDel;
        }

        int shiftBpIns = 0;
        if (insSites != null) {
            shiftBpIns = Arrays.binarySearch(insSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpIns < 0) {
                shiftBpIns = -shiftBpIns - 1;
            }
            relativeCodingStartPosIncDNA -= shiftBpIns;
        }
        int incodonIndex = (relativeCodingStartPosIncDNA) % 3;
        int curCodonStart = relativeCodingStartPosIncDNA - incodonIndex;

        if (incodonIndex < 0) {
            incodonIndex += 3;
        }

        StringBuilder info = new StringBuilder("c.");

        if (alt.endsWith("-")) {
            //forward sequence
            int s = ref.length() - delSeqLen - 1;
            if (s >= 0) {
                info.append(ref.charAt(s));
            }
            info.append(relativeCodingStartPosIncDNA + (ref.length() - delSeqLen));
            info.append("del-");
            info.append(ref.substring(ref.length() - delSeqLen));
        } else {
            //reverse sequence
            info.append('?');
            info.append(relativeCodingStartPosIncDNA - (ref.length() - delSeqLen));
            info.append("del-");
            info.append(ref.substring(0, delSeqLen));
        }

        if (mRnaSequence == null) {
            // System.out.println("The RefmRNA " + refID + " has no sequence data so the variant at site "+absPos+ " have no detailed coding change information");
            //Warning. not sure this is opproperate or not
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }

        /*
         if (codingStartRelativeSiteInSequence >= 0&&geneSym.equals("KIF1B")) {
         StringBuilder protSeq = new StringBuilder();
         for (int i = codingStartRelativeSiteInSequence; i < mRnaSequence.length(); i += 3) {
         String codon1 = mRnaSequence.substring(i, i + 3);
         protSeq.append(GlobalManager.codonTable.get(codon1));
         }
         System.out.println(protSeq.toString());
         }
         * 
         */
        curCodonStart += codingStartRelativeSiteInSequence;
        //because what we will obtain is the reference DNA so we need go back to the posistions of referce genome to get the correct referecne DNA
        curCodonStart -= shiftBpDel;
        curCodonStart += shiftBpIns;

        if (curCodonStart < 0) {
            //warning: this mixed the synonymous and unmapped varaint 
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }
        String codon;
        curCodonStart += 3;
        if (curCodonStart <= mRnaSequence.length()) {
            codon = mRnaSequence.substring(curCodonStart - 3, curCodonStart).toUpperCase();
        } else {
            //warning: this mixed the synonymous and unmapped varaint 
            info.append(":exonic");
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }
        /*
         //similar format c.G807A:p.Q269Q
         if (codon.charAt(incodonIndex) != ref) {
         System.out.println("The reference alleles in the sample data and database are not identical at site " + absPos + " on " + refID + " of " + geneSym);
         //Warning. not sure this is opproperate or not
         return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
         } else*/
        {
            char refP = '?';
            int codonIndex = (relativeCodingStartPosIncDNA) / 3 + 1;

            if (GlobalManager.codonTable.containsKey(codon)) {
                refP = GlobalManager.codonTable.get(codon);
            }
            info.append(':');
            info.append("p.");
            info.append(refP);
            info.append(codonIndex);

            if (delSeqLen % 3 != 0) {
                info.append(":frameshift");
                return new GeneFeature(GlobalManager.VarFeatureIDMap.get("frameshift"), info.toString());
            } else {
                //if (alt.startsWith("-"))
                info.append("del");
                boolean hasStopLoss = false;
                int startCheckSite = 0;
                StringBuilder altSB = new StringBuilder();
                if (alt.endsWith("-")) {
                    //if this is a forward strand
                    //but it looks like the same as the reverse
                    if (incodonIndex == 2) {
                        startCheckSite = 1;
                    } else if (incodonIndex == 1) {
                        info.append('?');
                        startCheckSite = 2;
                    } else if (incodonIndex == 0) {
                        startCheckSite = 0;
                    }
                    altSB.append(ref.substring(ref.length() - delSeqLen));
                } else {
                    if (incodonIndex == 2) {
                        startCheckSite = 1;
                    } else if (incodonIndex == 1) {
                        info.append('?');
                        startCheckSite = 2;
                    } else if (incodonIndex == 0) {
                        startCheckSite = 0;
                    }
                    altSB.append(ref.substring(0, delSeqLen));
                }

                for (int i = startCheckSite; i < delSeqLen; i += 3) {
                    if (i + 3 > altSB.length()) {
                        //Unspecified or unknown amino acid
                        info.append('?');
                        break;
                    }
                    codon = altSB.substring(i, i + 3).toUpperCase();
                    if (GlobalManager.codonTable.containsKey(codon)) {
                        char altP = GlobalManager.codonTable.get(codon);
                        if (altP == '*') {
                            hasStopLoss = true;
                        }
                        info.append(altP);
                    }
                }
                if (hasStopLoss) {
                    info.append(":stoploss");
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("stoploss"), info.toString());
                } else {
                    info.append(":nonframeshift");
                    return new GeneFeature(GlobalManager.VarFeatureIDMap.get("nonframeshift"), info.toString());
                }

            }
        }
    }

    public GeneFeature calculateAminoAcidInsertion(int relativeCodingStartPosInRef, String ref, String alt, int absPos, String geneSym) {
        int shiftBpDel = 0;
        int relativeCodingStartPosIncDNA = relativeCodingStartPosInRef;

        if (delSites != null) {
            shiftBpDel = Arrays.binarySearch(delSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpDel < 0) {
                shiftBpDel = -shiftBpDel - 1;
            }
            //because the referenc genome has deletions, we should shif the relative CDNA positions
            relativeCodingStartPosIncDNA += shiftBpDel;
        }

        int shiftBpIns = 0;
        if (insSites != null) {
            shiftBpIns = Arrays.binarySearch(insSites, relativeCodingStartPosIncDNA + codingStartRelativeSiteInSequence);
            if (shiftBpIns < 0) {
                shiftBpIns = -shiftBpIns - 1;
            }
            relativeCodingStartPosIncDNA -= shiftBpIns;
        }
        int incodonIndex = (relativeCodingStartPosIncDNA) % 3;
        int curCodonStart = relativeCodingStartPosIncDNA - incodonIndex;

        if (incodonIndex < 0) {
            incodonIndex += 3;
        }
        int insSeqLen = alt.length() - ref.length();
        StringBuilder info = new StringBuilder("c.");

        if (alt.endsWith("+")) {
            //the format will be like TC/AGC++
            //reverse sequence
            //unknown sequence
            info.append('?');
            info.append(relativeCodingStartPosIncDNA + 1);
            info.append("ins");
            info.append(alt.substring(0, alt.length() - 1));
        } else {
            //the format will be like CT/++CGA
            info.append(ref.charAt(ref.length() - 1));
            info.append(relativeCodingStartPosIncDNA + 1);
            info.append("ins");
            info.append(alt.substring(ref.length() - 1));
        }

        if (mRnaSequence == null) {
            // System.out.println("The RefmRNA " + refID + " has no sequence data so the variant at site "+absPos+ " have no detailed coding change information");
            //Warning. not sure this is opproperate or not
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }


        /*
         if (this.refID.equals("NM_133497") && absPos == 2707921) {
         int sss = 0;
         }
         */
        curCodonStart += codingStartRelativeSiteInSequence;
        //because what we will obtain is the reference DNA so we need go back to the posistions of referce genome to get the correct referecne DNA
        curCodonStart -= shiftBpDel;
        curCodonStart += shiftBpIns;

        if (curCodonStart < 0) {
            //warning: this mixed the synonymous and unmapped varaint 
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }
        String codon;
        curCodonStart += 3;
        if (curCodonStart <= mRnaSequence.length()) {
            codon = mRnaSequence.substring(curCodonStart - 3, curCodonStart).toUpperCase();
        } else {
            //warning: this mixed the synonymous and unmapped varaint 
            return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
        }
        /*
         //ignore this, sometimes ref is not avaible for indels
         if (codon.charAt(incodonIndex) != ref) {
         System.out.println("The reference alleles in the sample data and database are not identical at site " + absPos + " on " + refID + " of " + geneSym);
         //Warning. not sure this is opproperate or not
         return new GeneFeature(GlobalManager.VarFeatureIDMap.get("exonic"), info.toString());
         }
         */
        Character refP = '?';
        if (GlobalManager.codonTable.containsKey(codon)) {
            refP = GlobalManager.codonTable.get(codon);
            if (refP == null) {
                System.err.println("Unknown code for " + codon);
            }
        }

        int codonIndex = (relativeCodingStartPosIncDNA) / 3 + 1;

        info.append(':');
        info.append("p.");
        info.append(refP);
        info.append(codonIndex);
        GeneFeature gf = new GeneFeature();

        if (proteinDomainList != null) {
            for (ProteinDomain pd : proteinDomainList) {
                if (codonIndex >= pd.start && codonIndex <= pd.end) {
                    gf.infor = uniprotID + '|' + pd.toString();
                }
            }
        }

        if (insSeqLen % 3 != 0) {
            info.append("fs");
            gf.id = GlobalManager.VarFeatureIDMap.get("frameshift");
            gf.name = info.toString();
            return gf;
            //  return new GeneFeature(GlobalManager.VarFeatureIDMap.get("frameshift"), info.toString());
        } else {
            boolean hasStopGain = false;
            int startCheckSite = 0;

            info.append("ins");
            StringBuilder altSB = new StringBuilder();
            if (alt.endsWith("+")) {
                //for the revsers stand the start position is the one after the insertion
                if (incodonIndex == 2) {
                    startCheckSite = 1;
                } else if (incodonIndex == 1) {
                    info.append('?');
                    startCheckSite = 2;
                } else if (incodonIndex == 0) {
                    startCheckSite = 0;
                }
                altSB.append(alt.substring(0, alt.length() - ref.length()));
            } else {
                //for the forward stand the start position is the one before the insertion
                if (incodonIndex == 2) {
                    startCheckSite = 1;
                } else if (incodonIndex == 1) {
                    info.append('?');
                    startCheckSite = 2;
                } else if (incodonIndex == 0) {
                    startCheckSite = 0;
                }
                altSB.append(alt.substring(ref.length()));
            }
            for (int i = startCheckSite; i < insSeqLen; i += 3) {
                if (i + 3 > alt.length()) {
                    //Unspecified or unknown amino acid
                    info.append('?');
                    break;
                }

                codon = alt.substring(i, i + 3).toUpperCase();
                if (GlobalManager.codonTable.containsKey(codon)) {
                    char altP = GlobalManager.codonTable.get(codon);
                    if (altP == '*') {
                        hasStopGain = true;
                    }
                    info.append(altP);
                } else {
                    //Unspecified or unknown amino acid
                    info.append('?');
                }
            }
            if (hasStopGain) {
                gf.id = GlobalManager.VarFeatureIDMap.get("stopgain");
                gf.name = info.toString();
                return gf;
                // return new GeneFeature(GlobalManager.VarFeatureIDMap.get("stopgain"), info.toString());
            } else {
                gf.id = GlobalManager.VarFeatureIDMap.get("nonframeshift");
                gf.name = info.toString();
                return gf;
                //  return new GeneFeature(GlobalManager.VarFeatureIDMap.get("nonframeshift"), info.toString());
            }
        }

    }

//we know that the exons are not overlapped and sorted . otherwise it is risk
    private int binarySearch(int pos, int left, int right) {
        if (left > right) {
            return -left - 1;
        }
        int middle = (left + right) / 2;

        if (exons.get(middle).end == pos) {
            return middle;
        } else if (exons.get(middle).end > pos) {
            return binarySearch(pos, left, middle - 1);
        } else {
            return binarySearch(pos, middle + 1, right);
        }
    }
}
