/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.rpclibraries.math;

import java.io.IOException;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class MathLib {

    public static double[][] multiply(double[][] a, double[][] b) throws IOException {
        return MathLibClientStub.multiply(a, b);
    }

    public static double[] sort(double[] array) throws IOException {

        return MathLibClientStub.sort(array);
    }

    public static double max(double[] array) throws IOException {
        return MathLibClientStub.max(array);
    }

    public static double min(double[] array) throws IOException {
        return MathLibClientStub.min(array);
    }


}
