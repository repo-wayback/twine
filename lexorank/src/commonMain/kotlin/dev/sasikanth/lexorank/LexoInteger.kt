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

import dev.sasikanth.lexorank.numeralSystems.LexoNumeralSystem

class LexoInteger
private constructor(
  private val sys: LexoNumeralSystem,
  private val sign: Int,
  private val mag: IntArray
) {

  companion object {
    private val ZERO_MAG = intArrayOf(0)
    private val ONE_MAG = intArrayOf(1)
    private const val NEGATIVE_SIGN = -1
    private const val ZERO_SIGN = 0
    private const val POSITIVE_SIGN = 1

    fun parse(strFull: String, system: LexoNumeralSystem): LexoInteger {
      var str = strFull
      var sign = POSITIVE_SIGN
      if (strFull.indexOf(system.positiveChar()) == 0) {
        str = strFull.substring(1)
      } else if (strFull.indexOf(system.negativeChar()) == 0) {
        str = strFull.substring(1)
        sign = NEGATIVE_SIGN
      }

      val mag = IntArray(str.length)
      var strIndex = mag.size - 1

      for (magIndex in 0 until mag.size) {
        mag[magIndex] = system.toDigit(str[strIndex])
        strIndex--
      }

      return make(system, sign, mag)
    }

    fun zero(sys: LexoNumeralSystem): LexoInteger {
      return LexoInteger(sys, ZERO_SIGN, ZERO_MAG)
    }

    fun one(sys: LexoNumeralSystem): LexoInteger {
      return make(sys, POSITIVE_SIGN, ONE_MAG)
    }

    fun make(sys: LexoNumeralSystem, sign: Int, mag: IntArray): LexoInteger {
      var actualLength = mag.size
      while (actualLength > 0 && mag[actualLength - 1] == 0) {
        actualLength--
      }

      if (actualLength == 0) {
        return zero(sys)
      }

      if (actualLength == mag.size) {
        return LexoInteger(sys, sign, mag)
      }

      val nmag = IntArray(actualLength) { 0 }
      LexoHelper.arrayCopy(mag, 0, nmag, 0, actualLength)
      return LexoInteger(sys, sign, nmag)
    }

    private fun add(sys: LexoNumeralSystem, l: IntArray, r: IntArray): IntArray {
      val estimatedSize = maxOf(l.size, r.size)
      val result = IntArray(estimatedSize) { 0 }
      var carry = 0
      for (i in 0 until estimatedSize) {
        val lnum = if (i < l.size) l[i] else 0
        val rnum = if (i < r.size) r[i] else 0
        var sum = lnum + rnum + carry
        while (sum >= sys.base()) {
          carry++
          sum -= sys.base()
        }
        result[i] = sum
      }
      return extendWithCarry(result, carry)
    }

    private fun extendWithCarry(mag: IntArray, carry: Int): IntArray {
      return if (carry > 0) {
        val extendedMag = IntArray(mag.size + 1) { 0 }
        LexoHelper.arrayCopy(mag, 0, extendedMag, 0, mag.size)
        extendedMag[extendedMag.size - 1] = carry
        extendedMag
      } else {
        mag
      }
    }

    private fun subtract(sys: LexoNumeralSystem, l: IntArray, r: IntArray): IntArray {
      val rComplement = complement(sys, r, l.size)
      val rSum = add(sys, l, rComplement)
      rSum[rSum.size - 1] = 0
      return add(sys, rSum, ONE_MAG)
    }

    private fun multiply(sys: LexoNumeralSystem, l: IntArray, r: IntArray): IntArray {
      val result = IntArray(l.size + r.size) { 0 }
      for (li in l.indices) {
        for (ri in r.indices) {
          val resultIndex = li + ri
          var product = l[li] * r[ri]
          while (product >= sys.base()) {
            result[resultIndex + 1]++
            product -= sys.base()
          }
          result[resultIndex] += product
        }
      }
      return result
    }

    private fun complement(sys: LexoNumeralSystem, mag: IntArray, digits: Int): IntArray {
      if (digits <= 0) {
        throw IllegalArgumentException("Expected at least 1 digit")
      }

      val nmag = IntArray(digits) { sys.base() - 1 }
      for (i in mag.indices) {
        nmag[i] = sys.base() - 1 - mag[i]
      }
      return nmag
    }

    private fun compare(l: IntArray, r: IntArray): Int {
      if (l.size < r.size) {
        return -1
      }

      if (l.size > r.size) {
        return 1
      }

      for (i in l.size - 1 downTo 0) {
        if (l[i] < r[i]) {
          return -1
        }
        if (l[i] > r[i]) {
          return 1
        }
      }
      return 0
    }
  }

  fun add(other: LexoInteger): LexoInteger {
    checkSystem(other)
    if (isZero()) {
      return other
    }

    if (other.isZero()) {
      return this
    }

    if (sign != other.sign) {
      val pos: LexoInteger

      return if (sign == -1) {
        pos = negate()
        pos.subtract(other).negate()
      } else {
        pos = other.negate()
        subtract(pos)
      }
    }

    val result = add(sys, mag, other.mag)
    return make(sys, sign, result)
  }

  fun subtract(other: LexoInteger): LexoInteger {
    checkSystem(other)
    if (isZero()) {
      return other.negate()
    }

    if (other.isZero()) {
      return this
    }

    if (sign != other.sign) {
      val negate: LexoInteger
      val sum: LexoInteger
      if (sign == -1) {
        negate = negate()
        sum = negate.add(other)
        return sum.negate()
      } else {
        negate = other.negate()
        return add(negate)
      }
    }

    val cmp = compare(mag, other.mag)
    return if (cmp == 0) {
      zero(sys)
    } else if (cmp < 0) {
      make(sys, if (sign == -1) 1 else -1, subtract(sys, other.mag, mag))
    } else {
      make(sys, if (sign == -1) -1 else 1, subtract(sys, mag, other.mag))
    }
  }

  fun multiply(other: LexoInteger): LexoInteger {
    checkSystem(other)
    if (isZero()) {
      return this
    }

    if (other.isZero()) {
      return other
    }

    if (isOneish()) {
      return if (sign == other.sign) {
        make(sys, 1, other.mag)
      } else {
        make(sys, -1, other.mag)
      }
    }

    if (other.isOneish()) {
      return if (sign == other.sign) {
        make(sys, 1, mag)
      } else {
        make(sys, -1, mag)
      }
    }

    val newMag = multiply(sys, mag, other.mag)
    return if (sign == other.sign) {
      make(sys, 1, newMag)
    } else {
      make(sys, -1, newMag)
    }
  }

  fun negate(): LexoInteger {
    return if (isZero()) {
      this
    } else {
      make(sys, if (sign == 1) -1 else 1, mag)
    }
  }

  fun shiftLeft(times: Int = 1): LexoInteger {
    if (times == 0) {
      return this
    }

    return if (times < 0) {
      shiftRight(-times)
    } else {
      val nmag = IntArray(mag.size + times) { 0 }
      LexoHelper.arrayCopy(mag, 0, nmag, times, mag.size)
      make(sys, sign, nmag)
    }
  }

  fun shiftRight(times: Int = 1): LexoInteger {
    return if (mag.size - times <= 0) {
      zero(sys)
    } else {
      val nmag = IntArray(mag.size - times) { 0 }
      LexoHelper.arrayCopy(mag, times, nmag, 0, nmag.size)
      make(sys, sign, nmag)
    }
  }

  fun complement(): LexoInteger {
    return complementDigits(mag.size)
  }

  fun complementDigits(digits: Int): LexoInteger {
    return make(sys, sign, complement(sys, mag, digits))
  }

  fun isZero(): Boolean {
    return sign == 0 && mag.size == 1 && mag[0] == 0
  }

  fun isOne(): Boolean {
    return sign == 1 && mag.size == 1 && mag[0] == 1
  }

  fun getMag(index: Int): Int {
    return mag[index]
  }

  fun compareTo(other: LexoInteger?): Int {
    if (this === other) {
      return 0
    }

    if (other == null) {
      return 1
    }

    if (sign == -1) {
      if (other.sign == -1) {
        val cmp = compare(mag, other.mag)
        if (cmp == -1) {
          return 1
        }
        return if (cmp == 1) -1 else 0
      }

      return -1
    }

    if (sign == 1) {
      return if (other.sign == 1) {
        compare(mag, other.mag)
      } else {
        1
      }
    }

    if (other.sign == -1) {
      return 1
    }

    return if (other.sign == 1) {
      -1
    } else {
      0
    }
  }

  fun getSystem(): LexoNumeralSystem {
    return sys
  }

  fun format(): String {
    if (isZero()) {
      return "" + sys.toChar(0)
    }

    val sb = StringBuilder()
    for (digit in mag) {
      sb.insert(0, sys.toChar(digit))
    }

    if (sign == -1) {
      sb.insert(0, sys.negativeChar())
    }

    return sb.toString()
  }

  @Suppress("CovariantEquals")
  fun equals(other: LexoInteger?): Boolean {
    if (this === other) {
      return true
    }

    if (other == null) {
      return false
    }

    return sys.base() == other.sys.base() && compareTo(other) == 0
  }

  override fun toString(): String {
    return format()
  }

  private fun isOneish(): Boolean {
    return mag.size == 1 && mag[0] == 1
  }

  private fun checkSystem(other: LexoInteger) {
    if (sys.base() != other.sys.base()) {
      throw IllegalArgumentException("Expected numbers of the same numeral sys")
    }
  }
}
