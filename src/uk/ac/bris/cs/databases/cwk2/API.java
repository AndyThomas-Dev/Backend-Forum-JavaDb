package uk.ac.bris.cs.databases.cwk2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.ForumSummaryView;
import uk.ac.bris.cs.databases.api.ForumView;
import uk.ac.bris.cs.databases.api.Result;
import uk.ac.bris.cs.databases.api.PersonView;
import uk.ac.bris.cs.databases.api.SimplePostView;
import uk.ac.bris.cs.databases.api.SimpleTopicSummaryView;
import uk.ac.bris.cs.databases.api.TopicView;

// My imports
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author csxdb
 */
public class API implements APIProvider {

    private final Connection c;

    public API(Connection c) {
        this.c = c;
    }

    /* predefined methods */

    @Override
    // Done
    public Result<Map<String, String>> getUsers() {
        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT name, username FROM Person");

            Map<String, String> data = new HashMap<>();
            while (r.next()) {
                data.put(r.getString("username"), r.getString("name"));
            }

            return Result.success(data);
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override
    // Done
    public Result addNewPerson(String name, String username, String studentId) {
        if (studentId != null && studentId.equals("")) {
            return Result.failure("StudentId can be null, but cannot be the empty string.");
        }
        if (name == null || name.equals("")) {
            return Result.failure("Name cannot be empty.");
        }
        if (username == null || username.equals("")) {
            return Result.failure("Username cannot be empty.");
        }

        try (PreparedStatement p = c.prepareStatement(
            "SELECT count(1) AS c FROM Person WHERE username = ?"
        )) {
            p.setString(1, username);
            ResultSet r = p.executeQuery();

            if (r.next() && r.getInt("c") > 0) {
                return Result.failure("A user called " + username + " already exists.");
            }
        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        try (PreparedStatement p = c.prepareStatement(
            "INSERT INTO Person (name, username, stuId) VALUES (?, ?, ?)"
        )) {
            p.setString(1, name);
            p.setString(2, username);
            p.setString(3, studentId);
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
    }

    /* level 1 */
    @Override
    // Done
    public Result<PersonView> getPersonView(String username) {

        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT * FROM Person WHERE username = '" + username + "'");

            List<PersonView> data =  new ArrayList<PersonView>();

            while (r.next())
            {
                data.add(new PersonView(r.getString("name"), r.getString("username"),
                        r.getString("stuId")));
            }

            return Result.success(data.get(0));
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override
    // Done
    public Result<List<ForumSummaryView>> getForums() {

        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT id, title FROM Forum");

            List<ForumSummaryView> data =  new ArrayList<ForumSummaryView>();

            while (r.next()) {
                data.add(new ForumSummaryView(r.getInt("id"), r.getString("title")));
            }

            return Result.success(data);
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override

    // Find topic (based on id) and count total posts within it.
    // DONE - Not sure where this is used?
    public Result<Integer> countPostsInTopic(int topicId) {

        List<Integer> data =  new ArrayList<Integer>();

        try (Statement s = c.createStatement()) {

            ResultSet r = s.executeQuery("SELECT COUNT(*) AS c FROM Posts_In_Topic WHERE topicid = " + topicId);

            while (r.next()) {
                data.add(r.getInt("c"));
            }

            return Result.success(data.get(0));

        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override
    // DONE
    public Result<TopicView> getTopic(int topicId) {

        List<SimplePostView> posts = new ArrayList<SimplePostView>();

        try (Statement s = c.createStatement()) {

            ResultSet r = s.executeQuery("SELECT Post.username, Post.postedAt, Post.text, Posts_In_Topic.topicid FROM Post \n" +
                    "JOIN Posts_In_Topic ON Posts_In_Topic.postid = Post.id\n" +
                    "WHERE Posts_In_Topic.topicid = " + topicId);

            int counter = 1;

            while (r.next()) {
                // Add name rather than username
                posts.add(new SimplePostView(counter, r.getString("username"), r.getString("text"),
                        r.getString("postedAt") ));
                counter++;
            }

        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }

        try (Statement s = c.createStatement()) {
            // Determines link to create new post
            ResultSet r = s.executeQuery("SELECT id, title FROM Topic WHERE id = " + topicId);

            List<TopicView> data =  new ArrayList<TopicView>();

            while (r.next()) {
                data.add(new TopicView(r.getInt("id"), r.getString("title"), posts));
            }

            return Result.success(data.get(0));
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    /* level 2 */
    // DONE
    @Override
    public Result createForum(String title) {

        if (title == null || title.equals("")) {
            return Result.failure("Name cannot be empty.");
        }

        try (PreparedStatement p = c.prepareStatement(
                "SELECT count(1) AS c FROM Forum WHERE title = ?"
        )) {
            p.setString(1, title);
            ResultSet r = p.executeQuery();

            if (r.next() && r.getInt("c") > 0) {
                return Result.failure("A forum called " + title + " already exists.");
            }
        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }

        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Forum (title) VALUES (?)"
        )) {
            p.setString(1, title);
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
    }

    // DONE
    @Override
    public Result<ForumView> getForum(int id) {

        // First part - gets topics for forum
        List<SimpleTopicSummaryView> topicsInForum =  new ArrayList<SimpleTopicSummaryView>();

        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT Topic.title, Topic.id, Topics_In_Forum.forumid AS Forum FROM Topic \n" +
                    "JOIN Topics_In_Forum ON Topics_In_Forum.topicid = Topic.id\n" +
                    "WHERE Topics_In_Forum.forumid = " + id);

            // int topicId, int forumId, String title
            while (r.next()) {
                topicsInForum.add(new SimpleTopicSummaryView(r.getInt("Topic.id"), id, r.getString("Topic.title")));
            }

        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }

        // Get Id and Title for Forum
        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT id, title FROM Forum WHERE id = " + id );

            List<ForumView> data =  new ArrayList<ForumView>();

            while (r.next()) {
                data.add(new ForumView(r.getInt("id"), r.getString("title"), topicsInForum));
            }

            return Result.success(data.get(0));
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override
    // Add current date
    public Result createPost(int topicId, String username, String text) {

        String date;
        List<Integer> data =  new ArrayList<Integer>();

        if (text == null || text.equals("")) {
            return Result.failure("Text cannot be empty.");
        }

        // Creating entry in Post table
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Post (username, text, postedAt) VALUES (?, ?, ?)"
        )) {
            date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

            p.setString(1, username);
            p.setString(2, text);
            p.setString(3, date);
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        // Getting the id of the new post.
        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT id FROM Post WHERE username = '" + username + "' AND postedAt = '" + date + "'");

            while (r.next()) {
                data.add(r.getInt("id"));
            }

        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }

        // Inserting into associative table
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Posts_In_Topic (postid, topicid) VALUES (?, ?)"
        )) {

            p.setInt(1, data.get(0));
            p.setInt(2, topicId);
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
}


    /* level 3 */

    // Need to create first post as well
    @Override
    public Result createTopic(int forumId, String username, String title, String text) {

        if (title == null || title.equals("")) {
            return Result.failure("Title cannot be empty.");
        }

        String date;
        // New topic is inserted into table
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Topic (title, username, postedAT) VALUES (?, ?, ?)"
        )) {

            date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

            p.setString(1, title);
            p.setString(2, username);
            p.setString(3, date);
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        // Get the Topic Id (for later use)
        List<Integer> topicId =  new ArrayList<Integer>();

        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT id FROM Topic WHERE username = '" + username + "' AND postedAt = '" + date + "'");

            while (r.next()) {
                topicId.add(r.getInt("id"));
            }

        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }

        // Add to associative table (Topics in Forum)
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Topics_In_Forum (topicid, forumid) VALUES (?, ?)"
        )) {

            p.setInt(1, topicId.get(0));
            p.setInt(2, forumId);
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        // Create first post in topic
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Post (username, text, postedAt) VALUES (?, ?, ?)"
        )) {

            p.setString(1, username);
            p.setString(2, text);
            p.setString(3, date);
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        List<Integer> data =  new ArrayList<Integer>();

        // Getting the id of the new post.
        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT id FROM Post WHERE username = '" + username + "' AND postedAt = '" + date + "'");

            while (r.next()) {
                data.add(r.getInt("id"));
            }

        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }

        // Inserting into next associative table
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Posts_In_Topic (postid, topicid) VALUES (?, ?)"
        )) {

            p.setInt(1, data.get(0));
            p.setInt(2, topicId.get(0));
            p.executeUpdate();

            c.commit();
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }

        return Result.success();
    }

}
