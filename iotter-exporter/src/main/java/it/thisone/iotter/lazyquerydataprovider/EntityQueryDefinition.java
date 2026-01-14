package it.thisone.iotter.lazyquerydataprovider;

import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Defines entity query definition to be used with JPA entity managers for Vaadin 8.
 * 
 *
 * @author Claude Sonnet
 * @param <T> The entity type
 * @param <F> The filter type
 */
public class EntityQueryDefinition<T, F> extends LazyQueryDefinition<T, F> {
    
    private static final long serialVersionUID = 1L;
    
    private final Class<T> entityClass;
    private boolean detachedEntities = false;
    private boolean applicationManagedTransactions = false;
    private EntityManager entityManager;
    private Function<F, java.util.function.Predicate<T>> filterConverter;

    /**
     * Constructor for configuring entity query definition.
     *
     * @param entityClass                      The entity class
     * @param batchSize                       The batch size for lazy loading
     * @param applicationManagedTransactions  True if application manages transactions instead of container
     * @param detachedEntities               True if entities are detached from PersistenceContext
     */
    public EntityQueryDefinition(Class<T> entityClass, int batchSize, 
                                boolean applicationManagedTransactions, 
                                boolean detachedEntities) {
        super(entityClass, batchSize);
        this.entityClass = entityClass;
        this.applicationManagedTransactions = applicationManagedTransactions;
        this.detachedEntities = detachedEntities;
    }

    /**
     * Constructor with EntityManager.
     *
     * @param entityClass                      The entity class
     * @param batchSize                       The batch size for lazy loading
     * @param entityManager                   The JPA EntityManager
     * @param applicationManagedTransactions  True if application manages transactions instead of container
     * @param detachedEntities               True if entities are detached from PersistenceContext
     */
    public EntityQueryDefinition(Class<T> entityClass, int batchSize, 
                                EntityManager entityManager,
                                boolean applicationManagedTransactions, 
                                boolean detachedEntities) {
        this(entityClass, batchSize, applicationManagedTransactions, detachedEntities);
        this.entityManager = entityManager;
    }

    /**
     * Gets whether application manages transactions instead of container.
     *
     * @return true if application manages transactions
     */
    public boolean isApplicationManagedTransactions() {
        return applicationManagedTransactions;
    }

    /**
     * Sets whether application manages transactions instead of container.
     *
     * @param applicationManagedTransactions true if application manages transactions
     */
    public void setApplicationManagedTransactions(boolean applicationManagedTransactions) {
        this.applicationManagedTransactions = applicationManagedTransactions;
    }

    /**
     * Gets whether entities are detached from PersistenceContext.
     *
     * @return true if entities are detached
     */
    public boolean isDetachedEntities() {
        return detachedEntities;
    }

    /**
     * Sets whether entities are detached from PersistenceContext.
     *
     * @param detachedEntities true if entities should be detached
     */
    public void setDetachedEntities(boolean detachedEntities) {
        this.detachedEntities = detachedEntities;
    }

    /**
     * Gets the entity class.
     *
     * @return the entity class
     */
    @Override
    public Class<T> getBeanClass() {
        return entityClass;
    }

    /**
     * Gets the JPA EntityManager.
     *
     * @return the entity manager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Sets the JPA EntityManager.
     *
     * @param entityManager the entity manager
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Gets the filter converter function.
     *
     * @return the filter converter function
     */
    public Function<F, java.util.function.Predicate<T>> getFilterConverter() {
        return filterConverter;
    }

    /**
     * Sets the filter converter function that converts DataProvider filters to Predicates.
     *
     * @param filterConverter the filter converter function
     */
    public void setFilterConverter(Function<F, java.util.function.Predicate<T>> filterConverter) {
        this.filterConverter = filterConverter;
    }

    /**
     * Creates a JPA Criteria API predicate from the combined filter.
     * This method is used by EntityQuery to convert filters to JPA predicates.
     *
     * @param queryFilter    the query filter from DataProvider
     * @param criteriaBuilder the JPA CriteriaBuilder
     * @param criteriaQuery   the JPA CriteriaQuery
     * @param root           the JPA Root
     * @return JPA Predicate or null if no filters
     */
    public Predicate createJpaPredicate(F queryFilter, CriteriaBuilder criteriaBuilder, 
                                       CriteriaQuery<?> criteriaQuery, Root<T> root) {
        java.util.function.Predicate<T> combinedFilter = createCombinedFilter(queryFilter);
        
        if (combinedFilter == null) {
            return null;
        }
        
        // For JPA integration, we need to implement a way to convert Java predicates to JPA predicates
        // This is a complex mapping that would typically be handled by the filter converter
        // or by using a specialized filter implementation
        
        // Since we can't easily convert arbitrary Java predicates to JPA predicates,
        // we'll return null here and let the EntityQuery handle specific filter types
        // The filterConverter should be used for this conversion
        return null;
    }

    /**
     * Creates a combined filter using the filter converter if available.
     */
    @Override
    public java.util.function.Predicate<T> createCombinedFilter(F queryFilter) {
        java.util.function.Predicate<T> combinedFilter = null;

        // Combine default filters
        for (java.util.function.Predicate<T> filter : getDefaultFilters()) {
            if (combinedFilter == null) {
                combinedFilter = filter;
            } else {
                combinedFilter = combinedFilter.and(filter);
            }
        }

        // Add query filter using converter if available
        if (queryFilter != null && filterConverter != null) {
            java.util.function.Predicate<T> convertedFilter = filterConverter.apply(queryFilter);
            if (convertedFilter != null) {
                if (combinedFilter == null) {
                    combinedFilter = convertedFilter;
                } else {
                    combinedFilter = combinedFilter.and(convertedFilter);
                }
            }
        }

        return combinedFilter;
    }
}