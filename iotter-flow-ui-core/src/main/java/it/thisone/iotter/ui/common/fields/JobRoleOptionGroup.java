package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

import it.thisone.iotter.enums.JobRole;
import it.thisone.iotter.ui.common.UIUtils;

public class JobRoleOptionGroup extends RadioButtonGroup<JobRole> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JobRoleOptionGroup() {
		super();
		
		// Set enum values as items, sorted by localized name
		setItems(Arrays.stream(JobRole.values())
			.sorted(Comparator.comparing(role -> UIUtils.localize(role.getI18nKey())))
			.collect(Collectors.toList()));
		
		// Set caption generator to use i18n keys
		setLabelCaptionGenerator(role -> UIUtils.localize(role.getI18nKey()));
	}
	

}
