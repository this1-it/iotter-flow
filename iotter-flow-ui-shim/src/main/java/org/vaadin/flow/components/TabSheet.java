package org.vaadin.flow.components;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;

import java.util.*;

/**
 * A drop-in replacement for Vaadin 8's TabSheet. Shows both the {@link Tabs} component
 * and the tab contents.
 * <p></p>
 * You can add and populate tabs in two ways:
 * <ul><li>eagerly, by calling {@link #addTab()} function, or</li>
 * <li>lazily, by calling {@link #addLazyTab(SerializableSupplier)}</li>.
 * </ul>
 */
public class TabSheet extends Composite<Component> implements HasStyle, HasSize {
    /**
     * Maps {@link Tab} to the contents of the tab.
     */
    
    private final Map<Tab, Component> tabsToContents = new HashMap<>();

    /**
     * Maps {@link Tab} to the provider of the contents of the tab.
     */
    
    private final Map<Tab, SerializableSupplier<? extends Component>> tabsToContentProvider = new HashMap<>();
    
    private final VerticalLayout content = new VerticalLayout();
    
    private final Tabs tabsComponent = new Tabs();
    
    private final Div tabsContainer = new Div();

    public TabSheet() {
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();
        content.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        content.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        content.addClassName("tabsheet");

        tabsContainer.setWidthFull();
        // when TabSheet's height is defined, the following rules allow the container to grow or shrink as necessary.
        tabsContainer.getElement().getStyle().set("flexGrow", "1");
        tabsContainer.getElement().getStyle().set("flexShrink", "1");
        tabsContainer.setMinHeight("0");
        // flex container so children with height:100% resolve against the flex-computed height
        tabsContainer.getElement().getStyle().set("display", "flex");
        tabsContainer.getElement().getStyle().set("flexDirection", "column");
        tabsContainer.addClassName("tabsheet-container");

        content.add(tabsComponent, tabsContainer);

        tabsComponent.addSelectedChangeListener(e -> update());
    }

    @Override
    
    protected Component initContent() {
        return content;
    }

    /**
     * Adds a new tab to the tab host, with optional <code>label</code> and optional <code>contents</code>.
     * <p></p>
     * You can either provide the tab contents eagerly, or you can populate the tab
     * later on, by calling {@link #setTabContents}. To make the tab populate itself automatically when it's shown
     * for the first time, use {@link #addLazyTab(String, SerializableSupplier)}.
     */
    
    public Tab addTab(String label, Component contents) {
        final Tab tab = createTab();
        if (label != null) {
            tab.setLabel(label);
        }
        tabsComponent.add(tab);
        tabsToContents.put(tab, contents);
        update();
        return tab;
    }

    /**
     * Adds a new tab to the tab host, with optional <code>label</code> and no <code>contents</code>.
     * <p></p>
     * You can either provide the tab contents eagerly, or you can populate the tab
     * later on, by calling {@link #setTabContents}. To make the tab populate itself automatically when it's shown
     * for the first time, use {@link #addLazyTab(String, SerializableSupplier)}.
     */
    
    public Tab addTab(String label) {
        return addTab(label, null);
    }

    /**
     * Adds a new tab to the tab host, with no <code>label</code> and optional <code>contents</code>.
     * <p></p>
     * You can either provide the tab contents eagerly, or you can populate the tab
     * later on, by calling {@link #setTabContents}. To make the tab populate itself automatically when it's shown
     * for the first time, use {@link #addLazyTab(String, SerializableSupplier)}.
     */
    
    public Tab addTab(Component contents) {
        return addTab(null, contents);
    }

    /**
     * Adds a new tab to the tab host, with no <code>label</code> and no <code>contents</code>.
     * <p></p>
     * You can either provide the tab contents eagerly, or you can populate the tab
     * later on, by calling {@link #setTabContents}. To make the tab populate itself automatically when it's shown
     * for the first time, use {@link #addLazyTab(String, SerializableSupplier)}.
     */
    
    public Tab addTab() {
        return addTab(null, null);
    }

    /**
     * Adds a new tab to the tab host, with optional <code>label</code>. The tab contents is
     * constructed lazily when the tab is selected for the first time.
     */
    
    public Tab addLazyTab(String label,  SerializableSupplier<? extends Component> contentsProvider) {
        Objects.requireNonNull(contentsProvider);
        final Tab tab = createTab();
        if (label != null) {
            tab.setLabel(label);
        }
        tabsComponent.add(tab);
        tabsToContents.put(tab, null);
        tabsToContentProvider.put(tab, contentsProvider);
        update();
        return tab;
    }

    /**
     * Adds a new tab to the tab host, with no <code>label</code>. The tab contents is
     * constructed lazily when the tab is selected for the first time.
     */
    
    public Tab addLazyTab( SerializableSupplier<? extends Component> contentsProvider) {
        return addLazyTab(null, contentsProvider);
    }

    /**
     * Sets the contents of given tab to newContents.
     */
    public void setTabContents( Tab tab,  Component newContents) {
        checkOurTab(tab);
        tabsToContents.put(tab, newContents);
        tabsToContentProvider.remove(tab);
        update();
    }

    /**
     * Finds a tab containing given contents. Returns null if there is no
     * such tab.
     */
    
    public Tab findTabWithContents( Component contents) {
        Objects.requireNonNull(contents);
        return tabsToContents.entrySet().stream()
                .filter(it -> contents.equals(it.getValue()))
                .map(it -> it.getKey())
                .findAny()
                .orElse(null);
    }

    /**
     * Returns the contents of given tab. May return null if the tab has no contents,
     * or the tab has lazy contents and hasn't been displayed yet.
     */
    
    public Component getTabContents( Tab tab) {
        checkOurTab(tab);
        return tabsToContents.get(tab);
    }

    private void checkOurTab( Tab tab) {
        Objects.requireNonNull(tab);
        if (!tabsToContents.containsKey(tab)) {
            throw new IllegalArgumentException("Parameter tab: invalid value " + tab + ": not hosted in this TabSheet");
        }
    }

    /**
     * Removes a tab. If the tab is selected, another tab is selected automatically (if possible).
     */
    public void remove( Tab tab) {
        tabsToContents.remove(tab);
        tabsComponent.remove(tab);
        update();
    }

    /**
     * Currently selected tab. Defaults to null since by default there are no tabs.
     */
    
    public Tab getSelectedTab() {
        return tabsComponent.getSelectedTab();
    }

    /**
     * Currently selected tab. Defaults to null since by default there are no tabs.
     */
    public void setSelectedTab( Tab tab) {
        tabsComponent.setSelectedTab(tab);
    }

    /**
     * Returns the 0-based index of the currently selected tab. -1 if no tab is selected.
     */
    public int getSelectedIndex() {
        return tabsComponent.getSelectedIndex();
    }

    /**
     * Returns the 0-based index of the currently selected tab. -1 if no tab is selected.
     */
    public void setSelectedIndex(int index) {
        tabsComponent.setSelectedIndex(index);
    }

    /**
     * Returns the current number of tabs.
     */
    public int getTabCount() {
        return tabsToContents.keySet().size();
    }

    /**
     * Gets the orientation of this tab sheet.
     *
     * @return the orientation
     */
    
    public Tabs.Orientation getOrientation() {
        return tabsComponent.getOrientation();
    }

    /**
     * Sets the orientation of this tab sheet.
     *
     * @param orientation
     *            the orientation
     */
    public void setOrientation( Tabs.Orientation orientation) {
        tabsComponent.setOrientation(orientation);
    }

    public Component getSelectedTabContents() {
        return tabsContainer.getChildren().findFirst().orElse(null);
    }

    private void update() {
        final Component currentTabContent = getSelectedTabContents();
        final Tab selectedTab1 = getSelectedTab();

        Component newTabContent;
        if (selectedTab1 == null) {
            newTabContent = null;
        } else {
            newTabContent = tabsToContents.get(selectedTab1);
            if (newTabContent == null) {
                SerializableSupplier<? extends Component> provider = tabsToContentProvider.get(selectedTab1);
                if (provider != null) {
                    newTabContent = provider.get();
                    Objects.requireNonNull(newTabContent, "content provider for tab " + selectedTab1 + " provided null contents: " + provider);
                    tabsToContentProvider.remove(selectedTab1);
                    tabsToContents.put(selectedTab1, newTabContent);
                }
            }
        }

        if (!Objects.equals(currentTabContent, newTabContent)) {
            tabsContainer.removeAll();
            if (newTabContent != null) {
                tabsContainer.add(newTabContent);
            }
        }
    }

    /**
     * Removes all tabs.
     */
    public void removeAll() {
        tabsToContents.clear();
        tabsComponent.removeAll();
        update();
    }

    /**
     * A live list of all tabs. The list is immutable but live: it reflects changes when tabs are added or removed.
     */
    
    private final List<Tab> tabs = new AbstractList<Tab>() {
        @Override
        public Tab get(int index) {
            return (Tab) tabsComponent.getComponentAt(index);
        }

        @Override
        public int size() {
            return getTabCount();
        }
    };

    /**
     * Returns a live list of all tabs. The list is immutable but live: it reflects changes when tabs are added or removed.
     */
    
    public List<Tab> getTabs() {
        return tabs;
    }

    
    public Registration addSelectedChangeListener( ComponentEventListener<Tabs.SelectedChangeEvent> listener) {
        return tabsComponent.addSelectedChangeListener(listener);
    }

    public int indexOf( Tab tab) {
        Objects.requireNonNull(tab);
        return tabsComponent.indexOf(tab);
    }

    /**
     * Sometimes it's good to extend the {@link Tab} class, insert a bit of styling+business logic there,
     * then use your customized tabs with the TabSheet. In such case override this function and provide
     * instances of your custom tab here.
     * @return a new {@link Tab} instance, not null.
     */
    
    protected Tab createTab() {
        return new Tab();
    }
}
