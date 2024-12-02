package DingDingExercise.DataSet

import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.scala._

//按购买行为计算10大热销商品，按统计数量倒排
object exercise4 {
    def main(args: Array[String]): Unit = {
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val datas: DataSet[behavior] = env.readCsvFile("datas/UserBehavior.csv")
        val cleanedDatas: DataSet[behavior] = datas.filter(lines => lines.toString.contains("buy"))
        val tuple: DataSet[(Int, Int)] = cleanedDatas.map(lines => {
            (lines.item_id, 1)
        })
        val itemGroup: GroupedDataSet[(Int, Int)] = tuple.groupBy(0)
        val sums: AggregateDataSet[(Int, Int)] = itemGroup.sum(1)
        sums.sortPartition(1,Order.DESCENDING).first(10).print()

    }
}
