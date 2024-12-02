package DingDingExercise.TableAPI

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Expressions.$
import org.apache.flink.table.api.{EnvironmentSettings, Table}
import org.apache.flink.table.api.bridge.scala._

case class Stu(id: String, name: String, score: Double, timestamp: Long)

object exercise1 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val students: DataStream[String] = env.readTextFile("datas/StuList.txt")
        val data: DataStream[Stu] = students.map(lines => {
            val array: Array[String] = lines.split(",")
            Stu(array(0).trim, array(1).trim, array(2).toDouble, array(3).toLong)
        })
        val settings: EnvironmentSettings = EnvironmentSettings.newInstance().inStreamingMode().useBlinkPlanner().build()
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env, settings)
        // Table API的方式查询 myid 为 Stu_1 的学生的所有数据
        val StuTable: Table = tableEnv.fromDataStream(data, $("name"), $("id").as("myid"), $("score"))
        tableEnv.createTemporaryView("stu", StuTable)
        val resultTable1: Table = StuTable.select($("name"), $("myid"), $("score"))
            .where($("myid").isEqual("Stu_1"))

        // Flink SQL的方式查询 myid 为 Stu_2的学生的所有数据

        val resultTable2: Table = tableEnv.sqlQuery(" select name,myid,score from stu where myid = 'Stu_2' ")
        tableEnv.toAppendStream[(String, String, Double)](resultTable1).print("Stu_1")
        tableEnv.toAppendStream[(String, String, Double)](resultTable2).print("Stu_2")
        env.execute()
    }
}
