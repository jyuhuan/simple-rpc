/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.exceptions;

import java.io.IOException;

/**
 * Created by Yuhuan Jiang on 10/29/14.
 */
public class ReliableUdpTransmissionFailedException extends IOException {
    public ReliableUdpTransmissionFailedException(String message) {
        super(message);
    }
}
