package bulidSQL;

/**
 * 类名:		SQLBean
 * 描述:		数据表的相关属性
 * @author 	ZHANGHUANG
 */
public class SQLBean {
  private String tableName;    //表名

  private String columeName;   //列名

  private String columeCNName; //列中文名

  private String dataType;     //数据类型

  private String dataLength;   //数据长度

  private String dataSca;      //小数位数

  private String nullAble;     //可为空

  private String isPK;        //是否主键

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName.toUpperCase();
  }

  public String getColumeName() {
    return columeName;
  }

  public void setColumeName(String columeName) {
    this.columeName = columeName.toUpperCase();
  }

  public String getColumeCNName() {
    return columeCNName;
  }

  public void setColumeCNName(String columeCNName) {
    this.columeCNName = columeCNName.toUpperCase();
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType.toUpperCase();
  }

  public String getDataLength() {
    return dataLength;
  }

  public void setDataLength(String dataLength) {
    this.dataLength = dataLength;
  }

  public String getDataSca() {
    return dataSca;
  }

  public void setDataSca(String dataSca) {
    this.dataSca = dataSca;
  }

  public String getNullAble() {
    return nullAble;
  }

  public void setNullAble(String nullAble) {
    this.nullAble = nullAble.toUpperCase();
  }

  public String getIsPK() {
    return isPK;
  }

  public void setIsPK(String isPK) {
    this.isPK = isPK.toUpperCase();
  }
}
