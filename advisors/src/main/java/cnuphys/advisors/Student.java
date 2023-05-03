package cnuphys.advisors;

import java.util.ArrayList;
import java.util.List;

import cnuphys.advisors.enums.Major;
import cnuphys.advisors.io.ITabled;
import cnuphys.advisors.model.Course;
import cnuphys.advisors.model.DataManager;
import cnuphys.advisors.model.LearningCommunityCourse;

public class Student implements ITabled {

	/** student locked down and can't be reassigned by algorithm?? */
	public boolean locked;
	
	/** learning community as a string */
	public String lc;
	
	/** learning community as a number */
	public int lcNum;

	/** the student's id */
	public String id;

	/** the student's last name */
	public String lastName;

	/** the student's first name */
	public String firstName;

	/** is a plp student */
	public boolean plp;

	/** is an honors student */
	public boolean honor;

	/** is a pre med scholar? */
	public boolean preMedScholar;

	/** is a student*/
	public boolean prelaw;

	/** is an ilc student*/
	public boolean ilc;

	/** is a presidential scholar*/
	public boolean presidentialScholar;

	/** is a wind scholar*/
	public boolean windScholar;

	/** the student's major */
	public Major major;

	/** is this a community captain? */
	public boolean communityCaptain;

	/** in the BTMG program? */
	public boolean btmg;


	/** the assigned advisor */
	public Advisor advisor;

	/** Student schedule only of classes taught by an FCA */
	public List<Course> schedule = new ArrayList<>();


	public Student(String id, String lastName, String firstName, String lc, String plp, String honr, String prsc,
			String psp, String prelaw, String wind, String ccap, String btmg, String maj) {
		super();
		this.lc = lc;
		
		lcNum = Integer.parseInt(lc.replaceAll("[^\\d.]", ""));
		
		this.id = DataManager.fixId(id);
		this.lastName = lastName.replace("\"", "").trim();
		this.firstName = firstName.replace("\"", "").trim();
		this.ilc = false;   //will be assigned in ILC step
		this.plp = checkString(plp, "PLP");
		this.honor = checkString(honr, "HO");
		this.presidentialScholar = checkString(prsc, "PRS");
		this.preMedScholar = checkString(psp, "PSP");
		this.prelaw = checkString(prelaw, "LW");
		this.windScholar = checkString(wind, "WIN");
		this.communityCaptain = checkString(ccap, "CCAP");
		this.btmg = checkString(btmg, "BTM");


		String majorstr = maj.replace("\"", "").trim();

		major = Major.getValue(majorstr);
		if (major == null) {
			System.err.println("COULD not match major [" + majorstr + "]");
			System.exit(1);
		}
	}

	//check a string for a pattern
	private boolean checkString(String s, String patt) {
		return s.replace("\"", "").trim().toUpperCase().contains(patt.toUpperCase());
	}


	/**
	 * Add a course to the student's schedule
	 * @param course the course to add
	 */
	public void addCourse(Course course) {
		schedule.remove(course);
		schedule.add(course);
	}


	/**
	 * @return the _id
	 */
	public String getID() {
		return id;
	}

	/**
	 * Is this student assigned an advisor?
	 * @return true if student has an advisor
	 */
	public boolean assigned() {
		return (advisor != null);
	}


	/**
	 * Return the name and ID of the student in a string. This can be used
	 * for, among other things, sorting
	 * @return the name and ID of the student
	 */
	public String fullNameAndID() {
		return String.format("%s, %s [%s]", lastName, firstName, id);
	}

	/**
	 * Is a course in the student's Learning community?
	 * @param crn the crn
	 * @return true if the course is in the students LC
	 */
	public boolean courseInLC(String crn) {
		
		for (LearningCommunityCourse lc : DataManager.getLearningCommunityData().getLearningCommunityCourses()) {
			if (crn.equals(lc.crn)) {
				return true;
			}
		}
		
		return false;
	}


	@Override
	public String getValueAt(int col) {
		if (col == 1) {
			return id;
		}
		else if (col == 2) {
			return lastName;
		}
		else if (col == 3) {
			return firstName;
		}
		else if (col == 4) {
			return "L" + lcNum;
		}
		else if (col == 5) {
			return ilc ? "ILC" : "";
		}
		else if (col == 6) {
			return plp ? "PLP" : "";
		}
		else if (col == 7) {
			return honor ? "HON": "";
		}
		else if (col == 8) {
			return presidentialScholar ? "PRSC": "";
		}
		else if (col == 9) {
			return preMedScholar ? "PSP" : "";
		}
		else if (col == 10) {
			return prelaw ? "PLW" : "";
		}
		else if (col == 11) {
			return windScholar ? "WIND" : "";
		}
		else if (col == 12) {
			return communityCaptain ? "CCAP" : "";
		}
		else if (col == 13) {
			return btmg ? "BTMG" : "";
		}
		else if (col == 14) {
			return major.name();
		}
		else if (col == 15) {
			return advisor == null ? "---" : advisor.name;
		}

		else {
			System.err.println("Bad column in Student getValueAt [" + col + "]");
			System.exit(0);
		}

		return null;
	}

}
