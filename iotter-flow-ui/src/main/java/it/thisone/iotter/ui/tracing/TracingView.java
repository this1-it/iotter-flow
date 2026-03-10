package it.thisone.iotter.ui.tracing;

import org.springframework.beans.factory.ObjectProvider;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import it.thisone.iotter.ui.MainLayout;
import it.thisone.iotter.ui.common.BaseView;

@Route(value = TracingView.VIEW_NAME, layout = MainLayout.class)
@PageTitle("Tracing")
public class TracingView extends BaseView {

	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "tracing";

	private final ObjectProvider<TracingListing> listingProvider;
	private boolean initialized;

	public TracingView(ObjectProvider<TracingListing> listingProvider) {
		this.listingProvider = listingProvider;
		setSizeFull();
		addClassName(VIEW_NAME);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		if (initialized) {
			return;
		}
		initialized = true;

		TracingListing listing = listingProvider.getObject();
		listing.init();
		add(listing.getMainLayout());
		setFlexGrow(1, listing.getMainLayout());
	}

	@Override
	public String getI18nKey() {
		return VIEW_NAME;
	}
}
