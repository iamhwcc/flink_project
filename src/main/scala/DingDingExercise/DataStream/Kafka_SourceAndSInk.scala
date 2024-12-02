package DingDingExercise.DataStream

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

object Kafka_SourceAndSInk {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val properties = new Properties()
        properties.setProperty("bootstrap.servers","172.16.214.129:9092")
        val kafkaConsumer = new FlinkKafkaConsumer[String]("scoreCon",new SimpleStringSchema(),properties)
        val kafka: DataStream[String] = env.addSource(kafkaConsumer)
        val MaryLines: DataStream[String] = kafka.filter(line => line.contains("Mary"))
        val MaryCourses: DataStream[(String, String)] = MaryLines.map(line => {
            val words = line.split(" ")
            (words(0), words(1))
        })
        val tuples: DataStream[(String, Int)] = MaryCourses.map(t => (t._2, 1))
        val res: DataStream[(String, Int)] = tuples.keyBy(_ => "Mary").reduce((v1, v2) => ("Mary", v1._2 + v2._2))
        val finalRes: DataStream[String] = res.map(_._2.toString)
        finalRes.addSink(new FlinkKafkaProducer[String]("scorePro", new SimpleStringSchema(),properties))
        env.execute()
    }
}
