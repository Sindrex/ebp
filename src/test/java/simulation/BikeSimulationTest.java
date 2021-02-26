package simulation;

import objects.Bike;
import objects.Repair;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joergen Bele Reinfjell
 */
public class BikeSimulationTest {

    @Test
    public void newRandomRepair() {
        BikeSimulation simulation = new BikeSimulation(2, System.currentTimeMillis(),1000, 1000);
        Bike bike = new Bike.Builder().id(1).build();

        // Found in newrandomRepair()
        final double[] probability = new double[]{40, 30, 10, 10, 5, 5};

        final String[] descriptions = new String[]{
                "Deflated tire",     "Maintenance",                  "Battery replacement",
                "Disfigured wheel replacement", "Frame replacement", "No more space in this note book, will follow up."};

        int stats[] = new int[descriptions.length];
        int sum = 0;

        for (int i = 0; i < 10e4; i++) {
            Repair repair = simulation.newRandomRepair(bike);
            for (int j = 0; j < descriptions.length; j++) {
                if (descriptions[j].equals(repair.getDescription())) {
                    stats[j]++;
                    sum++;
                    break;
                }
            }
        }

        for (int stat: stats) {
            assertTrue(Math.abs(stat - sum/descriptions.length) < 1000);
        }

    }
}