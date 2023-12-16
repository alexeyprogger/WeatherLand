package com.example.weatherland

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Подключить местоположение?")
        dialog.setMessage("Местоположение отключено. Включить местоположение?")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Да"){_,_ ->
            listener.onClick(null)
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Нет"){_,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }
    fun searchByNameDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val edName = EditText(context)
        builder.setView(edName)
        val dialog = builder.create()
        dialog.setTitle("Введите название города:")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Показать"){_,_ ->
                listener.onClick(edName.text.toString())
                dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отменить"){_,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener{
        fun onClick(name: String?)
    }
}
