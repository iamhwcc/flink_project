package dataStream

import org.apache.flink.api.scala._

object BatchWordCount {
    def main(args: Array[String]): Unit = {
        // 1.创建执行环境
        val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

        // 2.读取数据
        val filePath = "datas/word.txt"
        val lines: DataSet[String] = env.readTextFile(filePath)

        // 3.转换数据
        val words: DataSet[String] = lines.flatMap(_.split(" "))
        val tuple: DataSet[(String, Int)] = words.map(word => {
            (word, 1)
        })
        //分组并统计
        val group: GroupedDataSet[(String, Int)]= tuple.groupBy(0)
        val res: AggregateDataSet[(String, Int)] = group.sum(1)
        // 4.输出数据
        res.print()
    }
}
