package Table_API

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Expressions.$
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import pojo.Event

import java.time.Duration

object TimeAndWindowTest {
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


        //2. 在将数据流转化为表的时候设置时间属性字段
        val eventStream = env
            .fromElements(
                Event("Alice", "./home", 1000L),
                Event("Bob", "./cart", 1000L),
                Event("Alice", "./prod?id=1", 5 * 1000L),
                Event("Cary", "./home", 60 * 1000L),
                Event("Bob", "./prod?id=3", 90 * 1000L),
                Event("Alice", "./prod?id=7", 105 * 1000L)
            )//在这里设置水位线
            .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[Event](Duration.ofSeconds(2))
            .withTimestampAssigner(new SerializableTimestampAssigner[Event] {
                override def extractTimestamp(element: Event, recordTimestamp: Long): Long = {
                    element.time
                }
            }))
        val eventTable: Table = tableEnv.fromDataStream(eventStream,$("url"),$("name").as("user"),
            $("time").rowtime().as("ts"))

        tableEnv.from("eventTable").printSchema()
        eventTable.printSchema();
    }
}
