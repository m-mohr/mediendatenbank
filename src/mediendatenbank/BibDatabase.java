package mediendatenbank;

import java.io.File;
import java.sql.Connection;
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
			Statement statement = this.connection.createStatement();
			statement.execute("UPDATE medien SET " + column + " = '" + data + "' WHERE id = '" + id + "' LIMIT 1");
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteEntry(Integer id) {
		try {
			Statement statement = this.connection.createStatement();
			statement.execute("DELETE FROM medien WHERE id = '" + id + "' LIMIT 1");
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Integer countEntries() {
		return this.countEntries("SELECT COUNT(*) FROM medien");
	}
	
	public Integer countEntries(Type type) {
		return this.countEntries("SELECT COUNT(*) FROM medien WHERE type = '" + this.typeToInt(type) + "'");
	}
	
	private Integer countEntries(String query) {
		try {
			Statement statement = this.connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			statement.close();

			resultSet.next();
			String count = resultSet.getString(1);
			resultSet.close();

			return new Integer(count);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<BibEntry> readData(Type type) {
		return this.readData(type, null);
	}

	public List<BibEntry> readData(Type type, String query) {
		List<BibEntry> list = new ArrayList();
		try {
			String querySql = "";
			if (query != null) {
				query = query.trim();
				if (!query.isEmpty()) {
					querySql = " AND title LIKE '" + query + "%' ";
				}
			}
			
			Statement statement = this.connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT id, title, medium, year, details, type FROM medien WHERE type = '" + this.typeToInt(type) + "' " + querySql + " ORDER BY title ASC");
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
			Statement statement = this.connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT DISTINCT medium FROM medien WHERE type = '" + this.typeToInt(type) + "' ORDER BY medium ASC");
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
			Statement statement = this.connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT value FROM settings WHERE title = '" + key + "' LIMIT 1");
			String value = null;
			if (rs.next()) {
				value = rs.getString("value");
			}
			statement.close();
			return value;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Integer getSettingAsInt(String key, Integer defaultValue) {
		String value = this.getSetting(key);
		try {
			return Integer.parseInt(value);
		} catch(Exception e) {
			return defaultValue;
		}
	}

	public void updateSetting(String key, String value) {
		try {
			String sql = "";
			if (getSetting(key) != null) {
				sql = "UPDATE settings SET value = '" + value + "' WHERE title = '" + key + "' LIMIT 1";
			}
			else {
				sql = "INSERT INTO settings (title, value) VALUES ('" + key + "', '" + value + "')";
			}
			Statement statement = this.connection.createStatement();
			statement.execute(sql);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int insertData(String title, String medium, String year, String details, Type type) {
		try {
			Statement statement = this.connection.createStatement();
			ResultSet rs = statement.executeQuery("INSERT INTO medien (title, medium, year, details, type) VALUES ('" + title + "', '" + medium + "', '" + year + "', '" + details + "', '" + this.typeToInt(type) + "'); call identity();");
			rs.next();
			int lastid = rs.getInt(1);
			statement.close();
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
