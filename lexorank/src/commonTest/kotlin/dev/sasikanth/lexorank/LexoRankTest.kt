/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.lexorank

import kotlin.test.Test
import kotlin.test.assertEquals

class LexoRankTest {

  @Test
  fun minLexoRank() {
    assertEquals("0|000000:", LexoRank.min().toString())
  }

  @Test
  fun maxLexoRank() {
    assertEquals("0|zzzzzz:", LexoRank.max().toString())
  }

  @Test
  fun betweenMinMax() {
    val min = LexoRank.min()
    val max = LexoRank.max()
    val between = min.between(max).toString()

    assertEquals("0|hzzzzz:", between)
  }

  @Test
  fun betweenMinGetNext() {
    val min = LexoRank.min()
    val next = min.genNext()
    val between = min.between(next).toString()

    assertEquals("0|0i0000:", between)
  }

  @Test
  fun betweenMaxGetPrev() {
    val min = LexoRank.max()
    val next = min.genPrev()
    val between = min.between(next).toString()

    assertEquals("0|yzzzzz:", between)
  }

  @Test
  fun moveToTest() {
    val testData =
      listOf(
        Triple("0", "1", "0|0i0000:"),
        Triple("1", "0", "0|0i0000:"),
        Triple("3", "5", "0|10000o:"),
        Triple("5", "3", "0|10000o:"),
        Triple("15", "30", "0|10004s:"),
        Triple("31", "32", "0|10006s:"),
        Triple("100", "200", "0|1000x4:"),
        Triple("200", "100", "0|1000x4:")
      )

    testData.forEach { (prevStep, nextStep, expected) ->
      var prevRank = LexoRank.min()
      val prevStepInt = +prevStep.toInt()

      for (i in 0 until prevStepInt) {
        prevRank = prevRank.genNext()
      }

      var nextRank = LexoRank.min()
      val nextStepInt = +nextStep.toInt()

      for (i in 0 until nextStepInt) {
        nextRank = nextRank.genNext()
      }

      val between = prevRank.between(nextRank)
      assertEquals(expected, between.toString())
    }
  }
}
