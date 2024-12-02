package DingDingExercise.TableAPI

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Expressions.$
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

object exercise4 {
    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val datas = env.readTextFile("datas/StuList.txt")
        val stream = datas.map(lines => {
            val array: Array[String] = lines.split(",")
            Stu(array(0), array(1), array(2).toDouble, array(3).toLong)
        })
        val tableEnv = StreamTableEnvironment.create(env)
        val myTable: Table = tableEnv.fromDataStream(stream,$("id"),$("name"),$("score"),
        $("timestamp"),$("prot").proctime())
        myTable.printSchema()
        tableEnv.toAppendStream[Row](myTable).print()
        env.execute()
    }
}
