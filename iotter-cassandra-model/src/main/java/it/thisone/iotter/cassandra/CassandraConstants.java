package it.thisone.iotter.cassandra;

/**
 */

public interface CassandraConstants {
	public static final int BATCH_LIMIT = 100;
	public static final int PREFETCH_LIMIT = 1000;
	
	public static final String MEASURES_CF = "measures";
	public static final String DATASINKS_CF = "datasinks";
	public static final String FEEDS_CF = "feeds";
	public static final String MEASURES_TS_TMP_CF = "measures_ts_tmp";
	public static final String MEASURES_IDX_TMP_CF = "measures_idx_tmp";
	public static final String MEASURE_TICKS_CF = "measure_ticks";
	public static final String MEASURE_STATS_CF = "measure_stats";
	public static final String ROLL_UP_MIN15_CF = "roll_up_min15";
	public static final String ROLL_UP_H1_CF = "roll_up_h1";
	public static final String ROLL_UP_W1_CF = "roll_up_w1";
	public static final String ROLL_UP_D1_CF = "roll_up_d1";
	public static final String ROLL_UP_M1_CF = "roll_up_m1";
	public static final String ROLL_UP_QUEUE = "roll_up_queue";
	public static final String ROLL_UP_FEED_LOCK_CF = "roll_up_feed_lock";
	public static final String ROLL_UP_SINK_LOCK_CF = "roll_up_sink_lock";
	
	public static final String SESSION_AUTHENTICATION = "session_authentication";
	public static final String SESSION_AUTHORIZATION = "session_authorization";

	public static final String MEASURES_EXPORT_TMP_CF = "measures_export_tmp";
	public static final String MEASURES_EXPORT_TS_CF = "measures_export_ts";
	public static final String MEASURES_SET_CF = "measures_set";
	
	
	
	public static final String UNIT = "mu";
	public static final String PID = "pid";
	
	public static final String ERR = "err";
	public static final String COUNT = "count";
	public static final String QUAL = "qual";
	public static final String TS = "ts";
	public static final String TR = "tr";
	public static final String MAXVAL = "maxval";
	public static final String MINVAL = "minval";
	public static final String MAXTS = "maxts";
	public static final String MINTS = "mints";
	public static final String ERRTS = "errts";

	public static final String SN = "sn";
	public static final String VAL = "val";
	public static final String KEY = "key";
	public static final String LBL = "lbl";
	public static final String OWN = "own";
	public static final String UID = "uid";
	public static final String ID = "id";
	public static final String RECORDS = "rec";
	
	public static final String CHECKSUM = "cks";
	public static final String PUBLISHING = "pub";
	public static final String TRACING = "trc";
	public static final String AVAL = "aval";
	public static final String TYPEVAR = "typ";
	public static final String ACTIVE_ALARMS = "aca";
	

	public static final String INACTIVITY = "im";
	public static final String ALARMED = "al";
	public static final String BLEVEL = "bl";
	
	public static final String LAST = "last";
	public static final String FIRST = "first";
	public static final String FREQ = "freq";
	public static final String CREATED = "crt";
	public static final String UPDATED = "upd";
	public static final String RUNNING = "run";

	
	public static final String REV = "rev";
	
	public static final String ACTIVE = "act";
	public static final String SELECTED = "sel";
	public static final String MASTER = "msn";
	public static final String NOTIFY = "ntf";
	public static final String STATUS = "st";
	public static final String LASTCONTACT = "lc";
	public static final String PROTOCOL = "prt";
	public static final String W_API_KEY = "wak";
	public static final String TIME_ZONE = "tz";
	public static final String FEEDS = "feeds";
	public static final String FEED = "feed";
	public static final String LASTROLLUP = "lr";
	

	public static final String SCALE = "scale";
	public static final String OFFSET = "offset";

	public static final String SINCE = "since";
	public static final String TTL = "ttl";
	public static final String MININTERPL = "mini";

	public static final String PRIORITY = "prt";
	public static final String DELAY_MINUTES = "dlymins";
	public static final String REPEAT_MINUTES = "rptmins";
	public static final String LOW_LOW = "lowlow";
	public static final String LOW = "low";
	public static final String HIGH_HIGH = "highhigh";
	public static final String HIGH = "high";
	public static final String ARMED = "armed";
	public static final String DELAY = "dly";
	public static final String REPEAT = "rpt";
	public static final String OPERATOR = "op";
	public static final String MEMBERS = "mbrs";
	
	public static final String FEEDALARMTHRESHOLDS_CF_NAME = "feed_alarm_thresholds";
	public static final String FEEDALARMS_CF_NAME = "feed_alarms";
	public static final String FEEDALARMEVENTS_CF_NAME = "feed_alarm_events";
	public static final String CONFIGURATION_REGISTRY_CF_NAME = "configuration_registry";

	public static final String DATETIME = "dt";
	public static final String THRESHOLD = "ths";
	public static final String TOPIC = "tpc";
	public static final String PERMISSION = "perm";
	public static final String GROUP = "grp";
	public static final String SECTION = "sect";
	
	public static final String TOKEN = "tkn";
	public static final String USER = "user";
	public static final String ROLE = "role";
	public static final String NETWORK = "ntw";
	public static final String EXPIRY = "exp";
	public static final String GRANT = "grnt";

	public static final String QID = "qid";
	public static final String KEYS = "keys";
	public static final String LOWER = "lower";
	public static final String UPPER = "upper";
	public static final String START = "start";
	public static final String TOTAL = "total";
	public static final String ASCENDING = "ascending";
	public static final String INTERPOLATION = "interpolation";
	public static final String BATCH_SIZE = "batch_size";



	@Deprecated
	public static final String PAYLOAD = "payload";

	@Deprecated
	public static final String CONFIGURATION_CF_NAME = "configurations";

	
}
