package services

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.FiniteDuration

trait ConfigsComponent {

  val appConfig: AppConfigs
  val kafkaConfig: KafkaConfigs

  trait BaseConfigs {

    val prefix: String

    val configs: Config = ConfigFactory.load()

    def getEnvOrConf(env: String, conf: String): String = {
      val c = sys.env.getOrElse(env, configs.getString(s"$prefix.$conf"))
      println(s"$conf = $c")
      c
    }
  }

  trait AppConfigs extends BaseConfigs {

    val threads: Int
    val inletThreads: Int
    val proxyEnabled: Boolean
    val picturesPath: String
    val nnPicturesPath: String
    val downloadTimeout: FiniteDuration
    val proxyKeeperUrl: String
    val proxyKeeperPassword: String
    val deleteChunk: Int
  }

  trait KafkaConfigs extends BaseConfigs{

    val appId: String
    val nnBrokers: String
    val сBrokers: String
    val сGroupId: String
    val neuralProducerTopic: String
    val neuralConsumerTopic: String
    val clickerConsumerTopic: String

    val neuralProducerConfig: Properties

    val clickerConsumerConfig: Properties
  }
}
