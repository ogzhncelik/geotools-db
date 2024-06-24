
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.locationtech.jts.geom.Geometry;
import org.sqlite.SQLiteConfig;

public class Database {

	Statement statement = null;
	Connection con = null;

	public Database() {

		try {
	        SQLiteConfig conf = new SQLiteConfig();
	        conf.enableLoadExtension(true);
	        
			String url = "jdbc:sqlite:C:/Users/Admin/Desktop/Baskent_Gaz/db/template.sqlite";
			con = DriverManager.getConnection(url, conf.toProperties());
			statement = con.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("SELECT load_extension('mod_spatialite')");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

	public ResultSet executeSql(String sql) throws SQLException {
		try {
			ResultSet rs = statement.executeQuery(sql);
			
			return rs;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void insertGeom(Geometry polygon, Integer buildingId) throws SQLException {
		try {
	        
	        String polygonWKT = polygon.toText();
			
			String firstQuery = "SELECT pk FROM building WHERE id = ?";
			
			PreparedStatement firstStatement = con.prepareStatement(firstQuery);
			
			firstStatement.setInt(1, buildingId);
			
			ResultSet rs = firstStatement.executeQuery();
			
			if (rs.next() == false) {
				System.out.print(buildingId + " Id'li bina bilgisi kayıt edildi\n");
				String sql = "INSERT INTO building (geom, id) VALUES ((GeomFromText(?, 4326)),?)";
		        PreparedStatement statement = con.prepareStatement(sql);
		        
		        statement.setString(1, polygonWKT);
		        statement.setInt(2,buildingId);
				statement.executeUpdate();
				statement.close();
			} 
			else {
				System.out.print(buildingId + " Id'li bina zaten bulunuyor. Var olan geometri bilgisi güncelleniyor\n");
				String sql = "UPDATE building SET geom=(GeomFromText(?, 4326)) WHERE id = ?";
				PreparedStatement statement = con.prepareStatement(sql);

		        statement.setString(1, polygonWKT);
		        statement.setInt(2,buildingId);
		        statement.executeUpdate();
		        statement.close();
			}
			
			firstStatement.close();
		} catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
	}
	
	public void updateValue(Integer integer, int door_id) throws SQLException {
		try {
			String sql = "Update door set inside_building= ? where id= ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1,integer);
			statement.setInt(2,door_id);                        
            		
			statement.executeUpdate();
			
			statement.close();
		} catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
	}
}
