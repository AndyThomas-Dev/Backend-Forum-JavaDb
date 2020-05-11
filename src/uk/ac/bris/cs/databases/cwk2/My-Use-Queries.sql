INSERT INTO Forum VALUES (1, 'First Forum'); 
INSERT INTO Topic VALUES (1, 'First Topic', 'vz19513', '03-05-2020 21:30:11');
INSERT INTO Topic VALUES (3, 'Topic In 2nd Forum', 'vz19513', 'Post content. Second Forum.');
INSERT INTO Post VALUES (1, 'First Post', 'vz19513','This is our first post');

INSERT INTO Posts_In_Topic VALUES (1, 1, 1);
INSERT INTO Topics_In_Forum VALUES (1, 1, 1);
INSERT INTO Topics_In_Forum VALUES (1, 2, 1);

ALTER TABLE Post DROP COLUMN title;

INSERT INTO table_name (column1, column2, column3, ...)
VALUES (value1, value2, value3, ...);

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

--- Question 3B Part 2
WITH LikedPosts AS (
	SELECT COUNT(User_Likes_Posts.postid) AS TotalPosts FROM Person
	JOIN  User_Likes_Posts ON User_Likes_Posts.userid =  Person.id 
	WHERE Person.id = 2
	),

LikedTopics AS (
	SELECT COUNT(User_Likes_Topics.topicid) AS TotalTopics FROM Person
	JOIN  User_Likes_Topics ON User_Likes_Topics.userid =  Person.id 
	WHERE Person.id = 2
	),
	
Totals AS (
SELECT TotalPosts FROM LikedPosts
UNION ALL
SELECT TotalTopics FROM LikedTopics
)

SELECT SUM(TotalPosts) AS TotalLikes FROM Totals;

----

INSERT INTO User_Likes_Posts (postid, userid)
VALUES (2, 2);

INSERT INTO User_Likes_Topics (topicid, userid)
VALUES (2, 2);
---

WITH RegionalGreens AS (
	SELECT Ward.id AS WardId, Ward.name AS WardName, Candidate.ward, Party.name AS PartyName, Candidate.party, Candidate.votes, SUM(votes) AS GreenVotes
	FROM Candidate 

	JOIN Ward ON Candidate.ward = Ward.id
	JOIN Party ON Candidate.party = Party.id

	WHERE Party.name = "Green"
	GROUP BY ward
	),

RegionalTotals AS (
	SELECT Ward.id AS WardId, Ward.name AS WardName2, Candidate.ward, Candidate.party, Candidate.votes,
        SUM(votes) AS VotesPerRegion
	FROM Candidate

	JOIN Ward ON Candidate.ward = Ward.id
	GROUP BY ward )

SELECT WardName, GreenVotes*100 / VotesPerRegion AS Percentage FROM Candidate
JOIN RegionalGreens ON Candidate.ward = RegionalGreens.WardId
JOIN RegionalTotals ON Candidate.ward = RegionalTotals.WardId
GROUP BY WardName;
