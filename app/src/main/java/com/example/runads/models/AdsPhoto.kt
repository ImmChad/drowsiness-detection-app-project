package com.vku.run_ads_taxi.models

class AdsPhoto {
    var photo_path=""
    var photo_name=""
    var photo_description =""
    var photo_length = 0
    var id = -1

    constructor(
        photo_path: String,
        photo_name: String,
        photo_description: String,
        photo_length: Int,
        id: Int
    ) {
        this.photo_path = photo_path
        this.photo_name = photo_name
        this.photo_description = photo_description
        this.photo_length = photo_length
        this.id = id
    }
}