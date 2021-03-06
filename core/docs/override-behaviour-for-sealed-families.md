## Override behaviour for sealed families

In order for PureConfig to disambiguate between different options of a sealed
family of case classes, it must read and write additional information in
configurations. By default it uses the additional field `type`, encoding the
concrete class represented in the configuration:

```scala
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._

sealed trait AnimalConf
case class DogConf(age: Int) extends AnimalConf
case class BirdConf(canFly: Boolean) extends AnimalConf

loadConfig[AnimalConf](parseString("""{ type: "dogconf", age: 4 }"""))
// returns Right(DogConf(4))
```

For sealed families, PureConfig provides a way to customize the conversion
without replacing the default `ConfigConvert`. By putting in scope an instance
of `CoproductHint` for that sealed family, we can customize how the
disambiguation is made. For example, if `type` clashes with one of the fields
of a case class option, we can use another field:

```scala
implicit val animalConfHint = new FieldCoproductHint[AnimalConf]("kind")
loadConfig[AnimalConf](parseString("""{ kind: "dogconf", age: 4 }"""))
// returns Right(DogConf(4))
```

`FieldCoproductHint` can also be adapted to write class names in a different
way:

```scala
implicit val animalConfHint = new FieldCoproductHint[AnimalConf]("type") {
  override def fieldValue(name: String) = name.dropRight("Conf".length)
}
loadConfig[AnimalConf](parseString("""{ type: "Bird", can-fly: true }"""))
// returns Right(BirdConf(true))
```

With a `CoproductHint` you can even opt not to use any extra field at all. For
example, if you encode enumerations using sealed traits, you can just write the
name of the class:

```scala
import com.typesafe.config.{ConfigFactory, ConfigValue}
import pureconfig._
import pureconfig.syntax._

sealed trait Season
case object Spring extends Season
case object Summer extends Season
case object Autumn extends Season
case object Winter extends Season

implicit val seasonHint = new CoproductHint[Season] {

  // Reads a config for Season (`cv`).
  // - If `name` is the name of the concrete season `cv` refers to, returns
  //   `Success(Some(conf))`, where `conf` is the config for the concrete class
  //   (in this case, an empty object).
  // - If `name` is not the name of the class for `cv`, returns
  //   `Success(None)`.
  // - If `cv` is an invalid config for Season (in this case, if it isn't a
  //   string), returns a `Failure`.
  def from(cv: ConfigValue, name: String) = cv.to[String].right.map { strConf =>
    if(strConf == name) Some(ConfigFactory.empty.root) else None
  }

  // Writes a config for a Season. `cv` is a config for the concrete season
  // `name` (in this case, `cv` is always an empty object).
  def to(cv: ConfigValue, name: String) = Right(name.toConfig)

  // If `from` returns a `Failure` for a concrete class, should we try other
  // concrete classes?
  def tryNextOnFail(name: String) = false
}

case class MyConf(list: List[Season])
loadConfig[MyConf](ConfigFactory.parseString("""list = [Spring, Summer, Autumn, Winter]"""))
// returns Right(MyConf(List(Spring, Summer, Autumn, Winter)))
```
