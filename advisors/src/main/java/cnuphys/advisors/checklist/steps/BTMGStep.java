package cnuphys.advisors.checklist.steps;

import cnuphys.advisors.Person;
import cnuphys.advisors.checklist.CheckListLaunchable;
import cnuphys.advisors.enums.EReason;
import cnuphys.advisors.model.AdvisorData;
import cnuphys.advisors.model.DataManager;
import cnuphys.advisors.model.StudentData;

public class BTMGStep extends CheckListLaunchable   {

	public BTMGStep(String info, boolean enabled) {
		super("BTMG", info, enabled);
	}

	/**
	 * Assign the bio tech and management advisees
	 */
	@Override
	public void launch() {

		//get the community advisors and students
		AdvisorData advisorData = DataManager.getFilteredAdvisorData(Person.BTMG);
		StudentData studentData = DataManager.getFilteredStudentData(Person.BTMG);
		DataManager.roundRobinAssign(advisorData.getAdvisors(), studentData.getStudents(), true, "In BTMG assign", EReason.BTMG);
	}
	

}
