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
    public Result<PersonView> getPersonView(String username) {
        throw new UnsupportedOperationException("Not supported yet. 7");
    }

    @Override
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
    public Result<Integer> countPostsInTopic(int topicId) {
        throw new UnsupportedOperationException("Not supported yet. 1");
    }

    @Override
    public Result<TopicView> getTopic(int topicId) {
        throw new UnsupportedOperationException("Not supported yet. 2");
    }

    /* level 2 */

    @Override
    public Result createForum(String title) {
        throw new UnsupportedOperationException("Not supported yet. 3");
    }

    @Override
    public Result<ForumView> getForum(int id) {
        throw new UnsupportedOperationException("Not supported yet. 4");
    }

    @Override
    public Result createPost(int topicId, String username, String text) {
        throw new UnsupportedOperationException("Not supported yet. 5");
    }


    /* level 3 */

    @Override
    public Result createTopic(int forumId, String username, String title, String text) {
        throw new UnsupportedOperationException("Not supported yet. 6");
    }

}
