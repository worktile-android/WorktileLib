package com.worktile.json.annotation

/**
 * Created by Android Studio.
 * User: HuBo
 * Email: hubozzz@163.com
 * Date: 2020/10/12
 * Time: 2:51 PM
 * Desc:
 */
@Target(AnnotationTarget.FIELD)
annotation class SerializedName(val value:String)