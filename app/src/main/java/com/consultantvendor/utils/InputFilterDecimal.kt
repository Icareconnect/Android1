package com.consultantvendor.utils

import android.text.Spanned
import android.text.method.DigitsKeyListener

class InputFilterDecimal : DigitsKeyListener(java.lang.Boolean.FALSE, java.lang.Boolean.TRUE) {

    private val beforeDecimal: Int
    private val afterDecimal: Int

    init {
        this.beforeDecimal = 10
        this.afterDecimal = 3
    }

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
        val builder = StringBuilder(dest)
        builder.insert(dstart, source)
        val temp = builder.toString()

        if (temp.trim { it <= ' ' } == " ") {
            return ""
        } else if (temp.indexOf('.') == -1) {
            if (temp.length > beforeDecimal) {
                return ""
            }
        } else {
            if (temp.substring(0, temp.indexOf('.')).length > beforeDecimal || temp.substring(temp.indexOf('.') + 1, temp.length).length > afterDecimal) {
                return ""
            }
        }

        return super.filter(source, start, end, dest, dstart, dend)
    }

}