import app.map.GoogleMapUtils;
import app.map.LatLng;

import java.io.*;

public class RandomPositions {
    public static void main(String[] args) {
        int n = 100;

        try (FileWriter writer = new FileWriter(new File("src/main/sql/pos.list"))) {
            for (int i = 0; i < n; i++) {
                LatLng p = GoogleMapUtils.getRandomRouteDestination();
                System.out.println(p);
                writer.write(p + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
