package DingDingExercise.DataSet

import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.java.aggregation.Aggregations
import org.apache.flink.api.scala._

case class AllBehavior(UserId: Int, pvSum: Long, buySum: Long, cartSum: Long, favSum: Long)

//计算每个用户的行为总数，表示为：（UserId,pvSum,buySum,cartSum,favSum）

object exercise6 {
    def main(args: Array[String]): Unit = {
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val datas: DataSet[behavior] = env.readCsvFile("datas/UserBehavior.csv")
        val group: GroupedDataSet[AllBehavior] = datas.map(lines => {
            lines.behavior_type match {
                case "pv" => AllBehavior(lines.user_id, 1, 0, 0, 0)
                case "buy" => AllBehavior(lines.user_id, 0, 1, 0, 0)
                case "cart" => AllBehavior(lines.user_id, 0, 0, 1, 0)
                case "fav" => AllBehavior(lines.user_id, 0, 0, 0, 1)
                case _ => AllBehavior(lines.user_id, 0, 0, 0, 0)
            }
        }).groupBy(_.UserId)
        group.reduce((t1, t2) => {
            AllBehavior(t1.UserId, t1.pvSum + t2.pvSum, t1.buySum + t2.buySum, t1.cartSum + t2.cartSum, t1.favSum + t2.favSum)
        }).print()
    }
}
