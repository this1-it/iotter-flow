package it.thisone.iotter.ui.groupwidgets;

import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.NetworkGroupService;

final class GroupWidgetDetails {

    private GroupWidgetDetails() {
    }

    static void removeExclusiveGroupIfNeeded(GroupWidget widget, NetworkGroupService networkGroupService) {
        if (widget == null || widget.getGroup() == null) {
            return;
        }
        NetworkGroup group = widget.getGroup();
        if (!group.isExclusive()) {
            return;
        }
        NetworkGroup managedGroup = networkGroupService.findOne(group.getId());
        if (managedGroup != null) {
            networkGroupService.removeMembers(managedGroup);
            networkGroupService.deleteById(managedGroup.getId());
        }
    }
}
