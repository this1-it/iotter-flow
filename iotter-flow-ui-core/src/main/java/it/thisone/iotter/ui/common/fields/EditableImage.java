package it.thisone.iotter.ui.common.fields;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Image;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.FailedEvent;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.ProgressUpdateEvent;
import com.vaadin.flow.server.StreamResource;



import com.vaadin.flow.component.orderedlayout.VerticalLayout;


import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.PopupNotification;

public class EditableImage 
extends AbstractCompositeField<VerticalLayout, EditableImage, ImageData>
implements Receiver {

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
	private ProgressBar progress = new ProgressBar();

	private ImageData newimage = null;
	private Button saveButton;
	private Button removeButton;
	private Button cancelButton;

	private static final String name = "editable.image";



	
	public EditableImage(ImageData imagedata) {
		super(imagedata);
		VerticalLayout content = getContent();
		
		content.setSizeFull();
		content.setSpacing(true);
		// content.setImmediate(true);
		
		image = new Image();
		imagePanel = new PanelFlow();
		uploadLayout = new VerticalLayout();
		
		HorizontalLayout imageLayout = buildMainLayout();
		content.add(imageLayout);
		content.add(uploadLayout);
		content.add(buildFooter());

		// Create the upload component and handle all its events
		buildUpload();


	}

	
	public HorizontalLayout buildMainLayout() {
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setPadding(true);
		content.add(progress);
		progress.setVisible(false);
		content.add(image);
	
		imagePanel.setContent(content);
		imagePanel.setSizeFull();
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.add(imagePanel);
		layout.setPadding(true);
		return layout;
	}


	private void buildUpload() {
	    
	    final Upload upload = new Upload(this);


	    
	    upload.addProgressListener(this::handleProgress);
	    upload.addFailedListener(this::handleFailed);
	    upload.addSucceededListener(this::handleSucceeded);

	    // If you previously used ChangeListener to detect selection,
	    // you can also hook into the start event if you need extra logic:
	    // upload.addStartedListener(e -> { /* e.getFilename(), e.getMIMEType(), etc. */ });

	    if (uploadLayout.getComponentCount() > 0) {
	        uploadLayout.removeAll();
	    }
	    uploadLayout.add(upload);
	}

	
	
	private StreamResource createStreamResource(final ImageData imageData) {
		return new StreamResource(imageData.getFilename(),
				() -> new ByteArrayInputStream(imageData.getData()));
	}

	@Override
	protected void setPresentationValue(final ImageData newValue) {
		this.currentValue = newValue;
		if (newValue == null || (newValue!= null && newValue.getData().length ==0)) {
			//image.setSource(new ThemeResource("img/empty-image.png"));
			saveButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;
		}
		int size = 0;
		try {
			image.setSrc(createStreamResource(newValue));
			BufferedImage bi = ImageIO.read(new ByteArrayInputStream(newValue.getData()));
			newValue.setWidth(bi.getWidth());
			newValue.setHeight(bi.getHeight());
			size = (int)(newValue.getData().length * 0.001);
			String caption = String.format("%s %d KBytes",newValue.toString(),size);
			imagePanel.setCaption(caption);
		} catch (Exception e) {
			//image.setSource(new ThemeResource("img/empty-image.png"));

			imagePanel.setCaption(null);
			PopupNotification.show(getI18nKey("upload_failed"), PopupNotification.Type.ERROR);
			return;
		}

		saveButton.setEnabled(true);
		removeButton.setEnabled(true);
		
		if (size > 1024) {
			saveButton.setEnabled(false);
			PopupNotification.show(getI18nKey("upload_too_big"), PopupNotification.Type.ERROR);
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		uploadLayout.setVisible(!readOnly);
	}




	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		newimage = new ImageData();
		newimage.setFilename(filename);
		newimage.setMimetype(mimeType);
		os.reset(); // Needed to allow re-uploading
		return os;
	}

	private void handleProgress(ProgressUpdateEvent event) {
		progress.setVisible(true);
		if (event.getContentLength() == -1) {
			progress.setIndeterminate(true);
		} else {
			progress.setIndeterminate(false);
			progress.setValue(((float) event.getReadBytes()) / ((float) event.getContentLength()));
		}
	}

	private void handleSucceeded(SucceededEvent event) {
		newimage.setData(os.toByteArray());
		setValue(newimage);
		progress.setVisible(false);
		// Create the upload component and handle all its events
		buildUpload();
	}

	private void handleFailed(FailedEvent event) {
		progress.setVisible(false);
		PopupNotification.show(getI18nKey("upload_failed"), PopupNotification.Type.ERROR);
	}

	private HorizontalLayout buildFooter() {
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		saveButton = createSaveButton();
		buttonLayout.add(saveButton);

		removeButton = createRemoveButton();
		buttonLayout.add(removeButton);

		cancelButton = createCancelButton();
		buttonLayout.add(cancelButton);

		HorizontalLayout footer = new HorizontalLayout();
		footer.setWidth(100.0f, Unit.PERCENTAGE);
		footer.add(buttonLayout);
		
		
		return footer;
	}

	@SuppressWarnings("serial")
	protected Button createSaveButton() {
		Button button = new Button(getI18nKey("save_button"));

		button.setIcon(VaadinIcon.FILE_TEXT.create());
		button.addClickListener(event -> {

				ImageData imageData = getValue();
				fireEvent(new EditorSavedEvent<ImageData>(EditableImage.this, imageData));
			}
		);
		return button;
	}

	@SuppressWarnings("serial")
	protected Button createRemoveButton() {
		Button button = new Button(getI18nKey("remove_button"));
		
		button.setIcon(VaadinIcon.TRASH.create());
		

		button.addClickListener(event -> {

				fireEvent(new EditorSavedEvent<ImageData>(EditableImage.this, new ImageData()));
			}
		);
		return button;
	}

	@SuppressWarnings("serial")
	protected Button createCancelButton() {
		Button button = new Button(getI18nKey("cancel_button"));
		button.setIcon(VaadinIcon.CLOSE.create());


		button.addClickListener(event -> fireEvent(new EditorSavedEvent<ImageData>(EditableImage.this, null)));
		return button;
	}



	public String getI18nKey(String key) {
		return UIUtils.localize(name + "." + key);
	}

}
