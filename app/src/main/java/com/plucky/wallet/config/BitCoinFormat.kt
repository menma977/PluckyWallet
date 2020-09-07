package com.plucky.wallet.config

import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * class BitCoinFormat
 * @property longFormat BigDecimal
 * @property bigDecimalFormat BigDecimal
 */
class BitCoinFormat {
  private var longFormat = BigDecimal(100000000)
  private var bigDecimalFormat = BigDecimal(0.00000001)
  private val decimalFormat = DecimalFormat("#.########")

  fun dogeToDecimal(value: BigDecimal): BigDecimal {
    return value.multiply(longFormat).setScale(0, BigDecimal.ROUND_HALF_DOWN)
  }

  fun decimalToDoge(value: BigDecimal): BigDecimal {
    return decimalFormat.format(value.multiply(bigDecimalFormat).setScale(8, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".").toBigDecimal()
  }

  fun toDollar(value: BigDecimal): BigDecimal {
    return value.setScale(2, BigDecimal.ROUND_HALF_DOWN)
  }
}