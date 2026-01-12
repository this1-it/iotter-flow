package com.vaadin.addon.charts;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */



import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.vaadin.addon.charts.model.Lang;
import com.vaadin.addon.charts.model.style.Theme;



/**
 * The ChartOptions extension configures a page local theme and other global
 * options like localized texts for charts. With this extension it is possible
 * to configure e.g. default colors used by all Chart objects displayed in the
 * UI.
 */
public class ChartOptions  {

    @JsonUnwrapped
    private Theme theme;

    private Lang lang;

    protected ChartOptions() {
    }

    private void notifyListeners() {
     }



    /**
     * Sets the theme to use.
     * <p/>
     * Note that if the view is already drawn, all existing {@link Chart}s will
     * be redrawn.
     *
     * @param theme
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
        buildOptionsJson();
        notifyListeners();
    }

    private void buildOptionsJson() {

    }

    /**
     * Returns the {@link Theme} in use or {@code null} if no theme has been
     * set.
     *
     * @return the {@link Theme} in use or {@code null}.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Changes the language of all charts.
     *
     * @param lang
     */
    public void setLang(Lang lang) {
        this.lang = lang;
        buildOptionsJson();
        notifyListeners();
    }

    /**
     * Returns the {@link Lang} in use or {@code null} if no lang configuration
     * has been set.
     *
     * @return the {@link Lang} in use or {@code null}.
     */
    public Lang getLang() {
        return lang;
    }



 

    /**
     * Returns a ChartOptions extension for the current UI. If a ChartOptions
     * extension has not yet been added, a new one is created and added.
     *
     * @return a ChartOptions instance connected to the currently active UI
     */
    public static ChartOptions get() {

        return null;
    }

}
