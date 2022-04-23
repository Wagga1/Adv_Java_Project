package investments;


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


public class Stock extends Asset {

	private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(10)).build();

	public Stock(String n, double q) throws InvalidDataException {
		this.name = n;
		this.qty = q;
		type = "stock"; // for type of asset
	}

	public Stock(String n, double q, double v, double t) throws InvalidDataException {
		this.name = n;
		this.qty = q;
		this.price = v;
		this.total = t;
		type = "stock"; // for type of asset
	}

	@Override
	public String toString() {
		return "Stock [name=" + name + ", type=" + type + ", qty=" + qty + ", value=" + price + ", total=" + total + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double value) {
		this.price = value;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@Override
	public void addAsset() throws SQLException {
		Stock stocks[];
		try {
			stocks = dbParser(); // Get current values from csv
			stocks = reallyAddAsset(stocks); // Update values to include additional assets
			System.out.printf("%.2f of the %s %s added to your portfolio.%n", this.qty, this.type, this.name);
		} catch (InvalidDataException | IOException e) {
			e.printStackTrace();
		}
	}

	public Stock[] reallyAddAsset_original(Stock[] c) throws InvalidDataException { // Process to actually add assets
		Stock[] stocks = c;
		double addQty = this.getQty();
		boolean found = false;
		for (int i = 0; i < stocks.length; i++) {
			if (name.equals(stocks[i].getName())) {
				double qty = stocks[i].getQty();
				double newQty = qty + addQty;
				stocks[i].setQty(newQty);
				found = true;
			}
		}
		if (!found) {
			Stock newStock = new Stock(name, qty);
			int newLength = stocks.length + 1;
			Stock obj[] = new Stock[newLength];
			int x = 0;
			for (int i = 0; i < stocks.length; i++) {
				obj[i] = stocks[i];
				x++;
			}
			obj[x] = newStock;
			stocks = obj;
		}
		return stocks;
	}

	public Stock[] reallyAddAsset(Stock[] c) throws InvalidDataException, SQLException {
		Stock[] stocks = c;
		String name = this.getName();
		double addQty = this.getQty();
		boolean found = false;

		for (int i = 0; i < stocks.length; i++) {
			if (name.equals(stocks[i].getName())) {
				double qty = stocks[i].getQty();
				double newQty = qty + addQty;
				stocks[i].setQty(newQty);
				found = true;
				try {
					Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				} catch (Exception e) {
					System.out.println("No DB yet");
					new CreateAssetDB();
					System.out.println("DB created");
				}

				try {
					Connection con = DriverManager.getConnection("jdbc:derby:assets;");
					System.out.println("CreateAssetDB already exists.");
					System.out.println("Connected to database!");
					final String SELECT_QUERY = "UPDATE stocks SET qty = " + stocks[i].getQty() + " WHERE symbol = '" + name + "'";
					Statement st = con.createStatement();
					st.executeUpdate(SELECT_QUERY);
					System.out.println("You now have " + stocks[i].getQty() + " of the stock " + name);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("reallyAddAsset FAILED");
				}
			} 
		}
		if (!found) {

			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			} catch (Exception e) {
				System.out.println("No DB yet");
				CreateAssetDB.go();
				System.out.println("DB created");
			}

			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				System.out.println("CreateAssetDB already exists.");
				System.out.println("Connected to database!");
				final String SELECT_QUERY = "INSERT INTO stocks (symbol, qty) VALUES ('" + name + "', " + addQty + ")";
				Statement st = con.createStatement();
				st.executeUpdate(SELECT_QUERY);
				System.out.println("You now have " + addQty + " of the stock " + name);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("reallyAddAsset FAILED");
			}
			Stock newStock = new Stock(name, qty);
			int newLength = stocks.length + 1;
			Stock obj[] = new Stock[newLength];
			int x = 0;
			for (int i = 0; i < stocks.length; i++) {
				obj[i] = stocks[i];
				x++;
			}
			obj[x] = newStock;
			stocks = obj;
		}
		return stocks;
	}

	@Override
	public void removeAsset() throws SQLException { // Process to hold the full asset addition
		Stock stocks[];
		try {
			stocks = dbParser(); // Get current values from db
			stocks = reallyRemoveAsset(stocks); // Update values to include additional assets
		} catch (InvalidDataException | IOException e) {
			e.printStackTrace();
		}
	}

	public Stock[] reallyRemoveAsset_original(Stock[] c) throws InvalidDataException, IOException {
		Stock[] stocks = c;
		String name = this.getName();
		double removeQty = this.getQty();
		boolean found = false;

		for (Stock stock : stocks) {
			if (name.equals(stock.getName())) {
				double qty = stock.getQty();
				if (Double.compare(removeQty, qty) > 0) {
					System.out.println("ERROR - Amount to remove is greater than existing amount.  You currently have " + qty + ".");
					return stocks;
				}
				double newQty = qty - removeQty;
				stock.setQty(newQty);
				System.out.println("reallyRemoveAsset: newly set i qty = " + stock.getQty());
				found = true;
				stock.setPrice(stock.updateValue());

				if (stock.type.equals("stock")) {
				} else if (stock.type.equals("stock")) {
					stock.total = stock.updateValue();
				} else {
					System.out.println("Something wrong with the file. Check " + stock.getName());
				}
			} else {
			}
		}
		if (!found) {
			System.out.println("ERROR - Amount to remove is greater than existing amount.");
		}
		return stocks;
	}

	public Stock[] reallyRemoveAsset(Stock[] c) throws InvalidDataException, IOException, SQLException {
		Stock[] stocks = c;
		String name = this.getName();
		double removeQty = this.getQty();
		boolean found = false;
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
		} catch (Exception e) {
			System.out.println("No DB yet");
			CreateAssetDB.go();
			System.out.println("DB created");
		}

		for (Stock stock : stocks) {
			if (name.equals(stock.getName())) {
				double qty = stock.getQty();
				if (Double.compare(removeQty, qty) > 0) {
					System.out.println("ERROR - Amount to remove is greater than existing amount.  You currently have " + qty + ".");
					return stocks;
				} else {
					try {
						double newQty = qty - removeQty;
						stock.setQty(newQty);
						found = true;
						Connection con = DriverManager.getConnection("jdbc:derby:assets;");
						System.out.println("CreateAssetDB already exists.");
						System.out.println("Connected to database!");
						final String SELECT_QUERY = "UPDATE cryptos SET qty = " + newQty + " WHERE base = '" + name + "'";
						Statement st = con.createStatement();
						st.executeUpdate(SELECT_QUERY);
						System.out.println("You now have " + newQty + " of the crypto " + name);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("reallyAddAsset FAILED");
					}
				}
			} else {
			}
		}
		if (!found) {
			System.out.println("ERROR - No " + name + " found in your portfolio, so you cannot remove any of it.");
		}
		return stocks;
	}

	@Override
	public Stock[] dbParser() throws InvalidDataException, IOException {
		int j = 0; // used to count records
		int k = 0; // used to cycle through the records to create objects
		final String SELECT_QUERY = "SELECT * FROM stocks";
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
		} catch (Exception e) {
			//System.out.println("No DB yet");
			Stock obj[] = new Stock[0];
			return obj;  // return empty Stock[]
		}

		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				j++;
			}
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
				obj[k] = new Stock(symbol, qty);
				k++;
			}
		} catch (Exception e) {
			System.out.println("CsvParser Failed to create obj[].");
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	public double updateValue() throws IOException { // NEED TO COMPLETE
		String name = this.getName();
		double value = 0;
		double total = 0;
		String address = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + name
				+ "&apikey=7T49UGGHN2CPR75Z";

		try {
			HttpRequest request = HttpRequest.newBuilder().GET()
					.uri(URI.create(address))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			Object obj = new JSONParser().parse(response.body());
			JSONObject jo = (JSONObject) obj;
			JSONObject joData = (JSONObject) jo.get("Global Quote");
			String amt = (String) joData.get("05. price");
			double qty = this.qty;
			value = Double.parseDouble(amt);
			total = value * qty;
			this.price = value;
			this.total = total;
			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				final String SELECT_QUERY = "UPDATE stocks SET price = " + this.price + ", value = " + this.total
						+ "WHERE symbol = '" + name + "'";
				Statement st = con.createStatement();
				st.executeUpdate(SELECT_QUERY);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Stock updateValue FAILED to update " + name);
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e);
		}
		return total;
	}
}
