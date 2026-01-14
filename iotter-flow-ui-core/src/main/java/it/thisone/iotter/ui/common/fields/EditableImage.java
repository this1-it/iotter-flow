package it.thisone.iotter.ui.common.fields;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.ThemeResource;
import com.vaadin.flow.component.Alignment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.Button.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.Layout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Type;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.Upload.FailedEvent;
import com.vaadin.flow.component.upload.Upload.FailedListener;
import com.vaadin.flow.component.upload.Upload.ProgressListener;
import com.vaadin.flow.component.upload.Upload.Receiver;
import com.vaadin.flow.component.upload.Upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload.SucceededListener;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.themes.ValoTheme;

import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.common.EditorRemovedEvent;
import it.thisone.iotter.ui.common.EditorRemovedListener;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.EditorSavedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.PopupNotification;

// https://dev.vaadin.com/svn/doc/book-examples/branches/vaadin-7/src/com/vaadin/book/examples/component/CustomFieldExample.java
public class EditableImage extends CustomField<ImageData> implements Receiver,
		ProgressListener, FailedListener, SucceededListener {

	private static final long serialVersionUID = -4520882923454628875L;
	// Put upload in this memory buffer that grows automatically
	private ByteArrayOutputStream os = new ByteArrayOutputStream(10240);
	
	private ImageData currentValue;

	@Override
	public ImageData getValue() {
		return currentValue;
	}

	private VerticalLayout content;
	private VerticalLayout uploadLayout;
	private PanelFlow imagePanel;
	private Image image;
	private ProgressBar progress = new ProgressBar(0.0f);

	private ImageData newimage = null;
	private Button saveButton;
	private Button removeButton;
	private Button cancelButton;

	private static final String name = "editable.image";


	public String getWindowStyle() {
		return "editable-image";
	}


	public float[] getWindowDimension() {
		return new float[]{0.4f, 0.6f};
	}
	
	public EditableImage(ImageData imagedata) {
		image = new Image();
		imagePanel = new PanelFlow();
		content = new VerticalLayout();
		
		content.setSizeFull();
		content.setSpacing(true);
		content.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		// content.setImmediate(true);
		
		uploadLayout = new VerticalLayout();
		//uploadLayout.setPadding(true);
		uploadLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		
		Layout imageLayout = buildMainLayout();
		content.addComponent(imageLayout);
		content.addComponent(uploadLayout);
		content.addComponent(buildFooter());
		content.setExpandRatio(imageLayout, 1f);
		// Create the upload component and handle all its events
		buildUpload();
		// These also set the image so can't call before it's set
		setValue(imagedata);

	}

	
	public Layout buildMainLayout() {
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setPadding(true);
		content.addComponent(progress);
		progress.setVisible(false);
		content.addComponent(image);
		image.setStyleName("embedded-image");
		imagePanel.setContent(content);
		imagePanel.setSizeFull();
		HorizontalLayout layout = new HorizontalLayout();
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		layout.setSizeFull();
		layout.addComponent(imagePanel);
		layout.setPadding(true);
		return layout;
	}


	private void buildUpload() {
	    // this == Upload.Receiver (and likely ProgressListener / FailedListener / SucceededListener)
	    final Upload upload = new Upload(getI18nKey("upload_new_image"), this);

	    // Start upload as soon as a file is selected (replaces ChangeListener + submitUpload())
	    upload.setImmediateMode(true);

	    // Optional: hide the button caption (same API in V8)
	    upload.setButtonCaption(null);

	    // Keep your listeners (same types in V8)
	    upload.addProgressListener(this);
	    upload.addFailedListener(this);
	    upload.addSucceededListener(this);

	    // If you previously used ChangeListener to detect selection,
	    // you can also hook into the start event if you need extra logic:
	    // upload.addStartedListener(e -> { /* e.getFilename(), e.getMIMEType(), etc. */ });

	    if (uploadLayout.getComponentCount() > 0) {
	        uploadLayout.removeAllComponents();
	    }
	    uploadLayout.addComponent(upload);
	}

	
	
	/* This should be static method */
	private StreamSource createStreamSource(final ImageData imageData) {
		return new StreamSource() {
			private static final long serialVersionUID = -4905654404647215809L;
			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(imageData.getData());
			}
		};
	}

	@Override
	protected void doSetValue(final ImageData newValue) {
		this.currentValue = newValue;
		if (newValue == null || (newValue!= null && newValue.getData().length ==0)) {
			image.setSource(new ThemeResource("img/empty-image.png"));
			image.markAsDirty();
			saveButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;
		}
		int size = 0;
		try {
			StreamSource source = createStreamSource(newValue);
			image.setSource(new StreamResource(source, newValue.getFilename()));
			StreamResource streamSource = (StreamResource) image.getSource();
			BufferedImage bi = ImageIO.read(streamSource.getStream()
					.getStream());
			newValue.setWidth(bi.getWidth());
			newValue.setHeight(bi.getHeight());
			size = (int)(newValue.getData().length * 0.001);
			String caption = String.format("%s %d KBytes",newValue.toString(),size);
			imagePanel.setLabel(caption);
		} catch (Exception e) {
			image.setSource(new ThemeResource("img/empty-image.png"));
			image.markAsDirty();
			imagePanel.setLabel(null);
			PopupNotification.show(getI18nKey("upload_failed"), Type.ERROR_MESSAGE);
			return;
		}
		image.markAsDirty();
		saveButton.setEnabled(true);
		removeButton.setEnabled(true);
		
		if (size > 1024) {
			saveButton.setEnabled(false);
			PopupNotification.show(getI18nKey("upload_too_big"), Type.ERROR_MESSAGE);
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		uploadLayout.setVisible(!readOnly);
	}

	@Override
	protected Component initContent() {
		return content;
	}


	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		newimage = new ImageData();
		newimage.setFilename(filename);
		newimage.setMimetype(mimeType);
		os.reset(); // Needed to allow re-uploading
		return os;
	}

	@Override
	public void updateProgress(long readBytes, long contentLength) {
		progress.setVisible(true);
		if (contentLength == -1)
			progress.setIndeterminate(true);
		else {
			progress.setIndeterminate(false);
			progress.setValue(((float) readBytes) / ((float) contentLength));
		}
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		newimage.setData(os.toByteArray());
		setValue(newimage);
		progress.setVisible(false);
		// Create the upload component and handle all its events
		buildUpload();
	}

	@Override
	public void uploadFailed(FailedEvent event) {
		PopupNotification.show(getI18nKey("upload_failed"), Notification.Type.ERROR_MESSAGE);
	}

	private Layout buildFooter() {
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		saveButton = createSaveButton();
		buttonLayout.addComponent(saveButton);
		buttonLayout.setExpandRatio(saveButton, 1);
		buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);

		removeButton = createRemoveButton();
		buttonLayout.addComponent(removeButton);
		buttonLayout.setExpandRatio(removeButton, 1);
		buttonLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);

		cancelButton = createCancelButton();
		buttonLayout.addComponent(cancelButton);
		buttonLayout.setExpandRatio(cancelButton, 1);
		buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		HorizontalLayout footer = new HorizontalLayout();
		footer.addClassName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		footer.setWidth(100.0f, Unit.PERCENTAGE);
		footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		footer.addComponent(buttonLayout);
		footer.setExpandRatio(buttonLayout, 1f);
		
		
		return footer;
	}

	@SuppressWarnings("serial")
	protected Button createSaveButton() {
		Button button = new Button(getI18nKey("save_button"));

		button.setIcon(VaadinIcons.FILE_TEXT);
		button.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		
		
		button.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ImageData imageData = getValue();
				fireEvent(new EditorSavedEvent<ImageData>(EditableImage.this, imageData));
			}
		});
		return button;
	}

	@SuppressWarnings("serial")
	protected Button createRemoveButton() {
		Button button = new Button(getI18nKey("remove_button"));
		
		button.setIcon(VaadinIcons.TRASH);
		button.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		
		button.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				fireEvent(new EditorSavedEvent<ImageData>(EditableImage.this, new ImageData()));
			}
		});
		return button;
	}

	@SuppressWarnings("serial")
	protected Button createCancelButton() {
		Button button = new Button(getI18nKey("cancel_button"));
		button.setIcon(VaadinIcons.CLOSE);
		button.setStyleName(ValoTheme.BUTTON_ICON_ONLY);


		button.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				fireEvent(new EditorSavedEvent(EditableImage.this, null));
			}
		});
		return button;
	}

	public void addListener(EditorSavedListener listener) {
		try {
			Method method = EditorSavedListener.class.getDeclaredMethod(
					EditorSavedListener.EDITOR_SAVED,
					new Class[] { EditorSavedEvent.class });
			addListener(EditorSavedEvent.class, listener, method);
		} catch (final java.lang.NoSuchMethodException e) {
			throw new java.lang.RuntimeException(
					"Internal error, editor saved method not found");
		}
	}

	public void removeListener(EditorSavedListener listener) {
		removeListener(EditorSavedEvent.class, listener);
	}

	public void addListener(EditorRemovedListener listener) {
		try {
			Method method = EditorRemovedListener.class.getDeclaredMethod(
					EditorRemovedListener.DETAILS_REMOVED,
					new Class[] { EditorRemovedEvent.class });
			addListener(EditorRemovedEvent.class, listener, method);
		} catch (final java.lang.NoSuchMethodException e) {
			throw new java.lang.RuntimeException(
					"Internal error, editor saved method not found");
		}
	}

	public void removeListener(EditorRemovedListener listener) {
		removeListener(EditorRemovedEvent.class, listener);
	}

	public String getI18nKey(String key) {
		return UIUtils.localize(name + "." + key);
	}

}
