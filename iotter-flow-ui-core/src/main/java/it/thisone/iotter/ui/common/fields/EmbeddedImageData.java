package it.thisone.iotter.ui.common.fields;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import com.vaadin.flow.component.icon.VaadinIcon;

import com.vaadin.flow.component.AbstractCompositeField;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.common.UIUtils;
public class EmbeddedImageData extends 

AbstractCompositeField<VerticalLayout, EmbeddedImageData, ImageData>


{

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
		super(null);
		image = new Image();
		panel = new PanelFlow();
		label = new Span("");

		panel.setContent(image);
		panel.setSizeFull();
		button = new Button(VaadinIcon.UPLOAD.create());
		button.setSizeFull();
		
		HorizontalLayout top = new HorizontalLayout();
		top.setSpacing(true);
		top.add(button);
		top.add(label);
		
		content = new VerticalLayout();
		content.setSpacing(true);
		
		content.add(top);
		content.add(image);

	}



	
	
	private StreamResource createStreamResource(final ImageData imageData) {
		return new StreamResource(imageData.getFilename(),
				() -> new ByteArrayInputStream(imageData.getData()));
	}

	@Override
	protected void setPresentationValue(final ImageData data) {
		this.currentValue = data;
		if (data == null) {
			label.setText(UIUtils.localize("groupwidgets.custommap.missing_image"));
			button.setIcon(VaadinIcon.UPLOAD.create());
			//image.setSource(new ThemeResource("img/empty-image.png"));
	

			return;
		}
		try {
			image.setSrc(createStreamResource(data));
			BufferedImage bi = ImageIO.read(new ByteArrayInputStream(data.getData()));
			data.setWidth(bi.getWidth());
			data.setHeight(bi.getHeight());
			int size = (int)(data.getData().length * 0.001);
			String caption = String.format("<b>%s %d Kbytes<b/>", data.toString(),size);
			label.setText(caption);
			button.setIcon(VaadinIcon.EDIT.create());
		} catch (Exception e) {
			//image.setSource(new ThemeResource("img/empty-image.png"));
			label.setText(UIUtils.localize("groupwidgets.custommap.missing_image"));
			button.setIcon(VaadinIcon.UPLOAD.create());
			return;
		}

	}







}
