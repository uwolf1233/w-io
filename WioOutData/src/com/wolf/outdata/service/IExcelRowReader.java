package com.wolf.outdata.service;

import java.util.List;

public interface IExcelRowReader {

	public void getRows(int sheetIndex,int curRow,List<String> rowlist);
	
}
