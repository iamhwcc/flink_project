import org.apache.flink.api.java.aggregation.Aggregations
import org.apache.flink.api.scala._

case class t(jidian: Int, xuefen: Int)

//数据集的用户数量（一个用户多次记录时，用户数为1）
object test {
    def main(args: Array[String]): Unit = {
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val value: DataSet[String] = env.readTextFile("datas/calculateGPA.txt")
        val tuple: DataSet[(Double, Int)] = value.map(line => {
            val arr: Array[String] = line.split(" ")
            (arr(0).toDouble, arr(1).toInt)
        })
        val value1: DataSet[Double] = tuple.map(tuple => tuple._1 * tuple._2)
        value1.reduce((t1, t2) => t1 + t2).print()
    }
}
