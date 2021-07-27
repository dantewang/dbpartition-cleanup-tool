package com.liferay.dbpartition.cleanup.tool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
 * @author Dante Wang
 */
public class DatabaseCleaner {

	public static final String PROPERTY_KEY_SCHEMA_NAME = "schemaName";
	public static final String PROPERTY_KEY_USER_NAME = "username";
	public static final String PROPERTY_KEY_PASSWORD = "password";
	public static final String PROPERTY_KEY_SERVER_ADDRESS = "serverAddress";

	public DatabaseCleaner(Properties properties) throws Exception {
		_databaseServerAddress = Objects.requireNonNull(
			properties.getProperty(PROPERTY_KEY_SERVER_ADDRESS));
		_databaseSchemaName = Objects.requireNonNull(
			properties.getProperty(PROPERTY_KEY_SCHEMA_NAME));
		_databaseUsername = Objects.requireNonNull(
			properties.getProperty(PROPERTY_KEY_USER_NAME));
		_databasePassword = Objects.requireNonNull(
			properties.getProperty(PROPERTY_KEY_PASSWORD));

		Class.forName(JDBC_DRIVER).newInstance();
	}

	public void execute() throws SQLException {
		List<Long> companyIds = new ArrayList<>();

		try (Connection connection = _getConnection()) {
			System.out.println(
				"*** Selecting company ids from default schema... ***");

			try (Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(
					"select companyId from Company where companyId >" +
						"(select min(companyId) from Company)")) {

				while (resultSet.next()) {
					long companyId = resultSet.getLong("companyId");

					companyIds.add(companyId);

					System.out.print(companyId);

					if (!resultSet.isLast()) {
						System.out.print(", ");
					}
				}

				System.out.println();
			}
		}

		List<Future<Void>> futures = new ArrayList<>();

		for (Long companyId : companyIds) {
			futures.add(
				_executorService.submit(
					() -> {
						System.out.println(
							"*** Deleting schema for company " + companyId +
								" ***");

						try (Connection connection = _getConnection()) {
							try (PreparedStatement preparedStatement =
									connection.prepareStatement(
										"drop schema IF EXISTS " +
											"lpartition_" + companyId)) {

								preparedStatement.executeUpdate();
							}
						}

						return null;
					}
				)
			);
		}

		for (Future<Void> future : futures) {
			try {
				future.get();
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	private Connection _getConnection() throws SQLException {
		return DriverManager.getConnection(
			JDBC_URL_PART1 + _databaseServerAddress + "/" +
				_databaseSchemaName + JDBC_URL_PART2,
			_databaseUsername, _databasePassword);
	}

	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String JDBC_URL_PART1 = "jdbc:mysql://";
	private static final String JDBC_URL_PART2 =
		"?characterEncoding=UTF-8&dontTrackOpenResources=true&" +
			"holdResultsOpenOverStatementClose=true&serverTimezone=GMT" +
				"&useFastDateParsing=false&useUnicode=true";

	private final ExecutorService _executorService =
		Executors.newWorkStealingPool();
	private final String _databasePassword;
	private final String _databaseSchemaName;
	private final String _databaseServerAddress;
	private final String _databaseUsername;

}