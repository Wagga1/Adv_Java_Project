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

public class Crypto extends Asset {

	private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(10)).build();

	public Crypto(String n, double q) throws InvalidDataException {
		this.name = n;
		this.qty = q;
		this.type = "crypto"; // for type of asset
	}

	public Crypto(String n, double q, double v, double t) throws InvalidDataException {
		this.name = n;
		this.qty = q;
		this.price = v;
		this.total = t;
		this.type = "crypto"; // for type of asset
	}

	@Override
	public String toString() {
		return "Crypto [name=" + name + ", type=" + type + ", qty=" + qty + ", value=" + price + ", total= " + total
				+ "]";
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
	public void addAsset() { // Process to hold the full asset addition
		Crypto cryptos[];
		try {
			cryptos = dbParser(); // Get current values from csv
			cryptos = reallyAddAsset(cryptos); // Update values to include additional assets
			System.out.printf("%.2f of the %s %s added to your portfolio.%n", this.qty, this.type, this.name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Crypto[] reallyAddAsset(Crypto[] c) throws IOException, SQLException, InvalidDataException {
		// Process to actually add assets
		Crypto[] cryptos = c;
		String name = this.getName();
		double addQty = this.getQty();
		boolean found = false;

		for (int i = 0; i < cryptos.length; i++) {
			if (name.equals(cryptos[i].getName())) {
				double qty = cryptos[i].getQty();
				double newQty = qty + addQty;
				cryptos[i].setQty(newQty);
				found = true;

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
					System.out.println("CreateAssetDB already exists.");
					System.out.println("Connected to database!");
					final String SELECT_QUERY = "UPDATE cryptos SET qty = " + cryptos[i].getQty() + " WHERE base = '"
							+ name + "'";
					Statement st = con.createStatement();
					st.executeUpdate(SELECT_QUERY);
					System.out.println("You now have " + cryptos[i].getQty() + " of the crypto " + name);
					st.close();
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("reallyAddAsset FAILED");
				}

			} 

		}
		if (!found) {
			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				con.close();
			} catch (Exception e) {
				System.out.println("No DB yet");
				CreateAssetDB.go();
				System.out.println("DB created");
			}

			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			System.out.println("CreateAssetDB already exists.");
			System.out.println("Connected to database!");
			final String SELECT_QUERY = "INSERT INTO cryptos (base, qty) VALUES ('" + name + "', " + addQty + ")";
			Statement st = con.createStatement();
			st.executeUpdate(SELECT_QUERY);
			System.out.println("You now have " + addQty + " of the crypto " + name);
			st.close();
			con.close();
		}
		return cryptos;
	}

	@Override
	public void removeAsset() throws SQLException { // Process to hold the full asset addition
		Crypto cryptos[];
		cryptos = dbParser(); // Get current values from db
		cryptos = reallyRemoveAsset(cryptos); // Update values to include additional assets
	}

	public Crypto[] reallyRemoveAsset(Crypto[] c) throws SQLException {
		// Process to actually reduce assets
		Crypto[] cryptos = c;
		String name = this.getName();
		double removeQty = this.getQty();
		boolean found = false;
		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			CreateAssetDB.go();
			System.out.println("DB created");
		}

		for (Crypto crypto : cryptos) {
			if (name.equals(crypto.getName())) {
				double qty = crypto.getQty();
				if (Double.compare(removeQty, qty) > 0) {
					System.out.println("ERROR - Amount to remove is greater than existing amount.  You currently have "
							+ qty + ".");
					return cryptos;
				} else {
					try {
						double newQty = qty - removeQty;
						crypto.setQty(newQty);
						found = true;
						Connection con = DriverManager.getConnection("jdbc:derby:assets;");
						System.out.println("CreateAssetDB already exists.");
						System.out.println("Connected to database!");
						final String SELECT_QUERY = "UPDATE cryptos SET qty = " + newQty + " WHERE base = '" + name
								+ "'";
						Statement st = con.createStatement();
						st.executeUpdate(SELECT_QUERY);
						System.out.println("You now have " + newQty + " of the crypto " + name);
						st.close();
						con.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("reallyAddAsset FAILED");
					}
				}
			}
		}
		if (!found) {
			System.out.println("ERROR - No " + name + " found in your portfolio, so you cannot remove any of it.");
		}
		return cryptos;
	}
	
	@Override
	public Crypto[] dbParser() {
		int j = 0; // used to count records
		int k = 0; // used to cycle through the records to create objects
		final String SELECT_QUERY = "SELECT * FROM cryptos";

		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			con.close();
		} catch (Exception e) {
			System.out.println("No DB yet");
			Crypto obj[] = new Crypto[0];
			return obj;  // return empty Crypto[]
		}

		try {
			Connection con = DriverManager.getConnection("jdbc:derby:assets;");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SELECT_QUERY);
			while (rs.next()) {
				j++;
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
				obj[k] = new Crypto(base, qty);
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

	@Override
	public double updateValue() {
		String name = this.name;
		double value = 0;
		double total = 0;
		String address = "https://api.coinbase.com/v2/prices/" + name + "-USD/spot";

		try {
			HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(address)).build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			Object obj = new JSONParser().parse(response.body());
			JSONObject jo = (JSONObject) obj;
			JSONObject joData = (JSONObject) jo.get("data");
			String amt = (String) joData.get("amount");
			double qty = this.qty;
			value = Double.parseDouble(amt);
			total = value * qty;
			this.price = value;
			this.total = total;
			try {
				Connection con = DriverManager.getConnection("jdbc:derby:assets;");
				final String SELECT_QUERY = "UPDATE cryptos SET amount = " + this.price + ", value = " + this.total
						+ "WHERE base = '" + name + "'";
				Statement st = con.createStatement();
				st.executeUpdate(SELECT_QUERY);
				st.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Crypto updateValue FAILED to update " + name);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("updateValue FAILED");
		}
		return total;
	}

}
