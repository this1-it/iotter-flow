package it.thisone.iotter.ui.common.fields;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.vaadin.easyuploads.UploadField;
//import org.vaadin.easyuploads.UploadField.FieldType;
//import org.vaadin.easyuploads.UploadField.StorageMode;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.FileResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.flow.component.Alignment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Span;

import it.thisone.iotter.persistence.model.ResourceData;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.export.EnhancedFileDownloader;

// https://dev.vaadin.com/svn/incubator/EasyUploads/src/org/vaadin/easyuploads/tests/UploadfieldExampleApplication.java
public class EditableResourceData extends CustomField<ResourceData> {

	private static final String ACCEPT_FILTER = ".xlsx,.xls";

	private static final long serialVersionUID = -4520882923454628875L;

	final private HorizontalLayout content;
	final private Span resourceLabel;
	final private UploadField uploadField;
	final private Button download;
	private EnhancedFileDownloader fileDownloader;

	private static final String name = "editable.resource";

	public EditableResourceData() {
		content = new HorizontalLayout();
		content.setSizeFull();
		content.setSpacing(true);
		content.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		// content.setImmediate(true);
		resourceLabel = new Span(getI18nKey("upload_resource"));
		resourceLabel.setSizeUndefined();
		uploadField = new UploadField();
		// {
		// 	private static final long serialVersionUID = 1L;
		// 	@Override
		// 	protected void updateDisplay() {
		// 		ResourceData resource = new ResourceData();
		// 		resource.setData((byte[]) getValue());
		// 		resource.setFilename(getLastFileName());
		// 		resource.setMimetype(getLastMimeType());
		// 		setInternalValue(resource);
		// 	}
		// };
		// uploadField.setStorageMode(StorageMode.MEMORY);
		// uploadField.setFieldType(FieldType.BYTE_ARRAY);
		uploadField.setAcceptFilter(ACCEPT_FILTER);
		uploadField.setButtonCaption(getI18nKey("choose_file"));
		
		// Add listener to handle file uploads
		uploadField.addValueChangeListener(event -> {
			if (uploadField.getValue() != null && uploadField.getLastFileName() != null) {
				ResourceData resource = new ResourceData();
				resource.setData((byte[]) uploadField.getValue());
				resource.setFilename(uploadField.getLastFileName());
				resource.setMimetype(uploadField.getLastMimeType());
				// Trigger value change in the custom field
				setValue(resource);
			}
		});

		download = new Button(VaadinIcons.DOWNLOAD);
		download.setVisible(false);

		content.addComponent(resourceLabel);
		content.addComponent(uploadField);
		content.addComponent(download);

	}

	public void clear() {
		setValue(null);
		uploadField.clear();
		resourceLabel.setValue(getI18nKey("upload_resource"));
		download.setVisible(false);
	}

	private ResourceData currentValue;

	@Override
	protected void doSetValue(final ResourceData resource) {
		this.currentValue = resource;
		if (resource == null || (resource != null && resource.getData().length == 0)) {
			resourceLabel.setValue(getI18nKey("upload_resource"));
			download.setVisible(false);
			return;
		}
		
		String caption = String.format("%s %s %d Bytes", resource.getFilename(), resource.getMimetype(),
				resource.getData().length);
		resourceLabel.setValue(caption);

		if (!resource.isNew()) {
			StreamResource stream = createStreamResource(resource);
			stream.setMIMEType(resource.getMimetype());
			stream.setBufferSize(resource.getData().length);
			fileDownloader = new EnhancedFileDownloader(createFileResource(resource));
			fileDownloader.setOverrideContentType(true);
			fileDownloader.extend(download);
			download.setVisible(true);
		}
	}

	@Override
	public ResourceData getValue() {
		return currentValue;
	}

	private StreamResource createStreamResource(final ResourceData resource) {
		return new StreamResource(new StreamSource() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public InputStream getStream() {

				return new ByteArrayInputStream(resource.getData());

			}
		}, resource.getFilename());
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		uploadField.setVisible(!readOnly);
	}

	@Override
	protected Component initContent() {
		return content;
	}


	public String getI18nKey(String key) {
		return UIUtils.localize(name + "." + key);
	}

	private FileResource createFileResource(final ResourceData resource) {
        final FileResource stream = new FileResource(new File("")) {
            @Override
            public DownloadStream getStream() {
                ByteArrayInputStream in = new ByteArrayInputStream(resource.getData());
                DownloadStream ds = new DownloadStream(in,
                		resource.getMimetype(), resource.getFilename());
                // Need a file download POPUP
                ds.setParameter("Content-Disposition","attachment; filename="+resource.getFilename());
                ds.setParameter("Content-Length", String.valueOf(resource.getData().length));
                ds.setCacheTime(getCacheTime());
                ds.setBufferSize(resource.getData().length);
                return ds;
            }
        };
        return stream;
    }

	public UploadField getUploadField() {
		return uploadField;
	}

	public Span getResourceLabel() {
		return resourceLabel;
	}
}