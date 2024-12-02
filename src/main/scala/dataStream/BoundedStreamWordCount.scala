package dataStream

import org.apache.flink.streaming.api.scala._

object BoundedStreamWordCount {
    def main(args: Array[String]): Unit = {
        // 1.创建流式执行环境
        val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        streamEnv.setParallelism(4)

        // 2.读取文本
        val filePath = "datas/word.txt"
        val lines: DataStream[String] = streamEnv.readTextFile(filePath)

        // 3.转换数据
        val words: DataStream[String] = lines.flatMap(_.split(" "))
        val tuple: DataStream[(String, Int)] = words.map(word => {
            (word, 1)
        })
        //分组并统计
        val group: KeyedStream[(String, Int), String] = tuple.keyBy(_._1)
        val res: DataStream[(String, Int)] = group.sum(1)

        // 4.输出
        res.print()

        // 5.执行流处理任务，来一个数据就处理并输出一个数据
        streamEnv.execute()
    }
}
