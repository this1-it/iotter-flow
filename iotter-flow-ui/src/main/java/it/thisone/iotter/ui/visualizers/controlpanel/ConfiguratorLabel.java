package it.thisone.iotter.ui.visualizers.controlpanel;

import java.util.List;

import com.vaadin.flow.component.html.Span;

public class ConfiguratorLabel extends Span {

    private static final long serialVersionUID = -3523430203186019762L;

    private final List<String> keys;

    public ConfiguratorLabel(List<String> keys) {
        super();
        this.keys = keys;
    }

    public List<String> getKeys() {
        return keys;
    }

    public static boolean isValidAscii(char ch) {
        return (ch >= 32 && ch < 127) || (ch >= 128 && ch < 256);
    }
}
