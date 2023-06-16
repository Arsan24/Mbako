package com.bangkit.capstone

object Constanta {
    enum class UserPreferences {
        UserName, UserContact, UserEmail, UserToken, UserLastLogin
    }

    enum class itemsDetail{
        UserProductName, ImageUrl, UserPrice, UploadTime, UserQuantity, ItemId
    }

    const val preferenceName = "Settings"
    const val preferenceDefaultValue = "Not Set"
    const val preferenceDefaultDateValue = "2000/04/30 00:00:00"

    const val TAG_FORGOT_PASS = "TEST_FORGOT_PASSWORD"
    const val TAG_RESET_PASS = "TEST_RESET_PASSWORD"
    const val TAG_SELL_ITEM = "TEST_SELL_ITEMS"
    const val CAMERA_PERMISSION_CODE = 10
    const val LOCATION_PERMISSION_CODE = 30

}