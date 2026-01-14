package it.thisone.iotter.quartz;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;

import it.thisone.iotter.integration.ExportMessage;
import it.thisone.iotter.integration.ExportService;
import it.thisone.iotter.integration.NotificationService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.service.DeviceService;

@Service
@DisallowConcurrentExecution
public class ExporterJob implements Job, Serializable {
// find ~/.m2/repository -type d -name "AERNET-2.3.0" -exec rm -rf {} +
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static Logger logger = LoggerFactory.getLogger(ExporterJob.class);

	public static Logger logger = LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);
	public static final String CHECK = "exporter";
	public static final String CHECK_GROUP = "exporter-quartz";

	@Autowired
	private ExportService exportService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private NotificationService notificationService;

	@Override
	public void execute(final JobExecutionContext ctx) throws JobExecutionException {

		long total = System.currentTimeMillis();
		DeviceCriteria criteria = new DeviceCriteria();
		criteria.setStatus(DeviceStatus.CONNECTED);
		criteria.setExporting(true);
		List<Device> exporting = deviceService.search(criteria, 0, 10000);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		Date givenDate = calendar.getTime();
		List<Device> filtered = exporting.stream()
				.filter(bean -> bean.getLastExportDate() == null || bean.getLastExportDate().before(givenDate))
				.sorted(Comparator.comparing(Device::getLastExportDate,
						Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());

		for (Device device : filtered) {
			String serial = device.getSerial();
			logger.info("Executing Job {} {} ", serial, ctx.getJobDetail());
			try {
				long duration = System.currentTimeMillis();
				ExportMessage message = exportService.exportData(device);
				if (message != null) {
					duration = System.currentTimeMillis() - duration;
					logger.info("Exported {} duration {} ", serial, InterpolationUtils.elapsed(duration));
					notificationService.forwardWeeklyExport(message);
				}
			} catch (Exception e) {
				logger.error("ExporterJob execution", e);
			}
		}

		total = System.currentTimeMillis() - total;
		logger.info("Exported devices {} duration {} ", exporting.size(), InterpolationUtils.elapsed(total));

	}

}