package cnuphys.ced.trigger;

import cnuphys.ced.alldata.RunTriggers;
import cnuphys.ced.clasio.filter.AEventFilter;

public class TriggerFilter extends AEventFilter {

	// the actual bits
	private int _bits;

	// the type
	private TriggerMatch _type = TriggerMatch.ANY;

	/**
	 * Create a trigger filter
	 */
	public TriggerFilter() {
		super();
		setActive(false);
	}

	@Override
	public boolean pass() {

		RunTriggers triggers = RunTriggers.getInstance();
		return triggers.hasTriggerRow(0) && _type.matches(_bits, triggers.trigger(0));
	}

	@Override
	protected void toggleActiveState() {
		super.toggleActiveState();
		TriggerDialog.getInstance().getTriggerActiveCheckBox().setSelected(isActive());
	}

	/**
	 * Set the trigger bits of the flter
	 */
	public void setBits(int bits) {
		_bits = bits;
	}

	/**
	 * Get the trigger bits of the filter
	 * @return the trigger bits of the filter
	 */
	public int getBits() {
		return _bits;
	}

	/**
	 * Set the type of trigger match
	 *
	 * @param type the trigger match
	 */
	public void setType(TriggerMatch type) {
		_type = type;
	}

	/**
	 * Get the pattern matching type
	 *
	 * @return the pattern matching type
	 */
	public TriggerMatch getType() {
		return _type;
	}

	/**
	 * Save the preferences to user pref
	 */
	@Override
	public void savePreferences() {
	}

	/**
	 * Read the preferences from the user pref
	 */
	@Override
	public void readPreferences() {
	}

	/**
	 * A builder for a Trigger Filter
	 */
	public static class Builder {

		private TriggerFilter _filter;

		public Builder() {
			_filter = new TriggerFilter();
		}

		public TriggerFilter build() {
			return _filter;
		}

		public Builder setBits(int bits) {
			_filter.setBits(bits);
			return this;
		}

		public Builder setType(TriggerMatch type) {
			_filter.setType(type);
			return this;
		}

		public Builder setActive(boolean active) {
			_filter.setActive(active);
			return this;
		}

		public Builder setName(String name) {
			_filter.setName(name);
			return this;
		}

	}

}
