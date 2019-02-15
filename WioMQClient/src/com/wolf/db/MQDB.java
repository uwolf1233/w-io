package com.wolf.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface MQDB {

	public void setIsDB(boolean b);
	
	public boolean getIsDB();
	
	public Connection getConnection() throws SQLException;
	
	
}
