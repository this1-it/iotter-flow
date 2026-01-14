package it.thisone.iotter.lazyquerydataprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.vaadin.flow.data.provider.QuerySortOrder;

/**
 * Default implementation of QueryDefinition for Vaadin 8 DataProvider.
 * 
 *
 * @author Claude Sonnet
 * @param <T> The bean type
 * @param <F> The filter type
 */
public class LazyQueryDefinition<T, F> implements QueryDefinition<T, F> {

    private int batchSize = 100;
    private int maxQuerySize = Integer.MAX_VALUE;
    private List<QuerySortOrder> defaultSortOrders = new ArrayList<>();
    private List<QuerySortOrder> sortOrders = new ArrayList<>();
    private List<Predicate<T>> defaultFilters = new ArrayList<>();
    private Class<T> beanClass;

    /**
     * Constructor for LazyQueryDefinition.
     *
     * @param beanClass  The bean class
     * @param batchSize  The batch size for lazy loading
     */
    public LazyQueryDefinition(Class<T> beanClass, int batchSize) {
        this.beanClass = beanClass;
        this.batchSize = batchSize;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public int getMaxQuerySize() {
        return maxQuerySize;
    }

    @Override
    public void setMaxQuerySize(int maxQuerySize) {
        this.maxQuerySize = maxQuerySize;
    }

    @Override
    public List<QuerySortOrder> getDefaultSortOrders() {
        return new ArrayList<>(defaultSortOrders);
    }

    @Override
    public void setDefaultSortOrders(List<QuerySortOrder> defaultSortOrders) {
        this.defaultSortOrders = new ArrayList<>(defaultSortOrders);
        if (sortOrders.isEmpty()) {
            this.sortOrders = new ArrayList<>(defaultSortOrders);
        }
    }

    @Override
    public List<QuerySortOrder> getSortOrders() {
        return sortOrders.isEmpty() ? new ArrayList<>(defaultSortOrders) : new ArrayList<>(sortOrders);
    }

    @Override
    public void setSortOrders(List<QuerySortOrder> sortOrders) {
        this.sortOrders = new ArrayList<>(sortOrders);
    }

    @Override
    public void addDefaultFilter(Predicate<T> filter) {
        if (filter != null) {
            defaultFilters.add(filter);
        }
    }

    @Override
    public void removeDefaultFilter(Predicate<T> filter) {
        defaultFilters.remove(filter);
    }

    @Override
    public void removeDefaultFilters() {
        defaultFilters.clear();
    }

    @Override
    public List<Predicate<T>> getDefaultFilters() {
        return new ArrayList<>(defaultFilters);
    }

    @Override
    public Class<T> getBeanClass() {
        return beanClass;
    }

    @Override
    public Predicate<T> createCombinedFilter(F queryFilter) {
        Predicate<T> combinedFilter = null;

        // Combine default filters
        for (Predicate<T> filter : defaultFilters) {
            if (combinedFilter == null) {
                combinedFilter = filter;
            } else {
                combinedFilter = combinedFilter.and(filter);
            }
        }

        // Add query filter if it's a Predicate
        if (queryFilter instanceof Predicate) {
            @SuppressWarnings("unchecked")
            Predicate<T> predicateFilter = (Predicate<T>) queryFilter;
            if (combinedFilter == null) {
                combinedFilter = predicateFilter;
            } else {
                combinedFilter = combinedFilter.and(predicateFilter);
            }
        }

        return combinedFilter;
    }
}