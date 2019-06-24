package bulidSQL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.PropertiesHepler;

/**
 * 类名:		BuildSQL
 * 描述:		构建SQL脚本
 * @author 	ZHANGHUANG
 * @ 注意文档中下传文件的编码
 *
 */
public class BuildSQL {
  private static Logger logger = LoggerFactory.getLogger(BuildSQL.class);

  public static void main(String[] args) {
    String dbName = PropertiesHepler.getValue("dbName");

    String path = PropertiesHepler.getValue("filePath");
    if (!path.endsWith("xls") && !path.endsWith("xlsx") && !path.endsWith("XLS") && !path.endsWith("XLSX")) {
      logger.info("不是Excel文件！不支持非Excel 文件解析！");
      return;
    }
    int readSheetIndex = Integer.parseInt(PropertiesHepler.getValue("readSheetIndex").trim()); //读取哪个sheet， 起始下标0
    int startLine = Integer.parseInt(PropertiesHepler.getValue("startLine").trim()); //从第几行开始读， 起始下标0
    int endLine = Integer.parseInt(PropertiesHepler.getValue("endLine").trim()); //从第几行开始读， 起始下标0
    

    int tableNameIndex = Integer.parseInt(PropertiesHepler.getValue("tableNameIndex").trim());//表名所在列下标
    int columeNameIndex = Integer.parseInt(PropertiesHepler.getValue("columeNameIndex").trim());//列名所在列下标
    int columeCNNameIndex = Integer.parseInt(PropertiesHepler.getValue("columeCNNameIndex").trim());//列中文名所在列下标
    int dataTypeIndex = Integer.parseInt(PropertiesHepler.getValue("dataTypeIndex").trim());//数据类型所在列下标
    int dataLengthIndex = Integer.parseInt(PropertiesHepler.getValue("dataLengthIndex").trim());//数据长度所在列下标
    int dataScaIndex = Integer.parseInt(PropertiesHepler.getValue("dataScaIndex").trim());//小数位数所在列下标
    int nullAbleIndex = Integer.parseInt(PropertiesHepler.getValue("nullAbleIndex").trim());//可为空所在列下标
    int isPKIndex = Integer.parseInt(PropertiesHepler.getValue("isPKIndex").trim()); //是否主键所在列下标

    List<SQLBean> bulidBeanList = bulidBeanList(path, readSheetIndex, startLine, endLine,tableNameIndex, columeNameIndex, columeCNNameIndex, dataTypeIndex,
        dataLengthIndex, dataScaIndex, nullAbleIndex, isPKIndex);
    if (dbName.equals("oracle")) {
      mkSQLScript(bulidBeanList);
    }
    if (dbName.equals("postgreSQL")) {
      //mkplpgSQLScript(bulidBeanList);  //TODO 
    }
  }

  /**
   * 描述:	 组装SQL脚本
   * @param 表名    列名    列中文名    字段类型    字段长度    小数位长度(无可不填)   是否可为空   是否主键
   */
  public static void mkSQLScript(List<SQLBean> list) {
    if (0 == list.size() || null == list) {
      logger.info("无数据......");
      return;
    }

    String tableName = list.get(0).getTableName();
    StringBuilder creTableSQL = new StringBuilder();
    StringBuilder comClumSQL = new StringBuilder();
    StringBuilder alterPKSQL = new StringBuilder();
    alterPKSQL.append("ALTER TABLE ").append(tableName).append(" ADD PRIMARY KEY(");

    creTableSQL.append("CREATE TABLE ");

    creTableSQL.append(tableName).append(" (\r\n");
    for (int i = 0; i < list.size(); i++) {
      creTableSQL.append("    ").append(list.get(i).getColumeName()).append(" ").append(list.get(i).getDataType());
      String clumLen = "";
      if (list.get(i).getDataType().equalsIgnoreCase("NUMBER") && !list.get(i).getDataSca().equals("0")) {
        clumLen = "(" + list.get(i).getDataLength() + "," + list.get(i).getDataSca() + ")";
      } else {
        if (!list.get(i).getDataType().equalsIgnoreCase("DATE")) {
          clumLen = "(" + list.get(i).getDataLength() + ")";
        }
      }
      creTableSQL.append(clumLen);
      if (list.get(i).getNullAble().equalsIgnoreCase("N") && list.get(i).getIsPK().equalsIgnoreCase("Y")) {
        creTableSQL.append(" NOT NULL,").append("\r\n");
      } else {
        creTableSQL.append(",\r\n");
      }

      if (list.get(i).getIsPK().equalsIgnoreCase("Y")) {
        alterPKSQL.append(list.get(i).getColumeName()).append(",");
      }

      if (!list.get(i).getColumeCNName().equals("")) {
        comClumSQL.append("COMMENT ON COLUMN ").append(tableName).append('.').append(list.get(i).getColumeName()).append(" IS ").append("'")
            .append(list.get(i).getColumeCNName()).append("';\r\n");
      }
    }
    creTableSQL.deleteCharAt(creTableSQL.lastIndexOf(","));
    alterPKSQL.deleteCharAt(alterPKSQL.lastIndexOf(","));
    alterPKSQL.append(");\r\n");

    creTableSQL.append(");\r\n");
    creTableSQL.append(comClumSQL);
    creTableSQL.append(alterPKSQL);

    logger.info("\r\n" + creTableSQL.toString());
    // return strBox.toString();
  }

  /**
   * 
   * 描述:		解析文件
   * @param path  excel文件路径
   * @param readSheetIndex  sheet页下标
   * @param startLine 从哪一行开始读
   * @param tableNameIndex 表名字所在的行下标
   * @param columeNameIndex 列名所在的行下标
   * @param columeCNNameIndex 列中文名值所在的行下标
   * @param dataTypeIndex 数据类型值下标
   * @param dataLengthIndex 数据长度值所在下标
   * @param dataScaIndex 小数长度值所在下标
   * @param nullAbleIndex 是否为空值所在下标
   * @param isPKIndex 是否主键值所在下标
   */
  public static List<SQLBean> bulidBeanList(String path, int readSheetIndex, int startLine,int endLine, int tableNameIndex, int columeNameIndex,
      int columeCNNameIndex, int dataTypeIndex, int dataLengthIndex, int dataScaIndex, int nullAbleIndex, int isPKIndex) {
    try {
      List<SQLBean> list = new ArrayList<SQLBean>();
      File f = new File(path); // 声明File对象f
      //logger.info("准备解析文件"+ f.getName());
      InputStream input = null; // 准备好一个输入的对象
      input = new FileInputStream(f);
      Workbook workbook = getWorkbookByStream(input);
      //读取sheet
      Sheet sheet = workbook.getSheetAt(readSheetIndex);
      if (sheet != null) {//工作表存在
        //Row headRow = sheet.getRow(0);
        //第一行为表头,从表头判断这个文件有多少列
        //int cellNum = headRow.getPhysicalNumberOfCells();
        //logger.info("文件行数:" + RowNums + ",表头列数" + cellNum);
       /* if (cellNum != 8) {
          logger.info("没按约定的格式。。。。。");
          logger.info("表名    列名    列中文名    字段类型    字段长度    小数位长度(无可不填)   可为空   是否主键");
          return null;
        }*/

        for (int i = startLine; i <= endLine; i++) {
          try {
            SQLBean bean = new SQLBean();
            Row row = sheet.getRow(i);
            //模板中第0列的值约定为填写：类型
            //按照文档规定顺序获取 
            String tableName = getCellVal(row.getCell(tableNameIndex)).toString();
            String columeName = getCellVal(row.getCell(columeNameIndex)).toString();
            String columeCNName = getCellVal(row.getCell(columeCNNameIndex)).toString();
            String dataType = getCellVal(row.getCell(dataTypeIndex)).toString();
            String dataLength = getCellVal(row.getCell(dataLengthIndex)).toString();
            String dataSca = getCellVal(row.getCell(dataScaIndex)).toString();
            if ("".equals(dataSca)) {
              dataSca = "0";
            }

            String nullAble = getCellVal(row.getCell(nullAbleIndex)).toString();
            String isPK = getCellVal(row.getCell(isPKIndex)).toString();

            bean.setTableName(tableName);
            bean.setColumeName(columeName);
            bean.setColumeCNName(columeCNName);
            bean.setDataType(dataType);
            bean.setDataLength(dataLength);
            bean.setDataSca(dataSca);
            bean.setNullAble(nullAble);
            bean.setIsPK(isPK);

            list.add(bean);
          } catch (Exception e) {
            logger.info("读取出现异常。。");
            e.printStackTrace();
            return null;
          }
        }
      }
      return list;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @desc HSSF是POI工程对Excel 97(-2007)文件操作<br/>
   *  XSSF是POI工程对Excel 2007 OOXML (.xlsx)文件
   * @throws Exception
   * @author ZhangHuang
   * @throws Exception 
   */
  public static Workbook getWorkbookByStream(InputStream InputStream) {
    Workbook workbook = null;
    try {
      workbook = new XSSFWorkbook(InputStream);
    } catch (Exception e) {
      try {
        workbook = new HSSFWorkbook(InputStream);
      } catch (Exception e1) {
        logger.info("不支持解析该文件！请将文件另存为高版本的Excel文件！");
      }
    }
    return workbook;
  }

  public static String getCellVal(Cell cell) throws Exception {
    if (cell != null) {
      int cellType = cell.getCellType();
      if (Cell.CELL_TYPE_NUMERIC == cellType) {
        double doublevalue = cell.getNumericCellValue();
        DecimalFormat df = new DecimalFormat("##########.##########");
        String value = df.format(doublevalue);
        return value;
      } else if (Cell.CELL_TYPE_STRING == cellType) {
        String value = cell.getStringCellValue();
        return value;
      }/*else if(Cell.CELL_TYPE_BLANK==cellType){
        return "";
       }*/
    }
    return "";
  }
}
