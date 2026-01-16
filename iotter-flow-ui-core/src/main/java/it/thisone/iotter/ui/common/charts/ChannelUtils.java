package it.thisone.iotter.ui.common.charts;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.UI;

import it.thisone.iotter.cassandra.model.FeedAlarm;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.Utils;

public class ChannelUtils {
	private static Logger logger = LoggerFactory.getLogger(ChannelUtils.class);
	public static final String LITERAL = "literal";
	public static final String CHANNEL_LABEL = "configuration.displayName";

	public static boolean isTypeAlarm(Channel channel) {
		String metaData = channel.getMetaData();
		return isTypeAlarm(metaData);
	}

	protected static boolean isTypeAlarm(String metaData) {
		return metaData != null && metaData.toLowerCase().contains(Constants.Provisioning.META_ALARM);
	}

	public static boolean isTypeDigital(Channel channel) {
		if (channel == null) return false;
		String metaData = channel.getMetaData();
		return isTypeDigital(metaData);
	}

	protected static boolean isTypeDigital(String metaData) {
		return  metaData != null && (metaData.toLowerCase().contains(Constants.Provisioning.META_ALARM) || 
				metaData.toLowerCase().contains(Constants.Provisioning.META_DIGITAL));
	}

	// Bug #2034
	public static String displayName(GraphicFeed feed) {
		if (feed.getChannel() != null) {
			return displayName(feed.getChannel());
		}
		return displayName(feed.getMetaData());
	}
	
	public static String displayName(Channel chnl) {
		String displayName = displayName(chnl.getMetaData());
		if (displayName != null && !displayName.isEmpty()) {
			chnl.getConfiguration().setDisplayName(displayName);
		}
		else {
			chnl.getConfiguration().setDisplayName(chnl.getConfiguration().getLabel());
		}
		return chnl.getConfiguration().getDisplayName();
	}
	
	public static String displayName(String metadata) {
		String displayName = null;
		String bundleId = Utils.messageBundleId(metadata);
		if (bundleId != null) {
			displayName = UIUtils.messageBundle(bundleId);
		}
		if (displayName!=null && displayName.isEmpty()) {
			System.err.print("");
		}
		return displayName;
	}
	
	public static ChoiceFormat enumChoiceFormat(String metadata, Locale locale) {
		String bundleId = Utils.messageBundleId(metadata);
		if (bundleId != null) {
			String code =  bundleId + Constants.Provisioning.META_ENUM;
			String defaultMessage = null;
			if (isTypeDigital(metadata)) {
				defaultMessage = "0=0;1=1";
			}
			String pattern = UIUtils.messageBundle(code, defaultMessage, locale);
			if (pattern == null) {
				return null; 
			}
			if (pattern.trim().isEmpty()) {
				return null; 
			}

			pattern = pattern.replaceAll("<", "");
			pattern = pattern.replaceAll("#", "");
			pattern = pattern.replaceAll("\u2264", "");

			pattern = pattern.replaceAll("=", "#");
			pattern = pattern.replaceAll(";", "|");
			
			try {
				return new ChoiceFormat(pattern);
			} catch (Throwable e) {
				logger.error(pattern,e);
			}

		}
		return null;
	}
	
	public static ChoiceFormat enumChoiceFormat(Channel chnl) {
		return enumChoiceFormat(chnl.getMetaData(), UI.getCurrent().getLocale());
	}
	
	public static ComboBox<Double> enumComboBox(Channel chnl) {
        // Get the ChoiceFormat renderer
        ChoiceFormat renderer = enumChoiceFormat(chnl);
        if (renderer == null) {
            return null;
        }
        
        // Prepare a list of limits as items and a mapping for captions.
        double[] limits = renderer.getLimits();
        Object[] formats = renderer.getFormats();
        List<Double> items = new ArrayList<>();
        Map<Double, String> captions = new HashMap<>();
        for (int i = 0; i < limits.length; i++) {
            items.add(limits[i]);
            captions.put(limits[i], (String) formats[i]);
        }
        
        // Create and configure the Vaadin 8 ComboBox.
        com.vaadin.flow.component.combobox.ComboBox<Double> combo = new ComboBox<>();
        combo.setItems(items);
        //combo.setEmptySelectionAllowed(false);

        // Use a caption generator to display the format string for each limit.
        combo.setItemLabelGenerator(item -> captions.get(item));
        return combo;
    }

	public static String getTypeVar(String metadata) {
		return Channel.getTypeVar(metadata);
	}

	public static String[] alarmParams(Channel chnl) {
		FeedAlarm alarm = UIUtils.getCassandraService().getAlarms().getAlarm(chnl.getDevice().getSerial(), chnl.getKey());
		if (alarm == null) {
			return null;
		}
		if (!alarm.isActive()) {
			return null;
		}
		return alarmParams(chnl,alarm);
	}

	public static String[] alarmParams(Channel chnl, IFeedAlarm alarm) {
		String[] params = new String[4];
		AlarmStatus status = AlarmStatus.valueOf(alarm.getStatus());
		switch (status) {
		case FIRE_UP:
			status = AlarmStatus.ON;
			break;
		case FIRE_DOWN:
			status = AlarmStatus.ON;
			break;
		default:
			break;
		}
		MeasureUnit measureUnit = chnl.getDefaultMeasure();
		params[0] = ChartUtils.formatMeasure(alarm.getValue(), measureUnit);
		params[1] = ChartUtils.formatDate(alarm.getTimestamp(), UIUtils.getBrowserTimeZone());
		params[2] = displayName(chnl);
		params[3] = status.name();
		return params;
	}
	
	public static List<String> alarms(Device entity) {
		List<String> entries = new ArrayList<String>();
		if (entity == null) {
			return entries;
		}	
		
 		Map<String, Channel> channels = new HashMap<String, Channel>();
		for (Channel chnl : entity.getChannels()) {
			if (chnl.getAlarm().isArmed()) {
				channels.put(chnl.getKey(), chnl);
			}
		}
		
		List<FeedAlarm> alarms = UIUtils.getCassandraService().getAlarms().findActiveAlarms(entity
				.getSerial());

		for (IFeedAlarm alarm : alarms) {
			if (channels.containsKey(alarm.getKey())) {
				Channel chnl = channels.get(alarm.getKey());
				String[] params = alarmParams(chnl, alarm);
				String entry = String.format("%s [%s] %s ", params[1], params[3], params[2]);
				entries.add(entry);
			}
		}
		return entries;
	}

	
}
