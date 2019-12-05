package github.forvisual.aac.aac_admin_app.ui.page;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.table.AbstractTableModel;

import github.forvisual.aac.aac_admin_app.AppContext;
import github.forvisual.aac.aac_admin_app.WorkImage;
import github.forvisual.aac.aac_admin_app.ui.page.component.BeforeWorkPanel;
import github.forvisual.aac.aac_admin_app.ui.page.component.WorkFormPanel;

import java.awt.BorderLayout;
import java.util.List;

public class ImageWorkPage extends JPanel {

	/**
	 * Create the panel.
	 */
	public ImageWorkPage() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane);
		
		BeforeWorkPanel beforeWorkPanel = new BeforeWorkPanel();
		WorkFormPanel formPanel = new WorkFormPanel();
		formPanel.setFormListener(beforeWorkPanel);
		beforeWorkPanel.setWorkImageListener(formPanel);
		
		splitPane.setLeftComponent(beforeWorkPanel);
		splitPane.setRightComponent(formPanel);

		splitPane.setDividerLocation(500);
	}
	
	
	class DepolyImageModel extends AbstractTableModel {

		private List<WorkImage> images;

		public DepolyImageModel() {
			images = AppContext.getInstance().getDeployableImages();
		}
		
		public WorkImage getItem(int rowIndex) {
			return this.images.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return images.size();
		}

		@Override
		public int getColumnCount() {
			/**
			 * img  | origin | filename | size
			 */
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			WorkImage img = images.get(rowIndex);
			if (columnIndex == 0) {
				return img.getImage();
			} else if (columnIndex == 1) {
				return img.getOrigin();
			} else if (columnIndex == 2) {
				return img.getImageFile().getName();
			} else if (columnIndex == 3) {
				return img.getImageFile().length();
			} else {
				throw new RuntimeException("out of range:  index: " + columnIndex);
			}	
		}

		public void removeItemAt(int currentIndex) {
			images.remove(currentIndex);
			// super.fireTableDataChanged();
			fireTableRowsDeleted(currentIndex, currentIndex);
		}
		
	}

}
