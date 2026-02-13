package it.thisone.iotter.ui.visualizers.controlpanel;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class IconSetResolver implements Serializable {

    private static final long serialVersionUID = 6190728250697245278L;

    public static final VaadinIcon LOADER = VaadinIcon.REFRESH;

    public static void preloadIcons() {
        // No-op in Flow: icon resources are lazily loaded by web components.
    }

    public IconSet resolveIconSetName(String resourceName) {
        IconSetEnum type = IconSetEnum.ICON_SET_01;
        for (IconSetEnum literal : IconSetEnum.values()) {
            if (literal.getDisplayName().equals(resourceName) || literal.name().equals(resourceName)) {
                type = literal;
                break;
            }
        }

        int[] values = new int[] { 0, 1 };
        VaadinIcon[] icons = new VaadinIcon[] { VaadinIcon.POWER_OFF, VaadinIcon.POWER_OFF };
        switch (type) {
            case ICON_SET_01:
                icons = new VaadinIcon[] { VaadinIcon.PLAY, VaadinIcon.STOP };
                break;
            case ICON_SET_02:
                icons = new VaadinIcon[] { VaadinIcon.STOP, VaadinIcon.PLAY };
                break;
            case ICON_SET_03:
                icons = new VaadinIcon[] { VaadinIcon.FIRE, VaadinIcon.CLOUD_O };
                break;
            case ICON_SET_04:
                icons = new VaadinIcon[] { VaadinIcon.CLOUD_O, VaadinIcon.FIRE };
                break;
            case ICON_SET_05:
                icons = new VaadinIcon[] { VaadinIcon.ARROW_DOWN, VaadinIcon.ARROW_UP };
                break;
            case ICON_SET_06:
                icons = new VaadinIcon[] { VaadinIcon.ARROW_UP, VaadinIcon.ARROW_DOWN };
                break;
            case ICON_SET_07:
                icons = new VaadinIcon[] { VaadinIcon.BELL, VaadinIcon.BELL_O };
                break;
            case ICON_SET_08:
                values = new int[] { 1 };
                icons = new VaadinIcon[] { VaadinIcon.BELL_SLASH };
                break;
            default:
                break;
        }
        return new IconSet(type, values, icons);
    }

    public enum IconSetEnum {
        ICON_SET_01("ic01"), ICON_SET_02("ic02"), ICON_SET_03("ic03"), ICON_SET_04("ic04"),
        ICON_SET_05("ic05"), ICON_SET_06("ic06"), ICON_SET_07("ic07"), ICON_SET_08("ic08");

        private final String displayName;

        IconSetEnum(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static class IconSet {
        private int position;
        private final IconSetEnum type;
        private final int[] values;
        private final VaadinIcon[] icons;

        public IconSet(IconSetEnum type, int[] values, VaadinIcon[] icons) {
            this.type = type;
            this.values = values;
            this.icons = icons;
        }

        public IconSetEnum getType() {
            return type;
        }

        public int[] getValues() {
            return values;
        }

        public VaadinIcon[] getIcons() {
            return icons;
        }

        public int getNextValue() {
            int i = (position + 1) % values.length;
            return values[i];
        }

        public Component createIconValue() {
            return new Icon(icons[position]);
        }

        public int getValue() {
            return values[position];
        }

        public void setIconValue(int value) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == value) {
                    position = i;
                    break;
                }
            }
        }

        public boolean contains(int value) {
            for (int current : values) {
                if (current == value) {
                    return true;
                }
            }
            return false;
        }
    }
}
