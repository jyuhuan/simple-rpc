/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

import me.yuhuan.network.exceptions.ProcedureExecutionException;
import me.yuhuan.rpclibraries.math.MathLib;
import me.yuhuan.rpclibraries.math.MathLibImplementations;
import me.yuhuan.utility.Console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class ClientProgram {
    public static void main(String[] args) throws IOException {

/*
        double[][] a = new double[][] {
                new double[] { 2.0, 4.1, 6.3, 0.4, 1.3 },
                new double[] { 1.2, 3.4, 5.6, 7.8, 9.9 },
                new double[] { 2.1, 3.2, 4.3, 5.4, 6.5 }
        };

        double[][] b = new double[][] {
                new double[] { 2.0, 4.1, 6.3 },
                new double[] { 1.2, 3.4, 5.6 },
                new double[] { 1.3, 3.5, 5.7 },
                new double[] { 1.4, 3.6, 5.8 },
                new double[] { 2.1, 3.2, 4.3 },
                new double[] { 2.1, 3.2, 4.3 }
        };
*/

        /*Random random = new Random();
        int rowSize = 1000;
        //int rowSize = Integer.parseInt(args[0]);
        int colSize = 1000;
        //int rowSize = Integer.parseInt(args[0]);

        double[][] matrixA = new double[rowSize][colSize];
        for (int r = 0; r < rowSize; r++) {
            for (int c = 0; c < colSize; c++) {
                matrixA[r][c] = random.nextDouble();
            }
        }

        double[][] matrixB = new double[rowSize][colSize];
        for (int r = 0; r < rowSize; r++) {
            for (int c = 0; c < colSize; c++) {
                matrixB[r][c] = random.nextDouble();
            }
        }

        try {
            double[][] multiplyResult = MathLib.multiply(matrixA, matrixB);
        }
        catch (Exception e) {
            Console.writeLine(e.getMessage());
        }*/


        /*ArrayList<String> recorder = new ArrayList<String>();
        int currentRunId = 0;
        for (int i = 0; i < 100; i++) {
            Console.writeLine("Running experiment " + currentRunId++ + " of the multiplication of " + rowSize + "x" + colSize);
            try {
                long startTime = System.nanoTime();
                double[][] c = MathLib.multiply(matrixA, matrixB);
                //double[][] d = MathLibImplementations.multiply(matrixA, matrixB);
                long estimatedTime = System.nanoTime() - startTime;
                recorder.add("" + estimatedTime);
            } catch (Exception e) {
                Console.writeLine("ERROR: " + e.getMessage());
            }
        }

        // print out recorder
        for (String s : recorder) {
            Console.writeLine(s);
        }*/


        //double[] array = new double[] { 7.1, 4.4, 7.9, 2.3, 9.5, 1.8, 2.3, 0.5, 0.2, 6.9, 3.3, 4.6 };
        int dim = 1000000;
        double[] array = new double[dim];
        Random random = new Random();
        for (int r = 0; r < dim; r++) {
            array[r] = random.nextDouble();
        }

        try {
            double[] sortedArray = MathLib.sort(array);
            double max = MathLib.max(array);
            double min = MathLib.min(array);
            int aa = 0;
        }
        catch (Exception e) {
            Console.writeLine(e.getMessage());
        }



        int aaaa = 0;

        // ...
    }
}
