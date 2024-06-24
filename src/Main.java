
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {

		System.out.println("Programa hoş geldiniz\n" + "1. seçenek : Poligon oluştur ve veritabanına kaydet\n" + "2. seçenek : Kapılar, binaların içinde mi sorgula\n"
				+ "3. seçenek : Kapıların bilgisini al");

		Scanner scan = new Scanner(System.in);

		System.out.println("Bir seçenek giriniz; ");
		int sayi = scan.nextInt();

		Database db = new Database();

		switch (sayi) {
		case 1:
			String sql = "Select building_id, st_astext(geom) as wkt_geom  from building_nodes  group by building_id,node_order;";

			ResultSet resultSet = db.executeSql(sql);

			
			Dictionary<Integer, ArrayList<String>> buildingNodeDict = new Hashtable<>();

			while (resultSet.next()) {

				int building_id = resultSet.getInt("building_id");

				String wkt = resultSet.getString("wkt_geom");

				ArrayList<String> nodeArrayList = buildingNodeDict.get(building_id);

				if (nodeArrayList != null) {
					nodeArrayList.add(wkt);
				} else {
					ArrayList<String> newList = new ArrayList<String>();
					newList.add(wkt);
					buildingNodeDict.put(building_id, newList);
				}

			}

			for (int i = 1; i < buildingNodeDict.size() + 1; i++) {
				ArrayList<String> arrayList = buildingNodeDict.get(i);
				Coordinate[] cordList = new Coordinate[arrayList.size()+1];
				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
				WKTReader reader = new WKTReader(geometryFactory);
				for (int j = 0; j < arrayList.size(); j++) {
					try {
						Point point = (Point) reader.read(arrayList.get(j));
						cordList[j] = point.getCoordinate();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				cordList[arrayList.size()] = cordList[0];
				Polygon polygon = geometryFactory.createPolygon(cordList);
				
				db.insertGeom(polygon,i);
			}
			break;

		case 2:
			String sql1 = "select door.id,st_Intersects(door.geom, building.geom) as result from door,building  where door.building_id=building.id;";
			ResultSet resultSet2 = db.executeSql(sql1);

			while (resultSet2.next()) {

				int result = resultSet2.getInt("result");
				int door_id =resultSet2.getInt("id");

				if (result == 0) {

					db.updateValue(0,door_id);
				} 
				else if (result == 1) {
					db.updateValue(1,door_id);

				} 
				else {
					db.updateValue(null,door_id);
				}

			}
			
			System.out.println("Bina ve kapıların birbirlerine göre durumları tamamlandı 3.seçenek ile görebilirsiniz");

			break;

		case 3:
			
			String sql2 = "select * from door;";
			ResultSet resultSet3 = db.executeSql(sql2);

			while (resultSet3.next()) {

				int door_id = resultSet3.getInt("id");
				int door_building_id = resultSet3.getInt("building_id");

				String door_no = resultSet3.getString("door_no");
				Integer inside_building = resultSet3.getInt("inside_building");
				
				String temp = "";
				
				if (resultSet3.wasNull()) {
			        temp = "Tespit Edilemedi";
			    } 
				else {
			        if (inside_building == 1) {
			            temp = "Evet";
			        } else {
			            temp = "Hayır";
			        }
			    }
				
				System.out.println("Kapı id:" + " " + door_id 
						+ "," + "Bina id:" + " " + door_building_id 
						+ "," + "Kapı No:" + " " + door_no 
						+ "," + "Bina İçinde Mi:" + temp );
			}

			break;

		default:
			
			System.out.println("Geçerli bir işlem seçin");

		}

	}

}
