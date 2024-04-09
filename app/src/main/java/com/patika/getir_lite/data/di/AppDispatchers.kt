package com.patika.getir_lite.data.di

import javax.inject.Qualifier

@Suppress("unused")
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val appDispatchers: AppDispatchers)

enum class AppDispatchers { Default, IO, }
