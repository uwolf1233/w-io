package com.wolf.outin.service;

public class ExcelReaderUtil {

	// excel2003扩展名
    public static final String EXCEL03_EXTENSION = ".xls";
    // excel2007扩展名
    public static final String EXCEL07_EXTENSION = ".xlsx";

    /**
     * 读取Excel文件，可能是03也可能是07版本
     * 
     * @param excel03
     * @param excel07
     * @param fileName
     * @throws Exception
     */
    public static void readExcel(IExcelRowReader reader, String fileName) throws Exception {
        // 处理excel2003文件
        if (fileName.endsWith(EXCEL03_EXTENSION)) {
        	InDataService exceXls = new InDataService();
            exceXls.setRowReader(reader); 
            exceXls.process(fileName);
            // 处理excel2007文件
        } else if (fileName.endsWith(EXCEL07_EXTENSION)) {
        	InDataService exceXlsx = new InDataService();
            exceXlsx.setRowReader(reader); 
            exceXlsx.process(fileName);
        } else {
            throw new Exception("文件格式错误，fileName的扩展名只能是xls或xlsx。");
        }
    }
	
}
