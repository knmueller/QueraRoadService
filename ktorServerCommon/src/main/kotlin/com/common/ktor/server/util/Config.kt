package com.common.ktor.server.util

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

private val config by lazy { HoconApplicationConfig(ConfigFactory.load()) }

fun getProperty(key: String, default: String) = config.propertyOrNull(key)?.getString() ?: default

fun getProperty(key: String) = config.propertyOrNull(key)?.getString()

fun getPropertyOrThrow(key: String) = getProperty(key) ?: throw IllegalStateException("Missing property $key")