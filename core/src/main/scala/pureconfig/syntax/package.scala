package pureconfig

import com.typesafe.config.{ ConfigValue, Config => TypesafeConfig }
import pureconfig.error.ConfigReaderFailures

package object syntax {
  implicit class PimpedAny[T](val any: T) extends AnyVal {
    def toConfig(implicit configConvert: ConfigConvert[T]): ConfigValue = configConvert.to(any)
  }

  implicit class PimpedConfigValue(val conf: ConfigValue) extends AnyVal {
    def to[T](implicit configConvert: ConfigConvert[T]): Either[ConfigReaderFailures, T] = configConvert.from(conf)
  }

  implicit class PimpedConfig(val conf: TypesafeConfig) extends AnyVal {
    def to[T: ConfigConvert]: Either[ConfigReaderFailures, T] = conf.root().to[T]
  }
}
