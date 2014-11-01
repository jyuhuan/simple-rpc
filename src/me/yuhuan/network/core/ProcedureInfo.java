/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.core;

/**
 * Created by Yuhuan Jiang on 10/18/14.
 */

/**
 * A class the represents information about a procedure.
 */
public class ProcedureInfo {
    public int programID;
    public int procedureID;
    public int versionID;

    public ProcedureInfo(int programID, int procedureID, int versionID) {
        this.programID = programID;
        this.procedureID = procedureID;
        this.versionID = versionID;
    }

    @Override
    public int hashCode() {
        //return programID * 100 + procedureID * 10 + versionID;
        int hashCode = 17;
        hashCode = hashCode * 23 + programID;
        hashCode = hashCode * 23 + procedureID;
        hashCode = hashCode * 23 + versionID;
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        ProcedureInfo theOther = (ProcedureInfo)other;
        return theOther.programID == programID && theOther.procedureID == procedureID && theOther.versionID == versionID;
    }

}
