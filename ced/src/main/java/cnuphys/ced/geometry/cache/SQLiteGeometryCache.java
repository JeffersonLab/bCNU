package cnuphys.ced.geometry.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** SQLite storage for versioned, detector-specific primitive geometry payloads. */
public final class SQLiteGeometryCache implements AutoCloseable {

	static final int SCHEMA_VERSION = 1;
	private static final String APPLICATION_VERSION = "application_version";
	private static final String GEOMETRY_VARIATION = "geometry_variation";
	private static final String SCHEMA = "schema_version";

	private final Path path;
	private final String applicationVersion;
	private final String geometryVariation;
	private Connection connection;

	public SQLiteGeometryCache(Path path, String applicationVersion, String geometryVariation) {
		this.path = path;
		this.applicationVersion = applicationVersion;
		this.geometryVariation = geometryVariation;
	}

	/** Open the database, recreating it when application or geometry metadata changed. */
	public void open() throws IOException, SQLException {
		Path parent = path.toAbsolutePath().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}

		connection = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
		createSchema();
		if (!metadataMatches()) {
			close();
			Files.deleteIfExists(path);
			connection = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
			createSchema();
			writeMetadata();
		}
	}

	public boolean read(IGeometryCache geometry) {
		if (!geometry.supportsCache()) {
			return false;
		}

		String sql = "SELECT format_version, payload FROM geometry_payload WHERE detector_name = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, geometry.getName());
			try (ResultSet results = statement.executeQuery()) {
				if (!results.next() || results.getInt(1) != geometry.getCacheFormatVersion()) {
					return false;
				}
				byte[] payload = results.getBytes(2);
				try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload))) {
					geometry.readGeometry(input);
					return input.available() == 0;
				}
			}
		} catch (SQLException | IOException | RuntimeException e) {
			System.err.println("Unable to read cached " + geometry.getName() + ": " + e.getMessage());
			return false;
		}
	}

	public boolean write(IGeometryCache geometry) {
		if (!geometry.supportsCache()) {
			return false;
		}

		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				DataOutputStream output = new DataOutputStream(bytes)) {
			geometry.writeGeometry(output);
			output.flush();
			String sql = "INSERT OR REPLACE INTO geometry_payload"
					+ " (detector_name, format_version, payload) VALUES (?, ?, ?)";
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				statement.setString(1, geometry.getName());
				statement.setInt(2, geometry.getCacheFormatVersion());
				statement.setBytes(3, bytes.toByteArray());
				return statement.executeUpdate() == 1;
			}
		} catch (SQLException | IOException | RuntimeException e) {
			System.err.println("Unable to cache " + geometry.getName() + ": " + e.getMessage());
			return false;
		}
	}

	private void createSchema() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS metadata"
					+ " (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS geometry_payload"
					+ " (detector_name TEXT PRIMARY KEY, format_version INTEGER NOT NULL, payload BLOB NOT NULL)");
		}
	}

	private boolean metadataMatches() throws SQLException {
		return Integer.toString(SCHEMA_VERSION).equals(readMetadata(SCHEMA))
				&& applicationVersion.equals(readMetadata(APPLICATION_VERSION))
				&& geometryVariation.equals(readMetadata(GEOMETRY_VARIATION));
	}

	private String readMetadata(String key) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT value FROM metadata WHERE key = ?")) {
			statement.setString(1, key);
			try (ResultSet results = statement.executeQuery()) {
				return results.next() ? results.getString(1) : null;
			}
		}

	}

	private void writeMetadata() throws SQLException {
		writeMetadata(SCHEMA, Integer.toString(SCHEMA_VERSION));
		writeMetadata(APPLICATION_VERSION, applicationVersion);
		writeMetadata(GEOMETRY_VARIATION, geometryVariation);
	}

	private void writeMetadata(String key, String value) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT OR REPLACE INTO metadata (key, value) VALUES (?, ?)")) {
			statement.setString(1, key);
			statement.setString(2, value);
			statement.executeUpdate();
		}
	}

	@Override
	public void close() throws SQLException {
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}
}
