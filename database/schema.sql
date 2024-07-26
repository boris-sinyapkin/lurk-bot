USE lurkdb;

-- list of known nodes
CREATE TABLE Node (
  NodeId BIGINT PRIMARY KEY,
  NodeIp CHAR(15),
  NodePort SMALLINT NOT NULL
);

-- manage access to nodes by Telegram chat id
CREATE TABLE NodeAccessControl (
  UserTelegramChatId BIGINT NOT NULL,
  NodeId BIGINT REFERENCES Node(NodeId),
  PRIMARY KEY (UserTelegramChatId, NodeId)
);