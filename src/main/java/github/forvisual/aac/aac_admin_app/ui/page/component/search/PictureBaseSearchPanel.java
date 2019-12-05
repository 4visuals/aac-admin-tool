package github.forvisual.aac.aac_admin_app.ui.page.component.search;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import github.forvisual.aac.aac_admin_app.AppContext;
import github.forvisual.aac.aac_admin_app.Util;
import github.forvisual.aac.aac_admin_app.WorkImage;
import github.forvisual.aac.aac_admin_app.model.Category;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;

public class PictureBaseSearchPanel extends JPanel {
	
	private DefaultComboBoxModel<Category> categoryCbModel;
	final private JComboBox<Category> categoryCbBox;
	
	private JTable table;
	private WorkImageModel tableModel;


	/**
	 * Create the panel.
	 */
	public PictureBaseSearchPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		{
			categoryCbModel = new DefaultComboBoxModel<>();
			List<Category> cates = AppContext.getInstance().getCategories();
			for (Category c : cates) {
				categoryCbModel.addElement(c);
			}
			categoryCbBox = new JComboBox<Category>(categoryCbModel);
		}
		
		{
			// table
			DefaultTableColumnModel cmodel = new DefaultTableColumnModel();
			TableColumn picColumn = new TableColumn(0, 100);
			picColumn.setHeaderValue("그림상징");
			picColumn.setMinWidth(100);
			picColumn.setMaxWidth(200);
			picColumn.setCellRenderer(new PictureRenderer());
			cmodel.addColumn(picColumn);
			
			TableColumn wordColumn = new TableColumn(1, 600);
			wordColumn.setHeaderValue("어휘");
			WordValueEditor handler = new WordValueEditor();
			wordColumn.setCellEditor(handler);
//			wordColumn.setCellRenderer(handler);
			cmodel.addColumn(wordColumn);
			
			TableColumn categoryColumn = new TableColumn(2, 100);
			categoryColumn.setHeaderValue("카테고리");
			categoryColumn.setCellRenderer(new CategoryCellRenderer());
			categoryColumn.setCellEditor(new DefaultCellEditor(categoryCbBox));
			cmodel.addColumn(categoryColumn);
			
			
			tableModel = new WorkImageModel();
			table = new JTable(tableModel, cmodel);
			table.setRowHeight(100);
			
			scrollPane.setViewportView(table);
			
		}
	}
	
	private class WorkImageModel extends AbstractTableModel {

		private List<WorkImage> images;

		public WorkImageModel() {
			images = AppContext.getInstance().getWorkingImages();
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
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
			 * img  | desc | category
			 */
			return 3;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if ( columnIndex == 0 ) {
				return BufferedImage.class;
			} else if ( columnIndex == 1) {
				return List.class;
			} else if (columnIndex == 2) {
				return Category.class;
			}
			else {
				throw new RuntimeException("no such column : " + columnIndex);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			WorkImage img = images.get(rowIndex);
			if (columnIndex == 0) {
				return img.getImage();
			} else if (columnIndex == 1) {
				return String.join(",", img.getDescriptions().toArray(new String[img.getDescriptions().size()]));
			} else if (columnIndex == 2) {
				return findCategory(img.getCateSeq());
			} else {
				throw new RuntimeException("out of range:  index: " + columnIndex);
			}	
		}
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			WorkImage img = images.get(rowIndex);
			if (columnIndex == 0) {
				;
			} else if (columnIndex == 1) {
				String text = (String) aValue;
				List<String> desc = Util.split(text, ",");
				AppContext.getInstance().replaceWords(img, desc);
				img.setDescription(desc);
				this.fireTableDataChanged();
			} else if (columnIndex == 2) {
				// return findCategory(img.getCateSeq());
				Category cate = (Category) aValue;
				img.setCateSeq(cate.getNum());
				AppContext.getInstance().updateCategory(img.getSeq(), cate.getNum());
				// ctx.cateDao.updateCategory(pic.getPicSeq(), cate.getSeq());
				
				// TODO 디비로 업데이트 해야 함
				// Excel.updateCell(userWordExcel, "main", rowIndex + 1, columnIndex, cate.getSeq());
				this.fireTableDataChanged();
			} else {
				throw new RuntimeException("out of range:  index: " + columnIndex);
			}	
		}

		private Category findCategory(int cateSeq) {
			int size = categoryCbModel.getSize();
			for(int i = 0 ; i < size ; i++) {
				Category c = categoryCbModel.getElementAt(i);
				if (c.getNum() == cateSeq) {
					return c;
				}
			}
			return null;
		}
		
	}
	class WordValueEditor extends DefaultCellEditor {

		
		public WordValueEditor() {
			super(new JTextField());
			JTextField tf = (JTextField) editorComponent;
		}

		@Override
		public Object getCellEditorValue() {
			
			String text = (String) super.getCellEditorValue();
			text = normalize(text);
			return text;
		}

		private String normalize(String text) {
			String [] tokens = text.split(",");
			StringBuilder sb = new StringBuilder();
			for (String tk : tokens) {
				String s = tk.replaceAll("\\s", "");
				sb.append(s);
				sb.append(',');
			}
			while(sb.length() > 0 && sb.charAt(sb.length()-1) == ',') {
				sb.delete(sb.length()-1, sb.length());
			}
			return sb.toString();
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
			this.img = ((BufferedImage) value);
			// fname = pic.getPicName(); // "userpic/x3333.png"
//			img = ctx.pictureDao.readPicture(pic);
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
		void paintText(Graphics g, String text, int W, int H) {
			FontMetrics fm= g.getFontMetrics();
			int w = W;
			int h = fm.getHeight() + 3;
			g.setColor(picNameBgColor);
			g.fillRect(0, H - h, w, h);
			
			g.setColor(Color.BLACK);
			g.drawString(fname, 2, H - 3);
			
		}
	}
	
	class CategoryCellRenderer extends JComponent implements TableCellRenderer {
		Category category;
		int gap = 5;
		Color bgColor = Color.WHITE;
		Color textColor = Color.BLACK;
		private RenderingHints hints;
		
		public CategoryCellRenderer() {
			hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			
		}
		@Override
		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			
			this.category = (Category) value;
			bgColor = isSelected ? table.getSelectionBackground() : Color.WHITE;
			textColor = isSelected ? table.getSelectionForeground() : Color.BLACK;
			return this;
		}
		
		@Override
		protected void paintComponent(Graphics g1) {
			Graphics2D g = (Graphics2D) g1;
			int W = getWidth();
			int H = getHeight();
			g.setColor(bgColor);
			g.fillRect(0, 0, W, H);
			
			g.setRenderingHints(hints);
			String categoryName = category.getName();
			FontMetrics fm = g.getFontMetrics();
			double fontHeight = fm.getHeight();
			Rectangle2D area = new Rectangle2D.Double(gap, (H - fontHeight)/2, fontHeight, fontHeight);
			// g.translate(arrowRect.getX(), arrowRect.getY());
			paintArrow(g, area);
			// g.translate(-arrowRect.getX(), -arrowRect.getY());
			
			area.setRect(area.getMaxX() + gap, area.getMinY(), W - area.getMaxX() - 2*gap, area.getHeight());
			paintText(g, categoryName, area, fm);
		}
		void paintText(Graphics2D g, String text, Rectangle2D area, FontMetrics fm ) {
			g.setColor(textColor);
			g.drawString(text, (int)area.getMinX(), (int)(area.getMinY()+ fm.getAscent()));
			
		}
		void paintArrow(Graphics2D g, Rectangle2D r) {
			g.setColor(Color.BLACK);
			int mg = 4;
			int [] xs = {(int) r.getMinX()+mg, (int) r.getCenterX(), (int) r.getMaxX()-mg};
			int [] ys = {(int) r.getMinY()+mg, (int) r.getMaxY()-mg, (int) r.getMinY()+mg};
			Polygon p = new Polygon(xs, ys, 3);
			g.fill(p);
		}
		
	}

}
