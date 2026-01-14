
package it.thisone.iotter.lazyquerydataprovider;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * Entity query implementation for Vaadin 8 DataProvider that uses JPA EntityManager.
 * 
 *
 * @param <T> the entity type
 * @param <F> the filter type
 *
 * @author Claude Sonnet
 */
public class EntityQuery<T, F> implements Query<T, F>, Serializable {

    private static final long serialVersionUID = 1L;

    private final EntityManager entityManager;
    private final boolean applicationTransactionManagement;
    private final Class<T> entityClass;
    private final EntityQueryDefinition<T, F> queryDefinition;
    private int cachedQuerySize = -1;

    /**
     * Constructor for configuring the query.
     *
     * @param entityQueryDefinition The entity query definition
     * @param entityManager        The JPA entity manager
     */
    public EntityQuery(EntityQueryDefinition<T, F> entityQueryDefinition, EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryDefinition = entityQueryDefinition;
        this.entityClass = entityQueryDefinition.getBeanClass();
        this.applicationTransactionManagement = entityQueryDefinition.isApplicationManagedTransactions();
    }

    @Override
    public int size(QueryDefinition<T, F> queryDefinition) {
        if (cachedQuerySize == -1) {
            if (queryDefinition.getBatchSize() == 0) {
                return 0;
            }

            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            final Root<T> root = cq.from(entityClass);

            cq.select(cb.count(root));

            // Apply filters if available
            Predicate wherePredicate = createWherePredicate(cb, cq, root, queryDefinition);
            if (wherePredicate != null) {
                cq.where(wherePredicate);
            }

            final javax.persistence.Query query = entityManager.createQuery(cq);
            cachedQuerySize = ((Number) query.getSingleResult()).intValue();
        }
        return cachedQuerySize;
    }

    @Override
    public Stream<T> loadItems(QueryDefinition<T, F> queryDefinition, int offset, int limit) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(entityClass);
        final Root<T> root = cq.from(entityClass);

        cq.select(root);

        // Apply filters if available
        Predicate wherePredicate = createWherePredicate(cb, cq, root, queryDefinition);
        if (wherePredicate != null) {
            cq.where(wherePredicate);
        }

        // Apply sorting
        setOrderClause(cb, cq, root, queryDefinition);

        final javax.persistence.TypedQuery<T> query = entityManager.createQuery(cq);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        final List<T> entities = query.getResultList();
        
        // Detach entities if configured
        if (this.queryDefinition.isDetachedEntities()) {
            entities.forEach(entityManager::detach);
        }

        return entities.stream();
    }

    @Override
    public void saveItems(List<T> addedItems, List<T> modifiedItems, List<T> removedItems) {
        if (applicationTransactionManagement) {
            entityManager.getTransaction().begin();
        }
        try {
            // Process added items
            for (T item : addedItems) {
                if (!removedItems.contains(item)) {
                    entityManager.persist(item);
                }
            }

            // Process modified items
            for (T item : modifiedItems) {
                if (!removedItems.contains(item)) {
                    if (queryDefinition.isDetachedEntities()) {
                        item = entityManager.merge(item);
                    }
                    entityManager.persist(item);
                }
            }

            // Process removed items
            for (T item : removedItems) {
                if (!addedItems.contains(item)) {
                    if (queryDefinition.isDetachedEntities()) {
                        item = entityManager.merge(item);
                    }
                    entityManager.remove(item);
                }
            }

            if (applicationTransactionManagement) {
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            if (applicationTransactionManagement && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving items", e);
        }
        
        // Clear cached size as data has changed
        cachedQuerySize = -1;
    }

    @Override
    public boolean deleteAllItems() {
        if (applicationTransactionManagement) {
            entityManager.getTransaction().begin();
        }
        try {
            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            final CriteriaQuery<T> cq = cb.createQuery(entityClass);
            final Root<T> root = cq.from(entityClass);

            cq.select(root);

            // Apply same filters as in regular queries
            Predicate wherePredicate = createWherePredicate(cb, cq, root, queryDefinition);
            if (wherePredicate != null) {
                cq.where(wherePredicate);
            }

            final javax.persistence.TypedQuery<T> query = entityManager.createQuery(cq);
            final List<T> entities = query.getResultList();
            
            for (T entity : entities) {
                entityManager.remove(entity);
            }

            if (applicationTransactionManagement) {
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            if (applicationTransactionManagement && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting all items", e);
        }
        
        // Clear cached size as data has changed
        cachedQuerySize = -1;
        return true;
    }

    @Override
    public T constructItem() {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            
            // Set default values using reflection
            // This is a simplified version - in production you might want to use a more sophisticated approach
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                // You could set default values here based on field types or annotations
            }
            
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error constructing new entity instance", e);
        }
    }

    @Override
    public void refresh() {
        cachedQuerySize = -1;
    }

    /**
     * Creates WHERE predicate for JPA Criteria API query.
     * This method handles the conversion from QueryDefinition filters to JPA predicates.
     */
    protected Predicate createWherePredicate(CriteriaBuilder cb, CriteriaQuery<?> cq, 
                                         Root<T> root, QueryDefinition<T, F> queryDefinition) {
        // This is a simplified implementation
        // In a real implementation, you would need to handle specific filter types
        // and convert them to appropriate JPA predicates
        
        if (queryDefinition instanceof EntityQueryDefinition) {
            EntityQueryDefinition<T, F> entityQueryDef = (EntityQueryDefinition<T, F>) queryDefinition;
            // Use the entity query definition's method if available
            // This would need to be implemented based on your specific filter types
        }
        
        // For now, return null - filters would need to be implemented based on your specific filter types
        return null;
    }

    /**
     * Sets ORDER BY clause for JPA Criteria API query based on sort orders.
     */
    private void setOrderClause(CriteriaBuilder cb, CriteriaQuery<T> cq, 
                               Root<T> root, QueryDefinition<T, F> queryDefinition) {
        List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();
        
        if (!sortOrders.isEmpty()) {
            List<Order> orders = new ArrayList<>();
            for (QuerySortOrder sortOrder : sortOrders) {
                Path<Object> property = getPropertyPath(root, sortOrder.getSorted());
                if (sortOrder.getDirection() == SortDirection.ASCENDING) {
                    orders.add(cb.asc(property));
                } else {
                    orders.add(cb.desc(property));
                }
            }
            cq.orderBy(orders);
        }
    }

    /**
     * Gets property path for nested properties (e.g., "customer.name").
     */
    private Path<Object> getPropertyPath(Root<?> root, String propertyId) {
        String[] propertyIdParts = propertyId.split("\\.");
        
        Path<Object> path = null;
        for (String part : propertyIdParts) {
            if (path == null) {
                path = root.get(part);
            } else {
                path = path.get(part);
            }
        }
        return path;
    }

    /**
     * Gets the query definition used by this query.
     *
     * @return the query definition
     */
    protected EntityQueryDefinition<T, F> getQueryDefinition() {
        return queryDefinition;
    }

    /**
     * Gets the entity manager used by this query.
     *
     * @return the entity manager
     */
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}