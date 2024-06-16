package com.example.runads.models

import com.vku.run_ads_taxi.models.AdsPhoto
import com.vku.run_ads_taxi.models.AdsVideo

class AdsMedia(
    var change_time: Int,
    var video: AdsVideo,
    var photo: AdsPhoto,
    var isLogin: String
) {
}