package cnuphys.advisors.model;

import java.util.ArrayList;
import java.util.List;

import cnuphys.advisors.Advisor;
import cnuphys.advisors.Student;
import cnuphys.advisors.checklist.CheckList;
import cnuphys.advisors.enums.Department;
import cnuphys.advisors.enums.EReason;
import cnuphys.advisors.enums.Major;
import cnuphys.advisors.enums.Specialty;
import cnuphys.advisors.frame.AdvisorAssign;
import cnuphys.bCNU.util.UnicodeSupport;

public class DataManager {

	//base file name for advisors csv file in data dir
    private static final String _advisorBaseName = "advisors.csv";
    private static final String _scheduleBaseName = "scheduleofclasses.csv";
    private static final String _studentsBaseName = "students.csv";
    private static final String _alcsBaseName = "alcs.csv";
    private static final String _honAdvBaseName = "honorsadvisors.csv";
    private static final String _pspAdvBaseName = "pspadvisors.csv";  //pre health scholars
    private static final String _presScholarAdvBaseName = "presscholaradvisors.csv";
    private static final String _studentSchedulesBaseName = "studentschedules.csv";
    private static final String _ccptAdvBaseName = "ccptadvisors.csv";

    //the advisor data
	private static AdvisorData _advisorData;

	//the class schedule
	private static Schedule _schedule;

	//the student data
	private static StudentData _studentData;

	//Learning Communities
//	private static LearningCommunityData _learningCommunityData;

	//ALCs
	private static ALCData _alcData;

	//synonym lists

	public static final DataAttribute rowAtt = new DataAttribute(" ", 40);

	public static final DataAttribute inClassAtt = new DataAttribute(UnicodeSupport.SMILEYFACE, 18);
	public static final DataAttribute idAtt = new DataAttribute("ID NUMBER", 72, "id", "cnuid");
	public static final DataAttribute advisorAtt = new DataAttribute("ADVISOR", 150);
	public static final DataAttribute lastNameAtt = new DataAttribute("LAST", 105, "last name", "lastname", "last_name", "lname");
	public static final DataAttribute firstNameAtt = new DataAttribute("FIRST", 72, "first name", "firstname", "lfirst_name", "fname");
	public static final DataAttribute departmentNameAtt = new DataAttribute("DEPT", 70, "department", "adv1_dept");
	public static final DataAttribute numAdviseeAtt = new DataAttribute("#ADV", 45);
	public static final DataAttribute numMajorAtt = new DataAttribute("#MAJ", 45);
	public static final DataAttribute enrollmentAtt = new DataAttribute("Count", 45);

	public static final DataAttribute reasonAtt = new DataAttribute("Reason", 44);

	public static final DataAttribute lcAtt= new DataAttribute("Learning Community", 240, "Learning Community Title");
	public static final DataAttribute lcNumAtt = new DataAttribute("LC#", 35, "lc");


	public static final DataAttribute crnAtt = new DataAttribute("CRN", 40);
	public static final DataAttribute subjectAtt = new DataAttribute("SUBJ", 70, "subject");
	public static final DataAttribute courseAtt = new DataAttribute("CRSE", 60, "course", "course #");
	public static final DataAttribute sectionAtt = new DataAttribute("SECT", 30, "section");
	public static final DataAttribute titleAtt = new DataAttribute("TITLE", 170);
	public static final DataAttribute hoursAtt = new DataAttribute("HOURS", 38);
	public static final DataAttribute llcAtt= new DataAttribute("LLC", 40, "Area of LLC", "Liberal Learning Attribute");
	public static final DataAttribute notesAtt = new DataAttribute("NOTES", 140);
	public static final DataAttribute typeAtt = new DataAttribute("TYPE", 30);
	public static final DataAttribute daysAtt = new DataAttribute("DAYS", 34);
	public static final DataAttribute timeAtt = new DataAttribute("TIME", 90);
	public static final DataAttribute locationAtt = new DataAttribute("LOC", 70, "location");
	public static final DataAttribute instructorAtt = new DataAttribute("INSTRUCTOR", 150);

	public static final DataAttribute alcAtt = new DataAttribute("ALC", 35);
	public static final DataAttribute prscAtt = new DataAttribute("PRSC", 38);
	public static final DataAttribute windAtt = new DataAttribute("WIND", 38);
	public static final DataAttribute ccapAtt = new DataAttribute("CCAP", 38);
	public static final DataAttribute plpAtt = new DataAttribute("PLP", 35);
	public static final DataAttribute honrAtt = new DataAttribute("HONR", 38);
	public static final DataAttribute pspAtt = new DataAttribute("PSP", 40);
	public static final DataAttribute prelawAtt = new DataAttribute("PRELAW", 46, "PLW");
	public static final DataAttribute majorAtt = new DataAttribute("MAJOR", 60, "Major_1st");
	public static final DataAttribute emailAtt = new DataAttribute("ADVISOR_EMAIL", 170, "email", "student_email");
	public static final DataAttribute directorAtt = new DataAttribute("DIRECTOR", 50);

	public static final DataAttribute specialAtt = new DataAttribute("SPECIAL", 50);
	public static final DataAttribute bannerBlockAtt = new DataAttribute("BANNER_BLOCK_CODE", 40);

	public static final DataAttribute prefFirstAtt = new DataAttribute("PREFERRED_NAME", 72);
	public static final DataAttribute cnuEmailAtt = new DataAttribute("CNU_EMAIL", 170);
	public static final DataAttribute bannerSportAtt = new DataAttribute("BANNER_SPRT", 70);
	public static final DataAttribute prStr1Att = new DataAttribute("PRSTR1", 70);
	public static final DataAttribute prStr2Att = new DataAttribute("PRSTR2", 70);
	public static final DataAttribute prCityAtt = new DataAttribute("PRCITY", 70);
	public static final DataAttribute prStAtt = new DataAttribute("PRST", 70);
	public static final DataAttribute prZipAtt = new DataAttribute("PRZIP", 70);


	/* the director of the honors program, if also a core advisor */
	public static Advisor honorsDirector;


	/**
	 * Initialize all the data
	 */
	public static void init() {
		_advisorData = new AdvisorData(_advisorBaseName);

		_schedule = new Schedule(_scheduleBaseName);
		_studentData = new StudentData(_studentsBaseName);
		_alcData = new ALCData(_alcsBaseName);

		new HonorsAdvisors(_honAdvBaseName);
		new PSPAdvisors(_pspAdvBaseName);

		new StudentSchedules(_studentSchedulesBaseName);
		new PresScholarAdvisors(_presScholarAdvBaseName);
		new CCPTAdvisors(_ccptAdvBaseName);

		//run the initial steps
		CheckList.getInstance().initRun();
	}

	/**
	 * Standardize an is to include leading 0's
	 * @param id the id to standardize
	 * @return the fixed id
	 */
	public static String fixId(String id) {
		String fixedId = id.replace("\"", "").trim();

		//add leading 0's
		while (fixedId.length() < 8) {
			fixedId = "0" + fixedId;
		}

		return fixedId;
	}

	/**
	 * Get the data for all ALCs
	 *
	 * @return the ALC data
	 */
	public static ALCData getALCData() {
		return _alcData;
	}

	/**
	 * Get the data for all core advisors
	 * @return the advisor data
	 */
	public static AdvisorData getAdvisorData() {
		return _advisorData;
	}

	/**
	 * Get filtered advisor data
	 * @param bits to match
	 * @return filtered advisor data
	 */
	public static AdvisorData getFilteredAdvisorData(int bits) {
		return _advisorData.subModel(bits);
	}

	/**
	 * Get a list of advisors whose subject matches a major
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Advisor> getAdvisorsForMajor(Major major) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		for (Advisor advisor : _advisorData.getAdvisors()) {
			if (major.isInMajorFamily(advisor.subject)) {
//			if (major == advisor.subject) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}

	/**
	 * Get a list of advisors whose subject matches a major and are not alc advisors
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Advisor> getAdvisorsForMajorNotALC(Major major) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		for (Advisor advisor : _advisorData.getAdvisors()) {
			if (major.isInMajorFamily(advisor.subject) && !advisor.alc()) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}

	/**
	 * Get a list of advisors whose subject matches a major and are  alc advisors
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Advisor> getAdvisorsForMajorALC(Major major) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		for (Advisor advisor : _advisorData.getAdvisors()) {
			if (major.isInMajorFamily(advisor.subject) && advisor.alc()) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}


	/**
	 * Get a list of honors advisors whose subject matches a major
	 *
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Advisor> getHonorsAdvisorsForMajor(Major major) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		for (Advisor advisor : _advisorData.getAdvisors()) {
			if (advisor.honors()) {
				if (major.isInMajorFamily(advisor.subject)) {
//					if (major == advisor.subject) {
					advisors.add(advisor);
				}
			}
		}

		return advisors;
	}

	/**
	 * Get a list of honors advisors whose subject matches a secondary major
	 *
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Advisor> getHonorsAdvisorsForSecondaryMajor(Major major) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		for (Advisor advisor : _advisorData.getAdvisors()) {
			if (advisor.honors()) {
				if ((advisor.preferred2ndMajor != null) && (advisor.preferred2ndMajor != advisor.subject)) {
					if (major.isInMajorFamily(advisor.preferred2ndMajor)) {
						advisors.add(advisor);
					}
				}
			}
		}

		return advisors;
	}

	/**
	 * Get a list advisors whose subject matches a secondary major
	 *
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Advisor> getAdvisorsForSecondaryMajor(Major major) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		for (Advisor advisor : _advisorData.getAdvisors()) {
			if ((advisor.preferred2ndMajor != null) && (advisor.preferred2ndMajor != advisor.subject)) {
				if (major.isInMajorFamily(advisor.preferred2ndMajor)) {
					advisors.add(advisor);
				}
			}
		}

		return advisors;
	}

	/**
	 * Get a list of honors advisors whose subject matches a specialty
	 *
	 * @param specialty the specialty to match
	 * @return the list
	 */
	public static List<Advisor> getHonorsAdvisorsForSpecialty(Specialty specialty) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		if (specialty != Specialty.NONE) {
			for (Advisor advisor : _advisorData.getAdvisors()) {
				if (advisor.honors()) {
					if (advisor.specialty == specialty) {
						advisors.add(advisor);
					}
				}
			}
		}

		return advisors;
	}

	/**
	 * Get a list of advisors whose subject matches a specialty
	 *
	 * @param specialty the specialty to match
	 * @return the list
	 */
	public static List<Advisor> getAdvisorsForSpecialty(Specialty specialty) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		if (specialty != Specialty.NONE) {
			for (Advisor advisor : _advisorData.getAdvisors()) {
				if (advisor.specialty == specialty) {
					advisors.add(advisor);
				}
			}
		}

		return advisors;
	}


	/**
	 * Get a list of advisors whose subject matches a department
	 * @param department the department to match
	 * @return the list
	 */
	public static List<Advisor> getAdvisorsForDepartment(Department department) {
		ArrayList<Advisor> advisors = new ArrayList<>();

		for (Advisor advisor : _advisorData.getAdvisors()) {
			if (department == advisor.department) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}


	/**
	 * Get a list of students whose major matches a given major
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Student> getStudentsForMajor(Major major) {
		ArrayList<Student> students = new ArrayList<>();

		for (Student student : _studentData.getStudents()) {
			if (major == student.major) {
				students.add(student);
			}
		}

		return students;
	}

	/**
	 * Get a list of of all unassigned students
	 * @return the list of unassigned students
	 */
	public static List<Student> getUnassignedStudents() {
		ArrayList<Student> students = new ArrayList<>();

		for (Student student : _studentData.getStudents()) {
			if (!student.assigned()) {
				students.add(student);
			}
		}

		return students;
	}

	/**
	 * Get a list of of all assigned students
	 * @return the list of assigned students
	 */
	public static List<Student> getAssignedStudents() {
		ArrayList<Student> students = new ArrayList<>();

		for (Student student : _studentData.getStudents()) {
			if (student.assigned()) {
				students.add(student);
			}
		}

		return students;
	}


	/**
	 * Get a list of students whose major matches a given major and are unassigned
	 *
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Student> getUnassignedStudentsForMajor(Major major) {
		ArrayList<Student> students = new ArrayList<>();

		for (Student student : _studentData.getStudents()) {
			if (!student.assigned()) {
				if (major == student.major) {
					students.add(student);
				}
			}
		}

		return students;
	}

	/**
	 * Get a list of unassigned honors students
	 *
	 * @return the list
	 */
	public static List<Student> getUnassignedHonorsStudents() {
		ArrayList<Student> students = new ArrayList<>();

		for (Student student : _studentData.getStudents()) {
			if (!student.assigned() && student.honors()) {
				students.add(student);
			}
		}

		return students;
	}

	/**
	 * Get a list of unassigned psp students
	 *
	 * @return the list
	 */
	public static List<Student> getUnassignedPSPStudents() {
		ArrayList<Student> students = new ArrayList<>();

		for (Student student : _studentData.getStudents()) {
			if (!student.assigned() && student.psp()) {
				students.add(student);
			}
		}

		return students;
	}


	/**
	 * Get a list of unassigned honors students whose major matches a given major
	 *
	 * @param major the major to match
	 * @return the list
	 */
	public static List<Student> getUnassignedHonorsStudentsForMajor(Major major) {
		ArrayList<Student> students = new ArrayList<>();

		for (Student student : _studentData.getStudents()) {
			if (!student.assigned() && student.honors()) {
				if (major == student.major) {
					students.add(student);
				}
			}
		}

		return students;
	}

	/**
	 * Get a list of unassigned honors students with a given specialty
	 *
	 * @param specialty the specialty to match
	 * @return the list
	 */
	public static List<Student> getUnassignedHonorsStudentsForSpecialty(Specialty specialty) {
		ArrayList<Student> students = new ArrayList<>();

		if (specialty != Specialty.NONE) {
			for (Student student : _studentData.getStudents()) {
				if (!student.assigned() && student.honors() && student.check(specialty.getBit())) {
					students.add(student);
				}
			}
		}

		return students;
	}

	/**
	 * Get a list of unassigned students with a given specialty
	 *
	 * @param specialty the specialty to match
	 * @return the list
	 */
	public static List<Student> getUnassignedStudentsForSpecialty(Specialty specialty) {
		ArrayList<Student> students = new ArrayList<>();

		if (specialty != Specialty.NONE) {
			for (Student student : _studentData.getStudents()) {
				if (!student.assigned() && student.check(specialty.getBit())) {
					students.add(student);
				}
			}
		}

		return students;
	}

	/**
	 * Get a list of unassigned students with a given bit set
	 *
	 * @param specialty the bit to check
	 * @return the list
	 */
	public static List<Student> getUnassignedStudentsWithBit(int bit) {
		ArrayList<Student> students = new ArrayList<>();

			for (Student student : _studentData.getStudents()) {
				if (!student.assigned() && student.check(bit)) {
					students.add(student);
				}
		}

		return students;
	}

	/**
	 * Get the schedule of classes data
	 * @return the schedule of classes
	 */
	public static Schedule getSchedule() {
		return _schedule;
	}
	/**
	 * Get the data for all students
	 * @return the student data
	 */
	public static StudentData getStudentData() {
		return _studentData;
	}

	public static StudentData getStudentData(List<Student> students) {
		return _studentData.subModel(students);
	}

	public static StudentData getFilteredStudentData(int bits) {
		return _studentData.subModel(bits);
	}

	/**
	 * Get the target cohort size
	 * @return the target cohort size
	 */
	public static double targetCohort() {
		double na = getAdvisorData().count();
		double ns = getStudentData().count();
		return ns/na;
	}

	private static Advisor hasLeastAdvisees(List<Advisor> advisors) {
		int min = Integer.MAX_VALUE;
		Advisor minAdvisor = null;

		for (Advisor advisor : advisors) {
			if (!advisor.locked()) {
				if (advisor.adviseeCount() < min) {
					min = advisor.adviseeCount();
					minAdvisor = advisor;
				}
			}
		}

		return minAdvisor;
	}

	public static int studentsHavingAdvisorAsInstructorCount() {
		int count = 0;
		for (Student student : getStudentData().getStudents()) {

			if (student.hasCourseWithAdvisor()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Assign a group of students to a group of advisors who can take them. The students
	 * are assigned to the advisors equally but if any advisor reached the target limit they do
	 * not get more students. It is possible than not all students get assign.When the students
	 * are assigned they are locked. I.e., this makes immutable assignments.
	 * @param advisors the (sub)list of advisors.
	 * @param students the (sub)list of students
	 * @param lockWhenDone if true, lock the students down
	 * @param errPrompt used in a error message if all advisors max out
	 */
	public static  void roundRobinAssign(List<Advisor> advisors, List<Student> students, boolean lockWhenDone, String errPrompt, EReason reason) {

		int numAdvisor = advisors.size();
		int numStudent = students.size();
		if ((numAdvisor == 0) || (numStudent == 0)) {
			return;
		}

		//the max to assign to any one advisor
		int globalTarget = AdvisorAssign.targetAverage();


		for (Student student : students) {

			if (student.locked()) {
				continue;
			}

			Advisor advisor = hasLeastAdvisees(advisors);

			if ((advisor == null) || (advisor.adviseeCount() >= globalTarget)) {
//				System.out.println(errPrompt + "  (Target reached) RoundRobin failed to assign student: " + student.fullNameAndID()
//				+ " (" + student.major.name() + ")");
				continue;
			}

			advisor.addAdvisee(student, lockWhenDone, reason);

			if (advisor.adviseeCount() >= globalTarget) {
				advisor.setLocked();
			}

		}

	}



}
