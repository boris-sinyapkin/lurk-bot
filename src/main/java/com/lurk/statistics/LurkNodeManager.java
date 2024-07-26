package com.lurk.statistics;

import java.util.Set;
import com.lurk.statistics.database.LurkDatabaseHelper;

/*
 * Node manager is in charge of all available nodes.
 */
public class LurkNodeManager {

    private final LurkDatabaseHelper databaseHelper;

    public LurkNodeManager(LurkDatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public Set<LurkNode> getVisibleNodes(long chatId) {
        return databaseHelper.getNodesFromDatabase(chatId);
    }
}
