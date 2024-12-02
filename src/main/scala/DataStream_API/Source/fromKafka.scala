package DataStream_API.Source

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.util._

object fromKafka {
    def main(args: Array[String]): Unit = {
        val environment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        //Kafka连接配置，用Properties保存配置
        //Properties是一个存储键值对的容器，通常存储配置文件
        val properties = new Properties()
        properties.setProperty("bootstrap.servers", "192.168.142.100:9092")
        properties.setProperty("group.id", "consume-group")

        val kafkaSource = new FlinkKafkaConsumer[String]("WordCount", new SimpleStringSchema(), properties) //SimpleStringSchema用于String的序列化和反序列化
        val stream: DataStream[String] = environment.addSource(kafkaSource)

        val words: DataStream[String] = stream.flatMap(_.split(" "))
        val tuple: DataStream[(String, Int)] = words.map(word => {
            (word, 1)
        })
        val res: DataStream[(String, Int)] = tuple.keyBy(_._1).sum(1)

        res.print()

        environment.execute()
    }
}
