/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kggseq.entity;

import java.util.Comparator;

/**
 *
 * @author MX Li
 */
public class VariantPositionComparator implements Comparator<Variant> {

    @Override
    //assume arg1 is the database variant and the refStartPosition is just one base before the insertion or deletion
    public int compare(Variant arg0, Variant arg1) {
        //the following code will meet the exception: java.lang.IllegalArgumentException: Comparison method violates its general contract!
        if (arg0.isIndel && arg1.isIndel) {
            int size0 = arg0.getAltAlleles().length;
            int size1 = arg1.getAltAlleles().length;
            for (int i = 0; i < size0 && i < size1; i++) {
                //sometimes the data are very complex; at the same position it may have both indels and snvs
                if (arg0.getAlleleStartPostion(i) == arg1.getAlleleStartPostion(i)) {
                    return (0);
                }
            }

            //Integer.compare only avaible since java 1.7
            // return Integer.compare(arg0.refStartPosition, arg1.refStartPosition);
            if (arg0.refStartPosition < arg1.refStartPosition) {
                return -1;
            } else if (arg0.refStartPosition == arg1.refStartPosition) {
                return 0;
            } else {
                return 1;
            }

        } else if (!arg0.isIndel && !arg1.isIndel) {
            //return Integer.compare(arg0.refStartPosition, arg1.refStartPosition);
            if (arg0.refStartPosition < arg1.refStartPosition) {
                return -1;
            } else if (arg0.refStartPosition == arg1.refStartPosition) {
                return 0;
            } else {
                return 1;
            }
        } else if (arg0.isIndel) {
            if (arg0.getAltAlleles().length > 0) {
                //return Integer.compare(arg0.getAlleleStartPostion(0), arg1.refStartPosition);
                if (arg0.getAlleleStartPostion(0) < arg1.refStartPosition) {
                    return -1;
                } else if (arg0.getAlleleStartPostion(0) == arg1.refStartPosition) {
                    return 0;
                } else {
                    return 1;
                }
            }
        } else {
            if (arg1.getAltAlleles().length > 0) {
                //return Integer.compare(arg0.refStartPosition, arg1.getAlleleStartPostion(0));
                if (arg0.refStartPosition < arg1.getAlleleStartPostion(0)) {
                    return -1;
                } else if (arg0.refStartPosition == arg1.getAlleleStartPostion(0)) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }

        //return Integer.compare(arg0.refStartPosition, arg1.refStartPosition);
        if (arg0.refStartPosition < arg1.refStartPosition) {
            return -1;
        } else if (arg0.refStartPosition == arg1.refStartPosition) {
            return 0;
        } else {
            return 1;
        }
    }
}
