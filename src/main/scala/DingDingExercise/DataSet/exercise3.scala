package DingDingExercise.DataSet

import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.scala._

//数据集各种行为类型的统计，按统计数量倒序
object exercise3 {
    def main(args: Array[String]): Unit = {
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val datas: DataSet[behavior] = env.readCsvFile("datas/UserBehavior.csv")
        val tuple: DataSet[(String, Int)] = datas.map(lines => {
            (lines.behavior_type, 1)
        })
        val group: GroupedDataSet[(String, Int)] = tuple.groupBy(0)
        val result: AggregateDataSet[(String, Int)] = group.sum(1)
        result.sortPartition(1,Order.DESCENDING).print()
    }
}
