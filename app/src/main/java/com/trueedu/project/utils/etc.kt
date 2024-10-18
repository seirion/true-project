package com.trueedu.project.utils

fun String?.toAccountNumFormat(): String {
    return if (this == null) {
        ""
    } else if (this.length == 10) {
        this.take(8) + "-" + this.drop(8)
    } else {
        this
    }
}