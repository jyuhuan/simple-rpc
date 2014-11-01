/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.collections;

/**
 * Created by Yuhuan Jiang on 10/18/14.
 */
public class Tuple2<T1, T2> {
    public final T1 e1;
    public final T2 e2;
    public Tuple2(T1 e1, T2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}