package com.worktile.json

internal const val ERROR_MASK = 0b00000001
const val THROW_NOT_FOUNT_EXCEPTION = 0b00000001

class NotFoundException(message: String) : Exception(message)