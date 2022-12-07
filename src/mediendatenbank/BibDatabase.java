package mediendatenbank;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.hsqldb.Server;
import org.hsqldb.jdbc.jdbcDataSource;

public class BibDatabase {
	
	public enum Type { AUDIO, VIDEO };

	private static BibDatabase instance;
	
	private Server server;
	private jdbcDataSource dataSource;
	private Connection connection;
	
	public static void init() throws BibUserException {
		instance = new BibDatabase();
		instance.connect();
		instance.updateDatabase();
	}

	public static BibDatabase get() {
		return instance;
	}
	
	protected BibDatabase() {}
	
	public String getDbFolder() {
		return "data/";
	}
	
	public String getDbName() {
		return "medien";
	}
	
	public String getDbFile() {
		return getDbFolder() + getDbName() + ".db";
	}
	
	protected final void connect() throws BibUserException {
		this.server = new Server();
		this.server.setDatabaseName(0, getDbName());
		this.server.setDatabasePath(0, getDbFile());
		this.server.start();

		this.dataSource = new jdbcDataSource();
		this.dataSource.setDatabase("jdbc:hsqldb:file:" + getDbFile());
		this.dataSource.setUser("sa");
		this.dataSource.setPassword("");
		try {
			this.connection = this.dataSource.getConnection();
		} catch (SQLException e) {
			throw new BibUserException(e);
		}

		try {
			Statement statement = this.connection.createStatement();
			statement.execute("SET IGNORECASE TRUE;");
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		if (this.connection == null) {
			return;
		}
		try {
			Statement statement = this.connection.createStatement();
			statement.execute("SHUTDOWN");
			statement.close();
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void export(File file) throws BibUserException {
		this.export(file.getAbsolutePath());
	}
	
	public void export(String file) throws BibUserException {
		shutdown();
		
		BibZip zip = new BibZip(getDbFolder());
		zip.saveZipFile(file);
		
		connect();
	}
	
	public boolean updateTitle(Integer id, String title) {
		return this.updateField(id, "title", title);
	}

	public boolean updateMedium(Integer id, String medium) {
		return this.updateField(id, "medium", medium);
	}
	
	public boolean updateYear(Integer id, String year) {
		return this.updateField(id, "year", year);
	}
	
	public boolean updateDetails(Integer id, String details) {
		return this.updateField(id, "details", details);
	}

	public boolean updateField(Integer id, String column, String data) {
		if (id == null || id < 1 || data == null) {
			return false;
		}
		try {
			PreparedStatement statement = this.connection.prepareStatement("UPDATE medien SET " + column + " = ? WHERE id = ?");
			statement.setString(1, data);
			statement.setInt(2, id);
			statement.executeUpdate();
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteEntry(Integer id) {
		try {
			PreparedStatement statement = this.connection.prepareStatement("DELETE FROM medien WHERE id = ?");
			statement.setInt(1, id);
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Integer countEntries() {
		try {
			PreparedStatement statement = this.connection.prepareStatement("SELECT COUNT(*) FROM medien");
			return this.countEntries(statement);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Integer countEntries(Type type) {
		try {
			PreparedStatement statement = this.connection.prepareStatement("SELECT COUNT(*) FROM medien WHERE type = ?");
			statement.setInt(1, this.typeToInt(type));
			return this.countEntries(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Integer countEntries(PreparedStatement statement) throws SQLException {
		ResultSet resultSet = statement.executeQuery();
		statement.close();

		resultSet.next();
		String count = resultSet.getString(1);
		resultSet.close();

		return Integer.valueOf(count);
	}

	public List<BibEntry> readData(Type type) {
		return this.readData(type, null);
	}

	public List<BibEntry> readData(Type type, String query) {
		if (query == null) {
			query = "";
		}
		else {
			query = query.trim();
		}
	
		List<BibEntry> list = new ArrayList();
		try {
			String querySql = "";
			if (!query.isEmpty()) {
				querySql += " AND title LIKE ? || '%'";
			}
			
			PreparedStatement statement = this.connection.prepareStatement(
				"SELECT id, title, medium, year, details, type FROM medien WHERE type = ? " + querySql + " ORDER BY title ASC"
			);
			statement.setInt(1, this.typeToInt(type));
			if (!query.isEmpty()) {
				statement.setString(2,query);
			}
			ResultSet resultSet = statement.executeQuery();
			statement.close();

			while (resultSet.next()) {
				BibEntry be = new BibEntry(
						resultSet.getInt("id"),
						resultSet.getString("title"),
						resultSet.getString("medium"),
						resultSet.getString("year"),
						resultSet.getString("details"),
						resultSet.getInt("type")
				);
				list.add(be);
			}
			resultSet.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String> readMediumValues(Type type) {
		List<String> list = new ArrayList<String>();
		try {
			PreparedStatement statement = this.connection.prepareStatement(
				"SELECT DISTINCT medium FROM medien WHERE type = ? ORDER BY medium ASC"
			);
			statement.setInt(1, this.typeToInt(type));
			ResultSet resultSet = statement.executeQuery();
			statement.close();

			while (resultSet.next()) {
				list.add(resultSet.getString("medium"));
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public String getSetting(String key) {
		try {
			PreparedStatement statement = this.connection.prepareStatement(
				"SELECT value FROM settings WHERE title = ? LIMIT 1"
			);
			statement.setString(1, key);
			ResultSet rs = statement.executeQuery();
			statement.close();
			String value = null;
			if (rs.next()) {
				value = rs.getString("value");
			}
			return value;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Integer getSettingAsInt(String key, Integer defaultValue) {
		String value = this.getSetting(key);
		try {
			return Integer.parseInt(value);
		} catch(Exception e) {
			return defaultValue;
		}
	}

	public boolean updateSetting(String key, String value) {
		try {
			String sql;
			if (getSetting(key) != null) {
				sql = "UPDATE settings SET value = ? WHERE title = ?";
			}
			else {
				sql = "INSERT INTO settings (value, title) VALUES (?,?)";
			}
			PreparedStatement statement = this.connection.prepareStatement(sql);
			statement.setString(1, value);
			statement.setString(2, key);
			statement.executeUpdate();
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public int insertData(String title, String medium, String year, String details, Type type) {
		try {
			PreparedStatement statement = this.connection.prepareStatement(
				"INSERT INTO medien (title, medium, year, details, type) VALUES (?, ?, ?, ?, ?);"
			);
			statement.setString(1, title);
			statement.setString(2, medium);
			statement.setString(3, year);
			statement.setString(4, details);
			statement.setInt(5, this.typeToInt(type));
			statement.executeUpdate();
			statement.close();
			
			Statement statement2 = this.connection.createStatement();
			ResultSet rs = statement2.executeQuery("call identity();");
			statement2.close();
			rs.next();
			int lastid = rs.getInt(1);
			return lastid;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	protected void createSettingsTable() {
		try {
			Statement statement = this.connection.createStatement();
			statement.execute("CREATE TABLE settings (title varchar(128) not null, value varchar(255) not null, CONSTRAINT titleindex UNIQUE (title))");
			statement.close();

			statement = this.connection.createStatement();
			statement.execute("INSERT INTO settings (title, value) VALUES ('font-size', '12');");
			statement.execute("INSERT INTO settings (title, value) VALUES ('db-version', '1');");
			statement.close();
		} catch (SQLException e) {}
	}

	protected void createDataTable() {
		try {
			Statement statement = this.connection.createStatement();
			statement.execute("CREATE TABLE medien (id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), title varchar(255) not null, medium varchar(4) not null, year varchar(4) not null, details varchar(255) not null)");
			statement.close();
		} catch (SQLException e) {}
	}
	
	protected void updateDatabase() {
		Integer dbVersion = this.getSettingAsInt("db-version", 0);
		if (dbVersion < 1) {
			this.createDataTable();
			this.createSettingsTable();
		}
		if (dbVersion < 2) {
			try {
				Statement statement = this.connection.createStatement();
				statement.execute("ALTER TABLE medien ADD type tinyint default '0' not null;");
				statement.close();
				this.updateSetting("db-version", "2");
			} catch (SQLException e) {}
		}
	}
	
	protected Integer typeToInt(Type type) {
		switch(type) {
			case AUDIO:
				return 0;
			case VIDEO: 
				return 1;
			default:
				return null;
		}
	}
}
