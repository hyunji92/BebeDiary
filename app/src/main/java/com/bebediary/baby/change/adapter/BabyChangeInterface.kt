package com.bebediary.baby.change.adapter

import com.bebediary.database.entity.Baby

interface BabyChangeInterface {
    fun addBaby()
    fun changeBaby(baby: Baby)
}