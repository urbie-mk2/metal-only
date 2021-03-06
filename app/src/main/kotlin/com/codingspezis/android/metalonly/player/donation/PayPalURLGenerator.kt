package com.codingspezis.android.metalonly.player.donation

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object PayPalURLGenerator {
    val utf8 = "UTF-8"

    /**
     * @param donationValue how much is donated
     * *
     * @param donator who is donating
     * *
     * @return PayPal URL for donating entered value
     */
    fun generatePaypalURL(donationValue: Float, donator: String): String {
        try {
            val correctedDonator: String =
                    if (donator.trim().isEmpty()) "ANONYM"
                    else donator

            val encodedDonationValue = URLEncoder.encode("" + donationValue, utf8)
            val encodedDonator = URLEncoder.encode(correctedDonator, utf8)

            // TODO Should this be moved to @Rest?
            return "https://www.paypal.com/cgi-bin/webscr?business=metalonly@gmx.de&cmd=_xclick&currency_code=EUR&amount=$encodedDonationValue&item_name=$encodedDonator%20Spende%20METAL%20ONLY%20e.V."
        } catch (e: UnsupportedEncodingException) {
            /* UTF-8 is a supported encoding */
        }
        return "https://www.paypal.com/"
    }
}
