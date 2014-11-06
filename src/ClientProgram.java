/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

import me.yuhuan.rpclibraries.math.MathLib;
import me.yuhuan.utility.Console;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class ClientProgram {

    private static Random random = new Random();

    private static double[][] createRandomMatrix(int rowSize, int colSize) {
        double[][] matrix = new double[rowSize][colSize];
        for (int r = 0; r < rowSize; r++) {
            for (int c = 0; c < colSize; c++) {
                matrix[r][c] = random.nextDouble();
            }
        }
        return matrix;
    }

    private static double[] createRandomArray(int arrayDimension) {
        double[] array = new double[arrayDimension];
        for (int i = 0; i < arrayDimension; i++) {
            array[i] = random.nextDouble();
        }
        return array;
    }

    private static void printMatrix(double[][] matrix) {
        int rowCount = matrix.length;
        int colCount = matrix[0].length;

        if (rowCount > 10 && colCount > 5) {
            int rowLim = 8;
            int colLim = 5;

            for (int r = 0; r < rowLim; r++) {
                for (int c = 0; c < colLim; c++) {
                    Console.write(matrix[r][c] + "\t\t");
                }
                Console.writeLine("... ");
            }
            for (int c = 0; c < colLim; c++) {
                Console.write(".............." + "\t\t");
            }
            Console.writeLine("");

        }
        else {
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    Console.write(matrix[r][c] + "\t\t");
                }
                Console.writeLine("");
            }
        }

        Console.writeLine("");

    }

    private static void printArray(double[] array) {
        int dim = array.length;
        if (dim > 5) {
            for (int i = 0; i < 5; i++) {
                Console.write(array[i] + "  ");
            }
            Console.writeLine("....");
        }
        else {
            for (int i = 0; i < dim; i++) {
                Console.write(array[i] + "  ");
            }
        }

        Console.writeLine("");

    }

    private static void printDouble(double d) {
        Console.writeLine("" + d + "\n");

    }

    public static void main(String[] args) throws IOException {
        int arrayDimension = 1000000;
        int rowSizeA = 1000;
        int colSizeA = 500;
        int rowSizeB = colSizeA;
        int colSizeB = 1000;

        double[][] matrixA = createRandomMatrix(rowSizeA, colSizeA);
        double[][] matrixB = createRandomMatrix(rowSizeB, colSizeB);
        double[] array = createRandomArray(arrayDimension);

        try {
            double[][] multiplyResult = MathLib.multiply(matrixA, matrixB);
            double[] sortedArray = MathLib.sort(array);
            double max = MathLib.max(array);
            double min = MathLib.min(array);

            printMatrix(multiplyResult);
            printArray(sortedArray);
            printDouble(max);
            printDouble(min);
        }
        catch (Exception e) {
            Console.writeLine(e.getMessage());
        }



        int aaaa = 0;

    }
}
