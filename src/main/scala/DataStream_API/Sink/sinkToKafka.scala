package DataStream_API.Sink

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer

import java.util.Properties

object sinkToKafka {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val properties = new Properties()
        properties.put("bootstrap.servers", "192.168.142.100:9092")
        val stream: DataStream[String] = env.readTextFile("datas/clicks.txt")
        stream.addSink(new FlinkKafkaProducer[String]("abc", new SimpleStringSchema(),properties))
        env.execute()
    }
}
