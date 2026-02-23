package com.smartshop.app.utils
// Extensions.kt
import android.util.Patterns
import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

// Email validation
fun String.isValidEmail(): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(this).matches()

// Password validation
fun String.isValidPassword(): Boolean = length >= 6

// Format price
fun Double.toCurrencyString(): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(this)

// View visibility helpers
fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

// Show snackbar
fun View.showSnackbar(message: String) =
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()