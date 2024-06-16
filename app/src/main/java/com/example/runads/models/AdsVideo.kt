package com.vku.run_ads_taxi.models

class AdsVideo {
    var video_path=""
    var video_name=""
    var video_thumbnail=""
    var video_description =""
    var video_length = ""
    var id = -1

    constructor(
        video_path: String,
        video_name: String,
        video_thumbnail: String,
        video_description: String,
        video_length: String,
        id: Int
    ) {
        this.video_path = video_path
        this.video_name = video_name
        this.video_thumbnail = video_thumbnail
        this.video_description = video_description
        this.video_length = video_length
        this.id = id
    }
}