/**

// Simple setup
  LazyQueryDataProvider<Customer, String> dataProvider =
      EntityQueryFactory.createSimple(Customer.class, entityManager);

  // Advanced setup with custom configuration
  EntityQueryDefinition<Customer, String> queryDef =
      new EntityQueryDefinition<>(Customer.class, 100, entityManager, false, true);

  // Add default filters
  queryDef.addDefaultFilter(customer -> customer.isActive());

  // Set default sorting
  queryDef.setDefaultSortOrder("name", SortDirection.ASCENDING);

  EntityQueryFactory<Customer, String> factory =
      new EntityQueryFactory<>(entityManager);

  LazyQueryDataProvider<Customer, String> dataProvider =
      new LazyQueryDataProvider<>(queryDef, factory);

  // Use with Grid
  Grid<Customer> grid = new Grid<>();
  grid.setDataProvider(dataProvider);

  // Add text filtering
  HeaderRow filterRow = grid.appendHeaderRow();
  TextField nameFilter = new TextField();
  nameFilter.addValueChangeListener(event -> {
      dataProvider.setFilter(event.getValue());
  });
  filterRow.getCell("name").setComponent(nameFilter);




 */
package it.thisone.iotter.lazyquerydataprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * LazyQueryDataProvider provides lazy loading of items from business services for Vaadin 8.
 * 
 *
 * @author Claude Sonnet
 * @param <T> The bean type
 * @param <F> The filter type
 */
public class LazyQueryDataProvider<T, F> extends AbstractBackEndDataProvider<T, F>
        implements ConfigurableFilterDataProvider<T, F, F> {

    private static final long serialVersionUID = 1L;

    private final QueryDefinition<T, F> queryDefinition;
    private final QueryFactory<T, F> queryFactory;
    private final ConcurrentMap<String, it.thisone.iotter.lazyquerydataprovider.Query<T, F>> queryCache = new ConcurrentHashMap<>();
    private boolean cacheQueries = true;
    private F configuredFilter;

    /**
     * Constructs LazyQueryDataProvider with given queryFactory and queryDefinition.
     *
     * @param queryDefinition The query definition to be used
     * @param queryFactory    The query factory to be used
     */
    public LazyQueryDataProvider(QueryDefinition<T, F> queryDefinition, QueryFactory<T, F> queryFactory) {
        this.queryDefinition = queryDefinition;
        this.queryFactory = queryFactory;
    }

    /**
     * Constructs LazyQueryDataProvider with given parameters.
     *
     * @param beanClass    The bean class
     * @param batchSize    The batch size for lazy loading
     * @param queryFactory The query factory to be used
     */
    public LazyQueryDataProvider(Class<T> beanClass, int batchSize, QueryFactory<T, F> queryFactory) {
        this(new LazyQueryDefinition<>(beanClass, batchSize), queryFactory);
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, F> query) {
        // Update query definition with current sort orders
        updateQueryDefinitionSortOrders(query);
        
        // Get or create query instance
        it.thisone.iotter.lazyquerydataprovider.Query<T, F> dataQuery = getQuery(query);
        
        // Load items using the query
        return dataQuery.loadItems(queryDefinition, query.getOffset(), query.getLimit());
    }

    @Override
    protected int sizeInBackEnd(Query<T, F> query) {
        // Update query definition with current sort orders
        updateQueryDefinitionSortOrders(query);
        
        // Get or create query instance
        it.thisone.iotter.lazyquerydataprovider.Query<T, F> dataQuery = getQuery(query);
        
        // Get size from query
        int size = dataQuery.size(queryDefinition);
        
        // Respect max query size limit
        return Math.min(size, queryDefinition.getMaxQuerySize());
    }

    /**
     * Gets or creates a query instance, optionally using cache.
     */
    private it.thisone.iotter.lazyquerydataprovider.Query<T, F> getQuery(Query<T, F> query) {
        String cacheKey = createCacheKey(query);
        
        if (cacheQueries) {
            return queryCache.computeIfAbsent(cacheKey, k -> {
                updateQueryDefinitionFilter(query);
                return queryFactory.constructQuery(queryDefinition);
            });
        } else {
            updateQueryDefinitionFilter(query);
            return queryFactory.constructQuery(queryDefinition);
        }
    }

    /**
     * Creates a cache key based on the query parameters.
     */
    private String createCacheKey(Query<T, F> query) {
        F effectiveFilter = query.getFilter().orElse(configuredFilter);
        StringBuilder key = new StringBuilder();
        key.append("filter:").append(effectiveFilter);
        key.append(";sort:");
        for (QuerySortOrder sortOrder : query.getSortOrders()) {
            key.append(sortOrder.getSorted()).append(":").append(sortOrder.getDirection()).append(",");
        }
        return key.toString();
    }

    /**
     * Updates the query definition with current sort orders from the query.
     */
    private void updateQueryDefinitionSortOrders(Query<T, F> query) {
        List<QuerySortOrder> sortOrders = new ArrayList<>(query.getSortOrders());
        queryDefinition.setSortOrders(sortOrders);
    }

    /**
     * Updates the query definition with current filter from the query.
     */
    private void updateQueryDefinitionFilter(Query<T, F> query) {
        if (queryDefinition instanceof FilterableQueryDefinition) {
            F effectiveFilter = query.getFilter().orElse(configuredFilter);
            @SuppressWarnings("unchecked")
            FilterableQueryDefinition<F> filterable = (FilterableQueryDefinition<F>) queryDefinition;
            filterable.setQueryFilter(effectiveFilter);
        }
    }

    @Override
    public void setFilter(F filter) {
        this.configuredFilter = filter;
        refreshAll();
    }

    /**
     * Refreshes the data provider by clearing query cache and refreshing all data.
     */
    @Override
    public void refreshAll() {
        queryCache.clear();
        super.refreshAll();
    }

    /**
     * Refreshes a specific item in the data provider.
     * Note: This will clear the entire query cache as we cannot determine 
     * which cached queries might contain this item.
     */
    @Override
    public void refreshItem(T item) {
        queryCache.clear();
        super.refreshItem(item);
    }

    /**
     * Gets the query definition used by this data provider.
     *
     * @return the query definition
     */
    public QueryDefinition<T, F> getQueryDefinition() {
        return queryDefinition;
    }

    /**
     * Gets the query factory used by this data provider.
     *
     * @return the query factory
     */
    public QueryFactory<T, F> getQueryFactory() {
        return queryFactory;
    }

    /**
     * Sets whether queries should be cached.
     * 
     * @param cacheQueries true to enable query caching, false to disable
     */
    public void setCacheQueries(boolean cacheQueries) {
        this.cacheQueries = cacheQueries;
        if (!cacheQueries) {
            queryCache.clear();
        }
    }

    /**
     * Returns whether query caching is enabled.
     * 
     * @return true if query caching is enabled
     */
    public boolean isCacheQueries() {
        return cacheQueries;
    }

    /**
     * Adds a default filter to the query definition.
     * 
     * @param filter the filter to add
     */
    public void addDefaultFilter(java.util.function.Predicate<T> filter) {
        queryDefinition.addDefaultFilter(filter);
        refreshAll();
    }

    /**
     * Removes a default filter from the query definition.
     * 
     * @param filter the filter to remove
     */
    public void removeDefaultFilter(java.util.function.Predicate<T> filter) {
        queryDefinition.removeDefaultFilter(filter);
        refreshAll();
    }

    /**
     * Removes all default filters from the query definition.
     */
    public void removeDefaultFilters() {
        queryDefinition.removeDefaultFilters();
        refreshAll();
    }

    /**
     * Sets the default sort orders for the query definition.
     * 
     * @param sortOrders the default sort orders
     */
    public void setDefaultSortOrders(List<QuerySortOrder> sortOrders) {
        queryDefinition.setDefaultSortOrders(sortOrders);
        refreshAll();
    }

    /**
     * Convenience method to set default sort order for a single property.
     * 
     * @param property the property to sort by
     * @param direction the sort direction
     */
    public void setDefaultSortOrder(String property, SortDirection direction) {
        List<QuerySortOrder> sortOrders = new ArrayList<>();
        sortOrders.add(new QuerySortOrder(property, direction));
        setDefaultSortOrders(sortOrders);
    }
}
