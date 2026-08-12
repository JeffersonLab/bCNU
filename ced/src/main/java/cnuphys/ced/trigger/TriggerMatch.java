package cnuphys.ced.trigger;

import java.util.EnumMap;

import edu.cnu.mdi.component.EnumComboBox;

public enum TriggerMatch {

	EXACT, ANY, ALL;

	/**
	 * A map for the names of the match types
	 */
	public static EnumMap<TriggerMatch, String> names = new EnumMap<>(TriggerMatch.class);

	static {
		names.put(EXACT, "  Exact  ");
		names.put(ANY, "    Any  ");
		names.put(ALL, "    All  ");
	}

	/**
	 * Get the nice name of the enum.
	 *
	 * @return the nice name, for combo boxes, menus, etc.
	 */
	public String getName() {
		return names.get(this);
	}

	/**
	 * Get a description of the pattern
	 *
	 * @return a description of the pattern
	 */
	public String getDescription() {
		switch (this) {
		case EXACT:
			return "trigger == pattern          ";
		case ANY:
			return "trigger & pattern != 0      ";
		case ALL:
			return "trigger & pattern == pattern";
		default:
			return "???";
		}
	}

	/**
	 * Test a trigger word against a bit pattern using this match type.
	 *
	 * @param pattern the configured filter pattern
	 * @param triggerWord the trigger word from the event
	 * @return whether the trigger word matches the pattern
	 */
	public boolean matches(int pattern, int triggerWord) {
		return switch (this) {
		case EXACT -> pattern == triggerWord;
		case ANY -> (pattern & triggerWord) != 0;
		case ALL -> (pattern & triggerWord) == pattern;
		};
	}

	/**
	 * Obtain a combo box of choices.
	 *
	 * @param defaultChoice
	 * @return the combo box of match choices
	 */
	public static EnumComboBox<TriggerMatch> getComboBox(TriggerMatch defaultChoice) {
		return new EnumComboBox<>(TriggerMatch.class, defaultChoice, null, TriggerMatch::getName);
	}
}
