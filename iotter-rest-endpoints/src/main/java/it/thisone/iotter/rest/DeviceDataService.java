package it.thisone.iotter.rest;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.ExportQuery;
import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.concurrent.RateLimitControl;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.eventbus.DeviceConnectedEvent;
import it.thisone.iotter.exporter.CDataPoint;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.rest.model.DataPoint;
import it.thisone.iotter.rest.model.DataResultSet;
import it.thisone.iotter.rest.model.DataRow;
import it.thisone.iotter.rest.model.DataWrite;
import it.thisone.iotter.rest.model.DataWriteSet;
import it.thisone.iotter.rest.model.DeviceAcknowledge;
import it.thisone.iotter.rest.model.DeviceData;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.ServiceableRetrieval;
import it.thisone.iotter.util.Utils;

@Api(value = "/v1/device/{serial}/data", tags = { "v1-device-data" })
@Path("/v1/device/{serial}/data")
@Component
public class DeviceDataService {
	@Autowired
	private CacheManager cacheManager;

	// private static Logger flogger = LoggerFactory.getLogger("performance");
	private static Logger exporter = LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);
	private static Logger logger = LoggerFactory.getLogger(DeviceDataService.class);

	// private static final Response.Status INVALID_DATA_STATUS =
	// Response.Status.NOT_ACCEPTABLE;
	private static final Response.Status INVALID_DATA_STATUS = Response.Status.CREATED;

	@Autowired
	@Qualifier("rateLimitControl")
	private RateLimitControl rateLimitControl;

	@Autowired
	
	private ObjectMapper mapper;

	@Autowired
	private CassandraService cassandraService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private TracingService tracingService;

	@Autowired
	private SubscriptionService subscriptionService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "data", notes = "Data related to the plant parameters can be exported specifying a time interval. "
			+ "It is necessary to specify the serial of the plant and the temporal extremes,"
			+ "eventually a read api if it has been configured on the plant"
			+ "Query can be temporally ordered and filtered by a list of parameters and temporal aggregation "
			+ "A parameter id is equivalent to a modbus register address "
			+ "which consists of an address and register read type (c Coil, d Discrete Input, i Input, h Holding ). "
			+ "i.e. a modbus register with address 8 and type read Coil can be queried with id = 8:c "
			+ "eventually a register can be addressed with a bit mask."
			+ "Temporal aggregation: real time data values with RAW, aggregated values in intervals MIN15 (15 minutes) ,H1 (1 hours), W1 (1 week), M1 (1 month) "
			+ "The data is retrieved in batches, the first response provides a query id that used in subsequent requests allows to get all the following batches. "
			+ "Batches can be retrieved arbitraly using start parameter, "
			+ "setting start=-1 means retrieve next batch in sequence. "
			+ "Different batches of same query cannot be requested simultaneosly. "
			+ "Batch size cannot be specify because it is proportional on how many parameter ids have been queried."
			+ "i.e. Having requested 100 parameters, query will returns a max of 500 rows or 50000 data points which is MAX_DATA_POINTS_SIZE "
			+ "Batch size = (MAX_DATA_POINTS_SIZE / parameters list size)."
			+ "Parameter last values can be queried setting from=0, to=0", response = DataResultSet.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "successful data retrieving", response = DataResultSet.class),
			@ApiResponse(code = 401, message = "unathorized", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "device not found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "invalid request", response = RestErrorMessage.class),
			@ApiResponse(code = 503, message = "service unavailable, a query is already running ", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "internal server error") })
	public Response readData( //
			@ApiParam(name = "serial", value = "device serial", required = true) @PathParam("serial") String serial, //
			@ApiParam(name = "api-key", value = "device read api key", required = true) @HeaderParam("api-key") String apiKey, //
			@ApiParam(name = "from", value = "from date in secs UTC") @QueryParam("from") long from, //
			@ApiParam(name = "to", value = "to date in secs UTC") @QueryParam("to") long to,
			@ApiParam(name = "asc", value = "flag for ascending order") @QueryParam("asc") Boolean ascending,
			@ApiParam(name = "id", value = "parameter id list", allowMultiple = true) @QueryParam(value = "id") List<String> id,
			@ApiParam(name = "interpolation", value = "temporal aggregation", allowableValues = "RAW,MIN15,H1,D1,W1,M1", defaultValue = "RAW") @QueryParam(value = "interpolation") String interpolation,
			@ApiParam(name = "qid", value = "query id") @QueryParam(value = "qid") String qid,
			@ApiParam(name = "start", value = "batch start position, use -1 in consecutive queries  ", defaultValue = "0") @QueryParam(value = "start") long start

	) {
		RestErrorMessage error = new RestErrorMessage();
		String owner = "";
		String lockId = "";
		DataResultSet result = null;
		ExportQuery query = null;
		try {

			Thread.sleep(ThreadLocalRandom.current().nextInt(16, 24) * 1000);
			Device device = deviceService.findDeviceCacheable(serial);
			if (device == null) {
				int status = Response.Status.NOT_FOUND.getStatusCode();
				int code = Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE;
				String message = String.format("device not found with serial %s", serial);
				error = new RestErrorMessage(status, code, message);
				logger.debug(error.getMessage());
				return Response.status(error.getStatus()).entity(error).build();
			}

			if (device.notActive()) {
				error = new RestErrorMessage(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
						Constants.Error.DEVICE_NOT_ACTIVE, "cannot write data device not active " + serial);
				logger.debug(error.getMessage());
				return Response.status(error.getStatus()).entity(error).build();
			}

			owner = device.getOwner();
			lockId = owner + "-read-data";
			if (!cassandraService.getRollup().lockSink(lockId, 20)) {
				error = new RestErrorMessage(Response.Status.TOO_MANY_REQUESTS.getStatusCode(),
						Constants.Error.GENERIC_APP_ERROR_CODE, "a read request is already running");
				exporter.error(String.format("%s %s %s", owner, serial, error.getMessage()));
				return Response.status(error.getStatus()).entity(error).build();
			}

			// Check rate limit
//	        Optional<SimpleRateLimiter> limiter = rateLimitControl.getRateLimiter(owner);
//	        boolean allowRequest = limiter.get().tryAcquire();
//	        if (!allowRequest) {
//				error = new RestErrorMessage(Response.Status.TOO_MANY_REQUESTS.getStatusCode(),
//						Constants.Error.GENERIC_APP_ERROR_CODE, "too many requests");
//				exporter.error(String.format("%s %s %s",owner, serial, error.getMessage()) );
//				return Response.status(error.getStatus()).entity(error).build();
//	        }

			// Check authorization
			String deviceApiKey = getApiKey(device);
			if (deviceApiKey != null) {
				if ((apiKey != null) && !deviceApiKey.equals(apiKey)) {
					int status = Response.Status.UNAUTHORIZED.getStatusCode();
					int code = Constants.Error.DEVICE_READAPIKEY_ERROR_CODE;
					String message = String.format("read request for device with serial %s has wrong read api key",
							serial);
					throw new RestServiceException(status, code, message);
				} else if (apiKey == null) {
					int status = Response.Status.UNAUTHORIZED.getStatusCode();
					int code = Constants.Error.DEVICE_READAPIKEY_ERROR_CODE;
					String message = String.format("read request for device with serial %s missing api key", serial,
							deviceApiKey, apiKey);
					throw new RestServiceException(status, code, message);
				}
			}

			if (qid == null && from == 0 && to == 0) {
				List<String> keys = feedKeys(id, device);
				if (keys.isEmpty()) {
					int status = Response.Status.NOT_ACCEPTABLE.getStatusCode();
					int code = Constants.Error.DEVICE_READ_PARAMETERS_ERROR_CODE;
					String message = String.format("read request for device with serial %s has empty ids list", serial);
					throw new RestServiceException(status, code, message);
				}
				result = readLastValues(device, keys);
			} else {
				query = readQuery(qid, device, from, to, start, ascending, id, interpolation, apiKey);
				result = readExport(device, query);
				result.setSerial(serial);
			}

			exporter.info("/v1/device/{}/data, id {}, from {}, to {}, size {}", result.getSerial(), id,
					result.getFrom(), result.getTo(), result.getRows().size());

			cassandraService.getRollup().unlockSink(lockId);
			return Response.status(Response.Status.OK).entity(result).build();
		} catch (Throwable e) {
			String endpoint = String.format("GET /v1/device/%s/data", serial);
			tracingService.traceRestError(e.toString(), owner, null, serial, endpoint + " " + e.getMessage(), null);
			logger.error(e.getMessage(), e);

			if (e instanceof RestServiceException) {
				error = new RestErrorMessage(((RestServiceException) e).getStatus(),
						((RestServiceException) e).getCode(), e.getMessage());
			} else {
				error = new RestErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
						Constants.Error.GENERIC_APP_ERROR_CODE, e.getMessage());
			}
			cassandraService.getRollup().unlockSink(lockId);

		}
		return Response.status(error.getStatus()).entity(error).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "data", notes = "device data writing", response = DeviceAcknowledge.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "successful data retrieving", response = DeviceAcknowledge.class),
			@ApiResponse(code = 401, message = "unathorized", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "device not found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "not acceptable data points", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "internal server error") })
	public Response writeData(@PathParam("serial") String serial, DataWrite request) {
		RestErrorMessage error = new RestErrorMessage();
		String owner = Constants.SYSTEM;
//		long startTime = System.currentTimeMillis();
		// Check for device existence
		try {
			Device device = deviceService.findDeviceCacheable(serial);
			if (device == null || request == null) {
				throw new RestServiceException(Response.Status.NOT_FOUND.getStatusCode(),
						Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "device not found " + serial);
			}
			owner = device.getOwner();
			if (device.notActive()) {
				error = new RestErrorMessage(INVALID_DATA_STATUS.getStatusCode(), Constants.Error.DEVICE_NOT_ACTIVE,
						"cannot write data device not active " + serial);
				logger.debug(error.getMessage());
				return Response.status(error.getStatus()).entity(error).build();
			}
			// Check authorization
			if (device.getWriteApikey() != null && request.getApi_key() != null) {
				if (!device.getWriteApikey().equals(request.getApi_key())) {
					logger.error("device with serial {} has wrong WriteApikey  {}", serial, device.getWriteApikey());
					throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(),
							Constants.Error.DEVICE_WRITEAPIKEY_ERROR_CODE, "device wrong api key " + serial);
				}
			} else {
				throw new RestServiceException(Response.Status.NOT_FOUND.getStatusCode(),
						Constants.Error.DEVICE_WRITEAPIKEY_ERROR_CODE, "missing api key " + serial);
			}

			// Insert data
			processDataWrite(request, device);

			boolean flag = false;
			long ts = InterpolationUtils.currentTimeMillis(device.getTimeZone());
			// logOperation(serial, "total execution time", startTime);
			return Response.status(Response.Status.CREATED).entity(new DeviceAcknowledge(ts, flag)).build();

		} catch (Throwable e) {
			int status = 500;
			int code = 5000;
			if (e instanceof RestServiceException) {
				status = ((RestServiceException) e).getStatus();
				code = ((RestServiceException) e).getCode();
			}

			String json = null;
			try {
				json = mapper.writeValueAsString(request);
			} catch (Exception ex) {
				// ignoring exception
			}
			String msg = String.format("POST /v1/device/%s/data %s", serial, e.getMessage());
			if (tracingService.traceRestError(e.toString(), owner, null, serial, msg, json)) {
				// logger.error(e.getMessage(), Utils.logStackTrace(e));
				logger.error("[{}] {}", msg, Utils.logStackTrace(e));
			}

			error = new RestErrorMessage(status, code, e.getMessage());
		}
		return Response.status(error.getStatus()).entity(error).build();
	}

	/**
	 * Processes the data write request for a device by handling each DataWriteSet.
	 * <p>
	 * This method validates and processes the data contained in the incoming request.
	 * For each DataWriteSet in the request, it ensures that there are data values, and then
	 * delegates detailed processing to the processDeviceData method. It aggregates various aspects 
	 * of the device data using several maps, including:
	 * <ul>
	 *   <li>Aggregations: Grouping measure aggregations keyed by device serial.</li>
	 *   <li>Events: Collecting raw measure events for further processing.</li>
	 *   <li>Devices: Maintaining a map of devices based on their serial.</li>
	 *   <li>Last Values: Tracking the latest feed values per device.</li>
	 *   <li>Tick Map: Recording timestamps (ticks) associated with the measures.</li>
	 *   <li>Data Points: Collecting data point mappings for subsequent use.</li>
	 * </ul>
	 * Once all DataWriteSets are processed, it calls the postProcessWrite method to handle any
	 * necessary updates (e.g., notification of device connection, updating feeds, or roll-up operations).
	 * </p>
	 *
	 * @param request the DataWrite request containing one or more DataWriteSet entries to be processed
	 * @param main    the primary Device for which the data write is being processed
	 * @throws RestServiceException if the request is invalid (for example, when data values are missing)
	 *         or if any error occurs during the processing of the device data
	 */
	private void processDataWrite(DataWrite request, Device main) throws RestServiceException {
		Map<String, List<MeasureAggregation>> aggregations = new HashMap<String, List<MeasureAggregation>>();
		Map<String, List<MeasureRaw>> events = new HashMap<String, List<MeasureRaw>>();
		Map<String, Device> devices = new HashMap<String, Device>();
		Map<String, List<Feed>> lastValuesMap = new HashMap<String, List<Feed>>();
		Map<String, Set<Date>> tickMap = new HashMap<String, Set<Date>>();
		
		Map<String, List<DataPoint>> dataPointMap = new HashMap<String, List<DataPoint>>();
		devices.put(main.getSerial(), main);
		for (DataWriteSet dataSet : request.getData()) {
			if (!dataSet.getValues().isEmpty()) {
				processDeviceData(main, dataSet, devices, events, lastValuesMap, aggregations, tickMap, dataPointMap);
			} else {
				throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
						Constants.Error.INVALID_DATA_ERROR_CODE, "data values are missing");
			}
		}
		postProcessWrite(main, events, devices, lastValuesMap, aggregations, tickMap,dataPointMap);
	}

	/**
	 * Post-processes device data after a data write operation.
	 * <p>
	 * This method performs several post-write operations:
	 * <ul>
	 *   <li>It triggers device connection notifications for devices that are either activated or verified (and have an associated master device).</li>
	 *   <li>It delegates further processing to the subscription service, which handles feed updates, measurement aggregations,
	 *       tick calculations, and prepares DataPoint objects for export (facilitating easy extraction of data into CSV or Excel formats).</li>
	 * </ul>
	 * </p>
	 *
	 * @param main           the primary Device for which the data write occurred
	 * @param events         a map of raw measurement events (MeasureRaw) keyed by device serial number
	 * @param devices        a map of Device objects involved in the data write, keyed by their serial numbers
	 * @param lastValuesMap  a map of lists of Feed objects representing the latest parameter values for each device
	 * @param aggregations   a map of measure aggregations (MeasureAggregation) keyed by device serial number, used for roll-up processing
	 * @param tickMap        a map of timestamp ticks (as Sets of Dates) for each device, used to track measurement intervals
	 * @param dataPointMap   a map of DataPoint lists for each device,the data points are transmitted via MQTT, enabling external applications
	 *       to easily integrate and consume real-time information
	 */
	private void postProcessWrite(Device main, Map<String, List<MeasureRaw>> events, Map<String, Device> devices,
			Map<String, List<Feed>> lastValuesMap, Map<String, List<MeasureAggregation>> aggregations,
			Map<String, Set<Date>> tickMap, Map<String, List<DataPoint>> dataPointMap) {

		for (Device device : devices.values()) {
			if (device.getStatus().equals(DeviceStatus.ACTIVATED)) {
				subscriptionService.deviceConnected(new DeviceConnectedEvent(this, device.getSerial()));
			}
			if (device.getStatus().equals(DeviceStatus.VERIFIED) && device.getMaster() != null) {
				subscriptionService.deviceConnected(new DeviceConnectedEvent(this, device.getSerial()));
			}
		}
		subscriptionService.postProcessData(main, events, devices, lastValuesMap, aggregations, tickMap, dataPointMap);
	}

	/**
	 * Processes the device-specific data for a given DataWriteSet.
	 * <p>
	 * This method handles the processing of data received for a specific device. It performs several tasks:
	 * <ul>
	 *   <li>Validates and updates the device data based on timestamps, ensuring they fall within valid ranges.</li>
	 *   <li>Retrieves or refreshes device configurations as needed and collects parameter identifiers from the data.</li>
	 *   <li>Generates MeasureRaw instances (representing raw measurement events) for valid data points.</li>
	 *   <li>Maintains and updates feed values with the latest measurement results, marking changes accordingly.</li>
	 *   <li>Aggregates data needed for roll-up operations, such as measure aggregations and timestamp ticks.</li>
	 *   <li>Builds a list of DataPoint objects for subsequent insertion and further processing.</li>
	 * </ul>
	 * The method updates several maps that are later used in post-processing to handle notifications, roll-up computations,
	 * and feed updates.
	 * </p>
	 *
	 * @param main          the primary Device for which the data is being processed.
	 * @param dataSet       the DataWriteSet containing the data points and related details to be processed.
	 * @param devices       a map of devices keyed by their serial numbers; updated if a new device is encountered.
	 * @param events        a map that accumulates raw measurement events (MeasureRaw) keyed by device serial.
	 * @param lastValuesMap a map that holds the most recent feed values for each device.
	 * @param aggregations  a map for aggregating measures (MeasureAggregation) to be used for roll-up operations.
	 * @param tickMap       a map collecting timestamp ticks (as Sets of Dates) for each device, used to track measurement time intervals.
	 * @param dataPointMap  a map that accumulates lists of DataPoint objects for each device, these sets are stored to facilitate easy extraction of data into CSV or Excel formats
	 * @throws RestServiceException if any data inconsistency, missing information, or invalid parameters are encountered during processing.
	 */
	@SuppressWarnings("static-access")
	private void processDeviceData(Device main, 
			DataWriteSet dataSet, 
			Map<String, Device> devices,
			Map<String, List<MeasureRaw>> events, 
			Map<String, List<Feed>> lastValuesMap,
			Map<String, List<MeasureAggregation>> aggregations, 
			Map<String, Set<Date>> tickMap, 
			Map<String, List<DataPoint>> dataPointMap)
			throws RestServiceException {
		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();

		Date lower = new Date();
		Date upper = new Date(0);

		Date epochPast = cassandraService.getMeasures().getEpoch();
		Calendar calendar = cassandraService.getMeasures().getCalendarUTC();
		calendar.add(Calendar.DATE, 1);
		Date epochFuture = calendar.getTime();
		Map<String, Set<String>> map = new HashMap<>();
		for (DeviceData deviceData : dataSet.getValues()) {
			if (deviceData.getSerial() == null) {
				deviceData.setSerial(main.getSerial());
			}
			Device device = devices.get(deviceData.getSerial());
			if (device == null) {
				device = deviceService.findDeviceCacheable(deviceData.getSerial());
				if (device == null) {
					throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
							Constants.Error.INVALID_DATA_ERROR_CODE,
							"datapoint with serial " + deviceData.getSerial() + " device not found");
				}
				devices.put(deviceData.getSerial(), device);
			}
			Set<String> ids = map.get(deviceData.getSerial());
			if (ids == null) {
				ids = new HashSet<>();
				map.put(deviceData.getSerial(), ids);
			}
			if (deviceData.getId() != null) {
				ids.add(deviceData.getId());
			} else if (deviceData.getIds() != null) {
				ids.addAll(Arrays.asList(deviceData.getIds()));
			}
		}

		for (String sn : map.keySet()) {
			Device device = devices.get(sn);
			Set<String> ids = map.get(sn);
			List<Feed> lastValues = loadActiveFeeds(device, ids);
			lastValuesMap.put(sn, lastValues);
			tickMap.put(sn, new HashSet<Date>());
			if (!events.containsKey(sn)) {
				events.put(sn, new ArrayList<MeasureRaw>());
			}
		}

		for (DeviceData deviceData : dataSet.getValues()) {
			boolean partial = deviceData.getPartial() != null;
			Device device = devices.get(deviceData.getSerial());
			TimeZone tz = InterpolationUtils.getTimeZone(device.getTimeZone());
			long millis = dataSet.getTimestamp() * 1000;
			Date timestamp = InterpolationUtils.removeTimeZoneOffset(millis, tz);

			/*
			 * validate timestamp
			 */
			if (device.hasRtc()) {
				if (timestamp.after(epochFuture)) {
					String msg = String.format("device %s rtc enabled, invalid future timestamp",
							deviceData.getSerial());
					throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
							Constants.Error.INVALID_DATA_ERROR_CODE, msg);
				}

				if (timestamp.before(epochPast)) {
					String msg = String.format("device %s rtc enabled, invalid passed timestamp",
							deviceData.getSerial());
					throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
							Constants.Error.INVALID_DATA_ERROR_CODE, msg);
				}
			} else {
				if (timestamp.after(epochFuture) || timestamp.before(epochPast)) {
					logger.warn("device {} rtc disabled, changed invalid timestamp", deviceData.getSerial());
					timestamp = new Date();
				}
			}

			tickMap.get(device.getSerial()).add(timestamp);
			lower = timestamp;
			upper = timestamp;

			/**
			 * create array with measure id if not defined
			 */
			if (deviceData.getId() != null) {
				if (deviceData.getIds() != null) {
					throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
							Constants.Error.INVALID_DATA_ERROR_CODE, "datapoint with ambiguous id/ids definition");
				}
				if (dataSet.getStep() < 0) {
					throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
							Constants.Error.INVALID_DATA_ERROR_CODE, "having id but missing step value");
				}
				deviceData.setIds(new String[deviceData.getValue().length]);
				for (int i = 0; i < deviceData.getValue().length; i++) {
					deviceData.getIds()[i] = deviceData.getId();
				}
			}

			if (!partial) {
				if (deviceData.getIds() == null || deviceData.getIds().length == 0) {
					// no data, only MeasureTick will be inserted
					continue;
				}
			} else {
				if (deviceData.getIds() == null) {
					deviceData.setIds(new String[0]);
					deviceData.setValue(new float[0]);
				}
			}

			if (deviceData.getValue() == null) {
				String msg = "datapoint values null with serial" + deviceData.getSerial();
				throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
						Constants.Error.INVALID_DATA_ERROR_CODE, msg);
			} else if (deviceData.getValue().length != deviceData.getIds().length) {
				String msg = String.format("datapoint ids [%d] / values[%d] have different length with serial %s",
						deviceData.getIds().length, deviceData.getValue().length, deviceData.getSerial());
				throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
						Constants.Error.INVALID_DATA_ERROR_CODE, msg);
			}

			List<Feed> lastValues = lastValuesMap.get(deviceData.getSerial());
			//
			Map<String, Feed> params = new HashMap<String, Feed>();
			//

			for (Feed feed : lastValues) {
				params.put(feed.getIdentifier(), feed);
			}

//			List<Feed> activeFeeds = device.activeFeeds();
//			if (activeFeeds.isEmpty()) {
//				device = deviceService.findBySerial(device.getSerial());
//				activeFeeds = device.activeFeeds();
//				logger.error("{} found {} active params after forced configuration read",device.getSerial(),  activeFeeds.size());
//				
//			}
//			for (Feed feed : activeFeeds) {
//				if (!params.containsKey(feed.getIdentifier())) {
//					params.put(feed.getIdentifier(), feed);
//				}
//			}

			/**
			 * create array with measure timestamp
			 */
			long[] ts = timestamps(timestamp.getTime() / 1000, deviceData.getIds().length, dataSet.getStep());
			List<String> invalid = new ArrayList<String>();
			List<DataPoint> dataPoints = new ArrayList<>();
			for (int idx = ts.length - 1; idx >= 0; idx--) {
				Feed param = params.get(deviceData.getIds()[idx]);
				if (param != null) {
					if (param.getDate() == null) {
						param.setDate(new Date(0));
					}
					if (param.getValue() == null) {
						param.setValue(Float.NaN);
					}
					String key = param.getKey();
					Date date = InterpolationUtils.removeTimeZoneOffset(ts[idx], tz);
					Float value = deviceData.getValue()[idx];
					Date received = new Date(dataSet.getTimestamp() * 1000);
					String error = "";

					if (date.before(lower)) {
						lower = date;
					}
					if (date.after(upper)) {
						upper = date;
					}

					if (dataSet.getOverwrite_tr() > 0) {
						received = new Date(dataSet.getOverwrite_tr() * 1000);
					}

					if (deviceData.getError() != null && deviceData.getError().length > idx) {
						if ((deviceData.getError()[idx] != null) && !deviceData.getError()[idx].equals("0")) {
							error = StringUtils.trim(deviceData.getError()[idx]);
						}
					}
					if (!error.isEmpty()) {
						value = param.getValue();
					}
					if (date.after(param.getDate())) {
						if (!equals(value, param.getValue())) {
							MeasureRaw measure = new MeasureRaw(key, value, date, received, error);
							measures.add(measure);
							if (dataSet.getStep() > 0) {
								tickMap.get(device.getSerial()).add(date);
							}
							if (param.isAlarmed()) {
								events.get(device.getSerial()).add(measure);
							}
						}
						param.setChanged(true);
						param.setError(error);
						param.setDate(date);
						param.setValue(value);
					} else {
						measures.add(new MeasureRaw(key, value, date, received, error));
					}

					DataPoint dp = new DataPoint();
					dp.setId(param.getRegisterId());
					dp.setValue(value);
					dataPoints.add(dp);

				} else {
					invalid.add(deviceData.getIds()[idx]);
				}
			}
			if (!invalid.isEmpty()) {
				// Change Request #1697
				String ids = String.join(",", invalid);
				String msg = String.format("device %s, params not found or inactive: %s", device.getSerial(), ids);
				// all parameters are missing
				if (invalid.size() == lastValues.size()) {
					throw new RestServiceException(INVALID_DATA_STATUS.getStatusCode(),
							Constants.Error.DEVICE_WRITE_PARAMETERS_ERROR_CODE, msg);
				} else {
					tracingService.traceRestError(ids, main.getOwner(), null, main.getSerial(), msg, null);
				}
			}

			// Feature #2162
			// dataPoints are stored to facilitate easy extraction of data into CSV or Excel formats
			List<CDataPoint> cborDataPoints = dataPoints.stream().map(dp -> {
				CDataPoint cdp = new CDataPoint();
				cdp.setId(dp.getId());
				cdp.setValue(dp.getValue());
				return cdp;
			}).collect(Collectors.toList());

			List<DataPoint> values = cassandraService.insertDataPoints(device, timestamp, cborDataPoints).stream().map(dp -> {
				DataPoint value = new DataPoint();
				value.setId(dp.getId());
				value.setValue(dp.getValue());
				return value;
			}).collect(Collectors.toList());
			dataPointMap.put(device.getSerial(), values);

		}

		try {
			for (String key : tickMap.keySet()) {
				Set<Date> set = tickMap.get(key);
				List<Date> sortedTicks = new ArrayList<Date>(set);
				Collections.sort(sortedTicks);
				cassandraService.getMeasures().insertTickBatch(key, sortedTicks);
			}

			cassandraService.getMeasures().insertRawBatch(measures);

		} catch (BackendServiceException e) {
			throw new RestServiceException(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
					Constants.Error.GENERIC_APP_ERROR_CODE, e.getMessage());
		}

		if (!lower.after(upper)) {
			Range<Date> interval = Range.closedOpen(lower, upper);
			for (String sn : map.keySet()) {
				List<Feed> lastValues = lastValuesMap.get(sn);
				if (!aggregations.containsKey(sn)) {
					aggregations.put(sn, new ArrayList<MeasureAggregation>());
				}
				for (Feed feed : lastValues) {
					MeasureAggregation aggregation = new MeasureAggregation(feed.getKey(), interval);
					aggregation.setSerial(feed.getSerial());
					aggregation.setChanged(feed.isChanged());
					aggregations.get(sn).add(aggregation);
				}
			}
		}

	}

	/*
	 * Nel caso in cui si dovessero comunicare misure il cui spaziamento temporale è
	 * minore di un secondo questa chiave contiene la spaziatura temporale tra i
	 * valori contenuti nella chiave v. I valori verranno quindi salvati con un
	 * timestamp che segue la regola: il primo datapoint è temporalmente posizionato
	 * all'instante indicato dal timestamp (ts) del dataset, il secondo all'istante
	 * ts - 1*step, il terzo timestamo - 2*step e così via (valore espresso in
	 * secondi)
	 */

	private long[] timestamps(long seconds, int length, float step) {
		long[] ts = new long[length];
		long millis = seconds * 1000;
		for (int i = 0; i < ts.length; i++) {
			if (step > 0f) {
				ts[i] = millis + (long) (step * 1000 * (-i));
			} else {
				ts[i] = millis;
			}
		}
		return ts;
	}

	private List<Feed> loadActiveFeeds(Device device, Set<String> ids) {
		// Feature #2139
		List<Channel> channels = device.getChannels().stream().filter(o -> ids.contains(o.getNumber()))
				.collect(Collectors.toList());
		if (channels.size() < ids.size()) {
			cacheManager.getCache(Constants.Cache.DEVICE).evict(device.getSerial());
			device = deviceService.findDeviceCacheable(device.getSerial());
			logger.error("{} reset of cached configuration (mis-matched param size {} < {}) {}", device.getSerial(),
					channels.size(), ids.size(), ids);
		}

		List<Feed> activeFeeds = cassandraService.activeFeeds(device);
		activeFeeds = activeFeeds.stream().filter(o -> ids.contains(o.getIdentifier())).collect(Collectors.toList());

		Set<String> keys = activeFeeds.stream().map(o -> o.getKey()).collect(Collectors.toSet());
		List<Feed> values = cassandraService.getFeeds().getFeedsValues(device.getSerial(), keys);
		for (Feed feed : values) {
			Optional<Feed> match = activeFeeds.stream().filter(o -> feed.getKey().equals(o.getKey())).findFirst();
			if (match.isPresent()) {
				match.get().setDate(feed.getDate());
				match.get().setValue(feed.getValue());
			}
		}
		return activeFeeds;
	}

	private boolean equals(Float number1, Float number2) {
		// Bug #1315
		return number1.equals(number2);
		// float epsilon = 0.00001f;
		// return (Math.abs(round(number1, 6) - round(number2, 6)) < epsilon);
	}

//	private void logOperation(String sn, String operation, long startTime) {
//		long endTime = System.currentTimeMillis() - startTime;
//		// flogger.info("/iotter/rest/v1/device/{}/data {} {} ms.", sn, operation,
//		// endTime);
//	}

//	private Device getDevice(String serial) {
//		if (serial.contains("-")) {
//			String[] parts = StringUtils.split(serial, "-");
//			String prefix = String.format("%s-%s", parts[0], parts[1]);
//			Device master = deviceService.findCacheable(parts[0]);
//			List<Device> slaves = deviceService.findSlaves(master);
//			for (Device slave : slaves) {
//				if (slave.getSerial().startsWith(prefix)) {
//					return slave;
//				}
//			}
//		}
//		return deviceService.findCacheable(serial);
//	}

	private String getApiKey(Device device) {
		Device master = device.getMaster();
		if (master == null) {
			return device.getActivationKey();
		}
		return master.getActivationKey();
	}

	private DataResultSet readExport(Device device, ExportQuery query) {
		DataResultSet result = new DataResultSet();
		ServiceableRetrieval batch = new ServiceableRetrieval();
		batch.setStart(query.getStart());
		batch.setQid(query.getQid());
		batch.setTotal(query.getTotal());
		batch.setExpires(query.getExpires().getTime() / 1000);
		List<ExportRow> rows = cassandraService.getExport().loadRows(query);
		if (query.isSingleBatch()) {
			batch.setTotal((long) rows.size());
			batch.setNext((long) -1);
		} else {
			batch.setNext(query.getNext());
		}
		batch.setBatchSize(rows.size());
		result.setFrom(query.getFrom().getTime() / 1000);
		result.setTo(query.getTo().getTime() / 1000);
		result.setBatch(batch);
		Map<String, Feed> map = feedMap(device, query.getKeys());
		for (String key : query.getKeys()) {
			Feed feed = map.get(key);
			if (feed != null) {
				DataPoint data = new DataPoint();
				data.setId(feed.getIdentifier());
				data.setLabel(feed.getLabel());
				data.setQual(feed.getQualifier());
				data.setUnit(feed.getUnit());
				data.setTypeVar(feed.getTypeVar());
				data.setScale(feed.getScale());
				result.getValues().add(data);
			}
		}
		for (ExportRow row : rows) {
			if (row != null) {
				DataRow data = new DataRow();
				data.setTimestamp(row.getTimestamp().getTime() / 1000);
				for (int i = 0; i < row.getValues().size(); i++) {
					String key = query.getKeys().get(i);
					Float raw = row.getValues().get(i);
					Feed feed = map.get(key);
					if (feed != null) {
						Float value = feed.getMeasureUnit().convert(raw);
						data.getValues().add(value);
					} else {
						data.getValues().add(null);
					}
				}
				result.getRows().add(data);
			}
		}

		Date lastContact = cassandraService.getFeeds().getLastContact(device.getSerial());
		if (lastContact != null) {
			result.setLastContact(lastContact.getTime() / 1000);
		}

		return result;
	}

	private ExportQuery readQuery(String qid, Device device, long from, long to, long start, Boolean ascending,
			List<String> id, String interpolation, String apiKey) throws RestServiceException {
		ExportQuery query = null;
		if (qid != null) {
			query = cassandraService.getExport().retrieveExportQuery(qid);
		} else {
			List<String> keys = feedKeys(id, device);
			if (keys.isEmpty()) {
				int status = Response.Status.NOT_ACCEPTABLE.getStatusCode();
				int code = Constants.Error.DEVICE_READ_PARAMETERS_ERROR_CODE;
				String message = String.format("read request for device with serial %s without valid ids ",
						device.getSerial());
				throw new RestServiceException(status, code, message);
			}
			query = cassandraService.getExport().buildExportQuery(device.getSerial(), keys, from, to, ascending,
					interpolation, apiKey);
		}

		if (query == null) {
			int status = Response.Status.NOT_ACCEPTABLE.getStatusCode();
			int code = Constants.Error.DEVICE_READ_PARAMETERS_ERROR_CODE;
			String message = String.format("read request for device with serial %s is expired", device.getSerial());
			throw new RestServiceException(status, code, message);
		}

		if (!query.isValid()) {
			int status = Response.Status.NOT_ACCEPTABLE.getStatusCode();
			int code = Constants.Error.DEVICE_READ_PARAMETERS_ERROR_CODE;
			String message = String.format("read request for device with serial %s is expired", device.getSerial());
			throw new RestServiceException(status, code, message);
		}

		if (query.getFrom().after(query.getTo())) {
			int status = Response.Status.NOT_ACCEPTABLE.getStatusCode();
			int code = Constants.Error.DEVICE_READ_PARAMETERS_ERROR_CODE;
			String message = String.format("read request for device with serial %s with invalid dates ",
					device.getSerial());
			throw new RestServiceException(status, code, message);
		}

		if (query.isSingleBatch()) {
			query.setStart(0l);
			return query;
		}

		if (query.getTotal() == null) {
			query.setStart(0l);
			cassandraService.getExport().writeTimeStamps(query);
		} else {
			if (start != -1) {
				query.setStart(start);
			} else {
			}
		}
		return query;
	}

	private Map<String, Feed> feedMap(Device device, List<String> keys) {
		Map<String, Feed> map = new HashMap<String, Feed>();
		for (Channel channel : device.getChannels()) {
			if (keys.contains(channel.getKey())) {
				map.put(channel.getKey(), cassandraService.buildFeed(channel));
			}
		}
		return map;
	}

	private DataResultSet readLastValues(Device device, List<String> keys) {
		List<Feed> feeds = cassandraService.getFeeds().getFeedsValues(device.getSerial(), new HashSet<String>(keys));
		long from = System.currentTimeMillis() / 1000;
		long to = 0;
		DataResultSet result = CassandraService.createDataResultSet(feeds, device, from, to);
		Date lastContact = cassandraService.getFeeds().getLastContact(device.getSerial());
		if (lastContact != null) {
			result.setLastContact(lastContact.getTime() / 1000);
		}
		return result;
	}

	private List<String> feedKeys(List<String> ids, Device device) {
		// LinkedHashSet It remembers the order in which the elements were
		// inserted into the set, and returns its elements in that order.
		Set<String> keys = new LinkedHashSet<String>();
		boolean filtered = ids != null && !ids.isEmpty();
		List<Channel> sortedChannels = new ArrayList<>(device.getChannels());
		Collections.sort(sortedChannels, new ChannelComparator());
		if (filtered) {
			for (String id : ids) {
				for (Channel channel : sortedChannels) {
					String identifier = channelIdentifier(channel);
					if (identifier.startsWith(id)) {
						keys.add(channel.getKey());
						break;
					}
				}
			}
		} else {
			for (Channel channel : sortedChannels) {
				keys.add(channel.getKey());
			}
		}
		return new ArrayList<>(keys);
	}

	private String channelIdentifier(Channel channel) {
		String metadata = channel.getMetaData();
		if (metadata != null && //
				metadata.contains("|") && //
				channel.getOid() != null) {
			ModbusRegister register = ModbusRegister.parseMetadata(metadata);
			if (register != null && //
					register.getAddress() != null && //
					register.getTypeRead() != null) {
				StringWriter sw = new StringWriter();
				sw.append(register.getAddress().toString());
				sw.append(":");
				sw.append(register.getTypeRead().getShortName().toLowerCase());
				if (register.getBitmask() != null) {
					sw.append(":");
					sw.append(register.getBitmask());
				}
				return sw.toString();
			}
		}
		return channel.getNumber();
	}

}
