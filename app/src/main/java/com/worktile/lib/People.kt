package com.worktile.lib

import com.worktile.json.annotation.Ignore

/**
 * Created by Android Studio.
 * User: HuBo
 * Email: hubozzz@163.com
 * Date: 2020/10/13
 * Time: 2:31 PM
 * Desc:
 */
class People {
    var name: String? = null

    @Ignore
    var address: String? = null
}