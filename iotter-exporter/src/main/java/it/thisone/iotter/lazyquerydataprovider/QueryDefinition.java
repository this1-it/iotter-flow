package it.thisone.iotter.lazyquerydataprovider;

import java.util.List;
import java.util.function.Predicate;

import com.vaadin.flow.data.provider.QuerySortOrder;

/**
 * Interface for defining properties for a query in Vaadin 8 DataProvider context.
 * 
 *
 * @author Claude Sonnet
 * @param <T> The bean type
 * @param <F> The filter type
 */
public interface QueryDefinition<T, F> {

    /**
     * Gets the batch size for lazy loading.
     *
     * @return the batch size
     */
    int getBatchSize();

    /**
     * Sets the query batch size.
     *
     * @param batchSize the batchSize to set
     */
    void setBatchSize(final int batchSize);

    /**
     * Gets the max query size.
     *
     * @return the max query size
     */
    int getMaxQuerySize();

    /**
     * Sets the max query size.
     *
     * @param maxQuerySize the max query size
     */
    void setMaxQuerySize(final int maxQuerySize);

    /**
     * Gets the default sort orders.
     * @return the default sort orders
     */
    List<QuerySortOrder> getDefaultSortOrders();

    /**
     * Sets the default sort orders.
     * @param defaultSortOrders the default sort orders
     */
    void setDefaultSortOrders(final List<QuerySortOrder> defaultSortOrders);

    /**
     * Gets sort orders.
     * @return the sort orders
     */
    List<QuerySortOrder> getSortOrders();

    /**
     * Sets sort orders.
     * @param sortOrders the sort orders
     */
    void setSortOrders(final List<QuerySortOrder> sortOrders);

    /**
     * Adds default filter.
     * @param filter the default filter to add
     */
    void addDefaultFilter(final Predicate<T> filter);

    /**
     * Removes default filter.
     * @param filter the default filter to remove
     */
    void removeDefaultFilter(final Predicate<T> filter);

    /**
     * Clears default filters.
     */
    void removeDefaultFilters();

    /**
     * Gets default filters.
     * @return the default filters.
     */
    List<Predicate<T>> getDefaultFilters();

    /**
     * Gets the bean class this definition is for.
     * @return the bean class
     */
    Class<T> getBeanClass();

    /**
     * Creates a combined predicate from all active filters.
     * @param queryFilter the query filter from DataProvider query
     * @return combined predicate
     */
    Predicate<T> createCombinedFilter(F queryFilter);
}