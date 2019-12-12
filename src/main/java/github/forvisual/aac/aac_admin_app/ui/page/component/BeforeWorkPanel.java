package github.forvisual.aac.aac_admin_app.ui.page.component;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import github.forvisual.aac.aac_admin_app.AppContext;
import github.forvisual.aac.aac_admin_app.Util;
import github.forvisual.aac.aac_admin_app.WorkImage;
import github.forvisual.aac.aac_admin_app.ui.page.BulkImageDialog;

import javax.swing.JButton;
import java.awt.FlowLayout;
/**
 * 작업해야할 사진들 나오는 화면
 * 
 * @author sue10
 *
 */
public class BeforeWorkPanel extends JPanel  implements FormResultListener {
	
	private int rowSize = 200;
	private int count = 0; // 7890
	private JTable table;
	WorkImageModel tableModel;
	
	WorkImageListener listener;
	
	Font font = new Font("나눔고딕", Font.BOLD, 20);
	private JLabel countLbl;

	/**
	 * Create the panel.
	 */
	public BeforeWorkPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		TableColumn picColumn = new TableColumn(0, 200);
		picColumn.setHeaderValue("그림");
		picColumn.setMinWidth(100);
		picColumn.setMaxWidth(200);
		picColumn.setCellRenderer(new PictureRenderer());
		columnModel.addColumn(picColumn);
		
		TableColumn originColumn = new TableColumn(1, 200);
		originColumn.setHeaderValue("출처");
		columnModel.addColumn(originColumn);
		
		TableColumn fileNameColumn = new TableColumn(2, 600);
		fileNameColumn.setHeaderValue("이름");
		columnModel.addColumn(fileNameColumn);
		
		TableColumn fileLenColumn = new TableColumn(3, 200);
		fileLenColumn.setHeaderValue("크기");
		columnModel.addColumn(fileLenColumn);
		
		
		
		tableModel = new WorkImageModel();
		table = new JTable(tableModel, columnModel);
		table.setRowHeight(rowSize);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				if (!e.getValueIsAdjusting() ) {
					int rowIndex = table.getSelectedRow();
					if (rowIndex == -1) {
						return;
					}
					WorkImage img = tableModel.getItem(rowIndex);
					notifyToListener(img);
				}
			}
		});
		scrollPane.setViewportView(table);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		add(panel, BorderLayout.SOUTH);
		
		JButton btnTrash = new JButton("TRASH");
		btnTrash.addActionListener(e->openDialog("TRASH"));
		panel.add(btnTrash);
		
		JButton btnReuse = new JButton("REUSE");
		btnReuse.addActionListener(e->openDialog("REUSE"));
		panel.add(btnReuse);
		
		count = table.getRowCount(); 
		countLbl = new JLabel("이미지 개수: " + count + "개");
//		countBtn.addActionListener(e->updateCount());
		panel.add(countLbl);

	}
	
	private void updateCount() {
		tableModel.reload();
		countLbl.setText("이미지 개수: " + table.getRowCount() + "개");
	}

	private void openDialog(String mode) {
		
		BulkImageDialog d = new BulkImageDialog();
		d.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				updateCount();
			}
		});
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setMode(mode);
		d.setModal(true);
		d.setVisible(true);
		
	}

	protected void notifyToListener(WorkImage img) {
		if (listener != null) {
			listener.workImageSelected(img);
		}
	}

	public void setWorkImageListener(WorkFormPanel formPanel) {
		listener = formPanel;
	}
	
	class WorkImageModel extends AbstractTableModel {

		private List<WorkImage> images;

		public WorkImageModel() {
			images = AppContext.getInstance().getBeforeImages();
		}
		
		public void reload() {
			images = AppContext.getInstance().getBeforeImages();
			fireTableDataChanged();
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
	
	class PictureRenderer extends JComponent implements TableCellRenderer {
		int gap = 4;
		BufferedImage img ;
		String fname;
		private Color bgColor = Color.WHITE;
		private Color picNameBgColor ;
		public PictureRenderer() {
			setPreferredSize(new Dimension(100, 100));
			setSize(getPreferredSize());
			picNameBgColor = new Color(255, 255, 255, 200);
		}
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			img = (BufferedImage) value;
			bgColor = isSelected ? table.getSelectionBackground() : Color.WHITE;
			return this;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			int W = getWidth();
			int H = getHeight();
			g.setColor(bgColor);
			g.fillRect(0, 0, W, H);
			if (img != null) {
				int sz = Math.min(W, H);
				// g.drawImage(img, gap, gap, sz-gap, sz-gap, 0, 0, img.getWidth(), img.getHeight(), null);
				Util.renderImage((Graphics2D)g, img, gap, gap, W-2*gap, H-2*gap, Util.AlignmentX.CENTER);
			}
			
			// paintText(g, fname, W, H);
			
		}
		
	}

	@Override
	public void submitted(WorkImage img, boolean result) {
		if (result) {
			int currentIndex = table.getSelectionModel().getMaxSelectionIndex();
			tableModel.removeItemAt(currentIndex);
			// table.clearSelection();
			int cnt = table.getRowCount();
			if (cnt > 0 ) {
				table.getSelectionModel().setSelectionInterval(currentIndex, currentIndex);				
			}
			countLbl.setText("이미지 개수: " + cnt + "개");
			
		}
	}
	
	@Override
	public void moveToReuse(WorkImage image) {
		int currentIndex = table.getSelectionModel().getMaxSelectionIndex();
		tableModel.removeItemAt(currentIndex);
	}
	@Override
	public void moveToTrashCan(WorkImage image) {
		;
	}
}
