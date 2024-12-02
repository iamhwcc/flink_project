package DingDingExercise.DataSet

import org.apache.flink.api.java.aggregation.Aggregations
import org.apache.flink.api.scala._


case class behavior(user_id:Int,item_id:Int,category_id:Int,behavior_type:String,time:Long)

//数据集总记录数
object exercise1 {
    def main(args: Array[String]): Unit = {
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        val datas: DataSet[behavior] = env.readCsvFile("datas/UserBehavior.csv")
//        datas.map(lines =>{
//            (lines.user_id,1)
//        }).aggregate(Aggregations.SUM,1).map(tuple=>tuple._2).print()
        println(datas.count())
    }
}
