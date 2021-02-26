package db.dao;

import objects.BikeType;
import org.junit.*;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sindre Paulshus
 */
public class TypeDaoTest {
    private TypeDao instance;

    @Before
    public void setUp() {
        instance = TypeDao.getInstance();
    }

    @After
    public void tearDown() {
        instance = null;
    }

    @Test
    public void getAll() {
        List<BikeType> typeList = instance.getAll();
        List<String> res = new ArrayList<>();

        for (BikeType t : typeList) {
            res.add(t.getName());
        }

        List<String> expRes = new ArrayList<>();
        expRes.add("offroad");
        expRes.add("tandem");
        expRes.add("tricycle");
        expRes.add("unicycle");
        expRes.add("urban");


        assertTrue(res.containsAll(expRes));

        //assertArrayEquals(expRes.toArray(), res.toArray());
    }

    @Test
    public void has() {
        boolean res = instance.has(new BikeType("offroad"));
        boolean expRes = true;
        assertEquals(expRes, res);
    }
}