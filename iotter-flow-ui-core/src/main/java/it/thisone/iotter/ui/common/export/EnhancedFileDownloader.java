package it.thisone.iotter.ui.common.export;

import java.io.Serializable;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

/**
 * an advanced file downloader
 * 
 * 
 * 
 */
public class EnhancedFileDownloader implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 7914516170514586601L;
	private Component extendedComponent;
	private EnhancedDownloaderListener downloaderListener;
	private DownloaderEvent downloadEvent;
	private AbstractStreamResource resource;
	private StreamRegistration registration;
	private boolean overrideContentType;

	public EnhancedFileDownloader(AbstractStreamResource resource) {
		this.resource = resource;
	}

    public void extend(Component target) {
		extendedComponent = target;
		if (downloadEvent != null) {
			downloadEvent.setExtendedComponent(target);
		}
		registerClickListener(target);
    }
	
	public abstract class DownloaderEvent {
		/**
		 * 
		 * @return
		 */
		public abstract Component getExtendedComponent();
		public abstract void setExtendedComponent(Component extendedComponet);
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
				private Component extendedComponent;
				@Override
				public void setExtendedComponent(Component extendedComponet) {
					this.extendedComponent = extendedComponet;
				}
				@Override
				public Component getExtendedComponent() {
					return this.extendedComponent;
				}
			};
			downloadEvent.setExtendedComponent(EnhancedFileDownloader.this.extendedComponent);
			this.downloaderListener = listener;
			this.downloadEvent = downloadEvent;
		}
	}

	public AbstractStreamResource getFileDownloadResource() {
		return resource;
	}

	public void setFileDownloadResource(AbstractStreamResource resource) {
		this.resource = resource;
	}

	public void setOverrideContentType(boolean overrideContentType) {
		this.overrideContentType = overrideContentType;
	}

	private void registerClickListener(Component target) {
		if (target instanceof ClickNotifier) {
			@SuppressWarnings("unchecked")
			ClickNotifier<Component> notifier = (ClickNotifier<Component>) target;
			notifier.addClickListener(event -> triggerDownload());
			return;
		}
		target.getElement().addEventListener("click", event -> triggerDownload());
	}

	private void triggerDownload() {
		if (resource == null) {
			return;
		}
		fireBeforeEvent();
		if (resource instanceof StreamResource) {
			StreamResource streamResource = (StreamResource) resource;
			streamResource.setHeader("Content-Disposition",
					"attachment; filename=\"" + streamResource.getName() + "\"");
			if (overrideContentType) {
				streamResource.setContentType("application/octet-stream;charset=UTF-8");
			}
		}

		VaadinSession session = VaadinSession.getCurrent();
		session.lock();
		try {
			if (registration != null) {
				registration.unregister();
			}
			registration = session.getResourceRegistry().registerResource(resource);
		} finally {
			session.unlock();
		}

		UI.getCurrent().getPage().open(registration.getResourceUri().toString(), "_blank");
		fireAfterEvent();
	}
}
