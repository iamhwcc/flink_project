package Table_API

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.{EnvironmentSettings, Table, TableEnvironment}
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}

object CommonAPI {
    def main(args: Array[String]): Unit = {
        //流处理Table环境
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        //批处理Table环境
//        val batchSetting = EnvironmentSettings.newInstance().inBatchMode().useBlinkPlanner().build()
//        val tableEnvironment: TableEnvironment = TableEnvironment.create(batchSetting)
        tableEnv.executeSql("CREATE TABLE eventTable (" +
          "uid STRING," +
          "url STRING," +
          "ts BIGINT" +
          ") WITH (" +
          " 'connector' = 'filesystem'," +
          " 'path' = 'datas/clicks.txt'," +
          " 'format' = 'csv' " +
          ")")
        val resultTable: Table = tableEnv.sqlQuery(" select uid, url, ts from eventTable where uid='Mary' ")
        val uidCount: Table = tableEnv.sqlQuery("select uid, count(url) from eventTable group by uid")

        //将resultTable这个Table对象转为Table环境里的一张表（虚拟表）
        tableEnv.createTemporaryView("tempTable",resultTable)

        tableEnv.executeSql("CREATE TABLE outputTable (" +
          "username STRING," +
          "url STRING," +
          "`timestamp` BIGINT" +
          ") WITH (" +
          " 'connector' = 'filesystem'," +
          " 'path' = 'outPut'," +
          " 'format' = 'csv' " +
          ")")

//        resultTable.executeInsert("outputTable")

        tableEnv.toDataStream(resultTable).print()

        env.execute()
    }
}
