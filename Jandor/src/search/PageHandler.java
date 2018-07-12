package search;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JLabel;

import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import ui.pwidget.PSpinner;

public abstract class PageHandler<T> extends PPanel {

	public static final int DEFAULT_PAGE_SIZE = 15;
	
	protected List<T> items;
	protected int pageSize;
	protected int pageCount;
	protected int currentPage;
	
	protected PSpinner pageSpinner;
	protected JLabel resultsLabel;
	
	public PageHandler(List<T> list) {
		this(list, DEFAULT_PAGE_SIZE);
	}
	
	public PageHandler(List<T> list, int pageSize) {
		this.items = list;
		this.pageSize = pageSize;
		currentPage = 1;
		pageCount = (int) Math.max(Math.ceil(list.size() / (double) pageSize), 1);
		init();
	}
	
	protected abstract void handlePageChange(List<T> pageItems);

	public void triggerChange() {
		updateResultsLabel();
		setVisible(true); //pageCount > 1);
		handlePageChange(getPageItems());
	}
	
	protected void init() {
		PButton firstPageButton = new PButton("<<");
		firstPageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				firstPage();
			}
			
		});
		firstPageButton.setPreferredSize(new Dimension(30, 20));
		
		PButton prevPageButton = new PButton("<");
		prevPageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				prevPage();
			}
			
		});
		prevPageButton.setPreferredSize(new Dimension(30, 20));
		
		pageSpinner = new PSpinner(currentPage, 1, pageCount) {

			@Override
			protected void handleChange(int value) {
				setPage(value);
			}
			
		};
		pageSpinner.setPreferredWidth(50);
		
		PButton nextPageButton = new PButton(">");
		nextPageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				nextPage();
			}
			
		});
		nextPageButton.setPreferredSize(new Dimension(30, 20));
		
		PButton lastPageButton = new PButton(">>");
		lastPageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				lastPage();
			}
			
		});
		lastPageButton.setPreferredSize(new Dimension(30, 20));
		
		resultsLabel = new JLabel("");
		resultsLabel.setForeground(Color.WHITE);
		
		c.insets(10, 10, 10, 0);
		add(firstPageButton, c);
		c.gridx++;
		c.insets(10, 10, 10, 10);
		add(prevPageButton, c);
		c.gridx++;
		c.insets(10, 10, 10, 0);
		add(new JLabel("Page"), c);
		c.insets(10, 10, 10, 10);
		c.gridx++;
		add(pageSpinner, c);
		c.gridx++;
		c.insets(10, 0, 10, 10);
		add(new JLabel("of  " + pageCount), c);
		c.insets(10, 10, 10, 0);
		c.gridx++;
		add(nextPageButton, c);
		c.gridx++;
		add(lastPageButton, c);
		c.gridx++;
		c.insets(10, 40, 10, 10);
		add(resultsLabel, c);
		
		updateResultsLabel();
	}
	
	public void nextPage() {
		if(currentPage < pageCount) {
			pageSpinner.setValue(currentPage + 1);
		}
	}
	
	public void prevPage() {
		if(currentPage > 1) {
			pageSpinner.setValue(currentPage - 1);
		}
	}
	
	public void firstPage() {
		pageSpinner.setValue(1);
	}
	
	public void lastPage() {
		pageSpinner.setValue(pageCount);
	}
	
	public void setPage(int page) {
		if(page < 1) {
			currentPage = 1;
			triggerChange();
		} else if(page > pageCount) {
			currentPage = pageCount;
			triggerChange();
		} else {
			currentPage = page;
			triggerChange();
		}
	}
	
	public void updateResultsLabel() {
		resultsLabel.setText("Showing cards " + (getStartIndex() + 1) + " - " + (getEndIndex()) + " of " + items.size() + " results.");
	}
	
	public void setItems(List<T> items) {
		this.items = items;
	}
	
	public List<T> getAllItems() {
		return items;
	}
	
	public List<T> getPageItems() {
		return items.subList(getStartIndex(), getEndIndex());
	}
	
	private int getStartIndex() {
		return (currentPage-1) * pageSize;
	}
	
	private int getEndIndex() {
		return Math.min(items.size(), getStartIndex() + pageSize);
	}
	
	public void refresh() {
		pageCount = (int) Math.round(items.size() / (double) pageSize);
		if(currentPage > pageCount) {
			currentPage = pageCount;
		}
	}
	
}
