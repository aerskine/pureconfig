package pureconfig.module

import _root_.enumeratum._
import _root_.enumeratum.values._
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{ fromNonEmptyStringConvertOpt, fromStringConvertOpt, fromNonEmptyStringConvert }
import pureconfig.error.CannotConvert

import scala.reflect.ClassTag

package object enumeratum {

  implicit def enumeratumConfigConvert[A <: EnumEntry](implicit enum: Enum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvertOpt[A](enum.withNameOption, _.entryName)

  implicit def enumeratumIntConfigConvert[A <: IntEnumEntry](implicit enum: IntEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvertOpt[A](v => enum.withValueOpt(v.toInt), _.value.toString)

  implicit def enumeratumLongConfigConvert[A <: LongEnumEntry](implicit enum: LongEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvertOpt[A](v => enum.withValueOpt(v.toLong), _.value.toString)

  implicit def enumeratumShortConfigConvert[A <: ShortEnumEntry](implicit enum: ShortEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvertOpt[A](v => enum.withValueOpt(v.toShort), _.value.toString)

  implicit def enumeratumStringConfigConvert[A <: StringEnumEntry](implicit enum: StringEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromStringConvertOpt[A](v => enum.withValueOpt(v), _.value.toString)

  implicit def enumeratumByteConfigConvert[A <: ByteEnumEntry](implicit enum: ByteEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvertOpt[A](v => enum.withValueOpt(v.toByte), _.value.toString)

  implicit def enumeratumCharConfigConvert[A <: CharEnumEntry](implicit enum: CharEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvert[A](
      s => location => ensureOneChar(s) match {
        case Right(v) => Right(enum.withValue(v))
        case Left(msg) => Left(CannotConvert(s, ct.runtimeClass.getSimpleName, msg, location))
      },
      _.value.toString)

  private val ensureOneChar: Seq[Char] => Either[String, Char] = {
    case Seq(c) => Right(c)
    case s => Left(s"""Cannot read a character value from "$s"""")
  }
}
