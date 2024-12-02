package Table_API

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Expressions.$
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import pojo.Event

object TableExample {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val eventStream = env
          .fromElements(
              Event("Alice", "./home", 1000L),
              Event("Bob", "./cart", 1000L),
              Event("Alice", "./prod?id=1", 5 * 1000L),
              Event("Cary", "./home", 60 * 1000L),
              Event("Bob", "./prod?id=3", 90 * 1000L),
              Event("Alice", "./prod?id=7", 105 * 1000L)
          )
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        val eventTable: Table = tableEnv.fromDataStream(eventStream)
        //用执行 SQL 的方式提取数据
        val table: Table = tableEnv.sqlQuery(""" select url , name from """ + eventTable + """ where name='Alice' """)
        // 调用Table API进行转换计算
//        val resultTable = eventTable.select($("url"), $("user"))
//          .where($("user").isEqual("Alice"))
        tableEnv.toDataStream(table).print()
        env.execute()
    }
}
