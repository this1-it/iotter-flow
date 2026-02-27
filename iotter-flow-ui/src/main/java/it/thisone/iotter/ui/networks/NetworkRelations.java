package it.thisone.iotter.ui.networks;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exceptions.ApplicationRuntimeException;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.ui.devices.DevicesListing;
import it.thisone.iotter.ui.networkgroups.NetworkGroupsListing;
import it.thisone.iotter.ui.users.UsersListing;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NetworkRelations extends VerticalLayout {

    private static final String NETWORK_RELATIONS = "networks.relations";
    private static final long serialVersionUID = 1L;

    private final NetworkService networkService;
    private final ObjectProvider<UsersListing> usersListingProvider;
    private final ObjectProvider<DevicesListing> devicesListingProvider;
    private final ObjectProvider<NetworkGroupsListing> networkGroupsListingProvider;

    private String title;

    public NetworkRelations(NetworkService networkService,
            ObjectProvider<UsersListing> usersListingProvider,
            ObjectProvider<DevicesListing> devicesListingProvider,
            ObjectProvider<NetworkGroupsListing> networkGroupsListingProvider) {
        this.networkService = networkService;
        this.usersListingProvider = usersListingProvider;
        this.devicesListingProvider = devicesListingProvider;
        this.networkGroupsListingProvider = networkGroupsListingProvider;
        setSizeFull();
        setSpacing(true);
    }

    public void init(String networkId) {
        Network network = networkService.findOne(networkId);
        if (network == null) {
            throw new ApplicationRuntimeException("Network not found " + networkId);
        }
        this.title = network.getName();

        removeAll();

        TabSheet tabSheet = new TabSheet();
        tabSheet.addClassName("tabsheet-framed");
        tabSheet.setSizeFull();

        UsersListing users = usersListingProvider.getObject();
        users.init(network);
        tabSheet.addTab(getLabel("network_users"), users.getMainLayout());

        DevicesListing devices = devicesListingProvider.getObject();
        devices.init(network, false);
        tabSheet.addTab(getLabel("network_devices"), devices.getMainLayout());

        if (Constants.USE_GROUPS) {
            NetworkGroupsListing groups = networkGroupsListingProvider.getObject();
            groups.init(network);
            tabSheet.addTab(getLabel("network_groups"), groups.getMainLayout());
        }

        add(tabSheet);
        setFlexGrow(1f, tabSheet);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("Name", this.title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.title).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NetworkRelations == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final NetworkRelations otherObject = (NetworkRelations) obj;
        return new EqualsBuilder().append(this.title, otherObject.title).isEquals();
    }

    public String getLabel(String key) {
        return getTranslation(NETWORK_RELATIONS + "." + key);
    }
}
