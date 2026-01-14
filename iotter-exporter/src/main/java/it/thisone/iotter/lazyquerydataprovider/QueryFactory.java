package it.thisone.iotter.lazyquerydataprovider;

/**
 * Interface for constructing queries for Vaadin 8 DataProvider.
 * 
 *
 * @author Claude Sonnet
 * @param <T> The bean type
 * @param <F> The filter type
 */
public interface QueryFactory<T, F> {
    
    /**
     * Constructs a new query according to the given QueryDefinition.
     *
     * @param queryDefinition The query definition containing configuration and filters
     * @return A new query constructed according to the given definition
     */
    Query<T, F> constructQuery(QueryDefinition<T, F> queryDefinition);
}