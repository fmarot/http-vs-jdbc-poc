package com.teamtter.httpdemo;

import java.sql.SQLException;

public class H2Starter {
	
	public static void main(String[] args) throws SQLException {
		
		args = new String[] {"-url", "jdbc:h2:C:\\\\Program Files\\\\Olea Sphere\\\\Data\\\\DB;LOCK_TIMEOUT=10000;TRACE_LEVEL_FILE=1;USER=sa;PASSWORD=@lympiqueDeMarseille_" };
		org.h2.tools.Console.main(args);
	}
}
