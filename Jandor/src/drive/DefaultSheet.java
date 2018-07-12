package drive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sheets.domain.UserSheet;
import util.DebugUtil;
import util.DriveUtil;

public class DefaultSheet {

	private List<List<String>> data;
	private String spreadsheetId;
	private String tabName;
	private String rangeStart;
	private String rangeEnd;

	public DefaultSheet(String spreadsheetId, String tabName, String rangeStart, String rangeEnd) {
		this(spreadsheetId, tabName, rangeStart, rangeEnd, true);
	}

	public DefaultSheet(String spreadsheetId, String tabName, String rangeStart, String rangeEnd, boolean read) {
		this.spreadsheetId = spreadsheetId;
		this.tabName = tabName;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.data = new ArrayList<List<String>>();
		if(read) {
			read();
		}
	}
	
	public String getSpreadsheetId() {
		return spreadsheetId;
	}

	public void setSpreadsheetId(String spreadsheetId) {
		this.spreadsheetId = spreadsheetId;
	}

	public String getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(String rangeStart) {
		this.rangeStart = rangeStart;
	}

	public String getRangeEnd() {
		return rangeEnd;
	}

	public void setRangeEnd(String rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}
	
	public void read() {
		if(DebugUtil.OFFLINE_MODE) {
			return;
		}
		convertValues(DriveUtil.getValues(spreadsheetId, tabName, rangeStart, rangeEnd));
	}
	
	public void write() {
		if(DebugUtil.OFFLINE_MODE) {
			return;
		}
		DriveUtil.setValue(spreadsheetId, tabName, rangeStart, rangeEnd, this);
	}

	public void writeRead() {
		write();
		read();
	}
	
	public void writeReadPrint() {
		write();
		read();
		print();
	}
	
	public void print() {
		if (getRowCount() == 0) {
            System.out.println("No data found.");
        } else {
        	for (List<String> row : getRows()) {
        		System.out.println(row);
        	}
        }
	}
	
	private void convertValues(List<List<Object>> values) {
		if(values == null) {
			data = new ArrayList<List<String>>();
		} else {
			int i = 0;
			data = new ArrayList<List<String>>();
			for(List<Object> row : values) {
				data.add(new ArrayList<String>());
				for(Object value : row) {
					data.get(i).add(String.valueOf(value));
				}
				i++;
			}
		}
	}
	
	public List<List<String>> getRows() {
		return data;
	}
	
	public List<List<String>> getCols() {
		List<List<String>> cols = new ArrayList<List<String>>();
		for(int i = 0; i < getMinColCount(); i++) {
			cols.add(getCol(i));
		}
		return cols;
	}
	
	public int getRowCount() {
		return data == null ? 0 : data.size();
	}
	
	public int getColCount() {
		return getMaxColCount();
	}
	
	private int getMaxColCount() {
		int maxCols = 0;
		for(List<String> row : data) {
			if(row.size() > maxCols) {
				maxCols = row.size();
			}
		}
		return maxCols;
	}
	
	private int getMinColCount() {
		int minCols = Integer.MAX_VALUE;
		for(List<String> row : data) {
			if(row.size() < minCols) {
				minCols = row.size();
			}
		}
		return minCols;
	}
	
	public List<String> getRow(int index) {
		if(index < 0 || index >= data.size()) {
			return null;
		}
		return data.get(index);
	}
	
	public int getRowIndex(String idColHeader, String id) {
		List<String> headers = getRow(0);
		int idCol = headers.indexOf(idColHeader);
		return getCol(idCol).indexOf(id);
	}
	
	public List<String> getRow(String idColHeader, String id) {
		return getRow(getRowIndex(idColHeader, id));
	}
	
	public List<String> getCol(int index) {
		if(index < 0 || index >= getMinColCount()) {
			return null;
		}
		List<String> col = new ArrayList<String>();
		for(List<String> row : data) {
			col.add(row.get(index));
		}
		return col;
	}
	
	public String getValue(int row, int col) {
		if(row < 0 || col < 0 || row >= getRowCount() || col >= getColCount()) {
			return null;
		}
		return getRow(row).get(col);
	}
	
	public String getValue(String idColHeader, String id, String valueColHeader) {
		List<String> headers = getRow(0);
		int idCol = headers.indexOf(idColHeader);
		int col = headers.indexOf(valueColHeader);
		return getValue(idCol, id, col);
	}
	
	public String getValue(int idCol, String id, int valueCol) {
		int row = getCol(idCol).indexOf(id);
		return getValue(row, valueCol);
	}
	
	public boolean exists(String idColHeader, String id) {
		return getValue(idColHeader, id, idColHeader) != null;
	}
	
	public void setValue(int row, int col, String value) {
		if(row < 0 || col < 0 || row >= getRowCount() || col >= getColCount()) {
			return;
		}
		getRow(row).set(col, value);
	}

	public void setValue(String idColHeader, String id, String valueColHeader, String value) {
		List<String> headers = getRow(0);
		int idCol = headers.indexOf(idColHeader);
		int col = headers.indexOf(valueColHeader);
		setValue(idCol, id, col, value);
	}
	
	public void setValue(int idCol, String id, int valueCol, String value) {
		int row = getCol(idCol).indexOf(id);
		setValue(row, valueCol, value);
	}
	
	public void setRow(int rowIndex, List<String> values) {
		setRow(rowIndex, values);
	}
	
	public void setRow(int rowIndex, String... row) {
		if(rowIndex < 0 || rowIndex >= data.size()) {
			return;
		}
		if(data.get(rowIndex).size() < row.length) {
			return;
		}
		
		for(int i = 0; i < row.length; i++) {
			data.get(rowIndex).set(i, row[i]);
		}
	}
	
	public void insertRow(int rowIndex, String... row) {
		insertRow(rowIndex, Arrays.asList(row));
	}
	
	public void insertRow(int rowIndex, List<String> row) {
		data.add(rowIndex, row);
	}
	
	public void appendRow(String... row) {
		appendRow(Arrays.asList(row));
	}
	
	public void appendRow(List<String> row) {
		data.add(row);
	}
	
	public void clearRow(int row) {
		if(row < 0 || row >= getRowCount()) {
			return;
		}
		for(int col = 0; col < getColCount(); col++) {
			setValue(row, col, "");
		}
	}
	
	public void deleteRow(int row) {
		if(row < 0 || row >= getRowCount()) {
			return;
		}
		data.remove(row);
		data.add(buildEmptyRow());
	}
	
	private List<String> buildEmptyRow() {
		List<String> row = new ArrayList<String>();
		for(int i = 0; i < getColCount(); i++) {
			row.add("");
		}
		return row;
	}
	
	public List<List<Object>> getRawData() {
		List<List<Object>> rawData = new ArrayList<List<Object>>();
		int i = 0;
		for(List<String> row : data) {
			rawData.add(new ArrayList<Object>());
			for(String value : row) {
				rawData.get(i).add(value);
			}
			i++;
		}
		return rawData;
 	}
	
	public static void main(String[] args) {
		UserSheet sheetData = new UserSheet();
        sheetData.print();
        sheetData.setValue("Username", "jmharter", "Password", "secretPassword");
        sheetData.writeReadPrint();
        sheetData.setValue("Username", "jmharter", "Password", "changeme");
        sheetData.insertRow(2, "bsliz", "changeme", "Brad", "Sliz", "bsliz@gmail.com");
        
        DriveUtil.createTab(DriveUtil.SHEET_ID_USERS, "my tab");
        sheetData.setTabName("my tab");
        sheetData.write();
        DriveUtil.deleteTab(DriveUtil.SHEET_ID_USERS, "my tab");
	}

}
