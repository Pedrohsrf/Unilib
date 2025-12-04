package com.example.unilib.Salas.Fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.unilib.Classes.Rotinas.getColor
import com.example.unilib.R
import java.util.Calendar

class DateDialog(
    private val dateSetCallback: (String) -> Unit
) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var dialogo = DatePickerDialog(
            requireContext(),
            R.style.CustomPickerDialogTheme,
            this,
            year,
            month,
            day
        );

        dialogo.setOnShowListener {
            dialogo.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(requireContext(), R.color.cinza_ativo))
            dialogo.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(requireContext(), R.color.cinza_ativo))
        }

        return dialogo
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)

        dateSetCallback(selectedDate)
    }
}