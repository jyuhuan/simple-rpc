/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

import me.yuhuan.network.exceptions.ProcedureExecutionException;
import me.yuhuan.rpclibraries.math.MathLib;
import me.yuhuan.utility.Console;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class ClientProgram {
    public static void main(String[] args) throws IOException {
        // ...

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

        Random random = new Random();
        int rowSize = 800;
        int colSize = 800;
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
            double[][] c = MathLib.multiply(matrixA, matrixB);
        }
        catch (ProcedureExecutionException e) {
            Console.writeLine(e.getMessage());
        }

        /*
        double[] array = new double[] { 7.1, 4.4 };
        try {
            double[] sortedArray = MathLib.sort(array);
        }
        catch (ProcedureExecutionException e) {
            Console.writeLine(e.getMessage());
        }

        try {
            double max = MathLib.max(array);
        }
        catch (ProcedureExecutionException e) {
            Console.writeLine(e.getMessage());
        }

        try {
            double min = MathLib.min(array);
        }
        catch (ProcedureExecutionException e) {
            Console.writeLine(e.getMessage());
        }
*/


        int aaaa = 0;

        // ...
    }
}
