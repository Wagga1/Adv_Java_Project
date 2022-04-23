package combsmanor.assetservice;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import investments.CreateAssetDB;
import investments.Crypto;
import investments.InvalidDataException;
import investments.Stock;

@RestController
public class AssetController {

	private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(10)).build();

	@CrossOrigin(origins = "*")
	@GetMapping("/addstock")
	public String addStock(@RequestParam("name") String n, @RequestParam("qty") double q) throws InvalidDataException, SQLException {
		boolean status = false;
		double qty;
		String name = n;
		name = name.toUpperCase();
		if (name == "" || name == null || q < 0.00000000000001 || name.length() < 3 || name.length() > 4) {
			return "Please verify the details you submit.";
		}
		double newQty = q;
		boolean found = false;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			CreateAssetDB.go();
			System.out.println("DB created");
		} finally {
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}

		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			final String SELECT_QUERY = "SELECT * FROM stocks WHERE symbol = '" + name + "'";
			st = con.createStatement();
			rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				qty = rs.getDouble("qty");
				newQty = qty + newQty;
				found = true;
			}
			if (found) {
				final String UPDATE_QUERY = "UPDATE stocks SET qty = " + newQty + " WHERE symbol = '" + name + "'";
				st.executeUpdate(UPDATE_QUERY);
				status = true;
			} else {
				final String INSERT_QUERY = "INSERT INTO stocks (symbol, qty) VALUES ('" + name + "', " + newQty + ")";
				st.executeUpdate(INSERT_QUERY);
				status = true;
			}
			st.close();
			con.close();
			System.out.println("You now have " + newQty + " of the stock " + name);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("addStock FAILED");

		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				st.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}
		return "You now have " + newQty + " shares of " + name + ".\n";
	}

	@CrossOrigin(origins = "*")
	@GetMapping("/removestock")
	public String removeStock(@RequestParam("name") String n, @RequestParam("qty") double q)
			throws InvalidDataException, SQLException {
		double qty = 0;
		String name = n;
		name = name.toUpperCase();
		if (name == "" || name == null || q < 0.00000000000001 || name.length() < 3 || name.length() > 4) {
			return "Please verify the details you submit.";
		}
		double removeQty = q;
		double newQty = 0;
		boolean found = false;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			CreateAssetDB.go();
			System.out.println("DB created");
		} finally {
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}
		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			final String SELECT_QUERY = "SELECT * FROM stocks WHERE symbol = '" + name + "'";
			st = con.createStatement();
			rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				qty = rs.getDouble("qty");
				newQty = qty - removeQty;
				found = true;
			}
			if (found) {
				String UPDATE_QUERY = "";
				if (newQty > 0) {
					UPDATE_QUERY = "UPDATE stocks SET qty = " + newQty + " WHERE symbol = '" + name + "'";
					st.executeUpdate(UPDATE_QUERY);
					System.out.println("You now have " + newQty + " of the stock " + name);
				} else if (newQty == 0) {
					UPDATE_QUERY = "DELETE FROM stocks WHERE symbol = '" + name + "'";
					st.executeUpdate(UPDATE_QUERY);
					System.out.println("You now have no more " + name);
				} else {
					System.out.println("Your portfolio contains " + qty + " shares of " + name
							+ ".  Please select a quantity less than or equal to " + qty + ".");
					st.close();
					con.close();
					return "Your portfolio contains " + qty + " shares of " + name
							+ ".  Please select a quantity less than or equal to that.";
				}
			} else {
				System.out.println("Please verify that you entered a stock that you already have. " + name + " is not currently in your inventory.");
				return "Please verify that you entered a stock that you already have. " + name
						+ " is not currently in your inventory.";
			}
			st.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("removeStock FAILED");

		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				st.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}
		return "You now have " + newQty + " shares of " + name + ".\n";
	}

	@CrossOrigin(origins = "*")
	@GetMapping("/addcrypto")
	public String addCrypto(@RequestParam("name") String n, @RequestParam("qty") double q) throws InvalidDataException, SQLException {
		double qty;
		String name = n;
		name = name.toUpperCase();
		if (name == "" || name == null || q < 0.00000000000001 || name.length() < 3 || name.length() > 4) {
			return "Please verify the details you submit.";
		}
		double newQty = q;
		boolean found = false;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			CreateAssetDB.go();
			System.out.println("DB created");
		} finally {
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}

		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			final String SELECT_QUERY = "SELECT * FROM cryptos WHERE base = '" + name + "'";
			st = con.createStatement();
			rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				qty = rs.getDouble("qty");
				newQty = qty + newQty;
				found = true;
			}
			if (found) {
				final String UPDATE_QUERY = "UPDATE cryptos SET qty = " + newQty + " WHERE base = '" + name + "'";
				st.executeUpdate(UPDATE_QUERY);
			} else {
				final String INSERT_QUERY = "INSERT INTO cryptos (base, qty) VALUES ('" + name + "', " + newQty + ")";
				st.executeUpdate(INSERT_QUERY);
			}
			st.close();
			con.close();
			System.out.println("You now have " + newQty + " of the crypto " + name);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("addCrypto FAILED");

		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				st.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}

		return "You now have " + newQty + " of " + name + ".\n";
	}

	@CrossOrigin(origins = "*")
	@GetMapping("/removecrypto")
	public String removeCrypto(@RequestParam("name") String n, @RequestParam("qty") double q)
			throws InvalidDataException, SQLException {
		double qty = 0;
		String name = n;
		name = name.toUpperCase();
		if (name == "" || name == null || name.length() < 3 || name.length() > 4) {
			return "Please verify the details you submit.";
		}
		double removeQty = q;
		double newQty = 0;
		boolean found = false;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			CreateAssetDB.go();
			System.out.println("DB created");
		} finally {
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}
		try {
			con = DriverManager.getConnection("jdbc:derby:assets;");
			final String SELECT_QUERY = "SELECT * FROM cryptos WHERE base = '" + name + "'";
			st = con.createStatement();
			rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				qty = rs.getDouble("qty");
				newQty = qty - removeQty;
				found = true;
			}
			if (found) {
				String UPDATE_QUERY = "";
				if (newQty > 0) {
					UPDATE_QUERY = "UPDATE cryptos SET qty = " + newQty + " WHERE base = '" + name + "'";
					st.executeUpdate(UPDATE_QUERY);
					System.out.println("You now have " + newQty + " of the crypto " + name);
				} else if (newQty == 0) {
					UPDATE_QUERY = "DELETE FROM cryptos WHERE base = '" + name + "'";
					st.executeUpdate(UPDATE_QUERY);
					System.out.println("You now have no more " + name);
				} else {
					System.out.println("Your portfolio contains " + qty + " shares of " + name
							+ ".  Please select a quantity less than or equal to " + qty + ".");
					st.close();
					con.close();
					return "Your portfolio contains " + qty + " units of " + name
							+ ".  Please select a quantity less than or equal to that.";
				}
			} else {
				return "Please verify that you entered a cryptocurrency that you already have. " + name
						+ " is not currently in your inventory.";
			}
			st.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("removeCrypto FAILED");
			return "Please verify the data you entered.";
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				st.close();
			} catch (Exception e) {
				/* Ignored */ }
			try {
				con.close();
			} catch (Exception e) {
				/* Ignored */ }
		}
		return "You now have " + newQty + " units of " + name + ".\n";
	}

	@CrossOrigin(origins = "*")
	@GetMapping("/portfolio")
	public String portfolio() throws InvalidDataException, IOException {
		System.out.println("Running portfolio");
		Crypto cryptos[] = null;
		Crypto cryptos2[] = null;
		Stock stocks[] = null;
		Stock stocks2[] = null;
		boolean db = false;
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
			db = true;
		} catch (Exception e) {
			System.out.println("No DB exists, hence you have no assets.");
		}

		if (db) {
			cryptos = cryptoParser();  // Get initial data for cryptos
			if (cryptos != null) {
				for (int m = 0; m < cryptos.length; m++) {
					updateCryptoValue(cryptos[m].getName(), cryptos[m].getQty()); //update price and total for each crypto
				}
				cryptos2 = cryptoParser(); // get updated data for cryptos
				for (int m = 0; m < cryptos2.length; m++) {
				}
			}
			
			stocks = stockParser();  // Get initial data for stocks
			
			if (stocks != null) {
				for (int m = 0; m < stocks.length; m++) {
					updateStockValue(stocks[m].getName(), stocks[m].getQty());
				}
				stocks2 = stockParser();
			}
		}

		int j = 0;
		String resp = "[";
		// String c = "";
		for (int i = 0; i < cryptos2.length; i++) {
			if (j > 0) {
				resp = resp + ",";
			}
			resp = resp + "{\"name\":\"" + cryptos2[i].getName() + "\", \"type\":\"" + cryptos2[i].getType()
					+ "\", \"qty\":\"" + cryptos2[i].getQty() + "\", \"price\":\"" + cryptos2[i].getPrice()
					+ "\", \"total\":\"" + cryptos2[i].getTotal() + "\"}";
			j++;
		}
		for (int i = 0; i < stocks2.length; i++) {
			if (j > 0) {
				resp = resp + ",";
			}
			resp = resp + "{\"name\":\"" + stocks2[i].getName() + "\", \"type\":\"" + stocks2[i].getType()
					+ "\", \"qty\":\"" + stocks2[i].getQty() + "\", \"price\":\"" + stocks2[i].getPrice()
					+ "\", \"total\":\"" + stocks2[i].getTotal() + "\"}";
			j++;
		}
		resp = resp + "]";
		System.out.println("Portfolio: " + resp);
		return resp;
	}

	public static Stock[] stockParser() throws InvalidDataException, IOException {
		int j = 0; // used to count records
		int k = 0; // used to cycle through the records to create objects
		final String SELECT_QUERY = "SELECT * FROM stocks ORDER BY symbol";
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			Stock obj[] = new Stock[0];
			return obj;
		}

		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				j++;  // count records to use below
			}
			st.close();
			con.close();
		} catch (Exception e) {
			System.out.println("CsvParser Failed to get records.");
			e.printStackTrace();
			Stock obj[] = new Stock[0];
			return obj;
		}
		Stock obj[] = new Stock[j];

		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				String symbol = rs.getString("symbol");
				double qty = rs.getDouble("qty");
				double price = rs.getDouble("price");
				double total = rs.getDouble("total");
				obj[k] = new Stock(symbol, qty, price, total);
				k++;
			}
			st.close();
			con.close();
		} catch (Exception e) {
			System.out.println("CsvParser Failed to create obj[].");
			e.printStackTrace();
		}
		return obj;
	}

	public static Crypto[] cryptoParser() throws InvalidDataException, IOException {
		int j = 0; // used to count records
		int k = 0; // used to cycle through the records to create objects
		final String SELECT_QUERY = "SELECT * FROM cryptos ORDER BY base";
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			Crypto obj[] = new Crypto[0];
			return obj;
		}
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				j++;  // count records to use below
			}
			st.close();
			con.close();
		} catch (Exception e) {
			System.out.println("CsvParser Failed to get records.");
			e.printStackTrace();
			Crypto obj[] = new Crypto[0];
			return obj;
		}
		Crypto obj[] = new Crypto[j];

		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				String base = rs.getString("base");
				double qty = rs.getDouble("qty");
				double price = rs.getDouble("price");
				double total = rs.getDouble("total");
				obj[k] = new Crypto(base, qty, price, total);
				k++;
			}
			st.close();
			con.close();
		} catch (Exception e) {
			System.out.println("CsvParser Failed to create obj[].");
			e.printStackTrace();
		}
		return obj;
	}

	@GetMapping("/updatestockvalue")
	public static double updateStockValue(String n, double q) {
		String name = n;
		double qty = q;
		double price = 0;
		double total = 0;
		double totalReturn = 0;
		String address = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + name
				+ "&apikey=7T49UGGHN2CPR75Z";
		try {
			HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(address)).build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			Object obj = new JSONParser().parse(response.body());
			JSONObject jo = (JSONObject) obj;
			JSONObject joData = (JSONObject) jo.get("Global Quote");
			String amt = (String) joData.get("05. price");
			price = Double.parseDouble(amt);
			total = price * qty;
			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				con.close();
			} catch (Exception e) {
				System.out.println("No DB yet");
				CreateAssetDB.go();
				System.out.println("DB created");
			}

			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				Statement st = con.createStatement();
				final String UPDATE_QUERY = "UPDATE stocks SET price = " + price + ", total = " + total
						+ " WHERE symbol = '" + name + "'";
				st.executeUpdate(UPDATE_QUERY);
				st.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("updateStockValue FAILED to update " + name);
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e);
		}
		return totalReturn;

	}

	@GetMapping("/updatecryptovalue")
	public static double updateCryptoValue(String n, double q) {
		String name = n;
		double qty = q;
		double price = 0;
		double total = 0;
		double totalReturn = 0;
		String address = "https://api.coinbase.com/v2/prices/" + name + "-USD/spot";

		try {
			HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(address)).build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			Object obj = new JSONParser().parse(response.body());
			JSONObject jo = (JSONObject) obj;
			JSONObject joData = (JSONObject) jo.get("data");
			String amt = (String) joData.get("amount");
			price = Double.parseDouble(amt);
			total = price * qty;
			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				con.close();
			} catch (Exception e) {
				System.out.println("No DB yet");
				CreateAssetDB.go();
				System.out.println("DB created");
			}

			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				Statement st = con.createStatement();
				final String UPDATE_QUERY = "UPDATE cryptos SET price = " + price + ", total = " + total
						+ " WHERE base = '" + name + "'";
				st.executeUpdate(UPDATE_QUERY);
				st.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("updateCryptoValue FAILED to update " + name);
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e);
		}
		return totalReturn;
	}
}
