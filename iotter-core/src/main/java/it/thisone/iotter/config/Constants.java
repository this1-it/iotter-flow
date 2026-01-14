package it.thisone.iotter.config;

public interface Constants {

	public static final class Provisioning {
		private Provisioning() {
		}

		public static final String[] SPEED = new String[] { "4800", "9600", "19200", "38400", "57600", "115200" };

		// None, Odd, Even, Mark, Space
		// public static final String[] PARITY = new String[] { "N", "O", "E",
		// "M", "S"};

		public static final String[] PARITY = new String[] { "N", "O", "E" };

		public static final String[] DATA_BITS = new String[] { "5", "6", "7", "8" };
		public static final String[] STOP_BITS = new String[] { "1", "1.5", "2" };
		public static final String[] PROTOCOL = new String[] { "SERIAL", "ETHERNET", "MODBUSTCP" };

		public static final String META_ALARM = "|alarm";
		public static final String META_DIGITAL = "|digital";
		public static final String META_INTEGER = "|integer";
		public static final String META_ENUM = "|enum";
		public static final int INACTIVITY_MINUTES = 10;

		public static final int MAX_PROFILES = 6;
		// la massima banda per un aernet deve essere 400 registri a 10 sec
		public static final int MAX_ALLOWED_BANDWIDTH_RATIO = 40;
		public static final int MAX_TOTAL_REGISTRIES = 800;

	}

	public static final class Error {
		private Error() {
		}

		public static final int SOCKET_TIMEOUT_ERROR_CODE = 5000;
		public static final int GENERIC_APP_ERROR_CODE = 5001;
		public static final int DEVICE_NOT_FOUND_ERROR_CODE = 5002;
		public static final int DEVICE_UNAUTHORIZED_ERROR_CODE = 5003;
		public static final int DEVICE_WRITEAPIKEY_ERROR_CODE = 5004;
		public static final int DEVICE_READAPIKEY_ERROR_CODE = 5005;
		public static final int INVALID_DATA_ERROR_CODE = 5006;
		public static final int DEVICE_CONF_NOT_FOUND_ERROR_CODE = 5007;
		public static final int DEVICE_WRITE_PARAMETERS_ERROR_CODE = 5008;
		public static final int INVALID_CONF_ERROR_CODE = 5009;
		public static final int DEVICE_PROVISIONING_ERROR_CODE = 5010;
		public static final int DEVICE_NOT_AVAILABLE = 5011;
		public static final int INVALID_INPUT_ERROR_CODE = 5012;
		public static final int DEVICE_NOT_ACTIVE = 5013;

		public static final int DEVICE_READ_BUSY = 5020;
		public static final int DEVICE_READ_PARAMETERS_ERROR_CODE = 5021;

		public static final int INVALID_USER_TOKEN = 1000;
		public static final int USER_NOT_FOUND = 1001;
		public static final int USER_ALREADY_REGISTER = 1002;
		public static final int USER_WITH_INVALID_SERIAL_NUM = 1003;
		public static final int USER_NOT_AUTHORIZED = 1004;
		public static final int TENANT_NOT_FOUND = 1005;
		public static final int USER_INVALID_INPUT_ERROR_CODE = 1006;

		public static final int NETWORK_HAS_ASSOCIATIONS = 2001;
		public static final int NETWORK_ALREADY_REGISTER = 2002;
		public static final int NETWORK_NOT_FOUND = 2003;

		public static final int DEVICE_HAS_ASSOCIATIONS = 3001;

	}

	public static final class PropertyName {
		private PropertyName() {
		}

		public static final String BUILD_DATE_TIME = "buildDateTime";
		public static final String BUILD_NUMBER = "buildNumber";
		public static final String BUILD_SCM_BRANCH = "buildScmBranch";

		public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
		public static final String IMPLEMENTATION_TITLE = "Implementation-Title";
	}

	public static final class MQTT {
		public static final String LOG4J_CATEGORY = "mqtt";

		public static enum Topics {
			DEVICE("DEVICE"), //
			MODBUSPROFILE("MODBUSPROFILE"), //
			IOTTER_DATA("iotter-data");
			
			private String name;

			private Topics(String value) {
				this.name = value;
			}

			@Override
			public String toString() {
				return name;
			}
		}

	}

	public static final class AMQ {
		private AMQ() {
		}

		public static final String CACHE_COORDINATION = "cache.coordination";
		public static final String JMS_PERSISTENCE_TOPIC = "jms/PersistenceTopic";
		public static final String JMS_CONNECTION_FACTORY = "jms/ConnectionFactory";

		public final static String JMS = "jms";
		public final static String ADMIN_QUEUE = "admin";
		public final static String ADMIN_QUEUE_REQ = "admin.request";
		public final static String CLIENT_QUEUE = "client";
		public final static String CLIENT_QUEUE_REQ = "client.request";
		public final static String INTEGRATION_QUEUE = "integration";
		public final static String INTEGRATION_QUEUE_REQ = "integration.request";
	}

	public static class AsyncExecutor {
		public static final String LOG4J_CATEGORY = "asyncExecutor";
		public static final int CORE_POOL_SIZE = 10;
		public static final int MAX_POOL_SIZE = 50;
		public static final int QUEUE_CAPACITY = 200;
		public static final String THREAD_NAME_PREFIX = "asyncexecutor-";
	}

	public static class Exporter {
		public static final String LOG4J_CATEGORY = "exporter";
		public static final String EMPTY_VALUE = "---";

	}

	public static class Auth {
		public static final String LOG4J_CATEGORY = "authentication";
	}
	
	public static class Migration {
		public static final String LOG4J_CATEGORY = "migration";
	}
	
	public static class Notifications {
		public static final String LOG4J_JAVAMAIL = "javamail";
		public static final String LOG4J_CATEGORY = "notifications";
		public static final String VISUALIZATION = "visualization";
		public static final String FULLNAME = "fullname";
		public static final String LOGIN = "login";
		public static final String URL = "url";
		public static final String TOKEN = "token";
		public static final String ALARM = "alarm";
	}

	public static class LocalUiExecutor {
		public static final int CORE_POOL_SIZE = 0;
		public static final int MAX_POOL_SIZE = 3;
		public static final int QUEUE_CAPACITY = 30;
		public static final int KEEP_ALIVE = 5;
		public static final String THREAD_NAME_PREFIX = "local-ui-";
	}

	public static class Validators {
		public static final int MIN_PASSWORD_LENGTH = 8;
		public static final int MIN_USERNAME_LENGTH = 8;
		public static final int MIN_SERIAL_NUMBER_LENGTH = 10;
	}

	public static class Cache {
		public static final String DEVICE = "device";
		public static final String ROLE = "role";
		public static final String UNIT_OF_MEASURE_CODE = "unit_of_measure_code";
		public static final String UNIT_OF_MEASURE = "unit_of_measure";
		public static final String TICKS = "ticks";
		public static final String TIMESTAMPS = "timestamps";
		public static final String MESSAGES = "messages";
		public static final String DATASINK = "datasink";
		public static final String DATAVALUES = "data_values";
		public static final String MODBUS_REGISTERS = "modbus_registers";
	}

	public static class RollUp {
		public static final String ROLL_UP_LOG4J_CATEGORY = "rollup";
		public static final int GRACE_TIME_SECS = 15 * 60;
		public static final int EPOCH_YEAR = 2016;
	}

	public static class REST {
		public static final String LOG4J_CATEGORY = "rest";
	}

	public static final String CLUSTER_ROLE = "cluster.role";

	// Bug #287 (In Progress): Eliminazione gruppi

	public static final boolean USE_GROUPS = false;
	public static final String IMAGES_PATH = "VAADIN/themes/iotter/img";
	public static final int REFRESHER_INTERVAL_IN_MILLIS = 30 * 1000;

	public static final int USER_TOKEN_HOURS = 3;
	public static final String SYSTEM = "_SYSTEM_";
	public static final String ANONYMOUS = "anonymous";

	/* do not change or database porting needed */
	public static final String DEFAULT_NETWORK = "default network";
	/* do not change or database porting needed */
	public static final String DEFAULT_GROUP = "default group";
	/* do not change or database porting needed */
	public static final String DEFAULT_WIDGET = "default widget";

	public static final float FIELD_SIZE = 18.0f;

	public static final String ROLE_SUPERVISOR = "Supervisor";
	public static final String ROLE_ADMINISTRATOR = "Administrator";
	public static final String ROLE_USER = "User";
	public static final String ROLE_SUPERUSER = "SuperUser";
	public static final String ROLE_PRODUCTION = "Production";
	public static final String ROLE_FINANCE = "Finance";
	public static final String ROLE_DEMO = "Demo";

	
	
	public static final String[] ALL_ROLES = { ROLE_SUPERVISOR, //
			ROLE_ADMINISTRATOR, //
			ROLE_FINANCE, //
			ROLE_PRODUCTION, //
			ROLE_SUPERUSER, //
			ROLE_USER, //
			ROLE_DEMO, //
	};

	public static final String SERIAL_REGEXP = "^[0-9]{8}$";

	public static final String LATITUDE_REGEXP = "^-?([1-8]?[1-9]|[1-9]0)\\.{1}\\d{1,6}";
	public static final String LONGITUDE_REGEXP = "^-?([1]?[1-7][1-9]|[1]?[1-8][0]|[1-9]?[0-9])\\.{1}\\d{1,6}";
	public static final String IMPORTER_LOG4J_CATEGORY = "importer";

	public static final int RAIN_GAUGE_SENSORS = 61;

}
