package Table_API

import Table_API.UDF_AggregateFunction.WeightedAvg
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Expressions.{$, call}
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.TableAggregateFunction
import org.apache.flink.types.Row
import org.apache.flink.util.Collector

import java.sql.Timestamp

object UDF_TableAggregateFunction {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        //1. 在创建表DDL的语句中设置时间属性字段
        tableEnv.executeSql("CREATE TABLE eventTable (" +
            "uid STRING," +
            "url STRING," +
            "ts BIGINT," +
            "et AS TO_TIMESTAMP( FROM_UNIXTIME( ts / 1000 ))," +
            "WATERMARK FOR et AS et - INTERVAL '2' SECOND" +
            ") WITH (" +
            " 'connector' = 'filesystem'," +
            " 'path' = 'datas/clicks.txt'," +
            " 'format' = 'csv' " +
            ")")

        //注册表聚合函数
        tableEnv.createTemporarySystemFunction("Top2", classOf[Top2])

        //使用SQL调用函数查询转换
        // 首先进行窗口聚合得到cnt值
        val urlCountWindowTable = tableEnv.sqlQuery(
            """
              |SELECT uid, COUNT(url) AS cnt, window_start as wstart, window_end as wend
              |FROM TABLE (
              |  TUMBLE(TABLE eventTable, DESCRIPTOR(et), INTERVAL '1' HOUR)
              |)
              |GROUP BY uid, window_start, window_end
              |""".stripMargin)

        val resultTable = urlCountWindowTable.groupBy($("wend"))
            .flatAggregate(call("Top2", $("uid"), $("cnt"),
                $("wstart"), $("wend")))
            .select($("uid"), $("rank"), $("cnt"), $("wend"))

        tableEnv.toRetractStream[Row](resultTable).print()
        env.execute()
    }

    //定义输出结果和中间累加器的样例类
    case class Top2Result(uid: String, window_start: Timestamp, window_end: Timestamp, cnt: Long, rank: Int)

    case class Top2Acc(var maxCount: Long, var SecondMaxCount: Long, var uid1: String, var uid2: String,
                       var window_start: Timestamp, var window_end: Timestamp)

    class Top2 extends TableAggregateFunction[Top2Result, Top2Acc] {
        override def createAccumulator(): Top2Acc = Top2Acc(Long.MinValue, Long.MinValue, null, null,
            null, null)

        //每来一行数据，需要使用accumulate进行聚合统计
        def accumulate(acc: Top2Acc, uid: String, cnt: Long, window_start: Timestamp, window_end: Timestamp): Unit = {
            acc.window_start = window_start
            acc.window_end = window_end
            // 判断当前count值是否排名前两位
            if (cnt > acc.maxCount) {
                // 名次向后顺延
                acc.SecondMaxCount = acc.maxCount
                acc.uid2 = acc.uid1
                acc.maxCount = cnt
                acc.uid1 = uid
            } else if (cnt > acc.SecondMaxCount) {
                acc.SecondMaxCount = cnt
                acc.uid2 = uid
            }
        }

        def emitValue(acc: Top2Acc, out: Collector[Top2Result]): Unit = {
            // 判断cnt值是否为初始值，如果没有更新过直接跳过不输出
            if (acc.maxCount != Long.MinValue) {
                out.collect(Top2Result(acc.uid1, acc.window_start, acc.window_end, acc.maxCount, 1))
            }
            if (acc.SecondMaxCount != Long.MinValue) {
                out.collect(Top2Result(acc.uid2, acc.window_start, acc.window_end, acc.SecondMaxCount, 2))
            }
        }
    }
}
