package it.thisone.iotter.security;



public interface EntityPermission {
	
	public enum USER implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, REMOVE;
		@Override
		public String toString() {
			return "USER." + super.toString();
		}
    }
	
	public enum DEVICE implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, ACTIVATE, REMOVE, RESET, MIGRATE, IMPORT, EXPORT , EXPORT_DATA;
		@Override
		public String toString() {
			return "DEVICE." + super.toString();
		}
    }

	public enum DEVICE_MODEL implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, ACTIVATE, REMOVE;
		@Override
		public String toString() {
			return "DEVICE_MODEL." + super.toString();
		}
    }
	
	public enum NETWORK implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, REMOVE;
		@Override
		public String toString() {
			return "NETWORK." + super.toString();
		}
    }
	
	public enum NETWORK_GROUP implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, REMOVE;
		@Override
		public String toString() {
			return "NETWORK_GROUP." + super.toString();
		}
    }
	
	public enum TRACING implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, REMOVE;
		@Override
		public String toString() {
			return "TRACING." + super.toString();
		}
    }
	
	public enum GROUP_WIDGET implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, REMOVE;
		@Override
		public String toString() {
			return "GROUP_WIDGET." + super.toString();
		}
    }

	public enum MEASURE_UNIT_TYPE implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, ACTIVATE, REMOVE;
		@Override
		public String toString() {
			return "MEASURE_UNIT_TYPE." + super.toString();
		}
    }
	
	
	public enum MODBUS_PROFILE implements EntityPermission {
		CREATE, MODIFY, VIEW, VIEWALL, ACTIVATE, REMOVE;
		@Override
		public String toString() {
			return "MODBUS_PROFILE." + super.toString();
		}
    }
	
 }
