package github.forvisual.aac.aac_admin_app.ui.page;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSplitPane;

import github.forvisual.aac.aac_admin_app.ui.page.component.BeforeWorkPanel;
import github.forvisual.aac.aac_admin_app.ui.page.component.WorkFormPanel;

import java.awt.BorderLayout;

public class ImageWorkPage extends JPanel {

	/**
	 * Create the panel.
	 */
	public ImageWorkPage() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane);
		splitPane.setDividerLocation(0.5);
		
		BeforeWorkPanel beforeWorkPanel = new BeforeWorkPanel();
		WorkFormPanel formPanel = new WorkFormPanel();
		formPanel.setFormListener(beforeWorkPanel);
		beforeWorkPanel.setWorkImageListener(formPanel);
		
		splitPane.setLeftComponent(beforeWorkPanel);
		splitPane.setRightComponent(formPanel);

	}

}
