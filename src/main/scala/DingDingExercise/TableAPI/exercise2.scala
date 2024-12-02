package DingDingExercise.TableAPI

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Expressions.$
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.descriptors.{Csv, FileSystem}
import org.apache.flink.types.Row

object exercise2 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val settings: EnvironmentSettings = EnvironmentSettings.newInstance().inStreamingMode().useBlinkPlanner().build()
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env, settings)
        tableEnv.executeSql("CREATE TABLE myStuTable (" +
          "`id` STRING," +
          "`name` STRING," +
          "score DOUBLE," +
          "`timestamp` BIGINT" +
          ") WITH (" +
          " 'connector' = 'filesystem', " +
          " 'path' = 'datas/StuList.txt', " +
          " 'format' = 'csv' " +
          ")")
        val myTable: Table = tableEnv.from("myStuTable")
        val groupTable: GroupedTable = myTable.groupBy($("id"))
        val resultTable1: Table = groupTable.select($("id"), $("id").count())//.count()返回BIGINT(Long)类型
        tableEnv.toRetractStream[Row](resultTable1).print("第(2)步")

        tableEnv.executeSql("CREATE TABLE outPutTable (" +
          "`id` STRING," +
          "score DOUBLE" +
          ") WITH (" +
          " 'connector' = 'filesystem', " +
          " 'path' = 'outPut/output', " +
          " 'format' = 'csv' " +
          ")")

        val resultTable2: Table = tableEnv.sqlQuery("select id, score from myStuTable")
        resultTable2.executeInsert("outPutTable")
        env.execute()
    }
}
