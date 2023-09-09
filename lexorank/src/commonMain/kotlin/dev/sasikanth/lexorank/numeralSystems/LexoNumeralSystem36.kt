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
package dev.sasikanth.lexorank.numeralSystems

import de.cketti.codepoints.deluxe.toCodePoint

class LexoNumeralSystem36 : LexoNumeralSystem {
  private val DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz".toList()

  override fun base(): Int {
    return 36
  }

  override fun positiveChar(): Char {
    return '+'
  }

  override fun negativeChar(): Char {
    return '-'
  }

  override fun radixPointChar(): Char {
    return ':'
  }

  override fun toDigit(variable: Char): Int {
    return when (variable) {
      in '0'..'9' -> {
        variable.toCodePoint().value - 48
      }
      in 'a'..'z' -> {
        variable.toCodePoint().value - 97 + 10
      }
      else -> {
        throw IllegalArgumentException("Variable should be within (0..9 || a..z) range")
      }
    }
  }

  override fun toChar(codepoint: Int): Char {
    return DIGITS[codepoint]
  }
}
