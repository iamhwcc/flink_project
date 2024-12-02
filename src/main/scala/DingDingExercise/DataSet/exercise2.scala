package DingDingExercise.DataSet

import org.apache.flink.api.java.aggregation.Aggregations
import org.apache.flink.api.scala._

//数据集的用户数量（一个用户多次记录时，用户数为1）
object exercise2 {
    def main(args: Array[String]): Unit = {
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val datas: DataSet[behavior] = env.readCsvFile("datas/UserBehavior.csv")
        print(datas.map(lines => {
            lines.user_id
        }).distinct().count())
        //        }).distinct().map(ids =>{
        //            (ids,1)
        //        }).aggregate(Aggregations.SUM,1).map(tuple=>tuple._2).print()
    }
}
