package investments;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateAssetDB {

	public static void main(String[] args) throws SQLException {
		//go();  // Use this one normally for empty DB
		goDemo(); // use this one for demo data
	}

	public static void go() throws SQLException {
		System.out.println("Running CreateAssetDB");
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			System.out.println("CreateAssetDB already exists.");
			System.out.println("Connected to database!");
			Statement st = con.createStatement();
			st.executeUpdate("DROP TABLE cryptos");
			st.executeUpdate("DROP TABLE stocks");
			st.executeUpdate("CREATE TABLE cryptos (base VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (base))");
			st.executeUpdate("CREATE TABLE stocks (symbol VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (symbol))");
			st.close();
			con.close();
		} catch (Exception e) {
			System.out.println("DB does NOT exist yet.");
			Connection con = DriverManager.getConnection("jdbc:derby:assets; create=true");
			System.out.println("Created new database for assets!");
			Statement st = con.createStatement();
			try {
				st.executeUpdate("CREATE TABLE cryptos (base VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (base))");
				st.executeUpdate("CREATE TABLE stocks (symbol VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (symbol))");
				st.close();
				con.close();
			} catch (Exception f) {
				f.printStackTrace();
				System.out.println("executeUpdate FAILED");
			}
			st.close();
			con.close();
		}
	}

	public static void goDemo() throws SQLException {
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			System.out.println("CreateAssetDB already exists.");
			System.out.println("Connected to database!");
			Statement st = con.createStatement();
			st.executeUpdate("DROP TABLE cryptos");
			st.executeUpdate("DROP TABLE stocks");
			st.executeUpdate("CREATE TABLE cryptos (base VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (base))");
			st.executeUpdate("CREATE TABLE stocks (symbol VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (symbol))");
			st.executeUpdate("INSERT INTO cryptos (base, qty) VALUES ('BTC',1.5), ('SHIB',25000), ('ETH',7)");
			st.executeUpdate("INSERT INTO stocks (symbol, qty) VALUES ('NOW',1), ('CSCO',1)");
			st.close();
			con.close();
		} catch (Exception e) {
			System.out.println("DB does NOT exist yet.");
			Connection con = DriverManager.getConnection("jdbc:derby:assets; create=true");
			System.out.println("Created new database for assets!");
			Statement st = con.createStatement();
			try {
				st.executeUpdate("CREATE TABLE cryptos (base VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (base))");
				st.executeUpdate("CREATE TABLE stocks (symbol VARCHAR(4) NOT NULL, qty DOUBLE NOT NULL, price DOUBLE, total DOUBLE, PRIMARY KEY (symbol))");
				st.executeUpdate("INSERT INTO cryptos (base, qty) VALUES ('BTC',1.5), ('SHIB',25000), ('ETH',7)");
				st.executeUpdate("INSERT INTO stocks (symbol, qty) VALUES ('NOW',1), ('CSCO',1)");
				st.close();
				con.close();
			} catch (Exception f) {
				f.printStackTrace();
				System.out.println("executeUpdate FAILED");
			}
			st.close();
			con.close();
		}
	}
}