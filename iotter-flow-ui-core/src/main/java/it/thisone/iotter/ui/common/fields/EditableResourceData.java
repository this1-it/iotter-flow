package it.thisone.iotter.ui.common.fields;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.AbstractCompositeField;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.UI;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;

import org.vaadin.firitin.components.upload.UploadFileHandler;

import it.thisone.iotter.persistence.model.ResourceData;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.export.EnhancedFileDownloader;


public class EditableResourceData 
extends AbstractCompositeField<HorizontalLayout, EditableResourceData, ResourceData> {


	private static final String[] ACCEPTED_FILE_TYPES = new String[] { ".xlsx", ".xls" };
	private static final int BUFFER_SIZE = 8192;

	private static final long serialVersionUID = -4520882923454628875L;

	final private HorizontalLayout content;
	final private Span resourceLabel;
	final private UploadFileHandler uploadField;
	final private Button download;
	private EnhancedFileDownloader fileDownloader;

	private static final String name = "editable.resource";

	public EditableResourceData() {
		super(null);
		content = getContent();
		content.setSizeFull();
		content.setSpacing(true);
		
		resourceLabel = new Span(getI18nKey("upload_resource"));
		resourceLabel.setSizeUndefined();
		uploadField = new UploadFileHandler((contentStream, fileName, mimeType) -> {
			byte[] data = readBytes(contentStream);
			if (data == null || fileName == null) {
				return;
			}
			UI ui = getUI().orElse(null);
			if (ui != null) {
				ui.access(() -> setResourceValue(data, fileName, mimeType));
			}
		});
		uploadField.setAcceptedFileTypes(ACCEPTED_FILE_TYPES);
		uploadField.setMaxFiles(1);
		uploadField.setUploadButton(new Button(getI18nKey("choose_file")));

		download = new Button(VaadinIcon.DOWNLOAD.create());
		download.setVisible(false);

		content.add(resourceLabel);
		content.add(uploadField);
		content.add(download);

	}

	public void clear() {
		setValue(null);
		uploadField.getElement().executeJs("this.files = []");
		resourceLabel.setText(getI18nKey("upload_resource"));
		download.setVisible(false);
	}

	private ResourceData currentValue;

	@Override
	protected void setPresentationValue(final ResourceData resource) {
		this.currentValue = resource;
		if (resource == null || (resource != null && resource.getData().length == 0)) {
			resourceLabel.setText(getI18nKey("upload_resource"));
			download.setVisible(false);
			return;
		}
		
		String caption = String.format("%s %s %d Bytes", resource.getFilename(), resource.getMimetype(),
				resource.getData().length);
		resourceLabel.setText(caption);

		if (!resource.isNew()) {
			StreamResource stream = createStreamResource(resource);
			stream.setContentType(resource.getMimetype());
			stream.setHeader("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"");
			fileDownloader = new EnhancedFileDownloader(stream);
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
		return new StreamResource(resource.getFilename(),
				() -> new ByteArrayInputStream(resource.getData()));
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		uploadField.setVisible(!readOnly);
	}



	public String getI18nKey(String key) {
		return getTranslation(name + "." + key);
	}

	public UploadFileHandler getUploadField() {
		return uploadField;
	}

	public Span getResourceLabel() {
		return resourceLabel;
	}

	private void setResourceValue(byte[] data, String fileName, String mimeType) {
		ResourceData resource = new ResourceData();
		resource.setData(data);
		resource.setFilename(fileName);
		resource.setMimetype(mimeType);
		setValue(resource);
	}

	private byte[] readBytes(InputStream contentStream) {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[BUFFER_SIZE];
			int read;
			while ((read = contentStream.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return output.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}
}
