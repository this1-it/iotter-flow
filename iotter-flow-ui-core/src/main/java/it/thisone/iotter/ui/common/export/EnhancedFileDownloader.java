package it.thisone.iotter.ui.common.export;

import java.io.IOException;

import com.vaadin.server.ConnectorResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.AbstractComponent;

/**
 * an advanced file downloader
 * 
 * 
 * 
 */
public class EnhancedFileDownloader extends FileDownloader {

	public EnhancedFileDownloader(Resource resource) {
		super(resource);
	}

	@Override
    public void extend(AbstractComponent target) {
		extendedComponent = target;
        super.extend(target);
    }
	
	/**
     *
     */
	private static final long serialVersionUID = 7914516170514586601L;
	private AbstractComponent extendedComponent;
	private EnhancedDownloaderListener downloaderListener;
	private DownloaderEvent downloadEvent;
	
	public abstract class DownloaderEvent {
		/**
		 * 
		 * @return
		 */
		public abstract AbstractComponent getExtendedComponent();
		public abstract void setExtendedComponent(AbstractComponent extendedComponet);
	}

	public interface EnhancedDownloaderListener {
		/**
		 * This method will be invoked just before the download starts. Thus, a
		 * new file path can be set.
		 * 
		 * @param downloadEvent
		 */
		public void beforeDownload(DownloaderEvent downloadEvent);

	
		/**
		 * This method will be invoked just after the download end.
		 * 
		 * @param downloadEvent
		 */
		public void afterDownload(DownloaderEvent downloadEvent);
	
	
	}

	public void fireBeforeEvent() {
		if (this.downloaderListener != null && this.downloadEvent != null) {
			this.downloaderListener.beforeDownload(this.downloadEvent);
		}
	}
	public void fireAfterEvent() {
		if (this.downloaderListener != null && this.downloadEvent != null) {
			this.downloaderListener.afterDownload(this.downloadEvent);
		}
	}

	public void addAdvancedDownloaderListener(EnhancedDownloaderListener listener) {
		if (listener != null) {
			DownloaderEvent downloadEvent = new DownloaderEvent() {
				private AbstractComponent extendedComponent;
				@Override
				public void setExtendedComponent(AbstractComponent extendedComponet) {
					this.extendedComponent = extendedComponet;
				}
				@Override
				public AbstractComponent getExtendedComponent() {
					return this.extendedComponent;
				}
			};
			downloadEvent.setExtendedComponent(EnhancedFileDownloader.this.extendedComponent);
			this.downloaderListener = listener;
			this.downloadEvent = downloadEvent;
		}
	}




	@Override
	public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
		if (!path.matches("dl(/.*)?")) {
			// Ignore if it isn't for us
			return false;
		}
		VaadinSession session = getSession();
		session.lock();
		// create resource dinamically
		EnhancedFileDownloader.this.fireBeforeEvent();
		DownloadStream stream;
		try {
			Resource resource = getFileDownloadResource();
			if (!(resource instanceof ConnectorResource)) {
				return false;
			}
			stream = ((ConnectorResource) resource).getStream();
			if (stream.getParameter("Content-Disposition") == null) {
				// Content-Disposition: attachment generally forces download
				stream.setParameter("Content-Disposition", "attachment; filename=\"" + stream.getFileName() + "\"");
			}
			// Content-Type to block eager browser plug-ins from hijacking
			// the file
			if (isOverrideContentType()) {
				stream.setContentType("application/octet-stream;charset=UTF-8");
			}
		} finally {
			session.unlock();
		}
		stream.writeResponse(request, response);
		EnhancedFileDownloader.this.fireAfterEvent();
		return true;
	}
}