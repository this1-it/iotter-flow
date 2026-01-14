package it.thisone.iotter.lazyquerydataprovider;

/**
 * Optional interface for query definitions that want to receive the current backend filter.
 *
 * @param <F> filter type
 */
public interface FilterableQueryDefinition<F> {

	void setQueryFilter(F filter);

	F getQueryFilter();
}
