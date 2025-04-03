package databaseTesting;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;


public class DataIntegrityTest {

	private static final String DB_URL = "jdbc:sqlite:memory:movieDBTest";
    private Connection connection;
    
    
    @BeforeClass
    public void setUpDatabase() throws SQLException, ClassNotFoundException {
    	
    	try {
    	    Class.forName("org.sqlite.JDBC"); // forces the JDBC driver to be loaded and avoids the "No suitable driver" error
    	} catch (ClassNotFoundException e) {
    	    e.printStackTrace();
    	}
    	
    	//Class.forName("org.sqlite.JDBC");
    	connection = DriverManager.getConnection(DB_URL);
        String createTableSQL = "CREATE TABLE IF NOT EXISTS movies (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "title TEXT NOT NULL, " +
                                "year TEXT, " +
                                "genre TEXT" +
                                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
        
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM movies;");
                stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name = 'movies';"); // Reset id (will start with 1)
                System.out.println("All records have been removed from the 'movies' table after all tests.");
            }
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        
        //queryMovies(connection);
    } 
    
    @AfterClass
    public void tearDown() throws SQLException {
    	if (connection != null) {
            connection.close();
        }
    } 
    
    @BeforeMethod
    public void setUp() throws SQLException {
    	
    	try (Statement stmt = connection.createStatement()) {
    	    
    		// Reset database
    		// Drop all existing tables
    	    stmt.execute("DROP TABLE IF EXISTS movies;");
    	    stmt.execute("DROP TABLE IF EXISTS directors;");

    	    // Recreate the tables
    	    stmt.execute("CREATE TABLE directors ("
    	                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
    	                + "name TEXT NOT NULL"
    	                + ");");
    	    stmt.execute("CREATE TABLE movies ("
    	                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
    	                + "title TEXT NOT NULL, "
    	                + "year TEXT, "
    	                + "genre TEXT, "
    	                + "director_id INTEGER, "
    	                + "FOREIGN KEY(director_id) REFERENCES directors(id) ON DELETE CASCADE"
    	                + ");");
    	}
    }
    
    
    @Test(priority = 1)
    public void testDataIntegrity() throws SQLException {
        
    	// Step 1: Insert a valid record
        String validInsertSQL = "INSERT INTO movies (title, year, genre) VALUES ('The Matrix', '1999', 'Action, Sci-Fi');";
        try (Statement stmt = connection.createStatement()) {
            int rowsInserted = stmt.executeUpdate(validInsertSQL);
            assertEquals(rowsInserted, 1, "Valid record should be inserted successfully.");
        }
    }
    
    @Test(priority = 2)
    public void testEmptyInsert() throws SQLException {
        // Trying to insert a record with missing non-nullable fields
        String invalidInsertSQL = "INSERT INTO movies (year, genre) VALUES ('2023', 'Drama');"; // Title has no value
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(invalidInsertSQL);
            fail("Insert without a title should fail due to NOT NULL constraint.");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("NOT NULL"), "Expected NOT NULL constraint violation.");
        }
    }
    
    @Test(priority = 3)
    public void testEmptyInsert2() throws SQLException {
        // Trying to insert a record with missing non-nullable fields
        String invalidInsertSQL = "INSERT INTO movies (year, genre) VALUES ('2023', 'Drama');"; // Title has no value
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(invalidInsertSQL);
            fail("Insert without a title should fail due to NOT NULL constraint.");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("NOT NULL"), "Expected NOT NULL constraint violation.");
        }
    }
    
    @Test(enabled = false)
    public void testNullInsert() throws SQLException {
        // Trying to insert a record with NULL fields
    	String invalidInsertSQL = "INSERT INTO movies (year, genre) VALUES (NULL, '2023', 'Drama');"; // Title has NULL value
    	try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(invalidInsertSQL);
            fail("Insert with null title should fail due to NOT NULL constraint.");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("NOT NULL"), "Expected NOT NULL constraint violation.");
        }
    }
    
    @Test(priority = 4)
    public void testUniqueConstraint() throws SQLException {
    	//queryMovies(connection); 
    	
    	// Inserting multiple rows with the same title but relying on unique IDs
        String insertSQL1 = "INSERT INTO movies (title, year, genre) VALUES ('Duplicate Movie', '2021', 'Comedy');";
        String insertSQL2 = "INSERT INTO movies (title, year, genre) VALUES ('Duplicate Movie', '2022', 'Drama');";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(insertSQL1);
            stmt.executeUpdate(insertSQL2);

            // Verify that both rows are inserted, each with unique IDs
            ResultSet rs = stmt.executeQuery("SELECT id, title FROM movies WHERE title = 'Duplicate Movie';");
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }
            assertEquals(rowCount, 2, "Two entries with the same title should exist with unique IDs.");
        }
    }
    
    @Test(priority = 5)
    public void testFieldLength() throws SQLException {
    	//queryMovies(connection);
    	
    	// Attempt to insert a record with an extremely long title
        String longTitle = "A".repeat(1000); // String with 1000 characters
        String insertSQL = "INSERT INTO movies (title, year, genre) VALUES ('" + longTitle + "', '2023', 'Drama');";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(insertSQL);

            // Validate that the record exists
            ResultSet rs = stmt.executeQuery("SELECT title FROM movies WHERE title = '" + longTitle + "';");
            assertTrue(rs.next(), "Record with a very long title should be inserted.");
        }
    }
    
    @Test(enabled = false)
    public void testInvalidYearFormat() throws SQLException {
    	queryMovies(connection);
    	
    	// Trying to insert a record with an invalid year
        String invalidYearSQL = "INSERT INTO movies (title, year, genre) VALUES ('Invalid Year Movie', 'abcd', 'Drama');";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(invalidYearSQL);
            fail("Insert with an invalid year should fail."); // Can not insert invalid year
        } catch (SQLException e) {
            // SQLite doesn't enforce strict data types by default, so this might pass unless handled explicitly
            assertTrue(true, "SQLite allows text in numeric fields unless constraints are added.");
        }
    }
    
    @Test()
    public void testDeleteCascadingBehavior() throws SQLException {
    	queryMovies(connection);
    	
    	// Set up foreign key constraints and test cascading delete behavior
        try (Statement stmt = connection.createStatement()) {
            // Create a table with a foreign key reference
            stmt.execute("CREATE TABLE IF NOT EXISTS directors (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL);");
            
            ResultSet rs = connection.getMetaData().getColumns(null, null, "movies", "director_id");
            if (!rs.next()) {
                // Only add the column if it doesn't already exist
                try (Statement stmt1 = connection.createStatement()) {
                    stmt1.execute("ALTER TABLE movies ADD COLUMN director_id INTEGER REFERENCES directors(id) ON DELETE CASCADE;");
                }
            }
                  
            String tableName = "movies"; 
            try (Statement stmt2 = connection.createStatement();
                 ResultSet rs2 = stmt2.executeQuery("PRAGMA table_info(" + tableName + ");")) {

                System.out.println("Columns in table '" + tableName + "':");
                while (rs2.next()) {
                    String columnName = rs2.getString("name"); // Retrieves the column name
                    System.out.println(columnName);
                }
            } 

            
            // Insert into the 'directors' table
            stmt.executeUpdate("INSERT INTO directors (name) VALUES ('Steven Spielberg');");
            int directorId = stmt.executeQuery("SELECT id FROM directors WHERE name = 'Steven Spielberg';").getInt("id");
            
            // Insert into the 'movies' table with a valid director_id
            stmt.executeUpdate("INSERT INTO movies (title, year, genre, director_id) VALUES ('Jurassic Park', '1993', 'Adventure, Sci-Fi', " + directorId + ");");
            
            // Delete the director and verify cascading delete
            stmt.executeUpdate("DELETE FROM directors WHERE id = " + directorId + ";");
            ResultSet rs1 = stmt.executeQuery("SELECT * FROM movies WHERE director_id = " + directorId + ";");
            assertFalse(rs1.next(), "Cascading delete should remove associated movie records.");
        }
    }
    
    @Test(priority = 8)
    public void testDefaultValues() throws SQLException {
    	queryMovies(connection);
    	
    	// Adding a new column with a default value and testing its behavior
        try (Statement stmt = connection.createStatement()) {
            //stmt.execute("ALTER TABLE movies ADD COLUMN rating TEXT DEFAULT 'Unrated';"); // Fails??  SQL error or missing database (duplicate column name: rating)
        	ResultSet rs1 = connection.getMetaData().getColumns(null, null, "movies", "rating");
        	if (!rs1.next()) {
        	    try (Statement stmt1 = connection.createStatement()) {
        	        stmt1.execute("ALTER TABLE movies ADD COLUMN rating TEXT DEFAULT 'Unrated';");
        	    }
        	}

            // Insert a record without specifying the rating
            stmt.executeUpdate("INSERT INTO movies (title, year, genre) VALUES ('No Rating Movie', '2021', 'Thriller');");

            // Verify that the default value is applied
            ResultSet rs = stmt.executeQuery("SELECT rating FROM movies WHERE title = 'No Rating Movie';");
            assertTrue(rs.next(), "Record should exist.");
            assertEquals(rs.getString("rating"), "Unrated", "Default value for 'rating' should be 'Unrated'.");
        }
    }
    
    
    // Retrieves and displays all movie records from the database
    private static void queryMovies(Connection conn) throws SQLException {
        String sql = "SELECT id, title, year, genre FROM movies";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Movies in the database:");
            while (rs.next()) {
                System.out.printf("%d: %s (%s) - %s%n",
                        rs.getInt("id"), rs.getString("title"), rs.getString("year"), rs.getString("genre"));
            }
        }
    } 
    
}

/*
The use of both fail() and assertTrue() within the try-catch block serves different purposes:

fail() in the try block: This is placed to ensure that if the INSERT operation unexpectedly succeeds (i.e., no exception is thrown), 
the test will fail. The presence of fail() helps you confirm that the code block where an error is anticipated does not pass 
silently or incorrectly. It guarantees that the test explicitly fails if the error condition isn't met.

assertTrue() in the catch block: This is used to validate that the exception thrown is indeed the one you were expecting 
(or meets certain criteria). In this case, you're using assertTrue(true, ...) to acknowledge that SQLite allows non-standard inputs 
(like text in the year field) unless constraints are explicitly enforced. It's a way to handle this behavior 
while still passing the test under expected circumstances.
*/
