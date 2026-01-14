package it.thisone.iotter.ui.common.fields;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.ThemeResource;
import com.vaadin.flow.component.Alignment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.Button.ClickListener;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.themes.ValoTheme;

import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.common.UIUtils;
public class EmbeddedImageData extends CustomField<ImageData> {

	private static final long serialVersionUID = -4520882923454628875L;
	
	private ImageData currentValue;

	@Override
	public ImageData getValue() {
		return currentValue;
	}

	private Span label;
	private Button button;
	private VerticalLayout content;
	private PanelFlow panel;
	private Image image;

	public EmbeddedImageData() {
		image = new Image();
		panel = new PanelFlow();
		label = new Span("");
		// panel.setImmediate(true);
		image.setStyleName("embedded-image");
		panel.setContent(image);
		panel.setSizeFull();
		button = new Button(VaadinIcons.UPLOAD);
		button.addClassName(ValoTheme.BUTTON_PRIMARY);
		button.addClassName(ValoTheme.BUTTON_LARGE);
		button.setCaptionAsHtml(true);
		button.setSizeFull();
		
		HorizontalLayout top = new HorizontalLayout();
		top.setSpacing(true);
		top.addComponent(button);
		top.addComponent(label);
		
		content = new VerticalLayout();
		content.setSpacing(true);
		content.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		content.addComponent(top);
		content.addComponent(image);
		content.setExpandRatio(image, 1f);
	}

    public void addClickListener(ClickListener listener) {
    	button.addClickListener(listener);
    }


	@Override
	public void setWidth(float width, Unit unit) {
		if (content != null) {
			content.setWidth(width, unit);
		}
		super.setWidth(width, unit);
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
	protected void doSetValue(final ImageData data) {
		this.currentValue = data;
		if (data == null) {
			label.setValue(UIUtils.localize("groupwidgets.custommap.missing_image"));
			button.setIcon(VaadinIcons.UPLOAD);
			image.setSource(new ThemeResource("img/empty-image.png"));
			image.markAsDirty();
			image.markAsDirty();
			return;
		}
		try {
			StreamSource source = createStreamSource(data);
			image.setSource(new StreamResource(source, data.getFilename()));
			StreamResource streamSource = (StreamResource) image.getSource();
			BufferedImage bi = ImageIO.read(streamSource.getStream()
					.getStream());
			data.setWidth(bi.getWidth());
			data.setHeight(bi.getHeight());
			int size = (int)(data.getData().length * 0.001);
			String caption = String.format("<b>%s %d Kbytes<b/>", data.toString(),size);
			label.setValue(caption);
			button.setIcon(VaadinIcons.EDIT);
		} catch (Exception e) {
			image.setSource(new ThemeResource("img/empty-image.png"));
			image.markAsDirty();
			label.setValue(UIUtils.localize("groupwidgets.custommap.missing_image"));
			button.setIcon(VaadinIcons.UPLOAD);
			return;
		}
		image.markAsDirty();
	}


	@Override
	protected Component initContent() {
		return content;
	}






}
