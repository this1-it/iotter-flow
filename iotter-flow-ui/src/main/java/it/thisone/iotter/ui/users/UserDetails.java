package it.thisone.iotter.ui.users;

import java.util.List;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.ui.common.AbstractBaseEntityDetails;
import it.thisone.iotter.ui.common.EditorConstraintException;

public class UserDetails extends AbstractBaseEntityDetails<User> {

	private static final long serialVersionUID = 1L;
	private final DeviceService deviceService;
	private final UserService userService;

	public UserDetails(User item, boolean remove, DeviceService deviceService, UserService userService) {
		super(item, User.class, "user.editor",
				new String[] { "firstName", "lastName", "username", "email", "role" }, remove);
		this.deviceService = deviceService;
		this.userService = userService;
	}

	@Override
	protected void onRemove() throws EditorConstraintException {
		User entity = getBean();

		if (entity.hasRole(Constants.ROLE_SUPERVISOR)) {
			throw new EditorConstraintException(getI18nLabel("supervisor.constraint"));
		}
		if (entity.hasRole(Constants.ROLE_PRODUCTION)) {
			throw new EditorConstraintException(getI18nLabel("production.constraint"));
		}

		if (entity.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			List<Device> devices = deviceService.findByOwner(entity.getUsername());
			if (!devices.isEmpty()) {
				throw new EditorConstraintException(getI18nLabel("administrator.constraint"));
			}
		}

		if (entity.hasRole(Constants.ROLE_ADMINISTRATOR) && AccountStatus.ACTIVE.equals(entity.getAccountStatus())) {
			throw new EditorConstraintException(getI18nLabel("administrator.constraint"));
		}

		userService.deleteOwnership(entity.getUsername());
	}
}
