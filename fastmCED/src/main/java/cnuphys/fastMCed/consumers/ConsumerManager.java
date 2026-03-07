package cnuphys.fastMCed.consumers;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.fastMCed.eventio.IPhysicsEventListener;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.streaming.IStreamProcessor;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;

/**
 * Managers consumers
 *
 * @author heddle
 *
 */
public class ConsumerManager extends Vector<PhysicsEventConsumer>
		implements IPhysicsEventListener, IStreamProcessor {

	// singleton
	private static ConsumerManager instance;

	// the base class for consumer plugins
	protected Class<PhysicsEventConsumer> _consumerClaz;

	// the menu
	private JMenu _menu;

	// why an event was flagged
	private String _flagExplanation;

	// private singleton constructor
	private ConsumerManager() {
		SocketConsumer socketConsumer = new SocketConsumer();
		socketConsumer.setActive(false);
		add(socketConsumer);
		BinaryFileMLTestData binaryConsumer = new BinaryFileMLTestData();
		binaryConsumer.setActive(true);
		add(binaryConsumer);
		PhysicsEventManager.getInstance().addPhysicsListener(this, 1);
		StreamManager.getInstance().addStreamListener(this);
	}

	/**
	 * Access for the singleton
	 *
	 * @return the singleton ConsumerManager
	 */
	public static ConsumerManager getInstance() {
		if (instance == null) {
			instance = new ConsumerManager();
		}
		return instance;
	}

	// create the menu item

	private boolean firstItem = true;

	private JCheckBoxMenuItem createMenuItem(final PhysicsEventConsumer consumer, boolean selected) {

		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
				consumer.setActive(item.isSelected());
			}

		};

		JCheckBoxMenuItem item = new JCheckBoxMenuItem(consumer.getConsumerName(), selected);
		item.addItemListener(il);

		if (firstItem) {
			_menu.addSeparator();
			firstItem = false;
		}
		return item;
	}

	/**
	 * Get the consumer menu
	 *
	 * @return the consumer menu
	 */
	public JMenu getMenu() {
		if (_menu == null) {
			_menu = new JMenu("Consumers");

			for (PhysicsEventConsumer consumer : this) {
				_menu.add(createMenuItem(consumer, consumer.isActive()));
			}
		}

		return _menu;
	}

	@Override
	public void streamingChange(StreamReason reason) {
		for (PhysicsEventConsumer consumer : this) {
			if (consumer.isActive()) {
				consumer.streamingChange(reason);
			}
		}
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		for (PhysicsEventConsumer consumer : this) {
			if (consumer.isActive()) {
				StreamProcessStatus status = consumer.streamingPhysicsEvent(event, particleHits);
				if (status == StreamProcessStatus.FLAG) {
					System.err.println("FLAGGED");
					_flagExplanation = consumer.flagExplanation() + "\nConsumer: " + consumer.getConsumerName();
					return StreamProcessStatus.FLAG;
				}
			}
		}

		return StreamProcessStatus.CONTINUE;
	}

	@Override
	public String flagExplanation() {
		return _flagExplanation;
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		for (PhysicsEventConsumer consumer : this) {
			if (consumer.isActive()) {
				consumer.newPhysicsEvent(event, particleHits);
			}
		}
	}

}
