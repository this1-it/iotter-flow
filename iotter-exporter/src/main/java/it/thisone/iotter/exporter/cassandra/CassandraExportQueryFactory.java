
package it.thisone.iotter.exporter.cassandra;



import java.util.Date;

import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.lazyquerydataprovider.Query;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
public class CassandraExportQueryFactory implements QueryFactory<ExportRow, Date> {

	private final IMeasureExporter queryExporter;
	
    public CassandraExportQueryFactory(IMeasureExporter exporter) {
    	queryExporter = exporter;
    }


    @Override
    public Query<ExportRow, Date> constructQuery(QueryDefinition<ExportRow, Date> definition) {
       return new CassandraExportQuery((CassandraExportQueryDefinition)definition, queryExporter);
    }

}
