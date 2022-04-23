package investments;

import java.io.IOException;
import java.sql.SQLException;

public abstract class Asset {
	public String name = ""; // for name of the asset
	public double qty = 0; // for quantity of asset
	public String type = ""; // for type of asset
	public double price = 0; // for the value of the 1 unit of the asset
	public double total = 0; // for the total value of all units of the asset

	public abstract Object[] dbParser() throws Exception;

	public abstract void addAsset() throws SQLException;

	public abstract void removeAsset() throws SQLException;

	public abstract double updateValue() throws IOException;

}
