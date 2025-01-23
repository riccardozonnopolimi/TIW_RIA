package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User getUserLog(String email, String password) throws SQLException {
		String performedAction = " finding a user by email and password ";
		PreparedStatement statement = null;
		ResultSet result = null;
		User user = null;

		try {

			String query = "SELECT * FROM user WHERE email = ? AND password = ?";
			statement = connection.prepareStatement(query);
			statement.setString(1, email);
			statement.setString(2, password);
			result = statement.executeQuery();
			if (result.next()) {
				user = new User(result.getString("username"), result.getString("email"), result.getString("password"));
				user.setId_user(result.getInt("id_user"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {

			try {
				result.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return user;
	}

	public void createUser(String username, String email, String password) throws SQLException {
		String performedAction = " creating a user ";
		PreparedStatement statement = null;
		String query = "INSERT INTO user (username, email, password) VALUES (?, ?, ?)";

		try {
			connection.setAutoCommit(false);
			statement = connection.prepareStatement(query);
			statement.setString(1, username);
			statement.setString(2, email);
			statement.setString(3, password);
			statement.executeUpdate();
			connection.commit();

		} catch (SQLException e) {
			connection.rollback();
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {

			try {

				statement.close();

			} catch (Exception e) {

				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		connection.setAutoCommit(true);

	}

	public User findUserByEmail(String email) throws SQLException {
		String performedAction = " finding a user by email ";
		PreparedStatement statement = null;
		ResultSet result = null;
		User user = null;
		String query = "SELECT * FROM user WHERE email = ?";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, email);
			result = statement.executeQuery();
			if (result.next()) {
				user = new User(result.getString("username"), result.getString("email"), result.getString("password"));
				user.setId_user(result.getInt("id_user"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return user;
	}

	public User findUserByUsername(String username) throws SQLException {
		String performedAction = " finding a user by username ";
		PreparedStatement statement = null;
		ResultSet result = null;
		User user = null;
		String query = "SELECT * FROM user WHERE username = ?";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, username);
			result = statement.executeQuery();
			if (result.next()) {
				user = new User(result.getString("username"), result.getString("email"), result.getString("password"));
				user.setId_user(result.getInt("id_user"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return user;
	}
}
