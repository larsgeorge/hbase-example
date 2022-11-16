package com.larsgeorge;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.RetriesExhaustedException;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.zookeeper.ReadOnlyZKClient;

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
    conf.set(HConstants.ZOOKEEPER_QUORUM, "107-21-193-76.compute-1.amazonaws.com");
    conf.setInt(ReadOnlyZKClient.RECOVERY_RETRY, 2);
    try (
      Connection connection = ConnectionFactory.createConnection(conf);
      Table table = connection.getTable(TABLE_NAME);
    ) {
      if (connection.getClusterId() == null) {
        System.out.println("ERROR: Could not connect to HBase!");
        System.exit(-1);
      }
      Scan scan = new Scan();
      ResultScanner scanner = table.getScanner(scan);
      for (Result result2 : scanner) {
        while (result2.advance())
          System.out.println("Cell: " + result2.current());
      }
    }
  }
}
