package it.thisone.iotter.lazyquerydataprovider;

import java.io.Serializable;

import javax.persistence.EntityManager;

/**
 * Factory for creating EntityQuery instances for Vaadin 8 DataProvider.
 * 
 *
 * @author Claude Sonnet
 * @param <T> The entity type
 * @param <F> The filter type
 */
public class EntityQueryFactory<T, F> implements QueryFactory<T, F>, Serializable {

    private static final long serialVersionUID = 1L;

    private final EntityManager entityManager;

    /**
     * Constructor for EntityQueryFactory.
     *
     * @param entityManager The JPA EntityManager to use for queries
     */
    public EntityQueryFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Query<T, F> constructQuery(QueryDefinition<T, F> queryDefinition) {
        if (!(queryDefinition instanceof EntityQueryDefinition)) {
            throw new IllegalArgumentException("EntityQueryFactory requires EntityQueryDefinition");
        }
        
        EntityQueryDefinition<T, F> entityQueryDefinition = (EntityQueryDefinition<T, F>) queryDefinition;
        
        // Set the entity manager in the definition if not already set
        if (entityQueryDefinition.getEntityManager() == null) {
            entityQueryDefinition.setEntityManager(entityManager);
        }
        
        return new EntityQuery<>(entityQueryDefinition, entityManager);
    }

    /**
     * Creates a new EntityQueryFactory with the given EntityManager.
     *
     * @param entityManager The JPA EntityManager
     * @param <T> The entity type
     * @param <F> The filter type
     * @return A new EntityQueryFactory instance
     */
    public static <T, F> EntityQueryFactory<T, F> create(EntityManager entityManager) {
        return new EntityQueryFactory<>(entityManager);
    }

    /**
     * Creates a complete LazyQueryDataProvider setup for JPA entities with string filtering.
     *
     * @param entityClass                    The JPA entity class
     * @param entityManager                  The JPA EntityManager
     * @param batchSize                     The batch size for lazy loading
     * @param applicationManagedTransactions Whether application manages transactions
     * @param detachedEntities              Whether entities should be detached
     * @param <T> The entity type
     * @return A configured LazyQueryDataProvider
     */
    public static <T> LazyQueryDataProvider<T, String> createForEntity(
            Class<T> entityClass,
            EntityManager entityManager,
            int batchSize,
            boolean applicationManagedTransactions,
            boolean detachedEntities) {
        
        EntityQueryDefinition<T, String> queryDefinition = new EntityQueryDefinition<>(
                entityClass, 
                batchSize, 
                entityManager,
                applicationManagedTransactions, 
                detachedEntities
        );
        
        EntityQueryFactory<T, String> factory = new EntityQueryFactory<>(entityManager);
        
        return new LazyQueryDataProvider<>(queryDefinition, factory);
    }

    /**
     * Creates a LazyQueryDataProvider with default settings for simple use cases.
     *
     * @param entityClass   The JPA entity class
     * @param entityManager The JPA EntityManager
     * @param <T> The entity type
     * @return A configured LazyQueryDataProvider with default settings
     */
    public static <T> LazyQueryDataProvider<T, String> createSimple(Class<T> entityClass, EntityManager entityManager) {
        return createForEntity(entityClass, entityManager, 50, false, false);
    }

    /**
     * Gets the EntityManager used by this factory.
     *
     * @return the entity manager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
}