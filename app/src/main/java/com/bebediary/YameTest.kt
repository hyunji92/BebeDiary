package com.hyundeee.app.usersearch

import android.net.Uri
import io.reactivex.subjects.PublishSubject


/**
 * Created by jeonghyeonji on 2017. 8. 8..
 */
open class YameTest {
    companion object {
        val testSubject: PublishSubject<Uri>? = PublishSubject.create()
    }
}
