package Table_API

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.ScalarFunction
import org.apache.flink.types.Row

object UDF_ScalarFunction {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        //1. 在创建表DDL的语句中设置时间属性字段
        tableEnv.executeSql("CREATE TABLE eventTable (" +
            "uid STRING," +
            "url STRING," +
            "ts BIGINT," +
            "eventTime AS TO_TIMESTAMP( FROM_UNIXTIME( ts / 1000 ))," +
            "WATERMARK FOR eventTime AS eventTime - INTERVAL '5' SECOND" +
            ") WITH (" +
            " 'connector' = 'filesystem'," +
            " 'path' = 'datas/clicks.txt'," +
            " 'format' = 'csv' " +
            ")")

        //注册标量函数
        tableEnv.createTemporarySystemFunction("myHash", classOf[myHash])

        //使用SQL调用函数查询转换
        val resultTable: Table = tableEnv.sqlQuery("select uid, myHash(uid) from eventTable")
        tableEnv.toAppendStream[Row](resultTable).print()
        env.execute()
    }
    //自定义UDF
    class myHash extends ScalarFunction {
        def eval(string: String): Int = {
            string.hashCode
        }
    }
}
