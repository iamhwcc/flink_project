package DingDingExercise.TableAPI

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.table.api.Expressions.$
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

import java.util.Properties

object exercise3 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        //连接kafka，建立输入表
        tableEnv.executeSql("CREATE TABLE instudent (" +
            "id STRING," +
            "name STRING," +
            "score DOUBLE," +
            "`time` BIGINT" +
            ") WITH (" +
            " 'connector' = 'kafka', " +
            " 'format' = 'csv', " +
            " 'topic' = 'input_topic'," +
            " 'properties.bootstrap.servers' = '172.16.214.129:9092'" +
            ")")
        //连接kafka，建立输出表
        tableEnv.executeSql("CREATE TABLE outstudent (" +
            "id STRING," +
            "name STRING," +
            "score DOUBLE," +
            "`time` BIGINT" +
            ") WITH (" +
            " 'connector' = 'kafka', " +
            " 'format' = 'csv', " +
            " 'topic' = 'output_topic'," +
            " 'properties.bootstrap.servers' = '172.16.214.129:9092'" +
            ")")
        val resultTable: Table = tableEnv.sqlQuery("select * from instudent where id = 'Stu_1'")
        resultTable.executeInsert("outstudent")
    }
}
