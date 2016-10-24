/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.entity;

import cern.colt.map.OpenIntIntHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author MX Li
 */
public class Chromosome {

    /**
     * @pdOid 2d5ae4e3-9217-4284-9d2e-2015123075fe
     */
    private String name;
    int id;
    /**
     * @pdRoleInfo migr=no name=Gene assc=association8 mult=0..*
     * type=Aggregation
     */
    /**
     * @pdRoleInfo migr=no name=SNP assc=association9 mult=0..* type=Aggregation
     */
    public List<Variant> variantList;
    public List<mRNA> mRNAList;
    public List<Gene> geneList;
    private boolean hasNotOrderVariantList = true;
    private boolean hasNotOrdermRNAList = true;
    private final static VariantPositionComparator varPosComp = new VariantPositionComparator();
    private final static SeqSegmentComparator mRNAPosComp = new SeqSegmentComparator();
    private OpenIntIntHashMap posIndexMap = new OpenIntIntHashMap();
    //always start from zeor
    public int genotypeSize = 0;

    public OpenIntIntHashMap getPosIndexMap() {
        return posIndexMap;
    }

    public boolean isHasNotOrderVariantList() {
        return hasNotOrderVariantList;
    }

    public void setHasNotOrderVariantList(boolean hasNotOrderVariantList) {
        this.hasNotOrderVariantList = hasNotOrderVariantList;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Chromosome(String name, int id) {
        this.name = name;
        this.id = id;
        mRNAList = new ArrayList<mRNA>();
        variantList = new ArrayList<Variant>();
        geneList = new ArrayList< Gene>();
    }

    public void addmRNA(mRNA gene) {
        mRNAList.add(gene);
        hasNotOrdermRNAList = true;
    }

    public void buildVariantIndexMap() {
        if (hasNotOrderVariantList) {
            List<Variant> variantList1 = new ArrayList<Variant>();
            Collections.sort(variantList1, varPosComp);
            hasNotOrderVariantList = false;
        }
        int len = variantList.size();
        posIndexMap.clear();
        for (int i = 0; i < len; i++) {
            posIndexMap.put(variantList.get(i).refStartPosition, i + 1);
        }
    }

    public void sortmRNAList() {
        if (hasNotOrdermRNAList) {
            Collections.sort(mRNAList, mRNAPosComp);
            hasNotOrdermRNAList = false;
        }
    }

    public int lookupVariantByMap(Variant tmpVar) throws Exception {
        if (hasNotOrderVariantList) {
            buildVariantIndexMap();
        }
        return posIndexMap.get(tmpVar.refStartPosition) - 1;
    }

    public int lookupVariantByMap(int positin) throws Exception {
        if (hasNotOrderVariantList) {
            buildVariantIndexMap();
        }
        return posIndexMap.get(positin) - 1;
    }

    public int[] lookupVariantIndexes(int postion) throws Exception {
        if (variantList.isEmpty()) {
            return null;
        }
        int index = lookupVariantByMap(postion);
        if (index < 0) {
            return null;
        }
        int startIndex = index - 1;
        while (startIndex >= 0 && variantList.get(startIndex).refStartPosition == postion) {
            startIndex--;
        }
        startIndex++;
        int endIndex = index + 1;
        while (endIndex < variantList.size() && variantList.get(endIndex).refStartPosition == postion) {
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

    public int lookupVariantByList(Variant tmpVar) throws Exception {
        if (hasNotOrderVariantList) {
            buildVariantIndexMap();
        }
        /*
         if (variantList.isEmpty()) {
         return -1;
         } else if (variantList.size() == 1) {
         if (variantList.get(0).refStartPosition == tmpVar.refStartPosition) {
         return 0;
         }
         return -1;
         }
         */
        return Collections.binarySearch(variantList, tmpVar, varPosComp);
    }

    public int lookupmRNA(Gene tmpVar) throws Exception {
        if (hasNotOrdermRNAList) {
            Collections.sort(mRNAList, mRNAPosComp);
            hasNotOrdermRNAList = false;
            // throw new Exception("Chromosome: " + name + " is not sorted and cannot loopup variant by binary search alogrithm!");
        }
        return Collections.binarySearch(mRNAList, tmpVar, mRNAPosComp);
    }
}
