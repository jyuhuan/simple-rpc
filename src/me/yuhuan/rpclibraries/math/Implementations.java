/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.rpclibraries.math;

import me.yuhuan.network.exceptions.ProcedureExecutionException;

import java.util.Arrays;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class Implementations {
    public static double[][] multiply(double[][] a, double[][] b) throws ProcedureExecutionException {
        int rowCountA = a.length;
        int colCountA = a[0].length;
        int rowCountB = b.length;

        if (colCountA != rowCountB) throw new ProcedureExecutionException("Matrix dimension mismatch!");

        int colCountB = b[0].length;
        double[][] c = new double[rowCountA][colCountB];
        for (int i = 0; i < rowCountA; i++) {
            for (int j = 0; j < colCountB; j++) {
                for (int k = 0; k < colCountA; k++) {
                    c[i][j] = c[i][j] + a[i][k] * b[k][j];
                }
            }
        }
        return c;
    }

    public static double[] sort(double[] array) throws ProcedureExecutionException {
        if (array.length == 0 || array == null) throw new ProcedureExecutionException("Null or empty array");
        double[] copy = array.clone();
        Arrays.sort(copy);
        return copy;
    }

    public static double max(double[] array) throws ProcedureExecutionException {
        if (array.length == 0 || array == null) throw new ProcedureExecutionException("Null or empty array");
        double max = array[0];
        for (int i = 0; i < array.length; i++) {
            double cur = array[i];
            if (cur > max) max = cur;
        }
        return max;
    }

    public static double min(double[] array) throws ProcedureExecutionException {
        if (array.length == 0 || array == null) throw new ProcedureExecutionException("Null or empty array");
        double min = array[0];
        for (int i = 0; i < array.length; i++) {
            double cur = array[i];
            if (cur < min) min = cur;
        }
        return min;
    }
}
