package app.map;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joergen Bele Reinfjell
 */
public class LatLngTest {

    @Test
    public void actuallyEquals() {
        LatLng l1 = new LatLng(1, 1);
        LatLng l2 = new LatLng(1, 1);
        assertEquals(l1, l2);
    }

    @Test
    public void actuallyNotEquals() {
        LatLng l1 = new LatLng(1, 0);
        LatLng l2 = new LatLng(1, 1);
        assertNotEquals(l1, l2);

        LatLng l3 = new LatLng(1, 0);
        LatLng l4 = new LatLng(0, 1);
        assertNotEquals(l1, l2);
    }

    @Test
    public void epsilonDifferenceTest() {
        LatLng l1 = new LatLng(1 + LatLng.EPSILON, 0);
        LatLng l2 = new LatLng(1, 0);
        assertEquals(l1, l2);

        LatLng l3 = new LatLng(1.1 + LatLng.EPSILON, 0);
        LatLng l4 = new LatLng(1, 0);
        assertNotEquals(l3, l4);
    }

    @Test
    public void distanceTest() {
        LatLng l1 = new LatLng(1 + LatLng.EPSILON, 0);
        LatLng l2 = new LatLng(1, 0);
        System.out.println(l1.distance(l2));
    }

    @Test
    public void compareTo() {
    }
}