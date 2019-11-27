package github.forvisual.aac.aac_admin_app.ui.page;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;

import github.forvisual.aac.aac_admin_app.ui.page.component.search.PictureBaseSearchPanel;

public class SearchPage extends JPanel {

	/**
	 * Create the panel.
	 */
	public SearchPage() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("사진으로", new PictureBaseSearchPanel());
		
	}

}
