package com.example.prediksipenyakit

import android.os.Parcel
import android.os.Parcelable

data class UserInputModel(
    val age: Int,
    val gender: String,
    val bmi: Float,
    val dailySteps: Int,
    val sleepHours: Float,
    val smoker: Int,
    val alcohol: Int,
    val waterIntake: Float,
    val caloriesConsumed: Int,
    val systolicBp: Int,
    val diastolicBp: Int,
    val heartRate: Int,
    val familyHistory: Int
) : Parcelable {

    // Constructor untuk membaca data dari Parcel (saat diterima)
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    // Fungsi untuk menulis data ke Parcel (saat dikirim)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(age)
        parcel.writeString(gender)
        parcel.writeFloat(bmi)
        parcel.writeInt(dailySteps)
        parcel.writeFloat(sleepHours)
        parcel.writeInt(smoker)
        parcel.writeInt(alcohol)
        parcel.writeFloat(waterIntake)
        parcel.writeInt(caloriesConsumed)
        parcel.writeInt(systolicBp)
        parcel.writeInt(diastolicBp)
        parcel.writeInt(heartRate)
        parcel.writeInt(familyHistory)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInputModel> {
        override fun createFromParcel(parcel: Parcel): UserInputModel {
            return UserInputModel(parcel)
        }

        override fun newArray(size: Int): Array<UserInputModel?> {
            return arrayOfNulls(size)
        }
    }
}