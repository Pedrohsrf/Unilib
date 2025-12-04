package com.example.unilib.Salas.Fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.example.unilib.Classes.Rotinas.getColor
import com.example.unilib.R
import java.util.Calendar

class TimeDialog(
    private val timeSetCallback: (String) -> Unit
) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val is24HourFormat = true

        var dialogo = TimePickerDialog(
            requireContext(),
            R.style.CustomPickerDialogTheme,
            this,
            hour,
            minute,
            is24HourFormat
        )

        dialogo.setOnShowListener {
            dialogo.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(requireContext(), R.color.cinza_ativo))
            dialogo.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(requireContext(), R.color.cinza_ativo))
        }

        return dialogo
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val selectedTime = String.format("%02d:%02d", hourOfDay, minute)

        timeSetCallback(selectedTime)
    }
}