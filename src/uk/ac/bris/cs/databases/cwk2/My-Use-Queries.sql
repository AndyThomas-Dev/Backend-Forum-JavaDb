INSERT INTO Forum VALUES (1, 'First Forum'); 
INSERT INTO Topic VALUES (1, 'First Topic', 'vz19513', '03-05-2020 21:30:11');
INSERT INTO Topic VALUES (3, 'Topic In 2nd Forum', 'vz19513', 'Post content. Second Forum.');
INSERT INTO Post VALUES (1, 'First Post', 'vz19513','This is our first post');

INSERT INTO Posts_In_Topic VALUES (1, 1, 1);
INSERT INTO Topics_In_Forum VALUES (1, 1, 1);
INSERT INTO Topics_In_Forum VALUES (1, 2, 1);

ALTER TABLE Post DROP COLUMN title;

-- Select topics alongside their forumid
SELECT Topic.title, Topic.id, Topics_In_Forum.forumid AS Forum FROM Topic
JOIN Topics_In_Forum ON Topics_In_Forum.topicid = Topic.id
WHERE Topics_In_Forum.forumid = 1;

--- Select posts for a topic
SELECT Post.title, Post.username, Post.postedAt, Post.text, Posts_In_Topic.topicid FROM Post
JOIN Posts_In_Topic ON Posts_In_Topic.postid = Post.id
WHERE Posts_In_Topic.topicid = 1;

--- Count posts in topic 
SELECT COUNT(*) AS c FROM Posts_In_Topic WHERE topicid = 1;

--- SELECT * FROM Post WHERE username = 'vz19513' AND postedAt = '03-05-2020';

--- Clear everything
DELETE FROM Posts_In_Topic WHERE id > 0;
DELETE FROM Posts WHERE id > 0;
DELETE FROM Topics_In_Forum WHERE id > 0;
DELETE FROM Topic WHERE id > 0;
DELETE FROM Forum WHERE id > 0;
