package db.dao;

import java.util.List;

/**
 * This interface specifies the general interface for interacting with Data
 * Access Objects (DAO's).
 * <br>
 *     All DAO's should implement this class, and should prefer to use these
 * methods. This means that one should prefer to not create additional methods,
 * eg. getBikeById(), and if there is ever the need to create such additional
 * methods one should always provide the same functionality by using the
 * methods specified by this interface when possible.
 *
 * @author Joergen Bele Reinfjell
 * Date 05.03.2018
 */
public interface DataAccessInterface<T> {
    /**
     * Get an element by using a reference object with the needed fields .
     * @param reference the reference object with the fields used for unique
     *                 identification.
     * @return the element uniquely identified by the parameters, or null.
     * @throws Exception on failure to get a uniquely identified object.
     */
    T get(T reference) throws Exception;

    /**
     * Adds an element to the database.
     * @param element the element to add.
     * @return true on success, false on failure.
     * @throws Exception on failure to add a uniquely identified object.
     */
    boolean add(T element) throws Exception;

    /**
     * Updates an element in the database.
     * @param element the element to update.
     * @return true on success, false on failure.
     * @throws Exception on failure to update a uniquely identified object.
     */
    boolean update(T element) throws Exception;

    /**
     * Delete a element from the database.
     * @param element the element to delete.
     * @return true on success, false on failure.
     * @throws Exception on failure to delete a uniquely identified object.
     */
    boolean delete(T element) throws Exception;

    /**
     * Get a list of all elements of a given type T.
     * @return list of all elements of type T on success or null on failure.
     * @throws Exception on failure to get all object of type T.
     */
    List<T> getAll() throws Exception;

    /**
     * Adds all list elements to the database.
     * @param elements list of elements to be added.
     * @return true on success, false on failure.
     * @throws Exception on failure to add all object of type T.
     */
    boolean addAll(List<T> elements) throws Exception;

    /**
     * Updates all list elements in the database.
     * @param elements list of elements to be updated.
     * @return true on success, false on failure.
     * @throws Exception on failure to update all object of type T.
     */
    boolean updateAll(List<T> elements) throws Exception;

    /**
     * Deletes all list elements from the  database.
     * @param elements list of elements to be deleted.
     * @return true on success, false on failure.
     * @throws Exception on failure to delete all object of type T.
     */
    boolean deleteAll(List<T> elements) throws Exception;
}
