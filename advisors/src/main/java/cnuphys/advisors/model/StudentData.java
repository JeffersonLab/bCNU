package cnuphys.advisors.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import cnuphys.advisors.Person;
import cnuphys.advisors.Student;
import cnuphys.advisors.dialogs.StudentDialog;
import cnuphys.advisors.io.DataModel;
import cnuphys.advisors.io.ITabled;
import cnuphys.advisors.table.CustomRenderer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;

/**
 * All the students
 * @author heddle
 *
 */
public class StudentData extends DataModel {

	private StudentDialog _dialog;


	//attributes for student data
	private static final DataAttribute studentAttributes[] = { DataManager.rowAtt, DataManager.idAtt, DataManager.lastNameAtt,
			DataManager.firstNameAtt,
			DataManager.alcAtt, DataManager.plpAtt, DataManager.honrAtt, DataManager.prscAtt,
			DataManager.pspAtt, DataManager.prelawAtt, DataManager.windAtt, DataManager.ccapAtt,
			DataManager.majorAtt, DataManager.bannerBlockAtt,
			DataManager.advisorAtt, DataManager.reasonAtt, DataManager.inClassAtt };

	public StudentData(String baseName) {
		super(baseName, studentAttributes);
		setCustomRenderer();
	}

	/**
	 * private constructor used to make a submodel
	 * @param baseModel
	 * @param bits
	 */
	private StudentData(StudentData baseModel, int bits) {
		super(baseModel, bits);
		setCustomRenderer();
	}

	/**
	 * private constructor used to make a submodel
	 * @param baseModel
	 * @param list
	 */
	private StudentData(StudentData baseModel, List<Student> list) {
		super(baseModel, list);
		setCustomRenderer();
	}

	/**
	 * Create a submodel using bit matching
	 */
	public StudentData subModel(int bits) {
		return new StudentData(this, bits);
	}


	/**
	 * Create a submodel using a list
	 */
	public StudentData subModel(List<Student> list) {
		return new StudentData(this, list);
	}

	//set the custom renderer
	private void setCustomRenderer() {

		renderer = new CustomRenderer(this);

		for (int i = 0; i < getColumnCount(); i++) {
			_dataTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
	}

	@Override
	protected void processData() {
		int idIndex = getColumnIndex(DataManager.idAtt);
		int lastIndex = getColumnIndex(DataManager.lastNameAtt);
		int firstIndex = getColumnIndex(DataManager.firstNameAtt);
		int alcIndex = getColumnIndex(DataManager.alcAtt);
		int prscIndex = getColumnIndex(DataManager.prscAtt);
		int windIndex = getColumnIndex(DataManager.windAtt);
		int ccapIndex = getColumnIndex(DataManager.ccapAtt);


		int plpIndex = getColumnIndex(DataManager.plpAtt);
		int honrIndex = getColumnIndex(DataManager.honrAtt);
		int pspIndex = getColumnIndex(DataManager.pspAtt);
//		int prelawIndex = getColumnIndex(DataManager.prelawAtt);
		int majorIndex = getColumnIndex(DataManager.majorAtt);
		int bannerBlockIndex = getColumnIndex(DataManager.bannerBlockAtt);

		int prefFirstIndex = getColumnIndex(DataManager.prefFirstAtt);
		int emailIndex = getColumnIndex(DataManager.cnuEmailAtt);
		int sportIndex = getColumnIndex(DataManager.bannerSportAtt);
		int prStr1Index = getColumnIndex(DataManager.prStr1Att);
		int prStr2Index = getColumnIndex(DataManager.prStr2Att);
		int prCityIndex = getColumnIndex(DataManager.prCityAtt);
		int prStateIndex = getColumnIndex(DataManager.prStAtt);
		int prZipIndex = getColumnIndex(DataManager.prZipAtt);

		for (String s[] : _data) {
			String id = s[idIndex];
    		String lastName = s[lastIndex];
			String firstName = s[firstIndex];
			String alc = s[alcIndex];

			String prsc = s[prscIndex];
			String wind = s[windIndex];
			String ccap = s[ccapIndex];

			String plp = s[plpIndex];
			String honr = s[honrIndex];
			String psp = s[pspIndex];
			String major = s[majorIndex];
			String bannerBlock = s[bannerBlockIndex];

			String prefFirst = s[prefFirstIndex];
			String email = s[emailIndex];
			String sport = s[sportIndex];
			String prStr1 = s[prStr1Index];
			String prStr2 = s[prStr2Index];
			String prCity = s[prCityIndex];
			String prState = s[prStateIndex];
		    String prZip = s[prZipIndex];


			_tableData.add(new Student(id, lastName, firstName, alc, plp, honr, prsc, psp, wind, ccap, major, bannerBlock,
					prefFirst, email, sport, prStr1, prStr2, prCity, prState, prZip));
		}

		//raw data not needed
		deleteRawData();

	}

	/**
	 * Get a highlight background color for a given row and column
	 * @param row the 0-based row
	 * @param column the 0-based column
	 * @return the highlight backgroundcolor, if null use default (black)
	 */
    @Override
	public Color getHighlightBackgroundColor(int row, int column) {
		Student student = getStudentFromRow(row);
		return (student.honors()) ? X11Colors.getX11Color("alice blue") : Color.white;
	}

	/**
	 * Get a highlight font for a given row and column
	 * @param row the 0-based row
	 * @param column the 0-based column
	 * @return the highlight color, if null use medium font
	 */
	@Override
	public Font getHighlightFont(int row, int column) {

		Student student = getStudentFromRow(row);
		boolean locked = student.locked();
		boolean plp = student.check(Person.PLP);

		Font font;
		if (locked && plp) {
			font = Fonts.mediumItalicBoldFont;
		}
		else if (locked) {
			font = Fonts.mediumItalicFont;
		}
		else if (plp) {
			font = Fonts.mediumBoldFont;
		}
		else {
			font = Fonts.mediumFont;
		}

		return font;
	}

	/**
	 * Get a highlight text color for a given row and column
	 *
	 * @param row    the 0-based row
	 * @param column the 0-based column
	 * @return the hightlight color, if null use default (black)
	 */
	@Override
	public Color getHighlightTextColor(int row, int column) {
		Student student = getStudentFromRow(row);
		if ((column == 2) || (column == 3)) {
			return student.locked() ? Color.gray : Color.black;
		}
		else {
			return Color.black;
		}
	}


 	/**
 	 * Get the student at the given 0-based row
 	 * @param row the row
 	 * @return the student at the given row
 	 */
     public Student getStudentFromRow(int row) {
     	return (Student)getFromRow(row);
	}

	/**
	 * Get the student with matching Id
	 *
	 * @param id the id to match
	 * @return the student with matching id or null
	 */
	public Student getStudentFromId(String id) {

		for (Student student : getStudents()) {
			if (student.id.equals(id)) {
				return student;
			}
		}
		return null;
	}

	/**
	 * Get the number of assigned students from student
	 * @return the number of assigned students
	 */
	public int getAssignedStudentCount() {
		int count = 0;
		for (Student student : getStudents()) {
			if (student.assigned()) {
				count ++;
			}
		}
		return count;
	}


	/**
	 * Get all the students in a list
	 * @return all the students
	 */
    public List<Student> getStudents() {

    	ArrayList<Student> list = new ArrayList<>();
		for (ITabled itabled : _tableData) {
			Student student = (Student) itabled;
			list.add(student);
		}

		return list;
    }


	@Override
	public void valueChanged(ListSelectionEvent e) {
	}


	/**
	 * Double clicked on a row
	 * @param row the 0-based row
	 * @param o the object at that location
	 */
	@Override
	protected void doubleClicked(int row, ITabled o) {
		Student student = (Student)o;


		Rectangle bounds = null;

		if (_dialog != null) {
			bounds = _dialog.getBounds();
			_dialog.setVisible(false);
		}

		_dialog = new StudentDialog(student);

		if (bounds != null) {
			_dialog.setBounds(bounds);
		}

		_dialog.setVisible(true);
	}


}
