package DataStream_API.Transformation

import org.apache.flink.streaming.api.scala._

object mapTest {
    def main(args: Array[String]): Unit = {
        val environment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        val stream: DataStream[Int] = environment.fromElements(1, 2, 3, 4, 5)
        val value: DataStream[Int] = stream.map(_ + 1)
        value.print()
        environment.execute()
    }
}
