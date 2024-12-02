package Table_API

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.AggregateFunction
import org.apache.flink.types.Row

import java.lang

object UDF_AggregateFunction {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        //1. 在创建表DDL的语句中设置时间属性字段
        tableEnv.executeSql("CREATE TABLE eventTable (" +
            "uid STRING," +
            "url STRING," +
            "ts BIGINT," +
            "eventTime AS TO_TIMESTAMP( FROM_UNIXTIME( ts / 1000 ))," +
            "WATERMARK FOR eventTime AS eventTime - INTERVAL '5' SECOND" +
            ") WITH (" +
            " 'connector' = 'filesystem'," +
            " 'path' = 'datas/clicks.txt'," +
            " 'format' = 'csv' " +
            ")")

        //注册聚合函数
        tableEnv.createTemporarySystemFunction("WeightedAvg",classOf[WeightedAvg])

        //使用SQL调用函数查询转换
        val resultTable: Table = tableEnv.sqlQuery("SELECT uid, WeightedAvg(ts,1) as avg_ts from eventTable " +
            "GROUP BY uid")

        tableEnv.toRetractStream[Row](resultTable).print()
        env.execute()
    }

    //定义一个样例类用于累加器
    case class WeightedAvgAcc(var sum: Long = 0, var count: Long = 0)

    //统计每个用户时间戳的加权平均值
    // 加权平均值 = ∑(时间戳 * 权值) / ∑(权值)
    class WeightedAvg extends AggregateFunction[java.lang.Long, WeightedAvgAcc] {
        override def getValue(acc: WeightedAvgAcc): lang.Long = {
            if (acc.count == 0) {
                null
            } else {
                acc.sum / acc.count
            }
        }

        override def createAccumulator(): WeightedAvgAcc = WeightedAvgAcc()

        def accumulate(acc: WeightedAvgAcc, inValue: java.lang.Long, inWeight: Int): Unit = {
            acc.sum += inWeight * inValue
            acc.count += inWeight
        }
    }
}
