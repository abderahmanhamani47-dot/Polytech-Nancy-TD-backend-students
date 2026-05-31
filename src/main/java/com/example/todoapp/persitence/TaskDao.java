package com.example.todoapp.persitence;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.dto.POSTdto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for {@link Task} entities.
 * Handles all JDBC interactions with the SQLite database.
 *
 * @author StudentMapper
 * @version 1.0
 */
public class TaskDao {

    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);

    // ✅ JDBC:sqlite corrigé en jdbc:sqlite (minuscules)
    private static final String DATABASE_URL = "jdbc:sqlite:task_database.db";

    /**
     * Constructs the DAO and initialises the database table.
     */
    public TaskDao() {
        try {
            createTableIfNotExists();
            clearTable();
            initializeTable();
        } catch (SQLException e) {
            log.error("Error during DAO initialisation: {}", e.getMessage());
        }
    }

    /**
     * Creates the Tasks table if it does not already exist.
     *
     * @throws SQLException if the query fails.
     */
    public void createTableIfNotExists() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Tasks (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    title       VARCHAR(50)  NOT NULL,
                    description VARCHAR(255),
                    done        BOOLEAN      NOT NULL DEFAULT 0
                );
                """;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
            log.info("Table 'Tasks' ready");
        }
    }

    /**
     * Clears all rows from the Tasks table and resets the auto-increment counter.
     *
     * @throws SQLException if the query fails.
     */
    public void clearTable() throws SQLException {
        // ✅ 2 requêtes séparées — SQLite n'accepte pas le multi-statement
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            connection.prepareStatement("DELETE FROM Tasks;").execute();
            connection.prepareStatement("DELETE FROM sqlite_sequence WHERE name = 'Tasks';").execute();
            log.info("Table 'Tasks' cleared");
        }
    }

    /**
     * Inserts sample data into the Tasks table.
     */
    public void initializeTable() {
        save(new POSTdto("Réviser DS de maths", "Séries numériques et probabilités."));
        save(new POSTdto("Valider mon PIVE", "PIVE Club Poker."));
        save(new POSTdto("Choisir mon parcours de 4A", "SIR ou SIA ?"));
        log.info("Table 'Tasks' initialised with sample data");
    }

    /**
     * Persists a new task from the given DTO.
     *
     * @param taskDTO the task data to save.
     * @return the created {@link Task} with its generated id.
     */
    public Task save(POSTdto taskDTO) {
        String sql = """
                INSERT INTO Tasks (title, description, done)
                VALUES (?, ?, 0);
                """;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, taskDTO.title());
            statement.setString(2, taskDTO.description());
            statement.executeUpdate();

            // ✅ On utilise les clés générées au lieu d'un SELECT après insertion
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    log.info("Task saved with id={}", id);
                    return new Task(id, taskDTO.title(), taskDTO.description(), false);
                }
            }

        } catch (SQLException e) {
            log.error("Error saving task: {}", e.getMessage());
        }

        throw new RuntimeException("Task save failed, no generated id");
    }

    /**
     * Retrieves a task by its id.
     *
     * @param id the task identifier.
     * @return an {@link Optional} containing the task, or empty if not found.
     */
    public Optional<Task> findById(int id) {
        String sql = "SELECT * FROM Tasks WHERE id = ?;";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildTaskModel(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Error finding task by id={}: {}", id, e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Retrieves all tasks, optionally filtering to done tasks only.
     *
     * @param todoOnly if {@code true}, returns only completed tasks.
     * @return collection of matching tasks.
     */
    public Collection<Task> getTasksList(boolean todoOnly) {
        // ✅ todoOnly=true retourne les tâches TERMINÉES (done=1), logique corrigée
        String sql = todoOnly
                ? "SELECT * FROM Tasks WHERE done = 1;"
                : "SELECT * FROM Tasks;";

        Collection<Task> tasksList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                tasksList.add(buildTaskModel(rs));
            }

        } catch (SQLException e) {
            log.error("Error retrieving tasks list: {}", e.getMessage());
        }

        return tasksList;
    }

    /**
     * Deletes a task by its id.
     *
     * @param id the task identifier.
     */
    public void deleteTaskById(int id) {
        // ✅ DELETE * invalide en SQL — corrigé en DELETE FROM
        String sql = "DELETE FROM Tasks WHERE id = ?;";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            int affected = statement.executeUpdate();
            log.info("deleteTaskById({}) → {} row(s) affected", id, affected);

        } catch (SQLException e) {
            log.error("Error deleting task id={}: {}", id, e.getMessage());
        }
    }

    /**
     * Updates a task by its id.
     *
     * @param id          the task identifier.
     * @param title       new title.
     * @param description new description.
     * @param done        new done status.
     */
    public void updateTaskById(int id, String title, String description, boolean done) {
        String sql = """
                UPDATE Tasks
                SET title = ?, description = ?, done = ?
                WHERE id = ?;
                """;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // ✅ Index corrigés (étaient dupliqués : setInt(1) deux fois)
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setBoolean(3, done);
            statement.setInt(4, id);
            int affected = statement.executeUpdate();
            log.info("updateTaskById({}) → {} row(s) affected", id, affected);

        } catch (SQLException e) {
            log.error("Error updating task id={}: {}", id, e.getMessage());
        }
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Task}.
     *
     * @param rs the result set positioned on the row to map.
     * @return the corresponding {@link Task}.
     * @throws SQLException if a column cannot be read.
     */
    private Task buildTaskModel(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("done")
        );
    }
}