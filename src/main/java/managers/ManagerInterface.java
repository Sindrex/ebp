package managers;

/**
 * Interface for the manager classes. Created as a last resort
 * to some consistency between the manager classes.
 *
 * @author Joergen Bele Reinfjell
 */
public interface ManagerInterface {
    /**
     * Refresh the locally stored cache of database objects.
     * @return true on success, otherwise false.
     */
    boolean refresh();


}
