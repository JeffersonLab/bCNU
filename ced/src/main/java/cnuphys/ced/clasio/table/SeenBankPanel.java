package cnuphys.ced.clasio.table;

import javax.swing.*;

import edu.cnu.mdi.ui.fonts.Fonts;
import cnuphys.ced.alldata.DataWarehouse;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

public class SeenBankPanel extends JPanel {
    private JList<String> bankList;
    private DefaultListModel<String> listModel;
    
    private static final int WIDTH = 170;

	public SeenBankPanel() {
		// Initialize the list model and JList
		listModel = new DefaultListModel<>();
		bankList = new JList<>(listModel);
		bankList.setFont(Fonts.smallFont);

		JLabel label = new JLabel("Seen Banks");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(Fonts.biggerFont);
		label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5) // top, left, bottom, right spacing
		));

		// Wrap the JList in a JScrollPane for scrolling as needed
		JScrollPane scrollPane = new JScrollPane(bankList);
		// Set preferred size: width WIDTH pixels; adjust height as required
        scrollPane.setPreferredSize(new Dimension(WIDTH, 300));
        
     // Create the clear button
    	JButton clearButton = new JButton("Clear");
    	clearButton.setFont(Fonts.mediumFont); // Optional: use your desired font
    	clearButton.setBorder(BorderFactory.createCompoundBorder(
    			BorderFactory.createEtchedBorder(),
    			BorderFactory.createEmptyBorder(5, 10, 5, 10)));

    	clearButton.addActionListener(e -> clearSeenBanks());

    	// Panel to center the button
    	JPanel buttonPanel = new JPanel();

    	buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    	buttonPanel.add(clearButton);

    	// Set layout and add components
    	setLayout(new BorderLayout());
    	add(label, BorderLayout.NORTH);
    	add(scrollPane, BorderLayout.CENTER);
    	add(buttonPanel, BorderLayout.SOUTH);
	  }
    
    /**
     * Updates the panel with the new list of banks.
     * This method should be called with the result of getSortedSeenBanks().
     *
     * @param banks the list of bank strings to display.
     */
    public void updateSeenBanks() {
        listModel.clear();
		List<String> bankCounts = DataWarehouse.getInstance().getSortedSeenBanks();

        for (String bankCount : bankCounts) {
            listModel.addElement(bankCount);
        }
    }
    
    //clear the seen banks
	private void clearSeenBanks() {
		DataWarehouse.getInstance().clearSeenBanks();
		updateSeenBanks();
	}
    
}
