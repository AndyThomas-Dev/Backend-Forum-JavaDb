DROP TABLE IF EXISTS Topics_In_Forum;
DROP TABLE IF EXISTS Forum;
DROP TABLE IF EXISTS Posts_In_Topic;
DROP TABLE IF EXISTS Topic;
DROP TABLE IF EXISTS Post;
DROP TABLE IF EXISTS Person;

CREATE TABLE Person (
id INTEGER PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(100) NOT NULL,
username VARCHAR(10) NOT NULL UNIQUE,
stuId VARCHAR(10) NULL
);

CREATE TABLE Post (
id INTEGER PRIMARY KEY AUTO_INCREMENT,
title VARCHAR(100) NOT NULL,
username VARCHAR(100) NOT NULL,
text VARCHAR(800) NOT NULL
);

CREATE TABLE Topic (
id INTEGER PRIMARY KEY AUTO_INCREMENT,
title VARCHAR(100) NOT NULL,
username VARCHAR(100) NOT NULL,
text VARCHAR(800) NOT NULL
);

CREATE TABLE Posts_In_Topic ( 
id INTEGER PRIMARY KEY AUTO_INCREMENT,
topicid INTEGER, 
postid INTEGER, 
FOREIGN KEY (topicid) REFERENCES Topic(id), 
FOREIGN KEY (postid) REFERENCES Post(id)
);

CREATE TABLE Forum (
id INTEGER PRIMARY KEY AUTO_INCREMENT,
title VARCHAR(100) NOT NULL
);

CREATE TABLE Topics_In_Forum ( 
id INTEGER PRIMARY KEY AUTO_INCREMENT, 
topicid INTEGER, 
forumid INTEGER, 
FOREIGN KEY (topicid) REFERENCES Topic(id), 
FOREIGN KEY (forumid) REFERENCES Forum(id)
);


INSERT INTO Forum VALUES (1, 'First Forum'); 
INSERT INTO Topic VALUES (1, 'First Topic', 'vz19513', 'This is the first topic');
INSERT INTO Topic VALUES (3, 'Topic In 2nd Forum', 'vz19513', 'Post content. Second Forum.');
INSERT INTO Post VALUES (1, 'First Post', 'vz19513','This is our first post');

INSERT INTO Posts_In_Topic VALUES (1, 1, 1);
INSERT INTO Topics_In_Forum VALUES (1, 1, 1);
INSERT INTO Topics_In_Forum VALUES (1, 2, 1);

ALTER TABLE Forum ALTER COLUMN topics DEFAULT 'Temp';

-- Select topics alongside their forumid
SELECT Topic.title, Topic.id, Topics_In_Forum.forumid AS Forum FROM Topic
JOIN Topics_In_Forum ON Topics_In_Forum.topicid = Topic.id
WHERE Topics_In_Forum.forumid = 1;

--- Select posts for a topic
SELECT Post.title, Post.username, Post.postedAt, Posts_In_Topic.topicid FROM Post
JOIN Posts_In_Topic ON Posts_In_Topic.postid = Post.id\n
WHERE Posts_In_Topic.topicid = 1;

