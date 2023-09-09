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

class LexoDecimal private constructor(private val mag: LexoInteger, private val sig: Int) {

  companion object {

    fun half(sys: LexoNumeralSystem): LexoDecimal {
      val mid = sys.base() / 2
      return make(LexoInteger.make(sys, 1, intArrayOf(mid)), 1)
    }

    fun parse(str: String, system: LexoNumeralSystem): LexoDecimal {
      val partialIndex = str.indexOf(system.radixPointChar())
      if (str.lastIndexOf(system.radixPointChar()) != partialIndex) {
        throw Error("More than one ${system.radixPointChar()}")
      }

      if (partialIndex < 0) {
        return make(LexoInteger.parse(str, system), 0)
      }

      val intStr = str.substring(0, partialIndex) + str.substring(partialIndex + 1)
      return make(LexoInteger.parse(intStr, system), str.length - 1 - partialIndex)
    }

    fun from(integer: LexoInteger): LexoDecimal {
      return make(integer, 0)
    }

    private fun make(integer: LexoInteger, sig: Int): LexoDecimal {
      if (integer.isZero()) {
        return LexoDecimal(integer, 0)
      }

      var zeroCount = 0
      var i = 0
      while (i < sig && integer.getMag(i) == 0) {
        zeroCount++
        i++
      }

      val newInteger = integer.shiftRight(zeroCount)
      val newSig = sig - zeroCount
      return LexoDecimal(newInteger, newSig)
    }
  }

  fun getSystem(): LexoNumeralSystem {
    return mag.getSystem()
  }

  fun add(other: LexoDecimal): LexoDecimal {
    var tmag = this.mag
    val tsig = this.sig
    var omag = other.mag
    var osig: Int = other.sig

    for (i in osig until tsig) {
      tmag = tmag.shiftLeft()
    }

    while (tsig > other.sig) {
      omag = omag.shiftLeft()
      osig++
    }

    return make(tmag.add(omag), tsig)
  }

  fun subtract(other: LexoDecimal): LexoDecimal {
    var thisMag = this.mag
    val thisSig = this.sig
    var otherMag = other.mag
    var otherSig: Int = other.sig

    for (i in otherSig until thisSig) {
      thisMag = thisMag.shiftLeft()
    }

    while (thisSig > otherSig) {
      otherMag = otherMag.shiftLeft()
      otherSig++
    }

    return make(thisMag.subtract(otherMag), thisSig)
  }

  fun multiply(other: LexoDecimal): LexoDecimal {
    return make(this.mag.multiply(other.mag), this.sig + other.sig)
  }

  fun floor(): LexoInteger {
    return this.mag.shiftRight(this.sig)
  }

  fun ceil(): LexoInteger {
    if (this.isExact()) {
      return this.mag
    }

    val floor = this.floor()
    return floor.add(LexoInteger.one(floor.getSystem()))
  }

  fun isExact(): Boolean {
    if (this.sig == 0) {
      return true
    }

    for (i in 0 until this.sig) {
      if (this.mag.getMag(i) != 0) {
        return false
      }
    }

    return true
  }

  fun getScale(): Int {
    return this.sig
  }

  fun setScale(nsig: Int, ceiling: Boolean = false): LexoDecimal {
    var newSig = nsig
    if (newSig >= this.sig) {
      return this
    }

    if (newSig < 0) {
      newSig = 0
    }

    val diff = this.sig - newSig
    var nmag = this.mag.shiftRight(diff)
    if (ceiling) {
      nmag = nmag.add(LexoInteger.one(nmag.getSystem()))
    }

    return make(nmag, newSig)
  }

  fun compareTo(other: LexoDecimal?): Int {
    if (this === other) {
      return 0
    }

    if (other == null) {
      return 1
    }

    var tMag = this.mag
    var oMag = other.mag

    if (this.sig > other.sig) {
      oMag = oMag.shiftLeft(this.sig - other.sig)
    } else if (this.sig < other.sig) {
      tMag = tMag.shiftLeft(other.sig - this.sig)
    }
    return tMag.compareTo(oMag)
  }

  fun format(): String {
    val intStr = mag.format()
    if (sig == 0) {
      return intStr
    }

    val stringBuilder = StringBuilder(intStr)
    val head = stringBuilder[0]
    val specialHead =
      head == mag.getSystem().positiveChar() || head == mag.getSystem().negativeChar()

    if (specialHead) {
      stringBuilder.deleteRange(0, 1)
    }

    while (stringBuilder.length < sig + 1) {
      stringBuilder.insert(0, mag.getSystem().toChar(0))
    }

    stringBuilder.insert(stringBuilder.length - sig, mag.getSystem().radixPointChar())

    if (stringBuilder.length - sig == 0) {
      stringBuilder.insert(0, mag.getSystem().toChar(0))
    }

    if (specialHead) {
      stringBuilder.insert(0, head)
    }

    return stringBuilder.toString()
  }

  @Suppress("CovariantEquals")
  fun equals(other: LexoDecimal?): Boolean {
    if (this === other) {
      return true
    }

    if (other == null) {
      return false
    }

    return this.mag.equals(other.mag) && this.sig == other.sig
  }

  override fun toString(): String {
    return this.format()
  }
}
