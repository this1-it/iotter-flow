package it.thisone.iotter.lazyquerydataprovider;

import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Utility class for converting common filter types to JPA Criteria API predicates.
 * This helps bridge the gap between Vaadin 8 DataProvider filters and JPA queries.
 *
 * @author Claude Sonnet
 * @param <T> The entity type
 */
public class JpaFilterConverter<T> {

    private final EntityManager entityManager;
    private final Class<T> entityClass;

    public JpaFilterConverter(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    /**
     * Creates a function that converts string filters to JPA predicates for text search.
     * This is useful for simple text-based filtering.
     *
     * @param propertyName the property name to filter on
     * @param caseSensitive whether the search should be case sensitive
     * @return filter converter function
     */
    public Function<String, java.util.function.Predicate<T>> createStringFilter(String propertyName, boolean caseSensitive) {
        return filterText -> {
            if (filterText == null || filterText.trim().isEmpty()) {
                return null;
            }
            
            return entity -> {
                try {
                    // This is a simplified in-memory predicate
                    // For actual JPA queries, you'd need to convert this to JPA predicates
                    Object propertyValue = getPropertyValue(entity, propertyName);
                    if (propertyValue == null) {
                        return false;
                    }
                    
                    String stringValue = propertyValue.toString();
                    String searchText = caseSensitive ? filterText : filterText.toLowerCase();
                    String targetText = caseSensitive ? stringValue : stringValue.toLowerCase();
                    
                    return targetText.contains(searchText);
                } catch (Exception e) {
                    return false;
                }
            };
        };
    }

    /**
     * Creates a JPA Criteria API predicate for string filtering.
     * This method should be used within JPA query construction.
     *
     * @param cb the CriteriaBuilder
     * @param root the Root entity
     * @param propertyName the property name to filter on
     * @param filterText the text to search for
     * @param caseSensitive whether the search should be case sensitive
     * @return JPA Predicate
     */
    public Predicate createStringPredicate(CriteriaBuilder cb, Root<T> root, 
                                         String propertyName, String filterText, boolean caseSensitive) {
        if (filterText == null || filterText.trim().isEmpty()) {
            return null;
        }

        Path<String> property = getPropertyPath(root, propertyName);
        String searchPattern = "%" + filterText + "%";

        if (caseSensitive) {
            return cb.like(property, searchPattern);
        } else {
            return cb.like(cb.lower(property), searchPattern.toLowerCase());
        }
    }

    /**
     * Creates a JPA Criteria API predicate for equality filtering.
     *
     * @param cb the CriteriaBuilder
     * @param root the Root entity
     * @param propertyName the property name to filter on
     * @param value the value to match
     * @return JPA Predicate
     */
    public Predicate createEqualsPredicate(CriteriaBuilder cb, Root<T> root, 
                                         String propertyName, Object value) {
        if (value == null) {
            return cb.isNull(getPropertyPath(root, propertyName));
        }
        return cb.equal(getPropertyPath(root, propertyName), value);
    }

    /**
     * Creates a JPA Criteria API predicate for range filtering.
     *
     * @param cb the CriteriaBuilder
     * @param root the Root entity
     * @param propertyName the property name to filter on
     * @param startValue the start value (inclusive)
     * @param endValue the end value (inclusive)
     * @return JPA Predicate
     */
    @SuppressWarnings("unchecked")
    public Predicate createRangePredicate(CriteriaBuilder cb, Root<T> root, 
                                        String propertyName, Comparable startValue, Comparable endValue) {
        Path<Comparable> property = getPropertyPath(root, propertyName);
        
        if (startValue != null && endValue != null) {
            return cb.between(property, startValue, endValue);
        } else if (startValue != null) {
            return cb.greaterThanOrEqualTo(property, startValue);
        } else if (endValue != null) {
            return cb.lessThanOrEqualTo(property, endValue);
        }
        
        return null;
    }

    /**
     * Gets property path for nested properties (e.g., "customer.name").
     */
    @SuppressWarnings("unchecked")
    private <P> Path<P> getPropertyPath(Root<T> root, String propertyName) {
        String[] propertyParts = propertyName.split("\\.");
        
        Path<P> path = null;
        for (String part : propertyParts) {
            if (path == null) {
                path = root.get(part);
            } else {
                path = path.get(part);
            }
        }
        return path;
    }

    /**
     * Gets property value using reflection.
     * This is used for in-memory filtering.
     */
    private Object getPropertyValue(T entity, String propertyName) throws Exception {
        String[] propertyParts = propertyName.split("\\.");
        Object current = entity;
        
        for (String part : propertyParts) {
            if (current == null) {
                return null;
            }
            
            // Use reflection to get property value
            try {
                java.lang.reflect.Field field = current.getClass().getDeclaredField(part);
                field.setAccessible(true);
                current = field.get(current);
            } catch (NoSuchFieldException e) {
                // Try getter method
                String getterName = "get" + Character.toUpperCase(part.charAt(0)) + part.substring(1);
                java.lang.reflect.Method getter = current.getClass().getMethod(getterName);
                current = getter.invoke(current);
            }
        }
        
        return current;
    }

    /**
     * Enhanced EntityQuery that uses JPA predicates for filtering.
     * This extends the basic EntityQuery to support JPA-based filtering.
     */
    public static class JpaEntityQuery<T, F> extends EntityQuery<T, F> {
        
        private final JpaFilterConverter<T> filterConverter;

        public JpaEntityQuery(EntityQueryDefinition<T, F> entityQueryDefinition, EntityManager entityManager) {
            super(entityQueryDefinition, entityManager);
            this.filterConverter = new JpaFilterConverter<>(entityManager, entityQueryDefinition.getBeanClass());
        }

        /**
         * Override to provide JPA-based filtering.
         */
        @Override
        protected Predicate createWherePredicate(CriteriaBuilder cb, CriteriaQuery<?> cq, 
                                               Root<T> root, QueryDefinition<T, F> queryDefinition) {
            // This is where you would implement specific filter handling
            // based on your filter types (F)
            
            // Example: if F is String, treat it as a text search
            if (queryDefinition instanceof EntityQueryDefinition) {
                EntityQueryDefinition<T, F> entityDef = (EntityQueryDefinition<T, F>) queryDefinition;
                
                // You can add custom filter handling here based on your needs
                // For example, if you have a specific filter type, convert it to JPA predicates
            }
            
            return super.createWherePredicate(cb, cq, root, queryDefinition);
        }

        public JpaFilterConverter<T> getFilterConverter() {
            return filterConverter;
        }
    }
}