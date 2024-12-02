package dataStream

import org.apache.flink.streaming.api.scala._

object StreamWordCount {
    def main(args: Array[String]): Unit = {
        // 1.创建流式执行环境
        val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        // 2.读取socket文本流数据
        val stream: DataStream[String] = streamEnv.socketTextStream("192.168.142.150", 7777)
        //        val parameter = ParameterTool.fromArgs(args)
        //        val hostname = parameter.get("host")
        //        val port = parameter.getInt("port")
        //        val stream = streamEnv.socketTextStream(hostname,port)

        // 3.转换数据
        val words: DataStream[String] = stream.flatMap(_.split(" "))
        val tuple: DataStream[(String, Int)] = words.map(word => {
            (word, 1)
        })
        //分组并统计
        val group: KeyedStream[(String, Int), String] = tuple.keyBy(_._1) //根据Key分组
        val res: DataStream[(String, Int)] = group.sum(1) //对相同的Key，统计value

        // 4.输出
        res.print()

        // 5.执行流处理任务，来一个数据就处理并输出一个数据
        streamEnv.execute()
    }
}
