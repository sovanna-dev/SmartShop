package com.smartshop.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// This annotation tells Hilt to set up
// dependency injection for the whole app
@HiltAndroidApp
class SmartShopApp : Application()