package DataStream_API.Source

import org.apache.flink.streaming.api.scala._

object fromTextFile {
    def main(args: Array[String]): Unit = {
        val environment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        val text: DataStream[String] = environment.readTextFile("datas/score.txt")
        val value: DataStream[String] = text.filter(l => l.contains("Flink"))

        environment.execute()
    }
}
