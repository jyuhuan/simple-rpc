/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rpc;

import me.yuhuan.collections.Tuple2;
import me.yuhuan.network.core.ProcedureInfo;
import me.yuhuan.network.core.ServerInfo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Yuhuan Jiang on 10/18/14.
 */

/**
 * A port mapper table. This class is capable of maintaining a list of servers that can execute the same procedure.
 * It deals with the case where multiple threads are writing.
 */
public class PortMap {

    //

    /**
     * A hash table  (can deal with concurrent writes). Each pair in this table looks like:
     * [ procedure1, [ 2, server1, server2, server3, ... ] ]
     * This means that each procedure can be supported by multiple servers.
     * This helps balance workload.
     */
    ConcurrentHashMap<ProcedureInfo, Tuple2<Integer, ArrayList<ServerInfo>>> _mappings;

    public PortMap() {
        _mappings = new ConcurrentHashMap<ProcedureInfo, Tuple2<Integer, ArrayList<ServerInfo>>>();
    }

    /**
     * Register a server that supports the given procedure.
     * @param procedureInfo The procedure to support.
     * @param serverInfo The server that supports the procedure specified by {@code procedureInfo}.
     */
    public void register(ProcedureInfo procedureInfo, ServerInfo serverInfo) {
        if (_mappings.containsKey(procedureInfo)) {
            _mappings.get(procedureInfo).e2.add(serverInfo);
        }
        else {
            ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
            servers.add(serverInfo);
            _mappings.put(procedureInfo, new Tuple2<Integer, ArrayList<ServerInfo> >(1, servers));
        }
    }

    /**
     * Checks whether a procedure is in the table.
     * @param procedureInfo The procedure to look up.
     * @return {@code true}, if the table contains the procedure. <br/> {@code false}, otherwise.
     */
    public boolean containsProcedure(ProcedureInfo procedureInfo) {
        return _mappings.containsKey(procedureInfo);
    }

    /*public void withdraw(ProcedureInfo procedureInfo) {
       _mappings.remove(procedureInfo);
    }*/

    /**
     * Get a server that supports the specified procedure.
     * This method automatically selects from one of the servers that support the procedure,
     * in a round-Robin fashion.
     * @param procedureInfo The procedure to look up.
     * @return A server that supports the procedure.
     */
    public ServerInfo getServerByProcedure(ProcedureInfo procedureInfo) {
        int lastServerUsed = _mappings.get(procedureInfo).e1;
        ArrayList<ServerInfo> servers = _mappings.get(procedureInfo).e2;
        lastServerUsed = (lastServerUsed + 1) % servers.size();
        return servers.get(lastServerUsed);
    }

    /**
     * Remove a procedure and the servers that support it.
     * @param procedureInfo The procedure to remove.
     */
    public void removeProcedure(ProcedureInfo procedureInfo) {
        _mappings.remove(procedureInfo);
    }

}
