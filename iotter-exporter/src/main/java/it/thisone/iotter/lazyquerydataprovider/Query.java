package it.thisone.iotter.lazyquerydataprovider;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for loading data in batches for Vaadin 8 DataProvider.
 * 
 *
 * @author Claude Sonnet
 * @param <T> The bean type
 * @param <F> The filter type
 */
public interface Query<T, F> {
    
    /**
     * Gets number of items available through this query.
     *
     * @param queryDefinition The query definition containing filters and sort orders
     * @return Number of items.
     */
    int size(QueryDefinition<T, F> queryDefinition);

    /**
     * Load batch of items as a stream.
     *
     * @param queryDefinition The query definition containing filters and sort orders
     * @param offset Starting offset of the item list
     * @param limit Maximum number of items to retrieve
     * @return Stream of items
     */
    Stream<T> loadItems(QueryDefinition<T, F> queryDefinition, int offset, int limit);

    /**
     * Saves the modifications done to items.
     * Optional operation - may throw UnsupportedOperationException.
     *
     * @param addedItems    Items to be inserted
     * @param modifiedItems Items to be updated  
     * @param removedItems  Items to be deleted
     */
    default void saveItems(List<T> addedItems, List<T> modifiedItems, List<T> removedItems) {
        throw new UnsupportedOperationException("Save operations not supported by this query implementation");
    }

    /**
     * Removes all items.
     * Optional operation - may throw UnsupportedOperationException.
     *
     * @return true if the operation succeeded or false in case of a failure
     */
    default boolean deleteAllItems() {
        throw new UnsupportedOperationException("Delete all operation not supported by this query implementation");
    }

    /**
     * Constructs new item to be used when adding items.
     * Optional operation - may throw UnsupportedOperationException.
     *
     * @return The new item
     */
    default T constructItem() {
        throw new UnsupportedOperationException("Item construction not supported by this query implementation");
    }

    /**
     * Refreshes any cached data in the query implementation.
     * This method is called when the data provider needs to refresh its data.
     */
    default void refresh() {
        // Default implementation does nothing
    }
}