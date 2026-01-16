package it.thisone.iotter.ui.common;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jatl.Html;
/*
 * https://code.google.com/p/jatl/source/browse/src/test/java/com/googlecode/jatl/HtmlBuilderTest.java
 */
import com.vaadin.flow.component.UI;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.main.IMainUI;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;


public class MarkupsUtils {
	private static Logger logger = LoggerFactory.getLogger(MarkupsUtils.class);

	public static String color(String text, String color) {
		return String.format("<span style=\"color: %s\">%s</span>", color, text);
	}

	public static String toHtml(GraphicWidget widget) {
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		String type = UIUtils.localize(widget.getType().getI18nKey());
		String beanName = (widget.getLabel() != null) ? type + ": " + widget.getLabel() : type + ": " + widget.getId();
		markup.text(beanName).br();
		if (!widget.getFeeds().isEmpty()) {
			markup.table().width("100%");
			markup.style("border-collapse: collapse;");
			markup.border("1").cellpadding("2").cellspacing("2");
			markup.thead().tr();
			markup.th().text("label").end();
			markup.th().text("measure").end();
			markup.th().text("status").end();
			markup.th().text("key").end();
			markup.th().text("range").end();
			markup.end().end();
			markup.tbody();
			for (GraphicFeed feed : widget.getFeeds()) {
				String uniqueKey = feed.getKey();
				String feedUnit = ChartUtils.getUnitOfMeasure(feed);
				String feedLabel = feed.getChannel().toString();
				String feedStatus = feed.getChannel().getConfiguration().isActive() ? "activated" : "deactivated";

				markup.tr();
				markup.td().text(feedLabel).end();
				markup.td().text(feedUnit).end();
				markup.td().text(feedStatus).end();
				markup.td().text(uniqueKey).end();
				markup.td().text(channelRange(feed.getChannel())).end();
				markup.end();
			}
			markup.end();
		}
		markup.done();
		// logger.debug(writer.toString());
		return writer.toString();
	}

	public static String channelRange(Channel chnl) {
		String range = null;
		DateFormat df = dateformat();
		if (chnl.getConfiguration().isActive()) {
			range = String.format("[%s‥+∞)", df.format(chnl.getConfiguration().getActivationDate()));
		} else {
			range = String.format("[%s‥%s)", df.format(chnl.getConfiguration().getActivationDate()),
					df.format(chnl.getConfiguration().getDeactivationDate()));
		}
		return range;
	}

	public static String toHtml(ChannelAdapterDataProvider container) {
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		markup.table().width("100%");
		markup.border("0").cellpadding("1").cellspacing("1");
		markup.thead().tr();
		markup.th().width("50%").end();
		markup.th().width("50%").end();
		markup.end().end();

		for (ChannelAdapter item : container.getItems()) {
			String label = item.getLabel();
			String unit = item.getMeasureUnit();
			String number = item.getLastMeasureValue();
			markup.tr();
			markup.td().text(label).end();
			markup.td().align("right").b().text(number).end().text(unit).end();
			markup.end();
		}
		markup.end();
		return writer.toString();
	}


	public static String stationLabel(Device device) {

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(UIUtils.getLocale());

		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);

		markup.table().width("100%");
		markup.border("0").cellpadding("1").cellspacing("0");

		markup.tr().td();
		markup.b().text(device.getLabel()).end();
		markup.text(" ");
		markup.text("sn ");
		markup.text(device.getSerial());
		markup.end().end();

		GeoLocation location = device.getLocation();
		markup.tr().td();
		markup.text(location.getAddress());
		markup.end().end();

		markup.tr().td();
		markup.text("Lat.");
		markup.b().text(df.format(location.getLatitude())).end();
		markup.text(" ");
		markup.text("Lon.");
		markup.b().text(df.format(location.getLongitude())).end();
		markup.text(" ");
		markup.text("Alt.");
		markup.b();
		markup.text(df.format(location.getElevation()));
		markup.end().end();

		markup.end();

		return writer.toString();
	}

	public static String toHtmlMeasureScale(List<MeasureUnit> list) {
		if (list.isEmpty())
			return "";
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		// markup.br();
		for (MeasureUnit u : list) {
			markup.text(u.getScale().toString());
			markup.br();
		}
		markup.end();
		markup.done();
		return writer.toString();
	}

	public static String toHtmlMeasureOffset(List<MeasureUnit> list) {
		if (list.isEmpty())
			return "";
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		// markup.br();
		for (MeasureUnit u : list) {
			markup.text(u.getOffset().toString());
			markup.br();
		}
		markup.end();
		markup.done();
		return writer.toString();
	}

	public static String toHtmlMeasureFormat(List<MeasureUnit> list) {
		if (list.isEmpty())
			return "";
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		// markup.br();
		for (MeasureUnit u : list) {
			markup.text(deNormalizedFormat(u.getFormat()));
			markup.br();
		}
		markup.end();
		markup.done();
		return writer.toString();
	}

	public static String toHtmlMeasureUnit(List<MeasureUnit> list) {
		if (list.isEmpty())
			return "";
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		// markup.br();
		for (MeasureUnit u : list) {
			//markup.text(UIUtils.getServiceFactory().getDeviceService().getUnitOfMeasureName(u.getType()));
			markup.br();
		}
		markup.end();
		markup.done();
		return writer.toString();
	}

	private static String deNormalizedFormat(String value) {
		String format = "";
		try {
			String[] ff = value.split("\\.");
			if (ff != null && ff.length > 0) {
				if (ff.length > 1) {
					format = String.format("%d.%d", ff[0].length(), ff[1].length());
				} else {
					format = String.format("%d.%d", ff[0].length(), 0);
				}
			}

		} catch (Exception e) {
			logger.error("unhandled format " + value, e);
		}
		return format;
	}

	public static String toHtml(List<MeasureUnit> list, String i18nSectionKey) {
		if (list.isEmpty())
			return "";
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		markup.table().width("100%");
		markup.style("border-collapse: collapse;");
		markup.border("0").cellpadding("2").cellspacing("2");
		markup.thead().tr();
		markup.th().text(getI18nLabel(i18nSectionKey, "symbol")).end();
		markup.th().text(getI18nLabel(i18nSectionKey, "scale")).end();
		markup.th().text(getI18nLabel(i18nSectionKey, "offset")).end();
		markup.th().text(getI18nLabel(i18nSectionKey, "format")).end();
		markup.end().end();
		markup.tbody();
		for (MeasureUnit u : list) {
			markup.tr();
			//markup.td().text(UIUtils.getServiceFactory().getDeviceService().getUnitOfMeasureName(u.getType())).end();
			markup.td().text(u.getScale().toString()).end();
			markup.td().text(u.getOffset().toString()).end();
			markup.td().text(u.getFormat()).end();
			markup.end();
		}
		markup.end();
		markup.done();
		return writer.toString();
	}

	private static String getI18nLabel(String sectionKey, String key) {
		return UIUtils.localize(sectionKey + "." + key);
	}

	/**
	 * Estimation of compression to be applied to label
	 * 
	 * @param text
	 *            : must be a single line of text olnly
	 * @return
	 */
	public static double calculateCompression(String text) {
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
		int size = 16;
		Font font = new Font("Arial", Font.PLAIN, size);
		double compression = font.getStringBounds(text, frc).getWidth() / (size * 10);
		return compression;
	}

	/**
	 * Font-size = 1/10th of the element's width.
	 * 
	 * @param width
	 *            pixel width
	 * @param compressor
	 * @return
	 */
	public static double calculateFontSize(double width, double compressor, double minFontSize, double maxFontSize) {
		return Math.max(Math.min(width / (compressor * 10), maxFontSize), minFontSize);
	}

	public static String simpleTable(Collection<String> items) {
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		markup.table().width("100%");
		markup.style("border-collapse: collapse;");
		markup.border("0").cellpadding("1").cellspacing("1");
		for (String item : items) {
			markup.tr();
			markup.td().raw(item).end();
			markup.end();
		}
		markup.end();
		return writer.toString();
	}

	public static String errorTooltip(String error, String i18nSectionKey) {
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		// if (error.contains(";")) {
		markup.table().width("100%");
		markup.style("border-collapse: collapse;");
		markup.border("1").cellpadding("1").cellspacing("1");

		/*
		 * markup.thead().tr();
		 * markup.th().width("50%").text(getI18nLabel(i18nSectionKey,
		 * "errtype")) .end();
		 * markup.th().width("50%").text(getI18nLabel(i18nSectionKey,
		 * "errcount")) .end(); markup.end().end();
		 */

		String[] errors = StringUtils.split(error, ";");
		for (int i = 0; i < errors.length; i++) {

			String[] tokens = StringUtils.split(errors[i], ":");
			String type = tokens[0];
			String count = "1";

			if (tokens.length > 1) {
				count = tokens[1];
			}

			markup.tr();
			markup.td().text(type).end();
			markup.td().text(count).end();
			markup.end();

		}
		markup.end();
		markup.done();
		// }
		// else {
		// markup.b().text(error).end();
		// }
		return writer.toString();

	}

	public static DateFormat dateformat() {
		DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
				UI.getCurrent().getLocale());
		f.setLenient(false);
		f.setTimeZone(((IMainUI) UI.getCurrent()).getTimeZone());
		return f;

	}

}
