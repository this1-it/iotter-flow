package it.thisone.iotter.ui.main;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontIcon;
import com.vaadin.flow.component.themes.ValoTheme;

public interface UiConstants {

	public static final String IDENTITY_PROFILE = "identityProfile";

	public static final String DEVICE_GROUPWIDGET_EVENT = "device_groupwidget_event";
	public static final int TOOLTIP_OPEN_DELAY = 100;

	// Bug #209 (Resolved): [VAADIN] side menu layout should be narrow
	// see also $v-iotter-sidebar-width in common.scss
	public static final int SIDEBAR_WIDTH = 0;
	
	
	public static final int VALO_UI_WITH_MENU_HEIGHT = 30;
	public static final int TOGGLE_WIDTH = 20;
	public static final int FOOTER_HEIGHT = 30; // div#footer
	public static final int HEADER_HEIGHT = 90; // div#header
	public static final int TAB_HEIGHT = 36 + 10;
	public static final int TOOLBAR_HEIGHT = 40;
	public static final int SMALL_HEADER_HEIGHT = 24;
	public static final boolean WITH_FOOTER = true;
	public static final int TOP_MENU_HEIGHT = 60;

	public static final String PROVISIONING = "provisioning";


	public static final String MAPS_DEVICES_GOOGLE = "maps.devices.google";

	public static final FontIcon ICON_HOME = VaadinIcons.HOME;
	public static final FontIcon ICON_USERS = VaadinIcons.USERS;
	public static final FontIcon ICON_DEVICES = VaadinIcons.DASHBOARD;
	public static final FontIcon ICON_DEVICECONFIGURATIONS = VaadinIcons.WRENCH;
	public static final FontIcon ICON_NETWORKS = VaadinIcons.SITEMAP;
	public static final FontIcon ICON_TRACING = VaadinIcons.EYE;
	public static final FontIcon ICON_GROUPWIDGETS = VaadinIcons.BAR_CHART;

	public static final FontIcon ICON_QUESTION = VaadinIcons.QUESTION;
	public static final FontIcon ICON_OK = VaadinIcons.CHECK_SQUARE_O;
	public static final FontIcon ICON_CLOCK = VaadinIcons.CLOCK;
	public static final FontIcon ICON_UPLOAD = VaadinIcons.UPLOAD;
	public static final FontIcon ICON_DOWNLOAD = VaadinIcons.DOWNLOAD;
	public static final FontIcon ICON_PLUS = VaadinIcons.ARROW_RIGHT;
	public static final FontIcon ICON_MINUS = VaadinIcons.ARROW_LEFT;
	public static final FontIcon ICON_CONFIRM = VaadinIcons.CHECK;
	public static final FontIcon ICON_SAVE = VaadinIcons.FILE_TEXT;
	public static final FontIcon ICON_CANCEL = VaadinIcons.CLOSE;
	public static final FontIcon ICON_REMOVE = VaadinIcons.TRASH;
	public static final FontIcon ICON_MODIFY = VaadinIcons.EDIT;
	public static final FontIcon ICON_DETAILS = VaadinIcons.INFO;
	public static final FontIcon ICON_COPY = VaadinIcons.COPY;

	public static final FontIcon ICON_ADD = VaadinIcons.PLUS;
	public static final FontIcon ICON_MOVE = VaadinIcons.ARROWS;
	public static final FontIcon ICON_CONTROL = VaadinIcons.COG;
	public static final FontIcon ICON_GRAPHS = VaadinIcons.BAR_CHART;
	public static final FontIcon ICON_ACTIVATE = VaadinIcons.BOLT;
	public static final FontIcon ICON_MAP = VaadinIcons.MAP_MARKER;
	public static final FontIcon ICON_CONFIGURATION = VaadinIcons.WRENCH;
	public static final FontIcon ICON_EDIT = VaadinIcons.EDIT;
	public static final FontIcon ICON_LINK = VaadinIcons.LINK;
	public static final FontIcon ICON_MENU = VaadinIcons.LIST;
	public static final FontIcon ICON_EYE = VaadinIcons.EYE;
	public static final FontIcon ICON_WARNING = VaadinIcons.WARNING;
	public static final FontIcon ICON_START = VaadinIcons.FLAG;
	public static final FontIcon ICON_RESET = VaadinIcons.ERASER;
	public static final FontIcon ICON_MIGRATION = VaadinIcons.RECYCLE;

	public static final String BUTTON_DEFAULT_STYLE = ValoTheme.BUTTON_PRIMARY;
	public static final String BASIC_VIEW_STYLE = "basic-view";
	public static final String FOCUSED_STYLE = "focused";
	public static final String INVALID_STYLE = "invalid";
	public static final String TIME_CONTROLS_STYLE = "timecontrols";
	public static final String TIME_BUTTONS_STYLE = "timebuttons";
	public static final String BUTTONS_STYLE = "buttons";
	public static final String NATIVE_BUTTONS_STYLE = "native-buttons";
	public static final String PROPERTY_EDITOR_STYLE = "property-editor";
	public static final String TOOLBAR_STYLE = "toolbar";
	public static final String MODAL_DIALOG_STYLE = "modal-dialog";
	public static final String S_WINDOW_STYLE = "common-details";
	public static final String ANONYMOUS_STYLE = "anonymous";
	public static final String ACTIVE_PARAM_STYLE = ValoTheme.LABEL_BOLD;
	public static final String RESPONSIVE_TEXT_STYLE = "responsive-text";

	public static final float[] S_DIMENSION = new float[] { 0.4f, 0.4f };
	public static final float[] M_DIMENSION = new float[] { 0.6f, 0.6f };
	public static final float[] L_DIMENSION = new float[] { 0.8f, 0.8f };
	public static final float[] XL_DIMENSION = new float[] { 0.9f, 0.9f };

	public static final String TABLE_STYLE = ValoTheme.TABLE_SMALL;
	public static final String FIELD_STYLE = "small";

	public static final String DISPLAY_1024PX_STYLE = "display-1024px";
	public static final String DISPLAY_768PX_STYLE = "display-768px";


	public static final String USERTOKEN_COOKIE = "usertoken";

	public static final String USERNAME_COOKIE = "username";

	public static final String PASSWORD_COOKIE = "password";

	public static final int MAX_COOKIE_AGE = 60 * 60 * 24 * 30;

	public static final String PARAM_SEPARATOR = "&";

	public static final String TOKEN_PARAM = "token";

	public static final String USERNAME_PARAM = "user";

	public static final String PASSWORD_PARAM = "password";

	public static final String VIEW_TYPE_PARAM = "view_type";

	public static final String VIEW_MODE_PARAM = "view_mode";

	public static final String VIEW_MODE_DEFAULT = "map";

	public static final String VIEW_MAP = "map";

	public static final String VIEW_LIST = "list";

	public static final String KEY_PARAM = "key";

	public static final String EXTERNAL_ID_PARAM = "external_id";

	public static final String ACTION_PARAM = "action";
	
	public static final int MAX_PARAMETERS = 10;
	
	

}
