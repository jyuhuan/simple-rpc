/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.utility;

import me.yuhuan.network.core.ProcedureInfo;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class Converters {

    /**
     * Converts a string of method name to its corresponding {@link me.yuhuan.network.core.ProcedureInfo ProcedureInfo} object.
     * @param methodName A string of the method name.
     * @return A {@link me.yuhuan.network.core.ProcedureInfo ProcedureInfo} object that represents the method.
     */
    public static ProcedureInfo methodNameToProcedureInfo(String methodName) {
        if (methodName.equals("multiply")) {
            return new ProcedureInfo(10001, 1, 1);
        }
        if (methodName.equals("sort")) {
            return new ProcedureInfo(10001, 2, 1);
        }
        if (methodName.equals("max")) {
            return new ProcedureInfo(10001, 3, 1);
        }
        if (methodName.equals("min")) {
            return new ProcedureInfo(10001, 4, 1);
        }
        return null;
    }

    //public static String


}
