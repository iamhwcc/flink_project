package DingDingExercise.TableAPI

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Expressions.$
import org.apache.flink.table.api.{Table, Tumble}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

import java.time.Duration

object exercise4_2 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val datas: DataStream[String] = env.readTextFile("datas/StuList.txt")
        val stream: DataStream[Stu] = datas.map(lines => {
            val array: Array[String] = lines.split(",")
            Stu(array(0), array(1), array(2).toDouble, array(3).toLong)
        }).assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[Stu](Duration.ofSeconds(3))
        .withTimestampAssigner(new SerializableTimestampAssigner[Stu] {
            override def extractTimestamp(element: Stu, recordTimestamp: Long): Long = element.timestamp*1000L;
        }))
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        val myTable: Table = tableEnv.fromDataStream(stream,$("id"),$("name"),
        $("score"),$("timestamp").rowtime().as("ts"))
        val resultTable1: Table = myTable.window(Tumble.over("10.seconds").on($("ts")).as("window"))
            .groupBy($("id"), $("window")).select($("id"), $("id").count(), $("score").avg()
                , $("window").end())
        tableEnv.toRetractStream[Row](resultTable1).print("T_window")
        tableEnv.createTemporaryView("student",myTable)
        val resultTable2: Table = tableEnv.sqlQuery(
            """SELECT
              |    id,
              |    ts,
              |    COUNT(id) OVER (PARTITION BY id ORDER BY ts ROWS BETWEEN 2 PRECEDING AND CURRENT ROW),
              |    AVG(score) OVER (PARTITION BY id ORDER BY ts ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)
              |FROM student
              |""".stripMargin)
        tableEnv.toRetractStream[Row](resultTable2).print("SQL_Over")
        env.execute()
    }
}
