package DingDingExercise.DataSet

import org.apache.flink.api.scala._
import org.apache.flink.api.common.operators.Order

//按购买和加入购物车次数之和统计前10名顾客，按次数倒排

object exercise5 {
    def main(args: Array[String]): Unit = {
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val datas: DataSet[behavior] = env.readCsvFile("datas/UserBehavior.csv")
        val buysAndCarts: DataSet[behavior] = datas.filter(lines => {
            lines.behavior_type.equals("buy") || lines.behavior_type.equals("cart")
        })
        val group: GroupedDataSet[(Int, String, Int)] = buysAndCarts.map(lines => {
            (lines.user_id, lines.behavior_type, 1)
        }).groupBy(0)
        group.sum(2).sortPartition(2, Order.DESCENDING).map(tuple => {
                s"用户: ${tuple._1} 的购买和加入购物车次数之和为 ${tuple._3}"
            }).first(10)
            .print()
    }
}
