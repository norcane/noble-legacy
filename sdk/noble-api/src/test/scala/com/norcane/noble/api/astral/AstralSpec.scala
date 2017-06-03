/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016-2017 norcane
 * |_| |_|\___/|_.__/|_|\___|
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.norcane.noble.api.astral

import org.specs2.matcher.Matchers
import org.specs2.mutable

/**
  * ''Specs2'' specification for the [[Astral]] class.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class AstralSpec extends mutable.Specification with Matchers {

  import Astral.Defaults._

  private val ast: Astral = Astral(Map(
    "string" -> "some text",
    "int" -> 42,
    "object1" -> Astral(Map(
      "object2" -> Astral(Map(
        "orwell" -> 1984
      ))
    ))
  ))

  "The ASTral object should" >> {

    "properly lookup direct value" >> {
      ast.get[String]("string") should beSome("some text")
    }

    "properly check value existence" >> {
      ast.has[Int]("int") should beTrue
    }

    "properly lookup nested ASTral objects" >> {
      ast.get[Int]("object1/object2/orwell") should beSome(1984)
    }

    "properly return keys collection" >> {
      ast.keys.toSet should beEqualTo(Set("string", "int", "object1"))
    }
  }

}
