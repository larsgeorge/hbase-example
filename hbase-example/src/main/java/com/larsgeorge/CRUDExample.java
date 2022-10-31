package com.larsgeorge;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class CRUDExample {
  private static final TableName TABLE_NAME = TableName.valueOf("testtable");
  private static final byte[] CF1_NAME = Bytes.toBytes("colfam1");
  private static final byte[] CF2_NAME = Bytes.toBytes("colfam2");
  private static final byte[] QUAL1_NAME = Bytes.toBytes("qual1");
  private static final byte[] QUAL2_NAME = Bytes.toBytes("qual2");
  private static final byte[] ROW_ID = Bytes.toBytes("row1");

/**
 * Simple CRUD example for HBase.
 *
 * @param args
 * @throws IOException
 */
public static void main(String[] args) throws IOException {
    Configuration conf = HBaseConfiguration.create();
    try (
      Connection connection = ConnectionFactory.createConnection(conf);
      Admin admin = connection.getAdmin();
    ) {
      if (!admin.tableExists(TABLE_NAME)) {
        TableDescriptor desc = TableDescriptorBuilder.newBuilder(TABLE_NAME)
        .setColumnFamily(ColumnFamilyDescriptorBuilder.of(CF1_NAME))
        .setColumnFamily(ColumnFamilyDescriptorBuilder.of(CF2_NAME))
        .build();
        admin.createTable(desc);
      }
    }

    try (
      Connection connection = ConnectionFactory.createConnection(conf);
      Table table = connection.getTable(TABLE_NAME);
    ) {
      Put put = new Put(ROW_ID);
      put.addColumn(CF1_NAME, QUAL1_NAME, Bytes.toBytes("val1"));
      put.addColumn(CF2_NAME, QUAL2_NAME, Bytes.toBytes("val2"));
      table.put(put);

      Scan scan = new Scan();
      ResultScanner scanner = table.getScanner(scan);
      for (Result result2 : scanner) {
        while (result2.advance())
          System.out.println("Cell: " + result2.current());
      }

      Get get = new Get(ROW_ID);
      get.addColumn(CF1_NAME, QUAL1_NAME);
      Result result = table.get(get);
      System.out.println("Get result: " + result);
      byte[] val = result.getValue(CF1_NAME, QUAL1_NAME);
      System.out.println("Value only: " + Bytes.toString(val));

      Delete delete = new Delete(ROW_ID);
      delete.addColumn(CF1_NAME, QUAL1_NAME);
      table.delete(delete);

      Scan scan2 = new Scan();
      ResultScanner scanner2 = table.getScanner(scan2);
      for (Result result2 : scanner2) {
        System.out.println("Scan: " + result2);
      }
    }
  }
}
