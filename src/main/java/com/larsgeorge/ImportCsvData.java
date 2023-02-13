package com.larsgeorge;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;

import com.opencsv.CSVReader;

public class ImportCsvData {

  class Column {
    public byte[] colFam;
    public byte[] qualifier;
    public byte[] value;
  }

  Configuration conf;
  Connection connection;
  Admin admin;
  String colFam;
  int batchSize = 1000;

  public ImportCsvData() {
    try {
      conf = HBaseConfiguration.create();
      connection = ConnectionFactory.createConnection(conf);
      admin = connection.getAdmin();
      colFam = "cf1";
    } catch (IOException e) {
      throw new RuntimeException("An error occurred.", e);
    }
  }

  void createTable(String name, String[] colFams) throws IOException {
    TableName tblName = TableName.valueOf(name);
    createTable(tblName, colFams);
  }

  void createTable(TableName name, String[] colFams) throws IOException {
    if (!admin.tableExists(name)) {
      TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(name);
      for (String colFam: colFams) {
        builder.setColumnFamily(ColumnFamilyDescriptorBuilder.of(colFam));
      }
      TableDescriptor desc = builder.build();
      admin.createTable(desc);
    }
  }

  void writeRecord(String name, byte[] rowKey, Column[] cols) throws IOException {
    TableName tblName = TableName.valueOf(name);
    Table table = connection.getTable(tblName);
    Put put = new Put(rowKey);
    for (Column col: cols) {
      put.addColumn(col.colFam, col.qualifier, col.value);
    }
    table.put(put);
  }

  private Put convertToPut(String[] record, byte[] colFam) {
    Put put = new Put(Bytes.toBytes(record[0]));
    int colCount = 0;
    for (String col: Arrays.copyOfRange(record, 1, record.length - 1)) {
      put.addColumn(colFam, Bytes.toBytes("col-" + colCount), Bytes.toBytes(col));
      colCount += 1;
    }
    return put;
  }

  private void flushPuts(Table table, List<Put> batch) throws IOException, InterruptedException {
    if (batch.size() > 0) {
      Object[] results = new Object[batch.size()];
      table.batch(batch, results);
      // iterate over results to find exception (that is, non-Result objects, including nulls)
      List<Object> failures = new ArrayList<>();
      for (Object result : results) {
        if (!(result instanceof Result)) {
          failures.add(result);
        }
      }
      if (!failures.isEmpty()) {
        throw new IOException("An error occurred writing data: " + failures);
      }    
    }
    batch.clear();
  }

  void parseFile(String filename) {
    String name = FilenameUtils.getBaseName(filename);
    TableName tblName = TableName.valueOf(name);
    List<Put> batch = new ArrayList<>();
    byte[] colFamBytes = Bytes.toBytes(colFam);
    try {
      FileReader filereader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(filereader);
      String[] nextRecord;
  
      createTable(tblName, new String[] { colFam.toString() });
      Table table = connection.getTable(tblName);
      int rowCount = 0;
      System.out.println("START - Importing into table: " + name);
      while ((nextRecord = csvReader.readNext()) != null) {
        Put put = convertToPut(nextRecord, colFamBytes);
        batch.add(put);
        if (batch.size() > batchSize) {
          rowCount += batch.size();
          flushPuts(table, batch);
          System.out.print(".");
        }
      }
      if (batch.size() > 0) {
        rowCount += batch.size();
        flushPuts(table, batch);
      }
      if (rowCount > batchSize) System.out.println();
      System.out.println("DONE - Rows written: " + rowCount);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    ImportCsvData icd = new ImportCsvData();
    icd.parseFile("projects-head.csv");     
  }
   
}
