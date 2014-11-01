/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.exceptions;

import java.io.IOException;

/**
 * Created by Yuhuan Jiang on 11/1/14.
 */
public class ProcedureExecutionException extends IOException {
    public ProcedureExecutionException(String message) {
        super(message);
    }
}
