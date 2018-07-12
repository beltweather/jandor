package ui.pwidget;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.TableModel;



public class PTable extends JTable implements ICreatedToolTip {

	public static final int ROW_HEIGHT = 150;
	
	private PPanel tooltipPanel;
	private JToolTip tooltip;
	
	private PTableKeyHolder tableKeyHolder;
	private boolean initialized = false;
	private int mousedRow = -1;
	private int mousedCol = -1;
	
	private int lastToolRow = -1;
	private int lastToolCol = -1;
	
	public PTable(PTableModel model) {
		super(model);
		init(model);
	}
	
	private void init(PTableModel model) {
		initialized = true;
		tableKeyHolder = new PTableKeyHolder();
		setRowHeight(ROW_HEIGHT);
		setShowGrid(true);
		setGridColor(ColorUtil.TABLE_GRID_COLOR);
		setIntercellSpacing(new Dimension(1, 1));
		setDefaultRenderer(Object.class, model.createRenderer());
		tableKeyHolder.setTableKey(model.createTableKey());
		setPreferredSize(new Dimension(700, 210));
		setMaximumSize(new Dimension(700, 210));
		setMinimumSize(new Dimension(700, 210));
		setBackground(ColorUtil.TABLE_BG_COLOR);
		setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, getGridColor()));
		setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		addMouseMotionListener(new MouseMotionAdapter(){
			
			@Override
			public void mouseMoved(MouseEvent e) {
				mousedRow = rowAtPoint(e.getPoint());
				mousedCol = columnAtPoint(e.getPoint());
				//repaint();
			}
			
		});
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				mousedRow = -1;
				mousedCol = -1;
				//repaint();
			}
			
		});
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		return new Dimension((int) size.getWidth(), getRowCount() * getRowHeight());
	}
	
	@Override
	public void setModel(TableModel model) {
		super.setModel(model);
		if(initialized && model instanceof PTableModel) {
			setDefaultRenderer(Object.class, ((PTableModel) model).createRenderer());
			tableKeyHolder.setTableKey(((PTableModel) model).createTableKey());
		}
	}
	
	public void refresh() {
		setModel(getModel());
		repaint();
	}
	
	public PTableKeyHolder getTableKeyHolder() {
		return tableKeyHolder;
	}

	public int getRowUnderMouse() {
		return mousedRow;
	}
	
	public int getColUnderMouse() {
		return mousedCol;
	}
	
	@Override
	public JToolTip createToolTip() {
		if(tooltip != null) {
			return tooltip;
		}
		
		tooltip = new JToolTip(){
			
			@Override
			public Dimension getPreferredSize() {
				if (getLayout() != null) {
			        return getLayout().preferredLayoutSize(tooltip);
			    }
			    return super.getPreferredSize();
			}
			
		};
		
		tooltipPanel = new PPanel();
		
		if(getModel() != null) {
			tooltipPanel.add(((PTableModel) getModel()).createTooltipContent(this, mousedRow, mousedCol), tooltipPanel.c);
		}
		
		tooltip.setComponent(this);
		tooltip.setLayout(new GridBagLayout());
		tooltip.setBackground(new Color(0, 0, 0, 0));
		tooltip.setOpaque(false);
		tooltip.setBorder(null);
		G c = JUtil.gbc();
		JUtil.strengthen(c);
		tooltip.add(tooltipPanel, c);
		
		return tooltip;
	}
	
	@Override
	public Point getToolTipLocation(MouseEvent e) {

		int row = rowAtPoint(e.getPoint());
		int col = columnAtPoint(e.getPoint());
		if(row < 0 || col < 0 || tooltipPanel == null) {
			return e.getPoint();
		}
		
		if(row != lastToolRow || col != lastToolCol) {
			lastToolRow = row;
			lastToolCol = col;
			tooltipPanel.clear();
			
			JComponent content = ((PTableModel) getModel()).createTooltipContent(this, row, col);
			if(content != null) {
				tooltipPanel.add(content, tooltipPanel.c);
			}
			
			tooltipPanel.revalidate();
		}
		
		return JUtil.fitOnScreen(e, tooltipPanel);
	}
	
	public void setShowTooltip(boolean show) {
		if(show) {
			setToolTipText(" ");
		} else {
			setToolTipText(null);
		}
	}

	@Override
	public void setToolTipCreator(ToolTipCreator creator) {}

	@Override
	public ToolTipCreator getToolTipCreator() {
		return ((PTableModel) getModel()).getToolTipCreator();
	}
	
}
