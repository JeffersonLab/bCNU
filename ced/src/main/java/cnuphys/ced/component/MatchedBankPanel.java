package cnuphys.ced.component;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.TextUtilities;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.clasio.ClasIoPresentBankPanel;

public class MatchedBankPanel extends JPanel {

	// the view owner
	private IBankMatching _bankMatcher;

	//the text area for matches
	private JTextArea _matchTextArea;

	// relevant present banks
	private ClasIoPresentBankPanel _presentBankPanel;

	private BaseView _view;


	/**
	 * Create a panel for matching banks
	 *
	 * @param view        the view owner
	 * @param bankMatcher the bank matcher (often the same object)
	 */
	public MatchedBankPanel(BaseView view, IBankMatching bankMatcher) {
		_view = view;
		_bankMatcher= bankMatcher;
		setLayout(new BorderLayout(4, 4));

		makeTextArea();
		makeBankPanel();
	}

	//create the text area for entering banks
	private void makeTextArea() {

		_matchTextArea = new JTextArea(6, 20);
		_matchTextArea.setLineWrap(true);
		_matchTextArea.setEditable(true);
		_matchTextArea.setFont(Fonts.mediumFont);
		_matchTextArea.setText(matchesToString());

		_matchTextArea.setBorder(new CommonBorder("Enter matches, comma separated, case sensitive"));

		add(_matchTextArea, BorderLayout.NORTH);

		KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					applyTextMatches();
				}
			}
		};
		_matchTextArea.addKeyListener(keyAdapter);

		FocusAdapter focusAdapter = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				applyTextMatches();
			}

		};
		_matchTextArea.addFocusListener(focusAdapter);
	}

	private void applyTextMatches() {
		try {
			stringToMatches();
		} catch (RuntimeException exception) {
			Log.getInstance().error("Could not update matched bank columns");
			Log.getInstance().exception(exception);
			_matchTextArea.setText(matchesToString());
		}
	}


	//create the bank panel
	private void makeBankPanel() {
		_presentBankPanel = ClasIoPresentBankPanel.createPresentBankPanel(_view, null, 18);
		add(_presentBankPanel.getScrollPane(), BorderLayout.CENTER);
	}

	//convert the string in the text area to an array of matches
	private void stringToMatches() {
		String[] matches = parseMatches(_matchTextArea.getText());
		if (matches == null) {
			_matchTextArea.setText(matchesToString());
			return;
		}

		_bankMatcher.setBankMatches(matches);
		_matchTextArea.setText(matchesToString());
		_bankMatcher.writeCommonProperties();
	}

	static String[] parseMatches(String text) {
		if (text == null) {
			return null;
		}

		String compactText = text.replaceAll("\\s", "");
		return compactText.isEmpty() ? null : TextUtilities.tokens(compactText, ",");
	}

	//convert the view matches to a comma separated string
	public String matchesToString() {
		return TextUtilities.stringArrayToString(_bankMatcher.getBanksMatches());
	}


	public void update() {
		_matchTextArea.setText(matchesToString());
	}


	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}


}
