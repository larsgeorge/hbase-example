package com.larsgeorge;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
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

public class ScanExample {
  private static final TableName TABLE_NAME = TableName.valueOf("hbase:meta");

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
      Table table = connection.getTable(TABLE_NAME);
    ) {
      System.out.println("inside");
      Scan scan = new Scan();
      ResultScanner scanner = table.getScanner(scan);
      for (Result result2 : scanner) {
        while (result2.advance())
          System.out.println("Cell: " + result2.current());
      }
    }
  }
}
