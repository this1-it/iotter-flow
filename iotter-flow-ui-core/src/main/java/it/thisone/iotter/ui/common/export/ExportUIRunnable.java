package it.thisone.iotter.ui.common.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification.Type;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;

import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exporter.IExportProvider;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.eventbus.ExportStartEvent;
import it.thisone.iotter.ui.uitask.UIRunnable;
import it.thisone.iotter.util.PopupNotification;

public class ExportUIRunnable implements UIRunnable {

	public static Logger logger = LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);

	private ExportStartEvent event;
	private File exported = null;
	private FileDownloader fileDownloader;
	private boolean alreadyLocked = false;
	
	public ExportUIRunnable(ExportStartEvent event) {
		super();
		this.event = event;
	}

	// do some slow processing here, but don't use any UI components.
	@Override
	public void runInBackground() {
		if (event.getConfig().getLockId() != null) {
			if (!UIUtils.getCassandraService().getRollup().lockSink(event.getConfig().getLockId(), 15 * 3600)) {
				alreadyLocked = true;
				return;
			}
		}
		logger.debug("start export {} - legacy: {} - owner: {} ",  event.getConfig().toString(), event.getProperties().isLegacy(), event.getOwner());
		long elapsed = System.currentTimeMillis();
		IExportProvider provider = UIUtils.getServiceFactory().getExportService();
		exported = provider.createExportDataFile(event.getConfig(), event.getProperties());
		UIUtils.getCassandraService().getRollup().unlockSink(event.getConfig().getLockId());
		elapsed = System.currentTimeMillis() - elapsed;
		logger.debug("end export {} elapsed: {}", event.getConfig().toString(), InterpolationUtils.elapsed(elapsed));
		if (exported != null && event.getEmail() != null && !event.getEmail().trim().isEmpty()) {
			UIUtils.getServiceFactory().getNotificationService().forwardVisualization(event.getEmail(), UI.getCurrent().getLocale(), exported, event.getConfig().getName());
		}
	}

	// do all UI changes based on the work done previously.
	@Override
	public void runInUI(Throwable ex) {
		if (alreadyLocked) {
			PopupNotification.show(UIUtils.localize("export.already_running_export") + " " + event.getConfig().getName(), Type.ERROR_MESSAGE);
			return;
		}
		
		if (exported == null) {
			UIUtils.getCassandraService().getRollup().unlockSink(event.getConfig().getLockId());
			PopupNotification.show(UIUtils.localize("export.failed_export") + " " + event.getConfig().getName(), Type.ERROR_MESSAGE);
			return;
		}
		
		
		Button lnkFile = new Button(event.getConfig().uniqueFileName(event.getProperties().getFileExtension()));
		lnkFile.setIcon(UIUtils.ICON_DOWNLOAD);
		lnkFile.setStyleName("link");
		try {
			StreamResource stream = createStreamResource(exported, event.getConfig().uniqueFileName(event.getProperties().getFileExtension()) );
			fileDownloader = new FileDownloader(stream);
			fileDownloader.extend(lnkFile);
			
		} catch (Exception e) {
			PopupNotification.show(UIUtils.localize("export.failed_export") + " " + event.getConfig().getName(), Type.ERROR_MESSAGE);
			return;
		}
		
		Dialog dialog = new Dialog();
		dialog.setHeaderTitle(UIUtils.localize("export.export_finished"));
		dialog.add(lnkFile);
		// dialog.addClassName("export-info");
		
		dialog.setDraggable(false);
		// dialog.setImmediate(true);
		dialog.open();
	}

	private StreamResource createStreamResource(final File file, String fileName) {
		StreamResource stream = new StreamResource(new StreamSource() {
			private static final long serialVersionUID = 3825453932246713909L;
			@Override
			public InputStream getStream() {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException e) {
				}
				return null;
			}
		}, fileName);
		return stream;
	}

}
