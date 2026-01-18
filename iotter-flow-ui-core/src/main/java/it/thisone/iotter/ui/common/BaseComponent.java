package it.thisone.iotter.ui.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;


import it.thisone.iotter.ui.main.UiConstants;


/**
 * Custom component with utility methods
 * @author tisone
 *
 */
public abstract class BaseComponent extends Composite<Div> implements UiConstants {
	private static final long serialVersionUID = -2527235916632043029L;
	private String i18nkey;


	public BaseComponent(String i18nkey) {
		super();
		this.i18nkey = i18nkey;
		
	}
	
	public BaseComponent(String i18nkey, String id) {
		super();
		this.i18nkey = i18nkey;
		setId(id);
		
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append(i18nkey, this.getId()).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(i18nkey).append(this.getId()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BaseComponent == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final BaseComponent otherObject = (BaseComponent) obj;
		return new EqualsBuilder().append(this.getId(), otherObject.getId()).isEquals();
	}
	
    public String getI18nLabel(String key) {
        return getTranslation(getI18nKey() + "." + key);
    }

	public String getI18nKey() {
		return i18nkey;
	}

	
	/**
	 * create dialog box used in all modal editor
	 * @param label
	 * @param content
	 * @param ratio
	 * @return
	 */
	public static Dialog createDialog(String label, Component content, float[] dimension, String style) {
		SideDrawer drawer = new SideDrawer();
		drawer.setDrawerContent(content);
		drawer.applyDimension(dimension);
		if (style != null && !style.trim().isEmpty()) {
			//drawer.addClassName(style);
		}
		return drawer;
	}
	
	/*
	 * Bug #174 [VAADIN] Controllare la gestione del resize dei dialog modali nel caso di resize del browser
	 * 
	 * UiUtils.MODAL_DIALOG_STYLE
	 */

	
	public static void makeFullSizeDialog(Dialog dialog) {
//		int width = UI.getCurrent().getPage().getBrowserWindowWidth();
//		int height = UI.getCurrent().getPage().getBrowserWindowHeight();
//		dialog.setWidth(width, Unit.PIXELS);
//		dialog.setHeight(height, Unit.PIXELS);
	}	
	
	public static void makeResponsiveDialog(Dialog dialog, float[] dimension, String style) {
//		if (dimension == null || dimension.length < 2) {
//			dimension = UIUtils.M_DIMENSION;
//		}
//		final BrowserWindowResizeListener resizeListener = createBrowserWindowResizeListener(dialog, dimension);
//		int width = UI.getCurrent().getPage().getBrowserWindowWidth();
//		int height = UI.getCurrent().getPage().getBrowserWindowHeight();
//		if (width < 481 || height < 481) {
//			dimension = new float[]{1f,1f};
//		}
//		if (UIUtils.isMobile()) {
//			dimension = new float[]{1f,1f};
//		}
		
//		dialog.setWidth(width * dimension[0], Unit.PIXELS);
//		dialog.setHeight(height * dimension[1], Unit.PIXELS);
//		UI.getCurrent().getPage()
//				.addBrowserWindowResizeListener(resizeListener);
		
//		dialog.addOpenedChangeListener(event -> {
//			if (!event.isOpened()) {
//				UI.getCurrent().getPage()
//					.removeBrowserWindowResizeListener(resizeListener);
//			}
//		});
		
		//dialog.setStyleName(UIUtils.MODAL_DIALOG_STYLE);
		//dialog.addClassName(style);
		
	}
	
//	@SuppressWarnings("serial")
//	public static BrowserWindowResizeListener createBrowserWindowResizeListener(final Dialog component, final float[] dimension) {
//		return new BrowserWindowResizeListener() {
//			@Override
//			public void browserWindowResized(BrowserWindowResizeEvent event) {
//				int width = UI.getCurrent().getPage().getBrowserWindowWidth();
//				int height = UI.getCurrent().getPage().getBrowserWindowHeight();
//				float x = dimension[0];
//				float y = dimension[1];
//				if (width < 481 || height < 481) {
//					x = 1f;
//					y = 1f;
//				}
//				component.setWidth(width * x, Unit.PIXELS);
//				component.setHeight(height * y, Unit.PIXELS);
//			}
//		};
//	}

	

	/**
	 * Bug #144 Issue on AbstractBaseEntityListing not properly displayed after upgrade to 7.3
	 * 
	 */
	public void setRootComposition(Component compositionRoot) {
		//setCompositionRoot(compositionRoot);
	}

	//@Override
//	@Override
//	protected void setCompositionRoot(Component compositionRoot) {
//		super.setCompositionRoot(compositionRoot);
//	}
	
//	public HasComponents findParentTabSheet() {
//		HasComponents component = getParent();
//		while (component != null) {
//			if (component instanceof TabSheet) {
//				return component;
//			}
//			component = component.getParent();
//		}
//		return null;
//	}
	
}
