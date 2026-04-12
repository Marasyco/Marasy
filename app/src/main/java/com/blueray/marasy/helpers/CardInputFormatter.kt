package com.blueray.marasy.helpers

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Utility class for formatting and validating credit card inputs
 */
object CardInputFormatter {
    
    /**
     * Formats card number with spaces every 4 digits (e.g., 1234 5678 9012 3456)
     */
    class CardNumberTextWatcher(private val editText: EditText) : TextWatcher {
        private var isFormatting = false
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        
        override fun afterTextChanged(editable: Editable?) {
            if (isFormatting) return
            
            isFormatting = true
            
            // Remove all spaces
            val original = editable.toString()
            val digitsOnly = original.replace(" ", "")
            
            // Limit to 16 digits
            val limitedDigits = if (digitsOnly.length > 16) {
                digitsOnly.substring(0, 16)
            } else {
                digitsOnly
            }
            
            // Add spaces every 4 digits
            val formatted = StringBuilder()
            for (i in limitedDigits.indices) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ")
                }
                formatted.append(limitedDigits[i])
            }
            
            // Update the EditText
            val formattedString = formatted.toString()
            if (formattedString != original) {
                editText.setText(formattedString)
                editText.setSelection(formattedString.length)
            }
            
            isFormatting = false
        }
    }
    
    /**
     * Formats expiry date as MM/YY
     */
    class ExpiryDateTextWatcher(private val editText: EditText) : TextWatcher {
        private var isFormatting = false
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        
        override fun afterTextChanged(editable: Editable?) {
            if (isFormatting) return
            
            isFormatting = true
            
            val original = editable.toString()
            val digitsOnly = original.replace("/", "")
            
            // Limit to 4 digits (MMYY)
            val limitedDigits = if (digitsOnly.length > 4) {
                digitsOnly.substring(0, 4)
            } else {
                digitsOnly
            }
            
            // Format as MM/YY
            val formatted = when {
                limitedDigits.length >= 3 -> {
                    "${limitedDigits.substring(0, 2)}/${limitedDigits.substring(2)}"
                }
                limitedDigits.length == 2 && !original.endsWith("/") -> {
                    "$limitedDigits/"
                }
                else -> limitedDigits
            }
            
            if (formatted != original) {
                editText.setText(formatted)
                editText.setSelection(formatted.length)
            }
            
            isFormatting = false
        }
    }
    
    /**
     * Limits CVV to 3 or 4 digits
     */
    class CvvTextWatcher(private val editText: EditText, private val maxLength: Int = 4) : TextWatcher {
        private var isFormatting = false
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        
        override fun afterTextChanged(editable: Editable?) {
            if (isFormatting) return
            
            isFormatting = true
            
            val original = editable.toString()
            val digitsOnly = original.filter { it.isDigit() }
            
            val limited = if (digitsOnly.length > maxLength) {
                digitsOnly.substring(0, maxLength)
            } else {
                digitsOnly
            }
            
            if (limited != original) {
                editText.setText(limited)
                editText.setSelection(limited.length)
            }
            
            isFormatting = false
        }
    }
    
    /**
     * Validates card number using Luhn algorithm
     */
    fun validateCardNumber(cardNumber: String): Boolean {
        val digitsOnly = cardNumber.replace(" ", "")
        
        if (digitsOnly.length < 13 || digitsOnly.length > 19) {
            return false
        }
        
        // Luhn algorithm
        var sum = 0
        var alternate = false
        
        for (i in digitsOnly.length - 1 downTo 0) {
            var digit = digitsOnly[i].toString().toInt()
            
            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit = (digit % 10) + 1
                }
            }
            
            sum += digit
            alternate = !alternate
        }
        
        return sum % 10 == 0
    }
    
    /**
     * Validates expiry date (MM/YY format)
     */
    fun validateExpiryDate(expiryDate: String): Boolean {
        val parts = expiryDate.split("/")
        
        if (parts.size != 2) return false
        
        val month = parts[0].toIntOrNull() ?: return false
        val year = parts[1].toIntOrNull() ?: return false
        
        if (month < 1 || month > 12) return false
        if (year < 0 || year > 99) return false
        
        // Check if card is expired
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        
        if (year < currentYear) return false
        if (year == currentYear && month < currentMonth) return false
        
        return true
    }
    
    /**
     * Validates CVV (3 or 4 digits)
     */
    fun validateCvv(cvv: String): Boolean {
        return cvv.length in 3..4 && cvv.all { it.isDigit() }
    }
    
    /**
     * Validates cardholder name (at least 2 parts)
     */
    fun validateCardholderName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.split(" ").filter { it.isNotEmpty() }.size >= 2
    }
    
    /**
     * Gets card type from card number
     */
    fun getCardType(cardNumber: String): String {
        val digitsOnly = cardNumber.replace(" ", "")
        
        return when {
            digitsOnly.startsWith("4") -> "Visa"
            digitsOnly.startsWith("5") -> "Mastercard"
            digitsOnly.startsWith("3") -> "American Express"
            digitsOnly.startsWith("6") -> "Discover"
            else -> "Unknown"
        }
    }
    
    /**
     * Extracts digits only from card number
     */
    fun getCardNumberDigitsOnly(cardNumber: String): String {
        return cardNumber.replace(" ", "")
    }
    
    /**
     * Extracts expiry month from MM/YY format
     */
    fun getExpiryMonth(expiryDate: String): String {
        return expiryDate.split("/").firstOrNull() ?: ""
    }
    
    /**
     * Extracts expiry year from MM/YY format (returns 2-digit year YY)
     */
    fun getExpiryYear(expiryDate: String): String {
        return expiryDate.split("/").getOrNull(1) ?: ""
    }
}
