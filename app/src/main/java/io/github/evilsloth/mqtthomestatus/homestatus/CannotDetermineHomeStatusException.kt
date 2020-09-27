package io.github.evilsloth.mqtthomestatus.homestatus

import java.lang.RuntimeException

class CannotDetermineHomeStatusException(message: String) : RuntimeException(message)
