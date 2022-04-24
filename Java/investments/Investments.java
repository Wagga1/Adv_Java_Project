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
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import java.time.Duration;

public class Investments {

	int count = 0;

	public static void main(String[] args) throws IOException, InterruptedException, NumberFormatException,
			SQLException, ParseException {

		while (true) {
			// Menu:
			System.out.println("Welcome to Combs Investments");
			System.out.println("");
			System.out.println("Main Menu:");
			System.out.println("1: Add Stocks");
			System.out.println("2: Remove Stocks");
			System.out.println("3: Add Crypto Coins");
			System.out.println("4: Remove Crypto Coins");
			System.out.println("5: Get Portfolio");
			System.out.println("0: Exit");

			Scanner hold = new Scanner(System.in);
			// Take input:
			Scanner scanner = new Scanner(System.in);
			String choice = scanner.nextLine();
			int i = Integer.parseInt(choice);

			// Use switch to decide path:
			switch (i) {
			case 1:
				try {
					System.out.println("What stock would you like to add (3-4 characters)?");
					scanner.useDelimiter("[A-Z]{3,4}");
					String name = scanner.nextLine();
					System.out.println("How many shares of " + name + " would you like to add?");
					scanner.useDelimiter("[0-9]");
					String qty_ = scanner.nextLine();
					double qty = Double.parseDouble(qty_);
					String s = addStock(name, qty);
					System.out.println(s);
				} catch (NumberFormatException e) {
					System.out.println("");
					System.out.println("Please only submit numbers for the quantity.");
					// e.printStackTrace();
					System.out.println("");
				}
				System.out.println("Press Enter key to continue...");
				hold.nextLine();

				break;
			case 2:
				try {
					System.out.println("What stock would you like to remove (3-4 characters)?");
					scanner.useDelimiter("[A-Z]{3,4}");
					String name = scanner.nextLine();
					System.out.println("How many shares of " + name + " would you like to remove?");
					scanner.useDelimiter("[0-9]");
					String qty_ = scanner.nextLine();
					double qty = Double.parseDouble(qty_);
					String s = removeStock(name, qty);
					System.out.println(s);
				} catch (NumberFormatException e) {
					System.out.println("");
					System.out.println("Please only submit numbers for the quantity.");
					// e.printStackTrace();
					System.out.println("");
				}
				System.out.println("Press Enter key to continue...");
				hold.nextLine();

				break;
			case 3:
				try {
					System.out.println("What coin would you like to add (3-4 characters)?");
					scanner.useDelimiter("[A-Z]{3,4}");
					String name = scanner.nextLine();
					System.out.println("How many " + name + " would you like to add?");
					scanner.useDelimiter("[0-9]");
					String qty_ = scanner.nextLine();
					double qty = Double.parseDouble(qty_);
					String c = addCrypto(name, qty);
					System.out.println(c);
				} catch (NumberFormatException e) {
					System.out.println("");
					System.out.println("Please only submit numbers for the quantity.");
					// e.printStackTrace();
					System.out.println("");
				}
				System.out.println("Press Enter key to continue...");
				hold.nextLine();

				break;
			case 4:
				try {
					System.out.println("What coin would you like to remove (3-4 characters)?");
					scanner.useDelimiter("[A-Z]{3,4}");
					String name = scanner.nextLine();
					System.out.println("How many " + name + " would you like to remove?");
					scanner.useDelimiter("[0-9]");
					String qty_ = scanner.nextLine();
					double qty = Double.parseDouble(qty_);
					String type = "crypto";
					Crypto crypto = new Crypto(name, qty);
					String s = removeCrypto(name, qty);
					System.out.println(s);
				} catch (NumberFormatException e) {
					System.out.println("");
					System.out.println("Please only submit numbers for the quantity.");
					// e.printStackTrace();
					System.out.println("");
				}
				System.out.println("Press Enter key to continue...");
				hold.nextLine();

				break;

			case 5:
				String x = getPortfolio();
				printPortfolio(x);
				System.out.println("Press Enter key to continue...");
				hold.nextLine();
				break;

			case 0:
				System.out.println("Thank you for your business!");
				scanner.close();
				hold.close();
				System.exit(0);
			}
		}
	}

	private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(10)).build();

	public static String getPortfolio() throws IOException, InterruptedException {
		String url = "http://localhost:8091/portfolio";
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println();
		return response.body();
	}

	public static String updateCryptoValue(String n, double q) throws IOException, InterruptedException {
		String url = "http://localhost:8091/updatecryptovalue?name=" + n + "&qty=" + q;
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println();
		return response.body();
	}

	public static String updateStockValue(String n, double q) throws IOException, InterruptedException {
		String url = "http://localhost:8091/updatecryptovalue?name=" + n + "&qty=" + q;
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println();
		return response.body();
	}

	public static void printPortfolio(String str)
			throws ParseException, IOException, InterruptedException {
		double gTotal = 0;

		// Print out portfolio header
		System.out.printf("%nSummary of your portfolio:%n");
		System.out.println("-".repeat(62));
		System.out.println("ASSET     TYPE      QTY            PRICE          VALUE");
		System.out.println("-".repeat(62));
		
		// Print the portfolio data
		JSONArray jsonarray = (JSONArray) new JSONParser().parse(str);
		for (Object element : jsonarray) {
			JSONObject jsonobject = (JSONObject) element;
			gTotal = gTotal + Double.parseDouble((String) jsonobject.get("total"));
			System.out.printf("%-10s%-10s%,-15.5f$%,-14.5f$%,-13.2f%n", jsonobject.get("name"), jsonobject.get("type"),
					Double.parseDouble((String) jsonobject.get("qty")),
					Double.parseDouble((String) jsonobject.get("price")),
					Double.parseDouble((String) jsonobject.get("total")));
		}

		// Print Summary
		System.out.println("-".repeat(62));
		System.out.printf("Your portfolio is worth a grand total of $%,.2f.%n", gTotal);
		System.out.println("-".repeat(62));
	}

	public static Crypto[] cryptoParser() throws IOException {
		int j = 0; // used to count records
		int k = 0; // used to cycle through the records to create objects
		final String SELECT_QUERY = "SELECT * FROM cryptos";

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

	public static Stock[] stockParser() throws IOException {
		String name;
		String type;
		int j = 0; // used to count records
		int k = 0; // used to cycle through the records to create objects
		final String SELECT_QUERY = "SELECT * FROM stocks";
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
				j++;
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
				obj[k] = new Stock(symbol, qty);
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

	public static String addStock(String n, double q) throws IOException, InterruptedException {
		String url = "http://localhost:8091/addstock?name=" + n + "&qty=" + q;
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public static String removeStock(String n, double q) throws IOException, InterruptedException {
		String url = "http://localhost:8091/removestock?name=" + n + "&qty=" + q;
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public static String addCrypto(String n, double q) throws IOException, InterruptedException {
		String url = "http://localhost:8091/addcrypto?name=" + n + "&qty=" + q;
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public static String removeCrypto(String n, double q) throws IOException, InterruptedException {
		String url = "http://localhost:8091/removecrypto?name=" + n + "&qty=" + q;
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

}
