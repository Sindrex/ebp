package managers;

import objects.Bike;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sindre Paulshus
 * @author Odd-Erik Frantzen
 */
public class BikeManagerTest {
    private BikeManager instance;

    @Before
    public void setUp() throws Exception {
        instance = new BikeManager();
    }

    @After
    public void tearDown() throws Exception {
        instance = null;
    }

    //Will be false as the bike with id 4 already is deleted.
    @Test
    public void deleteBike() {
        boolean res = false;
        try{
            instance.restoreBike(1, 1);
            res = instance.deleteBike(1);
            instance.restoreBike(1,1);
        }catch (Exception e){
            System.out.println("Error! " + e);
        }

        boolean expRes = true;
        assertEquals(expRes, res);
    }

    //expRes is false as you cant add the bike when it's already there.
    @Test
    public void addBike() {
        Bike b = new Bike(instance.getMaxId() + 1, "urban", "test", 10, "2018-04-23", Bike.ActiveStatus.DOCKED, 1);
        boolean res = instance.addBike(b);
        boolean expRes = true;
        assertEquals(expRes, res);
    }

    //expRes is false because this bike is restored already.
    @Test
    public void restoreBike() {
        boolean res = false;
        try{
            instance.deleteBike(1);
            res = instance.restoreBike(1, 1);
        }catch (Exception e){
            System.out.println("Error! Could not restore bike!");
        }
        boolean expRes = true;
        assertEquals(expRes, res);
    }

    @Test
    public void repairBike(){
        boolean res = false;
        try{
            res = instance.repairBike(1);
            instance.dockBike(1, 1);
        }catch (Exception e){
            System.out.println("Error! Could not repair bike!");
        }
        boolean expRes = true;
        assertEquals(expRes, res);
    }

    @Test
    public void dockBike(){
        boolean res = false;
        try{
            instance.rentBike(1,1);
            res = instance.dockBike(1, 1);
        }catch (Exception e){
            System.out.println("Error! Could not dock bike!");
        }
        boolean expRes = true;
        assertEquals(expRes, res);
    }

    @Test
    public void rentBike(){
        boolean res = false;
        try{
            instance.dockBike(1, 1);
            res = instance.rentBike(1, 1);
            instance.dockBike(1,1);
        }catch (Exception e){
            System.out.println("Error! Could not rent bike!");
        }
        boolean expRes = true;
        assertEquals(expRes, res);
    }

   @Test
   public void findBikeById() {
       Bike b = instance.findBike(BikeManager.SearchMode.SEARCH_BIKES_LIST, 1);
       assertTrue(b != null);
   }

   @Test
   public void getLatestHistories() {
        List<Bike> histories = instance.getBikeHistories(1, 100);
        assertTrue(histories != null);
   }
}