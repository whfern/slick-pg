package com.github.tminglei.slickpg

import scala.slick.driver.PostgresDriver
import scala.slick.lifted.Column
import scala.slick.jdbc.{SetParameter, PositionedParameters, PositionedResult, JdbcType}

trait PgArgonautSupport extends json.PgJsonExtensions with utils.PgCommonJdbcTypes { driver: PostgresDriver =>
  import argonaut._, Argonaut._

  /// alias
  trait JsonImplicits extends ArgonautJsonImplicits

  trait ArgonautJsonImplicits {
    implicit val argonautJsonTypeMapper =
      new GenericJdbcType[Json](
        "json",
        (s) => s.parse.toOption.getOrElse(jNull),
        (v) => v.nospaces,
        hasLiteralForm = false
      )

    implicit def argonautJsonColumnExtensionMethods(c: Column[Json])(
      implicit tm: JdbcType[Json], tm1: JdbcType[List[String]]) = {
        new JsonColumnExtensionMethods[Json, Json](c)
      }
    implicit def argonautJsonOptionColumnExtensionMethods(c: Column[Option[Json]])(
      implicit tm: JdbcType[Json], tm1: JdbcType[List[String]]) = {
        new JsonColumnExtensionMethods[Json, Option[Json]](c)
      }
  }

  trait ArgonautJsonPlainImplicits {

    implicit class PgJsonPositionedResult(r: PositionedResult) {
      def nextJson() = nextJsonOption().getOrElse(jNull)
      def nextJsonOption() = r.nextStringOption().map(_.parse)
    }

    implicit object SetJson extends SetParameter[Json] {
      def apply(v: Json, pp: PositionedParameters) = setJson(Option(v), pp)
    }
    implicit object SetJsonOption extends SetParameter[Option[Json]] {
      def apply(v: Option[Json], pp: PositionedParameters) = setJson(v, pp)
    }

    ///
    private def setJson(v: Option[Json], p: PositionedParameters) = v match {
      case Some(v) => p.setObject(utils.mkPGobject("json", v.nospaces), java.sql.Types.OTHER)
      case None    => p.setNull(java.sql.Types.OTHER)
    }
  }
}
