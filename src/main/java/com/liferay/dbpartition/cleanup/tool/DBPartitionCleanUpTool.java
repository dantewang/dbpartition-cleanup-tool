package com.liferay.dbpartition.cleanup.tool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Properties;

/*
 * @author Dante Wang
 */
public class DBPartitionCleanUpTool {

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			for (String arg : args) {
				if (arg.equals("--help") || arg.equals("-h")) {
					_printHelpInfo();

					System.exit(0);
				}
			}
		}

		DatabaseCleaner databaseCleaner = new DatabaseCleaner(
			_loadProperties(args));

		databaseCleaner.execute();
	}

	private static void _printHelpInfo() {
		System.out.println(
			"*** DB Partition Cleanup Tool ***\n" +
				"Args:\n" +
				"\t[propertyPath]: absolute path to property file, if not " +
					"specified, ./database.properties will be read\n" +
				"\t--help or -h: print this help info\n" +
				"Properties:\n" +
				"\t" + DatabaseCleaner.PROPERTY_KEY_SERVER_ADDRESS + "\n" +
				"\t" + DatabaseCleaner.PROPERTY_KEY_SCHEMA_NAME + "\n" +
				"\t" + DatabaseCleaner.PROPERTY_KEY_USER_NAME + "\n" +
				"\t" + DatabaseCleaner.PROPERTY_KEY_PASSWORD + "\n"
		);
	}

	private static Properties _loadProperties(String[] args)
		throws IOException {

		File propertyFile = null;

		if (args.length == 0) {
			System.out.println(
				"*** Loading properties from database.properties in current " +
					"directory ***");

			propertyFile = new File("database.properties");
		}
		else {
			System.out.println(
				"*** Loading properties from " + args[0] + "***");

			propertyFile = new File(args[0]);
		}

		Properties properties = new Properties();

		try (FileReader fileReader = new FileReader(propertyFile)) {
			properties.load(fileReader);
		}

		return properties;
	}

}