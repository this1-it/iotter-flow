package it.thisone.iotter.ui.main;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import it.thisone.iotter.ui.MainLayout;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Main")
public class MainView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	public MainView() {
		setSizeFull();
		add(new H1(getTranslation("main.view.welcome")));
	}
}
