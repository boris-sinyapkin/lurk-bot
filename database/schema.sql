USE lurkdb;

CREATE TABLE Node (
  NodeId INT PRIMARY KEY,
  NodeIp CHAR(15),
  NodePort INT NOT NULL
);

CREATE TABLE User (
  UserId INT PRIMARY KEY,
  UserTelegramChatId INT NOT NULL,
  UserTelegramNickname CHAR(32) NOT NULL
);

CREATE TABLE NodeAccessControl (
  UserId INT REFERENCES User(UserId),
  NodeId INT REFERENCES Node(NodeId),
  PRIMARY KEY (UserId, NodeId)
);