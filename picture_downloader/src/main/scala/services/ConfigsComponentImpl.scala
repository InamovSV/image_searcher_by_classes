package services

import java.util.Properties

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Random, Try}
trait ConfigsComponentImpl extends ConfigsComponent {

  class AppConfigsImpl extends AppConfigs {

    override val prefix: String = "app"

    val threads: Int = getEnvOrConf("downloader_threads","threads").toInt
    val proxyEnabled: Boolean = getEnvOrConf("d_proxy_enabled", "proxyEnabled").toBoolean
    val picturesPath: String = getEnvOrConf("d_pictures_path","picturesPath")
    val nnPicturesPath: String = getEnvOrConf("d_nn_pictures_path", "nnPicturesPath")
    val downloadTimeout: FiniteDuration = getEnvOrConf("d_timeout","downloadTimeoutMillisec").toInt milliseconds
    val proxyKeeperUrl: String = getEnvOrConf("proxykeeper_url", "proxyKeeperUrl")
    val proxyKeeperPassword: String = getEnvOrConf("proxykeeper_password", "proxyKeeperPassword")
    override val inletThreads: Int = getEnvOrConf("d_inlet_threads", "inletThreads").toInt
    override val deleteChunk: Int = getEnvOrConf("d_delete_chunk", "deleteChunk").toInt
  }

  class KafkaConfigsImpl extends KafkaConfigs {
    override val prefix: String = "kafka"

    val appId: String = Try(getEnvOrConf("neuralnet_appId", "neuralnet.appId")).getOrElse(Random.alphanumeric.take(6).mkString)
    val nnBrokers: String = getEnvOrConf("neuralnet_brokers", "neuralnet.brokers")
    val сBrokers: String = getEnvOrConf("clicker_brokers", "clicker.brokers")
    val сGroupId: String = getEnvOrConf("kafka_clicker_group_id", "clicker.groupId")
    val neuralProducerTopic: String = getEnvOrConf("kafka_neural_p_topic", "neuralnet.p_topic")
    val neuralConsumerTopic: String = getEnvOrConf("kafka_neural_c_topic", "neuralnet.с_topic")
    val clickerConsumerTopic: String = getEnvOrConf("kafka_clicker_c_topic", "clicker.c_topic")

    val neuralProducerConfig: Properties = {
      val props = new Properties()
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, nnBrokers)
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      //props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
      //props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "src/main/resources/keys/client.truststore.jks")
      //props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "deepcaptcha")
      //props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "src/main/resources/keys/kafka.client.keystore.jks")
      //props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "deepcaptcha")
      //props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "deepcaptcha")
      props
    }

    val clickerConsumerConfig: Properties = {
      val props = new Properties()
      props.put(ConsumerConfig.CLIENT_ID_CONFIG, appId)
      props.put(ConsumerConfig.GROUP_ID_CONFIG, сGroupId)
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, сBrokers)
      //props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
      //props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "src/main/resources/keys/client.truststore.jks")
      //props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "deepcaptcha")
      //props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "src/main/resources/keys/kafka.client.keystore.jks")
      //props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "deepcaptcha")
      //props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "deepcaptcha")
      props
    }
  }
}
