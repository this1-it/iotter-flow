package it.thisone.iotter.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.CassandraMeasures;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.rest.model.DataPoint;
import it.thisone.iotter.rest.model.DataResultSet;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.util.BacNet;

@SwaggerDefinition(info = @Info(description = "My API", version = "1.1.0-1",title = "Swagger"))

@Path("/v1/client/data/{serial}")
@Api(value = "/v1/client/data/{serial}", tags = { "v1-client-data" }, hidden=ClientEndpoint.HIDDEN)
@Component
public class ClientDataEndpoint extends ClientEndpoint {

	private static Logger logger = LoggerFactory.getLogger(ClientDataEndpoint.class);

	@Autowired
	private IMeasureExporter cassandraExport;
	@Autowired
	private CassandraFeeds cassandraFeeds;
	@Autowired
	private DeviceService deviceService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "read-device-data", value = "client reads device data", notes = //
	"data points are retrievied for specified UTC time interval [from,to);" //
			+ "retrieval can be filtered by parameter id; " //
			+ "data points are temporally decimated if it is estimated that there are more than 1000 points in interval ; " //
			+ "timestamp are ordered with asc = false ;" //
			+ "measure value is normalized to parameter default measure ;" //
			+ "ext = true returns label/qualifier/unit of parameter default measure;" //
			+ "last measures are retrieved if interval has from/to equals 0 ;" //
			+ "", //
			response = DataResultSet.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "successful data retrieving", response = DataResultSet.class),
			@ApiResponse(code = 401, message = "unathorized", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "device not found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "invalid query parameters", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "internal server error") })
	public Response retrieveData(
			@ApiParam(name = "serial", value = "device serial", required = true) @PathParam("serial") String serial, //
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey, //
			@ApiParam(name = "from", value = "interval start date UTC in secs") @QueryParam("from") long from, //
			@ApiParam(name = "to", value = "interval end date UTC in secs") @QueryParam("to") long to, //
			@ApiParam(name = "ext", value = "flag for full datapoint attributes") @QueryParam("ext") boolean extended,
			@ApiParam(name = "asc", value = "flag for ascending order") @QueryParam("asc") boolean ascending,
			@ApiParam(name = "id", value = "parameter id list", allowMultiple = true) @QueryParam(value = "id") List<String> id) {

		try {
			checkAuthorization(apiKey, serial);
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(), e.getCode(),
					e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}

		if (to > from) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_INPUT_ERROR_CODE, "invalid date range");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			logger.error("device not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result).build();
		}

		Map<String, Channel> map = new HashMap<>();
		for (Channel channel : device.getChannels()) {
			if (channel.getConfiguration().isActive()) {
				if (id != null && !id.isEmpty()) {
					if (id.contains(channel.getNumber())) {
						map.put(channel.getKey(), channel);
					}
				} else {
					map.put(channel.getKey(), channel);
				}
			}
		}
		List<FeedKey> activeKeys = new ArrayList<>();
		Set<String> set = new HashSet<String>();
		for (String key : map.keySet()) {
			activeKeys.add(new FeedKey(serial, key));
			set.add(key);
		}
		DataResultSet result = new DataResultSet();
		if (from != 0 && to != 0) {
			Date fromDate = CassandraMeasures.toUTCDate(from * 1000);
			Date toDate = CassandraMeasures.toUTCDate(to * 1000);
			List<MeasureRaw> measures = cassandraExport.loadMeasures(activeKeys, fromDate, toDate, false);
			result.setFrom(from);
			result.setTo(to);
			for (MeasureRaw measure : measures) {
				Channel channel = map.get(measure.getKey());
				if (channel == null) {
					continue;
				}

				MeasureUnit unit = channel.getDefaultMeasure();
				DataPoint data = new DataPoint();
				data.setId(channel.getNumber());
				data.setTs(measure.getDate().getTime() / 1000);
				Float value = unit.convert(measure.getValue());
				data.setValue(value);

				if (extended) {
					data.setLabel(channel.getConfiguration().getLabel());
					data.setQual(channel.getConfiguration().getQualifier());
					data.setUnit(BacNet.lookUp(channel.getDefaultMeasure().getType()));
				}

				result.getValues().add(data);
			}
		} else if (from == 0 && to == 0) {
			List<Feed> feeds = cassandraFeeds.getFeedsValues(serial, set);
			from = System.currentTimeMillis() / 1000;
			to = 0;
			for (Feed feed : feeds) {
				Channel channel = map.get(feed.getKey());
				if (channel == null) {
					continue;
				}
				if (feed.getValue() != null && feed.getDate() != null) {
					DataPoint data = new DataPoint();
					long ts = feed.getDate().getTime() / 1000;
					if (ts > to)
						to = ts;
					if (ts < from)
						from = ts;
					data.setId(channel.getNumber());
					data.setTs(ts);

					MeasureUnit unit = channel.getDefaultMeasure();
					Float value = unit.convert(feed.getValue());
					data.setValue(value);

					if (extended) {
						data.setLabel(channel.getConfiguration().getLabel());
						data.setQual(channel.getConfiguration().getQualifier());
						data.setUnit(BacNet.lookUp(channel.getDefaultMeasure().getType()));
					}
					result.getValues().add(data);
				}
			}

			result.setFrom(from);
			result.setTo(to);
			Date lastContact = cassandraFeeds.getLastContact(device.getSerial());
			if (lastContact != null) {
				result.setLastContact(lastContact.getTime() / 1000);
			}

		} else {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_INPUT_ERROR_CODE, "invalid date range");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}
		

		logger.debug("GET /v1/client/data/{} with {}", serial, id);

		return Response.status(Response.Status.OK).entity(result).build();
	}

}
