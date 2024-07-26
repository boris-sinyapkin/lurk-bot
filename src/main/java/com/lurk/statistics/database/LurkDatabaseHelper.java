package com.lurk.statistics.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lurk.statistics.LurkNode;

public class LurkDatabaseHelper {

    private static final Logger log = LoggerFactory.getLogger(LurkDatabaseHelper.class);

    private final LurkDatabaseConnector connector;

    public LurkDatabaseHelper(String jdbcUrl, String username, String password) {
        connector = new LurkDatabaseConnector(jdbcUrl, username, password);
    }

    public Set<LurkNode> getNodesFromDatabase(long chatId) {
        try (Connection connection = connector.establishConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT Node.NodeIp, Node.NodePort FROM Node
                    INNER JOIN NodeAccessControl
                    ON NodeAccessControl.NodeId = Node.NodeId AND 
                       NodeAccessControl.UserTelegramChatId = ?;
            """);
            statement.setLong(1, chatId);

            Set<LurkNode> nodes = new HashSet<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String nodeIp = resultSet.getString("NodeIp");
                short nodePort = resultSet.getShort("NodePort");
                nodes.add(new LurkNode(nodeIp, nodePort));
            }

            return nodes;
        } catch (SQLException e) {
            log.error("Error occured while retrieving nodes from database", e);
            return null;
        }
    }
}
